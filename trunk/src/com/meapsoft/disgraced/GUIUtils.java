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
package com.meapsoft.disgraced;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author douglas
 *
 */

public class GUIUtils
{	
	public static final int OPEN = 0;
	public static final int SAVE = 1;
	public static final int TARGET = 2;
	public static final int DIR = 3;
	public static final int OPENWAV = 4;
	public static final int OPENFEAT = 5;
	public static final int OPENSEG = 6;
	public static final int OPENEDL = 7;
	public static final int ANYMEAP = 8;
	public static final int SAVEEDL = 9;
	public static final int SAVEFEAT = 10;
	
	public static final int FATAL_ERROR = 0;
	public static final int MESSAGE = 1;
	
	public static String[] FileSelector(int mode, String defaultDirectory, JFrame jframe)
	{	
		if (jframe == null)
			jframe = new JFrame();
			
		JFileChooser chooser = new JFileChooser();
		
		chooser.setCurrentDirectory(new File(defaultDirectory));
		int returnVal = 0;
		
		if (mode == OPENWAV)
		{
			chooser.addChoosableFileFilter(new WavFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == OPEN)
		{
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == SAVE)
			returnVal = chooser.showSaveDialog(null);
		else if (mode == DIR)
		{
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			returnVal = chooser.showOpenDialog(jframe);
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
		else if (mode == SAVEEDL)
			returnVal = chooser.showSaveDialog(null);
		else if (mode == SAVEFEAT)
			returnVal = chooser.showSaveDialog(null);
		else
			return null;
			
		String[] name = new String[2];
		
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			try
			{
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

	//allows selection of multiple files
	public static String[] FileSelectorMulti(int mode, String defaultDirectory, JFrame jframe)
	{	
		if (jframe == null)
			jframe = new JFrame();
			
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		
		chooser.setCurrentDirectory(new File(defaultDirectory));
		int returnVal = 0;
		
		if (mode == OPENWAV)
		{
			chooser.addChoosableFileFilter(new WavFileFilter());
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == OPEN)
		{
			returnVal = chooser.showOpenDialog(null);
		}
		else if (mode == SAVE)
			returnVal = chooser.showSaveDialog(null);
		else if (mode == DIR)
		{
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			returnVal = chooser.showOpenDialog(jframe);
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
		else if (mode == SAVEEDL)
			returnVal = chooser.showSaveDialog(null);
		else if (mode == SAVEFEAT)
			returnVal = chooser.showSaveDialog(null);
		else
			return null;
			
		String[] names = null;
		
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			try
			{
				//name[0] = chooser.getSelectedFile().getAbsolutePath();
				//name[1] = chooser.getSelectedFile().getName();
				File[] files = chooser.getSelectedFiles();
				
				names = new String[files.length];
				
				for (int i = 0; i < files.length; i++)
					names[i] = files[i].getAbsolutePath();
			}
			catch (Exception e)
			{
				ShowDialog(e, "", MESSAGE, jframe);
				return null;
			}
		}
		
		return names;
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


}




