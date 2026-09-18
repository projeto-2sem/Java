package school.sptech;

import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) throws Exception {
        Auxiliar metodo = new Auxiliar();

        JiraService jiraService = new JiraService();

        String[] componentes = {"Memória", "CPU", "Disco"};
        Integer[] porcentagem = {85, 90, 95};
        Integer componenteDaVez = ThreadLocalRandom.current().nextInt(0, 3);
        Integer porcentagemDaVez = ThreadLocalRandom.current().nextInt(0, 3);

        metodo.mensagem("[INFO] Novo aquivo de leitura registrado");
        metodo.mensagem("[TRACE] Iniciando leitura dos arquivos CSVs", 3);
        metodo.mensagem("[TRACE] Coletando parametros do Banco de Dados", 3);
        metodo.mensagem("[TRACE] Analisando e comparando valores", 3);
        metodo.mensagem("[INFO] Analise completa, realizando diagnostico", 1);

        String mensagem = componentes[componenteDaVez] +
                " em " + porcentagem[porcentagemDaVez] + "% de uso";

        metodo.mensagem(mensagem, 1);

        metodo.mensagem("[TRACE] Criando ocorrencia no Jira", 3);

        jiraService.criarChamado(mensagem);

        metodo.mensagem("[INFO] Processo de analise ++e criação de ocorrencia completo");
    }
}