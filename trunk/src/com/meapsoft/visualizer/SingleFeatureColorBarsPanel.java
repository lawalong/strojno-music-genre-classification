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

import com.meapsoft.Chunk;
import com.meapsoft.EDLChunk;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.disgraced.ColorMap;

/**
 * 
 * @author douglas@music.columbia.edu
 * 
 */

public class SingleFeatureColorBarsPanel extends SingleFeaturePanel
{
	int numColors = 256;

	ColorMap colormap = ColorMap.getJet(numColors);

	public SingleFeatureColorBarsPanel()
	{
		numDrawableFeatures = -1;
	}

	public String getDisplayType()
	{
		return "ColorBars";
	}

	// stub class, implemented from abstract parent class
	public void updateData()
	{

	}

	public void drawData(Graphics g)
	{
		double zoomMulti = (zoomLevel * 4.0) / 4.0;
		int w = (int) (this.getWidth() * zoomMulti);
		// int w = this.getWidth();
		int h = this.getHeight();
		double yScaler = h / featureRange;

		double xScaler = w / timeRange;
		//System.out.println("w: " + w + " h: " + h);
		//System.out.println("xScaler: " + xScaler + " yScaler: " + yScaler);
		//System.out.println("numChunks: " + numChunks);

		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);

		double yIncr = (double) h / featureSize;
		//System.out.println("yIncr: " + yIncr);

		double localFirstEventTime = 0.0;
		
		if (edlFile == null)
			localFirstEventTime = ((FeatChunk)events.get(firstChunkToDraw)).startTime;
		else
			localFirstEventTime = ((EDLChunk)events.get(firstChunkToDraw)).dstTime;
		
		int x = 0;
		int width = 0;
		
        // keep track of our progress:
        progress.setMinimum(0);
        progress.setMaximum(numChunks); 
        progress.setValue(0);
		
		for (int chunkNum = firstChunkToDraw; chunkNum < numChunks && x < getWidth(); chunkNum++)
		{
			FeatChunk fC = getFeatChunkByNumber(chunkNum);
			
			if (edlFile == null)
			{
				x = (int) ((fC.startTime - localFirstEventTime) * xScaler);
				width = (int) (fC.length * xScaler) + 1;
			}
			else
			{
				EDLChunk eC = (EDLChunk)events.get(chunkNum);
				x = (int) ((eC.dstTime - localFirstEventTime) * xScaler);
				width = (int) (eC.length * xScaler) + 1;
			}

			// adjust to zero
			//double[] dataPoints = featFile.getFeatureByName(featureName, i);
			double[] dataPoints = fC.getFeatureByName(featureName);
			//double dataPoints[] = fC.getFeatures();// featureData[i];

			if (featureSize > 1)
			{
				for (int j = 1; j < featureSize + 1; j++)
				{
					double dataPoint = dataPoints[j - 1] - lowestValue;
					double colorIndex = (dataPoint / featureRange) * 255.0;
					double y = j * yIncr;
					// System.out.println("i: " + i + " j: " + j + " x: " + x +
					// " y: " + y + " h: " + h + " yIncr: " + yIncr);
					g.setColor(colormap.table[(int) colorIndex]);
					g.fillRect(x, h - (int) (y), width, (int) yIncr + 1);
				}
			}
			else
			{
				double dataPoint = dataPoints[0] - lowestValue;
				double colorIndex = (dataPoint / featureRange) * 255.0;
				g.setColor(colormap.table[(int) colorIndex]);
				g.fillRect(x, 0, width, h);
			}

			//increment the progress
			progress.setValue(progress.getValue()+1);
			
		}

		// g.setColor(this.defaultFGColor);
		// g.drawString(featureName, 20, 20);
		/*
		 * if (showSegmentTicks) drawSegmentTicks(g);
		 * 
		 * if (selected) drawBorder(g);
		 */
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
				SingleFeaturePanel sFP = new SingleFeatureColorBarsPanel();
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
