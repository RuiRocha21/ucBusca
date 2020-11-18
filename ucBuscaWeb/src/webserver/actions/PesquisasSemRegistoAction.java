package webserver.actions;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import webserver.models.PesquisasSemRegistoBean;
import webserver.rmi.infoPesquisas;
import java.util.ArrayList;
import java.util.Map;


public class PesquisasSemRegistoAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String tokens = null;
    ArrayList<infoPesquisas> resultado = new ArrayList<>();
    @Override
    public String execute() {
        if (this.tokens != null && !tokens.equals("")) {
            this.PesquisasSemRegistoBean().setTokens(tokens);
            resultado = this.PesquisasSemRegistoBean().PesquisasSemRegisto();
            return SUCCESS;
        }else {
            return INPUT;
        }
    }


    public PesquisasSemRegistoBean PesquisasSemRegistoBean(){
        if (!session.containsKey("PesquisasSemRegistoBean"))
            this.setPesquisasSemRegistoBean(new PesquisasSemRegistoBean());
        return (PesquisasSemRegistoBean) session.get("PesquisasSemRegistoBean");
    }

    public void setPesquisasSemRegistoBean(PesquisasSemRegistoBean pesquisa){
        this.session.put("PesquisasSemRegistoBean",pesquisa);
    }


    public Map<String, Object> getSession() {
        return session;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public String getTokens() {
        return tokens;
    }

    public void setTokens(String tokens) {
        this.tokens = tokens;
    }

    public ArrayList<infoPesquisas> getResultado() {
        return resultado;
    }

    public void setResultado(ArrayList<infoPesquisas> resultado) {
        this.resultado = resultado;
    }
}
