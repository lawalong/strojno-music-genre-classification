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
 * Returns a scalar corresponding to the mean chroma of a chunk. Legal values
 * are between 0 and 11, each corresponding to a different semitone.
 * 
 * Based on AvgMelSpec by Mike Mandel
 * 
 * @author Dan Ellis (dpwe@ee.columbia.edu)
 */

public class AvgChromaScalar extends AvgChroma
{

	// Default constructor
	public AvgChromaScalar()
	{
		super();
	}

	public AvgChromaScalar(int N, int outDim, double sampleRate)
	{
		super(N, outDim, sampleRate);
	}

	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{	
		double[] chromaVal = new double[1];

		double[] chromSpec;

		chromSpec = super.features(stft, startFrame, length, preEmphasis);

		// calculate complex average chroma
		double re = 0, im = 0;
		for (int bin = 0; bin < outDim; ++bin)
		{
			re = re + chromSpec[bin] * Math.cos(6.28318531 * bin / outDim);
			im = im + chromSpec[bin] * Math.sin(6.28318531 * bin / outDim);
		}

		double meanchrom = outDim * (Math.atan2(im, re) / 6.28318531);
		// atan2 returns -pi..pi
		// fold back to +ve octave
		if (meanchrom < 0)
			meanchrom += outDim;

		chromaVal[0] = meanchrom;

		return chromaVal;
	}

	public String description()
	{
		return "Single value giving dominant semitone within the octave.";
	}
}
