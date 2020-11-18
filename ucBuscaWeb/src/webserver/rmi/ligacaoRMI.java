package webserver.rmi;

import java.io.Serializable;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import static java.lang.Thread.sleep;

public class ligacaoRMI implements Serializable {

	public serverInterface server = null;
	    /**
	     *
	     * @return valid serverInterface
	     */

	public ligacaoRMI() {

		boolean exit = false;

		while(!exit) {
			try {
				sleep((long) (1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				server = (serverInterface) LocateRegistry.getRegistry("localhost", 7001).lookup("rmiserver");
				exit = true;
			} catch (ConnectException e) {
				System.out.println("RMI deixou de responder (ConnectException no metodo ligacaoRMI)...");
			} catch (NotBoundException v) {
				System.out.println("RMI deixou de responder (NotBoundException no metodo ligacaoRMI)...");
			} catch (RemoteException tt) {
				//System.out.println(tt);
				System.out.println("RMI deixou de responder (RemoteException no metodo ligacaoRMI)...");
			}
		}
	}
}
