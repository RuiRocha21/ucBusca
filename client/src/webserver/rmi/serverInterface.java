package webserver.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface serverInterface extends Remote {
	//estado do servidor rmi
	public boolean isAlive() throws RemoteException;
	public void subscribe(String nickname, clientInterface clientInterface) throws RemoteException;

	//requesito 1
	public String login(String nickname, String password, clientInterface client) throws RemoteException;
	public String registarUtils(String nome, String password) throws RemoteException;
	public String logout(String nickname) throws RemoteException;
	public String loginWEB(String email, String password) throws RemoteException;
	//requesito2 e 3
	public String indexarURL(String admin, String url) throws RemoteException;
	//requesito4 e 5
	public String pesquisaUtil(String nickname, String palavras) throws RemoteException;
	public String pesquisaAnonima(String palavras) throws RemoteException;

	//requesito6
	public String linksParaOutrasPaginas(String nickname,String url) throws RemoteException;
	//requesito7
	public String consultarPesquisasUtilizador(String nickname) throws RemoteException;
	//requesito8 e 9
	public String previlegioAdmin(String admin, String novoAdmin) throws RemoteException;

	//requesito 10, 11 e 12
	public String paginaAdmin(String nickname) throws RemoteException;

	//falta REST

}