package webserver.models;

import webserver.rmi.ligacaoRMI;
import webserver.rmi.Utilizador;
import java.rmi.RemoteException;

public class LigacaoFbBean extends ligacaoRMI {
    private String utilizador;
    private String facebookId;
    private String acessToken;
    private String password;

    public LigacaoFbBean(){
        super();
    }

    public String ligacaoFB(){
        String res = null;
        try {
            res = server.getLigacaoFB(utilizador,facebookId,acessToken);
        }catch (RemoteException re){
            System.out.println("RMI deixou de responder (RemoteException no metodo LoginFbBean)...");
        }
        return res;
    }

    public Utilizador getIdFacebook(){
        Utilizador res = null;
        try {
            res = server.getIdFacebook(facebookId);
        }catch (RemoteException re){
            System.out.println("RMI deixou de responder (RemoteException no metodo LoginFbBean)...");
        }
        return res;
    }

    public String getUtilizador() {
        return utilizador;
    }

    public void setUtilizador(String utilizador) {
        this.utilizador = utilizador;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getAcessToken() {
        return acessToken;
    }

    public void setAcessToken(String acessToken) {
        this.acessToken = acessToken;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
