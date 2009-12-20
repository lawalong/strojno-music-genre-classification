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

package com.meapsoft.featextractors;

import com.meapsoft.FeatFile;
import com.meapsoft.STFT;

/**
 * Extension of FeatureExtractor that takes a FeatFile instead of FFT data. This
 * allows meta features to be extracted from whole groups of already extracted
 * features.
 * 
 * @author Mike Mandel (mim@ee.columbia.edu)/Douglas Repetto
 */

public abstract class MetaFeatureExtractor extends FeatureExtractor
{
	// not used in a meta feature extractor
	public double[] features(STFT stft, long startFrame, int length, boolean preEmphasis)
	{
		return null;
	}

	/**
	 * Extract meta features from featFile. Clears the original features.
	 */
	public void features(FeatFile featFile)
	{
		features(featFile, true);
	}

	public abstract void features(FeatFile featFile,
			boolean clearOriginalFeatures);
}
