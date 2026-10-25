package school.sptech;

import school.sptech.Jira;
import school.sptech.dto.IssueFieldsDto;
import school.sptech.dto.IssueRequestDto;
import school.sptech.dto.IssueTypeDto;
import school.sptech.dto.ProjectDto;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import school.sptech.Medicao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

    private static final String nomeBucket =
            "amzn-datalake-medalion-sentry";

    public static void main(String[] args) {

        Auxiliar metodo = new Auxiliar();

        ConexaoBanco conexaoBanco = new ConexaoBanco();
        JdbcTemplate jdbcTemplate = conexaoBanco.getJdbcTemplate();

        String sql = "SELECT * FROM servidor";

        List<Servidor> servidores = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Servidor.class)
        );

        for (Servidor servidor : servidores) {
            System.out.println(servidor);
        }

        List<String> alertas = new ArrayList<>();

        metodo.mensagem("[INFO] Novo arquivo de leitura registrado");
        metodo.mensagem("[TRACE] Iniciando leitura dos arquivos JSONs", 3);
        metodo.mensagem("[TRACE] Coletando parametros do Banco de Dados", 3);
        metodo.mensagem("[TRACE] Analisando e comparando valores", 3);

        metodo.mensagem(
                "[INFO] Analise completa, realizando diagnostico", 1
        );

        System.out.println("\n" + "=".repeat(60));
        System.out.println("INICIANDO LEITURA DAS CAMADAS DO DATA LAKE");
        System.out.println("=".repeat(60) + "\n");

        S3Client s3Client = new S3Provider().getS3Client();

        try {

            listarBucket(s3Client);

            List<String> camadasMedallion =
                    List.of("bronze/", "prata/", "ouro/");

            for (String camada : camadasMedallion) {

                System.out.println("\n" + "=".repeat(60));
                System.out.println(
                        "PROCESSANDO CAMADA "
                                + camada.toUpperCase().replace("/", "")
                );
                System.out.println("=".repeat(60) + "\n");

                lerConteudoCamada(s3Client, camada);

                ListObjectsV2Request requisicao =
                        ListObjectsV2Request.builder()
                                .bucket(nomeBucket)
                                .prefix(camada)
                                .build();

                List<S3Object> objetos =
                        s3Client.listObjectsV2(requisicao).contents();

                for (S3Object objeto : objetos) {

                    String s3Key = objeto.key();

                    if (s3Key.equals(camada)) {
                        continue;
                    }

                    String nomeApenasDoArquivo =
                            s3Key.substring(s3Key.lastIndexOf("/") + 1);

                    String downloadLocal = "download_"
                            + camada.replace("/", "")
                            + "_" + nomeApenasDoArquivo;

                    // fazerDownload(s3Client, s3Key, downloadLocal);

                    if (camada.equals("ouro/")
                            && nomeApenasDoArquivo
                            .toLowerCase().endsWith(".json")) {

                        metodo.mensagem(
                                "[INFO] Analisando arquivo "
                                        + nomeApenasDoArquivo
                        );

                        GetObjectRequest getRequest =
                                GetObjectRequest.builder()
                                        .bucket(nomeBucket)
                                        .key(s3Key)
                                        .build();

                        String conteudo = s3Client.getObject(
                                getRequest,
                                ResponseTransformer.toBytes()
                        ).asUtf8String();

                        analisarJSON(conteudo, jdbcTemplate, alertas);
                    }
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "[ERRO] Falha na leitura do Data Lake: "
                            + e.getMessage()
            );

            return;

        } finally {
            s3Client.close();
        }

        System.out.println("[DEBUG] Total de alertas encontrados: " + alertas.size());

        if (alertas.isEmpty()) {
            metodo.mensagem("[INFO] Nenhum alerta encontrado.");

        } else {

            for (String alerta : alertas) {
                metodo.mensagem(alerta, 1);
            }

            String url = System.getenv("JIRA_URL");
            String email = System.getenv("JIRA_EMAIL");
            String token = System.getenv("JIRA_API_TOKEN");

            System.out.println("[DEBUG] URL configurada: " + (url != null));
            System.out.println("[DEBUG] Email configurado: " + (email != null));
            System.out.println("[DEBUG] Token configurado: " + (token != null));

            if (url == null || email == null || token == null) {
                System.err.println("[ERRO] Configure as variáveis de ambiente do Jira.");
            } else {
                metodo.mensagem("[TRACE] Criando ocorrencia no Jira", 3);

                Jira jira = new Jira(url, email, token);

                for (String alerta : alertas) {
                    try {
                        String resposta = jira.createIssue("KAN", alerta, "Task");
                        System.out.println("[INFO] Ocorrencia criada no Jira: " + resposta);

                    } catch (java.io.IOException e) {
                        System.err.println("[ERRO] Falha de comunicacao com o Jira: " + e.getMessage());

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("[ERRO] Criacao da ocorrencia interrompida.");

                        break;

                    } catch (RuntimeException e) {
                        System.err.println("[ERRO] Jira retornou um erro: " + e.getMessage());
                    }
                }
            }
        }


        metodo.mensagem("[INFO] Processo de analise e criação de ocorrencia completo");
    }



    public static void analisarJSON(String conteudo, JdbcTemplate jdbcTemplate, List<String> alertas) {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode raiz;

        try {
            raiz = mapper.readTree(conteudo);
        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao ler o JSON: " + e.getMessage());

            return;
        }

        if (raiz == null || raiz.isEmpty()) {
            System.out.println("[INFO] JSON sem dados para analisar.");

            return;
        }

        String sql = "SELECT tm.apelido, " +
                "sm.limiteAtencao AS limiteAtencao, " +
                "sm.limiteCritico AS limiteCritico " +
                "FROM servidor s " +
                "JOIN servidor_medicao sm " +
                "ON sm.servidorId = s.idServidor " +
                "JOIN tipo_medicao tm " +
                "ON tm.idTipoMedicao = sm.tipoMedicaoId " +
                "WHERE s.hostname = ? AND s.mac = ? " +
                "AND sm.ativo = 1";

        JsonNode registros;

        if (raiz.isArray()) {
            registros = raiz;
        } else {
            registros = mapper.createArrayNode().add(raiz);
        }

        for (JsonNode registro : registros) {

            String hostname = registro.path("hostname").asText("");
            String mac = registro.path("mac").asText("");

            if (hostname.isEmpty() || mac.isEmpty()) {
                System.out.println("[ERRO] Registro sem hostname ou MAC.");

                continue;
            }

            List<Medicao> medicoesAtivas = jdbcTemplate.query(
                    sql,
                    new BeanPropertyRowMapper<>(Medicao.class),
                    hostname,
                    mac
            );

            System.out.println("[DEBUG] Hostname do JSON: " + hostname);
            System.out.println("[DEBUG] MAC do JSON: " + mac);

            if (medicoesAtivas.isEmpty()) {
                System.out.println("[INFO] Nenhuma medicao ativa para " + hostname);

                continue;
            }

            for (Medicao medicaoConfig : medicoesAtivas) {

                String medicao = medicaoConfig.getApelido();
                Double limiteAtencao = medicaoConfig.getLimiteAtencao();
                Double limiteCritico = medicaoConfig.getLimiteCritico();

                String nomeColuna = "";

                if (medicao.equals("CPU")) {
                    nomeColuna = "cpu_percent";
                } else if (medicao.equals("MEMORIA")) {
                    nomeColuna = "memory_percent";
                } else if (medicao.equals("DISCO_LEITURA")) {
                    nomeColuna = "disk_read_bytes";
                } else if (medicao.equals("DISCO_ESCRITA")) {
                    nomeColuna = "disk_write_bytes";
                } else if (medicao.equals("REDE_RECEBIMENTO")) {
                    nomeColuna = "net_bytes_recv";
                } else if (medicao.equals("REDE_ENVIO")) {
                    nomeColuna = "net_bytes_sent";
                }

                if (nomeColuna.isEmpty()) {
                    System.out.println("[INFO] Medicao sem mapeamento: " + medicao);

                    continue;
                }

                JsonNode campo = registro.path(nomeColuna);

                if (campo.isMissingNode() || campo.isNull() || campo.asText().trim().isEmpty()) {
                    System.out.println("[INFO] Campo nao encontrado no JSON: " + nomeColuna);

                    continue;
                }

                double valor;

                try {
                    valor = Double.parseDouble(campo.asText().trim());
                } catch (NumberFormatException e) {
                    System.out.println("[ERRO] Valor invalido para " + medicao);

                    continue;
                }

                String unidade;

                if (medicao.equals("CPU") || medicao.equals("MEMORIA")) {
                    unidade = "%";
                } else {
                    unidade = " bytes";
                }

                System.out.println("[INFO] Servidor: " + hostname + " | Medicao: " + medicao
                        + " | Valor: " + valor + unidade);

                if (limiteAtencao == null || limiteCritico == null) {
                    System.out.println("[ERRO] Limites nao configurados para " + medicao);

                    continue;
                }

                if (valor >= limiteCritico) {
                    alertas.add("[CRITICO] " + medicao + " em " + valor + unidade + " no servidor " + hostname);

                } else if (valor >= limiteAtencao) {
                    alertas.add("[ALERTA] " + medicao + " em " + valor + unidade + " no servidor " + hostname);
                }
            }
        }
    }


    public static void listarBucket(S3Client s3Client) {

        try {
            List<Bucket> buckets = s3Client.listBuckets().buckets();

            System.out.println("Lista de buckets:");

            for (Bucket bucket : buckets) {
                System.out.println("- " + bucket.name());
            }

        } catch (S3Exception e) {
            System.err.println(
                    "Erro ao listar buckets: " + e.getMessage()
            );
        }
    }

    public static void fazerUpload(
            S3Client s3Client,
            String prefixo,
            File arquivoLocal) {

        System.out.println(
                "\nUPLOAD PARA PREFIXO: " + prefixo.toUpperCase()
        );

        try {
            String s3Key = prefixo + arquivoLocal.getName();

            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucket(nomeBucket)
                            .key(s3Key)
                            .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromFile(arquivoLocal)
            );

            System.out.println(
                    "Upload concluído com sucesso! Chave S3: " + s3Key
            );

        } catch (Exception e) {
            System.err.println(
                    "Falha ao realizar o upload: " + e.getMessage()
            );
        }
    }

    public static void lerConteudoCamada(
            S3Client s3Client,
            String prefixo) {

        System.out.println(
                "\nLEITURA DO PREFIXO: " + prefixo.toUpperCase()
        );

        try {
            ListObjectsV2Request requisicao =
                    ListObjectsV2Request.builder()
                            .bucket(nomeBucket)
                            .prefix(prefixo)
                            .build();

            List<S3Object> objetos =
                    s3Client.listObjectsV2(requisicao).contents();

            if (objetos.isEmpty()) {
                System.out.println(
                        "Nenhum arquivo encontrado no prefixo " + prefixo
                );
                return;
            }

            for (S3Object objeto : objetos) {

                if (objeto.key().equals(prefixo)) {
                    continue;
                }

                System.out.println("\nArquivo S3: " + objeto.key());
                System.out.println("Tamanho: " + objeto.size() + " bytes");

                GetObjectRequest getRequest =
                        GetObjectRequest.builder()
                                .bucket(nomeBucket)
                                .key(objeto.key())
                                .build();

                String conteudo = s3Client.getObject(
                        getRequest,
                        ResponseTransformer.toBytes()
                ).asUtf8String();

                System.out.println("Conteúdo do arquivo:");
                System.out.println(conteudo);
            }

        } catch (S3Exception e) {
            System.err.println(
                    "Erro de S3 ao listar camada " + prefixo
                            + ": " + e.getMessage()
            );

        } catch (Exception e) {
            System.err.println(
                    "Erro geral ao ler camada " + prefixo
                            + ": " + e.getMessage()
            );
        }
    }

    /*
    public static void fazerDownload(
            S3Client s3Client,
            String s3Key,
            String caminhoDestinoLocal) {

        System.out.println("\nDOWNLOAD DO ARQUIVO: " + s3Key);

        try {
            File arquivoDestino = new File(caminhoDestinoLocal);

            if (arquivoDestino.exists()) {
                arquivoDestino.delete();
            }

            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder()
                            .bucket(nomeBucket)
                            .key(s3Key)
                            .build();

            s3Client.getObject(
                    getObjectRequest,
                    ResponseTransformer.toFile(arquivoDestino.toPath())
            );

            System.out.println("Arquivo baixado com sucesso!");
            System.out.println(
                    "Caminho absoluto: " + arquivoDestino.getAbsolutePath()
            );

        } catch (S3Exception e) {
            System.err.println("Erro AWS S3: " + e.getMessage());

        } catch (Exception e) {
            System.err.println(
                    "Erro ao salvar arquivo: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
    */

}
