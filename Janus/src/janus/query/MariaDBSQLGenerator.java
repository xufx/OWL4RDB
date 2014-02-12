package janus.query;

import java.util.List;

class MariaDBSQLGenerator extends SQLGenerator {
	
	static SQLGenerator getInstance() {
		return new MariaDBSQLGenerator();
	}
	
	protected String getQueryToGetIndividualsOfSinglePKColumnClass(String aPK, String table) {
		return "SELECT " + getConcatCallStatement(table, aPK)
				+ " FROM " + table;
	}
	
	protected String getQueryToGetIndividualsOfTableClass(List<String> pk, String table) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		query.append(getConcatCallStatement(table, pk));
		
		query.append(" FROM " + table);
		
		return query.toString();
	}
	
	protected String getQueryToGetIndividualsOfNonNullableColumnClass(String keyColumn, String table) {
		return "SELECT DISTINCT " + getConcatCallStatement(table, keyColumn)
				+ " FROM " + table;
	}
	
	protected String getQueryToGetIndividualsOfNullableColumnClass(String keyColumn, String table) {
		return "SELECT DISTINCT " + getConcatCallStatement(table, keyColumn)
				+ " FROM " + table
				+ " WHERE " + keyColumn + " IS NOT NULL";
	}
	
	private String getConcatCallStatement(String table, String keyColumn) {
		return "concat('t=" + table + "&c="+ keyColumn + "&v=', " + keyColumn + ")";
	}
	
	private String getConcatCallStatement(String table, List<String> pk) {
		StringBuffer concat = new StringBuffer("concat('t=" + table + "'");
		
		for (String column: pk)
			concat.append(", '&k=" + column + "&v=', " + column);
		
		concat.append(")");
		
		return concat.toString();
	}
}
