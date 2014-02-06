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

public class TBox2Processor {
	
	void execute(QueryAtom atom, Map<String, Variable> varsMap) {
		
		if (atom.getType().equals(QueryAtomType.COMPLEMENT_OF)) {
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

			Set<URI> complementClasses = new ConcurrentSkipListSet<URI>();

			URI owlThing = Janus.ontBridge.getOWLThingURI();
			URI owlNothing = Janus.ontBridge.getOWLNothingURI();

			if (var1URIs.size() > 0 || var2URIs.size() > 0) {

				if (var1URIs.size() > 0) {

					for (URI cls: var1URIs) {
						if (cls.equals(owlThing))
							complementClasses.add(owlNothing);
						else if (cls.equals(owlNothing))
							complementClasses.add(owlThing);
						else
							var1URIs.remove(cls);
					}

					variable1.intersectURIs(var1URIs);
					variable2.intersectURIs(complementClasses);

					return;

				} else {

					for (URI cls: var2URIs) {
						if (cls.equals(owlThing))
							complementClasses.add(owlNothing);
						else if (cls.equals(owlNothing))
							complementClasses.add(owlThing);
						else
							var2URIs.remove(cls);
					}

					variable1.intersectURIs(complementClasses);
					variable2.intersectURIs(var2URIs);

					return;
				}

			} else {

				complementClasses.add(owlNothing);
				complementClasses.add(owlThing);

				variable1.intersectURIs(complementClasses);
				variable2.intersectURIs(complementClasses);
				return;

			}
		}

		if (atom.getType().equals(QueryAtomType.DISJOINT_WITH)) {
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

			Set<URI> allDisjointClasses = new ConcurrentSkipListSet<URI>();

			Set<URI> var1URIs = variable1.getURISet();
			Set<URI> var2URIs = variable2.getURISet();

			if (var1URIs.size() > 0 || var2URIs.size() > 0) {

				if (var1URIs.size() > 0) {

					for (URI cls: var1URIs) {
						Set<URI> disjointClasses = Janus.ontBridge.getAllDisjointClasses(cls);

						if (disjointClasses.size() < 1)
							var1URIs.remove(cls);
						else
							allDisjointClasses.addAll(disjointClasses);
					}

					variable1.intersectURIs(var1URIs);
					variable2.intersectURIs(allDisjointClasses);

					return;

				} else {

					for (URI cls: var2URIs) {
						Set<URI> disjointClasses = Janus.ontBridge.getAllDisjointClasses(cls);

						if (disjointClasses.size() < 1)
							var2URIs.remove(cls);
						else
							allDisjointClasses.addAll(disjointClasses);
					}

					variable1.intersectURIs(allDisjointClasses);
					variable2.intersectURIs(var2URIs);

					return;
				}
			} else {

				Set<URI> allClasses = new ConcurrentSkipListSet<URI>();

				URI owlThing = Janus.ontBridge.getOWLThingURI();
				allClasses.add(owlThing);
				allClasses.addAll(Janus.ontBridge.getAllSubClasses(owlThing));

				for (URI cls: allClasses) {
					Set<URI> disjointClasses = Janus.ontBridge.getAllDisjointClasses(cls);
					allDisjointClasses.addAll(disjointClasses);
				}

				variable1.intersectURIs(allDisjointClasses);
				variable2.intersectURIs(allDisjointClasses);
				return;

			}
		}

		if (atom.getType().equals(QueryAtomType.EQUIVALENT_CLASS)) {
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

			Set<URI> allEquivalentClasses = new ConcurrentSkipListSet<URI>();

			Set<URI> var1URIs = variable1.getURISet();
			Set<URI> var2URIs = variable2.getURISet();

			if (var1URIs.size() > 0 || var2URIs.size() > 0) {

				if (var1URIs.size() > 0) {

					for (URI cls: var1URIs) {
						Set<URI> equivalentClasses = Janus.ontBridge.getEquivalentClasses(cls);

						if (equivalentClasses.size() < 1)
							var1URIs.remove(cls);
						else
							allEquivalentClasses.addAll(equivalentClasses);
					}

					variable1.intersectURIs(var1URIs);
					variable2.intersectURIs(allEquivalentClasses);

					return;

				} else {

					for (URI cls: var2URIs) {
						Set<URI> equivalentClasses = Janus.ontBridge.getEquivalentClasses(cls);

						if (equivalentClasses.size() < 1)
							var2URIs.remove(cls);
						else
							allEquivalentClasses.addAll(equivalentClasses);
					}

					variable1.intersectURIs(allEquivalentClasses);
					variable2.intersectURIs(var2URIs);

					return;
				}

			} else {

				Set<URI> allClasses = new ConcurrentSkipListSet<URI>();

				URI owlThing = Janus.ontBridge.getOWLThingURI();
				URI owlNothing = Janus.ontBridge.getOWLNothingURI();
				allClasses.add(owlThing);
				allClasses.addAll(Janus.ontBridge.getAllSubClasses(owlThing));
				allClasses.remove(owlNothing);

				for (URI cls: allClasses) {
					Set<URI> equivalentClasses = Janus.ontBridge.getEquivalentClasses(cls);
					allEquivalentClasses.addAll(equivalentClasses);
				}

				variable1.intersectURIs(allEquivalentClasses);
				variable2.intersectURIs(allEquivalentClasses);
				return;

			}

		}

		if (atom.getType().equals(QueryAtomType.SUB_CLASS_OF)) {
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

			Set<URI> allSubClasses = new ConcurrentSkipListSet<URI>();
			Set<URI> allSuperClasses = new ConcurrentSkipListSet<URI>();

			Set<URI> var1URIs = variable1.getURISet();
			Set<URI> var2URIs = variable2.getURISet();

			if (var1URIs.size() > 0 || var2URIs.size() > 0) {
				if (var1URIs.size() > 0) {

					for (URI cls: var1URIs) {
						Set<URI> superClasses = Janus.ontBridge.getAllSuperClasses(cls);

						if (superClasses.size() < 1)
							var1URIs.remove(cls);
						else
							allSuperClasses.addAll(superClasses);
					}

					variable1.intersectURIs(var1URIs);
					variable2.intersectURIs(allSuperClasses);

					return;

				} else {

					for (URI cls: var2URIs) {
						Set<URI> subClasses = Janus.ontBridge.getAllSubClasses(cls);

						if (subClasses.size() < 1)
							var2URIs.remove(cls);
						else
							allSubClasses.addAll(subClasses);
					}

					variable1.intersectURIs(allSubClasses);
					variable2.intersectURIs(var2URIs);

					return;
				}
			} else {

				Set<URI> allClasses = new ConcurrentSkipListSet<URI>();

				URI owlThing = Janus.ontBridge.getOWLThingURI();
				allClasses.add(owlThing);
				allClasses.addAll(Janus.ontBridge.getAllSubClasses(owlThing));

				for (URI cls: allClasses) {
					Set<URI> subClasses = Janus.ontBridge.getAllSubClasses(cls);
					allSubClasses.addAll(subClasses);

					Set<URI> superClasses = Janus.ontBridge.getAllSuperClasses(cls);
					allSuperClasses.addAll(superClasses);
				}

				variable1.intersectURIs(allSubClasses);
				variable2.intersectURIs(allSuperClasses);

				return;

			}
		}
	}
}
