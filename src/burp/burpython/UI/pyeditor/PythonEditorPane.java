package burp.burpython.UI.pyeditor;

import java.awt.Color;
import java.awt.Font;
import java.util.regex.Pattern;

import javax.swing.JTextPane;

public class PythonEditorPane extends JTextPane {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    public PythonEditorPane(){
        setEditorKit(new PythonEditor());

        setFont(new Font("新宋体", Font.BOLD, 15));
    }

    public void setViewColor(String pattern, Color c){
        for(Pattern p:PythonView.patternColorsMap.keySet()){
            if(p.pattern().equals(pattern)){
                PythonView.patternColorsMap.replace(p, c);
            }
        }
    }

    public void setDefaultColor(Color c){
        PythonView.DEFAULT = c;
    }
    
}