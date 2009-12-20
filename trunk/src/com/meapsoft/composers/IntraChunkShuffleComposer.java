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
 * 
 * @author Douglas Repetto
 */

public class IntraChunkShuffleComposer extends Composer
{
	public static String oldDesc = "IntraChunkShuffleComposer chops each chunk up into small pieces and rearranges them. This keeps the meta chunks intact but scrambles them on a local level.";

	String outFileName = "shuffle.edl";

	FeatFile featFile;

	boolean debug = false;

	int numSubChunks = 4;

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public IntraChunkShuffleComposer()
	{
		initNameAndDescription("IntraChunkShuffle", new String(oldDesc));
	}
	
	public IntraChunkShuffleComposer(String featFN, String outFN, int chunks)
	{
		this(new FeatFile(featFN), new EDLFile(outFN), chunks);
	}

	public IntraChunkShuffleComposer(FeatFile featFN, EDLFile outFN, int chunks)
	{
		if (featFN == null || outFN == null)
			return;

		featFile = featFN;
		outFile = outFN;
		numSubChunks = chunks;

		if (numSubChunks <= 1)
			numSubChunks = 2;

		if (outFile == null)
			outFile = new EDLFile("");
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: IntraChunkShuffleComposer [-options] features.feat \n\n"
						+ "  where options include:\n"
						+ "    -o output_file  the file to write the output to (defaults to shuffle.edl)\n"
						+ "    -n num_chunks   the number of subchunks to divide each chunk into\n"
						+ "    -g              debug mode (prints out chunk features on each line of output file)");
		System.out.println();
		System.exit(0);
	}

	public IntraChunkShuffleComposer(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Vector features = new Vector();

		// Parse arguments
		String argString = "o:n:g";

		Getopt opt = new Getopt("IntraChunkShuffleComposer", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			switch (c)
			{
			case 'o':
				outFileName = opt.getOptarg();
				break;
			case 'n':
				numSubChunks = new Integer(opt.getOptarg()).intValue();
				if (numSubChunks <= 1)
					numSubChunks = 2;
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
			double length = ch.length / numSubChunks;
			double localStartTime = ch.startTime;

			for (int i = numSubChunks - 1; i >= 0; i--)
			{
				// make a new EDL chunk from the current features chunk
				// EDLChunk original = new EDLChunk(ch, currTime);

				EDLChunk chunk = new EDLChunk(ch.srcFile, localStartTime
						+ (i * length), length, currTime);

				// EDLChunk blip = new EDLChunk("data" +
				// System.getProperty("file.separator") + "blip.wav",
				// 0, 0.1, currTime);
				// write both chunks out to the new EDL file
				outFile.chunks.add(chunk);

				// Increment currTime by twice the length of the chunk
				// since we've added two chunks.
				currTime += length;
			}

			progress.setValue(progress.getValue() + 1);
		}

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		return outFile;
	}

	public static void main(String[] args)
	{
		IntraChunkShuffleComposer m = new IntraChunkShuffleComposer(args);
		long startTime = System.currentTimeMillis();
		m.run();
		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
