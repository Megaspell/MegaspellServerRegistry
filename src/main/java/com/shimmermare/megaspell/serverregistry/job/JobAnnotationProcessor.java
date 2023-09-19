package com.shimmermare.megaspell.serverregistry.job;

import org.quartz.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class JobAnnotationProcessor implements BeanPostProcessor {
    private final Scheduler scheduler;

    public JobAnnotationProcessor(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        if (!Job.class.isAssignableFrom(clazz)) return bean;

        CronJob cronJob = clazz.getAnnotation(CronJob.class);
        if (cronJob == null) return bean;

        @SuppressWarnings("unchecked")
        JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) clazz)
                .withIdentity(beanName)
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(beanName + "-trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronJob.cron()))
                .forJob(beanName)
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to initialize job " + beanName + " schedule", e);
        }
        return bean;
    }
}
