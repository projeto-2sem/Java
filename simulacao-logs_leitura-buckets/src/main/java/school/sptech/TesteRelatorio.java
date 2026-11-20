package school.sptech;

import school.sptech.relatorio.GeradorHtml;
import school.sptech.relatorio.ConversorPdf;

import java.util.ArrayList;
import java.util.List;

public class TesteRelatorio {

    public static void main(String[] args) {

        List<String> alertas = new ArrayList<>();

        alertas.add("[CRITICO] CPU em 67.0% no servidor roblox");
        alertas.add("[ALERTA] MEMORIA em 67.0% no servidor gta6");
        alertas.add("[CRITICO] DISCO em 67.0% no servidor fortnite");

        GeradorHtml gerador = new GeradorHtml();

        String html = gerador.gerarHtmlRelatorio(alertas);

        ConversorPdf conversor = new ConversorPdf();

        conversor.converterHtmlParaPdf(html, "teste-relatorio.pdf");

        System.out.println("Relatório gerado!");
    }
}
