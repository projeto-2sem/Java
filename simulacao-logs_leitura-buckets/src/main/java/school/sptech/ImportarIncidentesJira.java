package school.sptech;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;

public class ImportarIncidentesJira {

    public static void main(String[] args) {

        ConexaoBanco conexaoBanco = new ConexaoBanco();
        JdbcTemplate jdbcTemplate = conexaoBanco.getJdbcTemplate();

        String url = System.getenv("JIRA_URL");
        String email = System.getenv("JIRA_EMAIL");
        String token = System.getenv("JIRA_TOKEN");

        if (url == null || email == null || token == null) {
            System.out.println("[ERRO] Configure as variáveis do Jira.");
            return;
        }

        Jira jira = new Jira(url, email, token);

        try {

            System.out.println("[INFO] Buscando ocorrências existentes no Jira...");

            JsonNode resposta = jira.buscarIncidentes();
            JsonNode issues = resposta.path("issues");

            for (JsonNode issue : issues) {

                String jiraKey = issue.path("key").asText();

                JsonNode fields = issue.path("fields");

                String descricao = fields.path("summary").asText();

                String prioridade = fields.path("priority")
                        .path("name").asText("Medium");

                if (jiraKey.isEmpty() || descricao.isEmpty()) {
                    continue;
                }

                Integer quantidade = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM alertas WHERE jira_key = ?",
                        Integer.class,
                        jiraKey
                );

                if (quantidade != null && quantidade > 0) {
                    System.out.println("[INFO] Ocorrência já cadastrada: " + jiraKey);
                    continue;
                }

                String[] partes = descricao.split(" em ", 2);

                if (partes.length < 2 || !partes[1].contains(" no servidor ")) {
                    System.out.println("[ERRO] Formato de descrição inválido: " + jiraKey);
                    continue;
                }

                String componente = partes[0]
                        .replace("[CRITICO] ", "")
                        .replace("[ALERTA] ", "")
                        .toLowerCase();

                if (componente.contains("cpu")) {
                    componente = "cpu";
                } else if (componente.contains("memoria")) {
                    componente = "ram";
                } else if (componente.contains("disco")) {
                    componente = "disco";
                } else {
                    System.out.println("[ERRO] Componente não identificado: " + jiraKey);
                    continue;
                }

                String hostname = partes[1]
                        .split(" no servidor ")[1]
                        .split(" \\(MAC:")[0];

                String tipoAlerta;

                if (descricao.startsWith("[CRITICO]")) {
                    tipoAlerta = "critico";
                } else {
                    tipoAlerta = "atencao";
                }

                Integer idServidor;

                try {

                    idServidor = jdbcTemplate.queryForObject(
                            "SELECT idServidor FROM servidor WHERE hostname = ?",
                            Integer.class,
                            hostname
                    );

                } catch (Exception e) {

                    System.out.println("[ERRO] Servidor não encontrado: " + hostname);
                    continue;
                }

                Main.salvarAlertaNoBanco(
                        jdbcTemplate,
                        idServidor,
                        componente,
                        tipoAlerta,
                        jiraKey,
                        descricao,
                        prioridade
                );
            }

            System.out.println("[INFO] Importação finalizada.");

        } catch (Exception e) {

            System.out.println("[ERRO] Falha na importação: " + e.getMessage());
            e.printStackTrace();
        }
    }
}