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
 Version: 0.2.2  Date: 2004/07/12
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimm;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import javax.microedition.lcdui.Font;

import jimm.comm.Message;
import jimm.comm.PlainMessage;
import jimm.comm.SystemNotice;
import jimm.comm.UrlMessage;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

import DrawControls.TextList;

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
        TextList msgDisplay = (TextList) historyVector.elementAt(nr);
        
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
        TextList msgDisplay = (TextList) historyVector.elementAt(nr);
        int color, lastSize;
        
        Calendar stamp = Calendar.getInstance();
        stamp.setTime(time);
        
        if (!red) color = 0xFF;
        else color = 0xFF0000;
        
        lastSize = msgDisplay.getItemCount();
        msgDisplay.addBigText
        (
          from + " (" + stamp.get(Calendar.HOUR_OF_DAY) + ":" 
               + Util.makeTwo(stamp.get(Calendar.MINUTE)) + "):",
          color, 
          Font.STYLE_BOLD
        );

        
        if (url.length() > 0)
        {
            msgDisplay.addBigText(ResourceBundle.getString("url")+": "+url, 0x00FF00, Font.STYLE_PLAIN);
        }
        
        msgDisplay.addBigText(message, 0x0, Font.STYLE_PLAIN);
        
        msgDisplay.setTopItem(lastSize);        
    }
    
    // Returns the chat history form at the given nr
    public TextList getChatHistoryAt(int nr)
    {
        if (historyVector.size() > 0 && nr != -1)
            return (TextList) historyVector.elementAt(nr);
        else
            return new TextList("Error");
    }
    
    // Delete the chat hisotry at nr
    public void chatHistoryDelete(int nr)
    {
        if (historyVector.size() > 0)
        {
            TextList temp = (TextList)historyVector.elementAt(nr);
            temp.clear();
        }
    }
    
    // Returns if the chat history at the given number is shown
    public boolean chatHistoryShown(int nr)
    {
        if (historyVector.size() > 0)
        {
            TextList temp = (TextList)historyVector.elementAt(nr);
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
                TextList temp = (TextList)historyVector.elementAt(nr);
                return temp.getItemCount();
            }
            else
                return -1;
        }
    }

    // Creates a new chat form and returns the index number of it in the vector
    public int newChatForm(String name)
    {
        TextList chatForm = new TextList
                            (
                              name, 
                              TextList.getDefCapColor(),
                              TextList.getDefCapFontColor(),
                              TextList.getDefBackColor(),
                              TextList.SMALL_FONT,
                              TextList.SEL_NONE
                            );
        historyVector.addElement(chatForm);
        return historyVector.size()-1;
    }

}