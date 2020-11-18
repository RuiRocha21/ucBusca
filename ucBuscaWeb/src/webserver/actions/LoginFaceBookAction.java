package webserver.actions;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import webserver.models.LigacaoFbBean;
import webserver.rmi.Utilizador;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class LoginFaceBookAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4231L;
    private Map<String, Object> session;
    public String code = null;
    private final String NETWORK_NAME = "Facebook";
    private final String URL = "https://graph.facebook.com/me";
    private static final String API_APP_KEY = "543271353070225";
    private static final String API_APP_SECRET = "cef67b866dfed36903cc6c9ec8dada76";

    @Override
    public String execute() {
        // Build service to send request to Facebook
        final OAuth20Service service = new ServiceBuilder(API_APP_KEY)
                .apiSecret(API_APP_SECRET)
                .callback("http://127.0.0.1:8080/loginFacebook")
                .scope("publish_actions")
                .build(FacebookApi.instance());

        final OAuth2AccessToken accessToken;
        try {
            accessToken = service.getAccessToken(code);

            // Get user id and name
            final OAuthRequest request = new OAuthRequest(Verb.GET, URL);
            service.signRequest(accessToken, request);

            // Get response from API
            final Response response = service.execute(request);

            // Parse response
            JSONObject body = (JSONObject) JSONValue.parse(response.getBody());

            this.getLoginFbBean().setUtilizador((String) session.get("UTILIZADOR"));
            this.getLoginFbBean().setFacebookId((String) body.get("id"));
            Utilizador util = this.getLoginFbBean().getIdFacebook();

            if (util != null) {
                this.getLoginFbBean().setUtilizador(util.getNickname());
                this.getLoginFbBean().setPassword(util.getPassword());
                session.put("username", util.getNickname());
                session.put("password", util.getPassword());
                session.put("ADMIN", util.getAdmin());
                session.put("LOGGED_IN", true);
                return SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return LOGIN;
    }



    public LigacaoFbBean getLoginFbBean() {
        if(!session.containsKey("LoginFaceBookBean")) {
            this.setLoginFbBean(new LigacaoFbBean());
        }
        return (LigacaoFbBean) session.get("LoginFaceBookBean");
    }
    public void setLoginFbBean(LigacaoFbBean ligacao) {
        this.session.put("LoginFaceBookBean", ligacao);
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

}
