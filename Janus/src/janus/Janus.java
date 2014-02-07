package janus;

import java.io.File;

import janus.application.DBLoginDialog;
import janus.application.JanusWindow;
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
	
	public static final String DEFAULT_PARENT_PATH_FOR_ONT_NAMESPACE = "http://cosmos.ssu.ac.kr/ontologies/";
	
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
		
		DBLoginDialog loginDialog = new DBLoginDialog(splash);
		
		do {
			loginDialog.setVisible(true);
			
			if(!loginDialog.isNormalExit()) 
				System.exit(0);

			dbBridge = DBBridgeFactory.getDBBridge(loginDialog.getDBMSType(),
															loginDialog.getHost(),
															loginDialog.getPort(),
															loginDialog.getID(),
															loginDialog.getPassword(),
															loginDialog.getSchema());
			
			if(!dbBridge.isConnected()) 
				JOptionPane.showMessageDialog(splash, "Could not connect to the DBMS.", 
											  "Janus Error", JOptionPane.ERROR_MESSAGE);
			else 
				break;
		} while(true);
		
		sqlGenerator = SQLGeneratorFactory.getSQLGenerator(loginDialog.getDBMSType());
		
		loginDialog.dispose();
		
		Janus.cachedDBMetadata = CachedDBMetadataFactory.generateLocalDatabaseMetaData();
		
		ontMapper = new OntMapper();
		
		File ontFile = new File(Janus.DEFAULT_DIR_FOR_TBOX_FILE + dbBridge.getConnectedCatalog() + ".owl");
		if (!ontFile.exists())
			ontFile = ontMapper.generateTBoxFile();
			
		
		long start = System.currentTimeMillis();
		
		ontBridge = OntBridgeFactory.getOntBridge(ontFile, ReasonerType.PELLET_REASONER);
		
		long end = System.currentTimeMillis();
		System.out.println( "로딩 및 추론 시간 : " + ( end - start));
		
		//OntBridge ontBridge = OntBridgeFactory.getOntBridge("http://www.co-ode.org/ontologies/pizza/2007/02/12/pizza.owl");
		//OntBridge ontBridge = OntBridgeFactory.getOntBridge("http://cosmos.ssu.ac.kr/janus/schooling.owl");
		//OntBridge ontBridge = OntBridgeFactory.getOntBridge("http://protege.cim3.net/file/pub/ontologies/travel/travel.owl");
		//OntBridge ontBridge = OntBridgeFactory.getOntBridge("http://protege.cim3.net/file/pub/ontologies/wine/wine.owl", ReasonerType.PELLET_REASONER);
		//OntBridge ontBridge = OntBridge.getInstance("http://protege.cim3.net/file/pub/ontologies/tambis/tambis-full.owl");
		//OntBridge ontBridge = OntBridge.getInstance("http://protege.cim3.net/file/pub/ontologies/shuttle/shuttle-crew-ont.owl");
		//OntBridge ontBridge = OntBridge.getInstance("http://protege.cim3.net/file/pub/ontologies/people.pets/people+pets.owl");
		//OntBridge ontBridge = OntBridge.getInstance("http://protege.cim3.net/file/pub/ontologies/not.galen/not-galen.owl");
		//OntBridge ontBridge = OntBridge.getInstance("http://protege.cim3.net/file/pub/ontologies/koala/koala.owl"); //DIFF
		//OntBridge ontBridge = OntBridge.getInstance("http://protege.cim3.net/file/pub/ontologies/ka/ka.owl"); //DIFF
		//OntBridge ontBridge = OntBridge.getInstance("http://protege.cim3.net/file/pub/ontologies/generations/generations.owl"); //DIFF
		//OntBridge ontBridge = OntBridge.getInstance("http://protege.cim3.net/file/pub/ontologies/family.swrl.owl/family.swrl.owl");  //ERROR
		//OntBridge ontBridge = OntBridge.getInstance("http://protege.cim3.net/file/pub/ontologies/camera/camera.owl"); //OK
		
		//RuleBridge ruleBridge = RuleBridgeFactory.getRuleBridge("http://protege.stanford.edu/plugins/owl/testdata/importSWRL.owl");

		Janus.mappingMetadata = MappingMetadataFactory.generateMappingMetaData();
		
		JanusWindow mainWindow = new JanusWindow();
		splash.dispose();
		mainWindow.setVisible(true);
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