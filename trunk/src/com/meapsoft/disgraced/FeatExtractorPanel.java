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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import util.RTSI;

import com.meapsoft.DumpFeatsToTabsFile;
import com.meapsoft.FeatExtractor;
import com.meapsoft.featextractors.FeatureExtractor;
import com.meapsoft.featextractors.MetaFeatureExtractor;
import com.meapsoft.visualizer.SingleFeatureWaveformPanel;


/**
 * GUI interface for FeatExtractor.  
 *
 * @author Douglas Repetto (douglas@music.columbia.edu)
 * and the MEAP team
 */
public class FeatExtractorPanel extends MEAPsoftGUIPanel
{
	//feature extractor GUI
	JCheckBox enableBox;
	Vector featureCheckBoxes;
	Vector featureDescriptions;
    Vector featureWeightFields;
	JLabel inputSegmentsFileLabel;
	JLabel outputFeaturesFileLabel;
	JTextField inputSegmentsFileField;
	JTextField outputFeaturesFileField;
	JButton displayFeaturesButton;
    JCheckBox clearNonMetaFeatures;
    
    Vector featurePanels = new Vector();
    
    JButton runFeatExtButton;
    JButton dumpFeatsToTabsButton;
    JButton printFeatStatsButton;

    /**
     * Create a new FeatExtractor panel.
     */
    public FeatExtractorPanel(MEAPsoftGUI msg)
	{
        super(msg);
        BuildFeatureExtractorsGUI();

        title = "Feature Extractor";
        helpURL += "#" + title;
    }

	private void BuildFeatureExtractorsGUI()
	{
		featureDescriptions = new Vector();
		
		Color c = new Color((int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127));
        color = c;

		setBackground(c);
		BoxLayout fbl = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(fbl);
		
		Vector classNames = SniffFeatureExtractors();

		featureCheckBoxes = new Vector(classNames.size());
        featureWeightFields = new Vector(classNames.size());
		
		JPanel enablePanel = new JPanel();
		enablePanel.setBackground(c);
		
		enableBox = new JCheckBox("ENABLE FEATURE EXTRACTOR");
		enableBox.setBackground(c);
		enableBox.setSelected(true);
		enablePanel.add(enableBox);
		
		enablePanel.add(helpButton);
		
		add(enablePanel);
		
		JPanel controlsPanel = new JPanel();
		controlsPanel.setBackground(c);
		BoxLayout fecp = new BoxLayout(controlsPanel, BoxLayout.Y_AXIS);
		controlsPanel.setLayout(fecp);
		
		JPanel extractorInputFNPanel = new JPanel();
		extractorInputFNPanel.setBackground(c);
		JLabel fEINL = new JLabel("input segments file: ");
		extractorInputFNPanel.add(fEINL);
		inputSegmentsFileLabel = new JLabel(" " + dataBaseName + ".seg ");
		inputSegmentsFileLabel.setOpaque(true);
		inputSegmentsFileLabel.setBackground(c.darker());
		extractorInputFNPanel.add(inputSegmentsFileLabel);
		controlsPanel.add(extractorInputFNPanel);

		//int numFeatures = classNames.size();

		JPanel selectFeaturesPanel = new JPanel();
		selectFeaturesPanel.setBackground(c);
		TitledBorder title = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
			"Feature Extractors");
		title.setTitleJustification(TitledBorder.CENTER);
		selectFeaturesPanel.setBorder(title);
		controlsPanel.add(selectFeaturesPanel);


		JPanel selectMetaFeaturesPanel = new JPanel();
		selectMetaFeaturesPanel.setBackground(c);
		TitledBorder metaTitle = BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
			"Meta Feature Extractors");
        selectMetaFeaturesPanel.setBorder(metaTitle);
		metaTitle.setTitleJustification(TitledBorder.CENTER);
        controlsPanel.add(selectMetaFeaturesPanel);
		
        clearNonMetaFeatures = new JCheckBox("clear non-meta features");
        clearNonMetaFeatures.setBackground(c); 
        clearNonMetaFeatures.setSelected(true);
        clearNonMetaFeatures.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlsPanel.add(clearNonMetaFeatures);

		int fENum = 0;
		int mFENum = 0;
		//JPanel fEPanel = null;
		//JPanel mFEPanel = null;
		for (int i = 0; i < classNames.size(); i++)
		{
			String name = (String)classNames.elementAt(i);
			
			FeatureExtractor f = null;
			try
			{
				f = (FeatureExtractor)(Class.forName("com.meapsoft.featextractors." + name).newInstance());
			}
			catch (Exception e)
			{
				//we should never get here!
				e.printStackTrace();
			}

			//not using actions, we'll just check their states 
			//when the GO! button is hit...
			JToggleButton cb = new JCheckBox(name);
            if(f instanceof MetaFeatureExtractor)
                cb = new JRadioButton(name);

			featureCheckBoxes.add(cb);
			cb.setBackground(c);
			cb.setToolTipText((String)featureDescriptions.elementAt(i));
			
            cb.setActionCommand("enableFeatExt"+i);
            cb.addActionListener(this);

            JFormattedTextField tf = new JFormattedTextField(NumberFormat.getNumberInstance());
            tf.setValue(new Double(1.0));
            tf.setColumns(2);
            tf.setBackground(c);
            tf.setEnabled(false);
            featureWeightFields.add(tf);

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setBackground(c);
            panel.add(cb);
			panel.add(tf);

			if(!(f instanceof MetaFeatureExtractor))
			{
				fENum++;
                if (name.equals("AvgPitchSimple"))
                {
                    cb.setSelected(true);
                    tf.setEnabled(true);
                }

                selectFeaturesPanel.add(panel);
            }
			else
			{
				mFENum++;
                // hack to keep the MFE panel symmetric when it
                // contains only one row of checkboxes.
                if(mFENum < 4)
                    panel.setLayout(new FlowLayout());
                selectMetaFeaturesPanel.add(panel);
			}
		}
        selectFeaturesPanel.setLayout(new GridLayout((int)(fENum/4.0 + 1), 4));
        selectMetaFeaturesPanel.setLayout(new GridLayout((int)(mFENum/4.0 + 1), 4));

		add(controlsPanel);

		JPanel extractorOutputFNPanel = new JPanel();
		extractorOutputFNPanel.setBackground(c);
		JLabel fEONL = new JLabel("output features file: ");
		extractorOutputFNPanel.add(fEONL);
		outputFeaturesFileLabel = new JLabel(" " + dataBaseName + ".feat ");
		outputFeaturesFileLabel.setOpaque(true);
		outputFeaturesFileLabel.setBackground(c.darker());
		extractorOutputFNPanel.add(outputFeaturesFileLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
		add(extractorOutputFNPanel);
		
		JPanel displayFeaturesPanel = new JPanel();
		displayFeaturesPanel.setBackground(c);
		displayFeaturesButton = new JButton("display extracted features");
		displayFeaturesButton.setEnabled(false);
		displayFeaturesButton.addActionListener(this);
		displayFeaturesButton.setActionCommand("displayFeatures");
		displayFeaturesButton.setBackground(c);
		displayFeaturesPanel.add(displayFeaturesButton);
		add(displayFeaturesPanel);
		
        JPanel runPanel = new JPanel();
        runPanel.setBackground(c);
        
        runFeatExtButton = new JButton("run feature extractors");
        runFeatExtButton.setBackground(c);
        runFeatExtButton.addActionListener(this);
        runFeatExtButton.setActionCommand("run_feat_ext");
        runPanel.add(runFeatExtButton);
        add(runPanel);
        
        
        dumpFeatsToTabsButton = new JButton("dump feats to tabs");
        dumpFeatsToTabsButton.setBackground(c);
        dumpFeatsToTabsButton.addActionListener(this);
        dumpFeatsToTabsButton.setActionCommand("feats_to_tabs");
        runPanel.add(dumpFeatsToTabsButton);
        add(runPanel);
        
        printFeatStatsButton = new JButton("print feat stats");
        printFeatStatsButton.setBackground(c);
        printFeatStatsButton.addActionListener(this);
        printFeatStatsButton.setActionCommand("feat_stats");
        runPanel.add(printFeatStatsButton);
        add(runPanel);
        
        //this is a member of our parent class
        waveSpectPanel = new JPanel();
		waveSpectPanel.setLayout(new BoxLayout(waveSpectPanel, BoxLayout.Y_AXIS));
        waveSpectPanel.setBackground(c);
        add(waveSpectPanel);
	}

	private Vector SniffFeatureExtractors()
	{
        Vector v = null;
        try 
        {
            v = RTSI.findnames("com.meapsoft.featextractors", Class.forName("com.meapsoft.featextractors.FeatureExtractor"));
            // \todo{where else should we look for feature extractors?}
            // also check current directory:
            Vector v2 = RTSI.findnames(System.getProperty("user.dir"), Class.forName("com.meapsoft.featextractors.FeatureExtractor")); 
            if(v2 != null)
                v.addAll(v2);
            
            for (int i = 0; i < v.size(); i++)
            {
				FeatureExtractor f = null;
				
				try
				{
					String name = "com.meapsoft.featextractors." + (String)v.elementAt(i);
					f = (FeatureExtractor)(Class.forName(name).newInstance());
					featureDescriptions.add(f.description());
				}
				catch (Exception e)
				{
					GUIUtils.ShowDialog("", GUIUtils.MESSAGE, meapsoftGUI.jframe);	
                    //ShowDialog(e, "", FATAL_ERROR);
				}
            }
        }
        catch(ClassNotFoundException e)
        {
			GUIUtils.ShowDialog("", GUIUtils.FATAL_ERROR, meapsoftGUI.jframe);	
        }

        return v;
	}

	public void enableDisplayButton(boolean enable)
	{
		displayFeaturesButton.setEnabled(enable);		
	}
	
	public void actionPerformed(ActionEvent arg0)
	{
		String command = arg0.getActionCommand();

		if (command.equals("displayFeatures"))
		{
            DataDisplayPanel.spawnWindow(featFile.getFeatures(), featFile.filename);
        }
        else if(command.startsWith("enableFeatExt"))
        {
            int i = Integer.parseInt(command.substring("enableFeatExt".length()));

            ((JTextField)featureWeightFields.get(i)).setEnabled(
                ((JToggleButton)featureCheckBoxes.get(i)).isSelected());
        }
        else if (command.equals("run_feat_ext"))
        {
        	meapsoftGUI.RunFeatExtButtonPressed();
        }
        else if (command.equals("feats_to_tabs"))
        {
    		featFile.dumpFeatsToTabsFile();
        }
        else if (command.equals("feat_stats"))
        {
        	System.out.println(featFile.getFeatureStats());
        }
    }

	public synchronized int run()
	{
        if(!enableBox.isSelected())
        {
            // not all composers need features, so by default set the
            // featFile to be the empty segmentFile
            if(featFile == null)
                featFile = segmentFile;

            return 0;
        }

		boolean regularFE = false;
		boolean metaFE = false;
		
        Vector featExts = new Vector();
        Vector featWeights = new Vector();
		for(int i = 0; i < featureCheckBoxes.size(); i++)	
        {
			JToggleButton cb = (JToggleButton)featureCheckBoxes.elementAt(i);

            try
            {
                if (cb.isSelected())
                {
                	FeatureExtractor fe = (FeatureExtractor)(Class.forName("com.meapsoft.featextractors." + cb.getText())
					.newInstance());
					if (fe instanceof MetaFeatureExtractor)
						metaFE = true;
					else
						regularFE = true;
						
					featExts.add(fe);
                    featWeights.add(((JFormattedTextField)featureWeightFields.elementAt(i)).getValue());
                }
            }
            catch(Exception e)
            {
				GUIUtils.ShowDialog(e, "Error loading featextractor", GUIUtils.FATAL_ERROR, meapsoftGUI.jframe);	
                return -1;
            }
            
            if (metaFE && !regularFE)
            {
				GUIUtils.ShowDialog("You need at least one regular feature extractor in order to use a " +
					"meta feature extractor!", GUIUtils.MESSAGE, meapsoftGUI.jframe);
				return -1;	
            }
        }

        FeatExtractor featExtractor = 
            new FeatExtractor(segmentFile, featFile, featExts);
        featExtractor.writeMEAPFile = meapsoftGUI.writeMEAPFile;
        featExtractor.setFeatureExtractorWeights(featWeights);
        featExtractor.setClearNonMetaFeatures(
            clearNonMetaFeatures.isSelected());

        JPanel progressPanel = new JPanel();
        progressPanel.add(new JLabel("Extracting features: "));
        JProgressBar progressBar = new JProgressBar(featExtractor.getProgress());
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar);
        meapsoftGUI.setProgressPanel(progressPanel);

        try
        {				
            // clear whatever chunks may have already been calculated
            featFile.clearChunks();
            
            featExtractor.setup();
            featExtractor.processFeatFiles();

            if(featExtractor.writeMEAPFile)
                featFile.writeFile();
        }
        catch (Exception e)
        {
			GUIUtils.ShowDialog(e, "Error running Feature Extractor", GUIUtils.FATAL_ERROR, meapsoftGUI.jframe);	
            return -1;
		}

        displayFeaturesButton.setEnabled(true);

        return 0;
	}
}
