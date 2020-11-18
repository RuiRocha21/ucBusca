package webserver.actions;
import webserver.models.RegistoBean;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import java.util.Map;

public class RegistoAction extends ActionSupport implements SessionAware{
    private String utilizador = null;
    private String password = null;
    private String tipoUtilizador = null;
    private Map<String, Object> session;

    public String execute(){
        if(this.utilizador != null && !utilizador.equals("")) {
            this.getRegistoBean().setNickname(this.utilizador);
            this.getRegistoBean().setPassword(this.password);

            if(this.getRegistoBean().registar().equals("Utilizador " + this.utilizador + " registado com sucesso")){
                session.put("utilizador", utilizador);
                session.put("loggedin", true);
                return SUCCESS;
            }
            return ERROR;
        }
        else {
            return LOGIN;
        }
    }

    public RegistoBean getRegistoBean() {
        if(!session.containsKey("registoBean"))
            this.setRegistoBean(new RegistoBean());
        return (RegistoBean) session.get("registoBean");
    }

    public void setRegistoBean(RegistoBean registoBean) {
        this.session.put("registoBean", registoBean);
    }

    public String getUtilizador() {
        return utilizador;
    }

    public void setUtilizador(String utilizador) {
        this.utilizador = utilizador;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTipoUtilizador() {
        return tipoUtilizador;
    }

    public void setTipoUtilizador(String tipoUtilizador) {
        this.tipoUtilizador = tipoUtilizador;
    }

    public Map<String, Object> getSession() {
        return session;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
