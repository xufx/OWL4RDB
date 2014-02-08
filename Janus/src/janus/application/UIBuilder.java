package janus.application;

import janus.ImageURIs;
import janus.Janus;
import janus.TabNames;
import janus.application.actions.BuildTBoxAction;
import janus.application.actions.DumpKnowledgeBaseAction;
import janus.application.actions.RunAction;
import janus.application.actions.ShowAtomsAction;
import janus.application.actions.ShowDocumentAction;
import janus.application.dbscheme.DBTree;
import janus.application.description.DescrTree;
import janus.application.ontdata.IndividualList;
import janus.application.ontscheme.ClsTree;
import janus.application.ontscheme.DPTree;
import janus.application.ontscheme.OPTree;
import janus.application.query.QueryArea;
import janus.application.query.QueryTypes;
import janus.application.query.ResultSetPane;
import janus.ontology.RenderingType;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

class UIBuilder {
	static void buildUI(JFrame window) {
		UIRegistry.registerWindow(window);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int frameWidth = screenSize.width / 3 * 2;
		int frameHeight = screenSize.height / 3 * 2;
		window.setBounds((screenSize.width - frameWidth) >> 1, (screenSize.height - frameHeight) >> 1, frameWidth, frameHeight);
		
		window.setIconImage(new ImageIcon(ImageURIs.LOGO).getImage());
		
		window.getContentPane().add(buildBaseSplitPane(), BorderLayout.CENTER);
		
		window.getContentPane().add(buildToolBar(), BorderLayout.NORTH);
		
		window.setTitle("Janus");
	}
	
	private static JToolBar buildToolBar() {
		JToolBar toolBar = new JToolBar();
		
		JButton translate = toolBar.add(new BuildTBoxAction());
		translate.setIcon(new ImageIcon(ImageURIs.TOOLBAR_GENERATE_TBOX));
		translate.setToolTipText("Build TBOX");
		
		JButton run = toolBar.add(new RunAction());
		run.setIcon(new ImageIcon(ImageURIs.TOOLBAR_RUN));
		run.setToolTipText("Run Query");
		
		JButton dump = toolBar.add(new DumpKnowledgeBaseAction());
		dump.setIcon(new ImageIcon(ImageURIs.TOOLBAR_DUMP));
		dump.setToolTipText("Dump DB into OWL");
		
		JButton showAtoms = toolBar.add(new ShowAtomsAction());
		showAtoms.setIcon(new ImageIcon(ImageURIs.TOOLBAR_SHOW_ATOMS));
		showAtoms.setToolTipText("Show Atoms");
		
		JButton showDoc = toolBar.add(new ShowDocumentAction());
		showDoc.setIcon(new ImageIcon(ImageURIs.TOOLBAR_SHOW_DOC));
		showDoc.setToolTipText("Show Document");
		
		return toolBar;
	}
	
	private static JSplitPane buildBaseSplitPane() {
		JSplitPane baseSP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		baseSP.setResizeWeight(0.5);
		baseSP.setOneTouchExpandable(true);
		
		JTabbedPane schemeTab = new JTabbedPane(JTabbedPane.TOP);
		schemeTab.addTab(TabNames.DATABASE, buildDBTreePane());
		schemeTab.addTab(TabNames.ONTOLOGY, buildOntologyTreePane());
		
		baseSP.setLeftComponent(schemeTab);
		
		JSplitPane rightSP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		rightSP.setResizeWeight(0.5);
		rightSP.setOneTouchExpandable(true);
		
		JTabbedPane displayTab = new JTabbedPane(JTabbedPane.TOP);
		displayTab.addTab(TabNames.DOCUMENT, buildDocumentPane());
		displayTab.addTab(TabNames.INDIVIDUALS, buildIndividualsPane());
		displayTab.addTab(TabNames.QUERY, buildQueryPane());
		
		rightSP.setLeftComponent(displayTab);
		
		JTabbedPane descrTab = new JTabbedPane(JTabbedPane.TOP);
		descrTab.addTab(TabNames.DESCRIPTION, buildOntDescrPane());
		
		rightSP.setRightComponent(descrTab);
		
		baseSP.setRightComponent(rightSP);
		
		baseSP.setDividerLocation((int)(UIRegistry.getWindow().getWidth() / 4));
		
		return baseSP;
	}
	
	private static JSplitPane buildIndividualsPane() {
		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		sp.setResizeWeight(0.5);
		sp.setOneTouchExpandable(true);
		
		sp.setLeftComponent(buildIndividualListPane());
		
		//sp.setBottomComponent(buildSQLResultSetPane());
		
		return sp;
	}
	
	private static JScrollPane buildIndividualListPane() {
		JScrollPane sp = new IndividualList();
		
		UIRegistry.registerIndividualPane(sp);
		
		return sp;
	}
	
	private static JSplitPane buildQueryPane() {
		JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		sp.setResizeWeight(0.5);
		sp.setOneTouchExpandable(true);
		
		sp.setTopComponent(buildQueryStatementPane());
		
		sp.setBottomComponent(buildSQLResultSetPane());
		
		return sp;
	}
	
	private static JTabbedPane buildDocumentPane() {
		JTabbedPane tp = new JTabbedPane(JTabbedPane.TOP);
		
		UIRegistry.registerDocumentPane(tp);
		
		return tp;
	}
	
	private static JScrollPane buildSQLResultSetPane() {
		JScrollPane resultSetPane = new ResultSetPane();
		
		UIRegistry.registerQueryResultPane(resultSetPane);
		
		return resultSetPane;
	}
	
	private static JTabbedPane buildQueryStatementPane() {
		JTabbedPane queryTabs = new JTabbedPane(JTabbedPane.TOP);
		queryTabs.addTab(TabNames.SPARQL_DL, new QueryArea(QueryTypes.SPARQL_DL));
		queryTabs.addTab(TabNames.SQL, new QueryArea(QueryTypes.SQL));
		
		UIRegistry.registerQueryPane(queryTabs);
		
		return queryTabs;
	}
	
	private static JScrollPane buildOntDescrPane() {
		JScrollPane descrTree = new DescrTree();
		
		UIRegistry.registerDescriptionPane(descrTree);
		
		return descrTree;
	}
	
	private static JSplitPane buildOntologyTreePane() {
		JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		sp.setResizeWeight(0.5);
		sp.setOneTouchExpandable(true);
		
		JTabbedPane clsTreeTab = new JTabbedPane(JTabbedPane.TOP);
		clsTreeTab.addTab(TabNames.CLASSES, buildOntClassTreePane());
		
		sp.setTopComponent(clsTreeTab);
		
		JTabbedPane propertiesTab = new JTabbedPane(JTabbedPane.TOP);
		propertiesTab.addTab(TabNames.OBJECT_PROPERTIES, buildOntObjPropertyTreePane());
		propertiesTab.addTab(TabNames.DATA_PROPERTIES, buildOntDataPropertyTreePane());
		
		sp.setBottomComponent(propertiesTab);
		
		return sp;
	}

	private static JScrollPane buildDBTreePane() {
		JScrollPane dbTree = new DBTree();
		
		UIRegistry.registerDBSchemePane(dbTree);
		
		return dbTree;
	}
	
	private static JScrollPane buildOntClassTreePane() {
		JScrollPane clsTree = new ClsTree();
		
		UIRegistry.registerClassPane(clsTree);
		
		return clsTree;
	}
	
	private static JScrollPane buildOntObjPropertyTreePane() {
		JScrollPane opTree = new OPTree();
		
		UIRegistry.registerObjectPropertyPane(opTree);
		
		return opTree;
	}
	
	private static JScrollPane buildOntDataPropertyTreePane() {
		JScrollPane dpTree = new DPTree();
		
		UIRegistry.registerDataPropertyPane(dpTree);
		
		return dpTree;
	}
}
