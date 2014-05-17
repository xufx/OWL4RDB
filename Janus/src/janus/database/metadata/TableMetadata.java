package janus.database.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

class TableMetadata implements Comparable<TableMetadata> {
	private String tableName;

	private String superTableName;
	
	private Set<ColumnMetadata> columnMetaDataSet;
	
	TableMetadata(String tableName) {
		this.tableName = tableName;
		
		columnMetaDataSet = new ConcurrentSkipListSet<ColumnMetadata>();
	}
	
	public String getSuperColumn(String columnName) {
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.getColumnName().equals(columnName))
				return columnMetaData.getSuperColumn();
		
		return null;
	}
	
	public boolean isForeignKey(String columnName) {
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.getColumnName().equals(columnName))
				return columnMetaData.isForeignKey();
		
		return false;
	}
	
	public boolean isSingleColumnUniqueKey(String columnName) {
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.getColumnName().equals(columnName))
				return columnMetaData.isSingleColumnUniqueKey();
		
		return false;
	}
	
	public boolean isPrimaryKey(String columnName) {
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.getColumnName().equals(columnName))
				return columnMetaData.isPrimaryKey();
		
		return false;
	}
	
	public boolean isNotNull(String columnName) {
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.getColumnName().equals(columnName))
				return columnMetaData.isNotNull();
		
		return false;
	}
	
	public boolean isKey(String columnName) {
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.getColumnName().equals(columnName))
				return columnMetaData.isKey();
		
		return false;
	}
	
	public int getDataType(String columnName) {
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.getColumnName().equals(columnName))
				return columnMetaData.getDataType();
		
		return 2012;
	}
	
	public Set<String> getNotNullColumns() {
		Set<String> notNullColumns = new ConcurrentSkipListSet<String>();
		
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.isNotNull())
				notNullColumns.add(columnMetaData.getColumnName());
			
		return notNullColumns;
	}
	
	public Set<String> getSingleColumnUniqueKeys() {
		Set<String> singleColumnUniqueKeys = new ConcurrentSkipListSet<String>();
		
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.isSingleColumnUniqueKey())
				singleColumnUniqueKeys.add(columnMetaData.getColumnName());
			
		return singleColumnUniqueKeys;
	}
	
	public Set<String> getUniqueKeys() {
		Set<String> UniqueKeys = new ConcurrentSkipListSet<String>();
		
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.isUniqueKey())
				UniqueKeys.add(columnMetaData.getColumnName());
			
		return UniqueKeys;
	}
	
	public Set<String> getForeignKeys() {
		Set<String> foreignKeys = new ConcurrentSkipListSet<String>();
		
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.isForeignKey())
				foreignKeys.add(columnMetaData.getColumnName());
			
		return foreignKeys;
	}
	
	public List<String> getPrimaryKey() {
		List<String> primaryKeys = new ArrayList<String>();
		
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			if (columnMetaData.isPrimaryKey())
				primaryKeys.add(columnMetaData.getColumnName());
		
		Collections.sort(primaryKeys);
			
		return primaryKeys;
	}
	
	public Set<String> getColumns() {
		Set<String> columns = new ConcurrentSkipListSet<String>();
		
		for (ColumnMetadata columnMetaData: columnMetaDataSet)
			columns.add(columnMetaData.getColumnName());
			
		return columns;
	}
	
	/*int getRowCount() {
		List<String> primaryKeys = getPrimaryKey();
		
		String query = "SELECT COUNT(DISTINCT ";
		
		for(String pk: primaryKeys)
			query = query + pk + ", ";
		
		query = query.substring(0, query.lastIndexOf(",")) + ") FROM " + tableName;
		
		Janus.dbBridge.executeQuery(query);
		
		List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);
		
		return Integer.parseInt(rowData.get(0));
	}*/
	
	public String getTableName() {
		return tableName;
	}
	
	public void addColumnMetaData(ColumnMetadata columnMetaData) {
		columnMetaDataSet.add(columnMetaData);
	}
	
	public String getSuperTableName() {
		return superTableName;
	}

	public void setSuperTableName(String superTable) {
		this.superTableName = superTable;
	}

	@Override
	public int compareTo(TableMetadata o) {
		return tableName.compareTo(o.getTableName());
	}
}
