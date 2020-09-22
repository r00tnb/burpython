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

    public void haveJScrollPanel(Container c, String path){
        if(c instanceof ITextEditor){
            this.allScroll.add(path);
        }else if(c.getComponents().length>0){
            int i=0;
            for(Component cc:c.getComponents()){
                if(cc instanceof Container){
                    this.haveJScrollPanel((Container)cc, path+"."+i);
                }else{
                    this.allScroll.add(path+".notContainer");
                }
                i++;
            }
        }else
            this.allScroll.add(path+".none");
    }

    public MessageTab(String name) {
        this.name = name;
        this.mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        this.mainPanel.setName("okok");
        this.allScroll = new Vector<>();

        JButton b = new JButton("click");
        JTextArea t = new JTextArea();
        JScrollPane ss = new JScrollPane(t);
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                StringBuffer stringBuffer = new StringBuffer();
                Component[] cList = mainPanel.getParent().getComponents();
                int i = 0;
                for (Component c : cList) {
                    i++;
                    stringBuffer.append(c.toString() + "\n");
                    // stringBuffer.append(i+": "+(c instanceof JScrollPane)+"\n");
                    if (c instanceof JComponent && i == 1) {
                        haveJScrollPanel((Container)mainPanel.getParent(), "raw");
                        for(String str:allScroll){
                            stringBuffer.append(i+": "+str+"\n");
                        }
                        stringBuffer.append(i+": "+((JTextComponent)((JComponent)((JComponent)c).getComponent(0)).getComponent(0)).getText()+"\n");
                        ((JTextComponent)((JComponent)((JComponent)c).getComponent(0)).getComponent(0)).addMouseListener(new MouseListener() {

                            @Override
                            public void mouseClicked(MouseEvent e) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void mousePressed(MouseEvent e) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                // TODO Auto-generated method stub
                                //JOptionPane.showMessageDialog(null, "test123");
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                // TODO Auto-generated method stub

                            }
                            
                        });;
                        
                    }
                    
                }
                t.setText(stringBuffer.toString());
            }
            
        });
        this.mainPanel.add(ss);
        this.mainPanel.add(b);
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