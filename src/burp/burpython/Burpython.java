package burp.burpython;

import java.io.PrintWriter;
import java.util.Base64;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.burpython.UI.BurpythonTab;
import burp.burpython.core.Group;
import burp.burpython.core.Interpreter;
import burp.burpython.core.PythonScript;
import burp.burpython.core.Util;
import burp.burpython.core.protocol.GenerateTextAction;


public class Burpython {

    public IBurpExtenderCallbacks callbacks;
    public IExtensionHelpers helpers;
    public PrintWriter stdout;
    public IContextMenuInvocation invocation;
    public final String defaultEncoding = System.getProperty("file.encoding");

    private BurpythonTab mainTab;
    private Interpreter pythonInterpreter;
    private Boolean debug;

    public final String name = "Burpython";

    private static Burpython burpPython;

    public static Burpython getInstance(){
        if(Burpython.burpPython == null){
            Burpython.burpPython = new Burpython();
        }
        return Burpython.burpPython;
    }

    Burpython(){
        this.debug = false;
    }

    public synchronized void debug(Object obj){
        if(this.debug)
            this.stdout.println(obj);
    }
    public synchronized void info(Object obj){
        this.stdout.println(obj);
    }
    public synchronized void error(Object obj){
        this.stdout.println(obj);
    }
    public synchronized void printStackTrace(Exception e){
        this.stdout.println(e);
        for(StackTraceElement s:e.getStackTrace()){
            this.stdout.println("        at "+s);
        }
    }

    public void init(IBurpExtenderCallbacks callbacks, IExtensionHelpers helpers){//初始化插件并加载配置
        this.callbacks = callbacks;
        this.helpers = helpers;
        this.stdout = new PrintWriter(callbacks.getStdout(), true);
        this.pythonInterpreter = new Interpreter();
        this.callbacks.setExtensionName(this.name);
        this.mainTab = new BurpythonTab();

        loadConfig();
    }

    private void loadConfig(){
        String configString = this.callbacks.loadExtensionSetting(this.name);
        if(configString == null) return;

        String[] cls = configString.split("\\$");
        for(String obj:cls){
            if(obj.equals("")) continue;
            String[] oList = obj.split("\\|");
            for(int i=1;i<oList.length;i++){
                String[] attrList = oList[i].split(";");
                HashMap<String,String> attrMap = new HashMap<>();
                for(String attr:attrList){
                    String[] a = attr.split(":");
                    attrMap.put(a[0], a.length<2?"":a[1]);
                }
                if(oList[0].equals("Group")){
                    Group.get(Util.b64Decode(attrMap.get("name")));
                }else if(oList[0].equals("PythonScript")){
                    PythonScript s = new PythonScript(Util.b64Decode(attrMap.get("name")));
                    s.setDescription(Util.b64Decode(attrMap.get("description")));
                    s.setSourceCode(Util.b64Decode(attrMap.get("sourceCode")));
                    if(!Util.b64Decode(attrMap.get("group")).equals("")){
                        for(Group g:Group.getGroupList()){
                            if(g.getName().equals(Util.b64Decode(attrMap.get("group")))){
                                g.registerScript(s);
                            }
                        }
                    }
                }else if(oList[0].equals("Interpreter")){
                    this.pythonInterpreter.setAbsPath(Util.b64Decode(attrMap.get("absPath")));
                }
            }
        }
    }
    public void saveConfig(){
        /**
         * 使用自定义格式存储对象数据：
         *      1.类名以$开始
         *      2.对象以|开始，属性以;分割，其中以name:safasdf=的形式表达
         *      3.Group必须在pythonscript之前读取和存储
         *      4.所有字段都进行base64编码存储
         */
        StringBuffer configString = new StringBuffer();
        //save Group
        configString.append("$Group");
        for(Group g:Group.getGroupList()){
            if(g == Group.getDefaultGroup()) continue;
            configString.append("|");
            configString.append(Util.myFormat("name:%s", g.getName()));
        }
        //save PythonScript
        configString.append("$PythonScript");
        for(Group g:Group.getGroupList()){
            for(PythonScript s:g.getPythonScripts()){
                configString.append("|");
                configString.append(Util.myFormat("name:%s;description:%s;sourceCode:%s;group:%s", s.getName(),s.getDescription(),s.getSourceCode(),g.getName()));
            }
        }
        //save Interpreter
        configString.append("$Interpreter");
        configString.append(Util.myFormat("|absPath:%s", this.pythonInterpreter.getAbsPath()));

        this.callbacks.saveExtensionSetting(this.name, configString.toString());
    }

    public BurpythonTab getBurpythonTab(){
        return this.mainTab;
    }

    public Interpreter getPythonInterpreter() {
        return pythonInterpreter;
    }
    
}