package janus.application;

import janus.application.dbscheme.DBTree;
import janus.application.description.DescrTree;
import janus.application.ontscheme.ClsTree;
import janus.application.ontscheme.DPTree;
import janus.application.ontscheme.OPTree;
import janus.application.query.Showable;
import janus.application.query.Submittable;

import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class UIRegistry {
	private static JScrollPane classPane;
	private static JScrollPane dataPropertyPane;
	private static JScrollPane dbSchemePane;
	private static JScrollPane descriptionPane;
	private static JTabbedPane displayPane;
	private static JTabbedPane documentPane;
	private static JFrame frame;
	private static JTabbedPane individualsPane;
	private static JScrollPane objectPropertyPane;
	private static JTabbedPane queryPane;
	private static JScrollPane queryResultPane;
	
	
	public static JTabbedPane getIndividualsPane() {
		return individualsPane;
	}
	
	public static JTabbedPane getDisplayPane() {
		return displayPane;
	}
	
	public static JTabbedPane getDocumentPane() {
		return documentPane;
	}
	
	public static ClsTree getClsTreePane() {
		return (ClsTree)classPane;
	}
	
	static DPTree getDataPropTree() {
		return (DPTree)dataPropertyPane;
	}
	
	static DBTree getDBTree() {
		return (DBTree)dbSchemePane;
	}
	
	static DescrTree getDescriptionTree() {
		return (DescrTree)descriptionPane;
	}
	
	public static Window getDialogOwner() { return frame; }
	
	static OPTree getObjPropTree() {
		return (OPTree)objectPropertyPane;
	}
	
	public static Showable getQueryResultPane() {
		return (Showable)queryResultPane;
	}
	
	public static Submittable getSelectedQueryPane() {
		return (Submittable)queryPane.getSelectedComponent();
	}
	
	static JFrame getWindow() { return frame; }
	
	static void registerDisplayPane(JTabbedPane displayPane) {
		UIRegistry.displayPane = displayPane;
	}
	
	static void registerDocumentPane(JTabbedPane documentPane) {
		UIRegistry.documentPane = documentPane;
	}
	
	static void registerClassPane(JScrollPane classPane) {
		UIRegistry.classPane = classPane;
	}
	
	static void registerDataPropertyPane(JScrollPane dataPropertyPane) {
		UIRegistry.dataPropertyPane = dataPropertyPane;
	}
	
	static void registerDBSchemePane(JScrollPane dbSchemePane) {
		UIRegistry.dbSchemePane = dbSchemePane;
	}
	
	static void registerDescriptionPane(JScrollPane descriptionPane) {
		UIRegistry.descriptionPane = descriptionPane;
	}
	
	static void registerIndividualsPane(JTabbedPane individualsPane) {
		UIRegistry.individualsPane = individualsPane;
	}
	
	static void registerObjectPropertyPane(JScrollPane objectPropertyPane) {
		UIRegistry.objectPropertyPane = objectPropertyPane;
	}
	
	static void registerQueryPane(JTabbedPane queryPane) {
		UIRegistry.queryPane = queryPane;
	}
	
	static void registerQueryResultPane(JScrollPane queryResultPane) {
		UIRegistry.queryResultPane = queryResultPane;
	}
	
	static void registerWindow(JFrame frame) {
		UIRegistry.frame = frame;
	}
}