package com.shimmermare.megaspell.serverregistry.job;

import com.shimmermare.megaspell.serverregistry.server.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Profile("!test")
@Configuration
public class JobConfig {
    @Bean
    @ConfigurationProperties(prefix = "quartz-spring")
    public Map<String, String> quartzProperties() {
        return new HashMap<>();
    }

    @Bean
    public JobScheduler jobScheduler(
            DataSource dataSource,
            ApplicationContext applicationContext,
            @Qualifier("quartzProperties") Map<String, String> quartzProperties
    ) throws SchedulerException {
        return new JobScheduler(dataSource, applicationContext, quartzProperties);
    }

    @Bean
    public JobAnnotationProcessor jobAnnotationProcessor(
            JobScheduler scheduler,
            ApplicationContext applicationContext
    ) {
        return new JobAnnotationProcessor(scheduler, applicationContext);
    }

    @CronJob(cron = "0,30 * * * * ?")
    @Bean
    public UnregisterStaleOnlineServersJob unregisterStaleOnlineServersJob(
            ServerRegistryService serverRegistryService
    ) {
        return new UnregisterStaleOnlineServersJob(serverRegistryService);
    }

    @CronJob(cron = "0 * * * * ?")
    @Bean
    public RegisteredServersMetricsJob registeredServersMetricsJob(
            OnlineServerRepository onlineServerRepository,
            MeterRegistry meterRegistry
    ) {
        return new RegisteredServersMetricsJob(onlineServerRepository, meterRegistry);
    }

    @CronJob(cron = "0 * * * * ?")
    @Bean
    public PlayerMetricsJob playerMetricsJob(
            OnlineServerRepository onlineServerRepository,
            MeterRegistry meterRegistry
    ) {
        return new PlayerMetricsJob(onlineServerRepository, meterRegistry);
    }
}
