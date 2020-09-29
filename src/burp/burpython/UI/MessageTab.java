package burp.burpython.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import burp.IMessageEditor;
import burp.IMessageEditorTab;
import burp.ITextEditor;

public class MessageTab implements IMessageEditorTab {

    String name = "";
    JPanel mainPanel;
    Vector<String> allScroll;

    public MessageTab(String name) {
        this.name = name;
        this.mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        this.mainPanel.setName("okok");
        this.allScroll = new Vector<>();

        
    }

    @Override
    public String getTabCaption() {
        // TODO Auto-generated method stub
        return this.name;
    }

    @Override
    public Component getUiComponent() {
        // TODO Auto-generated method stub
        return this.mainPanel;
    }

    @Override
    public boolean isEnabled(byte[] content, boolean isRequest) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setMessage(byte[] content, boolean isRequest) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte[] getMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isModified() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public byte[] getSelectedData() {
        // TODO Auto-generated method stub
        return null;
    }
    
}