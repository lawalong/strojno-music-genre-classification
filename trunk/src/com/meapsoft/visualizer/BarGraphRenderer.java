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

/*
 * Created on Nov 15, 2006
 *
 * various ways of viewing EDL files
 * 
 */
package com.meapsoft.visualizer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.meapsoft.EDLFile;
import com.meapsoft.FeatFile;

/**
 * @author douglas repetto
 * 
 */

public class BarGraphRenderer extends Renderer
{
	JComboBox hOptions;

	JComboBox sOptions;

	double currXMulti = 0.0;

	double currHeight = 0.0;

	public BarGraphRenderer(FeatFile featFile, EDLFile eDLFile)
	{
		super(featFile, eDLFile, "BarGraph");
	}

	public BarGraphRenderer(Renderer r)
	{
		super(r);
		// have to do this so we're not stuck with invalid
		// multipliers from another renderer
		updateColorMultipliers();
	}

	public void draw(BufferedImage image, int width, int height)
	{
		// System.out.println("width: " + width + " height: " + height);
		int numChunks = events.size();

		// set up scaling factors
		ChunkVisInfo lastChunk = (ChunkVisInfo) events.elementAt(numChunks - 1);
		double xMulti = width / (lastStartTime + lastChunk.length);

		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.white);// Color.lightGray);
		graphics.fillRect(0, 0, width, height);

		for (int i = 0; i < numChunks; i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.elementAt(i);

			int[] featNum = { 0 };

			for (int j = 0; j < numFeatures; j++)
			{
				double features[] = cVI.getFeatures(featNum);
				double featureValue = features[0] - lowestFeatureValue[j];

				featNum[0] += elementsPerFeature[j];
				/*
				 * if (features[0] == highestFeatureValue[j])
				 * System.out.println("found hFV: " + features[0]);
				 */
				/*
				 * if (colorMapType == SQRT) { featureValue -=
				 * lowestFeatureValue[j]; featureValue =
				 * Math.sqrt(featureValue); } else if (colorMapType == SQUARE) {
				 * featureValue -= lowestFeatureValue[j]; featureValue *=
				 * featureValue; }
				 */
				double colorIndex = featureValue * colorMultipliers[j];
				Color extraColor;
				Color featureColor;

				if (cVI.selected)
				{
					extraColor = Color.white;
					featureColor = colormap.table[(int) colorIndex];
				}
				else
				{
					extraColor = Color.DARK_GRAY;
					featureColor = colormap.table[(int) colorIndex].darker()
							.darker().darker();
				}

				int blipHeight = 0;// (int)(cVI.length * yMulti);
				double yMulti = 0.0;// (height/longestChunk)/numFeatures;

				int which = hOptions.getSelectedIndex();

				if (which == 0)
				{
					yMulti = (height / lastStartTime) / numFeatures;
					blipHeight = (int) (Math.round(cVI.startTime * yMulti));
				}
				else if (which == 1)
				{
					yMulti = (height / lastStartTime) / numFeatures;
					blipHeight = (int) (Math.round(cVI.dstTime * yMulti));
				}
				else if (which == 2)
				{
					yMulti = (height / longestChunk) / numFeatures;
					blipHeight = (int) (Math.round(cVI.length * yMulti));
				}
				else if (which == 3)
				{
					yMulti = (height / (featureValueSpan[j])) / numFeatures;
					blipHeight = (int) (Math.round(featureValue * yMulti));
				}

				// + 1 is a kludge to avoid white bands
				int blipWidth = (int) (cVI.length * xMulti) + 1;

				// draw feats
				int x1 = (int) (Math.round(cVI.startTime * xMulti));
				// or draw EDL
				if (sOptions.getSelectedIndex() == 1)
					x1 = (int) (Math.round(cVI.dstTime * xMulti));

				int yOffset = Math.round((height / numFeatures) * j);
				int yTop = (height - yOffset) - (height / numFeatures);
				int yFeatStart = (height - yOffset) - blipHeight;

				// special drawing routine for multi-dimensional features
				if (elementsPerFeature[j] > 1)
				{
					// do background
					graphics.setColor(extraColor);
					graphics
							.fillRect(x1, yTop, blipWidth, height / numFeatures);
					// draw feature
					if (which == 3)
					{
						blipHeight = Math.round(height / numFeatures);
						kludgyMultiDimensionalDraw(x1, yTop, blipWidth,
								blipHeight, cVI, j, graphics);
					}
					else
						kludgyMultiDimensionalDraw(x1, yFeatStart, blipWidth,
								blipHeight, cVI, j, graphics);
				}
				else
				{
					// draw background
					graphics.setColor(extraColor);
					graphics
							.fillRect(x1, yTop, blipWidth, height / numFeatures);
					// draw feature
					graphics.setColor(featureColor);
					graphics.fillRect(x1, yFeatStart, blipWidth, blipHeight);
				}

				if (sOptions.getSelectedIndex() == 0)
					cVI.xFeat = x1;
				else
					cVI.xEDL = x1;

				cVI.width = blipWidth;
			}
			cVI.height = height;
		}

		if (dragRect != null)
		{
			// System.out.println("drawing: " + dragRect.toString());
			graphics.setColor(Color.black);
			graphics.drawRect(dragRect.x, dragRect.y, dragRect.width,
					dragRect.height);
		}

		currXMulti = xMulti;
		currHeight = height;
	}

	public void kludgyMultiDimensionalDraw(int x, int y, int w, int h,
			ChunkVisInfo cVI, int featNum, Graphics graphics)
	{
		int numElements = elementsPerFeature[featNum];

		int startElement = 0;

		for (int i = 0; i < featNum; i++)
			startElement += elementsPerFeature[i];

		double yIncr = (double) h / numElements;

		// System.out.println("x: " + x + " Oy: " + y + " w: " + w + " h: " + h
		// +
		// " featNum: " + featNum + " numElements: " + numElements +
		// " elementNum: " + elementNum + " yIncr: " + yIncr);

		for (int i = startElement; i < startElement + numElements; i++)
		{
			int[] fN = { i };
			double featureValue = cVI.getFeatures(fN)[0];

			featureValue -= lowestFeatureValue[featNum];

			double colorIndex = featureValue * colorMultipliers[featNum];

			Color c = colormap.table[(int) colorIndex];

			if (!cVI.selected)
				c = c.darker().darker().darker();

			graphics.setColor(c);

			int yLocal = (int) (Math.round((y + h) - (i * yIncr)));

			int miniBlipHeight = (int) (Math.round(yIncr));
			if (miniBlipHeight == 0)
				miniBlipHeight = 1;

			// System.out.println("eN: " + elementNum + " x: " + x + " yLocal: "
			// + yLocal + " w: " + w + " miniBlipHeight: " + miniBlipHeight);

			graphics.fillRect(x, yLocal, w, miniBlipHeight);
		}

	}

	public Vector getChunkVisInfosForPoint(Point p)
	{
		Vector chunks = new Vector();

		for (int i = 0; i < events.size(); i++)
		{
			ChunkVisInfo c = (ChunkVisInfo) events.elementAt(i);
			if (sOptions.getSelectedIndex() == 0)
			{
				if (p.x >= c.xFeat && p.x <= (c.xFeat + c.width))
				{
					chunks.add(c);
				}
			}
			else
			{
				if (p.x >= c.xEDL && p.x <= (c.xEDL + c.width))
				{
					chunks.add(c);
				}
			}
		}

		return chunks;
	}

	public int getFeatureNumberForPoint(Point p)
	{
		int featureNumber = -1;

		if (featFile != null)
		{
			int graphHeight = (int) (currHeight / numFeatures);

			for (int i = 0; i < numFeatures; i++)
			{
				int low = (int) (currHeight - (i * graphHeight)) - graphHeight;
				int high = (int) (currHeight - (i * graphHeight));
				if (p.y >= low && p.y <= high)
					featureNumber = i;
			}
		}
		return featureNumber;
	}

	public String getFeatureNameForPoint(Point p)
	{
		String featureName = "i don't know!";

		int whichFeature = getFeatureNumberForPoint(p);

		if (whichFeature != -1)
		{
			String fullFeatureName = (String) featFile.featureDescriptions
					.elementAt(whichFeature);
			// we're splitting on "." but have to use an escape sequence!
			String[] chunks = fullFeatureName.split("\\.");
			featureName = chunks[chunks.length - 1];
		}

		return featureName;
	}

	public double getFeatureValueForPoint(Point p)
	{
		double value = 0.0;

		int whichFeature = getFeatureNumberForPoint(p);

		if (whichFeature != -1)
		{
			for (int i = 0; i < events.size(); i++)
			{
				ChunkVisInfo cVI = (ChunkVisInfo) events.elementAt(i);

				if (sOptions.getSelectedIndex() == 0)
				{
					if (p.x >= cVI.xFeat && p.x <= (cVI.xFeat + cVI.width))
					{
						int[] featNum = { 0 };

						for (int j = 0; j < whichFeature; j++)
							featNum[0] += elementsPerFeature[j];
						value = cVI.getFeatures(featNum)[0];
						return value;
					}
				}
				else
				{
					if (p.x >= cVI.xEDL && p.x <= (cVI.xEDL + cVI.width))
					{
						int[] featNum = { 0 };

						for (int j = 0; j < whichFeature; j++)
							featNum[0] += elementsPerFeature[j];
						value = cVI.getFeatures(featNum)[0];
						return value;
					}
				}
			}
		}

		return value;
	}

	public void rangeFilterSelectionChanged()
	{
	}

	public void setDragRect(Rectangle r, boolean dS)
	{
		dragRect = r;
		dragShift = dS;

		if (!dragShift)
		{
			selectNone();
		}

		for (int i = 0; i < events.size(); i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.elementAt(i);
			Rectangle chunkRect = null;

			if (sOptions.getSelectedIndex() == 0)
			{
				chunkRect = new Rectangle(cVI.xFeat, cVI.yFeat, cVI.width,
						cVI.height);
			}
			else
			{
				chunkRect = new Rectangle(cVI.xEDL, cVI.yEDL, cVI.width,
						cVI.height);
			}
			if ((dragRect.x >= chunkRect.x && dragRect.x <= chunkRect.x
					+ chunkRect.width)
					|| (dragRect.x + dragRect.width >= chunkRect.x && dragRect.x
							+ dragRect.width <= chunkRect.x + chunkRect.width)
					|| (dragRect.x <= chunkRect.x && dragRect.x
							+ dragRect.width >= chunkRect.x + chunkRect.width))
				cVI.selected = true;
		}

		dragRect = null;
		dragShift = false;

		drawingPanel.repaint();
	}

	public JPanel buildGUI(Color bgColor)
	{

		JPanel panel = super.buildGUI(bgColor);

		JPanel hSPanel = new JPanel();
		hSPanel.setBackground(bgColor);
		hSPanel.setLayout(new BoxLayout(hSPanel, BoxLayout.Y_AXIS));

		JLabel hCL = new JLabel("height: ");
		hCL.setBackground(bgColor);
		hSPanel.add(hCL);

		String[] heightOptions = { "start time", "dest time", "length",
				"feature value" };

		hOptions = new JComboBox(heightOptions);
		hOptions.setBackground(bgColor);
		hOptions.setActionCommand("hOptions");
		hOptions.setSelectedIndex(2);
		hOptions.addActionListener(this);
		hSPanel.add(hOptions);

		JLabel sCL = new JLabel("show: ");
		sCL.setBackground(bgColor);
		hSPanel.add(sCL);

		String[] showOptions = { "feat file", "EDL file" };

		sOptions = new JComboBox(showOptions);
		sOptions.setBackground(bgColor);
		sOptions.setActionCommand("sOptions");
		sOptions.addActionListener(this);
		hSPanel.add(sOptions);

		controlsPanel.add(hSPanel);

		return panel;
	}

}
