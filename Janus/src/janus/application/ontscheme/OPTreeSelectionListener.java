package janus.application.ontscheme;

import janus.application.description.AttrDescribable;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class OPTreeSelectionListener implements TreeSelectionListener {
	private AttrDescribable announcer;
	
	public OPTreeSelectionListener(AttrDescribable announcer) {
		this.announcer = announcer;
	}

	public void valueChanged(TreeSelectionEvent e) {
		JTree tree = (JTree)e.getSource();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		OntTreeNode objPropNode = (OntTreeNode)node.getUserObject();
		
		announcer.describeOWLObjProp(objPropNode.getURI());
	}
}
