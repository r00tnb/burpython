package burp.burpython.core.protocol;

import java.util.HashMap;

import burp.burpython.Burpython;

public class DebugAction implements MyAction {

    @Override
    public ResponseData doAction(HashMap<String, String> paramMap) {
        // TODO Auto-generated method stub
        Burpython.getInstance().info(paramMap.get("debug"));
        return new ResponseData("ok");
    }
    
}