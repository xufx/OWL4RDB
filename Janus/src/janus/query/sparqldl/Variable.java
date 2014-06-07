package janus.query.sparqldl;

class Variable {
	private String name;

	private VariableTypes type;
	
	Variable(String name) {
		this.name = name;
		type = VariableTypes.UNKNOWN;
	}
	
	String getName() {
		return name;
	}
	
	VariableTypes getType() {
		return type;
	}
	
	void setType(VariableTypes type) {
		if (this.type.equals(type) 
				|| type.equals(VariableTypes.PROPERTIES) && (this.type.equals(VariableTypes.DATA_PROPERTIES) || this.type.equals(VariableTypes.OBJECT_PROPERTIES))
				|| type.equals(VariableTypes.INDIVIDUALS_OR_LITERALS) && (this.type.equals(VariableTypes.INDIVIDUALS) || this.type.equals(VariableTypes.LITERALS)))
			return;
		
		if (this.type.equals(VariableTypes.UNKNOWN) 
				|| (this.type.equals(VariableTypes.PROPERTIES) && (type.equals(VariableTypes.DATA_PROPERTIES) || type.equals(VariableTypes.OBJECT_PROPERTIES)))
				|| (this.type.equals(VariableTypes.INDIVIDUALS_OR_LITERALS) && (type.equals(VariableTypes.INDIVIDUALS) || type.equals(VariableTypes.LITERALS)))) {
			this.type = type;
			return;
		} 
		
		try {
			throw new Exception();
		} catch (Exception e) {
			System.out.println("The use of the variable" + name + " is wrong.");
			this.type = VariableTypes.WRONG;
		}
	}
}
