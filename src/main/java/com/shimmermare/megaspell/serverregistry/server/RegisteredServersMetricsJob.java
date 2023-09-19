package com.shimmermare.megaspell.serverregistry.server;

import com.shimmermare.megaspell.serverregistry.job.CronJob;
import io.micrometer.core.instrument.MeterRegistry;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.concurrent.atomic.AtomicInteger;

@CronJob(cron = "0 * * * * ?")
public class RegisteredServersMetricsJob implements Job {
    private final OnlineServerRepository onlineServerRepository;
    private final AtomicInteger registeredServersMetricGauge;

    public RegisteredServersMetricsJob(OnlineServerRepository onlineServerRepository, MeterRegistry meterRegistry) {
        this.onlineServerRepository = onlineServerRepository;
        registeredServersMetricGauge = meterRegistry.gauge("current_registered_servers", new AtomicInteger());
    }

    @Override
    public void execute(JobExecutionContext context) {
        int registeredServers = onlineServerRepository.count();
        registeredServersMetricGauge.set(registeredServers);
    }
}
