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
// This code is licensed under the GNU General Public License v2. 
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------

package gurux.serial;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import gurux.io.Parity;
import gurux.io.StopBits;

/**
 * Serial settings dialog.
 */
class GXSettings extends javax.swing.JDialog implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * Has user accept changes.
     */
    private boolean accepted;
    /**
     * Parent media component.
     */
    private GXSerial target;

    /**
     * Used baud rate.
     */
    private javax.swing.JComboBox<String> baudRate;
    /**
     * Baud rate label.
     */
    private javax.swing.JLabel baudRateLbl;
    /**
     * Baud rate panel.
     */
    private javax.swing.JPanel baudRatePanel;
    /**
     * Cancel button.
     */
    private javax.swing.JButton cancelBtn;
    /**
     * Data bits combo box.
     */
    private javax.swing.JComboBox<String> dataBitsCB;
    /**
     * Data bits label.
     */
    private javax.swing.JLabel dataBitsLbl;
    /**
     * Data bits panel.
     */
    private javax.swing.JPanel dataBitsPanel;
    /**
     * OK button.
     */
    private javax.swing.JButton okBtn;
    /**
     * Parity combo box.
     */
    private javax.swing.JComboBox<String> parityCB;
    /**
     * Parity label.
     */
    private javax.swing.JLabel parityLbl;
    /**
     * Parity panel.
     */
    private javax.swing.JPanel parityPanel;
    /**
     * List of serial ports.
     */
    private javax.swing.JComboBox<String> portCB;
    /**
     * Serial port label.
     */
    private javax.swing.JLabel portLbl;
    /**
     * Serial port panel.
     */
    private javax.swing.JPanel portPanel;
    /**
     * Stop bits combo box.
     */
    private javax.swing.JComboBox<String> stopBitsCB;
    /**
     * Stop bits label.
     */
    private javax.swing.JLabel stopBitsLbl;
    /**
     * Stop bits panel.
     */
    private javax.swing.JPanel stopBitsPanel;
    /**
     * Main panel.
     */
    private javax.swing.JPanel jPanel1;

    /**
     * Creates new form GXSettings.
     * 
     * @param parent
     *            Parent form.
     * @param modal
     *            Is dialog shown as modal dialog.
     * @param comp
     *            Parent component.
     */
    GXSettings(final java.awt.Frame parent, final boolean modal,
            final GXSerial comp) {
        super(parent, modal);
        super.setLocationRelativeTo(parent);
        initComponents();
        target = comp;
        String[] ports = GXSerial.getPortNames();
        portCB.setModel(new DefaultComboBoxModel<String>(ports));
        int[] rates = GXSerial.getAvailableBaudRates(null);
        baudRate.setModel(new DefaultComboBoxModel<String>(getStrings(rates)));
        // CHECKSTYLE:OFF
        int[] dataBits = new int[] { 7, 8 };
        // CHECKSTYLE:ON
        dataBitsCB.setModel(
                new DefaultComboBoxModel<String>(getStrings(dataBits)));
        String[] parity =
                new String[] { "None", "Odd", "Even", "Mark", "Space" };
        parityCB.setModel(new DefaultComboBoxModel<String>(parity));
        String[] stopBits = new String[] { "One", "One_Point_Five", "Two" };
        stopBitsCB.setModel(new DefaultComboBoxModel<String>(stopBits));
        this.portCB.setSelectedItem(target.getPortName());
        this.baudRate.setSelectedItem(String.valueOf(target.getBaudRate()));
        this.dataBitsCB.setSelectedItem(String.valueOf(target.getDataBits()));
        this.parityCB.setSelectedItem(findParity(target.getParity()));
        this.stopBitsCB.setSelectedItem(findStopBit(target.getStopBits()));
    }

    /**
     * Has user accept changes.
     * 
     * @return True, if user has accept changes.
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * If user press ESC.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.dispose();
    }

    /**
     * Convert Stop bits to string.
     * 
     * @param stopBits
     *            Stop bits.
     * @return Value of StopBits as string.
     */
    final String findStopBit(final StopBits stopBits) {
        switch (stopBits) {
        case ONE:
            return "One";
        case TWO:
            return "Two";
        case ONE_POINT_FIVE:
            return "One_Point_Five";
        default:
        }
        return "";
    }

    /**
     * Get value of parity as a string.
     * 
     * @param parity
     *            Value of Parity
     * @return Value of parity as a string.
     */
    final String findParity(final Parity parity) {
        switch (parity) {
        case NONE:
            return "None";
        case ODD:
            return "Odd";
        case EVEN:
            return "Even";
        case MARK:
            return "Mark";
        case SPACE:
            return "Space";
        default:
        }
        return "";
    }

    /**
     * Converts array of integers to array of strings.
     * 
     * @param list
     *            Integer list.
     * @return String array.
     */
    final String[] getStrings(final int[] list) {
        String[] tmp = new String[list.length];
        int pos = 0;
        for (int it : list) {
            tmp[pos] = String.valueOf(it);
            ++pos;
        }
        return tmp;
    }

    /**
     * Initialize settings.
     */
    // CHECKSTYLE:OFF
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        portPanel = new javax.swing.JPanel();
        portLbl = new javax.swing.JLabel();
        portCB = new javax.swing.JComboBox<String>();
        baudRatePanel = new javax.swing.JPanel();
        baudRateLbl = new javax.swing.JLabel();
        baudRate = new javax.swing.JComboBox<String>();
        dataBitsPanel = new javax.swing.JPanel();
        dataBitsLbl = new javax.swing.JLabel();
        dataBitsCB = new javax.swing.JComboBox<String>();
        parityPanel = new javax.swing.JPanel();
        parityLbl = new javax.swing.JLabel();
        parityCB = new javax.swing.JComboBox<String>();
        stopBitsPanel = new javax.swing.JPanel();
        stopBitsLbl = new javax.swing.JLabel();
        stopBitsCB = new javax.swing.JComboBox<String>();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        portPanel.setPreferredSize(new java.awt.Dimension(298, 35));

        portLbl.setText("Port:");

        portCB.setModel(
                new javax.swing.DefaultComboBoxModel<String>(new String[0]));

        javax.swing.GroupLayout PortPanelLayout =
                new javax.swing.GroupLayout(portPanel);
        portPanel.setLayout(PortPanelLayout);
        PortPanelLayout.setHorizontalGroup(PortPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(PortPanelLayout.createSequentialGroup()
                        .addContainerGap().addComponent(portLbl)
                        .addGap(70, 70, 70)
                        .addComponent(portCB, 0,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addContainerGap()));
        PortPanelLayout.setVerticalGroup(PortPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                        PortPanelLayout.createSequentialGroup()
                                .addContainerGap(
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addGroup(PortPanelLayout
                                        .createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(portLbl)
                                        .addComponent(portCB,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap()));

        baudRatePanel.setPreferredSize(new java.awt.Dimension(218, 35));

        baudRateLbl.setText("Baud Rate:");

        baudRate.setModel(
                new javax.swing.DefaultComboBoxModel<String>(new String[0]));

        javax.swing.GroupLayout BaudRatePanelLayout =
                new javax.swing.GroupLayout(baudRatePanel);
        baudRatePanel.setLayout(BaudRatePanelLayout);
        BaudRatePanelLayout.setHorizontalGroup(BaudRatePanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(BaudRatePanelLayout.createSequentialGroup()
                        .addContainerGap().addComponent(baudRateLbl)
                        .addGap(41, 41, 41)
                        .addComponent(baudRate, 0,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addContainerGap()));
        BaudRatePanelLayout.setVerticalGroup(BaudRatePanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                        BaudRatePanelLayout.createSequentialGroup()
                                .addContainerGap(
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addGroup(BaudRatePanelLayout
                                        .createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(baudRateLbl)
                                        .addComponent(baudRate,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap()));

        dataBitsPanel.setPreferredSize(new java.awt.Dimension(220, 35));

        dataBitsLbl.setText("Data Bits:");

        dataBitsCB.setModel(
                new javax.swing.DefaultComboBoxModel<String>(new String[0]));

        javax.swing.GroupLayout DataBitsPanelLayout =
                new javax.swing.GroupLayout(dataBitsPanel);
        dataBitsPanel.setLayout(DataBitsPanelLayout);
        DataBitsPanelLayout
                .setHorizontalGroup(DataBitsPanelLayout
                        .createParallelGroup(
                                javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(DataBitsPanelLayout.createSequentialGroup()
                                .addContainerGap().addComponent(dataBitsLbl)
                                .addGap(50, 50, 50).addComponent(dataBitsCB, 0,
                                        181, Short.MAX_VALUE)
                        .addContainerGap()));
        DataBitsPanelLayout.setVerticalGroup(DataBitsPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                        DataBitsPanelLayout.createSequentialGroup()
                                .addContainerGap(
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addGroup(DataBitsPanelLayout
                                        .createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(dataBitsCB,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(dataBitsLbl))
                                .addGap(14, 14, 14)));

        parityPanel.setPreferredSize(new java.awt.Dimension(219, 35));

        parityLbl.setText("Parity:");

        parityCB.setModel(
                new javax.swing.DefaultComboBoxModel<String>(new String[0]));

        javax.swing.GroupLayout ParityPanelLayout =
                new javax.swing.GroupLayout(parityPanel);
        parityPanel.setLayout(ParityPanelLayout);
        ParityPanelLayout.setHorizontalGroup(ParityPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ParityPanelLayout.createSequentialGroup()
                        .addContainerGap().addComponent(parityLbl)
                        .addGap(64, 64, 64)
                        .addComponent(parityCB, 0,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addContainerGap()));
        ParityPanelLayout.setVerticalGroup(ParityPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                        ParityPanelLayout.createSequentialGroup()
                                .addContainerGap(
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addGroup(ParityPanelLayout
                                        .createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(parityLbl)
                                        .addComponent(parityCB,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(17, 17, 17)));

        stopBitsLbl.setText("Stop Bits:");

        stopBitsCB.setModel(
                new javax.swing.DefaultComboBoxModel<String>(new String[0]));

        javax.swing.GroupLayout StopBitsPanelLayout =
                new javax.swing.GroupLayout(stopBitsPanel);
        stopBitsPanel.setLayout(StopBitsPanelLayout);
        StopBitsPanelLayout.setHorizontalGroup(StopBitsPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(StopBitsPanelLayout.createSequentialGroup()
                        .addContainerGap().addComponent(stopBitsLbl)
                        .addGap(49, 49, 49)
                        .addComponent(stopBitsCB, 0,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addContainerGap()));
        StopBitsPanelLayout.setVerticalGroup(StopBitsPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                        StopBitsPanelLayout.createSequentialGroup()
                                .addContainerGap(
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addGroup(StopBitsPanelLayout
                                        .createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(stopBitsLbl)
                                        .addComponent(stopBitsCB,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(17, 17, 17)));

        javax.swing.GroupLayout jPanel1Layout =
                new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(portPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(baudRatePanel,
                        javax.swing.GroupLayout.DEFAULT_SIZE, 298,
                        Short.MAX_VALUE)
                .addComponent(dataBitsPanel,
                        javax.swing.GroupLayout.DEFAULT_SIZE, 298,
                        Short.MAX_VALUE)
                .addComponent(parityPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                        298, Short.MAX_VALUE)
                .addComponent(stopBitsPanel,
                        javax.swing.GroupLayout.Alignment.TRAILING,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(jPanel1Layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup().addComponent(
                        portPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(baudRatePanel,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dataBitsPanel,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(parityPanel,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopBitsPanel,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)));

        okBtn.setText("OK");
        okBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okBtnActionPerformed(evt);
            }
        });

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout =
                new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout
                        .createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(okBtn)
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancelBtn).addContainerGap())
                .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addGroup(layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cancelBtn).addComponent(okBtn))
                        .addContainerGap()));

        pack();
    }
    // CHECKSTYLE:ON

    @Override
    protected JRootPane createRootPane() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        rootPane.registerKeyboardAction(this, stroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rootPane;
    }

    /**
     * Accept changes.
     * 
     * @param evt
     *            Action event parameter.
     */
    private void okBtnActionPerformed(final ActionEvent evt) {
        try {
            target.setPortName(this.portCB.getSelectedItem().toString());
            target.setBaudRate(Integer
                    .parseInt(this.baudRate.getSelectedItem().toString()));
            target.setDataBits(Integer
                    .parseInt(this.dataBitsCB.getSelectedItem().toString()));
            target.setParity(gurux.io.Parity.valueOf(
                    this.parityCB.getSelectedItem().toString().toUpperCase()));
            target.setStopBits(gurux.io.StopBits.valueOf(this.stopBitsCB
                    .getSelectedItem().toString().toUpperCase()));
            accepted = true;
            this.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    /**
     * Discard changes.
     * 
     * @param evt
     *            Action event parameter.
     */
    private void cancelBtnActionPerformed(final ActionEvent evt) {
        this.dispose();
    }
}
