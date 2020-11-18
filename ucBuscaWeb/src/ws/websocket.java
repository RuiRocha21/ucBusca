package ws;

import webserver.rmi.serverInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.Session;

@ServerEndpoint(value = "/ws")
public class websocket extends UnicastRemoteObject implements websocketInterface{
	private static serverInterface server;
	private Session session;
	
	public websocket() throws RemoteException {
		super();
	}
	
	@OnOpen
    public void start(Session session) throws RemoteException{
        this.session = session;

        if(server == null) {
            connectRMI();
        }

        try {
            server = (serverInterface) LocateRegistry.getRegistry("localhost", 7001).lookup("rmiserver");
            server.abrirWebSocket((websocketInterface)this);
        }
        catch(NotBoundException | RemoteException e) {
            System.out.println(e.getMessage());
        }
    }

    @OnClose
    public void end() throws RemoteException {
        try {
            server.fecharWebSocket((websocketInterface) this);
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
        }
        try {
            this.session.close();
        } catch (IOException e) {

        }
    }
    @OnMessage
    public void receiveMessage(String message) throws RemoteException {
        sendMessage(message);
    }

    @OnError public void handleError(Throwable t) throws RemoteException{
        t.printStackTrace();
    }

    private void sendMessage(String text) {

        try {
            this.session.getBasicRemote().sendText(text);
        } catch (IOException e) {

            try {
                this.session.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    @Override
    public void notificarUtilizador(String notificacao) throws RemoteException {
        sendMessage(notificacao);
    }

    private void connectRMI() throws RemoteException{
        try {
            server = (serverInterface) LocateRegistry.getRegistry("localhost", 7001).lookup("rmiserver");
        }
        catch(NotBoundException | RemoteException e) {
            System.out.println(e.getMessage());
        }
    }
	
	
	
}
