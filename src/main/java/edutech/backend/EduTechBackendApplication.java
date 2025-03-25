package edutech.backend;

import edutech.backend.config.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(JwtConfig.class)
@SpringBootApplication
public class EduTechBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduTechBackendApplication.class, args);
	}
}
