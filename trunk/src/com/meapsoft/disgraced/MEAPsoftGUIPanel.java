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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.meapsoft.EDLFile;
import com.meapsoft.FeatFile;
import com.meapsoft.MEAPUtil;
import com.meapsoft.visualizer.SingleFeatureSpectrumPanel;
import com.meapsoft.visualizer.SingleFeatureWaveformPanel;

/**
 * Abstract class that all MEAPsoftGUIPanels must extend.  Contains
 * the glue needed to let all of the MEAPsoft subsystems work
 * together in MEAPsoftGUI.
 *
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public abstract class MEAPsoftGUIPanel extends JPanel implements ActionListener, MouseListener
{
    // Background color of this Panel
    public Color color;

    //
    protected static MEAPsoftGUI meapsoftGUI;

    // name of audio player to use in segmenter and synthesizer panels
    protected static String audioPlayerName; 

    // various files used my MEAPsoftGUI components
    public static FeatFile segmentFile = null;
    public static FeatFile featFile = null;
    public static EDLFile edlFile = null;

	//IO data
	protected static String dataDirectory;
	protected static String meapsoftDirectory;
	protected static String slash;
	protected static String dataBaseName = "mann";
	//segmenter
	protected static String inputSoundFileNameFull;
	protected static String inputSoundFileNameShort;
	protected static String outputSegmentsFileName = dataBaseName + ".seg";
	//features
	protected static String inputSegmentsFileName = dataBaseName + ".seg";
	protected static String outputFeaturesFileName = dataBaseName + ".feat";
	//composer
	protected static String inputFeaturesFileName = dataBaseName + ".feat";
	protected static String outputEDLFileName = dataBaseName + ".edl";
	//synth
	protected static String inputEDLFileName = dataBaseName + ".edl";
	protected static String outputSoundFileNameFull;
	protected static String outputSoundFileNameShort;

    // help button
	protected JLabel helpButton;
    protected String helpURL;

    // window title (used for help window).
    protected String title;

    //panel to hold our waveform and spectrum panels
    JPanel waveSpectPanel;
    
    /**
     * Create a new MEAPsoftGUIPanel
     */
    public MEAPsoftGUIPanel(MEAPsoftGUI msg)
    { 
        meapsoftGUI = msg;

        dataDirectory = msg.dataDirectory;
        meapsoftDirectory = msg.meapsoftDirectory;
        slash = MEAPUtil.slash;

        if (System.getProperty("os.name").equals("Mac OS X"))
            audioPlayerName = "open";
        else if (System.getProperty("os.name").equals("Linux"))
            audioPlayerName = "play";
        else if (System.getProperty("os.name").startsWith("Windows"))
            audioPlayerName = 
                "C:\\Program Files\\Windows Media Player\\wmplayer.exe";
        else
            audioPlayerName = null;

        helpButton = new JLabel("(help)");
		helpButton.setForeground(Color.blue);
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		helpButton.addMouseListener(this);
        helpURL = "file:///" + meapsoftDirectory + slash + "doc" + slash 
            + "manual.html";
    }

    //this is called when we become the active pane
    //need to force waveform and spectrum panels to draw
    public void updateWaveformAndSpectrum()
    {
    	//waveformPanel.repaint();
    	//meapsoftGUI.waveformPanel.invalidate();
    	//spectrumPanel.repaint();
    	//meapsoftGUI.spectrumPanel.invalidate();
    }
    
    /**
     * Execute this MEAPsoft component.  Returns a negative number if
     * there was an error, 0 otherwise.
     */
    public abstract int run();

    /**
     * Convenience functions so that we don't have to reference
     * meapsoftGUI from our subclasses.  Curse Java's verbosity!
     */
    /*
    protected static void ShowDialog(Exception e, String message, int status)
    {
        meapsoftGUI.ShowDialog(e,  message, status);
    }

	protected static void ShowDialog(String message, int status)
    {
        meapsoftGUI.ShowDialog(message, status);
    }

	protected static  String[] FileSelector(int mode)
    {
        return meapsoftGUI.FileSelector(mode);
    }
	*/
    protected static void initSegmentFile()
    {        
        String fn = dataDirectory + slash + outputSegmentsFileName;

        if(segmentFile == null || segmentFile.filename != fn)
            segmentFile = new FeatFile(fn);
    }

    protected static void initFeatFile()
    {
        String fn = dataDirectory + slash + outputFeaturesFileName;

        if(featFile == null || featFile.filename != fn)
            featFile = new FeatFile(fn);
    }

    protected static void initEDLFile()
    {        
        String fn = dataDirectory + slash + outputEDLFileName;

        if(edlFile == null || edlFile.filename != fn)
            edlFile = new EDLFile(fn);
    }

    protected synchronized static void UpdateFileNames()
	{
		//segmenter
		outputSegmentsFileName = dataBaseName + ".seg";
		//features
		inputSegmentsFileName = dataBaseName + ".seg";
		outputFeaturesFileName = dataBaseName + ".feat";
		//composer
		inputFeaturesFileName = dataBaseName + ".feat";
		outputEDLFileName = dataBaseName + ".edl";
		//synth
		inputEDLFileName = dataBaseName + ".edl";
		String[] outputName = new String[2];
		outputName[0] = dataDirectory + slash + dataBaseName + ".MEAPED.wav";
		outputName[1] = dataBaseName + ".MEAPED.wav";
		SetOutputFileName(outputName);
	}
	
	protected synchronized static void SetInputFileName(String[] names)
	{
		inputSoundFileNameFull = names[0];
		inputSoundFileNameShort = names[1];
		dataBaseName = inputSoundFileNameShort;
		meapsoftGUI.segmenterPanel.inputSoundFileField.setText(inputSoundFileNameShort);
		UpdateFileNames();
		meapsoftGUI.UpdateInfoTexts();
	}
	
	protected static void SetOutputFileName(String[] names)
	{
		outputSoundFileNameFull = names[0];
		outputSoundFileNameShort = names[1];
		meapsoftGUI.synthPanel.outputFileNameField.setText(outputSoundFileNameShort);
	}

	protected static void RefreshGUI()
    {
        meapsoftGUI.RefreshGUI();
    }

    protected static void PlaySoundFile(String soundFile)
    {
        try
        {
            Process p = null;
            if(audioPlayerName != null)
            {
                String[] args = {audioPlayerName, soundFile};

                // need a special case for OS X because native OS X
                // applications are actually stored as directories...
                if(System.getProperty("os.name").equals("Mac OS X")
                   && new File(audioPlayerName).isDirectory())
                {
					String[] tmp = {"open", "-a", audioPlayerName, soundFile};
                    args = tmp;
                }

                p = Runtime.getRuntime().exec(args);
            }
            else
                GUIUtils.ShowDialog(
					"I don't know what program to use to play audio on your platform.  Please check your preferences.", 
					GUIUtils.MESSAGE, null);
        }
        catch (IOException e)
        {
            GUIUtils.ShowDialog(e, "Problem opening soundfile.", GUIUtils.MESSAGE, null);
        }
    }


	public void actionPerformed(ActionEvent arg0)
    {
    }

    public void mouseClicked(MouseEvent arg0)
	{		
		HelpWindow help = new HelpWindow(helpURL, title+" Help", color);
	}

	public void mousePressed(MouseEvent arg0)
	{
	}

	public void mouseReleased(MouseEvent arg0)
	{
	}

	public void mouseEntered(MouseEvent arg0)
	{
	}

	public void mouseExited(MouseEvent arg0)
	{
	}
}
