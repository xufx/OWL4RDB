package janus.application.ontscheme;

import janus.mapping.OntEntityTypes;

import java.net.URI;

import javax.swing.tree.TreePath;

public interface OntTree {
	URI getSelectedEntity();
	
	OntEntityTypes getEntityType(URI entity);
	
	TreePath getTreePathOfEntity(URI entity);
	
	void setSelectionPath(TreePath path);
}
