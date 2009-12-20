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

import java.util.Iterator;
import java.util.Vector;

/**
 * Representation of an EDLFile Chunk
 * 
 * Note that EDLChunk extends FeatChunk so that it can inherit the feature stuff
 * from FeatChunk. This is really only to associate each EDL chunk with a set of
 * features for visualization purposes (i.e. with ImagePanel, to see how the
 * composers rearrange things) since the features associated with chunks in EDLs
 * are basically irrelevant.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class EDLChunk extends FeatChunk
{
	// Time (in seconds) to synthesize this chunk.
	// (as opposed to startTime = the start time of the chunk in the
	// source file)
	public double dstTime;

	// List of commands that Synthesizer should apply to this chunk.
	public Vector commands;

	/**
	 * EDLFile Chunk constructor
	 */
	public EDLChunk(String sf, double st, double l, double dt)
	{
		super(sf, st, l, null);
		dstTime = dt;

		commands = new Vector();
	}

	public EDLChunk(Chunk c, double dt)
	{
		this(c.srcFile, c.startTime, c.length, dt);
	}

	public EDLChunk(FeatChunk c, double dt)
	{
		this(c.srcFile, c.startTime, c.length, dt);
		features = c.features;
	}

	/**
	 * Compare one EDLChunk to another - comparisons are performed based on
	 * dstTime field
	 */
	public int compareTo(Object o) throws ClassCastException
	{
		try
		{
			EDLChunk c = (EDLChunk) o;
			return Double.compare(dstTime, c.dstTime);
		}
		catch (ClassCastException e)
		{
			return super.compareTo(o);
		}
	}

	/**
	 * Write a description of this Chunk as a String in the format expected in
	 * an EDLFile
	 */
	public String toString()
	{
		StringBuffer s = new StringBuffer(200);
		s.append(dstTime).append("\t").append(srcFile.replaceAll(" ", "%20"))
				.append("\t");
		s.append(startTime).append("\t").append(length).append("\t");

		if (commands != null)
		{
			Iterator x = commands.iterator();
			while (x.hasNext())
				s.append(x.next()).append("\t");
		}

		s.append(comment).append("\n");

		return s.toString();
	}
}
