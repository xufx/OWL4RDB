package janus.application.dbscheme;

class DBTreeNode {
	static enum Type { CATALOG, TABLE, FIELD, KEY };
	
	private String value;
	private Type type;
	
	DBTreeNode(String value, Type type) {
		this.value = value;
		this.type = type;
	}
	
	public String toString() { return value; }
	Type getType() { return type; }
}