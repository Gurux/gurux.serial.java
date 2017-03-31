//
// --------------------------------------------------------------------------
//  Gurux Ltd
// 
//
//
// Filename:        $HeadURL$
//
// Version:         $Revision$,
//                  $Date$
//                  $Author$
//
// Copyright (c) Gurux Ltd
//
//---------------------------------------------------------------------------
//
//  DESCRIPTION
//
// This file is a part of Gurux Device Framework.
//
// Gurux Device Framework is Open Source software; you can redistribute it
// and/or modify it under the terms of the GNU General Public License 
// as published by the Free Software Foundation; version 2 of the License.
// Gurux Device Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of 
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU General Public License for more details.
//
// More information of Gurux products: http://www.gurux.org
//
// This code is licensed under the GNU General Public License v2. 
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------

package example;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import gurux.common.GXCommon;
import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.TraceEventArgs;
import gurux.common.enums.MediaState;
import gurux.serial.GXSerial;

/**
 * Gurux network example for Java.
 */
public class GuruxSerialExampleJava extends javax.swing.JFrame
        implements IGXMediaListener, ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    Timer StatisticTimer;

    GXSerial serial1 = new GXSerial();

    /**
     * Creates new form.
     */
    public GuruxSerialExampleJava() {
        initComponents();
        serial1.addListener(this);
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        ReceivedTB.setModel(listModel);
        DefaultListModel<String> m =
                (DefaultListModel<String>) ReceivedTB.getModel();
        m.clear();
        onMediaStateChange(serial1, new MediaStateEventArgs(MediaState.CLOSED));
    }

    // @Override
    public void onError(Object sender, Exception ex) {
        try {
            serial1.close();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (Exception Ex) {
            JOptionPane.showMessageDialog(this, Ex.getMessage());
        }
    }

    public void onReceived(Object sender, ReceiveEventArgs e) {
        try {
            DefaultListModel<String> m =
                    (DefaultListModel<String>) ReceivedTB.getModel();
            m.add(m.size(), GXCommon.bytesToHex((byte[]) e.getData()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    /*
     * Read statistics.
     */
    public void actionPerformed(ActionEvent e) {
        this.ReceivedStat.setText(String.valueOf(serial1.getBytesReceived()));
        this.SentStat.setText(String.valueOf(serial1.getBytesSent()));
        serial1.resetByteCounters();
    }

    public void onMediaStateChange(Object sender, MediaStateEventArgs e) {
        try {
            boolean IsOpen = e.getState() == MediaState.OPEN;
            // OpenBtm
            OpenBtn.setEnabled(!IsOpen);
            // Close Btn.
            CloseBtn.setEnabled(IsOpen);
            // Send Btn.
            SendBtn.setEnabled(IsOpen);
            SendTB.setEnabled(IsOpen);
            if (IsOpen) {
                StatisticTimer = new Timer(60000, this);
                StatisticTimer.setInitialDelay(1000);
                StatisticTimer.start();
            } else {
                if (StatisticTimer != null) {
                    StatisticTimer.stop();
                    StatisticTimer = null;
                }
            }
        } catch (Exception Ex) {
            JOptionPane.showMessageDialog(this, Ex.getMessage());
        }
    }

    // We do not use trace at the moment.
    public void onTrace(Object sender, TraceEventArgs e) {

    }

    // We do not need notify if property is changed.
    public void onPropertyChanged(Object sender, PropertyChangedEventArgs e) {

    }

    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        OpenBtn = new javax.swing.JButton();
        SendTB = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ReceivedTB = new javax.swing.JList<String>();
        CloseBtn = new javax.swing.JButton();
        PropertiesBtn = new javax.swing.JButton();
        SendBtn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        SentStat = new javax.swing.JLabel();
        ReceivedStat = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Received:");

        OpenBtn.setText("Open");
        OpenBtn.setName("OpenBtn"); // NOI18N
        OpenBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenBtnActionPerformed(evt);
            }
        });

        jLabel2.setText("Send:");

        ReceivedTB.setModel(new javax.swing.AbstractListModel<String>() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;
            String[] strings = {};

            public int getSize() {
                return strings.length;
            }

            public String getElementAt(int i) {
                return strings[i];
            }
        });
        jScrollPane1.setViewportView(ReceivedTB);

        CloseBtn.setText("Close");
        CloseBtn.setName(""); // NOI18N
        CloseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseBtnActionPerformed(evt);
            }
        });

        PropertiesBtn.setText("Properties");
        PropertiesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PropertiesBtnActionPerformed(evt);
            }
        });

        SendBtn.setText("Send");
        SendBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendBtnActionPerformed(evt);
            }
        });

        jLabel3.setText("Message statistics in last minute");

        SentStat.setText("Sent:");

        ReceivedStat.setText("Received:");

        javax.swing.GroupLayout layout =
                new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup().addContainerGap()
                        .addGroup(layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(SendBtn))
                                .addGroup(layout
                                        .createSequentialGroup().addGroup(layout
                                                .createParallelGroup(
                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(layout
                                                        .createSequentialGroup()
                                                        .addComponent(SendTB)
                                                        .addGap(24, 24, 24))
                                                .addComponent(jScrollPane1,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        289, Short.MAX_VALUE)
                                                .addGroup(
                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                        layout.createSequentialGroup()
                                                                .addGroup(layout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                layout.createParallelGroup(
                                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                        .addGroup(
                                                                                                layout.createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                SentStat)
                                                                                                        .addGap(77,
                                                                                                                77,
                                                                                                                77)
                                                                                                        .addComponent(
                                                                                                                ReceivedStat))
                                                                                        .addComponent(
                                                                                                jLabel3))
                                                                        .addGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                layout.createSequentialGroup()
                                                                                        .addGap(10,
                                                                                                10,
                                                                                                10)
                                                                                        .addComponent(
                                                                                                jLabel2)))
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)))
                                        .addGroup(layout
                                                .createParallelGroup(
                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(PropertiesBtn,
                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(
                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                        layout.createParallelGroup(
                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(
                                                                        OpenBtn)
                                                                .addComponent(
                                                                        CloseBtn)))))
                        .addContainerGap()));
        layout.setVerticalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup().addGap(14, 14, 14)
                        .addGroup(layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(SendTB,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                52,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(9, 9, 9))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(OpenBtn).addGap(5, 5, 5)
                                        .addComponent(CloseBtn)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(PropertiesBtn)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1,
                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(SendBtn,
                                        javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 97,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18).addComponent(jLabel3)
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(SentStat)
                                .addComponent(ReceivedStat))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /*
     * Open Media
     */
    private void OpenBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_OpenBtnActionPerformed
        try {
            serial1.open();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }// GEN-LAST:event_OpenBtnActionPerformed

    /*
     * Close Media.
     */
    private void CloseBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_CloseBtnActionPerformed
        try {
            serial1.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }// GEN-LAST:event_CloseBtnActionPerformed

    /*
     * Show media properties.
     */
    private void PropertiesBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_PropertiesBtnActionPerformed
        try {
            serial1.properties(this);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }// GEN-LAST:event_PropertiesBtnActionPerformed

    /*
     * Send new data.
     */
    private void SendBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_SendBtnActionPerformed
        try {
            serial1.send(SendTB.getText(), null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }// GEN-LAST:event_SendBtnActionPerformed

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting
        // code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.
         * html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
                    .getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger
                    .getLogger(GuruxSerialExampleJava.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger
                    .getLogger(GuruxSerialExampleJava.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger
                    .getLogger(GuruxSerialExampleJava.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger
                    .getLogger(GuruxSerialExampleJava.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GuruxSerialExampleJava().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CloseBtn;
    private javax.swing.JButton OpenBtn;
    private javax.swing.JButton PropertiesBtn;
    private javax.swing.JLabel ReceivedStat;
    private javax.swing.JList<String> ReceivedTB;
    private javax.swing.JButton SendBtn;
    private javax.swing.JTextField SendTB;
    private javax.swing.JLabel SentStat;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
