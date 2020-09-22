package burp.burpython.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import burp.burpython.Burpython;
import burp.burpython.core.protocol.*;

public class Robot{// 用于和脚本交互
    BufferedReader br;
    BufferedWriter bw;

    public Robot(BufferedReader br, BufferedWriter bw) {
        this.br = br;
        this.bw = bw;
    }

    public RequestData recvLine(){
        RequestData result = null;
        String data = "";
        try {
            data = this.br.readLine();
            result = RequestData.createFromString(data);
        }catch(IllegalArgumentException decodeException){
            Burpython.getInstance().error("Script Error:\n"+data+"\n"+this.recvAll()+"\n");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Burpython.getInstance().debug("connection closed");
        }
        return result;
    }
    public void sendLine(ResponseData data){
        try {
            this.bw.write(data.toString() + "\n");
            this.bw.flush();
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Burpython.getInstance().debug("connection closed");
        }
    }

    public String recvAll(){
        String s="";
        StringBuffer data = new StringBuffer();
        try {
            while ((s = this.br.readLine()) != null) {
                data.append(s+"\n");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Burpython.getInstance().debug("connection closed");
        }
        return data.toString();
    }

    public void work(){// 每次发送完以\n结尾
        RequestData request = null;
        try {
            while((request = this.recvLine()) != null){
                Burpython.getInstance().debug("recv: "+request);
                if(request.getAction() instanceof CloseAction){
                    break;
                }else if(request.getAction() instanceof DebugAction){
                    Burpython.getInstance().info(
                        "\n******************Info from script***************\n"+request.getParamsMap().get("debug")+"\n*******************************************\n");
                    continue;
                }
                ResponseData data = request.getAction().doAction(request.getParamsMap());
                if(data == null){
                    Burpython.getInstance().debug("Burpython close connection.");
                    break;
                }
                Burpython.getInstance().debug("send: "+data.info());
                this.sendLine(data);
            }
            
        }catch (Exception e) {
            //TODO: handle exception
            Burpython.getInstance().printStackTrace(e);
        }catch(Error err){
            err.printStackTrace(Burpython.getInstance().stdout);
        }
        finally{
            Burpython.getInstance().debug("over");
        }
    }
}