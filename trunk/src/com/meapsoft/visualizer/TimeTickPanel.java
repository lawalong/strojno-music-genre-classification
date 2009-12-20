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

package com.meapsoft.visualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.meapsoft.disgraced.SegmenterPanel;

/**
 * included on a SingleFeaturePanel. Spawns timer to draw a bar on playback
 * 
 * @author Mike Sorvillo (ms3311@columbia.edu and the MEAP team
 */

public class TimeTickPanel extends JPanel implements ComponentListener
{

	// ///////////////////////////////////////////////////////////////////////////////////////////
	// //member variables
	// ///////////////////////////////////////////////////////////////////////////////////////////

	private static final long serialVersionUID = 1L;

	private SingleFeaturePanel m_kParentPanel = null; // singlefeaturepanel

	private boolean m_bPlaying = false; // are we playing back?

	private Timer m_kProgressTimer = null; // a timer to keep track of the
											// progress bar

	private int m_iPlaybackTime = 0; // the amount of time that elapsed in
										// the playback

	private double m_dProgressX = 0; // where the progress bars x value is

	private int lastX = 0; // keep track of the last X drawn

	private double m_dUpdatePerTick = 0; // how to much update per tick

	private final int m_iTimerTick = 10; // the amount the timer will tick

	// private boolean firstTime = true;

	// ///////////////////////////////////////////////////////////////////////////////////////////
	// // constructors
	// ///////////////////////////////////////////////////////////////////////////////////////////

	public TimeTickPanel(SingleFeaturePanel kParentPanel)
	{
		// save the parent panel
		m_kParentPanel = kParentPanel;

		// add us as a window state listener
		addComponentListener(this);

		// set us to not have a border
		setBorder(BorderFactory.createEmptyBorder());

		setOpaque(false);
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////
	// //paintComponent() - paint on us
	// ///////////////////////////////////////////////////////////////////////////////////////////
	public void paintComponent(Graphics g)
	{
		// only if we're playing, do all this
		if (m_bPlaying)
		{
			/*
			 * if (firstTime) { System.out.println("this.bounds: " +
			 * this.getBounds().toString() + " m_kParentPanel.bounds: " +
			 * m_kParentPanel.getBounds().toString());
			 * 
			 * firstTime = false; }
			 */
			// draw the progress line
			int iProgressX = (int) Math.ceil(m_dProgressX);
			// g2d.setColor(new Color(0.78f, 0.0f, 0.0f));
			g.setColor(Color.black);
			// g.setStroke(new BasicStroke(1));
			g.drawLine(iProgressX, 0, iProgressX, (int) getSize().getHeight());

			// uncomment this to see our weird offset...somehow our bounds is
			// smaller than and offset from
			// our parent panel's bounds.
			g.drawRect(0, 0, getWidth(), getHeight());
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////
	// // play() - start the playback
	// ///////////////////////////////////////////////////////////////////////////////////////////
	public void play()
	{
		// flag us first
		m_bPlaying = true;

		// move our progress x back to the beginning, and move the playback time
		// to the start time
		lastX = 0;
		m_dProgressX = 0;
		m_iPlaybackTime = 0;

		// calculate how much to update per tick
		double dDuration = m_kParentPanel.getTimeRange();
		m_dUpdatePerTick = (m_iTimerTick * m_kParentPanel.getWidth())
				/ (dDuration * 1000);

		// System.out.println("width(): " + m_kParentPanel.getWidth() + "
		// dDuration: " + dDuration + " m_dUpdatePerTick: " + m_dUpdatePerTick);

		// create the task of this playback
		TimerTask kProgressTask = new TimerTask()
		{
			public void run()
			{
				// calc the time played so far
				double dTimePlayed = (double) (m_iPlaybackTime / 1000.0);

				// dont let it go past the playback time
				if (dTimePlayed < m_kParentPanel.getTimeRange())
				{
					// only call repaint() if we've actually moved...
					if ((int) Math.ceil(m_dProgressX) > lastX)
					{
						repaint();
						lastX = (int) Math.ceil(m_dProgressX);
					}
					// increment the playback time and our progress
					m_iPlaybackTime += m_iTimerTick;
					m_dProgressX += m_dUpdatePerTick;
				}
				else
				{
					stop();
				}
			}
		};

		// create a timer and schedule to update every 10ms
		m_kProgressTimer = new Timer();
		m_kProgressTimer.scheduleAtFixedRate(kProgressTask, 0, m_iTimerTick);
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////
	// // stop() - stops the playback
	// ///////////////////////////////////////////////////////////////////////////////////////////
	public void stop()
	{
		// flag us
		m_bPlaying = false;

		// cancel the timer
		m_kProgressTimer.cancel();

		// repaint us
		repaint();
	}

	public void componentHidden(ComponentEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void componentMoved(ComponentEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void componentResized(ComponentEvent e)
	{
		// set our size here
		this.setSize(m_kParentPanel.getWidth(), m_kParentPanel.getHeight());

		// recalculate the update per tick here
		double dDuration = m_kParentPanel.getTimeRange();
		m_dUpdatePerTick = (m_iTimerTick * m_kParentPanel.getWidth())
				/ (dDuration * 1000);

		// figure out how long we've been playing
		double dTimePlayed = (double) (m_iPlaybackTime / 1000.0);
		double perecentComplete = dTimePlayed / m_kParentPanel.getTimeRange();

		// also recalc where our x should be here
		m_dProgressX = perecentComplete * m_kParentPanel.getWidth();
	}

	public void componentShown(ComponentEvent e)
	{
		// TODO Auto-generated method stub

	}

}
