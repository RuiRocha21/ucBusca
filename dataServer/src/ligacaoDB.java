import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ligacaoDB {
    private static Statement statement;
    static  Connection conn = null;
    static String url       = "jdbc:mysql://127.0.0.1:3306/mysql?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    //Mysql@127.0.0.1:3306
    static String user      = "root";
    static String password  = "ucbusca2019";
    static String schema = "ucbusca";
    public static Connection getLigacao(){


        try{
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(url, user, password);
        }catch(SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void scriptSQL(String ficheiro) {
        // Ler ficheiro aqui.
        String script = "";

        BufferedReader br = null;
        try {
            String line;
            //System.out.println("ficheiro" + ficheiro);
            //File file = new File(ficheiro);
            FileReader fis = new FileReader(new File(ficheiro));
            br = new BufferedReader(fis);
            while (true) {
                line = br.readLine();
                if (line == null)
                    break;
                script += line;
            }
        } catch (Exception e) {
            System.out.println(e.getClass().getName() + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
        }
        try {
            statement = conn.createStatement();
            //statement.executeUpdate("CREATE DATABASE " +" root");
           // System.out.println("Created database with name " + "root" + ".");

            conn = DriverManager.getConnection(url, user, password);
            statement = conn.createStatement();

            for (String action : script.split(";")) {
                System.out.println("Executando:\n" + action);
                statement.executeUpdate(action + ";");
            }
            System.out.println("tabela " + ficheiro + " criada com sucesso");
            statement.close();
        } catch (Exception e) {
            System.out.println(e.getClass().getName() + e.getMessage());
        }

    }

    public static void guardaDados(String ficheiro,String query){
    	BufferedWriter bw = null;
    	try {
            // Prepara para escrever no arquivo
        	bw = new BufferedWriter(new FileWriter(ficheiro, true));
            // Escreve e fecha arquivo
            bw.write(query);
            bw.write(";");
            bw.write("\n");
            bw.close();
            System.out.println("dados guardados em disco");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}