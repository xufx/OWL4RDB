package janus.mapping.metadata;

import janus.Janus;
import janus.mapping.DatatypeMap;
import janus.mapping.OntMapper;
import janus.ontology.OWLEntityTypes;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class MappingMetadataFactory {
	public static MappingMetadata generateMappingMetaData() {
		MappingMetadata mappingMD = new MappingMetadata(Janus.ontBridge.getOntologyID());
		
		Set<String> tables = Janus.cachedDBMetadata.getTableNames();
		
		Set<String> disjointClasses = new ConcurrentSkipListSet<String>();
		
		for(String table : tables) {
			Set<String> columns = Janus.cachedDBMetadata.getColumns(table);
			
			String tableClass = table;
			//Set<String> keyObjectProperties = new ConcurrentSkipListSet<String>();
			//Rule 1
			//writer.println("Declaration(Class(:" + tableClass + "))");
			ClassMetadata tableClassMD = new ClassMetadata(tableClass, table);
			
			String superTable = Janus.cachedDBMetadata.getSuperTable(table);
			if (superTable != null) {
				String superTableClass = superTable;
				//Rule 18
				//writer.println("SubClassOf(:" + tableClass + " :" + superTableClass + ")");
				tableClassMD.setSuperClassName(superTableClass);
			} else
				disjointClasses.add(tableClass);
			
			
			for(String column: columns) {
				String dataProperty = OntMapper.DP_PREFIX + table + "." + column;
				String dataRange = null;
				//Rule 4
				//writer.println("Declaration(DataProperty(:" + dataProperty +"))");
				PropertyMetadata dpMD = new PropertyMetadata(dataProperty, OWLEntityTypes.DATA_PROPERTY, table, column);
				
				int sqlDataType = Janus.cachedDBMetadata.getDataType(table, column);
				String xmlSchemaDataType = DatatypeMap.get(sqlDataType);
				dataRange = xmlSchemaDataType;
				//Rule 9
				//writer.println("DataPropertyRange(:" +  dataProperty + " " + dataRange + ")");
				dpMD.setRange(dataRange);
				
				if (Janus.cachedDBMetadata.isKey(table, column)) {
					String columnClass = table + "." + column;
					String objectProperty = OntMapper.OP_PREFIX + table + "." + column;
					
					//Rule 2
					//writer.println("Declaration(Class(:" + columnClass + "))");
					ClassMetadata columnClassMD = new ClassMetadata(columnClass, table, column);
					//Rule 3
					//writer.println("Declaration(ObjectProperty(:" + objectProperty + "))");
					PropertyMetadata opMD = new PropertyMetadata(objectProperty, OWLEntityTypes.OBJECT_PROPERTY, table, column);
					//Rule 5
					//writer.println("ObjectPropertyDomain(:" + objectProperty + " :" + tableClass + ")");
					opMD.setDomain(tableClass);
					//Rule 6
					//writer.println("ObjectPropertyRange(:" + objectProperty + " :" + columnClass + ")");
					opMD.setRange(columnClass);
					//Rule 7
					//writer.println("DataPropertyDomain(:" + dataProperty + " :" + columnClass + ")");
					dpMD.setDomain(columnClass);
					//Rule 13
					//writer.println("HasKey(:" + columnClass + " () (:" + dataProperty + "))");
					//Rule 16
					//writer.println("SubClassOf(:" + columnClass + " DataAllValuesFrom(:" + dataProperty + " " + dataRange + "))");
					//Rule 17
					//writer.println("SubClassOf(:" + columnClass + " DataExactCardinality(1 :" + dataProperty + " " + dataRange + "))");
					//Rule 19
					//writer.println("SubClassOf(:" + tableClass + " ObjectAllValuesFrom(:" + objectProperty + " :" + columnClass + "))");
					
					/*
					if (localDBMD.isNotNull(table, column))
						//Rule 20
						writer.println("SubClassOf(:" + tableClass + " ObjectExactCardinality(1 :" + objectProperty + " :" + columnClass + "))");
					else
						//Rule 21
						writer.println("SubClassOf(:" + tableClass + " ObjectMaxCardinality(1 :" + objectProperty + " :" + columnClass + "))");
					

					
					if (localDBMD.isPrimaryKey(table, column))
						keyObjectProperties.add(objectProperty);
					
					if (localDBMD.isSingleColumnUniqueKey(table, column))
						//Rule 14
						writer.println("InverseFunctionalObjectProperty(:" + objectProperty + ")");
					*/
					
					if (Janus.cachedDBMetadata.isForeignKey(table, column)) {
						String superColumn = Janus.cachedDBMetadata.getSuperColumn(table, column);
						String superColumnClass = superColumn;
						
						//Rule 15
						//writer.println("SubClassOf(:" + columnClass + " :" + superColumnClass + ")");
						columnClassMD.setSuperClassName(superColumnClass);
						
						if (superTable != null && Janus.cachedDBMetadata.isPrimaryKey(table, column)) {
							String superObjectProperty = OntMapper.OP_PREFIX + superColumn;
							
							//Rule 25
							//writer.println("SubObjectProperty(:" + objectProperty + " :" + superObjectProperty + ")");
							opMD.setSuperProperty(superObjectProperty);
						}
						
						String superDataProperty = OntMapper.DP_PREFIX + superColumn;
						//Rule 26
						//writer.println("SubDataPropertyOf(:" + dataProperty + " :" + superDataProperty + ")");
						dpMD.setSuperProperty(superDataProperty);
					} else
						disjointClasses.add(columnClass);
					
					mappingMD.addOWLClassMetaData(columnClassMD);
					mappingMD.addOWLPropertyMetaData(opMD);
				} else {
					//Rule 8
					//writer.println("DataPropertyDomain(:" +  dataProperty + " :" + tableClass + ")");
					dpMD.setDomain(tableClass);
					//Rule 22
					//writer.println("SubClassOf(:" + tableClass + " DataAllValuesFrom(:" + dataProperty + " " + dataRange + "))");
					
					/*
					if (localDBMD.isNotNull(table, column))
						//Rule 23
						writer.println("SubClassOf(:" + tableClass + " DataExactCardinality(1 :" + dataProperty + " " + dataRange + "))");
					else
						//Rule 24
						writer.println("SubClassOf(:" + tableClass + " DataMaxCardinality(1 :" + dataProperty + " " + dataRange + "))");
					*/
				}
				
				mappingMD.addOWLPropertyMetaData(dpMD);
			} // END COLUMN
			
			/*
			if (keyObjectProperties.size() > 0) {
				StringBuffer keyObjectPropertyList = new StringBuffer();
				for(String keyObjectProperty: keyObjectProperties)
					keyObjectPropertyList = keyObjectPropertyList.append(":" + keyObjectProperty + " ");
				
				//Rule 12
				writer.println("HasKey(:" + tableClass + " (" + keyObjectPropertyList.toString().trim() + ") ())");
			}
			*/
			
			mappingMD.addOWLClassMetaData(tableClassMD);
		} // END TABLE
		
		/*
		if (disjointClasses.size() > 0) {
			StringBuffer disjointClassList = new StringBuffer();
			for(String disjointClass: disjointClasses)
				disjointClassList = disjointClassList.append(":" + disjointClass + " ");
			
			//Rule 27
			writer.println("DisjointClasses(" + disjointClassList.toString().trim() + ")");
		}
		*/
		
		return mappingMD;
	}
}
