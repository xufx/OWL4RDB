package janus.sparqldl;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.types.QueryAtomType;

public class ABox3Processor {
	void execute(QueryAtom atom, Map<String, Variable> varsMap) {

		if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
			List<QueryArgument> args = atom.getArguments();

			QueryArgument arg1 = args.get(0);
			QueryArgument arg2 = args.get(1);
			QueryArgument arg3 = args.get(2);

			String sVarName = arg1.toString();
			String pVarName = arg2.toString();
			String oVarName = arg3.toString();

			Variable sVariable = varsMap.get(sVarName);
			Variable pVariable = varsMap.get(pVarName);
			Variable oVariable = varsMap.get(oVarName);
if (pVariable == null) System.out.println("null");
			if (sVariable.hasIndividualsFinished() || pVariable.hasURIsFinished() || (oVariable.hasLiteralsFinished() && oVariable.hasIndividualsFinished())) {
				System.out.println("Since some of the variables in the " + atom.toString() + " have already been the empty set, this atom doesn't have to be calculated.");

				sVariable.makeFinished();
				pVariable.makeFinished();
				oVariable.makeFinished();

				return;
			}

			Set<IndividualSet> sVarIndividualSets = sVariable.getIndividualSets();
			Set<URI> varProperties = pVariable.getURISet();
			Set<IndividualSet> oVarIndividualSets = oVariable.getIndividualSets();
			Set<LiteralSet> oVarLiteralSets = oVariable.getLiteralSets();

			if (sVarIndividualSets.size() == 0 && varProperties.size() == 0 && oVarIndividualSets.size() == 0 && oVarLiteralSets.size() == 0) {
				List<Triple> triples = Utils.getAllTriples();

				Set<IndividualSet> sIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: triples) {
					IndividualSet individualSet = triple.getSubjectIndividual();

					if (sIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: sIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						sIndividualSets.add(individualSet);
				}

				sVariable.intersectIndividualSet(sIndividualSets);

				Set<URI> predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: triples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);

				Set<IndividualSet> oIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: triples) {
					IndividualSet individualSet = triple.getObjectIndividual();

					if (individualSet == null) continue;

					if (oIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: oIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oIndividualSets.add(individualSet);
				}

				oVariable.intersectIndividualSet(oIndividualSets);

				Set<LiteralSet> oLiteralSets = new ConcurrentSkipListSet<LiteralSet>();

				for (Triple triple: triples) {
					LiteralSet literalSet = triple.getObjectLiteral();

					if (literalSet == null) continue;

					if (oLiteralSets.contains(literalSet)) {
						for (LiteralSet containedLiteralSet: oLiteralSets) {
							if (containedLiteralSet.compareTo(literalSet) == 0) {
								Set<String> containedWhereSet = containedLiteralSet.getValueWhereSet();
								Set<String> whereSet = literalSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedLiteralSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedLiteralSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oLiteralSets.add(literalSet);
				}

				oVariable.intersectLiteralSet(oLiteralSets);


			} else if (sVarIndividualSets.size() == 0 && varProperties.size() == 0 && oVarIndividualSets.size() == 0 && oVarLiteralSets.size() > 0) {

				List<Triple> triples = new Vector<Triple>();

				for (LiteralSet oVarLiteralSet: oVarLiteralSets)
					triples.addAll(oVarLiteralSet.getSPTriples());

				Set<IndividualSet> sIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: triples) {
					IndividualSet individualSet = triple.getSubjectIndividual();

					if (sIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: sIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						sIndividualSets.add(individualSet);
				}

				sVariable.intersectIndividualSet(sIndividualSets);

				Set<URI> predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: triples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);

				Set<IndividualSet> oIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				oVariable.intersectIndividualSet(oIndividualSets);

			} else if (sVarIndividualSets.size() == 0 && varProperties.size() == 0 && oVarIndividualSets.size() > 0 && oVarLiteralSets.size() == 0) {

				List<Triple> triples = new Vector<Triple>();

				for (IndividualSet oVarIndividualSet: oVarIndividualSets)
					triples.addAll(oVarIndividualSet.getSPTriples());

				Set<IndividualSet> sIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: triples) {
					IndividualSet individualSet = triple.getSubjectIndividual();

					if (sIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: sIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						sIndividualSets.add(individualSet);
				}

				sVariable.intersectIndividualSet(sIndividualSets);

				Set<URI> predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: triples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);

				Set<LiteralSet> oLiteralSets = new ConcurrentSkipListSet<LiteralSet>();

				oVariable.intersectLiteralSet(oLiteralSets);

			} else if (sVarIndividualSets.size() == 0 && varProperties.size() == 0 && oVarIndividualSets.size() > 0 && oVarLiteralSets.size() > 0) {

				List<Triple> triples = new Vector<Triple>();

				for (IndividualSet oVarIndividualSet: oVarIndividualSets)
					triples.addAll(oVarIndividualSet.getSPTriples());

				for (LiteralSet oVarLiteralSet: oVarLiteralSets)
					triples.addAll(oVarLiteralSet.getSPTriples());

				Set<IndividualSet> sIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: triples) {
					IndividualSet individualSet = triple.getSubjectIndividual();

					if (sIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: sIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						sIndividualSets.add(individualSet);
				}

				sVariable.intersectIndividualSet(sIndividualSets);

				sVarIndividualSets = sVariable.getIndividualSets();

				for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
					if (sVarIndividualSet.getIndividualCount() < 1) {
						URI removalClsURI = sVarIndividualSet.getClassURI();

						List<Triple> removableTriples = new Vector<Triple>();
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

			} else if (sVarIndividualSets.size() == 0 && varProperties.size() > 0 && oVarIndividualSets.size() == 0 && oVarLiteralSets.size() == 0) {

				List<Triple> triples = Utils.getAllTriples(varProperties);

				Set<IndividualSet> sIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: triples) {
					IndividualSet individualSet = triple.getSubjectIndividual();

					if (sIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: sIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						sIndividualSets.add(individualSet);
				}

				sVariable.intersectIndividualSet(sIndividualSets);

				sVarIndividualSets = sVariable.getIndividualSets();

				for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
					if (sVarIndividualSet.getIndividualCount() < 1) {
						URI removalClsURI = sVarIndividualSet.getClassURI();

						List<Triple> removableTriples = new Vector<Triple>();
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

				Set<IndividualSet> oIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: triples) {
					IndividualSet individualSet = triple.getObjectIndividual();

					if (individualSet == null) continue;

					if (oIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: oIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oIndividualSets.add(individualSet);
				}

				oVariable.intersectIndividualSet(oIndividualSets);

				oVarIndividualSets = oVariable.getIndividualSets();

				for (IndividualSet oVarIndividualSet: oVarIndividualSets) {
					if (oVarIndividualSet.getIndividualCount() < 1) {
						URI removalClsURI = oVarIndividualSet.getClassURI();

						List<Triple> removableTriples = new Vector<Triple>();
						for (Triple triple: triples) {
							IndividualSet individualSet = triple.getObjectIndividual();

							if (removalClsURI.equals(individualSet.getClassURI()))
								removableTriples.add(triple);

						}
						triples.removeAll(removableTriples);

						oVarIndividualSets.remove(oVarIndividualSet);
					}
				}

				oVariable.intersectIndividualSet(oVarIndividualSets);

				Set<LiteralSet> oLiteralSets = new ConcurrentSkipListSet<LiteralSet>();

				for (Triple triple: triples) {

					LiteralSet literalSet = triple.getObjectLiteral();

					if (literalSet == null) continue;

					if (oLiteralSets.contains(literalSet)) {
						for (LiteralSet containedLiteralSet: oLiteralSets) {
							if (containedLiteralSet.compareTo(literalSet) == 0) {
								Set<String> containedWhereSet = containedLiteralSet.getValueWhereSet();
								Set<String> whereSet = literalSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedLiteralSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedLiteralSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oLiteralSets.add(literalSet);
				}

				oVariable.intersectLiteralSet(oLiteralSets);

				oVarLiteralSets = oVariable.getLiteralSets();

				for (LiteralSet oVarLiteralSet: oVarLiteralSets) {
					if (oVarLiteralSet.getLiteralCount() < 1) {
						String removalSelectColumn = oVarLiteralSet.getSelectColumn();

						List<Triple> removableTriples = new Vector<Triple>();
						for (Triple triple: triples) {
							LiteralSet literalSet = triple.getObjectLiteral();

							if (removalSelectColumn.equals(literalSet.getSelectColumn()))
								removableTriples.add(triple);

						}
						triples.removeAll(removableTriples);

						oVarLiteralSets.remove(oVarLiteralSet);
					}
				}

				oVariable.intersectLiteralSet(oVarLiteralSets);

				Set<URI> predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: triples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);

			} else if (sVarIndividualSets.size() == 0 && varProperties.size() > 0 && oVarIndividualSets.size() == 0 && oVarLiteralSets.size() > 0) {

				List<Triple> oVarLiteralTriples = new Vector<Triple>();

				for (LiteralSet oVarLiteralSet: oVarLiteralSets)
					oVarLiteralTriples.addAll(oVarLiteralSet.getSPTriples());

				Set<URI> predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: oVarLiteralTriples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);

				varProperties = pVariable.getURISet();

				List<Triple> removableTriples = new Vector<Triple>();
				for (Triple triple: oVarLiteralTriples) {
					URI predicate = triple.getPredicate();

					if (!varProperties.contains(predicate))
						removableTriples.add(triple);
				}
				oVarLiteralTriples.removeAll(removableTriples);

				Set<IndividualSet> sIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: oVarLiteralTriples) {
					IndividualSet individualSet = triple.getSubjectIndividual();

					if (sIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: sIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						sIndividualSets.add(individualSet);
				}

				sVariable.intersectIndividualSet(sIndividualSets);

				sVarIndividualSets = sVariable.getIndividualSets();

				for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
					if (sVarIndividualSet.getIndividualCount() < 1) {
						URI removalClsURI = sVarIndividualSet.getClassURI();

						removableTriples = new Vector<Triple>();
						for (Triple triple: oVarLiteralTriples) {
							IndividualSet individualSet = triple.getSubjectIndividual();

							if (removalClsURI.equals(individualSet.getClassURI()))
								removableTriples.add(triple);

						}
						oVarLiteralTriples.removeAll(removableTriples);

						sVarIndividualSets.remove(sVarIndividualSet);
					}
				}

				sVariable.intersectIndividualSet(sIndividualSets);

				Set<LiteralSet> oLiteralSets = new ConcurrentSkipListSet<LiteralSet>();

				for (Triple triple: oVarLiteralTriples) {

					LiteralSet literalSet = triple.getObjectLiteral();

					if (literalSet == null) continue;

					if (oLiteralSets.contains(literalSet)) {
						for (LiteralSet containedLiteralSet: oLiteralSets) {
							if (containedLiteralSet.compareTo(literalSet) == 0) {
								Set<String> containedWhereSet = containedLiteralSet.getValueWhereSet();
								Set<String> whereSet = literalSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedLiteralSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedLiteralSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oLiteralSets.add(literalSet);
				}

				oVariable.intersectLiteralSet(oLiteralSets);

				oVarLiteralSets = oVariable.getLiteralSets();

				for (LiteralSet oVarLiteralSet: oVarLiteralSets) {
					if (oVarLiteralSet.getLiteralCount() < 1) {
						String removalSelectColumn = oVarLiteralSet.getSelectColumn();

						removableTriples = new Vector<Triple>();
						for (Triple triple: oVarLiteralTriples) {
							LiteralSet literalSet = triple.getObjectLiteral();

							if (removalSelectColumn.equals(literalSet.getSelectColumn()))
								removableTriples.add(triple);

						}
						oVarLiteralTriples.removeAll(removableTriples);

						oVarLiteralSets.remove(oVarLiteralSet);
					}
				}

				oVariable.intersectLiteralSet(oVarLiteralSets);

				predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: oVarLiteralTriples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);

			} else if (sVarIndividualSets.size() == 0 && varProperties.size() > 0 && oVarIndividualSets.size() > 0 && oVarLiteralSets.size() == 0) {
				List<Triple> oVarIndividualTriples = new Vector<Triple>();

				for (IndividualSet oVarIndividualSet: oVarIndividualSets)
					oVarIndividualTriples.addAll(oVarIndividualSet.getSPTriples());

				Set<URI> predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: oVarIndividualTriples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);

				varProperties = pVariable.getURISet();

				List<Triple> removableTriples = new Vector<Triple>();
				for (Triple triple: oVarIndividualTriples) {
					URI predicate = triple.getPredicate();

					if (!varProperties.contains(predicate))
						removableTriples.add(triple);
				}
				oVarIndividualTriples.removeAll(removableTriples);

				Set<IndividualSet> sIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: oVarIndividualTriples) {
					IndividualSet individualSet = triple.getSubjectIndividual();

					if (sIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: sIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						sIndividualSets.add(individualSet);
				}

				sVariable.intersectIndividualSet(sIndividualSets);

				sVarIndividualSets = sVariable.getIndividualSets();

				for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
					if (sVarIndividualSet.getIndividualCount() < 1) {
						URI removalClsURI = sVarIndividualSet.getClassURI();

						removableTriples = new Vector<Triple>();
						for (Triple triple: oVarIndividualTriples) {
							IndividualSet individualSet = triple.getSubjectIndividual();

							if (removalClsURI.equals(individualSet.getClassURI()))
								removableTriples.add(triple);

						}
						oVarIndividualTriples.removeAll(removableTriples);

						sVarIndividualSets.remove(sVarIndividualSet);
					}
				}

				sVariable.intersectIndividualSet(sIndividualSets);

				Set<IndividualSet> oIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: oVarIndividualTriples) {

					IndividualSet individualSet = triple.getObjectIndividual();

					if (individualSet == null) continue;

					if (oIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: oIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oIndividualSets.add(individualSet);
				}

				oVariable.intersectIndividualSet(oIndividualSets);

				oVarIndividualSets = oVariable.getIndividualSets();

				for (IndividualSet oVarIndividualSet: oVarIndividualSets) {
					if (oVarIndividualSet.getIndividualCount() < 1) {
						URI removalClsURI = oVarIndividualSet.getClassURI();

						removableTriples = new Vector<Triple>();
						for (Triple triple: oVarIndividualTriples) {
							IndividualSet individualSet = triple.getObjectIndividual();

							if (removalClsURI.equals(individualSet.getClassURI()))
								removableTriples.add(triple);

						}
						oVarIndividualTriples.removeAll(removableTriples);

						oVarIndividualSets.remove(oVarIndividualSet);
					}
				}

				oVariable.intersectIndividualSet(oVarIndividualSets);

				predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: oVarIndividualTriples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);

			} else if (sVarIndividualSets.size() == 0 && varProperties.size() > 0 && oVarIndividualSets.size() > 0 && oVarLiteralSets.size() > 0) {
				List<Triple> oVarTriples = new Vector<Triple>();

				for (IndividualSet oVarIndividualSet: oVarIndividualSets)
					oVarTriples.addAll(oVarIndividualSet.getSPTriples());

				for (LiteralSet oVarLiteralSet: oVarLiteralSets)
					oVarTriples.addAll(oVarLiteralSet.getSPTriples());

				Set<URI> predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: oVarTriples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);

				varProperties = pVariable.getURISet();

				List<Triple> removableTriples = new Vector<Triple>();
				for (Triple triple: oVarTriples) {
					URI predicate = triple.getPredicate();

					if (!varProperties.contains(predicate))
						removableTriples.add(triple);
				}
				oVarTriples.removeAll(removableTriples);

				Set<IndividualSet> sIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: oVarTriples) {
					IndividualSet individualSet = triple.getSubjectIndividual();

					if (sIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: sIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						sIndividualSets.add(individualSet);
				}

				sVariable.intersectIndividualSet(sIndividualSets);

				sVarIndividualSets = sVariable.getIndividualSets();

				for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
					if (sVarIndividualSet.getIndividualCount() < 1) {
						URI removalClsURI = sVarIndividualSet.getClassURI();

						removableTriples = new Vector<Triple>();
						for (Triple triple: oVarTriples) {
							IndividualSet individualSet = triple.getSubjectIndividual();

							if (removalClsURI.equals(individualSet.getClassURI()))
								removableTriples.add(triple);

						}
						oVarTriples.removeAll(removableTriples);

						sVarIndividualSets.remove(sVarIndividualSet);
					}
				}

				sVariable.intersectIndividualSet(sIndividualSets);

				Set<IndividualSet> oIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: oVarTriples) {

					IndividualSet individualSet = triple.getObjectIndividual();

					if (individualSet == null) continue;

					if (oIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: oIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oIndividualSets.add(individualSet);
				}

				oVariable.intersectIndividualSet(oIndividualSets);

				oVarIndividualSets = oVariable.getIndividualSets();

				for (IndividualSet oVarIndividualSet: oVarIndividualSets) {
					if (oVarIndividualSet.getIndividualCount() < 1) {
						URI removalClsURI = oVarIndividualSet.getClassURI();

						removableTriples = new Vector<Triple>();
						for (Triple triple: oVarTriples) {
							IndividualSet individualSet = triple.getObjectIndividual();

							if (removalClsURI.equals(individualSet.getClassURI()))
								removableTriples.add(triple);

						}
						oVarTriples.removeAll(removableTriples);

						oVarIndividualSets.remove(oVarIndividualSet);
					}
				}

				oVariable.intersectIndividualSet(oVarIndividualSets);

				Set<LiteralSet> oLiteralSets = new ConcurrentSkipListSet<LiteralSet>();

				for (Triple triple: oVarTriples) {

					LiteralSet literalSet = triple.getObjectLiteral();

					if (literalSet == null) continue;

					if (oLiteralSets.contains(literalSet)) {
						for (LiteralSet containedLiteralSet: oLiteralSets) {
							if (containedLiteralSet.compareTo(literalSet) == 0) {
								Set<String> containedWhereSet = containedLiteralSet.getValueWhereSet();
								Set<String> whereSet = literalSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedLiteralSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedLiteralSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oLiteralSets.add(literalSet);
				}

				oVariable.intersectLiteralSet(oLiteralSets);

				oVarLiteralSets = oVariable.getLiteralSets();

				for (LiteralSet oVarLiteralSet: oVarLiteralSets) {
					if (oVarLiteralSet.getLiteralCount() < 1) {
						String removalSelectColumn = oVarLiteralSet.getSelectColumn();

						removableTriples = new Vector<Triple>();
						for (Triple triple: oVarTriples) {
							LiteralSet literalSet = triple.getObjectLiteral();

							if (removalSelectColumn.equals(literalSet.getSelectColumn()))
								removableTriples.add(triple);

						}
						oVarTriples.removeAll(removableTriples);

						oVarLiteralSets.remove(oVarLiteralSet);
					}
				}

				oVariable.intersectLiteralSet(oVarLiteralSets);

				predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: oVarTriples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);

			} else if (sVarIndividualSets.size() > 0 && varProperties.size() == 0 && oVarIndividualSets.size() == 0 && oVarLiteralSets.size() == 0) {
				List<Triple> sVarIndividualTriples = new Vector<Triple>();

				for (IndividualSet sVarIndividualSet: sVarIndividualSets)
					sVarIndividualTriples.addAll(sVarIndividualSet.getPOTriples());

				Set<IndividualSet> oIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: sVarIndividualTriples) {
					IndividualSet individualSet = triple.getObjectIndividual();

					if (individualSet == null) continue;

					if (oIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: oIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oIndividualSets.add(individualSet);
				}

				oVariable.intersectIndividualSet(oIndividualSets);

				Set<LiteralSet> oLiteralSets = new ConcurrentSkipListSet<LiteralSet>();

				for (Triple triple: sVarIndividualTriples) {
					LiteralSet literalSet = triple.getObjectLiteral();

					if (literalSet == null) continue;

					if (oLiteralSets.contains(literalSet)) {
						for (LiteralSet containedLiteralSet: oLiteralSets) {
							if (containedLiteralSet.compareTo(literalSet) == 0) {
								Set<String> containedWhereSet = containedLiteralSet.getValueWhereSet();
								Set<String> whereSet = literalSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedLiteralSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedLiteralSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oLiteralSets.add(literalSet);
				}

				oVariable.intersectLiteralSet(oLiteralSets);

				Set<URI> predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: sVarIndividualTriples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);

			} else if (sVarIndividualSets.size() > 0 && varProperties.size() == 0 && oVarIndividualSets.size() == 0 && oVarLiteralSets.size() > 0) {
				
				List<Triple> sVarIndividualTriples = new Vector<Triple>();

				for (IndividualSet sVarIndividualSet: sVarIndividualSets)
					sVarIndividualTriples.addAll(sVarIndividualSet.getPOTriples());
				
				Set<LiteralSet> oLiteralSets = new ConcurrentSkipListSet<LiteralSet>();

				for (Triple triple: sVarIndividualTriples) {
					LiteralSet literalSet = triple.getObjectLiteral();

					if (literalSet == null) continue;

					if (oLiteralSets.contains(literalSet)) {
						for (LiteralSet containedLiteralSet: oLiteralSets) {
							if (containedLiteralSet.compareTo(literalSet) == 0) {
								Set<String> containedWhereSet = containedLiteralSet.getValueWhereSet();
								Set<String> whereSet = literalSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedLiteralSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedLiteralSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oLiteralSets.add(literalSet);
				}

				oVariable.intersectLiteralSet(oLiteralSets);
				
				oVarLiteralSets = oVariable.getLiteralSets();

				for (LiteralSet oVarLiteralSet: oVarLiteralSets) {
					if (oVarLiteralSet.getLiteralCount() < 1) {
						String removalSelectColumn = oVarLiteralSet.getSelectColumn();

						List<Triple> removableTriples = new Vector<Triple>();
						for (Triple triple: sVarIndividualTriples) {
							LiteralSet literalSet = triple.getObjectLiteral();
							
							if (literalSet == null || removalSelectColumn.equals(literalSet.getSelectColumn()))
								removableTriples.add(triple);

						}
						sVarIndividualTriples.removeAll(removableTriples);

						oVarLiteralSets.remove(oVarLiteralSet);
					}
				}

				oVariable.intersectLiteralSet(oVarLiteralSets);
				
				oVarLiteralSets = oVariable.getLiteralSets();
				
				List<Triple> oVarLiteralTriples = new Vector<Triple>();

				for (LiteralSet oVarLiteralSet: oVarLiteralSets)
					oVarLiteralTriples.addAll(oVarLiteralSet.getSPTriples());
				
				Set<IndividualSet> sIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: oVarLiteralTriples) {
					IndividualSet individualSet = triple.getSubjectIndividual();

					if (sIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: sIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						sIndividualSets.add(individualSet);
				}

				sVariable.intersectIndividualSet(sIndividualSets);
				
				sVarIndividualSets = sVariable.getIndividualSets();

				for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
					if (sVarIndividualSet.getIndividualCount() < 1) {
						URI removalClsURI = sVarIndividualSet.getClassURI();

						List<Triple> removableTriples = new Vector<Triple>();
						for (Triple triple: oVarLiteralTriples) {
							IndividualSet individualSet = triple.getSubjectIndividual();

							if (removalClsURI.equals(individualSet.getClassURI()))
								removableTriples.add(triple);

						}
						oVarLiteralTriples.removeAll(removableTriples);

						sVarIndividualSets.remove(sVarIndividualSet);
					}
				}

				sVariable.intersectIndividualSet(sVarIndividualSets);
				
				Set<URI> predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: oVarLiteralTriples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);
				
			} else if (sVarIndividualSets.size() > 0 && varProperties.size() == 0 && oVarIndividualSets.size() > 0 && oVarLiteralSets.size() == 0) {
				List<Triple> sVarIndividualTriples = new Vector<Triple>();

				for (IndividualSet sVarIndividualSet: sVarIndividualSets)
					sVarIndividualTriples.addAll(sVarIndividualSet.getPOTriples());
				
				Set<IndividualSet> oIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: sVarIndividualTriples) {
					IndividualSet individualSet = triple.getObjectIndividual();

					if (individualSet == null) continue;

					if (oIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: oIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						oIndividualSets.add(individualSet);
				}

				oVariable.intersectIndividualSet(oIndividualSets);
				
				oVarIndividualSets = oVariable.getIndividualSets();

				for (IndividualSet oVarIndividualSet: oVarIndividualSets) {
					if (oVarIndividualSet.getIndividualCount() < 1) {
						URI removalClsURI = oVarIndividualSet.getClassURI();

						List<Triple> removableTriples = new Vector<Triple>();
						for (Triple triple: sVarIndividualTriples) {
							IndividualSet individualSet = triple.getObjectIndividual();
							
							if (individualSet == null || removalClsURI.equals(individualSet.getClassURI()))
								removableTriples.add(triple);

						}
						sVarIndividualTriples.removeAll(removableTriples);

						oVarIndividualSets.remove(oVarIndividualSet);
					}
				}

				oVariable.intersectIndividualSet(oVarIndividualSets);
				
				oVarIndividualSets = oVariable.getIndividualSets();
				
				List<Triple> oVarIndividualTriples = new Vector<Triple>();

				for (IndividualSet oVarIndividualSet: oVarIndividualSets)
					oVarIndividualTriples.addAll(oVarIndividualSet.getSPTriples());
				
				Set<IndividualSet> sIndividualSets = new ConcurrentSkipListSet<IndividualSet>();

				for (Triple triple: oVarIndividualTriples) {
					IndividualSet individualSet = triple.getSubjectIndividual();

					if (sIndividualSets.contains(individualSet)) {
						for (IndividualSet containedIndividualSet: sIndividualSets) {
							if (containedIndividualSet.compareTo(individualSet) == 0) {
								Set<String> containedWhereSet = containedIndividualSet.getValueWhereSet();
								Set<String> whereSet = individualSet.getValueWhereSet();

								if (!containedWhereSet.containsAll(whereSet)) {
									containedIndividualSet.connectValueWhereWithOR(whereSet);
								} else if (containedWhereSet.size() < 1 || whereSet.size() < 1)
									containedIndividualSet.connectValueWhereWithOR(whereSet);
							}
						}
					} else 
						sIndividualSets.add(individualSet);
				}

				sVariable.intersectIndividualSet(sIndividualSets);
				
				sVarIndividualSets = sVariable.getIndividualSets();

				for (IndividualSet sVarIndividualSet: sVarIndividualSets) {
					if (sVarIndividualSet.getIndividualCount() < 1) {
						URI removalClsURI = sVarIndividualSet.getClassURI();

						List<Triple> removableTriples = new Vector<Triple>();
						for (Triple triple: oVarIndividualTriples) {
							IndividualSet individualSet = triple.getSubjectIndividual();

							if (removalClsURI.equals(individualSet.getClassURI()))
								removableTriples.add(triple);

						}
						oVarIndividualTriples.removeAll(removableTriples);

						sVarIndividualSets.remove(sVarIndividualSet);
					}
				}

				sVariable.intersectIndividualSet(sVarIndividualSets);
				
				Set<URI> predicates = new ConcurrentSkipListSet<URI>();
				
				for (Triple triple: sVarIndividualTriples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);
				
				predicates = new ConcurrentSkipListSet<URI>();

				for (Triple triple: oVarIndividualTriples) {
					URI predicate = triple.getPredicate();

					predicates.add(predicate);
				}

				pVariable.intersectURIs(predicates);
				
				
			} else if (sVarIndividualSets.size() > 0 && varProperties.size() == 0 && oVarIndividualSets.size() > 0 && oVarLiteralSets.size() > 0) {
				System.out.println("요기3");
			} else if (sVarIndividualSets.size() > 0 && varProperties.size() > 0 && oVarIndividualSets.size() == 0 && oVarLiteralSets.size() == 0) {
				System.out.println("요기4");
			} else if (sVarIndividualSets.size() > 0 && varProperties.size() > 0 && oVarIndividualSets.size() == 0 && oVarLiteralSets.size() > 0) {
				System.out.println("요기5");
			} else if (sVarIndividualSets.size() > 0 && varProperties.size() > 0 && oVarIndividualSets.size() > 0 && oVarLiteralSets.size() == 0) {
				System.out.println("요기6");
			} else {
				System.out.println("요기7");
			}
		}
	}
}
