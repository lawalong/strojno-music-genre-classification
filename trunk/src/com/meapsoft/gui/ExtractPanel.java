/*
 * ExtractPanel.java
 *
 * Created on October 28, 2007, 3:16 PM
 */

package com.meapsoft.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.RTSI;

import com.meapsoft.FeatExtractor;
import com.meapsoft.MEAPUtil;
import com.meapsoft.featextractors.FeatureExtractor;
import com.meapsoft.featextractors.MetaFeatureExtractor;
import com.meapsoft.disgraced.GUIUtils;
import com.meapsoft.gui.widgets.FeatureExtractorCheckBox;

/**
 *
 * @author  Mike
 */
public class ExtractPanel extends BasePanel implements ListSelectionListener, ChangeListener
{
	//save this selected feature
    FeatureExtractorCheckBox mSelectedFeature = null;
	
	
    /** Creates new form ExtractPanel */
    public ExtractPanel(MeapsoftGUI mainScreen)
    {
    	//pass this up to our main screen
    	super(mainScreen);
        
    	//init our layout and stuff
        initComponents();
        
        //initialze the rest of the panel
        initPanel();
        
        //initialize the checkboxes here
        initCheckboxes();
    }
    
    ////////////////////////////////////////////////////////////////////////
    // initialization methods
    ////////////////////////////////////////////////////////////////////////
    private void initCheckboxes() 
    {
    	//sniff out the feature extrators first
    	Vector featureExtractors = sniffForClasses("com.meapsoft.featextractors", "com.meapsoft.featextractors.FeatureExtractor");
    	
    	//save a place for the default check here
    	FeatureExtractorCheckBox defaultChk = null;
    	
    	//iterate and create our checkboxes
        for(int i = 0; i < featureExtractors.size(); i++)
        {
        	//get the name
        	String name = featureExtractors.get(i).toString();
        	
        	//create the checkbox here
        	FeatureExtractorCheckBox chk = new FeatureExtractorCheckBox(name);
        	
        	//make us an actionlistener for the checkbox
        	chk.addChangeListener(this);
        	
        	//see if its a meta extractor
        	if(chk.getExtractor() instanceof MetaFeatureExtractor)
        	{
        		mMetaFeatureList.add(chk);
        	}
        	else
        	{
        		mFeatureList.add(chk);
        		
        		//check this one by default
        		if(chk.getName().equals("AvgPitch"))
        		{
        			defaultChk = chk;
        		}
        	}
        }
        
        //set previous selection, set selected value
        mFeatureList.setSelectedValue(defaultChk);
        
        //call a fake mouse event twice here.
        //once so it will make it the preview selection
        //another time so it will actually check the box
        mFeatureList.onMouseReleased(null);
        mFeatureList.onMouseReleased(null);
	}
    
    private void initPanel()
    {
    	//add us as a mouse listener of the checkBox
    	mFeatureList.addListSelectionListener(this);
    	mMetaFeatureList.addListSelectionListener(this);
    	
//    	//initialze the error label (for weights)
//    	ImageIcon icon = new ImageIcon(getClass().getResource("/com/exclaim.png"));
//    	mWeightErrorLbl.setIcon(icon);
//    	mWeightErrorLbl.setText("Invalid weight. It must be between 0 and 1");
//    	mWeightErrorLbl.setVisible(false);
    	
    	//make the meta feature options stuff invisible
		mMetaOptionsLbl.setVisible(false);
		mReplaceBtn.setVisible(false);
		mAppendBtn.setVisible(false);
    }
    
    ////////////////////////////////////////////////////////////////////////
    // stateChanged() - implemented from change listener
    ////////////////////////////////////////////////////////////////////////

	public void stateChanged(ChangeEvent e)
	{
		//see if any boxes are selected
		if(mMetaFeatureList.areAnyBoxesSelected())
		{
			mMetaOptionsLbl.setVisible(true);
			mReplaceBtn.setVisible(true);
			mAppendBtn.setVisible(true);
		}
		else
		{
			mMetaOptionsLbl.setVisible(false);
			mReplaceBtn.setVisible(false);
			mAppendBtn.setVisible(false);
		}
	}
    
    ////////////////////////////////////////////////////////////////////////
    // valueChanged() - implemented from list selection listener
    ////////////////////////////////////////////////////////////////////////
	public void valueChanged(ListSelectionEvent e) 
	{		
		//get the list who called this guy
		JList list = (JList)e.getSource();
		
		//get the selected feature at this point (only one allowed at a time)
		mSelectedFeature = (FeatureExtractorCheckBox)list.getSelectedValue();
		
		if(mSelectedFeature.getExtractor() instanceof MetaFeatureExtractor)
		{
			mFeatureTitleLbl.setText("Meta Feature:");
		}
		else
		{
			mFeatureTitleLbl.setText("Feature:");
		}
		
		
		//update all the data in the details now
		mFeatureNameLbl.setText(mSelectedFeature.getName());
		mFeatureDescTxt.setText(mSelectedFeature.getDescription());

//		//update weight
//		mWeightTxt.setText(String.valueOf(mSelectedFeature.getWeight()));
	}
	
    ////////////////////////////////////////////////////////////////////////
    // run() - does all the work for this guy
    ////////////////////////////////////////////////////////////////////////
	public synchronized int run()
    {
		boolean regularFE = false;
		boolean metaFE = false;
		
		//get the list of selected features and meta features
		Vector selectedFeatures = mFeatureList.getAllSelected();
		Vector selectedMetaFeatures = mMetaFeatureList.getAllSelected();
		
		//if we are using features, make sure we have meta features
		if(selectedFeatures.size() == 0 && selectedMetaFeatures.size() > 0)
		{
			GUIUtils.ShowDialog("You need at least one regular feature extractor in order to use a meta-feature extractor.", GUIUtils.MESSAGE, mMainScreen);
			return -1;		
		}
		
		//load all these features into adjacent vectors...yuck
		Vector features = new Vector();
		Vector weights = new Vector();
		
		//load features first
		for(int i = 0; i < selectedFeatures.size(); i++)
		{
			FeatureExtractorCheckBox chk = (FeatureExtractorCheckBox)selectedFeatures.get(i);
			features.add(chk.getExtractor());
			weights.add(chk.getWeight());
		}
		
		//then load meta features
		for(int i = 0; i < selectedMetaFeatures.size(); i++)
		{
			FeatureExtractorCheckBox chk = (FeatureExtractorCheckBox)selectedMetaFeatures.get(i);
			features.add(chk.getExtractor());
			weights.add(chk.getWeight());
		}
		
		
		//create the actual feat extractor, make it clear non-meta features
        FeatExtractor featExtractor = new FeatExtractor(segmentFile, featFile, features);
        featExtractor.writeMEAPFile = MEAPUtil.writeMEAPFile;
        featExtractor.setFeatureExtractorWeights(weights);
        featExtractor.setClearNonMetaFeatures(mReplaceBtn.isSelected());

	    //update the progress bar
	    mMainScreen.updateProgressBar("Extracting Features...", featExtractor.getProgress());

        try
        {				
            // clear whatever chunks may have already been calculated
            featFile.clearChunks();
            
            //do the magic!
            featExtractor.setup();
            featExtractor.processFeatFiles();

            //are we writing this?            
            if(featExtractor.writeMEAPFile)
	            featFile.writeFile();
        }
        catch (Exception e)
        {
			GUIUtils.ShowDialog(e, "Error running Feature Extractor", GUIUtils.FATAL_ERROR, mMainScreen);	
            return -1;
		}
          
        //add a visual preview here
        //THIS IS A BUG, so it is commented out. 
        //for some reason when paintComponent is being overridden
        //in SingleFeaturePanel, it is just being repainted over and over again
        //and we are getting null pointer errors in FeatChunk.getFeatureNumberForName
        
//        if(mMainScreen.shouldProcessVisualPreview())
//        {
//	        //get the preview panel here
//	        VisualPreviewPanel previewPanel = mMainScreen.getPreviewPanel();
//	        
//	        //for now draw all features. 
//	        //this is not good for usability
//	        //we should really be having a dropdown option in that panel
//	        //that allows the user which feature they want to see
//	        //then at that point, they can launch the visualizer
//	        //to see more details
//	        int numFeatures = featFile.featureDescriptions.size();
//	        
//	        for(int i = 0; i < numFeatures; i++)
//	        {
//	        	String featName = (String)featFile.featureDescriptions.elementAt(i);
//	        	previewPanel.addVisualPreview("Composed", "ColorBars", featName, featFile);
//	        	
//	        }
//        }
        
        return 0;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mMetaBtnGrp = new javax.swing.ButtonGroup();
        jTextField1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        mFeatureList = new com.meapsoft.gui.widgets.CheckBoxList();
        jPanel1 = new javax.swing.JPanel();
        mDetailsWrapper = new javax.swing.JPanel();
        mFeatureTitleLbl = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        mFeatureNameLbl = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        mFeatureDescTxt = new javax.swing.JTextPane();
        mMetaFeatureList = new com.meapsoft.gui.widgets.CheckBoxList();
        mMetaOptionsLbl = new javax.swing.JLabel();
        mReplaceBtn = new javax.swing.JRadioButton();
        mAppendBtn = new javax.swing.JRadioButton();

        jTextField1.setText("jTextField1");

        jLabel4.setText("The feature extractor will search for specific features in the segments and extract them.");

        jLabel1.setText("Step 1: Select one or more features to extract. Click to learn more about a feature");

        jLabel5.setText("Step 2: Select Meta-features");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Details"));

        mFeatureTitleLbl.setForeground(new java.awt.Color(102, 102, 255));
        mFeatureTitleLbl.setText("Feature:");

        jLabel3.setForeground(new java.awt.Color(102, 102, 255));
        jLabel3.setText("Description:");

        mFeatureNameLbl.setText("jLabel7");

        mFeatureDescTxt.setBorder(null);
        mFeatureDescTxt.setEditable(false);
        mFeatureDescTxt.setOpaque(false);
        jScrollPane2.setViewportView(mFeatureDescTxt);

        org.jdesktop.layout.GroupLayout mDetailsWrapperLayout = new org.jdesktop.layout.GroupLayout(mDetailsWrapper);
        mDetailsWrapper.setLayout(mDetailsWrapperLayout);
        mDetailsWrapperLayout.setHorizontalGroup(
            mDetailsWrapperLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mDetailsWrapperLayout.createSequentialGroup()
                .add(mDetailsWrapperLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mDetailsWrapperLayout.createSequentialGroup()
                        .add(41, 41, 41)
                        .add(mFeatureNameLbl))
                    .add(mDetailsWrapperLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(mDetailsWrapperLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(mFeatureTitleLbl)))
                    .add(mDetailsWrapperLayout.createSequentialGroup()
                        .add(40, 40, 40)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)))
                .addContainerGap())
        );
        mDetailsWrapperLayout.setVerticalGroup(
            mDetailsWrapperLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mDetailsWrapperLayout.createSequentialGroup()
                .addContainerGap()
                .add(mFeatureTitleLbl)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mFeatureNameLbl)
                .add(16, 16, 16)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .add(66, 66, 66))
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(mDetailsWrapper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mDetailsWrapper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        mMetaOptionsLbl.setText("Step 3: Set Meta-feature options");

        mMetaBtnGrp.add(mReplaceBtn);
        mReplaceBtn.setSelected(true);
        mReplaceBtn.setText("Replace features");
        mReplaceBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mMetaBtnGrp.add(mAppendBtn);
        mAppendBtn.setText("Append to features");
        mAppendBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addContainerGap(101, Short.MAX_VALUE))
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(24, 24, 24)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mAppendBtn)
                            .add(mReplaceBtn))
                        .add(34, 34, 34))
                    .add(layout.createSequentialGroup()
                        .add(mMetaOptionsLbl)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED))
                    .add(layout.createSequentialGroup()
                        .add(mMetaFeatureList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, mFeatureList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(10, 10, 10)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(mFeatureList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mMetaFeatureList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mMetaOptionsLbl)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mReplaceBtn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mAppendBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

//    //callback for when we type a key for the weight    
//    private void mWeightTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mWeightTxtKeyReleased
//
//        //get the input string
//        String str = mWeightTxt.getText();
//        
//        //set this error label to be invisible
//        mWeightErrorLbl.setVisible(false);    
//        mMainScreen.setLaunchButtonEnabled(true);
//        
//        //try to parse this and set the weight
//        try
//        {
//            float weight = Float.valueOf(str);
//            mSelectedFeature.setWeight(weight);
//            
//        }
//        catch(NumberFormatException e)
//        {
//            System.out.println("error in number");
//            mWeightErrorLbl.setText("Invalid weight. It must be between 0 and 1");
//            mWeightErrorLbl.setVisible(true);
//            mMainScreen.setLaunchButtonEnabled(false);
//        }
//        
//    }//GEN-LAST:event_mWeightTxtKeyReleased
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JRadioButton mAppendBtn;
    private javax.swing.JPanel mDetailsWrapper;
    private javax.swing.JTextPane mFeatureDescTxt;
    private com.meapsoft.gui.widgets.CheckBoxList mFeatureList;
    private javax.swing.JLabel mFeatureNameLbl;
    private javax.swing.JLabel mFeatureTitleLbl;
    private javax.swing.ButtonGroup mMetaBtnGrp;
    private com.meapsoft.gui.widgets.CheckBoxList mMetaFeatureList;
    private javax.swing.JLabel mMetaOptionsLbl;
    private javax.swing.JRadioButton mReplaceBtn;
    // End of variables declaration//GEN-END:variables



}
