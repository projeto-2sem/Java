package school.sptech.relatorio;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ConversorPdf {

    public void converterHtmlParaPdf(String html, String nomeArquivo) {
        System.out.printf("Iniciando conversão do HTML para %s%n", nomeArquivo);

        try (OutputStream arquivo = new FileOutputStream(nomeArquivo)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // O segundo parâmetro (baseUri) é usado para resolver caminhos relativos, como imagens
            builder.withHtmlContent(html, null);
            builder.toStream(arquivo);
            builder.run();

            System.out.println("Arquivo gerado com sucesso!");
        } catch (IOException e) {
            System.out.println("Erro ao gerar o PDF: " + e.getMessage());
        }
    }
}