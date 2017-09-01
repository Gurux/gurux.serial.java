
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
import gurux.common.GXCommon;
import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.TraceEventArgs;
import gurux.serial.GXSerial;

public class app implements IGXMediaListener {
    private GXSerial s;
    private int mode;

    /**
     * Constructor.
     * 
     * @param target
     */
    public app(GXSerial target, int m) {
        s = target;
        mode = m;
        s.addListener(this);
    }

    @Override
    public void onError(Object sender, Exception ex) {
        System.out.println("---------------------------------------");
        System.out.println(ex.getMessage());
        System.out.println("---------------------------------------");
    }

    @Override
    public void onReceived(Object sender, ReceiveEventArgs e) {
        if (mode == 1) {
            System.out.print(GXCommon.bytesToHex((byte[]) e.getData()));
        } else if (mode == 2) {
            System.out.print(new String((byte[]) e.getData()));
        } else {
            System.out.print(new String((byte[]) e.getData()) + " | "
                    + GXCommon.bytesToHex((byte[]) e.getData()));
        }
    }

    @Override
    public void onMediaStateChange(Object sender, MediaStateEventArgs e) {
        System.out.println("---------------------------------------");
        System.out.println("Media " + e.getState().toString());
        System.out.println("---------------------------------------");
    }

    @Override
    public void onTrace(Object sender, TraceEventArgs e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        // TODO Auto-generated method stub

    }
}
