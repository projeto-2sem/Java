package school.sptech;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Auxiliar {
    void timerSegundos(Integer segundos) throws InterruptedException {
        Integer intervalo = (segundos * 1000 )/ 5;
        for (int i = 0; i < 5; i++) {
            System.out.print(".");
            Thread.sleep(intervalo);
        }
        System.out.println(".");
    }

    void tempo(){
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.print(dataHora.format(formatter));
    }

    void mensagem(String mensagem, Integer segundos){
        tempo();
        System.out.print(" "+mensagem);
        timerSegundos(segundos);
    }

    void mensagem(String mensagem){
        tempo();
        System.out.println(" " + mensagem);
    }
}
