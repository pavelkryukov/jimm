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
Author(s): Igor Palkin
*******************************************************************************/


package jimm.comm;

import java.util.Calendar;
import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import jimm.comm.Util;
import jimm.DebugLog;
import jimm.Jimm;
import jimm.JimmException;
import jimm.Options;
import jimm.JimmUI;
import jimm.RunnableImpl;
import jimm.ContactListContactItem;
import jimm.ContactList;


public class SaveInfoAction extends Action
{

	// Receive timeout
	private static final int TIMEOUT = 3 * 1000; // milliseconds
	
	//TLVs
	private static final int NICK_TLV_ID = 0x0154;
	public  static  final int FIRSTNAME_TLV_ID = 0x0140; 
	private static final int LASTNAME_TLV_ID = 0x014A;
	private static final int EMAIL_TLV_ID = 0x015E;
	private static final int BDAY_TLV_ID = 0x023A;
	private static final int CITY_TLV_ID = 0x0190;
	private static final int GENDER_TLV_ID = 0x017C;
	
	private boolean infoShown;
	
	private boolean showInfoText = true;

	/****************************************************************************/
	
	private String[] strData = new String[JimmUI.UI_LAST_ID];	

	// Date of init
	private Date init;
	private int packetCounter;
	private int errorCounter;

	// Constructor
	public SaveInfoAction(String[] userInfo)
	{
		super(false, true);
		strData = userInfo;
	}

	// Init action
	protected synchronized void init() throws JimmException
	{
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Util.writeWord(stream ,ToIcqSrvPacket.CLI_SET_FULLINFO, false);

		Util.writeAsciizTLV(NICK_TLV_ID, stream, strData[JimmUI.UI_NICK], false);
		
		Util.writeAsciizTLV(FIRSTNAME_TLV_ID, stream, strData[JimmUI.UI_FIRST_NAME], false);
		Util.writeAsciizTLV(LASTNAME_TLV_ID, stream, strData[JimmUI.UI_LAST_NAME], false);
		Util.writeAsciizTLV(EMAIL_TLV_ID, stream, strData[JimmUI.UI_EMAIL], false);
		Util.writeAsciizTLV(CITY_TLV_ID, stream, strData[JimmUI.UI_CITY], false);

		//Bug, birthday date can not be saved now
//		String[] bDate =  Util.explode(strData[JimmUI.UI_BDAY], '.');
//		ByteArrayOutputStream bdayStream = new ByteArrayOutputStream();
//		Util.writeWord( bdayStream, Integer.parseInt(bDate[2]), false );
//		Util.writeWord( bdayStream, Integer.parseInt(bDate[1]), false );
//		Util.writeWord( bdayStream, Integer.parseInt(bDate[0]), false );
//		Util.writeTLV(BDAY_TLV_ID, stream, bdayStream, false);
		
		ByteArrayOutputStream sexStream = new ByteArrayOutputStream();
		Util.writeByte( sexStream, Util.stringToGender(strData[JimmUI.UI_GENDER]) );
		Util.writeTLV( GENDER_TLV_ID, stream, sexStream, false );
		
		ToIcqSrvPacket packet = new ToIcqSrvPacket(0,Options.getString(Options.OPTION_UIN), ToIcqSrvPacket.CLI_META_SUBCMD, new byte[0], stream.toByteArray());
		Icq.c.sendPacket(packet);
		DebugLog.addText( Util.toHexString(packet.toByteArray()) );
		
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
			DataInputStream stream = Util.getDataInputStream(fromIcqSrvPacket.getData(), 0);
			
			try
			{
				int type = Util.getWord(stream, false);
				switch (type)
				{
					case FromIcqSrvPacket.META_SET_FULLINFO_ACK: //  full user information
					{
						if(stream.readByte() != 0x0A)
						{
							errorCounter++;
							break;
						}
						
						consumed = true;
						packetCounter++;
						break;
					}
				}
			}
			catch(Exception e)
			{
			}
		}

		return (consumed);
	}
	
	// Returns true if the action is completed
	public synchronized boolean isCompleted()
	{
		return (packetCounter >= 1);
	}

	// Returns true if an error has occured
	public synchronized boolean isError()
	{
		return (this.init.getTime() + SaveInfoAction.TIMEOUT < System.currentTimeMillis())
				||
				errorCounter > 0;
	}
	
	public int getProgress()
	{
		return packetCounter > 0 ? 100 : 0;
	}
	
	 public void onEvent(int eventTuype)
	{
	    	switch (eventTuype)
	    	{
		    	case ON_COMPLETE:
		    		ContactList.activate();
		    		break;
	    	}
	}
}