package com.pixeldoctrine.torrboll;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@ComponentScan("com.pixeldoctrine")
public class TorrbollApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = run(TorrbollApplication.class, args);
		ScheduledTasks scheduledTasks = ctx.getBean(ScheduledTasks.class);
		scheduledTasks.processEmails();
	}
}
