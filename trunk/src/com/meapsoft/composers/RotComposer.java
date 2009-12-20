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

public class RotComposer extends Composer
{
	public static String oldDesc = "RotComposer rotates the beats in each measure by a selectable number of positions. You can set the number of beats/measure, the number of positions to rotate, and the direction of rotation.";

	String outFileName = "rot.edl";

	FeatFile featFile;

	boolean debug = false;

	int beatsPerMeasure = 4;

	int numPositions = 1;

	boolean rotateRight = true;

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public RotComposer()
	{
		initNameAndDescription("Rotation Composer", new String(oldDesc));
	}
	
	public RotComposer(String featFN, String outFN, int beatsPerMeasure,
			int numPositions, boolean left)
	{
		this(new FeatFile(featFN), new EDLFile(outFN), beatsPerMeasure,
				numPositions, left);
	}

	public RotComposer(FeatFile featFN, EDLFile outFN, int beatsPerMeasure,
			int numPositions, boolean left)
	{
		if (featFN == null || outFN == null)
			return;

		featFile = featFN;
		outFile = outFN;

		if (outFile == null)
			outFile = new EDLFile("");

		this.beatsPerMeasure = beatsPerMeasure;
		this.numPositions = numPositions;
		rotateRight = !left;
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: RotComposer [-options] features.feat \n\n"
						+ "  where options include:\n"
						+ "	-b beats_per_measure	the number of beats in a measure (defaults to four)\n"
						+ "	-n num_positions	the number of positions to rotate (defaults to one)\n"
						+ "	-l 				rotate left (default is rotate right)"
						+ "    -o output_file  the file to write the output to (defaults to shuffle.edl)\n"
						+ "    -g              debug mode (prints out chunk features on each line of output file)");
		System.out.println();
		System.exit(0);
	}

	public RotComposer(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Vector features = new Vector();

		// Parse arguments
		String argString = "b:n:l:o:g";

		Getopt opt = new Getopt("RotComposer", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			switch (c)
			{
			case 'b':
				beatsPerMeasure = new Integer(opt.getOptarg()).intValue();
				break;
			case 'n':
				numPositions = new Integer(opt.getOptarg()).intValue();
				break;
			case 'l':
				rotateRight = false;
				break;
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

	public EDLFile compose()
	{
		Iterator c = featFile.chunks.iterator();
		int totalChunks = featFile.chunks.size();

		double currTime = 0;
		// int currStartBeat = 0;
		int localStartBeat = 0;
		int currPosition = 0;

		if (rotateRight)
			localStartBeat = beatsPerMeasure - numPositions;
		else
			localStartBeat = numPositions;

		// System.out.println("localStartBeat: " + localStartBeat);

		for (int measure = 0; measure < totalChunks / beatsPerMeasure; measure++)
		{
			FeatChunk chunks[] = new FeatChunk[beatsPerMeasure];
			// int numBeatsFound = 0;

			for (int i = 0; i < beatsPerMeasure; i++)
				chunks[i] = (FeatChunk) c.next();

			currPosition = localStartBeat;

			for (int i = 0; i < beatsPerMeasure; i++)
			{
				// System.out.println("i: " + i + " currPosition: " +
				// currPosition);

				EDLChunk newChunk = new EDLChunk(chunks[currPosition], currTime);
				outFile.chunks.add(newChunk);
				currTime += newChunk.length;

				currPosition++;
				currPosition %= beatsPerMeasure;
			}

			progress.setValue(progress.getValue() + 1);
		}

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		return outFile;
	}

	public static void main(String[] args)
	{
		RotComposer m = new RotComposer(args);
		long startTime = System.currentTimeMillis();
		m.run();
		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
