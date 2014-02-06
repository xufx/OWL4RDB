package janus.ontology;

import java.net.URI;
import java.util.Set;

import de.derivo.sparqldlapi.QueryEngine;

public interface OntBridge {
	// for ontology
	String dumpTBoxOntology(RenderingType renderingType);
	URI getOntologyID();
	
	// for class hierarchy
	URI getOWLThingURI();
	URI getOWLNothingURI();
	Set<URI> getSubClses(URI clsURI);
	boolean hasEquivlntCls(URI clsURI);
	Set<String> getKeys(URI clsURI);

	// for object property hierarchy
	URI getOWLTopObjectProperty();
	URI getOWLBottomObjectProperty();
	Set<URI> getRootObjPropList();
	Set<URI> getSubObjProps(URI objPropURI);

	// for data property hierarchy
	URI getOWLTopDataProperty();
	URI getOWLBottomDataProperty();
	Set<URI> getRootDataPropList();
	Set<URI> getSubDataProps(URI dataPropURI);

	// for class description hierarchy
	Set<String> getAssertedAnonEquivlntClses(URI clsURI);
	Set<String> getAssertedAnonSuprclses(URI clsURI);
	Set<URI> getAssertedNamedEquivlntClses(URI clsURI);
	Set<URI> getAssertedNamedSuprclses(URI clsURI);
	Set<URI> getNamedDisjntClses(URI clsURI);
	Set<String> getAnonDisjntClses(URI clsURI);
	Set<String> getInferredAnonClses(URI clsURI);
	
	// for class query
	boolean areDisjointWith(URI clsURI1, URI clsURI2);
	boolean isEquivalentClass(URI clsURI1, URI clsURI2);
	boolean isSubClassOf(URI clsURI1, URI clsURI2);
	boolean containsClass(URI clsURI);
	Set<URI> getAllDisjointClasses(URI clsURI);
	Set<URI> getEquivalentClasses(URI clsURI);
	Set<URI> getAllSubClasses(URI clsURI);
	Set<URI> getAllSuperClasses(URI clsURI);
	Set<URI> getAllFamilyClasses(URI clsURI);
	Set<URI> getHasKeyDataProperties(URI clsURI);

	// for object property description hierarchy
	Set<URI> getNamedDisjntObjProps(URI objProptURI);
	Set<String> getAnonDisjntObjProps(URI objProptURI);
	Set<URI> getNamedEuivlntObjProps(URI objProptURI);
	Set<String> getAnonEuivlntObjProps(URI objProptURI);
	Set<URI> getNamedInverseProps(URI objProptURI);
	Set<String> getAnonInverseProps(URI objProptURI);
	Set<URI> getObjPropNamedDomains(URI objPropertyURI);
	Set<String> getObjPropAnonDomains(URI objPropertyURI);
	Set<URI> getObjPropNamedRanges(URI objProptURI);
	Set<String> getObjPropAnonRanges(URI objProptURI);
	Set<URI> getNamedSupObjProps(URI objProptURI);
	Set<String> getAnonSupObjProps(URI objProptURI);
	boolean isFunctionalObjProp(URI objProptURI);
	boolean isInverseFunctional(URI objProptURI);
	boolean isTransitive(URI objProptURI);
	boolean isSymmetric(URI objProptURI);
	boolean isAsymmetric(URI objProptURI);
	boolean isReflexive(URI objProptURI);
	boolean isIrreflexive(URI objProptURI);
	
	// for object property query
	boolean containsObjectProperty(URI objectProptURI);
	boolean isInverseObjectPropertyOf(URI objectProptURI1, URI objectProptURI2);
	boolean isEquivalentObjectPropertyOf(URI objectProptURI1, URI objectProptURI2);
	boolean isSubObjectPropertyOf(URI objPropURI1, URI objPropURI2);
	Set<URI> getAllSubObjProps(URI objPropURI);
	Set<URI> getAllSuperObjProps(URI objPropURI);
	Set<URI> getAllObjProps();
	Set<URI> getAllTransitiveObjProps();
	Set<URI> getAllSymmetricObjProps();
	Set<URI> getAllInverseFunctionalObjProps();
	Set<URI> getAllFunctionalObjProps();
	Set<URI> getInverseObjectProperties(URI objPropURI);
	Set<URI> getEquivalentObjectProperties(URI objPropURI);

	// for data property description hierarchy
	Set<URI> getNamedDataPropDomains(URI dataPropertyURI);
	Set<String> getAnonDataPropDomains(URI dataPropertyURI);
	Set<URI> getDataPropRangesOfDataType(URI dataProptURI);
	Set<String> getDataPropRangesButDataType(URI dataProptURI);
	Set<URI> getNamedDisjntDataProps(URI dataProptURI);
	Set<String> getAnonDisjntDataProps(URI dataProptURI);
	Set<URI> getNamedEuivlntDataProps(URI dataProptURI);
	Set<String> getAnonEuivlntDataProps(URI dataProptURI);
	Set<URI> getNamedSupDataProps(URI dataProptURI);
	Set<String> getAnonSupDataProps(URI dataProptURI);
	boolean isFunctionalDataProp(URI dataProptURI);
	
	
	// for data property query
	boolean containsDataProperty(URI dataProptURI);
	boolean isEquivalentDataPropertyOf(URI dataProptURI1, URI dataProptURI2);
	boolean isSubDataPropertyOf(URI dataPropURI1, URI dataPropURI2);
	Set<URI> getAllSubDataProps(URI objPropURI);
	Set<URI> getAllSuperDataProps(URI dataPropURI);
	Set<URI> getAllDataProps();
	Set<URI> getAllFunctionalDataProps();
	Set<URI> getEquivalentDataProperties(URI dataPropURI);

	// for reasoner management
	void disposeReasoner();
	
	// for SPARQL-DL
	public void executeQuery(String sparqldl);
	public Set<String> getSPARQLDLAtoms();
	public QueryEngine getQueryEngine();
	
	// for SWRL
	public Set<URI> getSWRLBuiltIns();
}