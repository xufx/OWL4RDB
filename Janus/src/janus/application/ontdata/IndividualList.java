package janus.application.ontdata;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.net.URI;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class IndividualList extends JScrollPane {
	JTable table;
	
	IndividualList(URI cls) {
		table = new IndividualListTable(new IndividualListTableModel(cls));
		table.setDefaultRenderer(Object.class, new IndividualListTableRenderer(new ImageIcon(ImageURIs.ONT_INDIVIDUAL)));
		table.setTableHeader(null);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDragEnabled(true);
		table.setFillsViewportHeight(true);
		
		setViewportView(table);
	}
}

@SuppressWarnings("serial")
class IndividualListTable extends JTable {
	
	IndividualListTable(TableModel dm) {
		super(dm);
	}

	public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);
        System.out.println(getValueAt(getSelectedRow(), getSelectedColumn()));
    }

	@Override
	public String getToolTipText(MouseEvent event) {
		Point p = event.getPoint();
		int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
		
        getValueAt(rowIndex, colIndex);
		
		// TODO Auto-generated method stub
		return super.getToolTipText(event);
	}
}

@SuppressWarnings("serial")
class IndividualListTableModel extends AbstractTableModel {

	private int columnCount;
	private int rowCount;
	private CachedRecord cache;
	
	private SQLResultSet resultSet;

	IndividualListTableModel(URI cls) {
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
class IndividualListTableRenderer extends DefaultTableCellRenderer {

	private Icon individualIcon;
	
	IndividualListTableRenderer(Icon individualIcon) {
		this.individualIcon = individualIcon;
	}
	
	@Override
	protected void setValue(Object value) {
		super.setValue(value);
		setIcon(individualIcon);
	}
}
