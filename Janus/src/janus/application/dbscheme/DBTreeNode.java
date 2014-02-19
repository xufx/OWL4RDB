package janus.application.dbscheme;

class DBTreeNode {
	private String value;
	private DBTreeNodeTypes type;
	
	DBTreeNode(String value, DBTreeNodeTypes type) {
		this.value = value;
		this.type = type;
	}
	
	public String toString() { return value; }
	DBTreeNodeTypes getType() { return type; }
}