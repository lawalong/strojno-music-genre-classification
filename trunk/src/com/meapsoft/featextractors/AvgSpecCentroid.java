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
 * A simple feature calculation that averages the spectral centroid of all
 * spectral frames in a given chunk.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */

public class AvgSpecCentroid extends FeatureExtractor
{

	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{
		double[] curFrame;
		double[] avgSpecCentroid = new double[1];
		double num = 0;
		double den = 0;

		for (int frame = 0; frame < length; frame++)
		{
			num = 0;
			den = 0;
			curFrame = stft.getFrame(startFrame + frame);
			for (int band = 0; band < stft.getRows(); band++)
			{
				double freqCenter = band * (stft.samplingRate / 2)
						/ (stft.getRows() - 1);
				// convert back to linear power
				double p = Math.pow(10, curFrame[band] / 10);

				num += freqCenter * p;
				den += p;
			}
			avgSpecCentroid[0] += num / (length * den);
		}

		return avgSpecCentroid;
	}

	public String description()
	{
		return "Computes the average spectral center of mass of a chunk's frames.";
	}
}
