package janus.query.sparqldl;

import janus.Janus;

import java.net.URI;
import java.util.List;
import java.util.Vector;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.types.QueryArgumentType;

class AtomSameAsProcessor {
	static boolean execute0WithoutPresenceCheck(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		
		URI iURI1 = URI.create(arg1.getValue());
		URI iURI2 = URI.create(arg2.getValue());
		
		if (!iURI1.equals(iURI2))
			return false;
		else
			return true;
	}
	
	static String execute0WithPresenceCheck(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg1 = args.get(0);
		
		URI iURI1 = URI.create(arg1.getValue());
		
		return Janus.sqlGenerator.getQueryToCheckPresenceOfIndividual(iURI1);
	}
	
	static SQLResultSet execute1(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		
		String varName = null;
		URI iURI = null;
		
		if (arg1.getType().equals(QueryArgumentType.VAR)) {
			varName = arg1.getValue();
			iURI = URI.create(arg2.getValue());
		}
		else {
			varName = arg2.getValue();
			iURI = URI.create(arg1.getValue());
		}
		
		if (!Janus.mappingMetadata.isBeableIndividual(iURI)) {
			System.err.println("No Such an Individual.");
			SQLResultSet resultSet = new SQLResultSet(Janus.sqlGenerator.getQueryToGetEmptyResultSet(varName), varName);
			resultSet.setEmptySet();
			return resultSet;
		}
		
		String query = Janus.sqlGenerator.getQueryToGetTheSameIndividualsAs(iURI, varName);
		
		return new SQLResultSet(query, varName);
	}
	
	static SQLResultSet execute2(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		String varName1 = args.get(0).getValue();
		String varName2 = args.get(1).getValue();
		
		String query = Janus.sqlGenerator.getQueryToGetAllPairsOfTheSameIndividuals(varName1, varName2);
		
		List<String> varNames = new Vector<String>();
		
		varNames.add(varName1);
		varNames.add(varName2);
		
		return new SQLResultSet(query, varNames);
	}
}
