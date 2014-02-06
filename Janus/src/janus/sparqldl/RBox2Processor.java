package janus.sparqldl;

import janus.Janus;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.types.QueryAtomType;

public class RBox2Processor {
	
	void execute(QueryAtom atom, Map<String, Variable> varsMap) {
		if (atom.getType().equals(QueryAtomType.INVERSE_OF)) {
			List<QueryArgument> args = atom.getArguments();

			String varName1 = args.get(0).toString();
			String varName2 = args.get(1).toString();

			Variable variable1 = varsMap.get(varName1);
			Variable variable2 = varsMap.get(varName2);

			if (variable1.hasURIsFinished() || variable2.hasURIsFinished()) {
				System.out.println("Either the variable " + varName1 + " or the variable " + varName2 + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");

				variable1.makeFinished();
				variable2.makeFinished();

				return;
			}

			Set<URI> var1URIs = variable1.getURISet();
			Set<URI> var2URIs = variable2.getURISet();

			if (var1URIs.size() > 0 || var2URIs.size() > 0) {

				if (var1URIs.size() > 0) {
					Set<URI> allInverseObjProperties = new ConcurrentSkipListSet<URI>();

					for (URI objProperty: var1URIs) {
						Set<URI> inverseObjProps = Janus.ontBridge.getInverseObjectProperties(objProperty);

						if (inverseObjProps.size() < 1)
							var1URIs.remove(objProperty);
						else
							allInverseObjProperties.addAll(inverseObjProps);
					}

					variable1.intersectURIs(var1URIs);
					variable2.intersectURIs(allInverseObjProperties);

					return;
				} else {
					Set<URI> allInverseObjProperties = new ConcurrentSkipListSet<URI>();

					for (URI objProperty: var2URIs) {
						Set<URI> inverseObjProps = Janus.ontBridge.getInverseObjectProperties(objProperty);

						if (inverseObjProps.size() < 1)
							var2URIs.remove(objProperty);
						else
							allInverseObjProperties.addAll(inverseObjProps);
					}

					variable1.intersectURIs(allInverseObjProperties);
					variable2.intersectURIs(var2URIs);

					return;
				}

			} else {

				Set<URI> allInverseObjProperties = new ConcurrentSkipListSet<URI>();

				Set<URI> allObjProperties = Janus.ontBridge.getAllObjProps();

				for (URI objProperty: allObjProperties) {
					Set<URI> inverseObjProps = Janus.ontBridge.getInverseObjectProperties(objProperty);
					allInverseObjProperties.addAll(inverseObjProps);
				}

				variable1.intersectURIs(allInverseObjProperties);
				variable2.intersectURIs(allInverseObjProperties);

				return;

			}
		}

		if (atom.getType().equals(QueryAtomType.EQUIVALENT_PROPERTY)) {
			List<QueryArgument> args = atom.getArguments();

			String varName1 = args.get(0).toString();
			String varName2 = args.get(1).toString();

			Variable variable1 = varsMap.get(varName1);
			Variable variable2 = varsMap.get(varName2);

			if (variable1.hasURIsFinished() || variable2.hasURIsFinished()) {
				System.out.println("Either the variable " + varName1 + " or the variable " + varName2 + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");

				variable1.makeFinished();
				variable2.makeFinished();

				return;
			}

			Set<URI> var1URIs = variable1.getURISet();
			Set<URI> var2URIs = variable2.getURISet();

			if (var1URIs.size() > 0 || var2URIs.size() > 0) {
				Set<URI> allEquiProperties = new ConcurrentSkipListSet<URI>();

				if (var1URIs.size() > 0) {	

					for (URI property: var1URIs) {
						Set<URI> equiProperties = new ConcurrentSkipListSet<URI>();

						if (Janus.ontBridge.containsObjectProperty(property))
							equiProperties = Janus.ontBridge.getEquivalentObjectProperties(property);
						else
							equiProperties = Janus.ontBridge.getEquivalentDataProperties(property);

						if (equiProperties.size() < 1)
							var1URIs.remove(property);
						else
							allEquiProperties.addAll(equiProperties);
					}

					variable1.intersectURIs(var1URIs);
					variable2.intersectURIs(allEquiProperties);

					return;

				} else {

					for (URI property: var2URIs) {
						Set<URI> equiProperties = new ConcurrentSkipListSet<URI>();

						if (Janus.ontBridge.containsObjectProperty(property))
							equiProperties = Janus.ontBridge.getEquivalentObjectProperties(property);
						else
							equiProperties = Janus.ontBridge.getEquivalentDataProperties(property);

						if (equiProperties.size() < 1)
							var2URIs.remove(property);
						else
							allEquiProperties.addAll(equiProperties);
					}

					variable1.intersectURIs(allEquiProperties);
					variable2.intersectURIs(var2URIs);

					return;
				}

			} else {

				Set<URI> allEquiProperties = new ConcurrentSkipListSet<URI>();

				Set<URI> allObjProperties = Janus.ontBridge.getAllObjProps();

				for (URI objProperty: allObjProperties) {
					Set<URI> equivalentObjProps = Janus.ontBridge.getEquivalentObjectProperties(objProperty);
					allEquiProperties.addAll(equivalentObjProps);
				}

				Set<URI> allDataProperties = Janus.ontBridge.getAllDataProps();

				for (URI dataProperty: allDataProperties) {
					Set<URI> equivalentDataProps = Janus.ontBridge.getEquivalentDataProperties(dataProperty);
					allEquiProperties.addAll(equivalentDataProps);
				}

				variable1.intersectURIs(allEquiProperties);
				variable2.intersectURIs(allEquiProperties);

				return;

			}
		}

		if (atom.getType().equals(QueryAtomType.SUB_PROPERTY_OF)) {
			List<QueryArgument> args = atom.getArguments();

			String varName1 = args.get(0).toString();
			String varName2 = args.get(1).toString();

			Variable variable1 = varsMap.get(varName1);
			Variable variable2 = varsMap.get(varName2);

			if (variable1.hasURIsFinished() || variable2.hasURIsFinished()) {
				System.out.println("Either the variable " + varName1 + " or the variable " + varName2 + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");

				variable1.makeFinished();
				variable2.makeFinished();

				return;
			}

			Set<URI> var1URIs = variable1.getURISet();
			Set<URI> var2URIs = variable2.getURISet();

			if (var1URIs.size() > 0 || var2URIs.size() > 0) {

				if (var1URIs.size() > 0) {	
					Set<URI> allSuperProperties = new ConcurrentSkipListSet<URI>();

					for (URI property: var1URIs) {
						Set<URI> superProperties = new ConcurrentSkipListSet<URI>();

						if (Janus.ontBridge.containsObjectProperty(property))
							superProperties = Janus.ontBridge.getAllSuperObjProps(property);
						else
							superProperties = Janus.ontBridge.getAllSuperDataProps(property);

						if (superProperties.size() < 1)
							var1URIs.remove(property);
						else
							allSuperProperties.addAll(superProperties);
					}

					variable1.intersectURIs(var1URIs);
					variable2.intersectURIs(allSuperProperties);

					return;

				} else {
					Set<URI> allSubProperties = new ConcurrentSkipListSet<URI>();

					for (URI property: var2URIs) {
						Set<URI> subProperties = new ConcurrentSkipListSet<URI>();

						if (Janus.ontBridge.containsObjectProperty(property))
							subProperties = Janus.ontBridge.getAllSubObjProps(property);
						else
							subProperties = Janus.ontBridge.getAllSubDataProps(property);

						if (subProperties.size() < 1)
							var2URIs.remove(property);
						else
							allSubProperties.addAll(subProperties);
					}

					variable1.intersectURIs(allSubProperties);
					variable2.intersectURIs(var2URIs);

					return;
				}

			} else {

				Set<URI> allSubProperties = new ConcurrentSkipListSet<URI>();
				Set<URI> allSuperProperties = new ConcurrentSkipListSet<URI>();

				Set<URI> allObjProperties = Janus.ontBridge.getAllObjProps();

				for (URI objProperty: allObjProperties) {
					Set<URI> subObjProps = Janus.ontBridge.getAllSubObjProps(objProperty);
					allSubProperties.addAll(subObjProps);

					Set<URI> superObjProps = Janus.ontBridge.getAllSuperObjProps(objProperty);
					allSuperProperties.addAll(superObjProps);
				}

				Set<URI> allDataProperties = Janus.ontBridge.getAllDataProps();

				for (URI dataProperty: allDataProperties) {
					Set<URI> subDataProps = Janus.ontBridge.getAllSubDataProps(dataProperty);
					allSubProperties.addAll(subDataProps);

					Set<URI> superDataProps = Janus.ontBridge.getAllSuperDataProps(dataProperty);
					allSuperProperties.addAll(superDataProps);
				}

				variable1.intersectURIs(allSubProperties);
				variable2.intersectURIs(allSuperProperties);

				return;
			}
		}
	}
}

