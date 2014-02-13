package janus.database.metadata;

public class Column {
	private String tableName;
	private String columnName;
	
	Column(String tableName, String columnName) {
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
