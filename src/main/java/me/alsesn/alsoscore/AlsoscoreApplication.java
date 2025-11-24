package me.alsesn.alsoscore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AlsoscoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlsoscoreApplication.class, args);
	}

}
