package net.rs.vulkanium.service;

import net.rs.vulkanium.client.compatibility.checks.PreLaunchChecks;
import net.rs.vulkanium.client.compatibility.environment.probe.GraphicsAdapterProbe;
import net.rs.vulkanium.client.compatibility.workarounds.Workarounds;
import net.rs.vulkanium.client.compatibility.workarounds.nvidia.NvidiaWorkarounds;
import net.rs.vulkanium.client.compatibility.workarounds.amd.AmdWorkarounds;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;

public class VulkaniumWorkarounds implements GraphicsBootstrapper {
    @Override
    public String name() {
        return "vulkanium";
    }

    @Override
    public void bootstrap(String[] arguments) {
        PreLaunchChecks.checkEnvironment();
        GraphicsAdapterProbe.findAdapters();
        Workarounds.init();

        // Context creation happens earlier on NeoForge, so we need to apply this now
        NvidiaWorkarounds.applyEnvironmentChanges();
        AmdWorkarounds.applyEnvironmentChanges();
    }
}
