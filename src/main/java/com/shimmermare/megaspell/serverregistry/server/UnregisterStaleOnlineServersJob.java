package com.shimmermare.megaspell.serverregistry.server;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class UnregisterStaleOnlineServersJob implements Job {
    private final ServerRegistryService serverRegistryService;

    public UnregisterStaleOnlineServersJob(ServerRegistryService serverRegistryService) {
        this.serverRegistryService = serverRegistryService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        serverRegistryService.unregisterStaleOnlineServers();
    }
}
