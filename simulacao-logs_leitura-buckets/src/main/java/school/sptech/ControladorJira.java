
package school.sptech;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:3333")
@RestController
@RequestMapping("/jira")
public class ControladorJira {

    private final Jira jira;

    public ControladorJira(Jira jira) {
        this.jira = jira;
    }

    @GetMapping("/teste")
    public String testarConexao() {
        return "API SENTRY funcionando!";
    }

    @GetMapping("/incidentes")
    public JsonNode buscarIncidentes()
            throws IOException, InterruptedException {

        return jira.buscarIncidentes();
    }
}
