
package school.sptech;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public class Jira {

    private final String baseUrl;
    private final String authHeader;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public Jira(String baseUrl, String email, String apiToken) {
        this.baseUrl = baseUrl;

        String auth = email + ":" + apiToken;
        this.authHeader = "Basic " + Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        this.objectMapper = new ObjectMapper();
    }

    public JsonNode buscarIncidentes()
            throws IOException, InterruptedException {

        String jql = "project = KAN ORDER BY created DESC";

        String consulta = baseUrl
                + "/rest/api/3/search/jql"
                + "?jql=" + URLEncoder.encode(jql, StandardCharsets.UTF_8)
                + "&fields=" + URLEncoder.encode(
                "summary,priority,created,status",
                StandardCharsets.UTF_8
        )
                + "&maxResults=100";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(consulta))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", authHeader)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        int status = response.statusCode();

        if (status >= 200 && status < 300) {
            return objectMapper.readTree(response.body());
        }

        throw new RuntimeException(
                "Erro ao buscar incidentes no Jira: "
                        + status + " - " + response.body()
        );
    }

    public String createIssue(String projectKey, String summary,
                              String description, String issueType,
                              String priorityName)
            throws IOException, InterruptedException {

        String priorityId = buscarPrioridadeId(priorityName);

        ObjectNode dados = objectMapper.createObjectNode();
        ObjectNode fields = dados.putObject("fields");

        ObjectNode project = fields.putObject("project");
        project.put("key", projectKey);

        fields.put("summary", summary);

        ObjectNode type = fields.putObject("issuetype");
        type.put("name", issueType);

        ObjectNode priority = fields.putObject("priority");
        priority.put("id", priorityId);

        ObjectNode descricao = fields.putObject("description");
        descricao.put("type", "doc");
        descricao.put("version", 1);

        ObjectNode paragrafo = objectMapper.createObjectNode();
        paragrafo.put("type", "paragraph");

        ObjectNode texto = objectMapper.createObjectNode();
        texto.put("type", "text");
        texto.put("text", description);

        paragrafo.putArray("content").add(texto);
        descricao.putArray("content").add(paragrafo);

        String json = objectMapper.writeValueAsString(dados);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/rest/api/3/issue"))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        int status = response.statusCode();

        if (status >= 200 && status < 300) {
            return response.body();
        }

        throw new RuntimeException(
                "Jira request failed: " + status + " - " + response.body()
        );
    }

    private String buscarPrioridadeId(String priorityName)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/rest/api/3/priority"))
                .header("Authorization", authHeader)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(
                    "Erro ao buscar prioridades: " + response.body()
            );
        }

        JsonNode prioridades = objectMapper.readTree(response.body());

        for (JsonNode prioridade : prioridades) {
            String nome = prioridade.path("name").asText("");

            if (nome.equalsIgnoreCase(priorityName)) {
                return prioridade.path("id").asText();
            }
        }

        throw new RuntimeException(
                "Prioridade nao encontrada no Jira: " + priorityName
        );
    }
}
