package nl.ase_wayfinding.user_profile_information_store.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.Map;

public class AwsSecretsInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        String activeProfile = context.getEnvironment().getProperty("spring.profiles.active", "default");

        if ("local".equals(activeProfile)) {
            System.out.println("🛑 Skipping AWS Secrets injection when running in local mode");
            return;
        }

        String secretName = "user-profile-information-store/dev/env";

        try (SecretsManagerClient client = SecretsManagerClient.create()) {
            String secretString = client.getSecretValue(GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build()).secretString();

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> secrets = mapper.readValue(secretString, Map.class);

            // Inject the secrets into Spring Environment
            context.getEnvironment().getPropertySources()
                    .addFirst(new MapPropertySource("aws-secrets", secrets));

            // ✅ Optional: Print test values
            String dbUrl = context.getEnvironment().getProperty("SPRING_DATASOURCE_URL");
            String dbUser = context.getEnvironment().getProperty("SPRING_DATASOURCE_USERNAME");

            System.out.println("✅ 🔐 AWS secrets injected:");
            System.out.println("🔐 DB URL: " + dbUrl);
            System.out.println("🔐 DB User: " + dbUser);
            System.out.println("🧩 All keys: " + secrets.keySet());

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load AWS Secrets", e);
        }
    }
}
