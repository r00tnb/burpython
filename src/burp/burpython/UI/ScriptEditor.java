package burp.burpython.UI;

import java.awt.Font;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import burp.burpython.Burpython;
import burp.burpython.UI.pyeditor.PythonEditor;
import burp.burpython.UI.pyeditor.PythonEditorPane;

public class ScriptEditor extends PythonEditorPane {

    public ScriptEditor(String defaultValue){
        super();
        this.setText(defaultValue);
    }

}