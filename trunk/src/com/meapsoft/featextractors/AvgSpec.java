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
 * A simple feature calculation that averages all spectral frames into a single
 * vector. This computes the mean in linear magnitude and then returns the
 * result in log magnitude.
 * 
 * @author Mike Mandel (mim@ee.columbia.edu)
 */

public class AvgSpec extends FeatureExtractor
{

	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{

		double[] avgSpec = new double[stft.getRows()];
		double[] curFrame;
		double sum = 0;

		for (int band = 0; band < avgSpec.length; band++)
			avgSpec[band] = 0;

		for (int frame = 0; frame < length; frame++)
		{
			curFrame = stft.getFrame(startFrame + frame);
			for (int band = 0; band < avgSpec.length; band++)
			{
				// double tmp = curFrame[band] / length;
				double tmp = Math.pow(10, curFrame[band] / 10) / length;
				avgSpec[band] += tmp;
				sum += tmp;
			}
		}

		double dBconst = 10 / Math.log(10);
		for (int band = 0; band < avgSpec.length; band++)
			// avgSpec[band] = avgSpec[band] / sum;
			avgSpec[band] = dBconst * Math.log(avgSpec[band] / sum);

		return avgSpec;
	}

	public String description()
	{
		return "Computes the mean spectrum or each chunk.";
	}
}
