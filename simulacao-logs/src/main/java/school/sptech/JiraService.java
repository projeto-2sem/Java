package school.sptech;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class JiraService {

    private HttpClient httpClient = HttpClient.newHttpClient();

    private String urlJira = "https://rafaellima12.atlassian.net";
    private String email = "bruno.tita@sptech.school";

    public void criarChamado(String mensagem) throws Exception {

        String apiToken = "ATATT3xFfGF0qQFvSz4X8yGd1pmkcvfT4Nu2M9CtFSpBVjrkD_W-YPf2AkM2S54GMLOYcJrd-op94EjQiueN15hrnoV7u1JpLfKYMyol-8XKwtkFYM2_dSCuoBLr76c_dqQqVc40sv7uQKQ5tNjX9WE3WSIgv_ZBbz4hIlTWQS66djUBxMCNwp8=80CB1D5A";

        String credenciais = email + ":" + apiToken;

        String tokenBase64 = Base64.getEncoder().encodeToString(
                credenciais.getBytes(StandardCharsets.UTF_8)
        );

        String json = """
                {
                    "fields": {
                        "project": {
                            "key": "SCRUM"
                        },
                        "summary": "%s",
                        "issuetype": {
                            "name": "Task"
                        }
                    }
                }
                """.formatted(mensagem);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlJira + "/rest/api/3/issue"))
                .header("Authorization", "Basic " + tokenBase64)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 201) {
            System.out.println("Chamado criado com sucesso");
            System.out.println(response.body());
        } else {
            System.out.println("Deu erro: " + response.statusCode());
            System.out.println(response.body());
        }
    }
}