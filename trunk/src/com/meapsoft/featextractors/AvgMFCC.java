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

import com.meapsoft.STFT;

/**
 * Averages all spectral frames together into a single feature vector and then
 * converts the vector to mel frequency cepstral coefficients, a commonly used
 * feature in speech recognition systems.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class AvgMFCC extends AvgMelSpec
{
	private int nceps = 13;

	// Default constructor - Use AvgMelSpec defaults
	public AvgMFCC()
	{
		super();
	}

	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{
		double[] mfccs = new double[nceps];
		Arrays.fill(mfccs, 0);

		// precompute DCT matrix
		int nmel = outDim;
		double m = Math.sqrt(2.0 / nmel);
		double[][] DCTcoeffs = new double[nmel][nceps];
		for (int i = 0; i < nmel; i++)
			for (int j = 0; j < nceps; j++)
				DCTcoeffs[i][j] = m
						* Math
								.cos(Math.PI * (j + 1) * (i + .5)
										/ (double) nmel);

		for (int frm = 0; frm < length; frm++)
		{
			double[] melSpec = super.features(stft, startFrame + frm, 1, false);

			// convert to cepstrum:
			for (int x = 0; x < melSpec.length; x++)
			{
				// convert from dB to plain old log magnitude
				melSpec[x] = melSpec[x] / 10;

				// take DCT
				for (int y = 0; y < mfccs.length; y++)
					mfccs[y] = mfccs[y] + DCTcoeffs[x][y] * melSpec[x] / length;
			}
		}

		return mfccs;
	}

	public String description()
	{
		return "Computes the mean MFCCs of a chunk, a commonly used feature in speech recognition.";
	}
}
