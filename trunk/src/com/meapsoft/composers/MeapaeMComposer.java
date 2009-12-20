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
 * Super simple demo Composer that just writes each chunk forward and then
 * backward
 * 
 * @author Douglas Repetto
 */
public class MeapaeMComposer extends Composer
{
	public static String oldDesc = "MeapaeMComposer makes palindromes by writing each chunk of audio forward and then backward.";

	String outFileName = "meapaem.edl";

	FeatFile featFile;

	boolean debug = false;

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public MeapaeMComposer()
	{
		initNameAndDescription("MeapeaM", new String(oldDesc));
	}
	
	public MeapaeMComposer(String featFN, String outFN)
	{
		this(new FeatFile(featFN), new EDLFile(outFN));
	}

	public MeapaeMComposer(FeatFile featFN, EDLFile outFN)
	{
		if (featFN == null || outFN == null)
			return;

		featFile = featFN;
		outFile = outFN;

		if (outFile == null)
			outFile = new EDLFile("");
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: MeapeaMComposer [-options] features.feat \n\n"
						+ "  where options include:\n"
						+ "    -o output_file  the file to write the output to (defaults to meapeam.edl)\n"
						+ "    -g              debug mode (prints out chunk features on each line of output file)");
		System.out.println();
		System.exit(0);
	}

	public MeapaeMComposer(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Vector features = new Vector();

		// Parse arguments
		String argString = "o:g";

		Getopt opt = new Getopt("MeapaeMComposer", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			switch (c)
			{
			case 'o':
				outFileName = opt.getOptarg();
				break;
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

	// super simple demo composer() method
	// put your code in here!
	public EDLFile compose()
	{
		Iterator c = featFile.chunks.iterator();
		double currTime = 0;

		// iterate through all the chunks that the segmenter found
		while (c.hasNext())
		{
			// your current features chunk
			FeatChunk ch = (FeatChunk) c.next();

			// make a new EDL chunk from the current features chunk
			EDLChunk original = new EDLChunk(ch, currTime);
			// we're going to make one more chunk for the backwards part
			EDLChunk backwards = new EDLChunk(ch, currTime + ch.length);

			// tell the 2nd chunk to add the reverse command so that
			// when the synthsizer sees this chunk it will render the audio
			// in reverse
			backwards.commands.add("reverse");

			// write both chunks out to the new EDL file
			outFile.chunks.add(original);
			outFile.chunks.add(backwards);

			// Increment currTime by twice the length of the chunk
			// since we've added two chunks.
			currTime += (ch.length * 2);

			progress.setValue(progress.getValue() + 1);
		}

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		return outFile;
	}

	public static void main(String[] args)
	{
		MeapaeMComposer m = new MeapaeMComposer(args);
		long startTime = System.currentTimeMillis();
		m.run();
		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
