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
import janus.mapping.OntEntity;
import janus.mapping.OntEntityTypes;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
class SubOPAssertionsTable extends JScrollPane {
	private JTable table;
	
	SubOPAssertionsTable(URI individual) {
		table = new JTable(new SubOPAssertionsTableModel(individual)) {
			@Override
			public String getToolTipText(MouseEvent event) {
				Point p = event.getPoint();
				int rowIndex = rowAtPoint(p);
		        int colIndex = columnAtPoint(p);
		        
		        String curie = getValueAt(rowIndex, colIndex).toString();
		        
		        return OntEntity.getURI(curie).toString();
			}
		};
		table.setDefaultRenderer(Object.class, 
				new SubOPAssertionsTableRenderer(new ImageIcon(ImageURIs.ONT_NAMED_OBJ_PROP), 
											  new ImageIcon(ImageURIs.ONT_INDIVIDUAL)));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDragEnabled(true);
		table.getTableHeader().setReorderingAllowed(false);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		setViewportView(table);
	}
}

@SuppressWarnings("serial")
class SubOPAssertionsTableModel extends AbstractTableModel {
	private OntEntityTypes individualType;

	private int columnCount;
	private int rowCount;
	private CachedRecord cache;
	
	private SQLResultSet resultSet;

	SubOPAssertionsTableModel(URI individual) {
		individualType = Janus.mappingMetadata.getIndividualType(individual);
		
		if (individualType.equals(OntEntityTypes.RECORD_INDIVIDUAL)) {
			String query = Janus.sqlGenerator.getQueryToGetOPAssertionsOfIndividual(individual, "'Object Property'", "'Target Individual'");
			resultSet = Janus.dbBridge.executeQuery(query);
		} else if (individualType.equals(OntEntityTypes.FIELD_INDIVIDUAL)) {
			String query = Janus.sqlGenerator.getQueryToGetOPAssertionsOfIndividual(individual, "'Object Property'", "'Source Individual'");
			resultSet = Janus.dbBridge.executeQuery(query);
		}
		
		rowCount = resultSet.getResultSetRowCount();
		columnCount = resultSet.getResultSetColumnCount();
		
		cache = new CachedRecord();
	}
	
	@Override
	public String getColumnName(int column) {
		int index = column + 1;
		
		if (column == 0 || column == 1)
			return resultSet.getColumnName(index);
		
		return super.getColumnName(index);
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
class SubOPAssertionsTableRenderer extends DefaultTableCellRenderer {
	private Icon opIcon;
	private Icon indIcon;
	
	SubOPAssertionsTableRenderer(Icon opIcon, Icon indIcon) {
		this.opIcon = opIcon;
		this.indIcon = indIcon;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (column == 0)
			setIcon(opIcon);
		else if (column == 1)
			setIcon(indIcon);
		
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
	}
}