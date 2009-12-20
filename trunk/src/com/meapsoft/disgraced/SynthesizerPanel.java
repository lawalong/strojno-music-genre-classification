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
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.meapsoft.Synthesizer;


/**
 * GUI interface for the Synthesizer.  
 *
 * @author Douglas Repetto (douglas@music.columbia.edu)
 * and the MEAP team
 */
public class SynthesizerPanel extends MEAPsoftGUIPanel
{
	//synthesizer
	String lastEDLFileName;
	JCheckBox enableBox;
	JLabel fileNameLabel;
	JTextField outputFileNameField;
	JButton listenButton;
	
	JButton runSynthButton;
	
    /**
     * Create a new Synthesizer panel
     */
    public SynthesizerPanel(MEAPsoftGUI msg)
	{
        super(msg);
        BuildSynthesizerGUI();

        title = "Synthesizer";
        helpURL += "#" + title;
    }

  	private void BuildSynthesizerGUI()
	{
		Color c = new Color((int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127));
        color = c;

		setBackground(c);
		BoxLayout synthbl = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(synthbl);
		
		JPanel enableSynthPanel = new JPanel();
		enableSynthPanel.setBackground(c);
		
		enableBox = new JCheckBox("ENABLE SYNTHESIZER");
		enableBox.setBackground(c);
		enableBox.setSelected(true);
		enableSynthPanel.add(enableBox);
		
		enableSynthPanel.add(helpButton);

		add(enableSynthPanel);
		
		JPanel fileIOPanel = new JPanel();
		fileIOPanel.setBackground(c);
		
		JLabel sNL = new JLabel("input edl file: ");
		fileIOPanel.add(sNL);
		fileNameLabel = new JLabel(" " + dataBaseName + ".edl ");
		fileNameLabel.setOpaque(true);
		fileNameLabel.setBackground(c.darker());
		fileIOPanel.add(fileNameLabel);
		add(fileIOPanel);
		
		JPanel synthControlsPanel = new JPanel();
		synthControlsPanel.setBackground(c);
		TitledBorder title = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
			"Synthesizer Controls");
		title.setTitleJustification(TitledBorder.CENTER);
		synthControlsPanel.setBorder(title);
        synthControlsPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		
        JPanel outputFileNamePanel = new JPanel();
        outputFileNamePanel.setBackground(c);
		
		JLabel outputFileNameBoxLabel = new JLabel("output sound file:");
		outputFileNamePanel.add(outputFileNameBoxLabel);
	
		outputFileNameField = new JTextField("chris_mann_MEAPed.wav");
		outputFileNameField.setColumns(20);
		//outputFileNameField.setEditable(false);
		outputFileNameField.addActionListener(this);
		outputFileNameField.setActionCommand("setOutputFile");
		outputFileNamePanel.add(outputFileNameField);
		
		JButton outputBrowseButton = new JButton("browse");
		outputBrowseButton.setBackground(c);
		outputBrowseButton.addActionListener(this);
		outputBrowseButton.setActionCommand("browseOutputFile");
		outputFileNamePanel.add(outputBrowseButton);

		listenButton = new JButton("listen");
		listenButton.setBackground(c);
		listenButton.addActionListener(this);
		listenButton.setActionCommand("listen");
		outputFileNamePanel.add(listenButton);
		
		synthControlsPanel.add(outputFileNamePanel);
		
		add(synthControlsPanel);

        JPanel runPanel = new JPanel();
        runPanel.setBackground(c);
        
        runSynthButton = new JButton("run synth");
        runSynthButton.setBackground(c);
        runSynthButton.addActionListener(this);
        runSynthButton.setActionCommand("run_synth");
        runPanel.add(runSynthButton);
        add(runPanel);
	}

	public void actionPerformed(ActionEvent arg0)
	{
		String command = arg0.getActionCommand();

        if (command.equals("listen"))
		{
			if (outputSoundFileNameFull == null)
			{
				GUIUtils.ShowDialog("You need to pick an output file!!!", GUIUtils.MESSAGE, meapsoftGUI.jframe);
				return;
			}
            PlaySoundFile(outputSoundFileNameFull);
		}
		else if (command.equals("setOutputFile"))
        {
            String name = outputFileNameField.getText(); 
            // default directory
            String names[] = {dataDirectory + slash + name, name};

            // does outputFileNameField contain a full path?
            if((new File(name)).isAbsolute())
                names[0] = name;

            String[] nameSplit = name.split("["+slash+"]");
            names[1] = nameSplit[nameSplit.length-1];
            
            SetOutputFileName(names);
        }
		else if (command.equals("browseOutputFile"))
		{
			String names[] = GUIUtils.FileSelector(GUIUtils.SAVE, meapsoftGUI.dataDirectory, meapsoftGUI.jframe);
			
			if (names[0] == null)
				return;
				
			SetOutputFileName(names);
		}
		else if (command.equals("run_synth"))
		{
			meapsoftGUI.RunSynthButtonPressed();
		}
    }

	public synchronized int run()
	{	
        if(!enableBox.isSelected())
            return 0;

        if (outputSoundFileNameFull == null)
        {
			GUIUtils.ShowDialog("You need to pick an output file!!!", GUIUtils.MESSAGE, meapsoftGUI.jframe);
            return -1;
        }
    
		Synthesizer synth = new Synthesizer(edlFile, outputSoundFileNameFull);
        synth.writeMEAPFile = meapsoftGUI.writeMEAPFile;

        JPanel progressPanel = new JPanel();
        progressPanel.add(new JLabel("Synthesizing: "));
        JProgressBar progressBar = new JProgressBar(synth.getProgress());
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar);
        meapsoftGUI.setProgressPanel(progressPanel);

        try
        {
            synth.doSynthesizer();
        }
        catch(Exception e)
        {
			GUIUtils.ShowDialog(e, "Error synthesizing audio file", GUIUtils.MESSAGE, meapsoftGUI.jframe);
            return -1;
        }
        
        return 0;
	}
}
