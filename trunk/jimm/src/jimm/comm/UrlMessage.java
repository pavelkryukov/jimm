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
 File: src/jimm/comm/UrlMessage.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm.comm;


import java.util.Date;

import jimm.ContactListContactItem;


public class UrlMessage extends Message
{


  // URL
  private String url;


  // Message text
  private String text;


  // Constructs an incoming message
  public UrlMessage(String sndrUin, String rcvrUin, Date date, String url, String text)
  {
    this.sndrUin = new String(sndrUin);
    this.rcvrUin = new String(rcvrUin);
    this.date = new Date(date.getTime());
    this.url = new String(url);
    this.text = new String(text);
  }


  // Constructs an outgoing message
  public UrlMessage(String sndrUin, ContactListContactItem rcvr, Date date, String url, String text)
  {
    this.sndrUin = new String(sndrUin);
    this.rcvr = rcvr;
    this.date = new Date(date.getTime());
    this.url = new String(url);
    this.text = new String(text);
  }


  // Returns the URL
  public String getUrl()
  {
    return (new String(this.url));
  }


  // Returns the message text
  public String getText()
  {
    return (new String(this.text));
  }


}
