package webserver.models;

import webserver.rmi.ligacaoRMI;
import webserver.rmi.infoHistorico;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class PesquisaLinksParaOutrasPaginasBean extends ligacaoRMI {
    public String utilizador;
    public String url;

    public PesquisaLinksParaOutrasPaginasBean(){
        super();
    }

    public ArrayList<infoHistorico> PesquisaLinksParaOutrasPaginas(){
        ArrayList<infoHistorico> resultado = new ArrayList<>();
        try {
            resultado =  server.linksParaOutrasPaginasWeb(utilizador,url);
        }catch (RemoteException e){
            //System.out.println(tt);
            System.out.println("RMI deixou de responder (RemoteException no PesquisaLinksParaOutrasPaginasBean)...");
        }
        return resultado;
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
}
