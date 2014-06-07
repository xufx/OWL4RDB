package janus.query.sparqldl;

import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
class URIResultSet extends DefaultTableModel implements SPARQLDLResultSet {
	
	URIResultSet() {}
	
	URIResultSet(String varName, URI[] URIs) {
		addColumn(varName, URIs);
	}
	
	URIResultSet(URI[][] URIs, String[] varNames) {
		super(URIs, varNames);
	}
	
	URIResultSet(Vector<Vector<URI>> URIs, Vector<String> varNames) {
		super(URIs, varNames);
	}
	
	URIResultSet(Vector<String> columnNames, int rowCount) {
		super(columnNames, rowCount);
	}
	
	public boolean isEmptySet() {
		return getRowCount() < 1;
	}
	
	public String toString() {
		int rowCount = getRowCount();
		int columnCount = getColumnCount();
		
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < columnCount; i++)
			buf.append(getColumnName(i) + "\t");
		buf.append("\n");
		
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < columnCount; j++)
				buf.append(getValueAt(i, j) + "\t");
			buf.append("\n");
		}
		
		return buf.toString();
	}
	
	Vector<String> getColumnNames() {
		int colCount = getColumnCount();
		Vector<String> varNames = new Vector<String>();
		for (int i = 0; i < colCount; i++)
			varNames.addElement(getColumnName(i));
		
		return varNames;
	}
	
	private List<String> getCommonColumnNames(URIResultSet arg) {
		List<String> varNamesOfThis = getColumnNames();
		
		List<String> varNamesOfArg = arg.getColumnNames();
		
		varNamesOfArg.retainAll(varNamesOfThis);
		
		return varNamesOfArg;
	}
	
	URIResultSet getNaturalJoinedURIResultSet(URIResultSet arg) {
		Vector<String> columnNames = getColumnNames();
		columnNames.addAll(arg.getColumnNames());
		
		int rowCountOfArg = arg.getRowCount();
		int rowCountOfNew = getRowCount() * rowCountOfArg;
		
		URIResultSet newTable = new URIResultSet(columnNames, rowCountOfNew);
		
		int colCountOfNew = newTable.getColumnCount();
		int colCountOfThis = getColumnCount();
		
		// Cartesian Product
		for (int i = 0; i < rowCountOfNew; i++) {
			for (int j = 0; j < colCountOfNew; j++) {
				Object value = null;
				
				if (j < colCountOfThis)
					value = getValueAt(i / rowCountOfArg, j);
				else
					value = arg.getValueAt(i % rowCountOfArg, j - colCountOfThis);
				
				newTable.setValueAt(value, i, j);
			}
		}
		
		Map<String, Vector<Integer>> colIndicesToVarName  = new Hashtable<String, Vector<Integer>>();
		
		List<String> commonVarNames = getCommonColumnNames(arg);
		
		for (String var: commonVarNames) {
			Vector<Integer> indices = new Vector<Integer>();
			for (int i = 0; i < colCountOfNew; i++) {
				if (newTable.getColumnName(i).equals(var))
					indices.add(i);
			}
			colIndicesToVarName.put(var, indices);
		}
		
		// Equi Join
		ROW:
		for (int i = 0; i < newTable.getRowCount(); i++) {
			for (String var: commonVarNames) {
				Vector<Integer> indices = colIndicesToVarName.get(var);
				URI value = (URI)newTable.getValueAt(i, indices.get(0));
				for (int j = 1; j < indices.size(); j++) {
					if (!((URI)newTable.getValueAt(i, indices.get(j))).equals(value)) {
						newTable.removeRow(i--);
						continue ROW;
					}
				}
			}
		}
		
		// Natural Join
		@SuppressWarnings("unchecked")
		Vector<Vector<URI>> data = newTable.getDataVector();
		int removedColumnCount = 0;
		for (String var: commonVarNames) {
			Vector<Integer> indices = colIndicesToVarName.get(var);
			for (int i = 1; i < indices.size(); i++) {
				for (Vector<URI> row: data)
					row.remove(indices.get(i) - removedColumnCount);
				columnNames.remove(indices.get(i) - removedColumnCount);
				removedColumnCount++;
			}
		}
		
		return new URIResultSet(data, columnNames);
	}
}
