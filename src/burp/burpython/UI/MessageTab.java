package burp.burpython.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import java.awt.GridLayout;

import javax.accessibility.Accessible;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.Scrollable;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import burp.IMessageEditor;
import burp.IMessageEditorTab;
import burp.ITextEditor;
import burp.burpython.Burpython;
import burp.burpython.UI.pyeditor.LineNumberHeaderView;
import burp.burpython.UI.pyeditor.TabToSpaceListener;
import burp.burpython.core.Util;

public class MessageTab extends JScrollPane implements IMessageEditorTab {

    String name = "";
    JTextArea textPane;
    byte[] content;
    int bodyIndex;
    String jsonData;
    String SPACE = "    ";

    public MessageTab(String name) {
        this.name = name;
        setViewportView(textPane = new JTextArea());
        textPane.addKeyListener(new TabToSpaceListener());
        LineNumberHeaderView line = new LineNumberHeaderView();
        line.setLineHeight(line.getFontMetrics(textPane.getFont()).getHeight());
        setRowHeaderView(line);
    }

    public byte[] buildData(){
        StringBuilder data = new StringBuilder();
        data.append(new String(content).substring(0,bodyIndex+4));
        data.append(jsonData.replaceAll("(\\n[ ]{4,})|\\n|\\t", ""));
        return data.toString().getBytes();
    }

    public void formatJson(){
        String body = new String(content).substring(bodyIndex);
        jsonData = Util.urlDecode(body);
        
        StringBuilder json = new StringBuilder();
        int spaceNum = 0;
        jsonData += "$";
        for(int i=0;i<jsonData.length()-1;i++){
            char ch = jsonData.charAt(i);
            json.append(ch);
            if(ch == '{' || ch == '['){
                spaceNum++;
                json.append("\n"+space(spaceNum));
            }
            if(ch == ','){
                json.append("\n"+space(spaceNum));
            }
            if(ch == '}' || ch == ']'){
                spaceNum--;
                json.deleteCharAt(json.length()-1);
                json.append("\n"+space(spaceNum)+ch);
                if(jsonData.charAt(i+1) != ',')
                    json.append("\n"+space(spaceNum));
            }
        }

        jsonData = json.toString();
    }

    protected String space(int num){
        String result = "";
        for(int i=0;i<num;i++){
            result += SPACE;
        }
        return result;
    }

    @Override
    public String getTabCaption() {
        // TODO Auto-generated method stub
        return this.name;
    }

    @Override
    public Component getUiComponent() {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public boolean isEnabled(byte[] content, boolean isRequest) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setMessage(byte[] content, boolean isRequest) {
        // TODO Auto-generated method stub
        this.content = content;
        this.bodyIndex = new String(content).lastIndexOf("\r\n\r\n")+4;
        formatJson();
        textPane.setText(jsonData);
    }

    @Override
    public byte[] getMessage() {
        // TODO Auto-generated method stub
        jsonData = textPane.getText();
        return buildData();
    }

    @Override
    public boolean isModified() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public byte[] getSelectedData() {
        // TODO Auto-generated method stub
        return null;
    }
    
}