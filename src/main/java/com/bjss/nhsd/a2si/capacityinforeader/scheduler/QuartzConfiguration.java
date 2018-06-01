package com.bjss.nhsd.a2si.capacityinforeader.scheduler;

import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class QuartzConfiguration {

    /**
     * General Quartz Configuration
     */
    @Value("${org.quartz.scheduler.instanceName}")
    private String instanceName;

    @Value("${org.quartz.scheduler.instanceId}")
    private String instanceId;

    @Value("${org.quartz.threadPool.threadCount}")
    private String threadCount;

    @Value("${org.quartz.jobStore.class}")
    private String jobStoreClass;

    @Value("${org.quartz.jobStore.driverDelegateClass}")
    private String driverDelegateClass;

    @Value("${org.quartz.jobStore.isClustered}")
    private String clustered;

    /**
     * Job Trigger Configuration
     * Ensure the Qualifier is used so a named trigger is created, rather than it trying to create by type.
     */
    // IMPORTANT!!!
    // Each Trigger MUST be manually added as a bean AND added to the scheduler factory bean
    @Autowired
    @Qualifier("dhuFtpJobTrigger")
    SimpleTriggerFactoryBean dhuFtpJobTrigger;

    // IMPORTANT!!!
    // Each Trigger MUST be manually added as a bean AND added to the scheduler factory bean
    @Autowired
    @Qualifier("ekhuftJobTrigger")
    SimpleTriggerFactoryBean ekhuftJobTrigger;

    /**
     * Data Source to Quartz Schema
     */
    @Autowired
    private DataSource dataSource;

    @Bean
    public org.quartz.spi.JobFactory jobFactory(ApplicationContext applicationContext) {

        QuartzJobFactory jobFactory = new QuartzJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    /**
     * Return Scheduler Factory Bean - this is the main Quartz Configuration component
     */
    @DependsOn("flywayInitializer")
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext) {

        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setJobFactory(jobFactory(applicationContext));

        Properties quartzProperties = new Properties();
        quartzProperties.setProperty("org.quartz.scheduler.instanceName",instanceName);
        quartzProperties.setProperty("org.quartz.scheduler.instanceId",instanceId);
        quartzProperties.setProperty("org.quartz.threadPool.threadCount",threadCount);
        quartzProperties.setProperty("org.quartz.jobStore.class", jobStoreClass);
        quartzProperties.setProperty("org.quartz.jobStore.driverDelegateClass", driverDelegateClass);
        quartzProperties.setProperty("org.quartz.jobStore.isClustered", clustered);

        schedulerFactoryBean.setDataSource(dataSource);

        schedulerFactoryBean.setQuartzProperties(quartzProperties);

        // IMPORTANT
        // Add the Job Triggers (the triggers include the job details)
        schedulerFactoryBean.setTriggers(new SimpleTrigger[]{
                dhuFtpJobTrigger.getObject(),
                ekhuftJobTrigger.getObject()
        });

        return schedulerFactoryBean;
    }


}
