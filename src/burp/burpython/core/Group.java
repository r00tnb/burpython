package burp.burpython.core;

import java.util.Vector;


public class Group {
    String name;
    Vector<PythonScript> scriptList;
    
    private static Group defaultGroup;
    private static Vector<Group> groupList = new Vector<>();

    final public static String LISTENER_ON = "on";
    final public static String LISTENER_OFF = "off";

    static {
        Group.getDefaultGroup();
        Group.get("listener");// 监听类型脚本分组
    }
    
    public static Boolean isBaseGroup(String name){
        if(name == null) return false;
        switch (name) {
            case "listener":
                return true;
            default:
                return false;
        }
    }

    public static Group get(String name){
        if(name == null) name = "";
        if(name.equals("")) return Group.getDefaultGroup();
        for(Group g:Group.groupList){
            if(g.getName().equals(name)){
                return g;
            }
        }
        Group g = new Group(name);
        Group.groupList.add(g);
        return g;
    }
    public static void remove(String name){
        for(Group g:Group.groupList){
            if(g.getName().equals(name)){
                Group.groupList.remove(g);
                return;
            }
        }
    }
    public static Vector<Group> getGroupList(){
        return Group.groupList;
    }
    public static Group getDefaultGroup(){
        if(Group.defaultGroup == null){
            Group.defaultGroup = new Group("");
            Group.groupList.add(Group.defaultGroup);
        }
        return Group.defaultGroup;
    }

    Group(String name){
        this.name = name;
        this.scriptList = new Vector<PythonScript>();
    }

    public void registerScript(PythonScript script){
        for(Group g:Group.getGroupList()){
            g.removeScript(script);
        }
        this.scriptList.add(script);
        if(!script.getState().equals("")) return;
        switch (this.name) {//设置加入内置组时的初始状态
            case "listener":
                script.setState(Group.LISTENER_OFF);
                break;
        
            default:
                break;
        }
    }

    public Boolean haveScript(String scriptName){
        for(PythonScript s:this.scriptList){
            if(s.getName().equals(scriptName)){
                return true;
            }
        }
        return false;
    }

    public Vector<PythonScript> getPythonScripts(){
        return this.scriptList;
    }

    public void removeScript(String scriptName){
        for(PythonScript s : this.scriptList){
            if(s.getName().equals(scriptName)){
                this.scriptList.remove(s);
                break;
            }
        }
    }
    public void removeScript(PythonScript script){
        this.scriptList.remove(script);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        for(Group g:Group.getGroupList()){
            if(g.getName().equals(name)){
                return;
            }
        }
        this.name = name;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        if(this == Group.getDefaultGroup()) return "(default)";
        if(Group.isBaseGroup(this.name)){
            return this.name+"(Base)";
        }
        return this.name+"(G)";
    }
}