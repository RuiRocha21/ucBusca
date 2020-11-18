package webserver.rmi;
import java.io.Serializable;


public class infoPesquisas implements Serializable,infoModel{
	private static final long serialVersionUID = 1123124L;
	private String resPesquisa;


	infoPesquisas(String resPesquisa){
		setInfoPesquisa(resPesquisa);

	}

	public infoPesquisas()
	{
		this(null);
	}

	public String geInfoPesquisa() {
		return resPesquisa;
	}

	public void setInfoPesquisa(String resPesquisa) {
		this.resPesquisa = resPesquisa;
	}


}

