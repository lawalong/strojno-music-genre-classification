/*
 * CheckListRenderer.java
 *
 * Created on October 5, 2007, 6:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.meapsoft.gui.widgets;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Mike
 */
public class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer
{
    
    /** Creates a new instance of CheckListRenderer */
    public CheckBoxListRenderer()
    {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        //render the text of the checkbox here
        JCheckBox kBox = (JCheckBox)value;
        
        //set all rendering things here!
        setSelected(kBox.isSelected());
        setText(kBox.getText());
        setBackground(isSelected ? new Color(49,106,197) : Color.white);
        setForeground(isSelected ? Color.white : Color.black);
        return this;
    }
    
}
