package janus.application.ontscheme;

import janus.ImageURIs;
import janus.Janus;
import java.net.URI;
import java.awt.Component;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;

@SuppressWarnings("serial")
public class ClsTree extends JScrollPane {
	private JTree tree;
	
	public ClsTree() {
		buildUI();
	}
	
	private void buildUI() {
		tree = new JTree(getClsHierarchy(Janus.ontBridge.getOWLThingURI()));
		tree.setDragEnabled(true);
		
		tree.setCellRenderer(new OntClassTreeCellRenderer(new ImageIcon(ImageURIs.ONT_NAMED_CLS), 
														new ImageIcon(ImageURIs.ONT_NAMED_EQUIVLNT_CLS)));
		
		ToolTipManager.sharedInstance().registerComponent(tree);
		
		setViewportView(tree);
	}
	
	private MutableTreeNode getClsHierarchy(URI clsURI) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new OntTreeNode(clsURI));
		
		Set<URI> children = Janus.ontBridge.getSubClses(clsURI);
		for(URI child : children)
			node.add(getClsHierarchy(child));
		
		return node;
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		tree.addTreeSelectionListener(listener);
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