package burp.burpython;

import java.io.PrintWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IInterceptedProxyMessage;
import burp.burpython.UI.BurpythonTab;
import burp.burpython.core.Group;
import burp.burpython.core.Interpreter;
import burp.burpython.core.PythonScript;
import burp.burpython.core.Util;


public class Burpython {

    public IBurpExtenderCallbacks callbacks;
    public IExtensionHelpers helpers;
    public PrintWriter stdout;
    volatile public IContextMenuInvocation invocation;
    volatile public Map<Boolean, IInterceptedProxyMessage> proxydata;

    private String defaultEncoding = System.getProperty("file.encoding");
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

    {
        this.defaultEncoding = System.getProperty("file.encoding");
        this.proxydata = new ConcurrentHashMap<>();
        this.debug = false;
    }

    Burpython(){
        
    }

    public void setDebug(Boolean b){
        this.debug = b;
    }
    public Boolean isDebug(){
        return this.debug;
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

        setDefaultEncoding();
    }

    public void setDefaultEncoding(){
        if(this.pythonInterpreter.isVersion3()){
            this.defaultEncoding = "UTF-8";
        }else{
            this.defaultEncoding = System.getProperty("file.encoding");
        }
    }

    public String getDefaultEncoding() {
        return this.defaultEncoding;
    }

    public void loadConfigFromString(String configString){
        if(configString == null ) return;
        //destroy data
        Group.removeAll();

        // load config
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
                    Group.get(Util.b64Decode(attrMap.get("name"),"UTF-8"));
                }else if(oList[0].equals("PythonScript")){
                    PythonScript s = PythonScript.create(Util.b64Decode(attrMap.get("name"),"UTF-8"));
                    if(s == null) continue;
                    s.setDescription(Util.b64Decode(attrMap.get("description"),"UTF-8"));
                    s.setSourceCode(Util.b64Decode(attrMap.get("sourceCode"),"UTF-8"));
                    s.setState(Util.b64Decode(attrMap.get("state"),"UTF-8"));
                    if(!Util.b64Decode(attrMap.get("group"),"UTF-8").equals("")){
                        for(Group g:Group.getGroupList()){
                            if(g.getName().equals(Util.b64Decode(attrMap.get("group"),"UTF-8"))){
                                g.registerScript(s);
                            }
                        }
                    }
                }else if(oList[0].equals("Interpreter")){
                    this.pythonInterpreter.setAbsPath(Util.b64Decode(attrMap.get("absPath"),"UTF-8"));
                }
            }
        }
    }

    private void loadConfig(){
        String configString = this.callbacks.loadExtensionSetting(this.name);
        if(configString == null) return;

        loadConfigFromString(configString);
    }

    public String outputConfigString(){
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
                configString.append(Util.myFormat("name:%s;description:%s;sourceCode:%s;group:%s;state:%s", s.getName(),s.getDescription(),s.getSourceCode(),g.getName(),s.getState()));
            }
        }
        //save Interpreter
        configString.append("$Interpreter");
        configString.append(Util.myFormat("|absPath:%s", this.pythonInterpreter.getAbsPath()));

        return configString.toString();
    }
    public void saveConfig(){
        this.callbacks.saveExtensionSetting(this.name, outputConfigString());
    }

    public BurpythonTab getBurpythonTab(){
        return this.mainTab;
    }

    public Interpreter getPythonInterpreter() {
        return pythonInterpreter;
    }
    
}