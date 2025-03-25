package startup.backend;

import startup.backend.config.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(JwtConfig.class)
@SpringBootApplication
public class StartUpBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartUpBackendApplication.class, args);
	}
}
