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
 * Created on Jul 14, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.meapsoft.disgraced;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

/**
 * @author douglas
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HelpWindow extends JFrame
{
	JButton closeButton;
    JTextComponent textArea = null;
	
	public HelpWindow(String url, String title, Color c)
	{
        super(title);

		JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(c);
		
        try
        {
            textArea = new JEditorPane(url);
        }
        catch(MalformedURLException e)
        {
            textArea = new JTextArea(url);
        }
        catch(IOException e)
        {}

		//textArea.setColumns(50);
		textArea.setBackground(c);
		textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
		panel.add(scrollPane, BorderLayout.CENTER);

        panel.setPreferredSize(new Dimension(600, 400));

		JPanel cbPanel = new JPanel();
        cbPanel.setBackground(c);
		closeButton = new JButton("close");
        closeButton.setBackground(c);
		closeButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			}
		);
        cbPanel.add(closeButton);
		panel.add(cbPanel, BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
		setLocation(23, 23);
        setVisible(true);
	}
}
