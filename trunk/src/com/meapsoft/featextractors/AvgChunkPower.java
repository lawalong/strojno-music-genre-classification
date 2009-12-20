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

import com.meapsoft.DSP;
import com.meapsoft.STFT;

/**
 * A really simple feature calculation that calculates the average power in a
 * chunk
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */

public class AvgChunkPower extends FeatureExtractor
{
	public AvgChunkPower()
	{
		
	}
	
	
	
	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{
		double[] power = new double[1];
		power[0] = 0;
		double[] samples = stft.getSamples(startFrame, startFrame + length);

		for (int i = 0; i < samples.length; i++)
			power[0] += samples[i] * samples[i] / samples.length;

		// log base 10
		// power[0] = Math.log(power[0])/Math.log(10);

		double[] logPower = DSP.times(DSP.log10(power), 10.0);
		if (logPower[0] == Double.NEGATIVE_INFINITY)
		{
			System.out.println("AvgChunkPower got a -Inf, setting power to -200.0");
			logPower[0] = -200.0;
		}
		return logPower;
	}

	public String description()
	{
		return "Computes the average power (in dB) in each chunk.";
	}
}
