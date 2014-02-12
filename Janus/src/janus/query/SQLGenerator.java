package janus.query;

import janus.Janus;
import janus.mapping.DBField;
import janus.mapping.metadata.ClassTypes;
import janus.mapping.metadata.IndividualTypes;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public abstract class SQLGenerator {
	
	/* Type I-I: when a table has one column primary key. 
	 * SELECT aPK FROM table
	 */
	protected abstract String getQueryToGetIndividualsOfSinglePKColumnClass(String aPK, String table);
	
	/* Type I-II: when a table has multiple column primary key.
	 * SELECT pk1, pk2, ... , pkn FROM table
	 */
	protected abstract String getQueryToGetIndividualsOfTableClass(List<String> pk, String table);
	
	/* Type I-III: to select a non-nullable key column.
	 * SELECT DISTINCT keyColumn FROM table
	 */
	protected abstract String getQueryToGetIndividualsOfNonNullableColumnClass(String keyColumn, String table);
	
	/* Type I-IV: to select a nullable key column.
	 * SELECT DISTINCT keyColumn FROM table WHERE keyColumn IS NOT NULL
	 */
	protected abstract String getQueryToGetIndividualsOfNullableColumnClass(String keyColumn, String table);
	
	public String getQueryToGetIndividualsOfClass(URI cls) {
		String query = null;
		
		if (Janus.mappingMetadata.getClassType(cls).equals(ClassTypes.TABLE_CLASS))
			query = getQueryToGetIndividualsOfTableClass(cls);
		else if (Janus.mappingMetadata.getClassType(cls).equals(ClassTypes.COLUMN_CLASS))
			query = getQueryToGetIndividualsOfColumnClass(cls);
		else if (Janus.mappingMetadata.getClassType(cls).equals(ClassTypes.OWL_THING))
			query = getQueryToGetIndividualsOfOwlThing();
		
		return query;
	}
	
	private String getQueryToGetIndividualsOfOwlThing() {
		List<String> queries = new Vector<String>();
		
		Set<URI> clses = Janus.ontBridge.getSubClses(Janus.ontBridge.getOWLThingURI());
		for (URI aCls: clses) {
			if (Janus.mappingMetadata.getClassType(aCls).equals(ClassTypes.TABLE_CLASS))
				queries.add(getQueryToGetIndividualsOfTableClass(aCls));
			else if (Janus.mappingMetadata.getClassType(aCls).equals(ClassTypes.COLUMN_CLASS))
				queries.add(getQueryToGetIndividualsOfColumnClass(aCls));
		}
		
		return getUnionQuery(queries);
	}
	
	private String getQueryToGetIndividualsOfTableClass(URI cls) {
		String table = Janus.mappingMetadata.getMappedTableNameToClass(cls);
		List<String> pk = Janus.cachedDBMetadata.getPrimaryKeys(table);
		
		return getQueryToGetIndividualsOfTableClass(pk, table);
	}
	
	private String getQueryToGetIndividualsOfColumnClass(URI cls) {
		String table = Janus.mappingMetadata.getMappedTableNameToClass(cls);
		String column = Janus.mappingMetadata.getMappedColumnNameToClass(cls);
		
		if (Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table))
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
		
		return getUnionQuery(queries);
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
		
		return getUnionQuery(queries);
	}
	
	private String getQueryToGetTypeOfRecord(URI cls, String table, List<DBField> fields) {
		
		StringBuffer query = new StringBuffer("SELECT '"  + cls.getFragment() +"'"
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
		
		query.append("'" + cls.getFragment() +"'"
				+ " FROM " + table
				+ " WHERE " + table + "." + column + " = " + "'" + value + "'");
		
		return query.toString();
	}
	
	private String getUnionQuery(List<String> queries) {
		StringBuffer query = new StringBuffer();
		
		for (int i = 0; i < queries.size() - 1; i++)
			query.append("(" + queries.get(i) +") UNION ");
		
		query.append("(" + queries.get(queries.size() - 1) + ")");
		
		return query.toString();
	}
	
	public String getQueryToGetTypesOfIndividual(URI individual) {
		String query = null;
		
		if (Janus.mappingMetadata.getIndividualType(individual).equals(IndividualTypes.RECORD_INDIVIDUAL))
			query = getQueryToGetTypesOfRecordIndividual(individual);
		else if (Janus.mappingMetadata.getIndividualType(individual).equals(IndividualTypes.FIELD_INDIVIDUAL))
			query = getQueryToGetTypesOfFieldIndividual(individual);
		
		return query;
	}
}