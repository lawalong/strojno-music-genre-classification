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

package com.meapsoft;

import java.util.Vector;

import javax.swing.BoundedRangeModel;

/**
 * Helper class for Segmenter.java - does all of the work in fact. Processes an
 * audio stream, finds onsets, and writes them to a file. Based on Mike Mandel's
 * Extractor.java
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 * @author Mike Mandel (mim@ee.columbia.edu)
 */

public class SegmentExtractor implements FrameListener, Runnable, OnsetListener
{
	Vector consumers = new Vector();

	STFT stft;

	long lastOnset = -1;

	int minChunkLen = 5;

	// int maxChunkLen = 85
	// no max length between onsets
	int maxChunkLen = Integer.MAX_VALUE;

	// keep track of whether or not we have seen any onsets yet...
	boolean recording = false;

	FeatFile outFile;

	String sourceFileName;

	private BoundedRangeModel progress = null;

	public SegmentExtractor(STFT stft, String sfn, FeatFile of,
			BoundedRangeModel brm)
	{
		this.stft = stft;
		sourceFileName = sfn;
		outFile = of;

		// maxChunkLen = stft.getRows();

		progress = brm;
	}

	public void run()
	{
		stft.start();
	}

	/**
	 * Callback function for OnsetDetector - processes a new onset
	 */
	public void newOnset(long nextOnset, int zeroFrames)
	{	
		if (!recording)
		{
			// Start recording at this onset, don't do anything else
			lastOnset = nextOnset;
			recording = true;
		}
		else if (nextOnset - lastOnset < minChunkLen)
		{
			// Ignore onsets that come too close together
		}
		else
		{
			// Got new onset, make a chunk from the last onset to this one
			outFile.chunks.add(new FeatChunk(sourceFileName, stft
					.fr2Seconds(lastOnset), stft.fr2Seconds(nextOnset
					- lastOnset), null));
			lastOnset = nextOnset;
		}
	}

	/**
	 * Callback function for FrameListener OnsetDetector - end previous onset if
	 * we've reached the max chunk length
	 */
	public void newFrame(STFT ignored, long frAddr)
	{
		progress.setValue(progress.getValue() + 1);

		if (recording && frAddr - lastOnset > maxChunkLen)
		{
			// Reached max length of chunk from last onset, make chunk
			recording = false;

			outFile.chunks.add(new FeatChunk(sourceFileName, stft
					.fr2Seconds(lastOnset), stft.fr2Seconds(maxChunkLen), null));
		}
		// we've hit the end of the file, let's put in a segment end marker
		else if (recording && frAddr == -1)
		{
			recording = false;

			outFile.chunks.add(new FeatChunk(sourceFileName, stft
					.fr2Seconds(lastOnset), stft.fr2Seconds(stft
					.getLastFrameAddress()
					- lastOnset), null));

			double lastStartTime = stft.fr2Seconds(lastOnset);
			double lastLength = stft.fr2Seconds(stft.getLastFrameAddress()
					- lastOnset);
			double lastEndTime = lastStartTime + lastLength;

			// System.out.println("last chunk data -- lastStartTime: " +
			// lastStartTime +
			// " lastLength: " + lastLength + " lastEndTime: " + lastEndTime);
		}
	}
}
