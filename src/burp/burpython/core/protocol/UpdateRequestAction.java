package burp.burpython.core.protocol;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;
import burp.burpython.Burpython;

public class UpdateRequestAction implements MyAction {

    @Override
    public ResponseData doAction(HashMap<String, String> paramMap) {
        // TODO Auto-generated method stub
        if (Burpython.getInstance().invocation.getToolFlag() != IBurpExtenderCallbacks.TOOL_REPEATER) return null;
        IHttpRequestResponse requestResponse = Burpython.getInstance().invocation.getSelectedMessages()[0];
        IRequestInfo requestInfo = Burpython.getInstance().helpers.analyzeRequest(requestResponse);
        byte[] body = Arrays.copyOfRange(requestResponse.getRequest(),requestInfo.getBodyOffset(),requestResponse.getRequest().length);
        List<String> headers = requestInfo.getHeaders();
        URL url = requestInfo.getUrl();
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
                case "get_all_request":
                    HashMap<String, String> result = new HashMap<>();
                    int i=0;
                    String urlPrefix = url.getProtocol()+"://"+url.getHost()+(url.getPort()==80?"":":"+url.getPort());
                    for(IHttpRequestResponse rr:Burpython.getInstance().callbacks.getSiteMap(urlPrefix)){
                        result.put(i+"", new String(rr.getRequest()));
                        i++;
                    }
                    return new ResponseData(result);
                default:
                    break;
            }
        }
        return null;
    }
    
}