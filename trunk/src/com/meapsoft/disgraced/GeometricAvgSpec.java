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
 * A simple feature calculation that computes the geometric mean
 * (arithmetic mean in log magnitude) of all spectral frames into a
 * single vector.  This is probably not an especially useful feature.
 *
 * @author Mike Mandel (mim@ee.columbia.edu)
 */

public class GeometricAvgSpec extends FeatureExtractor 
{
	
    public double[] features(STFT stft, long startFrame, int length) 
    {

        double[] avgSpec= new double[stft.getRows()];
        double[] curFrame;
        double sum = 0;
        
        for(int band=0; band<avgSpec.length; band++)
            avgSpec[band] = 0;
        
        for(int frame=0; frame<length; frame++) 
        {
            curFrame = stft.getFrame(startFrame+frame);
            for(int band=0; band<avgSpec.length; band++) 
            {
                double tmp = curFrame[band] / length;
                avgSpec[band] += tmp;
                sum += tmp;
            }
        }

        for(int band=0; band<avgSpec.length; band++)
            avgSpec[band] = avgSpec[band] / sum;
        
        return avgSpec;
    }

	public String description()
	{
		return "I am a generic FeatureExtractor";
	}

	public double[] features(STFT stft, long startFrame, int length,
			boolean preEmphasis) {
		// TODO Auto-generated method stub
		return null;
	}
}
