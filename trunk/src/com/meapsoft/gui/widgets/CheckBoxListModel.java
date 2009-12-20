/*
 * CheckBoxList.java
 *
 * Created on October 3, 2007, 5:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.meapsoft.gui.widgets;

import java.util.Vector;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Mike
 */
public class CheckBoxListModel<E> extends Vector implements ListModel
{

    /** Creates a new instance of CheckBoxList */
    public CheckBoxListModel()
    {
    }

    public int getSize()
    {
        return this.size();
    }

    public Object getElementAt(int index)
    {
        return this.get(index);
    }

    public void addListDataListener(ListDataListener l)
    {
    }

    public void removeListDataListener(ListDataListener l)
    {
    }

}
