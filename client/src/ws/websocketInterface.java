package ws;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface websocketInterface extends Remote{
    public void notificarUtilizador(String admin, String novoAdmin) throws RemoteException;
    public void ActualizarServidores() throws RemoteException;
}