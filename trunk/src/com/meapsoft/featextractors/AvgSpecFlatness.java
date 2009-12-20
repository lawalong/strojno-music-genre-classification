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
 * A simple feature calculation that averages the spectral flatness of
 * all spectral frames in a given chunk.  The feature varies between 0
 * and 1 with 1 being perfectly flat.
 *
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class AvgSpecFlatness extends FeatureExtractor
{
	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{
		double[] curFrame;
		double[] avgSpecFlatness = new double[1];

		avgSpecFlatness[0] = 0;

		double sumfpower = 0;
		for (int frame = 0; frame < length; frame++)
		{
			double sumxlogx = 0, sumx = 0;
			int nband = stft.getRows();
			curFrame = stft.getFrame(startFrame + frame);
			for (int band = 0; band < nband; band++)
			{
				// convert back to linear intensity
				double p = Math.pow(10, curFrame[band] / 20);
				sumxlogx += p * Math.log(p);
				sumx += p;
			}
			// weight per-frame SpecFlatnesses by power of each frame
			avgSpecFlatness[0] += sumx * (-sumxlogx / sumx + Math.log(sumx))
					/ Math.log(nband);
			sumfpower += sumx;
		}
		// normalize sum across all frames
		avgSpecFlatness[0] /= sumfpower;
		// should now be 0 for each frame has all energy in one bin, 
		// 1 for each frame has uniform energy distribution

		return avgSpecFlatness;
	}

	public String description()
	{
		return "Provides a measure of the peakiness of the average spectrum; units are 0 (very peaked) to 1 (very flat).";
	}
}
