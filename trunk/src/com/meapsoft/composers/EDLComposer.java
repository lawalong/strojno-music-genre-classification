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

import com.meapsoft.EDLFile;
import com.meapsoft.ParserException;

/**
 * Simply take an existing EDL file and apply the various composer options
 * (reverse, gain, etc.) to it. Mostly created for output from visualizers.
 * 
 * @author Douglas Repetto
 */

public class EDLComposer extends Composer
{
	public static String oldDesc = "EDLComposer applies composer options (gain, crossfade, etc.) to an existing .edl file. It is meant to be used to generate output from the visualizers.";

	String outFileName = "visualized.edl";

	EDLFile inEDLFile;

	// int[] featdim = null;
	// ChunkDist dist;
	boolean reverseSort = false;

	boolean debug = false;

	boolean normalizeFeatures = true;

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public EDLComposer()
	{
		initNameAndDescription("EDL Composer", new String(oldDesc));
	}
	
	public EDLComposer(String inEDLFN, String outEDLFN)
	{
		this(new EDLFile(inEDLFN), new EDLFile(outEDLFN));
	}

	public EDLComposer(EDLFile inEDL, EDLFile outEDL)
	{
		this.inEDLFile = inEDL;
		outFile = outEDL;

		if (outFile == null)
			outFile = new EDLFile(outFileName);

		// make sure we write out the edl file
		writeMEAPFile = true;
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: EDLComposer [-options] EDLFile.edl \n\n"
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
	public EDLComposer(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Vector features = new Vector();

		// Parse arguments
		String argString = "d:i:o:c:r:g";
		// featdim = parseFeatDim(args, argString);
		// dist = parseChunkDist(args, argString, featdim);
		parseCommands(args, argString);

		Getopt opt = new Getopt("EDLComposer", args, argString);
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

		inEDLFile = new EDLFile(args[args.length - 1]);
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

		if (!inEDLFile.haveReadFile)
			inEDLFile.readFile();

		if (inEDLFile.chunks.size() == 0)
			throw new ParserException(inEDLFile.filename, "No chunks found");

		if (normalizeFeatures)
		{
			// inEDLFile = (EDLFile) inEDLFile.clone();
			inEDLFile.normalizeFeatures();
			inEDLFile.applyFeatureWeights();
		}

		progress.setMaximum(inEDLFile.chunks.size());
	}

	// super complicated compose() method!!!
	public EDLFile compose()
	{
		outFile.chunks = inEDLFile.chunks;

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		return outFile;
	}

	public static void main(String[] args)
	{
		EDLComposer m = new EDLComposer(args);
		long startTime = System.currentTimeMillis();
		m.run();
		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
