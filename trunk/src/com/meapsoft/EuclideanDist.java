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

/**
 * Simple distance metric calculating the euclidean distance (squared) between
 * two chunks' feature vectors.
 * 
 * @author Mike Mandel (mim@ee.columbia.edu)
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */

public class EuclideanDist extends ChunkDist
{
	public EuclideanDist()
	{
	}

	public EuclideanDist(ChunkDist o)
	{
		super(o);
	}

	public EuclideanDist(int[] i)
	{
		super(i);
	}

	public EuclideanDist(ChunkDist o, int[] i)
	{
		super(o, i);
	}

	public double distance(Chunk ch1, Chunk ch2)
	{
		FeatChunk c1 = null;
		FeatChunk c2 = null;

		// this only works on FeatChunks
		try
		{
			c1 = (FeatChunk) ch1;
			c2 = (FeatChunk) ch2;
		}
		catch (ClassCastException e)
		{
			return super.distance(ch1, ch2);
		}

		double[] f1 = c1.getFeatures(featdim);
		double[] f2 = c2.getFeatures(featdim);
		double distSq = super.distance(c1, c2);

		for (int i = 0; i < f1.length; i++)
			distSq += (f1[i] - f2[i]) * (f1[i] - f2[i]);

		return distSq;
	}
}
