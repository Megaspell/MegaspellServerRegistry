package com.shimmermare.megaspell.serverregistry.server;

import com.shimmermare.megaspell.serverregistry.job.CronJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@CronJob(cron = "0,30 * * * * ?")
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
