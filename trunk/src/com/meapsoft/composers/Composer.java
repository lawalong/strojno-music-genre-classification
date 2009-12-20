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
import java.util.Vector;

import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.MEAPUtil;
import com.meapsoft.ParserException;

/**
 * Abstract class that defines the basic interface that all MEAPsoft composers
 * should follow.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public abstract class Composer extends MEAPUtil
{
	// Short description of this composer
	public String description = "I am a generic Composer.";

	//the name of this composer
	private String name = "This is my name";
	
	// The EDL file that this composer generates
	EDLFile outFile;

	// Commands to be applied to all
	Vector commands = null;

	/**
	 * Setup the Composer - read in files, etc.
	 * 
	 * This should be called before any call to compose() to ensure that the
	 * inputs and outputs are properly initialized.
	 */
	public void setup() throws IOException, ParserException
	{
		// clear old EDL
		outFile.clearChunks();

		// keep track of our progress in composing:
		progress.setMinimum(0);
		progress.setValue(0);
	}

	public void initNameAndDescription(String n, String d)
	{
		name = n;
		description = d;
	}
	
	/**
	 * Compose an EDLFile. This is where the magic happens.
	 */
	public abstract EDLFile compose();

	/*
	 * Setup, compose, and write output file. Handles exceptions.
	 */
	public void run()
	{
		try
		{
			doComposer();
		}
		catch (Exception e)
		{
			exceptionHandler.handleException(e);
		}
	}

	/**
	 * Setup, compose, and write output file. Doesn't handle exceptions.
	 */
	public void doComposer() throws IOException, ParserException
	{
		// System.out.println("Composer: doing setup...");
		setup();
		// System.out.println("Composer: doing compose...");
		compose();
		// System.out.println("Composer: adding commands...");
		addCommandsToAllEDLChunks();
		// System.out.println("Composer: writing file...");
		if (writeMEAPFile)
		{
			outFile.writeFile();
			// System.out.println("filename: " + outFile.filename);
		}
		// System.out.println("Composer: finished!");
	}

	/**
	 * Add a command to all chunks in outFile
	 */
	public void addCommandToAllEDLChunks(String cmd)
	{
		if (outFile != null)
		{
			Iterator i = outFile.chunks.iterator();
			while (i.hasNext())
			{
				EDLChunk chunk = (EDLChunk) i.next();

				if (chunk.commands != null)
					chunk.commands.add(cmd);
			}
		}
	}

	/**
	 * Add commands to all chunks in outFile
	 */
	public void addCommandsToAllEDLChunks(Vector cmds)
	{
		Iterator i = cmds.iterator();
		while (i.hasNext())
			addCommandToAllEDLChunks((String) i.next());
	}

	public void addCommandsToAllEDLChunks()
	{
		if (commands != null)
			addCommandsToAllEDLChunks(commands);
	}

	/**
	 * Add a command to be applied to all EDL chunks.
	 */
	public void addCommand(String cmd)
	{
		if (commands == null)
			commands = new Vector();

		commands.add(cmd);
	}

	/**
	 * Print usage of Compose command line options
	 */
	public static void printCommandLineOptions(char arg)
	{
		if (arg == 'c')
		{
			System.out
					.println("    -c command      apply command to all chunks in the EDL output file\n"
							+ "                    Supported commands include: reverse, crossfade(time), overlap(time) (time in seconds), gain(x)."
							+ "");
		}
		else
			MEAPUtil.printCommandLineOptions(arg);
	}

	/**
	 * Parse command strings from the command line arguments
	 */
	public void parseCommands(String[] args, String argString)
	{
		Getopt opt = new Getopt("Composer", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			if (c == 'c')
			{
				if (commands == null)
					commands = new Vector();

				commands.add(opt.getOptarg());
			}
		}
	}

	public String name()
	{
		return name;
	}
	
	public String description()
	{
		return description;
	}
}
