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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.meapsoft.EDLFile;
import com.meapsoft.FeatFile;
import com.meapsoft.MEAPUtil;
import com.meapsoft.composers.BlipComposer;
import com.meapsoft.composers.Composer;
import com.meapsoft.composers.EDLComposer;
import com.meapsoft.composers.HMMComposer;
import com.meapsoft.composers.HeadBangComposer;
import com.meapsoft.composers.IntraChunkShuffleComposer;
import com.meapsoft.composers.MashupComposer;
import com.meapsoft.composers.MeapaeMComposer;
import com.meapsoft.composers.NNComposer;
import com.meapsoft.composers.RotComposer;
import com.meapsoft.composers.ShoobyComposer;
import com.meapsoft.composers.SortComposer;
import com.meapsoft.composers.ThresholdComposer;
import com.meapsoft.composers.VQComposer;
import com.meapsoft.visualizer.Visualizer;


/**
 * GUI interface for the MEAPsoft Composers.  
 *
 * @author Douglas Repetto (douglas@music.columbia.edu)
 * and the MEAP team
 */
public class ComposerPanel extends MEAPsoftGUIPanel
{
	//composers
	String selectedComposer;
	JPanel selectComposerPanel;
	JPanel controlsPanel;
	Vector controlsPanels;
	JCheckBox enableBox;
    // EDL commands
    JCheckBox reverseChunks;
    JCheckBox addGainChunks;
    JTextField gainValueField;
    JCheckBox fadeInOutChunks;
    JCheckBox crossfadeChunks;
    JSlider fadeDurationSlider;

	JRadioButton enableSortComposerButton;
	JRadioButton enableNNComposerButton;
	JRadioButton enableBLComposerButton;
	JRadioButton enableMUComposerButton;
	JRadioButton enableMMComposerButton;
	JRadioButton enableICSComposerButton;
	JRadioButton enableHBComposerButton;
	JRadioButton enableThresholdComposerButton;
	JRadioButton enableRotComposerButton;
	JRadioButton enableLikelihoodComposerButton;
	JRadioButton enableEDLComposerButton;
    JRadioButton enableShoobyComposerButton;

    // sort composer controls
	JRadioButton highLowSortButton;
	JRadioButton lowHighSortButton;
    JCheckBox normalizeFeatCB;

	String chunkDBFeaturesNameFull;
	String chunkDBFeaturesNameShort;
	JLabel inputFileNameLabel;
	JLabel outputFileNameLabel;
	JTextField mashupChunkDBFileField;
	JSlider headbangBinSlider;
	JSlider headbangLengthSlider;
	JTextField intraChunkShuffleNumChunksField;
	JTextField thresholdTopField;
	JTextField thresholdBottomField;
	JRadioButton insideThresholdButton;
	JRadioButton outsideThresholdButton;
	JTextField rotBeatsPerMeasureField;
	JTextField rotNumPositionsField;
	JRadioButton rotLeftButton;
	JRadioButton rotRightButton;
	JTextField inputEDLFileField;
	String inputEDLFileNameFull;
	String inputEDLFileNameShort;
	JSlider vqNumCodewords;
	JSlider vqBeatsPerCW;
    JCheckBox vqQuantizeTrainingFile;
    JTextField vqFeatFileField;
    JButton vqBrowseButton;
    String vqFeaturesNameFull = null;
    String vqFeaturesNameShort = null;
	JSlider hmmNumStates;
	JSlider hmmBeatsPerState;
	JSlider hmmSequenceLength;
	JTextField shoobyFileLengthField;
	JTextField shoobyClumpWidthField;
	JSlider shoobyDrunkennessSlider;

	JButton displayComposerFeaturesButton;	
	
	JButton runComposerButton;
	
    /**
     * Create a new Composer panel
     */
    public ComposerPanel(MEAPsoftGUI msg)
    {
        super(msg);
        BuildComposersGUI();

        title = "Composer";
        helpURL += "#" + title;
    }


	private void BuildComposersGUI()
	{
		controlsPanels = new Vector();
		
		Color c = new Color((int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127));
        color = c;

		setBackground(c);
		BoxLayout cbl = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(cbl);
		
		JPanel enableComposersPanel = new JPanel();
		enableComposersPanel.setBackground(c);
		
		enableBox = new JCheckBox("ENABLE COMPOSERS");
		enableBox.setBackground(c);
		enableBox.setSelected(true);
		enableComposersPanel.add(enableBox);
		
		enableComposersPanel.add(helpButton);

		add(enableComposersPanel);
		
		JPanel composersInputFileNamePanel = new JPanel();
		composersInputFileNamePanel.setBackground(c);
		JLabel cINL = new JLabel("input features file: ");
		composersInputFileNamePanel.add(cINL);
		inputFileNameLabel = new JLabel(" " + dataBaseName + ".feat ");
		inputFileNameLabel.setOpaque(true);
		inputFileNameLabel.setBackground(c.darker());
		composersInputFileNamePanel.add(inputFileNameLabel);
		add(composersInputFileNamePanel);

        JPanel selectComposerContainer = new JPanel();
		//BoxLayout bl = new BoxLayout(selectComposerContainer, BoxLayout.Y_AXIS);
		//selectComposerContainer.setLayout(bl);
		selectComposerContainer.add(new JLabel("select a composer: "));
        selectComposerContainer.setBackground(c);
        
        /*
		TitledBorder ct = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
			"Composers");
		ct.setTitleJustification(TitledBorder.CENTER);
		selectComposerContainer.setBorder(ct);
		*/

		selectComposerPanel = new JPanel(new GridLayout(3, 4));
		selectComposerPanel.setBackground(c);

		ButtonGroup composerButtons = new ButtonGroup();
		
		controlsPanels.add(BuildSortComposerGUI(c, composerButtons));
		controlsPanels.add(BuildNearestNeighborComposerGUI(c, composerButtons));
		controlsPanels.add(BuildBlipComposerGUI(c, composerButtons));
		controlsPanels.add(BuildMashupComposerGUI(c, composerButtons));	
		controlsPanels.add(BuildMeapaeMComposerGUI(c, composerButtons));	
		controlsPanels.add(BuildIntraChunkShuffleComposerGUI(c, composerButtons));
		controlsPanels.add(BuildHeadBangComposerGUI(c, composerButtons));
		controlsPanels.add(BuildThresholdComposerGUI(c, composerButtons));
		controlsPanels.add(BuildRotComposerGUI(c, composerButtons));
		controlsPanels.add(BuildEDLComposerGUI(c, composerButtons));
		controlsPanels.add(BuildVQComposerGUI(c, composerButtons));
		controlsPanels.add(BuildHMMComposerGUI(c, composerButtons));
		controlsPanels.add(BuildShoobyComposerGUI(c, composerButtons));

		selectComposerContainer.add(selectComposerPanel);
        add(selectComposerContainer);
        
		controlsPanel = new JPanel();
		controlsPanel.setBackground(c);

		controlsPanel.add((JPanel)controlsPanels.elementAt(0));

		selectedComposer = "SortComposer";
		
		add(controlsPanel);		

        JPanel chunkPanel = new JPanel();
        chunkPanel.setLayout(new BoxLayout(chunkPanel, BoxLayout.Y_AXIS));
		chunkPanel.setBackground(c);
       	TitledBorder title = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
            "Universal Chunk Operations");
        title.setTitleJustification(TitledBorder.CENTER);
        chunkPanel.setBorder(title);
        
        JPanel chunkCommandPanel = new JPanel();
		chunkCommandPanel.setBackground(c);
		
        reverseChunks = new JCheckBox("reverse");
        reverseChunks.setBackground(c);
		reverseChunks.setToolTipText("Reverse audio in each chunk");
        chunkCommandPanel.add(reverseChunks);
        
		addGainChunks = new JCheckBox("apply gain value");
		addGainChunks.setBackground(c);
		addGainChunks.setToolTipText("Apply gain value to each chunk");
		addGainChunks.addActionListener(this);
		addGainChunks.setActionCommand("gain");
		chunkCommandPanel.add(addGainChunks);

		fadeInOutChunks = new JCheckBox("apply fade in/out");
		fadeInOutChunks.addActionListener(this);
        fadeInOutChunks.setBackground(c);
		fadeInOutChunks.setToolTipText("Fade in/out on each chunk of audio");
        fadeInOutChunks.setActionCommand("fade");
        chunkCommandPanel.add(fadeInOutChunks);
        crossfadeChunks = new JCheckBox("crossfade");
		crossfadeChunks.addActionListener(this);
        crossfadeChunks.setBackground(c);
		crossfadeChunks.setToolTipText("Overlap fades from chunk to chunk");
        crossfadeChunks.setActionCommand("fade");
        chunkCommandPanel.add(crossfadeChunks);
        
        chunkPanel.add(chunkCommandPanel);

        chunkPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel gainPanel = new JPanel();
        //gainPanel.setLayout(new BoxLayout(gainPanel, BoxLayout.X_AXIS));
        gainPanel.setBackground(c);
        JLabel gainLabel = new JLabel("gain value: ");
        gainPanel.add(gainLabel);
        gainValueField = new JTextField("1.0");
        gainValueField.setEnabled(false);
        gainPanel.add(gainValueField);
        
        chunkPanel.add(gainPanel);
        
        JPanel crossfadePanel = new JPanel();
        //crossfadePanel.setLayout(new BoxLayout(crossfadePanel, BoxLayout.X_AXIS));
        crossfadePanel.setBackground(c);
		JLabel crossfadeLabel = new JLabel("fade length (ms): ");
        crossfadePanel.add(crossfadeLabel);
        fadeDurationSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, 0);
		fadeDurationSlider.setBackground(c);
		fadeDurationSlider.setToolTipText("Duration of fades");
		fadeDurationSlider.setValue(10);
		fadeDurationSlider.setEnabled(false);
		fadeDurationSlider.setPaintLabels(true);
		fadeDurationSlider.setMajorTickSpacing(10);
		fadeDurationSlider.setMinorTickSpacing(2);
		fadeDurationSlider.setPaintTicks(true);
        crossfadePanel.add(fadeDurationSlider);
        
        chunkPanel.add(crossfadePanel);
        
		//JLabel uCPLabel = new JLabel("UCOs apply to all chunks in a file");
		//chunkPanel.add(uCPLabel);

		add(chunkPanel);

		JPanel composersOutputFileNamePanel = new JPanel();
		composersOutputFileNamePanel.setBackground(c);
		JLabel cONL = new JLabel("output edl file: ");
		composersOutputFileNamePanel.add(cONL);
		outputFileNameLabel = new JLabel(" " + dataBaseName + ".edl ");
		outputFileNameLabel.setOpaque(true);
		outputFileNameLabel.setBackground(c.darker());
		composersOutputFileNamePanel.add(outputFileNameLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(composersOutputFileNamePanel);

		JPanel displayComposerFeaturesPanel = new JPanel();
		displayComposerFeaturesPanel.setBackground(c);
		displayComposerFeaturesButton = new JButton("display composed features");
		displayComposerFeaturesButton.setEnabled(false);
		displayComposerFeaturesButton.addActionListener(this);
		displayComposerFeaturesButton.setActionCommand("displayComposerFeatures");
		displayComposerFeaturesButton.setBackground(c);
		displayComposerFeaturesPanel.add(displayComposerFeaturesButton);
		
		add(displayComposerFeaturesPanel);
		
        JPanel runPanel = new JPanel();
        runPanel.setBackground(c);
        
        runComposerButton = new JButton("run composer");
        runComposerButton.setBackground(c);
        runComposerButton.addActionListener(this);
        runComposerButton.setActionCommand("run_composer");
        runPanel.add(runComposerButton);
        add(runPanel);
        
        //this is a member of our parent class
        waveSpectPanel = new JPanel();
		waveSpectPanel.setLayout(new BoxLayout(waveSpectPanel, BoxLayout.Y_AXIS));
        waveSpectPanel.setBackground(c);
        add(waveSpectPanel);
	}
	
	private JPanel BuildSortComposerGUI(Color c, ButtonGroup composerButtons)
	{		
		//sort composer
		JPanel sortComposerPanel = new JPanel();
		BoxLayout bl = new BoxLayout(sortComposerPanel, BoxLayout.Y_AXIS);
		sortComposerPanel.setLayout(bl);

		enableSortComposerButton = new JRadioButton("simple sort");
		enableSortComposerButton.setBackground(c);
		composerButtons.add(enableSortComposerButton);
		enableSortComposerButton.setSelected(true);
		enableSortComposerButton.addActionListener(this);
		//set the action command to the name of the composer class!
		enableSortComposerButton.setActionCommand("SortComposer");
		selectComposerPanel.add(enableSortComposerButton);
		
		JTextArea description = new JTextArea(SortComposer.oldDesc);
		description.setColumns(50);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setBackground(c);
		description.setEditable(false);
		sortComposerPanel.add(description);
		
		JPanel controlsPanel = new JPanel();
		controlsPanel.setBackground(c);
		TitledBorder title = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
			"SimpleSort Controls");
		title.setTitleJustification(TitledBorder.CENTER);
		controlsPanel.setBorder(title);
		
		ButtonGroup sortButtonGroup = new ButtonGroup();
		lowHighSortButton = new JRadioButton("low to high");
		lowHighSortButton.setBackground(c);
		sortButtonGroup.add(lowHighSortButton);
		highLowSortButton = new JRadioButton("high to low");
		highLowSortButton.setBackground(c);
		sortButtonGroup.add(highLowSortButton);		
		
		controlsPanel.add(lowHighSortButton);
		controlsPanel.add(highLowSortButton);
		
		highLowSortButton.setSelected(true);
		
        normalizeFeatCB = new JCheckBox("normalize features");
        normalizeFeatCB.setBackground(c);
        normalizeFeatCB.setSelected(true);
        controlsPanel.add(normalizeFeatCB);

		sortComposerPanel.add(controlsPanel);
		
		return sortComposerPanel;
	}
	
	private JPanel BuildNearestNeighborComposerGUI(Color c, ButtonGroup composerButtons)
	{
		//nearest neighbor composer
		JPanel nNComposerPanel = new JPanel();
		nNComposerPanel.setBackground(c);
		enableNNComposerButton = new JRadioButton("nearest neighbor");
		enableNNComposerButton.setBackground(c);
		composerButtons.add(enableNNComposerButton);
		enableNNComposerButton.setSelected(false);
		enableNNComposerButton.addActionListener(this);
		//set the action command to the name of the composer class!
		enableNNComposerButton.setActionCommand("NNComposer");
		selectComposerPanel.add(enableNNComposerButton);
		
		JTextArea description = new JTextArea(NNComposer.oldDesc);
		description.setColumns(50);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setBackground(c);
		description.setEditable(false);
		nNComposerPanel.add(description);
		
		
		return nNComposerPanel;
	}
	
	private JPanel BuildBlipComposerGUI(Color c, ButtonGroup composerButtons)
	{
		// blip composer
		JPanel bLComposerPanel = new JPanel();
		bLComposerPanel.setBackground(c);
		enableBLComposerButton = new JRadioButton("add blips");
		enableBLComposerButton.setBackground(c);
		composerButtons.add(enableBLComposerButton);
		enableBLComposerButton.setSelected(false);
		enableBLComposerButton.addActionListener(this);
		//set the action command to the name of the composer class!
		enableBLComposerButton.setActionCommand("BlipComposer");
		selectComposerPanel.add(enableBLComposerButton);
		
		JTextArea description = new JTextArea(BlipComposer.oldDesc);
		description.setColumns(50);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setBackground(c);
		description.setEditable(false);
		bLComposerPanel.add(description);
		
		return bLComposerPanel;
	}

    private JPanel BuildMashupComposerGUI(Color c, ButtonGroup composerButtons)
    {
        //mashup composer
        JPanel mUComposerPanel = new JPanel();
        BoxLayout bl = new BoxLayout(mUComposerPanel, BoxLayout.Y_AXIS);
        mUComposerPanel.setLayout(bl);
        mUComposerPanel.setBackground(c);
        enableMUComposerButton = new JRadioButton("mashup!");
        enableMUComposerButton.setBackground(c);
        composerButtons.add(enableMUComposerButton);
        enableMUComposerButton.setSelected(false);
        enableMUComposerButton.addActionListener(this);
        //set the action command to the name of the composer
        //class!
        enableMUComposerButton.setActionCommand("MashupComposer");
        selectComposerPanel.add(enableMUComposerButton);
        
        JTextArea description = new JTextArea(MashupComposer.oldDesc);
        description.setColumns(50);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setBackground(c);
        description.setEditable(false);
        mUComposerPanel.add(description);
        
        JPanel controlsPanel = new JPanel();
        controlsPanel.setBackground(c);
        TitledBorder title = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
            "Mashup Controls");
        title.setTitleJustification(TitledBorder.CENTER);
        controlsPanel.setBorder(title);
        
        JLabel mULabel = new JLabel("chunk database features file:");
        controlsPanel.add(mULabel);
        mashupChunkDBFileField = new JTextField("chunk database .feat file");
        mashupChunkDBFileField.setColumns(20);
        mashupChunkDBFileField.addActionListener(this);
        mashupChunkDBFileField.setActionCommand("setMUChunkDBFeaturesFile");
        //mashupChunkDBFileField.setEditable(false);
        controlsPanel.add(mashupChunkDBFileField);
        JButton mUBrowseButton = new JButton("browse");
        mUBrowseButton.setBackground(c);
        mUBrowseButton.addActionListener(this);
        mUBrowseButton.setActionCommand("browseMUChunkDBFeaturesFile");
        controlsPanel.add(mUBrowseButton);
        
        mUComposerPanel.add(controlsPanel);
        
        //selectComposerPanel.add(mUComposerPanel);
        return mUComposerPanel;
    }
    
    private JPanel BuildMeapaeMComposerGUI(Color c, ButtonGroup composerButtons)
    {
        // MeapaeM composer
        JPanel MeapaeMComposerPanel = new JPanel();
        MeapaeMComposerPanel.setBackground(c);
        enableMMComposerButton = new JRadioButton("MeapaeM");
        enableMMComposerButton.setBackground(c);
        composerButtons.add(enableMMComposerButton);
        enableMMComposerButton.setSelected(false);
        enableMMComposerButton.addActionListener(this);
        //set the action command to the name of the composer
        //class!
        enableMMComposerButton.setActionCommand("MeapaeMComposer");
        selectComposerPanel.add(enableMMComposerButton);
        
        JTextArea description = new JTextArea(MeapaeMComposer.oldDesc);
        description.setColumns(50);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setBackground(c);
        description.setEditable(false);
        MeapaeMComposerPanel.add(description);
        
        return MeapaeMComposerPanel;
    }
    
    private JPanel BuildIntraChunkShuffleComposerGUI(Color c, ButtonGroup composerButtons)
    {
        // IntraChunkShuffle composer
        JPanel IntraChunkShuffleComposerPanel = new JPanel();
        IntraChunkShuffleComposerPanel.setBackground(c);
        BoxLayout bl = new BoxLayout(IntraChunkShuffleComposerPanel, BoxLayout.Y_AXIS);
        IntraChunkShuffleComposerPanel.setLayout(bl);
        enableICSComposerButton = new JRadioButton("IntraChunkShuffle");
        enableICSComposerButton.setBackground(c);
        composerButtons.add(enableICSComposerButton);
        enableICSComposerButton.setSelected(false);
        enableICSComposerButton.addActionListener(this);
        //set the action command to the name of the composer
        //class!
        enableICSComposerButton.setActionCommand("IntraChunkShuffleComposer");
        selectComposerPanel.add(enableICSComposerButton);

        JTextArea description = new JTextArea(IntraChunkShuffleComposer.oldDesc);
        description.setColumns(50);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setBackground(c);
        description.setEditable(false);
        IntraChunkShuffleComposerPanel.add(description);
        
        JPanel controlsPanel = new JPanel();
        controlsPanel.setBackground(c);
        TitledBorder title = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
            "IntraChunkShuffle Controls");
        title.setTitleJustification(TitledBorder.CENTER);
        controlsPanel.setBorder(title);
        
        JLabel iCSLabel = new JLabel("number of sub chunks:");
        controlsPanel.add(iCSLabel);
        intraChunkShuffleNumChunksField = new JTextField("4");
        controlsPanel.add(intraChunkShuffleNumChunksField);
        
        IntraChunkShuffleComposerPanel.add(controlsPanel);
        
        return IntraChunkShuffleComposerPanel;
    }

    private JPanel BuildHeadBangComposerGUI(Color c, ButtonGroup composerButtons)
    {
        //Headbang composer
        JPanel hBComposerPanel = new JPanel();
        BoxLayout bl = new BoxLayout(hBComposerPanel, BoxLayout.Y_AXIS);
        hBComposerPanel.setLayout(bl);
        hBComposerPanel.setBackground(c);
        enableHBComposerButton = new JRadioButton("head bang");
        enableHBComposerButton.setBackground(c);
        composerButtons.add(enableHBComposerButton);
        enableHBComposerButton.setSelected(false);
        enableHBComposerButton.addActionListener(this);
        //set the action command to the name of the composer
        //class!
        enableHBComposerButton.setActionCommand("HeadBangComposer");
        //composerRadioButtons.add(enableHBComposerButton);
        selectComposerPanel.add(enableHBComposerButton);
        
        JTextArea description = new JTextArea(HeadBangComposer.oldDesc);
        description.setColumns(50);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setBackground(c);
        description.setEditable(false);
        hBComposerPanel.add(description);
        
        JPanel hBControlPanel = new JPanel();
        hBControlPanel.setBackground(c);
        TitledBorder title = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
            "HeadBang Controls");
        title.setTitleJustification(TitledBorder.CENTER);
        hBControlPanel.setBorder(title);
        BoxLayout blInside = new BoxLayout(hBControlPanel, BoxLayout.Y_AXIS);
        hBControlPanel.setLayout(blInside);
        
        //panel for setting the resolution of the length bins.
        JPanel chunkResolutionPanel = new JPanel();
        chunkResolutionPanel.setBackground(c);
        JLabel hBLabel = new JLabel("         chunk length resolution:");
        chunkResolutionPanel.add(hBLabel);
        headbangBinSlider = new JSlider(JSlider.HORIZONTAL, 100, 12000, 100);
        headbangBinSlider.setBackground(c);
        headbangBinSlider.setValue(5000);
        Hashtable labelTable = new Hashtable();
        labelTable.put( new Integer(100), new JLabel("low") );
        labelTable.put( new Integer(12000), new JLabel("high") );
        headbangBinSlider.setLabelTable( labelTable );
        headbangBinSlider.setPaintLabels(true);
        headbangBinSlider.setMajorTickSpacing(1000);
        headbangBinSlider.setPaintTicks(true);
        
        //panel for setting the length of the output piece.
        JPanel pieceLengthPanel = new JPanel();
        pieceLengthPanel.setBackground(c);
        JLabel hBLabel2 = new JLabel("length of output piece in number of chunks:");
        pieceLengthPanel.add(hBLabel2);
        headbangLengthSlider = new JSlider(JSlider.HORIZONTAL, 10, 1000, 100);
        headbangLengthSlider.setBackground(c);
        headbangLengthSlider.setValue(200);
        Hashtable labelTable2 = new Hashtable();
        labelTable2.put( new Integer(10), new JLabel("short") );
        labelTable2.put( new Integer(1000), new JLabel("long") );
        headbangLengthSlider.setLabelTable( labelTable2 );
        headbangLengthSlider.setPaintLabels(true);
        headbangLengthSlider.setMajorTickSpacing(100);
        headbangLengthSlider.setPaintTicks(true);
        
        pieceLengthPanel.add(headbangLengthSlider);
        
        hBControlPanel.add(pieceLengthPanel);
        
        hBComposerPanel.add(hBControlPanel);
        
        return hBComposerPanel;
    }
 
	private JPanel BuildThresholdComposerGUI(Color c, ButtonGroup composerButtons)
	{
		// Threshold composer
		JPanel thresholdComposerPanel = new JPanel();
		thresholdComposerPanel.setBackground(c);
		BoxLayout bl = new BoxLayout(thresholdComposerPanel, BoxLayout.Y_AXIS);
		thresholdComposerPanel.setLayout(bl);
		enableThresholdComposerButton = new JRadioButton("ThresholdComposer");
		enableThresholdComposerButton.setBackground(c);
		composerButtons.add(enableThresholdComposerButton);
		enableThresholdComposerButton.setSelected(false);
		enableThresholdComposerButton.addActionListener(this);
		//set the action command to the name of the composer
		//class!
		enableThresholdComposerButton.setActionCommand("ThresholdComposer");
		selectComposerPanel.add(enableThresholdComposerButton);

		JTextArea description = new JTextArea(ThresholdComposer.oldDesc);
		description.setColumns(50);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setBackground(c);
		description.setEditable(false);
		thresholdComposerPanel.add(description);
        
		JPanel controlsPanel = new JPanel();
		controlsPanel.setBackground(c);
		TitledBorder title = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			"Threshold Composer Controls");
		title.setTitleJustification(TitledBorder.CENTER);
		controlsPanel.setBorder(title);
        
		JLabel bottomLabel = new JLabel("bottom threshold:");
		controlsPanel.add(bottomLabel);
		thresholdBottomField = new JTextField("1.0");
		controlsPanel.add(thresholdBottomField);
		
		JLabel topLabel = new JLabel("top threshold:");
		controlsPanel.add(topLabel);
		thresholdTopField = new JTextField("10.0");
		controlsPanel.add(thresholdTopField);
        
        JPanel chunkSelectionPanel = new JPanel();
        chunkSelectionPanel.setBackground(c);
		BoxLayout cSPL = new BoxLayout(chunkSelectionPanel, BoxLayout.Y_AXIS);
		chunkSelectionPanel.setLayout(cSPL);
        
        ButtonGroup thresholdGroup = new ButtonGroup();
        
        JLabel chunksLabel = new JLabel("use only chunks: ");
		chunkSelectionPanel.add(chunksLabel);
		insideThresholdButton = new JRadioButton("inside thresholds");
		insideThresholdButton.setBackground(c);
		thresholdGroup.add(insideThresholdButton);
		chunkSelectionPanel.add(insideThresholdButton);
		outsideThresholdButton = new JRadioButton("outside thresholds");
		outsideThresholdButton.setBackground(c);
		thresholdGroup.add(outsideThresholdButton);
		insideThresholdButton.setSelected(true);
		
		chunkSelectionPanel.add(outsideThresholdButton);
		controlsPanel.add(chunkSelectionPanel);
		thresholdComposerPanel.add(controlsPanel);
        
		return thresholdComposerPanel;
	}

	private JPanel BuildRotComposerGUI(Color c, ButtonGroup composerButtons)
	{
		// Rot composer
		JPanel rotComposerPanel = new JPanel();
		rotComposerPanel.setBackground(c);
		BoxLayout bl = new BoxLayout(rotComposerPanel, BoxLayout.Y_AXIS);
		rotComposerPanel.setLayout(bl);
		enableRotComposerButton = new JRadioButton("Rot");
		enableRotComposerButton.setBackground(c);
		composerButtons.add(enableRotComposerButton);
		enableRotComposerButton.setSelected(false);
		enableRotComposerButton.addActionListener(this);
		//set the action command to the name of the composer
		//class!
		enableRotComposerButton.setActionCommand("RotComposer");
		selectComposerPanel.add(enableRotComposerButton);

		JTextArea description = new JTextArea(RotComposer.oldDesc);
		description.setColumns(50);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setBackground(c);
		description.setEditable(false);
		rotComposerPanel.add(description);
        
		JPanel controlsPanel = new JPanel();
		controlsPanel.setBackground(c);
		TitledBorder title = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			"RotComposer Controls");
		title.setTitleJustification(TitledBorder.CENTER);
		controlsPanel.setBorder(title);
        
		JLabel bpmLabel = new JLabel("beats per measure:");
		controlsPanel.add(bpmLabel);
		rotBeatsPerMeasureField = new JTextField("4");
		controlsPanel.add(rotBeatsPerMeasureField);
		
		JLabel ptrLabel = new JLabel("beats to rotate:");
		controlsPanel.add(ptrLabel);
		rotNumPositionsField = new JTextField("1");
		controlsPanel.add(rotNumPositionsField);
		
		ButtonGroup directionGroup = new ButtonGroup();
		
		rotLeftButton = new JRadioButton("rotate left");
		rotLeftButton.setBackground(c);
		directionGroup.add(rotLeftButton);
		rotRightButton = new JRadioButton("rotate right");
		rotRightButton.setBackground(c);
		directionGroup.add(rotRightButton);
		rotRightButton.setSelected(true);

		controlsPanel.add(rotLeftButton);
		controlsPanel.add(rotRightButton);
		
		rotComposerPanel.add(controlsPanel);
		
		return rotComposerPanel;
	}
	
	private JPanel BuildEDLComposerGUI(Color c, ButtonGroup composerButtons)
	{		
		JPanel eDLComposerPanel = new JPanel();
		BoxLayout bl = new BoxLayout(eDLComposerPanel, BoxLayout.Y_AXIS);
		eDLComposerPanel.setLayout(bl);

		enableEDLComposerButton = new JRadioButton("EDL");
		enableEDLComposerButton.setBackground(c);
		composerButtons.add(enableEDLComposerButton);
		enableEDLComposerButton.setSelected(false);
		enableEDLComposerButton.addActionListener(this);
		//set the action command to the name of the composer class!
		enableEDLComposerButton.setActionCommand("EDLComposer");
		selectComposerPanel.add(enableEDLComposerButton);
		
		JTextArea description = new JTextArea(EDLComposer.oldDesc);
		description.setColumns(50);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setBackground(c);
		description.setEditable(false);
		eDLComposerPanel.add(description);

		JPanel controlsPanel = new JPanel();
		controlsPanel.setBackground(c);
		TitledBorder title = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
			"EDL Controls");
		title.setTitleJustification(TitledBorder.CENTER);
		controlsPanel.setBorder(title);
		
		JLabel edlComposerLabel = new JLabel("input EDL file:");
		controlsPanel.add(edlComposerLabel);
		inputEDLFileField = new JTextField("input .edl file");
		inputEDLFileField.setColumns(20);
		inputEDLFileField.addActionListener(this);
		inputEDLFileField.setActionCommand("setInputEDLFile");
		controlsPanel.add(inputEDLFileField);
		JButton iEDLBrowseButton = new JButton("browse");
		iEDLBrowseButton.setBackground(c);
		iEDLBrowseButton.addActionListener(this);
		iEDLBrowseButton.setActionCommand("browseInputEDLFile");
		controlsPanel.add(iEDLBrowseButton);

		eDLComposerPanel.add(controlsPanel);
		
		return eDLComposerPanel;
	}

	private JPanel BuildVQComposerGUI(Color c, ButtonGroup composerButtons)
	{		
		JPanel panel = new JPanel();
		BoxLayout bl = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(bl);

		JRadioButton enableButton = new JRadioButton("VQ");
		enableButton.setBackground(c);
		composerButtons.add(enableButton);
		enableButton.setSelected(false);
		enableButton.addActionListener(this);
		//set the action command to the name of the composer class!
		enableButton.setActionCommand("VQComposer");
		selectComposerPanel.add(enableButton);
		
		JTextArea description = new JTextArea(VQComposer.oldDesc);
		description.setColumns(50);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setBackground(c);
		description.setEditable(false);
		panel.add(description);

		JPanel controlsPanel = new JPanel();
        bl = new BoxLayout(controlsPanel, BoxLayout.Y_AXIS);
		controlsPanel.setLayout(bl);
		controlsPanel.setBackground(c);
		TitledBorder title = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
			"VQ Controls");
		title.setTitleJustification(TitledBorder.CENTER);
		controlsPanel.setBorder(title);
		
        JPanel p = new JPanel();
        p.setBackground(c);
		p.add(new JLabel("number of codewords:"));
        vqNumCodewords = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        vqNumCodewords.setBackground(c);
        vqNumCodewords.setPaintLabels(true);
        vqNumCodewords.setMajorTickSpacing(25);
        vqNumCodewords.setPaintTicks(true);		
		p.add(vqNumCodewords);
		controlsPanel.add(p);

        p = new JPanel();
        p.setBackground(c);
		p.add(new JLabel("beats per codeword:"));
        vqBeatsPerCW = new JSlider(JSlider.HORIZONTAL, 0, 16, 1);
        vqBeatsPerCW.setBackground(c);
        vqBeatsPerCW.setPaintLabels(true);
        vqBeatsPerCW.setMajorTickSpacing(2);
        vqBeatsPerCW.setPaintTicks(true);		
		p.add(vqBeatsPerCW);
		controlsPanel.add(p);

        p = new JPanel();
        p.setBackground(c);
        vqQuantizeTrainingFile = new JCheckBox("quantize training file");
        vqQuantizeTrainingFile.setBackground(c);
        vqQuantizeTrainingFile.setActionCommand("vqQuantizeTrainingFile");
        vqQuantizeTrainingFile.addActionListener(this);
        p.add(vqQuantizeTrainingFile);
        controlsPanel.add(p);

        p = new JPanel();
        p.setBackground(c);
        p.add(new JLabel("features file to quantize:"));
        vqFeatFileField = new JTextField(".feat file");
        vqFeatFileField.setColumns(20);
        vqFeatFileField.addActionListener(this);
        vqFeatFileField.setActionCommand("setVQFeaturesFile");
        p.add(vqFeatFileField);
        vqBrowseButton = new JButton("browse");
        vqBrowseButton.setBackground(c);
        vqBrowseButton.addActionListener(this);
        vqBrowseButton.setActionCommand("browseVQFeaturesFile");
        p.add(vqBrowseButton);
        controlsPanel.add(p);

		panel.add(controlsPanel);
		
		return panel;
	}

	private JPanel BuildHMMComposerGUI(Color c, ButtonGroup composerButtons)
	{		
		JPanel panel = new JPanel();
		BoxLayout bl = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(bl);

		JRadioButton enableButton = new JRadioButton("HMM");
		enableButton.setBackground(c);
		composerButtons.add(enableButton);
		enableButton.setSelected(false);
		enableButton.addActionListener(this);
		//set the action command to the name of the composer class!
		enableButton.setActionCommand("HMMComposer");
		selectComposerPanel.add(enableButton);
		
		JTextArea description = new JTextArea(HMMComposer.oldDesc);
		description.setColumns(50);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setBackground(c);
		description.setEditable(false);
		panel.add(description);

		JPanel controlsPanel = new JPanel();
        bl = new BoxLayout(controlsPanel, BoxLayout.Y_AXIS);
		controlsPanel.setLayout(bl);
		controlsPanel.setBackground(c);
		TitledBorder title = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
			"HMM Controls");
		title.setTitleJustification(TitledBorder.CENTER);
		controlsPanel.setBorder(title);
		
        JPanel p = new JPanel();
        p.setBackground(c);
		p.add(new JLabel("number of states:"));
        hmmNumStates = new JSlider(JSlider.HORIZONTAL, 0, 100, 25);
        hmmNumStates.setBackground(c);
        hmmNumStates.setPaintLabels(true);
        hmmNumStates.setMajorTickSpacing(25);
        hmmNumStates.setPaintTicks(true);		
		p.add(hmmNumStates);
		controlsPanel.add(p);

        p = new JPanel();
        p.setBackground(c);
		p.add(new JLabel("beats per state:"));
        hmmBeatsPerState = new JSlider(JSlider.HORIZONTAL, 0, 16, 4);
        hmmBeatsPerState.setBackground(c);
        hmmBeatsPerState.setPaintLabels(true);
        hmmBeatsPerState.setMajorTickSpacing(2);
        hmmBeatsPerState.setPaintTicks(true);		
		p.add(hmmBeatsPerState);
		controlsPanel.add(p);

        p = new JPanel();
        p.setBackground(c);
		p.add(new JLabel("generated sequence length (chunks):"));
        hmmSequenceLength = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        hmmSequenceLength.setBackground(c);
        hmmSequenceLength.setPaintLabels(true);
        hmmSequenceLength.setMajorTickSpacing(50);
        hmmSequenceLength.setPaintTicks(true);		
		p.add(hmmSequenceLength);
		controlsPanel.add(p);

		panel.add(controlsPanel);
		
		return panel;
	}

	private JPanel BuildShoobyComposerGUI(Color c, ButtonGroup composerButtons)
	{
		//nearest neighbor composer
		JPanel shoobyComposerPanel = new JPanel();
		BoxLayout bl = new BoxLayout(shoobyComposerPanel, BoxLayout.Y_AXIS);
        shoobyComposerPanel.setLayout(bl);

		shoobyComposerPanel.setBackground(c);
		enableShoobyComposerButton = new JRadioButton("Shooby Taylor");
		enableShoobyComposerButton.setBackground(c);
		composerButtons.add(enableShoobyComposerButton);
		enableShoobyComposerButton.setSelected(false);
		enableShoobyComposerButton.addActionListener(this);
		//set the action command to the name of the composer class!
		enableShoobyComposerButton.setActionCommand("ShoobyComposer");
		selectComposerPanel.add(enableShoobyComposerButton);
		
		JTextArea description = new JTextArea(ShoobyComposer.oldDesc);
		description.setColumns(50);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setBackground(c);
        description.setEditable(false);
		shoobyComposerPanel.add(description);
		
		JPanel controlsPanel = new JPanel();
        controlsPanel.setBackground(c);
        TitledBorder title = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
            "Shooby Taylor 'Controls'");
        title.setTitleJustification(TitledBorder.CENTER);
        controlsPanel.setBorder(title);
		
		JLabel lengthLabel = new JLabel("output file length (secs):");
		controlsPanel.add(lengthLabel);
		shoobyFileLengthField = new JTextField("100");
		controlsPanel.add(shoobyFileLengthField);
		
		JLabel clumpLabel = new JLabel("maximum clump size:");
		controlsPanel.add(clumpLabel);
		shoobyClumpWidthField = new JTextField("4");
		controlsPanel.add(shoobyClumpWidthField);
		
		//panel for setting the Shoobyness.
		JPanel pieceLengthPanel = new JPanel();
        JLabel intensityLabel = new JLabel("intensity of Shooby's scat:");
        pieceLengthPanel.add(intensityLabel);
        shoobyDrunkennessSlider = new JSlider(JSlider.HORIZONTAL, 10, 1000, 100);
        shoobyDrunkennessSlider.setBackground(c);
        shoobyDrunkennessSlider.setValue(200);
        Hashtable labelTable = new Hashtable();
        labelTable.put( new Integer(10), new JLabel("shwee") );
        labelTable.put( new Integer(1000), new JLabel("nd raaw pd paw") );
        shoobyDrunkennessSlider.setLabelTable( labelTable );
        shoobyDrunkennessSlider.setPaintLabels(true);
        shoobyDrunkennessSlider.setMajorTickSpacing(100);
        shoobyDrunkennessSlider.setPaintTicks(true);
        controlsPanel.add(shoobyDrunkennessSlider);
		
        shoobyComposerPanel.add(controlsPanel);
        
		return shoobyComposerPanel;
	}

	public void enableDisplayButton(boolean enable)
	{
		displayComposerFeaturesButton.setEnabled(enable);		
	}

    private String[] browseFile(String extension)
    {
        String names[] = GUIUtils.FileSelector(GUIUtils.OPEN, dataDirectory, 
                                               meapsoftGUI.jframe);

        if(!names[1].endsWith(extension))
            GUIUtils.ShowDialog("Please select a ." + extension + " file!", 
                                GUIUtils.MESSAGE, meapsoftGUI.jframe);   
        
        return names;
    }

    private String[] setFile(String name, String extenstion)
    {
        String fullPath = dataDirectory + slash + name;

        // does name contain a full path?
        if((new File(name)).isAbsolute())
            fullPath = name;
        
        String[] nameSplit = name.split("["+slash+"]");
        String shortPath = nameSplit[nameSplit.length-1];
        
        if(!fullPath.endsWith(extenstion))
            GUIUtils.ShowDialog("Please select a ." + extenstion + " file!", 
                                GUIUtils.MESSAGE, meapsoftGUI.jframe);

        String[] names = {fullPath, shortPath};

        return names;
    }

	public void actionPerformed(ActionEvent arg0)
	{
		String command = arg0.getActionCommand();
		
		if (command.equals("browseMUChunkDBFeaturesFile"))
		{
			String names[] = browseFile("feat");
			chunkDBFeaturesNameFull = names[0];
			chunkDBFeaturesNameShort = names[1];
            mashupChunkDBFileField.setText(chunkDBFeaturesNameShort);
		}
		else if (command.equals("setMUChunkDBFeaturesFile"))
		{
            String[] names = setFile(mashupChunkDBFileField.getText(), "feat");
			chunkDBFeaturesNameFull = names[0];
			chunkDBFeaturesNameShort = names[1];
            //mashupChunkDBFileField.setText(chunkDBFeaturesNameShort);
		}
		else if (command.equals("browseInputEDLFile"))
		{
			String names[] = browseFile("edl");
			inputEDLFileNameFull = names[0];
			inputEDLFileNameShort = names[1];
			
            dataBaseName = inputEDLFileNameShort.replaceAll(".edl", "");
            UpdateFileNames();
            outputEDLFileName = dataBaseName + ".out.edl";
            inputEDLFileName = outputEDLFileName;
            meapsoftGUI.UpdateInfoTexts();
            inputEDLFileField.setText(inputEDLFileNameShort);
		}
		else if (command.equals("setInputEDLFile"))
		{
			String[] names = setFile(inputEDLFileField.getText(), "edl");
			inputEDLFileNameFull = names[0];
			inputEDLFileNameShort = names[1];

            dataBaseName = inputEDLFileNameShort.replaceAll(".edl", "");
            UpdateFileNames();
            meapsoftGUI.UpdateInfoTexts();
            //inputEDLFileField.setText(inputEDLFileNameShort);
		}
        else if (command.equals("SortComposer"))
		{			
			//System.out.println("adding sort composer gui...");
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(0));
		}
		else if (command.equals("NNComposer"))
		{
			//System.out.println("adding NN composer gui...");
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(1));
		}
		else if (command.equals("BlipComposer"))
		{
			//System.out.println("adding blip composer gui...");
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(2));
		}
		else if (command.equals("MashupComposer"))
		{
			//System.out.println("adding mashup composer gui...");
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(3));
		}
		else if (command.equals("MeapaeMComposer"))
		{
			//System.out.println("adding MeapaeM composer gui...");
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(4));
		}
		else if (command.equals("IntraChunkShuffleComposer"))
		{
			//System.out.println("adding IntraChunkShuffleComposer composer gui...");
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(5));
		}
		else if (command.equals("HeadBangComposer"))
		{
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(6));
		}
		else if (command.equals("ThresholdComposer"))
		{
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(7));		
		}
		else if (command.equals("RotComposer"))
		{
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(8));
		}
		else if (command.equals("EDLComposer"))
		{
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(9));
		}
		else if (command.equals("VQComposer"))
		{
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(10));
		}
		else if (command.equals("HMMComposer"))
		{
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(11));
		}
		else if (command.equals("ShoobyComposer"))
		{
			//System.out.println("adding Shooby composer gui...");
			selectedComposer = command;
			controlsPanel.removeAll();
			controlsPanel.add((JPanel)controlsPanels.elementAt(12));
		}
		else if (command.equals("fade"))
        {
            boolean enable = fadeInOutChunks.isSelected() 
                || crossfadeChunks.isSelected();
            fadeDurationSlider.setEnabled(enable);
        }
        else if (command.equals("gain"))
        {
        		boolean enable = addGainChunks.isSelected();
        		gainValueField.setEnabled(enable);
        }
		else if (command.equals("displayComposerFeatures"))
		{
            DataDisplayPanel.spawnWindow(edlFile.getFeatures(), edlFile.filename);
		}
		else if (command.equals("displayComposedStructure"))
		{		
			Visualizer visualizer = new Visualizer(featFile, edlFile);
		}
        else if (command.equals("browseVQFeaturesFile"))
		{
			String names[] = browseFile("feat");
			vqFeaturesNameFull = names[0];
			vqFeaturesNameShort = names[1];
            
            vqFeatFileField.setText(vqFeaturesNameShort);
		}
		else if (command.equals("setVQFeaturesFile"))
		{
            String names[] = setFile(vqFeatFileField.getText(), "feat");
			vqFeaturesNameFull = names[0];
			vqFeaturesNameShort = names[1];
            //vqFeatFileField.setText(vqFeaturesNameShort);
		}
        else if (command.equals("vqQuantizeTrainingFile"))
        {
            boolean b = !vqQuantizeTrainingFile.isSelected();
            vqFeatFileField.setEnabled(b);
            vqBrowseButton.setEnabled(b);
        }
        else if (command.equals("run_composer"))
        {
        	meapsoftGUI.RunComposerButtonPressed();
        }
		
		invalidate();
		validate();
		RefreshGUI();
    }    
	
	public synchronized int run()
	{			
        if(!enableBox.isSelected())
            return 0;

        Composer composer = null;
		if (selectedComposer.equals("SortComposer"))
		{
			composer = new SortComposer(featFile, edlFile);
            ((SortComposer)composer).setReverseSort(highLowSortButton.isSelected());
            ((SortComposer)composer).setNormalizeFeatures(normalizeFeatCB.isSelected());
		}
		else if (selectedComposer.equals("NNComposer"))
		{
			composer = new NNComposer(featFile, edlFile);
		}
		else if (selectedComposer.equals("BlipComposer"))
		{
			composer = new BlipComposer(featFile, edlFile);
            ((BlipComposer)composer).setBlipWav(dataDirectory+slash+"blip.wav");
		}
		else if (selectedComposer.equals("MashupComposer"))
		{
			if (chunkDBFeaturesNameFull == null)
			{
				GUIUtils.ShowDialog("MashupComposer: Please select a chunk database features file!", GUIUtils.MESSAGE, meapsoftGUI.jframe);
				return -1;
			}
			
			edlFile = new EDLFile(dataDirectory + slash + dataBaseName + "_using_" + 
				chunkDBFeaturesNameShort + ".edl");

            FeatFile chunkDBFile = new FeatFile(chunkDBFeaturesNameFull);
            
			if (!(new File(chunkDBFeaturesNameFull).exists()))
			{
				GUIUtils.ShowDialog("MashupComposer: Please select a chunk database features file!", GUIUtils.MESSAGE, meapsoftGUI.jframe);
				return -1;
			}
				
            composer = new MashupComposer(featFile, chunkDBFile, edlFile);
		}
		else if (selectedComposer.equals("MeapaeMComposer"))
		{
			composer = new MeapaeMComposer(featFile, edlFile);
		}
		else if (selectedComposer.equals("IntraChunkShuffleComposer"))
		{
			int numSubChunks = 4;
			
			try
			{
				numSubChunks = new Integer(intraChunkShuffleNumChunksField.getText()).intValue();
			}
			catch (Exception e)
			{
				GUIUtils.ShowDialog("The number of sub chunks must be >= 2.", GUIUtils.MESSAGE, meapsoftGUI.jframe);
                return -1;
			}
			composer = new IntraChunkShuffleComposer(featFile, edlFile, numSubChunks);
		}
		else if (selectedComposer.equals("HeadBangComposer"))
		{
			composer = new HeadBangComposer(featFile, edlFile, headbangBinSlider.getValue(), headbangLengthSlider.getValue());
		}
		else if (selectedComposer.equals("ThresholdComposer"))
		{
			double top = new Double(thresholdTopField.getText()).doubleValue();
			double bottom = new Double(thresholdBottomField.getText()).doubleValue();
			composer = new ThresholdComposer(featFile, edlFile, top, bottom, insideThresholdButton.isSelected());
		}
		else if (selectedComposer.equals("RotComposer"))
		{
			int bpm = new Integer(rotBeatsPerMeasureField.getText()).intValue();
			int positions = new Integer(rotNumPositionsField.getText()).intValue();
			composer = new RotComposer(featFile, edlFile, bpm, positions, rotLeftButton.isSelected());
		}
		else if (selectedComposer.equals("EDLComposer"))
		{
			EDLFile input = new EDLFile(inputEDLFileNameFull);
			composer = new EDLComposer(input, edlFile);
		}
		else if (selectedComposer.equals("VQComposer"))
		{
			composer = new VQComposer(featFile, edlFile);

            VQComposer vqc = (VQComposer)composer;
            vqc.setCodebookSize(vqNumCodewords.getValue());
            vqc.setBeatsPerCodeword(vqBeatsPerCW.getValue());

            if(vqFeaturesNameFull != null)
            {
                if (!(new File(vqFeaturesNameFull).exists()))
                {
                    GUIUtils.ShowDialog("VQComposer: Please select a valid feature file!", 
                                        GUIUtils.MESSAGE, meapsoftGUI.jframe);
                    return -1;
                }
                else
                    vqc.setFeatsToQuantize(new FeatFile(vqFeaturesNameFull));
            }

            if(vqQuantizeTrainingFile.isSelected())
                vqc.setFeatsToQuantize(featFile);
		}
		else if (selectedComposer.equals("HMMComposer"))
		{
			composer = new HMMComposer(featFile, edlFile);

            ((HMMComposer)composer).setCodebookSize(
                hmmNumStates.getValue());

            ((HMMComposer)composer).setBeatsPerCodeword(
                hmmBeatsPerState.getValue());

            ((HMMComposer)composer).setSequenceLength(
                hmmSequenceLength.getValue());
		}
		else if (selectedComposer.equals("ShoobyComposer"))
		{
			int outFileLength = new Integer(shoobyFileLengthField.getText()).intValue();
			int maxClumpWidth = new Integer(shoobyClumpWidthField.getText()).intValue();
			composer = new ShoobyComposer(featFile, edlFile, outFileLength, maxClumpWidth, shoobyDrunkennessSlider.getValue());
		}
		else
		{
			GUIUtils.ShowDialog("I don't recognize that composer!", GUIUtils.MESSAGE, meapsoftGUI.jframe);
			return -1;
		}
		
        double crossfade = (double)fadeDurationSlider.getValue()/1000;
        if(fadeInOutChunks.isSelected() & crossfade > 0)
            composer.addCommand("fade("+crossfade+")");
        if(crossfadeChunks.isSelected() & crossfade > 0)
            composer.addCommand("crossfade("+crossfade+")");
        if(reverseChunks.isSelected())
            composer.addCommand("reverse");
        if(addGainChunks.isSelected())
        {
        	double gain = Double.parseDouble(gainValueField.getText());
        	
        	composer.addCommand("gain(" + gain + ")");
        }

        composer.writeMEAPFile = MEAPUtil.writeMEAPFile;
        
        JPanel progressPanel = new JPanel();
        progressPanel.add(new JLabel("Composing: "));
        JProgressBar progressBar = new JProgressBar(composer.getProgress());
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar);
        meapsoftGUI.setProgressPanel(progressPanel);

        try
        {
            composer.doComposer();
        }
        catch(Exception e)
        {
			GUIUtils.ShowDialog(e, "Error running composer", GUIUtils.MESSAGE, meapsoftGUI.jframe);
            return -1;
        }

        //displayComposerFeaturesButton.setEnabled(true);
        enableDisplayButton(true);
        
        
        return 0;
    }
}
