package janus.sparqldl;

import janus.Janus;
import janus.database.SQLResultSet;
import janus.mapping.OntMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Literal {
	private String value;
	
	Literal(String value) {
		this.value = value;
	}
	
	List<Triple> getSPTriples() {
		List<Triple> triples = new Vector<Triple>();
		
		try {
			String object = value;
			
			Set<String> tables = Janus.cachedDBMetadata.getTableNames();
			
			for (String table: tables) {
				Set<String> columns = Janus.cachedDBMetadata.getColumns(table);
				Set<String> keyColumns = Janus.cachedDBMetadata.getKeyColumns(table);
				List<String> primaryKeys = Janus.cachedDBMetadata.getPrimaryKeys(table);
				
				String query = "SELECT * FROM " + table + " WHERE ";
				for (String column: columns)
					query = query + column + " = '" + value + "' OR ";
				query = query.substring(0, query.lastIndexOf(" OR"));
				
				SQLResultSet rs = Janus.dbBridge.executeQuery(query);
				
				int columnCount = rs.getResultSetColumnCount();
				List<String> columnNames = new Vector<String>(columnCount);

				for(int column = 1; column <= columnCount; column++) {
					String columnName = rs.getResultSetColumnLabel(column);
					columnNames.add(columnName);
				}
				
				int rowCount = Janus.dbBridge.getResultSetRowCount();
				for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
					List<String> rowData = rs.getResultSetRowAt(rowIndex);
					
					for (int i = 0; i < columnCount; i++) {
						String cellData = rowData.get(i);
						
						if (cellData != null && cellData.equals(value)) {
							String columnName = columnNames.get(i);
							
							if (keyColumns.contains(columnName)) {
								String sIndividualFragment = OntMapper.CELL_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + table + "." + columnName + OntMapper.INDIVIDUAL_DELIMITER + cellData;
								URI sIndividualURI = new URI(Janus.ontBridge.getOntologyID() + "#" + sIndividualFragment);
								Individual sIndividual = new Individual(sIndividualURI);
								
								URI sClsURI = Janus.mappingMetadata.getMappedClass(table, columnName);
								IndividualSet sIndividualSet = new IndividualSet(sClsURI);
								sIndividualSet.acceptSameAsCondition(sIndividual);
								
								URI predicate = Janus.mappingMetadata.getMappedDataProperty(table, columnName);
								
								Triple triple = new Triple(sIndividualSet, predicate, object);
								triples.add(triple);
								
								Set<URI> familyClasses = sIndividual.getFamilyClasses();
								familyClasses.remove(sIndividual.getClassURI());

								for (URI familyClass: familyClasses) {
									URI sameAsSubjectIndividualURI = sIndividual.getTypeChangedIndividual(familyClass);
									Individual sameAsSubjectIndividual = new Individual(sameAsSubjectIndividualURI);

									if (sameAsSubjectIndividual.isExistentIndividual()) {
										IndividualSet sameAsIndividualSet = new IndividualSet(familyClass);
										sameAsIndividualSet.acceptSameAsCondition(sameAsSubjectIndividual);

										triple = new Triple(sameAsIndividualSet, predicate, object);
										triples.add(triple);
									}
								}
								
							} else {
								String sIndividualFragment = OntMapper.ROW_INDIVIDUAL_PREFIX + OntMapper.INDIVIDUAL_DELIMITER + primaryKeys.size() + OntMapper.INDIVIDUAL_DELIMITER + table;
								for (String pk: primaryKeys) {
									int pkIndex = columnNames.indexOf(pk);
									String pkData = rowData.get(pkIndex);
									sIndividualFragment = sIndividualFragment + OntMapper.INDIVIDUAL_DELIMITER + pk + OntMapper.INDIVIDUAL_DELIMITER + pkData;
								}
								URI sIndividualURI = new URI(Janus.ontBridge.getOntologyID() + "#" + sIndividualFragment);
								Individual sIndividual = new Individual(sIndividualURI);
								
								URI sClsURI = Janus.mappingMetadata.getMappedClass(table);
								IndividualSet sIndividualSet = new IndividualSet(sClsURI);
								sIndividualSet.acceptSameAsCondition(sIndividual);
								
								URI predicate = Janus.mappingMetadata.getMappedDataProperty(table, columnName);
								
								Triple triple = new Triple(sIndividualSet, predicate, object);
								triples.add(triple);
								
								Set<URI> familyClasses = sIndividual.getFamilyClasses();
								familyClasses.remove(sIndividual.getClassURI());

								for (URI familyClass: familyClasses) {
									URI sameAsSubjectIndividualURI = sIndividual.getTypeChangedIndividual(familyClass);
									Individual sameAsSubjectIndividual = new Individual(sameAsSubjectIndividualURI);

									if (sameAsSubjectIndividual.isExistentIndividual()) {
										IndividualSet sameAsIndividualSet = new IndividualSet(familyClass);
										sameAsIndividualSet.acceptSameAsCondition(sameAsSubjectIndividual);

										triple = new Triple(sameAsIndividualSet, predicate, object);
										triples.add(triple);
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
		
		return triples;
	}
}
