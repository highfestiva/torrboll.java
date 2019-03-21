package com.pixeldoctrine.torrboll.main;

import com.pixeldoctrine.torrboll.db.BackupResultRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@ComponentScan("com.pixeldoctrine")
@EnableScheduling
public class TorrbollApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = run(TorrbollApplication.class, args);
		BackupResultRepository repo = ctx.getBean(BackupResultRepository.class);
		repo.init();
	}
}
