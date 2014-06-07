package janus.query.sparqldl;

import java.util.List;
import java.util.Vector;

class SQLResultSet implements SPARQLDLResultSet {
	private String query;
	
	private List<String> columnNames;
	
	private boolean isEmptySet = false;
	
	SQLResultSet(String query, List<String> columnNames) {
		this.query = query;
		this.columnNames = columnNames;
	}
	
	SQLResultSet(String query, String columnName) {
		this.query = query;
		
		columnNames = new Vector<String>();
		columnNames.add(columnName);
	}
	
	public boolean isEmptySet() {
		return isEmptySet;
	}
	
	void setEmptySet() {
		isEmptySet = true;
	}

}
