package webserver.rmi;

import java.io.Serializable;

public class infoServidores implements Serializable,infoModel{
	private static final long serialVersionUID = 1123124L;
	private String info;
	
	public infoServidores(String info){
		setInfo(info);
	}

	public infoServidores()
	{
		this(null);
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
}