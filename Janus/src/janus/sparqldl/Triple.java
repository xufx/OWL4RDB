package janus.sparqldl;

import java.net.URI;

class Triple {
	private URI subject;
	private IndividualSet subjectIndividual;
	
	private URI predicate;
	
	private URI object;
	private IndividualSet objectIndividual;
	private LiteralSet objectLiteral;
	private String objectString;
	
	Triple(URI subject, URI predicate, IndividualSet objectIndividual) {
		this.subject = subject;
		this.predicate = predicate;
		this.objectIndividual = objectIndividual;
	}
	
	Triple(URI subject, URI predicate, LiteralSet objectLiteral) {
		this.subject = subject;
		this.predicate = predicate;
		this.objectLiteral = objectLiteral;
	}
	
	Triple(IndividualSet subjectIndividual, URI predicate, URI object) {
		this.subjectIndividual = subjectIndividual;
		this.predicate = predicate;
		this.object = object;
	}
	
	Triple(IndividualSet subjectIndividual, URI predicate, String objectString) {
		this.subjectIndividual = subjectIndividual;
		this.predicate = predicate;
		this.objectString = objectString;
	}
	
	Triple(IndividualSet subjectIndividual, URI predicate, IndividualSet objectIndividual) {
		this.subjectIndividual = subjectIndividual;
		this.predicate = predicate;
		this.objectIndividual = objectIndividual;
	}
	
	Triple(IndividualSet subjectIndividual, URI predicate, LiteralSet objectLiteral) {
		this.subjectIndividual = subjectIndividual;
		this.predicate = predicate;
		this.objectLiteral = objectLiteral;
	}
	
	URI getSubject() {
		return subject;
	}
	
	void setSubject(URI subject) {
		this.subject = subject;
	}
	
	URI getPredicate() {
		return predicate;
	}
	
	void setPredicate(URI predicate) {
		this.predicate = predicate;
	}
	
	IndividualSet getObjectIndividual() {
		return objectIndividual;
	}
	
	void setObjectIndividual(IndividualSet objectIndividual) {
		this.objectIndividual = objectIndividual;
	}
	
	LiteralSet getObjectLiteral() {
		return objectLiteral;
	}
	
	void setObjectLiteral(LiteralSet objectLiteral) {
		this.objectLiteral = objectLiteral;
	}
	
	IndividualSet getSubjectIndividual() {
		return subjectIndividual;
	}

	void setSubjectIndividual(IndividualSet subjectIndividual) {
		this.subjectIndividual = subjectIndividual;
	}
	
	URI getObject() {
		return object;
	}

	void setObject(URI object) {
		this.object = object;
	}
	
	String getObjectString() {
		return objectString;
	}

	void setObjectString(String objectString) {
		this.objectString = objectString;
	}
}
