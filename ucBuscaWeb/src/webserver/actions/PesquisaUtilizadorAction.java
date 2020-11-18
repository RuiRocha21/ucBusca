package webserver.actions;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import webserver.models.PesquisasComRegistoBean;
import webserver.rmi.infoPesquisas;
import java.util.ArrayList;
import java.util.Map;


public class PesquisaUtilizadorAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String tokens = null;
    ArrayList<infoPesquisas> resultado = new ArrayList<>();
    @Override
    public String execute() {
        if(this.tokens != null && !tokens.equals("")) {
            this.PesquisasComRegistoBean().setUtilizador((String) session.get("UTILIZADOR"));
            this.PesquisasComRegistoBean().setTokens(tokens);
            resultado = this.PesquisasComRegistoBean().PesquisasComRegisto();
            return SUCCESS;
        }
        return INPUT;
    }


    public PesquisasComRegistoBean PesquisasComRegistoBean(){
        if (!session.containsKey("PesquisasComRegistoBean"))
            this.setPesquisasComRegistoBean(new PesquisasComRegistoBean());
        return (PesquisasComRegistoBean) session.get("PesquisasComRegistoBean");
    }

    public void setPesquisasComRegistoBean(PesquisasComRegistoBean PesquisasComRegisto){
        this.session.put("PesquisasComRegistoBean",PesquisasComRegisto);
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
