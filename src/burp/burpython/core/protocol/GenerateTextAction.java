package burp.burpython.core.protocol;

import java.util.HashMap;

import javax.swing.JOptionPane;

import burp.burpython.core.Util;

/**
 * 用于实时生成文本
 * 
 */
public class GenerateTextAction implements MyAction {


    /**
     * @param paramMap 会将键为result的值当做最后的结果输出，当存在键ui时生成对话框，并获取结果返回给脚本
     * 
     */
    @Override
    public ResponseData doAction(HashMap<String, String> paramMap) {
        // TODO Auto-generated method stub
        for(String key:paramMap.keySet()){
            String text = paramMap.get(key);
            switch (key) {
                case "result":
                    Util.setSysClipboardText(text);
                    Util.pressCtrlV();
                    Util.selectText(-text.length());
                    return new ResponseData("over");
                case "ui":
                    String tips = paramMap.get("tips");
                    String ui = this.createUI(tips);
                    return new ResponseData("ok").put("ui",ui==null?"":ui);
            
                default:
                    break;
            }
        }
        
        return new ResponseData("ok");
    }

    public String createUI(String tips){
        return JOptionPane.showInputDialog(null, tips);
    }

}