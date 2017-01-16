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

package gurux.serial;

import java.lang.reflect.Array;

import gurux.common.GXSynchronousMediaBase;
import gurux.common.ReceiveEventArgs;
import gurux.common.enums.TraceLevel;
import gurux.common.enums.TraceTypes;
import gurux.io.NativeCode;

/**
 * Receive thread listens serial port and sends received data to the listeners.
 * 
 * @author Gurux Ltd.
 */
class GXReceiveThread extends Thread {

    /**
     * If receiver buffer is empty how long is waited for new data.
     */
    static final int WAIT_TIME = 200;

    /**
     * Serial port handle.
     */
    private long comPort;
    /**
     * Parent component where notifies are send.
     */
    private final GXSerial parentMedia;

    /**
     * Amount of bytes received.
     */
    private long bytesReceived = 0;

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent component.
     * @param hComPort
     *            Handle for the serial port.
     */
    GXReceiveThread(final GXSerial parent, final long hComPort) {
        super("GXSerial " + String.valueOf(hComPort));
        comPort = hComPort;
        parentMedia = parent;
    }

    /**
     * Get amount of received bytes.
     * 
     * @return Amount of received bytes.
     */
    public final long getBytesReceived() {
        return bytesReceived;
    }

    /**
     * Reset amount of received bytes.
     */
    public final void resetBytesReceived() {
        bytesReceived = 0;
    }

    /**
     * Handle received data.
     * 
     * @param buffer
     *            Received data from the serial port.
     */
    private void handleReceivedData(final byte[] buffer) {
        int len = buffer.length;
        if (len == 0) {
            try {
                Thread.sleep(WAIT_TIME);
            } catch (Exception ex) {
                return;
            }
            return;
        }
        bytesReceived += len;
        int totalCount = 0;
        if (parentMedia.getIsSynchronous()) {
            gurux.common.TraceEventArgs arg = null;
            synchronized (parentMedia.getSyncBase().getSync()) {
                parentMedia.getSyncBase().appendData(buffer, 0, len);
                // Search End of Packet if given.
                if (parentMedia.getEop() != null) {
                    if (parentMedia.getEop() instanceof Array) {
                        for (Object eop : (Object[]) parentMedia.getEop()) {
                            totalCount = GXSynchronousMediaBase.indexOf(buffer,
                                    GXSynchronousMediaBase.getAsByteArray(eop),
                                    0, len);
                            if (totalCount != -1) {
                                break;
                            }
                        }
                    } else {
                        totalCount = GXSynchronousMediaBase.indexOf(buffer,
                                GXSynchronousMediaBase.getAsByteArray(
                                        parentMedia.getEop()),
                                0, len);
                    }
                }
                if (totalCount != -1) {
                    if (parentMedia.getTrace() == TraceLevel.VERBOSE) {
                        arg = new gurux.common.TraceEventArgs(
                                TraceTypes.RECEIVED, buffer, 0, totalCount + 1);
                    }
                    parentMedia.getSyncBase().setReceived();
                }
            }
            if (arg != null) {
                parentMedia.notifyTrace(arg);
            }
        } else {
            parentMedia.getSyncBase().resetReceivedSize();
            byte[] data = new byte[len];
            System.arraycopy(buffer, 0, data, 0, len);
            if (parentMedia.getTrace() == TraceLevel.VERBOSE) {
                parentMedia.notifyTrace(new gurux.common.TraceEventArgs(
                        TraceTypes.RECEIVED, data));
            }
            ReceiveEventArgs e =
                    new ReceiveEventArgs(data, parentMedia.getPortName());
            parentMedia.notifyReceived(e);
        }
    }

    @Override
    public final void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] buff = NativeCode.read(this.comPort,
                        parentMedia.getReadTimeout(), parentMedia.getClosing());
                // If connection is closed.
                if (buff.length == 0
                        && Thread.currentThread().isInterrupted()) {
                    parentMedia.setClosing(0);
                    break;
                }
                handleReceivedData(buff);
            } catch (Exception ex) {
                if (!Thread.currentThread().isInterrupted()) {
                    parentMedia
                            .notifyError(new RuntimeException(ex.getMessage()));
                } else {
                    break;
                }
            }
        }
    }
}