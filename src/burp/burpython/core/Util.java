package burp.burpython.core;

import java.awt.Toolkit;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.Base64;


import burp.burpython.Burpython;

public class Util {
    public static String myFormat(String format, Object... args) {
        Object[] argsList = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            argsList[i] = Util.b64Encode(args[i].toString());
        }
        return String.format(format, argsList);
    }

    public static String strip(String data, String ch) {
        return lstrip(rstrip(data, ch), ch);
    }

    public static String lstrip(String data, String ch) {
        int start = 0;
        for (int i = 0; i < data.length(); i++) {
            Boolean go = false;
            for (int j = 0; j < ch.length(); j++) {
                if (data.charAt(i) == ch.charAt(j)) {
                    start += 1;
                    go = true;
                    break;
                }
            }
            if (!go) {
                break;
            }
        }
        return data.substring(start);
    }

    public static String rstrip(String data, String ch) {
        int end = data.length() - 1;
        for (int i = end; i >= 0; i--) {
            Boolean go = false;
            for (int j = 0; j < ch.length(); j++) {
                if (data.charAt(i) == ch.charAt(j)) {
                    end -= 1;
                    go = true;
                    break;
                }
            }
            if (!go) {
                break;
            }
        }
        return data.substring(0, end + 1);
    }

    public static String b64Encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    public static String b64Decode(String data) {
        if (data == null)
            data = "";

        return new String(Base64.getDecoder().decode(data));
    }

    // 生成随机字符串
    public static String random(int strLength) {
        strLength = Math.abs(strLength);
        if (strLength == 0)
            strLength = 1;
        String result = "";
        String okChar = "0123456789abcdef";
        for (int i = 0; i < strLength; i++) {
            result += okChar.charAt((int) (Math.random() * okChar.length()));
        }
        return result;
    }

    // 获得系统剪贴板文本内容
    public static String getSysClipboardText() {
        String ret = "";
        Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 获取剪切板中的内容
        Transferable clipTf = sysClip.getContents(null);

        if (clipTf != null) {
            // 检查内容是否是文本类型
            if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    ret = (String) clipTf.getTransferData(DataFlavor.stringFlavor);
                } catch (Exception e) {
                    Burpython.getInstance().printStackTrace(e);
                }
            }
        }

        return ret;

    }

    // 设置系统剪贴板内容
    public static void setSysClipboardText(String writeMe) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(writeMe);
        clip.setContents(tText, null);
    }

    // Ctrl+C
    public static void pressCtrlC() {
        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        } catch (AWTException e) {
            // TODO Auto-generated catch block
            Burpython.getInstance().printStackTrace(e);
        }
    }

    // Ctrl+V
    public static void pressCtrlV() {
        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        } catch (AWTException e) {
            // TODO Auto-generated catch block
            Burpython.getInstance().printStackTrace(e);
        }
    }

    // 模拟鼠标选择文本
    public static void selectText(int offset) {
        try {
            Robot robot = new Robot();
            Boolean numLock = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
            Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, false);
            robot.keyPress(KeyEvent.VK_SHIFT);
            if (offset > 0) {
                offset = offset>1000?1000:offset;
                for (int i = 0; i < offset; i++) {
                    robot.keyPress(KeyEvent.VK_RIGHT);
                    robot.keyRelease(KeyEvent.VK_RIGHT);
                }
            } else {
                offset = offset<-1000?-1000:offset;
                for (int i = 0; i < -offset; i++) {
                    robot.keyPress(KeyEvent.VK_LEFT);
                    robot.keyRelease(KeyEvent.VK_LEFT);
                }
            }
            robot.keyRelease(KeyEvent.VK_SHIFT);
            Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, numLock);
        } catch (AWTException e) {
            // TODO Auto-generated catch block
            Burpython.getInstance().printStackTrace(e);
        }
    }

    // 延时指定毫秒
    public static void delay(int ms) {
        try {
            Robot robot = new Robot();
            robot.delay(ms);
        } catch (AWTException e) {
            // TODO Auto-generated catch block
            Burpython.getInstance().printStackTrace(e);
            ;
        }
    }

    // 从文件读取文本
    public static String getStringFromFile(String fileName) {//读取resource文件下的资源
        StringBuffer content = new StringBuffer();
        try {
            InputStream input = Util.class.getResourceAsStream("/burp/burpython/resource/"+fileName);
            InputStreamReader reader = new InputStreamReader(input, "UTF8");
            char[] temp = new char[1024];
            while (reader.read(temp) != -1) {
                content.append(temp);
                temp = new char[1024];
            }
            input.close();
            reader.close();
        } catch (Exception e) {
            // TODO: handle exception
            Burpython.getInstance().printStackTrace(e);
        }
        return content.toString().trim();
    }

    // 创建临时文件,并写入数据
    public static File createTempFile(String fileName, String data, String dirName) {
        dirName = dirName==null?"":Util.strip(dirName, "\\/");
        String filePath = System.getProperty("java.io.tmpdir") + dirName +File.separator + Util.strip(fileName, "\\/");
        File file = new File(filePath);
        try {
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdir();
            }
            if(!file.exists()){
                file.createNewFile();
            }
            OutputStream writer = new FileOutputStream(file);
            writer.write(data.getBytes());
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Burpython.getInstance().printStackTrace(e);
        }
        return file;
    }
}