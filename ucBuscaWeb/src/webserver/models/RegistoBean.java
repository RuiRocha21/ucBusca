package webserver.models;

import webserver.rmi.ligacaoRMI;

import java.rmi.RemoteException;

public class RegistoBean extends ligacaoRMI  {
    private String nickname;
    private String password;

    public RegistoBean(){
        super();
    }

    public String registar(){
        String resultado = "";
        try {
            resultado = server.registarUtils(nickname,password);
        }catch (RemoteException e){
            //System.out.println(tt);
            System.out.println("RMI deixou de responder (RemoteException no metodo ligacaoRMI)...");
            resultado = "";
        }
        return resultado;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
