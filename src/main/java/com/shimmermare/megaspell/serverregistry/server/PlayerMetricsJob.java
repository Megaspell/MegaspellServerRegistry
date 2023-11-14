package com.shimmermare.megaspell.serverregistry.server;

import io.micrometer.core.instrument.MeterRegistry;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerMetricsJob implements Job {
    private final OnlineServerRepository onlineServerRepository;
    private final AtomicInteger concurrentOnlinePlayersMetricGauge;

    public PlayerMetricsJob(OnlineServerRepository onlineServerRepository, MeterRegistry meterRegistry) {
        this.onlineServerRepository = onlineServerRepository;
        concurrentOnlinePlayersMetricGauge = meterRegistry.gauge("concurrent_online_players", new AtomicInteger());
    }

    @Override
    public void execute(JobExecutionContext context) {
        int currentOnlinePlayers = onlineServerRepository.countOnlinePlayers();
        concurrentOnlinePlayersMetricGauge.set(currentOnlinePlayers);
    }
}
