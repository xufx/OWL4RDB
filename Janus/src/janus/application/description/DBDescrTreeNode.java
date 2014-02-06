package janus.application.description;

class DBDescrTreeNode {
static enum Type {COLUMN, NOT_NULL, AUTO_INC, LABEL, DATA_TYPE, DEFAULT_VALUE};
	
	private String value;
	private Type type;
	
	DBDescrTreeNode(String value, Type type) {
		this.value = value;
		this.type = type;
	}
	
	public String toString() { 
		return value;
	}
	
	Type getType() { return type; }
}
