package com.shimmermare.megaspell.serverregistry.job;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

public class JobScheduler implements DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);

    private final Scheduler scheduler;

    public JobScheduler(
            DataSource dataSource,
            ApplicationContext applicationContext,
            Map<String, String> quartzProperties
    ) throws SchedulerException {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(new ClassPathResource("org/quartz/impl/jdbcjobstore/tables_postgres.sql"));
        databasePopulator.execute(dataSource);

        JobFactory jobFactory = new JobBeanFactory(applicationContext);

        var props = new Properties();
        props.putAll(quartzProperties);

        SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);

        scheduler = schedulerFactory.getScheduler();

        scheduler.setJobFactory(jobFactory);
        scheduler.getListenerManager().addJobListener(new JobLogger());
    }

    public void scheduleJob(JobDetail jobDetail, CronTrigger trigger) {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            LOGGER.debug("Scheduled job {} with trigger {}", jobDetail.getKey().getName(), trigger.getCronExpression());
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to initialize job " + jobDetail.getKey().getName() + " schedule", e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void Start() throws SchedulerException {
        scheduler.start();
    }

    @Override
    public void destroy() throws Exception {
        scheduler.shutdown(true);
    }
}
