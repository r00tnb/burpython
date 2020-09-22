package burp.burpython.UI.pyeditor;

import java.awt.Graphics;
import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

import burp.burpython.Burpython;



public class PythonView extends PlainView {
    
    static HashMap<Pattern, Color> patternColorsMap;
    static Color DEFAULT;

    final static String KEYWORDS = 
    "\\b(True|False|and|as|assert|break|class|continue|def|del|elif|else|except|exec|finally|for|from|global|if|import|in|is|lambda|not|or|pass|print|raise|return|try|while|with|yield)\\b";
    final static String NAME_FUNCTION = "(\\w+(?=\\())";
    final static String NAME_OBJECT = "(\\w+)\\.";
    final static String STRING_NORMAL = "(('')|(\"\")|('.*?[^\\\\]')|(\".*?[^\\\\]\"))";
    final static String STRING_NOTE = "(^#.*)";
    final static String INTEGER = "(\\d+)";
    final static String OPERATOR = "([+\\-*&^%$!~()=/.{}\\[\\]])";
    final static String DEFINE = "(?:import|from) ([\\w, ]+)";
    

    int mulBoolean = 0;// 当匹配到'''或"""时需要闭合，该值为真。

    static{
        DEFAULT = Color.BLACK;
        patternColorsMap = new LinkedHashMap<>();
        //越后注册存储的pattern优先级越高
        patternColorsMap.put(Pattern.compile(INTEGER), Color.red);
        patternColorsMap.put(Pattern.compile(OPERATOR), Color.GRAY);
        patternColorsMap.put(Pattern.compile(DEFINE), new Color(0,150,150));
        patternColorsMap.put(Pattern.compile(KEYWORDS), Color.BLUE);
        patternColorsMap.put(Pattern.compile(NAME_OBJECT), new Color(0,150,150));
        patternColorsMap.put(Pattern.compile(NAME_FUNCTION), new Color(255,10,150));
        patternColorsMap.put(Pattern.compile(STRING_NORMAL), new Color(255,150,20));
        patternColorsMap.put(Pattern.compile(STRING_NOTE), new Color(0,200,0));
    }

    public PythonView(Element elem) {
        super(elem);
        // TODO Auto-generated constructor stub
        this.getDocument().putProperty(PlainDocument.tabSizeAttribute, 4);
    }

    @Override
    protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        try {
            Document doc = this.getDocument();
            String text = doc.getText(p0, p1-p0);
            if(text.length() == 0) return x;
            //g.setFont(new Font("新宋体",Font.BOLD,15));
            

            TreeMap<Integer, Integer> indexMap = new TreeMap<>();//文本块起始索引
            TreeMap<Integer, Color> colorMap = new TreeMap<>();//起始索引对应文本颜色
            TreeMap<Integer, Integer> prioMap = new TreeMap<>();//优先级越高越晚被渲染
            int trunk = text.length()+100;
            //匹配其他
            int prio = trunk;
            for(Pattern pattern:PythonView.patternColorsMap.keySet()){
                Matcher matcher = pattern.matcher(text);
                int i=0;
                while(matcher.find()){
                    indexMap.put(matcher.start(1), matcher.end(1));
                    colorMap.put(matcher.start(1), patternColorsMap.get(pattern));
                    prioMap.put(prio+i, matcher.start(1));
                    i++;
                }
                prio = (prio/trunk+1)*trunk;
            }
            //消除矛盾
            for(int p:prioMap.keySet()){
                int ps = prioMap.get(p);
                Integer pe = indexMap.get(ps);
                for(int q:prioMap.keySet()){
                    int qs = prioMap.get(q);
                    Integer qe = indexMap.get(qs);
                    if(qe == null){
                        continue;
                    }
                    if(p/trunk > q/trunk){
                        if((qs>ps && qs<pe) || (qe>ps && qe<pe)){
                            indexMap.remove(qs);
                            colorMap.remove(qs);
                        }else if(qs<ps && qe>pe){
                            Color c = colorMap.get(qs);
                            indexMap.replace(qs, ps);
                            indexMap.put(pe, qe);
                            colorMap.put(pe, c);
                        }
                    }
                }
            }

            //渲染文本
            Segment seg = this.getLineBuffer();
            int currentIndex = 0;
            for(Map.Entry<Integer, Integer> entry:indexMap.entrySet()){
                int start = entry.getKey();
                int end = entry.getValue();
                if(start>currentIndex){//渲染未匹配的字符串
                    g.setColor(PythonView.DEFAULT);
                    doc.getText(p0+currentIndex, start-currentIndex, seg);
                    x = Utilities.drawTabbedText(seg, x, y, g, this, currentIndex);
                }
                //渲染匹配的字符串
                g.setColor(colorMap.get(start));
                doc.getText(p0+start, end-start, seg);
                x = Utilities.drawTabbedText(seg, x, y, g, this, start);
                
                currentIndex = end;
            }
            //渲染最后没匹配的字符串
            g.setColor(PythonView.DEFAULT);
            doc.getText(p0+currentIndex, text.length()-currentIndex, seg);
            x = Utilities.drawTabbedText(seg, x, y, g, this, currentIndex);
        } catch (Exception e) {
            //TODO: handle exception
            Burpython.getInstance().printStackTrace(e);
        }

        return x;
    }
    
}