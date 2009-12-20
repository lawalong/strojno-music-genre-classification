package com.meapsoft.gui.widgets;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Tells us how to render these ComposerLabels in a JList
 * 
 *
 * @author Mike Sorvillo (ms3311@columbia.edu)
 * and the MEAP team
 */
public class ComposerListRenderer extends JLabel implements ListCellRenderer
{

    public ComposerListRenderer()
    {
        setOpaque(true);
    }
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
	{
        //render the text of the checkbox here
        JLabel lbl = (JLabel)value;
        
        //set all rendering things here!
        setText(lbl.getText());
        setBackground(isSelected ? new Color(49,106,197) : Color.white);
        setForeground(isSelected ? Color.white : Color.black);
        return this;
	}

}
