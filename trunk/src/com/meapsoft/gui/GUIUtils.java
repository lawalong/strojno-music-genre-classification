/*
 *  Copyright 2006-2007 Columbia University.
 *
 *  This file is part of MEAPsoft.
 *
 *  MEAPsoft is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  MEAPsoft is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MEAPsoft; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *
 *  See the file "COPYING" for the text of the license.
 */

/*
 * Created on Nov 28, 2006
 *
 * Various GUI utilities
 * see com.meapsoft.MEAPUtil for non-gui-related utilities
 * 
 */
package com.meapsoft.gui;

import com.meapsoft.gui.filters.EDLFileFilter;
import com.meapsoft.gui.filters.FeatFileFilter;
import com.meapsoft.gui.filters.MEAPFileFilter;
import com.meapsoft.gui.filters.SegFileFilter;
import com.meapsoft.gui.filters.SoundFileFilter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * @author douglas
 *
 */

public class GUIUtils
{	
	public static final int OPEN = 0;
	public static final int SAVE = 1;
	public static final int DIR = 2;
	public static final int OPENSOUND = 3;
    public static final int SAVESOUND = 4;
	public static final int OPENFEAT = 5;
	public static final int OPENSEG = 6;
	public static final int OPENEDL = 7;
	public static final int ANYMEAP = 8;
	
	public static final int FATAL_ERROR = 0;
	public static final int MESSAGE = 1;
	
	public static String[] showFileSelector(int mode, String defaultDirectory, JFrame jframe)
	{	
                //if we don't have a jframe, create it here
		if (jframe == null)
			jframe = new JFrame();
			
		JFileChooser chooser = new JFileChooser();

                //set the default directory
		chooser.setCurrentDirectory(new File(defaultDirectory));
                
		int returnVal = 0;

                //see what kind of dialog we want here
		if (mode == OPEN)
		{
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == SAVE)
        {
			returnVal = chooser.showSaveDialog(null);
        }
        else if (mode == DIR)
		{
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			returnVal = chooser.showOpenDialog(jframe);
		}
        else if (mode == OPENSOUND)
		{
			chooser.addChoosableFileFilter(new SoundFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
        else if (mode == SAVESOUND)
		{
			chooser.addChoosableFileFilter(new SoundFileFilter());
			returnVal = chooser.showSaveDialog(null);
		}
		else if (mode == OPENFEAT)
		{
			chooser.addChoosableFileFilter(new FeatFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == OPENSEG)
		{
			chooser.addChoosableFileFilter(new SegFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == OPENEDL)
		{
			chooser.addChoosableFileFilter(new EDLFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == ANYMEAP)
		{
			chooser.addChoosableFileFilter(new MEAPFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
		else
			return null;
			
                //create the array to return
		String[] name = new String[2];
		
                //if we clicked OK
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			try
			{
                            //set the string array
                            name[0] = chooser.getSelectedFile().getAbsolutePath();
                            name[1] = chooser.getSelectedFile().getName();
			}
			catch (Exception e)
			{
                            ShowDialog(e, "", MESSAGE, jframe);
                            return null;
			}
		}
		
		return name;
	}

	public static File[] showMultiFileSelector(int mode, String defaultDirectory, JFrame jframe)
	{	
        //if we don't have a jframe, create it here
		if (jframe == null)
			jframe = new JFrame();
			
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);

                //set the default directory
		chooser.setCurrentDirectory(new File(defaultDirectory));
                
		int returnVal = 0;

                //see what kind of dialog we want here
		if (mode == OPEN)
		{
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == SAVE)
        {
			returnVal = chooser.showSaveDialog(null);
        }
        else if (mode == DIR)
		{
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			returnVal = chooser.showOpenDialog(jframe);
		}
        else if (mode == OPENSOUND)
		{
			chooser.addChoosableFileFilter(new SoundFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
        else if (mode == SAVESOUND)
		{
			chooser.addChoosableFileFilter(new SoundFileFilter());
			returnVal = chooser.showSaveDialog(null);
		}
		else if (mode == OPENFEAT)
		{
			chooser.addChoosableFileFilter(new FeatFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == OPENSEG)
		{
			chooser.addChoosableFileFilter(new SegFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == OPENEDL)
		{
			chooser.addChoosableFileFilter(new EDLFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == ANYMEAP)
		{
			chooser.addChoosableFileFilter(new MEAPFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
		else
			return null;
		
        //if we clicked OK
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			return chooser.getSelectedFiles();
		}
		
		return null;
	}
	
	public static void ShowDialog(String message, int status, JFrame jframe)
	{		
		if (jframe == null)
			jframe = new JFrame();
			
		if (status == FATAL_ERROR)
		{
			JOptionPane.showMessageDialog(jframe, 
				"MEAPsoft has encountered a fatal error: " + message);
			System.exit(-1);
		}
		else
			JOptionPane.showMessageDialog(jframe, message);
	}
		
	public static void ShowDialog(Exception e, String message, int status, JFrame jframe)
	{
		if (jframe == null)
			jframe = new JFrame();
			
		message += ":\n" + e.getMessage();

		if (status == FATAL_ERROR)
			message = "MEAPsoft has encountered a fatal error: " + message;

		//if(message.length() > 70)
		//    message = message.substring(0, 70);
			
		final JOptionPane optionPane = 
			new JOptionPane(message, JOptionPane.QUESTION_MESSAGE);

		final JDialog dialog = new JDialog(jframe, "Exception!", true);
		//dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		optionPane.addPropertyChangeListener(
			new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
					String prop = e.getPropertyName();

					if (dialog.isVisible() 
					 && (e.getSource() == optionPane)
					 && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
						dialog.setVisible(false);
					}
				}
			});
		
		JPanel errorPanel = new JPanel();
		BoxLayout bl = new BoxLayout(errorPanel, BoxLayout.Y_AXIS);
		errorPanel.setLayout(bl);
		
		StringWriter sw = new StringWriter(); 
		e.printStackTrace(new PrintWriter(sw));
		JTextArea textbox = new JTextArea(sw.toString());
		textbox.setEditable(false);
		textbox.setColumns(50);
		textbox.setLineWrap(true);
		textbox.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textbox);
		
		errorPanel.add(optionPane);
		errorPanel.add(scrollPane);

		dialog.setContentPane(errorPanel);
		
		dialog.pack();
		dialog.setVisible(true);

		if (status == FATAL_ERROR)
			System.exit(-1);
	}

	//i can't believe i agreed to put this in.
	public static Color getRandomColor()
	{
		Color c = new Color((int)(Math.random() * 127 + 127),
				(int)(Math.random() * 127 + 127),
				(int)(Math.random() * 127 + 127));
		
		return c;
	}
	
	public static void initContainerColor(Component comp, Color color)
	{
		//set the background for this guy
		if(!(comp instanceof JTextField) 
				&& !(comp instanceof JScrollPane)
				&& !(comp instanceof JList))
		{
			comp.setBackground(color);
		}
		
		//if its a container, then go through its children
		if(comp instanceof Container)
		{
			Container container = (Container)comp;
			
			//iterate through all its children, and set their color too
			for(int i = 0; i < container.getComponentCount(); i++)
			{
				Component subComponent = (Component)container.getComponent(i);
				GUIUtils.initContainerColor(subComponent, color);
			}
		}
	}
	

	//Note: The following 4 methods were taken from JCommon:
	//http://www.jfree.org/jcommon/index.html
    public static void centerFrameOnScreen(final Window frame) 
    {
        positionFrameOnScreen(frame, 0.5, 0.5);
    }


    public static void positionFrameOnScreen(final Window frame,
                                             final double horizontalPercent,
                                             final double verticalPercent) {

        final Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension f = frame.getSize();
        final int w = Math.max(s.width - f.width, 0);
        final int h = Math.max(s.height - f.height, 0);
        final int x = (int) (horizontalPercent * w);
        final int y = (int) (verticalPercent * h);
        frame.setBounds(x, y, f.width, f.height);

    }
    
    public static void centerDialogInParent(final Dialog dialog) {
        positionDialogRelativeToParent(dialog, 0.5, 0.5);
    }


    public static void positionDialogRelativeToParent(final Dialog dialog,
                                                      final double horizontalPercent,
                                                      final double verticalPercent) {
        final Dimension d = dialog.getSize();
        final Container parent = dialog.getParent();
        final Dimension p = parent.getSize();

        final int baseX = parent.getX() - d.width;
        final int baseY = parent.getY() - d.height;
        final int w = d.width + p.width;
        final int h = d.height + p.height;
        int x = baseX + (int) (horizontalPercent * w);
        int y = baseY + (int) (verticalPercent * h);

        // make sure the dialog fits completely on the screen...
        final Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        x = Math.min(x, (s.width - d.width));
        x = Math.max(x, 0);
        y = Math.min(y, (s.height - d.height));
        y = Math.max(y, 0);

        dialog.setBounds(x, y, d.width, d.height);

    }
	
}




