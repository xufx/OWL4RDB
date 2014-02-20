package janus.application.dbscheme;

import janus.database.DBEntityTypes;

class DBTreeNode {
	private String value;
	private DBEntityTypes type;
	
	DBTreeNode(String value, DBEntityTypes type) {
		this.value = value;
		this.type = type;
	}
	
	public String toString() { return value; }
	DBEntityTypes getType() { return type; }
}