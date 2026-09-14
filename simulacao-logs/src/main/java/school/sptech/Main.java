package school.sptech;

import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Auxiliar metodo = new Auxiliar();

        String[] componentes = {"Memória", "CPU", "Disco"};
        Integer[] porcentagem = {85, 90, 95};
        Integer componenteDaVez = ThreadLocalRandom.current().nextInt(0, 3);
        Integer porcentagemDaVez = ThreadLocalRandom.current().nextInt(0, 3);
        metodo.mensagem("[INFO] Novo aquivo de leitura registrado");
        metodo.mensagem("[TRACE] Iniciando leitura dos arquivos CSVs", 3);
        metodo.mensagem("[TRACE] Coletando parametros do Banco de Dados", 3);
        metodo.mensagem("[TRACE] Analisando e comparando valores", 3);
        metodo.mensagem("[INFO] Analise completa, realizando diagnostico", 1);
        metodo.mensagem("[WARNING] "+ componentes[componenteDaVez]+
                " em "+ porcentagem[porcentagemDaVez]+"% de uso", 1);
        metodo.mensagem("[TRACE] Criando ocorrencia no Jira", 3);
        metodo.mensagem("[INFO] Processo de analise ++e criação de ocorrencia completo");

    }
}
