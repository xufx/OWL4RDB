package janus;

import java.io.File;
import java.net.URI;

import janus.application.JanusWindow;
import janus.application.dialog.SessionManager;
import janus.database.DBBridge;
import janus.database.DBBridgeFactory;
import janus.database.metadata.CachedDBMetadataFactory;
import janus.database.metadata.CachedDBMetadata;
import janus.mapping.OntMapper;
import janus.mapping.metadata.MappingMetadataFactory;
import janus.mapping.metadata.MappingMetadata;
import janus.ontology.OntBridge;
import janus.ontology.OntBridgeFactory;
import janus.ontology.ReasonerType;
import janus.query.SQLGenerator;
import janus.query.SQLGeneratorFactory;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Janus {
	public static final String DEFAULT_DIR_FOR_TBOX_FILE = "./ontologies/";
	public static final String DEFAULT_DIR_FOR_DUMP_FILE = "./ontologies/dump/";
	
	public static String ontologyURI;
	
	public static DBBridge dbBridge;
	public static OntBridge ontBridge;
	public static OntMapper ontMapper;
	public static CachedDBMetadata cachedDBMetadata;
	public static MappingMetadata mappingMetadata;
	public static SQLGenerator sqlGenerator;
	
	public static void main (String[] args) {
		Splash splash = new Splash(ImageURIs.SPLASH, ImageURIs.LOGO);
		splash.setVisible(true);
		
		setLookAndFeel();
		
		SessionManager sessionManager = new SessionManager(splash);
		
		do {
			sessionManager.setVisible(true);
			
			if(!sessionManager.isNormalExit()) 
				System.exit(0);

			dbBridge = DBBridgeFactory.getDBBridge(sessionManager.getDBMSType(),
															sessionManager.getHost(),
															sessionManager.getPort(),
															sessionManager.getID(),
															sessionManager.getPassword(),
															sessionManager.getSchema());
			
			if(!dbBridge.isConnected()) 
				JOptionPane.showMessageDialog(splash, "Could not connect to the DBMS.", 
											  "Janus Error", JOptionPane.ERROR_MESSAGE);
			else {
				ontologyURI = sessionManager.getOntologyIRI();
				break;
			}
			
		} while(true);
		
		sqlGenerator = SQLGeneratorFactory.getSQLGenerator(sessionManager.getDBMSType());
		
		Janus.cachedDBMetadata = CachedDBMetadataFactory.generateLocalDatabaseMetaData();
		
		ontMapper = new OntMapper();
		
		File ontFile = new File(Janus.DEFAULT_DIR_FOR_TBOX_FILE + dbBridge.getConnectedCatalog() + ".owl");
		if (!ontFile.exists())
			ontFile = ontMapper.generateTBoxFile();
			
		
		long start = System.currentTimeMillis();
		
		ontBridge = OntBridgeFactory.getOntBridge(ontFile, ReasonerType.PELLET_REASONER);
		
		long end = System.currentTimeMillis();
		System.out.println( "loading and reasoning time : " + ( end - start));

		Janus.mappingMetadata = MappingMetadataFactory.generateMappingMetaData();
		
		JanusWindow mainWindow = new JanusWindow(sessionManager.getSchema() + " <" + ontologyURI + ">");
		sessionManager.dispose();
		splash.dispose();
		mainWindow.setVisible(true);
		
		//System.out.println(Janus.sqlGenerator.getQueryToGetSourceIndividualsOfOPAssertion(URI.create("http://www.example.com/college#op_course.id"), URI.create("http://www.example.com/college#t=course&c=id&v=C0")));
		
		//System.out.println(Janus.sqlGenerator.getQueryToGetTargetIndividualsOfOPAssertion(URI.create("http://www.example.com/college#op_course.id"), URI.create("http://www.example.com/college#t=course&k=id&v=C0")));
	
		System.out.println(Janus.sqlGenerator.getQueryToGetObjectPropertiesOfOPAssertion(URI.create("http://www.example.com/college#t=person&k=id&v=P2"), URI.create("http://www.example.com/college#t=person&c=id&v=P2")));
	}
	
	private static void setLookAndFeel() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager
							.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
			}
		});
	}
}