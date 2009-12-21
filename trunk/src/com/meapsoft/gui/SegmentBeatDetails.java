/*
 * SegmentBeatDetails.java
 *
 * Created on October 28, 2007, 3:24 PM
 */

package com.meapsoft.gui;

/**
 *
 * @author  Mike
 */
public class SegmentBeatDetails extends javax.swing.JPanel
{
    
    /** Creates new form SegmentBeatDetails */
    public SegmentBeatDetails()
    {
        initComponents();
    }
    
    public boolean isCutTempoInHalf()
    {
        return mHalfTempChk.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        mHalfTempChk = new javax.swing.JCheckBox();

        mHalfTempChk.setText("Cut tempo in half");
        mHalfTempChk.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mHalfTempChk.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(24, 24, 24)
                .add(mHalfTempChk)
                .addContainerGap(129, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(28, 28, 28)
                .add(mHalfTempChk)
                .addContainerGap(71, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox mHalfTempChk;
    // End of variables declaration//GEN-END:variables
    
}