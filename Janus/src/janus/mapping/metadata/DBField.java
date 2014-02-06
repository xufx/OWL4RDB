package janus.mapping.metadata;

public class DBField implements Comparable<DBField> {
	private String tableName;
	private String columnName;
	private String value;
	
	public DBField(String tableName, String columnName, String value) {
		this.tableName = tableName;
		this.columnName = columnName;
		this.value = value;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public int compareTo(DBField o) {
		return columnName.compareTo(o.getColumnName());
	}
}
