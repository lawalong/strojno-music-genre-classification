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

// I can't believe that the huge Java API doesn't already include such
// a basic data structure

/**
 * Implementation of a binary max heap.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class MaxHeap extends Heap
{
	/**
	 * Creates an empty MaxHeap.
	 */
	public MaxHeap()
	{
		super();
	}

	/**
	 * Use given Comparator for all comparisons between elements in this
	 * MaxHeap. Otherwise rely on compareTo methods and Comparable Objects.
	 */
	public MaxHeap(Comparator c)
	{
		super(c);
	}

	/**
	 * Creates an empty MaxHeap with the given capacity.
	 */
	public MaxHeap(int capacity)
	{
		super(capacity);
	}

	/**
	 * Create a new MaxHeap containing the elements of the given Collection.
	 */
	public MaxHeap(Collection c)
	{
		super(c);
	}

	/**
	 * Delete the largest element of this MaxHeap.
	 */
	public Object deleteMax()
	{
		return remove(0);
	}

	/**
	 * Compare two Objects in this heap - wrapper around
	 * compareTo/Comparator.compare
	 */
	protected int cmp(int node1, int node2)
	{
		return -super.cmp(node1, node2);
	}

	// Simple test program
	public static void main(String args[])
	{
		MaxHeap h = new MaxHeap();
		Vector v = new Vector();

		// int[] numbers = {10, 1, 6, -10, 2, 7, 6};
		int[] numbers = { 10, 1, 6, -10, 2, 7, 4 };

		try
		{
			System.out.print("Adding ");
			Double d = null;
			for (int x = 0; x < numbers.length; x++)
			{
				System.out.print(numbers[x] + " ");
				d = new Double(numbers[x]);
				h.add(d);
				v.add(d);
			}

			System.out.print("\nCalling deleteMax: ");
			for (int x = 0; x < numbers.length; x++)
				System.out.print(h.deleteMax() + " ");

			System.out
					.print("\nRemoving an element (this should obey the heap property): ");
			h.addAll(v);
			h.remove(d);
			Iterator i = h.iterator();
			while (i.hasNext())
				System.out.print(i.next() + " ");

			System.out.print("\nRunning heapSort: ");
			// h.addAll(v);
			h.sort();
			i = h.iterator();
			while (i.hasNext())
				System.out.print(i.next() + " ");
			System.out.print("\n");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
