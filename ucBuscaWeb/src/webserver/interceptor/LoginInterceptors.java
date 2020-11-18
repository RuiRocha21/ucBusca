package webserver.interceptor;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

import java.util.Map;

public class LoginInterceptors implements Interceptor{
	private static final long serialVersionUID = 189237412378L;
	
	@Override
    public String intercept(ActionInvocation invocation) throws Exception {
        Map<String, Object> session = invocation.getInvocationContext().getSession();


        System.out.println("Intercepting . . .");

        if(session.get("LOGGED_IN") != null)
        {
            System.out.println("[" + session.get("UTILIZADOR") + "] abriu sessao.");
            return invocation.invoke();
        }
        else {
            System.out.println("nao esta ligado ao servidor, encaminhar para login");
            return Action.INPUT;
        }

    }



    @Override
    public void init() { }

    @Override
    public void destroy() { }
}
