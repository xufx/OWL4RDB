package janus.application.ontdata;

import java.net.URI;

import janus.TabNames;
import janus.mapping.OntEntity;
import janus.mapping.OntEntityTypes;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class AssertionsPane extends JSplitPane implements ListSelectionListener {
	private OntEntity ontEntity;
	
	private ClsAssertionsTable clsAssertionsTable;
	private DPAssertionsTable dpAssertionsTable;
	private OPAssertionsTable opAssertionsTable;
	private JTabbedPane types;
	private JTabbedPane subOPAssertions;
	private JTabbedPane subDPAssertions;
	
	public AssertionsPane(OntEntity ontEntity) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		
		this.ontEntity = ontEntity;
		
		setResizeWeight(0.5);
		setOneTouchExpandable(true);
		
		buildUI();
	}
	
	private void buildUI() {
		JTabbedPane entityAssertions = new JTabbedPane();
		if (ontEntity.getType().equals(OntEntityTypes.TABLE_CLASS) 
				|| ontEntity.getType().equals(OntEntityTypes.COLUMN_CLASS)
				|| ontEntity.getType().equals(OntEntityTypes.OWL_THING_CLASS))
			entityAssertions.addTab(TabNames.CLASS_ASSERTIONS, buildClsAssertionsTable());
		else if (ontEntity.getType().equals(OntEntityTypes.DATA_PROPERTY))
			entityAssertions.addTab(TabNames.DATA_PROPERTY_ASSERTIONS, buildDPAssertionsTable());
		else if (ontEntity.getType().equals(OntEntityTypes.OBJECT_PROPERTY))
			entityAssertions.addTab(TabNames.OBJECT_PROPERTY_ASSERTIONS, buildOPAssertionsTable());
		setLeftComponent(entityAssertions);
		
		JSplitPane rightSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		rightSP.setResizeWeight(0.5);
		rightSP.setOneTouchExpandable(true);
		
		types = new JTabbedPane();
		types.addTab(TabNames.TYPES, null);
		
		rightSP.setTopComponent(types);
		
		JSplitPane rightBottomSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		rightBottomSP.setResizeWeight(0.5);
		rightBottomSP.setOneTouchExpandable(true);
		
		subOPAssertions = new JTabbedPane();
		subOPAssertions.addTab(TabNames.OBJECT_PROPERTY_ASSERTIONS, null);
		
		rightBottomSP.setTopComponent(subOPAssertions);
		
		subDPAssertions = new JTabbedPane();
		subDPAssertions.addTab(TabNames.DATA_PROPERTY_ASSERTIONS, null);
		
		rightBottomSP.setBottomComponent(subDPAssertions);
		
		rightSP.setBottomComponent(rightBottomSP);
		
		setRightComponent(rightSP);
	}
	
	private JScrollPane buildClsAssertionsTable() {
		clsAssertionsTable = new ClsAssertionsTable(ontEntity.getURI());
		clsAssertionsTable.addClsAssertionsTableSelectionListener(this);
		
		return clsAssertionsTable;
	}
	
	private JScrollPane buildDPAssertionsTable() {
		dpAssertionsTable = new DPAssertionsTable(ontEntity.getURI());
		dpAssertionsTable.addDPAssertionsTableSelectionListener(this);
		
		return dpAssertionsTable;
	}
	
	private JScrollPane buildOPAssertionsTable() {
		opAssertionsTable = new OPAssertionsTable(ontEntity.getURI());
		opAssertionsTable.addOPAssertionsTableSelectionListener(this);
		
		return opAssertionsTable;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		URI selectedIndividual = null;

		if (ontEntity.getType().equals(OntEntityTypes.TABLE_CLASS) 
				|| ontEntity.getType().equals(OntEntityTypes.COLUMN_CLASS)
				|| ontEntity.getType().equals(OntEntityTypes.OWL_THING_CLASS))
			selectedIndividual = clsAssertionsTable.getSelectedIndividual();
		else if (ontEntity.getType().equals(OntEntityTypes.DATA_PROPERTY))
			selectedIndividual = dpAssertionsTable.getSelectedIndividual();
		else if (ontEntity.getType().equals(OntEntityTypes.OBJECT_PROPERTY))
			selectedIndividual = opAssertionsTable.getSelectedIndividual();
		
		if (selectedIndividual != null) {
			types.setComponentAt(types.indexOfTab(TabNames.TYPES), new TypesTable(selectedIndividual));
			subOPAssertions.setComponentAt(subOPAssertions.indexOfTab(TabNames.OBJECT_PROPERTY_ASSERTIONS), new SubOPAssertionsTable(selectedIndividual));
			subDPAssertions.setComponentAt(subDPAssertions.indexOfTab(TabNames.DATA_PROPERTY_ASSERTIONS), new SubDPAssertionsTable(selectedIndividual));
		}
	}
}