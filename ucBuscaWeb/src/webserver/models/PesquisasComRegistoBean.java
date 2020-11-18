package webserver.models;

import webserver.rmi.infoPesquisas;
import webserver.rmi.ligacaoRMI;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class PesquisasComRegistoBean extends ligacaoRMI {
    String utilizador;
    String tokens;

    public PesquisasComRegistoBean(){
        super();
    }

    public ArrayList<infoPesquisas> PesquisasComRegisto(){
        ArrayList<infoPesquisas> resultado = new ArrayList<>();
        try {
            resultado =  server.pesquisaUtilWeb(utilizador,tokens);
        }catch (RemoteException e){
            //System.out.println(tt);
            System.out.println("RMI deixou de responder (RemoteException no PesquisasComRegistoBean)...");
        }
        return resultado;
    }

    public String getUtilizador() {
        return utilizador;
    }

    public void setUtilizador(String utilizador) {
        this.utilizador = utilizador;
    }

    public String getTokens() {
        return tokens;
    }

    public void setTokens(String tokens) {
        this.tokens = tokens;
    }
}

