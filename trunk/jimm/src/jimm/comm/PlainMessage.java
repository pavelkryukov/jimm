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
 File: src/jimm/comm/PlainMessage.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm.comm;


import java.util.Date;

import jimm.ContactListContactItem;


public class PlainMessage extends Message
{


  // Jimm Advertisement
  public static final String ADVERTISEMENT = "\n \n* Sent using Jimm";


  /****************************************************************************/


  // Message text
  private String text;


  // Constructs an incoming message
  public PlainMessage(String sndrUin, String rcvrUin, Date date, String text)
  {
    this.sndrUin = new String(sndrUin);
    this.rcvrUin = new String(rcvrUin);
    this.date = new Date(date.getTime());
    this.text = new String(text);
  }


  // Constructs an outgoing message
  public PlainMessage(String sndrUin, ContactListContactItem rcvr, Date date, String text, boolean displayAdvertisement)
  {
    this.sndrUin = new String(sndrUin);
    this.rcvr = rcvr;
    this.date = new Date(date.getTime());
    if (displayAdvertisement)
    {
      this.text = text + PlainMessage.ADVERTISEMENT;
    }
    else
    {
      this.text = new String(text);
    }
  }


  // Constructs an outgoing message (w/o advertisement)
  public PlainMessage(String sndrUin, ContactListContactItem rcvr, Date date, String text)
  {
    this(sndrUin, rcvr, date, text, false);
  }


  // Returns the message text
  public String getText()
  {
    return (new String(this.text));
  }


}
