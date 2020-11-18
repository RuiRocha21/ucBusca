package webserver.rmi;

public class Utilizador {
	private String nickname;
	private String password;
	private String admin;
	
	Utilizador(String utilizador,String password,String admin){
		this.nickname = nickname;
		this.password = password;
		this.admin = admin;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAdmin() {
		return admin;
	}

	public void setAdmin(String admin) {
		this.admin = admin;
	}
	
	
	
}
