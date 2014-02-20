package janus.sparqldl;

import janus.Janus;
import janus.ontology.OWLEntityTypes;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Utils {
	static List<Triple> getAllTriples(Set<URI> properties) {
		List<Triple> triples = new Vector<Triple>();
		
		for (URI predicate: properties) {
			
			if (predicate.equals(Janus.ontBridge.getOWLTopObjectProperty()) || predicate.equals(Janus.ontBridge.getOWLTopDataProperty()) || predicate.equals(Janus.ontBridge.getOWLBottomObjectProperty()) || predicate.equals(Janus.ontBridge.getOWLBottomDataProperty())) {
				properties.remove(predicate);
				continue;
			}
			
			if (Janus.ontBridge.containsDataProperty(predicate)) {
				
				Set<URI> domains = Janus.ontBridge.getNamedDataPropDomains(predicate);
				URI assertedDomainURI = null;
				for (URI domain: domains) {
					assertedDomainURI = domain;
					break;
				}
				
				if (Janus.mappingMetadata.getClassType(assertedDomainURI).equals(OWLEntityTypes.COLUMN_CLASS)) {
					
					IndividualSet sIndividualSet = new IndividualSet(assertedDomainURI);
					
					LiteralSet oLiteralSet = new LiteralSet(assertedDomainURI, predicate);
					
					Triple triple = new Triple(sIndividualSet, predicate, oLiteralSet);
					triples.add(triple);
					
					Set<URI> familyClasses = sIndividualSet.getFamilyClasses();
					familyClasses.remove(assertedDomainURI);
					
					for (URI familyClass: familyClasses) {
						IndividualSet familyIndividualSet = new IndividualSet(familyClass);
						if (!Janus.ontBridge.isSubClassOf(familyClass, assertedDomainURI))
							familyIndividualSet.intersectWith(assertedDomainURI);
						
						triple = new Triple(familyIndividualSet, predicate, oLiteralSet);
						triples.add(triple);
					}
					
					for (URI familyClass: familyClasses) {
						String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
						String mappedColumn = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);
						
						URI mappedDataProperty = Janus.mappingMetadata.getMappedDataProperty(mappedTable, mappedColumn);
						
						LiteralSet familyLiteralSet = new LiteralSet(familyClass, mappedDataProperty);
						if (!Janus.ontBridge.isSubClassOf(familyClass, assertedDomainURI))
							familyLiteralSet.intersectWith(assertedDomainURI);
						
						triple = new Triple(sIndividualSet, predicate, familyLiteralSet);
						triples.add(triple);
					}
					
				} else {
					
					LiteralSet oLiteralSet = new LiteralSet(assertedDomainURI, predicate);
					
					IndividualSet sIndividualSet = new IndividualSet(assertedDomainURI);
					sIndividualSet.addAllValueWhereSet(oLiteralSet.getValueWhereSet());
					
					Triple triple = new Triple(sIndividualSet, predicate, oLiteralSet);
					triples.add(triple);
					
					Set<URI> familyClasses = sIndividualSet.getFamilyClasses();
					familyClasses.remove(assertedDomainURI);
					
					for (URI familyClass: familyClasses) {
						IndividualSet familyIndividualSet = new IndividualSet(familyClass);
						familyIndividualSet.intersectWith(sIndividualSet);
						
						triple = new Triple(familyIndividualSet, predicate, oLiteralSet);
						triples.add(triple);
					}
					
				}
				
			} else {
				
				Set<URI> domains = Janus.ontBridge.getObjPropNamedDomains(predicate);
				URI assertedDomainURI = null;
				for (URI domain: domains) {
					assertedDomainURI = domain;
					break;
				}
				
				Set<URI> ranges = Janus.ontBridge.getObjPropNamedRanges(predicate);
				URI assertedRangeURI = null;
				for (URI range: ranges) {
					assertedRangeURI = range;
					break;
				}
				
				IndividualSet oIndividualSet = new IndividualSet(assertedRangeURI);
				
				IndividualSet sIndividualSet = new IndividualSet(assertedDomainURI);
				sIndividualSet.addAllValueWhereSet(oIndividualSet.getValueWhereSet());
				
				Triple triple = new Triple(sIndividualSet, predicate, oIndividualSet);
				triples.add(triple);
				
				Set<URI> sFamilyClasses = sIndividualSet.getFamilyClasses();
				sFamilyClasses.remove(assertedDomainURI);
				
				for (URI familyClass: sFamilyClasses) {
					IndividualSet familyIndividualSet = new IndividualSet(familyClass);
					familyIndividualSet.intersectWith(sIndividualSet);
					
					triple = new Triple(familyIndividualSet, predicate, oIndividualSet);
					triples.add(triple);
				}
				
				Set<URI> oFamilyClasses = oIndividualSet.getFamilyClasses();
				oFamilyClasses.remove(assertedRangeURI);
				
				for (URI familyClass: oFamilyClasses) {
					IndividualSet familyIndividualSet = new IndividualSet(familyClass);
					familyIndividualSet.intersectWith(oIndividualSet);
					
					triple = new Triple(sIndividualSet, predicate, familyIndividualSet);
					triples.add(triple);
				}
				
			}
		}
		
		return triples;
	}
	
	static List<Triple> getAllTriples() {
		List<Triple> triples = new Vector<Triple>();
		
		Set<String> tables = Janus.cachedDBMetadata.getTableNames();
		
		for (String table: tables) {
			int tableRowCount = Janus.cachedDBMetadata.getRowCount(table);
			if (tableRowCount < 1) continue;
			
			Set<String> columns = Janus.cachedDBMetadata.getColumns(table);
			Set<String> keyColumns = Janus.cachedDBMetadata.getKeyColumns(table);
			Set<String> noNullColumns = Janus.cachedDBMetadata.getNoNullColumns(table);
			
			URI tableClsURI = Janus.mappingMetadata.getMappedClass(table);
			
			for (String column: columns) {
				
				URI dp = Janus.mappingMetadata.getMappedDataProperty(table, column);
				
				if (noNullColumns.contains(column)) {
					
					if (keyColumns.contains(column)) {
						
						URI columnClsURI = Janus.mappingMetadata.getMappedClass(table, column);
						URI op = Janus.mappingMetadata.getMappedObjectProperty(table, column);
						
						IndividualSet sTableIndividualSet = new IndividualSet(tableClsURI);
						IndividualSet oColumnIndividualSet = new IndividualSet(columnClsURI);
						
						Triple triple = new Triple(sTableIndividualSet, op, oColumnIndividualSet);
						triples.add(triple);
						
						IndividualSet sColumnIndividualSet = new IndividualSet(columnClsURI);
						LiteralSet oColumnLiteralSet = new LiteralSet(columnClsURI, dp);
						
						triple = new Triple(sColumnIndividualSet, dp, oColumnLiteralSet);
						triples.add(triple);
						
					} else {
						IndividualSet sTableIndividualSet = new IndividualSet(tableClsURI);
						LiteralSet oColumnLiteralSet = new LiteralSet(tableClsURI, dp);
						
						Triple triple = new Triple(sTableIndividualSet, dp, oColumnLiteralSet);
						triples.add(triple);
					}
				} else {
					String query = "SELECT COUNT(DISTINCT " + column + ") FROM " + table + " WHERE " + column + " IS NOT NULL";
					
					Janus.dbBridge.executeQuery(query);
					
					List<String> rowData = Janus.dbBridge.getResultSetRowAt(1);
					
					int columnRowCount = Integer.parseInt(rowData.get(0));
					
					if (columnRowCount < 1) continue;
					
					String valueCondition = table + "." + column + " IS NOT NULL";
					
					if (keyColumns.contains(column)) {
						
						URI columnClsURI = Janus.mappingMetadata.getMappedClass(table, column);
						URI op = Janus.mappingMetadata.getMappedObjectProperty(table, column);
						
						IndividualSet sTableIndividualSet = new IndividualSet(tableClsURI);
						sTableIndividualSet.addValueWhereSet(valueCondition);
						
						IndividualSet oColumnIndividualSet = new IndividualSet(columnClsURI);
						oColumnIndividualSet.addValueWhereSet(valueCondition);
						
						Triple triple = new Triple(sTableIndividualSet, op, oColumnIndividualSet);
						triples.add(triple);
						
						IndividualSet sColumnIndividualSet = new IndividualSet(columnClsURI);
						sColumnIndividualSet.addValueWhereSet(valueCondition);
						LiteralSet oColumnLiteralSet = new LiteralSet(columnClsURI, dp);
						oColumnLiteralSet.addValueWhereSet(valueCondition);
						
						triple = new Triple(sColumnIndividualSet, dp, oColumnLiteralSet);
						triples.add(triple);
						
					} else {
						
						IndividualSet sTableIndividualSet = new IndividualSet(tableClsURI);
						sTableIndividualSet.addValueWhereSet(valueCondition);
						LiteralSet oColumnLiteralSet = new LiteralSet(tableClsURI, dp);
						oColumnLiteralSet.addValueWhereSet(valueCondition);
						
						Triple triple = new Triple(sTableIndividualSet, dp, oColumnLiteralSet);
						triples.add(triple);
						
					}
				}
			}
		}
		
		return triples;
	}
}
