package burp.burpython.core;


public class PythonScript {
    String name = "";
    String sourceCode = "";
    String description = "";
    String state = "";// 在不同的内置分组中表现为不同的意义

    static public PythonScript create(String name){
        for(Group g:Group.getGroupList()){
            if(g.haveScript(name)){
                return null;
            }
        }
        return new PythonScript(name);
    }

    private PythonScript(String name) {
        this.name = name;
        this.sourceCode = Util.getStringFromFile("template.py");
        Group.getDefaultGroup().registerScript(this);
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public Group getGroup(){
        for(Group g:Group.getGroupList()){
            if(g.haveScript(this.name)){
                return g;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.name;
    }
}