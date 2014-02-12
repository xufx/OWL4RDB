package janus.sparqldl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

import janus.Janus;
import janus.database.SQLResultSet;
import janus.mapping.OntMapper;

class Individual {
	private URI individual;
	private IndividualTypes type;
	private String mappedTableName;
	private String[] hasKeyColumnNames;
	private String[] hasKeyColumnValues;
	private int numberOfHasKeyValue;
	
	Individual(URI individualURI) {
		this.individual = individualURI;
		
		String[] tokens = individual.getFragment().split(OntMapper.INDIVIDUAL_DELIMITER);
		
		if (tokens[0].equals(OntMapper.ROW_INDIVIDUAL_PREFIX)) {
			type = IndividualTypes.FROM_ROW;
			numberOfHasKeyValue = Integer.parseInt(tokens[1]);
			mappedTableName = tokens[2];
			hasKeyColumnNames = new String[numberOfHasKeyValue];
			hasKeyColumnValues = new String[numberOfHasKeyValue];
			for (int i = 3, j = 0; i < tokens.length; i += 2, j++) {
				hasKeyColumnNames[j] = tokens[i];
				hasKeyColumnValues[j] = tokens[i+1];
			} 
		} else {
			type = IndividualTypes.FROM_CELL;
			String[] tableDotColumn = tokens[1].split("\\.");
			mappedTableName = tableDotColumn[0];
			
			hasKeyColumnNames = new String[1];
			hasKeyColumnValues = new String[1];
			
			hasKeyColumnNames[0] = tableDotColumn[1];
			hasKeyColumnValues[0] = tokens[2];
		}
	}
	
	IndividualTypes getType() {
		return type;
	}
	
	String[] getHasKeyColumnValues() {
		return hasKeyColumnValues;
	}
	
	String getHasKeyColumnValueAt(int index) {
		return hasKeyColumnValues[index];
	}
	
	String getHasKeyColumnNameAt(int index) {
		return hasKeyColumnNames[index];
	}
	
	String getMappedTableName() {
		return mappedTableName;
	}
	
	int getNumberOfHasKeyValue() {
		return numberOfHasKeyValue;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Individual))
			return false;
		
		Individual another = (Individual)obj;
		if (individual.equals(another.individual))
			return true;
		
		URI cURI1 = getClassURI();
		URI cURI2 = another.getClassURI();
		
		if (Janus.ontBridge.areDisjointWith(cURI1, cURI2))
			return false;
		
		
		if (type.equals(IndividualTypes.FROM_CELL) || numberOfHasKeyValue < 2) {
			String columnValueOfAnother = another.getHasKeyColumnValueAt(0);
			if (hasKeyColumnValues[0].equals(columnValueOfAnother))
				return true;
			else
				return false;
		}
		
		List<String> values = new Vector<String>();
		for (String v: hasKeyColumnValues)
			values.add(v);

		String[] columnValuesOfAnother = another.getHasKeyColumnValues();
		for (String v: columnValuesOfAnother) {
			if (values.contains(v))  
				values.remove(v);
			else
				return false;
		}

		boolean[] flags = new boolean[numberOfHasKeyValue];
		for (int i = 0; i < flags.length; i++)
			flags[i] = false;

		if (Janus.ontBridge.isSubClassOf(cURI1, cURI2)) {
			for (int i = 0; i < hasKeyColumnNames.length; i++) {
				String pkClassName = mappedTableName + "." + hasKeyColumnNames[i];
				try {
					URI classURI = new URI(individual.getScheme(), individual.getHost(), individual.getPath(), pkClassName);
					int numOfPKOfAnother = another.getNumberOfHasKeyValue();
					for (int j = 0; j < numOfPKOfAnother; j++) {
						String pkClassNameOfAnother = another.getMappedTableName() + "." + another.getHasKeyColumnNameAt(j);

						URI classURIOfAnother = new URI(individual.getScheme(), individual.getHost(), individual.getPath(), pkClassNameOfAnother);

						if (Janus.ontBridge.isSubClassOf(classURI, classURIOfAnother)) {
							String columnValueOfAnother = another.getHasKeyColumnValueAt(j);
							if (hasKeyColumnValues[i].equals(columnValueOfAnother)) {
								flags[i] = true;
								break;
							} else
								return false;
						} else
							continue;
					} // end for2
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			} // end for1
		} else {
			for (int i = 0; i < hasKeyColumnNames.length; i++) {
				String pkClassName = mappedTableName + "." + hasKeyColumnNames[i];
				try {
					URI classURI = new URI(individual.getScheme(), individual.getHost(), individual.getPath(), pkClassName);
					int numOfPKOfAnother = another.getNumberOfHasKeyValue();
					for (int j = 0; j < numOfPKOfAnother; j++) {
						String pkClassNameOfAnother = another.getMappedTableName() + "." + another.getHasKeyColumnNameAt(j);

						URI classURIOfAnother = new URI(individual.getScheme(), individual.getHost(), individual.getPath(), pkClassNameOfAnother);

						if (Janus.ontBridge.isSubClassOf(classURIOfAnother, classURI)) {
							String columnValueOfAnother = another.getHasKeyColumnValueAt(j);
							if (hasKeyColumnValues[i].equals(columnValueOfAnother)) {
								flags[i] = true;
								break;
							} else
								return false;
						} else
							continue;
					} // end for2
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			} // end for1
		}

		for (boolean flag: flags)
			if (!flag)
				return false;

		return true;
	}
	
	boolean isExistentIndividual() {
		String select = "SELECT ";
		for (String columnName: hasKeyColumnNames)
			select = select + columnName + ", ";
		select = select.substring(0, select.lastIndexOf(", "));
		
		String from = " FROM " + mappedTableName;
		
		String where = " WHERE ";
		for (int i = 0; i < hasKeyColumnNames.length; i++)
			where = where + hasKeyColumnNames[i] + " = '" + hasKeyColumnValues[i] + "' AND ";
		where = where.substring(0, where.lastIndexOf("AND"));
		
		String query = select + from + where;
		
		Janus.dbBridge.executeQuery(query);
		if (Janus.dbBridge.getResultSetRowCount() > 0)
			return true;
		
		return false;
	}
	
	URI getClassURI() {
		URI classURI = null;
		if (type.equals(IndividualTypes.FROM_ROW)) {
			try {
				classURI = new URI(individual.getScheme(), individual.getHost(), individual.getPath(), mappedTableName);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
		} else {
			try {
				classURI = new URI(individual.getScheme(), individual.getHost(), individual.getPath(), mappedTableName + "." + hasKeyColumnNames[0]);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return classURI;
	}
	
	String getClassNameOf() {
		String className = null;
		if (type.equals(IndividualTypes.FROM_ROW))
			className = mappedTableName;
		else
			className = mappedTableName + "." + hasKeyColumnNames[0];
		
		return className;
	}
	
	URI getTypeChangedIndividual(URI familyClass) {
		String fragment = "";
		if (type.equals(IndividualTypes.FROM_CELL))
			fragment = OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + familyClass.getFragment() + OntMapper.INDIVIDUAL_DELIMITER + hasKeyColumnValues[0];
		else if (numberOfHasKeyValue < 2) {
			String targetClassName = familyClass.getFragment();
			List<String> pks = Janus.cachedDBMetadata.getPrimaryKeys(targetClassName);
			String primaryKey = null;
			for (String pk: pks) {
				primaryKey = pk;
				break;
			}
			fragment = OntMapper.ROW_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + "1" + OntMapper.INDIVIDUAL_DELIMITER + targetClassName + OntMapper.INDIVIDUAL_DELIMITER + primaryKey + OntMapper.INDIVIDUAL_DELIMITER + hasKeyColumnValues[0];
		} else {
			// transformation within family classes which are derived from tables with primary key comprised of multiple columns
			String targetClassName = familyClass.getFragment();
			String targetTableName = targetClassName;
			String rootTableName = Janus.cachedDBMetadata.getRootTable(targetTableName);
			
			fragment = OntMapper.ROW_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + String.valueOf(numberOfHasKeyValue) + OntMapper.INDIVIDUAL_DELIMITER + targetClassName;
			
			List<String> pks = Janus.cachedDBMetadata.getPrimaryKeys(targetClassName);
			for (String pk: pks) {
				String superTable = targetTableName;
				String superColumn = pk;
				while (!superTable.equals(rootTableName)) {
					String superTableDotColumn = Janus.cachedDBMetadata.getSuperColumn(superTable, superColumn);
					superTable = superTableDotColumn.substring(0, superTableDotColumn.indexOf("."));
					superColumn = superTableDotColumn.substring(superTableDotColumn.indexOf(".")+1);
				}
				String matchedRootColumnForTarget = superColumn;
				
				for (int i = 0; i < numberOfHasKeyValue; i++) {
					superTable = mappedTableName;
					superColumn = hasKeyColumnNames[i];
					while (!superTable.equals(rootTableName)) {
						String superTableDotColumn = Janus.cachedDBMetadata.getSuperColumn(superTable, superColumn);
						superTable = superTableDotColumn.substring(0, superTableDotColumn.indexOf("."));
						superColumn = superTableDotColumn.substring(superTableDotColumn.indexOf(".")+1);
					}
					String matchedRootColumnForMe = superColumn;
					
					if (matchedRootColumnForTarget.equals(matchedRootColumnForMe)) {
						fragment = fragment + OntMapper.INDIVIDUAL_DELIMITER + pk + OntMapper.INDIVIDUAL_DELIMITER + hasKeyColumnValues[i];
						break;
					}
				}
			}
		}
		
		
		URI typeChangedIndividual = null;
		try {
			typeChangedIndividual = new URI(individual.getScheme(), individual.getHost(), individual.getPath(), fragment);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
			
		return typeChangedIndividual;
	}
	
	Set<URI> getFamilyClasses() {
		Set<URI> familyClasses = null;
		
		if (type.equals(IndividualTypes.FROM_CELL)) {
			String rootColumn = Janus.cachedDBMetadata.getRootColumn(mappedTableName, hasKeyColumnNames[0]);
			
			try {
				URI rootClassURI = new URI(Janus.ontBridge.getOntologyID() + "#" + rootColumn);
				familyClasses = Janus.ontBridge.getAllSubClasses(rootClassURI);
				familyClasses.add(rootClassURI);
				familyClasses.remove(Janus.ontBridge.getOWLNothingURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			String rootTable = Janus.cachedDBMetadata.getRootTable(mappedTableName);
			
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
	
	String getValueOfTheColumn(String column) {
		String query = "SELECT " + column + " FROM " + mappedTableName + " WHERE ";
		for (int i = 0; i < numberOfHasKeyValue; i++)
			query = query + hasKeyColumnNames[i] + " = '" + hasKeyColumnValues[i] + "' AND ";
		
		query = query.substring(0, query.lastIndexOf("AND"));
		
		Janus.dbBridge.executeQuery(query);
		
		List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);

		return rowData.get(0);
	}
	
	URI getObjectIndividual(String column) {
		if (Janus.cachedDBMetadata.isKey(mappedTableName, column)) {
			String value = getValueOfTheColumn(column);
			if (value != null) {
				String cellIndividual = OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + mappedTableName + "." + column + OntMapper.INDIVIDUAL_DELIMITER + value;
				
				try {
					URI objectIndividual = new URI(Janus.ontBridge.getOntologyID() + "#" + cellIndividual);
					
					return objectIndividual;
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			
			return null;
		}
		
		return null;
	}
	
	Set<URI> findDataPropertyThatHoldsTheValue(String value) {
		Set<URI> dataProperties = new ConcurrentSkipListSet<URI>();
		
		Set<String> nonKeys = new ConcurrentSkipListSet<String>();
		
		Set<String> columns = Janus.cachedDBMetadata.getColumns(mappedTableName);
		
		for (String column: columns)
			if (!Janus.cachedDBMetadata.isKey(mappedTableName, column))
				nonKeys.add(column);
		
		String query = "SELECT ";
		for (String column: nonKeys)
			query = query + column + ", ";
		
		query = query.substring(0, query.lastIndexOf(",")) + " FROM " + mappedTableName + " WHERE ";
		
		for (int i = 0; i < numberOfHasKeyValue; i++)
			query = query + hasKeyColumnNames[i] + " = '" + hasKeyColumnValues[i] + "' AND ";
		
		query = query.substring(0, query.lastIndexOf("AND"));
		
		Janus.dbBridge.executeQuery(query);
		
		List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);

		int count = rowData.size();
		for (int i = 0; i < count; i++) {
			String cellData = rowData.get(i);
			if (cellData != null && cellData.equals(value)) {
				String columnName = Janus.dbBridge.getResultSetColumnLabel(i+1);

				URI dataProperty = Janus.mappingMetadata.getMappedDataProperty(mappedTableName, columnName);

				dataProperties.add(dataProperty);
			}
		}
		
		return dataProperties;
	}
	
	Set<IndividualSet> getAllObjectIndividuals() {
		Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

		URI sClassURI = getClassURI();

		Set<String> keyColumns = Janus.cachedDBMetadata.getKeyColumns(mappedTableName);
		List<String> primaryKeyColumns = Janus.cachedDBMetadata.getPrimaryKeys(mappedTableName);
		Set<String> nonPKKeyColumns = new ConcurrentSkipListSet<String>();
		nonPKKeyColumns.addAll(keyColumns);
		nonPKKeyColumns.removeAll(primaryKeyColumns);

		try {
			for (int i = 0; i < hasKeyColumnNames.length; i++) {

				URI mappedClsURI = Janus.mappingMetadata.getMappedClass(mappedTableName, hasKeyColumnNames[i]);

				IndividualSet individualSet = new IndividualSet(mappedClsURI);

				String cellIndividual = OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + mappedTableName + "." + hasKeyColumnNames[i] + OntMapper.INDIVIDUAL_DELIMITER + hasKeyColumnValues[i];
				URI objectIndividualURI = new URI(Janus.ontBridge.getOntologyID() + "#" + cellIndividual);
				Individual objectIndividual = new Individual(objectIndividualURI);

				individualSet.acceptSameAsCondition(objectIndividual);

				individualSets.add(individualSet);

				Set<URI> familyClasses = objectIndividual.getFamilyClasses();
				familyClasses.remove(mappedClsURI);

				for (URI familyClass: familyClasses) {
					individualSet = new IndividualSet(familyClass);

					individualSet.acceptSameAsCondition(objectIndividual);

					individualSets.add(individualSet);
				}
			}

			String query = "SELECT ";
			for (String nonPKKeyColumn: nonPKKeyColumns)
				query = query + nonPKKeyColumn + ", ";

			query = query.substring(0, query.lastIndexOf(",")) + " FROM " + mappedTableName + " WHERE ";

			for (int i = 0; i < hasKeyColumnNames.length; i++)
				query = query + hasKeyColumnNames[i] + " = '" + hasKeyColumnValues[i] + "' AND ";
			query = query.substring(0, query.lastIndexOf("AND"));

			Janus.dbBridge.executeQuery(query);

			int columnCount = Janus.dbBridge.getResultSetColumnCount();
			List<String> columnNames = new Vector<String>(columnCount);

			for(int column = 1; column <= columnCount; column++) {
				String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
				columnNames.add(columnName);
			}

			List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);

			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				String columnName = columnNames.get(columnIndex);
				String cellData = rowData.get(columnIndex);

				if (cellData != null) {
					URI mappedClsURI = Janus.mappingMetadata.getMappedClass(mappedTableName, columnName);

					IndividualSet individualSet = new IndividualSet(mappedClsURI);

					String cellIndividual = OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + mappedTableName + "." + columnName + OntMapper.INDIVIDUAL_DELIMITER + cellData;
					URI objectIndividualURI = new URI(Janus.ontBridge.getOntologyID() + "#" + cellIndividual);
					Individual objectIndividual = new Individual(objectIndividualURI);

					individualSet.acceptSameAsCondition(objectIndividual);

					individualSets.add(individualSet);

					Set<URI> familyClasses = objectIndividual.getFamilyClasses();
					familyClasses.remove(mappedClsURI);

					for (URI familyClass: familyClasses) {
						individualSet = new IndividualSet(familyClass);

						individualSet.acceptSameAsCondition(objectIndividual);

						individualSets.add(individualSet);
					}
				}
			}

			Set<URI> familyClasses = getFamilyClasses();
			familyClasses.remove(sClassURI);

			for (URI familyClass: familyClasses) {
				String familyTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
				URI familyIndividualURI = getTypeChangedIndividual(familyClass);
				Individual familyIndividual = new Individual(familyIndividualURI);

				keyColumns = Janus.cachedDBMetadata.getKeyColumns(familyTable);
				primaryKeyColumns = Janus.cachedDBMetadata.getPrimaryKeys(familyTable);
				nonPKKeyColumns = new ConcurrentSkipListSet<String>();
				nonPKKeyColumns.addAll(keyColumns);
				nonPKKeyColumns.removeAll(primaryKeyColumns);

				if (Janus.ontBridge.isSubClassOf(getClassURI(), familyClass)) {
					query = "SELECT ";
					for (String nonPKKeyColumn: nonPKKeyColumns)
						query = query + nonPKKeyColumn + ", ";

					query = query.substring(0, query.lastIndexOf(",")) + " FROM " + mappedTableName + " WHERE ";

					for (int i = 0; i < hasKeyColumnNames.length; i++)
						query = query + hasKeyColumnNames[i] + " = '" + hasKeyColumnValues[i] + "' AND ";
					query = query.substring(0, query.lastIndexOf("AND"));

					Janus.dbBridge.executeQuery(query);

					columnCount = Janus.dbBridge.getResultSetColumnCount();
					columnNames = new Vector<String>(columnCount);

					for(int column = 1; column <= columnCount; column++) {
						String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
						columnNames.add(columnName);
					}

					rowData = Janus.dbBridge.getResultSetRowAt(1);

					for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
						String columnName = columnNames.get(columnIndex);
						String cellData = rowData.get(columnIndex);

						if (cellData != null) {
							URI mappedClsURI = Janus.mappingMetadata.getMappedClass(familyTable, columnName);

							IndividualSet individualSet = new IndividualSet(mappedClsURI);

							String cellIndividual = OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + familyTable + "." + columnName + OntMapper.INDIVIDUAL_DELIMITER + cellData;
							URI objectIndividualURI = new URI(Janus.ontBridge.getOntologyID() + "#" + cellIndividual);
							Individual objectIndividual = new Individual(objectIndividualURI);

							individualSet.acceptSameAsCondition(objectIndividual);

							individualSets.add(individualSet);

							Set<URI> objectFamilyClasses = objectIndividual.getFamilyClasses();
							objectFamilyClasses.remove(mappedClsURI);

							for (URI objectFamilyClass: objectFamilyClasses) {
								individualSet = new IndividualSet(objectFamilyClass);

								individualSet.acceptSameAsCondition(objectIndividual);

								individualSets.add(individualSet);
							}
						}
					}
				} else {
					query = "SELECT * FROM " + familyTable + " WHERE ";

					for (int i = 0; i < hasKeyColumnNames.length; i++)
						query = query + familyIndividual.getHasKeyColumnNameAt(i) + " = '" + familyIndividual.getHasKeyColumnValueAt(i) + "' AND ";
					query = query.substring(0, query.lastIndexOf("AND"));

					Janus.dbBridge.executeQuery(query);

					if (Janus.dbBridge.getResultSetRowCount() > 0) {

						columnCount = Janus.dbBridge.getResultSetColumnCount();
						columnNames = new Vector<String>(columnCount);

						for(int column = 1; column <= columnCount; column++) {
							String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
							columnNames.add(columnName);
						}

						rowData = Janus.dbBridge.getResultSetRowAt(1);

						for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
							String columnName = columnNames.get(columnIndex);
							String cellData = rowData.get(columnIndex);

							if (cellData != null) {
								if (Janus.cachedDBMetadata.isKey(familyTable, columnName) && !Janus.cachedDBMetadata.isPrimaryKey(familyTable, columnName)) {
									URI mappedClsURI = Janus.mappingMetadata.getMappedClass(familyTable, columnName);

									IndividualSet individualSet = new IndividualSet(mappedClsURI);

									String cellIndividual = OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + familyTable + "." + columnName + OntMapper.INDIVIDUAL_DELIMITER + cellData;
									URI objectIndividualURI = new URI(Janus.ontBridge.getOntologyID() + "#" + cellIndividual);

									Individual objectIndividual = new Individual(objectIndividualURI);

									individualSet.acceptSameAsCondition(objectIndividual);

									individualSets.add(individualSet);

									Set<URI> objectFamilyClasses = objectIndividual.getFamilyClasses();
									objectFamilyClasses.remove(mappedClsURI);

									for (URI objectFamilyClass: objectFamilyClasses) {
										individualSet = new IndividualSet(objectFamilyClass);

										individualSet.acceptSameAsCondition(objectIndividual);

										individualSets.add(individualSet);
									}
								}
							}
						}
					}
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return individualSets;
	}
	
	Set<LiteralSet> getAllObjectLiterals() {
		Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();
		
		URI sClassURI = getClassURI();
		
		if (getType().equals(IndividualTypes.FROM_CELL)) {
			URI predicate = Janus.mappingMetadata.getMappedDataProperty(mappedTableName, hasKeyColumnNames[0]);
			
			LiteralSet literalSet = new LiteralSet(sClassURI, predicate);
			literalSet.acceptSameAsCondition(this);
			
			literalSets.add(literalSet);
			
			Set<URI> familyClasses = getFamilyClasses();
			familyClasses.remove(getClassURI());
			
			for (URI familyClass: familyClasses) {
				if (Janus.ontBridge.isSubClassOf(getClassURI(), familyClass)) {
					String familyTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
					String familyColumn = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);
					
					URI familyPredicate = Janus.mappingMetadata.getMappedDataProperty(familyTable, familyColumn);
					
					literalSet = new LiteralSet(familyClass, familyPredicate);
					literalSet.acceptSameAsCondition(this);
					
					literalSets.add(literalSet);
				} else {
					URI familyIndividualURI = getTypeChangedIndividual(familyClass);
					Individual familyIndividual = new Individual(familyIndividualURI);
					
					if (familyIndividual.isExistentIndividual()) {
						String familyTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
						String familyColumn = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);
						
						URI familyPredicate = Janus.mappingMetadata.getMappedDataProperty(familyTable, familyColumn);
						
						literalSet = new LiteralSet(familyClass, familyPredicate);
						literalSet.acceptSameAsCondition(this);
						
						literalSets.add(literalSet);
					}
				}
			}
		} else {
			Set<String> nonKeyColumns = Janus.cachedDBMetadata.getNonKeyColumns(mappedTableName);
			
			Set<String> noNullColumns = Janus.cachedDBMetadata.getNoNullColumns(mappedTableName);
			
			Set<String> noNullNonKeyColumns = new ConcurrentSkipListSet<String>();
			noNullNonKeyColumns.addAll(noNullColumns);
			noNullNonKeyColumns.retainAll(nonKeyColumns);
			
			for (String noNullNonKeyColumn: noNullNonKeyColumns) {
				URI mappedDataProperty = Janus.mappingMetadata.getMappedDataProperty(mappedTableName, noNullNonKeyColumn);
					
				LiteralSet literalSet = new LiteralSet(sClassURI, mappedDataProperty);
				literalSet.acceptSameAsCondition(this);

				literalSets.add(literalSet);
			}
			
			Set<String> nullableColumns = Janus.cachedDBMetadata.getNullableColumns(mappedTableName);
			
			Set<String> nullableNonKeyColumns = new ConcurrentSkipListSet<String>();
			nullableNonKeyColumns.addAll(nullableColumns);
			nullableNonKeyColumns.retainAll(nonKeyColumns);
			
			String query = "SELECT ";
			for (String nullableNonKeyColumn: nullableNonKeyColumns)
				query = query + nullableNonKeyColumn + ", ";
			
			query = query.substring(0, query.lastIndexOf(",")) + " FROM " + mappedTableName + " WHERE ";
			
			for (int i = 0; i < hasKeyColumnNames.length; i++)
				query = query + hasKeyColumnNames[i] + " = '" + hasKeyColumnValues[i] + "' AND ";
			query = query.substring(0, query.lastIndexOf("AND"));

			Janus.dbBridge.executeQuery(query);
			
			int columnCount = Janus.dbBridge.getResultSetColumnCount();
			List<String> columnNames = new Vector<String>(columnCount);

			for(int column = 1; column <= columnCount; column++) {
				String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
				columnNames.add(columnName);
			}

			List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);

			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				String columnName = columnNames.get(columnIndex);
				String cellData = rowData.get(columnIndex);

				if (cellData != null) {
					URI predicate = Janus.mappingMetadata.getMappedDataProperty(mappedTableName, columnName);

					LiteralSet literalSet = new LiteralSet(sClassURI, predicate);
					literalSet.acceptSameAsCondition(this);

					literalSets.add(literalSet);
				}
			}
			
			Set<URI> familyClasses = getFamilyClasses();
			familyClasses.remove(sClassURI);
			
			for (URI familyClass: familyClasses) {
				String familyTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
				URI familyIndividualURI = getTypeChangedIndividual(familyClass);
				Individual familyIndividual = new Individual(familyIndividualURI);
				
				if (Janus.ontBridge.isSubClassOf(getClassURI(), familyClass)) {
					nonKeyColumns = Janus.cachedDBMetadata.getNonKeyColumns(familyTable);
					
					noNullColumns = Janus.cachedDBMetadata.getNoNullColumns(familyTable);
					
					noNullNonKeyColumns = new ConcurrentSkipListSet<String>();
					noNullNonKeyColumns.addAll(noNullColumns);
					noNullNonKeyColumns.retainAll(nonKeyColumns);
					
					for (String noNullNonKeyColumn: noNullNonKeyColumns) {
						URI mappedDataProperty = Janus.mappingMetadata.getMappedDataProperty(familyTable, noNullNonKeyColumn);
							
						LiteralSet literalSet = new LiteralSet(familyClass, mappedDataProperty);
						literalSet.acceptSameAsCondition(this);

						literalSets.add(literalSet);
					}
					
					nullableColumns = Janus.cachedDBMetadata.getNullableColumns(familyTable);
					
					nullableNonKeyColumns = new ConcurrentSkipListSet<String>();
					nullableNonKeyColumns.addAll(nullableColumns);
					nullableNonKeyColumns.retainAll(nonKeyColumns);
					
					query = "SELECT ";
					for (String nullableNonKeyColumn: nullableNonKeyColumns)
						query = query + nullableNonKeyColumn + ", ";
					
					query = query.substring(0, query.lastIndexOf(",")) + " FROM " + mappedTableName + " WHERE ";
					
					for (int i = 0; i < hasKeyColumnNames.length; i++)
						query = query + familyIndividual.getHasKeyColumnNameAt(i) + " = '" + familyIndividual.getHasKeyColumnValueAt(i) + "' AND ";
					query = query.substring(0, query.lastIndexOf("AND"));

					Janus.dbBridge.executeQuery(query);
					
					columnCount = Janus.dbBridge.getResultSetColumnCount();
					columnNames = new Vector<String>(columnCount);

					for(int column = 1; column <= columnCount; column++) {
						String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
						columnNames.add(columnName);
					}

					rowData = Janus.dbBridge.getResultSetRowAt(1);

					for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
						String columnName = columnNames.get(columnIndex);
						String cellData = rowData.get(columnIndex);

						if (cellData != null) {
							URI predicate = Janus.mappingMetadata.getMappedDataProperty(familyTable, columnName);

							LiteralSet literalSet = new LiteralSet(familyClass, predicate);
							literalSet.acceptSameAsCondition(this);

							literalSets.add(literalSet);
						}
					}
				} else {
					query = "SELECT * FROM " + familyTable + " WHERE ";
					
					for (int i = 0; i < hasKeyColumnNames.length; i++)
						query = query + familyIndividual.getHasKeyColumnNameAt(i) + " = '" + familyIndividual.getHasKeyColumnValueAt(i) + "' AND ";
					query = query.substring(0, query.lastIndexOf("AND"));
					
					Janus.dbBridge.executeQuery(query);
					
					if (Janus.dbBridge.getResultSetRowCount() > 0) {

						columnCount = Janus.dbBridge.getResultSetColumnCount();
						columnNames = new Vector<String>(columnCount);

						for(int column = 1; column <= columnCount; column++) {
							String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
							columnNames.add(columnName);
						}


						rowData = Janus.dbBridge.getResultSetRowAt(1);

						for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
							String columnName = columnNames.get(columnIndex);
							String cellData = rowData.get(columnIndex);

							if (cellData != null) {
								if (!Janus.cachedDBMetadata.isKey(familyTable, columnName)) {
									URI predicate = Janus.mappingMetadata.getMappedDataProperty(familyTable, columnName);

									LiteralSet literalSet = new LiteralSet(familyClass, predicate);
									literalSet.acceptSameAsCondition(this);

									literalSets.add(literalSet);
								}
							}
						}
					}
				}
			}
		}

		return literalSets;
	}
	
	Set<URI> getAllProperties() {
		Set<URI> properties = new ConcurrentSkipListSet<URI>();
		
		if (getType().equals(IndividualTypes.FROM_CELL)) {
			URI predicate = Janus.mappingMetadata.getMappedDataProperty(mappedTableName, hasKeyColumnNames[0]);
			
			properties.add(predicate);
			
			Set<URI> familyClasses = getFamilyClasses();
			familyClasses.remove(getClassURI());
			
			for (URI familyClass: familyClasses) {
				if (Janus.ontBridge.isSubClassOf(getClassURI(), familyClass)) {
					String familyTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
					String familyColumn = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);
					
					URI familyPredicate = Janus.mappingMetadata.getMappedDataProperty(familyTable, familyColumn);
					
					properties.add(familyPredicate);
				} else {
					URI familyIndividualURI = getTypeChangedIndividual(familyClass);
					Individual familyIndividual = new Individual(familyIndividualURI);
					
					if (familyIndividual.isExistentIndividual()) {
						String familyTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
						String familyColumn = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);
						
						URI familyPredicate = Janus.mappingMetadata.getMappedDataProperty(familyTable, familyColumn);
						
						properties.add(familyPredicate);
					}
				}
			}
		} else {
			Set<String> noNullColumns = Janus.cachedDBMetadata.getNoNullColumns(mappedTableName);
			
			for (String noNullColumn: noNullColumns) {
				if (Janus.cachedDBMetadata.isKey(mappedTableName, noNullColumn)) {
					URI mappedObjProperty = Janus.mappingMetadata.getMappedObjectProperty(mappedTableName, noNullColumn);
					
					properties.add(mappedObjProperty);
				} else {
					URI mappedDataProperty = Janus.mappingMetadata.getMappedDataProperty(mappedTableName, noNullColumn);
					
					properties.add(mappedDataProperty);
				}
			}
			
			Set<String> nullableColumns = Janus.cachedDBMetadata.getNullableColumns(mappedTableName);
			
			String query = "SELECT ";
			for (String nullableColumn: nullableColumns)
				query = query + nullableColumn + ", ";
			
			query = query.substring(0, query.lastIndexOf(",")) + " FROM " + mappedTableName + " WHERE ";
			
			for (int i = 0; i < hasKeyColumnNames.length; i++)
				query = query + hasKeyColumnNames[i] + " = '" + hasKeyColumnValues[i] + "' AND ";
			query = query.substring(0, query.lastIndexOf("AND"));

			Janus.dbBridge.executeQuery(query);

			int columnCount = Janus.dbBridge.getResultSetColumnCount();
			List<String> columnNames = new Vector<String>(columnCount);

			for(int column = 1; column <= columnCount; column++) {
				String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
				columnNames.add(columnName);
			}

			List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);

			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				String columnName = columnNames.get(columnIndex);
				String cellData = rowData.get(columnIndex);

				if (cellData != null) {
					if (Janus.cachedDBMetadata.isKey(mappedTableName, columnName)) {
						URI predicate = Janus.mappingMetadata.getMappedObjectProperty(mappedTableName, columnName);
							
						properties.add(predicate);
							
					} else {
						URI predicate = Janus.mappingMetadata.getMappedDataProperty(mappedTableName, columnName);

						properties.add(predicate);
					}
				}
			}
			
			Set<URI> familyClasses = getFamilyClasses();
			familyClasses.remove(getClassURI());
			
			for (URI familyClass: familyClasses) {
				String familyTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
				URI familyIndividualURI = getTypeChangedIndividual(familyClass);
				Individual familyIndividual = new Individual(familyIndividualURI);
				
				if (Janus.ontBridge.isSubClassOf(getClassURI(), familyClass)) {
					noNullColumns = Janus.cachedDBMetadata.getNoNullColumns(familyTable);
					
					for (String noNullColumn: noNullColumns) {
						if (Janus.cachedDBMetadata.isKey(familyTable, noNullColumn)) {
							URI mappedObjProperty = Janus.mappingMetadata.getMappedObjectProperty(familyTable, noNullColumn);
							
							properties.add(mappedObjProperty);
						} else {
							URI mappedDataProperty = Janus.mappingMetadata.getMappedDataProperty(familyTable, noNullColumn);
							
							properties.add(mappedDataProperty);
						}
					}
					
					nullableColumns = Janus.cachedDBMetadata.getNullableColumns(familyTable);
					
					query = "SELECT ";
					for (String nullableColumn: nullableColumns)
						query = query + nullableColumn + ", ";
					
					query = query.substring(0, query.lastIndexOf(",")) + " FROM " + familyTable + " WHERE ";
					
					for (int i = 0; i < hasKeyColumnNames.length; i++)
						query = query + familyIndividual.getHasKeyColumnNameAt(i) + " = '" + familyIndividual.getHasKeyColumnValueAt(i) + "' AND ";
					query = query.substring(0, query.lastIndexOf("AND"));
					
					Janus.dbBridge.executeQuery(query);

					columnCount = Janus.dbBridge.getResultSetColumnCount();
					columnNames = new Vector<String>(columnCount);

					for(int column = 1; column <= columnCount; column++) {
						String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
						columnNames.add(columnName);
					}

					rowData = Janus.dbBridge.getResultSetRowAt(1);

					for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
						String columnName = columnNames.get(columnIndex);
						String cellData = rowData.get(columnIndex);

						if (cellData != null) {
							if (Janus.cachedDBMetadata.isKey(familyTable, columnName)) {
								URI predicate = Janus.mappingMetadata.getMappedObjectProperty(familyTable, columnName);
									
								properties.add(predicate);
									
							} else {
								URI predicate = Janus.mappingMetadata.getMappedDataProperty(familyTable, columnName);

								properties.add(predicate);
							}
						}
					}
				} else {
					query = "SELECT * FROM " + familyTable + " WHERE ";
					
					for (int i = 0; i < hasKeyColumnNames.length; i++)
						query = query + familyIndividual.getHasKeyColumnNameAt(i) + " = '" + familyIndividual.getHasKeyColumnValueAt(i) + "' AND ";
					query = query.substring(0, query.lastIndexOf("AND"));
					
					Janus.dbBridge.executeQuery(query);
					
					if (Janus.dbBridge.getResultSetRowCount() > 0) {

						columnCount = Janus.dbBridge.getResultSetColumnCount();
						columnNames = new Vector<String>(columnCount);

						for(int column = 1; column <= columnCount; column++) {
							String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
							columnNames.add(columnName);
						}


						rowData = Janus.dbBridge.getResultSetRowAt(1);

						for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
							String columnName = columnNames.get(columnIndex);
							String cellData = rowData.get(columnIndex);

							if (cellData != null) {
								if (Janus.cachedDBMetadata.isKey(familyTable, columnName)) {
									URI predicate = Janus.mappingMetadata.getMappedObjectProperty(familyTable, columnName);

									properties.add(predicate);

								} else {
									URI predicate = Janus.mappingMetadata.getMappedDataProperty(familyTable, columnName);

									properties.add(predicate);
								}
							}
						}
					}
				}
			}
		}

		return properties;
	}
	
	List<Triple> getSPTriples() {
		List<Triple> triples = new Vector<Triple>();
		
		try {
			URI sClassURI = Janus.mappingMetadata.getMappedClass(mappedTableName);

			URI predicate = Janus.mappingMetadata.getMappedObjectProperty(mappedTableName, hasKeyColumnNames[0]);
			URI object = individual;

			List<String> primaryKeys = Janus.cachedDBMetadata.getPrimaryKeys(mappedTableName);
			String query = "SELECT DISTINCT ";
			for (String pk: primaryKeys)
				query = query + pk + ", ";
			query = query.substring(0, query.lastIndexOf(",")) + " FROM " + mappedTableName + " WHERE " + hasKeyColumnNames[0] + " = " + hasKeyColumnValues[0];

			SQLResultSet rs = Janus.dbBridge.executeQuery(query);

			int columnCount = rs.getResultSetColumnCount();
			List<String> columnNames = new Vector<String>(columnCount);

			for(int column = 1; column <= columnCount; column++) {
				String columnName = rs.getResultSetColumnLabel(column);
				columnNames.add(columnName);
			}

			int rowCount = rs.getResultSetRowCount();
			for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
				List<String> rowData = rs.getResultSetRowAt(rowIndex);

				String tableClass = mappedTableName;
				String rowIndividual = OntMapper.ROW_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + primaryKeys.size() + OntMapper.INDIVIDUAL_DELIMITER + tableClass;
				for (String pk: primaryKeys) {
					int pkIndex = columnNames.indexOf(pk);
					String pkData = rowData.get(pkIndex);
					rowIndividual = rowIndividual + OntMapper.INDIVIDUAL_DELIMITER + pk + OntMapper.INDIVIDUAL_DELIMITER + pkData;
				}

				URI subjectIndividualURI = new URI(Janus.ontBridge.getOntologyID() + "#" + rowIndividual);
				Individual subjectIndividual = new Individual(subjectIndividualURI);

				IndividualSet individualSet = new IndividualSet(sClassURI);
				individualSet.acceptSameAsCondition(subjectIndividual);

				Triple triple = new Triple(individualSet, predicate, object);
				triples.add(triple);

				Set<URI> familyClasses = subjectIndividual.getFamilyClasses();
				familyClasses.remove(subjectIndividual.getClassURI());

				for (URI familyClass: familyClasses) {
					URI sameAsSubjectIndividualURI = subjectIndividual.getTypeChangedIndividual(familyClass);
					Individual sameAsSubjectIndividual = new Individual(sameAsSubjectIndividualURI);

					if (sameAsSubjectIndividual.isExistentIndividual()) {
						IndividualSet sameAsIndividualSet = new IndividualSet(familyClass);
						sameAsIndividualSet.acceptSameAsCondition(sameAsSubjectIndividual);

						triple = new Triple(sameAsIndividualSet, predicate, object);
						triples.add(triple);
					}
				}
			}
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return triples;
	}
	
	List<Triple> getPOTriples() {
		List<Triple> triples = new Vector<Triple>();
		
		URI sClassURI = getClassURI();
		
		if (getType().equals(IndividualTypes.FROM_CELL)) {
			URI subject = individual;
			URI predicate = Janus.mappingMetadata.getMappedDataProperty(mappedTableName, hasKeyColumnNames[0]);
			
			LiteralSet literalSet = new LiteralSet(sClassURI, predicate);
			literalSet.acceptSameAsCondition(this);
			
			Triple triple = new Triple(subject, predicate, literalSet);
			triples.add(triple);
			
			Set<URI> familyClasses = getFamilyClasses();
			familyClasses.remove(getClassURI());
			
			for (URI familyClass: familyClasses) {
				if (Janus.ontBridge.isSubClassOf(getClassURI(), familyClass)) {
					String familyTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
					String familyColumn = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);
					
					URI familyPredicate = Janus.mappingMetadata.getMappedDataProperty(familyTable, familyColumn);
					
					literalSet = new LiteralSet(familyClass, familyPredicate);
					literalSet.acceptSameAsCondition(this);
					
					triple = new Triple(subject, familyPredicate, literalSet);
					triples.add(triple);
				} else {
					URI familyIndividualURI = getTypeChangedIndividual(familyClass);
					Individual familyIndividual = new Individual(familyIndividualURI);
					
					if (familyIndividual.isExistentIndividual()) {
						String familyTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
						String familyColumn = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);
						
						URI familyPredicate = Janus.mappingMetadata.getMappedDataProperty(familyTable, familyColumn);
						
						literalSet = new LiteralSet(familyClass, familyPredicate);
						literalSet.acceptSameAsCondition(this);
						
						triple = new Triple(subject, familyPredicate, literalSet);
						triples.add(triple);
					}
				}
			}
		} else {
			String query = "SELECT * FROM " + mappedTableName + " WHERE ";
			for (int i = 0; i < hasKeyColumnNames.length; i++)
				query = query + hasKeyColumnNames[i] + " = '" + hasKeyColumnValues[i] + "' AND ";
			query = query.substring(0, query.lastIndexOf("AND"));

			SQLResultSet rs = Janus.dbBridge.executeQuery(query);

			int columnCount = rs.getResultSetColumnCount();
			List<String> columnNames = new Vector<String>(columnCount);

			for(int column = 1; column <= columnCount; column++) {
				String columnName = rs.getResultSetColumnLabel(column);
				columnNames.add(columnName);
			}

			List<String> rowData = rs.getResultSetRowAt(1);

			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				String columnName = columnNames.get(columnIndex);
				String cellData = rowData.get(columnIndex);

				if (cellData != null) {
					if (Janus.cachedDBMetadata.isKey(mappedTableName, columnName)) {
						try {
							URI subject = individual;
							URI predicate = Janus.mappingMetadata.getMappedObjectProperty(mappedTableName, columnName);
							
							URI mappedClsURI = Janus.mappingMetadata.getMappedClass(mappedTableName, columnName);

							IndividualSet individualSet = new IndividualSet(mappedClsURI);

							String cellIndividual = OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + mappedTableName + "." + columnName + OntMapper.INDIVIDUAL_DELIMITER + cellData;
							URI objectIndividualURI = new URI(Janus.ontBridge.getOntologyID() + "#" + cellIndividual);
							Individual objectIndividual = new Individual(objectIndividualURI);

							individualSet.acceptSameAsCondition(objectIndividual);
							
							Triple triple = new Triple(subject, predicate, individualSet);
							triples.add(triple);
							
							Set<URI> familyClasses = objectIndividual.getFamilyClasses();
							familyClasses.remove(objectIndividual.getClassURI());
							
							for (URI familyClass: familyClasses) {
								if (Janus.ontBridge.isSubClassOf(objectIndividual.getClassURI(), familyClass)) {
									IndividualSet familyClassIndividualSet = new IndividualSet(familyClass);
									
									familyClassIndividualSet.acceptSameAsCondition(objectIndividual);
									
									triple = new Triple(subject, predicate, familyClassIndividualSet);
									triples.add(triple);
								} else {
									URI familyIndividualURI = objectIndividual.getTypeChangedIndividual(familyClass);
									Individual familyIndividual = new Individual(familyIndividualURI);
									
									if (familyIndividual.isExistentIndividual()) {
										IndividualSet familyClassIndividualSet = new IndividualSet(familyClass);
										
										familyClassIndividualSet.acceptSameAsCondition(objectIndividual);
										
										triple = new Triple(subject, predicate, familyClassIndividualSet);
										triples.add(triple);
									}
								}
							}
							
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
					} else {
						URI subject = individual;
						URI predicate = Janus.mappingMetadata.getMappedDataProperty(mappedTableName, columnName);
						
						LiteralSet literalSet = new LiteralSet(sClassURI, predicate);
						literalSet.acceptSameAsCondition(this);
						
						Triple triple = new Triple(subject, predicate, literalSet);
						triples.add(triple);
					}
				}
			}
			
			Set<URI> familyClasses = getFamilyClasses();
			familyClasses.remove(getClassURI());
			
			for (URI familyClass: familyClasses) {
				String familyTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
				URI familyIndividualURI = getTypeChangedIndividual(familyClass);
				Individual familyIndividual = new Individual(familyIndividualURI);
				
				String familyQuery = "SELECT * FROM " + familyTable + " WHERE ";
				for (int i = 0; i < hasKeyColumnNames.length; i++)
					familyQuery = familyQuery + familyIndividual.getHasKeyColumnNameAt(i) + " = '" + familyIndividual.getHasKeyColumnValueAt(i) + "' AND ";
				familyQuery = familyQuery.substring(0, familyQuery.lastIndexOf("AND"));

				rs = Janus.dbBridge.executeQuery(familyQuery);
				
				int colCount = rs.getResultSetColumnCount();
				List<String> colNames = new Vector<String>(colCount);

				for(int column = 1; column <= colCount; column++) {
					String columnName = rs.getResultSetColumnLabel(column);
					colNames.add(columnName);
				}

				List<String> row = rs.getResultSetRowAt(1);
				
				for (int columnIndex = 0; columnIndex < colCount; columnIndex++) {
					String columnName = colNames.get(columnIndex);
					String cellData = row.get(columnIndex);

					if (cellData != null) {
						if (Janus.cachedDBMetadata.isKey(familyTable, columnName)) {
							try {
								URI subject = individual;
								URI predicate = Janus.mappingMetadata.getMappedObjectProperty(familyTable, columnName);
								
								String cellIndividual = OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + familyTable + "." + columnName + OntMapper.INDIVIDUAL_DELIMITER + cellData;
								URI objectIndividualURI = new URI(Janus.ontBridge.getOntologyID() + "#" + cellIndividual);
								Individual objectIndividual = new Individual(objectIndividualURI);
								
								URI mappedClsURI = Janus.mappingMetadata.getMappedClass(familyTable, columnName);

								IndividualSet individualSet = new IndividualSet(mappedClsURI);
								individualSet.acceptSameAsCondition(objectIndividual);
								
								Triple triple = new Triple(subject, predicate, individualSet);
								triples.add(triple);
								
								Set<URI> objectFamilyClasses = objectIndividual.getFamilyClasses();
								objectFamilyClasses.remove(objectIndividual.getClassURI());
								
								for (URI objectFamilyClass: objectFamilyClasses) {
									if (Janus.ontBridge.isSubClassOf(objectIndividual.getClassURI(), objectFamilyClass)) {
										individualSet = new IndividualSet(objectFamilyClass);
										individualSet.acceptSameAsCondition(objectIndividual);
										
										triple = new Triple(subject, predicate, individualSet);
										triples.add(triple);
									} else {
										URI objectFamilyIndividualURI = objectIndividual.getTypeChangedIndividual(objectFamilyClass);
										Individual objectFamilyIndividual = new Individual(objectFamilyIndividualURI);
										
										if (objectFamilyIndividual.isExistentIndividual()) {
											individualSet = new IndividualSet(objectFamilyClass);
											individualSet.acceptSameAsCondition(objectIndividual);
											
											triple = new Triple(subject, predicate, individualSet);
											triples.add(triple);
										}
									}
								}
								
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}
						} else {
							URI subject = individual;
							URI predicate = Janus.mappingMetadata.getMappedDataProperty(familyTable, columnName);

							LiteralSet literalSet = new LiteralSet(familyClass, predicate);
							literalSet.acceptSameAsCondition(this);
							
							Triple triple = new Triple(subject, predicate, literalSet);
							triples.add(triple);
						}
					}
				}
			}
		}

		return triples;
	}
	
	/*List<Triple> getTriples() {
		List<Triple> triples = new Vector<Triple>();
		
		if (getType().equals(IndividualTypes.FROM_CELL)) {
			URI subject = individual;
			URI predicate = Janus.mappingMD.getMappedDataProperty(mappedTableName, hasKeyColumnNames[0]);
			String objectLiteral = hasKeyColumnValues[0];
			
			Triple triple = new Triple(subject, predicate, objectLiteral);
			triples.add(triple);
			
			Set<URI> familyClasses = getFamilyClasses();
			familyClasses.remove(getClassURI());
			
			for (URI familyClass: familyClasses) {
				if (Janus.ontBridge.isSubClassOf(getClassURI(), familyClass)) {
					String familyTable = Janus.mappingMD.getMappedTableNameOfTheClass(familyClass);
					String familyColumn = Janus.mappingMD.getMappedColumnNameOfTheClass(familyClass);
					
					URI familyPredicate = Janus.mappingMD.getMappedDataProperty(familyTable, familyColumn);
					
					triple = new Triple(subject, familyPredicate, objectLiteral);
					triples.add(triple);
				} else {
					URI familyIndividualURI = getTypeChangedIndividual(familyClass);
					Individual familyIndividual = new Individual(familyIndividualURI);
					
					if (familyIndividual.isExistentIndividual()) {
						String familyTable = Janus.mappingMD.getMappedTableNameOfTheClass(familyClass);
						String familyColumn = Janus.mappingMD.getMappedColumnNameOfTheClass(familyClass);
						
						URI familyPredicate = Janus.mappingMD.getMappedDataProperty(familyTable, familyColumn);
						
						triple = new Triple(subject, familyPredicate, objectLiteral);
						triples.add(triple);
					}
				}
			}
		} else {
			String query = "SELECT * FROM " + mappedTableName + " WHERE ";
			for (int i = 0; i < hasKeyColumnNames.length; i++)
				query = query + hasKeyColumnNames[i] + " = '" + hasKeyColumnValues[i] + "' AND ";
			query = query.substring(0, query.lastIndexOf("AND"));

			Janus.dbBridge.executeQuery(query);

			int columnCount = Janus.dbBridge.getResultSetColumnCount();
			List<String> columnNames = new Vector<String>(columnCount);

			for(int column = 1; column <= columnCount; column++) {
				String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
				columnNames.add(columnName);
			}

			List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);

			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				String columnName = columnNames.get(columnIndex);
				String cellData = rowData.get(columnIndex);

				if (cellData != null) {
					if (Janus.localDBMD.isKey(mappedTableName, columnName)) {
						try {
							URI subject = individual;
							URI predicate = Janus.mappingMD.getMappedObjectProperty(mappedTableName, columnName);
							
							String cellIndividual = OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + mappedTableName + "." + columnName + OntMapper.INDIVIDUAL_DELIMITER + cellData;
							URI objectIndividual = new URI(Janus.ontBridge.getOntologyID() + "#" + cellIndividual);
							
							Triple triple = new Triple(subject, predicate, objectIndividual);
							triples.add(triple);
							
							Individual objectInstance = new Individual(objectIndividual);
							
							Set<URI> familyClasses = objectInstance.getFamilyClasses();
							familyClasses.remove(objectInstance.getClassURI());
							
							for (URI familyClass: familyClasses) {
								if (Janus.ontBridge.isSubClassOf(objectInstance.getClassURI(), familyClass)) {
									URI familyIndividualURI = objectInstance.getTypeChangedIndividual(familyClass);
									
									triple = new Triple(subject, predicate, familyIndividualURI);
									triples.add(triple);
								} else {
									URI familyIndividualURI = objectInstance.getTypeChangedIndividual(familyClass);
									Individual familyIndividual = new Individual(familyIndividualURI);
									
									if (familyIndividual.isExistentIndividual()) {
										triple = new Triple(subject, predicate, familyIndividualURI);
										triples.add(triple);
									}
								}
							}
							
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
					} else {
						URI subject = individual;
						URI predicate = Janus.mappingMD.getMappedDataProperty(mappedTableName, columnName);
						String objectLiteral = cellData;

						Triple triple = new Triple(subject, predicate, objectLiteral);
						triples.add(triple);
					}
				}
			}
			
			Set<URI> familyClasses = getFamilyClasses();
			familyClasses.remove(getClassURI());
			
			for (URI familyClass: familyClasses) {
				String familyTable = Janus.mappingMD.getMappedTableNameOfTheClass(familyClass);
				URI familyIndividualURI = getTypeChangedIndividual(familyClass);
				Individual familyIndividual = new Individual(familyIndividualURI);
				
				String familyQuery = "SELECT * FROM " + familyTable + " WHERE ";
				for (int i = 0; i < hasKeyColumnNames.length; i++)
					familyQuery = familyQuery + familyIndividual.getHasKeyColumnNameAt(i) + " = '" + familyIndividual.getHasKeyColumnValueAt(i) + "' AND ";
				familyQuery = familyQuery.substring(0, familyQuery.lastIndexOf("AND"));

				Janus.dbBridge.executeQuery(familyQuery);
				
				int colCount = Janus.dbBridge.getResultSetColumnCount();
				List<String> colNames = new Vector<String>(colCount);

				for(int column = 1; column <= colCount; column++) {
					String columnName = Janus.dbBridge.getResultSetColumnLabel(column);
					colNames.add(columnName);
				}

				List<String> row = Janus.dbBridge.getResultSetRowAt(1);
				
				for (int columnIndex = 0; columnIndex < colCount; columnIndex++) {
					String columnName = colNames.get(columnIndex);
					String cellData = row.get(columnIndex);

					if (cellData != null) {
						if (Janus.localDBMD.isKey(familyTable, columnName)) {
							try {
								URI subject = individual;
								URI predicate = Janus.mappingMD.getMappedObjectProperty(familyTable, columnName);
								
								String cellIndividual = OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + familyTable + "." + columnName + OntMapper.INDIVIDUAL_DELIMITER + cellData;
								URI objectIndividual = new URI(Janus.ontBridge.getOntologyID() + "#" + cellIndividual);
								
								Triple triple = new Triple(subject, predicate, objectIndividual);
								triples.add(triple);
								
								Individual objectInstance = new Individual(objectIndividual);
								
								Set<URI> objectFamilyClasses = objectInstance.getFamilyClasses();
								objectFamilyClasses.remove(objectInstance.getClassURI());
								
								for (URI objectFamilyClass: objectFamilyClasses) {
									if (Janus.ontBridge.isSubClassOf(objectInstance.getClassURI(), objectFamilyClass)) {
										URI objectFamilyIndividualURI = objectInstance.getTypeChangedIndividual(objectFamilyClass);
										
										triple = new Triple(subject, predicate, objectFamilyIndividualURI);
										triples.add(triple);
									} else {
										URI objectFamilyIndividualURI = objectInstance.getTypeChangedIndividual(objectFamilyClass);
										Individual objectFamilyIndividual = new Individual(objectFamilyIndividualURI);
										
										if (objectFamilyIndividual.isExistentIndividual()) {
											triple = new Triple(subject, predicate, objectFamilyIndividualURI);
											triples.add(triple);
										}
									}
								}
								
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}
						} else {
							URI subject = individual;
							URI predicate = Janus.mappingMD.getMappedDataProperty(familyTable, columnName);
							String objectLiteral = cellData;

							Triple triple = new Triple(subject, predicate, objectLiteral);
							triples.add(triple);
						}
					}
				}
			}
		}

		return triples;
	}*/
}
