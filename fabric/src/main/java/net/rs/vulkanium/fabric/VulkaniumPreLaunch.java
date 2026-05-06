package net.rs.vulkanium.fabric;

import net.rs.vulkanium.client.compatibility.checks.PreLaunchChecks;
import net.rs.vulkanium.client.compatibility.environment.probe.GraphicsAdapterProbe;
import net.rs.vulkanium.client.compatibility.workarounds.Workarounds;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class VulkaniumPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        PreLaunchChecks.checkEnvironment();
        GraphicsAdapterProbe.findAdapters();
        Workarounds.init();
    }
}
