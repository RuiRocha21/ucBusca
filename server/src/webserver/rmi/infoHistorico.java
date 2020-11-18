package webserver.rmi;

import java.io.Serializable;

public class infoHistorico implements Serializable,infoModel {
    private static final long serialVersionUID = 1123124L;
    private String termosPesquisa;

    infoHistorico(String termosPesquisa){
        settermosPesquisa(termosPesquisa);
    }

    public infoHistorico()
    {
        this(null);
    }

    public String gettermosPesquisa() {
        return termosPesquisa;
    }

    public void settermosPesquisa(String termosPesquisa) {
        this.termosPesquisa = termosPesquisa;
    }
}
