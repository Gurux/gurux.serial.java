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

public class AutoResetEvent
{
    private final Object monitor = new Object();
    private volatile boolean isOpen = false;

    public AutoResetEvent(boolean open)
    {
        isOpen = open;
    }

    public boolean waitOne()
    {
        synchronized (monitor) 
        {
            while (!isOpen) 
            {
                try
                {
                    monitor.wait();
                }
                catch(InterruptedException ex)
                {
                    return false;
                }                
            }
            isOpen = false;
            return true;
        }
    }

    public boolean waitOne(long timeout)
    {
        synchronized (monitor) 
        {
            boolean ret = true;
            long t = System.currentTimeMillis();
            while (!isOpen) 
            {
                try
                {
                    monitor.wait(timeout);
                }
                catch(InterruptedException ex)
                {
                    return false;
                }
                // Check for timeout
                if (System.currentTimeMillis() - t >= timeout)
                {
                    ret = false;
                    break;
                }
            }
            isOpen = false;
            return ret;
        }
    }

    public void set()
    {
        synchronized (monitor) 
        {
            isOpen = true;
            monitor.notify();
        }
    }

    public void reset()
    {
        isOpen = false;
    }
}