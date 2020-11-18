package webserver.rmi;

import webserver.rmi.infoPesquisas;
import webserver.rmi.infoHistorico;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import ws.websocketInterface;

public interface serverInterface extends Remote {
	//estado do servidor rmi
	public boolean isAlive() throws RemoteException;																	/*meta1*/
	public void subscribe(String nickname, clientInterface clientInterface) throws RemoteException;						/*meta1*/

	//requesito 1
	/*meta1*/
	public String login(String nickname, String password, clientInterface client) throws RemoteException;				/*meta1*/			
	public String registarUtils(String nome, String password) throws RemoteException;									/*meta1*/
	public String logout(String nickname) throws RemoteException;														/*meta1*/
	public ArrayList<String> loginWEB(String email, String password) throws RemoteException;										/*meta2*/
	//requesito2 e 3
	public String indexarURL(String admin, String url) throws RemoteException;											/*meta1*/
	//requesito4 e 5
	public String pesquisaUtil(String nickname, String palavras) throws RemoteException;								/*meta1*/
	public String pesquisaAnonima(String palavras) throws RemoteException;												/*meta1*/
	public ArrayList<infoPesquisas> pesquisaUtilWeb(String nickname, String palavras) throws RemoteException;			/*meta2*/
	public ArrayList<infoPesquisas> pesquisaAnonimaWeb(String palavras) throws RemoteException;							/*meta2*/
	//requesito6
	public String linksParaOutrasPaginas(String nickname,String url) throws RemoteException;							/*meta1*/
	public ArrayList<infoHistorico> linksParaOutrasPaginasWeb(String nickname,String url) throws RemoteException;		/*meta2*/
	//requesito7
	public String consultarPesquisasUtilizador(String nickname) throws RemoteException;									/*meta1*/
	public ArrayList<infoHistorico> consultarPesquisasUtilizadorWeb(String nickname) throws RemoteException;			/*meta2*/
	//requesito8 e 9
	public String previlegioAdmin(String admin, String novoAdmin) throws RemoteException;								/*meta1*/
	void abrirWebSocket(websocketInterface websocket) throws RemoteException;
	void fecharWebSocket(websocketInterface websocket) throws RemoteException;
	
	//requesito 10, 11 e 12
	public String paginaAdmin(String nickname) throws RemoteException;													/*meta1*/
	public ArrayList<infoServidores> paginaAdminWeb(String nickname) throws RemoteException;
	//REST
	
	public Utilizador getIdFacebook(String emailFaceBook) throws RemoteException;
	public String getLigacaoFB(String nickname,String emailFB,String acessToken) throws RemoteException;
	
	

}