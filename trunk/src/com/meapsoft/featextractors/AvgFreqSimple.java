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
 * Picks the strongest fourier harmonic in every frame and calls it the freq.
 * 
 * Based on AvgPitchSimple
 * 
 * @author Dan Ellis (dpwe@ee.columbia.edu) 2006-05-02, adapted by DR
 */

public class AvgFreqSimple extends FeatureExtractor
{
	// earliest FFT bin to use
	//static final int FIRSTBAND = 5;
	protected int firstBand = 3;

	// double[] pitchWt;
	protected double[] linSpec;

	protected double bin2hz;

	protected int N;

	// Default constructor
	public AvgFreqSimple()
	{
		this(FeatExtractor.nfft / 2 + 1, FeatExtractor.feSamplingRate);
	}

	public AvgFreqSimple(int N, double sampleRate)
	{
		this.N = N;
		linSpec = new double[N];

		// Create the weighting profile for choosing the pitch
		// Gaussian in log-F space centered on 110Hz with SD of 2 octaves

		bin2hz = sampleRate / (2 * (N - 1));
		
		//System.out.println("SR: " + sampleRate);
	}

	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{
		double[] freq = new double[1];
		double[] curFrame;

		boolean recalculateSTFT = stft.getRows() != N;
		
		RingMatrix newstft = null;
		if (recalculateSTFT)
		{
			if (preEmphasis)
			{
				double[] origSamples = stft.getSamples(startFrame, startFrame + length);
				double[] preEmphSamples = com.meapsoft.DSP.preEmphasis(origSamples);
				newstft = STFT.getSTFT(preEmphSamples, (N - 1) * 2, stft.nhop);
				length = newstft.getColumns();
			}
			else
			{
				// keep the same number of frames as in stft
				newstft = STFT.getSTFT(stft.getSamples(startFrame, startFrame
						+ length), (N - 1) * 2, stft.nhop);
				length = newstft.getColumns();
			}
		}
		

		// if there are no features we just bail.
		if (length == 0)
			return null;

		//double wtdsum = 0;
		// double sumwts = 0;

		// collect average linear spectrum
		Arrays.fill(linSpec, 0);
		for (int frame = 0; frame < length; frame++)
		{

			if (!recalculateSTFT)
				curFrame = stft.getFrame(startFrame + frame);
			else
				curFrame = newstft.getColumn(frame);

			if (curFrame != null)
			{
				for (int band = 0; band < linSpec.length; band++)
					linSpec[band] += Math.pow(10, curFrame[band] / 10) / length;
			}
		}

		// now pick best peak from linspec

		double pmax = -1;
		int maxbin = 0;
		for (int band = firstBand; band < N; band++)
		{
			// double pwr = pitchWt[band]*linSpec[band];
			double pwr = linSpec[band];
			if (pwr > pmax)
			{
				pmax = pwr;
				maxbin = band;
			}
		}
		
		// cubic interpolation
		double yz = linSpec[maxbin];
		double ym = linSpec[maxbin - 1];
		double yp = linSpec[maxbin + 1];
		// treating Y as k(x-x0)^2 + c, we have samples at y(0), y(-1), y(1)
		// yz = k x0^2 + c
		// yp = k (1-x0)^2 + c = k + k x0 ^ 2 - 2 k x0 + c
		// ym = k (1+x0)^2 + c = k + k x0 ^ 2 + 2 k x0 + c
		double k = (yp + ym) / 2 - yz;
		double x0 = (ym - yp) / (4 * k);
		// double c = yz - k*Math.pow(x0,2);
		// y = kx^2 +kx0^2 -2kx0.x +c
		// dy/dx = 2kx - 2kx0 = 0 when x = x0

		//System.out.println("bin2hz: " + bin2hz + " maxbin: " + maxbin + " x0: " + x0);
		// pitch[0] = hz2octs(bin2hz * (maxbin + x0));
		freq[0] = bin2hz * (maxbin + x0);
		
		
		//will this keep us from exploding???
		//going to try switching to linear interp here.
		//i think it's always the case that ym > yz in these situations,
		//rather than that yp > yz
		//if ( ym > yz || yp > yz) 
		if (ym > yz)
		{
			//System.out.println("ym > yz, switching to linear interp.");
			//System.out.println("yz: " + yz + " ym: " + ym + " yp: " + yp + " k: " + k);
			//System.out.println("bin2hz: " + bin2hz + " maxbin: " + maxbin + " x0: " + x0 + " freq: " + freq[0]);
			
			//double y0 = ym;
			x0 = 3;
			//double y1 = yz;
			//double x1 = 2;
			double x = 0.0;
			//double y = (ym + yz + yp)/3.0;
			
			//(y - y0)/(y1 - y0) = (x - x0)/(x1 - x0)
			
			//((x1 - x0) * (y = y0))/(y1 - y0) = x - x0
			//x = x0 + ((x1 - x0) * (y - y0))/(y1 - y0);
			
			//hmm, not sure what I'm doing exactly, but it seems to work
			//just nudging x left or right depending on center of gravity 
			//of the energy in ym and yz
			x = ym/(ym + yz);
						
			freq[0] = bin2hz * (maxbin - x);
			//System.out.println("bin2hz: " + bin2hz + " maxbin: " + maxbin + " x: " + x + " freq: " + freq[0]);
			
		}
		
		//not sure why we end up with large negative numbers sometimes!
		if (freq[0] < 0.0)
		{
			//System.out.println("yz: " + yz + " ym: " + ym + " yp: " + yp + " k: " + k);
			//System.out.println("bin2hz: " + bin2hz + " maxbin: " + maxbin + " x0: " + x0 + " freq: " + freq[0]);
			//System.out.println("freq[0] < 0.0, zeroing freq.");
			freq[0] = 0.0;
		}

		//why are we exploding???
		if (freq[0] > bin2hz * N)
		{
			//System.out.println("freq exploded, zeroing freq.");
			//System.out.println("yz: " + yz + " ym: " + ym + " yp: " + yp + " k: " + k);
			//System.out.println("bin2hz: " + bin2hz + " N: " + N + " maxbin: " + maxbin + " x0: " + x0 + " freq: " + freq[0]);
			freq[0] = bin2hz * maxbin;
		}
		
		if (Double.isNaN(freq[0]))
		{
			//System.out.println("uh oh, AvgFreqSimple got a Nan, zeroing freq.");
			//System.out.println("yz: " + yz + " ym: " + ym + " yp: " + yp + " k: " + k);
			//System.out.println("bin2hz: " + bin2hz + " N: " + N + " maxbin: " + maxbin + " x0: " + x0 + " freq: " + freq[0]);

			freq[0] = 0.0;
		}
		
		//System.out.println("freq[0]: " + freq[0]);
		return freq;
	}

	public String description()
	{
		return "Provides a frequency estimation (in Hz) for each segment of sound.";
	}
}
