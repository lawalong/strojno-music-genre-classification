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
import java.util.Random;
import java.util.Vector;

import com.meapsoft.BasicHist;
import com.meapsoft.Chunk;
import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.ParserException;

/**
 * Rocks it hard-core style. Finds the most common chunk length L and lengths
 * related by a factor of 2, i.e. L/2, L/4, L/8, L*2. These chunks are then
 * shuffled to create a new piece with a clear beat.
 * 
 * @author Victor Adan and Jeff Snyder
 */
public class HeadBangComposer extends Composer
{
	public static String oldDesc = "HeadBangComposer rocks it hard-core style. Finds the most common chunk length L and lengths related by a factor of 2, i.e. L/2, L/4, L/8, L*2. These chunks are then shuffled to create a new piece with a clear beat.\n";

	String outFileName = "headBang.edl";

	FeatFile featFile;

	int binsNum;

	int durRange;

	int newPieceLength;

	boolean debug = false;

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public HeadBangComposer()
	{
		initNameAndDescription("Head Banger", new String(oldDesc));
	}
	
	public HeadBangComposer(String featFN, String outFN)
	{
		this(featFN, outFN, 5000, 300);
	}

	public HeadBangComposer(FeatFile featFN, EDLFile outFN)
	{
		this(featFN, outFN, 5000, 300);
	}

	public HeadBangComposer(String featFN, String outFN, int binsNum,
			int newPieceLength)
	{
		this(new FeatFile(featFN), new EDLFile(outFN), binsNum, newPieceLength);
	}

	public HeadBangComposer(FeatFile featFN, EDLFile outFN, int binsNum,
			int newPieceLength)
	{
		featFile = featFN;
		outFile = outFN;
		this.binsNum = binsNum;
		// this.durRange = durRange;
		this.newPieceLength = newPieceLength;

		if (outFile == null)
			outFile = new EDLFile("");
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: HeadBangComposer [-options] features.feat \n\n"
						+ "  where options include:\n"
						+ "    -o output_file  the file to write the output to (defaults to sorted.edl)\n"
						+ "    -g              debug mode (prints out chunk features on each line of output file)\n"
						+ "    -b              number of bins for length histogram\n"
						+ "    -l              new piece length in number of chunks");
		printCommandLineOptions('i');
		// printCommandLineOptions('d');
		System.out.println();
		System.exit(0);
	}

	public HeadBangComposer(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Vector features = new Vector();

		// Parse arguments
		String argString = "o:gb:l:";

		Getopt opt = new Getopt("HeadBangComposer", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			switch (c)
			{
			case 'o':
				outFileName = opt.getOptarg();
				break;
			case 'l':
				// System.out.println( opt.getOptarg());
				newPieceLength = Integer.valueOf(opt.getOptarg()).intValue();
				break;
			case 'g':
				debug = true;
				break;
			case 'b':
				// System.out.println( opt.getOptarg());
				binsNum = Integer.valueOf(opt.getOptarg()).intValue();
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
		// make a histogram of chunk lengths
		int bins = binsNum;
		int range = 25856;
		BasicHist hist = new BasicHist(bins, 0, range);

		for (int i = 0; i < featFile.chunks.size(); i = i + 1)
		{
			// System.out.println(i);
			double len = Math.floor(((Chunk) featFile.chunks.get(i)).length
					* samplingRate);
			hist.add(Math.log(len) / Math.log(2));
			// System.out.println(i+" "+len+"
			// "+((FeatChunk)featFile.chunks.get(i)).getFeatures()[0]);
		}

		// find mode of histogram
		int currentValue = 0;
		int maxValue = 0;
		int maxValueBin = 0;
		for (int i = 0; i < bins; i++)
		{
			currentValue = hist.getValue(i);
			if (currentValue > maxValue)
			{
				maxValue = currentValue;
				maxValueBin = i;
			}

			// System.out.println("histo [" + i +"]" + hist.getValue(i));
		}
		// System.out.println(" maxValueBin =" + maxValueBin);

		// get length ranges to recover
		double lowBound = Math.pow(2,
				((double) range / (double) bins * maxValueBin));
		double highBound = Math.pow(2,
				((double) range / (double) bins * (maxValueBin + 1)));
		// System.out.println("low bound " + lowBound);
		// System.out.println("high bound " + highBound);

		double lowBoundDiv = lowBound / 2;
		double highBoundDiv = highBound / 2;
		double lowBoundDiv2 = lowBound / 4;
		double highBoundDiv2 = highBound / 4;
		double lowBoundMult = lowBound * 2;
		double highBoundMult = highBound * 2;

		Vector featChunks = new Vector(featFile.chunks);

		// find chunks that fall in divisions or multiples of mode.
		FeatChunk tempFeatChunk;
		double tempFeatLen;
		Vector modeChunks = new Vector();
		Vector multModeChunks = new Vector();
		Vector divModeChunks = new Vector();
		Vector div2ModeChunks = new Vector();

		for (int i = 0; i < featChunks.size(); i++)
		{
			tempFeatChunk = (FeatChunk) featChunks.get(i);
			tempFeatLen = Math.floor(tempFeatChunk.length * samplingRate);

			if (lowBoundDiv2 < tempFeatLen && tempFeatLen < highBoundDiv2)
			{
				div2ModeChunks.add(featChunks.get(i));
				// System.out.println(" found a div2 in chunk" + i);
			}
			else if (lowBoundDiv < tempFeatLen && tempFeatLen < highBoundDiv)
			{
				divModeChunks.add(featChunks.get(i));
				// System.out.println(" found a div in chunk" + i);
			}
			else if (lowBoundMult < tempFeatLen && tempFeatLen < highBoundMult)
			{
				multModeChunks.add(featChunks.get(i));
				// System.out.println(" found a mult in chunk" + i);
			}
			else if (lowBound < tempFeatLen && tempFeatLen < highBound)
			{
				modeChunks.add(featChunks.get(i));
				// System.out.println(" found a mode in chunk" + i);
			}
		}

		Vector outChunks = new Vector();

		Random rand = new Random();
		int index;
		for (int i = 0; i < newPieceLength; i++)
		{
			index = rand.nextInt(16);
			if (0 <= index && index < 4)
			{
				if (div2ModeChunks.size() > 0)
				{
					outChunks.add(div2ModeChunks.get(rand
							.nextInt(div2ModeChunks.size())));
					outChunks.add(div2ModeChunks.get(rand
							.nextInt(div2ModeChunks.size())));
					outChunks.add(div2ModeChunks.get(rand
							.nextInt(div2ModeChunks.size())));
					outChunks.add(div2ModeChunks.get(rand
							.nextInt(div2ModeChunks.size())));
					outChunks.add(div2ModeChunks.get(rand
							.nextInt(div2ModeChunks.size())));
					outChunks.add(div2ModeChunks.get(rand
							.nextInt(div2ModeChunks.size())));
					outChunks.add(div2ModeChunks.get(rand
							.nextInt(div2ModeChunks.size())));
					outChunks.add(div2ModeChunks.get(rand
							.nextInt(div2ModeChunks.size())));
					// System.out.println("Wrote 8 mode / 4 chunks");

				}
			}
			else if (4 <= index && index < 6)
			{
				if (divModeChunks.size() > 0)
				{
					outChunks.add(divModeChunks.get(rand.nextInt(divModeChunks
							.size())));
					outChunks.add(divModeChunks.get(rand.nextInt(divModeChunks
							.size())));
					outChunks.add(divModeChunks.get(rand.nextInt(divModeChunks
							.size())));
					outChunks.add(divModeChunks.get(rand.nextInt(divModeChunks
							.size())));
					// System.out.println("Wrote 4 mode / 2 chunks");
				}
			}
			else if (6 <= index && index < 14)
			{
				if (modeChunks.size() > 0)
				{
					outChunks.add(modeChunks.get(rand
							.nextInt(modeChunks.size())));
					outChunks.add(modeChunks.get(rand
							.nextInt(modeChunks.size())));
					// System.out.println("Wrote 2 mode chunks");
				}
			}
			else if (14 <= index)
			{
				if (multModeChunks.size() > 0)
				{
					outChunks.add(multModeChunks.get(rand
							.nextInt(multModeChunks.size())));
					// System.out.println("Wrote 1 mode * 2 chunk");
				}
			}

			progress.setValue(progress.getValue() + 1);
		}

		double currTime = 0;
		while (outChunks.size() > 0)
		{
			FeatChunk match = (FeatChunk) outChunks.remove(0);

			// turn match chunk into an EDL chunk
			EDLChunk nc = new EDLChunk(match, currTime);

			if (debug)
			{
				NumberFormat fmt = NumberFormat.getInstance();
				fmt.setMaximumFractionDigits(3);
				nc.comment = "    # feats = ";
				double[] feat = match.getFeatures();
				for (int x = 0; x < feat.length - 1; x++)
					nc.comment += fmt.format(feat[x]) + ", ";
				nc.comment += fmt.format(feat[feat.length - 1]);
			}

			outFile.chunks.add(nc);

			currTime += match.length;
		}

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		return outFile;
	}

	public static void main(String[] args)
	{
		HeadBangComposer m = new HeadBangComposer(args);
		long startTime = System.currentTimeMillis();
		m.run();
		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
