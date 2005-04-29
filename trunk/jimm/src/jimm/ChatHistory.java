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
 File: src/jimm/ChatHistory.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher, Artyomov Denis
 *******************************************************************************/

package jimm;

import java.util.Date;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Canvas;

import jimm.comm.Message;
import jimm.comm.PlainMessage;
import jimm.comm.SystemNotice;
import jimm.comm.UrlMessage;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

import DrawControls.TextList;

class ChatTextList extends TextList
{
	
    ChatTextList()
	{
		super
		(
            null, 
            TextList.getDefCapColor(),
            TextList.getDefCapFontColor(),
            TextList.getDefBackColor(),
            Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_CHAT_SMALL_FONT) 
               ? TextList.SMALL_FONT : TextList.MEDIUM_FONT,
            TextList.SEL_NONE
        );
		Jimm.setColorScheme(this);
	}
	
	protected void userPressKey(int keyCode) 
	{
		switch (getGameAction(keyCode))
		{
		case Canvas.LEFT: 
		    Jimm.jimm.getChatHistoryRef().incCounter(false);
			Jimm.jimm.getContactListRef().showNextPrevChat(false);
			break;
			
		case Canvas.RIGHT:
		    Jimm.jimm.getChatHistoryRef().incCounter(true);
			Jimm.jimm.getContactListRef().showNextPrevChat(true);
			break;
		}
	}
	
}

public class ChatHistory
{
    private Vector historyVector;
    private int counter;
    private int deleted;
    
    public ChatHistory()
    {
        historyVector = new Vector();
        counter = 1;
        deleted = 0;
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
            
            // #sijapp cond.if modules_HISTORY is "true" #
            if ( Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_HISTORY) )
            	Jimm.jimm.getHistory().addText(contact.getUin(), plainMsg.getText(), (byte)0, contact.getName());
            // #sijapp cond.end#	
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
                    //Jimm.jimm.getContactListRef().refreshVisibleList(true);
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
    synchronized public void addTextToForm(int nr,String from, String message, String url, Date time, boolean red)
    {
        
        TextList msgDisplay = (TextList) historyVector.elementAt(nr);
        
        if (msgDisplay.getSize() == 0)
        {
            deleted--;
        }
        
        msgDisplay.lock();
        int lastSize = msgDisplay.getItemCount();
        msgDisplay.addBigText
        (
          from + " (" + Util.getDateString(true) + "):",
          red ? 0xFF0000 : Jimm.jimm.getOptionsRef().getSchemeColor(Options.CLRSCHHEME_BLUE), 
          Font.STYLE_BOLD
        );

        if (url.length() > 0)
        {
            msgDisplay.addBigText(ResourceBundle.getString("url")+": "+url, 0x00FF00, Font.STYLE_PLAIN);
        }
        
        msgDisplay.addBigText(message, msgDisplay.getTextColor(), Font.STYLE_PLAIN);
        
        msgDisplay.setTopItem(lastSize);        
        msgDisplay.unlock();
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
            deleted++;
            counter = ((counter--) % (this.historyVector.size()-deleted))+1;
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
    public int chatHistorySize(int nr)
    {
        if ((historyVector.size() > nr) && (nr != -1))
        {
            TextList temp = (TextList) historyVector.elementAt(nr);
            return temp.getItemCount();
        } else
            return -1;
    }
    
    // Return the size of the History Vector minus deleted
    public int chatHistoryVectorSize(int nr)
    {
        if (this.historyVector.size() != 0)
        {
            TextList temp = (TextList) historyVector.elementAt(nr);
            if (temp.getSize() == 0)
                return this.historyVector.size() - deleted - 1;
            else
                return this.historyVector.size() - deleted;
        }
        else
            return 0;
    }
    
	// Return the counter for the ChatHistory
	public int getCounter()
	{
	    return counter;
	}
	
	// Sets the counter for the ChatHistory
	public void incCounter(boolean up){
	    if (up)
	        counter = ((counter++) % (this.historyVector.size()-deleted))+1;
	    else
	        counter = ((counter--) % (this.historyVector.size()-deleted))+1;
	}
    
    // Creates a new chat form and returns the index number of it in the vector
    public int newChatForm(String name)
    {
    	ChatTextList chatForm = new ChatTextList();

        // Calculate the title for the chatdisplay.
    	// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    	chatForm.setFullScreenMode(false);
        if (this.historyVector.size()-deleted == 0)
            chatForm.setTitle(name+" ("+counter+"/"+(this.historyVector.size()-deleted+1)+")");
        else
            chatForm.setTitle(name+" ("+((counter % (this.historyVector.size()-deleted))+1)+"/"+(this.historyVector.size()-deleted+1)+")");
        // #sijapp cond.else#
        if (this.historyVector.size()-deleted == 0)
            chatForm.setCaption(name+" ("+counter+"/"+(this.historyVector.size()-deleted+1)+")");
        else
            chatForm.setCaption(name+" ("+((counter % (this.historyVector.size()-deleted))+1)+"/"+(this.historyVector.size()-deleted+1)+")");
        // #sijapp cond.end#
        
        // Has to be ++ cause the subsequent added text will decrease -- because it is cleared
        deleted++;
        
        historyVector.addElement(chatForm);
        return historyVector.size()-1;
    }
    
    public void setColorScheme()
    {
    	int count = historyVector.size();
    	for (int i = 0; i < count; i++) Jimm.setColorScheme((ChatTextList)historyVector.elementAt(i));
    }

}
