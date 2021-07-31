package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.app.Root;

import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource({
		"classpath:application.properties",
})
public class SpringCDK implements CommandLineRunner {

	public static void main(final String[] args) {
		SpringApplication.run(SpringCDK.class, args);
	}

	@Override
	public void run(String... args) {
		new Root();
	}
}
