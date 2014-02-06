package janus.application.ontscheme;

import java.net.URI;

class OntTreeNode {
	private URI uri;
	
	OntTreeNode(URI uri) {
		this.uri = uri;
	}
	
	URI getURI() { return uri; }
	public String toString() { return uri.getFragment(); }
	String getToolTipText() { return uri.toString(); }
}
