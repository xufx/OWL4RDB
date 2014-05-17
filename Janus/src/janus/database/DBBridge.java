package janus.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class DBBridge {
	protected Connection connection;
	//protected ResultSetMetaData rsmd;
	//protected ResultSet rs;
	
	protected static void loadDriver(String driver) {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	protected Connection getConnection(String url, String user, String password) {
		Connection connection = null;
		
		try {
			connection = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) { e.printStackTrace(); }

		return connection;
	}
	
	public boolean isConnected() {
		if(connection == null) return false;
		
		return true;
	}
	
	public void disconnect() {
		try {
			if(isConnected()) connection.close();
		} catch (SQLException e) { e.printStackTrace(); }
	}
	
	/*public String getSuperTable(String catalog, String table) {
		Set<String> pks = getPrimaryKeys(catalog, table);
		Set<String> fks = getForeignKeys(catalog, table);
		
		if (fks.containsAll(pks)) {
			Set<String> referencedTables = new ConcurrentSkipListSet<String>();
			Set<String> referencedColumns = new ConcurrentSkipListSet<String>();
			
			for (String pk: pks) {
				String tableDotColumn = getColumnReferencedBy(catalog, table, pk);
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
				
				Set<String> primaryKeysInSuperTable = getPrimaryKeys(catalog, superTable);
				if (primaryKeysInSuperTable.size() == referencedColumns.size() && primaryKeysInSuperTable.containsAll(referencedColumns))
					return superTable;
			}
		}
		
		return null;
	}*/
	
	public String getColumnTypeName(String catalog, String table, String column) {
		String typeName = null;
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			
			ResultSet columns = dbmd.getColumns(catalog, null, table, column);
			columns.absolute(1);
			typeName = columns.getString("TYPE_NAME");
		} catch(SQLException e) { e.printStackTrace(); }
		
		return typeName;
	}
	
	public int getDataType(String catalog, String table, String column) {
		int type = 2012;
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			
			ResultSet columns = dbmd.getColumns(catalog, null, table, column);
			columns.absolute(1);
			type = columns.getInt("DATA_TYPE");
		} catch(SQLException e) { e.printStackTrace(); }
		
		return type;
	}
	
	public String getColumnDefaultValue(String catalog, String table, String column) {
		String defaultValue = null;
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			
			ResultSet columns = dbmd.getColumns(catalog, null, table, column);
			columns.absolute(1);
			defaultValue = columns.getString("COLUMN_DEF");
		} catch(SQLException e) { e.printStackTrace(); }
		
		return defaultValue;
	}
	
	// about DB Scheme
	public abstract String getConnectedCatalog();
	public abstract Set<String> getTables(String catalog);
	
	public Set<String> getPrimaryKeys(String catalog, String table) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			
			ResultSet pkSet = dbmd.getPrimaryKeys(catalog, null, table);
			while (pkSet.next())
				set.add(pkSet.getString("COLUMN_NAME"));
		} catch(SQLException e) { e.printStackTrace(); }
		
		return set;
	}
	
	/*public Set<String> getNonPKColumns(String catalog, String table) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			
			ResultSet fieldSet = dbmd.getColumns(catalog, null, table, null);
			while (fieldSet.next()) {
				String fieldName = fieldSet.getString("COLUMN_NAME");
				boolean alreadyExists = false;
				
				Set<String> pks = getPrimaryKeys(catalog, table);
				for(String pk : pks)
					if(fieldName.equals(pk)) {
						alreadyExists = true;
						break;
					}

				if(!alreadyExists)
					set.add(fieldName);
			}
		} catch(SQLException e) { e.printStackTrace(); }
		
		return set;
	}*/
	
	public Set<String> getColumns(String catalog, String table) {
		Set<String> set = new ConcurrentSkipListSet<String>();

		try {
			DatabaseMetaData dbmd = connection.getMetaData();

			ResultSet fieldSet = dbmd.getColumns(catalog, null, table, null);
			
			while (fieldSet.next()) {
				String fieldName = fieldSet.getString("COLUMN_NAME");
				set.add(fieldName);
			}
		} catch(SQLException e) { e.printStackTrace(); }

		return set;
	}
	
	public Set<String> getForeignKeys(String catalog, String table) {
		Set<String> set = new ConcurrentSkipListSet<String>();

		try {
			DatabaseMetaData dbmd = connection.getMetaData();

			ResultSet fkSet = dbmd.getImportedKeys(catalog, null, table);
			while (fkSet.next())
				set.add(fkSet.getString("FKCOLUMN_NAME"));
		} catch(SQLException e) { e.printStackTrace(); }

		return set;
	}
	
	public abstract Set<String> getUniqueKeys(String catalog, String table);
	public abstract Set<String> getSingleColumnUniqueKeys(String catalog, String table);
	
	public Set<String> getNotNullColumns(String catalog, String table) {
		Set<String> set = new ConcurrentSkipListSet<String>();

		try {
			DatabaseMetaData dbmd = connection.getMetaData();

			ResultSet fieldSet = dbmd.getColumns(catalog, null, table, null);
			
			while (fieldSet.next()) {
				String isNullable = fieldSet.getString("IS_NULLABLE");
				if (isNullable.equals("NO")) {
					String columnName = fieldSet.getString("COLUMN_NAME");
					set.add(columnName);
				}
			}
		} catch(SQLException e) { e.printStackTrace(); }

		return set;
	}
	
	// about column attributes
	public boolean isNullable(String catalog, String table, String column) {
		boolean is = true;
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			
			ResultSet columns = dbmd.getColumns(catalog, null, table, column);
			columns.absolute(1);
			if(columns.getString("IS_NULLABLE").equals("NO")) is = false;
		} catch(SQLException e) { e.printStackTrace(); }
		
		return is;
	}
	
	public boolean isAutoIncrement(String catalog, String table, String column) {
		boolean is = false;
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			
			ResultSet columns = dbmd.getColumns(catalog, null, table, column);
			columns.absolute(1);
			if(columns.getString("IS_AUTOINCREMENT").equals("YES")) is = true;
		} catch(SQLException e) { e.printStackTrace(); }
		
		return is;
	}
	
	public boolean isPrimaryKey(String catalog, String table, String column) {
		boolean is = false;
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			
			ResultSet pkSet = dbmd.getPrimaryKeys(catalog, null, table);
			while (pkSet.next())
				if(pkSet.getString("COLUMN_NAME").equals(column)) {
					is = true;
					break;
				}
		} catch(SQLException e) { e.printStackTrace(); }
		
		return is;
	}
	
	/*private boolean isForeignKey(String catalog, String table, String column) {
		boolean is = false;
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			
			ResultSet fkSet = dbmd.getImportedKeys(catalog, null, table);
			while (fkSet.next())
				if(fkSet.getString("FKCOLUMN_NAME").equals(column)) {
					is = true;
					break;
				}
		} catch(SQLException e) { e.printStackTrace(); }
		
		return is;
	}*/
	
	//public abstract boolean isUniqueKey(String catalog, String table, String column);
	//public abstract boolean isKey(String catalog, String table, String column);
	
	public String getColumnReferencedBy(String catalog, String table, String column) {
		try {
			DatabaseMetaData dbmd = connection.getMetaData();

			ResultSet fkSet = dbmd.getImportedKeys(catalog, null, table);
			while (fkSet.next())
				if (column.equals(fkSet.getString("FKCOLUMN_NAME"))) {
					String pkTable = fkSet.getString("PKTABLE_NAME");
					String pkColumn = fkSet.getString("PKCOLUMN_NAME");
					
					return pkTable + "." + pkColumn;
				}
					
		} catch(SQLException e) { e.printStackTrace(); }
		
		return null;
	}
	
	/*public String getRootColumnReferencedBy(String catalog, String table, String column) {
		String referencedColumn = getColumnReferencedBy(catalog, table, column);
		String[] tableDotColumn = referencedColumn.split("\\.");
		String parentTable = tableDotColumn[0];
		String parentColumn = tableDotColumn[1];
		
		while (isForeignKey(catalog, parentTable, parentColumn)) {
			referencedColumn = getColumnReferencedBy(catalog, parentTable, parentColumn);
			tableDotColumn = referencedColumn.split("\\.");
			parentTable = tableDotColumn[0];
			parentColumn = tableDotColumn[1];
		}
		
		return parentTable + "." + parentColumn;
	}*/
	
	// about SQL query
	public abstract SQLResultSet executeQuery(String query);
	
	//public abstract int findColumn(String columnLabel);
	
	protected abstract String buildURL(String host, String port, String schema);
}
