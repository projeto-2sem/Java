package school.sptech;

public class Medicao {

    private String apelido;
    private Double limiteAtencao;
    private Double limiteCritico;

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public Double getLimiteAtencao() {
        return limiteAtencao;
    }

    public void setLimiteAtencao(Double limiteAtencao) {
        this.limiteAtencao = limiteAtencao;
    }

    public Double getLimiteCritico() {
        return limiteCritico;
    }

    public void setLimiteCritico(Double limiteCritico) {
        this.limiteCritico = limiteCritico;
    }
}