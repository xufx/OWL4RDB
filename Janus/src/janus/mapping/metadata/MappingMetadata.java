package janus.mapping.metadata;

import janus.Janus;
import janus.database.DBColumn;
import janus.database.DBField;
import janus.mapping.DatatypeMap;
import janus.mapping.OntEntityTypes;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class MappingMetadata {
	private URI ontologyID;

	private Set<ClassMetadata> classes;
	private Set<PropertyMetadata> properties;
	
	public MappingMetadata(URI ontologyID) {
		this.ontologyID = ontologyID;
		
		classes = new ConcurrentSkipListSet<ClassMetadata>();
		properties = new ConcurrentSkipListSet<PropertyMetadata>();
	}
	
	public void addOWLClassMetaData(ClassMetadata classMetaData) {
		classes.add(classMetaData);
	}
	
	public void addOWLPropertyMetaData(PropertyMetadata propertyMetaData) {
		properties.add(propertyMetaData);
	}
	
	public String getMappedTableNameToClass(URI classURI) {
		String className = classURI.getFragment();
		for (ClassMetadata cls: classes) {
			if (cls.getClassName().equals(className))
				return cls.getMappedTableName();
		}
		
		return null;
	}
	
	public String getMappedTableNameToProperty(URI propertyURI) {
		String propertyName = propertyURI.getFragment();
		for (PropertyMetadata property: properties) {
			if (property.getPropertyFragment().equals(propertyName))
				return property.getMappedTable();
		}
		
		return null;
	}
	
	public String getMappedColumnNameToClass(URI classURI) {
		String className = classURI.getFragment();
		for (ClassMetadata cls: classes) {
			if (cls.getClassName().equals(className))
				return cls.getMappedColumnName();
		}
		
		return null;
	}
	
	public DBColumn getMappedColumnToClass(URI classURI) {
		String className = classURI.getFragment();
		for (ClassMetadata cls: classes) {
			if (cls.getClassName().equals(className))
				return cls.getMappedColumn();
		}
		
		return null;
	}
	
	public String getMappedColumnNameToProperty(URI propertyURI) {
		String propertyName = propertyURI.getFragment();
		for (PropertyMetadata property: properties) {
			if (property.getPropertyFragment().equals(propertyName))
				return property.getMappedColumnName();
		}
		
		return null;
	}
	
	public DBColumn getMappedColumnToProperty(URI propertyURI) {
		String propertyName = propertyURI.getFragment();
		for (PropertyMetadata property: properties) {
			if (property.getPropertyFragment().equals(propertyName))
				return property.getMappedColumn();
		}
		
		return null;
	}
	
	public OntEntityTypes getClassType(URI classURI) {
		if (classURI.toString().equals(OntEntityTypes.OWL_THING_CLASS.pattern()))
			return OntEntityTypes.OWL_THING_CLASS;
		
		String className = classURI.getFragment();
		for (ClassMetadata cls: classes) {
			if (cls.getClassName().equals(className))
				return cls.getClassType();
		}
		
		return null;
	}
	
	public OntEntityTypes getPropertyType(URI propertyURI) {
		String property = propertyURI.toString();
		
		if (property.equals(OntEntityTypes.OWL_TOP_DATA_PROPERTY.pattern()))
			return OntEntityTypes.OWL_TOP_DATA_PROPERTY;
		
		if (property.equals(OntEntityTypes.OWL_TOP_OBJECT_PROPERTY.pattern()))
			return OntEntityTypes.OWL_TOP_OBJECT_PROPERTY;
		
		String propertyFragment = propertyURI.getFragment();
		for (PropertyMetadata p: properties) {
			if (p.getPropertyFragment().equals(propertyFragment))
				return p.getPropertyType();
		}
		
		return null;
	}
	
	public URI getMappedDataProperty(String table, String column) {
		return getURIOfFragment(getMappedDataPropertyFragment(table, column));
	}
	
	public String getMappedDataPropertyFragment(String table, String column) {
		for (PropertyMetadata property: properties)
			if (property.getMappedTable().equals(table) 
					&& property.getMappedColumnName().equals(column) 
					&& property.getPropertyType().equals(OntEntityTypes.DATA_PROPERTY))
				return property.getPropertyFragment();
		
		return null;
	}
	
	public URI getMappedObjectProperty(String table, String column) {
		return getURIOfFragment(getMappedObjectPropertyFragment(table, column));
	}
	
	public String getMappedObjectPropertyFragment(String table, String column) {
		String op = null;
		
		for (PropertyMetadata property: properties)
			if (property.getMappedTable().equals(table) 
					&& property.getMappedColumnName().equals(column) 
					&& property.getPropertyType().equals(OntEntityTypes.OBJECT_PROPERTY))
				op = property.getPropertyFragment();
		
		return op;
	}
	
	public URI getMappedClass(String table) {
		return getURIOfFragment(getMappedClassFragment(table));
	}
	
	public String getMappedClassFragment(String table) {
		for (ClassMetadata cls: classes)
			if (cls.getMappedTableName().equals(table) 
					&& cls.getClassType().equals(OntEntityTypes.TABLE_CLASS))
				return cls.getClassName();
		
		return null;
	}
	
	public URI getMappedClass(String table, String column) {
		return getURIOfFragment(getMappedClassFragment(table, column));
	}
	
	public String getMappedClassFragment(String table, String column) {
		for (ClassMetadata cls: classes)
			if (cls.getClassType().equals(OntEntityTypes.COLUMN_CLASS) 
					&& cls.getMappedTableName().equals(table) 
					&& cls.getMappedColumnName().equals(column))
				return cls.getClassName();
		
		return null;
	}
	
	public URI getDomainClassOfProperty(URI prop) {
		String propertyFragment = prop.getFragment();
		
		for (PropertyMetadata property: properties) {
			if (property.getPropertyFragment().equals(propertyFragment))
				return getURIOfFragment(property.getDomain());
		}
		
		return null;
	}
	
	public URI getMappedIndividual(String table, String column, String value) {
		return IndividualMetadata.getMappedFieldIndividual(table, column, value);
	}
	
	public String getMappedIndividualFragment(String table, String column, String value) {
		return IndividualMetadata.getMappedFieldIndividualFragment(table, column, value);
	}
	
	public URI getMappedIndividual(String table, List<DBField> pkFields) {
		return IndividualMetadata.getMappedRecordIndividual(table, pkFields);
	}
	
	public String getMappedIndividualFragment(String table, List<DBField> pkFields) {
		return IndividualMetadata.getMappedRecordIndividualFragment(table, pkFields);
	}
	
	public String getMappedLiteral(String table, String column, String value) {
		return LiteralMetadata.getMappedLiteral(table, column, value);
	}
	
	public String getMappedTableNameToRecordIndividual(URI individual) {
		return IndividualMetadata.getMappedTableNameToRecordIndividual(individual);
	}
	
	public DBField getMappedDBFieldToFieldIndividual(URI individual) {
		return IndividualMetadata.getMappedDBFieldToFieldIndividual(individual);
	}
	
	public List<DBField> getMappedDBFieldsToRecordIndividual(URI individual) {
		return IndividualMetadata.getMappedDBFieldsToRecordIndividual(individual);
	}
	
	public OntEntityTypes getIndividualType(URI individual) {
		return IndividualMetadata.getIndividualType(individual);
	}
	
	public URI getIndividual(String individualFragment) {
		return IndividualMetadata.getIndividual(individualFragment);
	}
	
	public URI getClassURI(String classFragment) {
		return getURIOfFragment(classFragment);
	}
	
	public URI getObjectProperty(String opFragment) {
		return getURIOfFragment(opFragment);
	}
	
	public URI getDataProperty(String dpFragment) {
		return getURIOfFragment(dpFragment);
	}
	
	private URI getURIOfFragment(String fragment) {
		return URI.create(ontologyID + "#" + fragment);
	}
	
	URI getOntologyID() {
		return ontologyID;
	}
	
	public String getDatatypeOfTypedLiteral(String typedLiteral) {
		return LiteralMetadata.getDatatype(typedLiteral);
	}
	
	public String getLexicalValueOfTypedLiteral(String typedLiteral) {
		return LiteralMetadata.getLexicalValue(typedLiteral);
	}
	
	public Set<DBColumn> getMappedDBColumnsToDatatypeOfTypedLiteral(String datatypeOfTypedLiteral) {
		Set<Integer> mappedSQLTypes = DatatypeMap.getMappedSQLTypes(datatypeOfTypedLiteral);
		
		Set<DBColumn> mappedDBColumns = new ConcurrentSkipListSet<DBColumn>();
		
		Set<String> tables = Janus.cachedDBMetadata.getTableNames();
		
		for(String table: tables) {
			Set<String> columns = Janus.cachedDBMetadata.getColumns(table);
			
			for(String column: columns)
				if (mappedSQLTypes.contains(Janus.cachedDBMetadata.getDataType(table, column)))
					mappedDBColumns.add(new DBColumn(table, column));
		}
		
		return mappedDBColumns;
	}
}
