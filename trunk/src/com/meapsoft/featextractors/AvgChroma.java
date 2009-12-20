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

import com.meapsoft.FeatExtractor;
import com.meapsoft.RingMatrix;
import com.meapsoft.STFT;

/**
 * Averages all spectral frames together into a single feature vector and then
 * converts the vector into a 12-bin chroma
 * 
 * Based on AvgMelSpec by Mike Mandel
 * 
 * @author Dan Ellis (dpwe@ee.columbia.edu)
 */

public class AvgChroma extends FeatureExtractor
{

	// earliest FFT bin to use
	static final int FIRSTBAND = 3;

	protected double[][] chromaWts;

	protected double[] linSpec;

	protected int N, outDim;

	// #define CHROMA_LOG2 (0.69314718055995)

	public double hz2octs(double fq)
	{
		//return Math.log(fq / 370.0) / 0.69314718055995;
		//return Math.log(fq / 392.00) / 0.69314718055995;
		//261.63 is a C
		return Math.log(fq / 261.63) / 0.69314718055995;
	}

	// Default constructor
	public AvgChroma()
	{
		this(FeatExtractor.nfft / 2 + 1, 12, FeatExtractor.feSamplingRate);
	}

	public AvgChroma(int N, int outDim, double sampleRate)
	{
		this.N = N;
		this.outDim = outDim;
		linSpec = new double[N];

		chromaWts = new double[outDim][N];

		// Create the chroma inner products

		double bin2hz = sampleRate / (2 * (N - 1));
		for (int i = FIRSTBAND; i < N; ++i)
		{
			double tot = 0;
			// 1/12 = 1 semi = 0.083333
			// double binwidth = max(0.08333333, hz2octs(bin2hz*(i+1)) -
			// hz2octs(bin2hz*(i-1)))/2;
			double binwidth = hz2octs(bin2hz * (i + 1))
					- hz2octs(bin2hz * (i - 1));
			if (binwidth < 0.083333)
				binwidth = 0.083333;
			binwidth /= 4;
			double binocts = hz2octs(bin2hz * i);
			// fade out bins above 1 kHz
			double binwt = 1.0;
			if (bin2hz * i > 1000)
				binwt = Math.exp(-(bin2hz * i - 1000) / 500);
			for (int j = 0; j < outDim; ++j)
			{
				double bindelta = binocts - (((double) j) / outDim);
				bindelta = bindelta - Math.rint(bindelta);
				chromaWts[j][i] = binwt
						* Math.exp(-0.5 * Math.pow(bindelta / binwidth, 2));
				tot = tot + chromaWts[j][i];
			}
			// Normalize energy distribution from each fft bin
			// for (int j = 0; j < outDim; ++j) {
			// chromaWts[j][i] /= tot;
			// }
		}
	}

	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{
		int boff = 0;
		double[] chromSpec = new double[boff + outDim];
		double[] curFrame;
		double sum = 0;
		double sum2 = 0;

		boolean recalculateSTFT = stft.getRows() != N;
		RingMatrix newstft = null;
		if (recalculateSTFT)
		{
			// keep the same number of frames as in stft
			newstft = STFT.getSTFT(stft.getSamples(startFrame, startFrame
					+ length), (N - 1) * 2, stft.nhop);
			length = newstft.getColumns();
		}

		// intialize average to 0
		Arrays.fill(linSpec, 0);

		// collect average linear spectrum
		for (int frame = 0; frame < length; frame++)
		{

			if (!recalculateSTFT)
				curFrame = stft.getFrame(startFrame + frame);
			else
				curFrame = newstft.getColumn(frame);

			if (curFrame != null)
				for (int band = 0; band < linSpec.length; band++)
					linSpec[band] += Math.pow(10, curFrame[band] / 10) / length;
		}

		// // convert log magnitude to linear magnitude for binning
		// for(int band=0; band<linSpec.length; band++)
		// //linSpec[band] = Math.exp(linSpec[band]);
		// linSpec[band] = Math.pow(10,linSpec[band]/10);

		// matrix multiply to find bins
		for (int bin = 0; bin < outDim; bin++)
		{
			double val = 0;
			for (int band = FIRSTBAND; band < linSpec.length; band++)
			{
				val += linSpec[band] * chromaWts[bin][band];
			}
			chromSpec[boff + bin] = val;
			sum += val;
			sum2 += val * val;
		}

		// chroma vectors have unit norm
		// double mean = sum/outDim;
		// double sd = Math.sqrt( sum2/outDim - Math.pow(mean,2));
		double rms = Math.sqrt(sum2 / outDim);
		for (int bin = 0; bin < outDim; bin++)
		{
			// chromSpec[boff+bin] = (chromSpec[boff+bin] - mean)/ sd;
			chromSpec[boff + bin] = chromSpec[boff + bin] / rms;
		}

		if (boff > 0)
		{
			// calculate complex average chroma
			double re = 0, im = 0;
			for (int bin = 0; bin < outDim; ++bin)
			{
				re = re + chromSpec[boff + bin]
						* Math.cos(6.28318531 * bin / outDim);
				im = im + chromSpec[boff + bin]
						* Math.sin(6.28318531 * bin / outDim);
			}

			double meanchrom = outDim * (Math.atan2(im, re) / 6.28318531);
			// atan2 returns -pi..pi
			// fold back to +ve octave
			if (meanchrom < 0)
				meanchrom += outDim;

			chromSpec[0] = meanchrom;

		}
		return chromSpec;
	}

	public String description()
	{
		return "12-dimensional vector of energy distribution across each semitone of the octave.";
	}
}
