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
 * converts the vector to the mel frequency scale.
 * 
 * @author Mike Mandel (mim@ee.columbia.edu)
 */
public class AvgMelSpec extends FeatureExtractor
{

	// for each mel bin...
	protected double[] melCenter; // actual targe mel value at center of this

	// bin

	protected double[] melWidth; // mel width divisor for this bin (constant,

	// except broadens in low bins)

	// for each fft bin
	protected double[] melOfLin;

	protected double[] linSpec;

	protected int N;

	protected int outDim;

	public double lin2mel(double fq)
	{
		return 1127.0 * Math.log(1.0 + fq / 700.0);
	}

	public double mel2lin(double mel)
	{
		return 700.0 * (Math.exp(mel / 1127.0) - 1.0);
	}

	// Default constructor - Use 40 mel spaced bins
	public AvgMelSpec()
	{
		this(FeatExtractor.nfft / 2 + 1, FeatExtractor.feSamplingRate, 40);
	}

	public AvgMelSpec(int N, float sampleRate, int outDim)
	{
		this.N = N;
		this.outDim = outDim;
		linSpec = new double[N];

		// Calculate the locations of the bin centers on the mel scale and
		// as indices into the input vector
		melCenter = new double[outDim + 2];
		melWidth = new double[outDim + 2];

		double melMin = lin2mel(0);
		// double melMax = lin2mel(sampleRate/2);
		double melMax = lin2mel((8000.0 < sampleRate / 2) ? 8000.0
				: sampleRate / 2); // dpwe 2006-12-11 - hard maximum
		double hzPerBin = sampleRate / 2 / N;
		for (int i = 0; i < outDim + 2; i++)
		{
			melCenter[i] = melMin + i * (melMax - melMin) / (outDim + 1);
			// System.out.println("centersMel["+i+"]="+centersMel[i]+"
			// centersInd[]="+centersInd[i]);
		}
		for (int i = 0; i < outDim + 1; i++)
		{
			melWidth[i] = melCenter[i + 1] - melCenter[i];
			double linbinwidth = (mel2lin(melCenter[i + 1]) - mel2lin(melCenter[i]))
					/ hzPerBin;
			if (linbinwidth < 1)
			{
				melWidth[i] = lin2mel(mel2lin(melCenter[i]) + hzPerBin)
						- melCenter[i];
			}
			// System.out.println("melBin="+i+" melCenter="+melCenter[i]+"
			// melWidth="+melWidth[i]+"("+mel2lin(melCenter[i]-melWidth[i])/hzPerBin+".."+mel2lin(melCenter[i])/hzPerBin+".."+mel2lin(melCenter[i]+melWidth[i])/hzPerBin);
		}
		// precalculate mel translations of fft bin frequencies
		melOfLin = new double[N];
		for (int i = 0; i < N; i++)
		{
			melOfLin[i] = lin2mel(i * sampleRate / (2 * N));
			// System.out.println("linbin2Mel["+i+"]="+linbin2mel[i]);
		}
	}

	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{
		double[] melSpec = new double[outDim];
		double[] curFrame;
		double sum = 0;

		// we're expecting a certain frequency resolution...
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

		// System.out.println("startFrame: " + startFrame + " length: " +
		// length);
		// collect average linear spectrum
		for (int frame = 0; frame < length; frame++)
		{
			if (!recalculateSTFT)
				curFrame = stft.getFrame(startFrame + frame);
			else
				curFrame = newstft.getColumn(frame);

			// what's going on? For very sparse segments curFrame is sometimes
			// null???
			// is there a zero length segment or something?
			if (curFrame != null)
			{
				for (int band = 0; band < linSpec.length; band++)
				{
					/*
					 * if (startFrame == 7011) { System.out.println("frame: " +
					 * frame); System.out.println("band: " + band);
					 * System.out.println("linSpec.length: " + linSpec.length);
					 * System.out.println("curFrame.length: " +
					 * curFrame.length); }
					 */
					linSpec[band] += curFrame[band] / length;
				}
			}
			// else
			// System.out.println("why's currFrame == null???");
		}
		// convert log magnitude to linear magnitude for binning
		for (int band = 0; band < linSpec.length; band++)
			// linSpec[band] = Math.exp(linSpec[band]);
			linSpec[band] = Math.pow(10, linSpec[band] / 10);

		// convert to mel scale
		for (int bin = 0; bin < outDim; bin++)
		{
			// initialize
			melSpec[bin] = 0;

			for (int i = 0; i < linSpec.length; ++i)
			{
				double weight = 1.0 - (Math.abs(melOfLin[i] - melCenter[bin]) / melWidth[bin]);
				if (weight > 0)
				{
					melSpec[bin] += weight * linSpec[i];
				}
			}

			// Take log
			melSpec[bin] = 10 * Math.log(melSpec[bin]) / Math.log(10);

			sum += melSpec[bin];
		}

		// Audio scrubber takes care of normalization, level is a good cue
		// for(int bin=0; bin<outDim; bin++)
		// melSpec[bin] = melSpec[bin] / sum;

		return melSpec;
	}

	public String description()
	{
		return "Computes the mean spectrum of a chunk and converts it to the perceptually weighted Mel frequency scale.";
	}
}
