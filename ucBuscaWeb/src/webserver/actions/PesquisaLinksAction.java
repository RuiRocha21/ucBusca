package webserver.actions;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import webserver.models.*;
import webserver.rmi.infoHistorico;
import java.util.ArrayList;
import java.util.Map;

public class PesquisaLinksAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String url = null;
    ArrayList<infoHistorico> resultado = new ArrayList<>();
    @Override
    public String execute(){
        if(this.url != null && !url.equals("")) {
            this.PesquisaLinksParaOutrasPaginasBean().setUtilizador((String) session.get("UTILIZADOR"));
            this.PesquisaLinksParaOutrasPaginasBean().setUrl(url);
            resultado = this.PesquisaLinksParaOutrasPaginasBean().PesquisaLinksParaOutrasPaginas();
            return SUCCESS;
        }
        return INPUT;
    }

    public PesquisaLinksParaOutrasPaginasBean PesquisaLinksParaOutrasPaginasBean(){
        if (!session.containsKey("PesquisaLinksParaOutrasPaginasBean"))
            this.setPesquisaLinksParaOutrasPaginasBean(new PesquisaLinksParaOutrasPaginasBean());
        return (PesquisaLinksParaOutrasPaginasBean) session.get("PesquisaLinksParaOutrasPaginasBean");

    }

    public void setPesquisaLinksParaOutrasPaginasBean(PesquisaLinksParaOutrasPaginasBean PesquisaLinksBean) {
        this.session.put("PesquisaLinksParaOutrasPaginasBean", PesquisaLinksBean);
    }

    public Map<String, Object> getSession() {
        return session;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<infoHistorico> getResultado() {
        return resultado;
    }

    public void setResultado(ArrayList<infoHistorico> resultado) {
        this.resultado = resultado;
    }
}
