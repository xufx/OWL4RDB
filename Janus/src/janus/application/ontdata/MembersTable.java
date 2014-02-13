package janus.application.ontdata;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import janus.ImageURIs;
import janus.Janus;
import janus.database.SQLResultSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
class MembersTable extends JScrollPane {
	private JTable table;
	
	MembersTable(URI cls) {
		table = new JTable(new MembersTableModel(cls)) {

			@Override
			public String getToolTipText(MouseEvent event) {
				Point p = event.getPoint();
				int rowIndex = rowAtPoint(p);
		        int colIndex = columnAtPoint(p);
		        
		        return getIndividual((String)getValueAt(rowIndex, colIndex)).toString();
			}
		};
		table.setDefaultRenderer(Object.class, new MembersTableRenderer(new ImageIcon(ImageURIs.ONT_INDIVIDUAL)));
		table.setTableHeader(null);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDragEnabled(true);
		
		setViewportView(table);
	}
	
	void addMembersTableSelectionListener(ListSelectionListener x) {
		table.getSelectionModel().addListSelectionListener(x);
	}
	
	private URI getIndividual(String individualFragment) {
		return Janus.mappingMetadata.getIndividual(individualFragment);
	}
	
	URI getSelectedMember() {
		return getIndividual(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString());
	}
}

@SuppressWarnings("serial")
class MembersTableModel extends AbstractTableModel {
	
	private int columnCount;
	private int rowCount;
	private CachedRecord cache;
	
	private SQLResultSet resultSet;

	MembersTableModel(URI cls) {
		String query = Janus.sqlGenerator.getQueryToGetIndividualsOfClass(cls);
		
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
