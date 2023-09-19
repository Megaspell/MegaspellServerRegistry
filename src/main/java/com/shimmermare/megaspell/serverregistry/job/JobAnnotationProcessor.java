package com.shimmermare.megaspell.serverregistry.job;

import org.quartz.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;

public class JobAnnotationProcessor implements BeanPostProcessor {
    private final JobScheduler scheduler;
    private final ApplicationContext applicationContext;

    public JobAnnotationProcessor(JobScheduler scheduler, ApplicationContext applicationContext) {
        this.scheduler = scheduler;
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        if (!Job.class.isAssignableFrom(clazz)) return bean;

        CronJob cronJob = applicationContext.findAnnotationOnBean(beanName, CronJob.class);
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

        scheduler.scheduleJob(jobDetail, trigger);
        return bean;
    }
}
