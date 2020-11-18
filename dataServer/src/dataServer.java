import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;





/**
 * dataServer inicia o servidor e a base de dados
 * cria uma thread cada vez que recebe um pacote
 * o endereco MultiCast esta definido com ip 224.3.2.100 
 * port de receber 5213
 * port de envio 5214 
 */

public class dataServer extends Thread {
    private static String ENDERESSO_MULTICAST = "224.3.2.100";
    private static int PORT_RECEBE = 5213;
    private static MulticastSocket socket = null;

    private HashSet<String> links;
    private String verifica = "n";
     
	//Threads
    private DatagramPacket pacoteDatagrama;
    private String hashCode;
    private int PORTO_ENVIO = 5214;
    private static Connection ligacao;
    
    static Queue<String> pilha = new LinkedList<>(); 
    

    /**
     * Thread's constructor
     * @param pacoteDatagrama Multicast UDP pacote a processao
     * @param code Multicast's ID
     * @param ligacao ligacao a base de dados
     */
    
    dataServer(DatagramPacket pacDatagrama, String code) {
    	links = new HashSet<>();
        this.pacoteDatagrama = pacDatagrama;
        this.hashCode = code;
        
    }
    

    
    
    
    /**
     *  cria a socket multicast e envia datagramas udp estipulados pelo protocolo
     *  este metodo so é executado se o hash multicast corresponder ao hash RMI
     *
     * @param resp - mensagem a enviar ao servidor RMI
     * @param code - hash unico
     *
     */
    
    public void enviarRespostaMC(String resp, String code) {

        if(this.hashCode.equals(code)) {
            // so um servidor multicast respode ao servidor RMI
            try {

                MulticastSocket socket = new MulticastSocket();
                byte[] buffer = (resp+"hash|" + hashCode + ";").getBytes();
                InetAddress group = InetAddress.getByName(ENDERESSO_MULTICAST);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORTO_ENVIO);
                socket.send(packet);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     *  cria a socket multicast e envia datagramas udp estipulados pelo protocolo
     *  este metodo so é executado se o hash multicast corresponder ao hash RMI
     *
     * @param resp - mensagem a enviar ao servidor RMI
     *
     */
    
    public static void enviarRespostaMC(String resp) {

        try {
            MulticastSocket socket = new MulticastSocket();
            byte[] buffer = resp.getBytes();
            InetAddress group = InetAddress.getByName(ENDERESSO_MULTICAST);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 5214);
            socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * parser da mensagem recebida
     * @param msg de protocolo
     * @return ArrayList de tokens: (id, abcdef012), (type, login) , ....
     * */
    ArrayList limparMensagem(String msg) {

        String[] tokens = msg.split(";");
        String[] p;

        ArrayList<String[]> rtArray = new ArrayList<String[]>();

        for (int i = 0; i < tokens.length; i++) {
            p = tokens[i].split(Pattern.quote("|"));
            rtArray.add(p);
        }
        return rtArray;
    }
    
    
    /**
     * metodo para registo de utilizadores, primeiro verifica se ja existe. caso contrario, insere o utilizador na base de dados
     * resposta a retornar: result|s ou result|n
     *
     * @param id - ID do pacote
     * @param nickname nome de utilizador
     * @param password password do utilizador
     * @param code hash a enviar ao RMI
     * */
    public void registo(String id, String nickname, String password, String code) {
    	String rsp = "flag|"+id+";type|register;result|n;username|" + nickname + ";password|" + password + ";";
        
    	PreparedStatement pstmt;
        int rs;
    	
        try {
            pstmt = ligacao.prepareStatement("INSERT INTO ucbusca.utilizador (nickname, password, admin) VALUES (?,?,?)");
            pstmt.setString(1, nickname);
            pstmt.setString(2, password);
            pstmt.setString(3, "0");
            rs = pstmt.executeUpdate();

            System.out.println("inserido " + rs + " novo utilizador");
            rsp = "flag|"+id+";type|register;result|s;nickname|" + nickname + ";password|" + password + ";";
            String query = "INSERT INTO ucbusca.utilizador (nickname, password, admin) VALUES ('"+nickname+"','"+password+"','0')";
            String path = System.getProperty ("user.dir");
            String ficheiro = path  +"//src//"+ "utilizadores.sql";
            ligacaoDB.guardaDados(ficheiro, query);
        } catch (SQLException e) {
            e.printStackTrace();

            switch (e.getErrorCode()) {
                case 1062:
                    // duplicate entry
                    System.out.println("Got ERROR:1062");
                    String message = "utilizador : " + nickname +" ja existe.";
                    System.out.println(message);
                    rsp = "flag|"+id+";type|register;result|n;nickname|" + nickname + ";password|" + password + ";";
                    break;
            }

        }

        enviarRespostaMC(rsp, code);
    }
    
    /**
     * metodo para o utilizador entrar no sistema. verifica o nickname e password do utilizador
     * resposta para o servidor result|s ou result|n
     * se o utilizador tiver notificacoes sao inseridas na mensagem a enviar 
     *
     * @param id - ID do pacote
     * @param nickname nome de utilizador
     * @param password password do utilizador
     * @param code hash a enviar ao RMI
     * */
    
    public void login(String id, String nickname, String password, String code) {
    	 String rsp = "flag|"+id+";type|login;result|n;nickname|" + nickname + ";password|" + password + ";";

         // ------------- BD
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         try {
             pstmt = ligacao.prepareStatement("SELECT ucbusca.utilizador.password,ucbusca.utilizador.nickname,ucbusca.utilizador.admin from ucbusca.utilizador where ucbusca.utilizador.nickname = ?");
             String nickname_ =  nickname ;
             pstmt.setString(1, nickname_);
             dataServer.ligacao.setAutoCommit(false);
             rs = pstmt.executeQuery();
             dataServer.ligacao.commit();
             String dbPassword;
             String admin = "0";
             if (rs.next()) { // pedir password
                 dbPassword = rs.getString(1);
                 
                 if(dbPassword.equals(password)) { 
                	 admin = rs.getString(3);
	                 pstmt = ligacao.prepareStatement("SELECT ucbusca.notificacao.notificacao, ucbusca.notificacao.utilizador_nickname, ucbusca.notificacao.id from ucbusca.notificacao where ucbusca.notificacao.utilizador_nickname = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	                 pstmt.setString(1, nickname);
	                 dataServer.ligacao.setAutoCommit(false);
	                 rs = pstmt.executeQuery();
	                 dataServer.ligacao.commit();
	                 int contaNotifica = 0;
	                 String listaNotifica = "";
	                 if(admin.equals("1")) {
	                	 rsp = "flag|"+id+";type|login;result|sim;admin|sim;nickname|" + nickname + ";numero_notificacoes|";
	                     
	                 }else{
	                	 rsp = "flag|"+id+";type|login;result|sim;admin|nao;nickname|" + nickname + ";numero_notificacoes|";
	                     
	                 }
	                 while (rs.next()) {
	                     contaNotifica++;
	                     listaNotifica += "notificacao|"+ rs.getString(1) +";";
	                    
	                     //rs.deleteRow();
	                 }
	
	                 rsp += contaNotifica +";"+listaNotifica;
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try { rs.close(); pstmt.close(); } catch (SQLException e) { System.out.println(e); }
         }
         enviarRespostaMC(rsp, code);
    }
    
    /**
     * metodo para dar privilegio de administradores a utilizadores comuns
     *  @param id - ID do pacote
     * 	@param admin nome do adminstrador
     * 	@param nickname nome do utilizador a dar privilegios
     * 	@param code hash a enviar ao RMI
     */
    
    
    public void tornarAdmin(String id, String admin, String nickname, String code) {
        
    	String rsp = "flag|"+id+";type|privilege;result|n;admin|" + admin +";util|" + nickname + ";msg|Erro ao promover utilizador;";

        // ---------------- BD
        PreparedStatement pstmt;
        int rs;

        try {
            pstmt = ligacao.prepareStatement("UPDATE ucbusca.utilizador AS u, (SELECT ucbusca.utilizador.admin "+
            		"FROM ucbusca.utilizador WHERE ucbusca.utilizador.nickname = ? ) AS p  " + 
            		" SET u.admin = '1' " + 
            		" WHERE p.admin = '1' and u.nickname = ? ");
            pstmt.setString(1, admin);
            pstmt.setString(2, nickname);
            
            dataServer.ligacao.setAutoCommit(false);
            rs = pstmt.executeUpdate();
            dataServer.ligacao.commit();
            if (rs == 1) {
            	System.out.println("actualizacao com sucesso");
            	rsp = "flag|"+id+";type|privilege;result|s;admin|" + admin +";util|" + nickname + ";";
            	//guarda actualizacao em disco
            	String path = System.getProperty ("user.dir");
                String ficheiro = path  +"//src//"+ "update_utilizadores.sql";
                String query = "UPDATE ucbusca.utilizador AS u, (SELECT ucbusca.utilizador.admin "+
                		"FROM ucbusca.utilizador WHERE ucbusca.utilizador.nickname = '"+admin+"' ) AS p  " + 
                		" SET u.admin = '1' " + 
                		" WHERE p.admin = '1' and u.nickname = '"+ nickname+"'";
                ligacaoDB.guardaDados(ficheiro, query);
            }
            else if (rs == 0) {
            	System.out.println("Falha na promocao a admin");
            	
            }else {
            	System.out.println("Falha na promocao a admin 2");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    	
    	
    	
        enviarRespostaMC(rsp, code);

        
    }
    
    /**
     * metodo para notificar utilizadores
     *  @param id - ID do pacote
     * 	@param nickname nome do utilizador a notificar
     * 	msg mensagem de notificacao
     * @see #enviarRespostaMC(String, String)*
     */
    
    
    private void notificarUtilOff(String id, String nickname, String msg) {
    	PreparedStatement pstmt;
        int rs;

        try {
            pstmt = ligacao.prepareStatement("INSERT into ucbusca.notificacao (ucbusca.notificacao.id, ucbusca.notificacao.notificacao, ucbusca.notificacao.utilizador_nickname) VALUES (id,?, ?)");

            pstmt.setString(1, msg);
            pstmt.setString(2, nickname);
            dataServer.ligacao.setAutoCommit(false);
            rs = pstmt.executeUpdate();
            dataServer.ligacao.commit();
            if ( rs == 1) {
                System.out.println("adicionada a mensagem ao utlizador off "+nickname);
	            //guardar dados em disco
	            String path = System.getProperty ("user.dir");
	            String ficheiro = path  +"//src//"+ "notificacoes.sql";
	            String query = "INSERT into ucbusca.notificacao (ucbusca.notificacao.id, ucbusca.notificacao.notificacao, ucbusca.notificacao.utilizador_nickname) VALUES (id,'"+msg+"','"+nickname+"')";
	            ligacaoDB.guardaDados(ficheiro, query);
            }
            if ( rs == 0) {
                System.out.println("erro ao adicionar mensagem");
            }
            pstmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * metodo para procurar na base de dados todos os links associados a um url de entrada
     * @param id ID unico do pacote
     * @param nickname nome do utiizador
     * @param url url a visitar
     * @param code hash a enviar ao RMI
     * @see #enviarRespostaMC(String, String)
     */
    
    
    
    
   
    /**
     * metodo para um utilizador visualizar as suas consultas
     * @param id ID unico do pacote
     * @param nickname nome do utiizador
     * @param code hash a enviar ao RMI
     * @see #enviarRespostaMC(String, String)
     */
    private void historicoConsultas(String id, String nickname, String code) {
    	String rsp = "flag|"+id+";type|searchHistoric;result|n;nickname|" + nickname + ";numeroLinks|0;lista|historico vazio;";
    	PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	String res = "";
    	String lista = "";
    	int conta = 0;
    	try {
    		
    		pstmt = ligacao.prepareStatement("SELECT ucbusca.pesquisa.palavras FROM ucbusca.pesquisa WHERE ucbusca.pesquisa.utilizador = ?");
        	pstmt.setString(1, nickname);
        	dataServer.ligacao.setAutoCommit(false);
            rs = pstmt.executeQuery();
            dataServer.ligacao.commit();   	
            while(rs.next()) {
            	res = "s";
            	conta++;
	        	lista+="pesquisa|"+rs.getString(1)+"\n;"; 
	        }        	
            rsp = "flag|"+id+";type|searchHistoric;result|"+res+";nickname|" + nickname + ";numeroLinks|"+conta+";"+lista;
            pstmt.close();
        	rs.close();            
    	}catch (SQLException e) {
            e.printStackTrace();
        } 

        enviarRespostaMC(rsp, code);
        
    	
    }
    /**
     * metodo o Administrador consultar o estado dos servidores multicast
     * @param id ID unico do pacote
     * @param admin nome do admin
     * @param code hash a enviar ao RMI
     * @see #enviarRespostaMC(String, String)
     */
    //tratar para BD
	private void consultaPaginaAdmin(String id, String admin, String code) {
		String rsp = "flag|"+id+";type|updateSystem;result|n;admin|" + admin + ";";
		PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	int numServidores = 0;
    	int numPesquisasMaisImportantes = 0;
    	int numUrlsComMaisLigacoes = 0;
    	String infoServidores = "";
    	String infoLigacoes = "";
    	String infoPesquisas = "";
    	
    	try {
    		//info de servidores
    		pstmt = ligacao.prepareStatement("SELECT ucbusca.servidor.ip,ucbusca.servidor.port FROM ucbusca.servidor");
        	dataServer.ligacao.setAutoCommit(false);
            rs = pstmt.executeQuery();
            dataServer.ligacao.commit();   	
            while(rs.next()) {
            	numServidores++;
            	infoServidores+="servidor|"+rs.getString(1)+":"+rs.getString(2)+";";
	        }
            //info de pesquisas
            pstmt = ligacao.prepareStatement("SELECT ucbusca.pesquisa.palavras AS 'pesquisas', " + 
            "COUNT(*) AS 'num de ocurrencias' " + 
            "FROM ucbusca.pesquisa " + 
            "GROUP BY ucbusca.pesquisa.palavras " + 
            "ORDER BY COUNT(*) DESC;");
            dataServer.ligacao.setAutoCommit(false);
            rs = pstmt.executeQuery();
            dataServer.ligacao.commit();   	
            while(rs.next()) {
            	numPesquisasMaisImportantes++;
            	infoPesquisas += "pesquisa|" +rs.getString(2) +" - "+ rs.getString(1)+";";
            }
            //info de ligacoes
            pstmt = ligacao.prepareStatement("SELECT ucbusca.pagina.urlorigem AS 'urls', " + 
            		"COUNT(*) AS 'num de ligacoes' " + 
            		"FROM ucbusca.pagina " + 
            		"GROUP BY ucbusca.pagina.urlorigem " + 
            		"ORDER BY COUNT(*) DESC; ");
            dataServer.ligacao.setAutoCommit(false);
            rs = pstmt.executeQuery();
            dataServer.ligacao.commit();   	
            while(rs.next()) {
            	numUrlsComMaisLigacoes++;
            	infoLigacoes += "ligacao|" +rs.getString(2) +" - "+ rs.getString(1)+";";
            }   
        	rsp = "flag|"+id+";type|updateSystem;result|s;admin|" + admin + ";numServidores|"+numServidores+";"+infoServidores+"numPesquisas|"+numPesquisasMaisImportantes+";"+infoPesquisas+"numLigacoes|"+numUrlsComMaisLigacoes+";"+infoLigacoes;
        	pstmt.close();
        	rs.close();
        	
    	}catch (SQLException e) {
            e.printStackTrace();
        } 
    	
    	
    	
    	enviarRespostaMC(rsp, code);
		
		
    }
	
	
	
	/**
     * metodo o para um utilizador pesquisar por palavras
     * @param id ID unico do pacote
     * @param nickname nome do utilizador
     * @param palavras palavras a procurar no sistema
     * @param code hash a enviar ao RMI
     * @see #enviarRespostaMC(String, String)
     */
	
	private void pesquisarPorTermos(String id, String nickname, String palavras,String code) {
		String rsp = "flag|"+id+";type|searchUtil;result|n;nickname|" + nickname + ";";
		PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	String res = "n";
    	String lista = "";
    	int conta = 0;
    	int rs1;
    	try {
    		pstmt = ligacao.prepareStatement("SELECT ucbusca.pagina.url,ucbusca.pagina.titulo,ucbusca.pagina.sintese " + 
    				"from ucbusca.pagina " + 
    				"where LOCATE(?,ucbusca.pagina.sintese)>0");
        	pstmt.setString(1, palavras);
        	dataServer.ligacao.setAutoCommit(false);
            rs = pstmt.executeQuery();
            dataServer.ligacao.commit();   	
            while(rs.next()) {
            	res = "s";
            	conta++;
	        	lista+="url|"+rs.getString(1)+"\n;";
	        	lista+="titulo|"+rs.getString(2)+"\n;";
	        	lista+="sintese|"+rs.getString(3)+"\n\n;"; 
	        }   
            
            //actualizar tabela de pesquisas
            pstmt = ligacao.prepareStatement("INSERT INTO ucbusca.pesquisa (ucbusca.pesquisa.palavras,ucbusca.pesquisa.utilizador) VALUES (?,?);");
            pstmt.setString(1, palavras);
            pstmt.setString(2, nickname);
            rs1 = pstmt.executeUpdate();
            if(rs1 == 1) {
            	System.out.println("pesquisa inserida na base de dados");
            }
            
            
            //guardar dados BD
            String path = System.getProperty ("user.dir");
            String ficheiro = path  +"//src//"+ "pesquisas.sql";
            String query = "INSERT INTO ucbusca.pesquisa (ucbusca.pesquisa.id, ucbusca.pesquisa.palavras,ucbusca.pesquisa.utilizador) VALUES (id,'"+palavras+"','"+nickname+"')";
            ligacaoDB.guardaDados(ficheiro, query);
            pstmt.close();
        	rs.close();            
    	}catch (SQLException e) {
    		res = "n";
            e.printStackTrace();
        } 
    	rsp = "flag|"+id+";type|searchUtil;result|"+res+";nickname|"+nickname+";nLinks|"+conta+";"+lista;
    	enviarRespostaMC(rsp, code);	
    }
	/**
     * metodo o para um utilizador anonimo pesquisar por palavras
     * @param id ID unico do pacote
     * @param palavras palavras a procurar no sistema
     * @param code hash a enviar ao RMI
     * @see #enviarRespostaMC(String, String)
     */
	//tratar para BD
	private void pesquisarAnonimas(String id, String palavras,String code) {
		String nickname = "utilizadorNaoRegistado";
		String rsp = "flag|"+id+";type|anounymous;result|n;nickname|utilizadorNaoRegistado;";
		PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	String res = "n";
    	String lista = "";
    	int conta = 0;
    	int rs1;
    	try {
    		
    		pstmt = ligacao.prepareStatement("SELECT ucbusca.pagina.url,ucbusca.pagina.titulo,ucbusca.pagina.sintese " + 
    				"from ucbusca.pagina " + 
    				"where LOCATE(?,ucbusca.pagina.sintese)>0");
        	pstmt.setString(1, palavras);
        	dataServer.ligacao.setAutoCommit(false);
            rs = pstmt.executeQuery();
            dataServer.ligacao.commit();   	
            while(rs.next()) {
            	res = "s";
            	conta++;
	        	lista+="url|"+rs.getString(1)+"\n;";
	        	lista+="titulo|"+rs.getString(2)+"\n;";
	        	lista+="sintese|"+rs.getString(3)+"\n\n;"; 
	        }   
            
            //actualizar tabela de pesquisas
            pstmt = ligacao.prepareStatement("INSERT INTO ucbusca.pesquisa (ucbusca.pesquisa.palavras,ucbusca.pesquisa.utilizador) VALUES (?,?);");
            pstmt.setString(1, palavras);
            pstmt.setString(2, nickname);
            rs1 = pstmt.executeUpdate();
            if(rs1 == 1) {
            	System.out.println("pesquisa inserida na base de dados");
            }
            
            
            //guardar dados BD
            String path = System.getProperty ("user.dir");
            String ficheiro = path  +"//src//"+ "pesquisas.sql";
            String query = "INSERT INTO ucbusca.pesquisa (ucbusca.pesquisa.id, ucbusca.pesquisa.palavras,ucbusca.pesquisa.utilizador) VALUES (id,'"+palavras+"','"+nickname+"')";
            ligacaoDB.guardaDados(ficheiro, query);
            pstmt.close();
        	rs.close();            
    	}catch (SQLException e) {
    		res = "n";
            e.printStackTrace();
        } 
    	rsp = "flag|"+id+";type|anounymous;result|"+res+";nickname|"+nickname+";nLinks|"+conta+";"+lista;
    	enviarRespostaMC(rsp, code);
    }
	
	
	
	
    
	
	static String trataUrl(String string) {
		//"[qwertyuiopasdfghjklzxcvbnm QWERTYUIOPASDFGHJKLZXCVBNM_0123456789_=+-?]"
		String patternStr = "(https?:\\/\\/)?([\\w\\Q$-_+!*'(),%\\E]+\\.)+(\\w{2,63})(:\\d{1,4})?([\\w\\Q/$-_+!*'(),%\\E]+\\.?[\\w])*\\/?$";
		String replaceStr = "";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(string);
		string = matcher.replaceAll(replaceStr);
        return string;
    }
	
	
	
	static String trataAspas(String t) {
		String str="";
		str = t.replaceAll("\"([^\"]+)\"", "\"$1\"");
		return str;
	}
	
	
	public void obterLinksPagina(String URL) {
		int conta = 0;
        if (!links.contains(URL)) {
            try {
                Document doc = Jsoup.connect(URL).get();
                Elements outrosLinks = doc.select("a[href]");

                for (Element pagina : outrosLinks) {
                	if(conta==10) {
    					break;
    				}
    				conta++;
                	links.add(URL);
                    obterLinksPagina(pagina.attr("abs:href"));
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
	

	
	
	
	
	
	
	static int inserePaginasBD(String url_pai,String titulo, String texto, String url_filho) {
		int rs = 0;
		PreparedStatement pstmt;
		String textoSemPlicas = "";
		textoSemPlicas = trataAspas(texto);
		try {	
			 pstmt = ligacao.prepareStatement("INSERT INTO ucbusca.pagina (url, titulo, sintese, urlorigem)" + 
			 		"SELECT ?, ?, ?, ?" + 
			 		"WHERE NOT EXISTS (Select ucbusca.pagina.url, ucbusca.pagina.urlorigem From ucbusca.pagina WHERE ucbusca.pagina.url = ? AND ucbusca.pagina.urlorigem = ?) LIMIT 1;");
	         pstmt.setString(1, url_filho);
	         pstmt.setString(2, titulo);
	         pstmt.setString(3, textoSemPlicas);
	         pstmt.setString(4, url_pai);
	         pstmt.setString(5, url_filho);
	         pstmt.setString(6, url_pai);
	         dataServer.ligacao.setAutoCommit(false);
	         rs = pstmt.executeUpdate();
	         dataServer.ligacao.commit();
		} catch (SQLException e) {
        	
       	 e.printStackTrace();
       }
		return rs;
	}
	
	
	/**
     * metodo indexar uma pagina e recursivamente indexar as paginas encontradas
     * @param id ID unico do pacote
     * @param admin nome do admin
     * @param url url a indexar
     * @param code hash a enviar ao RMI
     * @see #enviarRespostaMC(String, String)
     */
	
	private void indexarURL(String id, String admin, String url, String code) {
		String rsp = "flag|"+id+";type|index;result|n;nickname|" + admin + ";";
		
		
		try {
			int rs = 0;
            // Attempt to connect and get the document
            Document doc = Jsoup.connect(url).get();  // Documentation: https://jsoup.org/

            // Title
            System.out.println(doc.title() + "\n");
            String textopai = doc.text();
            String textoSemPlicasPai = trataAspas(textopai);
            rs = inserePaginasBD(url,doc.title(),textoSemPlicasPai,url);
            // Get all links
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                if (link.attr("href").startsWith("#")) {
                    continue;
                }
                if (!link.attr("href").startsWith("http")) {
                    continue;
                }
                
                pilha.add(link.attr("href"));
                
                
                Document doc1 = Jsoup.connect(link.attr("href")).get();
                String titulo = trataAspas(doc1.title());
                String textofilho = trataAspas(link.text());
                String textoSemPlicas = trataAspas(textofilho);
                
                rs = inserePaginasBD(url,titulo,textoSemPlicas,link.attr("href"));
                
                if ( rs == 1) {

                    System.out.println("indexado url " + link.attr("href"));
                    
                    //guardar dados em disco
                    String path = System.getProperty ("user.dir");
                    String ficheiro = path  +"//src//"+ "paginas.sql";
                    String query = "INSERT INTO ucbusca.pagina (url, titulo, sintese, urlorigem) VALUES ('"+link.attr("href") +"','"+titulo+"','"+textoSemPlicas+"','"+url+"')";
                    ligacaoDB.guardaDados(ficheiro, query);
                }
                if ( rs == 0) {
                	
                    System.out.println("url ja indexado na base de dados");
                }
                
            	String urls_filhos = pilha.remove();
            	indexarRecursivo threadIndexar = new indexarRecursivo(urls_filhos);
            	threadIndexar.start();
            	rsp = "flag|"+id+";type|index;result|s;nickname|" + admin;
    
            }
      	
		}catch (IOException e) {
			System.out.println("erro na indexacao");
            e.printStackTrace();
        }
		System.out.println("fim da indexacao");
		enviarRespostaMC(rsp, code);
		
	}
	
	/**
     * metodo para pesquisar links para uma pagina especifica
     * <br>mensagem do protocolo :
     * <br>request:
     * <br>flag | id ; type | searchLinksForPage ; nickname | nickname ; url | url ;
     * <br>response:
     * <br>flag | id ; type | searchLinksForPage ; result|n ;nickname | nickname;
     * <br>flag | id ; type | searchLinksForPage ; result|n ;nickname | nickname; conta n; URL| url1; ....URL|n;
     * @param id ID unico do pacote
     * @param nickname nome do utiizador
     * @param url url da pagina
     * @param code_ hash a enviar ao RMI
     * @see #enviarRespostaMC(String, String)
     */
	public void consultaLigacoes(String id, String nickname,String url,String code_) {
		String rsp = "flag|"+id+";type|searchLinksForPage;result|n;nickname|"+nickname;
		PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	String links = "";
    	int contaLinks = 0;
        String res ="";
        try {
        	pstmt = ligacao.prepareStatement("SELECT DISTINCT url FROM ucbusca.pagina WHERE urlorigem = ?");
        	pstmt.setString(1, url);
        	dataServer.ligacao.setAutoCommit(false);
            rs = pstmt.executeQuery();
            dataServer.ligacao.commit();
            
            while(rs.next()) {
            	res = "s";
            	contaLinks++;
            	links+="url|"+rs.getString(1)+";";
	        }
			//			0			1						2		3					4
			rsp = "flag|"+id+";type|searchLinksForPage;result|s;nickname|"+nickname+";conta|"+contaLinks+";"+links;
			pstmt.close();
        	rs.close();
		}catch (SQLException e) {
    		res = "n";
            e.printStackTrace();
        } 
		enviarRespostaMC(rsp, code_);
		
	}

	
	public void setTokenFB(String id, String token, String nickname, String emailFB,String code) {
		//protocolo
		//request: flag | id; type | token; token | ---------; nickname | +++++++; emailFB | *********;
		//response: flag | id; type | getToken; result | y/n; ;
		
    	String rsp = "flag|"+id+";rsp|";
    	String aux = "";
    	
    	PreparedStatement pstmt;
        int rs;

        System.out.println("token "+token);
        System.out.println("email facebook "+emailFB);
        
        try {
            pstmt = ligacao.prepareStatement(  "UPDATE ucbusca.utilizador AS u "+
                    "SET u.tokenFB = ? , u.emailFB = ? " +
                    "WHERE u.nickname = ?");


            pstmt.setString(1, token);
            pstmt.setString(2, emailFB);
            pstmt.setString(3, nickname);

            rs = pstmt.executeUpdate();

            if (rs == 0) {
                aux = "n;";

            } else if (rs == 1) {
                aux = "y;";
            }

        } catch (SQLException e) {
            System.out.println(e);
            aux ="n;";
        }
        
        enviarRespostaMC(rsp+aux, code);
    }
	
	public void getToken(String id, String nickname, String code) {
		//protocolo
		//request: flag | id; type | getToken; nickname | +++++;
		//response: flag | id; type | getToken; result | y/n; token | **********;
		PreparedStatement pstmt;
        ResultSet rs;
        String token = "null", res = "n;";
        
        try {
            pstmt = ligacao.prepareStatement("SELECT token FROM ucbusca.utilizador WHERE nickname = ?");

            pstmt.setString(1, nickname);
            rs = pstmt.executeQuery();

            while(rs.next()) {

                token = rs.getString("token");
                res = "y;";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("getToken() result: " + res + " token: " + token);

        String rsp = "flag|"+id+";type|getToken;result|"+res+"token|"+token+";";
        enviarRespostaMC(rsp, code);
	}
	
	public void permissaoLoginFB(String id, String emailFB, String code) {
		// protocolo
		// request: flag | id; type | loginFB; emailFB | ++++++++;
        // response: flag | id; type | loginFB; rsp | y/n; nickname | **********;
		
		PreparedStatement pstmt;
        ResultSet rs;
        String nickname = "null", res = "n;";
        String password = "null";
        String admin = "null";
        try {
            pstmt = ligacao.prepareStatement("SELECT ucbusca.utilizador.nickname,ucbusca.utilizador.password,ucbusca.utilizador.admin FROM ucbusca.utilizador WHERE ucbusca.utilizador.emailFB = ?");

            pstmt.setString(1, emailFB);
            rs = pstmt.executeQuery();

            while(rs.next()) {

            	nickname = rs.getString("nickname");
            	password = rs.getString("password");
            	admin = rs.getString("admin");
                res = "y;";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("permissaoLoginFB() result: " + res);

        String rsp = "flag|"+id+";type|loginFB;res|"+res+"nickname|"+nickname+";password|"+password+";admin|"+admin+";";
        enviarRespostaMC(rsp, code);
		
	}
	
	public void getEmailFB(String id, String emailFB,String code) {
		PreparedStatement pstmt;
        ResultSet rs;
        String nickname = "null", result = "n;";


        try {
            pstmt = ligacao.prepareStatement("SELECT nickname FROM ucbusca.utilizador WHERE emailFB = ?");

            pstmt.setString(1, emailFB);
            rs = pstmt.executeQuery();

            while(rs.next()) {

                nickname = rs.getString("nickname");
                result = "y;";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String rsp = "flag|"+id+";type|getEmail;result|"+result+"nickname|"+nickname+";";
        enviarRespostaMC(rsp, code);
	}
	
	
	
    public void run() {
        String msg = new String(pacoteDatagrama.getData(), 0, pacoteDatagrama.getLength());
        System.out.println("recebi um datagrama de origem do endereco " + pacoteDatagrama.getAddress().getHostAddress() + ":" + pacoteDatagrama.getPort() + " com a mensagem:" + msg);

        // limpar mensagem
        ArrayList<String[]> limparMensagem = limparMensagem(msg); // se o pacote tiver flag trata o pedido

        String tipo = limparMensagem.get(1)[1];
        
        switch (tipo) {
        	case "register":
        		registo(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(3)[1], limparMensagem.get(limparMensagem.size()-1)[1]); 
        		break;
        	case "login":
        		login(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(3)[1], limparMensagem.get(limparMensagem.size()-1)[1]);
        		break;
        	case "searchLinksForPage":
        		consultaLigacoes(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(3)[1], limparMensagem.get(limparMensagem.size()-1)[1]);
        		break;
        	case "searchHistoric":
        		historicoConsultas(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(limparMensagem.size()-1)[1]);
        		break;
        	case "searchUtil":
        		pesquisarPorTermos(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(3)[1], limparMensagem.get(limparMensagem.size()-1)[1]);
        		break;
        	case "updateSystem":
        		consultaPaginaAdmin(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(limparMensagem.size()-1)[1]);
        		break;
        	case "anounymous":
        		pesquisarAnonimas(limparMensagem.get(0)[1], limparMensagem.get(3)[1], limparMensagem.get(limparMensagem.size()-1)[1]);
        		break;
        	case "index":
        		indexarURL(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(3)[1], limparMensagem.get(limparMensagem.size()-1)[1]);
        		break;
        	case "privilege":
        		tornarAdmin(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(3)[1], limparMensagem.get(limparMensagem.size()-1)[1]);
        		break;
        	case "notifyfail":
        		notificarUtilOff(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(3)[1]); ;
        		break;
        	case "token":
        		setTokenFB(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(3)[1], limparMensagem.get(4)[1], limparMensagem.get(5)[1]);
        		break;
        	case "getToken":
        		getToken(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(3)[1]);
        		break;
        	case "loginFB":
        		permissaoLoginFB(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(3)[1]);
        		break;
        	case "getEmail":
        		getEmailFB(limparMensagem.get(0)[1], limparMensagem.get(2)[1], limparMensagem.get(3)[1]);
        		break;
	        case "connectionrequest":
	            if(limparMensagem.get(0)[1].equals("s"))
	            	enviarRespostaMC("flag|r;type|ack;", hashCode);
	            else
	            	enviarRespostaMC("flag|"+limparMensagem.get(0)[1]+";type|ack;", hashCode);
	            break;
	        default:
	            System.out.println("tipo errado do protocolo: "+tipo);
	            break;
        }
        
        
        
    }
    /**
     * metodo para criar ligacao TCP
     * @return socket criado
     */
    public static ServerSocket criaSocket() {

        boolean exit = false;
        ServerSocket socket= null;
        int PORT;

        while(!exit) {
            PORT = ((int)(Math.random()*1000)) + 5000;
            try {
            	socket = new ServerSocket(PORT);
                exit = true;
            } catch (IOException v) {
                v.printStackTrace();
            }
        }
        return socket;
    }
    
    
    
    public static void inserirServidorBD(String ip,String port) {
    	int rs = 0;
		PreparedStatement pstmt;
		String textoSemPlicas = "";		
		try {	
			 pstmt = ligacao.prepareStatement("INSERT INTO ucbusca.servidor (ip,port) VALUES (?,?)");
	         pstmt.setString(1, ip);
	         pstmt.setString(2, port);
	         dataServer.ligacao.setAutoCommit(false);
	         rs = pstmt.executeUpdate();
	         dataServer.ligacao.commit();
	         
	         if ( rs == 1) {
                 //guardar dados em disco
                 String path = System.getProperty ("user.dir");
                 String ficheiro = path  +"//src//"+ "servidores.sql";
                 String query = "INSERT INTO ucbusca.servidor (ucbusca.servidor.id,ucbusca.servidor.ip,ucbusca.servidor.port) VALUES (id,'"+ip+"','"+port+"')";
                 ligacaoDB.guardaDados(ficheiro, query);
             }
	         
	         
		} catch (SQLException e) {
        	
       	 e.printStackTrace();
       }
    }
    
    
    public static void main(String[] args) {
    	String ip="";
    	String port="";
        try {

            socket = new MulticastSocket(PORT_RECEBE);
            InetAddress group = InetAddress.getByName(ENDERESSO_MULTICAST);
            socket.joinGroup(group);
            
			
            ligacao = ligacaoDB.getLigacao();
            if(ligacao != null){
                System.out.println("ligacao estabelecida");
                String path = System.getProperty ("user.dir");
                //path.replaceAll("[^a-zA-Z0-9 -]","/");
                //System.out.println("caminho " + path );

                ligacaoDB.scriptSQL(path  +"//src//"+ "tabelas.sql");
                ligacaoDB.scriptSQL(path  +"//src//"+ "utilizadores.sql");
                ligacaoDB.scriptSQL(path  +"//src//"+ "update_utilizadores.sql");
                ligacaoDB.scriptSQL(path  +"//src//"+ "notificacoes.sql");
                ligacaoDB.scriptSQL(path  +"//src//"+ "paginas.sql");
                ligacaoDB.scriptSQL(path  +"//src//"+ "pesquisas.sql");
                ligacaoDB.scriptSQL(path  +"//src//"+ "servidores.sql");
            }
			
          
			ServerSocket socketTCP = criaSocket();
        	
            ip = InetAddress.getLocalHost().getHostAddress();
            port=String.valueOf(socketTCP.getLocalPort());
            inserirServidorBD(ip,port);

            //enviar ack para Main RMI
            String code = UUID.randomUUID().toString().substring(24);
            enviarRespostaMC("flag|r;type|ack;hash|" + code + ";");
            enviarRespostaMC("flag|r;type|server;IPPORT|"+ip+":"+port +";hash|" + code + ";");
            System.out.println("Multicast server ligado como ID - " + code);

            while (true) {
                byte[] buffer = new byte[65536];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                dataServer threadtratamentoPedido = new dataServer(packet, code);
                threadtratamentoPedido.start();
            }
        } catch (IOException e) {
            e.printStackTrace();     
        } finally {
            socket.close();      
        }
    }
}

class indexarRecursivo extends Thread {
	
	String url ="";
	
	public indexarRecursivo(String lista){
		this.url = lista;
	}
	
	
	public void run() {
		try {
			int rs = 0;
			
            // Attempt to connect and get the document
            Document doc = Jsoup.connect(url).get();  // Documentation: https://jsoup.org/

            // Title
            System.out.println(doc.title() + "\n");
            String textopai = doc.text();
            String textoSemPlicasPai = dataServer.trataAspas(textopai);
            // Get all links
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                if (link.attr("href").startsWith("#")) {
                    continue;
                }
                if (!link.attr("href").startsWith("http")) {
                    continue;
                }
                dataServer.pilha.add(link.attr("href"));
                Document doc1 = Jsoup.connect(link.attr("href")).get();
                String titulo = doc1.title();
                String textofilho = link.text();
                String textoSemPlicas = dataServer.trataAspas(textofilho);
                titulo = dataServer.trataAspas(titulo);
                
                rs = dataServer.inserePaginasBD(url,titulo,textoSemPlicas,link.attr("href"));
                
                if ( rs == 1) {

                    System.out.println("indexado url " + link.attr("href"));
                    //guardar dados em disco
                    String path = System.getProperty ("user.dir");
                    String ficheiro = path  +"//src//"+ "paginas.sql";
                    String query = "INSERT INTO ucbusca.pagina (url, titulo, sintese, urlorigem) VALUES ('"+link.attr("href") +"','"+titulo+"','"+textoSemPlicas+"','"+url+"')";
                    ligacaoDB.guardaDados(ficheiro, query);
                }
                if ( rs == 0) {
                	
                    System.out.println("url ja inserido na base de dados");
                }
            }

		}catch (IOException e) {
			System.out.println("erro na indexacao");
            e.printStackTrace();
        }
		System.out.println("fim do indexacao da thread indexar");
	}
}
