package burp.burpython.core;


public class PythonScript {
    String name = "";
    String sourceCode = "";
    String description = "";

    public PythonScript(String name) {
        this.name = name;
        this.sourceCode = Util.getStringFromFile("template.py");
        Group.getDefaultGroup().registerScript(this);
    }

    public String getName() {
        return name;
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

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.name;
    }
}