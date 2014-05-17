package janus.mapping;

import janus.Janus;
import janus.database.DBField;
import janus.database.SQLResultSet;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

public class OntMapper {
	public static final String INDIVIDUAL_DELIMITER = "__";
	public static final String ROW_INDIVIDUAL_PREFIX = "r";
	public static final String CELL_INDIVIDUAL_PREFIX = "c";
	
	public static final String DP_PREFIX = "dp_"; //data property prefix
	public static final String OP_PREFIX = "op_"; //object property prefix
	
	//for individual
	public static final String TABLE_NAME = "t";
	public static final String PK_COLUMN_NAME = "k";
	public static final String COLUMN_NAME = "c";
	public static final String VALUE = "v";
	public static final String IS = "=";
	public static final String AND = "&";
	
	public static final String COLON = ":";
	public static final String NUMBER_SIGN = "#";
	
	
	
	
	private File output;
	private PrintWriter writer;
	
	private void preprocess(boolean isDump) {
		String catalog = Janus.cachedDBMetadata.getCatalog();
		
		if (isDump)
			output = new File(Janus.DEFAULT_DIR_FOR_DUMP_FILE + catalog + ".owl");
		else
			output = new File(Janus.DEFAULT_DIR_FOR_TBOX_FILE + catalog + ".owl");
		
		try {
			writer = new PrintWriter(output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writePrefixes() {
		try {
			writer.println("Prefix(" + PrefixMap.getPrefix(URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns")) + ":=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)");
			writer.println("Prefix(:=<" + Janus.ontologyURI + "#>)");
			writer.println("Prefix(" + PrefixMap.getPrefix(URI.create("http://www.w3.org/2001/XMLSchema")) + ":=<http://www.w3.org/2001/XMLSchema#>)");
			writer.println("Prefix(" + PrefixMap.getPrefix(URI.create("http://www.w3.org/2004/02/skos/core")) + ":=<http://www.w3.org/2004/02/skos/core#>)");
			writer.println("Prefix(" + PrefixMap.getPrefix(URI.create("http://www.w3.org/XML/1998/namespace")) + ":=<http://www.w3.org/XML/1998/namespace>)");
			writer.println("Prefix(" + PrefixMap.getPrefix(URI.create("http://www.w3.org/2000/01/rdf-schema")) + ":=<http://www.w3.org/2000/01/rdf-schema#>)");
			writer.println("Prefix(" + PrefixMap.getPrefix(URI.create("http://www.w3.org/2002/07/owl")) + ":=<http://www.w3.org/2002/07/owl#>)");
			writer.println();
			writer.println("Ontology(<" + Janus.ontologyURI + ">");
			writer.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writeTBox() {
		Set<String> tables = Janus.cachedDBMetadata.getTableNames();
		
		Set<String> disjointClasses = new ConcurrentSkipListSet<String>();
		
		for(String table : tables) {
			Set<String> columns = Janus.cachedDBMetadata.getColumns(table);
			
			String tableClass = table;
			Set<String> keyObjectProperties = new ConcurrentSkipListSet<String>();
			//Rule 1
			writer.println("Declaration(Class(:" + tableClass + "))");
			
			String superTable = Janus.cachedDBMetadata.getSuperTable(table);
			if (superTable != null) {
				String superTableClass = superTable;
				//Rule 18
				writer.println("SubClassOf(:" + tableClass + " :" + superTableClass + ")");
			} else
				disjointClasses.add(tableClass);
			
			
			for(String column: columns) {
				String dataProperty = OntMapper.DP_PREFIX + table + "." + column;
				String dataRange = null;
				//Rule 4
				writer.println("Declaration(DataProperty(:" + dataProperty +"))");
				
				int sqlDataType = Janus.cachedDBMetadata.getDataType(table, column);
				String xmlSchemaDataType = DatatypeMap.get(sqlDataType);
				dataRange = xmlSchemaDataType;
				//Rule 9
				writer.println("DataPropertyRange(:" +  dataProperty + " " + dataRange + ")");
				
				if (Janus.cachedDBMetadata.isKey(table, column)) {
					String columnClass = table + "." + column;
					String objectProperty = OntMapper.OP_PREFIX + table + "." + column;
					
					//Rule 2
					writer.println("Declaration(Class(:" + columnClass + "))");
					//Rule 3
					writer.println("Declaration(ObjectProperty(:" + objectProperty + "))");
					//Rule 5
					writer.println("ObjectPropertyDomain(:" + objectProperty + " :" + tableClass + ")");
					//Rule 6
					writer.println("ObjectPropertyRange(:" + objectProperty + " :" + columnClass + ")");
					//Rule 7
					writer.println("DataPropertyDomain(:" + dataProperty + " :" + columnClass + ")");
					//Rule 13
					writer.println("HasKey(:" + columnClass + " () (:" + dataProperty + "))");
					//Rule 16
					writer.println("SubClassOf(:" + columnClass + " DataAllValuesFrom(:" + dataProperty + " " + dataRange + "))");
					//Rule 17
					writer.println("SubClassOf(:" + columnClass + " DataExactCardinality(1 :" + dataProperty + " " + dataRange + "))");
					//Rule 19
					writer.println("SubClassOf(:" + tableClass + " ObjectAllValuesFrom(:" + objectProperty + " :" + columnClass + "))");
					
					if (Janus.cachedDBMetadata.isNotNull(table, column))
						//Rule 20
						writer.println("SubClassOf(:" + tableClass + " ObjectExactCardinality(1 :" + objectProperty + " :" + columnClass + "))");
					else
						//Rule 21
						writer.println("SubClassOf(:" + tableClass + " ObjectMaxCardinality(1 :" + objectProperty + " :" + columnClass + "))");

					
					if (Janus.cachedDBMetadata.isPrimaryKey(table, column))
						keyObjectProperties.add(objectProperty);
					
					if (Janus.cachedDBMetadata.isSingleColumnUniqueKey(table, column))
						//Rule 14
						writer.println("InverseFunctionalObjectProperty(:" + objectProperty + ")");
					
					if (Janus.cachedDBMetadata.isForeignKey(table, column)) {
						String superColumn = Janus.cachedDBMetadata.getSuperColumn(table, column);
						String superColumnClass = superColumn;
						
						//Rule 15
						writer.println("SubClassOf(:" + columnClass + " :" + superColumnClass + ")");
						
						if (superTable != null && Janus.cachedDBMetadata.isPrimaryKey(table, column)) {
							String superObjectProperty = OntMapper.OP_PREFIX + superColumn;
							
							//Rule 25
							writer.println("SubObjectPropertyOf(:" + objectProperty + " :" + superObjectProperty + ")");
						}
						
						String superDataProperty = OntMapper.DP_PREFIX + superColumn;
						//Rule 26
						writer.println("SubDataPropertyOf(:" + dataProperty + " :" + superDataProperty + ")");
					} else
						disjointClasses.add(columnClass);
				} else {
					//Rule 8
					writer.println("DataPropertyDomain(:" +  dataProperty + " :" + tableClass + ")");
					//Rule 22
					writer.println("SubClassOf(:" + tableClass + " DataAllValuesFrom(:" + dataProperty + " " + dataRange + "))");
					
					if (Janus.cachedDBMetadata.isNotNull(table, column))
						//Rule 23
						writer.println("SubClassOf(:" + tableClass + " DataExactCardinality(1 :" + dataProperty + " " + dataRange + "))");
					else
						//Rule 24
						writer.println("SubClassOf(:" + tableClass + " DataMaxCardinality(1 :" + dataProperty + " " + dataRange + "))");
				}
			}
			
			if (keyObjectProperties.size() > 0) {
				StringBuffer keyObjectPropertyList = new StringBuffer();
				for(String keyObjectProperty: keyObjectProperties)
					keyObjectPropertyList = keyObjectPropertyList.append(":" + keyObjectProperty + " ");
				
				//Rule 12
				writer.println("HasKey(:" + tableClass + " (" + keyObjectPropertyList.toString().trim() + ") ())");
			}
		}
		
		if (disjointClasses.size() > 0) {
			StringBuffer disjointClassList = new StringBuffer();
			for(String disjointClass: disjointClasses)
				disjointClassList = disjointClassList.append(":" + disjointClass + " ");
			
			//Rule 27
			writer.println("DisjointClasses(" + disjointClassList.toString().trim() + ")");
		}
	}
	
	private void postprocess() {
		try {
			writer.print(")");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void dumpABox() {
		Set<String> tableNames = Janus.cachedDBMetadata.getTableNames();
		
		for(String tableName : tableNames) {
			String tableClass = Janus.mappingMetadata.getMappedClassFragment(tableName);
			
			List<String> primaryKeys = Janus.cachedDBMetadata.getPrimaryKey(tableName);
			String query = "SELECT * FROM " + tableName;
			SQLResultSet resultSet = Janus.dbBridge.executeQuery(query);
			
			int columnCount = resultSet.getResultSetColumnCount();
			
			List<String> columnNames = new Vector<String>(columnCount);
			int[] columnDataTypes = new int[columnCount];
			for(int column = 1; column <= columnCount; column++) {
				String columnName = resultSet.getResultSetColumnLabel(column);
				columnNames.add(columnName);
				columnDataTypes[column - 1] = Janus.cachedDBMetadata.getDataType(tableName, columnName);
			}
			
			int rowCount = resultSet.getResultSetRowCount();
			for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
				List<String> rowData = resultSet.getResultSetRowAt(rowIndex);
				
				List<DBField> pkFields = new ArrayList<DBField>();
				for (String pk: primaryKeys) {
					int pkIndex = columnNames.indexOf(pk);
					String pkData = rowData.get(pkIndex);
					DBField pkField = new DBField(tableName, pk, pkData);
					
					pkFields.add(pkField);
				}
				
				String recordIndividual = Janus.mappingMetadata.getMappedIndividual(tableName, pkFields).toString();
				
				if (Janus.cachedDBMetadata.isRootTable(tableName))
					//Rule 29 - (i)
					writer.println("Declaration(NamedIndividual(<" + recordIndividual + ">))");	
				
				//Rule 29 - (ii)
				writer.println("ClassAssertion(:" + tableClass + " <" + recordIndividual + ">)");
				
				for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
					String cellData = rowData.get(columnIndex);
					
					if (cellData == null) continue;
					
					String columnName = columnNames.get(columnIndex);
					String literal = Janus.mappingMetadata.getMappedLiteral(tableName, columnName, cellData);
					String dp = Janus.mappingMetadata.getMappedDataPropertyFragment(tableName, columnName);
					
					if (Janus.cachedDBMetadata.isKey(tableName, columnName)) {
						String fieldIndividual = Janus.mappingMetadata.getMappedIndividual(tableName, columnName, cellData).toString();
						String columnClass = Janus.mappingMetadata.getMappedClassFragment(tableName, columnName);
						String op = Janus.mappingMetadata.getMappedObjectPropertyFragment(tableName, columnName);
						
						if (Janus.cachedDBMetadata.isRootColumn(tableName, columnName))
							//Rule 28 - (i)
							writer.println("Declaration(NamedIndividual(<" + fieldIndividual + ">))");
						
						//Rule 28 - (ii)
						writer.println("ClassAssertion(:" + columnClass + " <" + fieldIndividual + ">)");
						//Rule 28 - (iii)
						writer.println("DataPropertyAssertion(:" + dp + " <" + fieldIndividual + "> " + literal + ")");
						
						//Rule 29 - (iii)
						writer.println("ObjectPropertyAssertion(:" + op + " <" + recordIndividual + "> <" + fieldIndividual + ">)");
					} else
						//Rule 29 - (iv)
						writer.println("DataPropertyAssertion(:" + dp + " <" + recordIndividual + "> " + literal + ")");
				}
			}
		}
	}

	public File generateTBoxFile() {
		preprocess(false);
		writePrefixes();
		writeTBox();
		postprocess();
		
		System.out.println("Translating the schema into OWL has finished.");
		
		return output;
	}
	
	public File dumpKB() {
		preprocess(true);
		writePrefixes();
		writeTBox();
		dumpABox();
		postprocess();
		
		System.out.println("The knowledge base dump has finished.");
		
		return output;
	}
}
