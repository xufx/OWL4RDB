package janus.application.ontscheme;

import janus.ImageURIs;
import janus.Janus;
import janus.application.actions.GoToMappedColumnAction;
import janus.application.actions.GoToMappedTableAction;
import janus.application.actions.ShowClassAssertionsAction;
import janus.ontology.OntEntityTypes;

import java.net.URI;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class ClsTree extends JScrollPane implements OntTree {
	private JTree tree;
	private JPopupMenu popupMenu;
	private AbstractAction goToMappedTable;
	private AbstractAction goToMappedColumn;
	private AbstractAction showMembers;
	
	public ClsTree() {
		buildUI();
	}
	
	private void buildUI() {
		tree = new JTree(getClsHierarchy(new OntTreeNode(Janus.ontBridge.getOWLThingURI(), OntEntityTypes.OWL_THING_CLASS)));
		tree.setDragEnabled(true);
		
		tree.setCellRenderer(new OntClassTreeCellRenderer(new ImageIcon(ImageURIs.ONT_NAMED_CLS), 
														new ImageIcon(ImageURIs.ONT_NAMED_EQUIVLNT_CLS)));
		
		tree.add(buildPopupMenu());
		
		ToolTipManager.sharedInstance().registerComponent(tree);
		
		setViewportView(tree);
	}
	
	private JPopupMenu buildPopupMenu() {
		popupMenu = new JPopupMenu();
		
		goToMappedColumn = new GoToMappedColumnAction();
		popupMenu.add(goToMappedColumn);
		
		goToMappedTable = new GoToMappedTableAction();
		popupMenu.add(goToMappedTable);
		
		showMembers = new ShowClassAssertionsAction();
		popupMenu.add(showMembers);
		
		return popupMenu;
	}
	
	private MutableTreeNode getClsHierarchy(OntTreeNode entity) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(entity);
		
		Set<URI> children = Janus.ontBridge.getSubClses(entity.getURI());
		for(URI child : children)
			node.add(getClsHierarchy(new OntTreeNode(child, Janus.mappingMetadata.getClassType(child))));
		
		return node;
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		tree.addTreeSelectionListener(listener);
	}
	
	public void addTreePopupTrigger(MouseListener trigger) {
		tree.addMouseListener(trigger);
	}
	
	void showPopupMenu(int x, int y) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		OntTreeNode ontNode = (OntTreeNode)node.getUserObject();
		OntEntityTypes type = ontNode.getType();
		// setting mapped table menu enabled/disabled
		if (type.equals(OntEntityTypes.OWL_THING_CLASS) 
				|| type.equals(OntEntityTypes.COLUMN_CLASS))
			goToMappedTable.setEnabled(false);
		else
			goToMappedTable.setEnabled(true);
		// setting mapped column menu enabled/disabled
		if (type.equals(OntEntityTypes.OWL_THING_CLASS) 
				|| type.equals(OntEntityTypes.TABLE_CLASS))
			goToMappedColumn.setEnabled(false);
		else
			goToMappedColumn.setEnabled(true);
		
		popupMenu.show(this, x, y);
	}
	
	public OntTreeNode getSelectedEntity() {
		return getSelectedClass();
	}

	private OntTreeNode getSelectedClass() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		
		return (OntTreeNode)node.getUserObject();
	}
	
	TreePath getPathForLocation(int x, int y) {
		return tree.getPathForLocation(x, y);
	}
	
	public void setSelectionPath(TreePath path) {
		tree.setSelectionPath(path);
	}
	
	boolean isPathSelected(TreePath path) {
		return tree.isPathSelected(path);
	}
	
	boolean isSelectionEmpty() {
		return tree.isSelectionEmpty();
	}
	
	public TreePath getTreePathOfEntity(URI entity) {
		return getTreePathOfClass(entity);
	}
	
	private TreePath getTreePathOfClass(URI cls) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
		
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
		
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = e.nextElement();
			OntTreeNode clsNode = (OntTreeNode)node.getUserObject();
	        if (clsNode.getURI().equals(cls))
	            return new TreePath(node.getPath());
	    }
		
		return null;
	}
}

@SuppressWarnings("serial")
class OntClassTreeCellRenderer extends DefaultTreeCellRenderer {
    private Icon clsIcon;
    private Icon equivlntClsIcon;

    OntClassTreeCellRenderer(Icon clsIcon, Icon equivlntClsIcon) {
        this.clsIcon = clsIcon;
        this.equivlntClsIcon = equivlntClsIcon;
    }

    public Component getTreeCellRendererComponent(JTree tree,
							                      Object value,
							                      boolean sel,
							                      boolean expanded,
							                      boolean leaf,
							                      int row,
							                      boolean hasFocus) {
    	setToolTipText(getToolTipText(value));
    	
    	super.getTreeCellRendererComponent(tree, value, sel,
                        				   expanded, leaf, row,
                                           hasFocus);
    	
    	if(isEquivlntClsNode(value))
        	setIcon(equivlntClsIcon);
    	else
    		setIcon(clsIcon);
       
        return this;
    }
    
    private String getToolTipText(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        OntTreeNode clsNode = (OntTreeNode)node.getUserObject();
        
        return clsNode.getToolTipText();
    }

    private boolean isEquivlntClsNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        OntTreeNode clsNode = (OntTreeNode)node.getUserObject();
        
        return Janus.ontBridge.hasEquivlntCls(clsNode.getURI());
    }
}