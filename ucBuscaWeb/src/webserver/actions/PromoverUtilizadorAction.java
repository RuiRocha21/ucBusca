package webserver.actions;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import webserver.models.PromoverUtilizadoresBean;

import java.util.Map;

public class PromoverUtilizadorAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String utilizador = null;
    private String novoAdmin = null;
    private String resultado = null;
    @Override
    public String execute(){
        this.getPromoverUtilizadoresBean().setAdmin((String) session.get("UTILIZADOR"));
        this.getPromoverUtilizadoresBean().setNovoAdmin(this.novoAdmin);
        resultado = this.getPromoverUtilizadoresBean().promoverUtilizador();
        return SUCCESS;
    }


    public String getUtilizador() {
        return utilizador;
    }

    public void setUtilizador(String utilizador) {
        this.utilizador = utilizador;
    }

    public String getNovoAdmin() {
        return novoAdmin;
    }

    public void setNovoAdmin(String novoAdmin) {
        this.novoAdmin = novoAdmin;
    }


    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public PromoverUtilizadoresBean getPromoverUtilizadoresBean() {
        if (!session.containsKey("PromoverUtilizadoresBean"))
            this.setPromoverUtilizadoresBean(new PromoverUtilizadoresBean());
        return (PromoverUtilizadoresBean) session.get("PromoverUtilizadoresBean");
    }

    public void setPromoverUtilizadoresBean(PromoverUtilizadoresBean PromoverUtilizadores) {
        this.session.put("PromoverUtilizadoresBean", PromoverUtilizadores);
    }

    public Map<String, Object> getSession() {
        return session;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
