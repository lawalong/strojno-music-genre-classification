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

/*
 * Created on Nov 17, 2006
 *
 */
package com.meapsoft.visualizer;

import com.meapsoft.EDLChunk;

/**
 * @author douglas
 * 
 * This class lets us combine feat + EDL info in one place
 * 
 */

public class ChunkVisInfo extends EDLChunk
{
	int xFeat = 0;

	int xEDL = 0;

	int yFeat = 0;

	int yEDL = 0;

	int width = 0;

	int height = 0;

	// double[] features;
	public boolean selected = true;

	public ChunkVisInfo(String sf, double st, double l, double dt)
	{
		super(sf, st, l, dt);
	}
}
