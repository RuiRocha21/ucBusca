package webserver.models;

import webserver.rmi.ligacaoRMI;
import webserver.rmi.infoHistorico;
import java.rmi.RemoteException;
import java.util.ArrayList;


public class PesquisaHistoricoBean extends ligacaoRMI {
    String utilizador;

    public PesquisaHistoricoBean(){
        super();
    }

    public ArrayList<infoHistorico> HistoricoPesquisas(){
        ArrayList<infoHistorico> resultado = new ArrayList<>();
        try {
            resultado =  server.consultarPesquisasUtilizadorWeb(utilizador);
        }catch (RemoteException e){
            //System.out.println(tt);
            System.out.println("RMI deixou de responder (RemoteException no PesquisaHistoricoBean)...");
        }
        return resultado;
    }

    public String getUtilizador() {
        return utilizador;
    }

    public void setUtilizador(String utilizador) {
        this.utilizador = utilizador;
    }
}
