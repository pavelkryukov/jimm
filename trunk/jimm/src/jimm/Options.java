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
*******************************************************************************
File: src/jimm/Options.java
Version: ###VERSION###  Date: ###DATE###
Author(s): Manuel Linsmayer, Andreas Rossbacher, Artyomov Denis, Igor Palkin
******************************************************************************/


/*******************************************************************************
Current record store format:

Record #1: VERSION               (UTF8)
Record #2: OPTION KEY            (BYTE)
           OPTION VALUE          (Type depends on key)
           OPTION KEY            (BYTE)
           OPTION VALUE          (Type depends on key)
           OPTION KEY            (BYTE)
           OPTION VALUE          (Type depends on key)
           ...

Option key            Option value
  0 -  63 (00XXXXXX)  UTF8
 64 - 127 (01XXXXXX)  INTEGER
128 - 191 (10XXXXXX)  BOOLEAN
192 - 224 (110XXXXX)  LONG
225 - 255 (111XXXXX)  SHORT, BYTE-ARRAY (scrambled String)
******************************************************************************/


package jimm;

import jimm.ContactList;
import jimm.comm.Util;
import jimm.comm.Icq;
import jimm.util.ResourceBundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;


public class Options
{


	// Option keys
	public static final int OPTION_UIN                            =   0;   /* String  */
	public static final int OPTION_PASSWORD                       = 225;   /* String  */
	public static final int OPTION_SRV_HOST                       =   1;   /* String  */
	public static final int OPTION_SRV_PORT                       =   2;   /* String  */
	public static final int OPTION_KEEP_CONN_ALIVE                = 128;   /* boolean */
    public static final int OPTION_CONN_ALIVE_INVTERV             =  13;   /* String  */
	public static final int OPTION_CONN_PROP                      =  64;   /* int     */
	public static final int OPTION_CONN_TYPE                      =  83;   /* int     */
	public static final int OPTION_AUTO_CONNECT	  = 138;   /* boolean */
    // #sijapp cond.if target isnot  "MOTOROLA"#
	public static final int OPTION_SHADOW_CON                     = 139;   /* boolean */
    // #sijapp cond.end#
	public static final int OPTION_UI_LANGUAGE                    =   3;   /* String  */
	public static final int OPTION_DISPLAY_DATE                   = 129;   /* boolean */
	public static final int OPTION_CL_SORT_BY                     =  65;   /* int     */
	public static final int OPTION_CL_HIDE_OFFLINE                = 130;   /* boolean */
    // #sijapp cond.if target isnot  "DEFAULT"#	
	public static final int OPTION_MESSAGE_NOTIFICATION_MODE      =  66;   /* int     */
	public static final int OPTION_MESSAGE_NOTIFICATION_SOUNDFILE =   4;   /* String  */
	public static final int OPTION_MESSAGE_NOTIFICATION_VOLUME    =  67;   /* int     */
	public static final int OPTION_ONLINE_NOTIFICATION_MODE       =  68;   /* int     */
	public static final int OPTION_ONLINE_NOTIFICATION_SOUNDFILE  =   5;   /* String  */
	public static final int OPTION_ONLINE_NOTIFICATION_VOLUME     =  69;   /* int     */
	public static final int OPTION_VIBRATOR                       =  75;   /* integer */
    // #sijapp cond.end #	
	public static final int OPTION_CP1251_HACK                    = 133;   /* boolean */
    // #sijapp cond.if modules_TRAFFIC is "true" #
	public static final int OPTION_COST_PER_PACKET                =  70;   /* int     */
	public static final int OPTION_COST_PER_DAY                   =  71;   /* int     */
	public static final int OPTION_COST_PACKET_LENGTH             =  72;   /* int     */
	public static final int OPTION_CURRENCY                       =   6;   /* String  */
    // #sijapp cond.end #
	public static final int OPTION_ONLINE_STATUS                  = 192;   /* long    */
	public static final int OPTION_CHAT_SMALL_FONT                = 135;   /* boolean */
	public static final int OPTION_USER_GROUPS                    = 136;   /* boolean */
	public static final int OPTION_HISTORY                        = 137;   /* boolean */
	public static final int OPTION_SHOW_LAST_MESS                 = 142;   /* boolean */
	public static final int OPTION_CLASSIC_CHAT                   = 143;   /* boolean */
	public static final int OPTION_COLOR_SCHEME                   =  73;   /* int     */
	public static final int OPTION_STATUS_MESSAGE                 =   7;   /* String  */
	// #sijapp cond.if target is "MOTOROLA"#
	public static final int OPTION_LIGHT_TIMEOUT		          =  74;   /* int     */
	public static final int OPTION_LIGHT_MANUAL		              = 140;   /* boolean */
	// #sijapp cond.end#
	
	public static final int OPTION_USE_SMILES		              = 141;   /* boolean */
    // #sijapp cond.if modules_PROXY is "true" #
	public static final int OPTION_PRX_TYPE                       =  76;   /* int     */
	public static final int OPTION_PRX_SERV                       =   8;   /* String  */
	public static final int OPTION_PRX_PORT                       =   9;   /* String  */
	public static final int OPTION_AUTORETRY_COUNT                =  10;   /* String  */
	public static final int OPTION_PRX_NAME                       =  11;   /* String  */
	public static final int OPTION_PRX_PASS                       =  12;   /* String  */
	// #sijapp cond.end#
	
	public static final int OPTION_POPUP_WIN2                     =  84;   /*int*/
	public static final int OPTION_EXT_CLKEY0                     =  77;   /*int*/
	public static final int OPTION_EXT_CLKEYSTAR                  =  78;   /*int*/
	public static final int OPTION_EXT_CLKEY4                     =  79;   /*int*/
	public static final int OPTION_EXT_CLKEY6                     =  80;   /*int*/
	public static final int OPTION_EXT_CLKEYCALL                  =  81;   /*int*/
	public static final int OPTION_EXT_CLKEYPOUND                 =  82;   /*int*/
	public static final int OPTION_VISIBILITY_ID                  =  85;   /* int     */
	
	//Hotkey Actions
	public static final int HOTKEY_NONE     = 0;
	public static final int HOTKEY_INVIS    = 1;
	public static final int HOTKEY_INFO     = 2;
	public static final int HOTKEY_NEWMSG   = 3;
	public static final int HOTKEY_ONOFF    = 4;
	public static final int HOTKEY_OPTIONS  = 5;
	public static final int HOTKEY_MENU     = 6;
	public static final int HOTKEY_LOCK     = 7;
	// #sijapp cond.if modules_HISTORY is "true" #
	public static final int HOTKEY_HISTORY  = 8;
	// #sijapp cond.end #
	public static final int HOTKEY_MINIMIZE = 9;
	


	/**************************************************************************/

	// Hashtable containing all option key-value pairs
	static private Hashtable options;

	// Options form
	static public OptionsForm optionsForm;
	
	// Constructor
	public Options()
	{
		// Try to load option values from record store and construct options form
		try
		{
			options = new Hashtable();
		    Options.setDefaults();
			load();
			ResourceBundle.setCurrUiLanguage(getStringOption(Options.OPTION_UI_LANGUAGE));
			
		}
		// Use default values if loading option values from record store failed
		catch (Exception e)
		{
			Options.setDefaults();
		}
		
		
		
		// Construct option form
		optionsForm = new OptionsForm();
	}

	// Set default values
	// This is done before loading because older saves may not contain all new values
	static private void setDefaults()
	{
	    setStringOption (Options.OPTION_UIN,                            "");
		setStringOption (Options.OPTION_PASSWORD,                       "");
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"# ===>
		setStringOption (Options.OPTION_SRV_HOST,                       "login.icq.com");
		// #sijapp cond.else# ===
		// #sijapp cond.if modules_PROXY is "true" #
		setStringOption (Options.OPTION_SRV_HOST,                       "64.12.161.185"); //Cannot resolve host IP on MIDP1 devices
		// #sijapp cond.else#
		setStringOption (Options.OPTION_SRV_HOST,                       "login.icq.com");
		// #sijapp cond.end#
		// #sijapp cond.end# <===
		setStringOption (Options.OPTION_SRV_PORT,                       "5190");
		setBooleanOption(Options.OPTION_KEEP_CONN_ALIVE,                true);
        setStringOption (Options.OPTION_CONN_ALIVE_INVTERV,             "120");
		setIntOption    (Options.OPTION_CONN_PROP,                      0);
		setIntOption    (Options.OPTION_CONN_TYPE,                      0);
        // #sijapp cond.if target isnot "MOTOROLA"#
		setBooleanOption(Options.OPTION_SHADOW_CON,                      false);
        // #sijapp cond.end#
		setBooleanOption(Options.OPTION_AUTO_CONNECT,					 false);
		setStringOption (Options.OPTION_UI_LANGUAGE,                    ResourceBundle.langAvailable[0]);
		setBooleanOption(Options.OPTION_DISPLAY_DATE,                   false);
		setIntOption    (Options.OPTION_CL_SORT_BY,                     0);
		setBooleanOption(Options.OPTION_CL_HIDE_OFFLINE,                false);
		// #sijapp cond.if target is "SIEMENS1"#
		setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_MODE,      0);
		setStringOption (Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE, "message.mmf");
		setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_VOLUME,    50);
		setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_MODE,       0);
		setStringOption (Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE,  "online.mmf");
		setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_VOLUME,     50);
		// #sijapp cond.elseif target is "MIDP2" | target is "SIEMENS2"#
		setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_MODE,      0);
		setStringOption (Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE, "message.wav");
		setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_VOLUME,    50);
		setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_MODE,       0);
		setStringOption (Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE,  "online.wav");
		setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_VOLUME,     50);
        // #sijapp cond.elseif target is "MOTOROLA"#
        setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_MODE,      0);
		setStringOption (Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE, "message.mp3");
		setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_VOLUME,    50);
		setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_MODE,       0);
		setStringOption (Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE,  "online.mp3");
		setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_VOLUME,     50);
		setIntOption    (Options.OPTION_LIGHT_TIMEOUT,		             5);
		setBooleanOption(Options.OPTION_LIGHT_MANUAL,		             false);	
		// #sijapp cond.end#
		
		// #sijapp cond.if lang_RU is "true"#
		setBooleanOption(Options.OPTION_CP1251_HACK,                    true);
		// #sijapp cond.else#
		setBooleanOption(Options.OPTION_CP1251_HACK,                    false);
		// #sijapp cond.end#
        // #sijapp cond.if target isnot "DEFAULT"#
		setIntOption    (Options.OPTION_VIBRATOR,                       0);
		// #sijapp cond.end#		
        // #sijapp cond.if modules_TRAFFIC is "true" #
		setIntOption    (Options.OPTION_COST_PER_PACKET,                0);
		setIntOption    (Options.OPTION_COST_PER_DAY,                   0);
		setIntOption    (Options.OPTION_COST_PACKET_LENGTH,             1024);
		setStringOption (Options.OPTION_CURRENCY,                       "$");
	    // #sijapp cond.end #
		setLongOption   (Options.OPTION_ONLINE_STATUS,                  ContactList.STATUS_ONLINE);
		setBooleanOption(Options.OPTION_CHAT_SMALL_FONT,                true);
		setBooleanOption(Options.OPTION_USER_GROUPS,                    false);
		setBooleanOption(Options.OPTION_HISTORY,                        false);
		setIntOption    (Options.OPTION_COLOR_SCHEME,                   CLRSCHHEME_BOW);
        setStringOption (Options.OPTION_STATUS_MESSAGE,                 "User is currently unavailable.\n You could leave a message.");
        setBooleanOption(Options.OPTION_USE_SMILES,                     true);
        setBooleanOption(Options.OPTION_SHOW_LAST_MESS,                 false);
        // #sijapp cond.if modules_PROXY is "true" #
		setIntOption    (Options.OPTION_PRX_TYPE,                     	0);
		setStringOption (Options.OPTION_PRX_SERV,                     	"");
		setStringOption (Options.OPTION_PRX_PORT,						"1080");
		setStringOption (Options.OPTION_AUTORETRY_COUNT,                "1");
		setStringOption (Options.OPTION_PRX_NAME,                  		"");
		setStringOption (Options.OPTION_PRX_PASS,                       "");
	    // #sijapp cond.end #
		setIntOption    (Options.OPTION_VISIBILITY_ID,                  0);
		setIntOption    (Options.OPTION_EXT_CLKEY0,                     0);
		setIntOption    (Options.OPTION_EXT_CLKEYSTAR,                  0);
		setIntOption    (Options.OPTION_EXT_CLKEY4,                     0);
		setIntOption    (Options.OPTION_EXT_CLKEY6,                     0);
		setIntOption    (Options.OPTION_EXT_CLKEYCALL,                  0);
		setIntOption    (Options.OPTION_EXT_CLKEYPOUND,                 HOTKEY_LOCK);
		setIntOption    (Options.OPTION_POPUP_WIN2,                     0);
		setBooleanOption(Options.OPTION_CLASSIC_CHAT,                   false);
	}

	// Load option values from record store
	static public void load() throws IOException, RecordStoreException
	{
		// Open record store
		RecordStore account = RecordStore.openRecordStore("options", false);
		
		// Temporary variables
		byte[] buf;
		ByteArrayInputStream bais;
		DataInputStream dis;
		
		// Get version info from record store
		buf = account.getRecord(1);
		bais = new ByteArrayInputStream(buf);
		dis = new DataInputStream(bais);
	    Options.setDefaults();
		
		// Read all option key-value pairs
		buf = account.getRecord(2);
		bais = new ByteArrayInputStream(buf);
		dis = new DataInputStream(bais);
		while (dis.available() > 0)
		{
		    int optionKey = dis.readUnsignedByte();
		    if (optionKey < 64)   /* 0-63 = String */
		    {
		        setStringOption(optionKey, dis.readUTF());
			}
			else if (optionKey < 128)   /* 64-127 = int */
			{
			    setIntOption(optionKey, dis.readInt());
			}
			else if (optionKey < 192)   /* 128-191 = boolean */
			{
			    setBooleanOption(optionKey, dis.readBoolean());
			}
			else if (optionKey < 224)   /* 192-223 = long */
			{
			    setLongOption(optionKey, dis.readLong());
			}
			else   /* 226-255 = Scrambled String */
			{
			    byte[] optionValue = new byte[dis.readUnsignedShort()];
			    dis.readFully(optionValue);
			    optionValue = Util.decipherPassword(optionValue);
			    setStringOption(optionKey, Util.byteArrayToString(optionValue,2,optionValue.length-2,true));
			}
		}
		
		// Close record store
		account.closeRecordStore();

		// Hide offline?
		if (getBooleanOption(Options.OPTION_CL_HIDE_OFFLINE))
		{
			setIntOption(Options.OPTION_CL_SORT_BY, 0);
		}

	}


	// Save option values to record store
	static public void save() throws IOException, RecordStoreException
	{

		// Open record store
		RecordStore account = RecordStore.openRecordStore("options", true);

		// Add empty records if necessary
		while (account.getNumRecords() < 3)
		{
			account.addRecord(null, 0, 0);
		}

		// Temporary variables
		byte[] buf;
		ByteArrayOutputStream baos;
		DataOutputStream dos;

		// Add version info to record store
		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);
		dos.writeUTF(Jimm.VERSION);
		buf = baos.toByteArray();
		account.setRecord(1, buf, 0, buf.length);

		// Save all option key-value pairs
		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);
		Enumeration optionKeys = options.keys();
		while (optionKeys.hasMoreElements())
		{
		    int optionKey = ((Integer) optionKeys.nextElement()).intValue();
		    dos.writeByte(optionKey);
		    if (optionKey < 64)   /* 0-63 = String */
		    {
		        dos.writeUTF(getStringOption(optionKey));
			}
			else if (optionKey < 128)   /* 64-127 = int */
			{
			    dos.writeInt(getIntOption(optionKey));
			}
			else if (optionKey < 192)   /* 128-191 = boolean */
			{
			    dos.writeBoolean(getBooleanOption(optionKey));
			}
			else if (optionKey < 224)   /* 192-223 = long */
			{
				dos.writeLong(getLongOption(optionKey));
			}
			else   /* 226-255 = Scrambled String */
			{
			    byte[] optionValue = Util.stringToByteArray(getStringOption(optionKey),true);
				optionValue = Util.decipherPassword(optionValue);
				dos.writeShort(optionValue.length);
				dos.write(optionValue);
			}
		}
		buf = baos.toByteArray();
		account.setRecord(2, buf, 0, buf.length);

		// Close record store
		account.closeRecordStore();
	}


	// Option retrieval methods (no type checking!)
	static public synchronized String getStringOption(int key)
	{
		return ((String) options.get(new Integer(key)));
	}
	static public synchronized int getIntOption(int key)
	{
	    return (((Integer) options.get(new Integer(key))).intValue());
	}
	static public synchronized boolean getBooleanOption(int key)
	{
	    return (((Boolean) options.get(new Integer(key))).booleanValue());
	}
	static public synchronized long getLongOption(int key)
	{
	    return (((Long) options.get(new Integer(key))).longValue());
	}


	// Option setting methods (no type checking!)
	static public synchronized void setStringOption(int key, String value)
	{
	    options.put(new Integer(key), value);
	}
	static public synchronized void setIntOption(int key, int value)
	{
	    options.put(new Integer(key), new Integer(value));
	}
	static public synchronized void setBooleanOption(int key, boolean value)
	{
	    options.put(new Integer(key), new Boolean(value));
	}
	static public synchronized void setLongOption(int key, long value)
	{
	    options.put(new Integer(key), new Long(value));
	}
	

	/**************************************************************************/
	
	// Constants for color scheme
	private static final int CLRSCHHEME_BOW  = 0; // black on white
	private static final int CLRSCHHEME_WOB  = 1; // white on black
	private static final int CLRSCHHEME_WOBL = 2; // white on blue
	
	// Constants for method getSchemeColor to retrieving color from color scheme 
	public static final int  CLRSCHHEME_BACK = 1; // retrieving background color
	public static final int  CLRSCHHEME_TEXT = 2; // retrieving text color
	public static final int  CLRSCHHEME_BLUE = 3; // retrieving highlight color
	public static final int  CLRSCHHEME_CURS = 4; // retrieving curr mess highlight color
	
	// Constants for connection type
	public static final int  CONN_TYPE_SOCKET = 0;
	public static final int  CONN_TYPE_HTTP   = 1; 

	final static private int[] colors = 
	{
		0xFFFFFF, 0x000000, 0x0000FF, 0x404040,
		0x000000, 0xFFFFFF, 0x00FFFF, 0x808080,
		0x000080, 0xFFFFFF, 0x00FFFF, 0xFFFFFF
	};
	
	// Retrieves color value from color scheme
	static public int getSchemeColor(int type)
	{
		return (colors[getIntOption(OPTION_COLOR_SCHEME)*4+type-1]);
	}
	
	
	/**************************************************************************/
	/**************************************************************************/


	// Form for editing option values
	static public class OptionsForm implements CommandListener
	{
		private static boolean lastGroupsUsed, lastHideOffline;
		private static int lastSortMethod, lastColorScheme;

		// Commands
		static private Command backCommand;
		static private Command saveCommand;
        //#sijapp cond.if target is "MOTOROLA"#
		static private Command selectCommand;
        //#sijapp cond.end#

		// Options menu
		static private List optionsMenu;
        
        // Menu event list
		static private int[] eventList;

		// Options form
		static private Form optionsForm;

        // Static constants for menu actios
        private static final int OPTIONS_ACCOUNT    = 0;
        private static final int OPTIONS_NETWORK    = 1;
        // #sijapp cond.if modules_PROXY is "true"#       
        private static final int OPTIONS_PROXY      = 2;
        // #sijapp cond.end#
        private static final int OPTIONS_INTERFACE  = 3;
		private static final int OPTIONS_HOTKEYS  = 4;
        private static final int OPTIONS_SIGNALING  = 5;
        // #sijapp cond.if modules_TRAFFIC is "true"#
        private static final int OPTIONS_TRAFFIC    = 6;
        // #sijapp cond.end#
        // Exit has to be biggest element cause it also marks the size
        private static final int MENU_EXIT          = 7;

		// Options
        static private TextField uinTextField;
        static private TextField passwordTextField;
        static private TextField srvHostTextField;
        static private TextField srvPortTextField;
        static private ChoiceGroup keepConnAliveChoiceGroup;
        static private TextField connAliveIntervTextField;
        static private ChoiceGroup connPropChoiceGroup;
        static private ChoiceGroup connTypeChoiceGroup;
        static private ChoiceGroup autoConnectChoiceGroup;
        static private ChoiceGroup uiLanguageChoiceGroup;
        static private ChoiceGroup displayDateChoiceGroup;
        static private ChoiceGroup clSortByChoiceGroup;
        static private ChoiceGroup chrgChat;
        static private ChoiceGroup chrgPopupWin;
        static private ChoiceGroup clHideOfflineChoiceGroup;
        static private ChoiceGroup vibratorChoiceGroup;
		// #sijapp cond.if target isnot "DEFAULT"#
        static private ChoiceGroup messageNotificationModeChoiceGroup;
        static private ChoiceGroup onlineNotificationModeChoiceGroup;
        // #sijapp cond.if target isnot "RIM"#
        static private TextField messageNotificationSoundfileTextField;
        static private Gauge messageNotificationSoundVolume;
        static private TextField onlineNotificationSoundfileTextField;
        static private Gauge onlineNotificationSoundVolume;
        // #sijapp cond.end#
        // #sijapp cond.end#
        static private ChoiceGroup cp1251HackChoiceGroup;
		// #sijapp cond.if modules_TRAFFIC is "true" #
        static private TextField costPerPacketTextField;
        static private TextField costPerDayTextField;
        static private TextField costPacketLengthTextField;
        static private TextField currencyTextField;
		// #sijapp cond.end#
        static private ChoiceGroup showUserGroups;
        static private ChoiceGroup colorScheme;
		// #sijapp cond.if target is "MOTOROLA"#
        static private TextField lightTimeout;
        static private ChoiceGroup lightManual;
		// #sijapp cond.end#       
        // #sijapp cond.if modules_PROXY is "true"#
        static private ChoiceGroup srvProxyType;
        static private TextField srvProxyHostTextField;
		static private TextField srvProxyPortTextField;
		static private TextField srvProxyLoginTextField;
		static private TextField srvProxyPassTextField;
		static private TextField connAutoRetryTextField;
		// #sijapp cond.end#
		
		static private List keysMenu;
		static private List actionMenu;
		
		final static private String[] hotkeyActionNames = 
		{
			"ext_hotkey_action_none",
			"invisible_check",
			"info",
			"send_message",
			//#sijapp cond.if modules_HISTORY is "true"#
			"history",
			// #sijapp cond.end#
			"ext_hotkey_action_onoff",
			"options",
			"menu",
			"keylock",
			// #sijapp cond.if target is "MIDP2"#
			"minimize",
			// #sijapp cond.end#
		};
		
		static private int[] hotkeyOpts, hotkeyActions;
		
		
		// Constructor
		public OptionsForm() throws NullPointerException
		{
			/*************************************************************************/
			// Initialize hotkeys
			hotkeyOpts = new int[10];
			
			hotkeyActions = new int[20];
			int optIdx = 0;
			hotkeyActions[optIdx++] = Options.HOTKEY_NONE;
			hotkeyActions[optIdx++] = Options.HOTKEY_INVIS;
			hotkeyActions[optIdx++] = Options.HOTKEY_INFO;
			hotkeyActions[optIdx++] = Options.HOTKEY_NEWMSG;
			//#sijapp cond.if modules_HISTORY is "true"#
			hotkeyActions[optIdx++] = Options.HOTKEY_HISTORY;
			// #sijapp cond.end#
			hotkeyActions[optIdx++] = Options.HOTKEY_ONOFF;
			hotkeyActions[optIdx++] = Options.HOTKEY_OPTIONS;
			hotkeyActions[optIdx++] = Options.HOTKEY_MENU;
			hotkeyActions[optIdx++] = Options.HOTKEY_LOCK;
			// #sijapp cond.if target is "MIDP2"#
			hotkeyActions[optIdx++] = Options.HOTKEY_MINIMIZE;
			// #sijapp cond.end#
			
			keysMenu = new List(ResourceBundle.getString("ext_listhotkeys"), List.IMPLICIT);
			keysMenu.setCommandListener(this);
			actionMenu = new List(ResourceBundle.getString("ext_actionhotkeys"), List.EXCLUSIVE);
			actionMenu.setCommandListener(this);
			
			/*************************************************************************/
			// Initialize commands
			backCommand = new Command(ResourceBundle.getString("back"), Command.BACK, 2);
			saveCommand = new Command(ResourceBundle.getString("save"), Command.SCREEN, 1);
            // #sijapp cond.if target is "MOTOROLA"#
            selectCommand=new Command(ResourceBundle.getString("select"), Command.OK, 1);
            // #sijapp cond.end#
            
            eventList = new int[MENU_EXIT];
            optionsMenu = new List(ResourceBundle.getString("options"), List.IMPLICIT);
                 
            eventList[optionsMenu.append(ResourceBundle.getString("options_account"), null)]    = OPTIONS_ACCOUNT;
            eventList[optionsMenu.append(ResourceBundle.getString("options_network"), null)]    = OPTIONS_NETWORK;
            // #sijapp cond.if modules_PROXY is "true"#
            eventList[optionsMenu.append(ResourceBundle.getString("proxy"), null)]      = OPTIONS_PROXY;
            // #sijapp cond.end#
            eventList[optionsMenu.append(ResourceBundle.getString("options_interface"), null)]  = OPTIONS_INTERFACE;
		    eventList[optionsMenu.append(ResourceBundle.getString("options_hotkeys"), null)]  = OPTIONS_HOTKEYS;
            eventList[optionsMenu.append(ResourceBundle.getString("options_signaling"), null)]  = OPTIONS_SIGNALING;
            // #sijapp cond.if modules_TRAFFIC is "true"#
            eventList[optionsMenu.append(ResourceBundle.getString("traffic"), null)]      = OPTIONS_TRAFFIC;
            // #sijapp cond.end#

            // #sijapp cond.if target is "MOTOROLA"#
            optionsMenu.addCommand(selectCommand);
            // #sijapp cond.end#
            optionsMenu.addCommand(backCommand);
            optionsMenu.setCommandListener(this);            
                    
			// Initialize options form
			optionsForm = new Form(ResourceBundle.getString("options"));
			optionsForm.addCommand(saveCommand);
			optionsForm.addCommand(backCommand);
			optionsForm.setCommandListener(this);
			
			initSubMenuUI(OPTIONS_ACCOUNT);
			initSubMenuUI(OPTIONS_NETWORK);
            // #sijapp cond.if modules_PROXY is "true"#            
			initSubMenuUI(OPTIONS_PROXY);
            // #sijapp cond.end#     
			initSubMenuUI(OPTIONS_INTERFACE);
			initSubMenuUI(OPTIONS_SIGNALING);
            // #sijapp cond.if modules_TRAFFIC is "true"#            
            initSubMenuUI(OPTIONS_TRAFFIC);
            // #sijapp cond.end#            			
		}

	
		// Initialize the UI elements depending on the submenu id
        static public void initSubMenuUI(int i)
        {
            switch (i)
            {
            case OPTIONS_ACCOUNT:

                // Initialize elements (account section)
                uinTextField = new TextField(ResourceBundle.getString("uin"), getStringOption(Options.OPTION_UIN), 12,TextField.NUMERIC);
                passwordTextField = new TextField(ResourceBundle.getString("password"), getStringOption(Options.OPTION_PASSWORD), 32, TextField.PASSWORD);
                break;

            case OPTIONS_NETWORK:
                // Initialize elements (network section)
                srvHostTextField = new TextField(ResourceBundle.getString("server_host"), getStringOption(Options.OPTION_SRV_HOST), 32, TextField.ANY);
                srvPortTextField = new TextField(ResourceBundle.getString("server_port"), getStringOption(Options.OPTION_SRV_PORT), 5, TextField.NUMERIC);
                
                connTypeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("conn_type"), Choice.EXCLUSIVE);
                connTypeChoiceGroup.append(ResourceBundle.getString("socket"), null);
                connTypeChoiceGroup.append(ResourceBundle.getString("http"), null);
                connTypeChoiceGroup.setSelectedIndex(Options.getIntOption(Options.OPTION_CONN_TYPE),true);
                keepConnAliveChoiceGroup = new ChoiceGroup(ResourceBundle.getString("keep_conn_alive"), Choice.MULTIPLE);
                keepConnAliveChoiceGroup.append(ResourceBundle.getString("yes"), null);
                keepConnAliveChoiceGroup.setSelectedIndex(0, getBooleanOption(Options.OPTION_KEEP_CONN_ALIVE));
                connAliveIntervTextField = new TextField(ResourceBundle.getString("timeout_interv"), getStringOption(Options.OPTION_CONN_ALIVE_INVTERV), 3, TextField.NUMERIC);
                connPropChoiceGroup = new ChoiceGroup(ResourceBundle.getString("conn_prop"), Choice.MULTIPLE);
                connPropChoiceGroup.append(ResourceBundle.getString("async"), null);
                // #sijapp cond.if target isnot "MOTOROLA"#
                connPropChoiceGroup.append(ResourceBundle.getString("shadow_con"), null);
                // #sijapp cond.end#
                if (getIntOption(Options.OPTION_CONN_PROP) == 0)
                	connPropChoiceGroup.setSelectedIndex(0, false);
                else
                	connPropChoiceGroup.setSelectedIndex(0, true);
                // #sijapp cond.if target isnot "MOTOROLA"#
                connPropChoiceGroup.setSelectedIndex(1, getBooleanOption(Options.OPTION_SHADOW_CON));
                // #sijapp cond.end#
                autoConnectChoiceGroup = new ChoiceGroup(ResourceBundle.getString("auto_connect") + "?", Choice.MULTIPLE);
                autoConnectChoiceGroup.append(ResourceBundle.getString("yes"), null);
                autoConnectChoiceGroup.setSelectedIndex(0, getBooleanOption(Options.OPTION_AUTO_CONNECT));
                break;
            // #sijapp cond.if modules_PROXY is "true"#
            case OPTIONS_PROXY:              
                srvProxyType = new ChoiceGroup(ResourceBundle.getString("proxy_type"), Choice.EXCLUSIVE);
                srvProxyType.append(ResourceBundle.getString("proxy_do_not_use"), null);
                srvProxyType.append(ResourceBundle.getString("proxy_socks4"), null);
                srvProxyType.append(ResourceBundle.getString("proxy_socks5"), null);
                srvProxyType.append(ResourceBundle.getString("proxy_guess"), null);
                srvProxyType.setSelectedIndex(getIntOption(Options.OPTION_PRX_TYPE), true);
                
                srvProxyHostTextField = new TextField(ResourceBundle.getString("proxy_server_host"), getStringOption(Options.OPTION_PRX_SERV), 32, TextField.ANY);
                srvProxyPortTextField = new TextField(ResourceBundle.getString("proxy_server_port"), getStringOption(Options.OPTION_PRX_PORT), 5, TextField.NUMERIC);
                
                srvProxyLoginTextField = new TextField(ResourceBundle.getString("proxy_server_login"), getStringOption(Options.OPTION_PRX_NAME), 32, TextField.ANY);
                srvProxyPassTextField  = new TextField(ResourceBundle.getString("proxy_server_pass"), getStringOption(Options.OPTION_PRX_PASS), 32, TextField.PASSWORD);
                
                connAutoRetryTextField = new TextField(ResourceBundle.getString("auto_retry_count"), getStringOption(Options.OPTION_AUTORETRY_COUNT), 5, TextField.NUMERIC);
                break;
            // #sijapp cond.end#      

            case OPTIONS_INTERFACE:
                // Initialize elements (interface section)
                uiLanguageChoiceGroup = new ChoiceGroup(ResourceBundle.getString("language"), Choice.EXCLUSIVE);
                for (int j = 0; j < ResourceBundle.langAvailable.length; j++)
                {
                    uiLanguageChoiceGroup.append(ResourceBundle.getString("lang_" + ResourceBundle.langAvailable[j]), null);
                    if (ResourceBundle.langAvailable[j].equals(getStringOption(Options.OPTION_UI_LANGUAGE)))
                    {
                        uiLanguageChoiceGroup.setSelectedIndex(j, true);
                    }
                }
                displayDateChoiceGroup = new ChoiceGroup(ResourceBundle.getString("display_date"), Choice.MULTIPLE);
                displayDateChoiceGroup.append(ResourceBundle.getString("yes"), null);
                displayDateChoiceGroup.setSelectedIndex(0, getBooleanOption(Options.OPTION_DISPLAY_DATE));
                clSortByChoiceGroup = new ChoiceGroup(ResourceBundle.getString("sort_by"), Choice.EXCLUSIVE);
                clSortByChoiceGroup.append(ResourceBundle.getString("sort_by_status"), null);
                clSortByChoiceGroup.append(ResourceBundle.getString("sort_by_name"), null);
                clSortByChoiceGroup.setSelectedIndex(getIntOption(Options.OPTION_CL_SORT_BY), true);
                clHideOfflineChoiceGroup = new ChoiceGroup(ResourceBundle.getString("hide_offline"), Choice.MULTIPLE);
                clHideOfflineChoiceGroup.append(ResourceBundle.getString("yes"), null);
                clHideOfflineChoiceGroup.setSelectedIndex(0, getBooleanOption(Options.OPTION_CL_HIDE_OFFLINE));
                cp1251HackChoiceGroup = new ChoiceGroup(ResourceBundle.getString("cp1251"), Choice.MULTIPLE);
                cp1251HackChoiceGroup.append(ResourceBundle.getString("yes"), null);
                cp1251HackChoiceGroup.setSelectedIndex(0, getBooleanOption(Options.OPTION_CP1251_HACK));

                showUserGroups = new ChoiceGroup(ResourceBundle.getString("show_user_groups"), Choice.MULTIPLE);
                showUserGroups.append(ResourceBundle.getString("yes"), null);
                showUserGroups.setSelectedIndex(0, getBooleanOption(Options.OPTION_USER_GROUPS));

                colorScheme = new ChoiceGroup(ResourceBundle.getString("color_scheme"), Choice.EXCLUSIVE);
                colorScheme.append(ResourceBundle.getString("black_on_white"), null);
                colorScheme.append(ResourceBundle.getString("white_on_black"), null);
                colorScheme.append(ResourceBundle.getString("white_on_blue"), null);
                colorScheme.setSelectedIndex(getIntOption(Options.OPTION_COLOR_SCHEME), true);
                
                int idx1 = 0;
                chrgChat = new ChoiceGroup(ResourceBundle.getString("chat"), Choice.MULTIPLE);
                chrgChat.append(ResourceBundle.getString("chat_small_font"), null);
                chrgChat.setSelectedIndex(idx1++, getBooleanOption(Options.OPTION_CHAT_SMALL_FONT));
                
                // #sijapp cond.if modules_SMILES is "true"#
                chrgChat.append(ResourceBundle.getString("use_smiles"), null);
                chrgChat.setSelectedIndex(idx1++, getBooleanOption(Options.OPTION_USE_SMILES));
                // #sijapp cond.end#
                
				//#sijapp cond.if modules_HISTORY is "true"#
                chrgChat.append(ResourceBundle.getString("use_history"), null);
                chrgChat.setSelectedIndex(idx1++, getBooleanOption(Options.OPTION_HISTORY));
                chrgChat.append(ResourceBundle.getString("show_prev_mess"), null);
                chrgChat.setSelectedIndex(idx1++, getBooleanOption(OPTION_SHOW_LAST_MESS));
                //#sijapp cond.end#
                
                // #sijapp cond.if target is "SIEMENS2"#
                chrgChat.append(ResourceBundle.getString("cl_chat"), null);
                chrgChat.setSelectedIndex(idx1++, getBooleanOption(OPTION_CLASSIC_CHAT));
                // #sijapp cond.end#
               
				// #sijapp cond.if target is "MOTOROLA"#
				lightTimeout = new TextField(ResourceBundle.getString("backlight_timeout"), String.valueOf(getIntOption(Options.OPTION_LIGHT_TIMEOUT)), 2, TextField.NUMERIC);
				lightManual = new ChoiceGroup(ResourceBundle.getString("backlight_manual"), Choice.MULTIPLE);
				lightManual.append(ResourceBundle.getString("yes"), null);
				lightManual.setSelectedIndex(0, getBooleanOption(Options.OPTION_LIGHT_MANUAL));
				// #sijapp cond.end#

                break;
                
                            
            case OPTIONS_SIGNALING:
            	// Initialize elements (Signaling section)

            	// #sijapp cond.if target isnot "DEFAULT"#
                onlineNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("onl_notification"), Choice.EXCLUSIVE);
                onlineNotificationModeChoiceGroup.append(ResourceBundle.getString("no"), null);
                onlineNotificationModeChoiceGroup.append(ResourceBundle.getString("beep"), null);
                // #sijapp cond.if target isnot "RIM"#        
                onlineNotificationModeChoiceGroup.append(ResourceBundle.getString("sound"), null);               
                // #sijapp cond.end#                  
                onlineNotificationModeChoiceGroup.setSelectedIndex(getIntOption(Options.OPTION_ONLINE_NOTIFICATION_MODE),true);
                // #sijapp cond.if target isnot "RIM"#                 
                onlineNotificationSoundfileTextField = new TextField(ResourceBundle.getString("onl_sound_file_name"), getStringOption(Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE), 32, TextField.ANY);
                // #sijapp cond.end#                 
                messageNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("message_notification"),Choice.EXCLUSIVE);
                messageNotificationModeChoiceGroup.append(ResourceBundle.getString("no"), null);
                messageNotificationModeChoiceGroup.append(ResourceBundle.getString("beep"), null);
                // #sijapp cond.if target isnot "RIM"#                 
                messageNotificationModeChoiceGroup.append(ResourceBundle.getString("sound"), null);
                // #sijapp cond.end#                  
                messageNotificationModeChoiceGroup.setSelectedIndex(getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_MODE), true);
                // #sijapp cond.if target isnot "RIM"#                  
                messageNotificationSoundfileTextField = new TextField(ResourceBundle.getString("msg_sound_file_name"), getStringOption(Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE), 32, TextField.ANY);
                messageNotificationSoundVolume = new Gauge(ResourceBundle.getString("volume"), true, 10, getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_VOLUME) / 10);
                onlineNotificationSoundVolume = new Gauge(ResourceBundle.getString("volume"), true, 10, getIntOption(Options.OPTION_ONLINE_NOTIFICATION_VOLUME) / 10);
                // #sijapp cond.end#
                
                vibratorChoiceGroup = new ChoiceGroup(ResourceBundle.getString("vibration"), Choice.EXCLUSIVE);
                vibratorChoiceGroup.append(ResourceBundle.getString("no"), null);
                vibratorChoiceGroup.append(ResourceBundle.getString("yes"), null);
                vibratorChoiceGroup.append(ResourceBundle.getString("when_locked"), null);
                vibratorChoiceGroup.setSelectedIndex(getIntOption(Options.OPTION_VIBRATOR), true);
                // #sijapp cond.end#

                chrgPopupWin = new ChoiceGroup(ResourceBundle.getString("popup_win"), Choice.EXCLUSIVE);
                chrgPopupWin.append(ResourceBundle.getString("no"),       null);
                chrgPopupWin.append(ResourceBundle.getString("pw_forme"), null);
                chrgPopupWin.append(ResourceBundle.getString("pw_all"),   null);
                chrgPopupWin.setSelectedIndex(getIntOption(Options.OPTION_POPUP_WIN2), true);
                break;
            

            // #sijapp cond.if modules_TRAFFIC is "true"#
            case OPTIONS_TRAFFIC:
                // Initialize elements (cost section)
                costPerPacketTextField = new TextField(ResourceBundle.getString("cpp"), Util.intToDecimal(getIntOption(Options.OPTION_COST_PER_PACKET)), 6, TextField.ANY);
                costPerDayTextField = new TextField(ResourceBundle.getString("cpd"), Util.intToDecimal(getIntOption(Options.OPTION_COST_PER_DAY)), 6, TextField.ANY);
                costPacketLengthTextField = new TextField(ResourceBundle.getString("plength"), String.valueOf(getIntOption(Options.OPTION_COST_PACKET_LENGTH) / 1024), 4, TextField.NUMERIC);
                currencyTextField = new TextField(ResourceBundle.getString("currency"), getStringOption(Options.OPTION_CURRENCY), 4, TextField.ANY);
                break;
            // #sijapp cond.end#
            }
        }
        
        private String getHotKeyActName(String langStr, int option)
        {
        	int optionValue = Options.getIntOption(option);
        	for (int i = 0; i < hotkeyActionNames.length; i++)
        	{
        		if (hotkeyActions[i] == optionValue) 
        			return ResourceBundle.getString(langStr)+": "+ResourceBundle.getString(hotkeyActionNames[i]);  
        	}
        	return ResourceBundle.getString("ext_clhotkey0")+": <???>";
        }
        
		private void InitHotkeyMenuUI()
		{
			int optIdx = 0;
			
			int lastItemIndex = keysMenu.getSelectedIndex();
			while (keysMenu.size() != 0) keysMenu.delete(0);
			
			keysMenu.append(getHotKeyActName("ext_clhotkey0", Options.OPTION_EXT_CLKEY0), null);
			hotkeyOpts[optIdx++] = Options.OPTION_EXT_CLKEY0;
			
			keysMenu.append(getHotKeyActName("ext_clhotkey4", Options.OPTION_EXT_CLKEY4), null);
			hotkeyOpts[optIdx++] = Options.OPTION_EXT_CLKEY4;
			
			keysMenu.append(getHotKeyActName("ext_clhotkey6", Options.OPTION_EXT_CLKEY6), null);
			hotkeyOpts[optIdx++] = Options.OPTION_EXT_CLKEY6;
			
			// #sijapp cond.if target isnot "MOTOROLA"#
			keysMenu.append(getHotKeyActName("ext_clhotkeystar", Options.OPTION_EXT_CLKEYSTAR), null);
			hotkeyOpts[optIdx++] = Options.OPTION_EXT_CLKEYSTAR;
			// #sijapp cond.end#
			
			keysMenu.append(getHotKeyActName("ext_clhotkeypound", Options.OPTION_EXT_CLKEYPOUND), null);
			hotkeyOpts[optIdx++] = Options.OPTION_EXT_CLKEYPOUND;
			
			// #sijapp cond.if target is "SIEMENS2"#
			keysMenu.append(getHotKeyActName("ext_clhotkeycall", Options.OPTION_EXT_CLKEYCALL), null);
			hotkeyOpts[optIdx++] = Options.OPTION_EXT_CLKEYCALL;
			// #sijapp cond.end#
			
			keysMenu.setSelectedIndex(lastItemIndex == -1 ? 0 : lastItemIndex, true);
			
			keysMenu.addCommand(saveCommand);
			Jimm.display.setCurrent(keysMenu);			
		} 
		
		// Activate options menu
        static public void activate()
		{
			optionsMenu.setSelectedIndex(0, true);   // Reset
			optionsMenu.addCommand(backCommand);
			Jimm.display.setCurrent(optionsMenu);
		}
        
		// Command listener
		public void commandAction(Command c, Displayable d)
		{
			//Command handler for hotkeys list in Options...
			if (d == keysMenu)
			{
				if (c == List.SELECT_COMMAND)
				{
					while (actionMenu.size() != 0) actionMenu.delete(0);
					for (int i=0; i < hotkeyActionNames.length; i++)
						actionMenu.append(ResourceBundle.getString(hotkeyActionNames[i]),null);
					actionMenu.addCommand(saveCommand);
					actionMenu.addCommand(backCommand);

					int optValue = Options.getIntOption(hotkeyOpts[keysMenu.getSelectedIndex()]);
					for (int selIndex = 0; selIndex < hotkeyActions.length; selIndex++)
					{
						if (hotkeyActions[selIndex] == optValue)
						{
							actionMenu.setSelectedIndex(selIndex, true);
							break;
						}
					}
					
					Jimm.display.setCurrent(actionMenu);
					return;
				}
			}
			
			//Command handler for actions list in Hotkeys...
			if (d == actionMenu)
			{
				if (c == saveCommand)
				{ 
					Options.setIntOption
					(
						hotkeyOpts[keysMenu.getSelectedIndex()],
						hotkeyActions[actionMenu.getSelectedIndex()]
					);
				}
				InitHotkeyMenuUI();
				return;
			}
			
			// Look for select command
			// #sijapp cond.if target is "MOTOROLA"#
			if ((c == List.SELECT_COMMAND) || (c == selectCommand))
            // #sijapp cond.else#
			if (c == List.SELECT_COMMAND)
            // #sijapp cond.end#
			{
				lastHideOffline = getBooleanOption(Options.OPTION_CL_HIDE_OFFLINE);
				lastGroupsUsed = getBooleanOption(Options.OPTION_USER_GROUPS);
                lastSortMethod = getIntOption(Options.OPTION_CL_SORT_BY);
				lastColorScheme = getIntOption(Options.OPTION_COLOR_SCHEME);

				// Delete all items
				//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
				optionsForm.deleteAll();
				//#sijapp cond.else#
				while (optionsForm.size() > 0) { optionsForm.delete(0); }
				//#sijapp cond.end#
				
				// Add elements, depending on selected option menu item
				switch (eventList[optionsMenu.getSelectedIndex()])
				{
					case OPTIONS_ACCOUNT:
					    optionsForm.append(uinTextField);
						optionsForm.append(passwordTextField);
						break;
					case OPTIONS_NETWORK:
						optionsForm.append(srvHostTextField);
						optionsForm.append(srvPortTextField);
						optionsForm.append(connTypeChoiceGroup);
						optionsForm.append(keepConnAliveChoiceGroup);
                        optionsForm.append(connAliveIntervTextField);
						optionsForm.append(autoConnectChoiceGroup);
						optionsForm.append(connPropChoiceGroup);
						break;
                    // #sijapp cond.if modules_PROXY is "true"#
                    case OPTIONS_PROXY:
                        optionsForm.append(srvProxyType);
                        optionsForm.append(srvProxyHostTextField);
                        optionsForm.append(srvProxyPortTextField);
                        optionsForm.append(srvProxyLoginTextField);
                        optionsForm.append(srvProxyPassTextField);
                        optionsForm.append(connAutoRetryTextField);
                        break;
                    // #sijapp cond.end# 
					case OPTIONS_INTERFACE:
						optionsForm.append(uiLanguageChoiceGroup);
						optionsForm.append(displayDateChoiceGroup);
						optionsForm.append(showUserGroups);
						optionsForm.append(clSortByChoiceGroup);
						optionsForm.append(clHideOfflineChoiceGroup);
						
						optionsForm.append(chrgChat);
						
						optionsForm.append(cp1251HackChoiceGroup);
						optionsForm.append(colorScheme);
						// #sijapp cond.if target is "MOTOROLA"#
						optionsForm.append(lightTimeout);
						optionsForm.append(lightManual);
						// #sijapp cond.end #
						
						break;
						
					case OPTIONS_HOTKEYS:
						InitHotkeyMenuUI();
						return;
                                           
					case OPTIONS_SIGNALING:
						// #sijapp cond.if target isnot "DEFAULT"#     
						optionsForm.append(messageNotificationModeChoiceGroup);
						
                        // #sijapp cond.if target isnot "RIM"#                        
						optionsForm.append(messageNotificationSoundVolume);
						optionsForm.append(messageNotificationSoundfileTextField);
                        // #sijapp cond.end#
                                                
						optionsForm.append(vibratorChoiceGroup);
						optionsForm.append(onlineNotificationModeChoiceGroup);
						
                        // #sijapp cond.if target isnot "RIM"#                          
						optionsForm.append(onlineNotificationSoundVolume);
						optionsForm.append(onlineNotificationSoundfileTextField);
                        // #sijapp cond.end#
						
                        // #sijapp cond.end#
						optionsForm.append(chrgPopupWin);
						break;
			        

					// #sijapp cond.if modules_TRAFFIC is "true"#
					case OPTIONS_TRAFFIC:
						optionsForm.append(costPerPacketTextField);
						optionsForm.append(costPerDayTextField);
						optionsForm.append(costPacketLengthTextField);
						optionsForm.append(currencyTextField);
						break;
					// #sijapp cond.end#
				}

				// Activate options form
				Jimm.display.setCurrent(optionsForm);
			}

			// Look for back command
			else if (c == backCommand)
			{
				if (d == optionsForm)
				{
                   initSubMenuUI(eventList[optionsMenu.getSelectedIndex()]);
                   optionsMenu.addCommand(backCommand);
                   Jimm.display.setCurrent(optionsMenu);
               }
               else
               {
                   // Active MM/CL
                   if (Icq.isConnected())
                   {
                       ContactList.activate();
                   } else
                   {
                	   MainMenu.activate();
                   }
               }

           }

			// Look for save command
			else if (c == saveCommand)
			{

				// Save values, depending on selected option menu item
				switch (eventList[optionsMenu.getSelectedIndex()])
				{
					case OPTIONS_ACCOUNT:
						setStringOption(Options.OPTION_UIN,uinTextField.getString());
						setStringOption(Options.OPTION_PASSWORD,passwordTextField.getString());
						break;
					case OPTIONS_NETWORK:
					    setStringOption(Options.OPTION_SRV_HOST,srvHostTextField.getString());
					    setStringOption(Options.OPTION_SRV_PORT,srvPortTextField.getString());
					    setIntOption(Options.OPTION_CONN_TYPE,connTypeChoiceGroup.getSelectedIndex());
						setBooleanOption(Options.OPTION_KEEP_CONN_ALIVE,keepConnAliveChoiceGroup.isSelected(0));
                        setStringOption(Options.OPTION_CONN_ALIVE_INVTERV,connAliveIntervTextField.getString());
						setBooleanOption(Options.OPTION_AUTO_CONNECT,autoConnectChoiceGroup.isSelected(0));
						if (connPropChoiceGroup.isSelected(0))
							setIntOption(Options.OPTION_CONN_PROP,1);
						else
						    setIntOption(Options.OPTION_CONN_PROP,0);
                        // #sijapp cond.if target isnot "MOTOROLA"#
						setBooleanOption(Options.OPTION_SHADOW_CON,connPropChoiceGroup.isSelected(1));
                        // #sijapp cond.end#
						break;
                    // #sijapp cond.if modules_PROXY is "true"#
                    case OPTIONS_PROXY:
                        setIntOption(Options.OPTION_PRX_TYPE,srvProxyType.getSelectedIndex());
                        setStringOption(Options.OPTION_PRX_SERV,srvProxyHostTextField.getString());
                        setStringOption(Options.OPTION_PRX_PORT,srvProxyPortTextField.getString());
                        
                        setStringOption(Options.OPTION_PRX_NAME,srvProxyLoginTextField.getString());
                        setStringOption(Options.OPTION_PRX_PASS,srvProxyPassTextField.getString());
                        
                        setStringOption(Options.OPTION_AUTORETRY_COUNT,connAutoRetryTextField.getString());
                        break;
                    // #sijapp cond.end#      
					case OPTIONS_INTERFACE:
						setStringOption(Options.OPTION_UI_LANGUAGE,ResourceBundle.langAvailable[uiLanguageChoiceGroup.getSelectedIndex()]);
						setBooleanOption(Options.OPTION_DISPLAY_DATE,displayDateChoiceGroup.isSelected(0));
						
						int newSortMethod = 0;
						
						if (clHideOfflineChoiceGroup.isSelected(0))
						{
							newSortMethod = 0;
						}
						else
	  					{
							newSortMethod = clSortByChoiceGroup.getSelectedIndex();
						}
						
						setIntOption(Options.OPTION_CL_SORT_BY, newSortMethod);
						
						setBooleanOption(Options.OPTION_CL_HIDE_OFFLINE,clHideOfflineChoiceGroup.isSelected(0));
						setBooleanOption(Options.OPTION_CP1251_HACK,cp1251HackChoiceGroup.isSelected(0));
						
						int idx = 0;
						setBooleanOption(Options.OPTION_CHAT_SMALL_FONT, chrgChat.isSelected(idx++));
						
						// #sijapp cond.if modules_SMILES is "true"#
						setBooleanOption(Options.OPTION_USE_SMILES,      chrgChat.isSelected(idx++));
						// #sijapp cond.end#
						
						// #sijapp cond.if modules_HISTORY is "true"#
						setBooleanOption(Options.OPTION_HISTORY,         chrgChat.isSelected(idx++));
						setBooleanOption(Options.OPTION_SHOW_LAST_MESS,  chrgChat.isSelected(idx++));
						// #sijapp cond.end#
						
						// #sijapp cond.if target is "SIEMENS2"#
						setBooleanOption(Options.OPTION_CLASSIC_CHAT, chrgChat.isSelected(idx++));
						// #sijapp cond.end#
						
						boolean newUseGroups = showUserGroups.isSelected(0);
						setBooleanOption(Options.OPTION_USER_GROUPS, newUseGroups);
						
						int newColorScheme = colorScheme.getSelectedIndex(); 
						setIntOption(Options.OPTION_COLOR_SCHEME, newColorScheme);
						
						boolean newHideOffline = getBooleanOption(Options.OPTION_CL_HIDE_OFFLINE);
						ContactList.optionsChanged
						(
							(newUseGroups != lastGroupsUsed) || (newHideOffline != lastHideOffline),
							(newSortMethod != lastSortMethod)
						);
						
						if (lastColorScheme != newColorScheme) JimmUI.setColorScheme();
						// #sijapp cond.if target is "MOTOROLA"#
						setIntOption(Options.OPTION_LIGHT_TIMEOUT, Integer.parseInt(lightTimeout.getString()));
						setBooleanOption(Options.OPTION_LIGHT_MANUAL, lightManual.isSelected(0));
						// #sijapp cond.end#
						break;
                    				
					case OPTIONS_SIGNALING:
						// #sijapp cond.if target isnot "DEFAULT"# ===>
						setIntOption(Options.OPTION_MESSAGE_NOTIFICATION_MODE,messageNotificationModeChoiceGroup.getSelectedIndex());
						setIntOption(Options.OPTION_VIBRATOR, vibratorChoiceGroup.getSelectedIndex());
						setIntOption(Options.OPTION_ONLINE_NOTIFICATION_MODE,onlineNotificationModeChoiceGroup.getSelectedIndex());
						
						// #sijapp cond.if target isnot "RIM"#       
						setStringOption(Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE,messageNotificationSoundfileTextField.getString());
						setIntOption(Options.OPTION_MESSAGE_NOTIFICATION_VOLUME,messageNotificationSoundVolume.getValue()*10);
						setStringOption(Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE,onlineNotificationSoundfileTextField.getString());
						setIntOption(Options.OPTION_ONLINE_NOTIFICATION_VOLUME,onlineNotificationSoundVolume.getValue()*10);
                        // #sijapp cond.end#
						// #sijapp cond.end# <===
						setIntOption(Options.OPTION_POPUP_WIN2, chrgPopupWin.getSelectedIndex()); 
						break;
				    
					// #sijapp cond.if modules_TRAFFIC is "true"#
					case OPTIONS_TRAFFIC:
						setIntOption(Options.OPTION_COST_PER_PACKET,Util.decimalToInt(costPerPacketTextField.getString()));
						costPerPacketTextField.setString(Util.intToDecimal(getIntOption(Options.OPTION_COST_PER_PACKET)));
						setIntOption(Options.OPTION_COST_PER_DAY,Util.decimalToInt(costPerDayTextField.getString()));
						costPerDayTextField.setString(Util.intToDecimal(getIntOption(Options.OPTION_COST_PER_DAY)));
						setIntOption(Options.OPTION_COST_PACKET_LENGTH,Integer.parseInt(costPacketLengthTextField.getString()) * 1024);
						setStringOption(Options.OPTION_CURRENCY,currencyTextField.getString());
						break;
					// #sijapp cond.end#
						
				}

				// Save options
				try
				{
					save();
				}
				catch (Exception e)
				{
					JimmException.handleException(new JimmException(172,0,true));
				}

				// Activate MM/CL
				if (Icq.isConnected())
				{
					ContactList.activate();
				}
				else
				{
					activate();
				}
			}
		}
		
	} // end of 'class OptionsForm'
}


