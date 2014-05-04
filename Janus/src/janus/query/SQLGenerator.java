package janus.query;

import janus.Janus;
import janus.database.DBColumn;
import janus.database.DBField;
import janus.mapping.DatatypeMap;
import janus.mapping.OntEntity;
import janus.mapping.OntEntityTypes;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class SQLGenerator {
	
	/* Type I-I: when a table has one column primary key. 
	 * SELECT aPK FROM table
	 */
	protected abstract String getQueryToGetIndividualsOfSinglePKColumnClass(String aPK, String table);
	
	/* Type I-II: when a table has multiple column primary key.
	 * SELECT pk1, pk2, ... , pkn FROM table
	 */
	protected abstract String getQueryToGetIndividualsOfTableClass(String table);
	
	/* Type I-III: to select a non-nullable key column.
	 * SELECT DISTINCT keyColumn FROM table
	 */
	protected abstract String getQueryToGetIndividualsOfNonNullableColumnClass(String keyColumn, String table);
	
	/* Type I-IV: to select a nullable key column.
	 * SELECT DISTINCT keyColumn FROM table WHERE keyColumn IS NOT NULL
	 */
	protected abstract String getQueryToGetIndividualsOfNullableColumnClass(String keyColumn, String table);
	
	protected abstract String getQueryToGetEmptyResultSet(int columnCount);
	
	protected abstract String getQueryToGetOPAssertionOfRecord(URI op, String opColumn, String table, List<DBField> PKFields);
	
	// the param record is a source individual.
	protected abstract String getQueryToGetDPAssertionOfRecord(URI dp, String dpColumn, String table, List<DBField> PKFields);
	
	// the param field is a source individual.
	protected abstract String getQueryToGetDPAssertionOfField(URI dp, String dpColumn, String table, String value);
	
	protected abstract String getQueryToGetOPAssertionOfField(DBField field);
	
	protected abstract String getQueryToGetDPAssertionsOfDPWithTableClassDomain(URI dp);
	
	protected abstract String getQueryToGetDPAssertionsOfDPWithColumnClassDomain(URI dp);
	
	public abstract String getQueryToGetOPAsserionsOfOP(URI op);
	
	protected abstract String getQueryToGetDPAssertionsOfKeyColumnLiteral(DBColumn dbColumn, String lexicalValueOfLiteral);
	
	protected abstract String getQueryToGetDPAssertionsOfNonKeyColumnLiteral(DBColumn dbColumn, String lexicalValueOfLiteral);
	
	protected abstract String getQueryToGetAllClsAssertionsOfTableClass(String table, URI cls);
	
	protected abstract String getQueryToGetAllClsAssertionsOfColumnClass(String table, String column, URI cls);
	
	protected abstract String getQueryToGetAllOPAssertions(String table, String column);
	
	protected abstract String getQueryToGetAllDPAssertionsOfRecords(String table, String column);
	
	protected abstract String getQueryToGetAllDPAssertionsOfFields(String table, String column);
	
	// for PropertyValue(?a, ?p, ?d), which only ?a is a variable and ?p is an object property.
	public abstract String getQueryToGetSourceIndividualsOfOPAssertion(URI op, URI aTargetIndividual);
	
	// for PropertyValue(?a, ?p, ?d), which only ?d is a variable and ?p is an object property.
	public abstract String getQueryToGetTargetIndividualsOfOPAssertion(URI op, URI aSourceIndividual);
	
	// for PropertyValue(?a, ?p, ?d), which only ?a is a variable and ?p is a data property.
	public abstract String getQueryToGetSourceIndividualsOfDPAssertion(URI dp, String aTargetLiteral);
	
	// for PropertyValue(?a, ?p, ?d), which only ?d is a variable and ?p is a data property.
	public abstract String getQueryToGetTargetLiteralsOfDPAssertion(URI dp, URI aSourceIndividual);
	
	public String getQueryToGetIndividualsOfClass(URI cls) {
		String query = null;
		
		if (Janus.mappingMetadata.getClassType(cls).equals(OntEntityTypes.TABLE_CLASS))
			query = getQueryToGetIndividualsOfTableClass(cls);
		else if (Janus.mappingMetadata.getClassType(cls).equals(OntEntityTypes.COLUMN_CLASS))
			query = getQueryToGetIndividualsOfColumnClass(cls);
		else if (Janus.mappingMetadata.getClassType(cls).equals(OntEntityTypes.OWL_THING_CLASS))
			query = getQueryToGetIndividualsOfOwlThing();
		
		return query;
	}
	
	private String getQueryToGetIndividualsOfOwlThing() {
		List<String> queries = new Vector<String>();
		
		Set<URI> clses = Janus.ontBridge.getSubClses(Janus.ontBridge.getOWLThingURI());
		for (URI aCls: clses) {
			if (Janus.mappingMetadata.getClassType(aCls).equals(OntEntityTypes.TABLE_CLASS))
				queries.add(getQueryToGetIndividualsOfTableClass(aCls));
			else if (Janus.mappingMetadata.getClassType(aCls).equals(OntEntityTypes.COLUMN_CLASS))
				queries.add(getQueryToGetIndividualsOfColumnClass(aCls));
		}
		
		return getUnionQuery(queries, 1);
	}
	
	private String getQueryToGetIndividualsOfTableClass(URI cls) {
		String table = Janus.mappingMetadata.getMappedTableNameToClass(cls);
		
		return getQueryToGetIndividualsOfTableClass(table);
	}
	
	private String getQueryToGetIndividualsOfColumnClass(URI cls) {
		String table = Janus.mappingMetadata.getMappedTableNameToClass(cls);
		String column = Janus.mappingMetadata.getMappedColumnNameToClass(cls);
		
		if (Janus.cachedDBMetadata.isPrimaryKey(table, column) 
				&& Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table))
			return getQueryToGetIndividualsOfSinglePKColumnClass(column, table);
		else if (Janus.cachedDBMetadata.isNotNull(table, column))
			return getQueryToGetIndividualsOfNonNullableColumnClass(column, table);
		else
			return getQueryToGetIndividualsOfNullableColumnClass(column, table);
	}
	
	private String getQueryToGetTypesOfFieldIndividual(URI individual) {
		DBField field = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(individual);
		
		String table = field.getTableName();
		String column = field.getColumnName();
		String value = field.getValue();
		
		List<String> queries = new Vector<String>();
		
		queries.add(getQueryToGetTypeOfField(Janus.ontBridge.getOWLThingURI(), table, column, value));
		
		URI mappedClass = Janus.mappingMetadata.getMappedClass(table, column);
		
		Set<URI> familyClsses = Janus.ontBridge.getAllFamilyClasses(mappedClass);
		
		for (URI cls: familyClsses) {
			String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(cls);
			String mappedColumn = Janus.mappingMetadata.getMappedColumnNameToClass(cls);
			
			queries.add(getQueryToGetTypeOfField(cls, mappedTable, mappedColumn, value));
		}
		
		return getUnionQuery(queries, 1);
	}
	
	private String getQueryToGetTypesOfRecordIndividual(URI individual) {
		List<DBField> fields = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(individual);
		
		String table = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(individual);
		
		List<String> queries = new Vector<String>();
		
		queries.add(getQueryToGetTypeOfRecord(Janus.ontBridge.getOWLThingURI(), table, fields));
		
		URI mappedClass = Janus.mappingMetadata.getMappedClass(table);
		
		Set<URI> familyClsses = Janus.ontBridge.getAllFamilyClasses(mappedClass);
		
		for (URI cls: familyClsses) {
			String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(cls);
			
			List<DBField> familyFields = new Vector<DBField>();
			
			for (DBField field: fields) {
				String matchedPKColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(field.getTableName(), field.getColumnName(), mappedTable);
				familyFields.add(new DBField(mappedTable, matchedPKColumn, field.getValue()));
			}
			
			queries.add(getQueryToGetTypeOfRecord(cls, mappedTable, familyFields));
		}
		
		return getUnionQuery(queries, 1);
	}
	
	private String getQueryToGetTypeOfRecord(URI cls, String table, List<DBField> fields) {
		
		StringBuffer query = new StringBuffer("SELECT '"  + OntEntity.getCURIE(cls) +"'"
												+ " FROM " + table
												+ " WHERE ");
		
		for (int i = 0; i < fields.size() - 1; i++) {
			DBField field = fields.get(i);
			query.append(field.getTableName() + "." + field.getColumnName() + " = " + "'" + field.getValue() + "'");
			query.append(" AND ");
		}
		
		DBField lastField = fields.get(fields.size()-1);
		query.append(lastField.getTableName() + "." + lastField.getColumnName() + " = " + "'" + lastField.getValue() + "'");
		
		return query.toString();
	}
	
	private String getQueryToGetTypeOfField(URI cls, String table, String column, String value) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		if (!(Janus.cachedDBMetadata.isPrimaryKey(table, column)
				&& Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table)))
			query.append("DISTINCT ");
		
		query.append("'" + OntEntity.getCURIE(cls) +"'"
				+ " FROM " + table
				+ " WHERE " + table + "." + column + " = " + "'" + value + "'");
		
		return query.toString();
	}
	
	private String getUnionQuery(List<String> queries, int columnCount) {
		if (queries.isEmpty())
			return getQueryToGetEmptyResultSet(columnCount);
		
		if (queries.size() == 1)
			return queries.get(0);
		
		StringBuffer query = new StringBuffer();
		
		for (int i = 0; i < queries.size() - 1; i++)
			query.append("(" + queries.get(i) +") UNION ");
		
		query.append("(" + queries.get(queries.size() - 1) + ")");
		
		return query.toString();
	}
	
	public String getQueryToGetTypesOfIndividual(URI individual) {
		String query = null;
		
		if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.RECORD_INDIVIDUAL))
			query = getQueryToGetTypesOfRecordIndividual(individual);
		else if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.FIELD_INDIVIDUAL))
			query = getQueryToGetTypesOfFieldIndividual(individual);
		
		return query;
	}
	
	//object property assertions 
	public String getQueryToGetOPAssertionsOfIndividual(URI individual) {
		String query = null;
		
		if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.RECORD_INDIVIDUAL))
			query = getQueryToGetOPAssertionsOfRecordSourceIndividual(individual);
		else if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.FIELD_INDIVIDUAL))
			query = getQueryToGetOPAssertionsOfFieldTargetIndividual(individual);
		
		return query;
	}
	
	// the parameter individual is object.
	private String getQueryToGetOPAssertionsOfFieldTargetIndividual(URI individual) {
		DBField field = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(individual);
		
		String table = field.getTableName();
		String column = field.getColumnName();
		String value = field.getValue();
		
		List<String> queries = new Vector<String>();

		URI mappedClass = Janus.mappingMetadata.getMappedClass(table, column);

		Set<URI> familyClsses = Janus.ontBridge.getAllFamilyClasses(mappedClass);
		
		for (URI cls: familyClsses) {
			String familyTable = Janus.mappingMetadata.getMappedTableNameToClass(cls);
			String familyColumn = Janus.mappingMetadata.getMappedColumnNameToClass(cls);
						
			queries.add(getQueryToGetOPAssertionOfField(new DBField(familyTable, familyColumn, value)));
		}
		
		return getUnionQuery(queries, 2);
	}
	
	// the parameter individual is subject.
	private String getQueryToGetOPAssertionsOfRecordSourceIndividual(URI individual) {
		List<DBField> fields = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(individual);
		
		String table = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(individual);
		
		List<String> queries = new Vector<String>();
		
		URI mappedClass = Janus.mappingMetadata.getMappedClass(table);
		
		Set<URI> familyClsses = Janus.ontBridge.getAllFamilyClasses(mappedClass);
		
		for (URI cls: familyClsses) {
			String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(cls);
			
			List<DBField> familyFields = new Vector<DBField>();
			
			for (DBField field: fields) {
				String matchedPKColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(field.getTableName(), field.getColumnName(), mappedTable);
				familyFields.add(new DBField(mappedTable, matchedPKColumn, field.getValue()));
			}
			
			Set<String> keyColumns = Janus.cachedDBMetadata.getKeyColumns(mappedTable);
			
			for (String keyColumn: keyColumns) {
				URI mappedOP = Janus.mappingMetadata.getMappedObjectProperty(mappedTable, keyColumn);
				
				queries.add(getQueryToGetOPAssertionOfRecord(mappedOP, keyColumn, mappedTable, familyFields));
			}
		}
		
		return getUnionQuery(queries, 2);
	}
	
	// the parameter individual is subject.
	private String getQueryToGetDPAssertionsOfRecordSourceIndividual(URI individual) {
		List<DBField> fields = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(individual);

		String table = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(individual);

		List<String> queries = new Vector<String>();

		URI mappedClass = Janus.mappingMetadata.getMappedClass(table);

		Set<URI> familyClsses = Janus.ontBridge.getAllFamilyClasses(mappedClass);

		for (URI cls: familyClsses) {
			String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(cls);

			List<DBField> familyFields = new Vector<DBField>();

			for (DBField field: fields) {
				String matchedPKColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(field.getTableName(), field.getColumnName(), mappedTable);
				familyFields.add(new DBField(mappedTable, matchedPKColumn, field.getValue()));
			}

			Set<String> nonKeyColumns = Janus.cachedDBMetadata.getNonKeyColumns(mappedTable);

			for (String nonKeyColumn: nonKeyColumns) {
				URI mappedDP = Janus.mappingMetadata.getMappedDataProperty(mappedTable, nonKeyColumn);

				queries.add(getQueryToGetDPAssertionOfRecord(mappedDP, nonKeyColumn, mappedTable, familyFields));
			}
		}
		
		return getUnionQuery(queries, 2);
	}
	
	// the parameter individual is subject.
	private String getQueryToGetDPAssertionsOfFieldSourceIndividual(URI individual) {
		DBField field = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(individual);
		
		String table = field.getTableName();
		String column = field.getColumnName();
		String value = field.getValue();

		List<String> queries = new Vector<String>();

		URI mappedClass = Janus.mappingMetadata.getMappedClass(table, column);

		Set<URI> familyClsses = Janus.ontBridge.getAllFamilyClasses(mappedClass);

		for (URI cls: familyClsses) {
			String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(cls);
			String mappedColumn = Janus.mappingMetadata.getMappedColumnNameToClass(cls);
			
			URI mappedDP = Janus.mappingMetadata.getMappedDataProperty(mappedTable, mappedColumn);

			queries.add(getQueryToGetDPAssertionOfField(mappedDP, mappedColumn, mappedTable, value));
		}

		return getUnionQuery(queries, 2);
	}
	
	//data property assertions 
	public String getQueryToGetDPAssertionsOfSourceIndividual(URI individual) {
		String query = null;

		if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.RECORD_INDIVIDUAL))
			query = getQueryToGetDPAssertionsOfRecordSourceIndividual(individual);
		else if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.FIELD_INDIVIDUAL))
			query = getQueryToGetDPAssertionsOfFieldSourceIndividual(individual);

		return query;
	}
	
	//data property assertions 
	public String getQueryToGetDPAsserionsOfDP(URI dp) {
		String query = null;
		
		URI domainCls = Janus.mappingMetadata.getDomainClassOfProperty(dp);
		
		OntEntityTypes type = Janus.mappingMetadata.getClassType(domainCls);
		
		if (type.equals(OntEntityTypes.TABLE_CLASS))
			query = getQueryToGetDPAssertionsOfDPWithTableClassDomain(dp);
		else if (type.equals(OntEntityTypes.COLUMN_CLASS))
			query = getQueryToGetDPAssertionsOfDPWithColumnClassDomain(dp);
		
		return query;
	}
	
	//data property assertions
	public String getQueryToGetDPAssertionsOfTargetLiteral(String literal) {
		String lexicalValue = Janus.mappingMetadata.getLexicalValueOfTypedLiteral(literal);
		String datatypeOfTypedLiteral = Janus.mappingMetadata.getDatatypeOfTypedLiteral(literal);
		
		Set<DBColumn> dbColumns = Janus.mappingMetadata.getMappedDBColumnsToDatatypeOfTypedLiteral(datatypeOfTypedLiteral);
		
		List<String> queries = new Vector<String>();
		
		for(DBColumn dbColumn: dbColumns) {
			
			if (Janus.cachedDBMetadata.isKey(dbColumn.getTableName(), dbColumn.getColumnName()))
				queries.add(getQueryToGetDPAssertionsOfKeyColumnLiteral(dbColumn, lexicalValue));
			else
				queries.add(getQueryToGetDPAssertionsOfNonKeyColumnLiteral(dbColumn, lexicalValue));
		}
		
		return getUnionQuery(queries, 2);
	}
	
	// for Type(?a, ?C), which both ?a and ?C are empty.
	public String getQueryToGetAllClsAssertions() {
		List<String> queries = new Vector<String>();
		
		URI owlThing = Janus.ontBridge.getOWLThingURI();
		
		Set<String> tables = Janus.cachedDBMetadata.getTableNames();
		
		for (String table: tables) {
			
			if (Janus.cachedDBMetadata.isRootTable(table)) {
				queries.add(getQueryToGetAllClsAssertionsOfTableClass(table, owlThing));
			}
			
			URI mappedClass = Janus.mappingMetadata.getMappedClass(table);
			queries.add(getQueryToGetAllClsAssertionsOfTableClass(table, mappedClass));
			
			Set<String> keys = Janus.cachedDBMetadata.getKeyColumns(table);
			
			for (String key: keys) {
				if (Janus.cachedDBMetadata.isRootColumn(table, key))
					queries.add(getQueryToGetAllClsAssertionsOfColumnClass(table, key, owlThing));
				
				mappedClass = Janus.mappingMetadata.getMappedClass(table, key);
				queries.add(getQueryToGetAllClsAssertionsOfColumnClass(table, key, mappedClass));
			}
		}
		
		return getUnionQuery(queries, 2);
	}
	
	// for PropertyValue(?a, ?p, ?d), which ?a, ?p and ?d are all empty.
	public String getQueryToGetAllPropertyAssertions() {
		List<String> queries = new Vector<String>();
		
		Set<String> tables = Janus.cachedDBMetadata.getTableNames();
		
		for (String table: tables) {
			Set<String> columns = Janus.cachedDBMetadata.getColumns(table);
			
			for (String column: columns)
				if (Janus.cachedDBMetadata.isKey(table, column)) {
					queries.add(getQueryToGetAllOPAssertions(table, column));
					queries.add(getQueryToGetAllDPAssertionsOfFields(table, column));
				} else
					queries.add(getQueryToGetAllDPAssertionsOfRecords(table, column));
		}
		
		return getUnionQuery(queries, 3);
	}
	
	private Set<DBColumn> getColumsIncludedInTables(Set<String> tables, Set<DBColumn> columns) {
		Set<DBColumn> members = new ConcurrentSkipListSet<DBColumn>();
		
		for (DBColumn column: columns)
			if (tables.contains(column.getTableName()))
				members.add(column);
		
		return members;
	}
	
	// for PropertyValue(?a, ?p, ?d), which only ?p is a variable and both ?a and ?d are individuals.
	public String getQueryToGetObjectPropertiesOfOPAssertion(URI aSourceIndividual, URI aTargetIndividual) {
		List<String> queries = new Vector<String>();
		
		String srcTable = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(aSourceIndividual);
		
		Set<String> familyTablesOfSrc = Janus.cachedDBMetadata.getFamilyTables(srcTable);
		
		DBField targetField = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(aTargetIndividual);
		
		Set<DBColumn> familyColumnsOfTarget = Janus.cachedDBMetadata.getFamilyColumns(targetField.getTableName(), targetField.getColumnName());
		
		Set<DBColumn> columnsInFamilyTablesOfSrc = getColumsIncludedInTables(familyTablesOfSrc, familyColumnsOfTarget);
		
		List<DBField> srcFields = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(aSourceIndividual);
		
		for (DBColumn aColumn: columnsInFamilyTablesOfSrc) {
			String table = aColumn.getTableName();
			String column = aColumn.getColumnName();
			String value = targetField.getValue();
			
			URI op = Janus.mappingMetadata.getMappedObjectProperty(table, column);
			
			StringBuffer query = new StringBuffer("SELECT " + "'" + OntEntity.getCURIE(op) + "'" +
												 " FROM " + table + 
												 " WHERE ");
			
			boolean isTheSameColumn = false;
			boolean isTheSameValue = false;
			for (DBField srcField: srcFields) {
				String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(srcTable, srcField.getColumnName(), table);
				
				query.append(table + "." + matchedColumn + " = " + "'" + srcField.getValue() + "'" + " AND ");
				
				if (matchedColumn.equals(column)) {
					isTheSameColumn = true;
					
					if (value.equals(srcField.getValue()))
						isTheSameValue = true;
				}
			}
			
			if (isTheSameColumn) {
				if (isTheSameValue)
					query.delete(query.lastIndexOf(" AND "), query.length());
				else
					continue;
			} else
				query.append(table + "." + column + " = " + "'" + value + "'");
			
			queries.add(query.toString());
		}
		
		return getUnionQuery(queries, 1);
	}
	
	// for PropertyValue(?a, ?p, ?d), which only ?p is a variable, ?a is an individual and ?d is a literal.
	public String getQueryToGetDataPropertiesOfDPAssertion(URI aSourceIndividual, String aTargetLiteral) {
		OntEntityTypes individualType = Janus.mappingMetadata.getIndividualType(aSourceIndividual);
		
		if (individualType.equals(OntEntityTypes.RECORD_INDIVIDUAL))
			return getQueryToGetDataPropertiesOfDPAssertionWithRecordSrcIndividual(aSourceIndividual, aTargetLiteral);
		else if (individualType.equals(OntEntityTypes.FIELD_INDIVIDUAL)) 
			return getQueryToGetDataPropertiesOfDPAssertionWithFieldSrcIndividual(aSourceIndividual, aTargetLiteral);
		else
			return getQueryToGetEmptyResultSet(1);
	}
	
	private String getQueryToGetDataPropertiesOfDPAssertionWithFieldSrcIndividual(URI aSourceIndividual, String aTargetLiteral) {
		List<String> queries = new Vector<String>();
		
		String datatypeOfTargetLiteral = Janus.mappingMetadata.getDatatypeOfTypedLiteral(aTargetLiteral);
		
		Set<Integer> mappedSQLTypes = DatatypeMap.getMappedSQLTypes(datatypeOfTargetLiteral);
		
		String valueOfTargetLiteral = Janus.mappingMetadata.getLexicalValueOfTypedLiteral(aTargetLiteral);
		
		DBField srcField = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(aSourceIndividual);
		String srcTable = srcField.getTableName();
		String srcColumn = srcField.getColumnName();
		String srcValue = srcField.getValue();
		
		if (!mappedSQLTypes.contains(Janus.cachedDBMetadata.getDataType(srcTable, srcColumn)) 
				|| !srcValue.equals(valueOfTargetLiteral))
			return getUnionQuery(queries, 1);
		
		Set<DBColumn> familyColumnsOfSrc = Janus.cachedDBMetadata.getFamilyColumns(srcField.getTableName(), srcField.getColumnName());
		
		for (DBColumn aColumn: familyColumnsOfSrc) {
			String table = aColumn.getTableName();
			String column = aColumn.getColumnName();
			
			URI dp = Janus.mappingMetadata.getMappedDataProperty(table, column);
			
			StringBuffer query = new StringBuffer("SELECT ");	
			
			if (!(Janus.cachedDBMetadata.isSingleColumnUniqueKey(table, column)
					|| ((Janus.cachedDBMetadata.isPrimaryKey(table, column) 
							&& Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table)))))
				query.append("DISTINCT ");
				
			query.append("'" + OntEntity.getCURIE(dp) + "'" + 
						" FROM " + table + 
						" WHERE " + table + "." + column + " = " + "'" + valueOfTargetLiteral + "'");
			
			queries.add(query.toString());
		}
		
		return getUnionQuery(queries, 1);
	}
	
	private String getQueryToGetDataPropertiesOfDPAssertionWithRecordSrcIndividual(URI aSourceIndividual, String aTargetLiteral) {
		List<String> queries = new Vector<String>();
		
		String srcTable = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(aSourceIndividual);
		
		Set<String> familyTablesOfSrc = Janus.cachedDBMetadata.getFamilyTables(srcTable);
		
		List<DBField> srcFields = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(aSourceIndividual);
		
		String datatypeOfTargetLiteral = Janus.mappingMetadata.getDatatypeOfTypedLiteral(aTargetLiteral);
		
		Set<Integer> mappedSQLTypes = DatatypeMap.getMappedSQLTypes(datatypeOfTargetLiteral);
		
		String valueOfTargetLiteral = Janus.mappingMetadata.getLexicalValueOfTypedLiteral(aTargetLiteral);
		
		for (String table: familyTablesOfSrc) {
			Set<String> columns = Janus.cachedDBMetadata.getNonKeyColumns(table);
			
			for (String column: columns) {
				if (mappedSQLTypes.contains(Janus.cachedDBMetadata.getDataType(table, column))) {
					URI dp = Janus.mappingMetadata.getMappedDataProperty(table, column);
					
					StringBuffer query = new StringBuffer("SELECT " + "'" + OntEntity.getCURIE(dp) + "'" +
														 " FROM " + table + 
														 " WHERE ");
					
					boolean isTheSameColumn = false;
					boolean isTheSameValue = false;
					
					for (DBField srcField: srcFields) {
						String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(srcTable, srcField.getColumnName(), table);

						query.append(table + "." + matchedColumn + " = " + "'" + srcField.getValue() + "'" + " AND ");

						if (matchedColumn.equals(column)) {
							isTheSameColumn = true;

							if (valueOfTargetLiteral.equals(srcField.getValue()))
								isTheSameValue = true;
						}
					}
					
					if (isTheSameColumn) {
						if (isTheSameValue)
							query.delete(query.lastIndexOf(" AND "), query.length());
						else
							continue;
					} else
						query.append(table + "." + column + " = " + "'" + valueOfTargetLiteral + "'");

					queries.add(query.toString());
				}
			}	
		}
		
		return getUnionQuery(queries, 1);
	}
}