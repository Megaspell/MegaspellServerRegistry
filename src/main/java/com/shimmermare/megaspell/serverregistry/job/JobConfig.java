package com.shimmermare.megaspell.serverregistry.job;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Profile("!test")
@Configuration
public class JobConfig {
    @Bean
    @ConfigurationProperties(prefix = "quartz-spring")
    public Map<String, String> quartzProperties() {
        return new HashMap<>();
    }

    @Bean(destroyMethod = "shutdown")
    public Scheduler jobScheduler(
            DataSource dataSource,
            ApplicationContext applicationContext,
            @Qualifier("quartzProperties") Map<String, String> quartzProperties
    ) throws SchedulerException {

        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(new ClassPathResource("org/quartz/impl/jdbcjobstore/tables_postgres.sql"));
        databasePopulator.execute(dataSource);

        JobFactory jobFactory = new JobBeanFactory(applicationContext);

        var props = new Properties();
        props.putAll(quartzProperties);

        SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);
        Scheduler scheduler = schedulerFactory.getScheduler();

        scheduler.setJobFactory(jobFactory);
        scheduler.getListenerManager().addJobListener(new JobLogger());

        scheduler.start();

        return scheduler;
    }
}
