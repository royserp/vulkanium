package net.rs.vulkanium.neoforge.config;

import net.rs.vulkanium.api.config.ConfigEntryPointForge;
import net.rs.vulkanium.client.VulkaniumClientMod;
import net.rs.vulkanium.client.config.ConfigManager;
import net.rs.vulkanium.client.gui.VulkaniumConfigBuilder;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;

/**
 * Written with help from <a href="https://github.com/KingContaria/vulkanium-fabric/blob/de61e59a369dd8906ddb54050f48c02a29e3f217/neoforge/src/main/java/net/rs/vulkanium/neoforge/gui/VulkaniumConfigIntegrationAPIForge.java">Contaria's implementation of this class</a>.
 */
public class ConfigLoaderForge {
    private static ConfigManager.ModMetadata getModMetadata(String modId) {
        var mod = ModList.get().getModContainerById(modId).orElseThrow(() -> new
                NullPointerException("Mod with id " + modId + " not found in ModList")
        ).getModInfo();
        return new ConfigManager.ModMetadata(mod.getDisplayName(), mod.getVersion().toString());
    }

    public static void collectConfigEntryPoints() {
        ConfigManager.setModInfoFunction(ConfigLoaderForge::getModMetadata);

        // collect entry points from modes that specify it in their properties
        for (IModInfo mod : ModList.get().getMods()) {
            var modId = mod.getModId();

            if (modId.equals("vulkanium")) {
                ConfigManager.registerConfigEntryPoint(VulkaniumConfigBuilder::new, modId);
            } else {
                Object modProperty = mod.getModProperties().get(ConfigManager.CONFIG_ENTRY_POINT_KEY);
                if (modProperty == null) {
                    continue;
                }

                if (!(modProperty instanceof String)) {
                    VulkaniumClientMod.logger().warn("Mod '{}' provided a custom config integration but the value is of the wrong type: {}", modId, modProperty.getClass());
                    continue;
                }

                ConfigManager.registerConfigEntryPoint((String) modProperty, modId);
            }
        }

        // collect entry points from mods that specify it as an annotation
        var entryPointAnnotationType = Type.getType(ConfigEntryPointForge.class);
        for (var scanData : ModList.get().getAllScanData()) {
            for (var annotation : scanData.getAnnotations()) {
                if (annotation.targetType() == ElementType.TYPE && annotation.annotationType().equals(entryPointAnnotationType)) {
                    var className = annotation.clazz().getClassName();
                    var modIdData = annotation.annotationData().get("value");
                    if (modIdData == null) {
                        VulkaniumClientMod.logger().warn("Class '{}' has a vulkanium config api entry point annotation but didn't specify which mod it belongs to with the annotation's default parameter.", className);
                        continue;
                    }

                    var modId = modIdData.toString();
                    if (ModList.get().getModContainerById(modId).isEmpty()) {
                        VulkaniumClientMod.logger().warn("The mod with id '{}' that was provided as the owner of a vulkanium config api entry point annotation on class '{}' doesn't exist.", modId, className);
                        continue;
                    }

                    ConfigManager.registerConfigEntryPoint(className, modId);
                }
            }
        }
    }
}
