package com.shimmermare.megaspell.serverregistry.job;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * By default, Quartz creates a new instance of job for each execution.
 * To allow for easy DI jobs now are reusable singleton beans.
 */
public class JobBeanFactory implements JobFactory {
    private final ApplicationContext applicationContext;

    private final Map<String, Job> jobBeansCache = new HashMap<>();

    public JobBeanFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) {

        JobDetail jobDetail = bundle.getJobDetail();

        return jobBeansCache.computeIfAbsent(
                jobDetail.getKey().getName(),
                (name) -> (Job) applicationContext.getBean(name)
        );
    }
}
