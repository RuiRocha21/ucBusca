package webserver.models;

import webserver.rmi.ligacaoRMI;

import java.io.Serializable;
import java.rmi.RemoteException;

public class IndexarUrlBean extends ligacaoRMI implements Serializable {
    String admin;
    String url;

    public IndexarUrlBean(){
        super();
    }

    public String indexarUrl(){
        String resultado = "";
        try {
            resultado =  server.indexarURL(admin,url);
        }catch (RemoteException e){
            //System.out.println(tt);
            System.out.println("RMI deixou de responder (RemoteException no IndexarUrlBean)...");
        }
        return resultado;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
