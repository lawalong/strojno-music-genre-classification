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
 * A simple histogram class to count the frequency of values of a parameter of
 * interest.
 */
public class BasicHist
{
	int[] bins;

	int numBins;

	int underflows;

	int overflows;

	double lo;

	double hi;

	double range;

	/**
	 * The constructor will create an array of a given number of bins. The range
	 * of the histogram given by the upper and lower limt values.
	 */
	public BasicHist(int numBins, double lo, double hi)
	{
		this.numBins = numBins;

		bins = new int[numBins];

		this.lo = lo;
		this.hi = hi;
		range = hi - lo;
	}

	/**
	 * Add an entry to a bin. Include if value is in the range lo <= x < hi
	 */
	public void add(double x)
	{
		if (x >= hi)
			overflows++;
		else if (x < lo)
			underflows++;
		else
		{
			double val = x - lo;

			// Casting to int will round off to lower
			// integer value.
			int bin = (int) (numBins * (val / range));

			// Increment the corresponding bin.
			bins[bin]++;
		}
	}

	/** Clear the histogram bins. * */
	public void clear()
	{
		for (int i = 0; i < numBins; i++)
		{
			bins[i] = 0;
			overflows = 0;
			underflows = 0;
		}
	}

	/** Provide access to the bin values. * */
	public int getValue(int bin)
	{
		if (bin < 0)
			return underflows;
		else if (bin >= numBins)
			return overflows;
		else
			return bins[bin];
	}
}
