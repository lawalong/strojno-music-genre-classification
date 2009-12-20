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

package com.meapsoft;

import com.meapsoft.featextractors.AvgMelSpec;

/*
 * Onset detector based on Dan Ellis' beattrack.m.
 * 
 * Get an envelope that indicates the onsets. Here, we take a dB mel
 * spectrogram, sum it across frequency, then take first order difference (and
 * maybe smooth the result).
 * 
 * @author Dan Ellis (dpwe@ee.columbia.edu) @author Ron Weiss
 * (ronw@ee.columbia.edu)
 */
public class DpweOnsetDetector extends OnsetDetector
{
	// Onset detection function computed from the STFT
	protected double[] onsetFunction;

	// number of frames in onsetFunction
	private long numFrames;

	// silence threshold in dB
	protected double silenceThresh = -40;

	// gain applied to the median filter threshold in checkOnsets
	private double threshMult = 1;

	private AvgMelSpec melSpec;

	private double[] lastMelFrame = null;

	private double smtime = 0.10;

	static final int sr = 32000;

	static final int swin = 1024;

	static final int nmel = 40;

	static final double sgsrate = sr / (swin / 2);

	// dpwe debug 2006-12-11
	// static final int nDebugFrames = 1500;
	// private double[][] melFrames = new double[nDebugFrames][nmel];
	// private int nMelFrames = 0;

	// private double[] boost;

	// private double[][] specgram;

	public DpweOnsetDetector(STFT stft, long numFrames, double thresh)
	{
		super(stft, 0, 0);

		threshMult = thresh;

		onsetFunction = new double[(int) numFrames];
		this.numFrames = numFrames;
		melSpec = new AvgMelSpec(swin / 2 + 1, sr, nmel);

		// low frequency boost for bass drum
		// double[] freqs = DSP.times(DSP.range(0,swin/2), sr/swin);
		// double[] lfboost =
		// DSP.times(DSP.log10(DSP.max(DSP.plus(DSP.rdivide(DSP.minus(200,
		// freqs), 200.0), 1), 1)), 10.0);
		// // high frequency boost for noisy drums
		// //double[] hfboost =
		// DSP.times(DSP.log10(DSP.max(DSP.plus(DSP.rdivide(DSP.minus(freqs,
		// 6000), 6000), 1), 1)), 10.0);
		// double[] hfboost = new double[lfboost.length];
		// for(int x = 0; x < hfboost.length; x++)
		// {
		// if(freqs[x] < 6000)
		// hfboost[x] = 0;
		// else
		// hfboost[x] = 3;
		// }
		// boost = DSP.plus(lfboost, hfboost);

		// DSP.imagesc(boost);

		// specgram = new double[(int)numFrames][stft.getColumns()];
	}

	public DpweOnsetDetector(STFT stft, long numFrames, double thresh,
			double smt)
	{
		this(stft, numFrames, thresh);

		smtime = smt;
	}

	/**
	 * Computes the onset detection function in real time as the STFT gets new
	 * frames. Once the final frame is reached, look for onsets in the detection
	 * function.
	 */
	public void newFrame(STFT stft, long newestFrame)
	{
		if (newestFrame <= numFrames && newestFrame != -1)
		{
			// apply frequency weights
			// double[] D = stft.getFrame(newestFrame);
			// stft.setFrame(newestFrame, DSP.plus(D, boost));

			// if(newestFrame < specgram.length)
			// specgram[(int)newestFrame] = stft.getFrame(newestFrame);

			double[] melFrame = melSpec.features(stft, newestFrame, 1, false);

			// threshold:
			melFrame = DSP.max(melFrame, silenceThresh);

			// dpwe debug 2006-12-11
			// if (nMelFrames < nDebugFrames) {
			// for (int j = 0; j < nmel; ++j) {
			// melFrames[nMelFrames][j] = melFrame[j];
			// }
			// ++nMelFrames;
			// if (nMelFrames == nDebugFrames) {
			// DSP.imagesc(melFrames, "melFrames");
			// }
			// }

			// is this the first frame we've seen?
			if (lastMelFrame == null)
			{
				lastMelFrame = melFrame;
				return;
			}

			long currFrame = newestFrame - 1;
			onsetFunction[(int) currFrame] = DSP.mean(DSP.abs(DSP.minus(
					melFrame, lastMelFrame)));
			// DSP.mean(DSP.max(DSP.minus(melFrame, lastMelFrame), 0));

			lastMelFrame = melFrame;
		}
		else
		{
			// if this is the last frame in stft, do some smoothing and
			// find local maxes.
			checkOnsets();
		}
	}

	protected void checkOnsets()
	{
		// DSP.imagesc(specgram, "specgram");
		// DSP.imagesc(onsetFunction, "onset function");

		// DSP.wavwrite(DSP.rdivide(onsetFunction, DSP.max(onsetFunction)),
		// (int)sgsrate, "onsetFunction.wav");

		// smooth like crazy
		int winLen = (int) (smtime * sgsrate);
		// make it odd
		winLen = (int) Math.round((winLen - 1) / 2) * 2 + 1;
		double[] smwin = DSP.hanning(winLen);
		smwin = DSP.times(smwin, 1 / DSP.sum(smwin));
		onsetFunction = DSP.conv(smwin, onsetFunction);
		onsetFunction = DSP.slice(onsetFunction, (int) (winLen - 1) / 2,
				(int) (winLen - 1) / 2 + (int) numFrames - 1);

		// remove DC
		double[] b = { 1, -1 };
		double[] a = { 1, -0.99 };
		onsetFunction = DSP.filter(b, a, onsetFunction);

		onsetFunction = DSP.max(onsetFunction, 0);

		// normalize
		// onsetFunction = DSP.times(onsetFunction, 1/DSP.max(onsetFunction));

		// find local maxima in onsetFunction
		// double[] threshFunc = new double[onsetFunction.length];
		//double dcThresh = 0.005;
		//int nwin = 50;
		for (int fr = 1; fr < onsetFunction.length - 1; fr++)
		{
			// threshold using a median filter over 50 point window:
			double thresh = threshMult;
			/*
				dcThresh
					+ threshMult
					* DSP.median(DSP.slice(onsetFunction, fr > nwin / 2 ? fr
							- nwin / 2 : 0, fr > onsetFunction.length - nwin
							/ 2 ? onsetFunction.length : fr + nwin / 2 - 1));
			*/
			// threshFunc[fr] = thresh;

			if (onsetFunction[fr] > thresh
					&& onsetFunction[fr] > onsetFunction[fr - 1]
					&& onsetFunction[fr] > onsetFunction[fr + 1])
				notifyListeners(fr, 0);
		}

		// System.out.println(threshMult);

		// DSP.imagesc(onsetFunction, "smoothed onsetFunc");
		// DSP.imagesc(threshFunc, "thresh");
		// int len = (int)Math.min(20000, onsetFunction.length);
		// double[][] d = new double[2][len];
		// d[1] = DSP.slice(onsetFunction, 0, len);
		// d[0] = DSP.slice(threshFunc, 0, len);
		// DSP.imagesc(DSP.transpose(d), "onset function and threshold");

		// DSP.wavwrite(onsetFunction, (int)sgsrate,
		// "smoothedOnsetFunction.wav");
		// DSP.wavwrite(threshFunc, (int)sgsrate, "threshFunction.wav");
	}
}
