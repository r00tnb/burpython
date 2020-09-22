package burp.burpython.UI;

import java.util.Enumeration;
import java.util.Vector;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import burp.burpython.Burpython;
import burp.burpython.UI.pyeditor.LineNumberHeaderView;
import burp.burpython.core.Group;
import burp.burpython.core.PythonScript;

public class ScriptsTree extends JTree implements MouseListener, TreeSelectionListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String name = "Scripts Tree";
    private DefaultMutableTreeNode rootNode;
    private JSplitPane scriptInfoPanel;

    public ScriptsTree(String name) {
        super();
        this.name = name;
        this.scriptInfoPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    public void setSelectionScript(PythonScript script){
        int row = 2;
        for(Enumeration e=this.rootNode.children();e.hasMoreElements();row++){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
            if(node.isLeaf()){
                if(node.getUserObject() instanceof PythonScript && (PythonScript)node.getUserObject() == script){
                    this.setSelectionRow(row-1);
                }
            }else{
                for(Enumeration ee=node.children();ee.hasMoreElements();row++){
                    DefaultMutableTreeNode n = (DefaultMutableTreeNode)ee.nextElement();
                    if((PythonScript)n.getUserObject() == script){
                        this.setSelectionRow(row);
                    }
                }
            }
        }
    }

    public void setGroupData(Vector<Group> groupList) {
        this.rootNode = new DefaultMutableTreeNode(this.name);
        for (PythonScript s : Group.getDefaultGroup().getPythonScripts()) {// 先设置默认分类的数据
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(s, false);
            this.rootNode.add(node);
        }
        for (Group g : groupList) {
            if (g != Group.getDefaultGroup()) {
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(g, true);
                for (PythonScript s : g.getPythonScripts()) {
                    groupNode.add(new DefaultMutableTreeNode(s, false));
                }
                this.rootNode.add(groupNode);
            }
        }
        this.setModel(new DefaultTreeModel(this.rootNode));

        this.addMouseListener(this);// 设置数据后设置右键点击事件
        this.addTreeSelectionListener(this);
    }

    public void expandAll() {// 展开所有节点
        int rowCount = Group.getGroupList().size() + 1;
        for (int rowStart = Group.getDefaultGroup().getPythonScripts().size() + 1; rowStart <= rowCount; rowCount--) {
            this.expandRow(rowCount);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        if (e.isMetaDown()) {
            int x = e.getX();
            int y = e.getY();
            TreePath path = this.getPathForLocation(x, y);
            Object obj = path.getLastPathComponent();
            obj = ((DefaultMutableTreeNode) obj).getUserObject();
            final Object o = obj;
            ScriptsTree tree = this;

            ActionListener createGroupAction = new ActionListener() {// 创建组

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    String groupName = JOptionPane.showInputDialog(null, "Group name:", "type the new group name");
                    if (groupName == null)
                        return;
                    Group.get(groupName);
                    tree.setGroupData(Group.getGroupList());
                    tree.expandAll();
                }
            };
            ActionListener deleteGroupAction = new ActionListener() {// 删除组

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    if (JOptionPane.showConfirmDialog(null,
                            String.format("The group \"%s\" will be deleted!Are you sure?", ((Group) o).getName()),
                            "Tips", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
                        Group.remove(((Group) o).getName());
                        tree.setGroupData(Group.getGroupList());
                        tree.expandAll();
                    }
                }

            };
            ActionListener renameGroupAction = new ActionListener() {// 重命名组

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    String groupName = JOptionPane.showInputDialog(null, "Rename the group:", ((Group) o).getName());
                    if (groupName == null || ((Group) o).getName().equals(groupName))
                        return;
                    ((Group) o).setName(groupName);
                    tree.setGroupData(Group.getGroupList());
                    tree.expandAll();
                }

            };
            ActionListener createToolAction = new ActionListener() {// 创建脚本

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    String scriptName = JOptionPane.showInputDialog(null, "Script name:", "type the Script name");
                    if(scriptName == null || scriptName.equals("")) return;
                    Group group = (o instanceof Group) ? (Group) o : Group.getDefaultGroup();
                    for (Group g : Group.getGroupList()) {
                        if (g.haveScript(scriptName))
                            return;
                    }
                    PythonScript script = new PythonScript(scriptName);
                    group.registerScript(script);
                    tree.setGroupData(Group.getGroupList());
                    tree.expandAll();
                    setSelectionScript(script);
                }

            };
            ActionListener deleteToolAction = new ActionListener() {// 删除脚本

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    if (JOptionPane.showConfirmDialog(null,
                            String.format("The script \"%s\" will be deleted!Are you sure?", ((PythonScript) o).getName()),
                            "Tips", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
                        for(Group g:Group.getGroupList()){
                            if(g.haveScript(((PythonScript) o).getName())){
                                g.removeScript((PythonScript) o);
                                break;
                            }
                        }
                        tree.setGroupData(Group.getGroupList());
                        tree.expandAll();
                        tree.setSelectionRow(1);
                    }
                }

            };
            if (obj instanceof String) {
                // 根节点
                JPopupMenu menu = new JPopupMenu();
                JMenuItem createGroupMenu = new JMenuItem("create group");
                JMenuItem createToolsMenu = new JMenuItem("create tool");

                createGroupMenu.addActionListener(createGroupAction);
                createToolsMenu.addActionListener(createToolAction);

                menu.add(createGroupMenu);
                menu.add(createToolsMenu);
                menu.show(e.getComponent(), x, y);
            } else if (obj instanceof Group) {
                // 非叶节点
                JPopupMenu menu = new JPopupMenu();
                JMenuItem createToolsMenu = new JMenuItem("create tool");
                JMenuItem deleteGroupMenu = new JMenuItem("delete group");
                JMenuItem renameGroupMenu = new JMenuItem("rename");

                createToolsMenu.addActionListener(createToolAction);
                deleteGroupMenu.addActionListener(deleteGroupAction);
                renameGroupMenu.addActionListener(renameGroupAction);

                menu.add(createToolsMenu);
                menu.add(deleteGroupMenu);
                menu.add(renameGroupMenu);
                menu.show(e.getComponent(), x, y);
            } else if(obj instanceof PythonScript){
                JPopupMenu menu = new JPopupMenu();
                JMenuItem deleteScriptMenu = new JMenuItem("delete script");

                deleteScriptMenu.addActionListener(deleteToolAction);

                menu.add(deleteScriptMenu);
                menu.show(e.getComponent(), x, y);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public JSplitPane getScriptInfoPanel() {
        return scriptInfoPanel;
    }

    public void renderScriptInfoPanel(PythonScript script, int currentRow){
            JPanel scriptInfo = new JPanel(new GridLayout(1, 2, 10, 10));
            JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
            Box box = Box.createVerticalBox();
            JTextArea scriptNameField = new JTextArea(script.getName());
            JTextArea scriptDesField = new JTextArea(script.getDescription());
            JComboBox<Group> groupField = new JComboBox<>(Group.getGroupList());
            JButton saveButton = new JButton("EDIT");
            ScriptEditor editor = new ScriptEditor(script.getSourceCode());
            JScrollPane editorPanel = new JScrollPane(editor);

            scriptNameField.setBorder(BorderFactory.createTitledBorder("Script Name:"));
            scriptDesField.setBorder(BorderFactory.createTitledBorder("Description:"));
            groupField.setBorder(BorderFactory.createTitledBorder("Group:"));
            for(Group g:Group.getGroupList()){
                if(g.haveScript(script.getName())){
                    groupField.setSelectedItem(g);
                }
            }
            scriptNameField.setEnabled(false);
            groupField.setEnabled(false);
            editor.setEnabled(false);
            scriptDesField.setEnabled(false);
            saveButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    JButton btn = (JButton) e.getSource();
                    if (btn.getText().equals("EDIT")) {
                        scriptNameField.setEnabled(true);
                        groupField.setEnabled(true);
                        editor.setEnabled(true);
                        scriptDesField.setEnabled(true);
                        btn.setText("SAVE");
                    } else {
                        scriptNameField.setEnabled(false);
                        groupField.setEnabled(false);
                        editor.setEnabled(false);
                        scriptDesField.setEnabled(false);
                        btn.setText("EDIT");
                        //save info
                        String name = scriptNameField.getText();
                        if(name == null){
                            expandAll();
                            setSelectionScript(script);
                            return;
                        };
                        for(Group g:Group.getGroupList()){
                            if(g.haveScript(name) && !name.equals(script.getName())){
                                JOptionPane.showMessageDialog(null, String.format("The script name \"%s\" is existed!", name));
                                expandAll();
                                setSelectionScript(script);
                                return;
                            }
                        }
                        script.setName(name);
                        script.setSourceCode(editor.getText());
                        script.setDescription(scriptDesField.getText());
                        ((Group)groupField.getSelectedItem()).registerScript(script);
                        setGroupData(Group.getGroupList());
                        expandAll();
                        setSelectionScript(script);
                        groupField.setEnabled(false);
                    }
                }
                
            });

            //设置行号
            LineNumberHeaderView line = new LineNumberHeaderView();
            line.setLineHeight(line.getFontMetrics(editor.getFont()).getHeight());
            editorPanel.setRowHeaderView(line);

            panel.add(scriptNameField);
            panel.add(saveButton);
            box.add(groupField);
            box.add(scriptDesField);
            scriptInfo.add(panel);
            scriptInfo.add(box);
            this.scriptInfoPanel.setTopComponent(scriptInfo);
            this.scriptInfoPanel.setBottomComponent(editorPanel);
            this.scriptInfoPanel.setDividerLocation(300);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        // TODO Auto-generated method stub
        TreePath tp = e.getPath();
        Object obj = tp.getPath()[tp.getPathCount() - 1];
        obj = ((DefaultMutableTreeNode) obj).getUserObject();
        if (obj instanceof PythonScript) {
            PythonScript script = (PythonScript)obj;
            renderScriptInfoPanel(script, this.getMinSelectionRow());
        }
    }
}