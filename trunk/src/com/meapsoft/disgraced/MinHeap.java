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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import com.meapsoft.FeatChunk;

/**
 * Implementation of a binary min heap
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class MinHeap extends Heap
{
	/**
	 * Creates an empty MinHeap.
	 */
	public MinHeap()
	{
		super();
	}

	/**
	 * Use given Comparator for all comparisons between elements in this
	 * MinHeap. Otherwise rely on compareTo methods and Comparable Objects.
	 */
	public MinHeap(Comparator c)
	{
		super(c);
	}

	/**
	 * Creates an empty MinHeap with the given capacity.
	 */
	public MinHeap(int capacity)
	{
		super(capacity);
	}

	/**
	 * Create a new MinHeap containing the elements of the given Collection.
	 */
	public MinHeap(Collection c)
	{
		super(c);
	}

	/**
	 * Delete the smallest element of this MinHeap.
	 */
	public Object deleteMin()
	{
		return remove(0);
	}

	/**
	 * Perform heap sort on the data stored in this heap. After calling sort, a
	 * call to this objects iterator() method will iterate through the data
	 * stored in the heap in sorted order. This is not a stable sort.
	 */
	public void sort()
	{
		super.sort();

		// this just so happens to maintain the min-heap property
		isHeap = true;
	}

	// Simple test program
	public static void main(String args[])
	{
		Vector v = new Vector();
		MinHeap h = new MinHeap();

		// int[] numbers = {10, 1, 6, -10, 2, 7, 6};
		// int[] numbers = {10, 1, 6, -10, 2, 7, 4, 4, 4};
		int[] n = { 10, 1, 6, -10, 2, 7, 4, 4, 4, -2, 4, 4 };
		int[][] numbers = new int[n.length][2];

		int n4 = 0;
		for (int x = 0; x < n.length; x++)
		{
			numbers[x][0] = n[x];
			numbers[x][1] = n[x];

			if (n[x] == 4)
				numbers[x][1] = 40 + n4++;
		}

		try
		{
			System.out.print("Adding ");
			// Double d = null;
			FeatChunk d = null;
			for (int x = 0; x < numbers.length; x++)
			{
				System.out.print(numbers[x][0] + " (priority = "
						+ numbers[x][1] + "), ");
				// d = new Double(numbers[x]);

				d = new FeatChunk("", numbers[x][0], numbers[x][1], null);
				h.add(d);
				v.add(d);
			}

			System.out.println("\nCalling deleteMin: ");
			for (int x = 0; x < numbers.length; x++)
				System.out.print(h.deleteMin() + " ");

			System.out
					.println("\nRemoving an element (this should obey the heap property): ");
			h.addAll(v);
			h.remove(d);
			Iterator i = h.iterator();
			while (i.hasNext())
				System.out.print(i.next() + " ");

			System.out.println("\nRunning heapSort: ");
			// h.addAll(v);
			h.sort();
			i = h.iterator();
			while (i.hasNext())
				System.out.print(i.next() + " ");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
