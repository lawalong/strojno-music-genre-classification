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

package com.meapsoft.disgraced;

import com.meapsoft.STFT;
import com.meapsoft.featextractors.FeatureExtractor;

/**
 * A really simple feature calculation that calculates the total power
 * in a chunk
 *
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */

public class ChunkPower extends FeatureExtractor 
{	
    public double[] features(STFT stft, long startFrame, int length) 
    {
        double[] power = new double[1];
        power[0] = 0;
        double[] samples = stft.getSamples(startFrame, startFrame+length);

        for(int i=0; i<samples.length; i++) 
            power[0] += samples[i]*samples[i];
        
        return power;
    }

	public String description()
	{
		return "Computes the total power in each chunk.";
	}

	public double[] features(STFT stft, long startFrame, int length,
			boolean preEmphasis) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
