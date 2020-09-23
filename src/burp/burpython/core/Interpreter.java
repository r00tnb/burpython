package burp.burpython.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;

import burp.burpython.Burpython;

public class Interpreter {
    volatile String absPath;
    volatile String version;
    volatile Boolean useable = true;

    ConcurrentHashMap<String, Thread> threadMap;
    File burpythonPackage = null;

    {// 默认使用命令行中的python解释器
        this.threadMap = new ConcurrentHashMap<>();
        this.setAbsPath("python");

        // 生成临时文件用于python脚本调用
        String pack = Util.getStringFromFile("burpython.py");
        this.burpythonPackage = Util.createTempFile("burpython.py", pack, Util.random(16));

    }

    public Interpreter() {

    }

    public Interpreter(String abs) {
        this.setAbsPath(abs);
    }

    public void destroy() {
        File tempDir = this.burpythonPackage.getParentFile();
        for (File f : tempDir.listFiles()) {
            f.delete();
        }
        tempDir.delete();
    }

    public Boolean isVersion3() {
        if (this.useable && this.version.startsWith("Python 3"))
            return true;
        return false;
    }

    public String getAbsPath() {
        return absPath;
    }

    public String getVersion() {
        return version;
    }

    public Boolean isUseable() {
        return useable;
    }

    public void execScript(PythonScript script, Executable exec) {
        String cmd = "";
        if (this.isVersion3()) {
            cmd = String.format("import base64;import sys;sys.path.append(r'%s');exec(base64.b64decode(b'%s'))",
                this.burpythonPackage.getParentFile().getAbsolutePath(),Util.b64Encode(script.getSourceCode()));
        } else {
            cmd = String.format("import base64;import sys;sys.path.append(r'%s');exec base64.b64decode('%s')",
                this.burpythonPackage.getParentFile().getAbsolutePath(), Util.b64Encode(script.getSourceCode()));
        }
        try {
            this.executeCommand(script.getName()+"-"+Util.random(4),exec, this.absPath, "-c", cmd);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Burpython.getInstance().printStackTrace(e);
        }
    }

    private void syncExectuteCommand(String name, Executable exec, String... args){//同步执行
        ProcessBuilder builder = new ProcessBuilder();
        builder.redirectErrorStream(true);
        builder.command(args);
        Process process = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            process = builder.start();
            br = new BufferedReader(new InputStreamReader(process.getInputStream(),Burpython.getInstance().getDefaultEncoding()));
            bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), Burpython.getInstance().getDefaultEncoding()));
            exec.handle(br, bw);
            br.close();
            bw.close();
        }catch(IOException ie){
            Burpython.getInstance().debug("process stream closed or error");
        }catch (Exception e) {
            // TODO: handle exception
            exec.fail();
            Burpython.getInstance().printStackTrace(e);
        } finally {
            process.destroy();
        }
    }

    private void executeCommand(String name, Executable exec, String... args){//异步执行
        Thread thread = new Thread(new Runnable(){

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    syncExectuteCommand(name, exec, args);
                } catch (Exception e) {
                    //TODO: handle exception
                }finally{
                    threadMap.remove(name);
                } 
            }
            
        }, name);
        this.threadMap.put(name, thread);
        thread.start();
    }

    public void setAbsPath(String path) {
        this.syncExectuteCommand("test-"+Util.random(4),new Executable() {

            @Override
            public void handle(BufferedReader br, BufferedWriter bw) throws IOException {
                // TODO Auto-generated method stub
                String s = br.readLine();
                if (!s.contains("usage: python")) {
                    useable = false;
                    version = "";
                }else{
                    useable = true;
                }
            }

            @Override
            public void fail() {
                // TODO Auto-generated method stub
                useable = false;
                version = "";
            }
        }, path, "-h");
        this.absPath = path;

        if (!this.useable) {
            return;
        }

        this.syncExectuteCommand("getVersion-"+Util.random(4), new Executable() {

            @Override
            public void handle(BufferedReader br, BufferedWriter bw) throws IOException {
                // TODO Auto-generated method stub
                version = br.readLine();
                // absPath = br.readLine();
            }

            @Override
            public void fail() {
                // TODO Auto-generated method stub

            }

        }, this.absPath, "-V");
    }
}