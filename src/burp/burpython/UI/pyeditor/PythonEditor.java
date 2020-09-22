package burp.burpython.UI.pyeditor;

import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

public class PythonEditor extends StyledEditorKit {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Document createDefaultDocument() {
        // TODO Auto-generated method stub
        return super.createDefaultDocument();
    }

    @Override
    public ViewFactory getViewFactory() {
        // TODO Auto-generated method stub
        return new PythonViewFactory();
    }
}