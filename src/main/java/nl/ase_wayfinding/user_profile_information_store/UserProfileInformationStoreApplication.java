package nl.ase_wayfinding.user_profile_information_store;

import nl.ase_wayfinding.user_profile_information_store.config.AwsSecretsInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserProfileInformationStoreApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(UserProfileInformationStoreApplication.class);
		app.addInitializers(new AwsSecretsInitializer());
		app.run(args);
	}

}
