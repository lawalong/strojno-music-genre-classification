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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Hashtable;

import javax.sound.sampled.LineListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.meapsoft.EDLFile;
import com.meapsoft.Segmenter;
import com.meapsoft.Synthesizer;
import com.meapsoft.composers.BlipComposer;
import com.meapsoft.visualizer.SingleFeaturePanel;
import com.meapsoft.visualizer.SingleFeatureSpectrumPanel;
import com.meapsoft.visualizer.SingleFeatureWaveformPanel;

/**
 * GUI interface for Segmenter.  
 *
 * @author Douglas Repetto (douglas@music.columbia.edu)
 * and the MEAP team
 */
public class SegmenterPanel extends MEAPsoftGUIPanel
{
	//segmenter GUI
	JCheckBox enableBox;
	JRadioButton eventStyleButton;
	JRadioButton beatStyleButton;
	JCheckBox firstFrameBox;
	JTextField inputSoundFileField;
	JLabel outputSegFileLabel;
	JSlider thresholdSlider;
    JSlider densitySlider;
    JCheckBox halfTempoBox;
    JButton listenButton;
    JButton playWithBlips;

    JPanel controlPanel;
    JPanel eventPanel;
    JPanel beatPanel;
    
    JButton runSegmenterButton;

    private Thread playThread = null;
    
    /**
     * Create a new Segmenter panel
     */
    public SegmenterPanel(MEAPsoftGUI msg)
    {
        super(msg);
        BuildSegmenterGUI();

        title = "Segmenter";
        helpURL += "#" + title;
    }

	private void BuildSegmenterGUI()
	{
		Color c = new Color((int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127));
        color = c;

		setBackground(c);
		BoxLayout sbl = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(sbl);
		
		JPanel segmenterEnablePanel = new JPanel();

		segmenterEnablePanel.setBackground(c);
		
		enableBox = new JCheckBox("ENABLE SEGMENTER");
		enableBox.setBackground(c);
		enableBox.setSelected(true);
		segmenterEnablePanel.add(enableBox);
		
		helpButton = new JLabel("(help)");
		//helpButton.setBackground(c.darker());
		helpButton.setForeground(Color.blue);
		helpButton.addMouseListener(this);
		segmenterEnablePanel.add(helpButton);
		
		add(segmenterEnablePanel);
		
        Box segmenterControlsPanel = Box.createVerticalBox();
		segmenterControlsPanel.setBackground(c);
		TitledBorder title = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
			"Segmenter Controls");
		title.setTitleJustification(TitledBorder.CENTER);
		segmenterControlsPanel.setBorder(title);
			
		JPanel inputFileNamePanel = new JPanel();
		inputFileNamePanel.setBackground(c);
		
		JLabel inputFileNameBoxLabel = new JLabel("input sound file:");
		inputFileNamePanel.add(inputFileNameBoxLabel);
	
		inputSoundFileField = new JTextField("chris_mann.wav");
		inputSoundFileField.setColumns(20);
		inputSoundFileField.addActionListener(this);
		inputSoundFileField.setActionCommand("setInputFile");
		inputFileNamePanel.add(inputSoundFileField);
		
		JButton inputBrowseButton = new JButton("browse");
		inputBrowseButton.setBackground(c);
		inputBrowseButton.addActionListener(this);
		inputBrowseButton.setActionCommand("browseInputFile");
		inputFileNamePanel.add(inputBrowseButton);
		
		listenButton = new JButton("listen");
		listenButton.setBackground(c);
		listenButton.addActionListener(this);
		listenButton.setActionCommand("listen");
		inputFileNamePanel.add(listenButton);
		
		segmenterControlsPanel.add(inputFileNamePanel);
		
		JPanel detectorTypePanel = new JPanel();
		detectorTypePanel.setBackground(c);
		ButtonGroup onsetDetectorTypeGroup = new ButtonGroup();
		eventStyleButton = new JRadioButton("detect events");
		eventStyleButton.setBackground(c);
        eventStyleButton.addActionListener(this);
        eventStyleButton.setActionCommand("event_detector");
		beatStyleButton = new JRadioButton("detect beats");
		beatStyleButton.setBackground(c);
        beatStyleButton.addActionListener(this);
        beatStyleButton.setActionCommand("beat_detector");
		onsetDetectorTypeGroup.add(eventStyleButton);
		onsetDetectorTypeGroup.add(beatStyleButton);
		detectorTypePanel.add(eventStyleButton);
		detectorTypePanel.add(beatStyleButton);
		eventStyleButton.setSelected(true);

		segmenterControlsPanel.add(detectorTypePanel);

        eventPanel = new JPanel();
        eventPanel.setLayout(new BoxLayout(eventPanel, BoxLayout.Y_AXIS));
		eventPanel.setBackground(c);
        JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        thresholdPanel.setBackground(c);
		JLabel thresholdLabel = new JLabel("segment sensitivity: ");
		thresholdPanel.add(thresholdLabel);
		thresholdSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 1);
		thresholdSlider.setBackground(c);
		thresholdSlider.setValue(75);
		Hashtable labelTable = new Hashtable();
		labelTable.put( new Integer(0), new JLabel("low") );
		labelTable.put( new Integer(100), new JLabel("high") );
		thresholdSlider.setLabelTable( labelTable );
		thresholdSlider.setPaintLabels(true);
		thresholdSlider.setMajorTickSpacing(10);
		thresholdSlider.setPaintTicks(true);
		thresholdPanel.add(thresholdSlider);
        eventPanel.add(thresholdPanel);

        JPanel densityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        densityPanel.setBackground(c);
        JLabel densityLabel = new JLabel("segment density: ");
        densityPanel.add(densityLabel);
        densitySlider = new JSlider(JSlider.HORIZONTAL, 0, 20, 1);
        densitySlider.setBackground(c);
        densitySlider.setValue(15);
        densitySlider.setMajorTickSpacing(2);
        densitySlider.setPaintTicks(true);
		densitySlider.setLabelTable(labelTable);
		densitySlider.setPaintLabels(true);
        densityPanel.add(densitySlider);
        eventPanel.add(densityPanel);

		beatPanel = new JPanel();
        beatPanel.setBackground(c);
        halfTempoBox = new JCheckBox("cut tempo in half");
        halfTempoBox.setBackground(c);
        halfTempoBox.setSelected(false);
        beatPanel.add(halfTempoBox);

        // controlPanel is a wrapper around the event/beat detector knobs
        controlPanel = new JPanel();
		controlPanel.setBackground(c);
        controlPanel.add(eventPanel);
		segmenterControlsPanel.add(controlPanel);
		
		firstFrameBox = new JCheckBox("1st event = track start");
		firstFrameBox.setBackground(c);
		firstFrameBox.setSelected(true);
        firstFrameBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		segmenterControlsPanel.add(firstFrameBox);
	
        add(segmenterControlsPanel);

		JPanel outputSegFileNamePanel = new JPanel();
		outputSegFileNamePanel.setBackground(c);
		
		JLabel sFNL = new JLabel("output segment file: ");
		outputSegFileNamePanel.add(sFNL);
		outputSegFileLabel = new JLabel(" " + dataBaseName + ".seg ");
		outputSegFileLabel.setOpaque(true);
		outputSegFileLabel.setBackground(c.darker());
		outputSegFileNamePanel.add(outputSegFileLabel);
        		
        //add(Box.createRigidArea(new Dimension(0, 10)));
        add(outputSegFileNamePanel);

        JPanel runPanel = new JPanel();
        runPanel.setBackground(c);
        
        runSegmenterButton = new JButton("run segmenter");
        runSegmenterButton.setBackground(c);
        runSegmenterButton.addActionListener(this);
        runSegmenterButton.setActionCommand("run_segmenter");
        runPanel.add(runSegmenterButton);
        
        playWithBlips = new JButton("Play with blips");
		playWithBlips.setBackground(c);
		playWithBlips.setEnabled(false);
        playWithBlips.addActionListener(this);
        playWithBlips.setActionCommand("play_blips");
        runPanel.add(playWithBlips);

        add(runPanel);
        
        //this is a member of our parent class
        waveSpectPanel = new JPanel();
		//BoxLayout wSPBL = new BoxLayout(this, BoxLayout.Y_AXIS);
		waveSpectPanel.setLayout(new BoxLayout(waveSpectPanel, BoxLayout.Y_AXIS));
        waveSpectPanel.setBackground(c);
        
        //these were built in our parent's constructor
        waveSpectPanel.add(meapsoftGUI.waveformPanel);
        waveSpectPanel.add(meapsoftGUI.spectrumPanel);
        add(waveSpectPanel);
        
        JPanel zoomPanel = new JPanel();
        zoomPanel.setBackground(c);
        
        JButton zoomInButton = new JButton("zoom in");
        zoomInButton.setBackground(c);
        zoomInButton.setEnabled(true);
        zoomInButton.addActionListener(this);
        zoomInButton.setActionCommand("zoom_in");
        zoomPanel.add(zoomInButton);

        JButton zoomOutButton = new JButton("zoom out");
        zoomOutButton.setBackground(c);
        zoomOutButton.setEnabled(true);
        zoomOutButton.addActionListener(this);
        zoomOutButton.setActionCommand("zoom_out");
        zoomPanel.add(zoomOutButton);
        
        JButton zoomResetButton = new JButton("reset zoom");
        zoomResetButton.setBackground(c);
        zoomResetButton.setEnabled(true);
        zoomResetButton.addActionListener(this);
        zoomResetButton.setActionCommand("reset_zoom");
        zoomPanel.add(zoomResetButton);
        
        add(zoomPanel);
	}

	public void actionPerformed(ActionEvent arg0)
	{
		String command = arg0.getActionCommand();

		if (command.equals("listen"))
		{
            if (inputSoundFileNameFull == null)
            {
                GUIUtils.ShowDialog("You need to pick an input file!!!", GUIUtils.MESSAGE, meapsoftGUI.jframe);
                return;
            }
            PlaySoundFile(inputSoundFileNameFull);
		}
		else if (command.equals("browseInputFile"))
		{
			String names[] = GUIUtils.FileSelector(GUIUtils.OPEN, meapsoftGUI.dataDirectory, meapsoftGUI.jframe);
			
			if (names[0] == null)
				return;

			SetInputFileName(names);
		}
		else if (command.equals("setInputFile"))
		{
            String name = inputSoundFileField.getText(); 
            // default directory
            String names[] = {meapsoftGUI.dataDirectory + slash + name, name};

            // does outputFileNameField contain a full path?
            if((new File(name)).isAbsolute())
                names[0] = name;

            String[] nameSplit = name.split("["+slash+"]");
            names[1] = nameSplit[nameSplit.length-1];

			SetInputFileName(names);
		}
        else if(command.equals("event_detector"))
        {
            controlPanel.add(eventPanel);
            controlPanel.remove(beatPanel);
            RefreshGUI();
        }
        else if(command.equals("beat_detector"))
        {
            controlPanel.remove(eventPanel);
            controlPanel.add(beatPanel);
            RefreshGUI();
        }
		else if (command.equals("play_blips"))
        {
            EDLFile edl = new EDLFile("null");
            BlipComposer b = new BlipComposer(segmentFile, edl);
            b.setBlipWav(dataDirectory + slash + "blip.wav");
            b.compose();
            Synthesizer synth = new Synthesizer(edl, null);
            
            //add the waveform panel as a line listener
            synth.addLineListener((LineListener)meapsoftGUI.waveformPanel);
            
            if(playThread != null)
            {
                playThread.interrupt();
                playThread.stop();
            }
            playThread = new Thread(synth, "synthesizer");
            playThread.start();
            
    		//call the waveform panel here
    		//waveformPanel.actionPerformed(arg0);
    		
            playWithBlips.setText("STOP playing with blips!");
            playWithBlips.setActionCommand("stop_playing");
        }
        else if (command.equals("stop_playing"))
        {
            if(playThread != null)
            {
                playThread.interrupt();
                playThread.stop();
            }

    		//call the waveform panel here
    		//waveformPanel.actionPerformed(arg0);
            
            playWithBlips.setText("Play with blips!");
            playWithBlips.setActionCommand("play_blips");
        }
        else if (command.equals("run_segmenter"))
        {
        	meapsoftGUI.RunSegmenterButtonPressed();
        }	
        else if (command.equals("zoom_in"))
        {
        	meapsoftGUI.waveformPanel.zoomIn();
        	meapsoftGUI.spectrumPanel.zoomIn();
        	repaint();
        }
        else if (command.equals("zoom_out"))
        {
        	meapsoftGUI.waveformPanel.zoomOut();
        	meapsoftGUI.spectrumPanel.zoomOut();
        	repaint();
 
        }
        else if (command.equals("reset_zoom"))
        {
        	meapsoftGUI.waveformPanel.resetZoom();
        	meapsoftGUI.spectrumPanel.resetZoom();
        	repaint();
        }
    }
	
	public synchronized int run()
	{		
        if(!enableBox.isSelected())
            return 0;

        if (inputSoundFileNameFull == null)
        {
            GUIUtils.ShowDialog("You need to pick an input file!!!", GUIUtils.MESSAGE, meapsoftGUI.jframe);
            return -1;
        }

        // for event detector:
        //want value to be between 0 and 3
        //double thresh = 3.0-thresholdSlider.getValue()/6.666;
        double thresh = 5.0 - (thresholdSlider.getValue()/20.0);
        //System.out.println("thresholdSlider.getValue(): " + thresholdSlider.getValue() + " thresh: " + thresh);
        // want value between 1 and 0
        double smtime = (20.0-densitySlider.getValue())/20.0;
        //System.out.println("densitySlider.getValue(): " + densitySlider.getValue() + " smtime: " + smtime);
        // for beat detector:
        double tempoMult = 1.0;
        if(halfTempoBox.isSelected())
            tempoMult = 0.5;

		boolean beatOnsetDetector = beatStyleButton.isSelected();
		boolean firstFrameOnset = firstFrameBox.isSelected();
		
		String segmentsFileName = dataDirectory + slash + outputSegmentsFileName;
		
		Segmenter segmenter = new Segmenter(inputSoundFileNameFull,
			segmentsFileName, thresh, smtime, beatOnsetDetector, firstFrameOnset);
        segmenter.setTempoMultiplier(tempoMult);
        segmenter.writeMEAPFile = meapsoftGUI.writeMEAPFile;

        JPanel progressPanel1 = new JPanel();
        progressPanel1.add(new JLabel("Segmenting: "));
        JProgressBar progressBar1 = new JProgressBar(segmenter.getProgress());
        progressBar1.setStringPainted(true);
        progressPanel1.add(progressBar1);
        meapsoftGUI.setProgressPanel(progressPanel1);

        try
        {
            segmentFile = segmenter.processAudioFile(inputSoundFileNameFull);

            if(segmenter.writeMEAPFile)
                segmentFile.writeFile();

            segmentFile = segmenter.getSegFile();
        }
        catch (Exception e)
        {
            GUIUtils.ShowDialog(e, "Error running Segmenter", GUIUtils.MESSAGE, meapsoftGUI.jframe);

            return -1;
        }

        JPanel progressPanel2 = new JPanel();
        progressPanel2.add(new JLabel("drawing waveform "));
        JProgressBar progressBar2 = new JProgressBar(meapsoftGUI.waveformPanel.getProgress());
        progressBar2.setStringPainted(true);
        progressPanel2.add(progressBar2);
        meapsoftGUI.setProgressPanel(progressPanel2);

        meapsoftGUI.waveformPanel.initialize(segmentFile, "Waveform");
        meapsoftGUI.waveformPanel.setSegTickType(SingleFeaturePanel.SHORT_SEG_TICKS);
        meapsoftGUI.waveformPanel.updateWaveformPoints();
        
        JPanel progressPanel3 = new JPanel();
        progressPanel3.add(new JLabel("drawing spectrum "));
        JProgressBar progressBar3 = new JProgressBar(meapsoftGUI.spectrumPanel.getProgress());
        progressBar3.setStringPainted(true);
        progressPanel3.add(progressBar3);
        meapsoftGUI.setProgressPanel(progressPanel3);

        meapsoftGUI.spectrumPanel.initialize(segmentFile, "Spectrum");
        meapsoftGUI.spectrumPanel.setSegTickType(SingleFeaturePanel.SHORT_SEG_TICKS);
        meapsoftGUI.spectrumPanel.updateSpectra();

        playWithBlips.setEnabled(true);
        
        return 0;
	}
}
