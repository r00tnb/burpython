package burp.burpython.UI;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import javax.crypto.interfaces.PBEKey;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import burp.burpython.Burpython;
import burp.burpython.core.Group;
import burp.burpython.core.PythonScript;
import burp.burpython.core.Util;

public class BurpythonTab {
    private JSplitPane mainPanel;
    private JSplitPane leftPanel;
    private ScriptsTree scriptsTree;
    private JPanel pythonInfoPanel;

    public JSplitPane getMainPanel() {
        return this.mainPanel;
    }

    public BurpythonTab() {

    }

    public void renderPythonInfoPanel() {
        this.pythonInfoPanel.removeAll();
        Box tmp1 = Box.createVerticalBox();
        Box optionBox = Box.createVerticalBox();
        tmp1.add(new JLabel("version: " + Burpython.getInstance().getPythonInterpreter().getVersion()));
        if (!Burpython.getInstance().getPythonInterpreter().isUseable()) {
            JLabel l = new JLabel("ERROR!The python interpreter is not useable!Check it out.");
            l.setForeground(Color.RED);
            tmp1.add(l);
        }
        tmp1.add(optionBox);
        JCheckBox debugCheck = new JCheckBox("输出调试信息", Burpython.getInstance().isDebug());
        optionBox.add(debugCheck);
        debugCheck.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                // TODO Auto-generated method stub
                JCheckBox box = (JCheckBox)e.getSource();
                if(box.isSelected()){
                    Burpython.getInstance().setDebug(true);
                }else{
                    Burpython.getInstance().setDebug(false);
                }
            }
            
        });
        JButton exportPackage = new JButton("export Burpython package");
        optionBox.add(exportPackage);
        exportPackage.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setSelectedFile(new File("burpython.py"));
                fileChooser.showSaveDialog(null);
                if(fileChooser == null) return;
                File saveFile = fileChooser.getSelectedFile();
                if(saveFile.exists()){
                    if(JOptionPane.showConfirmDialog(null, "File exists.Are you want to over it?") != 0){
                        return;
                    }
                }
                try {
                    FileOutputStream writer = new FileOutputStream(saveFile);
                    writer.write(Util.getStringFromFile("burpython.py").getBytes());
                    writer.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    Burpython.getInstance().error("File \""+ saveFile.getAbsolutePath()+ "\" save failed!");
                }
            }
            
        });
        JPanel tmp2 = new JPanel(new GridLayout(2, 1, 10, 10));
        JTextArea pathText = new JTextArea(Burpython.getInstance().getPythonInterpreter().getAbsPath());
        pathText.setBorder(BorderFactory.createTitledBorder("Python path:"));
        pathText.setEnabled(false);
        JButton saveButton = new JButton("EDIT");
        saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                JButton btn = (JButton) e.getSource();
                if (btn.getText().equals("EDIT")) {
                    pathText.setEnabled(true);
                    btn.setText("SAVE");
                } else {
                    pathText.setEnabled(false);
                    btn.setText("EDIT");

                    // save path
                    Burpython.getInstance().getPythonInterpreter()
                            .setAbsPath(pathText.getText() == null ? "python" : pathText.getText());
                    renderPythonInfoPanel();
                }
            }

        });
        tmp2.add(pathText);
        tmp2.add(saveButton);
        this.pythonInfoPanel.add(tmp1);
        this.pythonInfoPanel.add(tmp2);
    }

    public void init() {
        this.mainPanel = new JSplitPane();
        this.scriptsTree = new ScriptsTree("Scripts Tree");
        this.leftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.pythonInfoPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        this.scriptsTree.setGroupData(Group.getGroupList());
        this.scriptsTree.expandAll();

        // python解释器信息
        this.renderPythonInfoPanel();

        this.leftPanel.setBottomComponent(this.pythonInfoPanel);
        this.leftPanel.setTopComponent(this.scriptsTree);
        this.leftPanel.setDividerLocation(300);

        this.mainPanel.setLeftComponent(this.leftPanel);
        this.mainPanel.setRightComponent(this.scriptsTree.getScriptInfoPanel());
        this.mainPanel.setDividerLocation(300);
    }
}