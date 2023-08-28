package com.shimmermare.megaspell.serverregistry.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobLogger implements JobListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobLogger.class);

    @Override
    public String getName() {
        return "jobLogger";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        JobKey jobKey = context.getJobDetail().getKey();
        LOGGER.info("Job {} ({}): executing", jobKey.getName(), jobKey.getGroup());
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        JobKey jobKey = context.getJobDetail().getKey();
        LOGGER.info("Job {} ({}): execution cancelled", jobKey.getName(), jobKey.getGroup());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobKey jobKey = context.getJobDetail().getKey();
        if (jobException == null) {
            LOGGER.info("Job {} ({}): successfully executed in {}ms", jobKey.getName(), jobKey.getGroup(), context.getJobRunTime());
        } else {
            LOGGER.error("Job {} ({}): execution failed with exception (refire: {})", jobKey.getName(),
                    jobKey.getGroup(), jobException.refireImmediately(), jobException);
        }
    }
}
