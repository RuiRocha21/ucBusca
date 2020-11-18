package webserver.models;

import webserver.rmi.infoServidores;
import webserver.rmi.ligacaoRMI;

import java.rmi.RemoteException;
import java.util.ArrayList;


public class paginaAdminWebBean extends ligacaoRMI {
    String admin;

    public paginaAdminWebBean(){
        super();
    }

    public ArrayList<infoServidores> paginaAdminWeb(){
        ArrayList<infoServidores> resultado = new ArrayList<>();
        try {
            resultado =  server.paginaAdminWeb(admin);
        }catch (RemoteException e){
            //System.out.println(tt);
            System.out.println("RMI deixou de responder (RemoteException no paginaAdminWebBean)...");
        }
        return resultado;

    }


    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}
