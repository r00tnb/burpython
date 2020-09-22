package burp.burpython.UI.pyeditor;


import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class PythonViewFactory implements ViewFactory {

    @Override
    public View create(Element elem) {
        // TODO Auto-generated method stub
        return new PythonView(elem);
    }
    
}