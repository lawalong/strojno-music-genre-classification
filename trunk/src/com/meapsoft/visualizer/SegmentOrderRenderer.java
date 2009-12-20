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
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.meapsoft.DSP;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatFile;

/**
 * @author douglas repetto
 * 
 */

public class SegmentOrderRenderer extends Renderer
{
	JCheckBox thickLines;

	JCheckBox multiLines;

	boolean thick = false;

	boolean multi = false;

	int currHeight = 0;

	int currWidth = 0;

	double currXMulti = 0.0;

	// amount of screen to use for top and bottom rows of blips
	double screenPercentage = .25;

	public SegmentOrderRenderer(FeatFile featFile, EDLFile eDLFile)
	{
		super(featFile, eDLFile, "SegmentOrderMapping");
	}

	public SegmentOrderRenderer(Renderer r)
	{
		super(r);

		// have to do this so we're not stuck with invalid
		// multipliers from another renderer
		updateColorMultipliers();
	}

	public void draw(BufferedImage image, int width, int height)
	{
		int numChunks = events.size();

		// set up scaling factors
		ChunkVisInfo lastChunk = (ChunkVisInfo) events.elementAt(numChunks - 1);
		double xMulti = width / (lastStartTime + lastChunk.length);
		// use 1/4 of the screen for plotting blips
		double scaledHeight = height * screenPercentage;

		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.white);// Color.lightGray);
		graphics.fillRect(0, 0, width, height);

		int blipHeight = (int) (scaledHeight / numFeatures);

		for (int i = 0; i < numChunks; i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.elementAt(i);

			int index = 0;
			for (int j = 0; j < numFeatures; j++)
			{
				int ndim = elementsPerFeature[j];

				// draw features in Matlab's 'axis xy' order
				// (i.e. lowest feature dimensions on the bottom)
				// for (int k = 0; k < ndim; k++)
				for (int k = ndim - 1; k >= 0; k--)
				{
					int[] featNum = { index };
					double featureValue = cVI.getFeatures(featNum)[0];

					featureValue -= lowestFeatureValue[j];
					/*
					 * if (colorMapType == SQRT) { featureValue -=
					 * lowestFeatureValue[j]; featureValue =
					 * Math.sqrt(featureValue); } else if (colorMapType ==
					 * SQUARE) { featureValue -= lowestFeatureValue[j];
					 * featureValue *= featureValue; }
					 */
					double colorIndex = featureValue * colorMultipliers[j];

					// graphics.setColor(colormap.table[(int)colorIndex]);

					if (cVI.selected)
					{
						graphics.setColor(colormap.table[(int) colorIndex]);
					}
					else
					{
						Color c = colormap.table[(int) colorIndex].darker()
								.darker().darker();
						graphics.setColor(c);
					}
					int x1 = (int) (cVI.startTime * xMulti);
					int blipWidth = (int) ((cVI.startTime + cVI.length) * xMulti);
					blipWidth = blipWidth - x1;
					int y = (int) (j * blipHeight + k * blipHeight / ndim);

					// int miniBlipHeight = blipHeight/ndim;
					int miniBlipHeight = (blipHeight + ndim / 2) / ndim;
					if (miniBlipHeight < 1)
						miniBlipHeight = 1;

					// draw top blip
					// if (cVI.selected)
					graphics.fillRect(x1, y, blipWidth, miniBlipHeight);

					// else
					// {
					// this is wonky, but if we always draw here then
					// multi-dimensional blips won't disappear when
					// they're deselected. Their rects are so small that
					// they just stack up and fill in all the space!
					// if (k == 0)
					// graphics.drawRect(x1, y, blipWidth, blipHeight);
					// }

					// draw bottom blip
					if (eDLFile != null)
					{
						if (cVI.dstTime != -1.0)
						{
							int x2 = (int) (cVI.dstTime * xMulti);

							// int y2 = (height - y) - blipHeight/ndim;
							// draw the bottom blip in the same
							// orientation as the top blip
							int y2 = height - j * blipHeight - (ndim - k)
									* blipHeight / ndim;
							// if (cVI.selected)
							graphics.fillRect(x2, y2, blipWidth + 1,
									miniBlipHeight);
							// else
							// {
							// if (k == 0)
							// graphics.drawRect(x2, y2, blipWidth, blipHeight);
							// }
							cVI.xEDL = x2;
							cVI.yEDL = y2;
						}
					}

					index++;

					cVI.xFeat = x1;
					cVI.yFeat = y;
					cVI.width = blipWidth;
					cVI.height = blipHeight;
				}
			}
		}

		// now do connections, if EDL file exists
		// we have to do it in its own loop, otherwise our messy chunk order
		// will result in lots of overlapping drawing!
		if (eDLFile != null)
		{
			for (int i = 0; i < numChunks; i++)
			{
				ChunkVisInfo cVI = (ChunkVisInfo) events.elementAt(i);

				int index = 0;

				for (int j = 0; j < numFeatures; j++)
				{
					/*
					 * for (int k = 0; k < elementsPerFeature[j]; k++) { int[]
					 * featNum = {index}; double[] values =
					 * cVI.getFeatures(featNum); double featureValue =
					 * values[0];
					 * 
					 * if (colorMapType == SQRT) { featureValue -=
					 * lowestFeatureValue[j]; featureValue =
					 * Math.sqrt(featureValue); } else if (colorMapType ==
					 * SQUARE) { featureValue -= lowestFeatureValue[j];
					 * featureValue *= featureValue; } else featureValue -=
					 * lowestFeatureValue[j];
					 * 
					 * double colorIndex = featureValue * colorMultipliers[j];
					 * 
					 * graphics.setColor(colormap.table[(int)colorIndex]);
					 */

					int ndim = elementsPerFeature[j];

					int[] featNum = DSP.irange(index, index + ndim - 1);
					double[] features = cVI.getFeatures(featNum);
					features = DSP.minus(features, lowestFeatureValue[j]);
					double featureValue = DSP.dot(features, features)
							/ Math.sqrt(ndim);

					featureValue = features[ndim - 1];
					/*
					 * if (sqrtFeatureValues.isSelected()) featureValue =
					 * Math.sqrt(featureValue); else if
					 * (squareFeatureValues.isSelected()) featureValue *=
					 * featureValue;
					 */
					double colorIndex = featureValue * colorMultipliers[j];

					graphics.setColor(colormap.table[(int) colorIndex]);

					int x1 = (int) (cVI.startTime * xMulti);
					int blipWidth = (int) ((cVI.startTime + cVI.length) * xMulti);
					blipWidth = blipWidth - x1;
					int y = (int) (j * blipHeight);

					if (cVI.dstTime != -1.0)
					{
						int x2 = (int) (cVI.dstTime * xMulti);

						// draw bottom blip
						if (cVI.selected)
						{
							// draw connection
							if (multi)
							{
								graphics.drawLine(x1 + blipWidth / 2, y
										+ blipHeight, x2 + blipWidth / 2,
										(height - y) - blipHeight);
								if (thick)
									graphics.drawLine((x1 + blipWidth / 2) + 1,
											y + blipHeight,
											(x2 + blipWidth / 2) + 1,
											(height - y) - blipHeight);
							}
							else
							{
								if (j == numFeatures - 1)
								{
									graphics.drawLine(x1 + blipWidth / 2, y
											+ blipHeight, x2 + blipWidth / 2,
											(height - y) - blipHeight);
									if (thick)
										graphics.drawLine(
												(x1 + blipWidth / 2) + 1, y
														+ blipHeight,
												(x2 + blipWidth / 2) + 1,
												(height - y) - blipHeight);
								}
							}
						}
					}

					index += ndim;

					graphics.setColor(Color.black);
					y = (blipHeight * j) + blipHeight;
					graphics.drawLine(0, y, width, y);
					graphics.drawLine(0, height - y, width, height - y);
				}
			}
		}

		if (dragRect != null)
		{
			// System.out.println("drawing: " + dragRect.toString());
			graphics.setColor(Color.black);
			graphics.drawRect(dragRect.x, dragRect.y, dragRect.width,
					dragRect.height);
		}

		currHeight = height;
		currWidth = width;
		currXMulti = xMulti;
	}

	public Vector getChunkVisInfosForPoint(Point p)
	{
		Vector chunks = new Vector();

		int whichFeature = getFeatureNumberForPoint(p);

		if (whichFeature != -1)
		{
			for (int i = 0; i < events.size(); i++)
			{
				ChunkVisInfo c = (ChunkVisInfo) events.elementAt(i);
				if (p.y >= 0 && p.y <= (currHeight * screenPercentage))
				{
					if (p.x >= c.xFeat && p.x <= (c.xFeat + c.width))
						chunks.add(c);
				}
				else if (p.y <= currHeight
						&& p.y >= currHeight - (currHeight * screenPercentage))
				{
					if (p.x >= c.xEDL && p.x <= (c.xEDL + c.width))
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
			double scaledHeight = currHeight * screenPercentage;
			int blipHeight = (int) (scaledHeight / numFeatures);
			// this only happens when we start up if the mouse moves over
			// display
			// before file is fully loaded/drawn
			if (blipHeight == 0)
				return featureNumber;

			int whichFeature = p.y / blipHeight;

			// try top
			if (whichFeature < numFeatures && whichFeature >= 0)
			{
				featureNumber = whichFeature;
				return featureNumber;
			}

			// try bottom
			whichFeature = (currHeight - p.y) / blipHeight;
			if (whichFeature < numFeatures && whichFeature >= 0)
			{
				featureNumber = whichFeature;
				return featureNumber;
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

				if (p.y >= 0 && p.y <= (currHeight * screenPercentage))
				{
					if (p.x >= cVI.xFeat && p.x <= cVI.xFeat + cVI.width)
					{
						int[] featNum = { 0 };

						for (int j = 0; j < whichFeature; j++)
							featNum[0] += elementsPerFeature[j];
						value = cVI.getFeatures(featNum)[0];
						return value;
					}
				}

				else if (p.y <= currHeight
						&& p.y >= currHeight - (currHeight / 4))
				{
					if (p.x >= cVI.xEDL && p.x <= cVI.xEDL + cVI.width)
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

			if (dragRect.y < currHeight / 2)
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

	public void actionPerformed(ActionEvent arg0)
	{
		super.actionPerformed(arg0);

		Object source = arg0.getSource();

		if (source == multiLines)
		{
			JCheckBox cb = (JCheckBox) source;

			multi = cb.isSelected();
		}
		if (source == thickLines)
		{
			JCheckBox cb = (JCheckBox) source;

			thick = cb.isSelected();
		}
	}

	public JPanel buildGUI(Color bgColor)
	{
		JPanel panel = super.buildGUI(bgColor);

		JPanel linesPanel = new JPanel();
		linesPanel.setBackground(bgColor);
		linesPanel.setLayout(new BoxLayout(linesPanel, BoxLayout.Y_AXIS));

		multiLines = new JCheckBox("multi lines");
		multiLines.setBackground(bgColor);
		multiLines.addActionListener(this);// dPanel);
		linesPanel.add(multiLines);

		thickLines = new JCheckBox("thick lines");
		thickLines.setBackground(bgColor);
		thickLines.addActionListener(this);// dPanel);
		linesPanel.add(thickLines);

		controlsPanel.add(linesPanel);

		return panel;
	}

}
