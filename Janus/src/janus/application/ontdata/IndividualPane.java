package janus.application.ontdata;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;

@SuppressWarnings("serial")
public class IndividualPane extends JSplitPane {
	private JList list;
	
	public IndividualPane() {
		super(JSplitPane.VERTICAL_SPLIT, true);
		setResizeWeight(0.5);
		setOneTouchExpandable(true);
		
		buildUI();
	}
	
	private void buildUI() {
		
		
		
		
		
		
		
		
		
		list = new JList();
		list.setDragEnabled(true);
		
		list.setCellRenderer(new IndividualListCellRenderer());
		
		setViewportView(list);
	}

}

@SuppressWarnings("serial")
class IndividualListCellRenderer extends JLabel implements ListCellRenderer {
    IndividualListCellRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        setText(value.toString());

        Color background;
        Color foreground;

        // check if this cell represents the current DnD drop location
        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index) {

            background = Color.BLUE;
            foreground = Color.WHITE;

        // check if this cell is selected
        } else if (isSelected) {
            background = Color.RED;
            foreground = Color.WHITE;

        // unselected, and not the DnD drop location
        } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        };

        setBackground(background);
        setForeground(foreground);

        return this;
    }
}
