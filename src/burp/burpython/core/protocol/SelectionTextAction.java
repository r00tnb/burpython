package burp.burpython.core.protocol;

import java.util.HashMap;

import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import burp.IContextMenuInvocation;
import burp.IHttpRequestResponse;
import burp.burpython.Burpython;
import burp.burpython.core.Util;

public class SelectionTextAction implements MyAction {

    @Override
    public ResponseData doAction(HashMap<String, String> paramMap) {
        // TODO Auto-generated method stub
        IHttpRequestResponse requestResponse = Burpython.getInstance().invocation.getSelectedMessages()[0];
        int[] selectBounds = Burpython.getInstance().invocation.getSelectionBounds();
        if(selectBounds == null) return null;
        int start = selectBounds[0], end = selectBounds[1];
        byte index = Burpython.getInstance().invocation.getInvocationContext();
        byte[] selectBytes = null;
        if(index == IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_RESPONSE || index == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_RESPONSE){
            selectBytes = requestResponse.getResponse();
        }else{
            selectBytes = requestResponse.getRequest();
        }
        for(String k:paramMap.keySet()){
            switch (k) {
                case "get_select_text":
                    return new ResponseData("ok").put("get_select_text", new String(selectBytes).substring(start, end));
                case "set_result":
                    String result = paramMap.get("result");
                    Boolean ui = paramMap.containsKey("ui");
                    String tips = paramMap.get("tips") == null?"The result is:":paramMap.get("tips");
                    if(ui){
                        createUI(result, tips);
                    }else{
                        Util.pressBackspace();
                        Util.setSysClipboardText(result);
                        Util.pressCtrlV();
                        Util.selectText(-result.length());
                    }
                default:
                    break;
            }
        }
        return null;
    }

    void createUI(String result, String tips){
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