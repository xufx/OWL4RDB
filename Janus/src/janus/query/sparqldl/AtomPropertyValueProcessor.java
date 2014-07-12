package janus.query.sparqldl;

import janus.Janus;
import janus.database.DBColumn;
import janus.database.DBField;
import janus.mapping.DatatypeMap;
import janus.mapping.OntEntityTypes;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;

class AtomPropertyValueProcessor {
	static boolean execute0WithoutPresenceCheck(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		QueryArgument arg3 = args.get(2);
		
		URI sURI = URI.create(arg1.getValue());
		URI pURI = URI.create(arg2.getValue());
		
		OntEntityTypes propertyType = Janus.mappingMetadata.getPropertyType(pURI);
		
		if (propertyType.equals(OntEntityTypes.OWL_TOP_DATA_PROPERTY)
				|| propertyType.equals(OntEntityTypes.OWL_TOP_OBJECT_PROPERTY))
			return false;
		
		if (propertyType.equals(OntEntityTypes.OBJECT_PROPERTY)) {
			
			URI oURI = URI.create(arg3.getValue());
			
			if (!(Janus.mappingMetadata.getIndividualType(sURI).equals(OntEntityTypes.RECORD_INDIVIDUAL)
					&& Janus.mappingMetadata.getIndividualType(oURI).equals(OntEntityTypes.FIELD_INDIVIDUAL)))
				return false;
			
			String tableMappedToSrcIndividual = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(sURI);
			
			Set<String> familyTablesOfIndividual = Janus.cachedDBMetadata.getFamilyTables(tableMappedToSrcIndividual);
			
			DBField fieldMappedToTargetIndividual = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(oURI);
			
			Set<DBColumn> familyColumnsOfTargetIndividual = Janus.cachedDBMetadata.getFamilyColumns(fieldMappedToTargetIndividual.getTableName(), fieldMappedToTargetIndividual.getColumnName());
			
			DBColumn columnMappedToProperty = Janus.mappingMetadata.getMappedColumnToProperty(pURI);
			
			if (!(familyTablesOfIndividual.contains(columnMappedToProperty.getTableName())
					&& familyColumnsOfTargetIndividual.contains(columnMappedToProperty)))
				return false;
			
		} else if (propertyType.equals(OntEntityTypes.DATA_PROPERTY)) {
			
			String oLiteral = arg3.getValue();
			
			DBColumn columnMappedToProperty = Janus.mappingMetadata.getMappedColumnToProperty(pURI);
			String propertyTable = columnMappedToProperty.getTableName();
			String propertyColumn = columnMappedToProperty.getColumnName();
			
			String datatypeOfLiteral = Janus.mappingMetadata.getDatatypeOfTypedLiteral(oLiteral);
			Set<Integer> mappedSQLTypes = DatatypeMap.getMappedSQLTypes(datatypeOfLiteral);
			
			if (!mappedSQLTypes.contains(Janus.cachedDBMetadata.getDataType(propertyTable, propertyColumn)))
					return false;
			
			if (Janus.mappingMetadata.getIndividualType(sURI).equals(OntEntityTypes.RECORD_INDIVIDUAL)) {
				
				if (Janus.cachedDBMetadata.isKey(propertyTable, propertyColumn))
					return false;
				
				String tableMappedToSrcIndividual = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(sURI);
				
				Set<String> familyTablesOfIndividual = Janus.cachedDBMetadata.getFamilyTables(tableMappedToSrcIndividual);
				
				if (!familyTablesOfIndividual.contains(propertyTable))
					return false;
				
			} else if (Janus.mappingMetadata.getIndividualType(sURI).equals(OntEntityTypes.FIELD_INDIVIDUAL)) {
				
				if (!Janus.cachedDBMetadata.isKey(propertyTable, propertyColumn))
					return false;
				
				DBField fieldMappedToSrcIndividual = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(sURI);
				
				Set<DBColumn> familyColumnsOfSrcIndividual = Janus.cachedDBMetadata.getFamilyColumns(fieldMappedToSrcIndividual.getTableName(), fieldMappedToSrcIndividual.getColumnName());
				
				if (!familyColumnsOfSrcIndividual.contains(columnMappedToProperty))
					return false;
				
			} else
				return false;
			
		} else
			return false;
		
		return true;
	}
	
	static Set<String> execute0WithPresenceCheck(QueryAtom atom) {
		Set<String> queries = new ConcurrentSkipListSet<String>();
		
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		QueryArgument arg3 = args.get(2);
		
		URI sURI = URI.create(arg1.getValue());
		URI pURI = URI.create(arg2.getValue());
		
		OntEntityTypes typeOfSIndividual = Janus.mappingMetadata.getIndividualType(sURI);
		
		DBColumn columnMappedToProperty = Janus.mappingMetadata.getMappedColumnToProperty(pURI);
		
		String tableMappedToProperty = columnMappedToProperty.getTableName();
		
		//-> for subject
		if (typeOfSIndividual.equals(OntEntityTypes.RECORD_INDIVIDUAL)) {
			
			List<DBField> fields = new ArrayList<DBField>();
			
			String tableMappedToSIndividual = Janus.mappingMetadata.getMappedTableNameToRecordIndividual(sURI);
			
			List<DBField> fieldsMappedToSIndividual = Janus.mappingMetadata.getMappedDBFieldsToRecordIndividual(sURI);
			
			List<String> pk = Janus.cachedDBMetadata.getPrimaryKey(tableMappedToProperty);
			
			for (String pkColumn: pk) {
				String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(tableMappedToProperty, pkColumn, tableMappedToSIndividual);
				
				for (DBField field: fieldsMappedToSIndividual)
					if (matchedColumn.equals(field.getColumnName())) {
						fields.add(new DBField(tableMappedToProperty, pkColumn, field.getValue()));
						break;
					}
			}
			
			queries.add(Janus.sqlGenerator.getQueryToCheckPresenceOfPKFields(fields));
			
		} else if (typeOfSIndividual.equals(OntEntityTypes.FIELD_INDIVIDUAL)) {
			
			DBField fieldMappedToSIndividual = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(sURI);
			
			queries.add(Janus.sqlGenerator.getQueryToCheckPresenceOfField(new DBField(tableMappedToProperty, columnMappedToProperty.getColumnName(), fieldMappedToSIndividual.getValue())));
			
		}
		//<- for subject
		
		//-> for object
		OntEntityTypes propertyType = Janus.mappingMetadata.getPropertyType(pURI);
		
		if (propertyType.equals(OntEntityTypes.OBJECT_PROPERTY)) {
			
			URI oURI = URI.create(arg3.getValue());
			
			DBField fieldMappedToOIndividual = Janus.mappingMetadata.getMappedDBFieldToFieldIndividual(oURI);
			
			queries.add(Janus.sqlGenerator.getQueryToCheckPresenceOfField(new DBField(tableMappedToProperty, columnMappedToProperty.getColumnName(), fieldMappedToOIndividual.getValue())));
			
		} else if (propertyType.equals(OntEntityTypes.DATA_PROPERTY)) {
			
			String oLiteral = arg3.getValue();
			
			queries.add(Janus.sqlGenerator.getQueryToCheckPresenceOfField(new DBField(tableMappedToProperty, columnMappedToProperty.getColumnName(), Janus.mappingMetadata.getLexicalValueOfTypedLiteral(oLiteral))));			
		}
		//<- for object
		
		return queries;
	}
	
	static SQLResultSet execute1(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();

		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		QueryArgument arg3 = args.get(2);

		String varName = null;

		URI sURI = null;
		URI pURI = null;
		URI oURI = null;
		String oLit = null;

		if (arg1.isVar()) {
			
			varName = arg1.getValue();
			pURI = URI.create(arg2.getValue());

			if (Janus.ontBridge.containsObjectProperty(pURI)) {
				
				oURI = URI.create(arg3.getValue());
				
				String query = Janus.sqlGenerator.getQueryToGetSourceIndividualsOfOPAssertion(pURI, oURI, varName);
				return new SQLResultSet(query, varName);
				
			} else if (Janus.ontBridge.containsDataProperty(pURI)) {
				
				oLit = arg3.getValue();
				
				String query = Janus.sqlGenerator.getQueryToGetSourceIndividualsOfDPAssertion(pURI, oLit, varName);
				return new SQLResultSet(query, varName);

			} else {
				System.err.println(pURI + " in the atom " + atom + " is not asserted.");
				SQLResultSet resultSet = new SQLResultSet(Janus.sqlGenerator.getQueryToGetEmptyResultSet(varName), varName);
				resultSet.setEmptySet();
				return resultSet;
			}
		
		} else if (arg2.isVar()) {
			
			varName = arg2.getValue();
			sURI = URI.create(arg1.getValue());

			if (!Janus.mappingMetadata.isBeableIndividual(sURI)) {
				System.err.println("No Such an Individual.");
				SQLResultSet resultSet = new SQLResultSet(Janus.sqlGenerator.getQueryToGetEmptyResultSet(varName), varName);
				resultSet.setEmptySet();
				return resultSet;
			}

			if (arg3.isLiteral()) {

				oLit = arg3.getValue();
				
				String query = Janus.sqlGenerator.getQueryToGetDataPropertiesOfDPAssertion(sURI, oLit, varName);
				return new SQLResultSet(query, varName);

			} else {

				oURI = URI.create(arg3.getValue());
				
				if (!Janus.mappingMetadata.isBeableIndividual(oURI)) {
					System.err.println("No Such an Individual.");
					SQLResultSet resultSet = new SQLResultSet(Janus.sqlGenerator.getQueryToGetEmptyResultSet(varName), varName);
					resultSet.setEmptySet();
					return resultSet;
				}
				
				String query = Janus.sqlGenerator.getQueryToGetObjectPropertiesOfOPAssertion(sURI, oURI, varName);
				return new SQLResultSet(query, varName);
				
			}
		} else {
			varName = arg3.getValue();

			sURI = URI.create(arg1.getValue());

			if (!Janus.mappingMetadata.isBeableIndividual(sURI)) {
				System.err.println("No Such an Individual.");
				SQLResultSet resultSet = new SQLResultSet(Janus.sqlGenerator.getQueryToGetEmptyResultSet(varName), varName);
				resultSet.setEmptySet();
				return resultSet;
			}
			
			pURI = URI.create(arg2.getValue());

			if (Janus.ontBridge.containsObjectProperty(pURI)) {
				
				String query = Janus.sqlGenerator.getQueryToGetTargetIndividualsOfOPAssertion(pURI, sURI, varName);
				return new SQLResultSet(query, varName);

			} else if (Janus.ontBridge.containsDataProperty(pURI)) {

				String query = Janus.sqlGenerator.getQueryToGetTargetLiteralsOfDPAssertion(pURI, sURI, varName);
				return new SQLResultSet(query, varName);
				
			} else  {
				System.err.println(pURI + " in the atom " + atom + " is not asserted.");
				SQLResultSet resultSet = new SQLResultSet(Janus.sqlGenerator.getQueryToGetEmptyResultSet(varName), varName);
				resultSet.setEmptySet();
				return resultSet;
			}
		}
	}
	
	static SQLResultSet execute2(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		QueryArgument arg3 = args.get(2);
		
		URI sURI = null;
		URI pURI = null;
		URI oURI = null;
		String oLit = null;
		
		if (!arg1.isVar()) {
			String pVarName = arg2.getValue();
			String oVarName = arg3.getValue();
			
			sURI = URI.create(arg1.getValue());
			
			if (!Janus.mappingMetadata.isBeableIndividual(sURI)) {
				System.err.println("No Such an Individual.");
				List<String> varNames = new Vector<String>(2);
				varNames.add(pVarName);
				varNames.add(oVarName);
				SQLResultSet resultSet = new SQLResultSet(Janus.sqlGenerator.getQueryToGetEmptyResultSet(varNames), varNames);
				resultSet.setEmptySet();
				return resultSet;
			}
			
			String query = Janus.sqlGenerator.getQueryToGetAllPropertyAssertionsOfSourceIndividual(sURI, pVarName, oVarName);
			List<String> varNames = new Vector<String>(2);
			varNames.add(pVarName);
			varNames.add(oVarName);
			return new SQLResultSet(query, varNames);
			
		} else if (!arg2.isVar()) {
			String sVarName = arg1.getValue();
			String oVarName = arg3.getValue();
			
			pURI = URI.create(arg2.getValue());
			
			if (Janus.ontBridge.containsDataProperty(pURI)) {
				
				String query = Janus.sqlGenerator.getQueryToGetDPAsserionsOfDP(pURI, sVarName, oVarName);
				List<String> varNames = new Vector<String>(2);
				varNames.add(sVarName);
				varNames.add(oVarName);
				return new SQLResultSet(query, varNames);
				
			} else if (Janus.ontBridge.containsObjectProperty(pURI)) {
				
				String query = Janus.sqlGenerator.getQueryToGetOPAsserionsOfOP(pURI, sVarName, oVarName);
				List<String> varNames = new Vector<String>(2);
				varNames.add(sVarName);
				varNames.add(oVarName);
				return new SQLResultSet(query, varNames);
				
			} else {
				
				System.err.println("No Such a Property.");
				List<String> varNames = new Vector<String>(2);
				varNames.add(sVarName);
				varNames.add(oVarName);
				SQLResultSet resultSet = new SQLResultSet(Janus.sqlGenerator.getQueryToGetEmptyResultSet(varNames), varNames);
				resultSet.setEmptySet();
				return resultSet;
				
			}
			
		} else {
			String sVarName = arg1.getValue();
			String pVarName = arg2.getValue();
			
			if (arg3.isURI())
				oURI = URI.create(arg3.getValue());
			else
				oLit = arg3.getValue();
			
			if (oURI != null) {
				
				String query = Janus.sqlGenerator.getQueryToGetOPAssertionsOfIndividual(oURI, pVarName, sVarName);
				List<String> varNames = new Vector<String>(2);
				varNames.add(pVarName);
				varNames.add(sVarName);
				return new SQLResultSet(query, varNames);
				
			} else {
				
				String query = Janus.sqlGenerator.getQueryToGetDPAssertionsOfTargetLiteral(oLit, pVarName, sVarName);
				List<String> varNames = new Vector<String>(2);
				varNames.add(pVarName);
				varNames.add(sVarName);
				return new SQLResultSet(query, varNames);
				
			}
		}
	}
	
	static SQLResultSet execute3(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		QueryArgument arg3 = args.get(2);
		
		String sVarName = arg1.getValue();
		String pVarName = arg2.getValue();
		String oVarName = arg3.getValue();
		
		String query = Janus.sqlGenerator.getQueryToGetAllPropertyAssertions(pVarName, sVarName, oVarName);
		List<String> varNames = new Vector<String>(3);
		varNames.add(pVarName);
		varNames.add(sVarName);
		varNames.add(oVarName);
		return new SQLResultSet(query, varNames);
	}
}
