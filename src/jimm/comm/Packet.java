/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-05  Jimm Project

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
 File: src/jimm/comm/Packet.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm.comm;


import jimm.JimmException;


abstract class Packet
{


	// Channel constants
	public static final int CHANNEL_CONNECT = 0x01;
	public static final int CHANNEL_SNAC = 0x02;
	public static final int CHANNEL_ERROR = 0x03;
	public static final int CHANNEL_DISCONNECT = 0x04;
	public static final int CHANNEL_PING = 0x05;


	// FLAP sequence number
	protected int sequence;
	
	// Returns the FLAP sequence number
	public int getSequence()
	{
		return (this.sequence);
	}

	// Sets the FLAP sequence number
	void setSequence(int sequence)
	{
		this.sequence = sequence;
	}


	// Returns the package as byte array
	public abstract byte[] toByteArray();

	
	// Parses given byte array and returns a Packet object
	public static Packet parse(byte[] buf, int off, int len) throws JimmException
	{

		// Check length (min. 6 bytes)
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        // #sijapp cond.if modules_FILES is "true"#
	    if (len < 2)
		// #sijapp cond.else#
		if (len < 6)
		// #sijapp cond.end#
        // #sijapp cond.else#
		if (len < 6)
	    // #sijapp cond.end#		    
		{
			throw (new JimmException(130, 0));
		}

		// Verify FLAP.ID
		if (Util.getByte(buf, off) != 0x2A)
		{
			// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		    // #sijapp cond.if modules_FILES is "true"#
			return (DCPacket.parse(buf, off, len));
			// #sijapp cond.else#
			throw (new JimmException(130, 1));
			// #sijapp cond.end#
			// #sijapp cond.else#
			throw (new JimmException(130, 1));
            // #sijapp cond.end#
		}

		// Get and verify FLAP.CHANNEL
		int channel = Util.getByte(buf, off + 1);
		if ((channel < 1) || (channel > 5))
		{
			throw (new JimmException(130, 2));
		}

		// Verify FLAP.LENGTH
		int length = Util.getWord(buf, off + 4);
		if ((length + 6) != len)
		{
			throw (new JimmException(130, 3));
		}

		// Parsing is done by a subclass
		switch (channel)
		{
			case Packet.CHANNEL_CONNECT:
				return (ConnectPacket.parse(buf, off, len));
			case Packet.CHANNEL_SNAC:
				return (SnacPacket.parse(buf, off, len));
			case Packet.CHANNEL_ERROR:
				return (ErrorPacket.parse(buf, off, len));
			case Packet.CHANNEL_DISCONNECT:
				return (DisconnectPacket.parse(buf, off, len));
			case Packet.CHANNEL_PING:
				return (PingPacket.parse(buf, off, len));
			default:
				throw (new JimmException(131, 0));
		}

	}


	// Parses given byte array and returns a Packet object
	public static Packet parse(byte[] buf) throws JimmException
	{
		return (Packet.parse(buf, 0, buf.length));
	}


}
