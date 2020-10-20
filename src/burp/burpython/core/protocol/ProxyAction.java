package burp.burpython.core.protocol;

import java.net.InetAddress;
import java.util.HashMap;

import javax.lang.model.util.ElementScanner6;

import burp.IHttpRequestResponse;
import burp.IInterceptedProxyMessage;
import burp.burpython.Burpython;

public class ProxyAction implements MyAction {

    @Override
    public ResponseData doAction(HashMap<String, String> paramMap) {
        // TODO Auto-generated method stub
        IInterceptedProxyMessage message = Burpython.getInstance().proxydata.values().iterator().next();
        Boolean isRequest = Burpython.getInstance().proxydata.keySet().iterator().next();
        if(message == null) return null;
        IHttpRequestResponse requestResponse = message.getMessageInfo();
        InetAddress ipaAddress = message.getClientIpAddress();
        
        for(String key:paramMap.keySet()){
            switch (key) {
                case "get_request_response":
                    byte[] request = requestResponse.getRequest();
                    byte[] response = requestResponse.getResponse();
                    return new ResponseData("ok").put("request", new String(request == null?"burpython_null".getBytes():request))
                        .put("response", new String(response == null?"burpython_null".getBytes():response));
                case "get_is_request":
                    return new ResponseData("ok").put("get_is_request", isRequest?"1":"0");    
                case "get_ip":
                    return new ResponseData("ok").put("get_ip", new String(ipaAddress.getCanonicalHostName()));
                case "get_proxy_listener_addr":
                    return new ResponseData("ok").put("get_proxy_listener_addr", message.getListenerInterface());
                case "set_action":
                    int action = Integer.parseInt(paramMap.get("action"));
                    message.setInterceptAction(action);
                    break;
                case "set_result": // 设置拦截的请求或响应
                    String requestOrResponse = paramMap.get("set_result");
                    if(isRequest){
                        requestResponse.setRequest(requestOrResponse.getBytes());
                    }else{
                        requestResponse.setResponse(requestOrResponse.getBytes());
                    }
                default:
                    break;
            }
        }

        return null;
    }
    
}