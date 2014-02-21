package janus.database;

public class DBColumn {
	private String tableName;
	private String columnName;
	
	public DBColumn(String tableName, String columnName) {
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
