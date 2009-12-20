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

package com.meapsoft.composers;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Vector;

import com.meapsoft.ChunkDist;
import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;

/**
 * Program that composes an EDL by hopping around from chunk to chunk. Starts
 * from a the first chunk in the source file. Always picks the nearest neighbor
 * in feature space from current chunk. Should replicate Dan Ellis' ordercols.m.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class NNComposer extends SortComposer
{
	public static String oldDesc = "NNComposer starts at the first chunk and proceeds through the sound file from each chunk to its nearest neighbor, according to the features in the input features file.";

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public NNComposer()
	{
		initNameAndDescription("Nearest Neighbor", new String(oldDesc));
	}
	
	public NNComposer(String featFN, String outFN)
	{
		super(featFN, outFN);
	}

	public NNComposer(FeatFile featFN, EDLFile outFN)
	{
		super(featFN, outFN);
	}

	public NNComposer(String featFN, String outFN, ChunkDist cd)
	{
		super(featFN, outFN, cd);
	}

	public NNComposer(FeatFile featFN, EDLFile outFN, ChunkDist cd)
	{
		super(featFN, outFN, cd);
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: NNComposer [-options] features.feat \n\n"
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
	 * NNComposer constructor. Parses command line arguments
	 */
	public NNComposer(String[] args)
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

			if (debug)
			{
				nc.comment = "    # feats = ";
				double[] feat = currChunk.getFeatures(featdim);
				for (int x = 0; x < feat.length - 1; x++)
					nc.comment += fmt.format(feat[x]) + ", ";
				nc.comment += fmt.format(feat[feat.length - 1]);
			}

			outFile.chunks.add(nc);

			currTime += currChunk.length;

			progress.setValue(progress.getValue() + 1);
		}

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		return outFile;
	}

	public static void main(String[] args)
	{
		NNComposer m = new NNComposer(args);
		long startTime = System.currentTimeMillis();

		m.run();

		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
