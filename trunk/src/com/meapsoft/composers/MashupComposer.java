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
import java.util.Iterator;

import com.meapsoft.ChunkDist;
import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.EuclideanDist;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.ParserException;

/**
 * Program that composes a mashup by replacing each segment of the input file
 * with the chunk in a given chunk database whos features most closely match
 * those of the input chunk.
 * 
 * Produces an EDL from two feature files. Basically an offline version of Mike
 * Mandel's mashup generator.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class MashupComposer extends Composer
{
	public static String oldDesc = "MashupComposer attempts to match chunks in the input features file using chunks from the chunk database features file. The result is the source sound file created from chunks in the chunk database.";

	String outFileName = "mashup.edl";

	FeatFile dstFile;

	FeatFile DBFile;

	int[] featdim = null;

	ChunkDist dist;

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public MashupComposer()
	{
		initNameAndDescription("Mashup!", new String(oldDesc));
	}
	
	public MashupComposer(String featFN, String DBFN, String outFN)
	{
		this(featFN, DBFN, outFN, new EuclideanDist());
	}

	public MashupComposer(String featFN, String DBFN, String outFN, ChunkDist cd)
	{
		this(new FeatFile(featFN), new FeatFile(DBFN), new EDLFile(outFN), cd);
	}

	public MashupComposer(FeatFile featFN, FeatFile DBFN, EDLFile outFN)
	{
		this(featFN, DBFN, outFN, new EuclideanDist());
	}

	public MashupComposer(FeatFile featFN, FeatFile DBFN, EDLFile outFN,
			ChunkDist cd)
	{
		dstFile = featFN;
		DBFile = DBFN;
		outFile = outFN;
		dist = cd;
		featdim = cd.featdim;

		if (outFile == null)
			outFile = new EDLFile("");
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: MashupComposer [-options] dest.feat chunkdb.feat \n\n"
						+ "  where options include:\n"
						+ "    -o output_file  the file to write the output to (defaults to mashup.edl)");
		printCommandLineOptions('i');
		printCommandLineOptions('d');
		printCommandLineOptions('c');
		System.out.println();
		System.exit(0);
	}

	/**
	 * MashupComposer constructor. Parses command line arguments
	 */
	public MashupComposer(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Parse arguments
		String argString = "d:i:o:c:";
		featdim = parseFeatDim(args, argString);
		dist = parseChunkDist(args, argString, featdim);
		parseCommands(args, argString);

		Getopt opt = new Getopt("MashupComposer", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			switch (c)
			{
			case 'o':
				outFileName = opt.getOptarg();
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

		dstFile = new FeatFile(args[args.length - 2]);
		DBFile = new FeatFile(args[args.length - 1]);
		outFile = new EDLFile(outFileName);

		System.out.println("Composing " + outFileName + " from "
				+ args[args.length - 2] + " (chunk db: "
				+ args[args.length - 1] + ").");
	}

	public void setup() throws IOException, ParserException
	{
		super.setup();

		if (!dstFile.haveReadFile)
			dstFile.readFile();
		if (!DBFile.haveReadFile)
			DBFile.readFile();

		if (dstFile.chunks.size() == 0)
			throw new ParserException(dstFile.filename, "No chunks found");
		if (DBFile.chunks.size() == 0)
			throw new ParserException(DBFile.filename, "No chunks found");

		// What if features don't match
		if (!dstFile.isCompatibleWith(DBFile))
			throw new ParserException(DBFile.filename,
					"Features do not match those in " + dstFile.filename);

		dstFile = (FeatFile) dstFile.clone();
		dstFile.normalizeFeatures();
		dstFile.applyFeatureWeights();

		DBFile = (FeatFile) DBFile.clone();
		DBFile.normalizeFeatures();
		DBFile.applyFeatureWeights();

		progress.setMaximum(dstFile.chunks.size());
	}

	public EDLFile compose()
	{
		NumberFormat fmt = NumberFormat.getInstance();
		fmt.setMaximumFractionDigits(3);

		Iterator dstchunks = dstFile.chunks.iterator();
		while (dstchunks.hasNext())
		{
			FeatChunk currChunk = (FeatChunk) dstchunks.next();
			double mindist = Double.MAX_VALUE;
			FeatChunk match = null;

			// find closest match to currChunk in DB
			Iterator i = DBFile.chunks.iterator();
			while (i.hasNext())
			{
				FeatChunk c = (FeatChunk) i.next();
				double d = dist.distance(currChunk, c);

				if (d < mindist)
				{
					mindist = d;
					match = c;
				}
			}

			// turn match chunk into an EDL chunk
			EDLChunk nc = new EDLChunk(match, currChunk.startTime);
			nc.comment = "    # dist = " + fmt.format(mindist);
			outFile.chunks.add(nc);

			progress.setValue(progress.getValue() + 1);
		}

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		return outFile;
	}

	public static void main(String[] args)
	{
		MashupComposer m = new MashupComposer(args);
		long startTime = System.currentTimeMillis();
		m.run();
		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
