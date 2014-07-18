package janus.query.rewriter;

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

import javax.swing.table.TableModel;

public abstract class SQLGenerator {
	
	/* Type I-I: when a table has one column primary key. 
	 * SELECT aPK FROM table
	 */
	protected abstract String getQueryToGetIndividualsOfSinglePKColumnClass(String aPK, String table, String columnName);
	
	/* Type I-II: when a table has multiple column primary key.
	 * SELECT pk1, pk2, ... , pkn FROM table
	 */
	protected abstract String getQueryToGetIndividualsOfTableClass(String table, String columnName);
	
	/* Type I-III: to select a non-nullable key column.
	 * SELECT DISTINCT keyColumn FROM table
	 */
	protected abstract String getQueryToGetIndividualsOfNonNullableColumnClass(String keyColumn, String table, String columnName);
	
	/* Type I-IV: to select a nullable key column.
	 * SELECT DISTINCT keyColumn FROM table WHERE keyColumn IS NOT NULL
	 */
	protected abstract String getQueryToGetIndividualsOfNullableColumnClass(String keyColumn, String table, String columnName);
	
	public abstract String getQueryToGetEmptyResultSet(int columnCount);
	
	public abstract String getQueryToGetEmptyResultSet(List<String> columnNames);
	
	public abstract String getQueryToGetEmptyResultSet(String columnName);
	
	public abstract String getQueryToGetOneBooleanValueResultSet(boolean value);
	
	protected abstract String getQueryToGetOPAssertionOfRecord(URI op, String opColumn, String table, List<DBField> PKFields, String pColumnName, String tColumnName);
	
	// the param record is a source individual.
	protected abstract String getQueryToGetDPAssertionOfRecord(URI dp, String dpColumn, String table, List<DBField> PKFields, String pColumnName, String tColumnName);
	
	// the param field is a source individual.
	protected abstract String getQueryToGetDPAssertionOfField(URI dp, String dpColumn, String table, String value, String pColumnName, String tColumnName);
	
	protected abstract String getQueryToGetOPAssertionOfField(DBField field, String pColumnName, String sColumnName);
	
	protected abstract String getQueryToGetDPAssertionsOfDPWithTableClassDomain(URI dp, String sColumnName, String tColumnName);
	
	protected abstract String getQueryToGetDPAssertionsOfDPWithColumnClassDomain(URI dp, String sColumnName, String tColumnName);
	
	public abstract String getQueryToGetOPAsserionsOfOP(URI op, String sColumnName, String tColumnName);
	
	protected abstract String getQueryToGetDPAssertionsOfKeyColumnLiteral(DBColumn dbColumn, String lexicalValueOfLiteral, String pColumnName, String sColumnName);
	
	protected abstract String getQueryToGetDPAssertionsOfNonKeyColumnLiteral(DBColumn dbColumn, String lexicalValueOfLiteral, String pColumnName, String sColumnName);
	
	protected abstract String getQueryToGetAllClsAssertionsOfTableClass(String table, URI cls, String columnName1, String columnName2);
	
	protected abstract String getQueryToGetAllClsAssertionsOfColumnClass(String table, String column, URI cls, String columnName1, String columnName2);
	
	protected abstract String getQueryToGetAllOPAssertions(String table, String column, String pColumnName, String sColumnName, String tColumnName);
	
	protected abstract String getQueryToGetAllDPAssertionsOfRecords(String table, String column, String pColumnName, String sColumnName, String tColumnName);
	
	protected abstract String getQueryToGetAllDPAssertionsOfFields(String table, String column, String pColumnName, String sColumnName, String tColumnName);
	
	// for PropertyValue(?a, ?p, ?d), which only ?a is a variable and ?p is an object property.
	public abstract String getQueryToGetSourceIndividualsOfOPAssertion(URI op, URI aTargetIndividual, String columnName);
	
	// for PropertyValue(?a, ?p, ?d), which only ?d is a variable and ?p is an object property.
	public abstract String getQueryToGetTargetIndividualsOfOPAssertion(URI op, URI aSourceIndividual, String columnName);
	
	// for PropertyValue(?a, ?p, ?d), which only ?a is a variable and ?p is a data property.
	public abstract String getQueryToGetSourceIndividualsOfDPAssertion(URI dp, String aTargetLiteral, String columnName);
	
	// for PropertyValue(?a, ?p, ?d), which only ?d is a variable and ?p is a data property.
	public abstract String getQueryToGetTargetLiteralsOfDPAssertion(URI dp, URI aSourceIndividual, String columnName);
	
	// for SameAs(?a1, ?a2), which both ?a1 and ?a2 are empty.
	public abstract String getQueryToGetAllPairsOfTheSameIndividuals(String columnName1, String columnName2);
	
	// for DifferentFrom(?a1, ?a2), which both ?a1 and ?a2 are empty.
	public abstract String getQueryToGetDiffIndividualsFrom(URI individual, String columnName);
	
	protected abstract String getQueryToGetAllIndividuals(String columnName);
	
	public abstract String getQueryToGetAllPairsOfDiffIndividuals(String columnName1, String columnName2);
	
	public abstract String getQueryCorrespondingToURIResultSet(TableModel aURIResultSet);
	
	public String getQueryToGetIndividualsOfClass(URI cls, String columnName) {
		String query = null;
		
		if (Janus.mappingMetadata.getClassType(cls).equals(OntEntityTypes.TABLE_CLASS))
			query = getQueryToGetIndividualsOfTableClass(cls, columnName);
		else if (Janus.mappingMetadata.getClassType(cls).equals(OntEntityTypes.COLUMN_CLASS))
			query = getQueryToGetIndividualsOfColumnClass(cls, columnName);
		else if (Janus.mappingMetadata.getClassType(cls).equals(OntEntityTypes.OWL_THING_CLASS))
			query = getQueryToGetAllIndividuals(columnName);
		
		return query;
	}
	
	/*private String getQueryToGetIndividualsOfOwlThing() {
		List<String> queries = new Vector<String>();
		
		Set<URI> clses = Janus.ontBridge.getSubClses(Janus.ontBridge.getOWLThingURI());
		for (URI aCls: clses) {
			if (Janus.mappingMetadata.getClassType(aCls).equals(OntEntityTypes.TABLE_CLASS))
				queries.add(getQueryToGetIndividualsOfTableClass(aCls));
			else if (Janus.mappingMetadata.getClassType(aCls).equals(OntEntityTypes.COLUMN_CLASS))
				queries.add(getQueryToGetIndividualsOfColumnClass(aCls));
		}
		
		return getUnionQuery(queries, 1);
	}*/
	
	private String getQueryToGetIndividualsOfTableClass(URI cls, String columnName) {
		String table = Janus.mappingMetadata.getMappedTableNameToClass(cls);
		
		return getQueryToGetIndividualsOfTableClass(table, columnName);
	}
	
	private String getQueryToGetIndividualsOfColumnClass(URI cls, String columnName) {
		String table = Janus.mappingMetadata.getMappedTableNameToClass(cls);
		String column = Janus.mappingMetadata.getMappedColumnNameToClass(cls);
		
		if (Janus.cachedDBMetadata.isPrimaryKey(table, column) 
				&& Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table))
			return getQueryToGetIndividualsOfSinglePKColumnClass(column, table, columnName);
		else if (Janus.cachedDBMetadata.isNotNull(table, column))
			return getQueryToGetIndividualsOfNonNullableColumnClass(column, table, columnName);
		else
			return getQueryToGetIndividualsOfNullableColumnClass(column, table, columnName);
	}
	
	private String getQueryToGetTypesOfFieldIndividual(URI individual, String columnName) {
		DBField field = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(individual);
		
		String table = field.getTableName();
		String column = field.getColumnName();
		String value = field.getValue();
		
		List<String> queries = new Vector<String>();
		
		queries.add(getQueryToGetTypeOfField(Janus.ontBridge.getOWLThingURI(), table, column, value, columnName));
		
		URI mappedClass = Janus.mappingMetadata.getMappedClass(table, column);
		
		Set<URI> familyClsses = Janus.ontBridge.getAllFamilyClasses(mappedClass);
		
		for (URI cls: familyClsses) {
			String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(cls);
			String mappedColumn = Janus.mappingMetadata.getMappedColumnNameToClass(cls);
			
			queries.add(getQueryToGetTypeOfField(cls, mappedTable, mappedColumn, value, columnName));
		}
		
		return getUnionQuery(queries, 1);
	}
	
	private String getQueryToGetTypesOfRecordIndividual(URI individual, String columnName) {
		List<DBField> fields = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(individual);
		
		String table = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(individual);
		
		List<String> queries = new Vector<String>();
		
		queries.add(getQueryToGetTypeOfRecord(Janus.ontBridge.getOWLThingURI(), table, fields, columnName));
		
		URI mappedClass = Janus.mappingMetadata.getMappedClass(table);
		
		Set<URI> familyClsses = Janus.ontBridge.getAllFamilyClasses(mappedClass);
		
		for (URI cls: familyClsses) {
			String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(cls);
			
			List<DBField> familyFields = new Vector<DBField>();
			
			for (DBField field: fields) {
				String matchedPKColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(field.getTableName(), field.getColumnName(), mappedTable);
				familyFields.add(new DBField(mappedTable, matchedPKColumn, field.getValue()));
			}
			
			queries.add(getQueryToGetTypeOfRecord(cls, mappedTable, familyFields, columnName));
		}
		
		return getUnionQuery(queries, 1);
	}
	
	private String getQueryToGetTypeOfRecord(URI cls, String table, List<DBField> fields, String columnName) {
		
		StringBuffer query = new StringBuffer("SELECT '" + OntEntity.getCURIE(cls) + "'" + " AS " + columnName 
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
	
	private String getQueryToGetTypeOfField(URI cls, String table, String column, String value, String columnName) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		if (!(Janus.cachedDBMetadata.isPrimaryKey(table, column)
				&& Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table)))
			query.append("DISTINCT ");
		
		query.append("'" + OntEntity.getCURIE(cls) + "'" + " AS " + columnName
				+ " FROM " + table
				+ " WHERE " + table + "." + column + " = " + "'" + value + "'");
		
		return query.toString();
	}
	
	String getUnionQuery(List<String> queries, int columnCount) {
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
	
	String getUnionQuery(List<String> queries, List<String> columnNames) {
		if (queries.isEmpty())
			return getQueryToGetEmptyResultSet(columnNames);
		
		if (queries.size() == 1)
			return queries.get(0);
		
		StringBuffer query = new StringBuffer();
		
		for (int i = 0; i < queries.size() - 1; i++)
			query.append("(" + queries.get(i) +") UNION ");
		
		query.append("(" + queries.get(queries.size() - 1) + ")");
		
		return query.toString();
	}
	
	String getUnionQuery(List<String> queries, String columnName) {
		if (queries.isEmpty())
			return getQueryToGetEmptyResultSet(columnName);
		
		if (queries.size() == 1)
			return queries.get(0);
		
		StringBuffer query = new StringBuffer();
		
		for (int i = 0; i < queries.size() - 1; i++)
			query.append("(" + queries.get(i) +") UNION ");
		
		query.append("(" + queries.get(queries.size() - 1) + ")");
		
		return query.toString();
	}
	
	public String getQueryToGetTypesOfIndividual(URI individual, String columnName) {
		String query = null;
		
		if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.RECORD_INDIVIDUAL))
			query = getQueryToGetTypesOfRecordIndividual(individual, columnName);
		else if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.FIELD_INDIVIDUAL))
			query = getQueryToGetTypesOfFieldIndividual(individual, columnName);
		
		return query;
	}
	
	//object property assertions 
	public String getQueryToGetOPAssertionsOfIndividual(URI individual, String pColumnName, String stColumnName) {
		String query = null;
		
		if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.RECORD_INDIVIDUAL))
			query = getQueryToGetOPAssertionsOfRecordSourceIndividual(individual, pColumnName, stColumnName); // stColumnName = tColumnName
		else if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.FIELD_INDIVIDUAL))
			query = getQueryToGetOPAssertionsOfFieldTargetIndividual(individual, pColumnName, stColumnName); //stColumnName = sColumnName
		
		return query;
	}
	
	public String getQueryToGetAllPropertyAssertionsOfSourceIndividual(URI srcIndividual, String pColumnName, String tColumnName) {
		List<String> queries = null;
		
		if (Janus.mappingMetadata.getIndividualType(srcIndividual).equals(OntEntityTypes.RECORD_INDIVIDUAL)) {
			queries = getQueriesToGetOPAssertionsOfRecordSourceIndividual(srcIndividual, pColumnName, tColumnName);
			queries.addAll(getQueriesToGetDPAssertionsOfSourceIndividual(srcIndividual, pColumnName, tColumnName));
		} else if (Janus.mappingMetadata.getIndividualType(srcIndividual).equals(OntEntityTypes.FIELD_INDIVIDUAL))
			queries = getQueriesToGetDPAssertionsOfSourceIndividual(srcIndividual, pColumnName, tColumnName);
		
		return getUnionQuery(queries, 2);
	}
	
	// the parameter individual is object.
	private String getQueryToGetOPAssertionsOfFieldTargetIndividual(URI individual, String pColumnName, String sColumnName) {
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
						
			queries.add(getQueryToGetOPAssertionOfField(new DBField(familyTable, familyColumn, value), pColumnName, sColumnName));
		}
		
		return getUnionQuery(queries, 2);
	}
	
	// the parameter individual is subject.
	private String getQueryToGetOPAssertionsOfRecordSourceIndividual(URI individual, String pColumnName, String tColumnName) {
		List<String> queries = getQueriesToGetOPAssertionsOfRecordSourceIndividual(individual, pColumnName, tColumnName);
		return getUnionQuery(queries, 2);
	}
	
	// the parameter individual is subject.
	private List<String> getQueriesToGetOPAssertionsOfRecordSourceIndividual(URI individual, String pColumnName, String tColumnName) {
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

				queries.add(getQueryToGetOPAssertionOfRecord(mappedOP, keyColumn, mappedTable, familyFields, pColumnName, tColumnName));
			}
		}

		return queries;
	}
	
	// the parameter individual is subject.
	private String getQueryToGetDPAssertionsOfRecordSourceIndividual(URI individual, String pColumnName, String tColumnName) {
		List<String> queries = getQueriesToGetDPAssertionsOfRecordSourceIndividual(individual, pColumnName, tColumnName);		
		return getUnionQuery(queries, 2);
	}
	
	// the parameter individual is subject.
	private List<String> getQueriesToGetDPAssertionsOfRecordSourceIndividual(URI individual, String pColumnName, String tColumnName) {
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

				queries.add(getQueryToGetDPAssertionOfRecord(mappedDP, nonKeyColumn, mappedTable, familyFields, pColumnName, tColumnName));
			}
		}

		return queries;
	}
	
	// the parameter individual is subject.
	private String getQueryToGetDPAssertionsOfFieldSourceIndividual(URI individual, String pColumnName, String tColumnName) {
		List<String> queries = getQueriesToGetDPAssertionsOfFieldSourceIndividual(individual, pColumnName, tColumnName);
		return getUnionQuery(queries, 2);
	}
	
	// the parameter individual is subject.
	private List<String> getQueriesToGetDPAssertionsOfFieldSourceIndividual(URI individual, String pColumnName, String tColumnName) {
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

			queries.add(getQueryToGetDPAssertionOfField(mappedDP, mappedColumn, mappedTable, value, pColumnName, tColumnName));
		}

		return queries;
	}
	
	//data property assertions 
	public String getQueryToGetDPAssertionsOfSourceIndividual(URI individual, String pColumnName, String tColumnName) {
		String query = null;

		if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.RECORD_INDIVIDUAL))
			query = getQueryToGetDPAssertionsOfRecordSourceIndividual(individual, pColumnName, tColumnName);
		else if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.FIELD_INDIVIDUAL))
			query = getQueryToGetDPAssertionsOfFieldSourceIndividual(individual, pColumnName, tColumnName);

		return query;
	}
	
	//data property assertions 
	public List<String> getQueriesToGetDPAssertionsOfSourceIndividual(URI individual, String pColumnName, String tColumnName) {
		List<String> queries = null;

		if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.RECORD_INDIVIDUAL))
			queries = getQueriesToGetDPAssertionsOfRecordSourceIndividual(individual, pColumnName, tColumnName);
		else if (Janus.mappingMetadata.getIndividualType(individual).equals(OntEntityTypes.FIELD_INDIVIDUAL))
			queries = getQueriesToGetDPAssertionsOfFieldSourceIndividual(individual, pColumnName, tColumnName);

		return queries;
	}
	
	//data property assertions 
	public String getQueryToGetDPAsserionsOfDP(URI dp, String sColumnName, String tColumnName) {
		String query = null;
		
		URI domainCls = Janus.mappingMetadata.getDomainClassOfProperty(dp);
		
		OntEntityTypes type = Janus.mappingMetadata.getClassType(domainCls);
		
		if (type.equals(OntEntityTypes.TABLE_CLASS))
			query = getQueryToGetDPAssertionsOfDPWithTableClassDomain(dp, sColumnName, tColumnName);
		else if (type.equals(OntEntityTypes.COLUMN_CLASS))
			query = getQueryToGetDPAssertionsOfDPWithColumnClassDomain(dp, sColumnName, tColumnName);
		
		return query;
	}
	
	//data property assertions
	public String getQueryToGetDPAssertionsOfTargetLiteral(String literal, String pColumnName, String sColumnName) {
		String lexicalValue = Janus.mappingMetadata.getLexicalValueOfTypedLiteral(literal);
		String datatypeOfTypedLiteral = Janus.mappingMetadata.getDatatypeOfTypedLiteral(literal);
		
		Set<DBColumn> dbColumns = Janus.mappingMetadata.getMappedDBColumnsToDatatypeOfTypedLiteral(datatypeOfTypedLiteral);
		
		List<String> queries = new Vector<String>();
		
		for(DBColumn dbColumn: dbColumns) {
			
			if (Janus.cachedDBMetadata.isKey(dbColumn.getTableName(), dbColumn.getColumnName()))
				queries.add(getQueryToGetDPAssertionsOfKeyColumnLiteral(dbColumn, lexicalValue, pColumnName, sColumnName));
			else
				queries.add(getQueryToGetDPAssertionsOfNonKeyColumnLiteral(dbColumn, lexicalValue, pColumnName, sColumnName));
		}
		
		return getUnionQuery(queries, 2);
	}
	
	// for Type(?a, ?C), which both ?a and ?C are empty.
	public String getQueryToGetAllClsAssertions(String columnName1, String columnName2) {
		List<String> queries = new Vector<String>();
		
		URI owlThing = Janus.ontBridge.getOWLThingURI();
		
		Set<String> tables = Janus.cachedDBMetadata.getTableNames();
		
		for (String table: tables) {
			
			if (Janus.cachedDBMetadata.isRootTable(table)) {
				queries.add(getQueryToGetAllClsAssertionsOfTableClass(table, owlThing, columnName1, columnName2));
			}
			
			URI mappedClass = Janus.mappingMetadata.getMappedClass(table);
			queries.add(getQueryToGetAllClsAssertionsOfTableClass(table, mappedClass, columnName1, columnName2));
			
			Set<String> keys = Janus.cachedDBMetadata.getKeyColumns(table);
			
			for (String key: keys) {
				if (Janus.cachedDBMetadata.isRootColumn(table, key))
					queries.add(getQueryToGetAllClsAssertionsOfColumnClass(table, key, owlThing, columnName1, columnName2));
				
				mappedClass = Janus.mappingMetadata.getMappedClass(table, key);
				queries.add(getQueryToGetAllClsAssertionsOfColumnClass(table, key, mappedClass, columnName1, columnName2));
			}
		}
		
		return getUnionQuery(queries, 2);
	}
	
	// for PropertyValue(?a, ?p, ?d), which ?a, ?p and ?d are all empty.
	public String getQueryToGetAllPropertyAssertions(String pColumnName, String sColumnName, String tColumnName) {
		List<String> queries = new Vector<String>();
		
		Set<String> tables = Janus.cachedDBMetadata.getTableNames();
		
		for (String table: tables) {
			Set<String> columns = Janus.cachedDBMetadata.getColumns(table);
			
			for (String column: columns)
				if (Janus.cachedDBMetadata.isKey(table, column)) {
					queries.add(getQueryToGetAllOPAssertions(table, column, pColumnName, sColumnName, tColumnName));
					queries.add(getQueryToGetAllDPAssertionsOfFields(table, column, pColumnName, sColumnName, tColumnName));
				} else
					queries.add(getQueryToGetAllDPAssertionsOfRecords(table, column, pColumnName, sColumnName, tColumnName));
		}
		
		return getUnionQuery(queries, 3);
	}
	
	// for PropertyValue(?a, ?p, ?d), which only ?p is a variable and both ?a and ?d are individuals.
	public String getQueryToGetObjectPropertiesOfOPAssertion(URI aSourceIndividual, URI aTargetIndividual, String columnName) {
		List<String> queries = new Vector<String>();
		
		String srcTable = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(aSourceIndividual);
		
		Set<String> familyTablesOfSrc = Janus.cachedDBMetadata.getFamilyTables(srcTable);
		
		DBField targetField = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(aTargetIndividual);
		
		Set<DBColumn> familyColumnsOfTarget = Janus.cachedDBMetadata.getFamilyColumns(targetField.getTableName(), targetField.getColumnName());
		
		Set<DBColumn> columnsInFamilyTablesOfSrc = Janus.cachedDBMetadata.getColumsIncludedInTables(familyTablesOfSrc, familyColumnsOfTarget);
		
		List<DBField> srcFields = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(aSourceIndividual);
		
		for (DBColumn aColumn: columnsInFamilyTablesOfSrc) {
			String table = aColumn.getTableName();
			String column = aColumn.getColumnName();
			String value = targetField.getValue();
			
			URI op = Janus.mappingMetadata.getMappedObjectProperty(table, column);
			
			StringBuffer query = new StringBuffer("SELECT " + "'" + OntEntity.getCURIE(op) + "'" + " AS " + columnName + 
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
		
		return getUnionQuery(queries, columnName);
	}
	
	// for PropertyValue(?a, ?p, ?d), which only ?p is a variable, ?a is an individual and ?d is a literal.
	public String getQueryToGetDataPropertiesOfDPAssertion(URI aSourceIndividual, String aTargetLiteral, String columnName) {
		OntEntityTypes individualType = Janus.mappingMetadata.getIndividualType(aSourceIndividual);
		
		if (individualType.equals(OntEntityTypes.RECORD_INDIVIDUAL))
			return getQueryToGetDataPropertiesOfDPAssertionWithRecordSrcIndividual(aSourceIndividual, aTargetLiteral, columnName);
		else if (individualType.equals(OntEntityTypes.FIELD_INDIVIDUAL)) 
			return getQueryToGetDataPropertiesOfDPAssertionWithFieldSrcIndividual(aSourceIndividual, aTargetLiteral, columnName);
		else
			return getQueryToGetEmptyResultSet(columnName);
	}
	
	private String getQueryToGetDataPropertiesOfDPAssertionWithFieldSrcIndividual(URI aSourceIndividual, String aTargetLiteral, String columnName) {
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
			return getUnionQuery(queries, columnName);
		
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
				
			query.append("'" + OntEntity.getCURIE(dp) + "'" + " AS " + columnName + 
						" FROM " + table + 
						" WHERE " + table + "." + column + " = " + "'" + valueOfTargetLiteral + "'");
			
			queries.add(query.toString());
		}
		
		return getUnionQuery(queries, 1);
	}
	
	private String getQueryToGetDataPropertiesOfDPAssertionWithRecordSrcIndividual(URI aSourceIndividual, String aTargetLiteral, String columnName) {
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
					
					StringBuffer query = new StringBuffer("SELECT " + "'" + OntEntity.getCURIE(dp) + "'" + " AS " + columnName + 
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
	
	public String getQueryToGetTheSameIndividualsAs(URI individual, String variable) {
		StringBuffer query = new StringBuffer(getQueryToCheckPresenceOfIndividual(individual));
		
		int start = query.indexOf("SELECT ") + "SELECT ".length();
		
		int end = query.indexOf("FROM");
		
		String str = "'" + OntEntity.getCURIE(individual) + "'" + " AS " + variable + " ";
		
		query.replace(start, end, str);
		
		return query.toString();
	}
	
	public String getQueryToCheckPresenceOfIndividual(URI individual) {
		StringBuffer query = new StringBuffer("SELECT " + "1 ");
		
		OntEntityTypes type = Janus.mappingMetadata.getIndividualType(individual);
		
		if (type.equals(OntEntityTypes.RECORD_INDIVIDUAL)) {
			String table = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(individual);
			
			query.append("FROM " + table + 
						" WHERE ");
			
			List<DBField> fields = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(individual);
			
			for (DBField field: fields)
				query.append(field.toString() + " AND ");
			
			query.delete(query.lastIndexOf(" AND "), query.length());
			
		} else if (type.equals(OntEntityTypes.FIELD_INDIVIDUAL)) {
			DBField field = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(individual);
			String table = field.getTableName();
			
			query.append("FROM " + table + 
						" WHERE " + field.toString());
		}
		
		return query.toString();
	}
	
	public String getQueryToCheckPresenceOfPKFields(List<DBField> pk) {
		StringBuffer query = new StringBuffer("SELECT " + "1 ");
		
		String table = pk.get(0).getTableName();
		
		query.append("FROM " + table + 
					" WHERE ");
		
		for (DBField field: pk)
			query.append(field.toString() + " AND ");
			
		query.delete(query.lastIndexOf(" AND "), query.length());
		
		return query.toString();
	}
	
	public String getQueryToCheckPresenceOfField(DBField field) {
		String table = field.getTableName();
		
		return "SELECT " + "1 " + 
			   "FROM " + table + 
			  " WHERE " + field.toString();
	}
	
	public String getQueryToCheckPresence(Set<String> queries) {
		StringBuffer query = new StringBuffer("SELECT CASE " + 
													 "WHEN " + "count(*) > 0 " + "THEN " + "'true' " +
													 "ELSE " + "'false' " + 
													 "END " + "AS " + "'' " +
											  "FROM " + "DUAL " +
											  "WHERE ");
		
		for (String aQuery: queries)
			query.append("EXISTS (" + aQuery + ") AND ");
		
		query.delete(query.lastIndexOf(" AND "), query.length());
		
		return query.toString();
	}
	
	public String getNaturalJoinedQuery(String query1, String query2, List<String> columnNamesToBeSelected) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		int columnCount = columnNamesToBeSelected.size();
		for (int i = 0; i < columnCount; i++) {
			query.append(columnNamesToBeSelected.get(i));
			
			if (i != columnCount - 1)
				query.append(", ");
			
		}
		
		query.append(" FROM " + "(" + query1 + ")" + " AS " + "T1" + " NATURAL JOIN " + "(" + query2 + ")" + " AS " + "T2");
		
		return query.toString();
	}
	
	public String getProjectedQuery(List<String> columnNames, String query) {
		StringBuffer projectedQuery = new StringBuffer("SELECT ");
		
		int columnCount = columnNames.size();
		for (int i = 0; i < columnCount; i++) {
			projectedQuery.append(columnNames.get(i));
			
			if (i != columnCount - 1)
				projectedQuery.append(", ");
		}
		
		projectedQuery.append(" FROM " + "(" + query + ")" + " AS " + "T");
		
		return projectedQuery.toString();
	}
}