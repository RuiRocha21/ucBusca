package webserver.rmi;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface clientInterface extends Remote{
	public void imprimeClienteAutenticado(String msg) throws RemoteException;
    public String getNickname() throws RemoteException;
}
