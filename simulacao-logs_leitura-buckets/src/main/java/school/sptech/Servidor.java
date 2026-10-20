package school.sptech;

public class Servidor {

    private Integer idServidor;
    private Integer sistemaOperacionalId;
    private Integer empresaId;
    private String apelido;
    private String hostname;
    private String mac;
    private String modelo;
    private Integer status;

    public Integer getIdServidor() {
        return idServidor;
    }

    public void setIdServidor(Integer idServidor) {
        this.idServidor = idServidor;
    }

    public Integer getSistemaOperacionalId() {
        return sistemaOperacionalId;
    }

    public void setSistemaOperacionalId(Integer sistemaOperacionalId) {
        this.sistemaOperacionalId = sistemaOperacionalId;
    }

    public Integer getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Integer empresaId) {
        this.empresaId = empresaId;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}