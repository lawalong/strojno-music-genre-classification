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

import com.meapsoft.STFT;

/**
 * Extract a vector of features for a chunk.
 * 
 * @author Mike Mandel (mim@ee.columbia.edu)
 */

public abstract class FeatureExtractor
{
	/**
	 * Extract features from a chunk of STFT data.
	 */
	public abstract double[] features(STFT stft, long startFrame, int length, boolean preEmphasis);

	/**
	 * Returns a short string describing what this feature extractor does.
	 */
	public abstract String description();
}
