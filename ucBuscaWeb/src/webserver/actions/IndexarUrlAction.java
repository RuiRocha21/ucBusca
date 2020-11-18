package webserver.actions;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import webserver.models.*;
import java.util.Map;

public class IndexarUrlAction extends ActionSupport implements SessionAware{
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String utilizador = null;
    private String url = null;
    private String resultado = null;

    @Override
    public String execute() {
        if(this.url != null && !url.equals("")) {
            this.IndexarUrlBean().setAdmin((String) session.get("UTILIZADOR"));
            this.IndexarUrlBean().setUrl(url);
            resultado = this.IndexarUrlBean().indexarUrl();
            return SUCCESS;
        }
        return INPUT;
    }

    public Map<String, Object> getSession() {
        return session;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public String getUtilizador() {
        return utilizador;
    }

    public void setUtilizador(String utilizador) {
        this.utilizador = utilizador;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public IndexarUrlBean IndexarUrlBean(){
        if (!session.containsKey("IndexarUrlBean"))
            this.setIndexarUrlBean(new IndexarUrlBean());
        return (IndexarUrlBean) session.get("IndexarUrlBean");
    }

    public void setIndexarUrlBean(IndexarUrlBean urlIndexar){
        this.session.put("IndexarUrlBean",urlIndexar);
    }

}
