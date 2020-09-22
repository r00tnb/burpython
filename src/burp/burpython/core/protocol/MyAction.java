package burp.burpython.core.protocol;

import java.util.HashMap;

public interface MyAction {
    public ResponseData doAction(HashMap<String, String> paramMap);
}