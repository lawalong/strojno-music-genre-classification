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

import com.meapsoft.DSP;

/**
 * Averages all spectral frames together into a single feature vector and then
 * applies a log-frequency harmonic template to enhance harmonic sets, 
 * then picks the biggest peak.
 * 
 * Based on AvgChroma
 * 
 * @author Dan Ellis (dpwe@ee.columbia.edu)
 */

public class AvgPitchTplt extends FeatureExtractor
{

	protected double[][] mapMx;

	protected double[] linSpec;

	protected int N, BPO, nOcts, nOpBins;

	public static final double LOG2 = 0.69314718055995;
	public static final double LOG_ONE_SEMITONE = 0.057762265046662;
	public static final double MIDI0C_HZ = 8.1757989156;
	public static final double MIDI_BPO = 12.0;
	public static final double C0_HZ = 16.3516;

	//    protected static int firstTime = 0;  // no first time at all

	protected double lowestFrq = 65.4;

	public double hz2octs(double fq)
	{
		if (fq <= 0)
			return 0;
		// notes above BASEPITCH will be positive, below will be negative.
		double BASEPITCH = C0_HZ;
		// with C0_HZ, integer part of return is octave number,
		// fractional part is chroma.
		// So C0 (16.35 Hz) -> 0.0, A0 (27.5 Hz) -> 0.25,
		// C4 (262 Hz) -> 4.0, A4 (440 Hz) -> 4.25

		return Math.log(fq / BASEPITCH) / LOG2;
	}

	// Default constructor
	public AvgPitchTplt()
	{
		this(FeatExtractor.nfft / 2 + 1, 48, 5, FeatExtractor.feSamplingRate);
	}

	public AvgPitchTplt(int N, int BPO, int nOcts, double sampleRate)
	{
		this.N = N;
		this.BPO = BPO;
		this.nOcts = nOcts;
		linSpec = new double[N];
		int nOpBins = nOcts * BPO;
		this.nOpBins = nOpBins;

		mapMx = new double[nOpBins][N];

		//System.out.println("N="+N);
		//System.out.println("sampleRate="+sampleRate);


		// Create the mapping matrix
		// mapMx = pitchTplt * logFmap

		// first, the logfsgram array (after logfsgram.m)
		// frequency spacing of FFT bins
		double hzPerBin = 2*sampleRate/(2*(N-1));
		double fratio = Math.pow(2.0, 1.0/(double)BPO);
		int nbins = (int)Math.floor(Math.log(sampleRate/2.0/lowestFrq)/Math.log(fratio));
		// the FFT-to-logfsgram matrix
		double LFM[][] = new double[nbins][N];

		// Freqs corresponding to each bin in FFT
		double fftfrqs[] = new double[N];
		for (int i = 0; i < N; ++i) {
			fftfrqs[i] = i*hzPerBin;
		}
		// Freqs corresponding to each bin in log F output
		double logffrqs[] = new double[nbins];
		for (int i = 0; i < nbins; ++i) {
			logffrqs[i] = lowestFrq*Math.exp(LOG2*((double)i)/(double)BPO);
		}
		// Bandwidths of each bin in log F
		// .. but bandwidth cannot be less than FFT binwidth, hzPerBin
		double logfbws[] = new double[nbins];
		for (int i = 0; i < nbins; ++i) {
			logfbws[i] = logffrqs[i] * (fratio - 1.0);
			if (logfbws[i] < hzPerBin)  logfbws[i] = hzPerBin;
		}
		double ovfctr = 0.5475;   // Adjusted by hand to make sum(mx'*mx) close to 1.0
		// Weighting matrix mapping energy in FFT bins to logF bins
		// is a set of Gaussian profiles depending on the difference in 
		// frequencies, scaled by the bandwidth of that bin
		for (int i = 0; i < nbins; ++i) {
			double rowE = 0;
			for (int j = 0; j < N; ++j) {
				double freqdiff = (logffrqs[i] - fftfrqs[j])/(ovfctr * logfbws[i]);
				LFM[i][j] = Math.exp(-0.5*freqdiff*freqdiff);
				rowE = rowE + LFM[i][j]*LFM[i][j];
			}
			// Normalize rows by sqrt(E), so multiplying by mx' gets approx orig spec back
			for (int j = 0; j < N; ++j) {
				LFM[i][j] = LFM[i][j] / Math.sqrt(2*rowE);
			}
		}

		// Weight frequencies around 400 Hz by scaling each row
		double fctr = 400;
		double fsdoct = 1.5;
		for (int i = 0; i < nbins; ++i) {
			double wt = Math.exp(-0.5*Math.pow(Math.log(logffrqs[i]/fctr)/LOG2/fsdoct,2));
			for (int j = 0; j < N; ++j) {
				LFM[i][j] = LFM[i][j]*wt;
			}
		}

		// Construct the pitch harmonics template
		int nharmonics = 15;
		int tpltlen = 4 * BPO;
		double tpltsd = 0.5;
		double tplt[] = new double[tpltlen];
		Arrays.fill(tplt, 0);

		for (int i = 1; i <= nharmonics; ++i) {
			double hb = (double)BPO * Math.log((double)i)/LOG2;
			for (int j = 0; j < tpltlen; ++j) {
				double bd = ((double)j - hb)/tpltsd;
				tplt[j] = tplt[j] + 1.0/(double)i * Math.exp(-0.5*bd*bd);
			}
		}
		double sumtplt = 0;
		// normalize sum to 1
		for (int j = 0; j < tpltlen; ++j) {
			sumtplt = sumtplt + tplt[j];
		}
		for (int j = 0; j < tpltlen; ++j) {
			tplt[j] = tplt[j]/sumtplt;
		}

		// Build a matrix of templates for every pitch
		double TM[][] = new double[nOpBins][nbins];
		for (int i = 0; i < nOpBins; ++i) {
			Arrays.fill(TM[i], 0);
			int tpl = tpltlen;
			if ( (tpl + i) > nbins ) {
				tpl = nbins - i;
			}
			for (int j = 0; j < tpl; ++j) {
				TM[i][i+j] = tplt[j];
			}
		}


		// Combine the matrices together
		for (int i = 0; i < nOpBins; ++i) {
			for (int j = 0; j < N; ++j) {
				double x = 0;
				for (int k = 0; k < nbins; ++k) {
					x = x + TM[i][k]*LFM[k][j];
				}
				mapMx[i][j] = x;
			}
		}

		// Save it out for debug
		//if (firstTime == 1) {
		//    for (int i = 0; i < nOpBins; ++i) {
		//	for (int j = 0; j < N; ++j) {
		//	    System.out.print(mapMx[i][j] + " ");
		//	}
		//	System.out.println("");
		//   }
		//    firstTime = 0;
		//} 


	}

	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{
		int boff = 0;
		//double[] logSpec = new double[nOpBins];
		double[] curFrame;
		double sum = 0;
		double sum2 = 0;
		double[] pitch = new double[1];

		boolean recalculateSTFT = stft.getRows() != N;
		RingMatrix newstft = null;
		if (recalculateSTFT)
		{
			// keep the same number of frames as in stft
			newstft = STFT.getSTFT(stft.getSamples(startFrame, startFrame
					+ length), (N - 1) * 2, stft.nhop);
			length = newstft.getColumns();
		}


		// Apply mapping to each frame, keep histogram of results
		int[] pitchcounts = new int[nOpBins];
		Arrays.fill(pitchcounts, 0);
		for (int frame = 0; frame < length; frame++) {
			if (!recalculateSTFT)
				curFrame = stft.getFrame(startFrame + frame);
			else
				curFrame = newstft.getColumn(frame);

			if (curFrame != null) {
				for (int band = 0; band < linSpec.length; band++) {
					linSpec[band] = Math.pow(10, curFrame[band] / 20);
					//System.out.print(linSpec[band] + " ");
				}
				//System.out.println("");

				// matrix multiply to find bins
				int maxbinix = 0;
				double maxbinval = 0;

				for (int bin = 0; bin < nOpBins; bin++) {
					double val = 0;
					for (int band = 0; band < linSpec.length; band++) {
						val += linSpec[band] * mapMx[bin][band];
					}
					// logSpec[bin] = val;
					if (val > maxbinval) {
						maxbinix = bin;
						maxbinval = val;
					}
				}
				++pitchcounts[maxbinix];
			}
		}
		// find the most popular pitch
		int maxbinix = 0;
		int maxbincount = 0;
		for (int bin = 0; bin < nOpBins; bin++) {
			if ( pitchcounts[bin] > maxbincount ) {
				maxbinix = bin;
				maxbincount = pitchcounts[bin];
			}
		}

		// return the bin index mapped to MIDI numbers
		double midiVal = MIDI_BPO*(Math.log(lowestFrq/MIDI0C_HZ)/LOG2 
				+ (((double)maxbinix)/BPO));

		pitch[0] = (double)midiVal;

		return pitch;
	}

	public String description()
	{
		return "MIDI value corresponding to dominant pitch in segment, as found by correlating log-f-spectrum against a template";
	}
}
