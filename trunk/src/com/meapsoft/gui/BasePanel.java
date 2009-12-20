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

package com.meapsoft.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;

import util.RTSI;

import com.meapsoft.EDLFile;
import com.meapsoft.FeatFile;
import com.meapsoft.MEAPUtil;
import com.meapsoft.gui.GUIUtils;
import com.meapsoft.visualizer.SingleFeatureSpectrumPanel;
import com.meapsoft.visualizer.SingleFeatureWaveformPanel;

/**
 * Abstract class that all tabs in the Meap GUI must extend.  Contains
 * the glue needed to let all of the MEAPsoft subsystems work
 * together in MeapGUI.
 *
 * @author Ron Weiss (ronw@ee.columbia.edu)
 * @author Mike Sorvillo (ms3311@columbia.edu)
 */
public abstract class BasePanel extends JPanel implements ActionListener, MouseListener
{
    //a reference to our gui
    public static  MeapsoftGUI mMainScreen;

    // various files used my MEAPsoftGUI components
    public static FeatFile segmentFile = null;
    public static FeatFile featFile = null;
    public static EDLFile edlFile = null;

	//IO data
	public static String dataDirectory;
	public static String meapsoftDirectory;
	public static String slash;
	public static String dataBaseName = "mann";
	//segmenter
	public static String inputSoundFileNameFull;
	public static String inputSoundFileNameShort;
	public static String outputSegmentsFileName = dataBaseName + ".seg";
	//features
	public static String inputSegmentsFileName = dataBaseName + ".seg";
	public static String outputFeaturesFileName = dataBaseName + ".feat";
	//composer
	public static String inputFeaturesFileName = dataBaseName + ".feat";
	public static String outputEDLFileName = dataBaseName + ".edl";
	//synth
	public static String inputEDLFileName = dataBaseName + ".edl";
	public static String outputSoundFileNameFull;
	public static String outputSoundFileNameShort;

    // help button
	protected JLabel helpButton;
    protected String helpURL;

    // window title (used for help window).
    protected String title;

    //the background color of this panel
    private Color mBkgColor = null;
    
    //panel to hold our waveform and spectrum panels
    JPanel waveSpectPanel;
    
    /**
     * Create a new BasePanel
     */
    public BasePanel(MeapsoftGUI msg)
    { 
        mMainScreen = msg;

        dataDirectory = msg.mDataDirectory;
        meapsoftDirectory = msg.mMeapsoftDirectory;
        slash = MEAPUtil.slash;
    }

    public void initColor(boolean flag)
    {
        //create a background color here
        mBkgColor = flag ? GUIUtils.getRandomColor() : null;
    }

    public Color getColor()
    {
    	return mBkgColor;
    }
    
    /**
     * Execute this MEAPsoft component.  Returns a negative number if
     * there was an error, 0 otherwise.
     */
    public abstract int run();
    
    protected void initSegmentFile()
    {        
        String fn = dataDirectory + slash + outputSegmentsFileName;

        if(segmentFile == null || segmentFile.filename != fn)
            segmentFile = new FeatFile(fn);
    }

    protected void initFeatFile()
    {
        String fn = dataDirectory + slash + outputFeaturesFileName;

        if(featFile == null || featFile.filename != fn)
            featFile = new FeatFile(fn);
    }

    protected void initEDLFile()
    {        
        String fn = dataDirectory + slash + outputEDLFileName;

        if(edlFile == null || edlFile.filename != fn)
            edlFile = new EDLFile(fn);
    }
 
    public synchronized static void updateFileNames()
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
		setOutputFileName(outputName);
		
		/*
		System.out.println("outputSegmentsFileName: " + outputSegmentsFileName + " inputSegmentsFileName: " + inputSegmentsFileName +
				" outputFeaturesFileName: " + outputFeaturesFileName + " inputFeaturesFileName: " + inputFeaturesFileName + 
				" outputEDLFileName: " + outputEDLFileName + " outputEDLFileName: " + outputEDLFileName + 
				" inputEDLFileName: " + inputEDLFileName);
		*/
	}
	
	protected synchronized static void setInputFileName(String[] names)
	{
		inputSoundFileNameFull = names[0];
		inputSoundFileNameShort = names[1];
		dataBaseName = inputSoundFileNameShort;
		updateFileNames();
	}
	
	protected static void setOutputFileName(String[] names)
	{
		outputSoundFileNameFull = names[0];
		outputSoundFileNameShort = names[1];
	}
    
    ////////////////////////////////////////////////////////////////////////
    // sniffForClasses() - sniffs for classes with a name in a package
    ////////////////////////////////////////////////////////////////////////
    public Vector sniffForClasses(String packageName, String className)
    {
        Vector v = null;
        try 
        {
        	//look in feature extractor directory
            v = RTSI.findnames(packageName, Class.forName(className));

            //also look in current directory
            Vector v2 = RTSI.findnames(System.getProperty("user.dir"), Class.forName(className)); 

            //add these to original vector
            if(v2 != null)
                v.addAll(v2);
        }
        catch(ClassNotFoundException e)
        {
			GUIUtils.ShowDialog("", GUIUtils.FATAL_ERROR, mMainScreen);	
        }

        //return this vector
        return v;
    }

	public void actionPerformed(ActionEvent arg0)
    {
    }

    public void mouseClicked(MouseEvent arg0)
	{		
		//HelpWindow help = new HelpWindow(helpURL, title+" Help", color);
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
