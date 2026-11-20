package school.sptech.relatorio;

import java.time.LocalDate;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class GeradorHtml {

    private final TemplateEngine templateEngine;

    public GeradorHtml() {

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    public String gerarHtmlRelatorio(List<String> alertas) {

        int totalAlertas = alertas.size();
        int alertasCriticos = 0;
        int alertasAtencao = 0;

        for (String alerta : alertas) {

            if (alerta.startsWith("[CRITICO]")) {
                alertasCriticos++;
            } else {
                alertasAtencao++;
            }
        }

        Context contexto = new Context();

        contexto.setVariable("alertas", alertas);
        contexto.setVariable("totalAlertas", totalAlertas);
        contexto.setVariable("alertasCriticos", alertasCriticos);
        contexto.setVariable("alertasAtencao", alertasAtencao);
        contexto.setVariable("dataGeracao", LocalDate.now());

        return templateEngine.process("relatorio-sentry", contexto);
    }
}
