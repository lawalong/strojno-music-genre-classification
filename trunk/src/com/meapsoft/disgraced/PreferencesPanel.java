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

package com.meapsoft.disgraced;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.meapsoft.MEAPUtil;

public class PreferencesPanel extends MEAPsoftGUIPanel
{
	//prefs/about
	JTextField dataBaseNameField;
    JTextField audioPlayerField;
	JCheckBox saveFilesPrefBox;

    /**
     * Create a new Preference panel
     */
    public PreferencesPanel(MEAPsoftGUI msg)
	{
        super(msg);
        BuildPrefsAboutGUI();

        title = "Preferences";
        helpURL += "#" + title;
    }

	private void BuildPrefsAboutGUI()
    {
		Color c = new Color((int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127));
        color = c;
        
		setBackground(c);
		BoxLayout prefsAboutBL = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(prefsAboutBL);

		add(helpButton);
		
		JPanel prefsPanel = new JPanel();
		prefsPanel.setLayout(new BoxLayout(prefsPanel, BoxLayout.Y_AXIS));
		prefsPanel.setBackground(c);
		TitledBorder title = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
			"Preferences");
		title.setTitleJustification(TitledBorder.CENTER);
		prefsPanel.setBorder(title);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(c);

        JPanel dBNPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		dBNPanel.setBackground(c);
		
		JLabel dataBaseNameLabel = new JLabel("file i/o base name: ");
		dataBaseNameLabel.setToolTipText("Name used to construct filenames for " 
                                         + "intermediate processing steps.");
		dBNPanel.add(dataBaseNameLabel);
		dataBaseNameField = new JTextField("mann");
		dataBaseNameField.setColumns(10);
		dataBaseNameField.addActionListener(this);
		dataBaseNameField.setActionCommand("dataBaseName");
		dBNPanel.add(dataBaseNameField);
		JButton dataBaseNameUpdateButton = new JButton("update");
		dataBaseNameUpdateButton.setBackground(c);
		dataBaseNameUpdateButton.addActionListener(this);
		dataBaseNameUpdateButton.setActionCommand("dataBaseName");
		dBNPanel.add(dataBaseNameUpdateButton);
        dBNPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(dBNPanel);
		
        JPanel aPPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        //aPPanel.setLayout(new BoxLayout(aPPanel, BoxLayout.X_AXIS));
        aPPanel.setBackground(c);
		
		JLabel audioPlayerLabel = new JLabel("audio player: ");
		aPPanel.add(audioPlayerLabel);
        if(audioPlayerName != null)
            audioPlayerField = new JTextField(audioPlayerName);
        else
            audioPlayerField = new JTextField("select audio player");
		audioPlayerField.setColumns(10);
		audioPlayerField.addActionListener(this);
        audioPlayerField.setActionCommand("selectAudioPlayer");
		aPPanel.add(audioPlayerField);
		JButton audioPlayerBrowseButton = new JButton("browse");
		audioPlayerBrowseButton.setBackground(c);
		audioPlayerBrowseButton.addActionListener(this);
        audioPlayerBrowseButton.setActionCommand("browseAudioPlayer");
        dataBaseNameUpdateButton.setPreferredSize(
            audioPlayerBrowseButton.getPreferredSize());
  		aPPanel.add(audioPlayerBrowseButton);		
        aPPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panel.add(aPPanel);
		prefsPanel.add(panel);

        panel = new JPanel();
        panel.setBackground(c);
        saveFilesPrefBox = new JCheckBox("save .seg .edl .feat files");
		saveFilesPrefBox.setBackground(c);
		saveFilesPrefBox.setSelected(true);
		panel.add(saveFilesPrefBox);
        prefsPanel.add(panel);
        prefsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		add(prefsPanel);

		JPanel aboutPanel = new JPanel();
		aboutPanel.setBackground(c);
		TitledBorder title2 = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
			"About MEAPsoft");
		title2.setTitleJustification(TitledBorder.CENTER);
		aboutPanel.setBorder(title2);
		
		JTextArea aboutTextArea = new JTextArea();
		aboutTextArea.setBackground(c);
		
		String aboutText = 
            "MEAPsoft was created by the participants in the " + 
            "Music Engineering Art Project at Columbia University.\n" +
            "\n" +
            "For more information, please see:\n" +
            "http://labrosa.ee.columbia.edu/meapsoft\n" +
            "\n" +
            "Sponsored by:\n" + 
            "The Academic Quality Fund\n" + 
            "LabROSA: http://labrosa.ee.columbia.edu \n" +
            "The Computer Music Center: http://music.columbia.edu/cmc \n" +
            "\n" +
            "Development by:\n" +
            "Ron Weiss, Douglas Repetto, Mike Mandel, Dan Ellis, Victor Adan, Jeff Snyder\n" +
            "\n" +
            "Additional contributions by:\n" +
            "John Arroyo, Johanna Devaney, Dan Iglesia, Graham Poliner\n\n" +
            "MEAPsoft version " + MEAPUtil.version;

		aboutTextArea.setText(aboutText);
        aboutTextArea.setEditable(false);
        aboutTextArea.setColumns(50);
		aboutTextArea.setLineWrap(true);
		aboutTextArea.setWrapStyleWord(true);
        aboutPanel.add(aboutTextArea);
		
		add(aboutPanel);
	}

	public void actionPerformed(ActionEvent arg0)
	{
		String command = arg0.getActionCommand();

		if (command.equals("dataBaseName"))
		{
            dataBaseName = dataBaseNameField.getText();
            UpdateFileNames();
            meapsoftGUI.UpdateInfoTexts();
		}
		else if (command.equals("selectAudioPlayer"))
        {
            audioPlayerName = audioPlayerField.getText();
        }
		else if (command.equals("browseAudioPlayer"))
        {
            String names[] = 
            	GUIUtils.FileSelector(GUIUtils.OPEN, meapsoftGUI.dataDirectory, meapsoftGUI.jframe);
            
			
			if(names[0] != null)
            {            
                audioPlayerName = names[0];
                audioPlayerField.setText(audioPlayerName);
            }
        }
    }

    public int run()
    {
        return 0;
    }
}
