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

import com.meapsoft.gui.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.meapsoft.ExceptionHandler;
import com.meapsoft.MEAPUtil;
import com.meapsoft.visualizer.SingleFeaturePanelTester;
import com.meapsoft.visualizer.SingleFeatureSpectrumPanel;
import com.meapsoft.visualizer.SingleFeatureWaveformPanel;
import com.meapsoft.visualizer.Visualizer;


/**
 * GUI interface for the MEAPsoft utilities.  
 *
 * @author Douglas Repetto (douglas@music.columbia.edu)
 * and the MEAP team
 */
public class MEAPsoftGUI extends MEAPUtil implements ActionListener, ChangeListener, Runnable
{
    protected JFrame jframe;
	private JPanel mainGuiPanel;
	
	//IO data
	public static String dataDirectory;
	public static String meapsoftDirectory;
	
	//tabbed GUI
	private JTabbedPane tabs;
    private JPanel[] stripes = new JPanel[5];

    // MEAPsoft component panels
    public SegmenterPanel segmenterPanel;
    public FeatExtractorPanel featExtPanel;
    public ComposerPanel composerPanel;
    public SynthesizerPanel synthPanel;
    public PreferencesPanel prefsPanel;
    //public ConsolePanel consolePanel;
    
    // we always want these two around
    public SingleFeatureWaveformPanel waveformPanel;
    public SingleFeatureSpectrumPanel spectrumPanel;

	//start button
	private JButton startButton;
    private JPanel goPanel;
    private JLabel statusBar;

    // the thread being used to run the MEAPsoft utilities
    private Thread runThread = null;
    // JPanel containing the progress bar when runThread is being executed
    private JPanel progressPanel = null; 
	
    // main background color of the mainGuiPanel
    private Color bgColor;

    private boolean savedEnableButtonStates[] = {true, true, true, true};
    
    boolean resizeWaiting = false;

	public MEAPsoftGUI()
	{				
        jframe = new JFrame("MEAPsoft-"+version);

		String paths[] = MEAPUtil.getPaths();
		if (paths != null)
		{
			meapsoftDirectory = paths[0];
			dataDirectory = paths[1];
		}
		else
			System.exit(-1);

        // Pop up a dialog box instead of spitting stuff out to the
        // console.
        exceptionHandler = new GUIExceptionHandler(jframe, "");

        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setResizable(true);

		BuildGUI();
        setDefaultInputFile();
        UpdateInfoTexts();
		//RefreshGUI();
		cycleComposers();

		jframe.show();
	}

    private void setDefaultInputFile()
    {
		String[] inputName = new String[2];
		inputName[0] = dataDirectory + slash + "chris_mann.wav";
		inputName[1] = "chris_mann.wav";
		segmenterPanel.SetInputFileName(inputName);
    }

	private void BuildGUI()
	{
		////////////////////
		// meta-GUI setup
		////////////////////

		mainGuiPanel = new JPanel();
		BoxLayout bl = new BoxLayout(mainGuiPanel, BoxLayout.Y_AXIS);
		mainGuiPanel.setLayout(bl);
		jframe.setContentPane(mainGuiPanel);
		
		Action goAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				//System.out.println("got a go...");
				GoButtonPressed();
			}
		};

		KeyStroke keystroke = 
			KeyStroke.getKeyStroke('g');//, java.awt.event.InputEvent.CTRL_MASK);
		mainGuiPanel.getInputMap().put(keystroke, "go");
		mainGuiPanel.getActionMap().put("go", goAction);

		Color c = new Color((int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127),
					(int)(Math.random() * 127 + 127));
        bgColor = c;

		JPanel stripe = new JPanel();
		stripe.setBackground(c);
		
		for (int i = 0; i < 5; i++)
		{
			JPanel p = new JPanel();
			p.add(new JLabel("      MEAP!      "));
			stripes[i] = p;
			stripe.add(p);
		}
		
		mainGuiPanel.add(stripe);
					
		tabs = new JTabbedPane();
		/*
		// Register a change listener
		tabs.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				JTabbedPane pane = (JTabbedPane)evt.getSource();
    
				// Get current tab
				int sel = pane.getSelectedIndex();
				//System.out.println("tab " + sel + " got clicked.");
				JPanel panel = (JPanel)pane.getComponentAt(sel);
				panel.invalidate();
				RefreshGUI();
			}
		});
		*/
		tabs.addChangeListener(this);
		tabs.setBackground(c);
		mainGuiPanel.add(tabs);		
		
        //mainGuiPanel.setPreferredSize(new Dimension(700, 600));

		////////////////////////////
		// now do GUIs for the tabs
		////////////////////////////
		
        //create our two always-present panels
        waveformPanel = new SingleFeatureWaveformPanel();
        waveformPanel.setMinimumSize(new Dimension(400, 100));
        waveformPanel.setPreferredSize(new Dimension(400,100));

        spectrumPanel = new SingleFeatureSpectrumPanel();
        spectrumPanel.setMinimumSize(new Dimension(400, 100));
        spectrumPanel.setPreferredSize(new Dimension(400,100));
        
        //
		segmenterPanel = new SegmenterPanel(this);
        featExtPanel = new FeatExtractorPanel(this);
		composerPanel = new ComposerPanel(this);
        synthPanel = new SynthesizerPanel(this);
        prefsPanel = new PreferencesPanel(this);

        // add tabs and stripes
        tabs.addTab("segmenter", segmenterPanel);
		tabs.setBackgroundAt(0, segmenterPanel.color);
		stripes[0].setBackground(segmenterPanel.color);

        tabs.addTab("feature extractors", featExtPanel);
		tabs.setBackgroundAt(1, featExtPanel.color);
		stripes[1].setBackground(featExtPanel.color);

        tabs.addTab("composers", composerPanel);
		tabs.setBackgroundAt(2, composerPanel.color);
		stripes[2].setBackground(composerPanel.color);

        tabs.addTab("synthesizer", synthPanel);
		tabs.setBackgroundAt(3, synthPanel.color);
		stripes[3].setBackground(synthPanel.color);

        tabs.addTab("prefs/about", prefsPanel);	
		tabs.setBackgroundAt(4, prefsPanel.color);
		stripes[4].setBackground(prefsPanel.color);

        //tabs.addTab("console", consolePanel);	

		/////////////////////
		//go button!
		/////////////////////
		//this appears at the bottom of the screen, but we do it here to use
		//the same color as the rest of the main screen
		goPanel = new JPanel();
		goPanel.setBackground(c);
		startButton = new JButton("go!");
		startButton.setBackground(c);
		startButton.addActionListener(this);
		startButton.setActionCommand("go");
		goPanel.add(startButton);
		
		JButton displayVisualizerButton = new JButton("launch visualizer");
		displayVisualizerButton.addActionListener(this);
		displayVisualizerButton.setActionCommand("launchVisualizer");
		displayVisualizerButton.setBackground(c);
		goPanel.add(displayVisualizerButton);
				
		JButton displayNewVisualizerButton = new JButton("launch new visualizer");
		displayNewVisualizerButton.addActionListener(this);
		displayNewVisualizerButton.setActionCommand("launchNewVisualizer");
		displayNewVisualizerButton.setBackground(c);
		goPanel.add(displayNewVisualizerButton);
		
		mainGuiPanel.add(goPanel);	

        JPanel statusBarPanel = new JPanel(new GridLayout(1,1)); 
        statusBarPanel.setBackground(c.brighter());
        statusBarPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        statusBar = new JLabel("...");
        statusBar.setBackground(c.brighter());
        statusBarPanel.add(statusBar);

        mainGuiPanel.add(statusBarPanel);

		jframe.validate();
		jframe.pack();
		//jframe.repaint();
        //RefreshGUI();
	}
	
	public synchronized void UpdateInfoTexts()
	{	
		//segmenter
		segmenterPanel.outputSegFileLabel.setText(" "  
            + segmenterPanel.outputSegmentsFileName + " ");
		//features
		featExtPanel.inputSegmentsFileLabel.setText(" " 
            + featExtPanel.inputSegmentsFileName + " ");
		featExtPanel.outputFeaturesFileLabel.setText(" " 
            + featExtPanel.outputFeaturesFileName + " ");
		//composer
		composerPanel.inputFileNameLabel.setText(" " 
            + composerPanel.inputFeaturesFileName + " ");
		composerPanel.outputFileNameLabel.setText(" " 
            + composerPanel.outputEDLFileName + " ");
		//synth
		synthPanel.fileNameLabel.setText(" " 
            + synthPanel.inputEDLFileName + " ");
		RefreshGUI();

        segmenterPanel.initSegmentFile();
        featExtPanel.initFeatFile();
		featExtPanel.enableDisplayButton(false);
        composerPanel.initEDLFile();
		composerPanel.enableDisplayButton(false);
	}
	
	public void cycleComposers()
	{
		//select composer tab
		tabs.setSelectedIndex(2);	
		
		int numComposers = composerPanel.controlsPanels.size();
		
		for (int i = 0; i < numComposers; i++)
		{	
			composerPanel.controlsPanel.removeAll();
			composerPanel.controlsPanel.add((JPanel)composerPanel.	controlsPanels.elementAt(i));	
			RefreshGUI();
		}
		
		composerPanel.controlsPanel.removeAll();
		composerPanel.controlsPanel.add((JPanel)composerPanel.	controlsPanels.elementAt(0));	
		RefreshGUI();

		tabs.setSelectedIndex(0);
	}
	
    public void RefreshGUI()
	{				
		mainGuiPanel.invalidate();
		tabs.invalidate();
		segmenterPanel.invalidate();
		featExtPanel.invalidate();
		composerPanel.invalidate();
		synthPanel.invalidate();
		prefsPanel.invalidate();
		goPanel.invalidate();
		
		jframe.invalidate();
		
		jframe.validate();
		//jframe.pack();
		jframe.repaint();		
	}

	public void actionPerformed(ActionEvent arg0)
	{
		String command = arg0.getActionCommand();

		if (command.equals("go"))
		{	
			SaveEnableButtonStates();
			GoButtonPressed();
        }
        if (command.equals("launchVisualizer"))
        {
        	Visualizer visualizer = new Visualizer(composerPanel.featFile, 
        		composerPanel.edlFile);
        }
        if (command.equals("launchNewVisualizer"))
        {
        	SingleFeaturePanelTester newVisualizer = new SingleFeaturePanelTester(null);//segmenterPanel.segmentFile);
        			//composerPanel.featFile);
        }
    }
	
	public void stateChanged(ChangeEvent arg0)
	{
		JTabbedPane pane = (JTabbedPane)arg0.getSource();
	    
		// Get current tab
		int sel = pane.getSelectedIndex();
		MEAPsoftGUIPanel panel = (MEAPsoftGUIPanel)pane.getComponentAt(sel);

		if (panel.waveSpectPanel != null)
		{
			panel.waveSpectPanel.add(waveformPanel);
			panel.waveSpectPanel.add(spectrumPanel);
		
			panel.invalidate();
		}
	}
	
	public void GoButtonPressed()
	{
		runThread = new Thread(this, "MEAPsoftGUI.run");
		runThread.start();
	}
	
	public void RunSegmenterButtonPressed()
	{
		SaveEnableButtonStates();
		segmenterPanel.enableBox.setSelected(true);
		featExtPanel.enableBox.setSelected(false);
		composerPanel.enableBox.setSelected(false);
		synthPanel.enableBox.setSelected(false);
		
		GoButtonPressed();
	}
	
	public void RunFeatExtButtonPressed()
	{
		SaveEnableButtonStates();
		segmenterPanel.enableBox.setSelected(false);
		featExtPanel.enableBox.setSelected(true);
		composerPanel.enableBox.setSelected(false);
		synthPanel.enableBox.setSelected(false);
		
		GoButtonPressed();
	}
	
	public void RunComposerButtonPressed()
	{
		SaveEnableButtonStates();
		segmenterPanel.enableBox.setSelected(false);
		featExtPanel.enableBox.setSelected(false);
		composerPanel.enableBox.setSelected(true);
		synthPanel.enableBox.setSelected(false);
		
		GoButtonPressed();
	}
	
	public void RunSynthButtonPressed()
	{
		SaveEnableButtonStates();
		segmenterPanel.enableBox.setSelected(false);
		featExtPanel.enableBox.setSelected(false);
		composerPanel.enableBox.setSelected(false);
		synthPanel.enableBox.setSelected(true);
		
		GoButtonPressed();
	}
	
	public void SaveEnableButtonStates()
	{
		//save states in case we were triggered by an individual panel
		//System.out.println("saving enable button states...");
		savedEnableButtonStates[0] = segmenterPanel.enableBox.isSelected();
		savedEnableButtonStates[1] = featExtPanel.enableBox.isSelected();
		savedEnableButtonStates[2] = composerPanel.enableBox.isSelected();
		savedEnableButtonStates[3] = synthPanel.enableBox.isSelected();
	}
	
	public void RestoreEnableButtonStates()
	{
		//System.out.println("restoring enable button states...");
		segmenterPanel.enableBox.setSelected(savedEnableButtonStates[0]);
		featExtPanel.enableBox.setSelected(savedEnableButtonStates[1]);
		composerPanel.enableBox.setSelected(savedEnableButtonStates[2]);
		synthPanel.enableBox.setSelected(savedEnableButtonStates[3]);
	}
	
    public synchronized void run()
    {
        jframe.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        writeMEAPFile = prefsPanel.saveFilesPrefBox.isSelected();

        updateStatusBar("running Segmenter on "
                        + segmenterPanel.inputSoundFileNameShort + "...");
        if (segmenterPanel.run() != 0)
        {
            runCleanup("Segmenter error.");
            return;
        }

        updateStatusBar("running Feature Extractor on " 
                        + featExtPanel.inputSegmentsFileName + "...");
        if (featExtPanel.run() != 0)
        {
            runCleanup("Feature Extractor error.");
            return;
        }
        
        updateStatusBar("running Composer on "
                        + composerPanel.inputFeaturesFileName + "...");
        if (composerPanel.run() != 0)
        {
            runCleanup("Composer error.");
            return;
        }

        updateStatusBar("running Synthesizer on "
                        + synthPanel.inputEDLFileName + "...");
        if (synthPanel.run() != 0)
        {
            runCleanup("Synthesizer error.");
            return;
        }

        runCleanup("Finished MEAPing.");
    }

    private void runCleanup(String message)
    {
        jframe.getContentPane().setCursor(Cursor.getDefaultCursor());
        
        if(progressPanel != null)
            goPanel.remove(progressPanel);
        
        goPanel.add(startButton);

        updateStatusBar(message);

        RestoreEnableButtonStates();
        
        RefreshGUI();
    }

    private void updateStatusBar(String message)
    {
        String s = new Date()+":   "+message;
        statusBar.setText(s);
    }

    synchronized void setProgressPanel(JPanel panel)
    {
        if(progressPanel != null)
            goPanel.remove(progressPanel);

        progressPanel = panel;

        progressPanel.setBackground(bgColor);

        goPanel.remove(startButton);
        goPanel.add(progressPanel);
        RefreshGUI();
    }
	
	public static void main(String[] args)
	{	
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() 
			{
				new MEAPsoftGUI();
			}
		});
	}
}


/**
 * Default MEAPsoftGUI ExceptionHandler - pops up a dialog box with a
 * stack trace.
 *
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
class GUIExceptionHandler extends ExceptionHandler
{
    private JFrame frame;
    private String msg;

    public GUIExceptionHandler(JFrame f, String message)
    {
        frame = f;
        msg = message;
    }

   /**
    * ExceptionHandler that pops up a dialog box with a stack trace
    */
    public void handleException(Exception e)
    {
        GUIUtils.ShowDialog(e, msg, GUIUtils.MESSAGE, frame);
    }
}
