package janus.application.ontscheme;

import java.net.URI;

import javax.swing.tree.TreePath;

public interface OntTree {
	URI getSelectedEntity();
	
	TreePath getTreePathOfEntity(URI entity);
	
	void setSelectionPath(TreePath path);
}
