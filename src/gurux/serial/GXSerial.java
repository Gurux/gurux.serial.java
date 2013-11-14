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

import gurux.io.StopBits;
import gurux.io.NativeCode;
import gurux.io.Parity;
import gurux.io.Handshake;
import gurux.common.*;
import java.util.ArrayList;
import java.util.List;

/** 
 The GXSerial component determines methods that make 
 * the communication possible using serial port connection. 
*/
public class GXSerial implements IGXMedia
{   
    //Values are saved if port is not open and user try to set them.
    int m_BaudRate = 9600;
    int m_DataBits = 8;    
    StopBits m_StopBits = StopBits.ONE;
    Parity m_Parity = Parity.NONE;
    
    long m_Closing = 0;
    int m_WriteTimeout;
    int m_ReadTimeout;
    private static boolean Initialized;
    int m_ReadBufferSize;
    GXReceiveThread Receiver;
    int m_hWnd;        
    String m_PortName;    
    GXSynchronousMediaBase m_syncBase;
    public long m_BytesReceived = 0;
    private long m_BytesSend = 0;
    private int m_Synchronous = 0;
    private TraceLevel m_Trace = TraceLevel.OFF;
    private Object privateEop;        
    private int ConfigurableSettings;
    private List<IGXMediaListener> MediaListeners = new ArrayList<IGXMediaListener>();
    
    /** 
     Constructor.
    */
    public GXSerial()
    {   
        initialize();
        m_ReadBufferSize = 256;        
        m_syncBase = new GXSynchronousMediaBase(m_ReadBufferSize);
        setConfigurableSettings(AvailableMediaSettings.All.getValue());
    }
    
    static void initialize()
    {
        if (!Initialized)
        {
            System.loadLibrary("gurux.serial.java");
            Initialized = true;        
        }
    }
    
    /** 
    Gets an array of serial port names for the current computer.

    @return 
    */
    public static String[] getPortNames()
    {                
        initialize();
        return NativeCode.getPortNames();
    }

    
    /** 
     Destructor.
    */
    @Override
    @SuppressWarnings("FinalizeDeclaration")    
    protected void finalize() throws Throwable
    {
        super.finalize();
        if (isOpen())
        {
            close();
        }
    }    

    /** 
     What level of tracing is used.
    */
    @Override
    public final TraceLevel getTrace()
    {
        return m_Trace;
    }
    @Override
    public final void setTrace(TraceLevel value)
    {
        m_Trace = m_syncBase.Trace = value;
    }
    
    private void NotifyPropertyChanged(String info)
    {
        for (IGXMediaListener listener : MediaListeners) 
        {
            listener.onPropertyChanged(this, new PropertyChangedEventArgs(info));
        }
    }         

    void notifyError(RuntimeException ex)
    {
        for (IGXMediaListener listener : MediaListeners) 
        {
            listener.onError(this, ex);
            if (m_Trace.ordinal() >= TraceLevel.ERROR.ordinal())
            {
                listener.onTrace(this, new TraceEventArgs(TraceTypes.ERROR, ex));
            }
        }
    }
    
    void notifyReceived(ReceiveEventArgs e)
    {
        for (IGXMediaListener listener : MediaListeners) 
        {
            listener.onReceived(this, e);
        }
    }
    
    void notifyTrace(TraceEventArgs e)
    {
        for (IGXMediaListener listener : MediaListeners) 
        {                
            listener.onTrace(this, e);
        }
    }
       
    /** <inheritdoc cref="IGXMedia.ConfigurableSettings"/>
    */
    @Override
    public final int getConfigurableSettings()
    {            
        return ConfigurableSettings;
    }
    @Override
    public final void setConfigurableSettings(int value)
    {
        this.ConfigurableSettings = value;
    }

    /**    
     Displays the copyright of the control, user license, and version information, in a dialog box. 
    */
    public final void aboutBox()
    {
        throw new UnsupportedOperationException();
    }

    /** 
     Sends data asynchronously. <br/>
     No reply from the receiver, whether or not the operation was successful, is expected.

     @param data Data to send to the device.
     @param receiver Not used.
     Reply data is received through OnReceived event.<br/>     		
     @see OnReceived OnReceived
     @see Open Open
     @see Close Close 
    */
    @Override
    public final void send(Object data, String receiver) throws Exception
    {
        if (m_hWnd == 0)
        {
            throw new RuntimeException("Serial port is not open.");
        }
        if (m_Trace == TraceLevel.VERBOSE)
        {
            notifyTrace(new TraceEventArgs(TraceTypes.SENT, data));
        }
        //Reset last position if Eop is used.
        synchronized (m_syncBase.m_ReceivedSync)
        {
            m_syncBase.m_LastPosition = 0;
        }
        byte[] buff = GXSynchronousMediaBase.getAsByteArray(data);
        if (buff == null)
        {
            throw new IllegalArgumentException("Data send failed. Invalid data.");
        }
        NativeCode.write(m_hWnd, buff, m_WriteTimeout);
        this.m_BytesSend += buff.length;
    }

    private void NotifyMediaStateChange(MediaState state)
    {
        for (IGXMediaListener listener : MediaListeners) 
        {                
            if (m_Trace.ordinal() >= TraceLevel.ERROR.ordinal())
            {
                listener.onTrace(this, new TraceEventArgs(TraceTypes.INFO, state));
            }
            listener.onMediaStateChange(this, new MediaStateEventArgs(state));
        }
    }
  
    /** 
     todo
    */
    @Override    
    public final void open() throws Exception
    {
        close();
        try
        {
            synchronized (m_syncBase.m_ReceivedSync)
            {
                m_syncBase.m_LastPosition = 0;
            }
            NotifyMediaStateChange(MediaState.OPENING);
            if (m_Trace.ordinal() >= TraceLevel.INFO.ordinal())
            {
                String eop = "None";
                if (getEop() instanceof byte[])
                {
                }
                else if (getEop() != null)
                {
                    eop = getEop().toString();
                }
                notifyTrace(new TraceEventArgs(TraceTypes.INFO, "Settings: Port: " + this.getPortName() + " Baud Rate: " + getBaudRate() + " Data Bits: " + (new Integer(getDataBits())).toString() + " Parity: " + getParity().toString() + " Stop Bits: " + getStopBits().toString() + " Eop:" + eop));
            }             
            long tmp[] = new long[1];
            m_hWnd = NativeCode.openSerialPort(m_PortName, tmp); 
            //If user has change values before open.
            if (m_BaudRate != 9600)
            {
                setBaudRate(m_BaudRate);
            }
            if (m_DataBits != 8)
            {
                setDataBits(m_DataBits);
            }
            if (m_Parity != Parity.NONE)
            {
                setParity(m_Parity);
            }
            if (m_StopBits != StopBits.ONE)
            {
                setStopBits(m_StopBits);
            }
            m_Closing = tmp[0];
            setRtsEnable(true);
            setDtrEnable(true);
            Receiver = new GXReceiveThread(this, m_hWnd);
            Receiver.start();
            NotifyMediaStateChange(MediaState.OPEN);
        }
        catch (Exception ex)
        {
            close();
            throw ex;
        }        
    }

    /** 
     * <inheritdoc cref="IGXMedia.Close"/>        
    */    
    @Override       
    public final void close()
    {       
        if (m_hWnd != 0)
        {    
            if (Receiver != null)
            {
                Receiver.interrupt();                    
                Receiver = null;
            } 
            try
            {
                NotifyMediaStateChange(MediaState.CLOSING);
            }
            catch (RuntimeException ex)
            {
                notifyError(ex);
                throw ex;
            }
            finally
            {
                try
                {
                    NativeCode.closeSerialPort(m_hWnd, m_Closing);
                }
                catch (java.lang.Exception e)
                {
                    //Ignore all errors on close.                    
                }
                m_hWnd = 0;    
                NotifyMediaStateChange(MediaState.CLOSED);
                m_BytesSend = m_BytesReceived = 0;
                m_syncBase.m_ReceivedSize = 0;
            }
        }                
    }

    /** 
    Used baud rate for communication.

    Can be changed without disconnecting.
  */ 
    public final int getBaudRate()
    {
        if (m_hWnd == 0)
        {
            return m_BaudRate;        
        }
        return NativeCode.getBaudRate(m_hWnd);
    }
    public final void setBaudRate(int value)
    {       
        boolean change = getBaudRate() != value;            
        if (change)
        {
            if (m_hWnd == 0)
            {
                m_BaudRate = value;        
            }
            else
            {
                NativeCode.setBaudRate(m_hWnd, value);
            }
            NotifyPropertyChanged("BaudRate");
        }
    }

    /** 
     True if the port is in a break state; otherwise, false.
    */
    public final boolean getBreakState()
    {
        return NativeCode.getBreakState(m_hWnd);
    }
    public final void setBreakState(boolean value)
    {
        boolean change;
        change = getBreakState() != value;
        if (change)
        {
            NativeCode.setBreakState(m_hWnd, value);
            NotifyPropertyChanged("BreakState");
        }
    }

    /* 
     * Gets the number of bytes in the receive buffer.
    */
    public final int getBytesToRead()
    {
        return NativeCode.getBytesToRead(m_hWnd);
    }

    /* 
     * Gets the number of bytes in the send buffer.
     */ 
    public final int getBytesToWrite()
    {
        return NativeCode.getBytesToWrite(m_hWnd);
    }

    /* 
    * Gets the state of the Carrier Detect line for the port.
    */
    public final boolean getCDHolding()
    {
        return NativeCode.getCDHolding(m_hWnd);
    }

    /* 
     * Gets the state of the Clear-to-Send line.
     */    
    public final boolean getCtsHolding()
    {
        return NativeCode.getCtsHolding(m_hWnd);
    }
   
    /** 
     * Gets or sets the standard length of data bits per byte.   
     */
    public final int getDataBits()
    {
        if (m_hWnd == 0)
        {
            return m_DataBits;
        }
        return NativeCode.getDataBits(m_hWnd);
    }

    public final void setDataBits(int value)
    {
        boolean change;
        change = getDataBits() != value;        
        if (change)
        {
            if (m_hWnd == 0)
            {
                m_DataBits = value;
            }
            else
            {
                NativeCode.setDataBits(m_hWnd, value);
            }
            NotifyPropertyChanged("DataBits");
        }
    }
        /** 
         Gets the state of the Data Set Ready (DSR) signal.
        */
        public final boolean getDsrHolding()
        {
            return NativeCode.getDsrHolding(m_hWnd);
        }

        /* 
         * Gets or sets a value that enables the Data Terminal Ready 
         * (DTR) signal during serial communication.        
        */
        public final boolean getDtrEnable()
        {
            return NativeCode.getDtrEnable(m_hWnd);
        }
        public final void setDtrEnable(boolean value)
        {
            boolean change;
            change = getDtrEnable() != value;
            NativeCode.setDtrEnable(m_hWnd, value);
            if (change)
            {                
                NotifyPropertyChanged("DtrEnable");
            }
        }
        
        /* 
         * Gets or sets the handshaking protocol for serial port transmission of data.
        */
        public final Handshake getHandshake()
        {
            return Handshake.values()[NativeCode.getHandshake(m_hWnd)];
        }
        public final void setHandshake(Handshake value)
        {
            boolean change;
            change = getHandshake() != value;
            if (change)
            {
                NativeCode.setHandshake(m_hWnd, value.ordinal());
                NotifyPropertyChanged("Handshake");
            }
        }

    /** <inheritdoc cref="IGXMedia.IsOpen"/>
     <seealso char="Connect">Open
     <seealso char="Close">Close
    */
    @Override
    public final boolean isOpen()
    {
        return m_hWnd != 0;
    }
    
    /** 
     * Gets or sets the parity-checking protocol.
     */
    public final Parity getParity()
    {
        if (m_hWnd == 0)
        {
            return m_Parity;
        }
        return Parity.values()[NativeCode.getParity(m_hWnd)];
    }

    public final void setParity(Parity value)
    {        
        boolean change;
        change = getParity() != value;        
        if (change)
        {
            if (m_hWnd == 0)
            {
                m_Parity = value;
            }
            else
            {
                NativeCode.setParity(m_hWnd, value.ordinal());
            }
            NotifyPropertyChanged("Parity");
        }
    }

    /** 
     Gets or sets the port for communications, including but not limited to all available COM ports.
    */
    public final String getPortName()
    {
        return m_PortName;
    }
                
    public final void setPortName(String value)
    {
        boolean change;
        change = !value.equals(m_PortName);
        m_PortName = value;
        if (change)
        {
            NotifyPropertyChanged("PortName");
        }
    }

    /*
     * Gets or sets the size of the System.IO.Ports.SerialPort input buffer.
     */ 
    public final int getReadBufferSize()
    {
        return m_ReadBufferSize;
    }
    public final void setReadBufferSize(int value)
    {
        boolean change;
        change = getReadBufferSize() != value;
        if (change)
        {
            m_ReadBufferSize = value;
            NotifyPropertyChanged("ReadBufferSize");
        }
    }

    /* 
    * Gets or sets the number of milliseconds before a time-out occurs when a read operation does not finish.
    */ 
    public final int getReadTimeout()
    {
        return m_ReadTimeout;
    }
    public final void setReadTimeout(int value)
    {
        boolean change = m_ReadTimeout != value;
        m_ReadTimeout = value;
        if (change)
        {
            NotifyPropertyChanged("ReadTimeout");
        }
    }
    
    /* 
     * Gets or sets a value indicating whether the 
     * Request to Send (RTS) signal is enabled during serial communication.
    */
    public final boolean getRtsEnable()
    {
        return NativeCode.getRtsEnable(m_hWnd);
    }
    public final void setRtsEnable(boolean value)
    {
        boolean change;
        change = getRtsEnable() != value;
        NativeCode.setRtsEnable(m_hWnd, value);
        if (change)
        {            
            NotifyPropertyChanged("RtsEnable");
        }
    }

    /** 
     Gets or sets the standard number of stopbits per byte.    
    */
    public final StopBits getStopBits()
    {
        if (m_hWnd == 0)
        {
            return m_StopBits;
        }
        return StopBits.values()[NativeCode.getStopBits(m_hWnd)];
    }
    public final void setStopBits(StopBits value)
    {
        boolean change;
        change = getStopBits() != value;
        if (change)
        {
            if (m_hWnd == 0)
            {
                m_StopBits = value;
            }
            else
            {
                NativeCode.setStopBits(m_hWnd, value.ordinal());
            }
            NotifyPropertyChanged("StopBits");
        }
    }

    /* 
     * Gets or sets the number of milliseconds before a time-out 
     * occurs when a write operation does not finish.
     */
    public final int getWriteTimeout()
    {
        return m_WriteTimeout;
    }
    public final void setWriteTimeout(int value)
    {
        boolean change = m_WriteTimeout != value;
        if (change)
        {
            m_WriteTimeout = value;
            NotifyPropertyChanged("WriteTimeout");
        }
    }
    
    @Override
    public final <T> boolean receive(ReceiveParameters<T> args)
    {
        return m_syncBase.receive(args);
    }

    /** 
     Sent byte count.

     @see BytesReceived BytesReceived
     @see ResetByteCounters ResetByteCounters
    */
    @Override
    public final long getBytesSent()
    {
        return m_BytesSend;
    }

    /** 
     Received byte count.

     @see BytesSent BytesSent
     @see ResetByteCounters ResetByteCounters
    */
    @Override
    public final long getBytesReceived()
    {
        return m_BytesReceived;
    }

    /** 
     Resets BytesReceived and BytesSent counters.

     @see BytesSent BytesSent
     @see BytesReceived BytesReceived
    */
    @Override
    public final void resetByteCounters()
    {
        m_BytesSend = m_BytesReceived = 0;
    }
   
    /** 
     Media settings as a XML string.
    */
    @Override
    public final String getSettings()
    {        
        return null;
        //TODO:
    }
    
    @Override
    public final void setSettings(String value)
    {   
        //TODO:
    }
    
    @Override
    public final void copy(Object target)
    {
        GXSerial tmp = (GXSerial)target;
        setPortName(tmp.getPortName());
        setBaudRate(tmp.getBaudRate()); 
        setStopBits(tmp.getStopBits());
        setParity(tmp.getParity());
        setDataBits(tmp.getDataBits());
    }

    @Override
    public String getName()
    {
        return getPortName();
    }

    @Override
    public String getMediaType()
    {
        return "Serial";
    }

    /** <inheritdoc cref="IGXMedia.Synchronous"/>
    */
    @Override
    public final Object getSynchronous()
    {
        synchronized (this)
        {
            int[] tmp = new int[]{m_Synchronous};
            GXSync obj = new GXSync(tmp);
            m_Synchronous = tmp[0];
            return obj;
        }
    }

    /** <inheritdoc cref="IGXMedia.IsSynchronous"/>
    */
    @Override
    public final boolean getIsSynchronous()
    {
        synchronized (this)
        {
            return m_Synchronous != 0;
        }
    }

    /** <inheritdoc cref="IGXMedia.ResetSynchronousBuffer"/>
    */
    @Override
    public final void resetSynchronousBuffer()
    {
        synchronized (m_syncBase.m_ReceivedSync)
        {
            m_syncBase.m_ReceivedSize = 0;
        }
    }

    /** <inheritdoc cref="IGXMedia.Validate"/>
    */
    @Override
    public final void validate()
    {
        if (getPortName() == null || getPortName().length() == 0)
        {
            throw new RuntimeException("Invalid port name.");
        }
    }

    /** <inheritdoc cref="IGXMedia.Eop"/>
    */
    @Override
    public final Object getEop()
    {
        return privateEop;
    }
    @Override
    public final void setEop(Object value)
    {
        privateEop = value;
    }

    @Override
    public void addListener(IGXMediaListener listener) 
    {        
        MediaListeners.add(listener);       
    }

    @Override
    public void removeListener(IGXMediaListener listener) 
    {
        MediaListeners.remove(listener);
    }          
}