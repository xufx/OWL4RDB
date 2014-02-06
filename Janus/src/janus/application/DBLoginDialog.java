package janus.application;

import janus.database.DBMSTypes;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class DBLoginDialog extends JDialog {
	
	private JTextField host;
	private JTextField port;
	private JTextField id;
	private JPasswordField password;
	private JTextField schema;
	
	private boolean NORMAL_EXIT = false;

	public DBLoginDialog(Frame owner) {
		super(owner, "Connect to Database", true);

		buildUI();
	}

	private void buildUI() {
		JPanel basePanel = new JPanel();
		setContentPane(basePanel);
		basePanel.setLayout(null);

		// Server Host
		JLabel hostLabel = new JLabel("Server Host: ", SwingConstants.RIGHT);
		hostLabel.setBounds(10, 10, 100, 22);
		basePanel.add(hostLabel);
		host = new JTextField("localhost");
		//host = new JTextField("mydbinstance.codvpbjzdi5o.ap-northeast-1.rds.amazonaws.com");
		host.setBounds(120, 10, 120, 22);
		basePanel.add(host);

		// Port
		JLabel portLabel = new JLabel("Port: ", SwingConstants.RIGHT);
		portLabel.setBounds(250, 10, 30, 22);
		basePanel.add(portLabel);
		port = new JTextField("3306");
		port.setBounds(290, 10, 40, 22);
		basePanel.add(port);

		// ID
		JLabel idLabel = new JLabel("Username: ", SwingConstants.RIGHT);
		idLabel.setBounds(10, 40, 100, 22);
		basePanel.add(idLabel);
		id = new JTextField("root");
		//id = new JTextField("awsuser");
		id.setBounds(120, 40, 120, 22);
		basePanel.add(id);

		// Password
		JLabel passwordLabel = new JLabel("Password: ", SwingConstants.RIGHT);
		passwordLabel.setBounds(10, 70, 100, 22);
		basePanel.add(passwordLabel);
		password = new JPasswordField("chlgodls");
		//password = new JPasswordField("apmsetup");
		//password = new JPasswordField("mypassword");
		password.setBounds(120, 70, 120, 22);
		basePanel.add(password);

		// Default Schema
		JLabel defaultSchemaLabel = new JLabel("Schema: ", SwingConstants.RIGHT);
		defaultSchemaLabel.setBounds(10, 100, 100, 22);
		basePanel.add(defaultSchemaLabel);
		schema = new JTextField("college");
		//schema = new JTextField("classicmodels");
		//schema = new JTextField("university");
		schema.setBounds(120, 100, 120, 22);
		basePanel.add(schema);

		// OK
		JButton OK = new JButton("OK");
		OK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NORMAL_EXIT = true;
				setVisible(false);
			}
		});
		OK.setBounds(90,130,80,22);
		basePanel.add(OK);

		// Cancel
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		cancel.setBounds(180,130,80,22);
		basePanel.add(cancel);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 345) >> 1, (screenSize.height- 192) >> 1, 345, 192);
		
		setResizable(false);
	}
	
	public DBMSTypes getDBMSType() { return DBMSTypes.MARIADB; }
	public String getHost() { return host.getText().trim(); }
	public String getPort() { return port.getText().trim(); }
	public String getID() { return id.getText().trim(); }
	public String getPassword() { return new String(password.getPassword()).trim(); }
	public String getSchema() { return schema.getText().trim(); }
	
	public boolean isNormalExit() { return NORMAL_EXIT; }
	
	public void setVisible(boolean b) {
		if(b) NORMAL_EXIT = false;
		
		super.setVisible(b);
	}
}