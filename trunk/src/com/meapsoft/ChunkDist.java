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

import java.io.Serializable;
import java.util.Comparator;

/**
 * A way to measure the distance between two chunks. Constructor can accept
 * another ChunkDist, which will be added to the first distance, so that
 * distance measures can be chained together.
 * 
 * @author Mike Mandel (mim@ee.columbia.edu)
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */

public abstract class ChunkDist implements Serializable, Comparator
{
	public Chunk targetChunk = null;

	public ChunkDist next;

	// only compute distance based on these feature dimensions
	public int[] featdim;

	public ChunkDist()
	{
		next = null;
		featdim = null;
	}

	public ChunkDist(ChunkDist next)
	{
		this.next = next;
	}

	public ChunkDist(int[] fd)
	{
		this();
		featdim = fd;
	}

	public ChunkDist(ChunkDist next, int[] fd)
	{
		this.next = next;
		featdim = fd;
	}

	/**
	 * Compute distance between two Chunks
	 */
	public double distance(Chunk c1, Chunk c2)
	{
		if (next != null)
			return next.distance(c1, c2);
		return 0;
	}

	/**
	 * Set target chunk for compare function to use. This needs to be called
	 * before compare() is called or there will be NullPointerExceptions.
	 */
	public void setTarget(Chunk t)
	{
		targetChunk = t;
	}

	/**
	 * Compare two Chunk's based on their distances from targetChunk
	 */
	public int compare(Object o1, Object o2) throws ClassCastException
	{
		Chunk c1 = (Chunk) o1;
		Chunk c2 = (Chunk) o2;

		// compare distances separate target vector (usually the origin)
		double d1 = distance(c1, targetChunk);
		double d2 = distance(c2, targetChunk);

		double comp = d1 - d2;
		 //System.out.println(d1 + "-" + d2 + " = " + (comp));

		if (comp > 0)
			return 1;
		else if (comp < 0)
			return -1;
		// Fall back to Chunk.compareTo if they are the same
		// distance from target (I don't expect this to happen
		// often...)
		else if (comp == 0)
			return c1.compareTo(c2);

		// this should never happen, but the glorious sun compiler requires it.
		return (0);
	}

	public boolean equals(Object obj)
	{
		return this == obj;
	}
}
