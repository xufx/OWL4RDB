package janus.query.sparqldl;

import janus.Janus;
import janus.database.DBColumn;
import janus.database.DBField;
import janus.mapping.OntEntityTypes;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.types.QueryArgumentType;

class AtomTypeProcessor {
	static boolean execute0WithoutPresenceCheck(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		
		URI iURI = URI.create(arg1.getValue());
		URI cURI = URI.create(arg2.getValue());
		
		OntEntityTypes individualType = Janus.mappingMetadata.getIndividualType(iURI);
		OntEntityTypes classType = Janus.mappingMetadata.getClassType(cURI);
		
		if (individualType == null || classType == null)
			return false;
		
		if (individualType.equals(OntEntityTypes.RECORD_INDIVIDUAL) 
				&& classType.equals(OntEntityTypes.TABLE_CLASS)) {
			
			String tableMappedToIndividual = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(iURI);
			
			Set<String> familyTablesOfIndividual = Janus.cachedDBMetadata.getFamilyTables(tableMappedToIndividual);
			
			String tableMappedToClass = Janus.mappingMetadata.getMappedTableNameToClass(cURI);
			
			if (!familyTablesOfIndividual.contains(tableMappedToClass))
				return false;
			
		} else if (individualType.equals(OntEntityTypes.FIELD_INDIVIDUAL) 
				&& classType.equals(OntEntityTypes.COLUMN_CLASS)) {
			
			DBField fieldMappedToIndividual = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(iURI);
			
			Set<DBColumn> familyColumnsOfIndividual = Janus.cachedDBMetadata.getFamilyColumns(fieldMappedToIndividual.getTableName(), fieldMappedToIndividual.getColumnName());
			
			DBColumn columnMappedToClass = Janus.mappingMetadata.getMappedColumnToClass(cURI);
			
			if (!familyColumnsOfIndividual.contains(columnMappedToClass))
				return false;
			
		} else if (!classType.equals(OntEntityTypes.OWL_THING_CLASS))
			return false;
		
		return true;
	}
	
	static String execute0WithPresenceCheck(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		
		URI iURI = URI.create(arg1.getValue());
		URI cURI = URI.create(arg2.getValue());
		
		OntEntityTypes individualType = Janus.mappingMetadata.getIndividualType(iURI);
		OntEntityTypes classType = Janus.mappingMetadata.getClassType(cURI);
		
		if (individualType.equals(OntEntityTypes.RECORD_INDIVIDUAL) 
				&& classType.equals(OntEntityTypes.TABLE_CLASS)) {
			
			List<DBField> fields = new ArrayList<DBField>();
			
			String tableMappedToIndividual = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(iURI);
			
			List<DBField> fieldsMappedToIndividual = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(iURI);
			
			String tableMappedToClass = Janus.mappingMetadata.getMappedTableNameToClass(cURI);
			
			List<String> pk = Janus.cachedDBMetadata.getPrimaryKey(tableMappedToClass);
			
			for (String pkColumn: pk) {
				String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(tableMappedToClass, pkColumn, tableMappedToIndividual);
				
				for (DBField field: fieldsMappedToIndividual)
					if (matchedColumn.equals(field.getColumnName())) {
						fields.add(new DBField(tableMappedToClass, pkColumn, field.getValue()));
						break;
					}
			}
			
			return Janus.sqlGenerator.getQueryToCheckPresenceOfPKFields(fields);
			
		} else if (individualType.equals(OntEntityTypes.FIELD_INDIVIDUAL) 
				&& classType.equals(OntEntityTypes.COLUMN_CLASS)) {
			
			DBField fieldMappedToIndividual = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(iURI);
			
			DBColumn columnMappedToClass = Janus.mappingMetadata.getMappedColumnToClass(cURI);
			
			return Janus.sqlGenerator.getQueryToCheckPresenceOfField(new DBField(columnMappedToClass.getTableName(), columnMappedToClass.getColumnName(), fieldMappedToIndividual.getValue()));
			
		} else // when (classType.equals(OntEntityTypes.OWL_THING_CLASS))
			return Janus.sqlGenerator.getQueryToCheckPresenceOfIndividual(iURI);
	}
	
	static SQLResultSet execute1(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		
		String varName = null;
		URI cURI = null;
		URI iURI = null;
		
		if (arg1.getType().equals(QueryArgumentType.VAR)) {
			varName = arg1.toString();
			cURI = URI.create(arg2.getValue());
		}
		else {
			varName = arg2.toString();
			iURI = URI.create(arg1.getValue());
		}
		
		if (iURI == null) {
			
			if (Janus.ontBridge.containsClass(cURI)) {
				String query = Janus.sqlGenerator.getQueryToGetIndividualsOfClass(cURI, varName);
				return new SQLResultSet(query, varName);
			} else {
				SQLResultSet resultSet = new SQLResultSet(Janus.sqlGenerator.getQueryToGetEmptyResultSet(varName), varName);
				resultSet.setEmptySet();
				return resultSet;
			}
		
		} else {
			
			if (!Janus.mappingMetadata.isBeableIndividual(iURI)) {
				System.err.println("No Such an Individual.");
				SQLResultSet resultSet = new SQLResultSet(Janus.sqlGenerator.getQueryToGetEmptyResultSet(varName), varName);
				resultSet.setEmptySet();
				return resultSet;
			}
			
			String query = Janus.sqlGenerator.getQueryToGetTypesOfIndividual(iURI, varName);
			return new SQLResultSet(query, varName);
		}
	}
	
	static SQLResultSet execute2(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		String varName1 = args.get(0).toString();
		String varName2 = args.get(1).toString();
		
		String query = Janus.sqlGenerator.getQueryToGetAllClsAssertions(varName2, varName1);
		List<String> varNames = new Vector<String>(2);
		varNames.add(varName2);
		varNames.add(varName1);
		return new SQLResultSet(query, varNames);
	}
}
