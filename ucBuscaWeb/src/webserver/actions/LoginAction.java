package webserver.actions;

import webserver.models.LoginBean;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import java.util.Map;

public class LoginAction extends ActionSupport implements SessionAware{
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String utilizador = null;
    private String password = null;
    private String tipoUtilizador = null;

    @Override
    public String execute() {
        if(this.utilizador != null && !utilizador.equals("") && this.password != null && !password.equals("")) {
            this.getLoginBean().setUtilizador(this.utilizador);
            this.getLoginBean().setPassword(this.password);

            if(this.getLoginBean().loginUtil().get(0).startsWith("utilizador " + this.utilizador)){
                session.put("UTILIZADOR", utilizador);
                session.put("ADMIN", false);
                session.put("LOGGED_IN", true);
                for(int i = 0; i< this.getLoginBean().loginUtil().size(); i++){
                    session.put("NOTIFICACAO", this.getLoginBean().loginUtil().get(i).toString());
                }
                return SUCCESS;
            }else if(this.getLoginBean().loginUtil().get(0).startsWith("admin " + this.utilizador)){
                session.put("UTILIZADOR", utilizador);
                session.put("ADMIN", true);
                session.put("LOGGED_IN", true);
                for(int i = 0; i< this.getLoginBean().loginUtil().size(); i++){
                    if(this.getLoginBean().loginUtil().get(i).contains(utilizador)){
                        session.put("NOTIFICACAO", "Foi promovido a Adminstrador");
                    }
                }
                return SUCCESS;
            }
        }
        return ERROR;
    }

    public String checkLogin(){
        if(session.containsKey("LOGGED_IN")){
            return SUCCESS;
        }
        return ERROR;
    }

    public String logout() {
        if (session.containsKey("UTILIZADOR") && session.containsKey("LOGGED_IN")) {
            session.remove("UTILIZADOR");
            session.remove("LOGGED_IN");
        }
        return SUCCESS;
    }

    public LoginBean getLoginBean() {
        if(!session.containsKey("ucBuscaBean"))
            this.setLoginBean(new LoginBean());
        return (LoginBean) session.get("ucBuscaBean");
    }

    public void setLoginBean(LoginBean loginBean) {
        this.session.put("ucBuscaBean", loginBean);
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

    public String getTipoUtilizador() {
        return tipoUtilizador;
    }

    public void setTipoUtilizador(String tipoUtilizador) {
        this.tipoUtilizador = tipoUtilizador;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
