package burp.burpython.UI;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.crypto.interfaces.PBEKey;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import burp.burpython.Burpython;
import burp.burpython.core.Group;
import burp.burpython.core.PythonScript;

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
        tmp1.add(new JLabel("version: " + Burpython.getInstance().getPythonInterpreter().getVersion()));
        if (!Burpython.getInstance().getPythonInterpreter().isUseable()) {
            JLabel l = new JLabel("ERROR!The python interpreter is not useable!Check it out.");
            l.setForeground(Color.RED);
            tmp1.add(l);
        }
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