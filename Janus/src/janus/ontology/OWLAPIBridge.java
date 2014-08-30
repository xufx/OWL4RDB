package janus.ontology;

import java.io.File;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;
import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import de.derivo.sparqldlapi.types.QueryAtomType;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

class OWLAPIBridge implements OntBridge {
	static OntBridge getInstance(File ontFile, ReasonerType reasonerType) {
		return new OWLAPIBridge(ontFile, reasonerType);
	}
	static OntBridge getInstance(String ontURI, ReasonerType reasonerType) {
		return new OWLAPIBridge(ontURI, reasonerType);
	}
	private OWLReasoner reasoner;
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	
	private ManchesterOWLSyntaxOWLObjectRendererImpl renderer;
	
	private QueryEngine queryEngine;
	
	private OWLAPIBridge(File ontFile, ReasonerType reasonerType) {
		manager = OWLManager.createOWLOntologyManager();
		
		try {
			ontology = manager.loadOntologyFromOntologyDocument(ontFile);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		initReasoner(reasonerType);
		
		initQueryEngine();
		
		initRenderer();
		
		System.out.println("Total Axiom Count Before Reasoning: " + ontology.getAxiomCount());
		
		InferredOntologyGenerator inferredOntologyGenerator = new InferredOntologyGenerator(reasoner);
		inferredOntologyGenerator.fillOntology(manager, ontology);
		
		System.out.println("Total Axiom Count After Reasoning: " + ontology.getAxiomCount());
	}
	
	private OWLAPIBridge(String ontURI, ReasonerType reasonerType) {
		manager = OWLManager.createOWLOntologyManager();
		
		IRI documentIRI = IRI.create(ontURI);
		try {
			ontology = manager.loadOntologyFromOntologyDocument(documentIRI);
		} catch (OWLOntologyCreationException e) { e.printStackTrace(); }
		
		initReasoner(reasonerType);
		
		initQueryEngine();
		
		initRenderer();
	}
	
	public boolean areDisjointWith(URI clsURI1, URI clsURI2) {
		OWLClass cls1 = getOWLCls(clsURI1);
		
		NodeSet<OWLClass> nodeSet = reasoner.getDisjointClasses(cls1);
		Set<Node<OWLClass>> nodes = nodeSet.getNodes();
		for (Node<OWLClass> node : nodes) {
			Set<OWLClass> entities = node.getEntities();
			if (!entities.isEmpty()) {
				OWLClass cls2 = getOWLCls(clsURI2);
				if (entities.contains(cls2))
					return true;
			}
		}
		
		return false;
	}
	
	public boolean containsClass(URI clsURI) {
		return ontology.containsClassInSignature(IRI.create(clsURI), true);
	}
	
	public boolean containsDataProperty(URI dataProptURI) {
		return ontology.containsDataPropertyInSignature(IRI.create(dataProptURI), true);
	}
	
	public boolean containsObjectProperty(URI objectProptURI) {
		return ontology.containsObjectPropertyInSignature(IRI.create(objectProptURI), true);
	}
	
	public void disposeReasoner() {
		reasoner.dispose();
	}
	
	public String dumpTBoxOntology(RenderingType renderingType) {
		OWLOntologyFormat format = manager.getOntologyFormat(ontology);
		OWLOntologyDocumentTarget documentTarget = new StringDocumentTarget();
		
		PrefixOWLOntologyFormat renderingFormat;
		if (renderingType.equals(RenderingType.FUNCTIONAL_SYNTAX))
			renderingFormat = new OWLFunctionalSyntaxOntologyFormat();
		else
			renderingFormat = new RDFXMLOntologyFormat();
		
		if(format.isPrefixOWLOntologyFormat())
			renderingFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
		
		try {
			manager.saveOntology(ontology, renderingFormat, documentTarget);
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
		
		return documentTarget.toString();
	}
	
	public void executeQuery(String sparql_dl_query) {
		Query query = null;
		
		try {
			query = Query.create(sparql_dl_query);
		} catch (QueryParserException e) {
			e.printStackTrace();
		}
		
		System.out.println("Requsted Query: " + query);
		
		try {
			long start = System.currentTimeMillis();
			
			QueryResult queryResult = queryEngine.execute(query);
			
			if (query.isAsk())
				System.out.println(queryResult.ask());
			else
				System.out.println(queryResult.toString());
			;
			System.out.println("...finished");
			long end = System.currentTimeMillis();
			System.out.println( "(Through In-Memory Reasoner) 질의 처리 시간 : " + ( end - start));
		} catch (QueryEngineException e) {
			e.printStackTrace();
		}
	}
	
	public Set<URI> getAllDataProps() {
		Set<URI> allDataProps = new ConcurrentSkipListSet<URI>();
		
		URI OWLTopDataProperty = getOWLTopDataProperty();
		allDataProps.add(OWLTopDataProperty);
		
		allDataProps.addAll(getAllSubDataProps(OWLTopDataProperty));
		
		return allDataProps;
	}
	
	public Set<URI> getAllDisjointClasses(URI clsURI) {
		OWLClass cls = getOWLCls(clsURI);
		
		Set<URI> classes = new ConcurrentSkipListSet<URI>();
		
		NodeSet<OWLClass> nodeSet = reasoner.getDisjointClasses(cls);
		Set<OWLClass> set = nodeSet.getFlattened();
		
		for (OWLClass owlClass : set)
			classes.add(owlClass.getIRI().toURI());		
		
		return classes;
	}
	
	public Set<URI> getAllFamilyClasses(URI clsURI) {
		Set<URI> family = getAllSubClasses(getOWLThingURI());
		family.removeAll(getAllDisjointClasses(clsURI));
		
		for(URI member: family) {
			OWLClass cls = getOWLCls(member);
			
			if (cls.getIRI().isReservedVocabulary())
				family.remove(member);
		}		
		
		return family;
	}
	
	public Set<URI> getAllFunctionalDataProps() {
		Set<URI> functionalDataProps = new ConcurrentSkipListSet<URI>();
		
		Set<URI> allDataProps = getAllDataProps();
		
		for (URI dataProp: allDataProps) {
			if (isFunctionalDataProp(dataProp))
				functionalDataProps.add(dataProp);
		}
		
		return functionalDataProps;
	}
	
	public Set<URI> getAllFunctionalObjProps() {
		Set<URI> functionalObjProps = new ConcurrentSkipListSet<URI>();
		
		Set<URI> allObjProps = getAllObjProps();
		
		for (URI objProp: allObjProps) {
			if (isFunctionalObjProp(objProp))
				functionalObjProps.add(objProp);
		}
		
		return functionalObjProps;
	}
	
	public Set<URI> getAllInverseFunctionalObjProps() {
		Set<URI> inverseFunctionalObjProps = new ConcurrentSkipListSet<URI>();
		
		Set<URI> allObjProps = getAllObjProps();
		
		for (URI objProp: allObjProps) {
			if (isInverseFunctional(objProp))
				inverseFunctionalObjProps.add(objProp);
		}
		
		return inverseFunctionalObjProps;
	}
	
	public Set<URI> getAllObjProps() {
		Set<URI> allObjProps = new ConcurrentSkipListSet<URI>();
		
		URI OWLTopObjectProperty = getOWLTopObjectProperty();
		allObjProps.add(OWLTopObjectProperty);
		
		allObjProps.addAll(getAllSubObjProps(OWLTopObjectProperty));
		
		return allObjProps;
	}
	
	public Set<URI> getAllSubClasses(URI clsURI) {
		OWLClass cls = getOWLCls(clsURI);
		
		Set<URI> classes = new ConcurrentSkipListSet<URI>();
		
		NodeSet<OWLClass> nodeSet = reasoner.getSubClasses(cls, false);
		Set<OWLClass> set = nodeSet.getFlattened();
		
		for (OWLClass owlClass : set)
			classes.add(owlClass.getIRI().toURI());
		
		classes.add(clsURI);
		
		return classes;
	}
	
	public Set<URI> getAllSubDataProps(URI dataPropURI) {
		OWLDataProperty dataProp = getOWLDataProp(dataPropURI);
		
		Set<URI> children = new ConcurrentSkipListSet<URI>();
		
		NodeSet<OWLDataProperty> nodeSet = reasoner.getSubDataProperties(dataProp, false);
		Set<OWLDataProperty> set = nodeSet.getFlattened();
		for (OWLDataProperty dp: set) {
			children.add(dp.getIRI().toURI());
		}
		
		return children;
	}
	
	public Set<URI> getAllSubObjProps(URI objPropURI) {
		OWLObjectProperty objProp = getOWLObjProp(objPropURI);
		
		Set<URI> children = new ConcurrentSkipListSet<URI>();
		
		NodeSet<OWLObjectPropertyExpression> nodeSet = reasoner.getSubObjectProperties(objProp, false);
		Set<OWLObjectPropertyExpression> set = nodeSet.getFlattened();
		for (OWLObjectPropertyExpression op: set) {
			if (!op.isAnonymous())
				children.add(op.asOWLObjectProperty().getIRI().toURI());
		}
		
		return children;
	}
	
	public Set<URI> getAllSuperClasses(URI clsURI) {
		OWLClass cls = getOWLCls(clsURI);
		
		Set<URI> classes = new ConcurrentSkipListSet<URI>();
		
		NodeSet<OWLClass> nodeSet = reasoner.getSuperClasses(cls, false);
		Set<OWLClass> set = nodeSet.getFlattened();
		
		for (OWLClass owlClass : set)
			classes.add(owlClass.getIRI().toURI());		
		
		return classes;
	}
	
	public Set<URI> getAllSuperDataProps(URI dataPropURI) {
		OWLDataProperty dataProp = getOWLDataProp(dataPropURI);
		
		Set<URI> superDataProps = new ConcurrentSkipListSet<URI>();
		
		NodeSet<OWLDataProperty> nodeSet = reasoner.getSuperDataProperties(dataProp, false);
		Set<OWLDataProperty> set = nodeSet.getFlattened();
		for (OWLDataProperty dp: set) {
			superDataProps.add(dp.getIRI().toURI());
		}
		
		return superDataProps;
	}
	
	public Set<URI> getAllSuperObjProps(URI objPropURI) {
		OWLObjectProperty objProp = getOWLObjProp(objPropURI);
		
		Set<URI> superObjProps = new ConcurrentSkipListSet<URI>();
		
		NodeSet<OWLObjectPropertyExpression> nodeSet = reasoner.getSuperObjectProperties(objProp, false);
		Set<OWLObjectPropertyExpression> set = nodeSet.getFlattened();
		for (OWLObjectPropertyExpression op: set) {
			if (!op.isAnonymous())
				superObjProps.add(op.asOWLObjectProperty().getIRI().toURI());
		}
		
		return superObjProps;
	}
	
	public Set<URI> getAllSymmetricObjProps() {
		Set<URI> symmetricObjProps = new ConcurrentSkipListSet<URI>();
		
		Set<URI> allObjProps = getAllObjProps();
		
		for (URI objProp: allObjProps) {
			if (isSymmetric(objProp))
				symmetricObjProps.add(objProp);
		}
		
		return symmetricObjProps;
	}
	
	public Set<URI> getAllTransitiveObjProps() {
		Set<URI> transitiveObjProps = new ConcurrentSkipListSet<URI>();
		
		Set<URI> allObjProps = getAllObjProps();
		
		for (URI objProp: allObjProps) {
			if (isTransitive(objProp))
				transitiveObjProps.add(objProp);
		}
		
		return transitiveObjProps;
	}
	public Set<String> getAnonDataPropDomains(URI dataPropertyURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLDataProperty dataProperty = getOWLDataProp(dataPropertyURI);
		
		Set<OWLClassExpression> domains = dataProperty.getDomains(ontology);
		for(OWLClassExpression domain : domains)
			if(domain.isAnonymous()) set.add(render(domain));
		
		return set;
	}
	public Set<String> getAnonDisjntClses(URI clsURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLClass cls = getOWLCls(clsURI);
		
		Set<OWLClassExpression> disjointClses = cls.getDisjointClasses(ontology);
		for(OWLClassExpression disjointCls : disjointClses)
			if(disjointCls.isAnonymous()) set.add(this.render(disjointCls));
		
		return set;
	}
	public Set<String> getAnonDisjntDataProps(URI dataProptURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLDataProperty dataPropt = getOWLDataProp(dataProptURI);
		
		Set<OWLDataPropertyExpression> disjoints = dataPropt.getDisjointProperties(ontology);
		for(OWLDataPropertyExpression disjoint : disjoints)
			if(disjoint.isAnonymous()) set.add(render(disjoint));
		
		return set;
	}
	public Set<String> getAnonDisjntObjProps(URI objProptURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		Set<OWLObjectPropertyExpression> disjoints = objProperty.getDisjointProperties(ontology);
		for(OWLObjectPropertyExpression disjoint : disjoints)
			if(disjoint.isAnonymous()) set.add(render(disjoint));
		
		return set;
	}
	public Set<String> getAnonEuivlntDataProps(URI dataProptURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLDataProperty dataPropt = getOWLDataProp(dataProptURI);
		
		Set<OWLDataPropertyExpression> equis = dataPropt.getEquivalentProperties(ontology);
		for(OWLDataPropertyExpression equi : equis)
			if(equi.isAnonymous()) set.add(render(equi));
		
		return set;
	}
	public Set<String> getAnonEuivlntObjProps(URI objProptURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		Set<OWLObjectPropertyExpression> equis = objProperty.getEquivalentProperties(ontology);
		for(OWLObjectPropertyExpression equi : equis)
			if(equi.isAnonymous()) set.add(render(equi));
		
		return set;
	}
	
	public Set<String> getAnonInverseProps(URI objProptURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		Set<OWLObjectPropertyExpression> inverses = objProperty.getInverses(ontology);
		for(OWLObjectPropertyExpression inverse : inverses)
			if(inverse.isAnonymous()) set.add(render(inverse));
		
		return set;
	}
	
	public Set<String> getAnonSupDataProps(URI dataProptURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLDataProperty dataPropt = getOWLDataProp(dataProptURI);
		
		Set<OWLDataPropertyExpression> suProps = dataPropt.getSuperProperties(ontology);
		for(OWLDataPropertyExpression suProp : suProps)
			if(suProp.isAnonymous()) set.add(render(suProp));
		
		return set;
	}
	
	public Set<String> getAnonSupObjProps(URI objProptURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		Set<OWLObjectPropertyExpression> suProps = objProperty.getSuperProperties(ontology);
		for(OWLObjectPropertyExpression suProp : suProps)
			if(suProp.isAnonymous()) set.add(render(suProp));
		
		return set;
	}
	
	private Set<OWLClassExpression> getAssertedAnonEquivlntClses(OWLClass cls) {
		Set<OWLClassExpression> set = new ConcurrentSkipListSet<OWLClassExpression>();
		
		Set<OWLClassExpression> equiClaziz = cls.getEquivalentClasses(ontology);
		for(OWLClassExpression equiCls : equiClaziz)
			if(equiCls.isAnonymous())
				set.add(equiCls);
		
		return set;
	}
	
	public Set<String> getAssertedAnonEquivlntClses(URI clsURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLClass cls = getOWLCls(clsURI);
		
		Set<OWLClassExpression> equiClses = cls.getEquivalentClasses(ontology);
		for(OWLClassExpression e : equiClses)
			if(e.isAnonymous()) set.add(render(e));
		
		return set;
	}
	
	private Set<OWLClassExpression> getAssertedAnonSupclses(OWLClass cls) {
		Set<OWLClassExpression> set = new ConcurrentSkipListSet<OWLClassExpression>();
		
		Set<OWLClassExpression> suClaziz = cls.getSuperClasses(ontology);
		for(OWLClassExpression suCls : suClaziz)
			if(suCls.isAnonymous())
				set.add(suCls);
		
		return set;
	}
	
	public Set<String> getAssertedAnonSuprclses(URI clsURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLClass cls = getOWLCls(clsURI);
		
		Set<OWLClassExpression> superClses = cls.getSuperClasses(ontology);
		for(OWLClassExpression e : superClses)
			if(e.isAnonymous()) set.add(render(e));
		
		return set;
	}
	
	public Set<URI> getAssertedNamedEquivlntClses(URI clsURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLClass cls = getOWLCls(clsURI);
		
		Set<OWLClassExpression> equiClses = cls.getEquivalentClasses(ontology);
		for(OWLClassExpression e : equiClses)
			if(!e.isAnonymous()) set.add(e.asOWLClass().getIRI().toURI());
		
		return set;
	}
	
	public Set<URI> getAssertedNamedSuprclses(URI clsURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLClass cls = getOWLCls(clsURI);
		
		Set<OWLClassExpression> superClses = cls.getSuperClasses(ontology);
		for(OWLClassExpression e : superClses)
			if(!e.isAnonymous()) set.add(e.asOWLClass().getIRI().toURI());
		
		return set;
	}
	
	public Set<String> getDataPropRangesButDataType(URI dataProptURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLDataProperty dataPropt = getOWLDataProp(dataProptURI);
		
		Set<OWLDataRange> ranges = dataPropt.getRanges(ontology);
		for(OWLDataRange range : ranges)
			if(!range.isDatatype()) set.add(render(range));
		
		return set;
	}
	
	public Set<URI> getDataPropRangesOfDataType(URI dataProptURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLDataProperty dataPropt = getOWLDataProp(dataProptURI);
		
		Set<OWLDataRange> ranges = dataPropt.getRanges(ontology);
		for(OWLDataRange range : ranges)
			if(range.isDatatype()) set.add(((OWLDatatype)range).getIRI().toURI());
		
		return set;
	}
	
	public Set<URI> getEquivalentClasses(URI clsURI) {
		OWLClass cls = getOWLCls(clsURI);
		
		Set<URI> classes = new ConcurrentSkipListSet<URI>();
		
		Node<OWLClass> node = reasoner.getEquivalentClasses(cls);
		Set<OWLClass> set = node.getEntities();
		
		for (OWLClass owlClass : set)
			classes.add(owlClass.getIRI().toURI());		
		
		return classes;
	}
	
	public Set<URI> getEquivalentDataProperties(URI dataPropURI) {
		OWLDataProperty dataProp = getOWLDataProp(dataPropURI);
		
		Set<URI> equivalentDataProps = new ConcurrentSkipListSet<URI>();
		
		Node<OWLDataProperty> node = reasoner.getEquivalentDataProperties(dataProp);
		Set<OWLDataProperty> set = node.getEntities();
		for (OWLDataProperty dp: set)
			equivalentDataProps.add(dp.getIRI().toURI());
		
		return equivalentDataProps;
	}
	
	public Set<URI> getEquivalentObjectProperties(URI objPropURI) {
		OWLObjectProperty objProp = getOWLObjProp(objPropURI);
		
		Set<URI> equivalentObjProps = new ConcurrentSkipListSet<URI>();
		
		Node<OWLObjectPropertyExpression> node = reasoner.getEquivalentObjectProperties(objProp);
		Set<OWLObjectPropertyExpression> set = node.getEntities();
		for (OWLObjectPropertyExpression op: set) {
			if (!op.isAnonymous())
				equivalentObjProps.add(op.asOWLObjectProperty().getIRI().toURI());
		}
		
		return equivalentObjProps;
	}
	
	private Set<String> getInferredAnonClses(OWLClass cls, Set<OWLClass> setOfExclusion) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		setOfExclusion.add(cls);
		
		Set<OWLClass> superclses = getInferredNamedSupclses(cls, setOfExclusion);
		for(OWLClass supercls : superclses) {
				Set<OWLClassExpression> anonSuperclses = getAssertedAnonSupclses(supercls);
				for(OWLClassExpression anonSupercls : anonSuperclses)
					set.add(render(anonSupercls));

				Set<OWLClassExpression> anonEquiClses = getAssertedAnonEquivlntClses(supercls);
				for(OWLClassExpression anonEquiCls : anonEquiClses)
					set.add(render(anonEquiCls));

				Set<String> anonGrandClses = getInferredAnonClses(supercls, setOfExclusion);
				for(String anonGrandcls : anonGrandClses)
					set.add(anonGrandcls);
			}
		
		return set;
	}
	
	public Set<String> getInferredAnonClses(URI clsURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLClass cls = getOWLCls(clsURI);
		
		Set<OWLClass> setOfExclusion = new ConcurrentSkipListSet<OWLClass>();
		setOfExclusion.add(cls);
		
		Set<OWLClass> superclses = getInferredNamedSupclses(cls, setOfExclusion);
		for(OWLClass supercls : superclses) {
				Set<OWLClassExpression> anonSuperclses = getAssertedAnonSupclses(supercls);
				for(OWLClassExpression anonSupercls : anonSuperclses)
					set.add(render(anonSupercls));

				Set<OWLClassExpression> anonEquiClses = getAssertedAnonEquivlntClses(supercls);
				for(OWLClassExpression anonEquiCls : anonEquiClses)
					set.add(render(anonEquiCls));

				Set<String> anonGrandClses = getInferredAnonClses(supercls, setOfExclusion);
				for(String anonGrandcls : anonGrandClses)
					set.add(anonGrandcls);
			}
		
		return set;
	}
	
	private Set<OWLClass> getInferredNamedSupclses(OWLClass cls, Set<OWLClass> setOfExclusion) {
		Set<OWLClass> set = new ConcurrentSkipListSet<OWLClass>();
		
		NodeSet<OWLClass> superclsesNodeSet = reasoner.getSuperClasses(cls, true);
		Set<OWLClass> superclses = superclsesNodeSet.getFlattened();
		for(OWLClass supercls : superclses)
				if(!setOfExclusion.contains(supercls))
					set.add(supercls);
		
		return set;
	}
	
	public Set<URI> getInverseObjectProperties(URI objPropURI) {
		OWLObjectProperty objProp = getOWLObjProp(objPropURI);
		
		Set<URI> inverseObjProps = new ConcurrentSkipListSet<URI>();
		
		Node<OWLObjectPropertyExpression> node = reasoner.getInverseObjectProperties(objProp);
		Set<OWLObjectPropertyExpression> set = node.getEntities();
		for (OWLObjectPropertyExpression op: set) {
			if (!op.isAnonymous())
				inverseObjProps.add(op.asOWLObjectProperty().getIRI().toURI());
		}
		
		return inverseObjProps;
	}
	
	public Set<String> getKeys(URI clsURI) {
		Set<String> KeyPropsForAllHasKeyAxioms = new ConcurrentSkipListSet<String>();
		
		OWLClass cls = getOWLCls(clsURI);
		Set<OWLHasKeyAxiom> keys = ontology.getHasKeyAxioms(cls);
		
		for (OWLHasKeyAxiom key: keys) {
			StringBuffer keyPropsForOneHasKeyAxiom = new StringBuffer();
			
			Set<OWLObjectPropertyExpression> oProps = key.getObjectPropertyExpressions();
			for (OWLObjectPropertyExpression oProp: oProps)
				keyPropsForOneHasKeyAxiom.append(render(oProp) + ", ");
			
			Set<OWLDataPropertyExpression> dProps = key.getDataPropertyExpressions();
			for (OWLDataPropertyExpression dProp: dProps)
				keyPropsForOneHasKeyAxiom.append(render(dProp) + ", ");
			
			KeyPropsForAllHasKeyAxioms.add(keyPropsForOneHasKeyAxiom.substring(0, keyPropsForOneHasKeyAxiom.lastIndexOf(", ")));
		}
		
		return KeyPropsForAllHasKeyAxioms;
	}
	
	public Set<URI> getNamedDataPropDomains(URI dataPropertyURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLDataProperty dataProperty = getOWLDataProp(dataPropertyURI);
		
		Set<OWLClassExpression> domains = dataProperty.getDomains(ontology);
		for(OWLClassExpression domain : domains)
			if(!domain.isAnonymous()) set.add(domain.asOWLClass().getIRI().toURI());
		
		return set;
	}
	
	public Set<URI> getNamedDisjntClses(URI clsURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLClass cls = getOWLCls(clsURI);
		
		NodeSet<OWLClass> nodeSet = reasoner.getDisjointClasses(cls);
		Set<Node<OWLClass>> nodes = nodeSet.getNodes();
		for (Node<OWLClass> node : nodes) {
			Set<OWLClass> entities = node.getEntities();
			for (OWLClass entity: entities)
				if(!entity.isAnonymous()) 
					set.add(entity.getIRI().toURI());
		}
		
		// no use of a reasoner
		/*Set<OWLClassExpression> disjointClses = cls.getDisjointClasses(ontology);
		for(OWLClassExpression disjointCls : disjointClses)
			if(!disjointCls.isAnonymous()) set.add(disjointCls.asOWLClass().getIRI().toURI());*/
		
		return set;
	}
	
	public Set<URI> getNamedDisjntDataProps(URI dataProptURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLDataProperty dataPropt = getOWLDataProp(dataProptURI);
		
		Set<OWLDataPropertyExpression> disjoints = dataPropt.getDisjointProperties(ontology);
		for(OWLDataPropertyExpression disjoint : disjoints)
			if(!disjoint.isAnonymous()) set.add(disjoint.asOWLDataProperty().getIRI().toURI());
		
		return set;
	}
	
	public Set<URI> getNamedDisjntObjProps(URI objProptURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		Set<OWLObjectPropertyExpression> disjoints = objProperty.getDisjointProperties(ontology);
		for(OWLObjectPropertyExpression disjoint : disjoints)
			if(!disjoint.isAnonymous()) set.add(disjoint.asOWLObjectProperty().getIRI().toURI());
		
		return set;
	}
	
	public Set<URI> getNamedEuivlntDataProps(URI dataProptURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLDataProperty dataPropt = getOWLDataProp(dataProptURI);
		
		Set<OWLDataPropertyExpression> equis = dataPropt.getEquivalentProperties(ontology);
		for(OWLDataPropertyExpression equi : equis)
			if(!equi.isAnonymous()) set.add(equi.asOWLDataProperty().getIRI().toURI());
		
		return set;
	}
	
	public Set<URI> getNamedEuivlntObjProps(URI objProptURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		Set<OWLObjectPropertyExpression> equis = objProperty.getEquivalentProperties(ontology);
		for(OWLObjectPropertyExpression equi : equis)
			if(!equi.isAnonymous()) set.add(equi.asOWLObjectProperty().getIRI().toURI());
		
		return set;
	}
	
	public Set<URI> getNamedInverseProps(URI objProptURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		Set<OWLObjectPropertyExpression> inverses = objProperty.getInverses(ontology);
		for(OWLObjectPropertyExpression inverse : inverses)
			if(!inverse.isAnonymous()) set.add(inverse.asOWLObjectProperty().getIRI().toURI());
		
		return set;
	}
	
	public Set<URI> getNamedSupDataProps(URI dataProptURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLDataProperty dataPropt = getOWLDataProp(dataProptURI);
		
		Set<OWLDataPropertyExpression> suProps = dataPropt.getSuperProperties(ontology);
		for(OWLDataPropertyExpression suProp : suProps)
			if(!suProp.isAnonymous()) set.add(suProp.asOWLDataProperty().getIRI().toURI());
		
		return set;
	}
	
	public Set<URI> getNamedSupObjProps(URI objProptURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		Set<OWLObjectPropertyExpression> suProps = objProperty.getSuperProperties(ontology);
		for(OWLObjectPropertyExpression suProp : suProps)
			if(!suProp.isAnonymous()) set.add(suProp.asOWLObjectProperty().getIRI().toURI());
		
		return set;
	}
	
	public Set<String> getObjPropAnonDomains(URI objPropertyURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objPropertyURI);
		
		Set<OWLClassExpression> domains = objProperty.getDomains(ontology);
		for(OWLClassExpression domain : domains)
			if(domain.isAnonymous()) set.add(render(domain));
		
		return set;
	}
	
	public Set<String> getObjPropAnonRanges(URI objProptURI) {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		Set<OWLClassExpression> ranges = objProperty.getRanges(ontology);
		for(OWLClassExpression range : ranges)
			if(range.isAnonymous()) set.add(render(range));
		
		return set;
	}
	
	public Set<URI> getObjPropNamedDomains(URI objPropertyURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objPropertyURI);
		
		Set<OWLClassExpression> domains = objProperty.getDomains(ontology);
		for(OWLClassExpression domain : domains)
			if(!domain.isAnonymous()) set.add(domain.asOWLClass().getIRI().toURI());
		
		return set;
	}
	
	public Set<URI> getObjPropNamedRanges(URI objProptURI) {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		Set<OWLClassExpression> ranges = objProperty.getRanges(ontology);
		for(OWLClassExpression range : ranges)
			if(!range.isAnonymous()) set.add(range.asOWLClass().getIRI().toURI());
		
		return set;
	}
	
	public URI getOntologyID() {
		return ontology.getOntologyID().getOntologyIRI().toURI();
	}
	
	private OWLClass getOWLCls(URI clsURI) {
		return manager.getOWLDataFactory().getOWLClass(IRI.create(clsURI));
	}
	
	private OWLDataProperty getOWLDataProp(URI dataProptURI) {
		return manager.getOWLDataFactory().getOWLDataProperty(IRI.create(dataProptURI));
	}
	
	public URI getOWLNothingURI() {
		return OWLRDFVocabulary.OWL_NOTHING.getIRI().toURI();
	}
	
	private OWLObjectProperty getOWLObjProp(URI objProptURI) {
		return manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(objProptURI));
	}
	
	public URI getOWLThingURI() {
		return OWLRDFVocabulary.OWL_THING.getIRI().toURI();
	}
	
	public URI getOWLTopDataProperty() {
		return OWLRDFVocabulary.OWL_TOP_DATA_PROPERTY.getIRI().toURI();
	}
	
	public URI getOWLTopObjectProperty() {
		return OWLRDFVocabulary.OWL_TOP_OBJECT_PROPERTY.getIRI().toURI();
	}
	
	public URI getOWLBottomDataProperty() {
		return OWLRDFVocabulary.OWL_BOTTOM_DATA_PROPERTY.getIRI().toURI();
	}
	
	public URI getOWLBottomObjectProperty() {
		return OWLRDFVocabulary.OWL_BOTTOM_OBJECT_PROPERTY.getIRI().toURI();
	}
	
	public QueryEngine getQueryEngine() {
		return queryEngine;
	}
	
	public Set<URI> getRootDataPropList() {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		for(OWLDataProperty dp: ontology.getDataPropertiesInSignature())
			if(dp.getSuperProperties(ontology).size() <= 0)
				set.add(dp.getIRI().toURI());
		
		return set;
	}
	
	public Set<URI> getRootObjPropList() {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		for(OWLObjectProperty op: ontology.getObjectPropertiesInSignature())
			if(op.getSuperProperties(ontology).size() <= 0)
				set.add(op.getIRI().toURI());
		
		return set;
	}
	
	public Set<String> getSPARQLDLAtoms() {
		Set<String> set = new ConcurrentSkipListSet<String>();
		
		QueryAtomType[] atoms = QueryAtomType.values();
		
		for(QueryAtomType atom : atoms) {
			String atomName = atom.toString();
			if (atomName != null) 
				set.add(atom.toString());
		}
		
		return set;
	}
	
	public Set<URI> getSubClses(URI clsURI) {
		OWLClass cls = getOWLCls(clsURI);
		
		Set<URI> children = new ConcurrentSkipListSet<URI>();
		
		if(reasoner.isSatisfiable(cls)) {
			NodeSet<OWLClass> subClses= reasoner.getSubClasses(cls, true);
			Set<OWLClass> set = subClses.getFlattened();
			for(OWLClass subCls : set)
				if(!subCls.equals(cls) && !subCls.isOWLNothing())
					children.add(subCls.getIRI().toURI());
		}
		
		return children;
	}
	
	public Set<URI> getSubDataProps(URI dataPropURI) {
		OWLDataProperty dp = getOWLDataProp(dataPropURI);
		
		Set<URI> children = new ConcurrentSkipListSet<URI>();
		
		Set<OWLDataPropertyExpression> subs = dp.getSubProperties(ontology);
		for(OWLDataPropertyExpression child : subs)
			if(!child.isAnonymous())
				children.add(child.asOWLDataProperty().getIRI().toURI());
		
		return children;
	}
	
	public Set<URI> getSubObjProps(URI objPropURI) {
		OWLObjectProperty op = getOWLObjProp(objPropURI);
		
		Set<URI> children = new ConcurrentSkipListSet<URI>();
		
		Set<OWLObjectPropertyExpression> subs = op.getSubProperties(ontology);
		for(OWLObjectPropertyExpression child : subs)
			if(!child.isAnonymous())
				children.add(child.asOWLObjectProperty().getIRI().toURI());
		
		return children;
	}
	
	public Set<URI> getSWRLBuiltIns() {
		Set<URI> set = new ConcurrentSkipListSet<URI>();
		
		SWRLBuiltInsVocabulary[] builtIns = SWRLBuiltInsVocabulary.values();
		
		for(SWRLBuiltInsVocabulary builtIn : builtIns)
			set.add(builtIn.getURI());
		
		return set;
	}
	
	public boolean hasEquivlntCls(URI clsURI) {
		OWLClass cls = getOWLCls(clsURI);
		return cls.isDefined(ontology);
	}
	
	private void initQueryEngine() {
		queryEngine = QueryEngine.create(manager, reasoner, true);
	}
	
	private void initReasoner(ReasonerType reasonerType) {
		OWLReasonerFactory reasonerFactory = null;
		
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		
		if (reasonerType.equals(ReasonerType.PELLET_REASONER)) {
			reasonerFactory = new PelletReasonerFactory();
			
		} else if (reasonerType.equals(ReasonerType.STRUCTURAL_REASONER)) {
			reasonerFactory = new StructuralReasonerFactory();
		}
		
		reasoner = reasonerFactory.createReasoner(ontology, config);
	}
	
	private void initRenderer() {
		renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	}
	
	public boolean isAsymmetric(URI objProptURI) {
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		return objProperty.isAsymmetric(ontology);
		
		//return ontology.getAsymmetricObjectPropertyAxioms(objProperty).size() > 0 ? true : false;
	}
	
	public boolean isEquivalentClass(URI clsURI1, URI clsURI2) {
		OWLClass cls1 = getOWLCls(clsURI1);
		
		Node<OWLClass> nodes = reasoner.getEquivalentClasses(cls1);
		if (nodes.getSize() > 0) {
			OWLClass cls2 = getOWLCls(clsURI2);
			if (nodes.contains(cls2))
				return true;
		}
		
		return false;
	}
	
	public boolean isEquivalentDataPropertyOf(URI dataProptURI1, URI dataProptURI2) {
		OWLDataProperty dataProperty1 = getOWLDataProp(dataProptURI1);
		
		 Node<OWLDataProperty> node = reasoner.getEquivalentDataProperties(dataProperty1);
		 
		 if (node.getSize() > 0) {
			 OWLDataProperty dataProperty2 = getOWLDataProp(dataProptURI2);
			 return node.contains(dataProperty2);
		 }
		 
		 return false;
	}
	
	public boolean isEquivalentObjectPropertyOf(URI objectProptURI1, URI objectProptURI2) {
		OWLObjectProperty objProperty1 = getOWLObjProp(objectProptURI1);
		
		 Node<OWLObjectPropertyExpression> node = reasoner.getEquivalentObjectProperties(objProperty1);
		 
		 if (node.getSize() > 0) {
			 OWLObjectProperty objProperty2 = getOWLObjProp(objectProptURI2);
			 return node.contains(objProperty2);
		 }
		 
		 return false;
	}
	
	public boolean isFunctionalDataProp(URI dataProptURI) {
		OWLDataProperty dataProperty = getOWLDataProp(dataProptURI);
		
		return dataProperty.isFunctional(ontology);
		
		//return ontology.getFunctionalDataPropertyAxioms(dataProperty).size() > 0 ? true : false;
	}
	
	public boolean isFunctionalObjProp(URI objProptURI) {
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		return objProperty.isFunctional(ontology);
		
		//return ontology.getFunctionalObjectPropertyAxioms(objProperty).size() > 0 ? true : false;
	}
	
	public boolean isInverseFunctional(URI objProptURI) {
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		return objProperty.isInverseFunctional(ontology);
		
		//return ontology.getInverseFunctionalObjectPropertyAxioms(objProperty).size() > 0 ? true : false;
	}
	
	public boolean isInverseObjectPropertyOf(URI objectProptURI1, URI objectProptURI2) {
		OWLObjectProperty objProperty1 = getOWLObjProp(objectProptURI1);
		
		 Node<OWLObjectPropertyExpression> node = reasoner.getInverseObjectProperties(objProperty1);
		 
		 if (node.getSize() > 0) {
			 OWLObjectProperty objProperty2 = getOWLObjProp(objectProptURI2);
			 return node.contains(objProperty2);
		 }
		 
		 return false;
	}
	
	public boolean isIrreflexive(URI objProptURI) {
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		return objProperty.isIrreflexive(ontology);
		
		//return ontology.getIrreflexiveObjectPropertyAxioms(objProperty).size() > 0 ? true : false;
	}
	
	public boolean isReflexive(URI objProptURI) {
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		return objProperty.isReflexive(ontology);
		
		//return ontology.getReflexiveObjectPropertyAxioms(objProperty).size() > 0 ? true : false;
	}
	
	public boolean isSubClassOf(URI clsURI1, URI clsURI2) {
		OWLClass cls2 = getOWLCls(clsURI2);
		
		NodeSet<OWLClass> nodeSet = reasoner.getSubClasses(cls2, false);
		Set<Node<OWLClass>> nodes = nodeSet.getNodes();
		for (Node<OWLClass> node : nodes) {
			Set<OWLClass> entities = node.getEntities();
			if (!entities.isEmpty()) {
				OWLClass cls1 = getOWLCls(clsURI1);
				if (entities.contains(cls1))
					return true;
			}
		}
		
		return false;
	}
	
	public Set<URI> getHasKeyDataProperties(URI clsURI) {
		Set<URI> dataProperties = new ConcurrentSkipListSet<URI>();
		
		OWLClass cls = getOWLCls(clsURI);
		
		Set<OWLHasKeyAxiom> keys = ontology.getHasKeyAxioms(cls);
		
		for (OWLHasKeyAxiom key: keys) {
			Set<OWLDataPropertyExpression> properties = key.getDataPropertyExpressions();
			
			for (OWLDataPropertyExpression dpe: properties) {
				OWLDataProperty dp = dpe.asOWLDataProperty();
				
				dataProperties.add(dp.getIRI().toURI());
			}
		}
		
		return dataProperties;
	}
		
	public boolean isSubDataPropertyOf(URI dataPropURI1, URI dataPropURI2) {
		OWLDataProperty dp1 = getOWLDataProp(dataPropURI1);
		OWLDataProperty dp2 = getOWLDataProp(dataPropURI2);
		
		NodeSet<OWLDataProperty> nodeSet = reasoner.getSubDataProperties(dp2, false);
		return nodeSet.containsEntity(dp1);
	}
	
	public boolean isSubObjectPropertyOf(URI objPropURI1, URI objPropURI2) {
		OWLObjectProperty op1 = getOWLObjProp(objPropURI1);
		OWLObjectProperty op2 = getOWLObjProp(objPropURI2);
		
		NodeSet<OWLObjectPropertyExpression> nodeSet = reasoner.getSubObjectProperties(op2, false);
		return nodeSet.containsEntity(op1);
	}
	
	public boolean isSymmetric(URI objProptURI) {
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		return objProperty.isSymmetric(ontology);
		
		//return ontology.getSymmetricObjectPropertyAxioms(objProperty).size() > 0 ? true : false;
	}
	
	public boolean isTransitive(URI objProptURI) {
		OWLObjectProperty objProperty = getOWLObjProp(objProptURI);
		
		return objProperty.isTransitive(ontology);
		
		//return ontology.getTransitiveObjectPropertyAxioms(objProperty).size() > 0 ? true : false;
	}
	
	private String render(OWLObject object) {
		String str = renderer.render(object);
		
		str = str.replaceAll("\n", "");
		
		return str;
	}
}