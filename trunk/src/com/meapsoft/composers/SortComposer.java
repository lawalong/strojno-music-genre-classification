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

import gnu.getopt.Getopt;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.TreeSet;

import com.meapsoft.ChunkDist;
import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.EuclideanDist;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.ParserException;

/**
 * Program that composes an EDL by sorting chunks in featFile based on some
 * subset of their features using some distance metric. Sorting is done based on
 * distance from the origin in feature space.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class SortComposer extends Composer
{
	public static String oldDesc = "SortComposer sorts the features in ascending or descending order. If there are multiple features, or more than one value per feature, it sorts according to distance in" + " Euclidean space.";

	String outFileName = "sorted.edl";

	FeatFile featFile;

	int[] featdim = null;

	ChunkDist dist;

	boolean reverseSort = false;

	boolean debug = false;

	boolean normalizeFeatures = true;

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public SortComposer()
	{
		initNameAndDescription("Simple Sort", new String(oldDesc));
	}
	
	public SortComposer(String featFN, String outFN)
	{
		this(featFN, outFN, new EuclideanDist());
	}

	public SortComposer(FeatFile featFN, EDLFile outFN)
	{
		this(featFN, outFN, new EuclideanDist());
	}

	public SortComposer(String featFN, String outFN, ChunkDist cd)
	{
		this(new FeatFile(featFN), new EDLFile(outFN), cd);
	}

	public SortComposer(FeatFile featFN, EDLFile outFN, ChunkDist cd)
	{
		featFile = featFN;
		outFile = outFN;
		dist = cd;
		featdim = cd.featdim;

		if (outFile == null)
			outFile = new EDLFile("");
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: SortComposer [-options] features.feat \n\n"
						+ "  where options include:\n"
						+ "    -o output_file  the file to write the output to (defaults to sorted.edl)\n"
						+ "    -g              debug mode (prints out chunk features on each line of output file)\n"
						+ "    -r              sort in reverse order");
		printCommandLineOptions('i');
		printCommandLineOptions('d');
		printCommandLineOptions('c');
		System.out.println();
		System.exit(0);
	}

	/**
	 * SortComposer constructor. Parses command line arguments
	 */
	public SortComposer(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Vector features = new Vector();

		// Parse arguments
		String argString = "d:i:o:c:r:g";
		featdim = parseFeatDim(args, argString);
		dist = parseChunkDist(args, argString, featdim);
		parseCommands(args, argString);

		Getopt opt = new Getopt("SortComposer", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			switch (c)
			{
			case 'o':
				outFileName = opt.getOptarg();
				break;
			case 'r':
				reverseSort = true;
				break;
			case 'g':
				debug = true;
				break;
			case 'd': // already handled above
				break;
			case 'i': // already handled above
				break;
			case 'c': // already handled above
				break;
			case '?':
				printUsageAndExit();
				break;
			default:
				System.out.print("getopt() returned " + c + "\n");
			}
		}

		// parse arguments
		int ind = opt.getOptind();
		if (ind > args.length)
			printUsageAndExit();

		featFile = new FeatFile(args[args.length - 1]);
		outFile = new EDLFile(outFileName);

		System.out.println("Composing " + outFileName + " from "
				+ args[args.length - 1] + ".");
	}

	public void setReverseSort(boolean b)
	{
		reverseSort = b;
	}

	public void setNormalizeFeatures(boolean b)
	{
		normalizeFeatures = b;
	}

	public void setup() throws IOException, ParserException
	{
		super.setup();

		if (!featFile.haveReadFile)
			featFile.readFile();

		if (featFile.chunks.size() == 0)
			throw new ParserException(featFile.filename, "No chunks found");

        if (normalizeFeatures)
		{
			featFile = (FeatFile) featFile.clone();
            featFile.normalizeFeatures();
			featFile.applyFeatureWeights();
		}

        progress.setMaximum(featFile.chunks.size());
	}

	public EDLFile compose()
	{
		// how many feature dimensions are we using?
		int maxdim = 0;
		if (featdim != null)
		{
			for (int x = 0; x < featdim.length; x++)
			{
				if (featdim[x] > maxdim)
					maxdim = featdim[x];
			}
		}
		else
			maxdim = ((FeatChunk) featFile.chunks.get(0)).numFeatures();

		FeatChunk targetChunk = new FeatChunk("", 0, 0, null);
		for (int i = 0; i <= maxdim; i++)
			// targetChunk.addFeature(0);
			targetChunk.addFeature(Integer.MIN_VALUE);

		dist.setTarget(targetChunk);

		// maintain a set of chunks sorted using dist from targetChunk
		TreeSet chunks = null;
		if (reverseSort)
            chunks = new TreeSet(Collections.reverseOrder(dist));
        else
            chunks = new TreeSet(dist);
		chunks.addAll(featFile.chunks);

        //System.out.println(chunks);

		NumberFormat fmt = NumberFormat.getInstance();
		fmt.setMaximumFractionDigits(3);

		double currTime = 0;
		while (chunks.size() > 0)
		{
			FeatChunk match = (FeatChunk) chunks.first();
            chunks.remove(match);

			// turn match chunk into an EDL chunk
			EDLChunk nc = new EDLChunk(match, currTime);

            if (debug)
			{
				nc.comment = "    # feats = ";
				double[] feat = match.getFeatures(featdim);
				for (int x = 0; x < feat.length - 1; x++)
					nc.comment += fmt.format(feat[x]) + ", ";
				nc.comment += fmt.format(feat[feat.length - 1]);
			}

            outFile.chunks.add(nc);

			currTime += match.length;

			progress.setValue(progress.getValue() + 1);
		}

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		return outFile;
	}

	public static void main(String[] args)
	{
		SortComposer m = new SortComposer(args);
		long startTime = System.currentTimeMillis();
		m.run();
		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
