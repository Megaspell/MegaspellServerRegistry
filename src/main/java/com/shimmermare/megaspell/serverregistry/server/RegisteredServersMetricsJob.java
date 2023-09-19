package com.shimmermare.megaspell.serverregistry.server;

import io.micrometer.core.instrument.MeterRegistry;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.concurrent.atomic.AtomicInteger;

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
