
package school.sptech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AplicacaoApi {

    public static void main(String[] args) {
        SpringApplication.run(AplicacaoApi.class, args);
    }

    @Bean
    public Jira jira() {

        String url = System.getenv("JIRA_URL");
        String email = System.getenv("JIRA_EMAIL");
        String token = System.getenv("JIRA_API_TOKEN");

        return new Jira(url, email, token);
    }
}
