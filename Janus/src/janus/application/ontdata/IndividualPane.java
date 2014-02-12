package janus.application.ontdata;

import janus.TabNames;

import java.net.URI;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class IndividualPane extends JSplitPane implements ListSelectionListener {
	private URI cls;
	
	private MembersTable membersTable;
	private JTabbedPane types;
	private JTabbedPane opAssertions;
	private JTabbedPane dpAssertions;
	
	public IndividualPane(URI cls) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		
		this.cls = cls;
		
		setResizeWeight(0.5);
		setOneTouchExpandable(true);
		
		buildUI();
	}
	
	private void buildUI() {
		JTabbedPane members = new JTabbedPane();
		members.addTab(TabNames.MEMBERS, buildMembersPane());
		
		setLeftComponent(members);
		
		JSplitPane rightSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		rightSP.setResizeWeight(0.5);
		rightSP.setOneTouchExpandable(true);
		
		types = new JTabbedPane();
		types.addTab(TabNames.TYPES, null);
		
		rightSP.setTopComponent(types);
		
		JSplitPane rightBottomSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		rightBottomSP.setResizeWeight(0.5);
		rightBottomSP.setOneTouchExpandable(true);
		
		opAssertions = new JTabbedPane();
		opAssertions.addTab(TabNames.OBJECT_PROPERTY_ASSERTIONS, null);
		
		rightBottomSP.setTopComponent(opAssertions);
		
		dpAssertions = new JTabbedPane();
		dpAssertions.addTab(TabNames.DATA_PROPERTY_ASSERTIONS, null);
		
		rightBottomSP.setBottomComponent(dpAssertions);
		
		rightSP.setBottomComponent(rightBottomSP);
		
		setRightComponent(rightSP);
	}
	
	private JScrollPane buildMembersPane() {
		membersTable = new MembersTable(cls);
		membersTable.addMembersTableSelectionListener(this);
		
		return membersTable;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		new TypesTable(membersTable.getSelectedMember());
	}
}