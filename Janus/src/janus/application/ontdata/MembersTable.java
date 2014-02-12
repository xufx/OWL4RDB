package janus.application.ontdata;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import janus.ImageURIs;
import janus.Janus;
import janus.database.SQLResultSet;
import janus.mapping.metadata.ClassTypes;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
class MembersTable extends JScrollPane {
	IndividualListTable table;
	
	MembersTable(URI cls) {
		table = new IndividualListTable(new MembersTableModel(cls));
		table.setDefaultRenderer(Object.class, new MembersTableRenderer(new ImageIcon(ImageURIs.ONT_INDIVIDUAL)));
		table.setTableHeader(null);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDragEnabled(true);
		
		setViewportView(table);
	}
	
	void addMembersTableSelectionListener(ListSelectionListener x) {
		table.getSelectionModel().addListSelectionListener(x);
	}
	
	JTable getSelectionSource() {
		return table;
	}
}

@SuppressWarnings("serial")
class IndividualListTable extends JTable {
	private URI ontologyURI;
	
	IndividualListTable(TableModel dm) {
		super(dm);
		
		ontologyURI = Janus.ontBridge.getOntologyID();
	}
	
	@Override
	public String getToolTipText(MouseEvent event) {
		Point p = event.getPoint();
		int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        
        return getIndividual((String)getValueAt(rowIndex, colIndex)).toString();
	}
	
	private URI getIndividual(String individualFragment) {
		String individualString = ontologyURI.getScheme() + ":" + ontologyURI.getSchemeSpecificPart() + "#" + individualFragment;
		
		URI individual = null;
		try {
			individual =  new URI(individualString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return individual;
	}
}

@SuppressWarnings("serial")
class MembersTableModel extends AbstractTableModel {
	
	private int columnCount;
	private int rowCount;
	private CachedRecord cache;
	
	private SQLResultSet resultSet;

	MembersTableModel(URI cls) {
		String query = getQuery(cls);
		
		resultSet = Janus.dbBridge.executeQuery(query);
		
		rowCount = resultSet.getResultSetRowCount();
		columnCount = resultSet.getResultSetColumnCount();
		
		cache = new CachedRecord();
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		
		Record record = cache.get(rowIndex);

		if (record == null) {
			record = getRecord(rowIndex);
			cache.put(rowIndex, record);
		}

		return record.get(columnIndex);
	}
	
	private String getQuery(URI cls) {
		String query = null;
		
		if (Janus.mappingMetadata.getClassType(cls).equals(ClassTypes.TABLE_CLASS))
			query = getQueryToGetIndividualsOfTableClass(cls);
		else if (Janus.mappingMetadata.getClassType(cls).equals(ClassTypes.COLUMN_CLASS))
			query = getQueryToGetIndividualsOfColumnClass(cls);
		else if (Janus.mappingMetadata.getClassType(cls).equals(ClassTypes.OWL_THING)) {
			
			List<String> queries = new Vector<String>();
			
			Set<URI> clses = Janus.ontBridge.getSubClses(cls);
			for (URI aCls: clses) {
				if (Janus.mappingMetadata.getClassType(aCls).equals(ClassTypes.TABLE_CLASS))
					queries.add(getQueryToGetIndividualsOfTableClass(aCls));
				else if (Janus.mappingMetadata.getClassType(aCls).equals(ClassTypes.COLUMN_CLASS))
					queries.add(getQueryToGetIndividualsOfColumnClass(aCls));
			}
			
			query = Janus.sqlGenerator.getUnionQuery(queries);
		}
		
		return query;
	}
	
	private String getQueryToGetIndividualsOfTableClass(URI cls) {
		String table = Janus.mappingMetadata.getMappedTableNameOfClass(cls);
		List<String> pk = Janus.cachedDBMetadata.getPrimaryKeys(table);
		
		return Janus.sqlGenerator.getQueryToGetIndividualsOfTableClass(pk, table);
	}
	
	private String getQueryToGetIndividualsOfColumnClass(URI cls) {
		String table = Janus.mappingMetadata.getMappedTableNameOfClass(cls);
		String column = Janus.mappingMetadata.getMappedColumnNameOfClass(cls);
		
		if (Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table))
			return Janus.sqlGenerator.getQueryToGetIndividualsOfSinglePKColumnClass(column, table);
		else if (Janus.cachedDBMetadata.isNotNull(table, column))
			return Janus.sqlGenerator.getQueryToGetIndividualsOfNonNullableColumnClass(column, table);
		else
			return Janus.sqlGenerator.getQueryToGetIndividualsOfNullableColumnClass(column, table);
	}

	private Record getRecord(int rowIndex) {
		return new Record(resultSet.getResultSetRowAt(rowIndex + 1));
	}

	private class Record {
		private List<String> record;
		
		Record(List<String> record) {
			this.record = record;
		}
		
		String get(int columnIndex) {
			return record.get(columnIndex);
		}
	}

	private class CachedRecord extends LinkedHashMap<Integer,Record> {
		private static final int MAX_ENTRIES = 100;

		@Override
		protected boolean removeEldestEntry(Entry<Integer, Record> eldest) {
			return size() > MAX_ENTRIES;
		}
	}
}

@SuppressWarnings("serial")
class MembersTableRenderer extends DefaultTableCellRenderer {

	private Icon individualIcon;
	
	MembersTableRenderer(Icon individualIcon) {
		this.individualIcon = individualIcon;
	}
	
	@Override
	protected void setValue(Object value) {
		super.setValue(value);
		setIcon(individualIcon);
	}
}
