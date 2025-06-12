package com.Interview.AiAgent;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class AiAgentApplication {

	public static void main(String[] args) {
		// Load .env once
		Dotenv dotenv = Dotenv.configure()
				.filename(".env") // optional, default is ".env"
				.ignoreIfMissing()
				.load();

		// Inject into system environment (Spring Boot reads from this)
		for (DotenvEntry entry : dotenv.entries()) {
			System.setProperty(entry.getKey(), entry.getValue());
		}

		SpringApplication.run(AiAgentApplication.class, args);
	}
}
