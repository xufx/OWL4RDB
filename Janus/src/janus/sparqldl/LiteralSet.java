package janus.sparqldl;

import janus.Janus;
import janus.mapping.metadata.ClassTypes;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

class LiteralSet implements Comparable<LiteralSet> {
	private URI classURI; // subject class URI
	private URI dataPropertyURI; // predicate
	
	private ClassTypes classType;
	
	private String mappedTable;
	
	private String mappedColumn = null; // select column for literals
	private List<String> primaryKeys = null; // null if COLUMN_CLASS
	
	private String selectColumn;
	private Set<String> fromSet;
	
	private Set<String> joinWhereSet;
	private Set<String> valueWhereSet;
	
	LiteralSet(URI classURI, URI dataPropertyURI) {
		this.classURI = classURI;
		this.dataPropertyURI = dataPropertyURI;
		
		classType = Janus.mappingMetadata.getClassType(classURI);
		
		mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(classURI);
		
		if (classType.equals(ClassTypes.COLUMN_CLASS))
			mappedColumn = Janus.mappingMetadata.getMappedColumnNameToClass(classURI);
		else {
			mappedColumn = Janus.mappingMetadata.getMappedColumnNameToProperty(dataPropertyURI);
			
			primaryKeys = Janus.cachedDBMetadata.getPrimaryKeys(mappedTable);
		}
		
		initializeSelectColumn();
		initializeFromSet();
		initializeWhereSet();
	}
	
	IndividualSet getSubjectIndividualSet() {
		IndividualSet individualSet = new IndividualSet(classURI);
		individualSet.addAllFromSet(fromSet);
		individualSet.addAllValueWhereSet(valueWhereSet);
		individualSet.addAllJoinWhereSet(joinWhereSet);
		
		return individualSet;
	}
	
	private void initializeSelectColumn() {
		selectColumn = mappedTable + "." + mappedColumn; 
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
	
	List<Triple> getSPTriples() {
		List<Triple> triples = new Vector<Triple>();
		
		URI dp = dataPropertyURI;
		LiteralSet object = this;

		if (classType.equals(ClassTypes.COLUMN_CLASS)) {

			IndividualSet sIndividualSet = new IndividualSet(classURI);
			sIndividualSet.addAllFromSet(fromSet);
			sIndividualSet.addAllValueWhereSet(valueWhereSet);
			sIndividualSet.addAllJoinWhereSet(joinWhereSet);

			Set<URI> familyClasses = sIndividualSet.getFamilyClasses();
			
			Set<IndividualSet> familyIndividualSets = new ConcurrentSkipListSet<IndividualSet>();
			Set<URI> familyDataProperties = new ConcurrentSkipListSet<URI>();
			
			for (URI familyClass: familyClasses) {
				IndividualSet familyIndividualSet = new IndividualSet(familyClass);
				familyIndividualSet.intersectWith(sIndividualSet);
				
				familyIndividualSets.add(familyIndividualSet);
				
				String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
				String mappedColumn = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);
				
				URI familyDataProperty = Janus.mappingMetadata.getMappedDataProperty(mappedTable, mappedColumn);
				familyDataProperties.add(familyDataProperty);
			}
			
			for (IndividualSet familyIndividualSet: familyIndividualSets) {
				for (URI familyDataProperty: familyDataProperties) {
					Triple triple = new Triple(familyIndividualSet, familyDataProperty, object);
					triples.add(triple);
				}
			}

		} else {

			URI sClsURI = Janus.mappingMetadata.getMappedClass(mappedTable);

			IndividualSet sIndividualSet = new IndividualSet(sClsURI);
			sIndividualSet.addAllFromSet(fromSet);
			sIndividualSet.addAllValueWhereSet(valueWhereSet);
			sIndividualSet.addAllJoinWhereSet(joinWhereSet);

			Triple triple = new Triple(sIndividualSet, dp, object);
			triples.add(triple);
			
			Set<URI> familyClasses = sIndividualSet.getFamilyClasses();
			familyClasses.remove(sClsURI);
			
			for (URI familyClass: familyClasses) {
				IndividualSet familyIndividualSet = new IndividualSet(familyClass);
				if (!Janus.ontBridge.isSubClassOf(familyClass, sClsURI))
					familyIndividualSet.intersectWith(sClsURI);
				
				triple = new Triple(familyIndividualSet, dp, object);
				triples.add(triple);
			}

		}
		
		return triples;
	}
	
	URI getClassURI() {
		return classURI;
	}
	
	URI getDataPropertyURI() {
		return dataPropertyURI;
	}
	
	String getSelectColumn() {
		return selectColumn;
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
	public int compareTo(LiteralSet o) {
		return selectColumn.compareTo(o.getSelectColumn());
	}
	
	String generateQuery() {
		String query = "SELECT DISTINCT " + selectColumn + " FROM ";
		
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
	
	public String getMappedTable() {
		return mappedTable;
	}
	
	public String getMappedColumn() {
		return mappedColumn;
	}
	
	void intersectWith(LiteralSet sameSelectColumnLiteralSet) {
		if (sameSelectColumnLiteralSet.getJoinWhereSet().size() == 0) {
			Set<String> familyValueWhereSet = sameSelectColumnLiteralSet.getValueWhereSet();
			
			for (String familyValueWhereCondition: familyValueWhereSet) {
				if (classType.equals(ClassTypes.COLUMN_CLASS)) {
					//String whereCondition = mappedTable + "." + mappedColumn + familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(" = "));
					String whereCondition = mappedTable + "." + mappedColumn + familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(" "));
					
					valueWhereSet.add(whereCondition);
				} else {
					String mappedTableToFamilyIndividual = sameSelectColumnLiteralSet.getMappedTable();
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
			fromSet.addAll(sameSelectColumnLiteralSet.getFromSet());

			String joinTable = sameSelectColumnLiteralSet.getMappedTable();
			
			if (!joinTable.equals(mappedTable)) {
				String joinColumn = null;
				if (classType.equals(ClassTypes.COLUMN_CLASS)) {
					joinColumn = sameSelectColumnLiteralSet.getMappedColumn();

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

			joinWhereSet.addAll(sameSelectColumnLiteralSet.getJoinWhereSet());
			valueWhereSet.addAll(sameSelectColumnLiteralSet.getValueWhereSet());
		}
	}
	
	void intersectWith(IndividualSet familyIndividualSet) {
		if (familyIndividualSet.getJoinWhereSet().size() == 0) {
			Set<String> familyValueWhereSet = familyIndividualSet.getValueWhereSet();
			
			for (String familyValueWhereCondition: familyValueWhereSet) {
				if (classType.equals(ClassTypes.COLUMN_CLASS)) {
					
					if (!Janus.ontBridge.areDisjointWith(classURI, familyIndividualSet.getClassURI())) {
						String operator = familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(" ")+1);
						if (operator.equals("IS NOT NULL") && Janus.cachedDBMetadata.isNotNull(mappedTable, mappedColumn))
							continue;
					}
					
					//String whereCondition = mappedTable + "." + mappedColumn + familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(" = "));
					String whereCondition = mappedTable + "." + mappedColumn + familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(" "));
					
					valueWhereSet.add(whereCondition);
				} else {
					String mappedTableToFamilyIndividual = familyIndividualSet.getMappedTable();
					//String conditionColumn = familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(".")+1, familyValueWhereCondition.indexOf(" = "));
					String conditionColumn = familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(".")+1, familyValueWhereCondition.indexOf(" "));
					
					if (Janus.cachedDBMetadata.isPrimaryKey(mappedTableToFamilyIndividual, conditionColumn)) {
						String mappedLocalColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(mappedTableToFamilyIndividual, conditionColumn, mappedTable);
						
						//String whereCondition = mappedTable + "." + mappedLocalColumn + familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(" = "));
						String whereCondition = mappedTable + "." + mappedLocalColumn + familyValueWhereCondition.substring(familyValueWhereCondition.indexOf(" "));
						
						valueWhereSet.add(whereCondition);
					} else {
						fromSet.add(mappedTableToFamilyIndividual);
						
						String joinTable = mappedTableToFamilyIndividual;
						
						for (String pk: primaryKeys) {
							String joinColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(mappedTable, pk, joinTable);

							String whereCondition = mappedTable + "." + pk + " = " + joinTable + "." + joinColumn;

							joinWhereSet.add(whereCondition);
						}
						
						valueWhereSet.add(familyValueWhereCondition);
					}
				}
			}
		} else {
			fromSet.addAll(familyIndividualSet.getFromSet());

			String joinTable = familyIndividualSet.getMappedTable();
			
			if (!joinTable.equals(mappedTable)) {
				String joinColumn = null;
				if (classType.equals(ClassTypes.COLUMN_CLASS)) {
					joinColumn = familyIndividualSet.getMappedColumn();

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

			joinWhereSet.addAll(familyIndividualSet.getJoinWhereSet());
			valueWhereSet.addAll(familyIndividualSet.getValueWhereSet());
		}
	}
	
	int getLiteralCount() {
		String originalQuery = generateQuery();
		
		String query = originalQuery.substring(0, originalQuery.indexOf("DISTINCT")) + "COUNT(";
		query = query + originalQuery.substring(originalQuery.indexOf("DISTINCT"), originalQuery.indexOf(" FROM")) + ") ";
		query = query + originalQuery.substring(originalQuery.indexOf("FROM"));
		
		//System.out.println(query);
		
		Janus.dbBridge.executeQuery(query);
		
		List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);
		
		return Integer.parseInt(rowData.get(0));
	}
}
