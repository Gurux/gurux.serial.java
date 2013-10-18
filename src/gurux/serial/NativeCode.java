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

public class NativeCode {
    public static native String[] getPortNames();   
    
    public static native int openSerialPort(String port);   
        
    public static native void closeSerialPort(long hComPort);   
    
    public static native int getBaudRate(long hComPort);        
    
    public static native void setBaudRate(long hComPort, int value);       
    
    public static native int getDataBits(long hComPort);
    
    public static native void setDataBits(long hComPort, int value);
    
    public static native int getParity(long hComPort);
            
    public static native void setParity(long hComPort, int value);    
    
    public static native int getStopBits(long hComPort);
            
    public static native void setStopBits(long hComPort, int value);    
    
    public static native boolean getBreakState(long hComPort);
    
    public static native void setBreakState(long hComPort, boolean value);
    
    public static native boolean getRtsEnable(long hComPort);
   
    public static native void setRtsEnable(long hComPort, boolean value);
     
    public static native boolean getDtrEnable(long hComPort);
   
    public static native void setDtrEnable(long hComPort, boolean value);       
    
    public static native boolean getDsrHolding(long hComPort);

    public static native int getBytesToRead(long hComPort);

    public static native int getBytesToWrite(long hComPort);
        
    public static native byte[] read(long hComPort, int readTimeout);  
    
    public static native void write(long hComPort, byte[] data, int writeTimeout);
    
    public static native boolean getCtsHolding(long hComPort);
    
    public static native boolean getCDHolding(long hComPort);
    
    public static native int getHandshake(long hComPort);
            
    public static native void setHandshake(long hComPort, int value);    

}
