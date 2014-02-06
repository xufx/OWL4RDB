package janus.application.query;

public enum QueryTypes {
	SPARQL_DL("SPARQL-DL"),
	SQL("SQL");
	
	private final String typeName;
	
	private QueryTypes(String typeName) {
		this.typeName = typeName;
	}
	
	public String toString() { return typeName; }
}
