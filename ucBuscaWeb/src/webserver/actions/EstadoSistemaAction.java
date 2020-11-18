package webserver.actions;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import webserver.models.paginaAdminWebBean;
import webserver.rmi.infoServidores;

import java.util.ArrayList;
import java.util.Map;

public class EstadoSistemaAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private ArrayList<infoServidores> resultado = new ArrayList<>();
    @Override
    public String execute(){
        this.paginaAdminWebBean().setAdmin((String) session.get("UTILIZADOR"));
        resultado = this.paginaAdminWebBean().paginaAdminWeb();
        return SUCCESS;

    }

    public paginaAdminWebBean paginaAdminWebBean() {
        if (!session.containsKey("PaginaAdminWebBean"))
            this.setPaginaAdminWebBean(new paginaAdminWebBean());
        return (paginaAdminWebBean) session.get("PaginaAdminWebBean");
    }

    public void setPaginaAdminWebBean(paginaAdminWebBean paginaAdminBean) {
        this.session.put("PaginaAdminWebBean", paginaAdminBean);
    }

    public Map<String, Object> getSession() {
        return session;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public ArrayList<infoServidores> getResultado() {
        return resultado;
    }

    public void setResultado(ArrayList<infoServidores> resultado) {
        this.resultado = resultado;
    }
}
