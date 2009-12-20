/*
 *  Copyright 2006 Columbia University.
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

package com.meapsoft.composers;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;

/**
 * Program that composes an EDL by hopping around from chunk to chunk. Starts
 * from a the first chunk in the source file. Always picks the nearest neighbor
 * in feature space from current chunk. Then makes a drunken walk through the
 * sorted file to the specifications of the user.
 * 
 * @authors Sam Pluta (spluta@gmail.com) and Ron Weiss (ronw@ee.columbia.edu)
 */
public class ShoobyComposer extends SortComposer
{
	public static String oldDesc = "ShoobyComposer starts at the first chunk and proceeds through the sound file from each chunk to its nearest neighbor, according to the features in the input features file. It then makes a drunken walk through the sorted file in the style of scat singer Shooby Taylor.";

	int outFileLength = 500;

	int maxClumpWidth = 6;

	int shoobyDrunkenness = 100;

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public ShoobyComposer()
	{
		initNameAndDescription("Shooby Taylor", new String(oldDesc));
	}
	
	public ShoobyComposer(String featFN, String outFN, int outFileLength,
			int maxClumpWidth, int shoobyDrunkenness)
	{
		super(featFN, outFN);
		this.outFileLength = outFileLength;
		this.maxClumpWidth = maxClumpWidth;
		this.shoobyDrunkenness = shoobyDrunkenness;
	}

	public ShoobyComposer(FeatFile featFN, EDLFile outFN, int outFileLength,
			int maxClumpWidth, int shoobyDrunkenness)
	{
		super(featFN, outFN);
		this.outFileLength = outFileLength;
		this.maxClumpWidth = maxClumpWidth;
		this.shoobyDrunkenness = shoobyDrunkenness;
	}

	public ShoobyComposer(FeatFile featFN, EDLFile outFN)
	{
		super(featFN, outFN);
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: ShoobyComposer [-options] features.feat \n\n"
						+ "  where options include:\n"
						+ "    -o output_file  the file to write the output to (defaults to sorted.edl)\n"
						+ "    -g              debug mode (prints out chunk features on each line of output file)");
		printCommandLineOptions('i');
		printCommandLineOptions('d');
		printCommandLineOptions('c');
		System.out.println();
		System.exit(0);
	}

	/**
	 * ShoobyComposer constructor. Parses command line arguments
	 */
	public ShoobyComposer(String[] args)
	{
		super(args);
	}

	public EDLFile compose()
	{
		// initial chunk - pick it at random:
		// int randIdx = (int)Math.floor(featFile.chunks.size()*Math.random());
		// start with the first chunk
		FeatChunk currChunk = (FeatChunk) featFile.chunks.get(0);

		dist.setTarget(currChunk);

        Vector chunks = new Vector(featFile.chunks);

        // Why did this need to be a MaxHeap?
		//MaxHeap chunks2 = new MaxHeap(500);
        Vector chunks2 = new Vector(500);

		NumberFormat fmt = NumberFormat.getInstance();
		fmt.setMaximumFractionDigits(3);

		double currTime = 0;
		while (chunks.size() > 0)
		{
			dist.setTarget(currChunk);
            currChunk = (FeatChunk) Collections.min(chunks, dist);
            chunks.remove(currChunk);

			// turn currChunk into an EDL chunk
			EDLChunk nc = new EDLChunk(currChunk, currTime);

			chunks2.add(nc);

			currTime += currChunk.length;

			progress.setValue(progress.getValue() + 1);
		}

		double currTime2 = 0;
		Random rand = new Random();
		int pointer;
		int pointer2;
		int clumpWidth;
		int moveWidth;

		while (currTime2 <= outFileLength)
		{
			pointer = rand.nextInt((int) chunks2.size());
			clumpWidth = rand.nextInt(maxClumpWidth) + 1;
			moveWidth = rand.nextInt(13) + 13;
			int pointersSize = 2 + rand.nextInt(3);
			int pointers[] = new int[pointersSize];
			pointers[0] = pointer;
			for (int pi = 1; pi < pointersSize; pi++)
			{
				pointers[pi] = pointers[0] + rand.nextInt(moveWidth);
			}
			;
			for (int i = 0; i < rand.nextInt(maxClumpWidth) + 1; i++)
			{
				pointer = pointers[rand.nextInt(pointersSize)];
				for (int i2 = 0; i2 < rand.nextInt(maxClumpWidth) + 1; i2++)
				{
					pointer2 = pointers[rand.nextInt(pointersSize)]
							+ rand.nextInt(clumpWidth + 1);
					if (pointer2 > (chunks2.size() - 1))
					{
						pointer2 = chunks2.size() - 1;
					}
					EDLChunk currChunk2 = (EDLChunk) chunks2.get(pointer2);
					EDLChunk nc = new EDLChunk(currChunk2.srcFile,
							currChunk2.startTime, currChunk2.length, currTime2);
					outFile.chunks.add(nc);
					currTime2 += nc.length;
				}
			}
		}

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		return outFile;
	}

	public static void main(String[] args)
	{
		ShoobyComposer m = new ShoobyComposer(args);
		long startTime = System.currentTimeMillis();

		m.run();

		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
