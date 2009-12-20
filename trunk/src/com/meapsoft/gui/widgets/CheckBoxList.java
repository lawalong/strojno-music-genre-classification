/*
 * CheckBoxList.java
 *
 * Created on October 5, 2007, 7:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.meapsoft.gui.widgets;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.meapsoft.gui.ExtractPanel;

/**
 * CheckBoxList - a widget that has checkboxes nested inside a JList
 * @author Mike
 */
public class CheckBoxList extends JScrollPane
{
	
    ////////////////////////////////////////////////////////////////////////
    // member variables
    ////////////////////////////////////////////////////////////////////////
    private CheckBoxListModel<JCheckBox> mModel;		//the vector of checkboxes
    private JList mList;								//our jlist
    private int mSelectedCount = 0;						//how many are selected
    private JCheckBox mPreviousSelection;				//the previously selected checkbox

    /** Creates a new instance of CheckBoxList */
    public CheckBoxList()
    {
        //create the list and the scroll stuff
        mList = new JList();
        mList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        this.setViewportView(mList);
    
        //create the model
        mModel = new CheckBoxListModel<JCheckBox>();
        
        //set all the layout stuff here
        mList.setModel(mModel);
        mList.setCellRenderer(new CheckBoxListRenderer());
        
        //initialize the previous selection
        mPreviousSelection = null;
        
        //add us as a list selection listener of our list
        initMouseListener();
    }
    
    
    ////////////////////////////////////////////////////////////////////////
    // initMouseListener()- initializes our mouse listener
    ////////////////////////////////////////////////////////////////////////
    public void initMouseListener()
    {
        mList.addMouseListener(new MouseAdapter()
        {
            public void mouseReleased(MouseEvent evt)
            {
            	onMouseReleased(evt);
            }
        });
    }
        
    ////////////////////////////////////////////////////////////////////////
    // add(), remove() - adds or removes a checkbox from our model
    ////////////////////////////////////////////////////////////////////////
    public void add(JCheckBox kBox)
    {
        mModel.add(kBox);
    }
    
    public void remove(JCheckBox kBox)
    {
        mModel.remove(kBox);
    }
    
    ////////////////////////////////////////////////////////////////////////
    // getAllSelected() - returns a vector of all objects selected
    ////////////////////////////////////////////////////////////////////////
    public Vector getAllSelected()
    {
        //create the vector here
        Vector kVec = new Vector();
        
        //iterate through all the checkboxes
        for(int i = 0; i < mModel.getSize(); i++)
        {
            JCheckBox kBox = (JCheckBox)mModel.getElementAt(i);
            
            //if that box is selected, add it to the vector
            if(kBox.isSelected())
            {
                kVec.add(kBox);
            }
        }
        
        //return this vector
        return kVec;
    }    
    
    public boolean areAnyBoxesSelected()
    {
    	return mSelectedCount > 0;
    }
    
    ////////////////////////////////////////////////////////////////////////
    // setSelectedValue(), getSelectedValue() - does what it says
    ////////////////////////////////////////////////////////////////////////
    public void setSelectedValue(JCheckBox box)
    {
		mList.setSelectedValue(box, true);
    }
    
    //return the selected item in our list
    public JCheckBox getSelectedValue()
    {
    	return (JCheckBox)mList.getSelectedValue();
    }
    
    ////////////////////////////////////////////////////////////////////////
    // add/removeListSelectionListener() - adds and removes listeners
    ////////////////////////////////////////////////////////////////////////
    public void addListSelectionListener(ListSelectionListener listener)
    {
    	mList.addListSelectionListener(listener);
    }
    
    public void removeListSelectionListner(ListSelectionListener listener)
    {
    	mList.removeListSelectionListener(listener);
    }    

    ////////////////////////////////////////////////////////////////////////
    // onMouseReleased() - callback for the mouse release
    ////////////////////////////////////////////////////////////////////////
    public void onMouseReleased(MouseEvent e)
    {
	    //get the checkbox
        JCheckBox kBox = (JCheckBox)mList.getSelectedValue();
        
        if(kBox == null) return;
        
        //if it wasnt the previously selected one, just return
        if(kBox != mPreviousSelection)
        {
            mPreviousSelection = kBox;
            return;
        }
        
        //if not, set it here either way
         mPreviousSelection = kBox;

         //increment or decrement our counter FIRST
         //if it is selected now, it will be unselected, decrement
         if(kBox.isSelected())
         {
         	mSelectedCount--;
         }
         else
         {
         	mSelectedCount++;
         }
         
        //update the check itself
        kBox.setSelected(!kBox.isSelected());

        //repaint and invalidate the box
        this.invalidate();
        this.repaint();
    }
}
