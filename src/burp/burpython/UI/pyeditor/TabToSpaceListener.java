package burp.burpython.UI.pyeditor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import burp.burpython.core.Util;

public class TabToSpaceListener extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        if(e.getKeyCode() == KeyEvent.VK_TAB){
            e.consume();
            Util.keyPress(' ');
            Util.keyPress(' ');
            Util.keyPress(' ');
            Util.keyPress(' ');
        }
    }
}