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
 File: src/jimm/ChatHistory.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimm;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.StringItem;

import jimm.comm.Message;
import jimm.comm.PlainMessage;
import jimm.comm.SystemNotice;
import jimm.comm.UrlMessage;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

public class ChatHistory
{

    private Vector historyVector;
    
    public ChatHistory()
    {
        historyVector = new Vector();
    }
    
    // Adds a message to the message display
    protected synchronized void addMessage(int nr,Message message,ContactListContactItem contact)
    {
        Form msgDisplay = (Form) historyVector.elementAt(nr);
        
        if (message instanceof PlainMessage)
        {
            PlainMessage plainMsg = (PlainMessage) message;
            if (!msgDisplay.isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_PLAIN);
            this.addTextToForm(nr,contact.getName(), plainMsg.getText(), "", plainMsg.getDate(), true);
        }
        if (message instanceof UrlMessage)
        {
            UrlMessage urlMsg = (UrlMessage) message;
            if (!msgDisplay.isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_URL);
            this.addTextToForm(nr,contact.getName(), urlMsg.getText(), urlMsg.getUrl(), urlMsg.getDate(), false);
        }
        if (message instanceof SystemNotice)
        {
            SystemNotice notice = (SystemNotice) message;
            if (!msgDisplay.isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_SYS_NOTICE);

            if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_YOUWEREADDED)
            {
                this.addTextToForm(nr,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("youwereadded")
                        + notice.getSndrUin(), "", notice.getDate(), false);
            } else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREQ)
            {
                contact.increaseMessageCount(ContactListContactItem.MESSAGE_AUTH_REQUEST);
                this.addTextToForm(nr,ResourceBundle.getString("sysnotice"), notice.getSndrUin()
                        + ResourceBundle.getString("wantsyourauth") + notice.getReason(), "", notice.getDate(), false);
            } else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREPLY)
            {
                if (notice.isAUTH_granted())
                {
                    contact.setBoolValue(ContactListContactItem.VALUE_NO_AUTH,false);
                    this.addTextToForm(nr,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("grantedby")
                            + notice.getSndrUin() + ".", "", notice.getDate(), false);
                    Jimm.jimm.getContactListRef().refreshVisibleList(true);
                } else if (notice.getReason() != null)
                    this.addTextToForm(nr,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("denyedby")
                            + notice.getSndrUin() + ". " + ResourceBundle.getString("reason") + ": " + notice.getReason(),
                            "", notice.getDate(), false);
                else
                    this.addTextToForm(nr,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("denyedby")
                            + notice.getSndrUin() + ". " + ResourceBundle.getString("noreason"), "", notice.getDate(),
                            false);
            }
        }
    }
    
    // Add text to message form
    public void addTextToForm(int nr,String from, String message, String url, Date time, boolean red)
    {
        Form msgDisplay = (Form) historyVector.elementAt(nr);
        
        Calendar stamp = Calendar.getInstance();
        stamp.setTime(time);
        
        Font prequelFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        Image prequel;
        // #sijapp cond.if target is "MIDP2"#
        prequel = Image.createImage(msgDisplay.getWidth(), prequelFont.getHeight() + 2);
        Font textFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        // #sijapp cond.else#
        prequel = Image.createImage(120, prequelFont.getHeight() + 2);
        // #sijapp cond.end#
        Graphics g = prequel.getGraphics();
        if (!red)
            g.setColor(0, 0, 255);
        else
            g.setColor(255, 0, 0);
        g.setFont(prequelFont);
        g.drawString(from + " (" + stamp.get(Calendar.HOUR_OF_DAY) + ":" + Util.makeTwo(stamp.get(Calendar.MINUTE)) + "):", 0, 10,
                Graphics.BASELINE | Graphics.LEFT);
        Image copy = Image.createImage(prequel);

        msgDisplay.append(new ImageItem(null, copy, ImageItem.LAYOUT_LEFT + ImageItem.LAYOUT_NEWLINE_BEFORE
                + ImageItem.LAYOUT_NEWLINE_AFTER, null));
        if (url.length() > 0)
        {
            StringItem urlItem = new StringItem(null,ResourceBundle.getString("url")+": "+url);
//          #sijapp cond.if target is "MIDP2"#
            urlItem.setFont(textFont);
//          #sijapp cond.end#
            msgDisplay.append(urlItem);
        }
        StringItem messageItem = new StringItem(null, message);
//      #sijapp cond.if target is "MIDP2"#
        messageItem.setFont(textFont);
//      #sijapp cond.end#
        msgDisplay.append(messageItem);
    }
    
    // Returns the chat history form at the given nr
    public Form getChatHistoryAt(int nr)
    {
        if (historyVector.size() > 0 && nr != -1)
            return (Form) historyVector.elementAt(nr);
        else
            return new Form("Error");
    }
    
    // Delete the chat hisotry at nr
    public void chatHistoryDelete(int nr)
    {
        if (historyVector.size() > 0)
        {
            Form temp = (Form)historyVector.elementAt(nr);
            temp.delete(0);
        }
    }
    
    // Returns if the chat history at the given number is shown
    public boolean chatHistoryShown(int nr)
    {
        if (historyVector.size() > 0)
        {
            Form temp = (Form)historyVector.elementAt(nr);
            return temp.isShown();
        }
        else
            return false;
    }
    
    // Returns the size of the chat histore at number nr
    public int chatHistorySize(int nr){
        {
            if ((historyVector.size() > nr) && (nr != -1))
            {
                Form temp = (Form)historyVector.elementAt(nr);
                return temp.size();
            }
            else
                return -1;
        }
    }

    // Creates a new chat form and returns the index number of it in the vector
    public int newChatForm(String name)
    {
        Form chatForm = new Form(name);
        historyVector.addElement(chatForm);
        return historyVector.size()-1;
    }

}