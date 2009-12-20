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

import java.awt.Graphics;

import javax.swing.JFrame;

import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;

/**
 * 
 * @author douglas@music.columbia.edu
 * 
 */

public class SingleFeatureBarGraphPanel extends SingleFeaturePanel
{
	public SingleFeatureBarGraphPanel()
	{
		super();

		numDrawableFeatures = 1;
	}

	public String getDisplayType()
	{
		return "BarGraph";
	}

	// stub class, implemented from abstract parent class
	public void updateData()
	{

	}

	public void drawData(Graphics g)
	{
		double zoomMulti = (zoomLevel * 4.0) / 4.0;
		int w = (int) (this.getWidth() * zoomMulti);
		int h = this.getHeight();
		double yScaler = h / featureRange;

		double xScaler = w / timeRange;
		//System.out.println("w: " + w + " h: " + h);
		//System.out.println("xScaler: " + xScaler + " yScaler: " + yScaler);
		//System.out.println("numChunks: " + numChunks);

		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);

		g.setColor(fGColor);

		double localFirstEventTime = ((FeatChunk) events
				.get(firstChunkToDraw)).startTime;
		int x = 0;

		for (int i = firstChunkToDraw; i < numChunks && x < getWidth(); i++)
		{
			FeatChunk fC = (FeatChunk) events.get(i);
			x = (int) ((fC.startTime - localFirstEventTime) * xScaler);
			int width = (int) (fC.length * xScaler) + 1;

			//double[] features = featFile.getFeatureByName(featureName, i);
			double[] features = fC.getFeatureByName(featureName);
			
			//we can just use features[0] because we know that bar graph panel only
			//draws one dimensional features!
			// adjust to zero
			double dataPoint = features[0] - lowestValue;
			// featureData[i][0] - lowestValue;
			int y = (int) (dataPoint * yScaler);

			// g.drawLine(xPrev, yPrev, x, y);
			g.fillRect(x, h - y, width, y);
		}

	}

	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out
					.println("usage: SingleFeatureBarGraphPanel myfilename myfeaturename\n");
			System.exit(-1);
		}

		final String fileName = args[0];
		final String featureName = args[1];
		final FeatFile fF = new FeatFile(fileName);
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("SingleFeatureBarGraphPanel");

				try
				{
					fF.readFile();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.exit(-1);
				}
				SingleFeaturePanel sFP = new SingleFeatureBarGraphPanel();
				if (sFP.initialize(fF, featureName) == -1)
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
