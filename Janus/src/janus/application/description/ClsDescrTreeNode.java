package janus.application.description;

import java.net.URI;

class ClsDescrTreeNode {
	static enum Type {NAMED_CLS, ANON_CLS, KEYS, LABEL};
	
	private Object value; // URI or String
	private Type type;
	
	ClsDescrTreeNode(Object value, Type type) {
		this.value = value;
		this.type = type;
	}
	
	public String toString() { 
		if(isURI()) return ((URI)value).getFragment();

		return value.toString();
	}
	
	Type getType() { return type; }
	
	URI getURI() { return (URI)value; }
	
	private boolean isURI() {
		switch(type) {
			case NAMED_CLS:
				return true;
			default:
				return false;
		}
	}
}
