package com.pixeldoctrine.torrboll.main;

import com.pixeldoctrine.torrboll.email.BackupEmailProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private BackupEmailProcessor processor;

    @Scheduled(cron = "0 0 11 * * *") // 11:00 every day
    public void processEmails() {
        log.info("Processing e-mails at {}.", AppConfiguration.DATE_FORMAT.format(new Date()));
		int numCataloguedEmails = processor.process();
        log.info("{} e-mails catalogued.", numCataloguedEmails);
    }
}
