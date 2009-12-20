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

package com.meapsoft.disgraced;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.meapsoft.FeatFile;
import com.meapsoft.MEAPUtil;

/**
 * A panel containing a graphical visualization of a matrix of data.
 * Like Matlab's imagesc() command
 *
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class DataDisplayPanel extends JPanel implements ActionListener, MouseMotionListener, MouseListener, Runnable
{
    // constants
    private static final float VZOOM_INCR = 2;
    private static final float HZOOM_INCR = 2;
    private static final float MIN_VZOOM = 0.03125f;
    private static final float MIN_HZOOM = 0.03125f;
    private static final float MAX_VZOOM = 16;
    private static final float MAX_HZOOM = 8;
    // Actions performed by various buttons
    private static final String ZOOM_RESET = "zoom_reset";
    private static final String ZOOM_IN = "zoom_in";
    private static final String ZOOM_OUT = "zoom_out";
    private static final String ZOOM_V = "zoom_v";
    private static final String ZOOM_H = "zoom_h";

    // Matrix of data to be displayed
    protected double[][] data;
    // title of the JFrame created by run()
    protected String windowTitle = "data";

    // GUI components
    private SpectrogramPanel dataPanel;
    private JScrollPane scroller;
    private JLabel statusbar;
    private JToolBar toolbar;
    private JToggleButton button_zoom_in;
    private JToggleButton button_zoom_out;

    // states to keep track of
    private float currHZoom = 1.0f;
    private float currVZoom = 1.0f;
    // are we in the zoomIn state or zoomOut? flag controlled by zoom
    // buttons on the menu bar
    private boolean zoomIn = true;
    private boolean hZoom = true;
    private boolean vZoom = false;
    // keep track of where zoom started when dragging a zoombox
    //private Point zoomStart = null;
    

    public DataDisplayPanel(double[][] d)
    {
        super();

        data = d;
    }

    public DataDisplayPanel(double[][] d, String title)
    {
        this(d);

        windowTitle = title;
    }


    public void setup() 
    {
        buildGUI();
    }

    private void buildGUI()
    {
        dataPanel = new SpectrogramPanel(data);

        setLayout(new BorderLayout());

        //TODO:
        // _ still need to set up axes.  and colorbar zooming
        // X want status bar on the bottom that lists value in data matrix
        //   at point indicated by mouse location 
        // X want to be able to do proper vertical scaling (automatically
        //   fill the window unless vertical scroll is set manually (then
        //   let scrollbar take over)
        // X zooming and scrolling don't play nice together yet -  Fixed in SpectrogramPanel.zoom 
        // C need to find out how to get the current visible size of the
        //   window and set up actionlisteners/whatever to update things

	    scroller = new JScrollPane(dataPanel);
        add(scroller, BorderLayout.CENTER);
        //add(hAxis, BorderLayout.WEST);
        //add(dataPanel.getColorBar(), BorderLayout.EAST);


        // tool bar
        toolbar = new JToolBar(JToolBar.HORIZONTAL);
        add(toolbar, BorderLayout.PAGE_START);
        // keep it anchored at the top
        toolbar.setFloatable(false);

        // toolbar buttons
        toolbar.add(new JLabel("Zoom:"));
        toolbar.addSeparator();

        JToggleButton tbutton = new JToggleButton("h", hZoom);
        tbutton.setActionCommand(ZOOM_H);
        tbutton.addActionListener(this);
        toolbar.add(tbutton);

        tbutton = new JToggleButton("v", vZoom);
        tbutton.setActionCommand(ZOOM_V);
        tbutton.addActionListener(this);
        toolbar.add(tbutton);
        toolbar.addSeparator();

        tbutton = new JToggleButton("in", zoomIn);
        tbutton.setActionCommand(ZOOM_IN);
        tbutton.addActionListener(this);
        toolbar.add(tbutton);
        button_zoom_in = tbutton;

        tbutton = new JToggleButton("out", !zoomIn);
        tbutton.setActionCommand(ZOOM_OUT);
        tbutton.addActionListener(this);
        toolbar.add(tbutton);
        button_zoom_out = tbutton;
        toolbar.addSeparator();

        JButton button = new JButton("reset");
        button.setActionCommand(ZOOM_RESET);
        button.addActionListener(this);
        toolbar.add(button);


        // status bar
        statusbar = new JLabel(windowTitle);
        add(statusbar, BorderLayout.SOUTH);
        dataPanel.addMouseMotionListener(this);

        // Mouse listener to zoom in/out on mouse clicks
        dataPanel.addMouseListener(this);

        // set the default size and reset the display
        this.setPreferredSize(new Dimension(500, 300));
        actionPerformed(new ActionEvent(button, 0, ZOOM_RESET));
    }


    public void actionPerformed(ActionEvent e) 
    {
        //String cmd = ((AbstractButton)e.getSource()).getActionCommand();
        String cmd = e.getActionCommand();
            
        if(cmd.equals(ZOOM_IN))
        {
            zoomIn = true;
            button_zoom_in.setSelected(true); 
            button_zoom_out.setSelected(false); 
        }
        else if(cmd.equals(ZOOM_OUT))
        {
            zoomIn = false;
            button_zoom_in.setSelected(false);
            button_zoom_out.setSelected(true);
        }
        else if(cmd.equals(ZOOM_RESET))
        {
            float h = (float)(this.getPreferredSize().getHeight() 
                              - scroller.getHorizontalScrollBar()
                                        .getPreferredSize().getHeight()
                              - statusbar.getPreferredSize().getHeight()
                              - toolbar.getPreferredSize().getHeight());
            // no, this should not be hardcoded (the 3 especially), but I
            // don't know how else to properly set the initial zoom so
            // that no vertial scrollbar is required
            boolean vz = vZoom, hz = hZoom;
            vZoom = true;  hZoom = true;
            zoomDataPanel(1, (h-3)/(float)dataPanel.getDataHeight());
            vZoom = vz;  hZoom = hz;
        }
        else if(cmd.equals(ZOOM_V))
            vZoom = !vZoom;
        else if(cmd.equals(ZOOM_H))
            hZoom = !hZoom;
    }

    public void mouseMoved(MouseEvent e)
    { 
        int x = (int)(e.getX()/currHZoom);
        // y axis is flipped (axis xy) 
        int y = (int)dataPanel.getDataHeight()-(int)(e.getY()/currVZoom)-1;

        if(x < dataPanel.getDataWidth() && x >= 0 
           && y < dataPanel.getDataHeight() && y >= 0)   
            //statusbar.setText(featFile.filename + ": features(" + x + "," + y 
            statusbar.setText("features(" + x + "," + y 
                           + ") = " + dataPanel.getData(x,y));
    }

    public void mouseDragged(MouseEvent e) { }

    public void mouseClicked(MouseEvent e) 
    { 
        boolean zoomin = zoomIn && e.getButton() == MouseEvent.BUTTON1 
            || !zoomIn && e.getButton() == MouseEvent.BUTTON3;
        if(zoomin)
            zoomDataPanel(currHZoom*HZOOM_INCR, currVZoom*VZOOM_INCR);
        else if(!zoomin)
            zoomDataPanel(currHZoom/HZOOM_INCR, currVZoom/VZOOM_INCR);
    }

    // take care of dragging a zoom rectangle and whatnot here
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }

    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }


    private void zoomDataPanel(float h, float v)
    {
        if(hZoom)
        {
            if(h > MAX_HZOOM)
                h = MAX_HZOOM;
            else if(h < MIN_HZOOM)
                h = MIN_HZOOM;

            currHZoom = h;
            dataPanel.hzoomSet(currHZoom);
        }
        if(vZoom)
        {
            if(v > MAX_VZOOM)
                v = MAX_VZOOM;
            else if(v < MIN_VZOOM)
                v = MIN_VZOOM;

            currVZoom = v;
            dataPanel.vzoomSet(currVZoom);
        }
    }

    /**
     * Launch this DataDisplayPanel in its own JFrame.
     */
    public void run()
     {
         JFrame jframe = new JFrame(windowTitle);
         jframe.setContentPane(this);
         jframe.pack();
         jframe.setVisible(true);
    }

    public static void spawnWindow(double[][] d)
    {
        spawnWindow(d, "data");
    }

    public static void spawnWindow(double[][] d, String title)
    {
        DataDisplayPanel p = new DataDisplayPanel(d, title);

        p.setup();

        p.run();
    }

    public static void main(String[] args)
    {
        // parse arguments
        int[] featdim = MEAPUtil.parseFeatDim(args,"i:"); 
        String filename = args[args.length-1];

        FeatFile f = new FeatFile(filename);

        try
        {
            f.readFile();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        
        DataDisplayPanel p = new DataDisplayPanel(f.getFeatures(featdim), filename);

        JFrame jframe = new JFrame(p.getClass().getName() + ": " + filename);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setContentPane(p);
        jframe.pack();
        jframe.setVisible(true);
    }
}

