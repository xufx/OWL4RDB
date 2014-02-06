package janus.sparqldl;

import janus.Janus;
import janus.mapping.OntMapper;
import janus.mapping.metadata.ClassTypes;
import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.QueryAtomGroup;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import de.derivo.sparqldlapi.types.QueryArgumentType;
import de.derivo.sparqldlapi.types.QueryAtomType;

public class SPARQLDLQueryEngine {
	private String originalQueryString;
	private Query originalQuery;
	private List<QueryAtom> group_a_0, group_a_1, group_a_2, group_a_3, group_t_0, group_t_1, group_t_2, group_r_0, group_r_1, group_r_2;
	
	private List<QueryAtom> groupGT1;
	
	private Map<String, String> prefixMap;
	private Map<String, Variable> varsMap;
	
	public SPARQLDLQueryEngine(String query) {
		this.originalQueryString = query;
		createQueryObject(query);
		
		group_a_0 = new Vector<QueryAtom>();
		group_a_1 = new Vector<QueryAtom>();
		group_a_2 = new Vector<QueryAtom>();
		group_a_3 = new Vector<QueryAtom>();
		group_t_0 = new Vector<QueryAtom>();
		group_t_1 = new Vector<QueryAtom>();
		group_t_2 = new Vector<QueryAtom>();
		group_r_0 = new Vector<QueryAtom>();
		group_r_1 = new Vector<QueryAtom>();
		group_r_2 = new Vector<QueryAtom>();
		
		groupGT1 = new Vector<QueryAtom>();
		
		prefixMap = new Hashtable<String, String>();
		varsMap = new Hashtable<String, Variable>();
	}
	
	public void executeQuery() {
		//when using, derivo
		Janus.ontBridge.executeQuery(originalQueryString);
		
		
		if (existsBlankNode() || existsAnnotationAtom()) {
			System.out.println("The result set is empty.");
			return;
		}
		
		//if (!existsABoxAtom()) {
			//Janus.ontBridge.executeQuery(originalQueryString);
			//return;
		//}
		
		preprocess();

		//long start = System.currentTimeMillis();

		execute();
		
		//long end = System.currentTimeMillis();
		//System.out.println( "질의 엔진 처리 시간 : " + ( end - start));
		
		show();
		
		//if (group_t.size() > 0 || group_r.size() > 0)
			//executeTBoxAndRBoxQuery(buildTBoxAndRBoxQuery());
		
		
		
		//Janus.ontBridge.executeQuery(schemaQuery);
		
		//Janus.ontBridge.executeQuery(originalQueryString);
	}
	
	/*private void show() {
		Set<String> vars = varsMap.keySet();
		for (String var: vars) {
			Variable variable = varsMap.get(var);
			
			if (variable.getVarType().equals(VariableTypes.INDIVIDUALS) || variable.getVarType().equals(VariableTypes.LITERALS) || variable.getVarType().equals(VariableTypes.INDIVIDUALS_OR_LITERALS))
				variable.printResultSet();
			else {
				Set<URI> resultSet = variable.getURISet();
				//System.out.println("The size of " + var + " is " + resultSet.size() + ".");
				for (URI element: resultSet)
					System.out.println(var + ": " + element);
			}
		}
	}*/
	
	private void show() {
		PrintWriter writer = getWriter();
		
		Set<String> vars = varsMap.keySet();
		for (String var: vars) {
			Variable variable = varsMap.get(var);
			
			if (variable.getVarType().equals(VariableTypes.INDIVIDUALS) || variable.getVarType().equals(VariableTypes.LITERALS) || variable.getVarType().equals(VariableTypes.INDIVIDUALS_OR_LITERALS))
				variable.printResultSet(writer);
			else {
				Set<URI> resultSet = variable.getURISet();
				//System.out.println("The size of " + var + " is " + resultSet.size() + ".");
				for (URI element: resultSet)
					writer.println(var + ": " + element);
			}
		}
		
		writer.close();
	}
	
	private PrintWriter getWriter() {
		File output = new File("./results/" + System.currentTimeMillis() + ".json");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return writer;
	}
	
	private boolean execute() {
		boolean result = executeGroupR0();
		
		if (result)
			result = executeGroupT0();
		else
			return false;
		
		if (result)
			result = executeGroupA0();
		else
			return false;
		
		if (result)
			result = executeGroupR1();
		else
			return false;
		
		if (result)
			result = executeGroupT1();
		else
			return false;
		
		if (result)
			result = executeGroupA1();
		else
			return false;
		
		ABox2Processor abox2Processor = new ABox2Processor();
		ABox3Processor abox3Processor = new ABox3Processor();
		RBox2Processor rbox2Processor = new RBox2Processor();
		TBox2Processor tbox2Processor = new TBox2Processor();
		
		AtomGraph graph = new AtomGraph(groupGT1);
		
		for (QueryAtom atom: groupGT1) {
			List<QueryAtom> bfsPath = graph.getBFSPath(atom);
			
			for (QueryAtom vAtom: bfsPath) {
				if (isTBoxAtom(vAtom))
					tbox2Processor.execute(vAtom, varsMap);
				else if (isRBoxAtom(vAtom))
					rbox2Processor.execute(vAtom, varsMap);
				else {
					List<QueryArgument> args = vAtom.getArguments();
					int groupIndex = args.size();
					for (QueryArgument arg: args)
						if (!arg.isVar()) groupIndex--;
					
					if (groupIndex < 3)
						abox2Processor.execute(vAtom, varsMap);
					else
						abox3Processor.execute(vAtom, varsMap);
				}
			}
		}
		
		/*
		if (result)
			result = executeGroupR2();
		else
			return false;
		
		if (result)
			result = executeGroupT2();
		else
			return false;
		
		if (result)
			result = executeGroupA2();
		else
			return false;
		
		if (result)
			result = executeGroupA3();
		else
			return false;
		*/
		
		return true;
	}
	
	private boolean executeGroupA1() {
		for (QueryAtom atom: group_a_1) {
			if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				
				String varName = null;
				URI iURI = null;
				
				if (arg1.getType().equals(QueryArgumentType.VAR)) {
					varName = arg1.toString();
					iURI = URI.create(arg2.getValue());
				}
				else {
					varName = arg2.toString();
					iURI = URI.create(arg1.getValue());
				}
				
				Variable variable = varsMap.get(varName);
				
				if (variable.hasIndividualsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				Individual individual = new Individual(iURI);
				
				if (individual.isExistentIndividual()) {
					Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();
					
					URI classOfIndividualArgument = individual.getClassURI();
					
					Set<URI> disjointClasses = Janus.ontBridge.getAllDisjointClasses(classOfIndividualArgument);
					
					for (URI disjointClass: disjointClasses) {
						if (Janus.ontBridge.containsClass(disjointClass)) {
							IndividualSet individualSet = new IndividualSet(disjointClass);
							individualSets.add(individualSet);
						}
					}
					
					Set<URI> familyClasses = individual.getFamilyClasses();
					
					for (URI familyClass: familyClasses) {
						IndividualSet individualSet = new IndividualSet(familyClass);
						individualSet.acceptDifferentFromCondition(individual);
						individualSets.add(individualSet);
					}
					
					variable.intersectIndividualSet(individualSets);
				} else {
					System.out.println(iURI + " in the atom " + atom + " is not asserted.");
					return false;
				}
			}
			
			if (atom.getType().equals(QueryAtomType.SAME_AS)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				
				String varName = null;
				URI iURI = null;
				
				if (arg1.getType().equals(QueryArgumentType.VAR)) {
					varName = arg1.toString();
					iURI = URI.create(arg2.getValue());
				}
				else {
					varName = arg2.toString();
					iURI = URI.create(arg1.getValue());
				}
				
				Variable variable = varsMap.get(varName);
				
				if (variable.hasIndividualsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				Individual individual = new Individual(iURI);
				
				if (individual.isExistentIndividual()) {
					Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();
					
					Set<URI> familyClasses = individual.getFamilyClasses();
					
					for (URI familyClass: familyClasses) {
						IndividualSet individualSet = new IndividualSet(familyClass);
						individualSet.acceptSameAsCondition(individual);
						individualSets.add(individualSet);
					}
					
					variable.intersectIndividualSet(individualSets);
				} else {
					System.out.println(iURI + " in the atom " + atom + " is not asserted.");
					return false;
				}
			}
			
			if (atom.getType().equals(QueryAtomType.TYPE)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				
				String varName = null;
				URI cURI = null;
				URI iURI = null;
				
				if (arg1.getType().equals(QueryArgumentType.VAR)) {
					varName = arg1.toString();
					cURI = URI.create(arg2.getValue());
				}
				else {
					varName = arg2.toString();
					iURI = URI.create(arg1.getValue());
				}
				
				Variable variable = varsMap.get(varName);
				
				if (iURI == null) {
					if (variable.hasIndividualsFinished()) {
						System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
						continue;
					}
					
					Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();
					
					if (Janus.ontBridge.containsClass(cURI)) {
						Set<URI> familyClasses = Janus.ontBridge.getAllFamilyClasses(cURI);
						
						for (URI familyClass: familyClasses) {
							IndividualSet individualSet = new IndividualSet(familyClass);
							
							if (!familyClass.equals(cURI))
								individualSet.intersectWith(cURI);
							
							individualSets.add(individualSet);
						}
						
						variable.intersectIndividualSet(individualSets);
					} else {
						System.out.println(cURI + " in the atom " + atom + " is not asserted.");
						return false;
					}
				} else {
					if (variable.hasURIsFinished()) {
						System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
						continue;
					}
					
					Individual individual = new Individual(iURI);
					
					if (individual.isExistentIndividual()) {
						Set<URI> classes = new ConcurrentSkipListSet<URI>();
						
						URI definedClassURI = individual.getClassURI();
						classes.add(definedClassURI);
						
						Set<URI> superClasses = Janus.ontBridge.getAllSuperClasses(definedClassURI);
						classes.addAll(superClasses);
						
						Set<URI> familyClasses = individual.getFamilyClasses();
						familyClasses.removeAll(classes);
						
						for(URI familyClass: familyClasses) {
							URI typeChangedIndividualURI = individual.getTypeChangedIndividual(familyClass);
							Individual typeChangedIndividual = new Individual(typeChangedIndividualURI);
							
							if (typeChangedIndividual.isExistentIndividual())
								classes.add(familyClass);
						}
						
						variable.intersectURIs(classes);
					} else {
						System.out.println(iURI + " in the atom " + atom + " is not asserted.");
						return false;
					}
				}
			}
			
			if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				QueryArgument arg3 = args.get(2);
				
				String varName = null;
				Variable variable = null;
				
				URI sURI = null;
				URI pURI = null;
				URI oURI = null;
				String oLit = null;
				
				if (arg1.isVar()) {
					varName = arg1.toString();
					
					variable = varsMap.get(varName);
					
					if (variable.hasIndividualsFinished()) {
						System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
						continue;
					}
					
					pURI = URI.create(arg2.getValue());
					
					if (Janus.ontBridge.containsObjectProperty(pURI)) {
						Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();
						
						oURI = URI.create(arg3.getValue());
						
						Set<URI> domains = Janus.ontBridge.getObjPropNamedDomains(pURI);
						
						URI domainClass = null;
						for (URI domain: domains)
							domainClass = domain;
						
						Set<URI> familyClasses = Janus.ontBridge.getAllFamilyClasses(domainClass);
						
						for (URI familyClass: familyClasses) {
							IndividualSet individualSet = new IndividualSet(familyClass);
							individualSet.acceptPropertyValueVOICondition(pURI, oURI);
							individualSets.add(individualSet);
						}
						
						variable.intersectIndividualSet(individualSets);
						
					} else if (Janus.ontBridge.containsDataProperty(pURI)) {
						Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();
						
						oLit = arg3.getValue();
						
						Set<URI> domains = Janus.ontBridge.getNamedDataPropDomains(pURI);
						
						URI domainClass = null;
						for (URI domain: domains)
							domainClass = domain;
						
						Set<URI> familyClasses = Janus.ontBridge.getAllFamilyClasses(domainClass);
						
						for (URI familyClass: familyClasses) {
							IndividualSet individualSet = new IndividualSet(familyClass);
							individualSet.acceptPropertyValueVDLCondition(pURI, oLit);
							individualSets.add(individualSet);
						}
						
						variable.intersectIndividualSet(individualSets);
						
					} else {
						System.out.println(pURI + " in the atom " + atom + " is not asserted.");
						return false;
					}
				} else if (arg2.isVar()) {
					varName = arg2.toString();
					
					variable = varsMap.get(varName);
					
					if (variable.hasURIsFinished()) {
						System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
						continue;
					}
					
					sURI = URI.create(arg1.getValue());
					
					Individual sIndividual = new Individual(sURI);
					
					if (!sIndividual.isExistentIndividual()) {
						System.out.println(sURI + " in the atom " + atom + " is not asserted.");
						return false;
					}
					
					if (arg3.isLiteral()) {
						variable.setVarType(VariableTypes.DATA_PROPERTIES);
						
						oLit = arg3.getValue();
						
						if (sIndividual.getType().equals(IndividualTypes.FROM_CELL)) {
							
							if (!sIndividual.getHasKeyColumnValueAt(0).equals(oLit)) {
								System.out.println("The subject " + sURI + " in the " + atom.toString() + " can't holds the value " + oLit + ".");
								return false;
							}
							
							Set<URI> dataProperties = new ConcurrentSkipListSet<URI>();
							
							Set<URI> familyClasses = sIndividual.getFamilyClasses();
							
							for (URI familyClass: familyClasses) {
								Set<URI> keyDataProperties = Janus.ontBridge.getHasKeyDataProperties(familyClass);
								
								dataProperties.addAll(keyDataProperties);
							}
							
							variable.intersectURIs(dataProperties);
							
						} else {
							Set<URI> dataProperties = new ConcurrentSkipListSet<URI>();
							
							Set<URI> familyClasses = sIndividual.getFamilyClasses();
							
							for (URI familyClass: familyClasses) {
								URI sameAsIndividualURI = sIndividual.getTypeChangedIndividual(familyClass);
								
								Individual sameAsIndividual = new Individual(sameAsIndividualURI);
								
								Set<URI> foundDataProperties = sameAsIndividual.findDataPropertyThatHoldsTheValue(oLit);
								
								dataProperties.addAll(foundDataProperties);
							}
							
							variable.intersectURIs(dataProperties);
						}
						
					} else {
						variable.setVarType(VariableTypes.OBJECT_PROPERTIES);
						
						oURI = URI.create(arg3.getValue());
						
						Individual oIndividual = new Individual(oURI);
						
						if (!oIndividual.isExistentIndividual()) {
							System.out.println(oURI + " in the atom " + atom + " is not asserted.");
							return false;
						}
						
						Set<URI> objectProperties = new ConcurrentSkipListSet<URI>();
						
						String sMappedTable = sIndividual.getMappedTableName();
						
						String oMappedTable = oIndividual.getMappedTableName();
						String oMappedColumn = oIndividual.getHasKeyColumnNameAt(0);
						String oMappedValue = oIndividual.getHasKeyColumnValueAt(0);
						
						if (sMappedTable.equals(oMappedTable)) {
							String value = sIndividual.getValueOfTheColumn(oMappedColumn);
							
							if (value != null && value.equals(oMappedValue)) {
								URI objectProperty = Janus.mappingMetadata.getMappedObjectProperty(oMappedTable, oMappedColumn);
								
								objectProperties.add(objectProperty);
							} else
								System.out.println("There aren't any relationships between " + sURI + " and " + oURI + " in the atom " + atom + ".");

						} else {
							URI sClassURI = sIndividual.getClassURI();
							URI oTableClassURI = Janus.mappingMetadata.getMappedClass(oMappedTable);
							
							if (!Janus.ontBridge.areDisjointWith(sClassURI, oTableClassURI)) {
								
								URI oTableIndividualURI = sIndividual.getTypeChangedIndividual(oTableClassURI);
								Individual oTableIndividual = new Individual(oTableIndividualURI);
								
								if (oTableIndividual.isExistentIndividual()) {

									if (Janus.cachedDBMetadata.isPrimaryKey(oMappedTable, oMappedColumn)) {	
										
										boolean existsMatchedPK = false;
										
										for (int i = 0; i < oTableIndividual.getNumberOfHasKeyValue(); i++) {
											if (oTableIndividual.getHasKeyColumnNameAt(i).equals(oMappedColumn) && oTableIndividual.getHasKeyColumnValueAt(i).equals(oMappedValue)) {
												URI oObjectProperty = Janus.mappingMetadata.getMappedObjectProperty(oMappedTable, oMappedColumn);

												objectProperties.add(oObjectProperty);

												String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(oMappedTable, oMappedColumn, sMappedTable);

												URI sObjectProperty = Janus.mappingMetadata.getMappedObjectProperty(sMappedTable, matchedColumn);

												objectProperties.add(sObjectProperty);

												break;
											}
										}
										
										if (!existsMatchedPK)
											System.out.println("There aren't any relationships between " + sURI + " and " + oURI + " in the atom " + atom + ".");
											
										
									} else {
										String value = oTableIndividual.getValueOfTheColumn(oMappedColumn);

										if (value != null && value.equals(oIndividual.getHasKeyColumnValueAt(0))) {
											URI oObjectProperty = Janus.mappingMetadata.getMappedObjectProperty(oMappedTable, oMappedColumn);

											objectProperties.add(oObjectProperty);
										} else
											System.out.println("There aren't any relationships between " + sURI + " and " + oURI + " in the atom " + atom + ".");
									}
								} else
									System.out.println("There aren't any relationships between " + sURI + " and " + oURI + " in the atom " + atom + ".");

							} else {
								Set<String> keyColumns = Janus.cachedDBMetadata.getKeyColumns(sMappedTable);
								
								boolean existsMatchedKeyColumns = false;
								for (String key: keyColumns) {
									URI clsURIMappedToFK = Janus.mappingMetadata.getMappedClass(sMappedTable, key);
									
									if (!Janus.ontBridge.areDisjointWith(clsURIMappedToFK, oIndividual.getClassURI())) {
										String fkValue = sIndividual.getValueOfTheColumn(key);
										
										if (fkValue != null && fkValue.equals(oMappedValue)) {
											URI objectProperty = Janus.mappingMetadata.getMappedObjectProperty(sMappedTable, key);
											
											objectProperties.add(objectProperty);
											
											existsMatchedKeyColumns = true;
										}
									}
								}
								
								if (!existsMatchedKeyColumns)
									System.out.println("There aren't any relationships between " + sURI + " and " + oURI + " in the atom " + atom + ".");
							}
						}
						
						variable.intersectURIs(objectProperties);
					}
				} else {
					varName = arg3.toString();
					
					variable = varsMap.get(varName);
					
					sURI = URI.create(arg1.getValue());
					
					Individual sIndividual = new Individual(sURI);
					
					URI sClassURI = sIndividual.getClassURI();
					
					if (!sIndividual.isExistentIndividual()) {
						System.out.println(sURI + " in the atom " + atom + " is not asserted.");
						variable.makeFinished();
						continue;
					}
					
					pURI = URI.create(arg2.getValue());
					
					if (Janus.ontBridge.containsObjectProperty(pURI)) {
						
						if (variable.hasIndividualsFinished()) {
							System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
							continue;
						}
						
						variable.setVarType(VariableTypes.INDIVIDUALS);
						
						Set<IndividualSet> individualSets = new ConcurrentSkipListSet<IndividualSet>();
						
						if (!sIndividual.getType().equals(IndividualTypes.FROM_ROW)) {
							System.out.println(sURI + " in the atom " + atom + " must be the individual derived from the row.");
							variable.makeFinished();
							continue;
						}
						
						Set<URI> domains = Janus.ontBridge.getObjPropNamedDomains(pURI);
						
						URI domainClass = null;
						for (URI domain: domains)
							domainClass = domain;
						
						if (sClassURI.equals(domainClass)) {
							String column = Janus.mappingMetadata.getMappedColumnNameOfTheProperty(pURI);
							
							URI oIndividualURI = sIndividual.getObjectIndividual(column);
							
							if (oIndividualURI != null) {
								Individual oIndividual = new Individual(oIndividualURI);
								
								Set<URI> familyClasses = oIndividual.getFamilyClasses();
								
								for (URI familyClass: familyClasses) {
									IndividualSet individualSet = new IndividualSet(familyClass);
									individualSet.acceptSameAsCondition(oIndividual);
									individualSets.add(individualSet);
								}
								
								variable.intersectIndividualSet(individualSets);
							} else 
								System.out.println("There aren't any object individuals for the subject individual " + sURI + " and the object property " + pURI + " in the atom " + atom + ".");
							
						} else if (!Janus.ontBridge.areDisjointWith(sClassURI, domainClass)) {
							URI domainIndividualURI = sIndividual.getTypeChangedIndividual(domainClass);
							Individual domainIndividual = new Individual(domainIndividualURI);
							
							if (domainIndividual.isExistentIndividual()) {
								String column = Janus.mappingMetadata.getMappedColumnNameOfTheProperty(pURI);
								
								URI oIndividualURI = domainIndividual.getObjectIndividual(column);
								
								if (oIndividualURI != null) {
									Individual oIndividual = new Individual(oIndividualURI);
									
									Set<URI> familyClasses = oIndividual.getFamilyClasses();
									
									for (URI familyClass: familyClasses) {
										IndividualSet individualSet = new IndividualSet(familyClass);
										individualSet.acceptSameAsCondition(oIndividual);
										individualSets.add(individualSet);
									}
									
									variable.intersectIndividualSet(individualSets);
								} else 
									System.out.println("There aren't any object individuals for the subject individual " + sURI + " and the object property " + pURI + " in the atom " + atom + ".");
							} else {
								System.out.println("The subject individual " + sURI + " can't the object property " + pURI + " in the atom " + atom + " because there isn't the same individual of the type " + domainClass + ".");
								variable.makeFinished();
								continue;
							}
						} else {
							System.out.println("The subject individual " + sURI + " can't have the object property " + pURI + " in the atom " + atom + ".");
							variable.makeFinished();
							continue;
						}
						
					} else if (Janus.ontBridge.containsDataProperty(pURI)) {
						
						if (variable.hasLiteralsFinished()) {
							System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
							continue;
						}
						
						variable.setVarType(VariableTypes.LITERALS);
						
						Set<LiteralSet> literalSets = new ConcurrentSkipListSet<LiteralSet>();
						
						String pMappedTable = Janus.mappingMetadata.getMappedTableNameOfTheProperty(pURI);
						String pMappedColumn = Janus.mappingMetadata.getMappedColumnNameOfTheProperty(pURI);
						URI domainClassURI = Janus.mappingMetadata.getMappedClass(pMappedTable, pMappedColumn);
						
						if (sIndividual.getType().equals(IndividualTypes.FROM_CELL)) {
							
							if (sIndividual.getMappedTableName().equals(pMappedTable) && sIndividual.getHasKeyColumnNameAt(0).equals(pMappedColumn)) {
								Set<URI> familyClasses = sIndividual.getFamilyClasses();
								
								for (URI familyClass: familyClasses) {
									URI familyIndividualURI = sIndividual.getTypeChangedIndividual(familyClass);
									Individual familyIndividual = new Individual(familyIndividualURI);
									
									if (familyIndividual.isExistentIndividual()) {
										String mappedTable = familyIndividual.getMappedTableName();
										String mappedColumn = familyIndividual.getHasKeyColumnNameAt(0);
										URI mappedDataProperty = Janus.mappingMetadata.getMappedDataProperty(mappedTable, mappedColumn);
										
										LiteralSet literalSet = new LiteralSet(familyClass, mappedDataProperty);
										literalSet.acceptSameAsCondition(familyIndividual);
										
										literalSets.add(literalSet);
									}
								}
								
								variable.intersectLiteralSet(literalSets);
								
							} else if (!Janus.ontBridge.areDisjointWith(sClassURI, domainClassURI)) {
								URI domainIndividualURI = sIndividual.getTypeChangedIndividual(domainClassURI);
								Individual domainIndividual = new Individual(domainIndividualURI);
								
								if (domainIndividual.isExistentIndividual()) {
									Set<URI> familyClasses = sIndividual.getFamilyClasses();
									
									for (URI familyClass: familyClasses) {
										URI familyIndividualURI = sIndividual.getTypeChangedIndividual(familyClass);
										Individual familyIndividual = new Individual(familyIndividualURI);
										
										if (familyIndividual.isExistentIndividual()) {
											String mappedTable = familyIndividual.getMappedTableName();
											String mappedColumn = familyIndividual.getHasKeyColumnNameAt(0);
											URI mappedDataProperty = Janus.mappingMetadata.getMappedDataProperty(mappedTable, mappedColumn);
											
											LiteralSet literalSet = new LiteralSet(familyClass, mappedDataProperty);
											literalSet.acceptSameAsCondition(familyIndividual);
											
											literalSets.add(literalSet);
										}
									}
									
									variable.intersectLiteralSet(literalSets);
								} else {
									System.out.println("The subject individual " + sURI + " can't the data property " + pURI + " in the atom " + atom + " because there isn't the same individual of the type " + domainClassURI + ".");
									variable.makeFinished();
									continue;
								}
							} else {
								System.out.println("The subject individual " + sURI + " can't have the data property " + pURI + " in the atom " + atom + ".");
								variable.makeFinished();
								continue;
							}
						} else {
							if (sIndividual.getMappedTableName().equals(pMappedTable)) {
								LiteralSet literalSet = new LiteralSet(sClassURI, pURI);
								literalSet.acceptSameAsCondition(sIndividual);
								
								literalSets.add(literalSet);
								
							} else if (!Janus.ontBridge.areDisjointWith(sClassURI, domainClassURI)) {
								URI domainIndividualURI = sIndividual.getTypeChangedIndividual(domainClassURI);
								Individual domainIndividual = new Individual(domainIndividualURI);
								
								if (domainIndividual.isExistentIndividual()) {
									LiteralSet literalSet = new LiteralSet(domainClassURI, pURI);
									literalSet.acceptSameAsCondition(domainIndividual);
									
									literalSets.add(literalSet);
								} else {
									System.out.println("The subject individual " + sURI + " can't the data property " + pURI + " in the atom " + atom + " because there isn't the same individual of the type " + domainClassURI + ".");
									variable.makeFinished();
									continue;
								}
							} else {
								System.out.println("The subject individual " + sURI + " can't have the data property " + pURI + " in the atom " + atom + ".");
								variable.makeFinished();
								continue;
							}
							
							variable.intersectLiteralSet(literalSets);
						}
						
					} else  {
						System.out.println(pURI + " in the atom " + atom + " is not asserted.");
						variable.makeFinished();
						continue;
					}
				}
			}
		}
		
		return true;
	}
	
	private boolean executeGroupA2() {
		
		// reorder
		/*List<QueryAtom> lastAtoms = new Vector<QueryAtom>();
		for (QueryAtom atom: group_a_2) {
			if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
				List<QueryArgument> args = atom.getArguments();
				
				if (args.get(1).isVar()) {
					QueryAtom lastAtom = atom;
					lastAtoms.add(lastAtom);
				}
			}
		}
		group_a_2.removeAll(lastAtoms);
		group_a_2.addAll(lastAtoms);*/
		
		for (QueryAtom atom: group_a_2) {
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
					
					continue;
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
						
						continue;
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
						
						continue;
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
						
						continue;
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
					
					continue;
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
					
					continue;
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
							if (!var1ClassSet.contains(familyClass)) {
								IndividualSet individualSet = new IndividualSet(familyClass);
								individualSet.intersectWith(var1IndividualSet);
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
						
						continue;
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
								
								continue;
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
								
								continue;
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
						
						continue;
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
						
						continue;
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
							
							String mappedTable = Janus.mappingMetadata.getMappedTableNameOfTheClass(assertedDomainURI);
							String mappedColumn = Janus.mappingMetadata.getMappedColumnNameOfTheClass(assertedDomainURI);
							
							URI mappedDataProperty = Janus.mappingMetadata.getMappedDataProperty(mappedTable, mappedColumn);
							
							LiteralSet literalSet = new LiteralSet(assertedDomainURI, mappedDataProperty);
							literalSets.add(literalSet);
							
							for (URI familyClass: familyClasses) {
								mappedTable = Janus.mappingMetadata.getMappedTableNameOfTheClass(familyClass);
								mappedColumn = Janus.mappingMetadata.getMappedColumnNameOfTheClass(familyClass);
								
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
								
								continue;
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
							
							continue;
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
						
						continue;
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
		
		return true;
	}
	
	private boolean executeGroupA3() {
		
		for (QueryAtom atom: group_a_3) {
			
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
				
				if (sVariable.hasIndividualsFinished() || pVariable.hasURIsFinished() || (oVariable.hasLiteralsFinished() && oVariable.hasIndividualsFinished())) {
					System.out.println("Since some of the variables in the " + atom.toString() + " have already been the empty set, this atom doesn't have to be calculated.");
					
					sVariable.makeFinished();
					pVariable.makeFinished();
					oVariable.makeFinished();
					
					continue;
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
					
				} else if (sVarIndividualSets.size() > 0 && varProperties.size() == 0 && oVarIndividualSets.size() > 0 && oVarLiteralSets.size() == 0) {
					
				} else if (sVarIndividualSets.size() > 0 && varProperties.size() == 0 && oVarIndividualSets.size() > 0 && oVarLiteralSets.size() > 0) {
					
				} else if (sVarIndividualSets.size() > 0 && varProperties.size() > 0 && oVarIndividualSets.size() == 0 && oVarLiteralSets.size() == 0) {
					
				} else if (sVarIndividualSets.size() > 0 && varProperties.size() > 0 && oVarIndividualSets.size() == 0 && oVarLiteralSets.size() > 0) {
					
				} else if (sVarIndividualSets.size() > 0 && varProperties.size() > 0 && oVarIndividualSets.size() > 0 && oVarLiteralSets.size() == 0) {
					
				} else {
					
				}
			}
		}
		
		return true;
	}
	
	private boolean executeGroupA0() {
		for (QueryAtom atom: group_a_0) {
			if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				
				/*if (arg1.isBnode() || arg2.isBnode()) {
					System.out.println(atom.toString() + " is false.");
					return false;
				}*/
				
				URI iURI1 = URI.create(arg1.getValue());
				URI iURI2 = URI.create(arg2.getValue());
				
				Individual individual1 = new Individual(iURI1);
				Individual individual2 = new Individual(iURI2);
				
				if (individual1.isExistentIndividual() && individual2.isExistentIndividual()) {
					if (individual1.equals(individual2)) {
						System.out.println(atom.toString() + " is false.");
						return false;
					} else {
						System.out.println(atom.toString() + " is true.");
						continue;
					}
				} else {
					try {
						throw new Exception();
					} catch (Exception e) {
						if (!individual1.isExistentIndividual())
							System.out.println(iURI1 + " in the atom " + atom + " is not asserted.");
						if (!individual2.isExistentIndividual())
							System.out.println(iURI2 + " in the atom " + atom + " is not asserted.");
						e.printStackTrace();
					}
				}
			}
			
			if (atom.getType().equals(QueryAtomType.SAME_AS)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				
				/*if (arg1.isBnode() || arg2.isBnode()) {
					System.out.println(atom.toString() + " is false.");
					return false;
				}*/
				
				URI iURI1 = URI.create(arg1.getValue());
				URI iURI2 = URI.create(arg2.getValue());
				
				Individual individual1 = new Individual(iURI1);
				Individual individual2 = new Individual(iURI2);
				
				if (individual1.isExistentIndividual() && individual2.isExistentIndividual()) {
					if (!individual1.equals(individual2)) {
						System.out.println(atom.toString() + " is false.");
						return false;
					} else {
						System.out.println(atom.toString() + " is true.");
						continue;
					}
				} else {
					try {
						throw new Exception();
					} catch (Exception e) {
						if (!individual1.isExistentIndividual())
							System.out.println(iURI1 + " in the atom " + atom + " is not asserted.");
						if (!individual2.isExistentIndividual())
							System.out.println(iURI2 + " in the atom " + atom + " is not asserted.");
						e.printStackTrace();
					}
				}
			}
			
			if (atom.getType().equals(QueryAtomType.TYPE)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				
				/*if (arg1.isBnode()) {
					System.out.println(atom.toString() + " is false.");
					return false;
				}*/
				
				URI iURI = URI.create(arg1.getValue());
				URI cURI = URI.create(arg2.getValue());
				
				Individual individual = new Individual(iURI);
				
				if (individual.isExistentIndividual()) {
					URI assertedClass = individual.getClassURI();
					
					if (assertedClass.equals(cURI)) {
						System.out.println(atom.toString() + " is true.");
						continue;
					}
					
					if (Janus.ontBridge.areDisjointWith(assertedClass, cURI))  {
						System.out.println(atom.toString() + " is false.");
						return false;
					}
					else {
						if (Janus.ontBridge.isSubClassOf(assertedClass, cURI))  {
							System.out.println(atom.toString() + " is true.");
							continue;
						}
						if (Janus.ontBridge.isSubClassOf(cURI, assertedClass)) {
							URI virtualIndividualURI = individual.getTypeChangedIndividual(cURI);
							Individual virtualIndividual = new Individual(virtualIndividualURI);
							if (virtualIndividual.isExistentIndividual())  {
								System.out.println(atom.toString() + " is true.");
								continue;
							}
							else  {
								System.out.println(atom.toString() + " is false.");
								return false;
							}
						}
							
						
					}
				}
			}
			
			if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				QueryArgument arg3 = args.get(2);
				
				/*if (arg1.isBnode()) {
					System.out.println(atom.toString() + " is false.");
					return false;
				}*/
				
				URI sURI = URI.create(arg1.getValue());
				URI pURI = URI.create(arg2.getValue());
				
				Individual subject = new Individual(sURI);
				
				if (!subject.isExistentIndividual()) {
					System.out.println(atom.toString() + " is false.");
					return false;
				}
				
				if (pURI.getFragment().startsWith(OntMapper.DP_PREFIX)) {
					String object = arg3.getValue();
					
					if (sURI.getFragment().startsWith(OntMapper.CELL_INDIVIDUAL_PREFIX)) {
						if (!subject.getHasKeyColumnValueAt(0).equals(object)) {
							System.out.println(atom.toString() + " is false.");
							return false;
						}
						
						URI subjectClass = subject.getClassURI();
						
						URI assertedDomain = null;
						Set<URI> domains = Janus.ontBridge.getNamedDataPropDomains(pURI);
						for (URI domain: domains) {
							assertedDomain = domain;
							break;
						}
						
						if (assertedDomain == null) {
							System.out.println(atom.toString() + " is false.");
							return false;
						}
						
						if (Janus.ontBridge.areDisjointWith(subjectClass, assertedDomain)) {
							System.out.println(atom.toString() + " is false.");
							return false;
						}
						
						boolean isSubjectSubclassOfDomain = Janus.ontBridge.isSubClassOf(subjectClass, assertedDomain);
						
						if (isSubjectSubclassOfDomain) {
							System.out.println(atom.toString() + " is true.");
							continue;
						}
						else {
							URI virtualSubjectURI = subject.getTypeChangedIndividual(assertedDomain);
							Individual virtualSubject = new Individual(virtualSubjectURI);
							
							if (virtualSubject.isExistentIndividual())  {
								System.out.println(atom.toString() + " is true.");
								continue;
							}
							else  {
								System.out.println(atom.toString() + " is false.");
								return false;
							}
						}
					} else {
						
					}
				} else {
					URI oURI = URI.create(arg3.getValue());
					Individual object = new Individual(oURI);
					
					if (!object.isExistentIndividual()) {
						System.out.println(atom.toString() + " is false.");
						return false;
					}
					
					if (oURI.getFragment().startsWith(OntMapper.ROW_INDIVIDUAL_PREFIX) || sURI.getFragment().startsWith(OntMapper.CELL_INDIVIDUAL_PREFIX)) {
						System.out.println(atom.toString() + " is false.");
						return false;
					}
					
					URI subjectClass = subject.getClassURI();
					
					URI assertedDomain = null;
					Set<URI> domains = Janus.ontBridge.getObjPropNamedDomains(pURI);
					for (URI domain: domains) {
						assertedDomain = domain;
						break;
					}
					
					URI assertedRange = null;
					Set<URI> ranges = Janus.ontBridge.getObjPropNamedRanges(pURI);
					for (URI range: ranges) {
						assertedRange = range;
						break;
					}
					
					URI objectClass = object.getClassURI();
					
					if (assertedDomain == null || assertedRange == null) {
						System.out.println(atom.toString() + " is false.");
						return false;
					}
					
					if (Janus.ontBridge.areDisjointWith(subjectClass, assertedDomain) || Janus.ontBridge.areDisjointWith(objectClass, assertedRange)) {
						System.out.println(atom.toString() + " is false.");
						return false;
					}
					
					boolean isSubjectSubclassOfDomain = Janus.ontBridge.isSubClassOf(subjectClass, assertedDomain);
					boolean isObjectSubclassOfRange = Janus.ontBridge.isSubClassOf(objectClass, assertedRange);
					
					if (isSubjectSubclassOfDomain && isObjectSubclassOfRange) {
						System.out.println(atom.toString() + " is true.");
						continue;
					}
					else {
						URI virtualSubjectURI = subject.getTypeChangedIndividual(assertedDomain);
						Individual virtualSubject = new Individual(virtualSubjectURI);
						
						URI virtualObjectURI = object.getTypeChangedIndividual(assertedRange);
						Individual virtualObject = new Individual(virtualObjectURI);
						
						if (virtualSubject.isExistentIndividual() && virtualObject.isExistentIndividual()) {
							System.out.println(atom.toString() + " is true.");
							continue;
						}
						else  {
							System.out.println(atom.toString() + " is false.");
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}
	
	private boolean executeGroupT0() {
		for (QueryAtom atom: group_t_0) {
			if (atom.getType().equals(QueryAtomType.COMPLEMENT_OF)) {
				List<QueryArgument> args = atom.getArguments();
				
				URI cURI1 = URI.create(args.get(0).getValue());
				URI cURI2 = URI.create(args.get(1).getValue());
				
				URI owlThing = Janus.ontBridge.getOWLThingURI();
				URI owlNothing = Janus.ontBridge.getOWLNothingURI();
				
				if ((cURI1.equals(owlThing) && cURI2.equals(owlNothing)) || (cURI1.equals(owlNothing) && cURI2.equals(owlThing))) {
					System.out.println(atom.toString() + " is true.");
					continue;
				} else {
					System.out.println(atom.toString() + " is false.");
					return false;
				}
			}
			
			if (atom.getType().equals(QueryAtomType.DISJOINT_WITH)) {
				List<QueryArgument> args = atom.getArguments();
				
				URI cURI1 = URI.create(args.get(0).getValue());
				URI cURI2 = URI.create(args.get(1).getValue());
				
				boolean result = Janus.ontBridge.areDisjointWith(cURI1, cURI2);
				
				if (result) {
					System.out.println(atom.toString() + " is true.");
					continue;
				} else {
					System.out.println(atom.toString() + " is false.");
					return false;
				}
			}
			
			if (atom.getType().equals(QueryAtomType.EQUIVALENT_CLASS)) {
				List<QueryArgument> args = atom.getArguments();
				
				URI cURI1 = URI.create(args.get(0).getValue());
				URI cURI2 = URI.create(args.get(1).getValue());
				
				boolean result = Janus.ontBridge.isEquivalentClass(cURI1, cURI2);
				
				if (result) {
					System.out.println(atom.toString() + " is true.");
					continue;
				} else {
					System.out.println(atom.toString() + " is false.");
					return false;
				}
			}
			
			if (atom.getType().equals(QueryAtomType.SUB_CLASS_OF)) {
				List<QueryArgument> args = atom.getArguments();
				
				URI cURI1 = URI.create(args.get(0).getValue());
				URI cURI2 = URI.create(args.get(1).getValue());
				
				if (cURI1.equals(cURI2)) {
					System.out.println(atom.toString() + " is true.");
					continue;
				}
				
				boolean result = Janus.ontBridge.isSubClassOf(cURI1, cURI2);
				
				if (result) {
					System.out.println(atom.toString() + " is true.");
					continue;
				} else {
					System.out.println(atom.toString() + " is false.");
					return false;
				}
			}
		}
		
		return true;
	}
	
	private boolean executeGroupR0() {
		for (QueryAtom atom: group_r_0) {
			if (atom.getType().equals(QueryAtomType.TRANSITIVE)) {
				List<QueryArgument> args = atom.getArguments();
				for (QueryArgument arg: args) {
					URI opURI = URI.create(arg.getValue());
					boolean result = Janus.ontBridge.isTransitive(opURI);
					if (!result) {
						System.out.println(atom.toString() + " is false.");
						return false;
					} else {
						System.out.println(atom.toString() + " is true.");
						break;
					}
				}
				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.SYMMETRIC)) {
				List<QueryArgument> args = atom.getArguments();
				for (QueryArgument arg: args) {
					URI opURI = URI.create(arg.getValue());
					boolean result = Janus.ontBridge.isSymmetric(opURI);
					if (!result) {
						System.out.println(atom.toString() + " is false.");
						return false;
					} else {
						System.out.println(atom.toString() + " is true.");
						break;
					}
				}
				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.INVERSE_FUNCTIONAL)) {
				List<QueryArgument> args = atom.getArguments();
				for (QueryArgument arg: args) {
					URI opURI = URI.create(arg.getValue());
					boolean result = Janus.ontBridge.isInverseFunctional(opURI);
					if (!result) {
						System.out.println(atom.toString() + " is false.");
						return false;
					} else {
						System.out.println(atom.toString() + " is true.");
						break;
					}
				}
				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.FUNCTIONAL)) {
				List<QueryArgument> args = atom.getArguments();
				for (QueryArgument arg: args) {
					URI pURI = URI.create(arg.getValue());
					
					boolean result = false;
					if (Janus.ontBridge.containsDataProperty(pURI))
						result = Janus.ontBridge.isFunctionalDataProp(pURI);
					else
						result = Janus.ontBridge.isFunctionalObjProp(pURI);
					
					if (!result) {
						System.out.println(atom.toString() + " is false.");
						return false;
					} else {
						System.out.println(atom.toString() + " is true.");
						break;
					}
				}
				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.DATA_PROPERTY)) {
				List<QueryArgument> args = atom.getArguments();
				for (QueryArgument arg: args) {
					URI dpURI = URI.create(arg.getValue());
					boolean result = Janus.ontBridge.containsDataProperty(dpURI);
					if (!result) {
						System.out.println(atom.toString() + " is false.");
						return false;
					} else {
						System.out.println(atom.toString() + " is true.");
						break;
					}
				}
				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.OBJECT_PROPERTY)) {
				List<QueryArgument> args = atom.getArguments();
				for (QueryArgument arg: args) {
					URI opURI = URI.create(arg.getValue());
					boolean result = Janus.ontBridge.containsObjectProperty(opURI);
					if (!result) {
						System.out.println(atom.toString() + " is false.");
						return false;
					} else {
						System.out.println(atom.toString() + " is true.");
						break;
					}
				}
				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.INVERSE_OF)) {
				List<QueryArgument> args = atom.getArguments();
				URI opURI1 = URI.create(args.get(0).getValue());
				URI opURI2 = URI.create(args.get(1).getValue());
				
				boolean result = Janus.ontBridge.isInverseObjectPropertyOf(opURI1, opURI2);
				if (!result) {
					System.out.println(atom.toString() + " is false.");
					return false;
				} else {
					System.out.println(atom.toString() + " is true.");
					continue;
				}
			}
			
			if (atom.getType().equals(QueryAtomType.EQUIVALENT_PROPERTY)) {
				List<QueryArgument> args = atom.getArguments();
				URI pURI1 = URI.create(args.get(0).getValue());
				URI pURI2 = URI.create(args.get(1).getValue());
				
				boolean result = false;
				if (Janus.ontBridge.containsDataProperty(pURI1))
					result = Janus.ontBridge.isEquivalentDataPropertyOf(pURI1, pURI2);
				else 
					result = Janus.ontBridge.isEquivalentObjectPropertyOf(pURI1, pURI2);
					
				if (!result) {
					System.out.println(atom.toString() + " is false.");
					return false;
				} else {
					System.out.println(atom.toString() + " is true.");
					continue;
				}
			}
			
			if (atom.getType().equals(QueryAtomType.SUB_PROPERTY_OF)) {
				List<QueryArgument> args = atom.getArguments();
				URI pURI1 = URI.create(args.get(0).getValue());
				URI pURI2 = URI.create(args.get(1).getValue());
				
				boolean result = false;
				if (Janus.ontBridge.containsDataProperty(pURI1))
					result = Janus.ontBridge.isSubDataPropertyOf(pURI1, pURI2);
				else 
					result = Janus.ontBridge.isSubObjectPropertyOf(pURI1, pURI2);
					
				if (!result) {
					System.out.println(atom.toString() + " is false.");
					return false;
				} else {
					System.out.println(atom.toString() + " is true.");
					continue;
				}
			}
		}
		
		return true;
	}
	
	private boolean executeGroupR1() {
		for (QueryAtom atom: group_r_1) {
			if (atom.getType().equals(QueryAtomType.TRANSITIVE)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName = args.get(0).toString();
				Variable variable;
				
				variable = varsMap.get(varName);
					
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				Set<URI> transitiveObjProps = Janus.ontBridge.getAllTransitiveObjProps();
				variable.intersectURIs(transitiveObjProps);
				
				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.SYMMETRIC)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName = args.get(0).toString();
				Variable variable;
				
				variable = varsMap.get(varName);
					
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				Set<URI> symmetricObjProps = Janus.ontBridge.getAllSymmetricObjProps();
				variable.intersectURIs(symmetricObjProps);

				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.INVERSE_FUNCTIONAL)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName = args.get(0).toString();
				Variable variable;
				
				variable = varsMap.get(varName);
					
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				Set<URI> inverseFunctionalObjProps = Janus.ontBridge.getAllInverseFunctionalObjProps();
				variable.intersectURIs(inverseFunctionalObjProps);

				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.FUNCTIONAL)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName = args.get(0).toString();
				Variable variable;
				
				variable = varsMap.get(varName);
					
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				if (variable.getVarType().equals(VariableTypes.OBJECT_PROPERTIES)) {
					Set<URI> functionalObjProps = Janus.ontBridge.getAllFunctionalObjProps();
					variable.intersectURIs(functionalObjProps);
					continue;
				}
				
				if (variable.getVarType().equals(VariableTypes.DATA_PROPERTIES)) {
					Set<URI> functionalDataProps = Janus.ontBridge.getAllFunctionalDataProps();
					variable.intersectURIs(functionalDataProps);
					continue;
				}
				
				if (variable.getVarType().equals(VariableTypes.PROPERTIES)) {
					Set<URI> functionalProps = new ConcurrentSkipListSet<URI>();
					
					functionalProps.addAll(Janus.ontBridge.getAllFunctionalObjProps());
					functionalProps.addAll(Janus.ontBridge.getAllFunctionalDataProps());
					
					variable.intersectURIs(functionalProps);
					
					continue;
				}
			}
			
			if (atom.getType().equals(QueryAtomType.DATA_PROPERTY)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName = args.get(0).toString();
				Variable variable;
				
				variable = varsMap.get(varName);
					
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				Set<URI> dataProps = Janus.ontBridge.getAllDataProps();
				variable.intersectURIs(dataProps);
				
				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.OBJECT_PROPERTY)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName = args.get(0).toString();
				Variable variable;
				
				variable = varsMap.get(varName);
					
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				Set<URI> objProps = Janus.ontBridge.getAllObjProps();
				variable.intersectURIs(objProps);
				
				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.INVERSE_OF)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName = null;
				URI opURI = null;
				
				for (QueryArgument arg: args)
					if (arg.getType().equals(QueryArgumentType.VAR))
						varName = arg.toString();
					else
						opURI = URI.create(arg.getValue());
				
				Variable variable = varsMap.get(varName);
				
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				Set<URI> inverseObjProps = Janus.ontBridge.getInverseObjectProperties(opURI);
				variable.intersectURIs(inverseObjProps);
				
				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.EQUIVALENT_PROPERTY)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName = null;
				URI pURI = null;
				
				for (QueryArgument arg: args)
					if (arg.getType().equals(QueryArgumentType.VAR))
						varName = arg.toString();
					else
						pURI = URI.create(arg.getValue());
				
				Variable variable = varsMap.get(varName);
				
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				if (Janus.ontBridge.containsObjectProperty(pURI)) {
					variable.setVarType(VariableTypes.OBJECT_PROPERTIES);
					
					Set<URI> equivalentObjProps = Janus.ontBridge.getEquivalentObjectProperties(pURI);
					variable.intersectURIs(equivalentObjProps);
					continue;
				}
				
				if (Janus.ontBridge.containsDataProperty(pURI)) {
					variable.setVarType(VariableTypes.DATA_PROPERTIES);
					
					Set<URI> equivalentDataProps = Janus.ontBridge.getEquivalentDataProperties(pURI);
					variable.intersectURIs(equivalentDataProps);
					continue;
				}
			}
			
			if (atom.getType().equals(QueryAtomType.SUB_PROPERTY_OF)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				
				String varName = null;
				URI pURI = null;
				
				if (arg1.getType().equals(QueryArgumentType.VAR)) {
					varName = arg1.toString();
					pURI = URI.create(arg2.getValue());
				}
				else {
					varName = arg2.toString();
					pURI = URI.create(arg1.getValue());
				}
				
				Variable variable = varsMap.get(varName);
				
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				if (Janus.ontBridge.containsObjectProperty(pURI)) {
					variable.setVarType(VariableTypes.OBJECT_PROPERTIES);
					
					if (arg1.getType().equals(QueryArgumentType.VAR)) {
						Set<URI> allSubObjProps = Janus.ontBridge.getAllSubObjProps(pURI);
						variable.intersectURIs(allSubObjProps);
						continue;
					}
					else {
						Set<URI> allSuperObjProps = Janus.ontBridge.getAllSuperObjProps(pURI);
						variable.intersectURIs(allSuperObjProps);
						continue;
					}
				}
				
				if (Janus.ontBridge.containsDataProperty(pURI)) {
					variable.setVarType(VariableTypes.DATA_PROPERTIES);
					
					if (arg1.getType().equals(QueryArgumentType.VAR)) {
						Set<URI> allSubDataProps = Janus.ontBridge.getAllSubDataProps(pURI);
						variable.intersectURIs(allSubDataProps);
						continue;
					}
					else {
						Set<URI> allSuperDataProps = Janus.ontBridge.getAllSuperDataProps(pURI);
						variable.intersectURIs(allSuperDataProps);
						continue;
					}
				}
			}
		}
		
		return true;
	}
	
	private boolean executeGroupR2() {
		for (QueryAtom atom: group_r_2) {
			
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
					
					continue;
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

						continue;
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

						continue;
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

					continue;
				
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
					
					continue;
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

						continue;

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

						continue;
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

					continue;
				
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
					
					continue;
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

						continue;

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

						continue;
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

					continue;
				}
			}
		}
		
		return true;
	}
	
	private boolean executeGroupT1() {
		for (QueryAtom atom: group_t_1) {
			if (atom.getType().equals(QueryAtomType.COMPLEMENT_OF)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				
				String varName = null;
				URI cURI = null;
				
				if (arg1.getType().equals(QueryArgumentType.VAR)) {
					varName = arg1.toString();
					cURI = URI.create(arg2.getValue());
				}
				else {
					varName = arg2.toString();
					cURI = URI.create(arg1.getValue());
				}
				
				Variable variable = varsMap.get(varName);
				
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				URI owlThing = Janus.ontBridge.getOWLThingURI();
				URI owlNothing = Janus.ontBridge.getOWLNothingURI();
				
				Set<URI> complementClasses = new ConcurrentSkipListSet<URI>();
				
				if (cURI.equals(owlThing))
					complementClasses.add(owlNothing);
				else if (cURI.equals(owlNothing))
					complementClasses.add(owlThing);
				else if (!Janus.ontBridge.containsClass(cURI)) {
						System.out.println("The class " + cURI + " in the " + atom.toString() + " is not defined.");
						return false;
				}
				
				variable.intersectURIs(complementClasses);
				continue;
			}
			
			if (atom.getType().equals(QueryAtomType.DISJOINT_WITH)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName = null;
				URI cURI = null;
				
				for (QueryArgument arg: args)
					if (arg.getType().equals(QueryArgumentType.VAR))
						varName = arg.toString();
					else
						cURI = URI.create(arg.getValue());
				
				Variable variable = varsMap.get(varName);
				
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				if (Janus.ontBridge.containsClass(cURI)) {
					Set<URI> disjointClasses = Janus.ontBridge.getAllDisjointClasses(cURI);
					variable.intersectURIs(disjointClasses);
					continue;
				} else {
					URI owlNothing = Janus.ontBridge.getOWLNothingURI();
					
					if (cURI.equals(owlNothing)) {
						Set<URI> disjointClasses = Janus.ontBridge.getAllDisjointClasses(cURI);
						variable.intersectURIs(disjointClasses);
						continue;
					} else {
						System.out.println("The class " + cURI + " in the " + atom.toString() + " is not defined.");
						return false;
					}
				}
			}
			
			if (atom.getType().equals(QueryAtomType.EQUIVALENT_CLASS)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName = null;
				URI cURI = null;
				
				for (QueryArgument arg: args)
					if (arg.getType().equals(QueryArgumentType.VAR))
						varName = arg.toString();
					else
						cURI = URI.create(arg.getValue());
				
				Variable variable = varsMap.get(varName);
				
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				if (Janus.ontBridge.containsClass(cURI)) {
					Set<URI> equivalentClasses = Janus.ontBridge.getEquivalentClasses(cURI);
					variable.intersectURIs(equivalentClasses);
					continue;
				} else {
					URI owlNothing = Janus.ontBridge.getOWLNothingURI();
					
					if (cURI.equals(owlNothing)) {
						Set<URI> equivalentClasses = Janus.ontBridge.getEquivalentClasses(cURI);
						variable.intersectURIs(equivalentClasses);
						continue;
					} else {
						System.out.println("The class " + cURI + " in the " + atom.toString() + " is not defined.");
						return false;
					}
				}
			}
			
			if (atom.getType().equals(QueryAtomType.SUB_CLASS_OF)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				
				String varName = null;
				URI cURI = null;
				
				if (arg1.getType().equals(QueryArgumentType.VAR)) {
					varName = arg1.toString();
					cURI = URI.create(arg2.getValue());
				}
				else {
					varName = arg2.toString();
					cURI = URI.create(arg1.getValue());
				}
				
				Variable variable = varsMap.get(varName);
				
				if (variable.hasURIsFinished()) {
					System.out.println("The variable " + varName + " in the " + atom.toString() + " has already been the empty set, so this atom doesn't have to be calculated.");
					continue;
				}
				
				if (Janus.ontBridge.containsClass(cURI)) {
					if (arg1.getType().equals(QueryArgumentType.VAR)) {
						Set<URI> allSubClasses = Janus.ontBridge.getAllSubClasses(cURI);
						variable.intersectURIs(allSubClasses);
						continue;
					}
					else {
						Set<URI> allSuperClasses = Janus.ontBridge.getAllSuperClasses(cURI);
						variable.intersectURIs(allSuperClasses);
						continue;
					}
				} else {
					URI owlNothing = Janus.ontBridge.getOWLNothingURI();
					
					if (cURI.equals(owlNothing)) {
						if (arg1.getType().equals(QueryArgumentType.VAR)) {
							Set<URI> allSubClasses = Janus.ontBridge.getAllSubClasses(cURI);
							variable.intersectURIs(allSubClasses);
							continue;
						}
						else {
							Set<URI> allSuperClasses = Janus.ontBridge.getAllSuperClasses(cURI);
							variable.intersectURIs(allSuperClasses);
							continue;
						}
					} else {
						System.out.println("The class " + cURI + " in the " + atom.toString() + " is not defined.");
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	private boolean executeGroupT2() {
		for (QueryAtom atom: group_t_2) {
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
					
					continue;
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

						continue;
						
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

						continue;
					}
					
				} else {
				
					complementClasses.add(owlNothing);
					complementClasses.add(owlThing);

					variable1.intersectURIs(complementClasses);
					variable2.intersectURIs(complementClasses);
					continue;
				
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
					
					continue;
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

						continue;
						
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

						continue;
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
					continue;
				
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
					
					continue;
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

						continue;
						
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

						continue;
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
					continue;
				
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
					
					continue;
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

						continue;
						
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

						continue;
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

					continue;

				}
			}
			
		}
		
		return true;
	}
	
	private boolean existsAnnotationAtom() {
		List<QueryAtomGroup> groups = originalQuery.getAtomGroups();
		for (QueryAtomGroup group : groups) {
			List<QueryAtom> atoms = group.getAtoms();
			for (QueryAtom atom : atoms)
				if (atom.getType().equals(QueryAtomType.ANNOTATION))
					return true;
		}
		
		return false;
	}
	
	private boolean existsBlankNode() {
		List<QueryAtomGroup> groups = originalQuery.getAtomGroups();
		for (QueryAtomGroup group : groups) {
			List<QueryAtom> atoms = group.getAtoms();
			for (QueryAtom atom : atoms) {
				List<QueryArgument> args = atom.getArguments();
				for (QueryArgument arg: args)
					if (arg.isBnode())
						return true;
			}
		}
		
		return false;
	}
	
	private void preprocess() {
		buildPrefixMap();
		
		divideAtomsIntoGroups();
		
		findVariableType();
		
		checkConflict();
	}
	
	private void checkConflict() {
		List<QueryAtomGroup> groups = originalQuery.getAtomGroups();
		
		for (QueryAtomGroup group : groups) {
			List<QueryAtom> atoms = group.getAtoms();
			
			for (QueryAtom atom : atoms) {
				
				if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM)) {
					
					List<QueryArgument> args = atom.getArguments();
					
					for (QueryAtom atom2 : atoms) {
						
						if (atom2.getType().equals(QueryAtomType.SAME_AS)) {
							
							List<QueryArgument> args2 = atom2.getArguments();
							
							if (args.containsAll(args2)) {
								System.out.println("Catched conflict...");
								
								QueryArgument arg1 = args2.get(0);
								QueryArgument arg2 = args2.get(1);
								
								if (arg1.getType().equals(QueryArgumentType.VAR) && arg2.getType().equals(QueryArgumentType.VAR)) {
									String varName1 = arg1.toString();
									String varName2 = arg2.toString();
									
									Variable variable1 = varsMap.get(varName1);
									Variable variable2 = varsMap.get(varName2);
									
									variable1.makeFinished();
									variable2.makeFinished();
								}
							}
						}
					}
				}
				
				if (atom.getType().equals(QueryAtomType.DISJOINT_WITH) || atom.getType().equals(QueryAtomType.COMPLEMENT_OF)) {
					
					List<QueryArgument> args = atom.getArguments();
					
					for (QueryAtom atom2 : atoms) {
						
						if (atom2.getType().equals(QueryAtomType.SUB_CLASS_OF) || atom2.getType().equals(QueryAtomType.EQUIVALENT_CLASS)) {
							
							List<QueryArgument> args2 = atom2.getArguments();
							
							if (args.containsAll(args2)) {
								System.out.println("Catched conflict...");
								
								QueryArgument arg1 = args2.get(0);
								QueryArgument arg2 = args2.get(1);
								
								if (arg1.getType().equals(QueryArgumentType.VAR) && arg2.getType().equals(QueryArgumentType.VAR)) {
									String varName1 = arg1.toString();
									String varName2 = arg2.toString();
									
									Variable variable1 = varsMap.get(varName1);
									Variable variable2 = varsMap.get(varName2);
									
									variable1.makeFinished();
									variable2.makeFinished();
								}
							}
						}
					}
				}
				
				if (atom.getType().equals(QueryAtomType.EQUIVALENT_CLASS)) {
					
					List<QueryArgument> args = atom.getArguments();
					
					for (QueryAtom atom2 : atoms) {
						
						if (atom2.getType().equals(QueryAtomType.SUB_CLASS_OF)) {
							
							List<QueryArgument> args2 = atom2.getArguments();
							
							if (args.containsAll(args2)) {
								System.out.println("Optimize...");
								
								group_t_0.remove(atom2);
								group_t_1.remove(atom2);
								group_t_2.remove(atom2);
							}
						}
					}
				}
			}
		}
	}
	
	private void findVariableType() {
		findVariableTypeFromRBoxAtoms();
		findVariableTypeFromTBoxAtoms();
		findVariableTypeFromABoxAtoms();
	}
	
	private void findVariableTypeFromABoxAtoms() {
		List<QueryAtom> group_a = new Vector<QueryAtom>();
		group_a.addAll(group_a_1);
		group_a.addAll(group_a_2);
		group_a.addAll(group_a_3);
		
		for (QueryAtom atom: group_a) {
			List<QueryArgument> args = atom.getArguments();
			int argIndex = 0;
			for (QueryArgument arg: args) {
				argIndex++;
				if (arg.isVar()) {
					
					String var = arg.toString();
					Variable variable;
					
					if (varsMap.containsKey(var))
						variable = varsMap.get(var);
					else
						variable = new Variable(var);
					
					
					if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM) || atom.getType().equals(QueryAtomType.SAME_AS))
						variable.setVarType(VariableTypes.INDIVIDUALS);
					else if (atom.getType().equals(QueryAtomType.TYPE)) {
						if (argIndex == 1)
							variable.setVarType(VariableTypes.INDIVIDUALS);
						else
							variable.setVarType(VariableTypes.CLASSES);
					}
					else if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
						if (argIndex == 1)
							variable.setVarType(VariableTypes.INDIVIDUALS);
						else if(argIndex == 2)
							variable.setVarType(VariableTypes.PROPERTIES);
						else if(argIndex == 3)
							variable.setVarType(VariableTypes.INDIVIDUALS_OR_LITERALS);
					}
					
					varsMap.put(var, variable);
				}
			}
		}
	}
	
	
	
	private void findVariableTypeFromTBoxAtoms() {
		List<QueryAtom> group_t = new Vector<QueryAtom>();
		group_t.addAll(group_t_1);
		group_t.addAll(group_t_2);
		
		for (QueryAtom atom: group_t) {
			List<QueryArgument> args = atom.getArguments();
			for (QueryArgument arg: args)
				if (arg.isVar()) {
					
					String var = arg.toString();
					Variable variable;
					
					if (varsMap.containsKey(var))
						variable = varsMap.get(var);
					else
						variable = new Variable(var);
					
					
					variable.setVarType(VariableTypes.CLASSES);
					
					varsMap.put(var, variable);
				}
		}
	}
	
	private void findVariableTypeFromRBoxAtoms() {
		List<QueryAtom> group_r = new Vector<QueryAtom>();
		group_r.addAll(group_r_1);
		group_r.addAll(group_r_2);
		
		for (QueryAtom atom: group_r) {
			List<QueryArgument> args = atom.getArguments();
			for (QueryArgument arg: args)
				if (arg.isVar()) {
					
					String var = arg.toString();
					Variable variable;
					
					if (varsMap.containsKey(var))
						variable = varsMap.get(var);
					else
						variable = new Variable(var);
					
					
					if (atom.getType().equals(QueryAtomType.DATA_PROPERTY))
						variable.setVarType(VariableTypes.DATA_PROPERTIES);
					else if (atom.getType().equals(QueryAtomType.OBJECT_PROPERTY) || atom.getType().equals(QueryAtomType.INVERSE_FUNCTIONAL) || atom.getType().equals(QueryAtomType.SYMMETRIC) || atom.getType().equals(QueryAtomType.TRANSITIVE) || atom.getType().equals(QueryAtomType.INVERSE_OF))
						variable.setVarType(VariableTypes.OBJECT_PROPERTIES);
					else if (atom.getType().equals(QueryAtomType.FUNCTIONAL) || atom.getType().equals(QueryAtomType.SUB_PROPERTY_OF) || atom.getType().equals(QueryAtomType.EQUIVALENT_PROPERTY))
						variable.setVarType(VariableTypes.PROPERTIES);
					
					varsMap.put(var, variable);
				}
		}
	}
	
	private void buildPrefixMap() {
		String copy = originalQueryString.toUpperCase();
		
		int fromIndex = 0;
		int prefixIndex = copy.indexOf("PREFIX", fromIndex);
		while (prefixIndex > -1) {
			int colonIndex = copy.indexOf(":", fromIndex);

			String prefix = originalQueryString.substring(prefixIndex + 6, colonIndex).trim();

			int URIBeginIndex = copy.indexOf("<", fromIndex);
			int URIEndIndex = copy.indexOf(">", fromIndex);

			String URI = originalQueryString.substring(URIBeginIndex + 1, URIEndIndex);

			prefixMap.put(URI, prefix);
			
			fromIndex = URIEndIndex+1;
			
			prefixIndex = copy.indexOf("PREFIX", fromIndex);
		}
	}
	
	private void createQueryObject(String query) {
		try {
			originalQuery = Query.create(query);
		} catch (QueryParserException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isTBoxAtom(QueryAtom atom) {
		QueryAtomType type = atom.getType();
		if (type.equals(QueryAtomType.COMPLEMENT_OF)
				|| type.equals(QueryAtomType.DISJOINT_WITH)
				|| type.equals(QueryAtomType.EQUIVALENT_CLASS)
				|| type.equals(QueryAtomType.SUB_CLASS_OF))
			return true;
		
		return false;
	}
	
	private boolean isRBoxAtom(QueryAtom atom) {
		QueryAtomType type = atom.getType();
		if (type.equals(QueryAtomType.DATA_PROPERTY)
				|| type.equals(QueryAtomType.FUNCTIONAL)
				|| type.equals(QueryAtomType.INVERSE_FUNCTIONAL)
				|| type.equals(QueryAtomType.OBJECT_PROPERTY)
				|| type.equals(QueryAtomType.SYMMETRIC)
				|| type.equals(QueryAtomType.TRANSITIVE)
				|| type.equals(QueryAtomType.INVERSE_OF)
				|| type.equals(QueryAtomType.EQUIVALENT_PROPERTY)
				|| type.equals(QueryAtomType.SUB_PROPERTY_OF))
			return true;
		
		return false;
	}
	
	// not support OR WHERE Clause
	private void divideAtomsIntoGroups() {
		List<QueryAtom> group2 = new Vector<QueryAtom>();
		List<QueryAtom> group3 = new Vector<QueryAtom>();
		
		List<QueryAtomGroup> groups = originalQuery.getAtomGroups();
		for (QueryAtomGroup group : groups) {
			List<QueryAtom> atoms = group.getAtoms();
			for (QueryAtom atom : atoms) {
				List<QueryArgument> args = atom.getArguments();
				int groupIndex = args.size();
				for (QueryArgument arg: args)
					if (!arg.isVar()) groupIndex--;
				
				if (groupIndex == 2)
					group2.add(atom);
				else if (groupIndex == 3)
					group3.add(atom);
				
				if (isTBoxAtom(atom)) {
					if (groupIndex == 0)
						group_t_0.add(atom);
					else if (groupIndex == 1)
						group_t_1.add(atom);
					else if (groupIndex == 2)
						group_t_2.add(atom);
					
					continue;
				} else if(isRBoxAtom(atom)) {
					if (groupIndex == 0)
						group_r_0.add(atom);
					else if (groupIndex == 1)
						group_r_1.add(atom);
					else if (groupIndex == 2)
						group_r_2.add(atom);

					continue;
				}
				
				if (groupIndex == 0)
					group_a_0.add(atom);
				else if (groupIndex == 1)
					group_a_1.add(atom);
				else if (groupIndex == 2)
					group_a_2.add(atom);
				else if (groupIndex == 3)
					group_a_3.add(atom);
			}
		}
		
		groupGT1.addAll(group2);
		groupGT1.addAll(group3);
	}
}
