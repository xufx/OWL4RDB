package janus.application.description;

import janus.Janus;
import janus.application.description.ClsDescrTreeNode.Type;
import java.net.URI;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

class ClsDescrTreeBuilder {
	static TreeNode buildHierarchy(URI cls) {
		// root node: to be visible
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ClsDescrTreeNode(cls, Type.NAMED_CLS));
		
		// equivalentClasses
		root.add(getEquivlntClsesHierarchy(cls));
		
		// superClasses
		root.add(getSuprClsesHierarchy(cls));
		
		// inferredAnonymousClasses
		root.add(getInferredAnonClsesHierarchy(cls));
		
		// keys
		root.add(getKeysHierarchy(cls));
		
		// disjointClasses
		root.add(getDisjntClsesHierarchy(cls));
		
		return root;
	}
	
	private static MutableTreeNode getKeysHierarchy(URI cls) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ClsDescrTreeNode("Keys", Type.LABEL));
		
		Set<String> setOfKeys = Janus.ontBridge.getKeys(cls);
		
		for (String keys: setOfKeys)
			node.add(new DefaultMutableTreeNode(new ClsDescrTreeNode(keys, Type.KEYS)));
		
		return node;
	}
	
	private static MutableTreeNode getDisjntClsesHierarchy(URI cls) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ClsDescrTreeNode("Disjoint classes", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getNamedDisjntClses(cls);
		for(URI disjoint : named)
			node.add(new DefaultMutableTreeNode(new ClsDescrTreeNode(disjoint, Type.NAMED_CLS)));
		
		Set<String> anon = Janus.ontBridge.getAnonDisjntClses(cls);
		for(String disjoint : anon)
			node.add(new DefaultMutableTreeNode(new ClsDescrTreeNode(disjoint, Type.ANON_CLS)));
		
		return node;
	}
	
	private static MutableTreeNode getEquivlntClsesHierarchy(URI cls) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ClsDescrTreeNode("Equivalent classes", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getAssertedNamedEquivlntClses(cls);
		for(URI equi : named)
			node.add(new DefaultMutableTreeNode(new ClsDescrTreeNode(equi, Type.NAMED_CLS)));
		
		Set<String> anon = Janus.ontBridge.getAssertedAnonEquivlntClses(cls);
		for(String equi : anon)
			node.add(new DefaultMutableTreeNode(new ClsDescrTreeNode(equi, Type.ANON_CLS)));
		
		return node;
	}
	
	private static MutableTreeNode getSuprClsesHierarchy(URI cls) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ClsDescrTreeNode("Superclasses", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getAssertedNamedSuprclses(cls);
		for(URI sup : named)
			node.add(new DefaultMutableTreeNode(new ClsDescrTreeNode(sup, Type.NAMED_CLS)));
		
		Set<String> anon = Janus.ontBridge.getAssertedAnonSuprclses(cls);
		for(String sup : anon)
			node.add(new DefaultMutableTreeNode(new ClsDescrTreeNode(sup, Type.ANON_CLS)));
		
		return node;
	}
	
	private static MutableTreeNode getInferredAnonClsesHierarchy(URI cls) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ClsDescrTreeNode("Inferred anonymous classes", Type.LABEL));
		
		Set<String> anons = Janus.ontBridge.getInferredAnonClses(cls);
		for(String inferred : anons)
			node.add(new DefaultMutableTreeNode(new ClsDescrTreeNode(inferred, Type.ANON_CLS)));
		
		return node;
	}
}