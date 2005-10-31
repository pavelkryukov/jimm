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
 File: src/jimm/comm/PingPacket.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


package jimm.comm;


import jimm.JimmException;


public class PingPacket extends Packet
{


	// Constructor
	public PingPacket(int sequence)
	{
		this.sequence = sequence;
	}


	// Constructor
	public PingPacket()
	{
		this(-1);
	}


	// Returns the package as byte array
	public byte[] toByteArray()
	{

		// Allocate memory
		byte buf[] = new byte[6];

		// Assemble FLAP header
		Util.putByte(buf, 0, 0x2A);   // FLAP.ID
		Util.putByte(buf, 1, 0x05);   // FLAP.CHANNEL
		Util.putWord(buf, 2, this.sequence);   // FLAP.SEQUENCE
		Util.putWord(buf, 4, 0x0000);   // FLAP.LENGTH

		// Return
		return (buf);

	}


	// Parses given byte array and returns a Packet object
	public static Packet parse(byte[] buf, int off, int len) throws JimmException
	{

		// Get FLAP sequence number
		int flapSequence = Util.getWord(buf, off + 2);

		// Get length of FLAP data
		int flapLength = Util.getWord(buf, off + 4);

		// Validate length of FLAP data
		if (flapLength != 0)
		{
			// throw (new JimmException(136, 0));
            // Ignore invalide PING packet
		}

		// Instantiate LoginPacket
		return (new PingPacket(flapSequence));

	}


	// Parses given byte array and returns a Packet object
	public static Packet parse(byte[] buf) throws JimmException
	{
		return (PingPacket.parse(buf, 0, buf.length));
	}


}
