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

package gurux.serial.enums;

import java.util.HashMap;

/**
 * Describes available settings for the media.
 */
public enum AvailableMediaSettings {
    /**
     * All network properties are shown.
     */
    ALL(-1), /**
              * Is Server is shown.
              */
    SERVER(0x1), /**
                  * Network protocol is shown.
                  */
    PROTOCOL(0x2), /**
                    * Port number is shown.
                    */
    PORT(0x4), /**
                * Host Name is shown.
                */
    HOST(0x8);

    /**
     * Integer value of enumeration.
     */
    private int intValue;

    /**
     * Collection of integer and enumeration values.
     */
    private static java.util.HashMap<Integer, AvailableMediaSettings> mappings;

    /**
     * Get mappings.
     * 
     * @return Hash map of enumeration and integer values.
     */
    private static HashMap<Integer, AvailableMediaSettings> getMappings() {
        synchronized (AvailableMediaSettings.class) {
            if (mappings == null) {
                mappings = new HashMap<Integer, AvailableMediaSettings>();
            }
        }
        return mappings;
    }

    /**
     * Constructor.
     * 
     * @param value
     *            Integer value for enumerator.
     */
    AvailableMediaSettings(final int value) {
        intValue = value;
        synchronized (AvailableMediaSettings.class) {
            getMappings().put(value, this);
        }
    }

    /**
     * Get enemerator's integer value.
     * 
     * @return Integer value of enumerator.
     */
    public int getValue() {
        return intValue;
    }

    /**
     * Get enumerator from integer value.
     * 
     * @param value
     *            integer value.
     * @return Enumerator value.
     */
    public static AvailableMediaSettings forValue(final int value) {
        return getMappings().get(value);
    }
}