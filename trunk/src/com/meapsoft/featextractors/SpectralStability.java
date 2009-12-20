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

import com.meapsoft.STFT;

/**
 * Tries to sort pitched from unpitched sounds
 * 
 * @author Douglas Repetto
 */

public class SpectralStability extends FeatureExtractor
{
	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{
		int numBands = stft.getRows();
		double[] currFrame;
		double[] prevFrame = null;
		// double lastEnergy = 0.0;
		double[] deltaSum = { 0.0 };

		// we'll sum the deltas in each bin from frame to frame
		// we'll return that total delta, maybe it'll act as an
		// indicator of the stability of the chunk...

		for (int frame = 0; frame < length; frame++)
		{
			currFrame = stft.getFrame(startFrame + frame);

			for (int bin = 0; bin < numBands; bin++)
			{
				double delta = 0.0;
				// skip first frame!
				if (prevFrame != null)
					delta = Math.abs(prevFrame[bin] - currFrame[bin]);

				// if (bin == 5 && prevFrame != null)
				// System.out.println("delta: " + delta);

				// this is a threshold of some sort...
				if (delta < 5.0)
					delta = 0.0;

				deltaSum[0] += delta;
			}

			prevFrame = currFrame;
		}

		// normalize for frame length
		deltaSum[0] /= length;

		return deltaSum;
	}

	public String description()
	{
		return "Tracks the stability of the spectral energy within each chunk of sound. "
				+ "More stable chunks are more likely to be pitched material. " +
				"Higher numbers indicate more change between frames, thus less stability. ";
	}
}
