package janus.sparqldl;

import janus.Janus;
import janus.mapping.metadata.ClassTypes;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.types.QueryAtomType;

public class ABox2Processor {

	void execute(QueryAtom atom, Map<String, Variable> varsMap) {

		if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM)) {
			List<QueryArgument> args = atom.getArguments();

			String varName1 = args.get(0).toString();
			String varName2 = args.get(1).toString();

			Variable variable1 = varsMap.get(varName1);
			Variable variable2 = varsMap.get(varName2);

			if (variable1.hasIndividualsFinished() || variable2.hasIndividualsFinished()) {
				System.out.println("Either the variable " + varName1 + " or the variable " + varName2 + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");

				variable1.makeFinished();
				variable2.makeFinished();

				return;
			}

			Set<IndividualSet> var1IndividualSets = variable1.getIndividualSets();
			Set<IndividualSet> var2IndividualSets = variable2.getIndividualSets();

			if (var1IndividualSets.size() > 0) {

				Individual distinctIndividual = null;
				int distinctIndividualCount = 0;
				for (IndividualSet individualSet: var1IndividualSets) {
					int count = individualSet.getIndividualCount();

					if (count < 1)
						var1IndividualSets.remove(individualSet);
					else if (count == 1) {
						if (distinctIndividualCount == 0) {
							distinctIndividual = new Individual(individualSet.getTheOneIndividual());
							distinctIndividualCount++;
						}
						else {
							if (!distinctIndividual.equals(new Individual(individualSet.getTheOneIndividual())))
								distinctIndividualCount++;
						}
					} else
						distinctIndividualCount = count;

					if (distinctIndividualCount > 1)
						break;
				}

				if (distinctIndividualCount == 0) {
					variable1.makeFinished();
					variable2.makeFinished();

					return;
				} else if (distinctIndividualCount == 1) {
					Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

					URI distinctIndividualClass = distinctIndividual.getClassURI();

					Set<URI> disjointClasses = Janus.ontBridge.getAllDisjointClasses(distinctIndividualClass);

					for (URI disjointClass: disjointClasses) {
						if (Janus.ontBridge.containsClass(disjointClass)) {
							IndividualSet individualSet = new IndividualSet(disjointClass);
							individualSets.add(individualSet);
						}
					}

					Set<URI> familyClasses = distinctIndividual.getFamilyClasses();

					for (URI familyClass: familyClasses) {
						IndividualSet individualSet = new IndividualSet(familyClass);
						individualSet.acceptDifferentFromCondition(distinctIndividual);
						individualSets.add(individualSet);
					}

					variable1.intersectIndividualSet(var1IndividualSets);
					variable2.intersectIndividualSet(individualSets);

				} else {
					Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

					URI owlThing = Janus.ontBridge.getOWLThingURI();

					Set<URI> allClasses = Janus.ontBridge.getAllSubClasses(owlThing);

					for (URI cls: allClasses) {
						if (Janus.ontBridge.containsClass(cls)) {
							IndividualSet individualSet = new IndividualSet(cls);
							individualSets.add(individualSet);
						}
					}

					variable1.intersectIndividualSet(var1IndividualSets);
					variable2.intersectIndividualSet(individualSets);

				}
			}

			if (var2IndividualSets.size() > 0) {

				Individual distinctIndividual = null;
				int distinctIndividualCount = 0;
				for (IndividualSet individualSet: var2IndividualSets) {
					int count = individualSet.getIndividualCount();

					if (count < 1)
						var2IndividualSets.remove(individualSet);
					else if (count == 1) {
						if (distinctIndividualCount == 0) {
							distinctIndividual = new Individual(individualSet.getTheOneIndividual());
							distinctIndividualCount++;
						}
						else {
							if (!distinctIndividual.equals(new Individual(individualSet.getTheOneIndividual())))
								distinctIndividualCount++;
						}
					} else
						distinctIndividualCount = count;

					if (distinctIndividualCount > 1)
						break;
				}

				if (distinctIndividualCount == 0) {
					variable1.makeFinished();
					variable2.makeFinished();

					return;
				} else if (distinctIndividualCount == 1) {
					Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

					URI distinctIndividualClass = distinctIndividual.getClassURI();

					Set<URI> disjointClasses = Janus.ontBridge.getAllDisjointClasses(distinctIndividualClass);

					for (URI disjointClass: disjointClasses) {
						if (Janus.ontBridge.containsClass(disjointClass)) {
							IndividualSet individualSet = new IndividualSet(disjointClass);
							individualSets.add(individualSet);
						}
					}

					Set<URI> familyClasses = distinctIndividual.getFamilyClasses();

					for (URI familyClass: familyClasses) {
						IndividualSet individualSet = new IndividualSet(familyClass);
						individualSet.acceptDifferentFromCondition(distinctIndividual);
						individualSets.add(individualSet);
					}

					variable1.intersectIndividualSet(individualSets);
					variable2.intersectIndividualSet(var2IndividualSets);

				} else {
					Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

					URI owlThing = Janus.ontBridge.getOWLThingURI();

					Set<URI> allClasses = Janus.ontBridge.getAllSubClasses(owlThing);

					for (URI cls: allClasses) {
						if (Janus.ontBridge.containsClass(cls)) {
							IndividualSet individualSet = new IndividualSet(cls);
							individualSets.add(individualSet);
						}
					}

					variable1.intersectIndividualSet(individualSets);
					variable2.intersectIndividualSet(var2IndividualSets);

				}
			}

			if (var1IndividualSets.size() < 1 && var2IndividualSets.size() < 1) {
				Set<String> tables = Janus.cachedDBMetadata.getTableNames();

				int rowCount = 0;

				for(String table : tables) {
					rowCount = Janus.cachedDBMetadata.getRowCount(table);

					if (rowCount > 0)
						break;
				}

				if (rowCount == 0) {
					variable1.makeFinished();
					variable2.makeFinished();

					return;
				} else if (rowCount > 0) {
					Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

					URI owlThing = Janus.ontBridge.getOWLThingURI();

					Set<URI> allClasses = Janus.ontBridge.getAllSubClasses(owlThing);

					for (URI cls: allClasses) {
						if (Janus.ontBridge.containsClass(cls)) {
							IndividualSet individualSet = new IndividualSet(cls);
							individualSets.add(individualSet);
						}
					}

					variable1.intersectIndividualSet(individualSets);
					variable2.intersectIndividualSet(individualSets);
				}
			}
		}

		if (atom.getType().equals(QueryAtomType.SAME_AS)) {
			List<QueryArgument> args = atom.getArguments();

			String varName1 = args.get(0).toString();
			String varName2 = args.get(1).toString();

			Variable variable1 = varsMap.get(varName1);
			Variable variable2 = varsMap.get(varName2);

			if (variable1.hasIndividualsFinished() || variable2.hasIndividualsFinished()) {
				System.out.println("Either the variable " + varName1 + " or the variable " + varName2 + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");

				variable1.makeFinished();
				variable2.makeFinished();

				return;
			}

			Set<IndividualSet> var1IndividualSets = variable1.getIndividualSets();
			Set<IndividualSet> var2IndividualSets = variable2.getIndividualSets();

			if (var1IndividualSets.size() > 0 && var2IndividualSets.size() == 0) {
				Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

				individualSets.addAll(var1IndividualSets);

				variable2.intersectIndividualSet(individualSets);
			} else if (var1IndividualSets.size() == 0 && var2IndividualSets.size() > 0) {
				Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

				individualSets.addAll(var2IndividualSets);

				variable1.intersectIndividualSet(individualSets);
			} else if (var1IndividualSets.size() > 0 && var2IndividualSets.size() > 0) {
				Set<IndividualSet> individualSets1 = new ConcurrentSkipListSet<IndividualSet>();
				Set<IndividualSet> individualSets2 = new ConcurrentSkipListSet<IndividualSet>();

				individualSets1.addAll(var1IndividualSets);
				individualSets2.addAll(var2IndividualSets);

				variable1.intersectIndividualSet(individualSets2);
				variable2.intersectIndividualSet(individualSets1);
			} else {
				Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

				URI owlThing = Janus.ontBridge.getOWLThingURI();

				Set<URI> allClasses = Janus.ontBridge.getAllSubClasses(owlThing);

				for (URI cls: allClasses) {
					if (Janus.ontBridge.containsClass(cls)) {
						IndividualSet individualSet = new IndividualSet(cls);
						individualSets.add(individualSet);
					}
				}

				variable1.intersectIndividualSet(individualSets);
				variable2.intersectIndividualSet(individualSets);
			}
		}

		if (atom.getType().equals(QueryAtomType.TYPE)) {
			List<QueryArgument> args = atom.getArguments();

			String varName1 = args.get(0).toString();
			String varName2 = args.get(1).toString();

			Variable variable1 = varsMap.get(varName1);
			Variable variable2 = varsMap.get(varName2);

			if (variable1.hasIndividualsFinished() || variable2.hasURIsFinished()) {
				System.out.println("Either the variable " + varName1 + " or the variable " + varName2 + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");

				variable1.makeFinished();
				variable2.makeFinished();

				return;
			}

			Set<IndividualSet> var1IndividualSets = variable1.getIndividualSets();
			Set<URI> var2URISet = variable2.getURISet();

			if (var1IndividualSets.size() > 0 && var2URISet.size() == 0) {

				Set<URI> var1ClassURISet = new ConcurrentSkipListSet<URI>();

				boolean existsIndividual = false;
				for (IndividualSet individualSet: var1IndividualSets) {
					int count = individualSet.getIndividualCount();

					if (count < 1) {
						Set<URI> subClasses = Janus.ontBridge.getAllSubClasses(individualSet.getClassURI());

						var1IndividualSets.remove(individualSet);

						for (URI subClass: subClasses) {
							IndividualSet removalIndividualSet = new IndividualSet(subClass);

							var1IndividualSets.remove(removalIndividualSet);
						}
					} else {
						var1ClassURISet.add(individualSet.getClassURI());

						existsIndividual = true;	
					}
				}

				if (existsIndividual) {
					URI owlThing = Janus.ontBridge.getOWLThingURI();
					var1ClassURISet.add(owlThing);
				}

				variable1.intersectIndividualSet(var1IndividualSets);
				variable2.intersectURIs(var1ClassURISet);

			} else if (var1IndividualSets.size() == 0 && var2URISet.size() > 0) {

				URI owlThing = Janus.ontBridge.getOWLThingURI();
				URI owlNothing = Janus.ontBridge.getOWLNothingURI();

				if (var2URISet.contains(owlNothing))
					var2URISet.remove(owlNothing);

				Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

				if (var2URISet.contains(owlThing)) {
					Set<URI> allSubClasses = Janus.ontBridge.getAllSubClasses(owlThing);
					allSubClasses.remove(owlThing);
					allSubClasses.remove(owlNothing);

					for (URI subClass: allSubClasses) {
						IndividualSet individualSet = new IndividualSet(subClass);

						individualSets.add(individualSet);
					}

					variable1.intersectIndividualSet(individualSets);
					variable2.intersectURIs(var2URISet);
				} else {
					Set<URI> groupRoots = variable2.getGroupRoots(var2URISet);

					for (URI groupRoot: groupRoots) {
						Set<URI> familyClasses = Janus.ontBridge.getAllFamilyClasses(groupRoot);

						for (URI familyClass: familyClasses) {
							IndividualSet individualSet = new IndividualSet(familyClass);

							if (!familyClass.equals(groupRoot))
								individualSet.intersectWith(groupRoot);

							individualSets.add(individualSet);
						}
					}

					variable1.intersectIndividualSet(individualSets);
					variable2.intersectURIs(var2URISet);	
				}

			} else if (var1IndividualSets.size() > 0 && var2URISet.size() > 0) {
				Set<URI> var1ClassSet = new ConcurrentSkipListSet<URI>();

				for (IndividualSet var1IndividualSet: var1IndividualSets)
					var1ClassSet.add(var1IndividualSet.getClassURI());

				var1ClassSet.retainAll(var2URISet);

				for (IndividualSet var1IndividualSet: var1IndividualSets) {
					if (!var1ClassSet.contains(var1IndividualSet.getClassURI()))
						var1IndividualSets.remove(var1IndividualSet);
				}
				
				for (IndividualSet var1IndividualSet: var1IndividualSets) {
					Set<URI> familyClasses = var1IndividualSet.getFamilyClasses();

					for (URI familyClass: familyClasses) {
						if (familyClass.equals(var1IndividualSet)) continue;
						
						if (!var1ClassSet.contains(familyClass)) {
							IndividualSet individualSet = new IndividualSet(familyClass);
							individualSet.intersectWith(var1IndividualSet);
							
							if (var1IndividualSets.contains(individualSet)) {
								for (IndividualSet containedIndividualSet: var1IndividualSets)
									if (containedIndividualSet.compareTo(individualSet) == 0)
										containedIndividualSet.intersectWith(individualSet);
							} else 
								var1IndividualSets.add(individualSet);
							
						}
					}
				}
				
				

				variable1.intersectIndividualSet(var1IndividualSets);
				variable2.intersectURIs(var1ClassSet);

			} else {
				Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

				URI owlThing = Janus.ontBridge.getOWLThingURI();
				URI owlNothing = Janus.ontBridge.getOWLNothingURI();

				Set<URI> allClasses = Janus.ontBridge.getAllSubClasses(owlThing);
				allClasses.remove(owlNothing);

				for (URI cls: allClasses) {
					if (!cls.equals(owlThing)) {
						IndividualSet individualSet = new IndividualSet(cls);
						individualSets.add(individualSet);
					}
				}

				variable1.intersectIndividualSet(individualSets);
				variable2.intersectURIs(allClasses);
			}
		}

		if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
			List<QueryArgument> args = atom.getArguments();

			QueryArgument arg1 = args.get(0);
			QueryArgument arg2 = args.get(1);
			QueryArgument arg3 = args.get(2);

			URI sURI = null;
			URI pURI = null;
			URI oURI = null;
			String oLit = null;

			if (!arg1.isVar()) {
				String pVarName = arg2.toString();
				String oVarName = arg3.toString();

				Variable pVariable = varsMap.get(pVarName);
				Variable oVariable = varsMap.get(oVarName);

				if (pVariable.hasURIsFinished() || (oVariable.hasLiteralsFinished() && oVariable.hasIndividualsFinished())) {
					System.out.println("Either the variable " + pVarName + " or the variable " + oVarName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");

					pVariable.makeFinished();
					oVariable.makeFinished();

					return;
				}

				sURI = URI.create(arg1.getValue());

				Individual sIndividual = new Individual(sURI);
				if (sIndividual.isExistentIndividual()) {
					Set<URI> varProperties = pVariable.getURISet();
					Set<IndividualSet> varIndividualSets = oVariable.getIndividualSets();
					Set<LiteralSet> varLiteralSets = oVariable.getLiteralSets();

					if (sIndividual.getType().equals(IndividualTypes.FROM_CELL)) {

						pVariable.setVarType(VariableTypes.DATA_PROPERTIES);
						oVariable.setVarType(VariableTypes.LITERALS);

						if (oVariable.hasLiteralsFinished()) {
							System.out.println("The variable " + oVarName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");

							pVariable.makeFinished();
							oVariable.makeFinished();

							return;
						}

						Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();
						oVariable.intersectIndividualSet(individualSets);

						if (varProperties.size() == 0 && varLiteralSets.size() == 0) {
							Set<URI> predicates = sIndividual.getAllProperties();
							pVariable.intersectURIs(predicates);

							Set<LiteralSet> literals = sIndividual.getAllObjectLiterals();
							oVariable.intersectLiteralSet(literals);
						} else if (varProperties.size() > 0 && varLiteralSets.size() == 0) {
							List<Triple> triples = sIndividual.getPOTriples();

							Set<URI> predicates = new ConcurrentSkipListSet<URI>();

							List<Triple> removableTriples = new Vector<Triple>();
							for (Triple triple: triples) {
								URI predicate = triple.getPredicate();

								if (!varProperties.contains(predicate))
									removableTriples.add(triple);
								else
									predicates.add(predicate);
							}
							triples.removeAll(removableTriples);

							pVariable.intersectURIs(predicates);

							Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();

							for (Triple triple: triples) {
								LiteralSet literalSet = triple.getObjectLiteral();

								literalSets.add(literalSet);
							}

							oVariable.intersectLiteralSet(literalSets);

						} else if (varProperties.size() == 0 && varLiteralSets.size() > 0) {
							List<Triple> triples = sIndividual.getPOTriples();

							Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();
							Set<URI> predicates = new ConcurrentSkipListSet<URI>();

							List<Triple> removableTriples = new Vector<Triple>();
							for (Triple triple: triples) {
								LiteralSet literalSet = triple.getObjectLiteral();
								URI predicate = triple.getPredicate();

								if (!varLiteralSets.contains(literalSet))
									removableTriples.add(triple);
								else { 
									LiteralSet sameVarLiteralSet = null;
									for (LiteralSet varLiteralSet: varLiteralSets) {
										if (varLiteralSet.getSelectColumn().equals(literalSet.getSelectColumn())) {
											sameVarLiteralSet = varLiteralSet;

											break;
										}
									}

									literalSet.addAllJoinWhereSet(sameVarLiteralSet.getJoinWhereSet());
									literalSet.addAllValueWhereSet(sameVarLiteralSet.getValueWhereSet());

									if (literalSet.getLiteralCount() < 1)
										removableTriples.add(triple);
									else {
										literalSets.add(literalSet);
										predicates.add(predicate);
									}
								}
							}
							triples.removeAll(removableTriples);

							pVariable.intersectURIs(predicates);
							oVariable.intersectLiteralSet(literalSets);

						} else if (varProperties.size() > 0 && varLiteralSets.size() > 0) {
							List<Triple> triples = sIndividual.getPOTriples();

							Set<URI> predicates = new ConcurrentSkipListSet<URI>();

							List<Triple> removableTriples = new Vector<Triple>();
							for (Triple triple: triples) {
								URI predicate = triple.getPredicate();

								if (!varProperties.contains(predicate))
									removableTriples.add(triple);
								else
									predicates.add(predicate);
							}
							triples.removeAll(removableTriples);

							Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();

							removableTriples = new Vector<Triple>();
							for (Triple triple: triples) {
								LiteralSet literalSet = triple.getObjectLiteral();

								if (!varLiteralSets.contains(literalSet))
									removableTriples.add(triple);
								else {
									LiteralSet sameVarLiteralSet = null;
									for (LiteralSet varLiteralSet: varLiteralSets) {
										if (varLiteralSet.getSelectColumn().equals(literalSet.getSelectColumn())) {
											sameVarLiteralSet = varLiteralSet;

											break;
										}
									}

									literalSet.addAllJoinWhereSet(sameVarLiteralSet.getJoinWhereSet());
									literalSet.addAllValueWhereSet(sameVarLiteralSet.getValueWhereSet());

									if (literalSet.getLiteralCount() < 1)
										removableTriples.add(triple);
									else 
										literalSets.add(literalSet);
								}
							}
							triples.removeAll(removableTriples);

							Set<URI> predicates2 = new ConcurrentSkipListSet<URI>();

							for (Triple triple: triples) {
								URI predicate = triple.getPredicate();

								predicates2.add(predicate);
							}

							predicates.retainAll(predicates2);

							pVariable.intersectURIs(predicates);
							oVariable.intersectLiteralSet(literalSets);
						}

					} else {
						if (oVariable.hasLiteralsFinished() && oVariable.hasIndividualsFinished()) {
							System.out.println("The variable " + oVarName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");

							pVariable.makeFinished();
							oVariable.makeFinished();

							return;
						}

						if (oVariable.hasLiteralsFinished() && !oVariable.hasIndividualsFinished()) {

							List<Triple> triples = sIndividual.getPOTriples();

							List<Triple> removableTriples = new Vector<Triple>();
							for (Triple triple: triples) {
								URI predicate = triple.getPredicate();

								if (Janus.ontBridge.containsDataProperty(predicate))
									removableTriples.add(triple);
							}
							triples.removeAll(removableTriples);

							if (varProperties.size() == 0 && varIndividualSets.size() == 0) {
								Set<URI> predicates = new ConcurrentSkipListSet<URI>();

								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates.add(predicate);
								}

								pVariable.intersectURIs(predicates);

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (individualSets.contains(individualSet)) {
										for (IndividualSet containedIndividualSet: individualSets) {
											if (containedIndividualSet.compareTo(individualSet) == 0) {
												Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
												Set<String> whereSet = individualSet.getValueWhereSet();

												if (!containedWhereSet.containsAll(whereSet))
													containedIndividualSet.connectValueWhereWithOR(whereSet);
											}
										}
									} else 
										individualSets.add(individualSet);
								}

								oVariable.intersectIndividualSet(individualSets);

							} else if (varProperties.size() > 0 && varIndividualSets.size() == 0) {

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();
								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									if (!varProperties.contains(predicate))
										removableTriples.add(triple);
									else 
										predicates.add(predicate);
								}
								triples.removeAll(removableTriples);

								pVariable.intersectURIs(predicates);

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (individualSets.contains(individualSet)) {
										for (IndividualSet containedIndividualSet: individualSets) {
											if (containedIndividualSet.compareTo(individualSet) == 0) {
												Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
												Set<String> whereSet = individualSet.getValueWhereSet();

												if (!containedWhereSet.containsAll(whereSet))
													containedIndividualSet.connectValueWhereWithOR(whereSet);
											}
										}
									} else 
										individualSets.add(individualSet);
								}

								oVariable.intersectIndividualSet(individualSets);
							} else if (varProperties.size() == 0 && varIndividualSets.size() > 0) {

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (!varIndividualSets.contains(individualSet))
										removableTriples.add(triple);
									else {
										if (individualSets.contains(individualSet)) {
											for (IndividualSet containedIndividualSet: individualSets) {
												if (containedIndividualSet.compareTo(individualSet) == 0) {
													Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
													Set<String> whereSet = individualSet.getValueWhereSet();

													if (!containedWhereSet.containsAll(whereSet))
														containedIndividualSet.connectValueWhereWithOR(whereSet);
												}
											}
										} else 
											individualSets.add(individualSet);
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectIndividualSet(individualSets);

								varIndividualSets = oVariable.getIndividualSets();

								for (IndividualSet individualSet: varIndividualSets) {
									if (individualSet.getIndividualCount() < 1) {
										removableTriples = new Vector<Triple>();
										for (Triple triple: triples) {
											IndividualSet tripleIndividualSet = triple.getObjectIndividual();

											if (tripleIndividualSet.compareTo(individualSet) == 0) {
												removableTriples.add(triple);
												varIndividualSets.remove(individualSet);
											}
										}
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectIndividualSet(varIndividualSets);

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates.add(predicate);
								}

								pVariable.intersectURIs(predicates);

							} else if (varProperties.size() > 0 && varIndividualSets.size() > 0) {

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();
								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									if (!varProperties.contains(predicate))
										removableTriples.add(triple);
									else 
										predicates.add(predicate);
								}
								triples.removeAll(removableTriples);

								pVariable.intersectURIs(predicates);

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (!varIndividualSets.contains(individualSet))
										removableTriples.add(triple);
									else {
										if (individualSets.contains(individualSet)) {
											for (IndividualSet containedIndividualSet: individualSets) {
												if (containedIndividualSet.compareTo(individualSet) == 0) {
													Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
													Set<String> whereSet = individualSet.getValueWhereSet();

													if (!containedWhereSet.containsAll(whereSet))
														containedIndividualSet.connectValueWhereWithOR(whereSet);
												}
											}
										} else 
											individualSets.add(individualSet);
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectIndividualSet(individualSets);

								varIndividualSets = oVariable.getIndividualSets();

								for (IndividualSet individualSet: varIndividualSets) {
									if (individualSet.getIndividualCount() < 1) {
										removableTriples = new Vector<Triple>();
										for (Triple triple: triples) {
											IndividualSet tripleIndividualSet = triple.getObjectIndividual();

											if (tripleIndividualSet.compareTo(individualSet) == 0) {
												removableTriples.add(triple);
												varIndividualSets.remove(individualSet);
											}
										}
									}
								}

								oVariable.intersectIndividualSet(varIndividualSets);

								predicates = new ConcurrentSkipListSet<URI>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates.add(predicate);
								}

								pVariable.intersectURIs(predicates);
							}

						} else if (!oVariable.hasLiteralsFinished() && oVariable.hasIndividualsFinished()) {

							List<Triple> triples = sIndividual.getPOTriples();

							List<Triple> removableTriples = new Vector<Triple>();
							for (Triple triple: triples) {
								URI predicate = triple.getPredicate();

								if (Janus.ontBridge.containsObjectProperty(predicate))
									removableTriples.add(triple);
							}
							triples.removeAll(removableTriples);

							if (varProperties.size() == 0 && varLiteralSets.size() == 0) {

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();

								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates.add(predicate);
								}

								pVariable.intersectURIs(predicates);

								Set<LiteralSet> LiteralSets = new ConcurrentSkipListSet<LiteralSet>();

								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();

									LiteralSets.add(literalSet);
								}

								oVariable.intersectLiteralSet(LiteralSets);

							} else if (varProperties.size() > 0 && varLiteralSets.size() == 0) {

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();
								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									if (!varProperties.contains(predicate))
										removableTriples.add(triple);
									else 
										predicates.add(predicate);
								}
								triples.removeAll(removableTriples);

								pVariable.intersectURIs(predicates);

								Set<LiteralSet> LiteralSets = new ConcurrentSkipListSet<LiteralSet>();

								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();

									LiteralSets.add(literalSet);
								}

								oVariable.intersectLiteralSet(LiteralSets);

							} else if (varProperties.size() == 0 && varLiteralSets.size() > 0) {

								Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();
								Set<URI> predicates = new ConcurrentSkipListSet<URI>();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();
									URI predicate = triple.getPredicate();

									if (!varLiteralSets.contains(literalSet))
										removableTriples.add(triple);
									else { 
										LiteralSet sameVarLiteralSet = null;
										for (LiteralSet varLiteralSet: varLiteralSets) {
											if (varLiteralSet.getSelectColumn().equals(literalSet.getSelectColumn())) {
												sameVarLiteralSet = varLiteralSet;

												break;
											}
										}

										literalSet.addAllJoinWhereSet(sameVarLiteralSet.getJoinWhereSet());
										literalSet.addAllValueWhereSet(sameVarLiteralSet.getValueWhereSet());

										if (literalSet.getLiteralCount() < 1)
											removableTriples.add(triple);
										else {
											literalSets.add(literalSet);
											predicates.add(predicate);
										}
									}
								}
								triples.removeAll(removableTriples);

								pVariable.intersectURIs(predicates);
								oVariable.intersectLiteralSet(literalSets);

							} else if (varProperties.size() > 0 && varLiteralSets.size() > 0) {
								Set<URI> predicates = new ConcurrentSkipListSet<URI>();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									if (!varProperties.contains(predicate))
										removableTriples.add(triple);
									else
										predicates.add(predicate);
								}
								triples.removeAll(removableTriples);

								Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();

									if (!varLiteralSets.contains(literalSet))
										removableTriples.add(triple);
									else {
										LiteralSet sameVarLiteralSet = null;
										for (LiteralSet varLiteralSet: varLiteralSets) {
											if (varLiteralSet.getSelectColumn().equals(literalSet.getSelectColumn())) {
												sameVarLiteralSet = varLiteralSet;

												break;
											}
										}

										literalSet.addAllJoinWhereSet(sameVarLiteralSet.getJoinWhereSet());
										literalSet.addAllValueWhereSet(sameVarLiteralSet.getValueWhereSet());

										if (literalSet.getLiteralCount() < 1)
											removableTriples.add(triple);
										else 
											literalSets.add(literalSet);
									}
								}
								triples.removeAll(removableTriples);

								Set<URI> predicates2 = new ConcurrentSkipListSet<URI>();

								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates2.add(predicate);
								}

								predicates.retainAll(predicates2);

								pVariable.intersectURIs(predicates);
								oVariable.intersectLiteralSet(literalSets);
							}

						} else if (!oVariable.hasLiteralsFinished() && !oVariable.hasIndividualsFinished()) {

							List<Triple> triples = sIndividual.getPOTriples();

							if (varProperties.size() == 0 && varLiteralSets.size() == 0 && varIndividualSets.size() == 0) {

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();

								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates.add(predicate);
								}

								pVariable.intersectURIs(predicates);

								Set<LiteralSet> LiteralSets = new ConcurrentSkipListSet<LiteralSet>();

								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();

									if (literalSet == null) continue;

									LiteralSets.add(literalSet);
								}

								oVariable.intersectLiteralSet(LiteralSets);

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (individualSet == null) continue;

									if (individualSets.contains(individualSet)) {
										for (IndividualSet containedIndividualSet: individualSets) {
											if (containedIndividualSet.compareTo(individualSet) == 0) {
												Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
												Set<String> whereSet = individualSet.getValueWhereSet();

												if (!containedWhereSet.containsAll(whereSet))
													containedIndividualSet.connectValueWhereWithOR(whereSet);
											}
										}
									} else 
										individualSets.add(individualSet);
								}

								oVariable.intersectIndividualSet(individualSets);

							} else if (varProperties.size() == 0 && varLiteralSets.size() == 0 && varIndividualSets.size() > 0) {

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								List<Triple> removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (individualSet == null) continue;

									if (!varIndividualSets.contains(individualSet))
										removableTriples.add(triple);
									else {
										if (individualSets.contains(individualSet)) {
											for (IndividualSet containedIndividualSet: individualSets) {
												if (containedIndividualSet.compareTo(individualSet) == 0) {
													Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
													Set<String> whereSet = individualSet.getValueWhereSet();

													if (!containedWhereSet.containsAll(whereSet))
														containedIndividualSet.connectValueWhereWithOR(whereSet);
												}
											}
										} else 
											individualSets.add(individualSet);
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectIndividualSet(individualSets);

								varIndividualSets = oVariable.getIndividualSets();

								for (IndividualSet individualSet: varIndividualSets) {
									if (individualSet.getIndividualCount() < 1) {
										removableTriples = new Vector<Triple>();
										for (Triple triple: triples) {
											IndividualSet tripleIndividualSet = triple.getObjectIndividual();

											if (tripleIndividualSet.compareTo(individualSet) == 0) {
												removableTriples.add(triple);
												varIndividualSets.remove(individualSet);
											}
										}
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectIndividualSet(varIndividualSets);

								Set<LiteralSet> LiteralSets = new ConcurrentSkipListSet<LiteralSet>();

								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();

									if (literalSet == null) continue;

									LiteralSets.add(literalSet);
								}

								oVariable.intersectLiteralSet(LiteralSets);

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates.add(predicate);
								}

								pVariable.intersectURIs(predicates);

							} else if (varProperties.size() == 0 && varLiteralSets.size() > 0 && varIndividualSets.size() == 0) {

								Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();

								List<Triple> removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();

									if (literalSet == null) continue;

									if (!varLiteralSets.contains(literalSet))
										removableTriples.add(triple);
									else { 
										LiteralSet sameVarLiteralSet = null;
										for (LiteralSet varLiteralSet: varLiteralSets) {
											if (varLiteralSet.getSelectColumn().equals(literalSet.getSelectColumn())) {
												sameVarLiteralSet = varLiteralSet;

												break;
											}
										}

										literalSet.addAllJoinWhereSet(sameVarLiteralSet.getJoinWhereSet());
										literalSet.addAllValueWhereSet(sameVarLiteralSet.getValueWhereSet());

										if (literalSet.getLiteralCount() < 1)
											removableTriples.add(triple);
										else {
											literalSets.add(literalSet);
										}
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectLiteralSet(literalSets);

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (individualSet == null) continue;

									if (individualSets.contains(individualSet)) {
										for (IndividualSet containedIndividualSet: individualSets) {
											if (containedIndividualSet.compareTo(individualSet) == 0) {
												Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
												Set<String> whereSet = individualSet.getValueWhereSet();

												if (!containedWhereSet.containsAll(whereSet))
													containedIndividualSet.connectValueWhereWithOR(whereSet);
											}
										}
									} else 
										individualSets.add(individualSet);
								}

								oVariable.intersectIndividualSet(individualSets);

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates.add(predicate);
								}

								pVariable.intersectURIs(predicates);

							} else if (varProperties.size() == 0 && varLiteralSets.size() > 0 && varIndividualSets.size() > 0) {

								Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();

								List<Triple> removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();

									if (literalSet == null) continue;

									if (!varLiteralSets.contains(literalSet))
										removableTriples.add(triple);
									else { 
										LiteralSet sameVarLiteralSet = null;
										for (LiteralSet varLiteralSet: varLiteralSets) {
											if (varLiteralSet.getSelectColumn().equals(literalSet.getSelectColumn())) {
												sameVarLiteralSet = varLiteralSet;

												break;
											}
										}

										literalSet.addAllJoinWhereSet(sameVarLiteralSet.getJoinWhereSet());
										literalSet.addAllValueWhereSet(sameVarLiteralSet.getValueWhereSet());

										if (literalSet.getLiteralCount() < 1)
											removableTriples.add(triple);
										else {
											literalSets.add(literalSet);
										}
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectLiteralSet(literalSets);

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (individualSet == null) continue;

									if (!varIndividualSets.contains(individualSet))
										removableTriples.add(triple);
									else {
										if (individualSets.contains(individualSet)) {
											for (IndividualSet containedIndividualSet: individualSets) {
												if (containedIndividualSet.compareTo(individualSet) == 0) {
													Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
													Set<String> whereSet = individualSet.getValueWhereSet();

													if (!containedWhereSet.containsAll(whereSet))
														containedIndividualSet.connectValueWhereWithOR(whereSet);
												}
											}
										} else 
											individualSets.add(individualSet);
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectIndividualSet(individualSets);

								varIndividualSets = oVariable.getIndividualSets();

								for (IndividualSet individualSet: varIndividualSets) {
									if (individualSet.getIndividualCount() < 1) {
										removableTriples = new Vector<Triple>();
										for (Triple triple: triples) {
											IndividualSet tripleIndividualSet = triple.getObjectIndividual();

											if (tripleIndividualSet.compareTo(individualSet) == 0) {
												removableTriples.add(triple);
												varIndividualSets.remove(individualSet);
											}
										}
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectIndividualSet(varIndividualSets);

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates.add(predicate);
								}

								pVariable.intersectURIs(predicates);

							} else if (varProperties.size() > 0 && varLiteralSets.size() == 0 && varIndividualSets.size() == 0) {

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();

								List<Triple> removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									if (!varProperties.contains(predicate))
										removableTriples.add(triple);
									else
										predicates.add(predicate);
								}
								triples.removeAll(removableTriples);

								pVariable.intersectURIs(predicates);

								Set<LiteralSet> LiteralSets = new ConcurrentSkipListSet<LiteralSet>();

								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();

									if (literalSet == null) continue;

									LiteralSets.add(literalSet);
								}

								oVariable.intersectLiteralSet(LiteralSets);

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (individualSet == null) continue;

									if (individualSets.contains(individualSet)) {
										for (IndividualSet containedIndividualSet: individualSets) {
											if (containedIndividualSet.compareTo(individualSet) == 0) {
												Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
												Set<String> whereSet = individualSet.getValueWhereSet();

												if (!containedWhereSet.containsAll(whereSet))
													containedIndividualSet.connectValueWhereWithOR(whereSet);
											}
										}
									} else 
										individualSets.add(individualSet);
								}

								oVariable.intersectIndividualSet(individualSets);

							} else if (varProperties.size() > 0 && varLiteralSets.size() == 0 && varIndividualSets.size() > 0) {

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();

								List<Triple> removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									if (!varProperties.contains(predicate))
										removableTriples.add(triple);
									else
										predicates.add(predicate);
								}
								triples.removeAll(removableTriples);

								pVariable.intersectURIs(predicates);

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (individualSet == null) continue;

									if (!varIndividualSets.contains(individualSet))
										removableTriples.add(triple);
									else {
										if (individualSets.contains(individualSet)) {
											for (IndividualSet containedIndividualSet: individualSets) {
												if (containedIndividualSet.compareTo(individualSet) == 0) {
													Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
													Set<String> whereSet = individualSet.getValueWhereSet();

													if (!containedWhereSet.containsAll(whereSet))
														containedIndividualSet.connectValueWhereWithOR(whereSet);
												}
											}
										} else 
											individualSets.add(individualSet);
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectIndividualSet(individualSets);

								varIndividualSets = oVariable.getIndividualSets();

								for (IndividualSet individualSet: varIndividualSets) {
									if (individualSet.getIndividualCount() < 1) {
										removableTriples = new Vector<Triple>();
										for (Triple triple: triples) {
											IndividualSet tripleIndividualSet = triple.getObjectIndividual();

											if (tripleIndividualSet.compareTo(individualSet) == 0) {
												removableTriples.add(triple);
												varIndividualSets.remove(individualSet);
											}
										}
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectIndividualSet(varIndividualSets);

								Set<LiteralSet> LiteralSets = new ConcurrentSkipListSet<LiteralSet>();

								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();

									if (literalSet == null) continue;

									LiteralSets.add(literalSet);
								}

								oVariable.intersectLiteralSet(LiteralSets);

								Set<URI> predicates2 = new ConcurrentSkipListSet<URI>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates2.add(predicate);
								}

								pVariable.intersectURIs(predicates2);

							} else if (varProperties.size() > 0 && varLiteralSets.size() > 0 && varIndividualSets.size() == 0) {

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();

								List<Triple> removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									if (!varProperties.contains(predicate))
										removableTriples.add(triple);
									else
										predicates.add(predicate);
								}
								triples.removeAll(removableTriples);

								pVariable.intersectURIs(predicates);

								Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();

									if (literalSet == null) continue;

									if (!varLiteralSets.contains(literalSet))
										removableTriples.add(triple);
									else { 
										LiteralSet sameVarLiteralSet = null;
										for (LiteralSet varLiteralSet: varLiteralSets) {
											if (varLiteralSet.getSelectColumn().equals(literalSet.getSelectColumn())) {
												sameVarLiteralSet = varLiteralSet;

												break;
											}
										}

										literalSet.addAllJoinWhereSet(sameVarLiteralSet.getJoinWhereSet());
										literalSet.addAllValueWhereSet(sameVarLiteralSet.getValueWhereSet());

										if (literalSet.getLiteralCount() < 1)
											removableTriples.add(triple);
										else {
											literalSets.add(literalSet);
										}
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectLiteralSet(literalSets);

								Set<URI> predicates2 = new ConcurrentSkipListSet<URI>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates2.add(predicate);
								}

								pVariable.intersectURIs(predicates2);

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (individualSet == null) continue;

									if (individualSets.contains(individualSet)) {
										for (IndividualSet containedIndividualSet: individualSets) {
											if (containedIndividualSet.compareTo(individualSet) == 0) {
												Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
												Set<String> whereSet = individualSet.getValueWhereSet();

												if (!containedWhereSet.containsAll(whereSet))
													containedIndividualSet.connectValueWhereWithOR(whereSet);
											}
										}
									} else 
										individualSets.add(individualSet);
								}

								oVariable.intersectIndividualSet(individualSets);

							} else if (varProperties.size() > 0 && varLiteralSets.size() > 0 && varIndividualSets.size() > 0) {

								Set<URI> predicates = new ConcurrentSkipListSet<URI>();

								List<Triple> removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									if (!varProperties.contains(predicate))
										removableTriples.add(triple);
									else
										predicates.add(predicate);
								}
								triples.removeAll(removableTriples);

								pVariable.intersectURIs(predicates);

								Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									LiteralSet literalSet = triple.getObjectLiteral();

									if (literalSet == null) continue;

									if (!varLiteralSets.contains(literalSet))
										removableTriples.add(triple);
									else { 
										LiteralSet sameVarLiteralSet = null;
										for (LiteralSet varLiteralSet: varLiteralSets) {
											if (varLiteralSet.getSelectColumn().equals(literalSet.getSelectColumn())) {
												sameVarLiteralSet = varLiteralSet;

												break;
											}
										}

										literalSet.addAllJoinWhereSet(sameVarLiteralSet.getJoinWhereSet());
										literalSet.addAllValueWhereSet(sameVarLiteralSet.getValueWhereSet());

										if (literalSet.getLiteralCount() < 1)
											removableTriples.add(triple);
										else {
											literalSets.add(literalSet);
										}
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectLiteralSet(literalSets);

								Set<URI> predicates2 = new ConcurrentSkipListSet<URI>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates2.add(predicate);
								}

								pVariable.intersectURIs(predicates2);

								Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getObjectIndividual();

									if (individualSet == null) continue;

									if (!varIndividualSets.contains(individualSet))
										removableTriples.add(triple);
									else {
										if (individualSets.contains(individualSet)) {
											for (IndividualSet containedIndividualSet: individualSets) {
												if (containedIndividualSet.compareTo(individualSet) == 0) {
													Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
													Set<String> whereSet = individualSet.getValueWhereSet();

													if (!containedWhereSet.containsAll(whereSet))
														containedIndividualSet.connectValueWhereWithOR(whereSet);
												}
											}
										} else 
											individualSets.add(individualSet);
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectIndividualSet(individualSets);

								varIndividualSets = oVariable.getIndividualSets();

								for (IndividualSet individualSet: varIndividualSets) {
									if (individualSet.getIndividualCount() < 1) {
										removableTriples = new Vector<Triple>();
										for (Triple triple: triples) {
											IndividualSet tripleIndividualSet = triple.getObjectIndividual();

											if (tripleIndividualSet.compareTo(individualSet) == 0) {
												removableTriples.add(triple);
												varIndividualSets.remove(individualSet);
											}
										}
									}
								}
								triples.removeAll(removableTriples);

								oVariable.intersectIndividualSet(varIndividualSets);

								Set<URI> predicates3 = new ConcurrentSkipListSet<URI>();
								for (Triple triple: triples) {
									URI predicate = triple.getPredicate();

									predicates3.add(predicate);
								}

								pVariable.intersectURIs(predicates3);
							}
						}
					}
				} else {
					System.out.println("The individual " + sURI + " in the " + atom.toString() + " is not asserted.");

					pVariable.makeFinished();
					oVariable.makeFinished();

					return;
				}

			} else if (!arg2.isVar()) {
				String sVarName = arg1.toString();
				String oVarName = arg3.toString();

				Variable sVariable = varsMap.get(sVarName);
				Variable oVariable = varsMap.get(oVarName);

				if (sVariable.hasIndividualsFinished() || (oVariable.hasLiteralsFinished() && oVariable.hasIndividualsFinished())) {
					System.out.println("Either the variable " + sVarName + " or the variable " + oVarName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");

					sVariable.makeFinished();
					oVariable.makeFinished();

					return;
				}

				pURI = URI.create(arg2.getValue());

				if (Janus.ontBridge.containsDataProperty(pURI)) {

					Set<IndividualSet> oIndividualSets = new ConcurrentSkipListSet<IndividualSet>();
					oVariable.intersectIndividualSet(oIndividualSets);

					Set<URI> domains = Janus.ontBridge.getNamedDataPropDomains(pURI);
					URI assertedDomainURI = null;
					for (URI domain: domains) {
						assertedDomainURI = domain;
						break;
					}

					if (Janus.mappingMetadata.getClassType(assertedDomainURI).equals(ClassTypes.COLUMN_CLASS)) {

						Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

						IndividualSet individualSet = new IndividualSet(assertedDomainURI);
						individualSets.add(individualSet);

						Set<URI> familyClasses = individualSet.getFamilyClasses();
						familyClasses.remove(assertedDomainURI);

						for (URI familyClass: familyClasses) {
							IndividualSet familyIndividualSet = new IndividualSet(familyClass);
							if (!Janus.ontBridge.isSubClassOf(familyClass, assertedDomainURI))
								familyIndividualSet.intersectWith(assertedDomainURI);

							individualSets.add(familyIndividualSet);
						}

						sVariable.intersectIndividualSet(individualSets);

						Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();

						String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(assertedDomainURI);
						String mappedColumn = Janus.mappingMetadata.getMappedColumnNameToClass(assertedDomainURI);

						URI mappedDataProperty = Janus.mappingMetadata.getMappedDataProperty(mappedTable, mappedColumn);

						LiteralSet literalSet = new LiteralSet(assertedDomainURI, mappedDataProperty);
						literalSets.add(literalSet);

						for (URI familyClass: familyClasses) {
							mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(familyClass);
							mappedColumn = Janus.mappingMetadata.getMappedColumnNameToClass(familyClass);

							mappedDataProperty = Janus.mappingMetadata.getMappedDataProperty(mappedTable, mappedColumn);

							LiteralSet familyLiteralSet = new LiteralSet(familyClass, mappedDataProperty);
							if (!Janus.ontBridge.isSubClassOf(familyClass, assertedDomainURI))
								familyLiteralSet.intersectWith(assertedDomainURI);

							literalSets.add(familyLiteralSet);
						}

						oVariable.intersectLiteralSet(literalSets);

						Set<IndividualSet> sVarIndividualSets = sVariable.getIndividualSets();
						Set<LiteralSet> oVarLiteralSets = oVariable.getLiteralSets();

						for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
							if (sVarIndividualSet.getIndividualCount() < 1) {
								URI removalClsURI = sVarIndividualSet.getClassURI();

								for(LiteralSet oVarLiteralSet: oVarLiteralSets) {
									if (removalClsURI.equals(oVarLiteralSet.getClassURI()))
										oVarLiteralSets.remove(oVarLiteralSet);
								}

								sVarIndividualSets.remove(sVarIndividualSet);
							}
						}

						for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
							URI sVarclsURI = sVarIndividualSet.getClassURI();

							for(LiteralSet oVarLiteralSet: oVarLiteralSets) {
								if (sVarclsURI.equals(oVarLiteralSet.getClassURI()))
									oVarLiteralSet.intersectWith(sVarIndividualSet);
							}
						}

						for (LiteralSet oVarLiteralSet: oVarLiteralSets) {
							if (oVarLiteralSet.getLiteralCount() < 1) {
								URI removalClsURI = oVarLiteralSet.getClassURI();

								for(IndividualSet sVarIndividualSet: sVarIndividualSets) {
									if (removalClsURI.equals(sVarIndividualSet.getClassURI()))
										sVarIndividualSets.remove(sVarIndividualSet);
								}

								oVarLiteralSets.remove(oVarLiteralSet);
							}
						}

						sVariable.intersectIndividualSet(sVarIndividualSets);
						oVariable.intersectLiteralSet(oVarLiteralSets);

					} else {
						Set<IndividualSet> sVarIndividualSets = sVariable.getIndividualSets();

						IndividualSet individualSet = new IndividualSet(assertedDomainURI);

						for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
							if (sVarIndividualSet.getClassURI().equals(assertedDomainURI)) {
								individualSet.intersectWith(sVarIndividualSet);
							}
						}

						Set<LiteralSet> oVarLiteralSets = oVariable.getLiteralSets();

						LiteralSet literalSet = new LiteralSet(assertedDomainURI, pURI);

						for (LiteralSet oVarLiteralSet: oVarLiteralSets) {
							if (oVarLiteralSet.getClassURI().equals(assertedDomainURI) && oVarLiteralSet.getDataPropertyURI().equals(pURI)) {
								literalSet.intersectWith(oVarLiteralSet);
							}
						}

						literalSet.intersectWith(individualSet);

						if (literalSet.getLiteralCount() < 1) {
							sVariable.makeFinished();
							oVariable.makeFinished();

							return;
						}

						individualSet.intersectWith(literalSet.getSubjectIndividualSet());

						Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();
						individualSets.add(individualSet);

						Set<URI> familyClasses = individualSet.getFamilyClasses();
						familyClasses.remove(assertedDomainURI);

						for (URI familyClass: familyClasses) {
							IndividualSet familyIndividualSet = new IndividualSet(familyClass);

							familyIndividualSet.intersectWith(assertedDomainURI);
							familyIndividualSet.intersectWith(individualSet);

							individualSets.add(familyIndividualSet);
						}

						sVariable.intersectIndividualSet(individualSets);

						Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();
						literalSets.add(literalSet);

						oVariable.intersectLiteralSet(literalSets);
					}

				} else {

					Set<LiteralSet> oLiteralSets = new ConcurrentSkipListSet<LiteralSet>();
					oVariable.intersectLiteralSet(oLiteralSets);

					Set<URI> domains = Janus.ontBridge.getObjPropNamedDomains(pURI);
					URI assertedDomainURI = null;
					for (URI domain: domains) {
						assertedDomainURI = domain;
						break;
					}

					Set<URI> ranges = Janus.ontBridge.getObjPropNamedRanges(pURI);
					URI assertedRangeURI = null;
					for (URI range: ranges) {
						assertedRangeURI = range;
						break;
					}

					Set<IndividualSet> sVarIndividualSets = sVariable.getIndividualSets();

					IndividualSet sIndividualSet = new IndividualSet(assertedDomainURI);

					for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
						if (sVarIndividualSet.getClassURI().equals(assertedDomainURI)) {
							sIndividualSet.intersectWith(sVarIndividualSet);
						}
					}

					Set<IndividualSet> oVarIndividualSets = oVariable.getIndividualSets();

					IndividualSet oIndividualSet = new IndividualSet(assertedRangeURI);

					for (IndividualSet oVarIndividualSet: oVarIndividualSets) {
						if (oVarIndividualSet.getClassURI().equals(assertedRangeURI)) {
							oIndividualSet.intersectWith(oVarIndividualSet);
						}
					}
					
					oIndividualSet.intersectWith(sIndividualSet);

					if (oIndividualSet.getIndividualCount() < 1) {
						sVariable.makeFinished();
						oVariable.makeFinished();
				
						return;
					}

					sIndividualSet.intersectWith(oIndividualSet.getSubjectIndividualSet());

					Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();
					individualSets.add(sIndividualSet);

					Set<URI> familyClasses = sIndividualSet.getFamilyClasses();
					familyClasses.remove(assertedDomainURI);

					for (URI familyClass: familyClasses) {
						IndividualSet familyIndividualSet = new IndividualSet(familyClass);

						familyIndividualSet.intersectWith(assertedDomainURI);
						familyIndividualSet.intersectWith(sIndividualSet);

						individualSets.add(familyIndividualSet);
					}

					sVariable.intersectIndividualSet(individualSets);

					individualSets = new ConcurrentSkipListSet<IndividualSet>();
					individualSets.add(oIndividualSet);

					familyClasses = oIndividualSet.getFamilyClasses();
					familyClasses.remove(assertedRangeURI);

					for (URI familyClass: familyClasses) {
						IndividualSet familyIndividualSet = new IndividualSet(familyClass);

						familyIndividualSet.intersectWith(assertedRangeURI);
						familyIndividualSet.intersectWith(oIndividualSet);

						individualSets.add(familyIndividualSet);
					}

					oVariable.intersectIndividualSet(individualSets);
				}

			} else {
				String sVarName = arg1.toString();
				String pVarName = arg2.toString();

				Variable sVariable = varsMap.get(sVarName);
				Variable pVariable = varsMap.get(pVarName);

				if (sVariable.hasIndividualsFinished() || pVariable.hasLiteralsFinished()) {
					System.out.println("Either the variable " + sVarName + " or the variable " + pVarName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");

					sVariable.makeFinished();
					pVariable.makeFinished();

					return;
				}

				if (arg3.isURI())
					oURI = URI.create(arg3.getValue());
				else
					oLit = arg3.getValue();

				if (oURI != null) {
					Individual individual = new Individual(oURI);

					if (individual.isExistentIndividual()) {
						List<Triple> triples = individual.getSPTriples();

						Set<URI> familyClasses = individual.getFamilyClasses();
						familyClasses.remove(individual.getClassURI());

						for (URI familyClass: familyClasses) {
							URI sameAsObjectIndividualURI = individual.getTypeChangedIndividual(familyClass);
							Individual sameAsObjectIndividual = new Individual(sameAsObjectIndividualURI);

							if (sameAsObjectIndividual.isExistentIndividual()) {
								List<Triple> sameAsObjectTriples = sameAsObjectIndividual.getSPTriples();

								for (Triple triple: sameAsObjectTriples)
									triple.setObject(oURI);

								triples.addAll(sameAsObjectTriples);
							}
						}

						Set<IndividualSet> sVarIndividualSets = sVariable.getIndividualSets();
						Set<URI> pVarURIs = pVariable.getURISet();

						if (sVarIndividualSets.size() == 0 && pVarURIs.size() == 0) {

							Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

							for (Triple triple: triples) {
								IndividualSet individualSet = triple.getSubjectIndividual();

								if (individualSets.contains(individualSet)) {
									for (IndividualSet containedIndividualSet: individualSets) {
										if (containedIndividualSet.compareTo(individualSet) == 0) {
											Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
											Set<String> whereSet = individualSet.getValueWhereSet();

											if (!containedWhereSet.containsAll(whereSet))
												containedIndividualSet.connectValueWhereWithOR(whereSet);
										}
									}
								} else 
									individualSets.add(individualSet);

							}

							sVariable.intersectIndividualSet(individualSets);


							Set<URI> predicates = new ConcurrentSkipListSet<URI>();

							for (Triple triple: triples) {
								URI predicate = triple.getPredicate();

								predicates.add(predicate);
							}

							pVariable.intersectURIs(predicates);

						} else if (sVarIndividualSets.size() == 0 && pVarURIs.size() > 0) {

							Set<URI> predicates = new ConcurrentSkipListSet<URI>();

							List<Triple> removableTriples = new Vector<Triple>();
							for (Triple triple: triples) {
								URI predicate = triple.getPredicate();

								if (!pVarURIs.contains(predicate))
									removableTriples.add(triple);
								else
									predicates.add(predicate);
							}
							triples.removeAll(removableTriples);

							pVariable.intersectURIs(predicates);

							Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

							for (Triple triple: triples) {
								IndividualSet individualSet = triple.getSubjectIndividual();

								if (individualSets.contains(individualSet)) {
									for (IndividualSet containedIndividualSet: individualSets) {
										if (containedIndividualSet.compareTo(individualSet) == 0) {
											Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
											Set<String> whereSet = individualSet.getValueWhereSet();

											if (!containedWhereSet.containsAll(whereSet))
												containedIndividualSet.connectValueWhereWithOR(whereSet);
										}
									}
								} else 
									individualSets.add(individualSet);

							}

							sVariable.intersectIndividualSet(individualSets);

						} else if (sVarIndividualSets.size() > 0 && pVarURIs.size() == 0) {

							Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

							List<Triple> removableTriples = new Vector<Triple>();
							for (Triple triple: triples) {
								IndividualSet individualSet = triple.getSubjectIndividual();

								if (!sVarIndividualSets.contains(individualSet))
									removableTriples.add(triple);
								else {
									if (individualSets.contains(individualSet)) {
										for (IndividualSet containedIndividualSet: individualSets) {
											if (containedIndividualSet.compareTo(individualSet) == 0) {
												Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
												Set<String> whereSet = individualSet.getValueWhereSet();

												if (!containedWhereSet.containsAll(whereSet))
													containedIndividualSet.connectValueWhereWithOR(whereSet);
											}
										}
									} else 
										individualSets.add(individualSet);
								}
							}
							triples.removeAll(removableTriples);

							sVariable.intersectIndividualSet(individualSets);

							sVarIndividualSets = sVariable.getIndividualSets();

							for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
								if (sVarIndividualSet.getIndividualCount() < 1) {
									URI removalClsURI = sVarIndividualSet.getClassURI();

									removableTriples = new Vector<Triple>();
									for (Triple triple: triples) {
										IndividualSet individualSet = triple.getSubjectIndividual();

										if (removalClsURI.equals(individualSet.getClassURI()))
											removableTriples.add(triple);

									}
									triples.removeAll(removableTriples);

									sVarIndividualSets.remove(sVarIndividualSet);
								}
							}

							sVariable.intersectIndividualSet(sVarIndividualSets);

							Set<URI> predicates = new ConcurrentSkipListSet<URI>();

							for (Triple triple: triples) {
								URI predicate = triple.getPredicate();

								predicates.add(predicate);
							}

							pVariable.intersectURIs(predicates);

						} else {

							Set<URI> predicates = new ConcurrentSkipListSet<URI>();

							List<Triple> removableTriples = new Vector<Triple>();
							for (Triple triple: triples) {
								URI predicate = triple.getPredicate();

								if (!pVarURIs.contains(predicate))
									removableTriples.add(triple);
								else
									predicates.add(predicate);
							}
							triples.removeAll(removableTriples);

							pVariable.intersectURIs(predicates);

							Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

							removableTriples = new Vector<Triple>();
							for (Triple triple: triples) {
								IndividualSet individualSet = triple.getSubjectIndividual();

								if (!sVarIndividualSets.contains(individualSet))
									removableTriples.add(triple);
								else {
									if (individualSets.contains(individualSet)) {
										for (IndividualSet containedIndividualSet: individualSets) {
											if (containedIndividualSet.compareTo(individualSet) == 0) {
												Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
												Set<String> whereSet = individualSet.getValueWhereSet();

												if (!containedWhereSet.containsAll(whereSet))
													containedIndividualSet.connectValueWhereWithOR(whereSet);
											}
										}
									} else 
										individualSets.add(individualSet);
								}
							}
							triples.removeAll(removableTriples);

							sVariable.intersectIndividualSet(individualSets);

							sVarIndividualSets = sVariable.getIndividualSets();

							for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
								if (sVarIndividualSet.getIndividualCount() < 1) {
									URI removalClsURI = sVarIndividualSet.getClassURI();

									removableTriples = new Vector<Triple>();
									for (Triple triple: triples) {
										IndividualSet individualSet = triple.getSubjectIndividual();

										if (removalClsURI.equals(individualSet.getClassURI()))
											removableTriples.add(triple);

									}
									triples.removeAll(removableTriples);

									sVarIndividualSets.remove(sVarIndividualSet);
								}
							}

							sVariable.intersectIndividualSet(sVarIndividualSets);

							Set<URI> predicates2 = new ConcurrentSkipListSet<URI>();

							for (Triple triple: triples) {
								URI predicate = triple.getPredicate();

								predicates2.add(predicate);
							}

							pVariable.intersectURIs(predicates2);

						}

					} else {
						System.out.println(oURI + " in the atom " + atom + " is not asserted.");

						sVariable.makeFinished();
						pVariable.makeFinished();
					}
				} else {

					Literal oLiteral = new Literal(oLit);
					List<Triple> triples = oLiteral.getSPTriples();

					Set<IndividualSet> sVarIndividualSets = sVariable.getIndividualSets();
					Set<URI> pVarURIs = pVariable.getURISet();

					if (sVarIndividualSets.size() == 0 && pVarURIs.size() == 0) {

						Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

						for (Triple triple: triples) {
							IndividualSet individualSet = triple.getSubjectIndividual();

							if (individualSets.contains(individualSet)) {
								for (IndividualSet containedIndividualSet: individualSets) {
									if (containedIndividualSet.compareTo(individualSet) == 0) {
										Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
										Set<String> whereSet = individualSet.getValueWhereSet();

										if (!containedWhereSet.containsAll(whereSet))
											containedIndividualSet.connectValueWhereWithOR(whereSet);
									}
								}
							} else 
								individualSets.add(individualSet);

						}

						sVariable.intersectIndividualSet(individualSets);


						Set<URI> predicates = new ConcurrentSkipListSet<URI>();

						for (Triple triple: triples) {
							URI predicate = triple.getPredicate();

							predicates.add(predicate);
						}

						pVariable.intersectURIs(predicates);

					} else if (sVarIndividualSets.size() == 0 && pVarURIs.size() > 0) {

						Set<URI> predicates = new ConcurrentSkipListSet<URI>();

						List<Triple> removableTriples = new Vector<Triple>();
						for (Triple triple: triples) {
							URI predicate = triple.getPredicate();

							if (!pVarURIs.contains(predicate))
								removableTriples.add(triple);
							else
								predicates.add(predicate);
						}
						triples.removeAll(removableTriples);

						pVariable.intersectURIs(predicates);

						Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

						for (Triple triple: triples) {
							IndividualSet individualSet = triple.getSubjectIndividual();

							if (individualSets.contains(individualSet)) {
								for (IndividualSet containedIndividualSet: individualSets) {
									if (containedIndividualSet.compareTo(individualSet) == 0) {
										Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
										Set<String> whereSet = individualSet.getValueWhereSet();

										if (!containedWhereSet.containsAll(whereSet))
											containedIndividualSet.connectValueWhereWithOR(whereSet);
									}
								}
							} else 
								individualSets.add(individualSet);

						}

						sVariable.intersectIndividualSet(individualSets);

					} else if (sVarIndividualSets.size() > 0 && pVarURIs.size() == 0) {

						Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

						List<Triple> removableTriples = new Vector<Triple>();
						for (Triple triple: triples) {
							IndividualSet individualSet = triple.getSubjectIndividual();

							if (!sVarIndividualSets.contains(individualSet))
								removableTriples.add(triple);
							else {
								if (individualSets.contains(individualSet)) {
									for (IndividualSet containedIndividualSet: individualSets) {
										if (containedIndividualSet.compareTo(individualSet) == 0) {
											Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
											Set<String> whereSet = individualSet.getValueWhereSet();

											if (!containedWhereSet.containsAll(whereSet))
												containedIndividualSet.connectValueWhereWithOR(whereSet);
										}
									}
								} else 
									individualSets.add(individualSet);
							}
						}
						triples.removeAll(removableTriples);

						sVariable.intersectIndividualSet(individualSets);

						sVarIndividualSets = sVariable.getIndividualSets();

						for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
							if (sVarIndividualSet.getIndividualCount() < 1) {
								URI removalClsURI = sVarIndividualSet.getClassURI();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getSubjectIndividual();

									if (removalClsURI.equals(individualSet.getClassURI()))
										removableTriples.add(triple);

								}
								triples.removeAll(removableTriples);

								sVarIndividualSets.remove(sVarIndividualSet);
							}
						}

						sVariable.intersectIndividualSet(sVarIndividualSets);

						Set<URI> predicates = new ConcurrentSkipListSet<URI>();

						for (Triple triple: triples) {
							URI predicate = triple.getPredicate();

							predicates.add(predicate);
						}

						pVariable.intersectURIs(predicates);

					} else {

						Set<URI> predicates = new ConcurrentSkipListSet<URI>();

						List<Triple> removableTriples = new Vector<Triple>();
						for (Triple triple: triples) {
							URI predicate = triple.getPredicate();

							if (!pVarURIs.contains(predicate))
								removableTriples.add(triple);
							else
								predicates.add(predicate);
						}
						triples.removeAll(removableTriples);

						pVariable.intersectURIs(predicates);

						Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();

						removableTriples = new Vector<Triple>();
						for (Triple triple: triples) {
							IndividualSet individualSet = triple.getSubjectIndividual();

							if (!sVarIndividualSets.contains(individualSet))
								removableTriples.add(triple);
							else {
								if (individualSets.contains(individualSet)) {
									for (IndividualSet containedIndividualSet: individualSets) {
										if (containedIndividualSet.compareTo(individualSet) == 0) {
											Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
											Set<String> whereSet = individualSet.getValueWhereSet();

											if (!containedWhereSet.containsAll(whereSet))
												containedIndividualSet.connectValueWhereWithOR(whereSet);
										}
									}
								} else 
									individualSets.add(individualSet);
							}
						}
						triples.removeAll(removableTriples);

						sVariable.intersectIndividualSet(individualSets);

						sVarIndividualSets = sVariable.getIndividualSets();

						for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
							if (sVarIndividualSet.getIndividualCount() < 1) {
								URI removalClsURI = sVarIndividualSet.getClassURI();

								removableTriples = new Vector<Triple>();
								for (Triple triple: triples) {
									IndividualSet individualSet = triple.getSubjectIndividual();

									if (removalClsURI.equals(individualSet.getClassURI()))
										removableTriples.add(triple);

								}
								triples.removeAll(removableTriples);

								sVarIndividualSets.remove(sVarIndividualSet);
							}
						}

						sVariable.intersectIndividualSet(sVarIndividualSets);

						Set<URI> predicates2 = new ConcurrentSkipListSet<URI>();

						for (Triple triple: triples) {
							URI predicate = triple.getPredicate();

							predicates2.add(predicate);
						}

						pVariable.intersectURIs(predicates2);

					}
				}
			}
		}
	}
}
