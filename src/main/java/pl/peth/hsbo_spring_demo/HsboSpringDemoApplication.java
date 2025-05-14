package pl.peth.hsbo_spring_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HsboSpringDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HsboSpringDemoApplication.class, args);
	}
}
