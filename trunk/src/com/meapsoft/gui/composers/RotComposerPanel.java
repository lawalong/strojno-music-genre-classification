/*
 * RotComposerPanel.java
 *
 * Created on November 16, 2007, 4:55 PM
 */

package com.meapsoft.gui.composers;

import com.meapsoft.composers.Composer;
import com.meapsoft.composers.RotComposer;

/**
 *
 * @author  ms3311
 */
public class RotComposerPanel extends ComposerSettingsPanel {
    
    /** Creates new form RotComposerPanel */
    public RotComposerPanel() {
        initComponents();
    }
    
    public int initComposer()
    {
		int bpm = new Integer(mMeasureTxt.getText()).intValue();
		int positions = new Integer(mNumRotationsTxt.getText()).intValue();
		mComposer = new RotComposer(mParentTab.featFile, mParentTab.edlFile, bpm, positions, mRotateLeftBtn.isSelected());
    	
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
        mButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        mRotateLeftBtn = new javax.swing.JRadioButton();
        mRotateRightBtn = new javax.swing.JRadioButton();
        mMeasureTxt = new javax.swing.JTextField();
        mNumRotationsTxt = new javax.swing.JTextField();

        jLabel1.setText("Beats per measure:");

        jLabel2.setText("Beats to rotate:");

        mButtonGroup.add(mRotateLeftBtn);
        mRotateLeftBtn.setText("Rotate Left");
        mRotateLeftBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mRotateLeftBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mButtonGroup.add(mRotateRightBtn);
        mRotateRightBtn.setSelected(true);
        mRotateRightBtn.setText("Rotate Right");
        mRotateRightBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mRotateRightBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mMeasureTxt.setText("4");

        mNumRotationsTxt.setText("1");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(mNumRotationsTxt)
                    .add(mMeasureTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE))
                .add(32, 32, 32)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mRotateLeftBtn)
                    .add(mRotateRightBtn))
                .addContainerGap(63, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(mRotateLeftBtn)
                    .add(mMeasureTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(mRotateRightBtn)
                    .add(mNumRotationsTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(98, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.ButtonGroup mButtonGroup;
    private javax.swing.JTextField mMeasureTxt;
    private javax.swing.JTextField mNumRotationsTxt;
    private javax.swing.JRadioButton mRotateLeftBtn;
    private javax.swing.JRadioButton mRotateRightBtn;
    // End of variables declaration//GEN-END:variables
    
}
