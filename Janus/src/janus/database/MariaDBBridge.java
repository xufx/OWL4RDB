package janus.database;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;

final class MariaDBBridge extends DBBridge {
	private MariaDBBridge(String host, String port, String id, String password, String schema) {
		loadDriver(DBMSTypes.MARIADB.driver());
		
		connection = getConnection(buildURL(host, port, schema), id, password);
	}
	
	static MariaDBBridge getInstance(String host, String port, String id, String password, String schema) {
		return new MariaDBBridge(host, port, id, password, schema);
	}
	
	protected String buildURL(String host, String port, String schema) {
		return "jdbc:mysql://" + host + ":" + port + "/" + schema;
	}
	
	public SQLResultSet executeQuery(String query) {
		SQLResultSet SQLRS = null;
		
		try {
			Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
															 ResultSet.CONCUR_READ_ONLY,
															 ResultSet.HOLD_CURSORS_OVER_COMMIT);
		
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			
			SQLRS = new SQLResultSet(rs, rsmd);
		} catch(SQLException e) { e.printStackTrace(); }
		
		return SQLRS;
	}
	
	/*public int findColumn(String columnLabel) {
		int index = 0;
		
		try {
			index = rs.findColumn(columnLabel);
		} catch(SQLException e) {e.printStackTrace(); }
		
		return index;
	}*/
	
	public String getConnectedCatalog() {
		String db = null;
		
		try {
			db = connection.getCatalog();
		} catch(SQLException e) { e.printStackTrace(); }
		
		return db;
	}
	
	public Set<String> getTables(String catalog) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			
			String[] types = {"TABLE", /*"VIEW", */"SYSTEM TABLE"/*, "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM"*/};
			ResultSet tableSet = dbmd.getTables(catalog, null, null, types);
			while (tableSet.next())
				set.add(tableSet.getString("TABLE_NAME"));
		} catch(SQLException e) { e.printStackTrace(); }
		
		return set;
	}
	
	public Set<String> getUniqueKeys(String catalog, String table) {
		Set<String> set = new ConcurrentSkipListSet<String>();

		try {
			Statement stmt = connection.createStatement();
			
			ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + catalog + "." + table);
			
			rs.absolute(1);
			
			String DDLForCreateTable = rs.getString(2);
			
			int beginIndexOfUniqueKey = DDLForCreateTable.indexOf("UNIQUE KEY");
			
			while (beginIndexOfUniqueKey > -1) {
				int beginIndexOfColumns = DDLForCreateTable.indexOf("(", beginIndexOfUniqueKey)+1;
				int endIndexOfColumns = DDLForCreateTable.indexOf(")", beginIndexOfUniqueKey);

				String columns = DDLForCreateTable.substring(beginIndexOfColumns, endIndexOfColumns);
				String[] list = columns.split(",");
				for (String columnName: list)
					set.add(columnName.substring(1, columnName.length()-1));
				
				beginIndexOfUniqueKey = DDLForCreateTable.indexOf("UNIQUE KEY", endIndexOfColumns+1);
			}
		} catch(SQLException e) { e.printStackTrace(); }

		return set;
	}
	
	public Set<String> getSingleColumnUniqueKeys(String catalog, String table) {
		Set<String> set = new ConcurrentSkipListSet<String>();

		try {
			Statement stmt = connection.createStatement();
			
			ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + catalog + "." + table);
			
			rs.absolute(1);
			
			String DDLForCreateTable = rs.getString(2);
			
			int beginIndexOfUniqueKey = DDLForCreateTable.indexOf("UNIQUE KEY");
			
			while (beginIndexOfUniqueKey > -1) {
				int beginIndexOfColumns = DDLForCreateTable.indexOf("(", beginIndexOfUniqueKey)+1;
				int endIndexOfColumns = DDLForCreateTable.indexOf(")", beginIndexOfUniqueKey);

				String columns = DDLForCreateTable.substring(beginIndexOfColumns, endIndexOfColumns);
				String[] list = columns.split(",");
				if (list.length == 1)
					set.add(list[0].substring(1, list[0].length()-1));
				
				beginIndexOfUniqueKey = DDLForCreateTable.indexOf("UNIQUE KEY", endIndexOfColumns+1);
			}
		} catch(SQLException e) { e.printStackTrace(); }

		return set;
	}
	
	/*public boolean isUniqueKey(String catalog, String table, String column) {
		boolean is = false;
		
		Set<String> ukSet = getUniqueKeys(catalog, table);
		for (String uk: ukSet)
			if(uk.equals(column)) {
				is = true;
				break;
			}
		
		return is;
	}*/
	
	/*public boolean isKey(String catalog, String table, String column) {
		if (isPrimaryKey(catalog, table, column)
				|| isForeignKey(catalog, table, column)
				|| isUniqueKey(catalog, table, column))
			return true;

		return false;
	}
	
	public boolean isForeignKey(String catalog, String table, String column) {
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
	
	public String getColumnTypeName(String catalog, String table, String column) {
		String typeName = null;
		
		try {
			Statement stmt = connection.createStatement();
			
			ResultSet rs = stmt.executeQuery("DESCRIBE " + catalog + "." + table + " " + column);
			ResultSetMetaData rsmd = rs.getMetaData();
			
			int cnt = rsmd.getColumnCount();
			
			rs.absolute(1);
			
			for(int i = 1; i <= cnt; i++)
				if(rsmd.getColumnLabel(i).equals("Type")) {
					typeName = toUpperCase(rs.getString(i));
					break;
				}
		} catch(SQLException e) { e.printStackTrace(); }
		
		return typeName;
	}
	
	private String toUpperCase(String type) {
		if(type.indexOf('(') < 0) return type.toUpperCase();
		
		int open = type.indexOf('(');
		int close = type.lastIndexOf(')');
		
		String head = type.substring(0, open).toUpperCase();
		String tail = type.substring(close+1).toUpperCase();
		
		return head + type.substring(open, close+1) + tail;
	}
	
	public String getColumnDefaultValue(String catalog, String table, String column) {
		String defaultValue = null;
		
		try {
			Statement stmt = connection.createStatement();
			
			ResultSet rs = stmt.executeQuery("DESCRIBE " + catalog + "." + table + " " + column);
			ResultSetMetaData rsmd = rs.getMetaData();
			
			int cnt = rsmd.getColumnCount();
			
			rs.absolute(1);
			
			for(int i = 1; i <= cnt; i++)
				if(rsmd.getColumnLabel(i).equals("Default")) {
					defaultValue = rs.getString(i);
					break;
				}
		} catch(SQLException e) { e.printStackTrace(); }
		
		return defaultValue;
	}
}