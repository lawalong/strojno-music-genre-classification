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
 * Created on Nov 17, 2006
 *
 */
package com.meapsoft.visualizer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author douglas
 */
public class DrawingPanel extends JPanel
{
	ActionListener actionListener;

	Renderer renderer;

	private BufferedImage image;

	private JScrollPane scroller;

	private int origW = -1;

	private int origH = -1;

	private double hZoom = 1.0;

	private double wZoom = 1.0;

	private double zoomIncrement = 0.25;

	boolean iJustZoomed = false;

	public DrawingPanel(Renderer renderer, ActionListener aL)
	{
		// setSize(800, 600);
		// setPreferredSize(new Dimension(800, 600));

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.actionListener = aL;
		this.renderer = renderer;
	}

	void setScroller(JScrollPane scroller)
	{
		this.scroller = scroller;
	}

	void setRenderer(Renderer renderer)
	{
		this.renderer = renderer;
		// System.out.println("using: " + this.visualizer.name);
		repaint();
	}

	public void setOrigWH(int w, int h)
	{
		origW = w;
		origH = h;
		image = (BufferedImage) createImage(w, h);
	}

	public void zoomIn()
	{
		iJustZoomed = true;

		// only zoom horizontally...
		// hZoom += zoomIncrement;
		wZoom += zoomIncrement;

		// zoom out vertically so we don't need a vertical scrollbar
		hZoom = 1
				- scroller.getHorizontalScrollBar().getPreferredSize()
						.getHeight() / origH;

		int w = (int) Math.round(origW * wZoom);
		int h = (int) Math.round(origH * hZoom);

		setSize(w, h);
		setPreferredSize(new Dimension(w, h));
		image = (BufferedImage) createImage(w, h);
		scroller.revalidate();

		// System.out.println("hZoom: " + hZoom + " wZoom: " + wZoom);
	}

	public void zoomOut()
	{
		if (hZoom == 1.0 || wZoom == 1.0)
			return;

		iJustZoomed = true;

		// only zoom horizontally...
		// hZoom -= zoomIncrement;
		wZoom -= zoomIncrement;

		// no scrollbar necessary anymore
		if (wZoom == 1.0)
			hZoom = 1.0;

		int w = (int) Math.round(origW * wZoom);
		int h = (int) Math.round(origH * hZoom);

		setSize(w, h);
		setPreferredSize(new Dimension(w, h));
		image = (BufferedImage) createImage(w, h);
		scroller.revalidate();

		// System.out.println("hZoom: " + hZoom + " wZoom: " + wZoom);
	}

	public void resetZoom()
	{
		iJustZoomed = true;

		hZoom = 1.0;
		wZoom = 1.0;

		int w = (int) Math.round(origW * wZoom);
		int h = (int) Math.round(origH * hZoom);

		setSize(w, h);
		setPreferredSize(new Dimension(w, h));
		image = (BufferedImage) createImage(w, h);
		scroller.revalidate();

		// System.out.println("w: " + w + " h: " + h);
	}

	public void paintComponent(Graphics g)
	{
		// System.out.println("pC...");
		// first time
		if (origW == -1)
		{
			origW = getWidth();
			origH = getHeight();
		}

		int w = (int) Math.round(origW * wZoom);
		int h = (int) Math.round(origH * hZoom);

		if (image == null)
		{
			// System.out.println("making new image!");
			image = (BufferedImage) createImage(w, h);
		}

		// System.out.println("gW: " + getWidth() + " gH: " + getHeight() +
		// " w: " + w + " h: " + h +
		// " origW: " + origW + " origH: " + origH);
		renderer.draw(image, w, h);
		g.drawImage(image, 0, 0, this);
	}
}
