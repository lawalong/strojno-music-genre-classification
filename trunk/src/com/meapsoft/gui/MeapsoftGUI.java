/*
 * MeapGUI.java
 *
 * Created on October 27, 2007, 3:31 PM
 */

package com.meapsoft.gui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BoundedRangeModel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.meapsoft.AudioReader;
import com.meapsoft.AudioReaderFactory;
import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatFile;
import com.meapsoft.MEAPUtil;
import com.meapsoft.Synthesizer;
import com.meapsoft.composers.BlipComposer;
import com.meapsoft.visualizer.SingleFeaturePanel;
import com.meapsoft.visualizer.SingleFeatureWaveformPanel;
import com.meapsoft.visualizer.Visualizer;

/**
 *
 * @author  Mike
 */
public class MeapsoftGUI extends javax.swing.JFrame implements Runnable, LineListener
{
    //our thread
    private Thread mRunThread = null;

    //our tabs
    private SegmenterPanel mSegmenterPanel;
    private ExtractPanel mExtractPanel;
    private ComposePanel mComposePanel;

    //our dialogs
    private SettingsDialog mSettingsDialog = null;
    
    //IO data
    public static String mDataDirectory;
    public static String mMeapsoftDirectory;
    
    //our background color
    private boolean mColorEnabled = true;
    private Color mBkgColor = null;
    
    //the color timer
    private Timer mColorTimer = null;
    private boolean mFlashColors = true;
    
    //stuff for our play controls
    private Thread mPlayThread = null;
    private Synthesizer mSynth = null; 
    
    //flags for things we've done already
    private boolean mVisualized = false;
    private boolean mSegmentedOnce = false;
    private boolean mExtractedOnce = false;
    private boolean mComposedOnce = false;


    
    /** Creates new form MeapGUI */
    public MeapsoftGUI()
    {
        initComponents();
        
        //initialize the gui
        initGui();
    }
    
////////////////////////////////////////////////////////////////////////
//getters and setters
////////////////////////////////////////////////////////////////////////
    
    public SegmenterPanel getSegmenterPanel()
    {
    	return mSegmenterPanel;
    }
    
    public ExtractPanel getExtractPanel()
    {
    	return mExtractPanel;
    }
    
    public ComposePanel getComposePanel()
    {
    	return mComposePanel;
    }
    
    public VisualPreviewPanel getPreviewPanel()
    {
        return mPreviewPanel;
    }
    
    public boolean shouldProcessVisualPreview()
    {
    	return mVisualizeChk.isSelected();
    }
    
    public void setVisualPreviewVisible(boolean flag)
    {
        if(flag)
        	mVisualized = true;
    
        mPreviewPanel.setVisible(flag);
    }
    
    public void setLaunchButtonEnabled(boolean flag)
    {
    	mGOBtn.setEnabled(flag);	
    }
    
    public Color getColor()
    {
    	return mBkgColor;
    }
    
    public boolean isColorEnabled()
    {
    	return mColorEnabled;
    }
    
    public void setColorEnabled(boolean flag)
    {
    	mColorEnabled = flag;
    }
    
    public void setFlashColors(boolean flag)
    {
    	mFlashColors = flag;
    }
           
    
////////////////////////////////////////////////////////////////////////
// updateProgressBar() - does what it says
////////////////////////////////////////////////////////////////////////
    public void updateProgressBar(String txt, BoundedRangeModel model)
    {
    	//flash the colors everytime we update this bar
    	flashColors();
    	
    	mProgressLbl.setText(txt);
    	mProgressBar.setStringPainted(true);
    	mProgressBar.setModel(model);
    }
    
////////////////////////////////////////////////////////////////////////
// initGui() - initializes the gui
////////////////////////////////////////////////////////////////////////
    private void initGui()
    {
    	//set the title
    	setTitle("Meapsoft - "+ MEAPUtil.version);
    	
        //set the path here
        String paths[] = MEAPUtil.getPaths();
        if (paths != null)
        {
                mMeapsoftDirectory = paths[0];
                mDataDirectory = paths[1];
        }
        else
                System.exit(-1);
		
        //make the save button disabled for now
        mSaveBtn.setEnabled(false);
        
        //now create all our tabs
        mSegmenterPanel = new SegmenterPanel(this);
        mExtractPanel = new ExtractPanel(this);
        mComposePanel = new ComposePanel(this);
        
        //add the tabs
        mTabBase.add(mSegmenterPanel, "Segment");
        mTabBase.add(mExtractPanel, "Extract Features");
        mTabBase.add(mComposePanel, "Compose");
        
        //init the files for all the panels
        initFilesForPanels();
        
        //init preview panel
        mPreviewPanel.setVisible(false);
        mPreviewPanel.setMainScreen(this);
        mPreviewPanel.initPreviewTabs();
        
        //create the dialogs
        mSettingsDialog = new SettingsDialog(this);
        mSettingsDialog.setVisible(false);
        
        //make the progress label have no text
        mProgressLbl.setText("");
        
        //add a listener on the progress bar
        mProgressBar.addChangeListener(new ChangeListener()
        {
        	public void stateChanged(ChangeEvent e) 
        	{
        		if(mProgressBar.getValue() == mProgressBar.getMaximum())
        		{
        			//reset all the progress bar
        			mProgressBar.setValue(0);
        			mProgressBar.setStringPainted(false);
        			mProgressLbl.setText("Processing complete");
        			
        			//cancel the flashing color timer
        			if(mFlashColors)
        				mColorTimer.cancel();
        		}
        	}
        });
        
        //init play controls
        mPlayInputBtn.setActionCommand("play");
        mPlayMeapedBtn.setActionCommand("play");
        mPlayMeapedBtn.setEnabled(false);
        
        mWithBlipsChk.setSelected(false);
        mWithBlipsChk.setEnabled(false);
        
        //initialize the colors
        initColors();
    }
    
////////////////////////////////////////////////////////////////////////
// initFilesForPanels() - inits the files for all the panels
////////////////////////////////////////////////////////////////////////
    
    public void initFilesForPanels()
    {
        mSegmenterPanel.initSegmentFile();
        mExtractPanel.initFeatFile();
		mComposePanel.initEDLFile();	
    }
    
////////////////////////////////////////////////////////////////////////
// initColors() - initializes colors for entire application
////////////////////////////////////////////////////////////////////////
    public void initColors()
    {
    	//init the color of all these panels, pass our flag in
    	mSegmenterPanel.initColor(mColorEnabled);
    	mExtractPanel.initColor(mColorEnabled);
    	mComposePanel.initColor(mColorEnabled);
    	
    	//set our background color
    	mBkgColor = mColorEnabled ? GUIUtils.getRandomColor() : null;
    	getContentPane().setBackground(mBkgColor);
    		    	
        //set the color of the tab base and tabs
    	mTabBase.setBackground(mBkgColor);
        mTabBase.setBackgroundAt(0, mSegmenterPanel.getColor());
        mTabBase.setBackgroundAt(1,  mExtractPanel.getColor());
        mTabBase.setBackgroundAt(2, mComposePanel.getColor());
        
        //set the colors for all these tabs and their components
        GUIUtils.initContainerColor(mSegmenterPanel, mSegmenterPanel.getColor());
        GUIUtils.initContainerColor(mExtractPanel, mExtractPanel.getColor());
        GUIUtils.initContainerColor(mComposePanel, mComposePanel.getColor());

        //set the color of the other panels
        GUIUtils.initContainerColor(mPreviewPanel, mBkgColor);
        GUIUtils.initContainerColor(mLaunchPanel, mBkgColor);

        //init the colors for our composer subpanels
        mComposePanel.initSettingPanelColors(mColorEnabled);
        
        //also init colors for our visual preview tabs
        mPreviewPanel.initColors(mColorEnabled);
        
        //set the settings panel
        mSettingsDialog.initColors(mColorEnabled);
    }
    
////////////////////////////////////////////////////////////////////////
 // flashColors() - flashes colors when we are running
 ////////////////////////////////////////////////////////////////////////
     public void flashColors()
     {
    	 //if we arent flashing colors, just return
    	 if(!mFlashColors)
    		 return;
    		 
    	 if(mColorTimer != null)
    		 mColorTimer.cancel();
    	 
         //create the task for a timer tick
         TimerTask task = new TimerTask()
         {
             public void run()
             {
            	 initColors();
            	 repaint();
             } 
         };
    	 
         //schedule this timer
    	 mColorTimer = new Timer();
    	 mColorTimer.scheduleAtFixedRate(task, 0, 50);
     }
     
////////////////////////////////////////////////////////////////////////
// update() - called for line listeners
////////////////////////////////////////////////////////////////////////
     public void update(LineEvent evt) 
 	{
 		//change the button based on the event here
 		if(evt.getType().equals(LineEvent.Type.START))
 		{
 			mPlayInputBtn.setText("Stop Original");
 			mPlayInputBtn.setActionCommand("stop");
 			
 			mPlayMeapedBtn.setText("Stop Meaped");
 			mPlayMeapedBtn.setActionCommand("stop");	
 		}
 		else if(evt.getType().equals(LineEvent.Type.STOP))
 		{
 			mPlayInputBtn.setText("Play Original");
 			mPlayInputBtn.setActionCommand("play");	
 			
 			mPlayMeapedBtn.setText("Play Meaped");
 			mPlayMeapedBtn.setActionCommand("play");
 			
 			//enable the buttons
 			mPlayInputBtn.setEnabled(true);
 			
 			//if we've composed once, reenable this button
 			if(mComposedOnce)
 			{
 				mPlayMeapedBtn.setEnabled(true);
 			}
 		}
 	} 
    
////////////////////////////////////////////////////////////////////////
// playFile() - plays original and meaped files (with and without blips)
////////////////////////////////////////////////////////////////////////
     
     public void playFile(boolean shouldPlay, boolean playOriginal)
     {
    	//if we're playing, play it
         if(shouldPlay)
         {
         	//disable the other play button
        	 if(playOriginal)
        	 {
        		 mPlayMeapedBtn.setEnabled(false);
        	 }
        	 else
        	 {
        		 mPlayInputBtn.setEnabled(false); 
        	 }
         	
            //create an EDL file to work with
            EDLFile edl = new EDLFile("null");
         	
             //if we are blipping, create a blip composer
 	    	if(mWithBlipsChk.isSelected())
 	    	{
 	    		//see which feat file we should use
 	    		FeatFile file = playOriginal ? BasePanel.segmentFile : BasePanel.edlFile;
 	    		
                BlipComposer b = new BlipComposer(file, edl);
                b.setBlipWav(BasePanel.dataDirectory + BasePanel.slash + "blip.wav");
                b.compose();
 	    	}
 	    	else
 	    	{
 	    		//set this file as read already
 	    		edl.haveReadFile = true;
 	    		
 	    		//create the output format here
 	    		AudioFormat outputFormat = new AudioFormat(44100, MEAPUtil.bitsPerSamp, 2, MEAPUtil.signed, MEAPUtil.bigEndian);
 	    		
 	    		try 
 	    		{
 					//see which file we should be playing here
 					String fileName = playOriginal ? BasePanel.inputSoundFileNameFull : BasePanel.outputSoundFileNameFull;
 	    			
 	    			//divide length by the framerate (thanks ron!)
 					AudioReader r = AudioReaderFactory.getAudioReader(fileName, outputFormat);
 					double length = r.getFrameLength() / r.getFormat().getFrameRate();
 					
 		    		//create a big chunk and add it
 		    		EDLChunk c = new EDLChunk(fileName, 0.0, length, 0.0);
 		    		edl.chunks.add(c);
 	    		} 
 	    		catch (Exception e) 
 	    		{
 					e.printStackTrace();
 				}
 	    	}
 	    	
             //create a synth from this edl file
             mSynth = new Synthesizer(edl, null);
             
             //are we being visualized?
 	    	if(mVisualized)
 	    	{
 	    		//if the original, add the waveform as listener
 	    		if(playOriginal)
 	    		{
 	    			SingleFeaturePanel wave = mPreviewPanel.getVisualPanel("Waveform");
 	    			mSynth.addLineListener(wave);
 	    		}
 	    		else
 	    		{
 	    			
 	    		}
 	    	}
 	    	
 	    	//add us as a line listener too
             mSynth.addLineListener(this);
             
             //if we have a thread, stop it. LEGACY CODE. unsafe
             if(mPlayThread != null)
             {
             	mPlayThread.interrupt();
             	mPlayThread.stop();
             }
             
             //start this guy up
             mPlayThread = new Thread(mSynth, "synthesizer");
             mPlayThread.start();
         }
         
         //if we aren't playing, stop it
         else
         {
         	//if we've composed before, enable this button
         	if(mComposedOnce && playOriginal)
         	{
         		mPlayMeapedBtn.setEnabled(true);
         	}
         	else
         	{
         		mPlayInputBtn.setEnabled(true);
         	}
         	
         	//stop the synth
         	if(mSynth != null)
         	{
         		mSynth.stop();	
         	}
         } 
     }
     
////////////////////////////////////////////////////////////////////////
// copyFile() - just a wrapper around the meap util one
////////////////////////////////////////////////////////////////////////
      public void copyFile(File src, File dst) 
      {
  		try 
		{
			MEAPUtil.copyFile(src, dst);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
      }
     
////////////////////////////////////////////////////////////////////////
// run() - starts this thread
////////////////////////////////////////////////////////////////////////
    public void run() 
    {
        //our error variable
        int error = 0;
                
        //reset our visualized flag
        mVisualized = false;
        
        //see what we need to do here
        if(mSegmentChkBox.isSelected() && error == 0)
        {
            error += mSegmenterPanel.run();
            mSegmentedOnce = true;
            
            //enable the with blips btn
            mWithBlipsChk.setEnabled(true);
        }
        if(mExtractChkBox.isSelected() && error == 0)
        {
            error += mExtractPanel.run();
            mExtractedOnce = true;
        }        
        if(mComposeChkBox.isSelected() && error == 0)
        {
            error += mComposePanel.run();
            mComposedOnce = true;
            
            //enable the play meaped button
            mPlayMeapedBtn.setEnabled(true);
        }
        
        //if we don't have an error here, enable this button
        if(error == 0)
        {
        	mGOBtn.setEnabled(true);
        	mPlayInputBtn.setEnabled(true);
        } 	
        
        //if we did something here, enable the save btn
        if(mSegmentedOnce || mExtractedOnce || mComposedOnce)
        {
        	mSaveBtn.setEnabled(true);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mTabBase = new javax.swing.JTabbedPane();
        mLaunchPanel = new javax.swing.JPanel();
        mSegmentChkBox = new javax.swing.JCheckBox();
        mExtractChkBox = new javax.swing.JCheckBox();
        mComposeChkBox = new javax.swing.JCheckBox();
        mGOBtn = new javax.swing.JButton();
        mSaveBtn = new javax.swing.JButton();
        mProgressLbl = new javax.swing.JLabel();
        mProgressBar = new javax.swing.JProgressBar();
        mVisualizeChk = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        mPlayInputBtn = new javax.swing.JButton();
        mPlayMeapedBtn = new javax.swing.JButton();
        mWithBlipsChk = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        mLaunchVisualizerButton = new javax.swing.JButton();
        mPreviewPanel = new com.meapsoft.gui.VisualPreviewPanel();
        mMenuBar = new javax.swing.JMenuBar();
        mFileMenu = new javax.swing.JMenu();
        mAboutMnuItem = new javax.swing.JMenuItem();
        mSettingsMnuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        mExitMnuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mLaunchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "MEAP!"));

        mSegmentChkBox.setSelected(true);
        mSegmentChkBox.setText("Segment");
        mSegmentChkBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mExtractChkBox.setSelected(true);
        mExtractChkBox.setText("Extract Features");
        mExtractChkBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mComposeChkBox.setSelected(true);
        mComposeChkBox.setText("Compose");
        mComposeChkBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mGOBtn.setText("GO");
        mGOBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mGOBtnActionPerformed(evt);
            }
        });

        mSaveBtn.setText("Save Files");
        mSaveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSaveBtnActionPerformed(evt);
            }
        });

        mProgressLbl.setText("Task Progress");

        mVisualizeChk.setSelected(true);
        mVisualizeChk.setText("Show previews");
        mVisualizeChk.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mVisualizeChk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mVisualizeChkActionPerformed(evt);
            }
        });

        jLabel2.setForeground(new java.awt.Color(0, 0, 255));
        jLabel2.setText("Playback");

        mPlayInputBtn.setText("input sound");
        mPlayInputBtn.setActionCommand("play input sound");
        mPlayInputBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mPlayInputBtnActionPerformed(evt);
            }
        });

        mPlayMeapedBtn.setText("MEAPed sound");
        mPlayMeapedBtn.setActionCommand("play MEAPed sound");
        mPlayMeapedBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mPlayMeapedBtnActionPerformed(evt);
            }
        });

        mWithBlipsChk.setSelected(true);
        mWithBlipsChk.setText("Play with blips");
        mWithBlipsChk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mWithBlipsChkActionPerformed(evt);
            }
        });

        jLabel1.setForeground(new java.awt.Color(0, 0, 255));
        jLabel1.setText("Visualizer");

        mLaunchVisualizerButton.setText("Launch visualizer");
        mLaunchVisualizerButton.setToolTipText("Launch visualizer");
        mLaunchVisualizerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mLaunchVisualizerButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout mLaunchPanelLayout = new org.jdesktop.layout.GroupLayout(mLaunchPanel);
        mLaunchPanel.setLayout(mLaunchPanelLayout);
        mLaunchPanelLayout.setHorizontalGroup(
            mLaunchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mLaunchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(mProgressLbl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
            .add(mLaunchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mSegmentChkBox)
                .addContainerGap(78, Short.MAX_VALUE))
            .add(mLaunchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mComposeChkBox)
                .addContainerGap(73, Short.MAX_VALUE))
            .add(mLaunchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mExtractChkBox)
                .addContainerGap(31, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mLaunchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mLaunchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mGOBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                    .add(mSaveBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
                .addContainerGap())
            .add(mLaunchPanelLayout.createSequentialGroup()
                .add(14, 14, 14)
                .add(mLaunchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mLaunchPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(mVisualizeChk))
                    .add(jLabel1)
                    .add(mLaunchPanelLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mPlayInputBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mPlayMeapedBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                    .add(mLaunchPanelLayout.createSequentialGroup()
                        .add(mWithBlipsChk)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .add(mLaunchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mLaunchVisualizerButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        mLaunchPanelLayout.setVerticalGroup(
            mLaunchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mLaunchPanelLayout.createSequentialGroup()
                .add(mSegmentChkBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mExtractChkBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mComposeChkBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mGOBtn)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mSaveBtn)
                .add(18, 18, 18)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mPlayInputBtn)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mPlayMeapedBtn)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mWithBlipsChk)
                .add(18, 18, 18)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mVisualizeChk)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(mLaunchVisualizerButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 124, Short.MAX_VALUE)
                .add(mProgressLbl)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        mPreviewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Waveform and spectrum"));

        mFileMenu.setText("File");

        mAboutMnuItem.setText("About");
        mAboutMnuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAboutMnuItemActionPerformed(evt);
            }
        });
        mFileMenu.add(mAboutMnuItem);

        mSettingsMnuItem.setText("Settings");
        mSettingsMnuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSettingsMnuItemActionPerformed(evt);
            }
        });
        mFileMenu.add(mSettingsMnuItem);
        mFileMenu.add(jSeparator1);

        mExitMnuItem.setText("Exit");
        mExitMnuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mExitMnuItemActionPerformed(evt);
            }
        });
        mFileMenu.add(mExitMnuItem);

        mMenuBar.add(mFileMenu);

        setJMenuBar(mMenuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(mLaunchPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(mTabBase, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 725, Short.MAX_VALUE))
                    .add(mPreviewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 926, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mTabBase, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
                    .add(mLaunchPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mPreviewPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mLaunchPanel.getAccessibleContext().setAccessibleName("MEAP");
        mPreviewPanel.getAccessibleContext().setAccessibleName("Waveform and spectrum");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mAboutMnuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mAboutMnuItemActionPerformed
    {//GEN-HEADEREND:event_mAboutMnuItemActionPerformed
        
    	JOptionPane.showMessageDialog(this,
        		"Here is some information about MeapSOFT",
        		"About MeapSOFT",  
        		JOptionPane.INFORMATION_MESSAGE);
        
    }//GEN-LAST:event_mAboutMnuItemActionPerformed

    private void mSettingsMnuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mSettingsMnuItemActionPerformed
    {//GEN-HEADEREND:event_mSettingsMnuItemActionPerformed
        
    	GUIUtils.centerDialogInParent(mSettingsDialog);
    	mSettingsDialog.setVisible(true);
    	
    }//GEN-LAST:event_mSettingsMnuItemActionPerformed

    private void mExitMnuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mExitMnuItemActionPerformed
    {//GEN-HEADEREND:event_mExitMnuItemActionPerformed
       
    	System.exit(0);
    	
    }//GEN-LAST:event_mExitMnuItemActionPerformed

    private void mGOBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mGOBtnActionPerformed

    	//before we start this thread, disable these buttons
        mGOBtn.setEnabled(false);
        mPlayInputBtn.setEnabled(false);
        mPlayMeapedBtn.setEnabled(false);
        mSaveBtn.setEnabled(false);
        
        //create the thread and start it up
        mRunThread = new Thread(this, "MeapGUI.run");
        mRunThread.start();
        
}//GEN-LAST:event_mGOBtnActionPerformed

    private void mPlayInputBtnActionPerformed(java.awt.event.ActionEvent evt) 
    {//GEN-FIRST:event_mPlayOriginalBtnActionPerformed
     
    	if(evt.getActionCommand().equals("play"))
    	{
    		playFile(true, true);
    	}
    	else if(evt.getActionCommand().equals("stop"))
    	{
    		playFile(false, true);
    	}
        	
}//GEN-LAST:event_mPlayOriginalBtnActionPerformed

    private void mPlayMeapedBtnActionPerformed(java.awt.event.ActionEvent evt) 
    {//GEN-FIRST:event_mPlayMeapedBtnActionPerformed

    	if(evt.getActionCommand().equals("play"))
    	{
    		playFile(true, false);
    	}
    	else if(evt.getActionCommand().equals("stop"))
    	{
    		playFile(false, false);
    	}
    	
}//GEN-LAST:event_mPlayMeapedBtnActionPerformed
    
    private void mVisualizeChkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mVisualizeChkActionPerformed
        
        //if we've visualized, toggle visibility
        if(mVisualized)
        {
            mPreviewPanel.setVisible(mVisualizeChk.isSelected());
        }
        
    }//GEN-LAST:event_mVisualizeChkActionPerformed

    private void mSaveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSaveBtnActionPerformed
        
    	//prompt the user here
    	String inputValue = JOptionPane.showInputDialog(this, 
    						"What do you want to name the files?",
    						"Enter a name",
    						JOptionPane.QUESTION_MESSAGE);
        
    	//if we canceled, don't do anything
    	if(inputValue == null)
    		return;
    	
    	//get the path and all three file names
    	String path = BasePanel.dataDirectory + BasePanel.slash;
    	File oldSegFile = new File(path + BasePanel.outputSegmentsFileName);
    	File oldFeatFile = new File(path + BasePanel.outputFeaturesFileName);
    	File oldEdlFile = new File(path + BasePanel.outputEDLFileName);
    	File meapedWavFile = new File(BasePanel.outputSoundFileNameFull);
    	
    	//do we have a seg file?
    	if(oldSegFile.exists())
    	{
    		//create a new file instance
    		File newSegFile = new File(path + inputValue + ".seg");
    		copyFile(oldSegFile, newSegFile);
    	}
    	
    	//do we have a feat file?
    	if(oldFeatFile.exists())
    	{
    		//create a new file instance
    		File newFeatFile = new File(path + inputValue + ".feat");
    		copyFile(oldFeatFile, newFeatFile);
    	}
    	
    	//do we have an edl file?
    	if(oldEdlFile.exists())
    	{
    		//create a new file instance
    		File newEdlFile = new File(path + inputValue + ".edl");
    		copyFile(oldEdlFile, newEdlFile);
    	}
    	
    	//do we have a wave file?
    	if(meapedWavFile.exists())
    	{
    		//create a new file instance
    		File newMeapedWaveFile = new File(path + inputValue + ".wav");
    		copyFile(meapedWavFile, newMeapedWaveFile);
    	}
    	
    }//GEN-LAST:event_mSaveBtnActionPerformed

    private void mWithBlipsChkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mWithBlipsChkActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mWithBlipsChkActionPerformed

    private void mLaunchVisualizerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mLaunchVisualizerButtonActionPerformed
        // TODO add your handling code here:
        Visualizer visualizer = new Visualizer(mExtractPanel.featFile, mComposePanel.edlFile);
    }//GEN-LAST:event_mLaunchVisualizerButtonActionPerformed

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
    	//check to see if we are windows or now.
    	if(System.getProperty("os.name").startsWith("Windows"))
    	{
	        try
	        {
	        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        }
	        catch (Exception ex)
	        {
	            ex.printStackTrace();
	        }
    	}

        
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
            	MeapsoftGUI gui = new MeapsoftGUI();
            	GUIUtils.centerFrameOnScreen(gui);
            	gui.setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JMenuItem mAboutMnuItem;
    private javax.swing.JCheckBox mComposeChkBox;
    private javax.swing.JMenuItem mExitMnuItem;
    private javax.swing.JCheckBox mExtractChkBox;
    private javax.swing.JMenu mFileMenu;
    private javax.swing.JButton mGOBtn;
    private javax.swing.JPanel mLaunchPanel;
    private javax.swing.JButton mLaunchVisualizerButton;
    private javax.swing.JMenuBar mMenuBar;
    private javax.swing.JButton mPlayInputBtn;
    private javax.swing.JButton mPlayMeapedBtn;
    private com.meapsoft.gui.VisualPreviewPanel mPreviewPanel;
    private javax.swing.JProgressBar mProgressBar;
    private javax.swing.JLabel mProgressLbl;
    private javax.swing.JButton mSaveBtn;
    private javax.swing.JCheckBox mSegmentChkBox;
    private javax.swing.JMenuItem mSettingsMnuItem;
    private javax.swing.JTabbedPane mTabBase;
    private javax.swing.JCheckBox mVisualizeChk;
    private javax.swing.JCheckBox mWithBlipsChk;
    // End of variables declaration//GEN-END:variables
    
}
