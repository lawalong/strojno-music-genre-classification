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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.meapsoft.EDLFile;
import com.meapsoft.FeatFile;

/**
 * @author douglas repetto
 * 
 */

public class ScatterPlotRenderer extends Renderer
{
	JPanel dataMappingControlsPanel;

	JComboBox xOptions;

	JComboBox yOptions;

	JComboBox hOptions;

	JComboBox wOptions;

	JComboBox cOptions;

	JComboBox sOptions;

	boolean ovals = false;

	int currWidth = 0;

	int currHeight = 0;

	double currXMulti = 0.0;

	double currYMulti = 0.0;

	// 1/20 of the screen
	double maxBlobSize = 0.05;

	public ScatterPlotRenderer(FeatFile featFile, EDLFile eDLFile)
	{
		super(featFile, eDLFile, "ScatterPlot");
	}

	public ScatterPlotRenderer(Renderer r)
	{
		super(r);
	}

	public void parseFiles()
	{
		super.parseFiles();
		updateOptionBoxes();
	}

	public void draw(BufferedImage image, int width, int height)
	{
		int numChunks = events.size();

		/*
		 * set up scaling factors
		 */

		double xMulti = 0.0;
		double yMulti = 0.0;
		double wMulti = 0.0;
		double hMulti = 0.0;

		// have to do width and height first so that we can use them
		// to calculate x and y correctly!

		int wSelection = this.wOptions.getSelectedIndex();

		if (wSelection == 0)
		{
			double multi = width * maxBlobSize;

			wMulti = multi / lastStartTime;
		}
		else if (wSelection == 1)
		{
			double multi = width * maxBlobSize;

			wMulti = multi / lastStartTime;
		}
		else if (wSelection == 2)
		{
			double multi = width * maxBlobSize;

			wMulti = multi / longestChunk;
		}
		else if (wSelection >= 3)
		{
			double multi = width * maxBlobSize;
			int which = wSelection - 3;

			wMulti = multi / highestFeatureValue[which];
		}

		int hSelection = this.hOptions.getSelectedIndex();

		if (hSelection == 0)
		{
			double multi = height * maxBlobSize;

			hMulti = multi / lastStartTime;
		}
		else if (hSelection == 1)
		{
			double multi = height * maxBlobSize;

			hMulti = multi / lastStartTime;
		}
		else if (hSelection == 2)
		{
			double multi = height * maxBlobSize;

			hMulti = multi / longestChunk;
		}
		else if (hSelection >= 3)
		{
			double multi = height * maxBlobSize;
			int which = hSelection - 3;

			hMulti = multi / highestFeatureValue[which];
		}

		int xSelection = this.xOptions.getSelectedIndex();

		if (xSelection == 0)
		{
			// can we really assume that the last chunk in the Vector is the
			// last chunk in time???
			ChunkVisInfo lastChunk = (ChunkVisInfo) events
					.elementAt(numChunks - 1);

			int adjWidth = width;

			if (wSelection == 0 || wSelection == 1)
				adjWidth = width
						- (int) (Math.round(wMulti * lastChunk.startTime));
			if (wSelection == 2)
				adjWidth = width
						- (int) (Math.round(wMulti * lastChunk.length));
			if (wSelection >= 3)
			{
				int whichW = wSelection - 3;
				adjWidth = width
						- (int) (Math.round(wMulti
								* highestFeatureValue[whichW]));
			}

			xMulti = adjWidth / lastStartTime;
		}
		else if (xSelection == 1)
		{
			// can we really assume that the last chunk in the Vector is the
			// last chunk in time???
			ChunkVisInfo lastChunk = (ChunkVisInfo) events
					.elementAt(numChunks - 1);

			int adjWidth = width;

			if (wSelection == 0 || wSelection == 1)// wST.isSelected())
				adjWidth = width
						- (int) (Math.round(wMulti * lastChunk.startTime));
			if (wSelection == 2)// wL.isSelected())
				adjWidth = width
						- (int) (Math.round(wMulti * lastChunk.length));
			if (wSelection >= 3)// wFV.isSelected())
			{
				int whichW = wSelection - 3;
				adjWidth = width
						- (int) (Math.round(wMulti
								* highestFeatureValue[whichW]));
			}

			xMulti = adjWidth / lastStartTime;
		}
		else if (xSelection == 2)
		{
			int adjWidth = width;

			if (wSelection == 0 || wSelection == 1)// wST.isSelected())
				adjWidth = width - (int) (Math.round(wMulti * lastStartTime));
			if (wSelection == 2)// wL.isSelected())
				adjWidth = width - (int) (Math.round(wMulti * longestChunk));
			if (wSelection >= 3)// wFV.isSelected())
			{
				int whichW = wSelection - 3;
				adjWidth = width
						- (int) (Math.round(wMulti
								* highestFeatureValue[whichW]));
			}

			xMulti = adjWidth / (longestChunk - shortestChunk);
		}
		else if (xSelection >= 3)// xFV.isSelected())
		{
			int adjWidth = width;
			int which = xSelection - 3;

			if (wSelection == 0 || wSelection == 1)// wST.isSelected())
				adjWidth = width - (int) (Math.round(wMulti * lastStartTime));
			if (wSelection == 2)// wL.isSelected())
				adjWidth = width - (int) (Math.round(wMulti * longestChunk));
			if (wSelection >= 3)// wFV.isSelected())
			{
				int whichW = wSelection - 3;
				adjWidth = width
						- (int) (Math.round(wMulti
								* highestFeatureValue[whichW]));
			}

			xMulti = adjWidth
					/ (highestFeatureValue[which] - lowestFeatureValue[which]);
		}

		int ySelection = this.yOptions.getSelectedIndex();

		if (ySelection == 0)// yST.isSelected())
		{
			// can we really assume that the last chunk in the Vector is the
			// last chunk in time???
			ChunkVisInfo lastChunk = (ChunkVisInfo) events
					.elementAt(numChunks - 1);

			int adjHeight = height;

			if (hSelection == 0 || hSelection == 1)// hST.isSelected())
				adjHeight = height
						- (int) (Math.round(hMulti * lastChunk.startTime));
			if (hSelection == 2)// hL.isSelected())
				adjHeight = height
						- (int) (Math.round(hMulti * lastChunk.length));
			if (hSelection >= 3)// hFV.isSelected())
			{
				int whichH = hSelection - 3;
				adjHeight = height
						- (int) (Math.round(hMulti
								* highestFeatureValue[whichH]));
			}

			yMulti = adjHeight / lastStartTime;
		}
		else if (ySelection == 1)// yDT.isSelected())
		{
			// can we really assume that the last chunk in the Vector is the
			// last chunk in time???
			ChunkVisInfo lastChunk = (ChunkVisInfo) events
					.elementAt(numChunks - 1);

			int adjHeight = height;

			if (hSelection == 0 || hSelection == 1)// hST.isSelected())
				adjHeight = height
						- (int) (Math.round(hMulti * lastChunk.startTime));
			if (hSelection == 2)// hL.isSelected())
				adjHeight = height
						- (int) (Math.round(hMulti * lastChunk.length));
			if (hSelection >= 3)// hFV.isSelected())
			{
				int whichH = hSelection - 3;
				adjHeight = height
						- (int) (Math.round(hMulti
								* highestFeatureValue[whichH]));
			}

			yMulti = adjHeight / lastStartTime;
		}
		else if (ySelection == 2)// yL.isSelected())
		{
			int adjHeight = height;

			if (hSelection == 0 || hSelection == 1)// hST.isSelected())
				adjHeight = height - (int) (Math.round(hMulti * lastStartTime));
			if (hSelection == 2)// hL.isSelected())
				adjHeight = height - (int) (Math.round(hMulti * longestChunk));
			if (hSelection >= 3)// hFV.isSelected())
			{
				int whichH = hSelection - 3;
				adjHeight = height
						- (int) (Math.round(hMulti
								* highestFeatureValue[whichH]));
			}

			yMulti = adjHeight / (longestChunk - shortestChunk);

			// yMulti = height/(longestChunk + longestChunk);
		}
		else if (ySelection >= 3)// xFV.isSelected())
		{
			int adjHeight = height;
			int which = ySelection - 3;

			if (hSelection == 0 || hSelection == 1)// wST.isSelected())
				adjHeight = height - (int) (Math.round(hMulti * lastStartTime));
			if (hSelection == 2)// wL.isSelected())
				adjHeight = height - (int) (Math.round(hMulti * longestChunk));
			if (hSelection >= 3)// wFV.isSelected())
			{
				int whichH = hSelection - 3;
				adjHeight = height
						- (int) (Math.round(hMulti
								* highestFeatureValue[whichH]));
			}

			yMulti = adjHeight
					/ (highestFeatureValue[which] - lowestFeatureValue[which]);
		}

		/*
		 * clear screen
		 */

		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, width, height);

		/*
		 * do each chunk
		 */

		for (int i = 0; i < numChunks; i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.elementAt(i);

			/*
			 * get color info
			 */

			int whichFeature = cOptions.getSelectedIndex();
			double featureValue = 0.0;
			int[] featNum = { 0 };

			if (whichFeature == 0)
				featureValue = cVI.startTime;
			else if (whichFeature == 1)
				featureValue = cVI.dstTime;
			else if (whichFeature == 2)
				featureValue = cVI.length;
			else if (whichFeature >= 3)
			{
				int which = whichFeature - 3;

				for (int j = 0; j < which; j++)
					featNum[0] += elementsPerFeature[j];

				double features[] = cVI.getFeatures(featNum);
				featureValue = features[0] - lowestFeatureValue[which];
			}

			/*
			 * TOO COMPLICATED RIGHT NOW, MY HEAD HURTS!!! We need to do all
			 * possible combos of mappings...
			 */
			/*
			 * if (colorMapType == SQRT) { featureValue -=
			 * lowestFeatureValue[0];//j]; featureValue =
			 * Math.sqrt(featureValue); } else if (colorMapType == SQUARE) {
			 * featureValue -= lowestFeatureValue[0];//j]; featureValue *=
			 * featureValue; } else featureValue -= lowestFeatureValue[0];//j];
			 */

			int colorIndex = (int) (Math.round(featureValue
					* colorMultipliers[0]));// j]);

			Color borderColor;
			Color featureColor;
			// graphics.setColor(colormap.table[colorIndex]);

			if (cVI.selected)
			{
				borderColor = Color.black;// colormap.table[(int)colorIndex].brighter().brighter();
				featureColor = colormap.table[(int) colorIndex];

				// graphics.setColor(borderColor);
			}
			else
			{
				borderColor = colormap.table[(int) colorIndex].darker()
						.darker().darker();
				featureColor = colormap.table[(int) colorIndex].darker()
						.darker().darker();
				// graphics.setColor(c);
			}

			/*
			 * find location
			 */
			int x = 0;
			int y = 0;
			int w = 0;
			int h = 0;

			if (xSelection == 0)
				x = (int) (Math.round(cVI.startTime * xMulti));
			else if (xSelection == 1)
				x = (int) (Math.round(cVI.dstTime * xMulti));
			else if (xSelection == 2)
				x = (int) (Math.round((cVI.length - shortestChunk) * xMulti));
			else if (xSelection >= 3)
			{
				whichFeature = xSelection - 3;
				featNum[0] = 0;

				for (int j = 0; j < whichFeature; j++)
					featNum[0] += elementsPerFeature[j];

				double features[] = cVI.getFeatures(featNum);
				double value = features[0] - lowestFeatureValue[whichFeature];
				x = (int) (Math.round(value * xMulti));
			}

			if (ySelection == 0)
				y = (int) (Math.round(cVI.startTime * yMulti));
			else if (ySelection == 1)
				y = (int) (Math.round(cVI.dstTime * yMulti));
			else if (ySelection == 2)
				y = (int) (Math.round((cVI.length - shortestChunk) * yMulti));
			else if (ySelection >= 3)
			{
				whichFeature = ySelection - 3;
				featNum[0] = 0;

				for (int j = 0; j < whichFeature; j++)
					featNum[0] += elementsPerFeature[j];

				double features[] = cVI.getFeatures(featNum);
				double value = features[0] - lowestFeatureValue[whichFeature];
				y = (int) (Math.round(value * yMulti));
			}

			if (hSelection == 0)
				h = (int) (Math.round(cVI.startTime * hMulti));
			else if (hSelection == 1)
				h = (int) (Math.round(cVI.dstTime * hMulti));
			else if (hSelection == 2)
				h = (int) (Math.round(cVI.length * hMulti));
			else if (hSelection >= 3)
			{
				whichFeature = hSelection - 3;
				featNum[0] = 0;

				for (int j = 0; j < whichFeature; j++)
					featNum[0] += elementsPerFeature[j];

				double features[] = cVI.getFeatures(featNum);
				double value = features[0] - lowestFeatureValue[whichFeature];
				h = (int) (Math.round(value * hMulti));
			}

			if (wSelection == 0)
				w = (int) (Math.round(cVI.startTime * wMulti));
			else if (wSelection == 1)
				w = (int) (Math.round(cVI.dstTime * wMulti));
			else if (wSelection == 2)
				w = (int) (Math.round(cVI.length * wMulti));
			else if (wSelection >= 3)
			{
				whichFeature = wSelection - 3;
				featNum[0] = 0;

				for (int j = 0; j < whichFeature; j++)
					featNum[0] += elementsPerFeature[j];

				double features[] = cVI.getFeatures(featNum);
				double value = features[0] - lowestFeatureValue[whichFeature];
				w = (int) (Math.round(value * wMulti));
			}

			if (h == 0)
				h = 1;
			if (w == 0)
				w = 1;

			// System.out.println("x: " + x + " y: " + y + " h: " + h + " w: " +
			// w);

			whichFeature = cOptions.getSelectedIndex();

			int wInset = w - 4;
			if (wInset < 1)
				wInset = 1;

			int hInset = h - 4;
			if (hInset < 1)
				hInset = 1;

			// special drawing routine for multi-dimensional features
			if (whichFeature >= 3 && elementsPerFeature[whichFeature - 3] > 1)
			{
				if (cVI.selected)
				{
					graphics.setColor(borderColor);
					graphics.fillRect(x, y, w, h);
					kludgyMultiDimensionalDraw(x + 2, y + 2, wInset, hInset,
							cVI, cOptions.getSelectedIndex() - 3, graphics);
				}
				else
					kludgyMultiDimensionalDraw(x, y, w, h, cVI, cOptions
							.getSelectedIndex() - 3, graphics);

			}
			// regular drawing routines
			else
			{
				graphics.setColor(borderColor);

				if (ovals)
					graphics.fillOval(x, y, w, h);
				else
					graphics.fillRect(x, y, w, h);

				graphics.setColor(featureColor);

				if (ovals)
					graphics.fillOval(x + 2, y + 2, wInset, hInset);
				else
					graphics.fillRect(x + 2, y + 2, wInset, hInset);
			}
			// else
			// {
			// if (ovals)
			// graphics.drawOval(x, y, w, h);
			// else
			// graphics.drawRect(x, y, w, h);
			// }

			cVI.xFeat = x;
			cVI.yFeat = y;
			cVI.width = w;
			cVI.height = h;
		}

		if (dragRect != null)
		{
			// System.out.println("drawing: " + dragRect.toString());
			graphics.setColor(Color.black);
			graphics.drawRect(dragRect.x, dragRect.y, dragRect.width,
					dragRect.height);
		}
	}

	public void kludgyMultiDimensionalDraw(int x, int y, int w, int h,
			ChunkVisInfo cVI, int featNum, Graphics graphics)
	{
		int numElements = elementsPerFeature[featNum];

		int elementNum = numElements - 1;

		for (int i = 0; i < featNum; i++)
		{
			elementNum += elementsPerFeature[i];
		}

		// int bottom = (elementNum - numElements) + 1;

		double yIncr = (double) h / numElements;

		// System.out.println("x: " + x + " Oy: " + y + " w: " + w + " h: " + h
		// +
		// " featNum: " + featNum + " numElements: " + numElements +
		// " elementNum: " + elementNum + " bottom: " + bottom + " yIncr: " +
		// yIncr);

		// for (; elementNum >= bottom; elementNum--)
		for (int i = 0; i < elementNum; i++)
		{
			int[] fN = { i };
			double featureValue = cVI.getFeatures(fN)[0];

			featureValue -= lowestFeatureValue[featNum];

			double colorIndex = featureValue * colorMultipliers[featNum];

			Color c = colormap.table[(int) colorIndex];

			if (!cVI.selected)
				c = c.darker().darker().darker();

			graphics.setColor(c);

			// int n = numElements - i;

			int yLocal = (int) ((y + h) - (i * yIncr));

			int miniBlipHeight = (int) (Math.round(yIncr));
			if (miniBlipHeight == 0)
				miniBlipHeight = 1;

			// System.out.println("eN: " + elementNum + " n: " + n + " x: " + x
			// + " yLocal: " + yLocal + " w: " + w + " miniBlipHeight: " +
			// miniBlipHeight);

			graphics.fillRect(x, yLocal, w, miniBlipHeight);

		}

	}

	public Vector getChunkVisInfosForPoint(Point p)
	{
		Vector chunks = new Vector();

		for (int i = 0; i < events.size(); i++)
		{
			ChunkVisInfo c = (ChunkVisInfo) events.elementAt(i);

			if (p.x >= c.xFeat && p.x <= (c.xFeat + c.width))
			{
				if (p.y >= c.yFeat && p.y <= (c.yFeat + c.height))
				{
					chunks.add(c);
				}
			}
		}

		return chunks;
	}

	// doesn't make sense for scatter plot...
	// well, it sort of makes sense...
	// we return -1,-2,-3 for start time, dst time, length
	public int getFeatureNumberForPoint(Point p)
	{
		int which = cOptions.getSelectedIndex();

		// kludge!!!
		if (which < 3)
			which = -1 - which;
		else
			which -= 3;

		return which;
	}

	public String getFeatureNameForPoint(Point p)
	{
		int whichFeature = getFeatureNumberForPoint(p);

		if (whichFeature == -1)
			return optionBoxStrings[0];
		else if (whichFeature == -2)
			return optionBoxStrings[1];
		else if (whichFeature == -3)
			return optionBoxStrings[2];
		else if (whichFeature >= 0)
			return optionBoxStrings[whichFeature + 3];
		else
			return "i don't know!";
	}

	public double getFeatureValueForPoint(Point p)
	{
		double value = 0.0;

		for (int i = 0; i < events.size(); i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.elementAt(i);

			if (p.x >= cVI.xFeat && p.x <= (cVI.xFeat + cVI.width))
			{
				if (p.y >= cVI.yFeat && p.y <= (cVI.yFeat + cVI.height))
				{
					int whichFeature = cOptions.getSelectedIndex();

					if (whichFeature == 0)
						return cVI.startTime;
					else if (whichFeature == 1)
						return cVI.dstTime;
					else if (whichFeature == 2)
						return cVI.length;
					else if (whichFeature >= 3)
					{
						int which = whichFeature - 3;
						int[] featNum = { 0 };

						for (int j = 0; j < which; j++)
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
		cOptions.setSelectedIndex(featureSelector.getSelectedIndex());
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
			Rectangle chunkRect = new Rectangle(cVI.xFeat, cVI.yFeat,
					cVI.width, cVI.height);

			if (dragRect.intersects(chunkRect))
				cVI.selected = true;
		}

		dragRect = null;
		dragShift = false;

		drawingPanel.repaint();
	}

	public void actionPerformed(ActionEvent arg0)
	{
		super.actionPerformed(arg0);

		String command = arg0.getActionCommand();

		if (command.equals("sOptions"))
		{
			if (sOptions.getSelectedIndex() == 0)
				ovals = false;
			else
				ovals = true;
		}
		else if (command.equals("cOptions"))
		{
			// have to avoid a loop!
			if (featureSelector.getSelectedIndex() != cOptions
					.getSelectedIndex())
				featureSelector.setSelectedIndex(cOptions.getSelectedIndex());
			updateColorMultipliers();
		}
	}

	public void updateOptionBoxes()
	{
		xOptions.removeAllItems();

		for (int i = 0; i < optionBoxStrings.length; i++)
			xOptions.addItem(optionBoxStrings[i]);

		yOptions.removeAllItems();

		for (int i = 0; i < optionBoxStrings.length; i++)
			yOptions.addItem(optionBoxStrings[i]);

		cOptions.removeAllItems();

		for (int i = 0; i < optionBoxStrings.length; i++)
			cOptions.addItem(optionBoxStrings[i]);

		hOptions.removeAllItems();

		for (int i = 0; i < optionBoxStrings.length; i++)
			hOptions.addItem(optionBoxStrings[i]);

		wOptions.removeAllItems();

		for (int i = 0; i < optionBoxStrings.length; i++)
			wOptions.addItem(optionBoxStrings[i]);

		cOptions.removeAllItems();

		for (int i = 0; i < optionBoxStrings.length; i++)
			cOptions.addItem(optionBoxStrings[i]);
	}

	public void updateColorMultipliers()
	{
		super.updateColorMultipliers();

		int whichOption;// cOptions.getSelectedIndex();

		// first time through we don't have a gui yet...
		if (cOptions == null)
		{
			whichOption = 3;
		}
		else
			whichOption = cOptions.getSelectedIndex();

		if (whichOption == 0)
		{
			for (int i = 0; i < numFeatures; i++)
			{
				colorMultipliers[i] = (numColors - 1.0) / lastStartTime;
			}
		}
		else if (whichOption == 1)
		{
			for (int i = 0; i < numFeatures; i++)
			{
				colorMultipliers[i] = (numColors - 1.0) / lastStartTime;
			}
		}
		else if (whichOption == 2)
		{
			for (int i = 0; i < numFeatures; i++)
			{
				colorMultipliers[i] = (numColors - 1.0) / longestChunk;
			}
		}
		else if (whichOption >= 3)
		{
			int wO = whichOption - 3;

			for (int i = 0; i < numFeatures; i++)
			{
				double lowValue = lowestFeatureValue[wO]
						- lowestFeatureValue[wO];
				double highValue = highestFeatureValue[wO]
						- lowestFeatureValue[wO];

				featureValueSpan[i] = highValue - lowValue;
				colorMultipliers[i] = (numColors - 1.0) / featureValueSpan[i];
			}

		}

	}

	public JPanel buildGUI(Color bgColor)
	{
		JPanel panel = super.buildGUI(bgColor);

		// dataMappingControlsPanel = new JPanel();
		// dataMappingControlsPanel.setBackground(bgColor);

		JPanel xYPanel = new JPanel();
		xYPanel.setBackground(bgColor);
		xYPanel.setLayout(new BoxLayout(xYPanel, BoxLayout.Y_AXIS));

		JLabel xCL = new JLabel("x axis: ");
		xCL.setBackground(bgColor);
		xYPanel.add(xCL);

		updateOptionBoxStrings();

		xOptions = new JComboBox(optionBoxStrings);
		xOptions.setBackground(bgColor);
		xOptions.setActionCommand("xOptions");
		xOptions.addActionListener(this);
		xYPanel.add(xOptions);

		JLabel yCL = new JLabel("y axis: ");
		yCL.setBackground(bgColor);
		xYPanel.add(yCL);

		yOptions = new JComboBox(optionBoxStrings);
		yOptions.setSelectedIndex(1);
		yOptions.setBackground(bgColor);
		yOptions.setActionCommand("yOptions");
		yOptions.addActionListener(this);
		xYPanel.add(yOptions);

		// dataMappingControlsPanel.add(xYPanel);
		controlsPanel.add(xYPanel);

		JPanel hwPanel = new JPanel();
		hwPanel.setBackground(bgColor);
		hwPanel.setLayout(new BoxLayout(hwPanel, BoxLayout.Y_AXIS));

		JLabel hCL = new JLabel("height: ");
		hCL.setBackground(bgColor);
		hwPanel.add(hCL);

		hOptions = new JComboBox(optionBoxStrings);
		hOptions.setSelectedIndex(2);
		hOptions.setBackground(bgColor);
		hOptions.setActionCommand("hOptions");
		hOptions.addActionListener(this);
		hwPanel.add(hOptions);

		JLabel wCL = new JLabel("width: ");
		wCL.setBackground(bgColor);
		hwPanel.add(wCL);

		wOptions = new JComboBox(optionBoxStrings);
		wOptions.setSelectedIndex(2);
		wOptions.setBackground(bgColor);
		wOptions.setActionCommand("wOptions");
		wOptions.addActionListener(this);
		hwPanel.add(wOptions);

		// dataMappingControlsPanel.add(hwPanel);
		controlsPanel.add(hwPanel);

		JPanel cPanel = new JPanel();
		cPanel.setBackground(bgColor);
		cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.Y_AXIS));

		JLabel cCL = new JLabel("color:");
		cCL.setBackground(bgColor);
		cPanel.add(cCL);

		updateOptionBoxStrings();
		cOptions = new JComboBox(optionBoxStrings);
		// start with color = 1st feature
		cOptions.setSelectedIndex(3);
		cOptions.setBackground(bgColor);
		cOptions.setActionCommand("cOptions");
		cOptions.addActionListener(this);
		cPanel.add(cOptions);

		JLabel sCL = new JLabel("shape:");
		sCL.setBackground(bgColor);
		cPanel.add(sCL);

		String[] shapes = { "rects", "ovals" };
		sOptions = new JComboBox(shapes);
		sOptions.setBackground(bgColor);
		sOptions.setActionCommand("sOptions");
		sOptions.addActionListener(this);
		cPanel.add(sOptions);

		/*
		 * ovalBoxes = new JCheckBox("ovals"); ovalBoxes.setBackground(bgColor);
		 * ovalBoxes.setActionCommand("ovals");
		 * ovalBoxes.addActionListener(this); cPanel.add(ovalBoxes);
		 */
		// dataMappingControlsPanel.add(cPanel);
		controlsPanel.add(cPanel);

		// controlsPanel.add(dataMappingControlsPanel);

		return panel;
	}
}
