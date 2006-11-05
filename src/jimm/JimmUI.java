/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-06  Jimm Project

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
 Author(s): Artyomov Denis, Igor Palkin, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.TimerTask;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import jimm.comm.Icq;
import jimm.comm.RequestInfoAction;
import jimm.util.ResourceBundle;
import DrawControls.TextList;
import DrawControls.VirtualList;

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
	final private static Command cmdBack2    = new Command(ResourceBundle.getString("back"),          Command.ITEM,   10); /* Back button to fix Symbian bug */
	final private static Command cmdCopyText = new Command(ResourceBundle.getString("copy_text"),     Command.ITEM,   3);
	final private static Command cmdCopyAll  = new Command(ResourceBundle.getString("copy_all_text"), Command.ITEM,   4);  

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
		commands.put(cmdBack2,            new Integer(CMD_BACK)  );
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
			if ((c == cmdCancel) || (c == cmdBack) || (c == cmdBack2)) cancelUserInfo();
			
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
			
			actionTag = -1;
		}
		
		// Message box
		if (d == msgForm)
		{
			listener.commandAction(c, d);
			msgForm = null;
			actionTag = -1;
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
	static private int actionTag = -1;

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

		JimmUI.listener = listener;
		msgForm.setCommandListener(_this);
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
		
	
		if (lastDisplayable_ != null) lastDisplayable = lastDisplayable_;
		if (aboutTextList == null) aboutTextList = new TextList(null);
		
		aboutTextList.lock();
		aboutTextList.clear();
		aboutTextList.setCursorMode(TextList.SEL_NONE);
		setColorScheme(aboutTextList);
		aboutTextList.setColors(0xffffff, 0x006fb1, 0x006fb1, 0x006fb1, 0xffffff);
		
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA"#
		aboutTextList.setFontSize(Font.SIZE_MEDIUM);
		//#sijapp cond.else#
		aboutTextList.setFontSize(Font.SIZE_SMALL);
		//#sijapp cond.end#

		aboutTextList.setCaption(ResourceBundle.getString("about"));
		
		String commaAndSpace = ", "; 
	    
		StringBuffer str = new StringBuffer();
		str.append(" ").append(ResourceBundle.getString("about_info")).append("\n\n")
		
		   .append(ResourceBundle.getString("cell_phone")).append(": ")
		   .append(Jimm.microeditionPlatform);
		
		if (Jimm.microeditionProfiles != null) str.append(commaAndSpace).append(Jimm.microeditionProfiles);
		
		String locale = System.getProperty("microedition.locale");
		if (locale != null) str.append(commaAndSpace).append(locale);
		
		str.append("\n\n")
		   .append(ResourceBundle.getString("free_heap")).append(": ")
		   .append(freeMem).append("kb\n")
		   .append(ResourceBundle.getString("total_mem")).append(": ")
		   .append(Runtime.getRuntime().totalMemory()/1024)
		   .append("kb\n\n")
		   .append(ResourceBundle.getString("latest_ver")).append(':');
		
		if (versionLoaded) str.append(' ').append(version);
		else str.append(" ...");
		
		try
		{
			Image image = SplashCanvas.getSplashImage();
			aboutTextList.addBigText("\n", 0xffffff, Font.STYLE_PLAIN, -1)
				.addImage(image, null, image.getWidth(), image.getHeight(), -1)
				.addBigText(str.toString(), 0xffffff, Font.STYLE_PLAIN, -1);
		
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
			Options.getSchemeColor(Options.CLRSCHHEME_TEXT),
			Options.getSchemeColor(Options.CLRSCHHEME_CAP),
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
        HttpConnection httemp;
        InputStream istemp;
        
        String version;

        // Timer routine
        public void run()
        {
            try
            {
                httemp = (HttpConnection) Connector.open("http://www.jimm.org/en/current_ver");
                if (httemp.getResponseCode() != HttpConnection.HTTP_OK) throw new IOException();
                istemp = httemp.openInputStream();
                byte[] version_ = new byte[(int)httemp.getLength()];
                istemp.read(version_,0,version_.length);
                version = new String(version_);
                versionLoaded = true;
            } catch (Exception e)
            {
                e.printStackTrace();
            	version = ResourceBundle.getString("");
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
				if (item != null) item.showClientInfo();
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
	final public static int UI_UIN_LIST   = 35;
	
	//////
	final public static int UI_LAST_ID    = 36;
	
	static private int uiBigTextIndex;
	static private String uiSectName = null;
	
	static private void addToTextList(String str, String langStr, TextList list)
	{
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
	
	static private void addToTextList(int index, String[] data, String langStr, TextList list)
	{
		String str = data[index];
		if (str == null) return;
		if (str.length() == 0) return;

		addToTextList(str, langStr, list);
	}
	
	static public void fillUserInfo(String[] data, TextList list)
	{
		uiSectName = "main_info";
		addToTextList(UI_UIN_LIST,  data, "uin",        list);
		addToTextList(UI_NICK,      data, "nick",       list);
		addToTextList(UI_NAME,      data, "name",       list);
		addToTextList(UI_GENDER,    data, "gender",     list);
		addToTextList(UI_AGE,       data, "age",        list);
		addToTextList(UI_EMAIL,     data, "email",      list);
		if (data[UI_AUTH] != null) addToTextList
		(
			data[UI_AUTH].equals("1") ? ResourceBundle.getString("yes") : ResourceBundle.getString("no"),
			"auth", list
		);
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
		infoTextList.addCommand(cmdBack2);
	}
	
	static public TextList getInfoTextList(String caption, boolean addCommands)
	{
		infoTextList = new TextList(null);
		
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA"#
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
			infoTextList.addCommand(cmdBack2);
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
	
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	static public void addMessageText(TextList textList, String text, int messTotalCounter)
	{
		//#sijapp cond.if modules_SMILES is "true" #
		Emotions.addTextWithEmotions(textList, text, Font.STYLE_PLAIN, textList.getTextColor(), messTotalCounter);
		//#sijapp cond.else#
		textList.addBigText(text, textList.getTextColor(), Font.STYLE_PLAIN, messTotalCounter);
		//#sijapp cond.end#
		textList.doCRLF(messTotalCounter);
	}
	
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	private final static long[] statuses = 
	{
		ContactList.STATUS_AWAY,
		ContactList.STATUS_CHAT,
		ContactList.STATUS_DND,
		ContactList.STATUS_INVISIBLE,
		ContactList.STATUS_NA,
		ContactList.STATUS_OCCUPIED,
		ContactList.STATUS_OFFLINE,
		ContactList.STATUS_ONLINE,
		ContactList.STATUS_INVIS_ALL,
	};
	
	//#sijapp cond.if target="MOTOROLA"#
	public final static int[] st_colors =
	{
		0x00AACC,
		0xFFFF00,
		0xFF8888,
		0xBBBBBB,
		0xAA0088,
		0xFFCC66,
		0xFF0000,
		0x00FF00,
		0x888888,
		0x0000FF
	};
	//#sijapp cond.end#
	
	public final static int[] imageIndexes = { 0, 1, 2, 3, 4, 5, 6, 7, 3 };
	
	private final static String[] statusStrings = 
	{
		"status_away",
		"status_chat",
		"status_dnd",
		"status_invisible",
		"status_na",
		"status_occupied",
		"status_offline",
		"status_online",
		"status_invis_all"
	};
	
	public static int getStatusIndex(long status)
	{
		for (int i = 0; i < statuses.length; i++) if (statuses[i] == status) return i;
		return -1;
	}
	
	public static int getStatusImageIndex(long status)
	{
		int index = getStatusIndex(status);
		return (index == -1) ? -1 : imageIndexes[index];
	}

	public static String getStatusString(long status)
	{
		int index = getStatusIndex(status);
		return (index == -1) ? null : ResourceBundle.getString(statusStrings[index]);
	}

	
	public static final int SHS_TYPE_ALL   = 1;
	public static final int SHS_TYPE_EMPTY = 2;
	
	public static int[] showGroupSelector(String caption, int tag, CommandListener listener, int type, int excludeGroupId)
	{
		ContactListGroupItem[] groups = ContactList.getGroupItems();
		String[] groupNamesTmp = new String[groups.length];
		int[] groupIdsTmp = new int[groups.length];

		int index = 0;
		for (int i = 0; i < groups.length; i++)
		{
			int groupId = groups[i].getId();
			if (groupId == excludeGroupId) continue;
			switch (type)
			{
			case SHS_TYPE_EMPTY:
				ContactListContactItem[] cItems = ContactList.getGroupItems(groupId);
				if (cItems.length != 0) continue;
				break;
			}
			
			groupIdsTmp[index] = groupId;
			groupNamesTmp[index] = groups[i].getName();
			index++;
		}
		
		if (index == 0)
		{
			Alert alert = new Alert("", ResourceBundle.getString("no_availible_groups"), null, AlertType.INFO );
			alert.setTimeout(Alert.FOREVER);
			Jimm.display.setCurrent(alert);
			return null;
		}
		
		String[] groupNames = new String[index];
		int[] groupIds = new int[index];

		System.arraycopy(groupIdsTmp, 0, groupIds, 0, index);
		System.arraycopy(groupNamesTmp, 0, groupNames, 0, index);
			
		showSelector(ResourceBundle.getString(caption), groupNames, listener, tag, false);
		
		return groupIds;
	}
	

}
