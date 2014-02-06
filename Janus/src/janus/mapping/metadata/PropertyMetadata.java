package janus.mapping.metadata;

class PropertyMetadata implements Comparable<PropertyMetadata> {
	private String propertyName;

	private PropertyTypes propertyType;
	
	private String mappedTable;
	private String mappedColumn;

	private String domain;
	private String range;
	
	private String superProperty;

	PropertyMetadata(String propertyName, PropertyTypes propertyType, String mappedTable, String mappedColumn) {
		this.propertyName = propertyName;
		this.mappedTable = mappedTable;
		this.mappedColumn = mappedColumn;
		
		this.propertyType = propertyType;
	}
	
	public String getMappedTable() {
		return mappedTable;
	}
	
	public String getMappedColumn() {
		return mappedColumn;
	}
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}
	
	public String getSuperProperty() {
		return superProperty;
	}

	public void setSuperProperty(String superProperty) {
		this.superProperty = superProperty;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public PropertyTypes getPropertyType() {
		return propertyType;
	}

	@Override
	public int compareTo(PropertyMetadata o) {
		return propertyName.compareTo(o.getPropertyName());
	}
}
