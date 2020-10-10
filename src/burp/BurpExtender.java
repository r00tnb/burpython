package burp;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import burp.burpython.Burpython;
import burp.burpython.UI.BurpythonMenu;
import burp.burpython.UI.BurpythonTab;
import burp.burpython.UI.MessageTab;
import burp.burpython.core.Executable;
import burp.burpython.core.Group;
import burp.burpython.core.PythonScript;
import burp.burpython.core.Robot;

public class BurpExtender implements IBurpExtender, ITab, IContextMenuFactory, IExtensionStateListener,
        IMessageEditorTabFactory, IProxyListener {

    public IBurpExtenderCallbacks callbacks;
    public IExtensionHelpers helpers;
    public Burpython burpPython;
    public BurpythonTab mainTab;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        // TODO Auto-generated method stub
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.burpPython = Burpython.getInstance();
        this.burpPython.init(this.callbacks, this.helpers);// init burpython and load config
        this.mainTab = this.burpPython.getBurpythonTab();

        this.callbacks.registerContextMenuFactory(this);
        this.callbacks.registerExtensionStateListener(this);
        this.callbacks.registerMessageEditorTabFactory(this);
        this.callbacks.registerProxyListener(this);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    mainTab.init();
                    // callbacks.customizeUiComponent(mainTab.getMainPanel());
                    callbacks.addSuiteTab(BurpExtender.this);
                } catch (Exception e) {
                    // TODO: handle exception
                    Burpython.getInstance().printStackTrace(e);
                }

            }

        });

    }

    @Override
    public String getTabCaption() {
        // TODO Auto-generated method stub
        return this.burpPython.name;
    }

    @Override
    public Component getUiComponent() {
        // TODO Auto-generated method stub

        return this.mainTab.getMainPanel();
    }

    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        // TODO Auto-generated method stub
        Burpython.getInstance().invocation = invocation;
        Vector<JMenuItem> menuList = new Vector<>();
        menuList.add(new BurpythonMenu(this.burpPython.name));
        return menuList;
    }

    @Override
    public void extensionUnloaded() {
        // TODO Auto-generated method stub
        this.burpPython.saveConfig();
        this.burpPython.getPythonInterpreter().destroy();
    }

    @Override
    public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
        // TODO Auto-generated method stub
        return new MessageTab("BodyJSONBeautiful");
    }

    @Override
    public void processProxyMessage(boolean messageIsRequest, IInterceptedProxyMessage message) {
        // TODO Auto-generated method stub
        Burpython.getInstance().proxydata = new ConcurrentHashMap<>();
        Burpython.getInstance().proxydata.put(messageIsRequest, message);
        for (PythonScript p : Group.get("listener").getPythonScripts()) {
            if (p.getState().equals(Group.LISTENER_ON)) {
                Burpython.getInstance().debug("Exec "+p.getName()+" to handle request from proxy.");
                Burpython.getInstance().getPythonInterpreter().execScript(p, new Executable() {

                    @Override
                    public void handle(BufferedReader br, BufferedWriter bw) throws IOException {
                        // TODO Auto-generated method stub
                        new Robot(br, bw, p).work();
                        p.setRunState(false);
                    }

                    @Override
                    public void fail() {
                        // TODO Auto-generated method stub
                        p.setRunState(false);
                    }
                    
                },true);
            }
        }
    }
    
}