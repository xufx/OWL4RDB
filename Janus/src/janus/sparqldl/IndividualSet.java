package janus.sparqldl;

import janus.Janus;
import janus.mapping.OntMapper;
import janus.mapping.metadata.ClassTypes;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

class IndividualSet implements Comparable<IndividualSet> {
	private URI classURI;
	
	private ClassTypes classType;
	
	private String mappedTable;
	
	private String mappedColumn = null; // null if TABLE_CLASS
	private List<String> primaryKeys = null; // null if COLUMN_CLASS
	
	private Set<String> selectSet;
	private Set<String> fromSet;
	private Set<String> joinWhereSet;
	private Set<String> valueWhereSet;
	
	IndividualSet(URI classURI) {
		this.classURI = classURI;
		
		classType = Janus.mappingMetadata.getClassType(classURI);
		
		mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(classURI);
		
		if (classType.equals(ClassTypes.COLUMN_CLASS))
			mappedColumn = Janus.mappingMetadata.getMappedColumnNameToClass(classURI);
		else
			primaryKeys = Janus.cachedDBMetadata.getPrimaryKeys(mappedTable);
		
		initializeSelectSet();
		initializeFromSet();
		initializeWhereSet();
	}
	
	IndividualSet getSubjectIndividualSet() {
		URI subjectClassURI = Janus.mappingMetadata.getMappedClass(mappedTable);
		
		IndividualSet individualSet = new IndividualSet(subjectClassURI);
		individualSet.addAllFromSet(fromSet);
		individualSet.addAllValueWhereSet(valueWhereSet);
		individualSet.addAllJoinWhereSet(joinWhereSet);
		
		return individualSet;
	}
	
	private void initializeSelectSet() {
		selectSet = new ConcurrentSkipListSet<String>();
		
		if (classType.equals(ClassTypes.COLUMN_CLASS)) {
			String selectColumn = mappedTable + "." + mappedColumn;
			
			selectSet.add(selectColumn);
		} else {
			for(String pk: primaryKeys) {
				String selectColumn = mappedTable + "." + pk;
				
				selectSet.add(selectColumn);
			}
		} 
	}
	
	private void initializeWhereSet() {
		joinWhereSet = new ConcurrentSkipListSet<String>();
		valueWhereSet = new ConcurrentSkipListSet<String>();
		
		if (classType.equals(ClassTypes.COLUMN_CLASS)) {
			if (!Janus.cachedDBMetadata.isNotNull(mappedTable, mappedColumn))
				valueWhereSet.add(mappedTable + "." + mappedColumn + " IS NOT NULL"); 
		}
	}
	
	private void initializeFromSet() {
		fromSet = new ConcurrentSkipListSet<String>();
		
		String fromTable = mappedTable;
		
		fromSet.add(fromTable);
	}
	
	void acceptDifferentFromCondition(Individual exclusionMember) {
		if (classType.equals(ClassTypes.COLUMN_CLASS)) {
			String value = exclusionMember.getHasKeyColumnValueAt(0);
			
			String whereCondition = mappedTable + "." + mappedColumn + " <> '" + value + "'";
			
			valueWhereSet.add(whereCondition);
		} else {
			URI individualChangedIntoMyType = exclusionMember.getTypeChangedIndividual(classURI);
			Individual individual = new Individual(individualChangedIntoMyType); 
			
			int columnCount = individual.getNumberOfHasKeyValue();
			for (int i = 0; i < columnCount; i++) {
				String column = individual.getHasKeyColumnNameAt(i);
				String value = individual.getHasKeyColumnValueAt(i);
				
				String whereCondition = mappedTable + "." + column + " <> '" + value + "'";
				
				valueWhereSet.add(whereCondition);
			}					
		}
	}
	
	void acceptSameAsCondition(Individual inclusionMember) {
		if (classType.equals(ClassTypes.COLUMN_CLASS)) {
			String value = inclusionMember.getHasKeyColumnValueAt(0);
			
			String whereCondition = mappedTable + "." + mappedColumn + " = '" + value + "'";
			
			valueWhereSet.add(whereCondition);
		} else {
			URI individualChangedIntoMyType = inclusionMember.getTypeChangedIndividual(classURI);
			Individual individual = new Individual(individualChangedIntoMyType); 
			
			int columnCount = individual.getNumberOfHasKeyValue();
			for (int i = 0; i < columnCount; i++) {
				String column = individual.getHasKeyColumnNameAt(i);
				String value = individual.getHasKeyColumnValueAt(i);
				
				String whereCondition = mappedTable + "." + column + " = '" + value + "'";
				
				valueWhereSet.add(whereCondition);
			}					
		}
	}
	
	public String getMappedTable() {
		return mappedTable;
	}
	
	public String getMappedColumn() {
		return mappedColumn;
	}
	
	List<Triple> getPOTriples() {
		List<Triple> triples = new Vector<Triple>();
		
		Set<URI> sFamilyClasses = getFamilyClasses();

		if (classType.equals(ClassTypes.COLUMN_CLASS)) {
			
			for (URI familyClass: sFamilyClasses) {
				
				IndividualSet familyIndividualSet = new IndividualSet(familyClass);
				familyIndividualSet.intersectWith(this);
				
				String mappedTableOfFamilyClass = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
				String mappedColumnOfFamilyClass = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);
				
				URI dp = Janus.mappingMetadata.getMappedDataProperty(mappedTableOfFamilyClass, mappedColumnOfFamilyClass);
				
				LiteralSet oLiteralSet = new LiteralSet(familyClass, dp);
				oLiteralSet.intersectWith(familyIndividualSet);
				
				Triple triple = new Triple(this, dp, oLiteralSet);
				triples.add(triple);
			}
			
		} else {
			
			for (URI familyClass: sFamilyClasses) {
				
				IndividualSet sFamilyIndividualSet = new IndividualSet(familyClass);
				sFamilyIndividualSet.intersectWith(this);
				
				String mappedTableOfFamilyClass = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
			
				Set<String> columns = Janus.cachedDBMetadata.getColumns(mappedTableOfFamilyClass);

				for (String column: columns) {
					if (Janus.cachedDBMetadata.isKey(mappedTableOfFamilyClass, column)) {
						URI op = Janus.mappingMetadata.getMappedObjectProperty(mappedTableOfFamilyClass, column);

						URI oClsURI = Janus.mappingMetadata.getMappedClass(mappedTableOfFamilyClass, column);
						IndividualSet oIndividualSet = new IndividualSet(oClsURI);
						oIndividualSet.intersectWith(sFamilyIndividualSet);

						Triple triple = new Triple(this, op, oIndividualSet);
						triples.add(triple);
						
						Set<URI> oFamilyClasses = oIndividualSet.getFamilyClasses();
						
						for (URI oFamilyClass: oFamilyClasses) {
							
							IndividualSet oFamilyIndividualSet = new IndividualSet(oFamilyClass);
							if (!Janus.ontBridge.isSubClassOf(oFamilyClass, oClsURI))
								oFamilyIndividualSet.intersectWith(oClsURI);
							oFamilyIndividualSet.intersectWith(oIndividualSet);
							
							triple = new Triple(this, op, oFamilyIndividualSet);
							triples.add(triple);
						}
						
					} else {
						URI dp = Janus.mappingMetadata.getMappedDataProperty(mappedTableOfFamilyClass, column);

						LiteralSet oLiteralSet = new LiteralSet(familyClass, dp);
						oLiteralSet.intersectWith(sFamilyIndividualSet);

						Triple triple = new Triple(this, dp, oLiteralSet);
						triples.add(triple);
					}
				}
			}
		}
			
		return triples;
	}
	
	List<Triple> getSPTriples() {
		List<Triple> triples = new Vector<Triple>();
		
		URI sClsURI = Janus.mappingMetadata.getMappedClass(mappedTable);
		URI op = Janus.mappingMetadata.getMappedObjectProperty(mappedTable, mappedColumn);
		IndividualSet object = this;

		if (classType.equals(ClassTypes.COLUMN_CLASS)) {

			IndividualSet sIndividualSet = new IndividualSet(sClsURI);
			sIndividualSet.intersectWith(this);
			/*sIndividualSet.addAllFromSet(fromSet);
			sIndividualSet.addAllValueWhereSet(valueWhereSet);
			sIndividualSet.addAllJoinWhereSet(joinWhereSet);*/
			
			Set<URI> sFamilyClasses = sIndividualSet.getFamilyClasses();
			
			Set<IndividualSet> familyIndividualSets = new ConcurrentSkipListSet<IndividualSet>();
			
			for (URI familyClass: sFamilyClasses) {
				IndividualSet familyIndividualSet = new IndividualSet(familyClass);
				familyIndividualSet.intersectWith(sIndividualSet);
				familyIndividualSets.add(familyIndividualSet);
				
				Triple triple = new Triple(familyIndividualSet, op, object);
				triples.add(triple);
			}
			
			if (Janus.cachedDBMetadata.isPrimaryKey(mappedTable, mappedColumn)) {
				Set<URI> familyObjectProperties = new ConcurrentSkipListSet<URI>();
				
				for (URI familyClass: sFamilyClasses) {
					String mappedTableToFamilyClass = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
					String mappedColumnToFamilyClass = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(mappedTable, mappedColumn, mappedTableToFamilyClass);
					
					op = Janus.mappingMetadata.getMappedObjectProperty(mappedTableToFamilyClass, mappedColumnToFamilyClass);
					familyObjectProperties.add(op);
				}
				
				for (IndividualSet familyIndividualSet: familyIndividualSets) {
					for (URI familyObjectProperty: familyObjectProperties) {
						Triple triple = new Triple(familyIndividualSet, familyObjectProperty, object);
						triples.add(triple);
					}
				}
			}
			
			Set<URI> oFamilyClasses = getFamilyClasses();
			
			for (URI familyClass: oFamilyClasses) {
				IndividualSet familyIndividualSet = new IndividualSet(familyClass);
				familyIndividualSet.intersectWith(classURI);
				familyIndividualSet.intersectWith(this);
				
				IndividualSet subjectCls = familyIndividualSet.getSubjectIndividualSet();
				
				String mappedTableToFamilyClass = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
				String mappedColumnToFamilyClass = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);
				
				op = Janus.mappingMetadata.getMappedObjectProperty(mappedTableToFamilyClass, mappedColumnToFamilyClass);
				
				Triple triple = new Triple(subjectCls, op, object);
				triples.add(triple);
			}
			
		}
		
		return triples;
	}
	
	void connectValueWhereWithOR(Set<String> anotherConditions) {
		if (valueWhereSet.size() < 1 || anotherConditions.size() < 1) {
			valueWhereSet = new ConcurrentSkipListSet<String>();
			
			return;
		}
		
		String whereCondition = "((";
		for (String condition: valueWhereSet)
			whereCondition = whereCondition + condition + " AND ";
		whereCondition = whereCondition.substring(0, whereCondition.lastIndexOf(" AND")) + ") OR (";
		for (String condition: anotherConditions)
			whereCondition = whereCondition + condition + " AND ";
		whereCondition = whereCondition.substring(0, whereCondition.lastIndexOf(" AND")) + "))";
		
		valueWhereSet = new ConcurrentSkipListSet<String>();
		valueWhereSet.add(whereCondition);
	}
	
	// param: family or subject or object
	void intersectWith(IndividualSet otherIndividualSet) {
		if (otherIndividualSet.getJoinWhereSet().size() == 0) {
			Set<String> familyValueWhereSet = otherIndividualSet.getValueWhereSet();
			
			for (String familyValueWhereCondition: familyValueWhereSet) {
				
				if (classType.equals(ClassTypes.COLUMN_CLASS)) {
					
					if (!Janus.ontBridge.areDisjointWith(classURI, otherIndividualSet.getClassURI())) {
						String operator = familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(" ")+1);
						
						if (operator.equals("IS NOT NULL") && !Janus.ontBridge.isSubClassOf(classURI, otherIndividualSet.getClassURI())) {
							String mappedTableToFamilyIndividual = otherIndividualSet.getMappedTable();
							
							fromSet.add(mappedTableToFamilyIndividual);
							
							String joinTable = mappedTableToFamilyIndividual;
							String joinColumn = Janus.mappingMetadata.getMappedColumnNameToClass(otherIndividualSet.getClassURI());
							
							String whereCondition = mappedTable + "." + mappedColumn + " = " + joinTable + "." + joinColumn;
							
							joinWhereSet.add(whereCondition);
							
							continue;
						}
						
						
						if (operator.equals("IS NOT NULL") && Janus.cachedDBMetadata.isNotNull(mappedTable, mappedColumn))
							continue;
					}
					
					String mappedTableToFamilyIndividual = otherIndividualSet.getMappedTable();
					String conditionColumn = familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(".")+1, familyValueWhereCondition.indexOf(" "));
					
					if (mappedTableToFamilyIndividual.equals(mappedTable)) {
						valueWhereSet.add(familyValueWhereCondition);
					} else {
						fromSet.add(mappedTableToFamilyIndividual);
						
						Set<URI> familyClasses = getFamilyClasses();
						for (URI familyClass: familyClasses) {
							if (Janus.mappingMetadata.getMappedTableNameToClass(familyClass).equals(mappedTableToFamilyIndividual)) {
								String joinTable = mappedTableToFamilyIndividual;
								String joinColumn = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);
								
								String whereCondition = mappedTable + "." + mappedColumn + " = " + joinTable + "." + joinColumn;

								joinWhereSet.add(whereCondition);
							}
						}
						
						String whereCondition = mappedTableToFamilyIndividual + "." + conditionColumn + familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(" "));
						
						valueWhereSet.add(whereCondition);
					}
				} else {
					String mappedTableToFamilyIndividual = otherIndividualSet.getMappedTable();
					//String conditionColumn = familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(".")+1, familyValueWhereCondition.indexOf(" = "));
					String conditionColumn = familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(".")+1, familyValueWhereCondition.indexOf(" "));
					
					if (Janus.cachedDBMetadata.isPrimaryKey(mappedTableToFamilyIndividual, conditionColumn)) {
						String mappedLocalColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(mappedTableToFamilyIndividual, conditionColumn, mappedTable);
						
						//String whereCondition = mappedTable + "." + mappedLocalColumn + familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(" = "));
						String whereCondition = mappedTable + "." + mappedLocalColumn + familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(" "));
						
						valueWhereSet.add(whereCondition);
					} else {
						if (!mappedTable.equals(mappedTableToFamilyIndividual)) {
							fromSet.add(mappedTableToFamilyIndividual);

							String joinTable = mappedTableToFamilyIndividual;

							for (String pk: primaryKeys) {
								String joinColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(mappedTable, pk, joinTable);

								String whereCondition = mappedTable + "." + pk + " = " + joinTable + "." + joinColumn;

								joinWhereSet.add(whereCondition);
							}
						}
						
						valueWhereSet.add(familyValueWhereCondition);
					}
				}
			}
		} else {
			fromSet.addAll(otherIndividualSet.getFromSet());

			String joinTable = otherIndividualSet.getMappedTable();
			
			if (!joinTable.equals(mappedTable)) {
				String joinColumn = null;
				if (classType.equals(ClassTypes.COLUMN_CLASS)) {
					joinColumn = otherIndividualSet.getMappedColumn();

					String whereCondition = mappedTable + "." + mappedColumn + " = " + joinTable + "." + joinColumn;

					joinWhereSet.add(whereCondition);
				}
				else {
					for (String pk: primaryKeys) {
						joinColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(mappedTable, pk, joinTable);

						String whereCondition = mappedTable + "." + pk + " = " + joinTable + "." + joinColumn;

						joinWhereSet.add(whereCondition);
					}
				}
			}

			joinWhereSet.addAll(otherIndividualSet.getJoinWhereSet());
			valueWhereSet.addAll(otherIndividualSet.getValueWhereSet());
		}
	}
	
	void intersectWith(URI familyClassURI) {
		if (Janus.ontBridge.isSubClassOf(classURI, familyClassURI) || classURI.equals(familyClassURI))
			return;
		
		String joinTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClassURI);
		
		fromSet.add(joinTable);
		
		String joinColumn = null;
		if (classType.equals(ClassTypes.COLUMN_CLASS)) {
			joinColumn = Janus.mappingMetadata.getMappedColumnNameToClass(familyClassURI);
			
			String whereCondition = mappedTable + "." + mappedColumn + " = " + joinTable + "." + joinColumn;
			
			joinWhereSet.add(whereCondition);
		}else {
			for (String pk: primaryKeys) {
				joinColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(mappedTable, pk, joinTable);
				
				String whereCondition = mappedTable + "." + pk + " = " + joinTable + "." + joinColumn;
				
				
				joinWhereSet.add(whereCondition);
			}
		}
	}
	
	void acceptPropertyValueVOICondition(URI objProperty, URI objIndividual) {
		String whereCondition = null;
		
		Individual individual = new Individual(objIndividual);
		String value = individual.getHasKeyColumnValueAt(0);
		
		Set<URI> domains = Janus.ontBridge.getObjPropNamedDomains(objProperty);
		
		URI domainClass = null;
		for (URI domain: domains)
			domainClass = domain;
		
		if (domainClass.equals(classURI)) {
			String mappedColumnToObjProperty = Janus.mappingMetadata.getMappedColumnNameToProperty(objProperty);
			
			whereCondition = mappedTable + "." + mappedColumnToObjProperty + " = '" + value + "'";
		} else {
			String mappedTableToObjProperty = Janus.mappingMetadata.getMappedTableNameToProperty(objProperty);
			String mappedColumnToObjProperty = Janus.mappingMetadata.getMappedColumnNameToProperty(objProperty);
			List<String> pks = Janus.cachedDBMetadata.getPrimaryKeys(mappedTableToObjProperty);
			
			if ((pks.contains(mappedColumnToObjProperty) && pks.size() == 1) || 
				(pks.contains(mappedColumnToObjProperty) && pks.size() > 1 && Janus.ontBridge.isSubClassOf(classURI, domainClass))) {
				String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(mappedTableToObjProperty, mappedColumnToObjProperty, mappedTable);
				
				whereCondition = mappedTable + "." + matchedColumn + " = '" + value + "'";
			} else {
				intersectWith(domainClass);
				
				whereCondition = mappedTableToObjProperty + "." + mappedColumnToObjProperty + " = '" + value + "'";
			}
		}
		
		valueWhereSet.add(whereCondition);
	}
	
	void acceptPropertyValueVDLCondition(URI dataProperty, String objLiteral) {
		String whereCondition = null;
		
		String value = objLiteral;
		
		Set<URI> domains = Janus.ontBridge.getNamedDataPropDomains(dataProperty);
		
		URI domainClass = null;
		for (URI domain: domains)
			domainClass = domain;
		
		if (classType.equals(ClassTypes.COLUMN_CLASS)) {
			whereCondition = mappedTable + "." + mappedColumn + " = '" + value + "'";
		} else {
			if (domainClass.equals(classURI)) {
				String mappedColumnToDataProperty = Janus.mappingMetadata.getMappedColumnNameToProperty(dataProperty);
			
				whereCondition = mappedTable + "." + mappedColumnToDataProperty + " = '" + value + "'";
			} else {
				String mappedTableToDataProperty = Janus.mappingMetadata.getMappedTableNameToProperty(dataProperty);
				String mappedColumnToDataProperty = Janus.mappingMetadata.getMappedColumnNameToProperty(dataProperty);
			
				intersectWith(domainClass);
				
				whereCondition = mappedTableToDataProperty + "." + mappedColumnToDataProperty + " = '" + value + "'";
			}
		}
		
		valueWhereSet.add(whereCondition);
	}
	
	URI getClassURI() {
		return classURI;
	}
	
	Set<String> getSelectSet() {
		return selectSet;
	}
	
	Set<String> getFromSet() {
		return fromSet;
	}
	
	Set<String> getJoinWhereSet() {
		return joinWhereSet;
	}
	
	Set<String> getValueWhereSet() {
		return valueWhereSet;
	}
	
	void addValueWhereSet(String condition) {
		valueWhereSet.add(condition);
	}
	
	void addAllSelectSet(Set<String> collection) {
		selectSet.addAll(collection);
	}
	
	void addAllFromSet(Set<String> collection) {
		fromSet.addAll(collection);
	}
	
	void addAllJoinWhereSet(Set<String> collection) {
		joinWhereSet.addAll(collection);
	}
	
	void addAllValueWhereSet(Set<String> collection) {
		valueWhereSet.addAll(collection);
	}
	
	@Override
	public int compareTo(IndividualSet o) {
		return classURI.compareTo(o.getClassURI());
	}
	
	private String generateQuery() {
		String query = "SELECT DISTINCT ";
		
		for (String selectColumn: selectSet)
			query = query + selectColumn + ", ";
		
		query = query.substring(0, query.lastIndexOf(",")) + " FROM ";
		
		for (String fromTable: fromSet)
			query = query + fromTable + ", ";

		query = query.substring(0, query.lastIndexOf(","));
		
		if (valueWhereSet.size() > 0 || joinWhereSet.size() > 0) {
			query = query + " WHERE ";

			for (String whereCondition: joinWhereSet)
				query = query + whereCondition + " AND ";
			
			for (String whereCondition: valueWhereSet)
				query = query + whereCondition + " AND ";

			query = query.substring(0, query.lastIndexOf(" AND"));
		}
		
		return query;
	}
	
	// when this individual set has the only one individual
	URI getTheOneIndividual() {
		String query = generateQuery();//System.out.println(query);
		
		Janus.dbBridge.executeQuery(query);
		
		String table = Janus.mappingMetadata.getMappedTableNameToClass(classURI);
		List<String> primaryKeys = Janus.cachedDBMetadata.getPrimaryKeys(table);
		
		if (Janus.mappingMetadata.getClassType(classURI).equals(ClassTypes.COLUMN_CLASS)) {
			String columnName = Janus.mappingMetadata.getMappedColumnNameToClass(classURI);
			
			List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);
			String cellData = rowData.get(0);
				
			String cellIndividual = Janus.ontBridge.getOntologyID() + "#" + OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + table + "." + columnName + OntMapper.INDIVIDUAL_DELIMITER + cellData;
			
			try {
				return new URI(cellIndividual);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
		} else {
			int pkSize = primaryKeys.size();
			if (pkSize < 2) {
				
				String pk = null;
				for (String primaryKey: primaryKeys) {
					pk = primaryKey;
					break;
				}
				
				List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);
				String pkData = rowData.get(0);
					
				String rowIndividual = Janus.ontBridge.getOntologyID() + "#" + OntMapper.ROW_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + pkSize + OntMapper.INDIVIDUAL_DELIMITER + table + OntMapper.INDIVIDUAL_DELIMITER + pk + OntMapper.INDIVIDUAL_DELIMITER + pkData;
				
				try {
					return new URI(rowIndividual);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				
			} else {
				List<String> columnNames = new Vector<String>(pkSize);
				for(int column = 1; column <= pkSize; column++) {
					String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
					columnNames.add(columnName);
				}
				
				List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);
					
				String rowIndividual = Janus.ontBridge.getOntologyID() + "#" + OntMapper.ROW_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + pkSize + OntMapper.INDIVIDUAL_DELIMITER + table;
				for (String pk: primaryKeys) {
					int pkIndex = columnNames.indexOf(pk);
					String pkData = rowData.get(pkIndex);
					rowIndividual = rowIndividual + OntMapper.INDIVIDUAL_DELIMITER + pk + OntMapper.INDIVIDUAL_DELIMITER + pkData;
				}
					
				try {
					return new URI(rowIndividual);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	int getIndividualCount() {
		String originalQuery = generateQuery();
		
		String query = originalQuery.substring(0, originalQuery.indexOf("DISTINCT")) + "COUNT(";
		query = query + originalQuery.substring(originalQuery.indexOf("DISTINCT"), originalQuery.indexOf(" FROM")) + ") ";
		query = query + originalQuery.substring(originalQuery.indexOf("FROM"));
		
		//System.out.println(query);
		
		Janus.dbBridge.executeQuery(query);
		
		List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);
		
		return Integer.parseInt(rowData.get(0));
	}
	
	void printIndividual(String varName) {
		String query = generateQuery();System.out.println(varName + ": " + query);
		
		Janus.dbBridge.executeQuery(query);
		
		String table = Janus.mappingMetadata.getMappedTableNameToClass(classURI);
		List<String> primaryKeys = Janus.cachedDBMetadata.getPrimaryKeys(table);
		
		if (Janus.mappingMetadata.getClassType(classURI).equals(ClassTypes.COLUMN_CLASS)) {
			String columnName = Janus.mappingMetadata.getMappedColumnNameToClass(classURI);
			int rowCount = Janus.dbBridge.getResultSetRowCount();
			for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
				List<String> rowData = Janus.dbBridge.getResultSetRowAt(rowIndex);
				String cellData = rowData.get(0);
				
				String cellIndividual = Janus.ontBridge.getOntologyID() + "#" + OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + table + "." + columnName + OntMapper.INDIVIDUAL_DELIMITER + cellData;
				System.out.println(varName + ": " + cellIndividual);
			}
		} else {
			int pkSize = primaryKeys.size();
			if (pkSize < 2) {
				
				String pk = null;
				for (String primaryKey: primaryKeys) {
					pk = primaryKey;
					break;
				}
				
				int rowCount = Janus.dbBridge.getResultSetRowCount();
				for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
					List<String> rowData = Janus.dbBridge.getResultSetRowAt(rowIndex);
					String pkData = rowData.get(0);
					
					String rowIndividual = Janus.ontBridge.getOntologyID() + "#" + OntMapper.ROW_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + pkSize + OntMapper.INDIVIDUAL_DELIMITER + table + OntMapper.INDIVIDUAL_DELIMITER + pk + OntMapper.INDIVIDUAL_DELIMITER + pkData;
					System.out.println(varName + ": " + rowIndividual);
				}
			} else {
				List<String> columnNames = new Vector<String>(pkSize);
				for(int column = 1; column <= pkSize; column++) {
					String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
					columnNames.add(columnName);
				}
				
				int rowCount = Janus.dbBridge.getResultSetRowCount();
				for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
					List<String> rowData = Janus.dbBridge.getResultSetRowAt(rowIndex);
					
					String rowIndividual = Janus.ontBridge.getOntologyID() + "#" + OntMapper.ROW_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + pkSize + OntMapper.INDIVIDUAL_DELIMITER + table;
					for (String pk: primaryKeys) {
						int pkIndex = columnNames.indexOf(pk);
						String pkData = rowData.get(pkIndex);
						rowIndividual = rowIndividual + OntMapper.INDIVIDUAL_DELIMITER + pk + OntMapper.INDIVIDUAL_DELIMITER + pkData;
					}
					
					System.out.println(varName + ": " + rowIndividual);
				}
			}
		}
	}
	
	void printIndividual(PrintWriter writer, String varName) {
		String query = generateQuery();System.out.println(varName + ": " + query);
		
		Janus.dbBridge.executeQuery(query);
		
		String table = Janus.mappingMetadata.getMappedTableNameToClass(classURI);
		List<String> primaryKeys = Janus.cachedDBMetadata.getPrimaryKeys(table);
		
		if (Janus.mappingMetadata.getClassType(classURI).equals(ClassTypes.COLUMN_CLASS)) {
			String columnName = Janus.mappingMetadata.getMappedColumnNameToClass(classURI);
			int rowCount = Janus.dbBridge.getResultSetRowCount();
			for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
				List<String> rowData = Janus.dbBridge.getResultSetRowAt(rowIndex);
				String cellData = rowData.get(0);
				
				String cellIndividual = Janus.ontBridge.getOntologyID() + "#" + OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + table + "." + columnName + OntMapper.INDIVIDUAL_DELIMITER + cellData;
				writer.println(varName + ": " + cellIndividual);
			}
		} else {
			int pkSize = primaryKeys.size();
			if (pkSize < 2) {
				
				String pk = null;
				for (String primaryKey: primaryKeys) {
					pk = primaryKey;
					break;
				}
				
				int rowCount = Janus.dbBridge.getResultSetRowCount();
				for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
					List<String> rowData = Janus.dbBridge.getResultSetRowAt(rowIndex);
					String pkData = rowData.get(0);
					
					String rowIndividual = Janus.ontBridge.getOntologyID() + "#" + OntMapper.ROW_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + pkSize + OntMapper.INDIVIDUAL_DELIMITER + table + OntMapper.INDIVIDUAL_DELIMITER + pk + OntMapper.INDIVIDUAL_DELIMITER + pkData;
					writer.println(varName + ": " + rowIndividual);
				}
			} else {
				List<String> columnNames = new Vector<String>(pkSize);
				for(int column = 1; column <= pkSize; column++) {
					String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
					columnNames.add(columnName);
				}
				
				int rowCount = Janus.dbBridge.getResultSetRowCount();
				for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
					List<String> rowData = Janus.dbBridge.getResultSetRowAt(rowIndex);
					
					String rowIndividual = Janus.ontBridge.getOntologyID() + "#" + OntMapper.ROW_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + pkSize + OntMapper.INDIVIDUAL_DELIMITER + table;
					for (String pk: primaryKeys) {
						int pkIndex = columnNames.indexOf(pk);
						String pkData = rowData.get(pkIndex);
						rowIndividual = rowIndividual + OntMapper.INDIVIDUAL_DELIMITER + pk + OntMapper.INDIVIDUAL_DELIMITER + pkData;
					}
					
					writer.println(varName + ": " + rowIndividual);
				}
			}
		}
	}
	
	Set<URI> getFamilyClasses() {
		Set<URI> familyClasses = null;
		
		if (Janus.mappingMetadata.getClassType(classURI).equals(ClassTypes.COLUMN_CLASS)) {
			String rootColumn = Janus.cachedDBMetadata.getRootColumn(mappedTable, mappedColumn);
			
			try {
				URI rootClassURI = new URI(Janus.ontBridge.getOntologyID() + "#" + rootColumn);
				familyClasses = Janus.ontBridge.getAllSubClasses(rootClassURI);
				familyClasses.add(rootClassURI);
				familyClasses.remove(Janus.ontBridge.getOWLNothingURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			String rootTable = Janus.cachedDBMetadata.getRootTable(mappedTable);
			
			try {
				URI rootClassURI = new URI(Janus.ontBridge.getOntologyID() + "#" + rootTable);
				familyClasses = Janus.ontBridge.getAllSubClasses(rootClassURI);
				familyClasses.add(rootClassURI);
				familyClasses.remove(Janus.ontBridge.getOWLNothingURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		return familyClasses;
	}
}
