package burp.burpython.UI;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

import burp.IContextMenuInvocation;
import burp.burpython.Burpython;
import burp.burpython.core.Executable;
import burp.burpython.core.Group;
import burp.burpython.core.PythonScript;
import burp.burpython.core.Robot;

public class BurpythonMenu extends JMenu {


    public BurpythonMenu(String name) {
        super(name);

        this.init();
    }

    public void init() {
        for (Group g : Group.getGroupList()) {
            JMenu groupMenu = new JMenu(g.getName());
            for(PythonScript s:g.getPythonScripts()){
                JMenuItem menu = new JMenuItem(s.getName());
                menu.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO Auto-generated method stub
                        Burpython.getInstance().debug("You click "+s.getName());
                        Burpython.getInstance().getPythonInterpreter().execScript(s, new Executable(){

                            @Override
                            public void handle(BufferedReader br, BufferedWriter bw) throws IOException {
                                // TODO Auto-generated method stub
                                new Robot(br, bw).work();
                            }

                            @Override
                            public void fail() {
                                // TODO Auto-generated method stub
                                
                            }
                            
                        });
                    }
                    
                });
                if(g == Group.getDefaultGroup()){
                    this.add(menu);
                }else{
                    groupMenu.add(menu);
                }
            }
            if(g != Group.getDefaultGroup())
                this.add(groupMenu);
        }
    }
}