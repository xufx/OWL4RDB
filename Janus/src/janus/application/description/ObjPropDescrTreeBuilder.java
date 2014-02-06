package janus.application.description;

import janus.Janus;
import janus.application.description.PropChars;
import janus.application.description.ObjPropDescrTreeNode.Type;
import java.net.URI;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

class ObjPropDescrTreeBuilder {
	static TreeNode buildHierarchy(URI objProp) {
		//root node: to be visible
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ObjPropDescrTreeNode(objProp, Type.NAMED_OBJ_PROP));
		
		// Domains(intersection)
		root.add(getDomainsHierarchy(objProp));
		
		// Ranges(intersection)
		root.add(getRangesHierarchy(objProp));
		
		// Equivalent object properties
		root.add(getEquivlntObjPropsHierarchy(objProp));
		
		// Super properties
		root.add(getSuprPropsHierarchy(objProp));
		
		// Inverse properties
		root.add(getInversePropsHierarchy(objProp));
		
		// Disjoint properties
		root.add(getDisjntPropsHierarchy(objProp));
		
		// Property chains : not supported in OWL API yet (This is an OWL 2.0 feature.)
		root.add(getPropChainsHierarchy(objProp));
		
		// Characteristics
		root.add(getPropCharacteristicsHierarchy(objProp));
		
		return root;
	}
	
	private static MutableTreeNode getPropCharacteristicsHierarchy(URI objProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ObjPropDescrTreeNode("Characteristics", Type.LABEL));
		node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(PropChars.FUNCTIONAL.toString(), Type.PROP_CHAR)));
		node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(PropChars.INVERSE_FUNCTIONAL.toString(), Type.PROP_CHAR)));
		node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(PropChars.TRANSITIVE.toString(), Type.PROP_CHAR)));
		node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(PropChars.SYMMETRIC.toString(), Type.PROP_CHAR)));
		node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(PropChars.ASYMMETRIC.toString(), Type.PROP_CHAR)));
		node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(PropChars.REFLEXIVE.toString(), Type.PROP_CHAR)));
		node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(PropChars.IRREFLEXIVE.toString(), Type.PROP_CHAR)));
		
		return node;
	}
	
	private static MutableTreeNode getPropChainsHierarchy(URI objProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ObjPropDescrTreeNode("Property chains", Type.LABEL));
		
		return node;
	}
	
	private static MutableTreeNode getDisjntPropsHierarchy(URI objProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ObjPropDescrTreeNode("Disjoint properties", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getNamedDisjntObjProps(objProp);
		for(URI disjoint : named)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(disjoint, Type.NAMED_OBJ_PROP)));
		
		Set<String> anon = Janus.ontBridge.getAnonDisjntObjProps(objProp);
		for(String disjoint : anon)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(disjoint, Type.ANON_OBJ_PROP)));
		
		return node;
	}
	
	private static MutableTreeNode getInversePropsHierarchy(URI objProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ObjPropDescrTreeNode("Inverse properties", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getNamedInverseProps(objProp);
		for(URI inverse : named)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(inverse, Type.NAMED_OBJ_PROP)));
		
		Set<String> anon = Janus.ontBridge.getAnonInverseProps(objProp);
		for(String inverse : anon)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(inverse, Type.ANON_OBJ_PROP)));
		
		return node;
	}
	
	private static MutableTreeNode getSuprPropsHierarchy(URI objProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ObjPropDescrTreeNode("Super properties", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getNamedSupObjProps(objProp);
		for(URI supr : named)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(supr, Type.NAMED_OBJ_PROP)));
		
		Set<String> anon = Janus.ontBridge.getAnonSupObjProps(objProp);
		for(String supr : anon)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(supr, Type.ANON_OBJ_PROP)));
		
		return node;
	}
	
	private static MutableTreeNode getEquivlntObjPropsHierarchy(URI objProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ObjPropDescrTreeNode("Equivalent object properties", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getNamedEuivlntObjProps(objProp);
		for(URI obj : named)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(obj, Type.NAMED_OBJ_PROP)));
		
		Set<String> anon = Janus.ontBridge.getAnonEuivlntObjProps(objProp);
		for(String obj : anon)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(obj, Type.ANON_OBJ_PROP)));
		
		return node;
	}
	
	private static MutableTreeNode getRangesHierarchy(URI objProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ObjPropDescrTreeNode("Ranges(intersection)", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getObjPropNamedRanges(objProp);
		for(URI cls : named)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(cls, Type.NAMED_CLS)));
		
		Set<String> anon = Janus.ontBridge.getObjPropAnonRanges(objProp);
		for(String cls : anon)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(cls, Type.ANON_CLS)));
		
		return node;
	}
	
	private static MutableTreeNode getDomainsHierarchy(URI objProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ObjPropDescrTreeNode("Domains(intersection)", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getObjPropNamedDomains(objProp);
		for(URI cls : named)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(cls, Type.NAMED_CLS)));
		
		Set<String> anon = Janus.ontBridge.getObjPropAnonDomains(objProp);
		for(String cls : anon)
			node.add(new DefaultMutableTreeNode(new ObjPropDescrTreeNode(cls, Type.ANON_CLS)));
		
		return node;
	}
}
