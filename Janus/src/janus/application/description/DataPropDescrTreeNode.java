package janus.application.description;

import java.net.URI;

class DataPropDescrTreeNode {
	static enum Type {NAMED_CLS, ANON_CLS, NAMED_DATA_PROP, ANON_DATA_PROP, DATA_TYPE, DATA_RANGE_BUT_DATA_TYPE, PROP_CHAR, LABEL};
	
	private Object value; // URI or String
	private Type type;
	
	DataPropDescrTreeNode(Object value, Type type) {
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
			case NAMED_DATA_PROP:
			case DATA_TYPE:
				return true;
			default:
				return false;
		}
	}

}
