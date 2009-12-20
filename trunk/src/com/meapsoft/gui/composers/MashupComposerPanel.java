/*
 * MashupComposerPanel.java
 *
 * Created on November 16, 2007, 4:42 PM
 */

package com.meapsoft.gui.composers;

import java.io.File;

import com.meapsoft.EDLFile;
import com.meapsoft.FeatFile;
import com.meapsoft.composers.Composer;
import com.meapsoft.composers.MashupComposer;
import com.meapsoft.disgraced.GUIUtils;

/**
 *
 * @author  ms3311
 */
public class MashupComposerPanel extends ComposerSettingsPanel {
    
	private String mChunkDBFeaturesNameShort = "";
	private String mChunkDBFeaturesNameFull = "";
	
	
    /** Creates new form MashupComposerPanel */
    public MashupComposerPanel() {
        initComponents();
    }

    public int initComposer()
    {
		if (mChunkDBFeaturesNameFull == null)
		{
			GUIUtils.ShowDialog("MashupComposer: Please select a chunk database features file!", GUIUtils.MESSAGE, mParentTab.mMainScreen);
			return -1;
		}
		
		mParentTab.edlFile = new EDLFile(mParentTab.dataDirectory + mParentTab.slash + mParentTab.dataBaseName + "_using_" + 
				mChunkDBFeaturesNameShort + ".edl");

        FeatFile chunkDBFile = new FeatFile(mChunkDBFeaturesNameFull);
        
		if (!(new File(mChunkDBFeaturesNameFull).exists()))
		{
			GUIUtils.ShowDialog("MashupComposer: Please select a chunk database features file!", GUIUtils.MESSAGE, mParentTab.mMainScreen);
			return -1;
		}
			
        mComposer = new MashupComposer(mParentTab.featFile, chunkDBFile, mParentTab.edlFile);
    
        return 0;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        mFeatFileTxt = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        mBrowseBtn = new javax.swing.JButton();

        mFeatFileTxt.setText("chunk database .feat file");

        jLabel1.setText("Feature file:");

        mBrowseBtn.setText("Browse");
        mBrowseBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mBrowseBtnActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(35, 35, 35)
                        .add(mFeatFileTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 171, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(mBrowseBtn))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel1)))
                .addContainerGap(48, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mFeatFileTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mBrowseBtn))
                .addContainerGap(149, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void mBrowseBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mBrowseBtnActionPerformed
    {//GEN-HEADEREND:event_mBrowseBtnActionPerformed

		String names[] = browseFile("feat");
		mChunkDBFeaturesNameFull = names[0];
		mChunkDBFeaturesNameShort = names[1];
        mFeatFileTxt.setText(mChunkDBFeaturesNameShort);
    	
    }//GEN-LAST:event_mBrowseBtnActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton mBrowseBtn;
    private javax.swing.JTextField mFeatFileTxt;
    // End of variables declaration//GEN-END:variables
    
}
