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
 File: src/jimm/comm/ActionListener.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.ContactList;
import jimm.ContactListContactItem;
import jimm.Jimm;
import jimm.JimmException;

import java.util.Date;

public class ActionListener
{

    // Capability denotes that the client support type-2 messages
    public static final byte[] CAP_AIM_SERVERRELAY =
    { (byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x49, (byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1,
            (byte) 0x82, (byte) 0x22, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00};

    // Capability deontes that the client supports UTF-8 messages
    public static final byte[] CAP_UTF8 =
    { (byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x4E, (byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1,
            (byte) 0x82, (byte) 0x22, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00};

    /** ************************************************************************* */

    // ICQ object
    protected Icq icq;

    // Set ICQ object
    protected void setIcq(Icq icq)
    {
        this.icq = icq;
    }

    // Forwards received packet
    protected void forward(Packet packet) throws JimmException
    {

        // Watch out for channel 4 (Disconnect) packets
        if (packet instanceof DisconnectPacket)
        {
            DisconnectPacket disconnectPacket = (DisconnectPacket) packet;

            // Throw exception
            if (disconnectPacket.getError() == 0x0001)
            { // Multiple logins
                throw (new JimmException(110, 0));
            } else
            { // Unknown error
                throw (new JimmException(100, 0));
            }

        }

        /** *********************************************************************** */

        // Watch out for channel 2 (SNAC) packets
        if (packet instanceof SnacPacket)
        {
            SnacPacket snacPacket = (SnacPacket) packet;

            // Watch out for SRV_USERONLINE packets
            if ((snacPacket.getFamily() == SnacPacket.SRV_USERONLINE_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_USERONLINE_COMMAND))
            {

                // Get data
                byte[] buf = snacPacket.getData();

                // Get UIN of the contact changing status
                int uinLen = Util.getByte(buf, 0);
                String uin = Util.byteArrayToString(buf, 1, uinLen);

                // Get new status and client capabilities
                long status = ContactList.STATUS_ONLINE;
                int capabilities = 0x00000000;
                int marker = 1 + uinLen + 2;
                int tlvNum = Util.getWord(buf, marker);
                marker += 2;
                for (int i = 0; i < tlvNum; i++)
                {
                    int tlvType = Util.getWord(buf, marker);
                    byte[] tlvData = Util.getTlv(buf, marker);
                    if (tlvType == 0x0006) // STATUS
                    {
                        status = Util.getDWord(tlvData, 0);
                    } else if (tlvType == 0x000D) // CAPABILITIES
                    {
                        for (int j = 0; j < tlvData.length / 16; j++)
                        {
                            if (Util.byteArrayEquals(tlvData, j * 16, ActionListener.CAP_AIM_SERVERRELAY, 0, 16))
                            {
                                capabilities |= ContactListContactItem.CAP_AIM_SERVERRELAY_INTERNAL;
                            } else if (Util.byteArrayEquals(tlvData, j * 16, ActionListener.CAP_UTF8, 0, 16))
                            {
                                capabilities |= ContactListContactItem.CAP_UTF8_INTERNAL;
                            }
                        }

                    }
                    marker += 2 + 2 + tlvData.length;
                }

                // Update contact list
                Jimm.jimm.getContactListRef().update(uin, status, capabilities);

            }

            /** ********************************************************************* */

            // Watch out for SRV_USEROFFLINE packets
            if ((snacPacket.getFamily() == SnacPacket.SRV_USEROFFLINE_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_USEROFFLINE_COMMAND))
            {

                // Get raw data
                byte[] buf = snacPacket.getData();

                // Get UIN of the contact that goes offline
                int uinLen = Util.getByte(buf, 0);
                String uin = Util.byteArrayToString(buf, 1, uinLen);

                // Update contact list
                Jimm.jimm.getContactListRef().update(uin, ContactList.STATUS_OFFLINE);

            }

            /** ********************************************************************* */

            // Watch out for SRV_RECVMSG
            if ((snacPacket.getFamily() == SnacPacket.SRV_RECVMSG_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_RECVMSG_COMMAND))
            {

                // Get raw data, initialize marker
                byte[] buf = snacPacket.getData();
                int marker = 0;

                // Check length
                if (buf.length < 11) { throw (new JimmException(150, 0, false)); }

                // Get message format
                marker += 8;
                int format = Util.getWord(buf, marker);
                marker += 2;

                // Get UIN length
                int uinLen = Util.getByte(buf, marker);
                marker += 1;

                // Check length
                if (buf.length < marker + uinLen + 4) { throw (new JimmException(150, 1, false)); }

                // Get UIN
                String uin = Util.byteArrayToString(buf, marker, uinLen);
                marker += uinLen;

                // Skip WARNING
                marker += 2;

                // Skip TLVS
                int tlvCount = Util.getWord(buf, marker);
                marker += 2;
                for (int i = 0; i < tlvCount; i++)
                {
                    byte[] tlvData = Util.getTlv(buf, marker);
                    if (tlvData == null) { throw (new JimmException(150, 2, false)); }
                    marker += 4 + tlvData.length;
                }

                // Get message data and initialize marker
                byte[] msgBuf;
                int tlvType;
                do
                {
                    msgBuf = Util.getTlv(buf, marker);
                    if (msgBuf == null) { throw (new JimmException(150, 3, false)); }
                    tlvType = Util.getWord(buf, marker);
                    marker += 4 + msgBuf.length;
                } while ((tlvType != 0x0002) && (tlvType != 0x0005));
                int msgMarker = 0;

                //////////////////////
                // Message format 1 //
                //////////////////////
                if (format == 0x0001)
                {

                    // Variables for all possible TLVs
                    // byte[] capabilities = null;
                    byte[] message = null;

                    // Read all TLVs
                    while (msgMarker < msgBuf.length)
                    {

                        // Get next TLV
                        byte[] tlvValue = Util.getTlv(msgBuf, msgMarker);
                        if (tlvValue == null) { throw (new JimmException(151, 0, false)); }

                        // Get type of next TLV
                        tlvType = Util.getWord(msgBuf, msgMarker);

                        // Update markers
                        msgMarker += 4 + tlvValue.length;

                        // Save value
                        switch (tlvType)
                        {
                        case 0x0501:
                            // capabilities
                            // capabilities = tlvValue;
                            break;
                        case 0x0101:
                            // message
                            message = tlvValue;
                            break;
                        default:
                            throw (new JimmException(151, 1, false));
                        }

                    }

                    // Process packet if at least the message TLV was present
                    if (message != null)
                    {

                        // Check length of message
                        if (message.length < 4) { throw (new JimmException(151, 2, false)); }

                        // Get message text
                        String text;
                        if (Util.getWord(message, 0) == 0x0002)
                        {
                            text = Util.crlfToCr(Util.ucs2beByteArrayToString(message, 4, message.length - 4));
                        } else
                        {
                            text = Util.crlfToCr(Util.byteArrayToString(message, 4, message.length - 4));
                        }

                        // Construct object which encapsulates the received
                        // plain message
                        PlainMessage plainMsg = new PlainMessage(uin, this.icq.getUin(), new Date(), text);
                        Jimm.jimm.getContactListRef().addMessage(plainMsg);

                    }

                }
                //////////////////////
                // Message format 2 //
                //////////////////////
                else if (format == 0x0002)
                {

                    // Check length
                    if (msgBuf.length < 10) { throw (new JimmException(152, 0, false)); }

                    // Get and validate SUB_MSG_TYPE2.ACKTYPE
                    int ackType = Util.getWord(msgBuf, msgMarker);
                    if (ackType != 0x0000) return; // Only normal messages are
                                                   // supported yet
                    msgMarker += 2;

                    // Skip SUB_MSG_TYPE2.TIME and SUB_MSG_TYPE2.ID
                    msgMarker += 4 + 4;

                    // Check length
                    if (msgBuf.length < msgMarker + 16) { throw (new JimmException(152, 1, false)); }

                    // Skip SUB_MSG_TYPE2.CAPABILITY
                    msgMarker += 16;

                    // Get message data and initialize marker
                    byte[] msg2Buf;
                    do
                    {
                        msg2Buf = Util.getTlv(msgBuf, msgMarker);
                        if (msg2Buf == null) { throw (new JimmException(152, 2, false)); }
                        tlvType = Util.getWord(msgBuf, msgMarker);
                        msgMarker += 4 + msg2Buf.length;
                    } while (tlvType != 0x2711);
                    int msg2Marker = 0;

                    // Check length
                    if (msg2Buf.length < 2 + 2 + 16 + 3 + 4 + 2 + 2 + 2 + 12 + 2 + 2 + 2 + 2) { throw (new JimmException(
                            152, 3, false)); }

                    // Skip values up to (and including) SUB_MSG_TYPE2.UNKNOWN
                    // (before MSGTYPE)
                    msg2Marker += 2 + 2 + 16 + 3 + 4 + 2 + 2 + 2 + 12;

                    // Get and validate message type
                    int msgType = Util.getWord(msg2Buf, msg2Marker, false);
                    msg2Marker += 2;
                    if ((msgType != 0x0001) && (msgType != 0x0004) && (msgType != 0x001A)) return;

                    // Skip UNKNOWN and PRIORITY
                    msg2Marker += 2 + 2;

                    // Get length of text
                    int textLen = Util.getWord(msg2Buf, msg2Marker, false);
                    msg2Marker += 2;

                    // Check length
                    if (msg2Buf.length < msg2Marker + textLen + 4 + 4) { throw (new JimmException(152, 4, false)); }

                    // Get raw text
                    byte[] rawText = new byte[textLen];
                    System.arraycopy(msg2Buf, msg2Marker, rawText, 0, textLen);
                    msg2Marker += textLen;

                    // Plain message or URL message
                    if (((msgType == 0x0001) || (msgType == 0x0004)) && (rawText.length > 1))
                    {

                        // Skip FOREGROUND and BACKGROUND
                        if ((msgType == 0x0001) || (msgType == 0x0004))
                        {
                            msg2Marker += 4 + 4;
                        }

                        // Check encoding (by checking GUID)
                        boolean isUtf8 = false;
                        if (msg2Buf.length >= msg2Marker + 4)
                        {
                            int guidLen = (int) Util.getDWord(msg2Buf, msg2Marker, false);
                            if (guidLen == 38)
                            {
                                if (Util.byteArrayToString(msg2Buf, msg2Marker + 4, guidLen).equals(
                                        "{0946134E-4C7F-11D1-8222-444553540000}"))
                                {
                                    isUtf8 = true;
                                }
                            }
                            msg2Marker += 4 + guidLen;
                        }

                        // Decode text and create Message object
                        Message message;
                        if (msgType == 0x0001)
                        {

                            // Decode text
                            String text = Util.crlfToCr(Util.byteArrayToString(rawText, isUtf8));

                            // Instantiate message object
                            message = new PlainMessage(uin, this.icq.getUin(), new Date(), text);

                        } else
                        {

                            // Search for delimited
                            int delim = -1;
                            for (int i = 0; i < rawText.length; i++)
                            {
                                if (rawText[i] == 0xFE)
                                {
                                    delim = i;
                                    break;
                                }
                            }

                            // Decode text; split text first, if delimiter could
                            // be found
                            String urlText, url;
                            if (delim != -1)
                            {
                                urlText = Util.crlfToCr(Util.byteArrayToString(rawText, 0, delim, isUtf8));
                                url = Util.crlfToCr(Util.byteArrayToString(rawText, delim + 1, rawText.length - delim
                                        - 1, isUtf8));
                            } else
                            {
                                urlText = Util.crlfToCr(Util.byteArrayToString(rawText, isUtf8));
                                url = "";
                            }

                            // Instantiate UrlMessage object
                            message = new UrlMessage(uin, this.icq.getUin(), new Date(), url, urlText);

                        }

                        // Forward message object to contact list
                        Jimm.jimm.getContactListRef().addMessage(message);

                        // Acknowledge message
                        byte[] ackBuf = new byte[10 + 1 + uinLen + 2 + 51 + 3];
                        int ackMarker = 0;
                        System.arraycopy(buf, 0, ackBuf, ackMarker, 10);
                        ackMarker += 10;
                        Util.putByte(ackBuf, ackMarker, uinLen);
                        ackMarker += 1;
                        byte[] uinRaw = Util.stringToByteArray(uin);
                        System.arraycopy(uinRaw, 0, ackBuf, ackMarker, uinRaw.length);
                        ackMarker += uinRaw.length;
                        Util.putWord(ackBuf, ackMarker, 0x0003);
                        ackMarker += 2;
                        System.arraycopy(msg2Buf, 0, ackBuf, ackMarker, 51);
                        ackMarker += 51;
                        Util.putWord(ackBuf, ackMarker, 0x0001, false);
                        ackMarker += 2;
                        Util.putByte(ackBuf, ackMarker, 0x00);
                        ackMarker += 1;
                        SnacPacket ackPacket = new SnacPacket(SnacPacket.CLI_ACKMSG_FAMILY,
                                SnacPacket.CLI_ACKMSG_COMMAND, 0, new byte[0], ackBuf);
                        this.icq.c.sendPacket(ackPacket);

                    }
                    // Extended message
                    else if (msgType == 0x001A)
                    {

                        // Check length
                        if (msg2Buf.length < msg2Marker + 2 + 18 + 4) { throw (new JimmException(152, 5, false)); }

                        // Save current marker position
                        int extDataStart = msg2Marker;

                        // Skip EXTMSG.LEN and EXTMSG.UNKNOWN
                        msg2Marker += 2 + 18;

                        // Get length of plugin string
                        int pluginLen = (int) Util.getDWord(msg2Buf, msg2Marker, false);
                        msg2Marker += 4;

                        // Check length
                        if (msg2Buf.length < msg2Marker + pluginLen + 15 + 4 + 4) { throw (new JimmException(152, 6,
                                false)); }

                        // Get plugin string
                        String plugin = Util.byteArrayToString(msg2Buf, msg2Marker, pluginLen);
                        msg2Marker += pluginLen;

                        // Skip EXTMSG.UNKNOWN and EXTMSG.LEN
                        msg2Marker += 15 + 4;

                        // Get length of text
                        textLen = (int) Util.getDWord(msg2Buf, msg2Marker, false);
                        msg2Marker += 4;

                        // Check length
                        if (msg2Buf.length < msg2Marker + textLen) { throw (new JimmException(152, 7, false)); }

                        // Get text
                        String text = Util.crlfToCr(Util.byteArrayToString(msg2Buf, msg2Marker, textLen));
                        msg2Marker += textLen;

                        // URL message
                        if (plugin.equals("Send Web Page Address (URL)"))
                        {

                            // Search for delimiter
                            int delim = text.indexOf(0xFE);

                            // Split message, if delimiter could be found
                            String urlText;
                            String url;
                            if (delim != -1)
                            {
                                urlText = text.substring(0, delim);
                                url = text.substring(delim + 1);
                            } else
                            {
                                urlText = text;
                                url = "";
                            }

                            // Forward message message to contact list
                            UrlMessage message = new UrlMessage(uin, this.icq.getUin(), new Date(), url, urlText);
                            Jimm.jimm.getContactListRef().addMessage(message);

                            // Acknowledge message
                            byte[] ackBuf = new byte[10 + 1 + uinLen + 2 + 51 + 3 + 20 + 4 + (int) pluginLen + 19 + 4
                                    + textLen];
                            int ackMarker = 0;
                            System.arraycopy(buf, 0, ackBuf, ackMarker, 10);
                            ackMarker += 10;
                            Util.putByte(ackBuf, ackMarker, uinLen);
                            ackMarker += 1;
                            byte[] uinRaw = Util.stringToByteArray(uin);
                            System.arraycopy(uinRaw, 0, ackBuf, ackMarker, uinRaw.length);
                            ackMarker += uinRaw.length;
                            Util.putWord(ackBuf, ackMarker, 0x0003);
                            ackMarker += 2;
                            System.arraycopy(msgBuf, 0, ackBuf, ackMarker, 51);
                            ackMarker += 51;
                            Util.putWord(ackBuf, ackMarker, 0x0001, false);
                            ackMarker += 2;
                            Util.putByte(ackBuf, ackMarker, 0x00);
                            ackMarker += 1;
                            System.arraycopy(msg2Buf, extDataStart, ackBuf, ackMarker, 20 + 4 + (int) pluginLen + 19
                                    + 4 + textLen);
                            SnacPacket ackPacket = new SnacPacket(SnacPacket.CLI_ACKMSG_FAMILY,
                                    SnacPacket.CLI_ACKMSG_COMMAND, 0, new byte[0], ackBuf);
                            this.icq.c.sendPacket(ackPacket);

                        }
                        // Other messages
                        else
                        {
                            // Discard
                        }

                    }

                }
                //////////////////////
                // Message format 4 //
                //////////////////////
                else if (format == 0x0004)
                {

                    // Check length
                    if (msgBuf.length < 8) { throw (new JimmException(153, 0, false)); }

                    // Skip SUB_MSG_TYPE4.UIN
                    msgMarker += 4;

                    // Get SUB_MSG_TYPE4.MSGTYPE
                    int msgType = Util.getWord(msgBuf, msgMarker, false);
                    msgMarker += 2;

                    // Only plain messages and URL messagesa are supported
                    if ((msgType != 0x0001) && (msgType != 0x0004)) return;

                    // Get length of text
                    int textLen = Util.getWord(msgBuf, msgMarker, false);
                    msgMarker += 2;

                    // Check length (exact match required)
                    if (msgBuf.length != 8 + textLen) { throw (new JimmException(153, 1, false)); }

                    // Get text
                    String text = Util.crlfToCr(Util.byteArrayToString(msgBuf, msgMarker, textLen));
                    msgMarker += textLen;

                    // Plain message
                    if (msgType == 0x0001)
                    {

                        // Forward message to contact list
                        PlainMessage plainMsg = new PlainMessage(uin, this.icq.getUin(), new Date(), text);
                        Jimm.jimm.getContactListRef().addMessage(plainMsg);

                    }
                    // URL message
                    else if (msgType == 0x0004)
                    {

                        // Search for delimiter
                        int delim = text.indexOf(0xFE);

                        // Split message, if delimiter could be found
                        String urlText;
                        String url;
                        if (delim != -1)
                        {
                            urlText = text.substring(0, delim);
                            url = text.substring(delim + 1);
                        } else
                        {
                            urlText = text;
                            url = "";
                        }

                        // Forward message message to contact list
                        UrlMessage urlMsg = new UrlMessage(uin, this.icq.getUin(), new Date(), url, urlText);
                        Jimm.jimm.getContactListRef().addMessage(urlMsg);

                    }

                }

            }

            //	  Watch out for SRV_ADDEDYOU
            if ((snacPacket.getFamily() == SnacPacket.SRV_ADDEDYOU_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_ADDEDYOU_COMMAND))
            {
                // Get data
                byte[] buf = snacPacket.getData();

                // Get UIN of the contact changing status
                int uinLen = Util.getByte(buf, 0);
                String uin = Util.byteArrayToString(buf, 1, uinLen);

                // Create a new system notice
                SystemNotice notice = new SystemNotice(SystemNotice.SYS_NOTICE_YOUWEREADDED, uin, false, null);

                // Handle the new system notice
                Jimm.jimm.getContactListRef().addMessage(notice);

            }

            //	  Watch out for SRV_AUTHREQ
            if ((snacPacket.getFamily() == SnacPacket.SRV_AUTHREQ_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_AUTHREQ_COMMAND))
            {

                int authMarker = 0;
                
                // Get data
                byte[] buf = snacPacket.getData();

                // Get UIN of the contact changing status
                int length = Util.getByte(buf, 0);
                authMarker += 1;
                String uin = Util.byteArrayToString(buf, authMarker, length);
                authMarker += length;

                // Get reason
                length = Util.getWord(buf, authMarker);
                String reason = Util.byteArrayToString(buf, authMarker, length + 2);

                // Create a new system notice
                SystemNotice notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHREQ, uin, false, reason);

                // Handle the new system notice
                Jimm.jimm.getContactListRef().addMessage(notice);

            }

            //	  Watch out for SRV_AUTHREPLY
            if ((snacPacket.getFamily() == SnacPacket.SRV_AUTHREPLY_FAMILY)
                    && (snacPacket.getCommand() == SnacPacket.SRV_AUTHREPLY_COMMAND))
            {

                int authMarker = 0;
                // Get data
                byte[] buf = snacPacket.getData();

                // Get UIN of the contact changing status
                int length = Util.getByte(buf, 0);
                authMarker += 1;
                String uin = Util.byteArrayToString(buf, authMarker, length);
                authMarker += length;

                // Get granted boolean
                boolean granted = false;
                if (Util.getByte(buf, authMarker) == 0x01)
                {
                    granted = true;
                }
                authMarker += 1;

                // Get reason only of not granted
                SystemNotice notice;
                if (!granted)
                {
                    length = Util.getWord(buf, authMarker);
                    String reason = Util.byteArrayToString(buf, authMarker, length + 2);
                    // Create a new system notice
                    if (length == 0)
                        notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHREPLY, uin, granted, null);
                    else
                        notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHREPLY, uin, granted, reason);
                } else
                {
                    // Create a new system notice
                    System.out.println("Auth granted");
                    notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHREPLY, uin, granted, "");
                }

                // Handle the new system notice
                Jimm.jimm.getContactListRef().addMessage(notice);
            }

        }

    }

}