package janus.database;

public class Column {
	private String tableName;
	private String columnName;
	
	public Column(String tableName, String columnName) {
		this.tableName = tableName;
		this.columnName = columnName;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String getColumnName() {
		return columnName;
	}
}
