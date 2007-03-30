/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-04  Jimm Project

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 ********************************************************************************
 File: src/jimm/comm/ErrorPacket.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer
 *******************************************************************************/

package jimm.comm;

import jimm.JimmException;

public class ErrorPacket extends Packet
{

	// Returns the package as byte array
	public byte[] toByteArray()
	{
		return (null);
	}

	// Parses given byte array and returns a Packet object
	public static Packet parse(byte[] buf, int off, int len)
			throws JimmException
	{
		return (null);
	}

	// Parses given byte array and returns a Packet object
	public static Packet parse(byte[] buf) throws JimmException
	{
		return (ErrorPacket.parse(buf, 0, buf.length));
	}

}
