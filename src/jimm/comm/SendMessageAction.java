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
 File: src/jimm/comm/SendMessageAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm.comm;


import jimm.ContactListContactItem;
import jimm.JimmException;


public class SendMessageAction extends Action
{


  // Plain message
  private PlainMessage plainMsg;


  // URL message
  private UrlMessage urlMsg;


  // Constructor
  public SendMessageAction(Message msg)
  {
    if (msg instanceof PlainMessage)
    {
      this.plainMsg = (PlainMessage) msg;
      this.urlMsg = null;
    }
    else if (msg instanceof UrlMessage)
    {
      this.plainMsg = null;
      this.urlMsg = (UrlMessage) msg;
    }
  }


  // Returns true if the action can be performed
  public boolean isExecutable()
  {
    return (this.icq.isConnected());
  }


  // Returns true if this is an exclusive command
  public boolean isExclusive()
  {
    return (false);
  }


  // Init action
  protected void init() throws JimmException
  {

    // Forward init request depending on message type
    if (this.plainMsg != null)
      this.initPlainMsg();
    else
      this.initUrlMsg();

  }


  // Init action for plain messages
  private void initPlainMsg() throws JimmException
  {

    // Get receiver object
    ContactListContactItem rcvr = this.plainMsg.getRcvr();

    // What message format/encoding should we use?
    int type = 1;
    boolean utf8 = utf8 = rcvr.hasCapability(ContactListContactItem.CAP_UTF8_INTERNAL);
    // if ((rcvr.getStatus() != ContactList.STATUS_OFFLINE) && rcvr.hasCapability(ContactListContactItem.CAP_AIM_SERVERRELAY))
    // {
    //   type = 2;
    // }

    //////////////////////
    // Message format 1 //
    //////////////////////

    if (type == 1)
    {

      // Get UIN
      byte[] uinRaw = Util.stringToByteArray(rcvr.getUin());

      // Get text
      byte[] textRaw;
      if (utf8)
      {
        textRaw = Util.stringToUcs2beByteArray(this.plainMsg.getText());
      }
      else
      {
        textRaw = Util.stringToByteArray(this.plainMsg.getText());
      }

      // Pack data
      byte[] buf = new byte[10 + 1 + uinRaw.length + 4 + (utf8 ? 6 : 5) + 4 + 4 + textRaw.length + 4];
      int marker = 0;
      Util.putDWord(buf, marker, 0x00000000);   // CLI_SENDMSG.TIME
      marker += 4;
      Util.putDWord(buf, marker, 0x00000000);   // CLI_SENDMSG.ID
      marker += 4;
      Util.putWord(buf, marker, 0x0001);   // CLI_SENDMSG.FORMAT
      marker += 2;
      Util.putByte(buf, marker, uinRaw.length);   // CLI_SENDMSG.UIN
      System.arraycopy(uinRaw, 0, buf, marker + 1, uinRaw.length);
      marker += 1 + uinRaw.length;
      Util.putWord(buf, marker, 0x0002);   // CLI_SENDMSG.SUB_MSG_TYPE1
      Util.putWord(buf, marker + 2, (utf8 ? 6 : 5) + 4 + 4 + textRaw.length);
      marker += 4;
      Util.putWord(buf, marker, 0x0501);   // SUB_MSG_TYPE1.CAPABILITIES
      if (utf8)
      {
        Util.putWord(buf, marker + 2, 0x0002);
        Util.putWord(buf, marker + 4, 0x0106);
        marker += 6;
      }
      else
      {
        Util.putWord(buf, marker + 2, 0x0001);
        Util.putByte(buf, marker + 4, 0x01);
        marker += 5;
      }
      Util.putWord(buf, marker, 0x0101);   // SUB_MSG_TYPE1.MESSAGE
      Util.putWord(buf, marker + 2, 4 + textRaw.length);
      marker += 4;
      if (utf8)
      {
        Util.putDWord(buf, marker, 0x00020000);   // MESSAGE.ENCODING
      }
      else
      {
        Util.putDWord(buf, marker, 0x00000000);   // MESSAGE.ENCODING
      }
      marker += 4;
      System.arraycopy(textRaw, 0, buf, marker, textRaw.length);   // MESSAGE.MESSAGE
      marker += textRaw.length;
      Util.putWord(buf, marker, 0x0006);   // CLI_SENDMSG.UNKNOWN
      Util.putWord(buf, marker + 2, 0x0000);
      marker += 4;

      // Send packet
      SnacPacket snacPkt = new SnacPacket(SnacPacket.CLI_SENDMSG_FAMILY, SnacPacket.CLI_SENDMSG_COMMAND, 0, new byte[0], buf);
      this.icq.c.sendPacket(snacPkt);

    }

    //////////////////////
    // Message format 2 //
    //////////////////////

    else if (type == 2)
    {
      // TODO: Implement
    }

  }


  // Init action for URL messages
  private void initUrlMsg() throws JimmException
  {

    // Get UIN
    String uin = this.urlMsg.getRcvrUin();

    // Get message
    String message = this.urlMsg.getText() + (char) (0xFE) + this.urlMsg.getUrl();

    // Pack data
    byte[] buf = new byte[10 + 1 + uin.length() + 4 + 4 + 2 + 2 + message.length() + 1 + 4];
    int marker = 0;
    Util.putDWord(buf, marker, 0x00000000);   // CLI_SENDMSG.TIME
    marker += 4;
    Util.putDWord(buf, marker, 0x00000000);   // CLI_SENDMSG.ID
    marker += 4;
    Util.putWord(buf, marker, 0x0004);   // CLI_SENDMSG.FORMAT
    marker += 2;
    Util.putByte(buf, marker, uin.length());   // CLI_SENDMSG.UIN
    System.arraycopy(Util.stringToByteArray(uin), 0, buf, marker + 1, uin.length());
    marker += 1 + uin.length();
    Util.putWord(buf, marker, 0x0005);   // CLI_SENDMSG.SUB_MSG_TYPE4
    Util.putWord(buf, marker + 2, 4 + 2 + 2 + message.length() + 1);
    marker += 4;
    Util.putDWord(buf, marker, Long.parseLong(this.urlMsg.getSndrUin()), false);   // SUB_MSG_TYPE4.UIN
    marker += 4;
    Util.putWord(buf, marker, 0x0004, false);   // SUB_MSG_TYPE4.MSGTYPE
    marker += 2;
    Util.putWord(buf, marker, message.length() + 1, false);   // SUB_MSG_TYPE4.MESSAGE
    System.arraycopy(Util.stringToByteArray(message), 0, buf, marker + 2, message.length());
    Util.putByte(buf, marker + 2 + message.length(), 0x00);
    marker += 2 + message.length() + 1;
    Util.putWord(buf, marker, 0x0006);   // CLI_SENDMSG.UNKNOWN
    Util.putWord(buf, marker + 2, 0x0000);
    marker += 4;

    // Send packet
    SnacPacket snacPkt = new SnacPacket(SnacPacket.CLI_SENDMSG_FAMILY, SnacPacket.CLI_SENDMSG_COMMAND, 0, new byte[0], buf);
    this.icq.c.sendPacket(snacPkt);

  }


  // Forwards received packet, returns true if packet was consumed
  protected boolean forward(Packet packet) throws JimmException
  {
    return (false);
  }


  // Returns true if the action is completed
  public boolean isCompleted()
  {
    return (true);
  }


  // Returns true if an error has occured
  public boolean isError()
  {
    return (false);
  }


}
