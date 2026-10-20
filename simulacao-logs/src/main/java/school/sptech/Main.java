package school.sptech;

import school.sptech.config.Jira;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    private static final String nomeBucket = "amzn-datalake-medalion-sentry";

    public static void main(String[] args) throws Exception {

        Auxiliar metodo = new Auxiliar();

        ConexaoBanco conexaoBanco = new ConexaoBanco();

        var jdbcTemplate = conexaoBanco.getJdbcTemplate();

        String sql = "SELECT * FROM servidor";

        List<Servidor> servidores = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Servidor.class));

        for (Servidor servidor : servidores) {
            System.out.println(servidor);
        }

        String[] componentes = {"Memória", "CPU", "Disco"};
        Integer[] porcentagem = {85, 90, 95};

        Integer componenteDaVez = ThreadLocalRandom.current().nextInt(0, 3);
        Integer porcentagemDaVez = ThreadLocalRandom.current().nextInt(0, 3);

        metodo.mensagem("[INFO] Novo aquivo de leitura registrado");
        metodo.mensagem("[TRACE] Iniciando leitura dos arquivos CSVs", 3);
        metodo.mensagem("[TRACE] Coletando parametros do Banco de Dados", 3);
        metodo.mensagem("[TRACE] Analisando e comparando valores", 3);
        metodo.mensagem("[INFO] Analise completa, realizando diagnostico", 1);

        String mensagem = componentes[componenteDaVez] + " em " + porcentagem[porcentagemDaVez] + "% de uso";

        metodo.mensagem(mensagem, 1);

        metodo.mensagem("[TRACE] Criando ocorrencia no Jira", 3);

        Jira jira = new Jira(
                "https://sentry-scada.atlassian.net",
                "bruno.tita@sptech.school",
                "ATATT3xFfGF0Dk6zNZQchkJWe3xwlqwtYx8v92mHPt_0MN6qrxNkMqDzHn7PkhpGZDijBfgfavsn_mHbXvRMlMDR2cQjJB4kpYsnbAeUM4XDUBfDWrGW2M4-k78XMtio0eqOuhU0NyhPs4uWbsnUMLgLww5RTG8Cr714Z4DyEsvQPHtWKCvcwGQ=16353D7F"
        );

        jira.createIssue("KAN", mensagem, "Task");

        metodo.mensagem("[INFO] Processo de analise e criação de ocorrencia completo");

        System.out.println("\n" + "=".repeat(60));
        System.out.println("INICIANDO LEITURA DAS CAMADAS DO DATA LAKE");
        System.out.println("=".repeat(60) + "\n");

        S3Client s3Client = new S3Provider().getS3Client();

        listarBucket(s3Client);

        List<String> camadasMedallion = List.of("bronze/", "prata/", "ouro/");

        for (String camada : camadasMedallion) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("PROCESSANDO CAMADA " + camada.toUpperCase().replace("/", ""));
            System.out.println("=".repeat(60) + "\n");

            lerConteudoCamada(s3Client, camada);

            ListObjectsV2Request requisicao = ListObjectsV2Request.builder().bucket(nomeBucket).prefix(camada).build();

            List<S3Object> objetos = s3Client.listObjectsV2(requisicao).contents();

            for (S3Object objeto : objetos) {
                String s3Key = objeto.key();
                if (s3Key.equals(camada)) continue;

                String nomeApenasDoArquivo = s3Key.substring(s3Key.lastIndexOf("/") + 1);
                String downloadLocal = "download_" + camada.replace("/", "") + "_" + nomeApenasDoArquivo;

                //  fazerDownload(s3Client, s3Key, downloadLocal);
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
            System.err.println("Erro ao listar buckets: " + e.getMessage());
        }
    }

    public static void fazerUpload(S3Client s3Client, String prefixo, File arquivoLocal) {
        System.out.println("\nUPLOAD PARA PREFIXO: " + prefixo.toUpperCase());

        try {
            String s3Key = prefixo + arquivoLocal.getName();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(nomeBucket).key(s3Key).build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(arquivoLocal));
            System.out.println("Upload concluído com sucesso! Chave S3: " + s3Key);
        } catch (Exception e) {
            System.err.println(" Falha ao realizar o upload: " + e.getMessage());
        }
    }

    public static void lerConteudoCamada(S3Client s3Client, String prefixo) {
        System.out.println("\nLEITURA DO PREFIXO: " + prefixo.toUpperCase());

        try {
            ListObjectsV2Request requisicao = ListObjectsV2Request.builder().bucket(nomeBucket).prefix(prefixo).build();

            List<S3Object> objetos = s3Client.listObjectsV2(requisicao).contents();

            if (objetos.isEmpty()) {
                System.out.println("Nenhum arquivo encontrado no prefixo " + prefixo);
                return;
            }

            for (S3Object objeto : objetos) {
                if (objeto.key().equals(prefixo)) continue;

                System.out.println("\n Arquivo S3: " + objeto.key());
                System.out.println(" Tamanho: " + objeto.size() + " bytes");

                GetObjectRequest getRequest = GetObjectRequest.builder().bucket(nomeBucket).key(objeto.key()).build();

                String conteudo = s3Client.getObject(getRequest, ResponseTransformer.toBytes()).asUtf8String();

                System.out.println(" Conteúdo do arquivo:");
                System.out.println(conteudo);
            }
        } catch (S3Exception e) {
            System.err.println(" Erro de S3 ao listar camada " + prefixo + ": " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            System.err.println(" Erro geral ao ler camada " + prefixo + ": " + e.getMessage());
        }
    }

        /*
    public static void fazerDownload(
            S3Client s3Client,
            String s3Key,
            String caminhoDestinoLocal
    ) {
        System.out.println("\n DOWNLOAD DO ARQUIVO: " + s3Key);

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