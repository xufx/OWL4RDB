package janus.database.metadata;

import janus.Janus;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class CachedDBMetadataFactory {
	public static CachedDBMetadata generateLocalDatabaseMetaData() {
		String catalog = Janus.dbBridge.getConnectedCatalog();
		CachedDBMetadata localDBMD = new CachedDBMetadata(catalog);
		
		Set<String> tables = Janus.dbBridge.getTables(catalog);
		for(String table : tables) {
			TableMetadata tableMetaData = new TableMetadata(table);
			
			Set<String> columns = Janus.dbBridge.getColumns(catalog, table);
			Set<String> primaryKeys = Janus.dbBridge.getPrimaryKeys(catalog, table);
			Set<String> foreignKeys = Janus.dbBridge.getForeignKeys(catalog, table);
			Set<String> uniqueKeys = Janus.dbBridge.getUniqueKeys(catalog, table);
			Set<String> singleColumnUniqueKeys = Janus.dbBridge.getSingleColumnUniqueKeys(catalog, table);
			Set<String> notNullColumns = Janus.dbBridge.getNotNullColumns(catalog, table);
			
			String superTable = getSuperTable(primaryKeys, foreignKeys, catalog, table);
			tableMetaData.setSuperTableName(superTable);
			
			for(String column: columns) {
				ColumnMetadata columnMetaData = new ColumnMetadata(column);
				
				int sqlDataType = Janus.dbBridge.getDataType(catalog, table, column);
				columnMetaData.setDataType(sqlDataType);
				
				if (primaryKeys.contains(column))
					columnMetaData.setPrimaryKey(true);
				
				if (foreignKeys.contains(column)) {
					columnMetaData.setForeignKey(true);
					
					String superColumn = Janus.dbBridge.getColumnReferencedBy(catalog, table, column);
					columnMetaData.setSuperColumn(superColumn);
				}
				
				if (uniqueKeys.contains(column))
					columnMetaData.setUniqueKey(true);
				
				if (singleColumnUniqueKeys.contains(column))
					columnMetaData.setSingleColumnUniqueKey(true);
				
				if (notNullColumns.contains(column))
					columnMetaData.setNotNull(true);
				
				tableMetaData.addColumnMetaData(columnMetaData);
			}
			
			localDBMD.addTableMetaData(tableMetaData);
		}
		
		return localDBMD;
	}
	
	private static String getSuperTable(Set<String> pks, Set<String> fks, String catalog, String table) {
		if (fks.containsAll(pks)) {
			Set<String> referencedTables = new ConcurrentSkipListSet<String>();
			Set<String> referencedColumns = new ConcurrentSkipListSet<String>();
			
			for (String pk: pks) {
				String tableDotColumn = Janus.dbBridge.getColumnReferencedBy(catalog, table, pk);
				String[] splitTableDotColumn = tableDotColumn.split("\\.");
				String referencedTable = splitTableDotColumn[0];
				String referencedColumn = splitTableDotColumn[1];
				referencedTables.add(referencedTable);
				referencedColumns.add(referencedColumn);
			}
			
			if (referencedTables.size() == 1 && referencedColumns.size() == pks.size()) {
				String superTable = null;
				for (String referencedTable: referencedTables)
					superTable = referencedTable;
				
				Set<String> primaryKeysInSuperTable = Janus.dbBridge.getPrimaryKeys(catalog, superTable);
				if (primaryKeysInSuperTable.size() == referencedColumns.size() && primaryKeysInSuperTable.containsAll(referencedColumns))
					return superTable;
			}
		}
		
		return null;
	}
}
