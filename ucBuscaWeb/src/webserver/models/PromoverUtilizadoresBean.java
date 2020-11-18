package webserver.models;

import webserver.rmi.ligacaoRMI;

import java.rmi.RemoteException;

public class PromoverUtilizadoresBean extends ligacaoRMI {
    String novoAdmin;
    String admin;

    public PromoverUtilizadoresBean(){
        super();
    }

    public String promoverUtilizador(){
        String resultado = "";
        try {
            resultado =  server.previlegioAdmin(admin,novoAdmin);
        }catch (RemoteException e){
            //System.out.println(tt);
            System.out.println("RMI deixou de responder (RemoteException no PromoverUtilizadoresBean)...");
        }
        return resultado;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getNovoAdmin() {
        return novoAdmin;
    }

    public void setNovoAdmin(String novoAdmin) {
        this.novoAdmin = novoAdmin;
    }
}
