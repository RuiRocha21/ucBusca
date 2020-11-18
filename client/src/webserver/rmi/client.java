package webserver.rmi;
import java.io.*;
import java.util.*;
import java.net.*;
import java.rmi.*;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import static java.lang.Thread.sleep;

public class client extends UnicastRemoteObject implements clientInterface {

	private static final long serialVersionUID = 1L;
	private static String nickname = "";
    private serverInterface serverInterface;
    private static String ip;
    
    private client() throws RemoteException {
        super();
    }

    /**
     * metodo imprimir ao cliente
     * @param msg mensagem a imprimir
     */
    public void imprimeClienteAutenticado(String msg) throws RemoteException {
        System.out.println(msg);
    }


	@Override
	public String getNickname() throws RemoteException {
		// TODO Auto-generated method stub
		return nickname;
	}
    
	/**
     * metodo para fazer CallBack quando o servidor rmi falha
     * @client interface da instancia do cliente
     */
	
	private void waitForServer(client client) {

        boolean exit = false;
        while(!exit) {
            try {
                sleep((long) (1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                this.serverInterface = (serverInterface) LocateRegistry.getRegistry(ip, 7001).lookup("rmiserver");
                this.serverInterface.subscribe(nickname, client);
                exit = true;
            } catch (ConnectException e) {
                System.out.println("RMI deixou de responder (ConnectException)...");
            } catch (NotBoundException v) {
                System.out.println("RMI deixou de responder (NotBoundException)...");
            } catch (RemoteException tt) {
                System.out.println("RMI deixou de responder (RemoteException)...");
            }
        }
	}
	
	
	public final static void limparTela(){
		try {

			  if( System.getProperty( "os.name" ).startsWith( "Window" ) ) {
			     Runtime.getRuntime().exec("cls");

			  } else {

			    Runtime.getRuntime().exec("clear");

			  }

			} catch (IOException e) {

			  for(int i = 0; i < 1000; i++) {

			    System.out.println();

			  }

			}

	}
	
	public static void menuInicial() {
		System.out.println("1- login");
		System.out.println("2- registar");
		System.out.println("3- pesquisar");
		System.out.println("4- sair");
	}
	
	public static void menuUtil(String util) {
		System.out.println("1- pesquisar paginas por tokens");
		System.out.println("2- pesquisar ligacoes para uma pagina");
		System.out.println("3- consultar historico");
		System.out.println("4- sair");
	}
	
	public static void menuAdmin(String Admin) {
		System.out.println("1- inserir urls para indexar");
		System.out.println("2- pesquisar paginas por tokens");
		System.out.println("3- pesquisar ligacoes para uma pagina");
		System.out.println("4- consultar historico");
		System.out.println("5- dar privilegios de adminstrador a utilizadores");
		System.out.println("6- consultar estados dos servidores");
		System.out.println("7- sair");
	}
    
	public static void main(String args[]) throws IOException, NotBoundException {
		if (args.length != 1) {
            System.out.println("USAR: java client <ip da maquina do servidor>");
            System.exit(0);
        } else {
            ip = args[0];
        }
		
		

        Scanner sc = new Scanner(System.in);
        client client = new client();
        boolean ligacaoRMI = false;
        
        System.out.println("ligar ao servidor com o IP: " + ip);
        
        
        while (!ligacaoRMI) {
            try {
                client.serverInterface = (serverInterface) LocateRegistry.getRegistry(ip, 7001).lookup("rmiserver");
                ligacaoRMI = true;
            } catch (ConnectException e) {
                System.out.println("ligacao com o IP " + ip + " falhou!!!!");
                try {
                    sleep((long) (1000));
                } catch (InterruptedException re) {
                    e.printStackTrace();
                }
            }
        }
        try {
	        String op="0";
	        while(!op.equals("4")){
	        	limparTela();
	        	menuInicial();
	        	op = sc.nextLine();
	        	if (op.equals("1")){	//login
	        		System.out.print("nickname: ");
	                String nickname = sc.nextLine();
	        		System.out.print("Password: ");
	                String pw = sc.nextLine();
	                String result = client.serverInterface.login(nickname, pw, client);
	                System.out.println(result);
	                String [] verifica = result.split(" ");
	                if (verifica[0].equals("utilizador")) {	//funcionalidades de utilizador
	                	 while (!op.equals("4")) {
	                		 limparTela();
	                		 menuUtil(nickname);
	                		 op = sc.nextLine();
	                		 if (op.equals("1")){	//pesquisar por palavras
	                			System.out.print("termos da pesquias: ");
	                     		String termos = sc.nextLine();
	                     		System.out.println(client.serverInterface.pesquisaUtil(nickname,termos));
	                		 }else if (op.equals("2")){	//pesquisar por url
		                			System.out.print("URL: ");
		                     		String url = sc.nextLine();
		                     		System.out.println(client.serverInterface.linksParaOutrasPaginas(nickname,url));
	                		 }else if (op.equals("3")){	//pesquisar historico
	                			System.out.println(client.serverInterface.consultarPesquisasUtilizador(nickname));
	                		 }else if (op.equals("4")){
	                			System.out.println(client.serverInterface.logout(nickname));
	                			System.exit(0);
	                		 }else {
	                			System.out.println("opcao errada!");
	                		 }
	                	 }
	        		}
	                if (verifica[0].equals("admin")) {	//funcionalidade de admin
	                	while (!op.equals("7")) {
	                		limparTela();
							menuAdmin(nickname);
							op = sc.nextLine();
							if (op.equals("1")){	//indexar paginas
								System.out.print("url a indexar: ");
								String url = sc.nextLine();
								System.out.println(client.serverInterface.indexarURL(nickname,url));
							}else if (op.equals("2")){	//pesquisar por palavras
								System.out.print("termos da pesquias: ");
								String termos = sc.nextLine();
								System.out.println(client.serverInterface.pesquisaUtil(nickname,termos));
							}else if (op.equals("3")){	//pesquisar por url
	                			System.out.print("URL: ");
	                     		String url = sc.nextLine();
	                     		System.out.println(client.serverInterface.linksParaOutrasPaginas(nickname,url));
							}else if (op.equals("4")){	//pesquisar historico
								System.out.println(client.serverInterface.consultarPesquisasUtilizador(nickname));
							}else if (op.equals("5")){	//dar privilegio
								System.out.print("nome do utilizador a promover a admnistrador: ");
								String novoAdmin = sc.nextLine();
								System.out.println(client.serverInterface.previlegioAdmin(nickname, novoAdmin)); 
							}else if (op.equals("6")){	//consultar pagina Admin
								System.out.println(client.serverInterface.paginaAdmin(nickname));
							}else if (op.equals("7")){	//logout
								System.out.println(client.serverInterface.logout(nickname));
								System.exit(0);
							}else {
								System.out.println("opcao errada!");
							}
	               	 	}
	        		}
	        	}
	            else if (op.equals("2")){	//registar
	        		System.out.print("nickname: ");
	                String nick = sc.nextLine();
	        		System.out.print("Password: ");
	                String pass = sc.nextLine();
	                System.out.println(client.serverInterface.registarUtils(nick, pass));
	        		
	        	}else if (op.equals("3")){	//pesquisas sem login
	        		System.out.print("termos da pesquias: ");
	        		String termos = sc.nextLine();
	        		System.out.println(client.serverInterface.pesquisaAnonima(termos));
	        	}else if (op.equals("4")){	//logout
	        		System.out.println("VOLTE SEMPRE!");
	                System.exit(0);
	            }else{	//caso contrario
	            	menuInicial();
	            }        	
	        }	//fim while
        
        } catch (RemoteException b) {
            client.waitForServer(client);
        } 
        
	}
	
}