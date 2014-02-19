package janus.application.dialog;

import janus.database.DBMSTypes;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class SessionManager extends JDialog {
	
	private JComboBox<DBMSTypes> DBMSType;
	private JTextField host;
	private JTextField port;
	private JTextField id;
	private JPasswordField password;
	private JTextField schema;
	private JTextField ontologyIRI;
	
	private boolean NORMAL_EXIT = false;

	public SessionManager(Frame owner) {
		super(owner, "Session Manager", true);

		buildUI();
	}

	private void buildUI() {
		JPanel basePanel = new JPanel();
		setContentPane(basePanel);
		basePanel.setLayout(null);
		
		// DBMS Type
		JLabel DBMSTypeLabel = new JLabel("DBMS Type: ", SwingConstants.RIGHT);
		DBMSTypeLabel.setBounds(10, 10, 100, 25);
		basePanel.add(DBMSTypeLabel);
		DBMSType = new JComboBox<DBMSTypes>();
		DBMSType.addItem(DBMSTypes.MARIADB);
		DBMSType.setBounds(120, 10, 120, 25);
		basePanel.add(DBMSType);

		// Server Host
		JLabel hostLabel = new JLabel("Server Host: ", SwingConstants.RIGHT);
		hostLabel.setBounds(10, 40, 100, 25);
		basePanel.add(hostLabel);
		host = new JTextField("localhost");
		host.setBounds(120, 40, 120, 25);
		basePanel.add(host);

		// Port
		JLabel portLabel = new JLabel("Port: ", SwingConstants.RIGHT);
		portLabel.setBounds(250, 40, 30, 25);
		basePanel.add(portLabel);
		port = new JTextField("3306");
		port.setBounds(290, 40, 40, 25);
		basePanel.add(port);

		// ID
		JLabel idLabel = new JLabel("Username: ", SwingConstants.RIGHT);
		idLabel.setBounds(10, 70, 100, 25);
		basePanel.add(idLabel);
		id = new JTextField("root");
		id.setBounds(120, 70, 120, 25);
		basePanel.add(id);

		// Password
		JLabel passwordLabel = new JLabel("Password: ", SwingConstants.RIGHT);
		passwordLabel.setBounds(10, 100, 100, 25);
		basePanel.add(passwordLabel);
		password = new JPasswordField();
		password.setBounds(120, 100, 120, 25);
		basePanel.add(password);

		// Default Schema
		JLabel defaultSchemaLabel = new JLabel("Schema: ", SwingConstants.RIGHT);
		defaultSchemaLabel.setBounds(10, 130, 100, 25);
		basePanel.add(defaultSchemaLabel);
		schema = new JTextField("college");
		schema.setBounds(120, 130, 120, 25);
		basePanel.add(schema);
		
		// ontology IRI
		JLabel ontologyIRILabel = new JLabel("Ontology IRI: ", SwingConstants.RIGHT);
		ontologyIRILabel.setBounds(10, 160, 100, 25);
		basePanel.add(ontologyIRILabel);
		ontologyIRI = new JTextField("<http://www.example.com/college>");
		ontologyIRI.setBounds(120, 160, 210, 25);
		basePanel.add(ontologyIRI);

		// OK
		JButton OK = new JButton("OK");
		OK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NORMAL_EXIT = true;
				setVisible(false);
			}
		});
		getRootPane().setDefaultButton(OK);
		OK.setBounds(90,190,80,25);
		basePanel.add(OK);

		// Cancel
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		cancel.setBounds(180,190,80,25);
		basePanel.add(cancel);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 370) >> 1, (screenSize.height- 255) >> 1, 370, 255);
		
		setResizable(false);
	}
	
	public DBMSTypes getDBMSType() { return DBMSTypes.MARIADB; }
	public String getHost() { return host.getText().trim(); }
	public String getPort() { return port.getText().trim(); }
	public String getID() { return id.getText().trim(); }
	public String getPassword() { return new String(password.getPassword()).trim(); }
	public String getSchema() { return schema.getText().trim(); }
	
	public String getOntologyIRI() { 
		String iri = ontologyIRI.getText().trim();
		iri = iri.substring(iri.indexOf("<")+1, iri.indexOf(">"));
		
		return iri; 
	}
	
	public boolean isNormalExit() { return NORMAL_EXIT; }
	
	public void setVisible(boolean b) {
		if(b) NORMAL_EXIT = false;
		
		super.setVisible(b);
	}
}