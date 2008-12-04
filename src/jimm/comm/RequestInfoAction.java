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
import java.io.DataInputStream;

import jimm.JimmException;
import jimm.Options;
import jimm.JimmUI;
import jimm.MainThread;
import jimm.ContactItem;
import jimm.ContactList;

public class RequestInfoAction extends Action
{

	// Receive timeout
	private static final int TIMEOUT = 10 * 1000; // milliseconds

	private boolean infoShown;

	private boolean showInfoText = true;

	/****************************************************************************/

	private String[] strData = new String[JimmUI.UI_LAST_ID];

	// Date of init
	private Date init;

	private int packetCounter;

	private boolean notFound = false;

	private String existingNick;

	// Constructor
	public RequestInfoAction(String uin, String nick)
	{
		super(false, true);
		existingNick = nick;
		infoShown = false;
		packetCounter = 0;
		strData[JimmUI.UI_UIN] = uin;
	}

	// Init action
	protected void init() throws JimmException
	{

		// Send a CLI_METAREQINFO packet
		byte[] buf = new byte[6];
		Util.putWord(buf, 0, ToIcqSrvPacket.CLI_META_REQMOREINFO_TYPE, false);
		Util.putDWord(buf, 2, Long.parseLong(strData[JimmUI.UI_UIN]), false);
		ToIcqSrvPacket packet = new ToIcqSrvPacket(0, Options
				.getString(Options.OPTION_UIN), ToIcqSrvPacket.CLI_META_SUBCMD,
				new byte[0], buf);
		Icq.sendPacket(packet);

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
			if (fromIcqSrvPacket.getSubcommand() != FromIcqSrvPacket.SRV_META_SUBCMD)
				return false;

			// Get packet data
			DataInputStream stream = Util.getDataInputStream(fromIcqSrvPacket
					.getData(), 0);

			// Watch out for SRV_METAGENERAL packet
			try
			{
				int type = Util.getWord(stream, false);
				int successByte = stream.readByte(); // Success byte

				switch (type)
				{
				case FromIcqSrvPacket.SRV_META_GENERAL_TYPE: //  basic user information
				{
					strData[JimmUI.UI_NICK] = Util.readAsciiz(stream); // nickname
					// first name + last name
					String fistName = Util.readAsciiz(stream);
					String lastName = Util.readAsciiz(stream);
					strData[JimmUI.UI_FIRST_NAME] = fistName;
					strData[JimmUI.UI_LAST_NAME] = lastName;
					if ((fistName.length() != 0) || (lastName.length() != 0))
						strData[JimmUI.UI_NAME] = fistName + " " + lastName;
					strData[JimmUI.UI_EMAIL] = Util.readAsciiz(stream); // email
					strData[JimmUI.UI_CITY] = Util.readAsciiz(stream); // home city
					strData[JimmUI.UI_STATE] = Util.readAsciiz(stream); // home state
					strData[JimmUI.UI_PHONE] = Util.readAsciiz(stream); // home phone
					strData[JimmUI.UI_FAX] = Util.readAsciiz(stream); // home fax
					strData[JimmUI.UI_ADDR] = Util.readAsciiz(stream); // home address
					strData[JimmUI.UI_CPHONE] = Util.readAsciiz(stream); // cell phone
					packetCounter++;
					consumed = true;
					break;
				}

				case 0x00DC: // more user information
				{
					int age = Util.getWord(stream, false);
					strData[JimmUI.UI_AGE] = (age != 0) ? Integer.toString(age)
							: new String();
					strData[JimmUI.UI_GENDER] = Util.genderToString(stream
							.readByte());
					strData[JimmUI.UI_HOME_PAGE] = Util.readAsciiz(stream);
					int year = Util.getWord(stream, false);
					int mon = stream.readByte();
					int day = stream.readByte();
					strData[JimmUI.UI_BDAY] = (year != 0) ? day + "." + mon
							+ "." + year : new String();
					packetCounter++;
					consumed = true;
					break;
				}

				case 0x00D2: // work user information
				{
					for (int i = JimmUI.UI_W_CITY; i <= JimmUI.UI_W_ADDR; i++)
						strData[i] = Util.readAsciiz(stream); // city - address
					Util.readAsciiz(stream); // work zip code
					Util.getWord(stream, false); // work country code
					strData[JimmUI.UI_W_NAME] = Util.readAsciiz(stream); // work company
					strData[JimmUI.UI_W_DEP] = Util.readAsciiz(stream); // work department
					strData[JimmUI.UI_W_POS] = Util.readAsciiz(stream); // work position
					packetCounter++;
					consumed = true;
					break;
				}

				case 0x00E6: // user about information
				{
					strData[JimmUI.UI_ABOUT] = Util.readAsciiz(stream); // notes string
					packetCounter++;
					consumed = true;
					break;
				}

				case 0x00F0: // user interests information
				{
					StringBuffer sb = new StringBuffer();
					int counter = stream.readByte();
					String item;
					for (int i = 0; i < counter; i++)
					{
						Util.getWord(stream, false);
						item = Util.readAsciiz(stream);
						if (item.trim().length() == 0)
							continue;
						if (sb.length() != 0)
							sb.append(", ");
						sb.append(item);
					}
					strData[JimmUI.UI_INETRESTS] = sb.toString();
					packetCounter++;
					consumed = true;
					break;
				}

				case 0x00FA: // end snac
				{
					if (successByte != 0x0A)
						notFound = true;
					packetCounter++;
					consumed = true;
					break;
				}
				}

			} catch (Exception e)
			{
				e.printStackTrace();
			}

			// is completed?
			if (isCompleted())
			{
				if (!infoShown && showInfoText)
				{
					MainThread.showUserInfo(strData);
					tryToChangeName();
					infoShown = true;
				}
			}

		} // end 'if (packet instanceof FromIcqSrvPacket)'

		return (consumed);
	}

	// Rename contact if its name consists of digits
	private void tryToChangeName()
	{
		if (strData[JimmUI.UI_UIN].equals(existingNick))
		{
			ContactItem item = ContactList
					.getItembyUIN(strData[JimmUI.UI_UIN]);
			item.rename(strData[JimmUI.UI_NICK]);
		}
	}

	// Returns true if the action is completed
	public synchronized boolean isCompleted()
	{
		return ((packetCounter >= 5) || notFound);
	}

	// Returns true if an error has occured
	public synchronized boolean isError()
	{
		return (this.init.getTime() + RequestInfoAction.TIMEOUT < System
				.currentTimeMillis());
	}

}
