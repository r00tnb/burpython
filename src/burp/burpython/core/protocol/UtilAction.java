package burp.burpython.core.protocol;

import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import burp.burpython.core.Util;

public class UtilAction implements MyAction {//Provide ui for scripts

    @Override
    public ResponseData doAction(HashMap<String, String> paramMap) {
        // TODO Auto-generated method stub
        String tips = paramMap.get("tips");
        String result = "";
        for(String key:paramMap.keySet()){
            switch (key) {
                case "get_from_ui_input":
                    result = JOptionPane.showInputDialog(null, tips);
                    return new ResponseData("ok").put("get_from_ui_input", result);
                case "set_to_mouse_pointer":
                    result = paramMap.get("result");
                    Util.setSysClipboardText(result);
                    Util.pressCtrlV();
                    Util.selectText(-result.length());
                    return new ResponseData("ok");
                case "set_to_ui_textarea":
                    result = paramMap.get("result");
                    createResultUI(result, tips);
                    return new ResponseData("ok");
                default:
                    break;
            }
        }
        return null;
    }

    void createResultUI(String result, String tips){
        JDialog dialog = new JDialog();
        dialog.setTitle("result");
        dialog.setModal(true);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JLabel label = new JLabel(tips);  
        JTextArea textArea = new JTextArea(result);
        JScrollPane scrollPane = new JScrollPane(textArea);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textArea.setLineWrap(true);

        splitPane.setTopComponent(label);
        splitPane.setBottomComponent(scrollPane);
        splitPane.setDividerLocation(100);

        dialog.setContentPane(splitPane);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
    
}