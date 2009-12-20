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
import com.meapsoft.disgraced.ColorMap;

/**
 * 
 * @author douglas@music.columbia.edu
 * 
 */

public class SingleFeatureLineGraphPanel extends SingleFeaturePanel
{
	// int numColors = 256;
	ColorMap colormap;// = ColorMap.getJet(numColors);

	public SingleFeatureLineGraphPanel()
	{
		numDrawableFeatures = -1;
	}

	public int initialize(FeatFile featFile, String featureName)
	{
		int error = super.initialize(featFile, featureName);
		if (error < 0)
			return error;

		colormap = ColorMap.getJet(featureSize);

		return 1;
	}

	public String getDisplayType()
	{
		return "LineGraph";
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

		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);

		g.setColor(fGColor);

		for (int j = 0; j < featureSize; j++)
		{
			int xPrev = 0;
			int yPrev = 0;

			if (featureSize > 1)
				g.setColor(colormap.table[j]);
			else
				g.setColor(fGColor);

			double localFirstEventTime = ((FeatChunk) events.get(firstChunkToDraw)).startTime;

			int x = 0;

			
			for (int i = firstChunkToDraw; i < numChunks && x < getWidth(); i++)
			{
				FeatChunk fC = (FeatChunk) events.get(i);

				double dX = ((fC.startTime - localFirstEventTime) * xScaler);
				x = (int) dX;
				int xMid = (int)(dX + (fC.length * xScaler * 0.5));
				// adjust to zero

				//double[] features = featFile.getFeatureByName(featureName, i);
				double[] features = fC.getFeatureByName(featureName);
				double dataPoint = features[j] - lowestValue;

				int y = h - (int) (dataPoint * yScaler);

				// don't draw the first one to avoid false line
				if (i == firstChunkToDraw)
				{
					g.drawLine(0, y, xMid, y);
				}
				else
				{
					g.drawLine(xPrev, yPrev, xMid, y);
				}
				
				//do last little segment on last chunk
				if (i == numChunks - 1)
				{
					g.drawLine(xMid, y, w, y);
				}
					
				xPrev = xMid;
				yPrev = y;
			}
		}
	}

	public static void main(String[] args)
	{

		if (args.length < 2)
		{
			System.out
					.println("usage: SingleFeaturePanel myfilename myfeaturename\n");
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
				JFrame frame = new JFrame("SingleFeaturePanel");

				try
				{
					fF.readFile();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.exit(-1);
				}
				SingleFeaturePanel sFP = new SingleFeatureLineGraphPanel();
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
