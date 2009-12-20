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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.meapsoft.EDLFile;
import com.meapsoft.FeatFile;

/**
 * @author douglas repetto
 * 
 */

public class LineGraphRenderer extends Renderer
{
	JComboBox sOptions;

	JCheckBox connectOption;

	boolean connect;

	double currXMulti = 0.0;

	double currHeight = 0.0;

	public LineGraphRenderer(FeatFile featFile, EDLFile eDLFile)
	{
		super(featFile, eDLFile, "LineGraph");
	}

	public LineGraphRenderer(Renderer r)
	{
		super(r);
		// have to do this so we're not stuck with invalid
		// multipliers from another renderer
		updateColorMultipliers();
	}

	public void draw(BufferedImage image, int width, int height)
	{
		// System.out.println("width: " + width + " height: " + height);
		// height *= .5;
		int numChunks = events.size();

		// set up scaling factors
		ChunkVisInfo lastChunk = (ChunkVisInfo) events.elementAt(numChunks - 1);
		double xMulti = width / (lastStartTime + lastChunk.length);

		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, width, height);

		Point[][] previousPoints = new Point[numChunks][numFeatures];

		int colorMulti = numColors / numFeatures;

		for (int i = 0; i < numChunks; i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.elementAt(i);

			int[] featNum = { 0 };

			for (int j = 0; j < numFeatures; j++)
			{
				double features[] = cVI.getFeatures(featNum);
				double featureValue = features[0] - lowestFeatureValue[j];

				featNum[0] += elementsPerFeature[j];

				// double colorIndex = featureValue * colorMultipliers[j];

				int colorIndex = j * colorMulti;

				Color featureColor = colormap.table[(int) colorIndex];

				if (!cVI.selected)
					featureColor = featureColor.darker().darker().darker();

				graphics.setColor(featureColor);

				int blobSize = (int) Math.round(width / numChunks) - 2;
				if (blobSize <= 1)
					blobSize = 2;

				double yMulti = (height - blobSize)
						/ (highestFeatureValue[j] - lowestFeatureValue[j]);

				// draw feats
				int x = (int) (Math.round(cVI.startTime * xMulti));
				// or draw EDL
				if (sOptions.getSelectedIndex() == 1)
					x = (int) (Math.round(cVI.dstTime * xMulti));
				int y = (int) (Math.round(featureValue * yMulti));

				graphics.fillOval(x, y, blobSize, blobSize);

				if (i > 0 && connectOption.isSelected())
				{
					Point pP = previousPoints[i - 1][j];
					if (pP != null)
					{
						int pX = pP.x + (blobSize / 2);
						int pY = pP.y + (blobSize / 2);
						graphics.setColor(featureColor.brighter());
						graphics.drawLine(x + (blobSize / 2), y
								+ (blobSize / 2), pX, pY);
					}
				}
				previousPoints[i][j] = new Point(x, y);

				// record our position
				if (j == 0)
				{
					if (sOptions.getSelectedIndex() == 0)
					{
						cVI.xFeat = x;
						cVI.yFeat = y;
					}
					else
					{
						cVI.xEDL = x;
						cVI.yEDL = y;
					}
					cVI.width = blobSize;
					cVI.height = blobSize;
				}
			}
		}

		currHeight = height;

		if (dragRect != null)
		{
			graphics.setColor(Color.black);
			graphics.drawRect(dragRect.x, dragRect.y, dragRect.width,
					dragRect.height);
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
					chunks.add(c);
			}
			else
			{
				if (p.x >= c.xEDL && p.x <= (c.xEDL + c.width))
					chunks.add(c);
			}

		}

		return chunks;
	}

	public int getFeatureNumberForPoint(Point p)
	{
		int featureNumber = -1;

		if (featFile != null)
		{
			for (int i = 0; i < events.size(); i++)
			{
				ChunkVisInfo cVI = (ChunkVisInfo) events.elementAt(i);

				if (sOptions.getSelectedIndex() == 0)
				{
					if (p.x >= cVI.xFeat && p.x <= (cVI.xFeat + cVI.width))
					{
						int[] featNum = { 0 };

						for (int j = 0; j < numFeatures; j++)
						{
							double features[] = cVI.getFeatures(featNum);
							double featureValue = features[0]
									- lowestFeatureValue[j];

							featNum[0] += elementsPerFeature[j];

							double yMulti = currHeight
									/ (highestFeatureValue[j] - lowestFeatureValue[j]);
							int y = (int) (Math.round(featureValue * yMulti));

							if (p.y >= y && p.y <= y + cVI.height)
								featureNumber = j;
						}
					}
				}
				else
				{
					if (p.x >= cVI.xEDL && p.x <= (cVI.xEDL + cVI.width))
					{
						int[] featNum = { 0 };

						for (int j = 0; j < numFeatures; j++)
						{
							double features[] = cVI.getFeatures(featNum);
							double featureValue = features[0]
									- lowestFeatureValue[j];

							featNum[0] += elementsPerFeature[j];

							double yMulti = currHeight
									/ (highestFeatureValue[j] - lowestFeatureValue[j]);
							int y = (int) (Math.round(featureValue * yMulti));

							if (p.y >= y && p.y <= y + cVI.height)
								featureNumber = j;
						}
					}
				}
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
						if (p.y >= cVI.yFeat && p.y <= (cVI.yFeat + cVI.height))
						{
							int[] featNum = { 0 };

							for (int j = 0; j < whichFeature; j++)
								featNum[0] += elementsPerFeature[j];
							value = cVI.getFeatures(featNum)[0];
							return value;
						}
					}
				}
				else
				{
					if (p.x >= cVI.xEDL && p.x <= (cVI.xEDL + cVI.width))
					{
						if (p.y >= cVI.yEDL && p.y <= (cVI.yEDL + cVI.height))
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

	/*
	 * public void actionPerformed(ActionEvent arg0) { Object source =
	 * arg0.getSource(); String command = arg0.getActionCommand();
	 * 
	 * super.actionPerformed(arg0); }
	 */
	public JPanel buildGUI(Color bgColor)
	{

		JPanel panel = super.buildGUI(bgColor);

		JPanel cSPanel = new JPanel();
		cSPanel.setBackground(bgColor);
		cSPanel.setLayout(new BoxLayout(cSPanel, BoxLayout.Y_AXIS));

		connectOption = new JCheckBox("connect dots");
		connectOption.setBackground(bgColor);
		connectOption.setActionCommand("connect");
		connectOption.addActionListener(this);

		cSPanel.add(connectOption);

		JLabel sCL = new JLabel("show: ");
		sCL.setBackground(bgColor);
		cSPanel.add(sCL);

		String[] showOptions = { "feat file", "EDL file" };

		sOptions = new JComboBox(showOptions);
		sOptions.setBackground(bgColor);
		sOptions.setActionCommand("sOptions");
		sOptions.addActionListener(this);
		cSPanel.add(sOptions);

		controlsPanel.add(cSPanel);

		return panel;
	}

}
