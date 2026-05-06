package net.rs.vulkanium.client;

import net.rs.vulkanium.client.config.ConfigManager;
import net.rs.vulkanium.client.console.Console;
import net.rs.vulkanium.client.console.message.MessageLevel;
import net.rs.vulkanium.client.data.fingerprint.FingerprintMeasure;
import net.rs.vulkanium.client.data.fingerprint.HashedFingerprint;
import net.rs.vulkanium.client.gui.VulkaniumDebugEntry;
import net.rs.vulkanium.client.gui.VulkaniumOptions;
import net.rs.vulkanium.client.services.PlatformRuntimeInformation;
import net.rs.vulkanium.mixin.features.gui.hooks.debug.DebugScreenEntriesAccessor;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class VulkaniumClientMod {
    private static VulkaniumOptions OPTIONS;
    private static final Logger LOGGER = LoggerFactory.getLogger("Vulkanium");
    public static final Identifier SODIUM_DEBUG_ENTRY_FULL = Identifier.fromNamespaceAndPath("vulkanium", "debug_full");
    public static final Identifier SODIUM_DEBUG_ENTRY_REDUCED = Identifier.fromNamespaceAndPath("vulkanium", "debug_reduced");

    private static String MOD_VERSION;

    public static void onInitialization(String version) {
        var entries = DebugScreenEntriesAccessor.getEntries();
        entries.put(SODIUM_DEBUG_ENTRY_FULL, new VulkaniumDebugEntry(true));
        entries.put(SODIUM_DEBUG_ENTRY_REDUCED, new VulkaniumDebugEntry(false));

        MOD_VERSION = version;

        OPTIONS = loadConfig();

        ConfigManager.registerConfigsEarly();

        try {
            updateFingerprint();
        } catch (Throwable t) {
            LOGGER.error("Failed to update fingerprint", t);
        }
    }

    public static VulkaniumOptions options() {
        if (OPTIONS == null) {
            throw new IllegalStateException("Config not yet available");
        }

        return OPTIONS;
    }

    public static Logger logger() {
        if (LOGGER == null) {
            throw new IllegalStateException("Logger not yet available");
        }

        return LOGGER;
    }

    private static VulkaniumOptions loadConfig() {
        try {
            return VulkaniumOptions.loadFromDisk();
        } catch (Exception e) {
            LOGGER.error("Failed to load configuration file", e);
            LOGGER.error("Using default configuration file in read-only mode");

            Console.instance().logMessage(MessageLevel.SEVERE, "vulkanium.console.config_not_loaded", true, 12.5);

            var config = VulkaniumOptions.defaults();
            config.setReadOnly();

            return config;
        }
    }

    public static void restoreDefaultOptions() {
        OPTIONS = VulkaniumOptions.defaults();

        try {
            VulkaniumOptions.writeToDisk(OPTIONS);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config file", e);
        }
    }

    public static String getVersion() {
        if (MOD_VERSION == null) {
            throw new NullPointerException("Mod version hasn't been populated yet");
        }

        return MOD_VERSION;
    }

    private static void updateFingerprint() {
        var current = FingerprintMeasure.create();

        if (current == null) {
            return;
        }

        HashedFingerprint saved = null;

        try {
            saved = HashedFingerprint.loadFromDisk();
        } catch (Throwable t) {
            LOGGER.error("Failed to load existing fingerprint",  t);
        }

        if (saved == null || !current.looselyMatches(saved)) {
            HashedFingerprint.writeToDisk(current.hashed());

            OPTIONS.notifications.hasSeenDonationPrompt = false;
            OPTIONS.notifications.hasClearedDonationButton = false;

            try {
                VulkaniumOptions.writeToDisk(OPTIONS);
            } catch (IOException e) {
                LOGGER.error("Failed to update config file", e);
            }
        }
    }

    public static boolean allowDebuggingOptions() {
        return PlatformRuntimeInformation.getInstance().isDevelopmentEnvironment();
    }
}
