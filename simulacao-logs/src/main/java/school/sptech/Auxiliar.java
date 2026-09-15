package school.sptech;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Auxiliar {
    void timerSegundos(Integer segundos) {
        int intervalo = (segundos * 1000) / 5;

        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(intervalo);
                System.out.print(".");
            } catch (InterruptedException e) {
                return;
            }
        }
        System.out.println(); // Apenas quebra a linha no final
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
