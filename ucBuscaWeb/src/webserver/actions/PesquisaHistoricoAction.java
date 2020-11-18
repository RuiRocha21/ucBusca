package webserver.actions;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import webserver.models.*;
import webserver.rmi.infoHistorico;
import java.util.ArrayList;
import java.util.Map;

public class PesquisaHistoricoAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    ArrayList<infoHistorico> resultado = new ArrayList<>();
    @Override
    public String execute() {
        this.PesquisaHistoricoBean().setUtilizador((String) session.get("UTILIZADOR"));
        resultado = this.PesquisaHistoricoBean().HistoricoPesquisas();
        return SUCCESS;
    }

    public PesquisaHistoricoBean PesquisaHistoricoBean(){
        if (!session.containsKey("PesquisaHistoricoBean"))
            this.setPesquisaHistoricoBean(new PesquisaHistoricoBean());
        return (PesquisaHistoricoBean) session.get("PesquisaHistoricoBean");

    }

    public void setPesquisaHistoricoBean(PesquisaHistoricoBean historico) {
        this.session.put("PesquisaHistoricoBean", historico);
    }

    public Map<String, Object> getSession() {
        return session;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public ArrayList<infoHistorico> getResultado() {
        return resultado;
    }

    public void setResultado(ArrayList<infoHistorico> resultado) {
        this.resultado = resultado;
    }
}
