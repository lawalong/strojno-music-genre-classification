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

import com.meapsoft.featextractors.AvgMelSpec;

import java.util.Arrays;

import com.meapsoft.DSP;
import com.meapsoft.STFT;

/**
 * Computes the entropy of each chunk.
 * 
 * @author Victor Adan (vga2102@columbia.edu)
 */

public class Entropy extends FeatureExtractor
{

	private AvgMelSpec melSpec;

	static final int swin = 1024;
	static final int nmel = 40;
	static final int sr = 32000;

	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{

		double[][] frames = new double[length][nmel];
		double[] currFrame;
		double[] workFrame;
		double dist;
		double[][] distMat = new double[length][length];
		double V;
		double[] px = new double[length];
		double[] entropy = new double[1];
		entropy[0] = 0;
		int k = (int) Math.round(Math.sqrt(length));

		melSpec = new AvgMelSpec(swin / 2 + 1, sr, nmel);

		// min and max 
		double maxVal = 0;
		double minVal = 0;
		for (int frame = 0; frame < length; frame++)
		{
			frames[frame] = melSpec.features(stft, startFrame + frame, 1, false);
			currFrame = frames[frame];
			// normalization factor currFrame
         double maxTmp = DSP.max(currFrame);
			if (maxTmp > maxVal)
			{
				maxVal = maxTmp;
			}
         double minTmp = DSP.min(currFrame);
			if (minTmp < minVal)
			{
				minVal = minTmp;
			}
		}

      //normalize data
      for (int frame = 0; frame < length; frame ++)
      {
         //System.out.println("bin val");
         for (int bin=0; bin < nmel; bin++){
            frames[frame][bin] -=  minVal;
            frames[frame][bin] /=  (maxVal-minVal);
            //System.out.print(frames[frame][bin] + " ");

         }
      }

		for (int frame = 0; frame < length; frame++)
		{
			currFrame = frames[frame];
			for (int wFrame= 0; wFrame < length; wFrame++)
			{
            workFrame = frames[wFrame] ;
				dist = 0;
				// get distances
				for (int i = 0; i < nmel; i++)
				{
					dist += Math.pow((currFrame[i] - workFrame[i] ), 2);
				}
            //if (Double.isNaN(dist))
            //   dist = 0;
				dist = Math.sqrt(dist);
				distMat[frame][wFrame] = dist;
				distMat[wFrame][frame] = dist;
			}
			Arrays.sort(distMat[frame]);

			if (distMat[frame][k] < 0.0001)
			{
				//System.out.println("Found distance < 0.0001");
				distMat[frame][k] = 0.0001;
			}
         //System.out.println("dist mat");
         //System.out.println(distMat[frame][k]);
			V = Math.pow(distMat[frame][k]+1, nmel/2.0) ;
			px[frame] = ((double) k / length) / V;
         //System.out.println("px");
         //System.out.println(px[frame]);
		}

		// normalize pmf // this should not be done.
		//double pxsum;
		//pxsum = DSP.sum(px);
      //System.out.println("pxsum");
      //System.out.println(pxsum);
		for (int i = 0; i < length; i++)
		{
			//px[i] /= pxsum;
			entropy[0] += px[i] * Math.log(1.0 / px[i]) / Math.log(2);

		}
		//System.out.println(entropy[0]);
      entropy[0] /= (Math.log(length) / Math.log(2));
		//System.out.println(entropy[0]);
		//System.out.println("---");
		return entropy;
	}

	public String description()
	{
		return "Computes the entropy of each chunk.";
	}
}
