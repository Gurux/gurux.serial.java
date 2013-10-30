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

import gurux.common.ReceiveParameters;
import java.lang.reflect.Array;
import java.lang.reflect.Type;

class GXSynchronousMediaBase
{
    public static byte[] getAsByteArray(Object data)
    {
        if (data == null)
        {
            return new byte[0];
        }
        if (data instanceof byte[])
        {
            return (byte[]) data;
        }
        if (data instanceof Byte)
        {
            return new byte[] {((Number)data).byteValue()};
        }   
        java.nio.ByteBuffer b;
        if (data instanceof Short)
        {
            b = java.nio.ByteBuffer.allocate(2);
            b.putShort(((Number)data).shortValue());
            return b.array();
        }
        if (data instanceof Integer)
        {
            b = java.nio.ByteBuffer.allocate(4);
            b.putInt(((Number)data).intValue());
            return b.array();
        }
        throw new RuntimeException("Unknown data type " + 
                data.getClass().getName());
    }
    
    public static Object byteArrayToObject(byte[] value, 
            Type type, int[] readBytes)     
    {
        if (type == byte[].class)
        {
            readBytes[0] = value.length;
            return value;
        }
        if (type == Byte.class)
        {
            readBytes[0] = 1;
            return value[0];            
        }
        java.nio.ByteBuffer buff = java.nio.ByteBuffer.wrap(value);
        if (type == Short.class)
        {
            readBytes[0] = 2;
            buff.getShort();
        }
        if (type == Integer.class)
        {
            readBytes[0] = 4;
            buff.getInt();
        }
        if (type == Long.class)
        {
            readBytes[0] = 8;
            buff.getLong();
        }
        if (type == String.class)
        {
            readBytes[0] = value.length;
            return new String(value);
        }
        throw new RuntimeException("Invalid object type.");
    }
    
    public RuntimeException Exception;
    public byte[] m_Received = null;
    public AutoResetEvent m_ReceivedEvent = new AutoResetEvent(false);
    public final Object m_ReceivedSync = new Object();
    public int m_ReceivedSize;
    public int m_LastPosition;
    /** 
     Trace level.
    */
    public gurux.common.TraceLevel Trace;

    public GXSynchronousMediaBase(int bufferSize)
    {
        m_Received = new byte[bufferSize];
        m_LastPosition = 0;
    }

    public final void appendData(byte[] data, int index, int count)
    {
        synchronized (m_ReceivedSync)
        {
            //Allocate new buffer.
            if (m_ReceivedSize + count > m_Received.length)
            {
                byte[] tmp = new byte[2 * m_Received.length];
                System.arraycopy(m_Received, 0, tmp, 0, m_ReceivedSize);
                m_Received = tmp;
            }
            System.arraycopy(data, index, m_Received, m_ReceivedSize, count);
            m_ReceivedSize += count - index;
        }
    }

    /**
     * Finds the first occurrence of the pattern in the text.
     */
    static public int indexOf(byte[] data, byte[] pattern, int index, int count) 
    {
        int[] failure = computeFailure(pattern);

        int j = 0;
        if (data.length == 0 || data.length < index)
        {
            return -1;        
        }

        for (int i = index; i < count; i++) 
        {
            while (j > 0 && pattern[j] != data[i]) 
            {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) 
            {
                j++; 
            }
            if (j == pattern.length) 
            {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
     */
    static private int[] computeFailure(byte[] pattern)
    {
        int[] failure = new int[pattern.length];
        int j = 0;
        for (int i = 1; i < pattern.length; i++) 
        {
            while (j > 0 && pattern[j] != pattern[i]) 
            {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) 
            {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }      
        
    @SuppressWarnings("unchecked")
    public final <T> boolean receive(ReceiveParameters<T> args)
    {
        if (args.getEop() == null && args.getCount() == 0)
        {
            throw new IllegalArgumentException("Either Count or Eop must be set.");
        }
        int nSize = 0;
        byte[] terminator = null;
        if (args.getEop() != null)
        {
            if (args.getEop() instanceof Array)
            {                
                terminator = getAsByteArray(Array.get(args.getEop(), 0));
            }
            else
            {
                terminator = getAsByteArray(args.getEop());
            }
            nSize = terminator.length;
        }

        int nMinSize = (int)Math.max(args.getCount(), nSize);
        int waitTime = args.getWaitTime();
        if (waitTime <= 0)
        {
            waitTime = -1;
        }

        //Wait until reply occured.		
        int nFound = -1;
        int LastBuffSize = 0;
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.util.Date StartTime = calendar.getTime();
        boolean retValue = true;
        do
        {
            if (waitTime == 0)
            {
                //If we do not want to read all data.
                if (!args.getAllData())
                {
                    return false;
                }
                retValue = false;
                break;
            }
            if (waitTime != -1)
            {
                waitTime -= calendar.getTime().getTime() - StartTime.getTime();
                StartTime = new java.util.Date();
                if (waitTime < 0)
                {
                    waitTime = 0;
                }
            }
            boolean received;
            synchronized (m_ReceivedSync)
            {
                received = !(LastBuffSize == m_ReceivedSize || m_ReceivedSize < nMinSize);
            }
            //Do not wait if there is data on the buffer...
            if (!received)
            {
                if (waitTime == -1)
                {
                    received = m_ReceivedEvent.waitOne();
                }
                else
                {
                    received = m_ReceivedEvent.waitOne(waitTime);
                }
            }
            if (this.Exception != null)
            {
                RuntimeException ex = this.Exception;
                this.Exception = null;
                throw ex;
           }
           //If timeout occured.
           if (!received)
           {
                //If we do not want to read all data.
                if (!args.getAllData())
                {
                    return false;
                }
                retValue = false;
                break;
           }
            synchronized (m_ReceivedSync)
            {
                LastBuffSize = m_ReceivedSize;
                //Read more data, if not enought
                if (m_ReceivedSize < nMinSize)
                {
                        continue;
                }
                //If only byte count matters.
                if (nSize == 0)
                {
                        nFound = args.getCount();
                }
                else
                {
                        int index = m_LastPosition != 0 && m_LastPosition < m_ReceivedSize ? m_LastPosition : args.getCount();
                        //If terminator found.
                        if (args.getEop() instanceof Array)
                        {
                            for (Object it : (Object[]) args.getEop())
                            {                                
                                nFound = indexOf(m_Received, getAsByteArray(it), index, m_ReceivedSize);
                                if (nFound != -1)
                                {
                                    break;
                                }
                            }
                        }
                        else
                        {
                            nFound = indexOf(m_Received, terminator, index, m_ReceivedSize);
                        }
                        m_LastPosition = m_ReceivedSize;
                        if (nFound != -1)
                        {
                            ++nFound;
                        }
                }
            }
        }
        while (nFound == -1);
        if (nSize == 0) //If terminator is not given read only bytes that are needed.
        {
            nFound = args.getCount();
        }
        Object data;
        synchronized (m_ReceivedSync)
        {
            if (args.getAllData()) //If all data is copied.
            {
                nFound = m_ReceivedSize;
            }
            //Convert bytes to object.
            byte[] tmp = new byte[nFound];
            System.arraycopy(m_Received, 0, tmp, 0, nFound);
            int[] readBytes = new int[1]; 
            data = byteArrayToObject(tmp, args.getReplyType(), readBytes);
            //Remove read data.
            m_ReceivedSize -= nFound;
            //Received size can go less than zero if we have received data and we try to read more.
            if (m_ReceivedSize < 0)
            {
                m_ReceivedSize = 0;
            }
            if (m_ReceivedSize != 0)
            {
                System.arraycopy(m_Received, nFound, m_Received, 0, m_ReceivedSize);
            }
        }
        //Reset count after read.
        args.setCount(0);
        //Append data.
        int oldReplySize;
        if (args.getReply() == null)
        {
            args.setReply((T)data);
        }
        else
        {            
            if (args.getReply() instanceof String)
            {
                String str = (String) args.getReply();
                str += (String)data;
                data = str;
                args.setReply((T)data);
            }
            else if (args.getReply() instanceof byte[])
            {
                byte[] oldArray = (byte[])args.getReply();
                byte[] newArray = (byte[]) data;
                if (newArray == null)
                {
                    throw new IllegalArgumentException();
                }
                oldReplySize = Array.getLength(oldArray);
                int len = oldReplySize + Array.getLength(newArray);
                byte[] arr = new byte[len];
                //Copy old values.
                System.arraycopy((byte[]) args.getReply(), 0, arr, 0, Array.getLength(oldArray));
                //Copy new values.
                System.arraycopy(newArray, 0, arr, Array.getLength(oldArray), Array.getLength(newArray));
                Object tmp2 = arr;
                args.setReply((T)tmp2);
            }
            else
            {
                throw new RuntimeException("Invalid reply type");
            }
        }
        return retValue;
    }
}