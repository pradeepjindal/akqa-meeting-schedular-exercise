package com.prajin.ie.akqa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class AkqaApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(AkqaApplication.class, args);
		MeetingScheduler meetingScheduler = (MeetingScheduler) context.getBean("meetingScheduler");
		meetingScheduler.schedule(args.length == 0 ? null : args[0]);
	}

}
