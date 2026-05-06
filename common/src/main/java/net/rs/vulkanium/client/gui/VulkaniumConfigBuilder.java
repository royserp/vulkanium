package net.rs.vulkanium.client.gui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.rs.vulkanium.api.config.ConfigEntryPoint;
import net.rs.vulkanium.api.config.ConfigState;
import net.rs.vulkanium.api.config.StorageEventHandler;
import net.rs.vulkanium.api.config.option.OptionFlag;
import net.rs.vulkanium.api.config.option.OptionImpact;
import net.rs.vulkanium.api.config.option.Range;
import net.rs.vulkanium.api.config.structure.*;
import net.rs.vulkanium.client.VulkaniumClientMod;
import net.rs.vulkanium.client.compatibility.environment.OsUtils;
import net.rs.vulkanium.client.compatibility.workarounds.Workarounds;
import net.rs.vulkanium.client.vk.arena.staging.MappedStagingBuffer;
import net.rs.vulkanium.client.vk.device.RenderDevice;
import net.rs.vulkanium.client.gui.options.control.ControlValueFormatterImpls;
import net.rs.vulkanium.client.render.chunk.DeferMode;
import net.rs.vulkanium.client.render.chunk.translucent_sorting.QuadSplittingMode;
import net.rs.vulkanium.client.services.PlatformRuntimeInformation;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.*;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

// TODO: get initialValue from the vanilla options (it's private)
public class VulkaniumConfigBuilder implements ConfigEntryPoint {
    private static final Identifier SODIUM_ICON = Identifier.fromNamespaceAndPath("vulkanium", "textures/gui/config-icon.png");
    private static final VulkaniumOptions DEFAULTS = VulkaniumOptions.defaults();

    private final Options vanillaOpts;
    private final StorageEventHandler vanillaStorage;
    private final VulkaniumOptions vulkaniumOpts;
    private final StorageEventHandler vulkaniumStorage;

    private final @Nullable Window window;

    public VulkaniumConfigBuilder() {
        var minecraft = Minecraft.getInstance();
        this.window = minecraft.getWindow();

        this.vanillaOpts = minecraft.options;
        this.vanillaStorage = this.vanillaOpts == null ? null : () -> {
            this.vanillaOpts.save();

            VulkaniumClientMod.logger().info("Flushed changes to Minecraft configuration");
        };

        this.vulkaniumOpts = VulkaniumClientMod.options();
        this.vulkaniumStorage = () -> {
            try {
                VulkaniumOptions.writeToDisk(this.vulkaniumOpts);
            } catch (IOException e) {
                throw new RuntimeException("Couldn't save configuration changes", e);
            }

            VulkaniumClientMod.logger().info("Flushed changes to Vulkanium configuration");
        };
    }

    private Monitor getMonitor() {
        if (this.window == null) {
            return null;
        }
        return this.window.findBestMonitor();
    }

    public static void registerIcon(TextureManager textureManager) {
        textureManager.registerAndLoad(SODIUM_ICON, new VulkaniumLogo());
    }

    static class VulkaniumLogo extends ReloadableTexture {
        public VulkaniumLogo() {
            super(SODIUM_ICON);
        }

        @Override
        public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
            try (InputStream inputStream = VulkaniumConfigBuilder.class.getResourceAsStream("/config-icon.png")) {
                return new TextureContents(NativeImage.read(inputStream), new TextureMetadataSection(false, false, MipmapStrategy.AUTO, 0.1f));
            }
        }
    }

    @Override
    public void registerConfigEarly(ConfigBuilder builder) {
        new VulkaniumConfigBuilder().buildEarlyConfig(builder);
    }

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        new VulkaniumConfigBuilder().buildFullConfig(builder);
    }

    private static ModOptionsBuilder createModOptionsBuilder(ConfigBuilder builder) {
        return builder.registerOwnModOptions()
                .setName("Vulkanium")
                .setIcon(SODIUM_ICON)
                .formatVersion(version -> {
                    var result = version.splitWithDelimiters("\\+", 2);
                    return result[0];
                });
    }

    private void buildEarlyConfig(ConfigBuilder builder) {
        createModOptionsBuilder(builder).addPage(
                builder.createOptionPage()
                        .setName(Component.translatable("vulkanium.options.pages.performance"))
                        .addOptionGroup(
                                builder.createOptionGroup()
                                        .addOption(this.buildNoErrorContextOption(builder))));
    }

    private void buildFullConfig(ConfigBuilder builder) {
        createModOptionsBuilder(builder)
                .setColorTheme(builder.createColorTheme().setFullThemeRGB(
                        Colors.THEME, Colors.THEME_LIGHTER, Colors.THEME_DARKER))
                .addPage(this.buildGeneralPage(builder))
                .addPage(this.buildQualityPage(builder))
                .addPage(this.buildPerformancePage(builder))
                .addPage(this.buildAdvancedPage(builder));
    }

    private OptionPageBuilder buildGeneralPage(ConfigBuilder builder) {
        var generalPage = builder.createOptionPage().setName(Component.translatable("vulkanium.options.pages.general"));
        generalPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        // TODO: make RD option respect Vanilla's >16 RD only allowed if memory >1GB constraint
                        builder.createIntegerOption(Identifier.parse("vulkanium:general.render_distance"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.renderDistance"))
                                .setTooltip(Component.translatable("vulkanium.options.view_distance.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.translateVariable("options.chunks"))
                                .setRange(2, 32, 1)
                                .setDefaultValue(12)
                                .setBinding(this.vanillaOpts.renderDistance()::set, this.vanillaOpts.renderDistance()::get)
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:general.simulation_distance"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.simulationDistance"))
                                .setTooltip(Component.translatable("vulkanium.options.simulation_distance.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.translateVariable("options.chunks"))
                                .setRange(5, 32, 1)
                                .setDefaultValue(12)
                                .setBinding(this.vanillaOpts.simulationDistance()::set, this.vanillaOpts.simulationDistance()::get)
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:general.fov"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.fov"))
                                .setTooltip(Component.translatable("options.fov.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.number())
                                .setRange(30, 110, 1)
                                .setDefaultValue(70)
                                .setBinding(this.vanillaOpts.fov()::set, this.vanillaOpts.fov()::get)
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:general.gamma"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.gamma"))
                                .setTooltip(Component.translatable("vulkanium.options.brightness.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.brightness())
                                .setRange(0, 100, 1)
                                .setDefaultValue(50)
                                .setBinding(value -> this.vanillaOpts.gamma().set(value * 0.01D), () -> (int) (this.vanillaOpts.gamma().get() / 0.01D))
                )
        );
        generalPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:general.gui_scale"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.guiScale"))
                                .setTooltip(Component.translatable("vulkanium.options.gui_scale.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.guiScale())
                                .setValidatorProvider((state) -> {
                                    var savedValue = state.readIntOption(Identifier.parse("vulkanium:general.gui_scale"));
                                    var realMax = this.window.calculateScale(0, Minecraft.getInstance().isEnforceUnicode());
                                    var presentationMax = Math.max(savedValue, realMax);
                                    return new GUIScaleRange(presentationMax);
                                }, ConfigState.UPDATE_ON_REBUILD, ConfigState.UPDATE_ON_APPLY)
                                .setDefaultValue(0)
                                .setBinding(this.vanillaOpts.guiScale()::set, this.vanillaOpts.guiScale()::get)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:general.fullscreen"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.fullscreen"))
                                .setTooltip(Component.translatable("vulkanium.options.fullscreen.tooltip"))
                                .setDefaultValue(false)
                                .setBinding(value -> {
                                    this.vanillaOpts.fullscreen().set(value);

                                    if (this.window.isFullscreen() != this.vanillaOpts.fullscreen().get()) {
                                        this.window.toggleFullScreen();

                                        // The client might not be able to enter full-screen mode
                                        this.vanillaOpts.fullscreen().set(this.window.isFullscreen());
                                    }
                                }, this.vanillaOpts.fullscreen()::get)
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:general.fullscreen_resolution"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.fullscreen.resolution"))
                                .setTooltip(Component.translatable("vulkanium.options.fullscreen_resolution.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.resolution())
                                // the max value of 1 when the monitor is not available prevents an exception from being thrown
                                .setValidator(new FullscreenResolutionRange())
                                .setDefaultValue(0)
                                .setBinding(value -> {
                                    var monitor = this.getMonitor();
                                    if (monitor != null) {
                                        this.window.setPreferredFullscreenVideoMode(0 == value ? Optional.empty() : Optional.of(monitor.getMode(value - 1)));
                                    }
                                }, () -> {
                                    var monitor = this.getMonitor();
                                    if (monitor == null) {
                                        return 0;
                                    } else {
                                        Optional<VideoMode> optional = this.window.getPreferredFullscreenVideoMode();
                                        return optional.map((videoMode) -> monitor.getVideoModeIndex(videoMode) + 1).orElse(0);
                                    }
                                })
                                .setEnabledProvider(
                                        (state) -> {
                                            var monitor = this.getMonitor();
                                            if (monitor == null || monitor.getModeCount() <= 0) {
                                                return false;
                                            }
                                            var os = OsUtils.getOs();
                                            return (os == OsUtils.OperatingSystem.WIN || os == OsUtils.OperatingSystem.MAC) &&
                                                    state.readBooleanOption(Identifier.parse("vulkanium:general.fullscreen"));
                                        },
                                        Identifier.parse("vulkanium:general.fullscreen"))
                                .setFlags(OptionFlag.REQUIRES_VIDEOMODE_RELOAD)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:general.vsync"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.vsync"))
                                .setTooltip(Component.translatable("vulkanium.options.v_sync.tooltip"))
                                .setDefaultValue(true)
                                .setBinding(this.vanillaOpts.enableVsync()::set, this.vanillaOpts.enableVsync()::get)
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:general.framerate_limit"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.framerateLimit"))
                                .setTooltip(Component.translatable("vulkanium.options.fps_limit.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.fpsLimit())
                                .setRange(10, 260, 10)
                                .setDefaultValue(60)
                                .setBinding(this.vanillaOpts.framerateLimit()::set, this.vanillaOpts.framerateLimit()::get)
                )
                .addOption(
                        builder.createEnumOption(Identifier.parse("vulkanium:general.inactivity_fps_limit"), InactivityFpsLimit.class)
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.inactivityFpsLimit"))
                                .setTooltip(Component.translatable("options.inactivityFpsLimit.tooltip"))
                                .setDefaultValue(InactivityFpsLimit.AFK)
                                .setElementNameProvider(value -> Component.translatable("options.inactivityFpsLimit." + value.getSerializedName()))
                                .setBinding(this.vanillaOpts.inactivityFpsLimit()::set, this.vanillaOpts.inactivityFpsLimit()::get)
                )
        );
        generalPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createEnumOption(Identifier.parse("vulkanium:general.preferred_graphics_backend"), net.minecraft.client.PreferredGraphicsApi.class)
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.preferredGraphicsBackend"))
                                .setTooltip(Component.translatable("options.preferredGraphicsBackend.tooltip"))
                                .setDefaultValue(net.minecraft.client.PreferredGraphicsApi.DEFAULT)
                                .setElementNameProvider(value -> Component.translatable("options.graphicsApi." + value.getSerializedName()))
                                .setBinding(this.vanillaOpts.preferredGraphicsBackend()::set, this.vanillaOpts.preferredGraphicsBackend()::get)
                                .setFlags(OptionFlag.REQUIRES_GAME_RESTART)
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:general.menu_blur"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.menu_background_blurriness"))
                                .setTooltip(Component.translatable("options.menu_background_blurriness.tooltip"))
                                .setValueFormatter(ControlValueFormatterImpls.number())
                                .setRange(0, 10, 1)
                                .setDefaultValue(5)
                                .setBinding(this.vanillaOpts.menuBackgroundBlurriness()::set, this.vanillaOpts.menuBackgroundBlurriness()::get)
                )
                .addOption(
                        builder.createEnumOption(Identifier.parse("vulkanium:general.attack_indicator"), AttackIndicatorStatus.class)
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.attackIndicator"))
                                .setTooltip(Component.translatable("vulkanium.options.attack_indicator.tooltip"))
                                .setDefaultValue(AttackIndicatorStatus.CROSSHAIR)
                                .setElementNameProvider(AttackIndicatorStatus::caption)
                                .setBinding(this.vanillaOpts.attackIndicator()::set, this.vanillaOpts.attackIndicator()::get)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:general.autosave_indicator"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.autosaveIndicator"))
                                .setTooltip(Component.translatable("vulkanium.options.autosave_indicator.tooltip"))
                                .setDefaultValue(true)
                                .setBinding(this.vanillaOpts.showAutosaveIndicator()::set, this.vanillaOpts.showAutosaveIndicator()::get)
                )
        );
        return generalPage;
    }

    private OptionPageBuilder buildQualityPage(ConfigBuilder builder) {
        var qualityPage = builder.createOptionPage().setName(Component.translatable("vulkanium.options.pages.quality"));

        qualityPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createEnumOption(Identifier.parse("vulkanium:quality.preset"), net.minecraft.client.GraphicsPreset.class)
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.graphics"))
                                .setTooltip(Component.translatable("options.graphics.tooltip"))
                                .setDefaultValue(net.minecraft.client.GraphicsPreset.FANCY)
                                .setElementNameProvider(value -> Component.translatable("options.graphics." + value.getSerializedName()))
                                .setBinding(value -> {
                                    value.apply(Minecraft.getInstance());
                                }, () -> this.vanillaOpts.graphicsPreset().get())
                                .setImpact(OptionImpact.VARIES)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:quality.graphics"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.improvedTransparency"))
                                .setTooltip(Component.translatable("options.improvedTransparency.tooltip"))
                                .setDefaultValue(false)
                                .setBinding(this.vanillaOpts.improvedTransparency()::set, this.vanillaOpts.improvedTransparency()::get)
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
        );

        qualityPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createEnumOption(Identifier.parse("vulkanium:quality.clouds"), CloudStatus.class)
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.renderClouds"))
                                .setTooltip(Component.translatable("vulkanium.options.clouds_quality.tooltip"))
                                .setElementNameProvider(EnumOptionBuilder.nameProviderFrom(
                                        Component.translatable("options.off"),
                                        Component.translatable("options.clouds.fast"),
                                        Component.translatable("options.clouds.fancy")))
                                .setDefaultValue(CloudStatus.FANCY)
                                .setBinding((value) -> {
                                    this.vanillaOpts.cloudStatus().set(value);

                                    if (Minecraft.getInstance().gameRenderer.gameRenderState().useShaderTransparency()) {
                                        RenderTarget framebuffer = Minecraft.getInstance().levelRenderer.cloudsTarget();
                                        if (framebuffer != null) {
                                            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(framebuffer.getColorTexture(), 0xFFFFFFFF, framebuffer.getDepthTexture(), 1.0f);
                                        }
                                    }
                                }, () -> this.vanillaOpts.cloudStatus().get())
                                .setImpact(OptionImpact.LOW)
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.render_cloud_distance"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.renderCloudsDistance"))
                                .setTooltip(Component.translatable("vulkanium.options.clouds_distance.tooltip"))
                                .setRange(2, 128, 2)
                                .setDefaultValue(128)
                                .setBinding((value) -> {
                                    this.vanillaOpts.cloudRange().set(value);

                                    Minecraft.getInstance().levelExtractor.allChanged();
                                }, () -> this.vanillaOpts.cloudRange().get())
                                .setImpact(OptionImpact.LOW)
                                .setValueFormatter(ControlValueFormatterImpls.translateVariable("options.chunks"))
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.weather"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.weatherRadius"))
                                .setTooltip(Component.translatable("options.weatherRadius.tooltip"))
                                .setDefaultValue(10)
                                .setRange(new Range(3, 10, 1))
                                .setValueFormatter(ControlValueFormatterImpls.number())
                                .setBinding(this.vanillaOpts.weatherRadius()::set, this.vanillaOpts.weatherRadius()::get)
                                .setImpact(OptionImpact.LOW)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:quality.leaves"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.cutoutLeaves"))
                                .setTooltip(Component.translatable("options.cutoutLeaves.tooltip"))
                                .setDefaultValue(true)
                                .setBinding(this.vanillaOpts.cutoutLeaves()::set, this.vanillaOpts.cutoutLeaves()::get)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createEnumOption(Identifier.parse("vulkanium:quality.particles"), ParticleStatus.class)
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.particles"))
                                .setTooltip(Component.translatable("vulkanium.options.particle_quality.tooltip"))
                                .setElementNameProvider(EnumOptionBuilder.nameProviderFrom(
                                        Component.translatable("options.particles.all"),
                                        Component.translatable("options.particles.decreased"),
                                        Component.translatable("options.particles.minimal")
                                ))
                                .setDefaultValue(ParticleStatus.ALL)
                                .setBinding(this.vanillaOpts.particles()::set, this.vanillaOpts.particles()::get)
                                .setImpact(OptionImpact.MEDIUM)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:quality.ao"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.ao"))
                                .setTooltip(Component.translatable("vulkanium.options.smooth_lighting.tooltip"))
                                .setDefaultValue(true)
                                .setBinding(this.vanillaOpts.ambientOcclusion()::set, this.vanillaOpts.ambientOcclusion()::get)
                                .setImpact(OptionImpact.LOW)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.biome_blend"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.biomeBlendRadius"))
                                .setValueFormatter(ControlValueFormatterImpls.biomeBlend())
                                .setTooltip(Component.translatable("vulkanium.options.biome_blend.tooltip"))
                                .setRange(0, 7, 1)
                                .setDefaultValue(2)
                                .setBinding(this.vanillaOpts.biomeBlendRadius()::set, this.vanillaOpts.biomeBlendRadius()::get)
                                .setImpact(OptionImpact.LOW)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.entity_distance"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.entityDistanceScaling"))
                                .setValueFormatter(ControlValueFormatterImpls.percentage())
                                .setTooltip(Component.translatable("vulkanium.options.entity_distance.tooltip"))
                                .setRange(50, 500, 25)
                                .setDefaultValue(100)
                                .setBinding((value) -> this.vanillaOpts.entityDistanceScaling().set(value / 100.0), () -> Math.round(this.vanillaOpts.entityDistanceScaling().get().floatValue() * 100.0F))
                                .setImpact(OptionImpact.HIGH)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:quality.entity_shadows"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.entityShadows"))
                                .setTooltip(Component.translatable("vulkanium.options.entity_shadows.tooltip"))
                                .setDefaultValue(true)
                                .setBinding(this.vanillaOpts.entityShadows()::set, this.vanillaOpts.entityShadows()::get)
                                .setImpact(OptionImpact.MEDIUM)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:quality.vignette"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.vignette"))
                                .setTooltip(Component.translatable("options.vignette.tooltip"))
                                .setDefaultValue(true)
                                .setBinding(this.vanillaOpts.vignette()::set, this.vanillaOpts.vignette()::get)
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.fade_time"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.chunkFade"))
                                .setTooltip(Component.translatable("options.chunkFade.tooltip"))
                                .setDefaultValue(750)
                                .setValueFormatter(ControlValueFormatterImpls.chunkFade())
                                .setRange(new Range(0, 2000, 50))
                                .setBinding(fade -> this.vanillaOpts.chunkSectionFadeInTime().set((double) fade / 1000.0), () -> (int) (this.vanillaOpts.chunkSectionFadeInTime().get() * 1000.0))
                )
        );

        qualityPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.mipmap_levels"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.mipmapLevels"))
                                .setValueFormatter(ControlValueFormatterImpls.multiplier())
                                .setTooltip(Component.translatable("vulkanium.options.mipmap_levels.tooltip"))
                                .setRange(0, 4, 1)
                                .setDefaultValue(4)
                                .setBinding(this.vanillaOpts.mipmapLevels()::set, this.vanillaOpts.mipmapLevels()::get)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                )
        );

        qualityPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createEnumOption(Identifier.parse("vulkanium:quality.filtering_mode"), TextureFilteringMethod.class)
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.textureFiltering"))
                                .setTooltip(i -> Component.translatable("options.textureFiltering." + i.name().toLowerCase(Locale.ROOT) + ".tooltip"))
                                .setElementNameProvider(name -> {
                                    return Component.translatable("options.textureFiltering." + name.name().toLowerCase(Locale.ROOT));
                                })
                                .setDefaultValue(TextureFilteringMethod.RGSS)
                                .setBinding(this.vanillaOpts.textureFiltering()::set, this.vanillaOpts.textureFiltering()::get)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.anisotropy_bit"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.maxAnisotropy"))
                                .setRange(new Range(0, 3, 1))
                                .setTooltip(Component.translatable("options.maxAnisotropy.tooltip"))
                                .setDefaultValue(0)
                                .setValueFormatter(ControlValueFormatterImpls.anisotropyBit())
                                .setBinding(this.vanillaOpts.maxAnisotropyBit()::set, this.vanillaOpts.maxAnisotropyBit()::get)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                .setEnabledProvider(i -> {
                                    return i.readEnumOption(Identifier.parse("vulkanium:quality.filtering_mode"), TextureFilteringMethod.class) == TextureFilteringMethod.ANISOTROPIC;
                                }, Identifier.parse("vulkanium:quality.filtering_mode"))
                )
        );

        qualityPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.fov_effect_scale"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.fovEffectScale"))
                                .setTooltip(Component.translatable("options.fovEffectScale.tooltip"))
                                .setRange(0, 100, 1)
                                .setDefaultValue(100)
                                .setValueFormatter(ControlValueFormatterImpls.percentage())
                                .setBinding(value -> this.vanillaOpts.fovEffectScale().set(value / 100.0), () -> (int) (this.vanillaOpts.fovEffectScale().get() * 100.0))
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.screen_effect_scale"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.screenEffectScale"))
                                .setTooltip(Component.translatable("options.screenEffectScale.tooltip"))
                                .setRange(0, 100, 1)
                                .setDefaultValue(100)
                                .setValueFormatter(ControlValueFormatterImpls.percentage())
                                .setBinding(value -> this.vanillaOpts.screenEffectScale().set(value / 100.0), () -> (int) (this.vanillaOpts.screenEffectScale().get() * 100.0))
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.darkness_effect_scale"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.darknessEffectScale"))
                                .setTooltip(Component.translatable("options.darknessEffectScale.tooltip"))
                                .setRange(0, 100, 1)
                                .setDefaultValue(100)
                                .setValueFormatter(ControlValueFormatterImpls.percentage())
                                .setBinding(value -> this.vanillaOpts.darknessEffectScale().set(value / 100.0), () -> (int) (this.vanillaOpts.darknessEffectScale().get() * 100.0))
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.glint_speed"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.glintSpeed"))
                                .setTooltip(Component.translatable("options.glintSpeed.tooltip"))
                                .setRange(0, 100, 1)
                                .setDefaultValue(50)
                                .setValueFormatter(ControlValueFormatterImpls.percentage())
                                .setBinding(value -> this.vanillaOpts.glintSpeed().set(value / 100.0), () -> (int) (this.vanillaOpts.glintSpeed().get() * 100.0))
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.glint_strength"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.glintStrength"))
                                .setTooltip(Component.translatable("options.glintStrength.tooltip"))
                                .setRange(0, 100, 1)
                                .setDefaultValue(75)
                                .setValueFormatter(ControlValueFormatterImpls.percentage())
                                .setBinding(value -> this.vanillaOpts.glintStrength().set(value / 100.0), () -> (int) (this.vanillaOpts.glintStrength().get() * 100.0))
                )
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:quality.damage_tilt_strength"))
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.damageTiltStrength"))
                                .setTooltip(Component.translatable("options.damageTiltStrength.tooltip"))
                                .setRange(0, 100, 1)
                                .setDefaultValue(100)
                                .setValueFormatter(ControlValueFormatterImpls.percentage())
                                .setBinding(value -> this.vanillaOpts.damageTiltStrength().set(value / 100.0), () -> (int) (this.vanillaOpts.damageTiltStrength().get() * 100.0))
                )
        );
        return qualityPage;
    }

    private OptionPageBuilder buildPerformancePage(ConfigBuilder builder) {
        var performancePage = builder.createOptionPage().setName(Component.translatable("vulkanium.options.pages.performance"));

        performancePage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:performance.chunk_update_threads"))
                                .setStorageHandler(this.vulkaniumStorage)
                                .setName(Component.translatable("vulkanium.options.chunk_update_threads.name"))
                                .setValueFormatter(ControlValueFormatterImpls.quantityOrDisabled(
                                        (v) -> Component.translatable("vulkanium.options.chunk_update_threads.value", v),
                                        Component.translatable("vulkanium.options.default")
                                ))
                                .setTooltip(Component.translatable("vulkanium.options.chunk_update_threads.tooltip"))
                                .setRange(0, Runtime.getRuntime().availableProcessors(), 1)
                                .setDefaultValue(DEFAULTS.performance.chunkBuilderThreads)
                                .setBinding(value -> this.vulkaniumOpts.performance.chunkBuilderThreads = value, () -> this.vulkaniumOpts.performance.chunkBuilderThreads)
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createEnumOption(Identifier.parse("vulkanium:performance.always_defer_chunk_updates"), DeferMode.class)
                                .setStorageHandler(this.vulkaniumStorage)
                                .setName(Component.translatable("vulkanium.options.defer_chunk_updates.name"))
                                .setTooltip(Component.translatable("vulkanium.options.defer_chunk_updates.tooltip"))
                                .setDefaultValue(DEFAULTS.performance.chunkBuildDeferMode)
                                .setBinding(value -> this.vulkaniumOpts.performance.chunkBuildDeferMode = value, () -> this.vulkaniumOpts.performance.chunkBuildDeferMode)
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_UPDATE)
                )
                .addOption(
                        builder.createEnumOption(Identifier.parse("vulkanium:performance.prioritize_chunk_updates"), net.minecraft.client.PrioritizeChunkUpdates.class)
                                .setStorageHandler(this.vanillaStorage)
                                .setName(Component.translatable("options.prioritizeChunkUpdates"))
                                .setTooltip(Component.translatable("options.prioritizeChunkUpdates.tooltip"))
                                .setDefaultValue(net.minecraft.client.PrioritizeChunkUpdates.NONE)
                                .setElementNameProvider(net.minecraft.client.PrioritizeChunkUpdates::caption)
                                .setBinding(this.vanillaOpts.prioritizeChunkUpdates()::set, this.vanillaOpts.prioritizeChunkUpdates()::get)
                                .setImpact(OptionImpact.HIGH)
                )
        );

        performancePage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:performance.use_block_face_culling"))
                                .setStorageHandler(this.vulkaniumStorage)
                                .setName(Component.translatable("vulkanium.options.use_block_face_culling.name"))
                                .setTooltip(Component.translatable("vulkanium.options.use_block_face_culling.tooltip"))
                                .setDefaultValue(DEFAULTS.performance.useBlockFaceCulling)
                                .setBinding(value -> this.vulkaniumOpts.performance.useBlockFaceCulling = value, () -> this.vulkaniumOpts.performance.useBlockFaceCulling)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:performance.use_fog_occlusion"))
                                .setStorageHandler(this.vulkaniumStorage)
                                .setName(Component.translatable("vulkanium.options.use_fog_occlusion.name"))
                                .setTooltip(Component.translatable("vulkanium.options.use_fog_occlusion.tooltip"))
                                .setDefaultValue(DEFAULTS.performance.useFogOcclusion)
                                .setBinding(value -> this.vulkaniumOpts.performance.useFogOcclusion = value, () -> this.vulkaniumOpts.performance.useFogOcclusion)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_UPDATE)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:performance.use_entity_culling"))
                                .setStorageHandler(this.vulkaniumStorage)
                                .setName(Component.translatable("vulkanium.options.use_entity_culling.name"))
                                .setTooltip(Component.translatable("vulkanium.options.use_entity_culling.tooltip"))
                                .setDefaultValue(DEFAULTS.performance.useEntityCulling)
                                .setBinding(value -> this.vulkaniumOpts.performance.useEntityCulling = value, () -> this.vulkaniumOpts.performance.useEntityCulling)
                                .setImpact(OptionImpact.MEDIUM)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:performance.animate_only_visible_textures"))
                                .setStorageHandler(this.vulkaniumStorage)
                                .setName(Component.translatable("vulkanium.options.animate_only_visible_textures.name"))
                                .setTooltip(Component.translatable("vulkanium.options.animate_only_visible_textures.tooltip"))
                                .setDefaultValue(DEFAULTS.performance.animateOnlyVisibleTextures)
                                .setBinding(value -> this.vulkaniumOpts.performance.animateOnlyVisibleTextures = value, () -> this.vulkaniumOpts.performance.animateOnlyVisibleTextures)
                                .setImpact(OptionImpact.HIGH)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_UPDATE)
                )
                .addOption(
                        this.buildNoErrorContextOption(builder)
                )
        );

        if (PlatformRuntimeInformation.getInstance().isDevelopmentEnvironment()) {
            performancePage.addOptionGroup(builder.createOptionGroup()
                    .addOption(
                            builder.createEnumOption(Identifier.parse("vulkanium:performance.quad_splitting"), QuadSplittingMode.class)
                                    .setStorageHandler(this.vulkaniumStorage)
                                    .setName(Component.translatable("vulkanium.options.quad_splitting.name"))
                                    .setTooltip(Component.translatable("vulkanium.options.quad_splitting.tooltip"))
                                    .setImpact(OptionImpact.MEDIUM)
                                    .setDefaultValue(DEFAULTS.performance.quadSplittingMode)
                                    .setBinding(value -> this.vulkaniumOpts.performance.quadSplittingMode = value, () -> this.vulkaniumOpts.performance.quadSplittingMode)
                                    .setEnabled(VulkaniumClientMod.options().debug.terrainSortingEnabled)
                                    .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                    )
            );
        }
        return performancePage;
    }

    private OptionBuilder buildNoErrorContextOption(ConfigBuilder builder) {
        return builder.createBooleanOption(Identifier.parse("vulkanium:performance.use_no_error_context"))
                .setStorageHandler(this.vulkaniumStorage)
                .setName(Component.translatable("vulkanium.options.use_no_error_context.name"))
                .setTooltip(Component.translatable("vulkanium.options.use_no_error_context.tooltip"))
                .setDefaultValue(DEFAULTS.performance.useNoErrorGLContext)
                .setBinding(value -> this.vulkaniumOpts.performance.useNoErrorGLContext = value, () -> this.vulkaniumOpts.performance.useNoErrorGLContext)
                .setEnabledProvider((state) -> {
                    return !Workarounds.isWorkaroundEnabled(Workarounds.Reference.NO_ERROR_CONTEXT_UNSUPPORTED);
                })
                .setImpact(OptionImpact.LOW)
                .setFlags(OptionFlag.REQUIRES_GAME_RESTART);
    }

    private OptionPageBuilder buildAdvancedPage(ConfigBuilder builder) {
        var advancedPage = builder.createOptionPage().setName(Component.translatable("vulkanium.options.pages.advanced"));

        advancedPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(Identifier.parse("vulkanium:advanced.use_persistent_mapping"))
                                .setStorageHandler(this.vulkaniumStorage)
                                .setName(Component.translatable("vulkanium.options.use_persistent_mapping.name"))
                                .setTooltip(Component.translatable("vulkanium.options.use_persistent_mapping.tooltip"))
                                .setDefaultValue(DEFAULTS.advanced.useAdvancedStagingBuffers)
                                .setBinding(value -> this.vulkaniumOpts.advanced.useAdvancedStagingBuffers = value, () -> this.vulkaniumOpts.advanced.useAdvancedStagingBuffers)
                                .setImpact(OptionImpact.MEDIUM)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                )
        );

        advancedPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createIntegerOption(Identifier.parse("vulkanium:advanced.cpu_render_ahead_limit"))
                                .setStorageHandler(this.vulkaniumStorage)
                                .setName(Component.translatable("vulkanium.options.cpu_render_ahead_limit.name"))
                                .setValueFormatter(ControlValueFormatterImpls.translateVariable("vulkanium.options.cpu_render_ahead_limit.value"))
                                .setTooltip(Component.translatable("vulkanium.options.cpu_render_ahead_limit.tooltip"))
                                .setRange(0, 9, 1)
                                .setDefaultValue(DEFAULTS.advanced.cpuRenderAheadLimit)
                                .setBinding(value -> this.vulkaniumOpts.advanced.cpuRenderAheadLimit = value, () -> this.vulkaniumOpts.advanced.cpuRenderAheadLimit)
                )
        );
        return advancedPage;
    }

}
