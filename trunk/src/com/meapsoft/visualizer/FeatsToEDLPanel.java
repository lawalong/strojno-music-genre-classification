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

package com.meapsoft.visualizer;

import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.disgraced.ColorMap;

/**
 * 
 * @author douglas@music.columbia.edu
 * 
 */

public class FeatsToEDLPanel extends JPanel//extends SingleFeaturePanel
{
	private static final long serialVersionUID = 1L;
	
	FeatFile featFile;
	EDLFile eDLFile;
	
	SingleFeatureColorBarsPanel featsPanel;
	SingleFeatureColorBarsPanel eDLPanel;
	SingleFeatureCrissCrossPanel cCPanel;
	
	public FeatsToEDLPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public int initialize(FeatFile featFile, EDLFile eDLFile)
	{
		this.featFile = featFile;
		this.eDLFile = eDLFile;
		
		featsPanel = new SingleFeatureColorBarsPanel();
		featsPanel.initialize(featFile, "AvgFreqSimple");
		featsPanel.setShowPanelInfo(true);
		featsPanel.setSegTickType(SingleFeaturePanel.SHORT_SEG_TICKS);

		cCPanel = new SingleFeatureCrissCrossPanel();
		cCPanel.initialize(featFile, eDLFile, "length");
		
		eDLPanel = new SingleFeatureColorBarsPanel();
		eDLPanel.initialize(featFile, eDLFile, "AvgFreqSimple");
		eDLPanel.setShowPanelInfo(true);
		eDLPanel.setSegTickType(SingleFeaturePanel.SHORT_SEG_TICKS);
		
		
		add(featsPanel);
		add(cCPanel);
		add(eDLPanel);

		return 1;
	}
	
	public String getDisplayType()
	{
		return "FeatsToEDL";
	}


	public static void main(String[] args)
	{

		if (args.length < 2)
		{
			System.out
					.println("usage: FeatsToEDLPanel myfile.feat myfile.edl\n");
			System.exit(-1);
		}

		final String featFileName = args[0];
		final String eDLFileName = args[1];
		final FeatFile fF = new FeatFile(featFileName);
		final EDLFile eDLF = new EDLFile(eDLFileName);
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("FeatsToEDLPanel");

				try
				{
					fF.readFile();
					eDLF.readFile();
					
					//EDLChunk eC = (EDLChunk)eDLF.chunks.elementAt(0);
					//System.out.println("chunk 0: sT: " + eC.startTime + " dT: " + eC.dstTime);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.exit(-1);
				}
				FeatsToEDLPanel sFP = new FeatsToEDLPanel();
				if (sFP.initialize(fF, eDLF) == -1)
				{
					System.out.println("hmm, something wrong, bailing.");
					System.exit(-1);
				}
				sFP.setSize(600, 400);

				frame.setContentPane(sFP);
				frame.pack();
				frame.setVisible(true);
				frame.setBounds(100, 100, 600, 400);

				sFP.repaint();
			}
		});
	}
}
