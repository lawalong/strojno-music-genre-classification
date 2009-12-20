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
 * A really simple feature calculation that calculates the RMS amplitude of a
 * chunk
 * 
 * @author Ron & Douglas
 */

public class RMSAmplitude extends FeatureExtractor
{
	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{
		double power = 0.0d;
		double[] samples = stft.getSamples(startFrame, startFrame + length);

		for (int i = 0; i < samples.length; i++)
			power += samples[i] * samples[i] / samples.length;

		double rms = Math.sqrt(power);

		double dB[] = new double[1];
		dB[0] = 20 * (Math.log(rms) / Math.log(10));

		return dB;
	}

	public String description()
	{
		return "Computes the RMS amplitude in dB for each chunk. 0dB is max amplitude.";
	}
}
