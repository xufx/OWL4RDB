package janus.query;

import janus.Janus;
import janus.database.DBColumn;
import janus.database.DBField;
import janus.mapping.DatatypeMap;
import janus.mapping.OntMapper;

import java.net.URI;
import java.util.List;

class MariaDBSQLGenerator extends SQLGenerator {
	
	static SQLGenerator getInstance() {
		return new MariaDBSQLGenerator();
	}
	
	protected String getQueryToGetIndividualsOfSinglePKColumnClass(String aPK, String table) {
		return "SELECT " + getConcatCallStatementToBuildFieldIndividual(table, aPK)
				+ " FROM " + table;
	}
	
	protected String getQueryToGetIndividualsOfTableClass(String table) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		query.append(getConcatCallStatementToBuildRecordIndividual(table));
		
		query.append(" FROM " + table);
		
		return query.toString();
	}
	
	protected String getQueryToGetIndividualsOfNonNullableColumnClass(String keyColumn, String table) {
		return "SELECT DISTINCT " + getConcatCallStatementToBuildFieldIndividual(table, keyColumn)
				+ " FROM " + table;
	}
	
	protected String getQueryToGetIndividualsOfNullableColumnClass(String keyColumn, String table) {
		return "SELECT DISTINCT " + getConcatCallStatementToBuildFieldIndividual(table, keyColumn)
				+ " FROM " + table
				+ " WHERE " + keyColumn + " IS NOT NULL";
	}
	
	protected String getQueryToGetResultSetOf2X0() {
		return "SELECT '', '' FROM DUAL WHERE FALSE";
	} 
	
	private String getConcatCallStatementToBuildFieldIndividual(String table, String keyColumn) {
		DBColumn rootColumn = Janus.cachedDBMetadata.getRootColumn(table, keyColumn);
		
		return "concat('" + OntMapper.TABLE_NAME + "=" + rootColumn.getTableName() + "&" + OntMapper.COLUMN_NAME + "="+ rootColumn.getColumnName() + "&" + OntMapper.VALUE + "=', " + table + "." + keyColumn + ")";
	}
	
	// generates an individual with ordered column names.
	private String getConcatCallStatementToBuildRecordIndividual(String table) {
		String rootTable = Janus.cachedDBMetadata.getRootTable(table);
		
		StringBuffer concat = new StringBuffer("concat('" + OntMapper.TABLE_NAME + "=" + rootTable + "'");
		
		List<String> pkOfRootTable = Janus.cachedDBMetadata.getPrimaryKey(rootTable);
		
		for (String column: pkOfRootTable) {
			String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(rootTable, column, table);
			concat.append(", '&" + OntMapper.PK_COLUMN_NAME + "=" + column + "&" + OntMapper.VALUE + "=', " + table + "." + matchedColumn);
		}
		
		concat.append(")");
		
		return concat.toString();
	}
	
	protected String getQueryToGetOPAssertionOfRecord(URI op, String opColumn, String table, List<DBField> PKFields) {
		String concatCallStatement = getConcatCallStatementToBuildFieldIndividual(table, opColumn);
		
		StringBuffer query = new StringBuffer("SELECT '"  + op.getFragment() +"', " + concatCallStatement
												+ " FROM " + table
												+ " WHERE ");
		
		for (int i = 0; i < PKFields.size() - 1; i++) {
			DBField field = PKFields.get(i);
			query.append(field.getTableName() + "." + field.getColumnName() + " = " + "'" + field.getValue() + "'");
			query.append(" AND ");
		}
		
		DBField lastField = PKFields.get(PKFields.size()-1);
		query.append(lastField.getTableName() + "." + lastField.getColumnName() + " = " + "'" + lastField.getValue() + "'");
		
		if (!Janus.cachedDBMetadata.isNotNull(table, opColumn))
			query.append(" AND " + table + "." + opColumn + " IS NOT NULL");
		
		return query.toString();
	}
	
	private String getConcatCallStatementToBuildLiteral(String table, String dpColumn) {
		int sqlDataType = Janus.cachedDBMetadata.getDataType(table, dpColumn);
		String xmlSchemaDataType = DatatypeMap.get(sqlDataType);
		
		return "concat('\"', " + table + "." + dpColumn + ", '\"', '^^', '" + xmlSchemaDataType + "')";
	}
	
	protected String getQueryToGetDPAssertionOfRecord(URI dp, String dpColumn, String table, List<DBField> PKFields) {
		StringBuffer query = new StringBuffer("SELECT '"  + dp.getFragment() +"', " + getConcatCallStatementToBuildLiteral(table, dpColumn)
												+ " FROM " + table
												+ " WHERE ");
		
		for (int i = 0; i < PKFields.size() - 1; i++) {
			DBField field = PKFields.get(i);
			query.append(field.getTableName() + "." + field.getColumnName() + " = " + "'" + field.getValue() + "'");
			query.append(" AND ");
		}
		
		DBField lastField = PKFields.get(PKFields.size()-1);
		query.append(lastField.getTableName() + "." + lastField.getColumnName() + " = " + "'" + lastField.getValue() + "'");
		
		if (!Janus.cachedDBMetadata.isNotNull(table, dpColumn))
			query.append(" AND " + table + "." + dpColumn + " IS NOT NULL");
		
		return query.toString();
	}
	
	protected String getQueryToGetDPAssertionOfField(URI dp, String dpColumn, String table, String value) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		if (!(Janus.cachedDBMetadata.isPrimaryKey(table, dpColumn)
				&& Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table)))
			query.append("DISTINCT ");
		
		query.append("'"  + dp.getFragment() +"', " + getConcatCallStatementToBuildLiteral(table, dpColumn) 
				+ " FROM " + table
				+ " WHERE " + table + "." + dpColumn + " = '" + value + "'");
		
		return query.toString();
	}
	
	protected String getQueryToGetOPAssertionOfField(DBField field) {
		String table = field.getTableName();
		String column = field.getColumnName();
		String value = field.getValue();
		
		URI op = Janus.mappingMetadata.getMappedObjectProperty(table, column);

		return "SELECT " + getConcatCallStatementToBuildRecordIndividual(table) + ", '" + op.getFragment() + "'"
						+ " FROM " + table 
						+ " WHERE " + table + "." + column + " = " + "'" + value + "'";
	}
	
	protected String getQueryToGetDPAssertionsOfDPWithTableClassDomain(URI dp) {
		DBColumn mappedColumn = Janus.mappingMetadata.getMappedColumnToProperty(dp);
		String table = mappedColumn.getTableName();
		String dpColumn = mappedColumn.getColumnName();
		
		return "SELECT " + getConcatCallStatementToBuildRecordIndividual(table) + ", " + getConcatCallStatementToBuildLiteral(table, dpColumn)
				+ " FROM " + table;
	}
	
	protected String getQueryToGetDPAssertionsOfDPWithColumnClassDomain(URI dp) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		DBColumn mappedColumn = Janus.mappingMetadata.getMappedColumnToProperty(dp);
		String table = mappedColumn.getTableName();
		String column = mappedColumn.getColumnName();
		
		if (!(Janus.cachedDBMetadata.isPrimaryKey(table, column)
				&& Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table)))
			query.append("DISTINCT ");
		
		query.append(getConcatCallStatementToBuildFieldIndividual(table, column) + ", " + getConcatCallStatementToBuildLiteral(table, column)
					 + " FROM " + table);
		
		if (!Janus.cachedDBMetadata.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	//object property assertions 
	public String getQueryToGetOPAsserionsOfOP(URI op) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		DBColumn mappedColumn = Janus.mappingMetadata.getMappedColumnToProperty(op);
		String table = mappedColumn.getTableName();
		String column = mappedColumn.getColumnName();
		
		query.append(getConcatCallStatementToBuildRecordIndividual(table) + ", " + getConcatCallStatementToBuildFieldIndividual(table, column)
				     + " FROM " + table);
		
		if (!Janus.cachedDBMetadata.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	//data property assertions
	protected String getQueryToGetDPAssertionsOfKeyColumnLiteral(DBColumn dbColumn, String lexicalValueOfLiteral) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		String table = dbColumn.getTableName();
		String column = dbColumn.getColumnName();
		
		if (!(Janus.cachedDBMetadata.isPrimaryKey(table, column)
				&& Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table)))
			query.append("DISTINCT ");
		
		URI dp = Janus.mappingMetadata.getMappedDataProperty(table, column);
		
		query.append(getConcatCallStatementToBuildFieldIndividual(table, column) + ", '" + dp.getFragment() + "'"
				 + " FROM " + table
				 + " WHERE " + dbColumn.toString() + " = " + "'" + lexicalValueOfLiteral + "'");
				
		return query.toString();
	}
	
	protected String getQueryToGetDPAssertionsOfNonKeyColumnLiteral(DBColumn dbColumn, String lexicalValueOfLiteral) {
		return null;
	}
}