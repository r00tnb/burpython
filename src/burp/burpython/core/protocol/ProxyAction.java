package burp.burpython.core.protocol;

import java.net.InetAddress;
import java.util.HashMap;

import burp.IHttpRequestResponse;
import burp.IInterceptedProxyMessage;
import burp.burpython.Burpython;

public class ProxyAction implements MyAction {

    @Override
    public ResponseData doAction(HashMap<String, String> paramMap) {
        // TODO Auto-generated method stub
        IInterceptedProxyMessage message = Burpython.getInstance().proxydata.values().iterator().next();
        if(message == null) return null;
        IHttpRequestResponse requestResponse = message.getMessageInfo();
        InetAddress ipaAddress = message.getClientIpAddress();
        
        for(String key:paramMap.keySet()){
            switch (key) {
                case "get_request":
                    return new ResponseData("ok").put("get_request", new String(requestResponse.getRequest()));
                    
                case "get_ip":
                    return new ResponseData("ok").put("get_ip", new String(ipaAddress.getCanonicalHostName()));
                case "get_proxy_listener_addr":
                    return new ResponseData("ok").put("get_proxy_listener_addr", message.getListenerInterface());
                case "set_action":
                    int action = Integer.parseInt(paramMap.get("action"));
                    message.setInterceptAction(action);
                    break;
                default:
                    break;
            }
        }

        return null;
    }
    
}