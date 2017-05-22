
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
import gurux.io.BaudRate;
import gurux.io.Parity;
import gurux.io.StopBits;
import gurux.serial.GXSerial;

public class mainApp {

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        try {
            byte[] data = null;
            String port = null;
            int databits = 8;
            BaudRate baudRate = BaudRate.BAUD_RATE_9600;
            Parity parity = Parity.NONE;
            StopBits stopbits = StopBits.ONE;
            boolean hex = false;
            if (args.length == 0) {
                ShowHelp();
                System.out.println("Available serial ports.");
                System.out.println("------------------------------------");
                for (String it : GXSerial.getPortNames()) {
                    System.out.println(it);
                }
                return;
            }
            for (String it : args) {
                String item = it.trim();
                if (item.startsWith("/sp=")) {
                    port = item.replaceFirst("/sp=", "");
                } else if (item.startsWith("/r=")) {
                    baudRate = BaudRate.forValue(
                            Integer.parseInt(item.replaceFirst("/r=", "")));
                } else if (item.startsWith("/d=")) {
                    databits = Integer.parseInt(item.replaceFirst("/d=", ""));
                } else if (item.startsWith("/p=")) {
                    parity = Parity.valueOf(item.replaceFirst("/p=", ""));
                } else if (item.startsWith("/s=")) {
                    stopbits = StopBits.valueOf(item.replaceFirst("/s=", ""));
                } else if (item.startsWith("/h=")) {
                    data = GXCommon.hexToBytes(item.replaceFirst("/h=", ""));
                    hex = true;
                } else if (item.startsWith("/a=")) {
                    data = item.replaceFirst("/a=", "")
                            .replaceAll("\\\\r", "\\\r")
                            .replaceAll("\\\\n", "\\\n").getBytes();
                } else {
                    ShowHelp();
                    return;
                }
            }
            if (port == null) {
                ShowHelp();
            }
            GXSerial s = new GXSerial();
            s.setPortName(port);
            s.setBaudRate(baudRate);
            s.setDataBits(databits);
            s.setParity(parity);
            s.setStopBits(stopbits);
            System.out.println("Opening Serial port:");
            System.out.println("Port:" + s.getPortName());
            System.out.println("BaudRate: " + s.getBaudRate());
            System.out.println("DataBits: " + String.valueOf(s.getDataBits()));
            System.out.println("Parity: " + String.valueOf(s.getParity()));
            System.out.println("StopBits: " + String.valueOf(s.getStopBits()));
            app a = new app(s, 1);
            s.open();
            int v;
            System.out.println("------------------------------------");
            System.out.println("Serial port open. Press x to close.");
            System.out.println("------------------------------------");
            if (data != null) {
                System.out.println("<- " + new String(data) + " | "
                        + GXCommon.bytesToHex(data));
                s.send(data, null);
            }
            byte[] tmp = new byte[1];
            while ((v = System.in.read()) != 'x') {
                tmp[0] = (byte) v;
                System.out.println(
                        "<- " + (char) v + " | " + GXCommon.bytesToHex(tmp));
                s.send(tmp[0], null);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Show help.
     */
    static void ShowHelp() {
        System.out.println("gurux.serial.terminal commmands.");
        System.out.println("------------------------------------");
        System.out.println("gurux.serial.terminal /sp=COM1 p");
        System.out
                .println(" /sp=\t serial port. (Example: COM1, /dev/TTYUSB0)");
        System.out.println(" /r=\t Serial port baud rate. 9600 is default.");
        System.out.println(" /d=\t Serial port databits. 8 is default.");
        System.out.println(" /p=\t Serial port Parity. NONE is default.");
        System.out.println(" /s=\t Serial port stopbits. ONE is default.");
        System.out.println(" /a=\t ASCII data to send.");
        System.out.println(" /h=\t Hex data to send.");
        System.out.println(" /e=\t End of Data. \r\n is default");
        System.out.println("------------------------------------");
    }
}
