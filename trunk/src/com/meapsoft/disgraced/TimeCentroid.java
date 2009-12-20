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
 * A simple feature calculation that computes the centroid in time of
 * a given chunk.  This might be useful in seeing where a chunk is
 * localized in time (e.g. a stationary sinusoid will have a
 * TimeCentroid of .5, but a drum hit with a fast decay will have a
 * TimeCentroid closer to 0), or to test an OnsetDetector.
 *
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */

public class TimeCentroid extends FeatureExtractor 
{
	
    public double[] features(STFT stft, long startFrame, int length) 
    {
        double[] curFrame;
        double[] TimeCentroid = new double[1];
        double num0 = 0;
        //double num1 = 0;
        double den = 0;

        for(int frame=0; frame<length; frame++) 
        {
            curFrame = stft.getFrame(startFrame+frame);
            double timeCenter = stft.fr2Seconds(frame);

            for(int band=0; band<stft.getRows(); band++) 
            {
                //double freqCenter = band*(stft.samplingRate/2)/(stft.getRows()-1);
                // convert back to linear power
                double p = Math.pow(10,curFrame[band]/10);

                num0 += timeCenter*p;
                //num1 += freqCenter*p;
                den += p;
            }
        }
        
        TimeCentroid[0] = num0/den;
        //TimeFreqCentroid[1] = num1/den;

        // scale it by length so it ends up between zero and one.
        TimeCentroid[0] /= stft.fr2Seconds(length);

        return TimeCentroid;
    }

	public String description()
	{
		//return "Computes the center of mass in the time/frequency plane of each chunk's spectrogram.";
        return "Computes the center of mass of a chunk's power in time.  Gives a simple indication of a chunk's stationarity.";
	}

	public double[] features(STFT stft, long startFrame, int length,
			boolean preEmphasis) {
		// TODO Auto-generated method stub
		return null;
	}
}
