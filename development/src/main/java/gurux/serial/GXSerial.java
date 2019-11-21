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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import gurux.common.GXCommon;
import gurux.common.GXSync;
import gurux.common.GXSynchronousMediaBase;
import gurux.common.IGXMedia;
import gurux.common.IGXMedia2;
import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.ReceiveParameters;
import gurux.common.TraceEventArgs;
import gurux.common.enums.MediaState;
import gurux.common.enums.TraceLevel;
import gurux.common.enums.TraceTypes;
import gurux.io.BaudRate;
import gurux.io.Handshake;
import gurux.io.NativeCode;
import gurux.io.Parity;
import gurux.io.StopBits;
import gurux.serial.enums.AvailableMediaSettings;

/**
 * The GXSerial component determines methods that make the communication
 * possible using serial port connection.
 */
public class GXSerial implements IGXMedia, IGXMedia2, AutoCloseable {

    private int receiveDelay;

    private int asyncWaitTime;

    /**
     * Read buffer size.
     */
    static final int DEFUALT_READ_BUFFER_SIZE = 256;

    /**
     * Amount of default data bits.
     */
    static final int DEFAULT_DATA_BITS = 8;

    // Values are saved if port is not open and user try to set them.
    /**
     * Serial port baud rate.
     */
    private BaudRate baudRate = BaudRate.BAUD_RATE_9600;
    /**
     * Used data bits.
     */
    private int dataBits = DEFAULT_DATA_BITS;
    /**
     * Stop bits.
     */
    private StopBits stopBits = StopBits.ONE;
    /**
     * Used parity.
     */
    private Parity parity = Parity.NONE;

    /**
     * Closing handle for serial port library.
     */
    private long closing = 0;
    /**
     * Write timeout.
     */
    private int writeTimeout;
    /**
     * Read timeout.
     */
    private int readTimeout;
    /**
     * Is serial port initialized.
     */
    private static boolean initialized;
    /**
     * Read buffer size.
     */
    private int readBufferSize;
    /**
     * Receiver thread.
     */
    private GXReceiveThread receiver;
    /**
     * Handle to serial port.
     */
    private int hWnd;
    /**
     * Name of serial port.
     */
    private String portName;
    /**
     * Synchronously class.
     */
    private GXSynchronousMediaBase syncBase;
    /**
     * Amount of bytes sent.
     */
    private long bytesSend = 0;
    /**
     * Synchronous counter.
     */
    private int synchronous = 0;
    /**
     * Trace level.
     */
    private TraceLevel trace = TraceLevel.OFF;
    /**
     * End of packet.
     */
    private Object eop;
    /**
     * Configurable settings.
     */
    private int configurableSettings;
    /**
     * Media listeners.
     */
    private List<IGXMediaListener> mediaListeners =
            new ArrayList<IGXMediaListener>();

    /**
     * Constructor.
     */
    public GXSerial() {
        initialize();
        readBufferSize = DEFUALT_READ_BUFFER_SIZE;
        syncBase = new GXSynchronousMediaBase(readBufferSize);
        setConfigurableSettings(AvailableMediaSettings.ALL.getValue());
    }

    /**
     * Constructor.
     * 
     * @param port
     *            Serial port.
     * @param baudRateValue
     *            Baud rate.
     * @param dataBitsValue
     *            Data bits.
     * @param parityValue
     *            Parity.
     * @param stopBitsValue
     *            Stop bits.
     */
    public GXSerial(final String port, final BaudRate baudRateValue,
            final int dataBitsValue, final Parity parityValue,
            final StopBits stopBitsValue) {
        initialize();
        readBufferSize = DEFUALT_READ_BUFFER_SIZE;
        syncBase = new GXSynchronousMediaBase(readBufferSize);
        setConfigurableSettings(AvailableMediaSettings.ALL.getValue());
        setPortName(port);
        setBaudRate(baudRateValue);
        setDataBits(dataBitsValue);
        setParity(parityValue);
        setStopBits(stopBitsValue);
    }

    /**
     * Returns synchronous class used to communicate synchronously.
     * 
     * @return Synchronous class.
     */
    final GXSynchronousMediaBase getSyncBase() {
        return syncBase;
    }

    /**
     * Get handle for closing.
     * 
     * @return Handle for closing.
     */
    final long getClosing() {
        return closing;
    }

    /**
     * Set handle for closing.
     * 
     * @param value
     *            Handle for closing.
     */
    final void setClosing(final long value) {
        closing = value;
    }

    /**
     * Is Windows operating system.
     * 
     * @param os
     *            Operating system name.
     * @return True if Windows.
     */
    static boolean isWindows(final String os) {
        return (os.indexOf("win") >= 0);
    }

    /**
     * Is Mac operating system.
     * 
     * @param os
     *            Operating system name.
     * @return True if Mac.
     */
    static boolean isMac(final String os) {
        return (os.indexOf("mac") >= 0);
    }

    /**
     * Is Unix operating system.
     * 
     * @param os
     *            Operating system name.
     * @return True if Unix.
     */
    static boolean isUnix(final String os) {
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0
                || os.indexOf("aix") >= 0);
    }

    /**
     * Is Solaris operating system.
     * 
     * @param os
     *            Operating system name.
     * @return True if Solaris.
     */
    static boolean isSolaris(final String os) {
        return (os.indexOf("sunos") >= 0);
    }

    /**
     * Initialize Gurux serial port library.
     */
    private static void initialize() {
        if (!initialized) {
            String path;
            String os = System.getProperty("os.name").toLowerCase();
            boolean is32Bit =
                    System.getProperty("sun.arch.data.model").equals("32");
            if (isWindows(os)) {
                if (is32Bit) {
                    path = "win32";
                } else {
                    path = "win64";
                }
            } else if (isUnix(os)) {
                if (System.getProperty("os.arch").indexOf("arm") != -1) {
                    if (is32Bit) {
                        path = "arm32";
                    } else {
                        path = "arm64";
                    }
                } else {
                    if (is32Bit) {
                        path = "linux86";
                    } else {
                        path = "linux64";
                    }
                }
            } else {
                throw new RuntimeException("Invald operating system. " + os);
            }
            File file;
            try {
                file = File.createTempFile("gurux.serial.java", ".dll");
            } catch (IOException e1) {
                throw new RuntimeException(
                        "Failed to load file. " + path + "/gurux.serial.java");
            }
            try (InputStream in = GXSerial.class.getResourceAsStream("/" + path
                    + "/" + System.mapLibraryName("gurux.serial.java"))) {
                Files.copy(in, file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                System.load(file.getAbsolutePath());
            } catch (Exception e) {
                throw new RuntimeException("Failed to load file. " + path
                        + "/gurux.serial.java" + e.toString());
            }
        }
    }

    /**
     * Gets an array of serial port names for the current computer.
     * 
     * @return Collection of available serial ports.
     */
    public static String[] getPortNames() {
        initialize();
        return NativeCode.getPortNames();
    }

    /**
     * Get baud rates supported by given serial port.
     * 
     * @param portName
     *            Name of serial port.
     * @return Collection of available baud rates.
     */
    public static final BaudRate[]
            getAvailableBaudRates(final String portName) {
        return new BaudRate[] { BaudRate.BAUD_RATE_300, BaudRate.BAUD_RATE_600,
                BaudRate.BAUD_RATE_1800, BaudRate.BAUD_RATE_2400,
                BaudRate.BAUD_RATE_4800, BaudRate.BAUD_RATE_9600,
                BaudRate.BAUD_RATE_19200, BaudRate.BAUD_RATE_38400 };
    }

    @Override
    protected final void finalize() throws Throwable {
        super.finalize();
        if (isOpen()) {
            close();
        }
    }

    @Override
    public final TraceLevel getTrace() {
        return trace;
    }

    @Override
    public final void setTrace(final TraceLevel value) {
        trace = value;
        syncBase.setTrace(value);
    }

    /**
     * Notify that property has changed.
     * 
     * @param info
     *            Name of changed property.
     */
    private void notifyPropertyChanged(final String info) {
        for (IGXMediaListener listener : mediaListeners) {
            listener.onPropertyChanged(this,
                    new PropertyChangedEventArgs(info));
        }
    }

    /**
     * Notify clients from error occurred.
     * 
     * @param ex
     *            Occurred error.
     */
    final void notifyError(final RuntimeException ex) {
        for (IGXMediaListener listener : mediaListeners) {
            listener.onError(this, ex);
            if (trace.ordinal() >= TraceLevel.ERROR.ordinal()) {
                listener.onTrace(this,
                        new TraceEventArgs(TraceTypes.ERROR, ex));
            }
        }
    }

    /**
     * Notify clients from new data received.
     * 
     * @param e
     *            Received event argument.
     */
    final void notifyReceived(final ReceiveEventArgs e) {
        for (IGXMediaListener listener : mediaListeners) {
            listener.onReceived(this, e);
        }
    }

    /**
     * Notify clients from trace events.
     * 
     * @param e
     *            Trace event argument.
     */
    final void notifyTrace(final TraceEventArgs e) {
        for (IGXMediaListener listener : mediaListeners) {
            listener.onTrace(this, e);
        }
    }

    @Override
    public final int getConfigurableSettings() {
        return configurableSettings;
    }

    @Override
    public final void setConfigurableSettings(final int value) {
        configurableSettings = value;
    }

    @Override
    public final boolean properties(final javax.swing.JFrame parent) {
        GXSettings dlg = new GXSettings(parent, true, this);
        dlg.pack();
        dlg.setVisible(true);
        return dlg.isAccepted();
    }

    /**
     * Displays the copyright of the control, user license, and version
     * information, in a dialog box.
     */
    public final void aboutBox() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void send(final Object data, final String target)
            throws Exception {
        if (hWnd == 0) {
            throw new RuntimeException("Serial port is not open.");
        }
        if (trace == TraceLevel.VERBOSE) {
            notifyTrace(new TraceEventArgs(TraceTypes.SENT, data));
        }
        // Reset last position if end of packet is used.
        synchronized (syncBase.getSync()) {
            syncBase.resetLastPosition();
        }
        byte[] buff = GXSynchronousMediaBase.getAsByteArray(data);
        if (buff == null) {
            throw new IllegalArgumentException(
                    "Data send failed. Invalid data.");
        }
        NativeCode.write(hWnd, buff, writeTimeout);
        this.bytesSend += buff.length;
    }

    /**
     * Notify client from media state change.
     * 
     * @param state
     *            New media state.
     */
    private void notifyMediaStateChange(final MediaState state) {
        for (IGXMediaListener listener : mediaListeners) {
            if (trace.ordinal() >= TraceLevel.ERROR.ordinal()) {
                listener.onTrace(this,
                        new TraceEventArgs(TraceTypes.INFO, state));
            }
            listener.onMediaStateChange(this, new MediaStateEventArgs(state));
        }
    }

    @Override
    public final void open() throws Exception {
        close();
        try {
            if (portName == null || portName == "") {
                throw new IllegalArgumentException(
                        "Serial port is not selected.");
            }
            synchronized (syncBase.getSync()) {
                syncBase.resetLastPosition();
            }
            notifyMediaStateChange(MediaState.OPENING);
            if (trace.ordinal() >= TraceLevel.INFO.ordinal()) {
                String eopString = "None";
                if (getEop() instanceof byte[]) {
                    eopString = GXCommon.bytesToHex((byte[]) getEop());
                } else if (getEop() != null) {
                    eopString = getEop().toString();
                }
                notifyTrace(new TraceEventArgs(TraceTypes.INFO,
                        "Settings: Port: " + this.getPortName() + " Baud Rate: "
                                + getBaudRate() + " Data Bits: "
                                + String.valueOf(getDataBits()) + " Parity: "
                                + String.valueOf(getParity()) + " Stop Bits: "
                                + String.valueOf(getStopBits()) + " Eop:"
                                + eopString));
            }
            long[] tmp = new long[1];
            hWnd = NativeCode.openSerialPort(portName, tmp);
            // If user has change values before open.
            if (baudRate != BaudRate.BAUD_RATE_9600) {
                setBaudRate(baudRate);
            }
            if (dataBits != DEFAULT_DATA_BITS) {
                setDataBits(dataBits);
            }
            if (parity != Parity.NONE) {
                setParity(parity);
            }
            if (stopBits != StopBits.ONE) {
                setStopBits(stopBits);
            }
            closing = tmp[0];
            setRtsEnable(true);
            setDtrEnable(true);
            receiver = new GXReceiveThread(this, hWnd);
            receiver.start();
            notifyMediaStateChange(MediaState.OPEN);
        } catch (Exception ex) {
            close();
            throw ex;
        }
    }

    @Override
    public final void close() {
        if (hWnd != 0) {
            if (receiver != null) {
                receiver.interrupt();
                receiver = null;
            }
            try {
                notifyMediaStateChange(MediaState.CLOSING);
            } catch (RuntimeException ex) {
                notifyError(ex);
                throw ex;
            } finally {
                try {
                    NativeCode.closeSerialPort(hWnd, closing);
                } catch (java.lang.Exception e) {
                    // Ignore all errors on close.
                }
                hWnd = 0;
                notifyMediaStateChange(MediaState.CLOSED);
                bytesSend = 0;
                syncBase.resetReceivedSize();
            }
        }
    }

    /**
     * Used baud rate for communication. Can be changed without disconnecting.
     * 
     * @return Used baud rate.
     */
    public final BaudRate getBaudRate() {
        if (hWnd == 0) {
            return baudRate;
        }
        return BaudRate.forValue(NativeCode.getBaudRate(hWnd));
    }

    /**
     * Set new baud rate.
     * 
     * @param value
     *            New baud rate.
     */
    public final void setBaudRate(final BaudRate value) {
        boolean change = getBaudRate() != value;
        if (change) {
            if (hWnd == 0) {
                baudRate = value;
            } else {
                NativeCode.setBaudRate(hWnd, value.getValue());
            }
            notifyPropertyChanged("BaudRate");
        }
    }

    /**
     * Get break state.
     * 
     * @return True if the port is in a break state; otherwise, false.
     */
    public final boolean getBreakState() {
        return NativeCode.getBreakState(hWnd);
    }

    /**
     * Set break state.
     * 
     * @param value
     *            True if the port is in a break state; otherwise, false.
     */
    public final void setBreakState(final boolean value) {
        boolean change;
        change = getBreakState() != value;
        if (change) {
            NativeCode.setBreakState(hWnd, value);
            notifyPropertyChanged("BreakState");
        }
    }

    /**
     * Gets the number of bytes in the receive buffer.
     * 
     * @return Amount of read bytes.
     */
    public final int getBytesToRead() {
        return NativeCode.getBytesToRead(hWnd);
    }

    /**
     * Gets the number of bytes in the send buffer.
     * 
     * @return Amount of bytes to write in the send buffer.
     */
    public final int getBytesToWrite() {
        return NativeCode.getBytesToWrite(hWnd);
    }

    /**
     * Gets the state of the Carrier Detect line for the port.
     * 
     * @return Is Carrier Detect in holding state.
     */
    public final boolean getCDHolding() {
        return NativeCode.getCDHolding(hWnd);
    }

    /**
     * Gets the state of the Clear-to-Send line.
     * 
     * @return Clear-to-Send state.
     */
    public final boolean getCtsHolding() {
        return NativeCode.getCtsHolding(hWnd);
    }

    /**
     * Gets the standard length of data bits per byte.
     * 
     * @return Amount of data bits.
     */
    public final int getDataBits() {
        if (hWnd == 0) {
            return dataBits;
        }
        return NativeCode.getDataBits(hWnd);
    }

    /**
     * Sets the standard length of data bits per byte.
     * 
     * @param value
     *            Amount of data bits.
     */
    public final void setDataBits(final int value) {
        boolean change;
        change = getDataBits() != value;
        if (change) {
            if (hWnd == 0) {
                dataBits = value;
            } else {
                NativeCode.setDataBits(hWnd, value);
            }
            notifyPropertyChanged("DataBits");
        }
    }

    /**
     * Gets the state of the Data Set Ready (DSR) signal.
     * 
     * @return Is Data Set Ready set.
     */
    public final boolean getDsrHolding() {
        return NativeCode.getDsrHolding(hWnd);
    }

    /**
     * Get is Data Terminal Ready (DTR) signal enabled.
     * 
     * @return Is DTR enabled.
     */
    public final boolean getDtrEnable() {
        return NativeCode.getDtrEnable(hWnd);
    }

    /**
     * Set is Data Terminal Ready (DTR) signal enabled.
     * 
     * @param value
     *            Is DTR enabled.
     */
    public final void setDtrEnable(final boolean value) {
        boolean change;
        change = getDtrEnable() != value;
        NativeCode.setDtrEnable(hWnd, value);
        if (change) {
            notifyPropertyChanged("DtrEnable");
        }
    }

    /**
     * Gets the handshaking protocol for serial port transmission of data.
     * 
     * @return Used handshake protocol.
     */
    public final Handshake getHandshake() {
        return Handshake.values()[NativeCode.getHandshake(hWnd)];
    }

    /**
     * Sets the handshaking protocol for serial port transmission of data.
     * 
     * @param value
     *            Handshake protocol.
     */
    public final void setHandshake(final Handshake value) {
        boolean change;
        change = getHandshake() != value;
        if (change) {
            NativeCode.setHandshake(hWnd, value.ordinal());
            notifyPropertyChanged("Handshake");
        }
    }

    @Override
    public final boolean isOpen() {
        return hWnd != 0;
    }

    /**
     * Gets the parity-checking protocol.
     * 
     * @return Used parity.
     */
    public final Parity getParity() {
        if (hWnd == 0) {
            return parity;
        }
        return Parity.values()[NativeCode.getParity(hWnd)];
    }

    /**
     * Sets the parity-checking protocol.
     * 
     * @param value
     *            Used parity.
     */
    public final void setParity(final Parity value) {
        boolean change;
        change = getParity() != value;
        if (change) {
            if (hWnd == 0) {
                parity = value;
            } else {
                NativeCode.setParity(hWnd, value.ordinal());
            }
            notifyPropertyChanged("Parity");
        }
    }

    /**
     * Gets the port for communications, including but not limited to all
     * available COM ports.
     * 
     * @return Used serial port
     */
    public final String getPortName() {
        return portName;
    }

    /**
     * Sets the port for communications, including but not limited to all
     * available COM ports.
     * 
     * @param value
     *            Used serial port.
     */
    public final void setPortName(final String value) {
        boolean change;
        change = !value.equals(portName);
        portName = value;
        if (change) {
            notifyPropertyChanged("PortName");
        }
    }

    /**
     * Gets the size of the serial port input buffer.
     * 
     * @return Size of input buffer.
     */
    public final int getReadBufferSize() {
        return readBufferSize;
    }

    /**
     * Sets the size of the serial port input buffer.
     * 
     * @param value
     *            Size of input buffer.
     */
    public final void setReadBufferSize(final int value) {
        boolean change;
        change = getReadBufferSize() != value;
        if (change) {
            readBufferSize = value;
            notifyPropertyChanged("ReadBufferSize");
        }
    }

    /**
     * Gets the number of milliseconds before a time-out occurs when a read
     * operation does not finish.
     * 
     * @return Read timeout.
     */
    public final int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the number of milliseconds before a time-out occurs when a read
     * operation does not finish.
     * 
     * @param value
     *            Read timeout.
     */
    public final void setReadTimeout(final int value) {
        boolean change = readTimeout != value;
        readTimeout = value;
        if (change) {
            notifyPropertyChanged("ReadTimeout");
        }
    }

    /**
     * Gets a value indicating whether the Request to Send (RTS) signal is
     * enabled during serial communication.
     * 
     * @return Is RTS enabled.
     */
    public final boolean getRtsEnable() {
        return NativeCode.getRtsEnable(hWnd);
    }

    /**
     * Sets a value indicating whether the Request to Send (RTS) signal is
     * enabled during serial communication.
     * 
     * @param value
     *            Is RTS enabled.
     */
    public final void setRtsEnable(final boolean value) {
        boolean change;
        change = getRtsEnable() != value;
        NativeCode.setRtsEnable(hWnd, value);
        if (change) {
            notifyPropertyChanged("RtsEnable");
        }
    }

    /**
     * Gets the standard number of stop bits per byte.
     * 
     * @return Used stop bits.
     */
    public final StopBits getStopBits() {
        if (hWnd == 0) {
            return stopBits;
        }
        return StopBits.values()[NativeCode.getStopBits(hWnd)];
    }

    /**
     * Sets the standard number of stop bits per byte.
     * 
     * @param value
     *            Used stop bits.
     */
    public final void setStopBits(final StopBits value) {
        boolean change;
        change = getStopBits() != value;
        if (change) {
            if (hWnd == 0) {
                stopBits = value;
            } else {
                NativeCode.setStopBits(hWnd, value.ordinal());
            }
            notifyPropertyChanged("StopBits");
        }
    }

    /**
     * Gets the number of milliseconds before a time-out occurs when a write
     * operation does not finish.
     * 
     * @return Used time out.
     */
    public final int getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * Sets the number of milliseconds before a time-out occurs when a write
     * operation does not finish.
     * 
     * @param value
     *            Used time out.
     */
    public final void setWriteTimeout(final int value) {
        boolean change = writeTimeout != value;
        if (change) {
            writeTimeout = value;
            notifyPropertyChanged("WriteTimeout");
        }
    }

    @Override
    public final <T> boolean receive(final ReceiveParameters<T> args) {
        return syncBase.receive(args);
    }

    @Override
    public final long getBytesSent() {
        return bytesSend;
    }

    @Override
    public final long getBytesReceived() {
        return receiver.getBytesReceived();
    }

    @Override
    public final void resetByteCounters() {
        bytesSend = 0;
        receiver.resetBytesReceived();
    }

    @Override
    public final String getSettings() {
        StringBuilder sb = new StringBuilder();
        String nl = System.getProperty("line.separator");

        if (portName != null && !portName.isEmpty()) {
            sb.append("<Port>");
            sb.append(portName);
            sb.append("</Port>");
            sb.append(nl);
        }
        if (baudRate != BaudRate.BAUD_RATE_9600) {
            sb.append("<BaudRate>");
            sb.append(String.valueOf(baudRate.getValue()));
            sb.append("</BaudRate>");
            sb.append(nl);
        }
        if (stopBits != StopBits.ONE) {
            sb.append("<StopBits>");
            sb.append(String.valueOf(stopBits.ordinal()));
            sb.append("</StopBits>");
            sb.append(nl);
        }
        if (parity != Parity.NONE) {
            sb.append("<Parity>");
            sb.append(String.valueOf(parity.ordinal()));
            sb.append("</Parity>");
            sb.append(nl);
        }
        if (dataBits != DEFAULT_DATA_BITS) {
            sb.append("<DataBits>");
            sb.append(String.valueOf(dataBits));
            sb.append("</DataBits>");
            sb.append(nl);
        }
        return sb.toString();
    }

    @Override
    public final void setSettings(final String value) {
        // Reset to default values.
        portName = "";
        baudRate = BaudRate.BAUD_RATE_9600;
        stopBits = StopBits.ONE;
        parity = Parity.NONE;
        dataBits = DEFAULT_DATA_BITS;

        if (value != null && !value.isEmpty()) {
            try {
                DocumentBuilderFactory factory =
                        DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                StringBuilder sb = new StringBuilder();
                if (value.startsWith("<?xml version=\"1.0\"?>")) {
                    sb.append(value);
                } else {
                    String nl = System.getProperty("line.separator");
                    sb.append("<?xml version=\"1.0\"?>\r\n");
                    sb.append(nl);
                    sb.append("<Net>");
                    sb.append(value);
                    sb.append(nl);
                    sb.append("</Net>");
                }
                InputSource is =
                        new InputSource(new StringReader(sb.toString()));
                Document doc = builder.parse(is);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getChildNodes();
                if (nList.getLength() != 1) {
                    throw new IllegalArgumentException(
                            "Invalid XML root node.");
                }
                nList = nList.item(0).getChildNodes();
                for (int pos = 0; pos < nList.getLength(); ++pos) {
                    Node it = nList.item(pos);
                    if (it.getNodeType() == Node.ELEMENT_NODE) {
                        if ("Port".equalsIgnoreCase(it.getNodeName())) {
                            setPortName(it.getFirstChild().getNodeValue());
                        } else if ("BaudRate"
                                .equalsIgnoreCase(it.getNodeName())) {
                            setBaudRate(BaudRate.forValue(Integer.parseInt(
                                    it.getFirstChild().getNodeValue())));
                        } else if ("StopBits"
                                .equalsIgnoreCase(it.getNodeName())) {
                            setStopBits(StopBits.values()[Integer.parseInt(
                                    it.getFirstChild().getNodeValue())]);
                        } else if ("Parity"
                                .equalsIgnoreCase(it.getNodeName())) {
                            setParity(Parity.values()[Integer.parseInt(
                                    it.getFirstChild().getNodeValue())]);
                        } else if ("DataBits"
                                .equalsIgnoreCase(it.getNodeName())) {
                            setDataBits(Integer.parseInt(
                                    it.getFirstChild().getNodeValue()));
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    @Override
    public final void copy(final Object target) {
        GXSerial tmp = (GXSerial) target;
        setPortName(tmp.getPortName());
        setBaudRate(tmp.getBaudRate());
        setStopBits(tmp.getStopBits());
        setParity(tmp.getParity());
        setDataBits(tmp.getDataBits());
    }

    @Override
    public final String getName() {
        return getPortName();
    }

    @Override
    public final String getMediaType() {
        return "Serial";
    }

    @Override
    public final Object getSynchronous() {
        synchronized (this) {
            int[] tmp = new int[] { synchronous };
            GXSync obj = new GXSync(tmp);
            synchronous = tmp[0];
            return obj;
        }
    }

    @Override
    public final boolean getIsSynchronous() {
        synchronized (this) {
            return synchronous != 0;
        }
    }

    @Override
    public final void resetSynchronousBuffer() {
        synchronized (syncBase.getSync()) {
            syncBase.resetReceivedSize();
        }
    }

    @Override
    public final void validate() {
        if (getPortName() == null || getPortName().length() == 0) {
            throw new RuntimeException("Invalid port name.");
        }
    }

    @Override
    public final Object getEop() {
        return eop;
    }

    @Override
    public final void setEop(final Object value) {
        eop = value;
    }

    @Override
    public final void addListener(final IGXMediaListener listener) {
        mediaListeners.add(listener);
    }

    @Override
    public final void removeListener(final IGXMediaListener listener) {
        mediaListeners.remove(listener);
    }

    @Override
    public int getReceiveDelay() {
        return receiveDelay;
    }

    @Override
    public void setReceiveDelay(final int value) {
        receiveDelay = value;
    }

    @Override
    public int getAsyncWaitTime() {
        return asyncWaitTime;
    }

    @Override
    public void setAsyncWaitTime(final int value) {
        asyncWaitTime = value;
    }

    @Override
    public Object getAsyncWaitHandle() {
        return null;
    }
}