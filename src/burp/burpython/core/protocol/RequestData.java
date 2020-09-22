package burp.burpython.core.protocol;

import java.util.HashMap;
import java.util.regex.Pattern;

import burp.burpython.Burpython;
import burp.burpython.core.Util;

public class RequestData {
    MyAction action;
    HashMap<String, String> paramsMap;

    public static RequestData createFromString(String data) {
        if (data == null)
            return null;
        data = Util.strip(data, "\r\n");
        if (data.toLowerCase().startsWith("debug")) {
            RequestData request = new RequestData();
            request.action = new DebugAction();
            request.paramsMap.put("debug", Util.b64Decode(data.substring(6)));
            return request;
        }

        HashMap<String, String> paramsMap = new HashMap<>();
        MyAction action = null;
        String[] textList = Util.b64Decode(data).split(" ");
        String actionType = textList[0];
        if (Pattern.compile("[^a-zA-Z0-9]+").matcher(actionType).find()) {// 判断是否有非法字符
            return null;
        }
        try {
            action = (MyAction) Class.forName("burp.burpython.core.protocol." + actionType + "Action").newInstance();
        } catch (ClassNotFoundException cne) {
            // TODO Auto-generated catch block
            Burpython.getInstance().error(actionType+"Action is not exist!");
        }catch(Exception e){
            Burpython.getInstance().printStackTrace(e);
        }
        String params = textList.length>1?textList[1]:"";
        String[] paramList = params.split("&");
        for(String param:paramList){
            if(param.equals("")) continue;
            String[] p = param.split(":");
            paramsMap.put(p[0], p.length<2?"":Util.b64Decode(p[1]));
        }
        RequestData request = new RequestData();
        request.paramsMap = paramsMap;
        request.action = action;
        return  request;
    }

    private RequestData(){
        this.paramsMap = new HashMap<>();
        this.action = null;
    }

    public MyAction getAction() {
        return action;
    }
    public HashMap<String, String> getParamsMap() {
        return paramsMap;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        String result = this.action.getClass().getName()+" ";
        for(String k : paramsMap.keySet()){
            result += k+":"+paramsMap.get(k)+"&";
        }
        return result.substring(0, result.length()-1);
    }
}