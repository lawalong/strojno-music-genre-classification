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

package com.meapsoft.featextractors;

import java.util.Arrays;

import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;

/**
 * Finds the median beat length for all segments (really just median segment length). 
 * Computes the difference between each frame's length and the median length.
 * Outputs that difference in seconds.
 * 
 * @author Douglas Repetto
 */
public class MedianBeatLengthOffset extends MetaFeatureExtractor
{
	public void features(FeatFile featFile, boolean clearOriginalFeatures)
	{
		int numChunks = featFile.chunks.size();

		double[] lengths = new double[numChunks];

		//gather lenghts for all chunks
		for (int chunk = 0; chunk < numChunks; chunk++)
		{
			FeatChunk curChunk = (FeatChunk) featFile.chunks.get(chunk);
			lengths[chunk] = curChunk.length;
		}

		//sort them low to high
		Arrays.sort(lengths);
		
		double medianLength = 0.0;
		
		
		//are we even or odd?
		if (numChunks % 2 == 0)
		{
			//we have an even number of chunks, so take the average of
			//the two middle chunks
			int middle = numChunks/2;
			medianLength += lengths[middle-1];
			medianLength += lengths[middle];
			medianLength /= 2.0;
			//System.out.println("numChunks: " + numChunks + " middle: " + middle + " medianLength: " + medianLength);
		}
		else
		{
			//we're odd so just take the middle value
			int middle = (int)Math.floor(numChunks/2.0);
			medianLength = lengths[middle];
			//System.out.println("numChunks: " + numChunks + " middle: " + middle + " medianLength: " + medianLength);
		}
		
		//System.out.println("shortest: " + lengths[0] + " longest: " + lengths[numChunks - 1]);
		//write out offset values
		for (int chunk = 0; chunk < numChunks; chunk++)
		{
			FeatChunk curChunk = (FeatChunk) featFile.chunks.get(chunk);

			double value = curChunk.length - medianLength;
			double[] feats = new double[1];
			feats[0] = value;
	
			if (clearOriginalFeatures)
				curChunk.clearFeatures();
	
			curChunk.addFeature(feats);
			
			//System.out.println("chunk: " + chunk + " length: " + curChunk.length + " median: " +  medianLength + " diff: " + value);
		}
		
	}

	public String description()
	{
		return "Returns the difference between the length of each segment and the median " +
				"segment length (in seconds).";
	}

}
