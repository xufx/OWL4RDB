package janus.application.ontdata;

import java.net.URI;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

@SuppressWarnings("serial")
public class IndividualPane extends JSplitPane {
	private JScrollPane individualList;
	
	public IndividualPane(URI cls) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		setResizeWeight(0.5);
		setOneTouchExpandable(true);
		
		buildUI(cls);
	}
	
	private void buildUI(URI cls) {
		individualList = new IndividualList(cls);
		
		setLeftComponent(individualList);
	}
}