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

import jimm.Jimm;
import jimm.JimmException;
import jimm.Options;
import jimm.JimmUI;
import jimm.RunnableImpl;

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
	private static final int TIMEOUT = 10 * 1000; // milliseconds

	private boolean infoShown;

	/****************************************************************************/
	
	private String[] strData = new String[JimmUI.UI_LAST_ID];	

	// Date of init
	private Date init;
	private int packetCounter;


	// Constructor
	public RequestInfoAction(String uin)
	{
		infoShown = false;
		packetCounter = 0;
		strData[JimmUI.UI_UIN] = uin;
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
		Util.putDWord(buf, 2, Long.parseLong(strData[JimmUI.UI_UIN]), false);
		ToIcqSrvPacket packet = new ToIcqSrvPacket(0, Options.getStringOption(Options.OPTION_UIN), ToIcqSrvPacket.CLI_META_SUBCMD, new byte[0], buf);
		Jimm.jimm.getIcqRef().c.sendPacket(packet);

		// Save date
		this.init = new Date();

	}
	

	// Forwards received packet, returns true if packet was consumed
	protected synchronized boolean forward(Packet packet) throws JimmException
	{
		boolean consumed = false; 

		// Watch out for SRV_FROMICQSRV packet
		if (packet instanceof FromIcqSrvPacket)
		{
			FromIcqSrvPacket fromIcqSrvPacket = (FromIcqSrvPacket) packet;

			// Watch out for SRV_META packet
			if (fromIcqSrvPacket.getSubcommand() != FromIcqSrvPacket.SRV_META_SUBCMD) return false;
			
			// Get packet data
			byte[] data = fromIcqSrvPacket.getData();
			
			// Watch out for SRV_METAGENERAL packet
			switch (Util.getWord(data, 0, false))
			{
			case FromIcqSrvPacket.SRV_META_GENERAL_TYPE: //  basic user information
				{
					BinaryInputStream stream = new BinaryInputStream(data, 3);
					
					strData[JimmUI.UI_NICK]   = stream.readAsciiz();     // nickname
					// first name + last name
					strData[JimmUI.UI_NAME]   = stream.readAsciiz()+" "+stream.readAsciiz();
					strData[JimmUI.UI_EMAIL]  = stream.readAsciiz();     // email
					strData[JimmUI.UI_CITY]   = stream.readAsciiz();     // home city
					strData[JimmUI.UI_STATE]  = stream.readAsciiz();     // home state
					strData[JimmUI.UI_PHONE]  = stream.readAsciiz();     // home phone
					strData[JimmUI.UI_FAX]    = stream.readAsciiz();     // home fax
					strData[JimmUI.UI_ADDR]   = stream.readAsciiz();     // home address
					strData[JimmUI.UI_CPHONE] = stream.readAsciiz();     // cell phone
					packetCounter++;
					consumed = true;
					break;
				}
				
			case 0x00DC: // more user information
				{
					BinaryInputStream stream = new BinaryInputStream(data, 3);
						
					int age = stream.readWord();
					strData[JimmUI.UI_AGE]       = (age != 0) ? Integer.toString(age) : new String();
					strData[JimmUI.UI_GENDER]    = Util.genderToString( stream.readByte() );
					strData[JimmUI.UI_HOME_PAGE] = stream.readAsciiz();
					int year = stream.readWord();
					int mon  = stream.readByte();
					int day  = stream.readByte();
					strData[JimmUI.UI_BDAY] = (year != 0) ? day+"."+mon+"."+year : new String();
					packetCounter++;
					consumed = true;
					break;
				}
					
			case 0x00D2: // work user information
				{
					BinaryInputStream stream = new BinaryInputStream(data, 3);
					for (int i = JimmUI.UI_W_CITY; i <= JimmUI.UI_W_ADDR; i++) strData[i] = stream.readAsciiz(); // city - address
					stream.readAsciiz();                   // work zip code
					stream.readWord();                     // work country code
					strData[JimmUI.UI_W_NAME] = stream.readAsciiz(); // work company
					strData[JimmUI.UI_W_DEP]  = stream.readAsciiz(); // work department
					strData[JimmUI.UI_W_POS]  = stream.readAsciiz(); // work position
					packetCounter++;
					consumed = true;
					break;
				}
					
			case 0x00E6: // user about information
				{
					BinaryInputStream stream = new BinaryInputStream(data, 3);
					strData[JimmUI.UI_ABOUT] = stream.readAsciiz(); // notes string
					packetCounter++;
					consumed = true;
					break;
				}
					
			case 0x00F0: // user interests information
				{
					BinaryInputStream stream = new BinaryInputStream(data, 3);
					StringBuffer sb = new StringBuffer();
					int counter = stream.readByte();
					for (int i = 0; i < counter; i++)
					{
						stream.readWord();
						String item = stream.readAsciiz();
						if (item.trim().length() == 0) continue;
						if (sb.length() != 0) sb.append(", ");
						sb.append(item);
					}
					strData[JimmUI.UI_INETRESTS] = sb.toString();
					packetCounter++;
					consumed = true;
					break;
				}
			}
			
			// is completed?
			if (isCompleted())
			{
				if (!infoShown)
				{
					RunnableImpl.callSerially(RunnableImpl.TYPE_SHOW_USER_INFO, (Object)strData);
					infoShown = true;
				}
			}
			
		} // end 'if (packet instanceof FromIcqSrvPacket)'

		return (consumed);
	}


	// Returns true if the action is completed
	public synchronized boolean isCompleted()
	{
		return (packetCounter >= 5);
	}


	// Returns true if an error has occured
	public synchronized boolean isError()
	{
		return (this.init.getTime() + RequestInfoAction.TIMEOUT < System.currentTimeMillis());
	}


}
