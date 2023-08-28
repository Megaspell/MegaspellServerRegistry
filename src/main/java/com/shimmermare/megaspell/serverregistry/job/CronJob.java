package com.shimmermare.megaspell.serverregistry.job;

import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes implementing {@link org.quartz.Job} that are marked with this annotation will be
 * automatically scheduled for execution.
 */
@ConditionalOnBean(Scheduler.class)
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CronJob {
    String cron();
}
