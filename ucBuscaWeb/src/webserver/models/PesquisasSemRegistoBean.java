package webserver.models;

import webserver.rmi.infoPesquisas;
import webserver.rmi.ligacaoRMI;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class PesquisasSemRegistoBean extends ligacaoRMI {
    String tokens;

    public PesquisasSemRegistoBean(){
        super();
    }

    public ArrayList<infoPesquisas> PesquisasSemRegisto(){
        java.util.ArrayList<webserver.rmi.infoPesquisas> resultado = new ArrayList<>();
        try {
            resultado =  server.pesquisaAnonimaWeb(tokens);
        }catch (RemoteException e){
            //System.out.println(tt);
            System.out.println("RMI deixou de responder (RemoteException no PesquisasSemRegistoBean)...");
        }
        return resultado;
    }

    public String getTokens() {
        return tokens;
    }

    public void setTokens(String tokens) {
        this.tokens = tokens;
    }
}
