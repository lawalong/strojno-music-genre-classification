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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Representation of a MEAPsoft file.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */

public abstract class MEAPFile implements Serializable
{
	public String filename;

	// have we read this file in yet?
	public boolean haveReadFile = false;

	// have we written this file yet?
	public boolean haveWrittenFile = false;

	/**
	 * Parse this MEAPFile.
	 */
	public abstract void readFile() throws IOException, ParserException;

	/**
	 * Write out properly formatted representation of this object
	 */
	protected abstract void write(Writer w) throws IOException;

	/**
	 * Write the file represented by this object.
	 */
	public void writeFile() throws IOException
	{
		writeFile(filename);
	}

	/**
	 * Write the file represented by this object.
	 */
	public void writeFile(String fn) throws IOException
	{
		writeFile(fn, false);
	}

	/**
	 * Write the file represented by this object.
	 * 
	 * If append is false, fn will be overwritten.
	 */
	public void writeFile(String fn, boolean append) throws IOException
	{
		if (fn == null)
			fn = filename;

		// only append if we are outputting to a different file
		BufferedWriter out = new BufferedWriter(new FileWriter(fn, append));
		write(out);
		out.close();

		haveWrittenFile = true;
	}

	/**
	 * Returns a String representing the properly formatted contents of this
	 * file
	 */
	public String toString()
	{
		StringWriter s = new StringWriter();

		try
		{
			write(s);
		}
		// this can't happen on a StringWriter, but we must appease Java
		catch (IOException e)
		{
			;
		}

		return s.toString();
	}
}
