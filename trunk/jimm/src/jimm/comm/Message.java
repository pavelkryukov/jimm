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
 File: src/jimm/comm/Message.java
 Version: 0.3.1  Date: 2004/12/25
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm.comm;


import java.util.Date;

import jimm.ContactListContactItem;


public abstract class Message
{


  // Senders UIN (set for both incoming and outgoing messages)
  protected String sndrUin;


  // Receivers UIN (set only for incoming messages)
  protected String rcvrUin;


  // Receiver object (set only for outgoing messages)
  protected ContactListContactItem rcvr;


  // Date of dispatch
  protected Date date;


  // Returns the senders UIN
  public String getSndrUin()
  {
    return (new String(this.sndrUin));
  }


  // Returns the receivers UIN
  public String getRcvrUin() {
    if (this.rcvrUin != null)
    {
      return (new String(this.rcvrUin));
    }
    else
    {
      return (this.rcvr.getUin());
    }
  }


  // Returns the receiver
  public ContactListContactItem getRcvr()
  {
    return (this.rcvr);
  }


  // Returns the date of dispatch
  public Date getDate()
  {
    return (new Date(this.date.getTime()));
  }


}
