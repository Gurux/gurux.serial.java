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

import gurux.io.NativeCode;
import gurux.common.ReceiveEventArgs;
import gurux.common.TraceLevel;
import gurux.common.TraceTypes;
import java.lang.reflect.Array;

class GXReceiveThread extends Thread
{     
    long ComPort;
    private GXSerial m_Parent;   
    int BufferPosition = 0;
    Object Eop = null;

    public GXReceiveThread(GXSerial parent, long hComPort)
    {        
        super("GXSerial " + new Long(hComPort).toString());
        ComPort = hComPort;
        m_Parent = parent;       
        Eop = m_Parent.getEop();
        BufferPosition = 0;
    }
    
    private void handleReceivedData( byte[] Buffer)
    {
        int len = Buffer.length;
        if (len == 0)
        {
            return;
        }
        m_Parent.m_BytesReceived += len;
        int totalCount = 0;
        if (m_Parent.getIsSynchronous())
        {
            gurux.common.TraceEventArgs arg = null;
            synchronized (m_Parent.m_syncBase.m_ReceivedSync)
            {                
                m_Parent.m_syncBase.appendData(Buffer, BufferPosition, len);                
                if (Eop != null) //Search Eop if given.
                {                    
                    if (Eop instanceof Array)
                    {                                
                        for(Object eop : (Object[]) Eop)
                        {
                            totalCount = GXSynchronousMediaBase.indexOf(Buffer, 
                                GXSynchronousMediaBase.getAsByteArray(eop), 
                                BufferPosition - len, BufferPosition);
                            if (totalCount != -1)
                            {
                                break;
                            }
                        }
                    }
                    else
                    {
                        totalCount = GXSynchronousMediaBase.indexOf(Buffer, 
                                GXSynchronousMediaBase.getAsByteArray(Eop), 
                                BufferPosition - len, BufferPosition);
                    }
                }
                if (totalCount != -1)
                {
                    if (m_Parent.getTrace() == TraceLevel.VERBOSE)
                    {
                        arg = new gurux.common.TraceEventArgs(TraceTypes.RECEIVED, Buffer, 0, totalCount + 1);
                    }
                    m_Parent.m_syncBase.m_ReceivedEvent.set();
                }
            }
            if (arg != null)
            {
                m_Parent.notifyTrace(arg);
            }
        }
        else
        {
            m_Parent.m_syncBase.m_ReceivedSize = 0;
            if (Eop != null) //Search Eop if given.
            {          
                if (Eop instanceof Array)
                {
                    for(Object eop : (Object[]) Eop)
                    {
                        
                        totalCount = GXSynchronousMediaBase.indexOf(Buffer, 
                                GXSynchronousMediaBase.getAsByteArray(eop), 
                                BufferPosition - len, BufferPosition);
                        if (totalCount != -1)
                        {
                            break;
                        }
                    }
                }
                else
                {                    
                    totalCount = GXSynchronousMediaBase.indexOf(Buffer, 
                            GXSynchronousMediaBase.getAsByteArray(Eop), 
                            BufferPosition - len, BufferPosition);
                }
                if (totalCount != -1)
                {
                    byte[] data = new byte[len];
                    System.arraycopy(Buffer, 0, data, 0, totalCount);
                    System.arraycopy(Buffer, 0, Buffer, totalCount, BufferPosition - totalCount);
                    BufferPosition = 0;
                    ReceiveEventArgs e = new ReceiveEventArgs(data, m_Parent.getPortName());
                    m_Parent.notifyReceived(e);                
                    if (m_Parent.getTrace() == TraceLevel.VERBOSE)
                    {
                        m_Parent.notifyTrace(new gurux.common.TraceEventArgs(TraceTypes.RECEIVED, Buffer, 0, len));                    
                    }                    
                }
            }
            else
            {
                byte[] data = new byte[len];
                System.arraycopy(Buffer, 0, data, 0, len);                
                if (m_Parent.getTrace() == TraceLevel.VERBOSE)
                {
                    m_Parent.notifyTrace(new gurux.common.TraceEventArgs(TraceTypes.RECEIVED, data));                    
                }
                ReceiveEventArgs e = new ReceiveEventArgs(data, m_Parent.getPortName());
                m_Parent.notifyReceived(e);                
            }
        }
    }   
    
    /** 
     Receive data from the server using the established socket connection

     @return The data received from the server
    */
    @Override
    public final void run()
    {            	
        while(!Thread.currentThread().isInterrupted())
        {
            try
            {   
                byte[] buff = NativeCode.read(this.ComPort, m_Parent.m_ReadTimeout, m_Parent.m_Closing);
                //If connection is closed.
                if (buff.length == 0 && Thread.currentThread().isInterrupted())
                {
                    m_Parent.m_Closing = 0;
                    break;                	
                }
                handleReceivedData(buff);
            }
            catch(Exception ex)
            { 
                if (!Thread.currentThread().isInterrupted())
                {
                    m_Parent.notifyError(new RuntimeException(ex.getMessage()));
                }
                else
                {
                    break;                
                }
            }
        }
    }
}