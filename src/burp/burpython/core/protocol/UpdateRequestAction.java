package burp.burpython.core.protocol;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;
import burp.burpython.Burpython;

public class UpdateRequestAction implements MyAction {

    @Override
    public ResponseData doAction(HashMap<String, String> paramMap) {
        // TODO Auto-generated method stub
        if (Burpython.getInstance().invocation.getToolFlag() != IBurpExtenderCallbacks.TOOL_REPEATER) return null;// 只在repeater中使用
        IHttpRequestResponse requestResponse = Burpython.getInstance().invocation.getSelectedMessages()[0];
        IRequestInfo requestInfo = Burpython.getInstance().helpers.analyzeRequest(requestResponse);
        byte[] body = Arrays.copyOfRange(requestResponse.getRequest(),requestInfo.getBodyOffset(),requestResponse.getRequest().length);
        List<String> headers = requestInfo.getHeaders();
        URL url = requestInfo.getUrl();
        String host = url.getHost()+(url.getPort()==80?"":":"+url.getPort());
        for(String k:paramMap.keySet()){
            switch (k) {
                case "get_request":
                    return new ResponseData("ok").put("request", new String(requestResponse.getRequest()));
                case "update_header":
                    String name = paramMap.get("header_name");
                    String value = paramMap.get("header_value");
                    for(String header:headers){
                        if(header.startsWith(name)){
                            headers.remove(header);
                            header = name+": "+value;
                            headers.add(header);
                            requestResponse.setRequest(Burpython.getInstance().helpers.buildHttpMessage(headers, body));
                            return new ResponseData("ok");
                        }
                    }
                case "set_request":
                    String request = paramMap.get("request");
                    requestResponse.setRequest(request.getBytes());
                    return new ResponseData("ok");
                case "get_all_request":
                    HashMap<String, String> result = new HashMap<>();
                    int i=0;
                    for(IHttpRequestResponse rr:getRequestResponseByHost(host)){
                        result.put(i+"", new String(rr.getRequest()));
                        i++;
                    }
                    return new ResponseData(result);
                case "get_last_request":
                    IHttpRequestResponse r = getLastRequestReponse(host);
                    if(r != null){
                        return new ResponseData("ok").put("last", new String(r.getRequest()));
                    }
                default:
                    break;
            }
        }
        return null;
    }

    Vector<IHttpRequestResponse> getRequestResponseByHost(String host){
        IHttpRequestResponse[] rrl = Burpython.getInstance().callbacks.getProxyHistory();
        Vector<IHttpRequestResponse> rrv = new Vector<>();
        for(IHttpRequestResponse r:rrl){
            List<String> header_l = Burpython.getInstance().helpers.analyzeRequest(r.getRequest()).getHeaders();
            for(String h:header_l){
                if(h.startsWith("Host") && h.endsWith(host)){
                    rrv.add(r);
                    break;
                }
            }
        }
        return rrv;
    }

    IHttpRequestResponse getLastRequestReponse(String host){
        IHttpRequestResponse[] rrl = Burpython.getInstance().callbacks.getProxyHistory();
        for(int i=rrl.length-1;i>=0;i--){
            List<String> header_l = Burpython.getInstance().helpers.analyzeRequest(rrl[i].getRequest()).getHeaders();
            for(String h:header_l){
                if(h.startsWith("Host") && h.endsWith(host)){
                    return rrl[i];
                }
            }
        }
        return null;
    }
    
}