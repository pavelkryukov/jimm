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
 File: src/jimm/JimmUI.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Igor Palkin
 *******************************************************************************/

package jimm;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.lcdui.*;

import java.io.DataInputStream;
import java.util.*;

import DrawControls.*;
import jimm.comm.Icq;
import jimm.comm.RequestInfoAction;
import jimm.util.ResourceBundle;
import jimm.ContactListContactItem;
import jimm.ContactList;

public class JimmUI implements CommandListener
{
	// Commands codes
	final public static int CMD_OK     = 1;
	final public static int CMD_CANCEL = 2;
	final public static int CMD_YES    = 3;
	final public static int CMD_NO     = 4;
	final public static int CMD_FIND   = 5;
	final public static int CMD_BACK   = 6;
	
	// Commands
	final private static Command cmdOk       = new Command(ResourceBundle.getString("ok"),            Command.OK,     1);
	final private static Command cmdCancel   = new Command(ResourceBundle.getString("cancel"),        Command.BACK,   2);
	final private static Command cmdYes      = new Command(ResourceBundle.getString("yes"),           Command.OK,     1);
	final private static Command cmdNo       = new Command(ResourceBundle.getString("no"),            Command.CANCEL, 2);
	final private static Command cmdFind     = new Command(ResourceBundle.getString("find"),          Command.OK,     1);
	final private static Command cmdBack     = new Command(ResourceBundle.getString("back"),          Command.BACK,   2);
	final private static Command cmdCopyText = new Command(ResourceBundle.getString("copy_text"),     Command.ITEM,   2);
	final private static Command cmdCopyAll  = new Command(ResourceBundle.getString("copy_all_text"), Command.ITEM,   3);  

	static private CommandListener listener;
	static private Hashtable commands = new Hashtable();
	static private Displayable lastDisplayable;
	static private JimmUI _this;
	
	// Associate commands and commands codes 
	static 
	{
		commands.put(cmdOk,               new Integer(CMD_OK)    );
		commands.put(List.SELECT_COMMAND, new Integer(CMD_OK)    );
		commands.put(cmdCancel,           new Integer(CMD_CANCEL));
		commands.put(cmdYes,              new Integer(CMD_YES)   );
		commands.put(cmdNo,               new Integer(CMD_NO)    );
		commands.put(cmdFind,             new Integer(CMD_FIND)  );
		commands.put(cmdBack,             new Integer(CMD_BACK)  );
	}
	
	JimmUI()
	{
		_this = this;
	}
	
	// Returns commands index of command
	public static int getCommandIdx(Command command)
	{
		Object result = commands.get(command);
		return (result == null) ? -1 : ((Integer)result).intValue();  
	}
	
	// Place "object = null;" code here:
	private static void clearAll()
	{
		msgForm = null;
		aboutTextList = null;
		System.gc();
	}
	
	public void commandAction(Command c, Displayable d)
	{
		// "About" -> "Back"
		if ((d == aboutTextList) && (c == cmdBack))
		{
			synchronized(_this)
			{
				Jimm.display.setCurrent(lastDisplayable);
				aboutTextList = null;
			}
		}
		
		// "User info"
		if (d == infoTextList)
		{
			// "User info" -> "Cancel, Back"
			if ((c == cmdCancel) || (c == cmdBack)) cancelUserInfo();
			
			// "User info" -> "Copy text, Copy all"
			else if ((c == cmdCopyText) || (c == cmdCopyAll))
			{
				JimmUI.setClipBoardText
				(
					"["+getCaption(infoTextList)+"]\n"
					+infoTextList.getCurrText(0, (c == cmdCopyAll))
				);
			}
		}
		
		// "Selector"
		if (d == lstSelector)
		{
			lastSelectedItemIndex = lstSelector.getSelectedIndex();
			
			// "Selector" -> "Cancel"
			if (c == cmdCancel) Jimm.display.setCurrent(lastDisplayable);
			
			// "Selector" -> "Ok"
			else if ((c == cmdOk) || (c == List.SELECT_COMMAND)) listener.commandAction(c, d);
			
			lstSelector = null;
		}
	}
	
	public static void setCaption(Displayable ctrl, String caption)
	{
		if (ctrl instanceof VirtualList)
		{
			VirtualList vl = (VirtualList)ctrl;
			vl.setCaption(caption);
		}
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		else ctrl.setTitle(caption);
		// #sijapp cond.end#
	}
	
	public static String getCaption(Displayable ctrl)
	{
		if (ctrl == null) return null;
		String result = null;
		if (ctrl instanceof VirtualList)
		{
			VirtualList vl = (VirtualList)ctrl;
			result = vl.getCaption();
		}
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		else result = ctrl.getTitle();
		// #sijapp cond.end#
		
		return result;
	}
	
	
	/////////////////////////
	//                     // 
	//     Message Box     //
	//                     //
	/////////////////////////
	static private Form msgForm;
	static private int actionTag;

	public static int getCommandType(Command testCommand, int testTag)
	{
		return (actionTag == testTag) ? getCommandIdx(testCommand) : -1;
	}

	final public static int MESBOX_YESNO    = 1;
	final public static int MESBOX_OKCANCEL = 2;
	static public void messageBox(String cap, String text, int type, CommandListener listener, int tag)
	{
		clearAll();
		
		actionTag = tag;
		msgForm = new Form(cap);
		msgForm.append(text);
		
		switch (type)
		{
		case MESBOX_YESNO:
			msgForm.addCommand(cmdYes);
			msgForm.addCommand(cmdNo);
			break;
			
		case MESBOX_OKCANCEL:
			msgForm.addCommand(cmdOk);
			msgForm.addCommand(cmdCancel);
			break;
		}

		msgForm.setCommandListener(listener);
		Jimm.display.setCurrent(msgForm);
	}
	
	//////////////////////////////////////////////////////////////////////////////
	private static TextList aboutTextList;
    
    // String for recent version
    static private String version;
    static private boolean versionLoaded = false;
    
	static public void about(Displayable lastDisplayable_)
	{
		System.gc();
		long freeMem = Runtime.getRuntime().freeMemory()/1024;
		
		final int textColor = Options.getSchemeColor(Options.CLRSCHHEME_TEXT);
	
		if (lastDisplayable_ != null) lastDisplayable = lastDisplayable_;
		if (aboutTextList == null) aboutTextList = new TextList(null);
		
		aboutTextList.lock();
		aboutTextList.clear();
		aboutTextList.setCursorMode(TextList.SEL_NONE);
		setColorScheme(aboutTextList);
		
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		aboutTextList.setFontSize(Font.SIZE_MEDIUM);
		//#sijapp cond.else#
		aboutTextList.setFontSize(Font.SIZE_SMALL);
		//#sijapp cond.end#

		aboutTextList.setCaption(ResourceBundle.getString("about"));
	    
		StringBuffer str = new StringBuffer();
		str.append(" ").append(ResourceBundle.getString("about_info")).append("\n")
		   .append(ResourceBundle.getString("free_heap")).append(": ")
		   .append(freeMem).append("kb\n\n")
		   .append(ResourceBundle.getString("latest_ver")).append(":");
		
		if (versionLoaded) str.append(" ").append(version);
		else str.append(" ...");
		
		try
		{
			Image image = Image.createImage("/icon.png");
			aboutTextList
				.addImage(image, null, image.getWidth(), image.getHeight(), -1)
				.addBigText(str.toString(), textColor, Font.STYLE_PLAIN, -1);
		
			aboutTextList.addCommand(cmdBack);
			aboutTextList.setCommandListener(_this);
           
            // Set the color sceme (background would not fit otherwise)
			Jimm.display.setCurrent(aboutTextList);
		}
		catch (Exception e) {}
		
		aboutTextList.unlock();
        
		if (!versionLoaded) Jimm.jimm.getTimerRef().schedule(new GetVersionInfoTimerTask(), 2000);
	}
    	
	//////////////////////
	//                  //
	//    Clipboard     //
	//                  //
	//////////////////////
	
	static private String clipBoardText;
	
	static private String insertQuotingChars(String text, String qChars)
	{
		StringBuffer result = new StringBuffer();
		int size = text.length();
		boolean wasNewLine = true;
		for (int i = 0; i < size; i++)
		{
			char chr = text.charAt(i);
			if (wasNewLine) result.append(qChars);
			result.append(chr);
			wasNewLine = (chr == '\n');
		}
		
		return result.toString();
	}
	
	static public String getClipBoardText()
	{
		return clipBoardText;
	}
	
	static public void setClipBoardText(String text)
	{
		clipBoardText = text;
	}
	
	static public void setClipBoardText(boolean incoming, String date, String from, String text)
	{
		StringBuffer sb = new StringBuffer();
		sb.append('[').append(from).append(' ').append(date).append(']').append(CRLFstr)
		  .append( insertQuotingChars(text, incoming ? ">> " : "<< ") );
		clipBoardText = sb.toString();
	}
	
	static public void clearClipBoardText()
	{
		clipBoardText = null;	
	}
	
	final public static String CRLFstr = "\n";
	
	////////////////////////
	//                    //
	//    Color scheme    //
	//                    //
	////////////////////////
	
	static public void setColorScheme(VirtualList vl)
	{
		if (vl == null) return;
		
		vl.setColors
		(
			Options.getSchemeColor(Options.CLRSCHHEME_BLUE),
			Options.getSchemeColor(Options.CLRSCHHEME_BACK),
			Options.getSchemeColor(Options.CLRSCHHEME_BLUE),
			Options.getSchemeColor(Options.CLRSCHHEME_TEXT)
		);
	}
	
	static public void setColorScheme()
	{
		// #sijapp cond.if modules_HISTORY is "true" #
		HistoryStorage.setColorScheme();
		// #sijapp cond.end#
		
		ChatHistory.setColorScheme();
		setColorScheme((VirtualList)ContactList.getVisibleContactListRef());
	}
    
    /*****************************************************************************/
    /*****************************************************************************/
    /*****************************************************************************/


    // Waits until contact listupdate is completed
    public static class GetVersionInfoTimerTask extends TimerTask
    {

        // Try to get current Jimm version from Jimm server
        ContentConnection ctemp;
        DataInputStream istemp;
        byte[] version_;

        // Timer routine
        public void run()
        {
            try
            {
                String url = "http://www.jimm.org/en/current_ver";
                ctemp = (ContentConnection) Connector.open(url);

                istemp = ctemp.openDataInputStream();
                version_ = new byte[istemp.available()];
                istemp.readFully(version_);
                version = new String(version_);
                versionLoaded = true;
            } catch (Exception e)
            {
                version = ResourceBundle.getString("no_recent_ver");
            }
            
            synchronized(_this)
            {
            	if ((aboutTextList != null) && aboutTextList.isShown())
            	{
            		aboutTextList.addBigText
            		(
            			version,
            			aboutTextList.getTextColor(),
            			Font.STYLE_PLAIN,
            			-1
            		);
            	}
            }
        }

    }
    
    /************************************************************************/
    /************************************************************************/
    /************************************************************************/
    
    ///////////////////
    //               //
    //    Hotkeys    //
    //               //
    ///////////////////

	static public void execHotKey(ContactListContactItem cItem, int keyCode, int type)
	{
		switch (keyCode)
		{
		case Canvas.KEY_NUM0:
			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEY0), cItem, type);
			break;
		case Canvas.KEY_NUM4:
			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEY4), cItem, type);
			break;
			
		case Canvas.KEY_NUM6:
			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEY6), cItem, type);
			break;

		case Canvas.KEY_STAR:
			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEYSTAR), cItem, type);
			break;
			
		case Canvas.KEY_POUND:
			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEYPOUND), cItem, type);
			break;
			
			
		// #sijapp cond.if target is "SIEMENS2"#
		case -11:
			// This means the CALL button was pressed...
			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEYCALL), cItem, type);
			break;
		// #sijapp cond.end#
			
		}
	}
	
	private static long lockPressedTime = -1;
	static private void execHotKeyAction(int actionNum, ContactListContactItem item, int keyType)
	{
		if (keyType == VirtualList.KEY_PRESSED)
		{
			lockPressedTime = System.currentTimeMillis();
			
			switch (actionNum)
			{

			// #sijapp cond.if modules_HISTORY is "true" #
			case Options.HOTKEY_HISTORY:
				if (item != null) item.showHistory();
				break;
			// #sijapp cond.end#

			case Options.HOTKEY_INFO:
				if (item != null)
					requiestUserInfo
					(
						item.getStringValue(ContactListContactItem.CONTACTITEM_UIN),
						item.getStringValue(ContactListContactItem.CONTACTITEM_NAME)
					);
				break;

			case Options.HOTKEY_NEWMSG:
				if (item != null) item.newMessage();
				break;

			case Options.HOTKEY_ONOFF:
				if (Options.getBoolean(Options.OPTION_CL_HIDE_OFFLINE)) 
					Options.setBoolean(Options.OPTION_CL_HIDE_OFFLINE, false);
				else 
					Options.setBoolean(Options.OPTION_CL_HIDE_OFFLINE, true);
				Options.safe_save();
				ContactList.optionsChanged(true, false);
				ContactList.activate();
				break;

			case Options.HOTKEY_OPTIONS:
				Options.editOptions();
				break;

			case Options.HOTKEY_MENU:
				MainMenu.activate();
				break;
				
			// #sijapp cond.if target is "MIDP2"#
			case Options.HOTKEY_MINIMIZE:
				Jimm.setMinimized(true);
				break;
			// #sijapp cond.end#
				
			case Options.HOTKEY_CLI_INFO:
				item.showClientInfo();
				break;
				
			//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			case Options.HOTKEY_FULLSCR:
				boolean fsValue = !Options.getBoolean(Options.OPTION_FULL_SCREEN);
				VirtualList.setFullScreen(fsValue);
				Options.setBoolean(Options.OPTION_FULL_SCREEN, fsValue);
				Options.safe_save();
				break;
			//#sijapp cond.end#
			}
		}

		else if ((keyType == VirtualList.KEY_REPEATED) || (keyType == VirtualList.KEY_RELEASED))
		{
			if (lockPressedTime == -1) return;
			long diff = System.currentTimeMillis()-lockPressedTime;
			if ((actionNum == Options.HOTKEY_LOCK) && (diff > 900))
			{
				lockPressedTime = -1;
				SplashCanvas.lock();
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	//                                                                       //
	//               U S E R   A N D   C L I E N T   I N F O                 //
	//                                                                       //
	///////////////////////////////////////////////////////////////////////////
	
	// Information about the user
	final public static int UI_UIN        = 0;
	final public static int UI_NICK       = 1;
	final public static int UI_NAME       = 2;
	final public static int UI_EMAIL      = 3;
	final public static int UI_CITY       = 4;
	final public static int UI_STATE      = 5;
	final public static int UI_PHONE      = 6;
	final public static int UI_FAX        = 7;
	final public static int UI_ADDR       = 8;
	final public static int UI_CPHONE     = 9;
	final public static int UI_AGE        = 10;
	final public static int UI_GENDER     = 11;
	final public static int UI_HOME_PAGE  = 12;
	final public static int UI_BDAY       = 13;
	final public static int UI_W_CITY     = 14;
	final public static int UI_W_STATE    = 15;
	final public static int UI_W_PHONE    = 16;
	final public static int UI_W_FAX      = 17;
	final public static int UI_W_ADDR     = 18;
	final public static int UI_W_NAME     = 19;
	final public static int UI_W_DEP      = 20;
	final public static int UI_W_POS      = 21;
	final public static int UI_ABOUT      = 22;
	final public static int UI_INETRESTS  = 23;
	final public static int UI_AUTH       = 24;
	final public static int UI_STATUS     = 25;
	final public static int UI_ICQ_CLIENT = 26;
	final public static int UI_SIGNON     = 27;
	final public static int UI_ONLINETIME = 28;
	final public static int UI_IDLE_TIME  = 29;
	
	final public static int UI_ICQ_VERS   = 31;
	final public static int UI_INT_IP     = 32;
	final public static int UI_EXT_IP     = 33;
	final public static int UI_PORT       = 34;
	
	//////
	final public static int UI_LAST_ID    = 35;
	
	static private int uiBigTextIndex;
	static private String uiSectName = null;
	
	static private void addToTextList(int index, String[] data, String langStr, TextList list)
	{
		String str = data[index];
		if (str == null) return;
		if (str.length() == 0) return;

		if (uiSectName != null)
		{
			list.addBigText
			(
				ResourceBundle.getString(uiSectName),
				list.getTextColor(),
				Font.STYLE_BOLD,
				-1
			).doCRLF(-1);
			uiSectName = null;
		}
		
		list.addBigText(ResourceBundle.getString(langStr)+": ", list.getTextColor(), Font.STYLE_PLAIN, uiBigTextIndex)
		  .addBigText(str, Options.getSchemeColor(Options.CLRSCHHEME_BLUE), Font.STYLE_PLAIN, uiBigTextIndex)
		  .doCRLF(uiBigTextIndex);
		uiBigTextIndex++;
	}
	
	static public void fillUserInfo(String[] data, TextList list)
	{
		uiSectName = "main_info";
		addToTextList(UI_NICK,      data, "nick",       list);
		addToTextList(UI_NAME,      data, "name",       list);
		addToTextList(UI_GENDER,    data, "gender",     list);
		addToTextList(UI_AGE,       data, "age",        list);
		addToTextList(UI_EMAIL,     data, "email",      list);
		addToTextList(UI_AUTH,      data, "auth",       list);
		addToTextList(UI_BDAY,      data, "birth_day",  list);
		addToTextList(UI_CPHONE,    data, "cell_phone", list);
		addToTextList(UI_HOME_PAGE, data, "home_page",  list);
		addToTextList(UI_ABOUT,     data, "notes",      list);
		addToTextList(UI_INETRESTS, data, "interests",  list);
		
		if (data[UI_STATUS] != null)
		{
	        int stat = Integer.parseInt(data[UI_STATUS]);
	        int imgIndex = 0;
	        if (stat == 0) imgIndex = 6;
	        else if (stat == 1) imgIndex = 7;
	        else if (stat == 2) imgIndex = 3;
	        list
				.addBigText(ResourceBundle.getString("status") + ": ",list.getTextColor(),Font.STYLE_PLAIN, uiBigTextIndex)
				.addImage
				(
					ContactList.getImageList().elementAt(imgIndex),
					null,
					ContactList.getImageList().getWidth(),
					ContactList.getImageList().getHeight(),
					uiBigTextIndex
				)
				.doCRLF(uiBigTextIndex);
	        uiBigTextIndex++;
		}
		
		uiSectName = "home_info";
		addToTextList(UI_CITY,      data, "city",  list);
		addToTextList(UI_STATE,     data, "state", list);
		addToTextList(UI_ADDR,      data, "addr",  list);
		addToTextList(UI_PHONE,     data, "phone", list);
		addToTextList(UI_FAX,       data, "fax",   list);
		
		uiSectName = "work_info";
		addToTextList(UI_W_NAME,    data, "title",    list);
		addToTextList(UI_W_DEP,     data, "depart",   list);
		addToTextList(UI_W_POS,     data, "position", list);
		addToTextList(UI_W_CITY,    data, "city",     list);
		addToTextList(UI_W_STATE,   data, "state",    list);
		addToTextList(UI_W_ADDR,    data, "addr",     list);
		addToTextList(UI_W_PHONE,   data, "phone",    list);
		addToTextList(UI_W_FAX,     data, "fax",      list);
		
		uiSectName = "dc_info";
		addToTextList(UI_ICQ_CLIENT, data, "icq_client",     list);
		addToTextList(UI_SIGNON,     data, "li_signon_time", list);
		addToTextList(UI_ONLINETIME, data, "li_online_time", list);
		addToTextList(UI_IDLE_TIME,  data, "li_idle_time",   list);
		
		uiSectName = "DC";
		addToTextList(UI_ICQ_VERS, data, "ICQ version", list);
		addToTextList(UI_INT_IP,   data, "Int IP",      list);
		addToTextList(UI_EXT_IP,   data, "Ext IP",      list);
		addToTextList(UI_PORT,     data, "Port",        list);
	}
	
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	static private TextList infoTextList = null;
	
	static public void requiestUserInfo(String uin, String name)
	{
		RequestInfoAction act = new RequestInfoAction(uin, name);
		
		infoTextList = getInfoTextList(uin, false);
		infoTextList.addCommand(cmdCancel);
		infoTextList.setCommandListener(_this);
		
		try
		{
			Icq.requestAction(act);
		}
		catch (JimmException e)
		{
			JimmException.handleException(e);
			if (e.isCritical()) return;
		}
		
		infoTextList.add(ResourceBundle.getString("wait"));
		showInfoTextList(infoTextList);
	}
	
	static private void cancelUserInfo()
	{
		infoTextList = null;
		Jimm.display.setCurrent(lastDisplayable);
	}
	
	static public void showUserInfo(String[] data)
	{
		if (infoTextList == null) return;
		infoTextList.clear();
		JimmUI.fillUserInfo(data, infoTextList);
		infoTextList.removeCommand(cmdCancel);
		infoTextList.addCommand(cmdBack);
		infoTextList.addCommand(cmdCopyText);
		infoTextList.addCommand(cmdCopyAll);
	}
	
	static public TextList getInfoTextList(String caption, boolean addCommands)
	{
		infoTextList = new TextList(null);
		
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		infoTextList.setFontSize(Font.SIZE_MEDIUM);
		// #sijapp cond.else#
		infoTextList.setFontSize(Font.SIZE_SMALL);
		// #sijapp cond.end#

		infoTextList.setCaption(caption);
		
		JimmUI.setColorScheme(infoTextList);
		infoTextList.setCursorMode(TextList.SEL_NONE);
		
		if (addCommands)
		{
			infoTextList.addCommand(cmdBack);
			infoTextList.addCommand(cmdCopyText);
			infoTextList.addCommand(cmdCopyAll);
			infoTextList.setCommandListener(_this);
		}
		
		return infoTextList;
	}
	
	static public void showInfoTextList(TextList list)
	{
		lastDisplayable = Jimm.display.getCurrent();
		Jimm.display.setCurrent(list);
	}
	
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	static public String[] stdSelector = {"currect_contact", "all_contact_except_this", "all_contacts" };
	
	static private List lstSelector;
	
	static private int lastSelectedItemIndex;
	
	static public void showSelector(String caption, String[] elements, CommandListener listener, int tag, boolean translateWords)
	{
		if (translateWords) for (int i = 0; i < elements.length; i++) elements[i] = ResourceBundle.getString(elements[i]);
		actionTag = tag;
		lstSelector = new List(ResourceBundle.getString(caption), List.IMPLICIT, elements, null);
		lstSelector.addCommand(cmdOk);
		lstSelector.addCommand(cmdCancel);
		lstSelector.setCommandListener(_this);
		lastDisplayable = Jimm.display.getCurrent();
		JimmUI.listener = listener;
		Jimm.display.setCurrent(lstSelector);
	}
	
	static public int getLastSelIndex()
	{
		return lastSelectedItemIndex;
	}

}
