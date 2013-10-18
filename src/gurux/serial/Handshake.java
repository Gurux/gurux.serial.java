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

/*
 * Specifies the control protocol used in establishing a serial port communication.
 */
    public enum Handshake
    {
        /*
         * No control is used for the handshake.
         * */
        NONE,
        /*        
        * The XON/XOFF software control protocol is used. The XOFF control is sent
        * to stop the transmission of data. The XON control is sent to resume the transmission.
        * These software controls are used instead of Request to Send (RTS) and Clear
        * to Send (CTS) hardware controls.
        */
        XOnXOff,
        /* Request-to-Send (RTS) hardware flow control is used. RTS signals that data
        * is available for transmission. If the input buffer becomes full, the RTS
        * line will be set to false. The RTS line will be set to true when more room
        * becomes available in the input buffer.
        */
        RequestToSend,
        /*
        * Both the Request-to-Send (RTS) hardware control and the XON/XOFF software
        * controls are used.
        */
        RequestToSendXOnXOff,
    }