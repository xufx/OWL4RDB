package janus.application.description;

import janus.mapping.OntEntity;

import java.net.URI;

class ObjPropDescrTreeNode {
	static enum Type {NAMED_CLS, ANON_CLS, NAMED_OBJ_PROP, ANON_OBJ_PROP, PROP_CHAR, LABEL};
	
	private Object value; // URI or String
	private Type type;
	
	ObjPropDescrTreeNode(Object value, Type type) {
		this.value = value;
		this.type = type;
	}
	
	public String toString() { 
		if(isURI()) return OntEntity.getCURIE(((URI)value));

		return value.toString();
	}
	
	Type getType() { return type; }
	
	URI getURI() { return (URI)value; }
	
	private boolean isURI() {
		switch(type) {
			case NAMED_CLS:
			case NAMED_OBJ_PROP:
				return true;
			default:
				return false;
		}
	}
}
