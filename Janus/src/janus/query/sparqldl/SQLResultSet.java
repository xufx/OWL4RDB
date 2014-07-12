package janus.query.sparqldl;

import janus.Janus;

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
	
	public String getQuery() {
		return query;
	}
	
	public Vector<String> getColumnNames() {
		return new Vector<String>(columnNames);
	}
	
	public SPARQLDLResultSet getNaturalJoinedResultSet(SPARQLDLResultSet arg) {
		String thisQuery = getQuery();
		String argQuery = arg.getQuery();

		List<String> thisColumnNames = getColumnNames();
		List<String> argColumnNames = arg.getColumnNames();

		argColumnNames.removeAll(thisColumnNames);
		thisColumnNames.addAll(argColumnNames);
		List<String> naturalJoinedColumnNames = thisColumnNames;

		String naturalJoinedQuery = Janus.sqlGenerator.getNaturalJoinedQuery(thisQuery, argQuery, naturalJoinedColumnNames);

		return new SQLResultSet(naturalJoinedQuery, naturalJoinedColumnNames);
	}

}
