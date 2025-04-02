package pl.peth.hsbo_spring_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class HsboSpringDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HsboSpringDemoApplication.class, args);
	}
}
