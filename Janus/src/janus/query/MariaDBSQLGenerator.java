package janus.query;

import janus.Janus;
import janus.database.DBColumn;
import janus.database.DBField;
import janus.mapping.DatatypeMap;
import janus.mapping.OntEntity;
import janus.mapping.OntEntityTypes;
import janus.mapping.OntMapper;

import java.net.URI;
import java.util.List;
import java.util.Set;

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
	
	protected String getQueryToGetEmptyResultSet(int columnCount) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		for (int i = 0; i < columnCount-1; i++)
			query.append("'', ");
		
		query.append("''");
		
		query.append(" FROM DUAL WHERE FALSE");
		
		return query.toString();
	} 
	
	private String getConcatCallStatementToBuildFieldIndividual(String table, String keyColumn) {
		DBColumn rootColumn = Janus.cachedDBMetadata.getRootColumn(table, keyColumn);
		
		return "concat('" + OntMapper.COLON + OntMapper.TABLE_NAME + "=" + rootColumn.getTableName() + "&" + OntMapper.COLUMN_NAME + "="+ rootColumn.getColumnName() + "&" + OntMapper.VALUE + "=', " + table + "." + keyColumn + ")";
	}
	
	// generates an individual with ordered column names.
	private String getConcatCallStatementToBuildRecordIndividual(String table) {
		String rootTable = Janus.cachedDBMetadata.getRootTable(table);
		
		StringBuffer concat = new StringBuffer("concat('" + OntMapper.COLON + OntMapper.TABLE_NAME + "=" + rootTable + "'");
		
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
		
		StringBuffer query = new StringBuffer("SELECT " + "'" + OntEntity.getCURIE(op) + "'" + ", " + concatCallStatement
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
		StringBuffer query = new StringBuffer("SELECT '"  + OntEntity.getCURIE(dp) +"', " + getConcatCallStatementToBuildLiteral(table, dpColumn)
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
		
		query.append("'"  + OntEntity.getCURIE(dp) +"', " + getConcatCallStatementToBuildLiteral(table, dpColumn) 
				+ " FROM " + table
				+ " WHERE " + table + "." + dpColumn + " = '" + value + "'");
		
		return query.toString();
	}
	
	protected String getQueryToGetOPAssertionOfField(DBField field) {
		String table = field.getTableName();
		String column = field.getColumnName();
		String value = field.getValue();
		
		URI op = Janus.mappingMetadata.getMappedObjectProperty(table, column);

		return "SELECT " + "'" + OntEntity.getCURIE(op) + "'" + ", " + getConcatCallStatementToBuildRecordIndividual(table)
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
		
		query.append("'" + OntEntity.getCURIE(dp) + "'" + ", " + getConcatCallStatementToBuildFieldIndividual(table, column)
				 + " FROM " + table
				 + " WHERE " + dbColumn.toString() + " = " + "'" + lexicalValueOfLiteral + "'");
				
		return query.toString();
	}
	
	protected String getQueryToGetDPAssertionsOfNonKeyColumnLiteral(DBColumn dbColumn, String lexicalValueOfLiteral) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		String table = dbColumn.getTableName();
		String column = dbColumn.getColumnName();
		
		URI dp = Janus.mappingMetadata.getMappedDataProperty(table, column);
		
		query.append("'" + OntEntity.getCURIE(dp) + "'" + ", " + getConcatCallStatementToBuildRecordIndividual(table) 
				 + " FROM " + table
				 + " WHERE " + dbColumn.toString() + " = " + "'" + lexicalValueOfLiteral + "'");
		
		return query.toString();
	}
	
	protected String getQueryToGetAllClsAssertionsOfTableClass(String table, URI cls) {
		return "SELECT " + "'" + OntEntity.getCURIE(cls) + "'" + ", " + getConcatCallStatementToBuildRecordIndividual(table)
				        + " FROM " + table;
	}
	
	protected String getQueryToGetAllClsAssertionsOfColumnClass(String table, String column, URI cls) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		if (!(Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table)
				&& Janus.cachedDBMetadata.isPrimaryKey(table, column)))
			query.append("DISTINCT ");
		
		query.append("'" + OntEntity.getCURIE(cls) + "'" + ", " + getConcatCallStatementToBuildFieldIndividual(table, column)
		        + " FROM " + table);
		
		if (!Janus.cachedDBMetadata.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	protected String getQueryToGetAllOPAssertions(String table, String column) {
		URI op = Janus.mappingMetadata.getMappedObjectProperty(table, column);
		
		StringBuffer query = new StringBuffer("SELECT " + "'" + OntEntity.getCURIE(op) + "'" + ", " + getConcatCallStatementToBuildRecordIndividual(table) + ", " + getConcatCallStatementToBuildFieldIndividual(table, column)
												+ " FROM " + table);
		
		if (!Janus.cachedDBMetadata.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	protected String getQueryToGetAllDPAssertionsOfRecords(String table, String column) {
		URI dp = Janus.mappingMetadata.getMappedDataProperty(table, column);
		
		StringBuffer query = new StringBuffer("SELECT " + "'" + OntEntity.getCURIE(dp) + "'" + ", " + getConcatCallStatementToBuildRecordIndividual(table) + ", " + getConcatCallStatementToBuildLiteral(table, column)
												+ " FROM " + table);
		
		if (!Janus.cachedDBMetadata.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	protected String getQueryToGetAllDPAssertionsOfFields(String table, String column) {
		URI dp = Janus.mappingMetadata.getMappedDataProperty(table, column);
		
		StringBuffer query = new StringBuffer("SELECT ");
		
		if (!(Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table)
				&& Janus.cachedDBMetadata.isPrimaryKey(table, column)))
			query.append("DISTINCT ");
		
		query.append("'" + OntEntity.getCURIE(dp) + "'" + ", " + getConcatCallStatementToBuildFieldIndividual(table, column) + ", " + getConcatCallStatementToBuildLiteral(table, column)
		        + " FROM " + table);
		
		if (!Janus.cachedDBMetadata.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	public String getQueryToGetSourceIndividualsOfOPAssertion(URI op, URI aTargetIndividual) {
		DBColumn opColumn = Janus.mappingMetadata.getMappedColumnToProperty(op);
		
		String table = opColumn.getTableName();
		
		return "SELECT " + getConcatCallStatementToBuildRecordIndividual(table)
				+ " FROM " + table
				+ " WHERE " + opColumn.toString() + " = " + "'" + Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(aTargetIndividual).getValue() + "'";
	}
	
	public String getQueryToGetTargetIndividualsOfOPAssertion(URI op, URI aSourceIndividual) {
		DBColumn opColumn = Janus.mappingMetadata.getMappedColumnToProperty(op);
		
		String table = opColumn.getTableName();
		String column = opColumn.getColumnName();
		
		StringBuffer query = new StringBuffer("SELECT " + getConcatCallStatementToBuildFieldIndividual(table, column)
										   + " FROM " + table
										   + " WHERE ");
		
		List<DBField> srcFields = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(aSourceIndividual);
		
		for (int i = 0; i < srcFields.size()-1; i++) {
			String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(srcFields.get(i).getTableName(), srcFields.get(i).getColumnName(), table);
			
			query.append(table + "." + matchedColumn + " = " + "'" + srcFields.get(i).getValue() + "'" + " AND ");
		}
		
		DBField lastSrcField = srcFields.get(srcFields.size()-1);
		String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(lastSrcField.getTableName(), lastSrcField.getColumnName(), table);
		query.append(table + "." + matchedColumn + " = " + "'" + lastSrcField.getValue() + "'");
		
		return query.toString();
	}
	
	public String getQueryToGetTargetLiteralsOfDPAssertion(URI dp, URI aSourceIndividual) {
		DBColumn dpColumn = Janus.mappingMetadata.getMappedColumnToProperty(dp);
		String table = dpColumn.getTableName();
		String column = dpColumn.getColumnName();
		
		OntEntityTypes srcIndividualType = Janus.mappingMetadata.getIndividualType(aSourceIndividual);
		
		if (srcIndividualType.equals(OntEntityTypes.RECORD_INDIVIDUAL)) {
			
			String srcTable = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(aSourceIndividual);
			
			Set<String> familyTablesOfSrc = Janus.cachedDBMetadata.getFamilyTables(srcTable);
			
			if (!familyTablesOfSrc.contains(table))
				return getQueryToGetEmptyResultSet(1);
			
			StringBuffer query = new StringBuffer("SELECT " + getConcatCallStatementToBuildLiteral(table, column) + 
												 " FROM " + table +
												 " WHERE ");
			
			List<DBField> srcFields = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(aSourceIndividual);
			
			for (DBField field: srcFields) {
				String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(srcTable, field.getColumnName(), table);
				
				query.append(table + "." + matchedColumn + " = " + "'" + field.getValue() + "'" + " AND ");
			}
			
			query.delete(query.lastIndexOf(" AND "), query.length());
			
			return query.toString();
			
		} else if (srcIndividualType.equals(OntEntityTypes.FIELD_INDIVIDUAL)) {
			
			DBField srcField = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(aSourceIndividual);
			String srcTable = srcField.getTableName();
			String srcColumn = srcField.getColumnName();
			
			Set<DBColumn> familyColumnsOfSrc = Janus.cachedDBMetadata.getFamilyColumns(srcTable, srcColumn);
			
			if (!familyColumnsOfSrc.contains(dpColumn))
				return getQueryToGetEmptyResultSet(1);
			
			StringBuffer query = new StringBuffer("SELECT ");
			
			if (!(Janus.cachedDBMetadata.isSingleColumnUniqueKey(table, column)
					|| ((Janus.cachedDBMetadata.isPrimaryKey(table, column) 
							&& Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table)))))
				query.append("DISTINCT ");
			
			query.append(getConcatCallStatementToBuildLiteral(table, column) + 
					" FROM " + table +
					" WHERE ");
			
			String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(srcTable, srcColumn, table);
			
			query.append(table + "." + matchedColumn + " = " + "'" + srcField.getValue() + "'");
			
			return query.toString();
			
		} else
			return getQueryToGetEmptyResultSet(1);
	}
	
	public String getQueryToGetSourceIndividualsOfDPAssertion(URI dp, String aTargetLiteral) {
		DBColumn dpColumn = Janus.mappingMetadata.getMappedColumnToProperty(dp);
		String table = dpColumn.getTableName();
		String column = dpColumn.getColumnName();
		
		String datatypeOfTargetLiteral = Janus.mappingMetadata.getDatatypeOfTypedLiteral(aTargetLiteral);
		
		Set<Integer> mappedSQLTypes = DatatypeMap.getMappedSQLTypes(datatypeOfTargetLiteral);
		
		String valueOfTargetLiteral = Janus.mappingMetadata.getLexicalValueOfTypedLiteral(aTargetLiteral);
		
		if (!mappedSQLTypes.contains(Janus.cachedDBMetadata.getDataType(table, column)))
			return getQueryToGetEmptyResultSet(1);
		
		StringBuffer query = new StringBuffer("SELECT ");
		
		if (Janus.cachedDBMetadata.isKey(table, column)) {
			
			if (!(Janus.cachedDBMetadata.isSingleColumnUniqueKey(table, column)
					|| ((Janus.cachedDBMetadata.isPrimaryKey(table, column) 
							&& Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table)))))
				query.append("DISTINCT ");
			
			query.append(getConcatCallStatementToBuildFieldIndividual(table, column));
			
		} else {
			query.append(getConcatCallStatementToBuildRecordIndividual(table));
		}
		
		query.append(" FROM " + table + 
					 " WHERE " + dpColumn.toString() + " = " + "'" + valueOfTargetLiteral + "'");
		
		return query.toString();
	}
}