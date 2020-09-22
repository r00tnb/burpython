package burp.burpython.core.protocol;

import java.util.HashMap;

import burp.burpython.core.Util;


public class ResponseData {
    HashMap<String, String> paramMap;

    public ResponseData(HashMap<String, String> paramsMap){
        this.paramMap = paramsMap;
    }

    public ResponseData(String data){
        this.paramMap = new HashMap<>();
        this.paramMap.put("msg", data);
    }

    public String get(String key){
        return this.paramMap.get(key);
    }
    public ResponseData put(String key, String value){
        this.paramMap.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        String response = "";
        for(String s:paramMap.keySet()){
            response += s+":"+Util.b64Encode(paramMap.get(s))+"&";
        }
        response = response.equals("")?"":response.substring(0, response.length()-1);
        return response;
    }

    public String info(){
        String response = "";
        for(String s:paramMap.keySet()){
            response += s+":"+paramMap.get(s)+"&";
        }
        response = response.equals("")?"":response.substring(0, response.length()-1);
        return response;
    }
}