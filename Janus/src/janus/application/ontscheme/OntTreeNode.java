package janus.application.ontscheme;

import janus.mapping.OntEntityTypes;

import java.net.URI;

class OntTreeNode {
	private URI uri;
	private OntEntityTypes type;
	
	OntTreeNode(URI uri, OntEntityTypes type) {
		this.uri = uri;
		this.type = type;
	}
	
	URI getURI() { return uri; }
	public String toString() { return uri.getFragment(); }
	String getToolTipText() { return uri.toString(); }
	OntEntityTypes getType() { return type; }
}
