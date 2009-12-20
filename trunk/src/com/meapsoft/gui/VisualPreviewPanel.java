/*
 * VisualPreviewPanel.java
 *
 * Created on October 28, 2007, 2:46 PM
 */

package com.meapsoft.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import com.meapsoft.FeatFile;
import com.meapsoft.visualizer.SingleFeatureBarGraphPanel;
import com.meapsoft.visualizer.SingleFeatureColorBarsPanel;
import com.meapsoft.visualizer.SingleFeatureCrissCrossPanel;
import com.meapsoft.visualizer.SingleFeaturePanel;
import com.meapsoft.visualizer.SingleFeatureSpectrumPanel;
import com.meapsoft.visualizer.SingleFeatureWaveformPanel;

/**
 *
 * @author  Mike
 */
public class VisualPreviewPanel extends BasePanel
{
    //a hashmap of visual panels
    private HashMap mVisualPanels;
	
    //create our preview tabs here
    JPanel mSegmentsPreviewTab = null;
    JPanel mFeaturesPreviewTab = null;
    JPanel mComposedPreviewTab = null;
	
    /** Creates new form VisualPreviewPanel */
    public VisualPreviewPanel()
    {
    	//call our parent with nothing here...just for consistency
    	super(null);
    	
        initComponents();
                
        //create our visual panels
        initVisualPanels();
    }
    
////////////////////////////////////////////////////////////////////////
// setters and getters
////////////////////////////////////////////////////////////////////////
     
     public void setMainScreen(MeapsoftGUI mainScreen)
     {
     	mMainScreen = mainScreen;
     }
     
     public SingleFeaturePanel getVisualPanel(String key)
     {
     	if(mVisualPanels.containsKey(key))
     	{
     		return (SingleFeaturePanel)mVisualPanels.get(key);
     	}
     	
     	return null;
     }
     
////////////////////////////////////////////////////////////////////////
// initPreviewTabs() - initializes the preview tabs
////////////////////////////////////////////////////////////////////////
	public void initPreviewTabs()
	{
		//create the tabs
		mSegmentsPreviewTab = new JPanel();
		mSegmentsPreviewTab.setLayout(new BoxLayout(mSegmentsPreviewTab, BoxLayout.Y_AXIS));
    	mVisualTabBase.add(mSegmentsPreviewTab, "Segments");
		
		mFeaturesPreviewTab = new JPanel();
		mFeaturesPreviewTab.setLayout(new BoxLayout(mFeaturesPreviewTab, BoxLayout.Y_AXIS));
		mVisualTabBase.add(mFeaturesPreviewTab, "Features");
		
		mComposedPreviewTab = new JPanel();
		mComposedPreviewTab.setLayout(new BoxLayout(mComposedPreviewTab, BoxLayout.Y_AXIS));
		mVisualTabBase.add(mComposedPreviewTab, "Composed");
		
		//iterate through all tabs. disable them, and set color
	  	for(int i = 0;i < mVisualTabBase.getTabCount(); i++)
	  	{
	  		mVisualTabBase.setEnabledAt(i, false);
	  		
	  		if(mMainScreen.isColorEnabled())
	  			mVisualTabBase.setBackgroundAt(i, GUIUtils.getRandomColor());
	  	}
	}
     
////////////////////////////////////////////////////////////////////////
// initVisualPanels() - initializes the visual panels
////////////////////////////////////////////////////////////////////////
        private void initVisualPanels()
        {
        	//create our hashmap first
        	mVisualPanels = new HashMap();
        	
            //instantiate these for use later on
            SingleFeaturePanel waveformPanel = new SingleFeatureWaveformPanel();
            waveformPanel.setMinimumSize(new Dimension(400, 45));
            waveformPanel.setPreferredSize(new Dimension(400,45));
            waveformPanel.setSegTickType(SingleFeaturePanel.SHORT_SEG_TICKS);

            SingleFeatureSpectrumPanel spectrumPanel = new SingleFeatureSpectrumPanel();
            spectrumPanel.setMinimumSize(new Dimension(400, 45));
            spectrumPanel.setPreferredSize(new Dimension(400,45));	
            spectrumPanel.setSegTickType(SingleFeaturePanel.SHORT_SEG_TICKS);
            
            SingleFeatureColorBarsPanel colorBarsPanel = new SingleFeatureColorBarsPanel();
            colorBarsPanel.setMinimumSize(new Dimension(400, 45));
            colorBarsPanel.setPreferredSize(new Dimension(400,45));	
            colorBarsPanel.setSegTickType(SingleFeaturePanel.NO_SEG_TICKS);
            
            //add these to our hashmap
            mVisualPanels.put("Waveform", waveformPanel);
            mVisualPanels.put("Spectrum", spectrumPanel);
            mVisualPanels.put("ColorBars", colorBarsPanel);
        }
    
////////////////////////////////////////////////////////////////////////
// addVisualPreview() - adds a visual preview panel
////////////////////////////////////////////////////////////////////////
        
         public void addVisualPreview(String tabName, String panelName, String featureName, FeatFile featureFile)
         {            
             //get the tab with this name and remove its contents
             JPanel previewTab = getPreviewTab(tabName);
              	
             //get the single feature panel here
             SingleFeaturePanel featurePanel = this.getVisualPanel(panelName);
			 
             //add this before we update the feature panel
			 previewTab.add(featurePanel);
			 
             //update the progress bar here
             String updateString = "Drawing " + panelName + "...";
			 mMainScreen.updateProgressBar(updateString, featurePanel.getProgress());
			
			 //initialize the panel here
			 featurePanel.initialize(featureFile, featureName);
			 featurePanel.updateData();
			 
			 //repaint the feature panel after we add it
			 featurePanel.repaint();
			 
             //set us to be visible
             mMainScreen.setVisualPreviewVisible(true);
         } 
         
////////////////////////////////////////////////////////////////////////
// getPreviewTab() - gets a preview tab with this name
////////////////////////////////////////////////////////////////////////
            
        private JPanel getPreviewTab(String tabName)
        {
        	//if we don't have a tab
        	int tabIndex = mVisualTabBase.indexOfTab(tabName);
        	        	
        	//get the tab and enable it
        	JPanel tab = (JPanel)mVisualTabBase.getComponent(tabIndex);
        	mVisualTabBase.setEnabledAt(tabIndex, true);
        	mVisualTabBase.setSelectedIndex(tabIndex);
        	
        	//return this tab
        	return tab;
        }
        
////////////////////////////////////////////////////////////////////////
// initColors() - initializes the colors
////////////////////////////////////////////////////////////////////////
          
        public void initColors(boolean flag)
        {
        	Color tabColor = flag ? GUIUtils.getRandomColor() : null;
        	
        	for(int i = 0;i < mVisualTabBase.getTabCount(); i++)
        	{
        		mVisualTabBase.setBackgroundAt(i, tabColor);
        	}
        }
        
        
    	public synchronized int run() 
    	{
    		return 0;
    	}  
    	            
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mVisualTabBase = new javax.swing.JTabbedPane();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Visual Preview", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(mVisualTabBase, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mVisualTabBase, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 142, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane mVisualTabBase;
    // End of variables declaration//GEN-END:variables
    
}
