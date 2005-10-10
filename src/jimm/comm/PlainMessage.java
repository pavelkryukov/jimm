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
 File: src/jimm/comm/PlainMessage.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


package jimm.comm;


import java.util.Date;

import jimm.ContactListContactItem;


public class PlainMessage extends Message
{
	// Message text
	private String text;


	// Constructs an incoming message
	public PlainMessage(String sndrUin, String rcvrUin, Date date, String text, boolean offline)
	{
		this.sndrUin = new String(sndrUin);
		this.rcvrUin = new String(rcvrUin);
		this.date = new Date(date.getTime());
		this.text = new String(text);
		this.offline = offline;
	}


	// Constructs an outgoing message
	public PlainMessage(String sndrUin, ContactListContactItem rcvr, int _messageType, Date date, String text)
	{
		this.sndrUin = new String(sndrUin);
		this.rcvr = rcvr;
        this.messageType = _messageType;
		this.date = new Date(date.getTime());
		this.text = new String(text);
	}


	// Returns the message text
	public String getText()
	{
		return (new String(this.text));
	}


}
