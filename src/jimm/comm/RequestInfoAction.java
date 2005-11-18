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
 File: src/jimm/comm/RequestInfoAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm.comm;


import java.util.Date;

import jimm.JimmException;
import jimm.Options;

class BinaryInputStream
{
	private byte[] data;
	private int pointer;
	
	public BinaryInputStream(byte[] data, int pointer)
	{
		this.data    = data;
		this.pointer = pointer;
	}
	
	public String readAsciiz()
	{
		int len = Util.getWord(data, pointer, false);
		if (len == 0) return new String();
		pointer += (2+len);
		return Util.byteArrayToString(data, pointer-len, len); 
	}
	
	public int readWord()
	{
		pointer += 2;
		return Util.getWord(data, pointer-2, false);
	}
	
	public int readByte()
	{
		return data[pointer++];
	}
}

public class RequestInfoAction extends Action
{


	// Receive timeout
	private static final int TIMEOUT = 20 * 1000; // milliseconds


	/****************************************************************************/


	// Information about the user
	final public static int UIN        = 0;
	final public static int NICK       = 1;
	final public static int NAME       = 2;
	final public static int EMAIL      = 3;
	final public static int CITY       = 4;
	final public static int STATE      = 5;
	final public static int PHONE      = 6;
	final public static int FAX        = 7;
	final public static int ADDR       = 8;
	final public static int CPHONE     = 9;
	final public static int AGE        = 10;
	final public static int GENDER     = 11;
	final public static int HOME_PAGE  = 12;
	final public static int BDAY       = 13;
	final public static int W_CITY     = 14;
	final public static int W_STATE    = 15;
	final public static int W_PHONE    = 16;
	final public static int W_FAX      = 17;
	final public static int W_ADDR     = 18;
	final public static int W_NAME     = 19;
	final public static int W_DEP      = 20;
	final public static int W_POS      = 21;
	final public static int ABOUT      = 22;
	final public static int INETRESTS  = 23;
	
	final public static int LAST_ID    = 24;
	
	private String[] strData = new String[LAST_ID];	

	// Date of init
	private Date init;


	// Constructor
	public RequestInfoAction(String uin)
	{
		strData[UIN] = uin;
	}

	public String getStringData(int id)
	{
		return strData[id];
	}


	// Returns true if the action can be performed
	public boolean isExecutable()
	{
		return (Icq.isConnected());
	}


	// Returns true if this is an exclusive command
	public boolean isExclusive()
	{
		return (false);
	}


	// Init action
	protected void init() throws JimmException
	{

		// Send a CLI_METAREQINFO packet
		byte[] buf = new byte[6];
		Util.putWord(buf, 0, ToIcqSrvPacket.CLI_META_REQMOREINFO_TYPE, false);
		Util.putDWord(buf, 2, Long.parseLong(strData[UIN]), false);
		ToIcqSrvPacket packet = new ToIcqSrvPacket(0, Options.getStringOption(Options.OPTION_UIN), ToIcqSrvPacket.CLI_META_SUBCMD, new byte[0], buf);
		Icq.Connection.sendPacket(packet);

		// Save date
		this.init = new Date();

	}
	

	// Forwards received packet, returns true if packet was consumed
	protected synchronized boolean forward(Packet packet) throws JimmException
	{

		// Watch out for SRV_FROMICQSRV packet
		if (packet instanceof FromIcqSrvPacket)
		{
			FromIcqSrvPacket fromIcqSrvPacket = (FromIcqSrvPacket) packet;

			// Watch out for SRV_META packet
			if (fromIcqSrvPacket.getSubcommand() == FromIcqSrvPacket.SRV_META_SUBCMD)
			{
				// Get packet data
				byte[] data = fromIcqSrvPacket.getData();

				// Watch out for SRV_METAGENERAL packet
				switch (Util.getWord(data, 0, false))
				{
				case FromIcqSrvPacket.SRV_META_GENERAL_TYPE: //  basic user information
					{
						BinaryInputStream stream = new BinaryInputStream(data, 3);
						strData[NICK]   = stream.readAsciiz();     // nickname
						strData[NAME]   = stream.readAsciiz()+" "+ // first name + last name
						                 stream.readAsciiz();
						strData[EMAIL]  = stream.readAsciiz();     // email 
						strData[CITY]   = stream.readAsciiz();     // home city
						strData[STATE]  = stream.readAsciiz();     // home state
						strData[PHONE]  = stream.readAsciiz();     // home phone
						strData[FAX]    = stream.readAsciiz();     // home fax
						strData[ADDR]   = stream.readAsciiz();     // home address
						strData[CPHONE] = stream.readAsciiz();     // cell phone
						return (true);
					}
				
				case 0x00DC: // more user information
					{
						BinaryInputStream stream = new BinaryInputStream(data, 3);
						
						int age = stream.readWord();
						strData[AGE]       = (age != 0) ? Integer.toString(age) : new String();
						strData[GENDER]    = Util.genderToString( stream.readByte() );
						strData[HOME_PAGE] = stream.readAsciiz();
						int year = stream.readWord();
						int mon  = stream.readByte();
						int day  = stream.readByte();
						strData[BDAY] = (year != 0) ? year+"."+mon+"."+day : new String();
						return true;
					}
					
				case 0x00D2: // work user information
					{
						BinaryInputStream stream = new BinaryInputStream(data, 3);
						for (int i = W_CITY; i <= W_ADDR; i++) strData[i] = stream.readAsciiz(); // city - address
						stream.readAsciiz();                   // work zip code
						stream.readWord();                     // work country code
						strData[W_NAME] = stream.readAsciiz(); // work company
						strData[W_DEP]  = stream.readAsciiz(); // work department
						strData[W_POS]  = stream.readAsciiz(); // work position
						return (true);
					}
					
				case 0x00E6: // user about information
					{
						BinaryInputStream stream = new BinaryInputStream(data, 3);
						strData[ABOUT] = stream.readAsciiz(); // notes string
						return true;
					}
					
				case 0x00F0: // user interests information
					{
						BinaryInputStream stream = new BinaryInputStream(data, 3);
						StringBuffer sb = new StringBuffer();
						int counter = stream.readByte();
						for (int i = 0; i < counter; i++)
						{
							stream.readWord();
							sb.append(stream.readAsciiz());
							if (i != (counter-1)) sb.append(", ");
						}
						strData[INETRESTS] = sb.toString(); 
						return true;
					}
				
				}
			}

		}

		// Packet has not been consumed
		return (false);

	}


	// Returns true if the action is completed
	public synchronized boolean isCompleted()
	{
		for (int i = 0; i < LAST_ID; i++) if (strData[i] == null) return false;
		return true;
	}


	// Returns true if an error has occured
	public synchronized boolean isError()
	{
		return (this.init.getTime() + RequestInfoAction.TIMEOUT < System.currentTimeMillis());
	}


}
