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
import java.util.Iterator;

import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.ParserException;

/**
 * ThresholdComposer selects chunks with feature values falling inside the top
 * and bottom thresholds. It then creates an output file composed exclusively of
 * either the selected chunks or the not-selected chunks. Try using it on speech
 * to eliminate pauses (using ChunkPower), or on pitched sounds to extract
 * certain pitch ranges (using AvgPitchSimple). You will probably need to do a
 * feature analysis of your file first and then look at the features to get a
 * feel for the range of data values. ThresholdComposer only really makes sense
 * for one-dimensional features like pitch and power.
 * 
 * @author Douglas Repetto
 */

public class ThresholdComposer extends Composer
{
	public static String oldDesc = "ThresholdComposer selects chunks with feature values falling inside the top and bottom thresholds. It then creates an output file composed exclusively of either the selected chunks or the not-selected chunks. ThresholdComposer only really makes sense for one-dimensional features like pitch and power.";

	String outFileName = "threshold.edl";

	FeatFile featFile;

	boolean debug = false;

	double thresholdTop = 50.0;

	double thresholdBottom = 25.0;

	boolean insideThreshold = true;

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public ThresholdComposer()
	{
		initNameAndDescription("Threshold Composer", new String(oldDesc));
	}
	
	public ThresholdComposer(String featFN, String outFN, double thresholdTop,
			double thresholdBottom, boolean insideThreshold)
	{
		this(new FeatFile(featFN), new EDLFile(outFN), thresholdTop,
				thresholdBottom, insideThreshold);
	}

	public ThresholdComposer(FeatFile featFN, EDLFile outFN,
			double thresholdTop, double thresholdBottom, boolean insideThreshold)
	{
		if (featFN == null || outFN == null)
			return;

		featFile = featFN;
		outFile = outFN;
		this.thresholdTop = thresholdTop;
		this.thresholdBottom = thresholdBottom;
		this.insideThreshold = insideThreshold;

		if (outFile == null)
			outFile = new EDLFile("");
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: ThresholdComposer [-options] features.feat \n\n"
						+ "  where options include:\n"
						+ "    -o output_file  the file to write the output to (defaults to shuffle.edl)\n"
						+ "    -t threshold top   sets the top threshold\n"
						+ "	-b threshold bottom	 sets the bottom threshold\n"
						+ "	-e exclude chunks inside the thresholds; default is to include chunks\n"
						+ "    -g              debug mode (prints out chunk features on each line of output file)");
		System.out.println();
		System.exit(0);
	}

	public ThresholdComposer(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Vector features = new Vector();

		// Parse arguments
		String argString = "o:t:b:e:g";

		Getopt opt = new Getopt("ThresholdComposer", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			switch (c)
			{
			case 'o':
				outFileName = opt.getOptarg();
				break;
			case 't':
				thresholdTop = new Double(opt.getOptarg()).doubleValue();
			case 'b':
				thresholdBottom = new Double(opt.getOptarg()).doubleValue();
			case 'e':
				insideThreshold = false;
			case 'g':
				debug = true;
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

	public void setup() throws IOException, ParserException
	{
		super.setup();

		if (!featFile.haveReadFile)
			featFile.readFile();

		if (featFile.chunks.size() == 0)
			throw new ParserException(featFile.filename, "No chunks found");

		progress.setMaximum(featFile.chunks.size());
	}

	public EDLFile compose()
	{
		Iterator c = featFile.chunks.iterator();
		double currTime = 0;

		// iterate through all the chunks that the segmenter found
		while (c.hasNext())
		{
			// your current features chunk
			FeatChunk ch = (FeatChunk) c.next();

			double[] feats = ch.getFeatures();

			// System.out.println("feature[0]: " + feats[0]);
			// System.out.println("insideThreshold: " + insideThreshold + "
			// thresholdBottom: " + thresholdBottom +
			// " thresholdTop: " + thresholdTop);

			// sweetspot is area between thresholds
			boolean inSweetSpot = false;

			// see if we're in the sweet spot
			if (feats[0] > thresholdBottom && feats[0] < thresholdTop)
				inSweetSpot = true;

			// System.out.println("inSweetSpot: " + inSweetSpot);

			// if we want chunks that are in the sweetspot
			if (inSweetSpot && insideThreshold)
			{
				// make a new EDL chunk from the current features chunk
				EDLChunk newChunk = new EDLChunk(ch, currTime);
				outFile.chunks.add(newChunk);
				currTime += ch.length;
				// System.out.println("inSweetSpot && insideThreshold, so
				// writing chunk!");
			}
			// if we want chunks that are not in the sweetspot
			else if (!inSweetSpot && !insideThreshold)
			{
				// make a new EDL chunk from the current features chunk
				EDLChunk newChunk = new EDLChunk(ch, currTime);
				outFile.chunks.add(newChunk);
				currTime += ch.length;
				// System.out.println("!inSweetSpot && !insideThreshold, so
				// writing chunk!");
			}
			// else
			// System.out.println("not writing chunk!!!");

			progress.setValue(progress.getValue() + 1);
		}

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		return outFile;
	}

	public static void main(String[] args)
	{
		ThresholdComposer m = new ThresholdComposer(args);
		long startTime = System.currentTimeMillis();
		m.run();
		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
