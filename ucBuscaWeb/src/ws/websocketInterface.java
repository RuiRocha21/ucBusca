package ws;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface websocketInterface extends Remote{
	 public void notificarUtilizador(String notificacao) throws RemoteException;
}
