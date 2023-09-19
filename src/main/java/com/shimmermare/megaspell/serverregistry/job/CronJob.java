package com.shimmermare.megaspell.serverregistry.job;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated beans of type {@link org.quartz.Job} will be automatically scheduled for execution.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CronJob {
    String cron();
}
