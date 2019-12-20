package com.pixeldoctrine.torrboll.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import com.pixeldoctrine.torrboll.email.BackupEmailProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private BackupEmailProcessor processor;

	private TaskScheduler scheduler;


	public ScheduledTasks() throws IOException {
		scheduler = new ConcurrentTaskScheduler();
		Properties prop = new Properties();
		prop.load(new FileInputStream("processCron.properties"));
		String processCron = prop.getProperty("process.cron");
		log.info("PROCESS CRON IS: " + processCron + " (restart service after changing properties)");
		CronTrigger cronTrigger = new CronTrigger(processCron);
		scheduler.schedule(new EmailRunner(), cronTrigger);
	}

	private class EmailRunner implements Runnable {
		public void run() {
			log.info("Processing e-mails at {}.", AppConfiguration.TIME_FORMAT.format(ZonedDateTime.now()));
			int numCataloguedEmails = processor.process();
			log.info("{} e-mails catalogued.", numCataloguedEmails);
		}
    }
}
