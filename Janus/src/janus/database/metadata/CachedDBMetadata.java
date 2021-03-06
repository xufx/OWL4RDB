package janus.database.metadata;

import janus.database.DBColumn;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class CachedDBMetadata {
	private String catalog;

	private Set<TableMetadata> tables;
	
	CachedDBMetadata(String catalog) {
		this.catalog = catalog;
		tables = new ConcurrentSkipListSet<TableMetadata>();
	}
	
	public String getSuperColumn(String tableName, String columnName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.getSuperColumn(columnName);
		
		return null;
	}
	
	public String getRootTableDotColumn(String tableName, String columnName) {
		String superColumn = getSuperColumn(tableName, columnName);
		
		while (superColumn != null) {
			String[] tableDotColumn = superColumn.split("\\.");
			tableName = tableDotColumn[0];
			columnName = tableDotColumn[1];
			
			superColumn = getSuperColumn(tableName, columnName);
		}
		
		return tableName + "." + columnName;
	}
	
	public DBColumn getRootColumn(String tableName, String columnName) {
		String superColumn = getSuperColumn(tableName, columnName);
		
		while (superColumn != null) {
			String[] tableDotColumn = superColumn.split("\\.");
			tableName = tableDotColumn[0];
			columnName = tableDotColumn[1];
			
			superColumn = getSuperColumn(tableName, columnName);
		}
		
		return new DBColumn(tableName, columnName);
	}
	
	public boolean isForeignKey(String tableName, String columnName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.isForeignKey(columnName);
		
		return false;
	}
	
	public boolean isRootTable(String tableName) {
		if (getRootTable(tableName).equals(tableName))
			return true;
		else
			return false;
	}
	
	public boolean isRootColumn(String tableName, String columnName) {
		String rootColumn = getRootTableDotColumn(tableName, columnName);
		String myColumn = tableName + "." + columnName;
		
		if (rootColumn.equals(myColumn))
			return true;
		else
			return false;
	}
	
	public boolean isSingleColumnUniqueKey(String tableName, String columnName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.isSingleColumnUniqueKey(columnName);
		
		return false;
	}
	
	public boolean isPrimaryKey(String tableName, String columnName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.isPrimaryKey(columnName);
		
		return false;
	}
	
	public boolean isNotNull(String tableName, String columnName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.isNotNull(columnName);
		
		return false;
	}
	
	public boolean isKey(String tableName, String columnName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.isKey(columnName);
		
		return false;
	}
	
	public int getDataType(String tableName, String columnName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.getDataType(columnName);
		
		return 2012;
	}
	
	public void addTableMetaData(TableMetadata tableMetaData) {
		tables.add(tableMetaData);
	}
	
	public String getCatalog() {
		return catalog;
	}
	
	public Set<String> getTableNames() {
		Set<String> tableNames = new ConcurrentSkipListSet<String>();
		
		for (TableMetadata table: tables)
			tableNames.add(table.getTableName());
		
		return tableNames;
	}
	
	public Set<String> getColumns(String tableName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.getColumns();
		
		return null;
	}
	
	public List<String> getPrimaryKey(String tableName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.getPrimaryKey();
		
		return null;
	}
	
	public boolean isPrimaryKeySingleColumn(String tableName) {
		if (getPrimaryKey(tableName).size() < 2)
			return true;
		
		return false;
	}
	
	/*public int getRowCount(String tableName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.getRowCount();
		
		return -1;
	}*/
	
	public Set<String> getForeignKeys(String tableName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.getForeignKeys();
		
		return null;
	}
	
	public Set<String> getUniqueKeys(String tableName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.getUniqueKeys();
		
		return null;
	}
	
	public Set<String> getSingleColumnUniqueKeys(String tableName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.getSingleColumnUniqueKeys();
		
		return null;
	}
	
	public Set<String> getNoNullColumns(String tableName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.getNotNullColumns();
		
		return null;
	}
	
	public Set<String> getNullableColumns(String tableName) {
		Set<String> nullableColumns = new ConcurrentSkipListSet<String>();
		
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName)) {
				nullableColumns.addAll(table.getColumns());
				nullableColumns.removeAll(table.getNotNullColumns());
				
				break;
			}
		
		return nullableColumns;
	}
	
	public String getSuperTable(String tableName) {
		for (TableMetadata table: tables)
			if (table.getTableName().equals(tableName))
				return table.getSuperTableName();
		
		return null;
	}
	
	public String getRootTable(String tableName) {
		String superTable = getSuperTable(tableName);
		
		while (superTable != null) {
			tableName = superTable;
			
			superTable = getSuperTable(tableName);
		}
		
		return tableName;
	}
	
	public Set<String> getKeyColumns(String tableName) {
		Set<String> keyColumns = new ConcurrentSkipListSet<String>();
		
		keyColumns.addAll(getPrimaryKey(tableName));
		keyColumns.addAll(getForeignKeys(tableName));
		keyColumns.addAll(getUniqueKeys(tableName));
		
		return keyColumns;
	}
	
	public Set<String> getNonKeyColumns(String tableName) {
		Set<String> nonKeyColumns = new ConcurrentSkipListSet<String>();
		
		nonKeyColumns.addAll(getColumns(tableName));
		nonKeyColumns.removeAll(getKeyColumns(tableName));
		
		return nonKeyColumns;
	}
	
	public String getMatchedPKColumnAmongFamilyTables(String srcTable, String srcColumn, String targetTable) {
		String rootTableName = getRootTable(targetTable);
		
		String superTable = srcTable;
		String superColumn = srcColumn;
		while (!superTable.equals(rootTableName)) {
			String superTableDotColumn = getSuperColumn(superTable, superColumn);
			superTable = superTableDotColumn.substring(0, superTableDotColumn.indexOf("."));
			superColumn = superTableDotColumn.substring(superTableDotColumn.indexOf(".")+1);
		}
		String matchedRootColumnForMe = superColumn;
		
		List<String> targetPKs = getPrimaryKey(targetTable);
		for (String targetPK: targetPKs) {
			superTable = targetTable;
			superColumn = targetPK;
			while (!superTable.equals(rootTableName)) {
				String superTableDotColumn = getSuperColumn(superTable, superColumn);
				superTable = superTableDotColumn.substring(0, superTableDotColumn.indexOf("."));
				superColumn = superTableDotColumn.substring(superTableDotColumn.indexOf(".")+1);
			}
			String matchedRootColumnForTarget = superColumn;
			
			if (matchedRootColumnForTarget.equals(matchedRootColumnForMe))
				return targetPK;
		}
		
		return null;
	}
	
	public Set<String> getFamilyTables(String tableName) {
		Set<String> familyTables = new ConcurrentSkipListSet<String>();
		
		String rootTable = getRootTable(tableName);
		
		Set<String> allTables = getTableNames();
		for (String table: allTables)
			if (getRootTable(table).equals(rootTable))
				familyTables.add(table);
		
		return familyTables;
	}
	
	public Set<DBColumn> getFamilyColumns(DBColumn column) {
		Set<DBColumn> familyColumns = new ConcurrentSkipListSet<DBColumn>();
		
		DBColumn rootColumn = getRootColumn(column.getTableName(), column.getColumnName());
		
		Set<String> allTables = getTableNames();
		for (String table: allTables) {
			Set<String> columns = getKeyColumns(table);
			for (String aColumn: columns)
				if (getRootColumn(table, aColumn).equals(rootColumn))
					familyColumns.add(new DBColumn(table, aColumn));
		}
		
		return familyColumns;
	}
	
	public Set<DBColumn> getFamilyColumns(String tableName, String columnName) {
		return getFamilyColumns(new DBColumn(tableName, columnName));
	}
	
	public Set<DBColumn> getColumsIncludedInTables(Set<String> tables, Set<DBColumn> columns) {
		Set<DBColumn> members = new ConcurrentSkipListSet<DBColumn>();
		
		for (DBColumn column: columns)
			if (tables.contains(column.getTableName()))
				members.add(column);
		
		return members;
	}
	
	public Set<String> getRootTables() {
		Set<String> rootTables = new ConcurrentSkipListSet<String>();
		
		Set<String> tables = getTableNames();
		for (String table: tables)
			if (isRootTable(table))
				rootTables.add(table);
		
		return rootTables;
	}
	
	public Set<DBColumn> getRootColumns() {
		Set<DBColumn> rootColumns = new ConcurrentSkipListSet<DBColumn>();
		
		Set<String> tables = getTableNames();
		for (String table: tables) {
			Set<String> keyColumns = getKeyColumns(table);
			for (String column: keyColumns)
				if (isRootColumn(table, column))
					rootColumns.add(new DBColumn(table, column));
		}
		
		return rootColumns;
	}
}
