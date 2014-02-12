package janus.mapping.metadata;

import janus.mapping.DBField;

import java.net.URI;
import java.net.URISyntaxException;
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
			if (property.getPropertyName().equals(propertyName))
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
	
	public String getMappedColumnNameToProperty(URI propertyURI) {
		String propertyName = propertyURI.getFragment();
		for (PropertyMetadata property: properties) {
			if (property.getPropertyName().equals(propertyName))
				return property.getMappedColumn();
		}
		
		return null;
	}
	
	public ClassTypes getClassType(URI classURI) {
		if (classURI.toString().equals(ClassTypes.OWL_THING.value()))
			return ClassTypes.OWL_THING;
		
		String className = classURI.getFragment();
		for (ClassMetadata cls: classes) {
			if (cls.getClassName().equals(className))
				return cls.getClassType();
		}
		
		return null;
	}
	
	public URI getMappedDataProperty(String table, String column) {
		return getURIOfFragment(getMappedDataPropertyFragment(table, column));
	}
	
	public String getMappedDataPropertyFragment(String table, String column) {
		for (PropertyMetadata property: properties)
			if (property.getMappedTable().equals(table) 
					&& property.getMappedColumn().equals(column) 
					&& property.getPropertyType().equals(PropertyTypes.DATA_PROPERTY))
				return property.getPropertyName();
		
		return null;
	}
	
	public URI getMappedObjectProperty(String table, String column) {
		return getURIOfFragment(getMappedObjectPropertyFragment(table, column));
	}
	
	public String getMappedObjectPropertyFragment(String table, String column) {
		String op = null;
		
		for (PropertyMetadata property: properties)
			if (property.getMappedTable().equals(table) 
					&& property.getMappedColumn().equals(column) 
					&& property.getPropertyType().equals(PropertyTypes.OBJECT_PROPERTY))
				op = property.getPropertyName();
		
		return op;
	}
	
	public URI getMappedClass(String table) {
		return getURIOfFragment(getMappedClassFragment(table));
	}
	
	public String getMappedClassFragment(String table) {
		for (ClassMetadata cls: classes)
			if (cls.getMappedTableName().equals(table) 
					&& cls.getClassType().equals(ClassTypes.TABLE_CLASS))
				return cls.getClassName();
		
		return null;
	}
	
	public URI getMappedClass(String table, String column) {
		return getURIOfFragment(getMappedClassFragment(table, column));
	}
	
	public String getMappedClassFragment(String table, String column) {
		for (ClassMetadata cls: classes)
			if (cls.getClassType().equals(ClassTypes.COLUMN_CLASS) 
					&& cls.getMappedTableName().equals(table) 
					&& cls.getMappedColumnName().equals(column))
				return cls.getClassName();
		
		return null;
	}
	
	public URI getMappedIndividual(String table, String column, String value) {
		return IndividualMetadata.getMappedIndividual(table, column, value);
	}
	
	public String getMappedIndividualFragment(String table, String column, String value) {
		return IndividualMetadata.getMappedIndividualFragment(table, column, value);
	}
	
	public URI getMappedIndividual(String table, List<DBField> pkFields) {
		return IndividualMetadata.getMappedIndividual(table, pkFields);
	}
	
	public String getMappedIndividualFragment(String table, List<DBField> pkFields) {
		return IndividualMetadata.getMappedIndividualFragment(table, pkFields);
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
	
	public IndividualTypes getIndividualType(URI individual) {
		return IndividualMetadata.getIndividualType(individual);
	}
	
	public URI getIndividual(String individualFragment) {
		return IndividualMetadata.getIndividual(individualFragment);
	}
	
	public URI getClassURI(String classFragment) {
		return getURIOfFragment(classFragment);
	}
	
	private URI getURIOfFragment(String fragment) {
		URI uri = null;
		try {
			uri =  new URI(ontologyID + "#" + fragment);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return uri;
	}
	
	URI getOntologyID() {
		return ontologyID;
	}
}
