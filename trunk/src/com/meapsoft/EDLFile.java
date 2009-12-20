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

package com.meapsoft;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;

/**
 * Representation of a MEAPsoft edit decision list file.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */

public class EDLFile extends FeatFile // implements Cloneable
{
	public EDLFile(String fn)
	{
		filename = fn;

		chunks = new Vector(100, 0);
	}

	/**
	 * Read in an EDL file
	 */
	public void readFile() throws IOException, ParserException
	{
		BufferedReader in = new BufferedReader(new FileReader(filename));

		String audioFile = "";
		double dstTime;
		double chunkLength;
		double srcTime;

		// each line (excluding comments) should look like:
		// dest_time src_filename src_time chunk_length cmd1 cmd2 ...

		// Parse each line of the input file
		long lineno = 0;
		String line;
		while ((line = in.readLine()) != null)
		{
			lineno++;

			// extract any comments from the current line
			String comment = "";
			Matcher c = commentPattern.matcher(line + "\n");
			if (c.find())
			{
				// comments go all the way to the end of the line
				comment = c.group() + line.substring(c.end()) + "\n";
				line = line.substring(0, c.start());
			}

			Matcher p = linePattern.matcher(line);
			// is there anything else?
			if (!p.find())
				continue;
			try
			{
				dstTime = Double.parseDouble(p.group(1));
			}
			catch (NumberFormatException nfe)
			{
				throw new ParserException(filename, lineno,
						"Could not parse dest start time \"" + p.group(1)
								+ "\".");
			}

			if (!p.find())
				throw new ParserException(filename, lineno,
						"Could not find source filename.");
			audioFile = p.group(1);
			// decode spaces in the file name
			audioFile = audioFile.replaceAll("%20", " ");

			if (!p.find())
				throw new ParserException(filename, lineno,
						"Could not find source start time.");
			try
			{
				srcTime = Double.parseDouble(p.group(1));
			}
			catch (NumberFormatException nfe)
			{
				throw new ParserException(filename, lineno,
						"Could not parse source start time \"" + p.group(1)
								+ "\".");
			}

			if (!p.find())
				throw new ParserException(filename, lineno,
						"Could not find chunk length.");
			try
			{
				chunkLength = Double.parseDouble(p.group(1));
			}
			catch (NumberFormatException nfe)
			{
				throw new ParserException(filename, lineno,
						"Could not parse chunk length \"" + p.group(1) + "\".");
			}

			EDLChunk ch = new EDLChunk(audioFile, srcTime, chunkLength, dstTime);
			ch.comment = comment;

			// everything else on the current line is a command
			while (p.find())
				ch.commands.add(p.group(1));

			chunks.add(ch);
		}
		
		in.close();
		haveReadFile = true;
	}

	/**
	 * Get the matrix of features associated with each chunk in this EDLFile
	 */
	public double[][] getFeatures()
	{
		return getFeatures(null);
	}

	/**
	 * Get the matrix of features associated with each chunk in this EDLFile
	 */
	public double[][] getFeatures(int[] featdim)
	{
		// want chunks sorted in time;
		Collections.sort(chunks);

		double[][] f = super.getFeatures(featdim);

		return f;
	}

	/**
	 * Clone this EDLFile
	 */
	/*
	 * public Object clone() { EDLFile o = new EDLFile(this.filename); //
	 * superclass (MEAPFile) fields o.haveReadFile = this.haveReadFile;
	 * o.haveWrittenFile = this.haveWrittenFile; // local fields
	 * o.featureDescriptions = new Vector(this.featureDescriptions);
	 * 
	 * o.chunks = new Vector(); Iterator i = this.chunks.iterator();
	 * while(i.hasNext()) o.chunks.add(((EDLChunk)i.next()).clone());
	 * 
	 * return o; }
	 */

	/**
	 * Write the contents of this FeatFile
	 */
	protected void write(Writer w) throws IOException
	{
		// write the header
		w.write("#dst_time\tsrc_filename\tsrc_onset_time\tsrc_chunk_length\t[commands]\n");

		// its much easier to read if the output is in sorted in time;
		Collections.sort(chunks);

		Iterator i = chunks.iterator();
		while (i.hasNext())
			w.write(i.next().toString());
	}
}
