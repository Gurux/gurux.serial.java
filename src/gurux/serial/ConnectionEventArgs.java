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

/** 
    Argument class for IGXMedia connection and disconnection events.
*/
public class ConnectionEventArgs
{
    private String privateInfo;
    private boolean privateAccept;

    /** 
     Constructor
    */
    public ConnectionEventArgs()
    {
        setAccept(true);
    }

    /** 
     Constructor
    */
    public ConnectionEventArgs(String info)
    {
        setAccept(true);
        setInfo(info);
    }

    /** 
     Media depend information.
    */
    public final String getInfo()
    {
        return privateInfo;
    }
    public final void setInfo(String value)
    {
        privateInfo = value;
    }

    /** 
     False, if the client is not accepted to connect.
    */
    public final boolean getAccept()
    {
        return privateAccept;
    }
    public final void setAccept(boolean value)
    {
        privateAccept = value;
    }
}
