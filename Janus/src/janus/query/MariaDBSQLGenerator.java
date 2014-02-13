package janus.query;

import janus.Janus;
import janus.database.metadata.Column;
import janus.mapping.DBField;
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
	
	protected String getQueryToGetIndividualsOfTableClass(List<String> pk, String table) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		query.append(getConcatCallStatementToBuildRecordIndividual(table, pk));
		
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
		return "concat('" + OntMapper.TABLE_NAME + "=" + table + "&" + OntMapper.COLUMN_NAME + "="+ keyColumn + "&" + OntMapper.VALUE + "=', " + keyColumn + ")";
	}
	
	private String getConcatCallStatementToBuildFieldIndividual(String rootTable, String rootColumn, String valueTable, String valueColumn) {
		return "concat('" + OntMapper.TABLE_NAME + "=" + rootTable + "&" + OntMapper.COLUMN_NAME + "="+ rootColumn + "&" + OntMapper.VALUE + "=', " + valueTable + "." + valueColumn + ")";
	}
	
	private String getConcatCallStatementToBuildRecordIndividual(String table, List<String> pk) {
		StringBuffer concat = new StringBuffer("concat('" + OntMapper.TABLE_NAME + "=" + table + "'");
		
		for (String column: pk)
			concat.append(", '&" + OntMapper.PK_COLUMN_NAME + "=" + column + "&" + OntMapper.VALUE + "=', " + column);
		
		concat.append(")");
		
		return concat.toString();
	}
	
	protected String getQueryToGetOPAssertionOfRecord(URI op, String opColumn, String table, List<DBField> PKFields) {
		Column rootColumn = Janus.cachedDBMetadata.getRootColumn(table, opColumn);
		
		String concatCallStatement = getConcatCallStatementToBuildFieldIndividual(rootColumn.getTableName(), rootColumn.getColumnName(), table, opColumn);
		
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
}