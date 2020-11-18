package webserver.rmi;
import webserver.rmi.infoPesquisas;
import webserver.rmi.infoServidores;
import ws.websocketInterface;
import webserver.rmi.Utilizador;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.model.Verifier;
import com.github.scribejava.core.oauth.OAuthService;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.*;
import com.github.scribejava.apis.FacebookApi;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import uc.sd.apis.FacebookApi2;


import static java.lang.Thread.sleep;

public class server extends UnicastRemoteObject implements serverInterface{
	private ConcurrentHashMap<String, clientInterface> utilsActivos = new ConcurrentHashMap<String, clientInterface>();
	private static final long serialVersionUID = 1L;
	private static serverInterface servidorRMI;
	public static int contadorGlobal = 0;
	public static ArrayList<String> hashMulticast = new ArrayList<>();
	private static String ENDERESSO_MULTICAST = "224.3.2.100";
    private static int PORTO_ENVIO = 5213, PORT_RECEBE = 5214;
    private static MulticastSocket socket = null;
    private static InetAddress group = null;
    private static String ip;
    private static final Set<websocketInterface> webSockets = new CopyOnWriteArraySet<>();
    private static final HashMap<String,websocketInterface> admins = new HashMap<String,websocketInterface>();
    private static OAuthService service;
    private static final String API_APP_KEY = "543271353070225";
    private static final String API_APP_SECRET = "cef67b866dfed36903cc6c9ec8dada76";
    private HashMap<String, String> tokens = new HashMap<String,String>();
    
	public server() throws RemoteException {
        super();
    }
	
	/****************************** meta 1 ***********************************/
	
	
	/**
     * metodo para fazer ping ao servidor principal
     * 
     */
	public boolean isAlive() throws RemoteException {
        return true;
    }
	
	/**
     * Metodo para tratar a string recebida por multicast
     * @param msg mensagem recebida por multicast
     */
	static ArrayList<String[]> limparMensagem(String msg) {

        String[] tokens = msg.split(";");
        String[] p;

        ArrayList<String[]> msgTratada = new ArrayList<String[]>();

        for (int i = 0; i < tokens.length; i++) {
            p = tokens[i].split(Pattern.quote("|"));
            msgTratada.add(p);
        }
        return msgTratada;
    }
	
	/**
     * metodo para enviar mensagem por multicast
     * @param resp mensagem aa enviar para a rede multicast
     */
	public static void enviarDatagramaUDP(String resp) {


        if (contadorGlobal == hashMulticast.size()) contadorGlobal = 0;
        if (hashMulticast.size() > 0)
            resp += "hash|" + hashMulticast.get(contadorGlobal++);


        enviarDatagramaParaGrupoMult(resp);

    }
	
	/**
     * metodo para receber mensagem da rede multicast com as normas do protocolo
     * @param msg mensagem a receber pela rede de servidores multicast
     * @return message mensagem recebida pela rede multicast
     */
	public static String recebeDatagramaUDP(String msg) {

        String message = null;
        boolean exit = false;
        while (!exit) {
            try {

                socket = new MulticastSocket(PORT_RECEBE);  // cria um socket
                group = InetAddress.getByName(ENDERESSO_MULTICAST);
                socket.joinGroup(group);

                socket.setSoTimeout(1000);


                byte[] buffer = new byte[65535];

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message: " + message);
                socket.leaveGroup(group);
                
                exit = true;

            } catch (SocketTimeoutException te) {
                System.out.println("Multicast nao responde.....");
                enviarDatagramaUDP(msg);
            }catch (IOException e){
            	e.printStackTrace();
            }finally {
            	socket.close();
            }
        }
        return message;
    }
	
	/**
     * metodo para enviar mensagem por multicast
     * <br>mensagem do protocolo :
     * <br>resquest:
     * <br>flag | s ; type | connectionrequest;
     * <br>response:
     * <br>flag | r ; type | ack;
     * @param resp mensagem a enviar para a rede multicast
     */
	public static void enviarDatagramaParaGrupoMult(String resp) {


        try {

            MulticastSocket socket = new MulticastSocket();
            byte[] buffer = resp.getBytes();

            InetAddress group = InetAddress.getByName(ENDERESSO_MULTICAST);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORTO_ENVIO);
            socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
	
	/**
     * metodo para identificar os utilizadores ligados
     * @param nickname nome de utilizador
     * @param clientInterface interface do cliente
     */
	public void subscribe(String nickname, clientInterface clientInterface) {
        this.utilsActivos.put(nickname, clientInterface);
    }
	
	/**
     * metodo para o utilizador se ligar ao sistema
     * <br>mensagem do protocolo :
     * <br>request: 
     * <br>flag | id ; type | login ;nickname nickname ; password | password;
     * <br>response:
     * <br>flag | id ; type | login ; result | s ; admin | s ; nickname | nickname ; numero_notificacoes |0;
     * <br>flag | id ; type | login ; result | s ; admin | s ; nickname | nickname ; numero_notificacoes |x; notificacao | notificacao1; notificacao | notificacao2; ….notificacao | notificacao n;
	 * <br>flag | id ; type | login ; result | s ; admin | n ; nickname | nickname ; numero_notificacoes |0;
	 * <br>flag | id ; type | login ; result | s ; admin | n ; nickname | nickname ; numero_notificacoes | x; notificacao | notificacao1; notificacao | notificacao2;
	 * <br>flag | id ; type | login ; result | n ; admin | n ; nickname | nickname ;
     * @param nickname nome de utilizador
     * @param password password do utilizador
     * @param client interface do cliente
     */
	public String login(String nickname, String password, clientInterface client) {
        String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));

        String msg = "flag|"+id+";type|login;nickname|" + nickname + ";password|" + password + ";";
        boolean exit = false;
        String rspToClient = "";
        enviarDatagramaUDP(msg);
        
        while (!exit) {
            String rsp = recebeDatagramaUDP(msg);
            ArrayList<String[]> msgTratada = limparMensagem(rsp);

            if (msgTratada.get(0)[1].equals(id)) {
            	
            	switch(msgTratada.get(2)[1]) {
            		case "sim":
            			switch(msgTratada.get(3)[1]) {
	                    	case "nao":
	                    		System.out.println("msgTratada.get(2)[1]  1 " + msgTratada.get(3)[1]);
	                    		int numNotifications = Integer.parseInt(msgTratada.get(5)[1]);
	    	                    
	    	                    rspToClient = "utilizador " + nickname;
	    	                    subscribe(nickname, client);
	    	
	    	                    if (numNotifications > 0) {
	    	                        rspToClient += "\notificacoes:\n";
	    	                        for (int i = 0; i < numNotifications; i++) {
	    	                            rspToClient += msgTratada.get(6 + i)[1] + "\n";
	    	                        }
	    	                    }
	    	                    return rspToClient;
	    	                   
	                    	case "sim":
	                    		System.out.println("msgTratada.get(2)[1]  2 " + msgTratada.get(3)[1]);
	                    		 numNotifications = Integer.parseInt(msgTratada.get(5)[1]);
	                    			
	     	                    rspToClient = "admin " + nickname;
	     	                    subscribe(nickname, client);
	     	
	     	                    if (numNotifications > 0) {
	     	                        rspToClient += "\notificacoes:\n";
	     	                        for (int i = 0; i < numNotifications; i++) {
	     	                            rspToClient += msgTratada.get(6 + i)[1] + "\n";
	     	                        }
	     	                    }
	     	                   
	     	                   return rspToClient;
	     	                default:
	     	                	System.out.println("msgTratada.get(2)[1]  3 " + msgTratada.get(3)[1]);
	     	                	
	     	                	rspToClient = "credenciais erradas, tente de novo!"; 
	     	                	return rspToClient;
            			}
            		case "nao":
            			System.out.println("msgTratada.get(2)[1]  4" + msgTratada.get(2)[1]);
 	                	
 	                	rspToClient = "credenciais erradas, tente de novo!"; 
 	                	return rspToClient;
            		default:
            			System.out.println("msgTratada.get(2)[1]  5  " + msgTratada.get(2)[1]);
 	                	
 	                	rspToClient = "credenciais erradas, tente de novo!"; 
 	                	return rspToClient;
            	}
                    		
        	}else {
        		System.out.println("msgTratada.get(2)[1]  6" + msgTratada.get(3)[1]);
        		
        		rspToClient = "credenciais erradas, tente de novo!"; 
        		
        	}
            exit = true;  
        }
        
        return rspToClient;
        
    }
	
	
	
	
	/**
     * metodo para o utilizador se registar ao sistema
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag id ; type | register ; nickname | nome ; password | password;
     * <br>response:
     * <br>flag | id ; type | register; result | n; nickname | nickname ; password | password
     * <br>flag | id ; type | register; result | s; nickname | nickname ; password | password
     * @param nome nome de utilizador
     * @param password password de utilizador
     */
	public String registarUtils(String nome, String password) {


        String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));
        String msg = "flag|"+id+";type|register;nickname|" + nome + ";password|" + password + ";"; // msg de protocolo para registo
        boolean sair = false;
        String respostaParaCliente = "Falha no registo";
        
       enviarDatagramaUDP(msg);

        while (!sair) {
            String rsp = recebeDatagramaUDP(msg);
            ArrayList<String[]> msgTratada = limparMensagem(rsp);

            if (msgTratada.get(0)[1].equals(id)) {
                if (msgTratada.get(2)[1].equals("s")) {
                	respostaParaCliente = "Utilizador " + nome + " registado com sucesso";

                } else {
                	respostaParaCliente = msgTratada.get(5)[1]; // resulto n, caso ocorra erro
                }
                sair = true;
            }
        }
        return respostaParaCliente;
    }
	
	/**
     * metodo para o utilizador sair do sistema
     * @param nickname nome de utilizador
     */
	public String logout(String nickname) {
		utilsActivos.remove(nickname);
        return "Acabou de sair do Sistema. Volte sempre!";
    }
	
	/**
     * metodo para dar privilegios de utilizador
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | privilege ; admin | admin ;util | novoAdmin ;
     * <br>flag | idNotify ;type | notifyfail ; nickname | novoAdmin ; message | "Foi promovido a Administrador por " ;
     * <br>response:
     * <br>flag | id ; type | privilege ; result | s ; admin | admin ; util | nickname
     * <br>flag | id ; type | privilege ; result | n ; admin | admin ; util | nickname
     * @param admin nome do admin
     * @param novoAdmin nome do utilizador
     */
	public String previlegioAdmin(String admin, String novoAdmin) {

        String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));

        String uuidNotify = UUID.randomUUID().toString();
        String idNotify = uuid.substring(0, Math.min(uuid.length(), 8));

        String msg = "flag|"+id+";type|privilege;admin|" + admin + ";util|" + novoAdmin + ";";
        boolean exit = false;
        String respostaParaClienteRMI = "Falha ao comunicar com " + novoAdmin + " para novo Admin";
        
        enviarDatagramaUDP(msg);

        while (!exit) {
            String resposta = recebeDatagramaUDP(msg);
            ArrayList<String[]> mensagemTratada = limparMensagem(resposta);

            if (mensagemTratada.get(0)[1].equals(id)) {
                System.out.println(mensagemTratada.get(2)[1]);
                if (mensagemTratada.get(2)[1].equals("s")) {
                    System.out.println("Sucesss");
                    respostaParaClienteRMI = novoAdmin + " promovido a Admnistrador com successo";
                    // notificar utilizador
                    try {
                    	enviarNotificacaoWebsocket(respostaParaClienteRMI);
                    	utilsActivos.get(novoAdmin).imprimeClienteAutenticado("promovido a Admnistrador por " + admin);
                    } catch (RemoteException re) {

                    	utilsActivos.remove(novoAdmin);
                        System.out.println("utlizador desligado");
                        enviarDatagramaUDP("flag|"+idNotify+";type|notifyfail;nickname|" + novoAdmin + ";message|" + "Foi promovido a Admnistrador por " + admin + ";");

                    } catch (NullPointerException npe) {

                        System.out.println("utlizador desligado");
                        enviarDatagramaUDP("flag|"+idNotify+";type|notifyfail;nickname|" + novoAdmin + ";message|" + "Foi promovido a Admnistrador por " + admin + ";");

                    }
                } else {
                	respostaParaClienteRMI = mensagemTratada.get(mensagemTratada.size()-2)[1];
                }
                exit = true;
            }
        }
        return respostaParaClienteRMI;
    }
	
	/**
     * metodo para pesquisar links para uma pagina especifica
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | searchLinksForPage ; nickname | nickname ; url | url ;
     * <br>response:
     * <br>flag | id ; type | searchLinksForPage ; result|n ;nickname | nickname;
     * <br>flag | id ; type | searchLinksForPage ; result|n ;nickname | nickname; conta n; URL| url1; ....URL|n;
     * @param nickname nome do utilizador
     * @param url url da pagina
     */
	public String linksParaOutrasPaginas(String nickname,String url) {
		String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));
        String msg = "flag|"+id+";type|searchLinksForPage;util|"+nickname+";URL|"+url+";";
        String respostaParaClienteRMI = "";
        Boolean exit = false;
        enviarDatagramaUDP(msg);
        while(!exit) {
        	String respostaMC = recebeDatagramaUDP(msg);
        	ArrayList<String[]> mensagemTratada = limparMensagem(respostaMC);
        	if(mensagemTratada.get(0)[1].equals(id)) {
        		if (mensagemTratada.get(2)[1].equals("s")) {
        			int nItems = Integer.parseInt(mensagemTratada.get(4)[1]);
        			int corta = 0;
        			if(nItems > 100) {
        				corta = nItems/4;
        			}else {
        				corta = nItems;
        			}
	        		respostaParaClienteRMI += "encontrado " + corta + " links associado ao link " + url + "\n";
	        		int i = 0;
	        		
	        		while(i<corta) {
	        			respostaParaClienteRMI += mensagemTratada.get(5+i)[1] +"\n"; 
	        			i++;
	        			if(mensagemTratada.get(5+i)[0].equals("hash")){
	        				return respostaParaClienteRMI;
	        			}
	        		}	
        		}
        		exit = true;
        		return respostaParaClienteRMI;
        	}
        }
        return respostaParaClienteRMI;
	}
	
	/**
     * metodo para pesquisar as pesquisas que um utilizador efectuou
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | searchHistoric ; nickname | nickname ;
     * <br>response:
     * <br>flag | id ; type | searchHistoric ; result | n; nickname | nickname ; numeroLinks | 0 ; lista | historico vazio;
     * <br>flag | id ; type | searchHistoric ; result | s; nickname | nickname ; numeroLinks | n ; lista | url| url 1; | url 2; ….| url n
     * @param nickname nome de utilizador
     */
	public String consultarPesquisasUtilizador(String nickname) throws RemoteException{
		String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));

        String msg = "flag|"+id+";type|searchHistoric;nickname|"+nickname+";";
        String respostaParaCliente = "";
        enviarDatagramaUDP(msg);
        boolean exit = false;
        while(!exit) {
        	String respostaMC = recebeDatagramaUDP(msg);
        	ArrayList<String[]> mensagemTratada = limparMensagem(respostaMC);
        	if(mensagemTratada.get(0)[1].equals(id)) {
	        	if (mensagemTratada.get(2)[1].equals("s")) {
	        		int nItems = Integer.parseInt(mensagemTratada.get(4)[1]);
	        		respostaParaCliente += "pesquisas efectuadas pelo utilizador "+nickname+"\n";
	        		
	        		for(int i = 6; i <=mensagemTratada.size()-1;i++) {
	        			respostaParaCliente += mensagemTratada.get(i-1)[1] ; 
	        		}
	        	}
	        	exit = true;
        	}
        	
        	
        }
        
        return respostaParaCliente;
	}
	
	/**
     * metodo para indexar uma pagina
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | indexManual ; admin | admin ; URL | url;
     * <br>response:
     * <br>flag | id ; type | indexManual ; result | s ; nickname | admin;
     * <br>flag | id ; type | indexManual ; result | n ; nickname | admin;
     * @param admin nome do admin
     * @param url da pagina
     */
	public String indexarURL(String admin, String url) throws RemoteException{
		String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));

        String msg = "flag|"+id+";type|index;type|"+admin+";URL|"+url+";";
        String respostaParaCliente = "";
        enviarDatagramaUDP(msg);
        boolean exit = false;
        while(!exit) {
        	String respostaMC = recebeDatagramaUDP(msg);
            ArrayList<String[]> mensagemTratada = limparMensagem(respostaMC);
            
            if(mensagemTratada.get(0)[1].equals(id)) {
            	if(mensagemTratada.get(2)[1].equals("s")){
            		respostaParaCliente = "url indexado com sucesso!";
            	}
            	if(mensagemTratada.get(2)[1].equals("n")){
            		respostaParaCliente = "o url nao foi indexado!";
            	}
            	exit = true;
            }
            
        }
		return respostaParaCliente;
	}
	/**
     * metodo para consultar os dados do sistema
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | updateSystem ; admin | nickname ;
     * <br>response:
     * <br>flag | id ; type | updateSystem ; result | s ; admin | admin ; numAdmin contaNumAdmins ; numUtils contaNumUtils ; NumLinksBD | contaNumLinksBD NumPesquisasTotal | contaNumPesquisas ;conta | n ; url | url1 …; url | url2 ; nserv | n; ip|ip1 ;port | port1;
     * @param nickname nome do admin
     */
	public String paginaAdmin(String nickname) throws RemoteException{
		String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));
        
        String msg = "flag|"+id+";type|updateSystem;admin|"+nickname+";";
        String respostaParaCliente = "";
        enviarDatagramaUDP(msg);
        boolean exit = false;
		
        while(!exit) {
        	String respostaMC = recebeDatagramaUDP(msg);
        	ArrayList<String[]> mensagemTratada = limparMensagem(respostaMC);
        	if(mensagemTratada.get(0)[1].equals(id)) {
        		if (mensagemTratada.get(2)[1].equals("s")) {

        			int nServidores = Integer.parseInt(mensagemTratada.get(4)[1]);
        			respostaParaCliente += "Servidores Activos \n";
        			for(int i = 6; i <6+nServidores;i++) {
        				respostaParaCliente +=mensagemTratada.get(i-1)[1] + "\n"; 
	        		}
        			respostaParaCliente +="\n";
        			int nPesquisas = Integer.parseInt(mensagemTratada.get(5+nServidores)[1]);
        			respostaParaCliente += "Pesquisas Mais Importantes \n";
        			for(int i = 6+nServidores+1; i <6+nServidores+nPesquisas+1;i++) {
        				respostaParaCliente +=mensagemTratada.get(i-1)[1] + "\n"; 
	        		}
        			respostaParaCliente +="\n";
        			int nLigacoes = Integer.parseInt(mensagemTratada.get(6+nServidores+nPesquisas)[1]);
        			respostaParaCliente += "URLs com mais ligacoes \n";
        			for(int i = 6+nServidores+nPesquisas+1+1; i <=mensagemTratada.size()-1;i++) {
        				respostaParaCliente +=mensagemTratada.get(i-1)[1] + "\n"; 
	        		}
        		}
        		exit = true;
        	}
        }
		
		return respostaParaCliente;
	}
	
	/**
     * metodo para um utilizador comum efectuar pesquisas
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | searchUtil ; util l nickname ; words | palavras;
     * <br>response:
     * <br>flag | id “ ; type | searchUtil ; result | s ; nickname | nickname ; links | n; URL | url 1; TITULO | titulo1; TEXTO | texto1 ; … URL | url n; TITULO | titulo n; TEXTO | texto ;
     * @param nickname nome do admin
     * @param palavras texto a pesquisar
     */
	public String pesquisaUtil(String nickname, String palavras) throws RemoteException{
		String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));
        
        String msg = "flag|"+id+";type|searchUtil;util|"+nickname+";words|"+palavras+";";
        String respostaParaCliente = "";
        enviarDatagramaUDP(msg);
        boolean exit = false;
        while(!exit) {
        	String respostaMC = recebeDatagramaUDP(msg);
        	ArrayList<String[]> mensagemTratada = limparMensagem(respostaMC);
    		if(mensagemTratada.get(0)[1].equals(id)) {
	    		if (mensagemTratada.get(2)[1].equals("s")) {
	    			for(int i=6;i<=mensagemTratada.size()-1;i++) {
	    				respostaParaCliente +=mensagemTratada.get(i-1)[0] + "  "+mensagemTratada.get(i-1)[1] ;
	    			}
	    			
	    			return respostaParaCliente;
	    		}
	    		else {
	    			respostaParaCliente +="nao existe paginas com esse texto";
	    			return respostaParaCliente;
	    		}
    		}
        }
        return respostaParaCliente;
	}
	/**
	 * mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | anounymous ; util | anounymous ; words | palavras ;
     * <br>response:
     * <br>flag | id “ ; type | searchAnonymous ; result | n ;
     * metodo para um utilizador sem registo efectuar pesquisas
     * @param palavras texto a pesquisar
     */
	public String pesquisaAnonima(String palavras) throws RemoteException{
		String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));
        String nickname = "utilizadorNaoRegistado";
        String msg = "flag|"+id+";type|anounymous;util|"+nickname+";words|"+palavras+";";
        String respostaParaCliente = "";
        enviarDatagramaUDP(msg);
        boolean exit = false;
        while(!exit) {
        	String respostaMC = recebeDatagramaUDP(msg);
        	ArrayList<String[]> mensagemTratada = limparMensagem(respostaMC);
    		if(mensagemTratada.get(0)[1].equals(id)) {
	    		if (mensagemTratada.get(2)[1].equals("s")) {
	    			for(int i=6;i<=mensagemTratada.size()-1;i++) {
	    				respostaParaCliente +=mensagemTratada.get(i-1)[0] + "  "+mensagemTratada.get(i-1)[1] ;
	    			}
	    			
	    			return respostaParaCliente;
	    		}
	    		else {
	    			respostaParaCliente +="nao existe paginas com esse texto";
	    			return respostaParaCliente;
	    		}
    		}
        }
        return respostaParaCliente;
	}
	
	/*--------------------------------------------metodos meta2------------------------------------*/
	
	/**
     * metodo para o utilizador se ligar ao sistema
     * <br>mensagem do protocolo :
     * <br>request: 
     * <br>flag | id ; type | login ;nickname nickname ; password | password;
     * <br>response:
     * <br>flag | id ; type | login ; result | s ; admin | s ; nickname | nickname ; numero_notificacoes |0;
     * <br>flag | id ; type | login ; result | s ; admin | s ; nickname | nickname ; numero_notificacoes |x; notificacao | notificacao1; notificacao | notificacao2; ….notificacao | notificacao n;
	 * <br>flag | id ; type | login ; result | s ; admin | n ; nickname | nickname ; numero_notificacoes |0;
	 * <br>flag | id ; type | login ; result | s ; admin | n ; nickname | nickname ; numero_notificacoes | x; notificacao | notificacao1; notificacao | notificacao2;
	 * <br>flag | id ; type | login ; result | n ; admin | n ; nickname | nickname ;
     * @param nickname nome de utilizador
     * @param password password do utilizador
     * */
	
	public ArrayList<String> loginWEB(String nickname, String password) {
		//inserir numa arrylist as notificacoes para passar para o webserver
        String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));
        String msg = "flag|"+id+";type|login;nickname|" + nickname + ";password|" + password + ";";
        boolean exit = false;
        ArrayList<String> resposta = new ArrayList<>();
        String notificacao ="";
        String rsp = "";
        enviarDatagramaUDP(msg);    
        while (!exit) {
        	
        	rsp = recebeDatagramaUDP(msg);

            ArrayList<String[]> msgTratada = limparMensagem(rsp);
            if (msgTratada.get(0)[1].equals(id)) {
            	switch(msgTratada.get(2)[1]) {
            		case "sim":
            			switch(msgTratada.get(3)[1]) {
	                    	case "nao":
	                    		int numNotifications = Integer.parseInt(msgTratada.get(5)[1]);
	                    		resposta.add("utilizador " + nickname);
	    	                    if (numNotifications > 0) {
	    	                        try {
	    	                        	enviarNotificacaoWebsocket("notificacoes:\n");
	    	                        } catch (RemoteException e) {
	    	                            e.printStackTrace();
	    	                        }
	    	                        
	    	                        for (int i = 0; i < numNotifications; i++) {
	    	                        	notificacao = msgTratada.get(6 + i)[1] + "\n";
	    	                        	resposta.add(notificacao);
	    	                        }
	    	                        try {
	    	                        	enviarNotificacaoWebsocket(notificacao);
	    	                        } catch (RemoteException e) {
	    	                            e.printStackTrace();
	    	                        }
	    	                    }
	    	                    
	    	                    return resposta;                   
	                    	case "sim":
	                    		numNotifications = Integer.parseInt(msgTratada.get(5)[1]);             			
	     	                    //aqui inserir num hashmap de admin para callbacks
	     	                   resposta.add("admin " + nickname);
	     	                    if (numNotifications > 0) {
	     	                       resposta.add("notificacoes:\n");;
	     	                        for (int i = 0; i < numNotifications; i++) {
	     	                        	notificacao = msgTratada.get(6 + i)[1] + "\n";
	     	                        	resposta.add(notificacao);
	     	                        }
	     	                       try {
	    	                        	enviarNotificacaoWebsocket(notificacao);
	    	                        } catch (RemoteException e) {
	    	                            e.printStackTrace();
	    	                        }
	     	                    }
	     	                   
	     	                   return resposta; 
            			}
            		case "nao":
            			resposta.add("credenciais erradas, tente de novo!");
 	                	return resposta;
            		
            	}                    		
        	}
            exit = true;  
        }       
        return resposta;      
    }
	
	/**
     * metodo para um utilizador comum efectuar pesquisas
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | searchUtil ; util l nickname ; words | palavras;
     * <br>response:
     * <br>flag | id “ ; type | searchUtil ; result | s ; nickname | nickname ; links | n; URL | url 1; TITULO | titulo1; TEXTO | texto1 ; … URL | url n; TITULO | titulo n; TEXTO | texto ;
     * @param nickname nome do admin
     * @param palavras texto a pesquisar
     */
	
	public ArrayList<infoPesquisas> pesquisaUtilWeb(String nickname, String palavras) throws RemoteException{
		String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));
        
        String msg = "flag|"+id+";type|searchUtil;util|"+nickname+";words|"+palavras+";";
        enviarDatagramaUDP(msg);
		ArrayList<infoPesquisas> resposta = new ArrayList<>();
		
		boolean exit = false;
        while(!exit) {
        	String respostaMC = recebeDatagramaUDP(msg);
        	ArrayList<String[]> mensagemTratada = limparMensagem(respostaMC);
    		if(mensagemTratada.get(0)[1].equals(id)) {
	    		if (mensagemTratada.get(2)[1].equals("s")) {
	    			int nResultados = Integer.parseInt(mensagemTratada.get(4)[1]);
	    			for(int i=6;i<mensagemTratada.size()-1;i++) {
	    				String resPesquisa = mensagemTratada.get(i-1)[1];
	    				infoPesquisas novo = new infoPesquisas(resPesquisa);
	    				resposta.add(novo);
	    			}
	    			enviarNotificacaoWebsocket("nova pesquisa em sistema");
	    		}
	    		exit = true;
    		}
        }
        for(int i = 0 ; i< resposta.size();i++) {
        	System.out.println("conteudo para web "+ resposta.get(i).geInfoPesquisa());
        }
		return resposta;
	}
	
	/**
	 * mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | anounymous ; util | anounymous ; words | palavras ;
     * <br>response:
     * <br>flag | id “ ; type | searchAnonymous ; result | n ;
     * metodo para um utilizador sem registo efectuar pesquisas
     * @param palavras texto a pesquisar
     */
	
	public ArrayList<infoPesquisas> pesquisaAnonimaWeb(String palavras) throws RemoteException{
		String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));
        String nickname = "utilizadorNaoRegistado";
        String msg = "flag|"+id+";type|anounymous;util|"+nickname+";words|"+palavras+";";
        enviarDatagramaUDP(msg);
		ArrayList<infoPesquisas> resposta = new ArrayList<>();
		
		boolean exit = false;
        while(!exit) {
        	String respostaMC = recebeDatagramaUDP(msg);
        	ArrayList<String[]> mensagemTratada = limparMensagem(respostaMC);
    		if(mensagemTratada.get(0)[1].equals(id)) {
	    		if (mensagemTratada.get(2)[1].equals("s")) {
	    			int nResultados = Integer.parseInt(mensagemTratada.get(4)[1]);
	    			for(int i=6;i<mensagemTratada.size()-1;i++) {
	    				String resPesquisa = mensagemTratada.get(i-1)[1];
	    				infoPesquisas novo = new infoPesquisas(resPesquisa);
	    				resposta.add(novo);
	    			}
	    		}
	    		exit = true;
    		}
        }		
		return resposta;
	}
	
	/**
     * metodo para pesquisar as pesquisas que um utilizador efectuou
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | searchHistoric ; nickname | nickname ;
     * <br>response:
     * <br>flag | id ; type | searchHistoric ; result | n; nickname | nickname ; numeroLinks | 0 ; lista | historico vazio;
     * <br>flag | id ; type | searchHistoric ; result | s; nickname | nickname ; numeroLinks | n ; lista | url| url 1; | url 2; ….| url n
     * @param nickname nome de utilizador
     */
	
	public ArrayList<infoHistorico> consultarPesquisasUtilizadorWeb(String nickname) throws RemoteException{
		String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));

        String msg = "flag|"+id+";type|searchHistoric;nickname|"+nickname+";";
        ArrayList<infoHistorico> resposta = new ArrayList<>();
        enviarDatagramaUDP(msg);
        boolean exit = false;
        while(!exit) {
        	String respostaMC = recebeDatagramaUDP(msg);
        	ArrayList<String[]> mensagemTratada = limparMensagem(respostaMC);
        	if(mensagemTratada.get(0)[1].equals(id)) {
	        	if (mensagemTratada.get(2)[1].equals("s")) {
	        		int nItems = Integer.parseInt(mensagemTratada.get(4)[1]);
	        		for(int i = 6; i <=mensagemTratada.size()-1;i++) {
						String url = mensagemTratada.get(i-1)[1];
						infoHistorico novo = new infoHistorico(url);
						resposta.add(novo);
	        		}
	        	}
	        	exit = true;
        	}      	
        }
        return resposta;
	}
	
	/**
     * metodo para pesquisar links para uma pagina especifica
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | searchLinksForPage ; nickname | nickname ; url | url ;
     * <br>response:
     * <br>flag | id ; type | searchLinksForPage ; result|n ;nickname | nickname;
     * <br>flag | id ; type | searchLinksForPage ; result|n ;nickname | nickname; conta n; URL| url1; ....URL|n;
     * @param nickname nome do utilizador
     * @param url url da pagina
     */

	public ArrayList<infoHistorico> linksParaOutrasPaginasWeb(String nickname,String url) throws RemoteException{
		String uuid = UUID.randomUUID().toString();
		String id = uuid.substring(0, Math.min(uuid.length(), 8));

		String msg = "flag|"+id+";type|searchLinksForPage;util|"+nickname+";URL|"+url+";";
		ArrayList<infoHistorico> resposta = new ArrayList<>();
		enviarDatagramaUDP(msg);
		boolean exit = false;
		while(!exit) {
			String respostaMC = recebeDatagramaUDP(msg);
			ArrayList<String[]> mensagemTratada = limparMensagem(respostaMC);
			if(mensagemTratada.get(0)[1].equals(id)) {
				if (mensagemTratada.get(2)[1].equals("s")) {
					int nItems = Integer.parseInt(mensagemTratada.get(4)[1]);
					for(int i = 6; i <=mensagemTratada.size()-1;i++) {
						String links = mensagemTratada.get(i-1)[1];
						infoHistorico novo = new infoHistorico(links);
						resposta.add(novo);
					}
				}
				exit = true;
			}
		}
		return resposta;
	}

	/**
     * metodo para consultar os dados do sistema
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | updateSystem ; admin | nickname ;
     * <br>response:
     * <br>flag | id ; type | updateSystem ; result | s ; admin | admin ; numAdmin contaNumAdmins ; numUtils contaNumUtils ; NumLinksBD | contaNumLinksBD NumPesquisasTotal | contaNumPesquisas ;conta | n ; url | url1 …; url | url2 ; nserv | n; ip|ip1 ;port | port1;
     * @param nickname nome do admin
     */
	
	public ArrayList<infoServidores> paginaAdminWeb(String nickname) throws RemoteException{
		String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));       
        String msg = "flag|"+id+";type|updateSystem;admin|"+nickname+";";
        ArrayList<infoServidores> resposta = new ArrayList<>();
        enviarDatagramaUDP(msg);
        infoServidores iten;
        String notifica = "";
        boolean exit = false;
        while(!exit) {
        	String respostaMC = recebeDatagramaUDP(msg);
        	ArrayList<String[]> mensagemTratada = limparMensagem(respostaMC);
        	if(mensagemTratada.get(0)[1].equals(id)) {
        		if (mensagemTratada.get(2)[1].equals("s")) {
        			int nServidores = Integer.parseInt(mensagemTratada.get(4)[1]);
        			iten = new infoServidores("Servidores Activos \n");
        			notifica +="Servidores Activos \n";
        			resposta.add(iten);
        			for(int i = 6; i <6+nServidores;i++) {
        				iten = new infoServidores(mensagemTratada.get(i-1)[1] + "\n");
        				resposta.add(iten);
        				notifica +=mensagemTratada.get(i-1)[1] + "\n";
	        		}
        			iten = new infoServidores("\n");
        			resposta.add(iten);
        			int nPesquisas = Integer.parseInt(mensagemTratada.get(5+nServidores)[1]);
        			iten = new infoServidores("Pesquisas Mais Importantes \n");
        			notifica +="Pesquisas Mais Importantes \n";
        			resposta.add(iten);
        			for(int i = 6+nServidores+1; i <6+nServidores+nPesquisas+1;i++) {
        				iten = new infoServidores(mensagemTratada.get(i-1)[1] + "\n");
        				resposta.add(iten);
        				notifica +=mensagemTratada.get(i-1)[1] + "\n";
	        		}
        			iten = new infoServidores("\n");
        			resposta.add(iten);
        			int nLigacoes = Integer.parseInt(mensagemTratada.get(6+nServidores+nPesquisas)[1]);
        			iten = new infoServidores("URLs com mais ligacoes \n");
        			resposta.add(iten);
        			notifica +="URLs com mais ligacoes \n";
        			for(int i = 6+nServidores+nPesquisas+1+1; i <=mensagemTratada.size()-1;i++) {
        				iten = new infoServidores(mensagemTratada.get(i-1)[1] + "\n"); 
        				resposta.add(iten);
        				notifica +=mensagemTratada.get(i-1)[1] + "\n";
	        		}
        			enviarNotificacaoWebsocket(notifica);
        		}
        		exit = true;
        	}
        }	
		return resposta;
	}
	@Override
	public void abrirWebSocket(websocketInterface websocket) throws RemoteException{
		System.out.println("websocket registou");
        webSockets.add(websocket);
	}
	
	@Override
	public void fecharWebSocket(websocketInterface websocket) throws RemoteException{
		if(webSockets.remove(websocket)) {
            System.out.println("websocket deresgistou");
        }
        else {
            System.out.println("O websocket que não foi registrado tentou cancelar o registro");
        }
	}
	
	/**
     * metodo para enviar notificacoes por websockets
     * @param notificacao notificacao a enviar por websocket
     */

	private static void enviarNotificacaoWebsocket(String notificacao ) throws RemoteException{
        webSockets.forEach((s) -> {
            try {
                s.notificarUtilizador(notificacao);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

	
	
	public Utilizador getIdFacebook(String emailFaceBook) throws RemoteException {
		Utilizador utilizador = null;
        String uuid = UUID.randomUUID().toString();
        String id = uuid.substring(0, Math.min(uuid.length(), 8));
        String msg = "flag|"+id+";type|loginFB;emailFB|"+emailFaceBook+";";
        enviarDatagramaUDP(msg);
        boolean exit = false;
        
        while (!exit) {
            String rsp = recebeDatagramaUDP(msg);
            ArrayList<String[]> mensagemTratada = limparMensagem(rsp);

            if (mensagemTratada.get(0)[1].equals(id)) {
                if(mensagemTratada.get(2)[1].equals("y")){
                	String nickname = mensagemTratada.get(3)[1];
                	String password = mensagemTratada.get(4)[1];
                	String admin = mensagemTratada.get(5)[1];
                	utilizador = new Utilizador(nickname,password,admin);
                } else {
                	utilizador = null;
                }
                exit = true;
            }
        }
        
        
		return utilizador;
	}
	

	 
	 public String getLigacaoFB(String nickname,String emailFB,String acessToken) throws RemoteException{

	        // flag | id; type | getToken; email | eeee;
	        // flag | id; type | getToken; result | y/n; token | ttttt;
	
	        String uuid = UUID.randomUUID().toString();
	        String id = uuid.substring(0, Math.min(uuid.length(), 8));
	
	        String msg = "flag|"+id+";type|ligacaoFB;nickname|"+nickname+";emailFB|"+emailFB+";token|"+acessToken+";";
	        boolean exit = false;
	        String token = "";
	
	        enviarDatagramaUDP(msg);
	
	        while (!exit) {
	            String rsp = recebeDatagramaUDP(msg);
	            ArrayList<String[]> mensagemTratada = limparMensagem(rsp);
	
	            if (mensagemTratada.get(0)[1].equals(id)) {
	                if(mensagemTratada.get(2)[1].equals("y")){
	                    token = mensagemTratada.get(3)[1]; // token
	                } else {
	                    token = "null";
	                }
	                exit = true;
	            }
	        }
	
	        return token;
	  }

	public static void main(String args[]) throws NotBoundException, AlreadyBoundException, IOException {
		String msg;
        String q = null;

        MulticastSocket socket = new MulticastSocket(PORT_RECEBE);  // criacao do sicket e bind do mesmo
        InetAddress group = InetAddress.getByName(ENDERESSO_MULTICAST);
        socket.joinGroup(group);
		/*
        if(args.length < 0) {
            System.out.println("usar java -jar server.jar <ip do servidor web>");
            System.exit(0);
        } else {
            ip = args[0];
        }*/
        ip = "194.210.33.241";
        
        try {
            int falhaLigacao = 0;
            boolean ultimaTentativa = false, secundario = false;
            Registry r = null;
            servidorRMI = new server();
            
            
            
            System.getProperties().put("java.security.policy", "policy.all");
    		System.setSecurityManager(new RMISecurityManager());
            
            try {
                // tenta fazer bind, se falher e porque esta a ser usado
                r = LocateRegistry.createRegistry(7001);
                r.bind("rmiserver", servidorRMI);

            } catch (ExportException ree) {
            	serverInterface mainServerInterface;
                System.out.println("arrancar RMI Backup");
                while (!secundario) {
                    try {

                        try {
                            // lookup a interface e ping
                            mainServerInterface = (serverInterface) LocateRegistry.getRegistry(7001).lookup("rmiserver");
                            System.out.println("Pinging MainRMI");
                            if (mainServerInterface.isAlive()) {
                            	ultimaTentativa = false;
                            }

                            if(!ultimaTentativa) {
                            	falhaLigacao = 0;
                            }

                        } catch (RemoteException re) {
                            System.out.println("falha de acesso Main RMI");
                            //secundario
                            falhaLigacao++;

                            if (falhaLigacao == 5 && ultimaTentativa) { /* mainRMI em baixo*/
                            	falhaLigacao = 0;
                            	secundario = true;
                                r = LocateRegistry.createRegistry(7001); /* criacao de um novo registry*/
                            }
                            ultimaTentativa = true;
                        }

                        sleep((long) (500));

                    } catch (InterruptedException Et) {
                        System.out.println(Et);
                    }
                }
            }
            r.rebind("rmiserver", servidorRMI);
            System.out.println("servidor Primario RMI");
            
            q = "flag|s;type|connectionrequest;";
            server.enviarDatagramaParaGrupoMult(q);
             
            
            
		} catch (RemoteException re) {
            System.out.println("Exception no server.main: " + re);
        }
		
		  while(true) {
            //rosposta do multicast
            // Response -> flag | id; type | ack; hash | hhhh;


            byte[] buffer = new byte[65536];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String msgMC = new String(packet.getData(), 0, packet.getLength());
            System.out.println("pacote recebido do endereco " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " com a mensagem: " + msgMC);
            ArrayList<String[]> msgTratada = server.limparMensagem(msgMC);

            if (msgTratada.get(0)[1].equals("r") && msgTratada.get(1)[1].equals("ack")) {
                if (!server.hashMulticast.contains(msgTratada.get(2)[1])) {
                    server.hashMulticast.add(msgTratada.get(2)[1]);
                }
            }
            
            if(msgTratada.get(0)[1].equals("r") && msgTratada.get(2)[0].equals("IPPORT")) {
            	server.enviarNotificacaoWebsocket("actualizacao do estado dos servidores: o servidor com o endereco " + msgTratada.get(2)[1] + " acabou de ligar");
            }
            
            
        }
		  
		  
		 
		
		
	}
}