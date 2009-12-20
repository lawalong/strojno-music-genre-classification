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
 * Program that adds a blip after each chunk
 * 
 * @author Dan Ellis (dpwe@ee.columbia.edu)
 */
public class BlipComposer extends Composer
{
	public static String oldDesc = "BlipComposer inserts a blip at the beginning of each chunk in the input features file. Especially useful for understanding the output of the segmenter.";

	String outFileName = "blipped.edl";

	FeatFile featFile;

	boolean debug = false;
	
	boolean silentMode = false;

	// path to blip wav file
	String blipWav = "data" + System.getProperty("file.separator") + "blip.wav";

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public BlipComposer()
	{
		initNameAndDescription("Add Blips", new String(oldDesc));
	}
	
	public BlipComposer(String featFN, String outFN)
	{
		this(new FeatFile(featFN), new EDLFile(outFN), false);
	}

	public BlipComposer(String featFN, String outFN, boolean silentMode)
	{
		this(new FeatFile(featFN), new EDLFile(outFN), silentMode);
	}
	
	public BlipComposer(FeatFile featFN, EDLFile outFN)
	{
		this(featFN, outFN, false);
	}
	
	public BlipComposer(FeatFile featFN, EDLFile outFN, boolean silentMode)
	{
		if (featFN == null || outFN == null)
			return;

		featFile = featFN;
		outFile = outFN;
		
		this.silentMode = silentMode;

		if (outFile == null)
			outFile = new EDLFile("");
	}

	
	public void printUsageAndExit()
	{
		System.out
				.println("Usage: BlipComposer [-options] features.feat \n\n"
						+ "  where options include:\n"
						+ " -s  silent mode -- only output blips, not original sound\n"
						+ "    -o output_file  the file to write the output to (defaults to blipped.edl)\n"
						+ "    -f blip_file    the audio file to insert at the beginning of each chunk (defaults to data/blip.wav)");
		printCommandLineOptions('c');
		System.out.println();
		System.exit(0);
	}

	public BlipComposer(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Vector features = new Vector();

		// Parse arguments
		String argString = "o:c:f:g";
		parseCommands(args, argString);

		Getopt opt = new Getopt("BlipComposer", args, argString);
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
			case 'f':
				blipWav = opt.getOptarg();
				break;
			case 'c': // already handled above
				break;
			case 's': 
				silentMode = true;
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

		// System.out.println("blip chunks in: " + featFile.chunks.size());
	}

	public EDLFile compose()
	{
		Iterator c = featFile.chunks.iterator();
		double currTime = 0;

		while (c.hasNext())
		{
			FeatChunk ch = (FeatChunk) c.next();

			EDLChunk nc = new EDLChunk(ch, currTime);
			// hard-coded parameters of blip
			EDLChunk blip = new EDLChunk(blipWav, 0, 0.1, currTime);

			outFile.chunks.add(blip);
			
			//if we're in silent mode we don't write sound, just blips
			if (!silentMode)
				outFile.chunks.add(nc);

			currTime += ch.length;

			progress.setValue(progress.getValue() + 1);
		}
		progress.setValue(progress.getValue() + 1);

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		// System.out.println("blip chunks out: " + outFile.chunks.size());
		return outFile;
	}

	public String description()
	{
		return description;
	}

	public void setBlipWav(String bp)
	{
		blipWav = bp;
	}

	public static void main(String[] args)
	{
		BlipComposer m = new BlipComposer(args);
		long startTime = System.currentTimeMillis();
		m.run();
		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
