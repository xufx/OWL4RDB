package janus.application.ontdata;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import janus.ImageURIs;
import janus.Janus;
import janus.database.SQLResultSet;
import janus.ontology.OWLEntityTypes;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
class OPAssertionsTable extends JScrollPane {
	private JTable table;
	private OWLEntityTypes individualType;
	
	OPAssertionsTable(URI individual) {
		individualType = Janus.mappingMetadata.getIndividualType(individual);
		
		table = new JTable(new OPAssertionsTableModel(individual)) {
			@Override
			public String getToolTipText(MouseEvent event) {
				Point p = event.getPoint();
				int rowIndex = rowAtPoint(p);
		        int colIndex = columnAtPoint(p);
		        
		        String value = getValueAt(rowIndex, colIndex).toString();
		        
		        if (individualType.equals(OWLEntityTypes.RECORD_INDIVIDUAL)) {
		        	if (colIndex == 0)
		        		return getObjectProperty(value).toString();
		        	else if (colIndex == 1)
		        		return getIndividual(value).toString();
		        } else {
		        	if (colIndex == 0)
		        		return getIndividual(value).toString();
		        	else if (colIndex == 1)
		        		return getObjectProperty(value).toString();
		        }
		        
		        return super.getToolTipText();
			}
		};
		table.setDefaultRenderer(Object.class, 
				new OPAssertionsTableRenderer(new ImageIcon(ImageURIs.ONT_NAMED_OBJ_PROP), 
											  new ImageIcon(ImageURIs.ONT_INDIVIDUAL),
											  individualType));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDragEnabled(true);
		table.getTableHeader().setReorderingAllowed(false);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		setViewportView(table);
	}
	
	private URI getObjectProperty(String opFragment) {
		return Janus.mappingMetadata.getObjectProperty(opFragment);
	}
	
	private URI getIndividual(String individualFragment) {
		return Janus.mappingMetadata.getIndividual(individualFragment);
	}
}

@SuppressWarnings("serial")
class OPAssertionsTableModel extends AbstractTableModel {
	private OWLEntityTypes individualType;

	private int columnCount;
	private int rowCount;
	private CachedRecord cache;
	
	private SQLResultSet resultSet;

	OPAssertionsTableModel(URI individual) {
		individualType = Janus.mappingMetadata.getIndividualType(individual);
		
		String query = Janus.sqlGenerator.getQueryToGetOPAssertionsOfIndividual(individual);
		
		resultSet = Janus.dbBridge.executeQuery(query);
		
		rowCount = resultSet.getResultSetRowCount();
		columnCount = resultSet.getResultSetColumnCount();
		
		cache = new CachedRecord();
	}
	
	@Override
	public String getColumnName(int column) {
		String columnName = super.getColumnName(column);
		
		if (individualType.equals(OWLEntityTypes.RECORD_INDIVIDUAL)) {
			if (column == 0)
				columnName = "Object Property";
			else if (column == 1)
				columnName = "Target Individual";
		} else {
			if (column == 0)
				columnName = "Source Individual";
			else if (column == 1)
				columnName = "Object Property";
		}
		
		return columnName;
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
class OPAssertionsTableRenderer extends DefaultTableCellRenderer {
	private OWLEntityTypes individualType;
	
	private Icon opIcon;
	private Icon indIcon;
	
	OPAssertionsTableRenderer(Icon opIcon, Icon indIcon, OWLEntityTypes individualType) {
		this.individualType = individualType;
		
		this.opIcon = opIcon;
		this.indIcon = indIcon;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (individualType.equals(OWLEntityTypes.RECORD_INDIVIDUAL)) {
			if (column == 0)
				setIcon(opIcon);
			else if (column == 1)
				setIcon(indIcon);
		} else {
			if (column == 0)
				setIcon(indIcon);
			else if (column == 1)
				setIcon(opIcon);
		}
		
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
	}
}