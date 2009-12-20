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

import java.io.IOException;

import java.util.Date;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Representation of a single audio chunk (i.e. a single line of an EDL or
 * feature file).
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public abstract class Chunk implements Comparable
{
	// Filename of audio file that contains this chunk
	public String srcFile;

	// Start time (in seconds) of this chunk in srcFile
	public double startTime;

	// Length (in seconds) of this chunk
	public double length;

	// Comment string associated with this chunk - used so that
	// FeatExtractor will not strip the comments from a segment file
	public String comment = "";

	private boolean cacheSamples = false;

	private AudioFormat sampleCacheFormat;

	private byte[] sampleCache;

	/**
	 * Chunk constructor
	 */
	public Chunk(String sf, double st, double l)
	{
		srcFile = sf;
		startTime = st;
		length = l;
	}

	/**
	 * Load samples corresponding to this Chunk
	 */
	public double[] getSamples(AudioFormat format) throws IOException,
			UnsupportedAudioFileException
	{
		AudioReader reader = AudioReaderFactory.getAudioReader(srcFile, format);

		float sampleRate = format.getSampleRate();
		int numChannels = format.getChannels();

		int startSample = (int) (startTime * sampleRate * numChannels);
		int numSamples = (int) (length * sampleRate * numChannels);

		reader.skipSamples(startSample);
		double[] samples = new double[numSamples];
		reader.readSamples(samples);
		reader.closeStream();

		return samples;
	}

	/**
	 * Compare one Chunk to another - comparisons are performed based on srcFile
	 * and then startTime fields.
	 */
	public int compareTo(Object o) throws ClassCastException
	{
		Chunk c = (Chunk) o;

		int compare = this.srcFile.compareTo(c.srcFile);
		if (compare == 0)
			compare = Double.compare(this.startTime, c.startTime);

		return compare;
	}

	/**
	 * Return a string representation of this chunk, using the segment file
	 * format.
	 */
	public String toString()
	{
		// concatenating strings is super slow. Better to use StringBuffer
		// guesstimate the string length
		StringBuffer s = new StringBuffer(200);
		s.append(srcFile.replaceAll(" ", "%20")).append(" ").append(startTime)
				.append(" ");
		s.append(length).append(" ").append(comment).append("\n");

		return s.toString();
	}
}
