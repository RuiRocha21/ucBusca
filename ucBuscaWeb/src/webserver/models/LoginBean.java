package webserver.models;

import webserver.rmi.ligacaoRMI;

import java.rmi.RemoteException;
import java.util.ArrayList;


public class LoginBean extends ligacaoRMI {
    private String utilizador;
    private String password;

    public LoginBean(){
        super();
    }

    

    public ArrayList<String> loginUtil(){
        ArrayList<String> resultado = new ArrayList<>();
        try{
            resultado =  server.loginWEB(utilizador,password);
        }catch (RemoteException e){
            //System.out.println(tt);
            System.out.println("RMI deixou de responder (RemoteException no metodo LoginBean)...");
        }
        return resultado;
    }


    public String getUtilizador() {
        return utilizador;
    }

    public void setUtilizador(String utilizador) {
        this.utilizador = utilizador;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
