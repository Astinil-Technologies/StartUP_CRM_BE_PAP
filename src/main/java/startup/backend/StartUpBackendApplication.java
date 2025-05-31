package startup.backend;

import org.springframework.scheduling.annotation.EnableScheduling;
import startup.backend.config.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(JwtConfig.class)
@SpringBootApplication
@EnableScheduling
public class StartUpBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartUpBackendApplication.class, args);
	}
}
