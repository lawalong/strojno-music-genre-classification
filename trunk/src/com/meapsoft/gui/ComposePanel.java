/*
 * ComposePanel.java
 *
 * Created on October 28, 2007, 2:59 PM
 */

package com.meapsoft.gui;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.RTSI;

import com.meapsoft.MEAPUtil;
import com.meapsoft.Synthesizer;
import com.meapsoft.composers.Composer;
import com.meapsoft.gui.GUIUtils;
import com.meapsoft.gui.composers.ComposerSettingsPanel;
import com.meapsoft.gui.widgets.ComposerLabel;
import com.meapsoft.gui.widgets.ComposerListRenderer;
import com.meapsoft.gui.widgets.FeatureExtractorCheckBox;

/**
 * The panel for the Composers
 * @author  Mike
 */
public class ComposePanel extends BasePanel implements ListSelectionListener
{
    ////////////////////////////////////////////////////////////////////////
    // members variables
    ////////////////////////////////////////////////////////////////////////
	
    Vector<JLabel> mListModel = new Vector<JLabel>();		//a model for the list
	
    /** Creates new form ComposePanel */
    public ComposePanel(MeapsoftGUI mainScreen)
    {
    	//pass this up to our main screen
    	super(mainScreen);
    	
    	//init the netbeans components
        initComponents();
        
        //init the panel
        initPanel();
        
        //initialize the available composers here
        initComposers();
     }
    
    ////////////////////////////////////////////////////////////////////////
    // initialization methods
    ////////////////////////////////////////////////////////////////////////
    private void initComposers() 
    {
    	//sniff out the composers first
    	Vector composers = sniffForClasses("com.meapsoft.composers", "com.meapsoft.composers.Composer");
    	
    	//save a place for the default label here
    	ComposerLabel defaultLabel = null;
    	
    	//iterate and create our composer labels
        for(int i = 0; i < composers.size(); i++)
        {
        	//get the name
        	String name = composers.get(i).toString();
        	
        	//create the composer label here and add it
        	ComposerLabel lbl = new ComposerLabel(name, this);

        	//if it is the sort composer, select this value
        	if(lbl.getName().equals("Simple Sort"))
        	{
        		defaultLabel = lbl;
        	}
        	
        	//add this to the list model
        	mListModel.add(lbl);
        }
        
        //after we iterate, set the model
        mComposerList.setListData(mListModel);
        mComposerList.setCellRenderer(new ComposerListRenderer());
        
        //after we do this, set the selected value
        mComposerList.setSelectedValue(defaultLabel, false);
	}
    
    public void initPanel()
    {
    	//add us as a list selection listener
    	mComposerList.addListSelectionListener(this);
    	
    	//default some stuff to be invisible
    	mGainLbl.setVisible(false);
    	mGainValueTxt.setVisible(false);
    	mFadeLbl.setVisible(false);
    	mFadeSlider.setVisible(false);
    }
    
    ////////////////////////////////////////////////////////////////////////
    // initSettingPanelColors() - inits the colors for all these guys
    ////////////////////////////////////////////////////////////////////////
    public void initSettingPanelColors(boolean flag) 
    {
    	for(int i = 0; i < mListModel.size(); i++)
    	{
    		ComposerLabel lbl = (ComposerLabel)mListModel.get(i);
    		
    		Color panelColor = flag ? this.getColor() : null;
    		GUIUtils.initContainerColor(lbl.getSettingsPanel(), panelColor);
    	}
    }
    	
    ////////////////////////////////////////////////////////////////////////
    // valueChanged() - when the value gets changed
    ////////////////////////////////////////////////////////////////////////
	public void valueChanged(ListSelectionEvent e) 
	{
		//get the selected value here
		ComposerLabel lbl = (ComposerLabel)mComposerList.getSelectedValue();

		//set the description
		mDescriptionLbl.setText(lbl.getDescription());
		
		//remove all the old settings first
		mSettingsWrapper.removeAll();
		
		//get the settings panel 
		ComposerSettingsPanel settingsPanel = lbl.getSettingsPanel();
		
		//try to do all this here
		try
		{
			//make the settings label visible
			mSettingsLbl.setVisible(true);
			
			//add this new panel
			mSettingsWrapper.add(settingsPanel);
			
			//give this panel a size
			settingsPanel.setSize(500, 300);
			
			//if we don't have any components, get rid
			//of the settings label, so the user does
			//not think they made a mistake
			if(settingsPanel.getComponentCount() == 0)
			{
				mSettingsLbl.setVisible(false);
			}
		}
		catch(Exception ex)
		{
			System.out.println("\n\n\nBUMMER!\n\n\n");
			ex.printStackTrace();
			GUIUtils.ShowDialog(ex, lbl.getName() + " does not have a settings panel", GUIUtils.MESSAGE, mMainScreen);	
		}
		
		//do all our repainting here
		mSettingsWrapper.validate();
		mSettingsWrapper.invalidate();
		mSettingsWrapper.repaint();
	}
    
    ////////////////////////////////////////////////////////////////////////
    // run() - actually does all the work here
    ////////////////////////////////////////////////////////////////////////
    public synchronized int run()
    {
		//get the selected value here
		ComposerLabel lbl = (ComposerLabel)mComposerList.getSelectedValue();
	
		//get the settings panel
		ComposerSettingsPanel settingsPanel = lbl.getSettingsPanel();
		
		//create a composer to work with here
		Composer composer;
		
		//initialize the composer here
		//we should always have a settings panel for each composer
		try
		{
			//initialze the composer here
			int error = settingsPanel.initComposer();
			
			//if we have an error, return
			if(error == -1)
			{
				return -1;
			}
			
			composer = settingsPanel.getComposer();
		}
		catch(Exception e)
		{
			GUIUtils.ShowDialog(e, lbl.getName() + " does not have a settings panel", GUIUtils.MESSAGE, mMainScreen);	
			return -1;
		}

		//get the slider value
        double crossfade = (double)mFadeSlider.getValue()/1000;
        
        //see what options are selected
        if(mFadeChk.isSelected() & crossfade > 0)
        {
            composer.addCommand("fade("+crossfade+")");
        }
        if(mCrossFadeChk.isSelected() & crossfade > 0)
        {
            composer.addCommand("crossfade("+crossfade+")");
        }
        if(mReverseChk.isSelected())
        {
            composer.addCommand("reverse");
        }
        if(mGainChk.isSelected())
        {
        	double gain = Double.parseDouble(mGainValueTxt.getText());
        	composer.addCommand("gain(" + gain + ")");
        }

        //should we write the meap file?
        composer.writeMEAPFile = MEAPUtil.writeMEAPFile;
        
	    //update the progress bar
	    mMainScreen.updateProgressBar("Composing...", composer.getProgress());
        
	    //try to actually do the composing here
        try
        {
            composer.doComposer();
        }
        catch(Exception e)
        {
			GUIUtils.ShowDialog(e, "Error running composer", GUIUtils.MESSAGE, mMainScreen);
            return -1;
        }
        
        //now synthesize this guy
        int error = synthesize();
        
        if(error != 0)
        	return -1;
                   
        //return with no error
    	return 0;
    }
    
    ////////////////////////////////////////////////////////////////////////
    // synthesize() - called by run. synthesizes this guy
    ////////////////////////////////////////////////////////////////////////
    public int synthesize()
    {
        if (outputSoundFileNameFull == null)
        {
			GUIUtils.ShowDialog("You need to pick an output file!!!", GUIUtils.MESSAGE, mMainScreen);
            return -1;
        }
    
		Synthesizer synth = new Synthesizer(edlFile, outputSoundFileNameFull);
        synth.writeMEAPFile = MEAPUtil.writeMEAPFile;

	    //update the progress bar
	    mMainScreen.updateProgressBar("Synthesizing...", synth.getProgress());
        
        try
        {
            synth.doSynthesizer();
        }
        catch(Exception e)
        {
			GUIUtils.ShowDialog(e, "Error synthesizing audio file", GUIUtils.MESSAGE, mMainScreen);
            return -1;
        }
        
    	return 0;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        mComposerList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        mDetailsPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        mSettingsLbl = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        mDescriptionLbl = new javax.swing.JTextPane();
        mSettingsWrapper = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        mReverseChk = new javax.swing.JCheckBox();
        mFadeSlider = new javax.swing.JSlider();
        mFadeChk = new javax.swing.JCheckBox();
        mFadeLbl = new javax.swing.JLabel();
        mGainChk = new javax.swing.JCheckBox();
        mCrossFadeChk = new javax.swing.JCheckBox();
        mGainLbl = new javax.swing.JLabel();
        mGainValueTxt = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();

        jLabel1.setText("The composer rearranges the segments based on the features extracted.");

        jScrollPane1.setViewportView(mComposerList);

        jLabel2.setText("Step 1: Select a composer:");

        jLabel3.setText("Step 2: Edit composer details:");

        mDetailsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("")));

        jLabel7.setForeground(new java.awt.Color(102, 102, 255));
        jLabel7.setText("Description:");

        mSettingsLbl.setForeground(new java.awt.Color(102, 102, 255));
        mSettingsLbl.setText("Settings:");

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mDescriptionLbl.setEditable(false);
        mDescriptionLbl.setOpaque(false);
        jScrollPane2.setViewportView(mDescriptionLbl);

        org.jdesktop.layout.GroupLayout mSettingsWrapperLayout = new org.jdesktop.layout.GroupLayout(mSettingsWrapper);
        mSettingsWrapper.setLayout(mSettingsWrapperLayout);
        mSettingsWrapperLayout.setHorizontalGroup(
            mSettingsWrapperLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 294, Short.MAX_VALUE)
        );
        mSettingsWrapperLayout.setVerticalGroup(
            mSettingsWrapperLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 91, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout mDetailsPanelLayout = new org.jdesktop.layout.GroupLayout(mDetailsPanel);
        mDetailsPanel.setLayout(mDetailsPanelLayout);
        mDetailsPanelLayout.setHorizontalGroup(
            mDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mDetailsPanelLayout.createSequentialGroup()
                .add(mDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mDetailsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(mDetailsPanelLayout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
                    .add(mDetailsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(mDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mDetailsPanelLayout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(mSettingsWrapper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(mSettingsLbl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        mDetailsPanelLayout.setVerticalGroup(
            mDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mDetailsPanelLayout.createSequentialGroup()
                .add(jLabel7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mSettingsLbl)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mSettingsWrapper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setText("Step 3: Apply operations to all segments:");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        mReverseChk.setText("Reverse");
        mReverseChk.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mFadeSlider.setMajorTickSpacing(10);
        mFadeSlider.setMaximum(50);
        mFadeSlider.setMinorTickSpacing(5);
        mFadeSlider.setPaintLabels(true);
        mFadeSlider.setPaintTicks(true);
        mFadeSlider.setValue(10);

        mFadeChk.setText("Fade in/out");
        mFadeChk.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mFadeChk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mFadeChkActionPerformed(evt);
            }
        });

        mFadeLbl.setText("length (ms):");

        mGainChk.setText("Gain");
        mGainChk.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mGainChk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mGainChkActionPerformed(evt);
            }
        });

        mCrossFadeChk.setText("Crossfade");
        mCrossFadeChk.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mCrossFadeChk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mCrossFadeChkActionPerformed(evt);
            }
        });

        mGainLbl.setText("value:");

        mGainValueTxt.setText("1.0");

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(mReverseChk)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(mGainLbl))
                    .add(mGainChk))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mGainValueTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(16, 16, 16)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mFadeChk)
                    .add(mCrossFadeChk))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mFadeLbl)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mFadeSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mFadeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(0, 0, 0)
                                .add(mReverseChk))
                            .add(mGainChk))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(mGainLbl)
                            .add(mGainValueTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                        .add(mFadeChk)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mCrossFadeChk))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(mFadeLbl))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 145, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(mDetailsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel4)
                .addContainerGap(301, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mDetailsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void mCrossFadeChkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mCrossFadeChkActionPerformed
    {//GEN-HEADEREND:event_mCrossFadeChkActionPerformed
        if(mFadeChk.isSelected() || mCrossFadeChk.isSelected())
        {
            mFadeLbl.setVisible(true);
            mFadeSlider.setVisible(true);
        }
        else
        {
            mFadeLbl.setVisible(false);
            mFadeSlider.setVisible(false); 
        }
    }//GEN-LAST:event_mCrossFadeChkActionPerformed

    private void mFadeChkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mFadeChkActionPerformed
    {//GEN-HEADEREND:event_mFadeChkActionPerformed

        if(mFadeChk.isSelected() || mCrossFadeChk.isSelected())
        {
            mFadeLbl.setVisible(true);
            mFadeSlider.setVisible(true);
        }
        else
        {
            mFadeLbl.setVisible(false);
            mFadeSlider.setVisible(false); 
        }
        
    }//GEN-LAST:event_mFadeChkActionPerformed

    private void mGainChkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mGainChkActionPerformed
    {//GEN-HEADEREND:event_mGainChkActionPerformed

        if(mGainChk.isSelected())
        {
            mGainLbl.setVisible(true);
            mGainValueTxt.setVisible(true);
        }
        else
        {
            mGainLbl.setVisible(false);
            mGainValueTxt.setVisible(false); 
        }
        
    }//GEN-LAST:event_mGainChkActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JList mComposerList;
    private javax.swing.JCheckBox mCrossFadeChk;
    private javax.swing.JTextPane mDescriptionLbl;
    private javax.swing.JPanel mDetailsPanel;
    private javax.swing.JCheckBox mFadeChk;
    private javax.swing.JLabel mFadeLbl;
    private javax.swing.JSlider mFadeSlider;
    private javax.swing.JCheckBox mGainChk;
    private javax.swing.JLabel mGainLbl;
    private javax.swing.JTextField mGainValueTxt;
    private javax.swing.JCheckBox mReverseChk;
    private javax.swing.JLabel mSettingsLbl;
    private javax.swing.JPanel mSettingsWrapper;
    // End of variables declaration//GEN-END:variables


    
}
