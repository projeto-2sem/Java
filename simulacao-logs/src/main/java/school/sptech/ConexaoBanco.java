package school.sptech;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class ConexaoBanco {

    private DataSource fonteDados;
    private JdbcTemplate jdbcTemplate;

    public ConexaoBanco() {

        BasicDataSource fonteDadosBasica = new BasicDataSource();

        fonteDadosBasica.setUrl("jdbc:mysql://localhost:3306/sentry");
        fonteDadosBasica.setUsername("root");
        fonteDadosBasica.setPassword("Guiguinina09@");

        this.fonteDados = fonteDadosBasica;
        this.jdbcTemplate = new JdbcTemplate(fonteDados);
    }

    public DataSource getFonteDados() {
        return fonteDados;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}