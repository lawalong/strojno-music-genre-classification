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

import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.STFT;

/**
 * Computes a "likelihood" or distinctiveness value for each chunk. Values start
 * near 0.0 for very common (but not necessarily similar) sounds and increase
 * for more distinctive sounds.
 * 
 * @author Dan Ellis/Douglas Repetto
 */
public class Likelihood extends MetaFeatureExtractor
{
	public void features(FeatFile featFile, boolean clearOriginalFeatures)
	{
		int totalChunks = featFile.chunks.size();

		FeatChunk testChunk = (FeatChunk) featFile.chunks.get(0);
		double[] testChunkFeatures = testChunk.getFeatures();
		int numFeatures = testChunkFeatures.length;

		// Pass 1 - gather feature statistics
		double sumx[] = new double[numFeatures];
		double sumx2[] = new double[numFeatures];
		double meanx[] = new double[numFeatures];
		double stdx[] = new double[numFeatures];
		double mahaldist[] = new double[totalChunks];

		Arrays.fill(sumx, 0);
		Arrays.fill(sumx2, 0);
		Arrays.fill(meanx, 0);
		Arrays.fill(stdx, 0);
		Arrays.fill(mahaldist, 0);

		double N = 0.0;

		for (int chunk = 0; chunk < totalChunks; chunk++)
		{
			FeatChunk curChunk = (FeatChunk) featFile.chunks.get(chunk);
			double[] features = curChunk.getFeatures();

			for (int feature = 0; feature < numFeatures; feature++)
			{
				sumx[feature] += features[feature];
				sumx2[feature] += features[feature] * features[feature];
			}
			N = N + 1.0;
		}

		// figure mean and variance
		for (int feature = 0; feature < numFeatures; feature++)
		{
			meanx[feature] = sumx[feature] / N;
			stdx[feature] = Math.sqrt(sumx2[feature] / N - meanx[feature]
					* meanx[feature]);
		}

		// Pass 2 - figure Mahalanobis distance (= unlikeliness score)
		// under diagonal covariance Gaussian for each frame
		// (i.e. Euclidean distance to mean in a variance-normalized space)

		for (int chunk = 0; chunk < totalChunks; chunk++)
		{
			FeatChunk curChunk = (FeatChunk) featFile.chunks.get(chunk);
			double[] features = curChunk.getFeatures();

			mahaldist[chunk] = 0;

			for (int feature = 0; feature < numFeatures; feature++)
			{
				double dist = (features[feature] - meanx[feature])
						/ stdx[feature];
				mahaldist[chunk] += dist * dist;
			}
			mahaldist[chunk] = Math.sqrt(mahaldist[chunk] / numFeatures);

			double value = mahaldist[chunk];
			double[] feats = new double[1];
			feats[0] = value;

			if (clearOriginalFeatures)
				curChunk.clearFeatures();

			curChunk.addFeature(feats);
		}
	}

	public String description()
	{
		return "Returns the likelihood of each chunk. Lower numbers mean a segment is more common, "
				+ "higher numbers mean it is more distinct.";
	}
}
