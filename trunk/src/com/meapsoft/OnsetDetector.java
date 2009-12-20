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

import java.util.ArrayList;

/*
 * Base class for all onset detectors. Looks at an STFT and determines if there
 * is anything that looks like an onset in it.
 * 
 * @author Mike Mandel (mim@ee.columbia.edu)
 */

public class OnsetDetector implements FrameListener
{
	STFT stft;

	long lastSeen;

	int histLen = 4, onsLen = 512;

	double[][] history;

	double[] onsets;

	ArrayList listeners;

	// Keep track of the mean of each band with a leaky integrator
	double[] state;

	// Controls the length over which the mean is calculated for each
	// band.
	double adaptation = 0.01;

	// The threshold for an onset, as a fraction of the mean for each
	// band
	double bandThresh = 2;

	// The fraction of band onsets required to trigger a global onset
	double bandFrac = 0.3;

	public OnsetDetector(STFT stft, double bandThresh, double bandFrac)
	{
		this.stft = stft;
		this.bandThresh = bandThresh;
		this.bandFrac = bandFrac;

		this.listeners = new ArrayList();
		this.history = new double[histLen + 1][];
		this.onsets = new double[onsLen];

		state = new double[stft.getRows()];
		for (int i = 0; i < state.length; i++)
			state[i] = 0.5 / adaptation;

		stft.addFrameListener(this);
	}

	public void addOnsetListener(OnsetListener listener)
	{
		listeners.add(listener);
	}

	public void removeOnsetListener(OnsetListener listener)
	{
		listeners.remove(listener);
	}

	protected void notifyListeners(long frAddr, int zeroFrames)
	{
		for (int i = 0; i < listeners.size(); i++)
			((OnsetListener) listeners.get(i)).newOnset(frAddr, zeroFrames);
	}

	// Determine if there has been an onset since the last time this was
	// called. If there was, notify our listeners.
	public void newFrame(STFT stft, long newestFrame)
	{
		// Get the history for the first column to be analyzed
		for (int i = 0; i < histLen + 1; i++)
		{
			history[i] = stft.getFrame(lastSeen + 1 - histLen + i);
			if (history[i] == null)
				history[i] = new double[stft.getRows()];
		}

		// Analyze each column in turn, bucket-brigade the history
		for (long fr = lastSeen + 1; fr <= newestFrame; fr++)
		{
			double result;
			int curOns = (int) (fr % onsets.length);
			onsets[curOns] = 0;

			for (int band = 0; band < history[0].length; band++)
			{
				result = 0;
				for (int i = 0; i < histLen; i++)
					result += Math.abs(history[i][band]
							- history[histLen][band]);

				// See if this band is an onset
				if (result > bandThresh * state[band])
					++onsets[curOns];

				// update running mean
				state[band] = adaptation * result + (1 - adaptation)
						* state[band];
			}

			// get the next history
			for (int i = 0; i < histLen; i++)
				history[i] = history[i + 1];
			history[histLen] = stft.getFrame(fr);
		}

		checkOnsets(lastSeen, newestFrame);

		// No onsets found
		lastSeen = newestFrame;
	}

	public void checkOnsets(long lastSeen, long newestFrame)
	{
		// Simple onset detector: if enough bands have onsetted, trigger
		// a global onset
		for (long fr = lastSeen + 1; fr <= newestFrame; fr++)
		{
			int curOns = (int) (fr % onsets.length);

			if (onsets[curOns] > bandFrac * state.length)
			{
				// System.out.print((int)(onsets[curOns]/10));
				// System.out.print("*");
				notifyListeners(fr, 0);
			}
		}
	}
}
