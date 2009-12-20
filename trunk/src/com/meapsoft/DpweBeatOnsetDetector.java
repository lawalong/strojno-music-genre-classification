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

import java.util.Arrays;

/*
 * Beat detector based on Dan Ellis' beattrack.m.
 * 
 * 3 stages:
 * 
 * 1. Get an envelope that indicates the onsets Here, we take a dB mel
 * spectrogram, sum it across frequency, then take first order difference (and
 * maybe smooth the result).
 * 
 * 2. Estimate the global tempo We autocorrelate the entire onset envelope and
 * choose the best peak in the acceptable range (around 100-250 bpm).
 * 
 * 3. Go through the onset envelope choosing the best set of times that both lie
 * near a lot of maxima in the envelope and are spaced apart by the global tempo
 * period. We do this with dynamic programming, choosing a best predecessor for
 * every beat, then finally tracing back the beat that has the best total score
 * at the end.
 * 
 * @author Dan Ellis (dpwe@ee.columbia.edu)
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class DpweBeatOnsetDetector extends DpweOnsetDetector
{
	private static final int acmax = 128;

	// stabilize beat over this many periods each way
	// - more = smoother, should be integer
	// private static final int stabwin = 2;

	// submultiple of the detected tactus = 1/mult
	private double divisor = 1;

	// DP search constants
	private double tightness = 6.0; // strictness to tempo

	private double alpha = 0.9; // how much history is weighted vs. local events

	// no thresholds here
	public DpweBeatOnsetDetector(STFT stft, long numFrames)
	{
		super(stft, numFrames, 0);
	}

	public DpweBeatOnsetDetector(STFT stft, long numFrames, double mult)
	{
		this(stft, numFrames);

		divisor = 1 / mult;
	}

	protected void checkOnsets()
	{
		double[] mm = onsetFunction;

		// DSP.imagesc(DSP.slice(mm, 0, 10000), "mm");

		// remove DC
		double[] b = { 1, -1 };
		double[] a = { 1, -0.99 };
		double[] fmm = DSP.filter(b, a, mm);

		// DSP.imagesc(DSP.slice(fmm, 0, 10000), "fmm");

		double[] xfmm = DSP.xcorr(fmm, fmm, acmax);

		// find local max in the global ac
		xfmm = DSP.slice(xfmm, acmax, 2 * acmax);
		byte[] xpks = localmax(xfmm);

		// will not include 'edge peak' at first index, but make sure
		xpks[0] = 0;

		// delete all peaks that occur before first point below zero)
		// for(int x = 0; x < xfmm.length; x++)
		// if(xfmm[x] < 0)
		// break;
		// else
		// xpks[x] = 0;

		// largest local max after first neg pts
		double maxpk = DSP.max(DSP.subsref(xfmm, xpks));

		// DSP.imagesc(xfmm, "xfmm");
		// DSP.imagesc(DSP.times(xfmm, DSP.todouble(xpks)), "xpks");

		// then period is shortest period with a peak that approaches
		// the max
		int startpd = 0;
		int pd = 0;
		for (int x = 0; x < xfmm.length; x++)
		{
			if (xpks[x] == 1)
			{
				if (xfmm[x] > 0.5 * maxpk)
				{
					startpd = x;
					break;
				}
			}
		}

		// apply divisor
		startpd = (int) (divisor * startpd);
		// should be pretty stable
		pd = startpd;

		// % Smooth beat events
		// templt = exp(-0.5*(([-pd:pd]/(pd/32)).^2));
		double[] templt = DSP.exp(DSP.times(DSP.power(DSP.times(DSP.range(-pd,
				pd), 32.0 / pd), 2.0), -0.5));
		// localscore = conv(templt,onsetenv);
		// localscore =
		// localscore(round(length(templt)/2)+[1:length(onsetenv)]);
		double[] localscore = DSP.conv(templt, fmm);
		localscore = DSP.slice(localscore, templt.length / 2, templt.length / 2
				+ fmm.length - 1);
		// DSP.imagesc(localscore, "localscore");

		// % DP version:
		// % backlink(time) is index of best preceding time for this point
		// % cumscore(time) is total cumulated score to this point

		// backlink = zeros(1,length(localscore));
		// cumscore = zeros(1,length(localscore));

		int[] backlink = new int[localscore.length];
		double[] cumscore = new double[localscore.length];

		// % search range for previous beat
		// prange = round(-2*pd):-round(pd/2);
		int prangemin = -2 * pd;
		int prangemax = -pd / 2;

		// % Skewed window
		// txwt = exp(-0.5*((tightness*log(prange/-pd)).^2));
		double[] txwt = DSP.exp(DSP.times(DSP.power(
				DSP.times(DSP.log(DSP.times(DSP.range(prangemin, prangemax),
						-1.0 / pd)), tightness), 2.0), -0.5));

		// starting = 1;
		int starting = 1;
		double maxlocalscore = DSP.max(localscore);

		// for i = 1:length(localscore)
		for (int i = 0; i < localscore.length; ++i)
		{

			// timerange = i + prange;
			double[] scorecands = new double[txwt.length];
			double[] valvals;

			// % Are we reaching back before time zero?
			// zpad = max(0, min(1-timerange(1),length(prange)));
			if (i + prangemin < 0)
			{
				int valpts = 0;
				for (int j = 0; j < scorecands.length; ++j)
					scorecands[j] = 0;
				if (i + prangemax >= 0)
				{
					valpts = i + prangemax + 1;
					valvals = DSP.times(DSP.slice(txwt, txwt.length - valpts,
							txwt.length - 1), DSP.slice(cumscore, i + prangemax
							- valpts + 1, i + prangemax));
					for (int j = 0; j < valpts; ++j)
						scorecands[scorecands.length - valpts + j] = valvals[j];
				}
			}
			else
			{
				// % Search over all possible predecessors and apply transition
				// % weighting
				// scorecands = txwt .*
				// [zeros(1,zpad),cumscore(timerange(zpad+1:end))];
				scorecands = DSP.times(txwt, DSP.slice(cumscore, i + prangemin,
						i + prangemax));
			}

			// % Find best predecessor beat
			// [vv,xx] = max(scorecands);
			double vv = DSP.max(scorecands);
			int xx = DSP.argmax(scorecands);
			// % Add on local score
			// cumscore(i) = alpha*vv + (1-alpha)*localscore(i);
			cumscore[i] = alpha * vv + (1 - alpha) * localscore[i];

			// % special case to catch first onset
			// if starting == 1 & localscore(i) < 0.01*max(localscore);
			if (starting == 1 && localscore[i] < 0.01 * maxlocalscore)
			{
				// backlink(i) = -1;
				backlink[i] = -1;
			}
			else
			{
				// backlink(i) = timerange(xx);
				backlink[i] = i + prangemin + xx;
				// % prevent it from resetting, even through a stretch of
				// silence
				starting = 0;
			}
		}

		// %%%% Backtrace

		// % Cumulated score is stabilized to lie in constant range,
		// % so just look for one near the end that has a reasonable score
		// medscore = median(cumscore(localmax(cumscore)));
		double medscore = DSP.median(DSP.subsref(cumscore, localmax(cumscore)));
		// bestendx = max(find(cumscore .* localmax(cumscore) > 0.5*medscore));
		int bestendx = 0;
		int jj = cumscore.length - 2;
		while (jj > 0 && bestendx == 0)
		{
			if (cumscore[jj] > cumscore[jj - 1]
					&& cumscore[jj] >= cumscore[jj + 1]
					&& cumscore[jj] > 0.5 * medscore)
			{
				bestendx = jj;
			}
			--jj;
		}

		// b = bestendx;

		int nbeats = 0;
		int[] tmplinks = new int[cumscore.length];
		tmplinks[0] = bestendx;
		// while backlink(b(end)) > 0
		int bb;
		while ((bb = backlink[tmplinks[nbeats]]) > 0)
		{
			// b = [b,backlink(b(end))];
			++nbeats;
			tmplinks[nbeats] = bb;
		}

		// b = fliplr(b);
		++nbeats;

		// % return beat times in secs
		// b = b / sgsrate;

		for (int j = 0; j < nbeats; ++j)
		{
			int thisbeat = tmplinks[nbeats - 1 - j];
			// System.out.println("thisbeat = "+thisbeat+" nextbeat =
			// "+nextbeat+" pd = "+pd);
			// if(cb < fmmxs.length)
			// fmmxs[cb] = fmmx;
			// if(cb < xcrs.length)
			// xcrs[cb++] = xcr;

			notifyListeners(thisbeat, 0);
		}

		// DSP.imagesc(DSP.transpose(fmmxs), "fmmxs");
		// DSP.imagesc(templt), "templt");
		// DSP.imagesc(DSP.transpose(xcrs), "xcrs");
	}

	// Find local maxima in function a. Returns a binary array with
	// 1's where a has a local maximum.
	protected byte[] localmax(double[] a)
	{
		byte[] v = new byte[a.length];
		Arrays.fill(v, (byte) 0);

		for (int fr = 1; fr < a.length - 1; fr++)
			if (a[fr] > a[fr - 1] && a[fr] > a[fr + 1])
				v[fr] = 1;

		return v;
	}
}
