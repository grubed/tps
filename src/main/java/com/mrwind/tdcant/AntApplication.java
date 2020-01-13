package com.mrwind.tdcant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication

@EnableAsync
public class AntApplication {

	public static void main(String[] args) {
		SpringApplication.run(AntApplication.class, args);
	}

}
