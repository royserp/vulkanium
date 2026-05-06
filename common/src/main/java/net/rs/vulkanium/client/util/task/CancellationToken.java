package net.rs.vulkanium.client.util.task;

public interface CancellationToken {
    boolean isCancelled();

    void setCancelled();
}
