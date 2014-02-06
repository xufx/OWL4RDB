package janus.application.description;

import janus.Janus;
import janus.application.description.PropChars;
import janus.application.description.DataPropDescrTreeNode.Type;
import java.net.URI;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

class DataPropDescrTreeBuilder {
	static TreeNode buildHierarchy(URI dataProp) {
		//root node: to be visible
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new DataPropDescrTreeNode(dataProp, Type.NAMED_DATA_PROP));
		
		// Domains(intersection)
		root.add(getDomainsHierarchy(dataProp));
		
		// Ranges
		root.add(getRangesHierarchy(dataProp));
		
		// Equivalent properties
		root.add(getEquivltPropsHierarchy(dataProp));
		
		// Super properties
		root.add(getSuprPropsHierarchy(dataProp));
		
		// Disjoint properties
		root.add(getDisjntPropsHierarchy(dataProp));
		
		// Characteristics
		root.add(getPropCharacteristicsHierarchy(dataProp));
		
		return root;
	}
	
	private static MutableTreeNode getPropCharacteristicsHierarchy(URI dataProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DataPropDescrTreeNode("Characteristics", Type.LABEL));
		node.add(new DefaultMutableTreeNode(new DataPropDescrTreeNode(PropChars.FUNCTIONAL.toString(), Type.PROP_CHAR)));
		
		return node;
	}
	
	private static MutableTreeNode getDisjntPropsHierarchy(URI dataProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DataPropDescrTreeNode("Disjoint properties", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getNamedDisjntDataProps(dataProp);
		for(URI disjoint : named)
			node.add(new DefaultMutableTreeNode(new DataPropDescrTreeNode(disjoint, Type.NAMED_DATA_PROP)));
		
		Set<String> anon = Janus.ontBridge.getAnonDisjntDataProps(dataProp);
		for(String disjoint : anon)
			node.add(new DefaultMutableTreeNode(new DataPropDescrTreeNode(disjoint, Type.ANON_DATA_PROP)));
		
		return node;
	}
	
	private static MutableTreeNode getSuprPropsHierarchy(URI dataProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DataPropDescrTreeNode("Super properties", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getNamedSupDataProps(dataProp);
		for(URI sup : named)
			node.add(new DefaultMutableTreeNode(new DataPropDescrTreeNode(sup, Type.NAMED_DATA_PROP)));
		
		Set<String> anon = Janus.ontBridge.getAnonSupDataProps(dataProp);
		for(String sup : anon)
			node.add(new DefaultMutableTreeNode(new DataPropDescrTreeNode(sup, Type.ANON_DATA_PROP)));
		
		return node;
	}
	
	private static MutableTreeNode getEquivltPropsHierarchy(URI dataProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DataPropDescrTreeNode("Equivalent properties", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getNamedEuivlntDataProps(dataProp);
		for(URI equi : named)
			node.add(new DefaultMutableTreeNode(new DataPropDescrTreeNode(equi, Type.NAMED_DATA_PROP)));
		
		Set<String> anon = Janus.ontBridge.getAnonEuivlntDataProps(dataProp);
		for(String equi : anon)
			node.add(new DefaultMutableTreeNode(new DataPropDescrTreeNode(equi, Type.ANON_DATA_PROP)));
		
		return node;
	}
	
	private static MutableTreeNode getRangesHierarchy(URI dataProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DataPropDescrTreeNode("Ranges", Type.LABEL));
		
		Set<URI> dt = Janus.ontBridge.getDataPropRangesOfDataType(dataProp);
		for(URI range : dt)
			node.add(new DefaultMutableTreeNode(new DataPropDescrTreeNode(range, Type.DATA_TYPE)));
		
		Set<String> bdt = Janus.ontBridge.getDataPropRangesButDataType(dataProp);
		for(String range : bdt)
			node.add(new DefaultMutableTreeNode(new DataPropDescrTreeNode(range, Type.DATA_RANGE_BUT_DATA_TYPE)));
		
		return node;
	}
	
	private static MutableTreeNode getDomainsHierarchy(URI dataProp) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DataPropDescrTreeNode("Domains(intersection)", Type.LABEL));
		
		Set<URI> named = Janus.ontBridge.getNamedDataPropDomains(dataProp);
		for(URI domain : named)
			node.add(new DefaultMutableTreeNode(new DataPropDescrTreeNode(domain, Type.NAMED_CLS)));
		
		Set<String> anon = Janus.ontBridge.getAnonDataPropDomains(dataProp);
		for(String domain : anon)
			node.add(new DefaultMutableTreeNode(new DataPropDescrTreeNode(domain, Type.NAMED_CLS)));
		
		return node;
	}
}
