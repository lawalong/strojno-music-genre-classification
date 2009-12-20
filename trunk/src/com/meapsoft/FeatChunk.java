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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * Representation of a FeatFile Chunk
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */

public class FeatChunk extends Chunk implements Cloneable
{
	// Keep track of the types (and dimensions) of features contained
	// in the chunks in this file
	public Vector featureDescriptions = new Vector();
	
	// List of features associated with this chunk.
	protected Vector features = null;

	/**
	 * FeatChunk constructor
	 */
	public FeatChunk(String sf, double st, double l, Vector fD)
	{
		super(sf, st, l);

		features = new Vector();
		featureDescriptions = fD;
	}

	/**
	 * Add the given features to this chunk.
	 */
	public void addFeature(Object[] f)
	{
		features.addAll(Arrays.asList(f));
	}

	/**
	 * Add the given features to this chunk.
	 */
	public void addFeature(Collection f)
	{
		features.addAll(f);
	}

	/**
	 * Add the given features to this chunk.
	 */
	public void addFeature(double[] f)
	{
		// Java Fucking Sucks. why in gods name does the Double class need
		// to exist? Why can't all instances of doubles in Object context
		// be automatically promoted to Doubles. Why damnit why. (This
		// is actually solved in Java 1.5... They call it autoboxing)
		for (int i = 0; i < f.length; i++)
			features.add(new Double(f[i]));
	}

	/**
	 * Add the given features to this chunk.
	 */
	public void addFeature(Object f)
	{
		features.add(f);
	}

	/**
	 * Add the given features to this chunk.
	 */
	public void addFeature(double f)
	{
		features.add(new Double(f));
	}

	/**
	 * Set feature dimension idx to the given feature value.
	 */
	public void setFeature(int idx, double f)
	{
		features.set(idx, new Double(f));
	}

	/**
	 * Set feature dimension idx to the given feature value.
	 */
	public void setFeatures(double[] f)
	{
		features = new Vector(f.length);
		for (int x = 0; x < f.length; x++)
			features.add(x, new Double(f[x]));
	}

	/**
	 * Returns the number of features associated with this chunk
	 */
	public int numFeatures()
	{
		return features.size();
	}

	/**
	 * Get the features associated with this chunk
	 */
	public double[] getFeatures()
	{
		double[] feats = new double[features.size()];

		for (int i = 0; i < feats.length; i++)
			feats[i] = ((Double) features.get(i)).doubleValue();

		return feats;
	}

	/**
	 * Get the subset of features corresponding to the dimensions listed in idx.
	 */
	public double[] getFeatures(int[] idx)
	{
		if (features == null)
			return null;

		if (idx == null)
			return getFeatures();

		double[] feats = new double[idx.length];

		for (int i = 0; i < idx.length; i++)
			feats[i] = ((Double) features.get(idx[i])).doubleValue();

		return feats;
	}
	
	
	public double[] getFeatureByName(String featName)
	{
		//System.out.println("getting feature: " + featName + " (all chunks)");
		
		int featNum = getFeatureNumberForName(featName);
		
		//System.out.println("gFBN featNum: " + featNum);
		
		//we don't have that feature
		if (featNum == -1000)
		{
			System.out.println("I don't have a feature by that name: " + featName);
			return null;
		}
		
		//not sure what to do about waveform and spectrum right now...
		if (featNum == -2 || featNum == -3)
		{
			System.out.println("I can't return waveform or spectrum right now.");
			return null;	
		}
		
		//do normal features
		if (featNum >= 0)
		{
			double[] features = getFeatures(getIndexRangeForFeatureNumber(featNum));
			return features;
		}	
		
		//special case for Length
		if (featNum == -1)
		{
			double[] lengths = { length };
			
			return lengths;
		}
		
		return null;

	}

	public int getFeatureNumberForName(String featName)
	{	
		//do length
		if (featName.endsWith("ength"))
			return -1;
		
		if (featName.endsWith("aveform"))
			return -2;
		
		if (featName.endsWith("ectrum"))
			return -3;
		
		int numFeats = featureDescriptions.size();
		
		for (int i = 0; i < numFeats; i++)
		{
			String desc = (String) featureDescriptions.elementAt(i);
			//System.out.println("gFNFN trying: " + desc);
//			 dumb 1.4.2 way of telling if a string contains another string
			//if (desc.indexOf(featName) > 0)
			if (desc.contains(featName))
			{
				return i;
			}
		}
		
		System.out.println(featName + " doesn't seem to be a valid feature.");
		
		return -1000;
	}
	
	public int[] getIndexRangeForFeatureNumber(int featNum)
	{
		int numFeats = featureDescriptions.size();
		
		if (featNum >= numFeats)
		{
			System.out.println("featNum >= numFeats");
			return null;
		}
		
		int[] featLengths = getFeatureLengths();
		int featLength = featLengths[featNum];

		
		int indexOffset = 0;
		
		for (int i = 0; i < featNum; i++)
			indexOffset += featLengths[i];
		
		int[] indexs = new int[featLength];
		
		for (int i = 0; i < featLength; i++)
			indexs[i] = indexOffset + i;
		
		return indexs;
	}
	
	public boolean containsFeature(String featName)
	{
		if (getFeatureNumberForName(featName) > -1000)
			return true;
		else
			return false;
	}

	/**
	 * Return the number of elements in each feature
	 * 
	 * i.e. for AvgMelSpec (0-39) and AvgChroma (40-51) we return [40, 12]
	 * 
	 */

	public int[] getFeatureLengths()
	{
		int[] lengths = new int[featureDescriptions.size()];

		for (int i = 0; i < featureDescriptions.size(); i++)
		{
			int numDim = Integer.parseInt(((String) featureDescriptions.get(i))
					.split("[()]")[1]);
			lengths[i] = numDim;
		}
		return lengths;
	}

	
	/**
	 * Remove the features associated with this Chunk
	 */
	public void clearFeatures()
	{
		features.clear();
	}
	
	/**
	 * Write a description of this Chunk as a String in the format expected in a
	 * FeatFile
	 */
	public String toString()
	{
		// concatenating strings is super slow. Better to use StringBuffer
		// guesstimate the string length
		StringBuffer s = new StringBuffer(20 * features.size());
		s.append(srcFile.replaceAll(" ", "%20")).append("\t").append(startTime)
				.append("\t").append(length).append("\t");

		if (features != null)
		{
			Iterator x = features.iterator();
			while (x.hasNext())
				s.append(x.next()).append("\t");
		}

		s.append(comment).append("\n");

		return s.toString();
	}

	/**
	 * Clone this FeatChunk
	 */
	public Object clone()
	{
		FeatChunk o = new FeatChunk(this.srcFile, this.startTime, this.length, this.featureDescriptions);
		o.comment = this.comment;
		o.features = (Vector) this.features.clone();

		return o;
	}
}
