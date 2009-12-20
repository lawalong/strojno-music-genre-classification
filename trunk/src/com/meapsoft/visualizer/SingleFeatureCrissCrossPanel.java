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

import java.awt.Color;
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
 * This panel just draws crisscrosses to show how the chunks
 * in a feat file are related to the chunks in an edl file.
 */

public class SingleFeatureCrissCrossPanel extends SingleFeaturePanel
{
	public SingleFeatureCrissCrossPanel()
	{
		numDrawableFeatures = -1;
	}

	public String getDisplayType()
	{
		return "CrissCross";
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
		//double yScaler = h / featureRange;

		double xScaler = w / timeRange;

		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);

		g.setColor(Color.black);
		//double yIncr = (double) h / featureSize;
		//System.out.println("yIncr: " + yIncr);

		double localFirstEventTime = 0.0;
		
		if (edlFile == null)
			localFirstEventTime = ((FeatChunk)events.get(firstChunkToDraw)).startTime;
		else
			localFirstEventTime = ((EDLChunk)events.get(firstChunkToDraw)).dstTime;
		
		int xFeat = 0;
		int xEDL = 0;
		//int width = 0;
		
		for (int chunkNum = firstChunkToDraw; chunkNum < numChunks && xFeat < getWidth() && xEDL < getWidth(); chunkNum++)
		{
			if (edlFile == null)
			{
				System.out.println("you need to send me an EDL file!");
			}
			
			FeatChunk fC = getFeatChunkByNumber(chunkNum);
			EDLChunk eC = (EDLChunk)events.get(chunkNum);
			
			xFeat = (int)(((fC.startTime - localFirstEventTime) + (fC.length/2.0)) * xScaler);
			xEDL = (int) (((eC.dstTime - localFirstEventTime) + (eC.length/2.0)) * xScaler);
			//width = (int) (eC.length * xScaler) + 1;

			g.drawLine(xFeat, 0, xEDL, h);

		}
	}

}
