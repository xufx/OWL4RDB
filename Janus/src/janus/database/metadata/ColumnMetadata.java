package janus.database.metadata;

class ColumnMetadata implements Comparable<ColumnMetadata> {
	private String columnName;

	private int dataType; //java.sql.Types.something

	private String superColumn;
	
	private boolean isNotNull;
	private boolean isPrimaryKey;
	private boolean isForeignKey;
	private boolean isUniqueKey;
	private boolean isSingleColumnUniqueKey;
	
	ColumnMetadata(String columnName) {
		this.columnName = columnName;
	}
	
	public String getSuperColumn() {
		return superColumn;
	}
	
	public int getDataType() {
		return dataType;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public boolean isSingleColumnUniqueKey() {
		return isSingleColumnUniqueKey;
	}

	public void setSingleColumnUniqueKey(boolean isSingleColumnUniqueKey) {
		this.isSingleColumnUniqueKey = isSingleColumnUniqueKey;
	}

	public boolean isUniqueKey() {
		return isUniqueKey;
	}

	public void setUniqueKey(boolean isUniqueKey) {
		this.isUniqueKey = isUniqueKey;
	}

	public boolean isForeignKey() {
		return isForeignKey;
	}

	public void setForeignKey(boolean isForeignKey) {
		this.isForeignKey = isForeignKey;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public void setPrimaryKey(boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public boolean isNotNull() {
		return isNotNull;
	}

	public void setNotNull(boolean isNotNull) {
		this.isNotNull = isNotNull;
	}

	public void setSuperColumn(String superColumn) {
		this.superColumn = superColumn;
	}
	
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	
	public boolean isKey() {
		if (isPrimaryKey || isForeignKey || isUniqueKey)
			return true;
		
		return false;
	}

	@Override
	public int compareTo(ColumnMetadata o) {
		return columnName.compareTo(o.getColumnName());
	}
}
