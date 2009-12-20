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
import com.meapsoft.DSP;

/**
 * Averages all spectral frames together into a single feature vector and then
 * converts the vector into a 6-bin "tonal centroid" 
 * as described in 
 * C. Harte, M. Sandler, and M. Gasser. 
 * Detecting Harmonic Change in Musical Audio. 
 * In Proceedings of the Audio and Music Computing for Multimedia Workshop 
 * (in conjunction with ACM Multimedia 2006), October 27, 2006, Santa Barbara
 * http://www.ofai.at/cgi-bin/tr-online?number+2006-13
 * 
 * Based on AvgChroma
 * 
 * @author Dan Ellis (dpwe@ee.columbia.edu)
 * 2007-12-03
 */

public class AvgTonalCentroid extends AvgChroma
{
	protected double[][] tctrWts;
        static final int tctrDims = 6;

	// Default constructor
	public AvgTonalCentroid()
	{
		super();
		InitializeWeights();
	}

	public AvgTonalCentroid(int N, int chrmDims, double sampleRate)
	{
		super(N, chrmDims, sampleRate);
		InitializeWeights();
	}

        private void InitializeWeights()
        {
		// radii of circles from Harte et al
		double r1 = 1.0, r2 = 1.0, r3 = 0.5;
		int chrmDims = outDim;

		// initialize the weights matrix
		tctrWts = new double[tctrDims][chrmDims];
		for (int j = 0; j < chrmDims; ++j) {
		    tctrWts[0][j] = r1 * Math.sin(Math.PI * j * 7/6);
		    tctrWts[1][j] = r1 * Math.cos(Math.PI * j * 7/6);
		    tctrWts[2][j] = r2 * Math.sin(Math.PI * j * 3/2);
		    tctrWts[3][j] = r2 * Math.cos(Math.PI * j * 3/2);
		    tctrWts[4][j] = r3 * Math.sin(Math.PI * j * 2/3);
		    tctrWts[5][j] = r3 * Math.cos(Math.PI * j * 2/3);
		}
	}

        private double[] chromaPickPeaks(double[] in)
        {
	    // return only the local-maximum values in the vector <in>
	    // treated as a circular vector i.e. end values wrap around 
	    // for comparison.
	    double[] out = new double[in.length];
	    for(int i = 0; i< in.length; ++i) {
		// calculate indices i-1 and i+1 with modulo (circular) indexing
		int im1 = (i-1 + in.length) % in.length;
		int ip1 = (i+1) % in.length;
		if( in[i] > in[im1] && in[i] <= in[ip1]) {
		    out[i] = in[i];
		} else {
		    out[i] = 0;
		}
	    }
	    return out;
	}

	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{	
		double[] tctrVec = new double[tctrDims];
		double[] chromSpec;

		chromSpec = super.features(stft, startFrame, length, preEmphasis);
		// keep only local maxima
		chromSpec = chromaPickPeaks(chromSpec);

		// normalize chromSpec
		chromSpec = DSP.times(chromSpec, 1.0/DSP.sum(chromSpec));

		// calculate tonal central
		tctrVec = DSP.times(tctrWts, chromSpec);

		return tctrVec;
	}

	public String description()
	{
		return "Six dimensional vector that encodes similarity in tonal space";
	}
}
