package school.sptech;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

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

        String jql = "project = KAN AND resolution = Unresolved ORDER BY created DESC";

        ArrayNode todasIssues = objectMapper.createArrayNode();

        String nextPageToken = null;
        boolean ultimaPagina = false;

        while (!ultimaPagina) {

            String consulta = baseUrl
                    + "/rest/api/3/search/jql"
                    + "?jql=" + URLEncoder.encode(jql, StandardCharsets.UTF_8)
                    + "&fields=" + URLEncoder.encode(
                    "summary,priority,created,status",
                    StandardCharsets.UTF_8
            )
                    + "&maxResults=100";

            if (nextPageToken != null) {
                consulta += "&nextPageToken="
                        + URLEncoder.encode(nextPageToken, StandardCharsets.UTF_8);
            }

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

                JsonNode pagina = objectMapper.readTree(response.body());

                JsonNode issues = pagina.path("issues");

                if (issues.isArray()) {
                    todasIssues.addAll((ArrayNode) issues);
                }

                ultimaPagina = pagina.path("isLast").asBoolean(true);

                if (!ultimaPagina) {

                    String proximoToken = pagina.path("nextPageToken").asText(null);

                    if (proximoToken == null || proximoToken.isEmpty()
                            || proximoToken.equals(nextPageToken)) {

                        ultimaPagina = true;

                    } else {

                        nextPageToken = proximoToken;
                    }
                }

                System.out.println(
                        "[INFO] Ocorrências encontradas até agora: "
                                + todasIssues.size()
                );

            } else {

                throw new RuntimeException(
                        "Erro ao buscar incidentes no Jira: "
                                + status + " - " + response.body()
                );
            }
        }

        ObjectNode resultado = objectMapper.createObjectNode();
        resultado.set("issues", todasIssues);

        return resultado;
    }

    public String createIssue(
            String projectKey,
            String summary,
            String description,
            String issueType,
            String priorityName)
            throws IOException, InterruptedException {

        String priorityId = buscarPrioridadeId(priorityName);

        ObjectNode campos = objectMapper.createObjectNode();

        ObjectNode projeto = objectMapper.createObjectNode();
        projeto.put("key", projectKey);

        ObjectNode tipo = objectMapper.createObjectNode();
        tipo.put("name", issueType);

        ObjectNode prioridade = objectMapper.createObjectNode();
        prioridade.put("id", priorityId);

        ObjectNode texto = objectMapper.createObjectNode();
        texto.put("type", "text");
        texto.put("text", description);

        ObjectNode paragrafo = objectMapper.createObjectNode();
        paragrafo.put("type", "paragraph");
        paragrafo.set(
                "content",
                objectMapper.createArrayNode().add(texto)
        );

        ObjectNode descricao = objectMapper.createObjectNode();
        descricao.put("type", "doc");
        descricao.put("version", 1);
        descricao.set(
                "content",
                objectMapper.createArrayNode().add(paragrafo)
        );

        campos.set("project", projeto);
        campos.put("summary", summary);
        campos.set("issuetype", tipo);
        campos.set("priority", prioridade);
        campos.set("description", descricao);

        ObjectNode requisicaoJson = objectMapper.createObjectNode();
        requisicaoJson.set("fields", campos);

        String corpo = objectMapper.writeValueAsString(requisicaoJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/rest/api/3/issue"))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", authHeader)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
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
                "Erro ao criar ocorrência no Jira: "
                        + status + " - " + response.body()
        );
    }


    private String buscarPrioridadeId(String priorityName)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/rest/api/3/priority"))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", authHeader)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() >= 200 && response.statusCode() < 300) {

            JsonNode prioridades = objectMapper.readTree(response.body());

            for (JsonNode prioridade : prioridades) {

                String nome = prioridade.path("name").asText();

                if (nome.equalsIgnoreCase(priorityName)) {
                    return prioridade.path("id").asText();
                }
            }

            throw new RuntimeException(
                    "Prioridade não encontrada no Jira: " + priorityName
            );
        }

        throw new RuntimeException(
                "Erro ao buscar prioridades no Jira: "
                        + response.statusCode() + " - " + response.body()
        );
    }
}
