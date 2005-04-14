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
Author(s): Manuel Linsmayer, Andreas Rossbacher, Artyomov Denis
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
import jimm.util.ResourceBundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
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
	public static final int OPTION_CONN_TYPE                      =  64;   /* int     */
	public static final int OPTION_AUTO_CONNECT					  = 138;   /* boolean */
	public static final int OPTION_SHADOW_CON                     = 139;   /* boolean */
	public static final int OPTION_UI_LANGUAGE                    =   3;   /* String  */
	public static final int OPTION_DISPLAY_DATE                   = 129;   /* boolean */
	public static final int OPTION_CL_SORT_BY                     =  65;   /* int     */
	public static final int OPTION_CL_HIDE_OFFLINE                = 130;   /* boolean */
	public static final int OPTION_MESSAGE_NOTIFICATION_MODE      =  66;   /* int     */
	public static final int OPTION_MESSAGE_NOTIFICATION_SOUNDFILE =   4;   /* String  */
	public static final int OPTION_MESSAGE_NOTIFICATION_VOLUME    =  67;   /* int     */
	public static final int OPTION_ONLINE_NOTIFICATION_MODE       =  68;   /* int     */
	public static final int OPTION_ONLINE_NOTIFICATION_SOUNDFILE  =   5;   /* String  */
	public static final int OPTION_ONLINE_NOTIFICATION_VOLUME     =  69;   /* int     */
	public static final int OPTION_VIBRATOR                       = 131;   /* boolean */
	public static final int OPTION_CP1251_HACK                    = 133;   /* boolean */
	public static final int OPTION_COST_PER_PACKET                =  70;   /* int     */
	public static final int OPTION_COST_PER_DAY                   =  71;   /* int     */
	public static final int OPTION_COST_PACKET_LENGTH             =  72;   /* int     */
	public static final int OPTION_CURRENCY                       =   6;   /* String  */
	public static final int OPTION_ONLINE_STATUS                  = 192;   /* long    */
	public static final int OPTION_CHAT_SMALL_FONT                = 135;   /* boolean */
	public static final int OPTION_USER_GROUPS                    = 136;   /* boolean */
	public static final int OPTION_HISTORY                        = 137;   /* boolean */
	public static final int OPTION_COLOR_SCHEME                   =  73;   /* int     */


	/**************************************************************************/

	// Hashtable containing all option key-value pairs
	private Hashtable options;

	// Options form
	public OptionsForm optionsForm;

	// Constructor
	public Options()
	{

		// Try to load option values from record store and construct options form
		try
		{
		    this.options = new Hashtable();
			this.load();
			ResourceBundle.setCurrUiLanguage(this.getStringOption(Options.OPTION_UI_LANGUAGE));
			
			// Construct option form
			this.optionsForm = new OptionsForm();
		}
		// Use default values if loading option values from record store failed
		catch (Exception e)
		{
		    this.setStringOption (Options.OPTION_UIN,                            "");
			this.setStringOption (Options.OPTION_PASSWORD,                       "");
			this.setStringOption (Options.OPTION_SRV_HOST,                       "login.icq.com");
			this.setStringOption (Options.OPTION_SRV_PORT,                       "5190");
			this.setBooleanOption(Options.OPTION_KEEP_CONN_ALIVE,                true);
			this.setIntOption    (Options.OPTION_CONN_TYPE,                      0);
			this.setBooleanOption(Options.OPTION_SHADOW_CON,                      false);
			this.setBooleanOption(Options.OPTION_AUTO_CONNECT,					 false);
			this.setStringOption (Options.OPTION_UI_LANGUAGE,                    ResourceBundle.LANG_AVAILABLE[0]);
			this.setBooleanOption(Options.OPTION_DISPLAY_DATE,                   false);
			this.setIntOption    (Options.OPTION_CL_SORT_BY,                     0);
			this.setBooleanOption(Options.OPTION_CL_HIDE_OFFLINE,                false);
			// #sijapp cond.if target is "SIEMENS"#
			this.setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_MODE,      0);
			this.setStringOption (Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE, "message.mmf");
			this.setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_VOLUME,    50);
			this.setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_MODE,       0);
			this.setStringOption (Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE,  "online.mmf");
			this.setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_VOLUME,     50);
			// #sijapp cond.elseif target is "MIDP2"#
			this.setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_MODE,      0);
			this.setStringOption (Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE, "message.wav");
			this.setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_VOLUME,    50);
			this.setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_MODE,       0);
			this.setStringOption (Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE,  "online.wav");
			this.setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_VOLUME,     50);
            // #sijapp cond.elseif target is "MOTOROLA"#
            this.setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_MODE,      0);
			this.setStringOption (Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE, "message.mp3");
			this.setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_VOLUME,    50);
			this.setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_MODE,       0);
			this.setStringOption (Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE,  "online.mp3");
			this.setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_VOLUME,     50);
			// #sijapp cond.else#
			this.setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_MODE,      0);
			this.setStringOption (Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE, "");
			this.setIntOption    (Options.OPTION_MESSAGE_NOTIFICATION_VOLUME,    0);
			this.setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_MODE,       0);
			this.setStringOption (Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE,  "");
			this.setIntOption    (Options.OPTION_ONLINE_NOTIFICATION_VOLUME,     0);
			// #sijapp cond.end#
			
			// #sijapp cond.if lang_RU is "true" and lang_EN isnot "true"#
			this.setBooleanOption(Options.OPTION_CP1251_HACK,                    true);
			// #sijapp cond.else#
			this.setBooleanOption(Options.OPTION_CP1251_HACK,                    false);
			// #sijapp cond.end#
			
			this.setBooleanOption(Options.OPTION_VIBRATOR,                       false);
			this.setIntOption    (Options.OPTION_COST_PER_PACKET,                0);
			this.setIntOption    (Options.OPTION_COST_PER_DAY,                   0);
			this.setIntOption    (Options.OPTION_COST_PACKET_LENGTH,             1024);
			this.setStringOption (Options.OPTION_CURRENCY,                       "$");
			this.setLongOption   (Options.OPTION_ONLINE_STATUS,                  ContactList.STATUS_ONLINE);
			this.setBooleanOption(Options.OPTION_CHAT_SMALL_FONT,                true);
			this.setBooleanOption(Options.OPTION_USER_GROUPS,                    false);
			this.setBooleanOption(Options.OPTION_HISTORY,                        false);
			this.setIntOption    (Options.OPTION_COLOR_SCHEME,                   CLRSCHHEME_BOW); 
			
			// Construct option form
			this.optionsForm = new OptionsForm();
		}

	}


	// Load option values from record store
	public void load() throws IOException, RecordStoreException
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
		if (!(dis.readUTF().equals(Jimm.VERSION))) throw (new IOException());
		
		// Read all option key-value pairs
		buf = account.getRecord(2);
		bais = new ByteArrayInputStream(buf);
		dis = new DataInputStream(bais);
		while (dis.available() > 0)
		{
		    int optionKey = dis.readUnsignedByte();
		    if (optionKey < 64)   /* 0-63 = String */
		    {
		        this.setStringOption(optionKey, dis.readUTF());
			}
			else if (optionKey < 128)   /* 64-127 = int */
			{
			    this.setIntOption(optionKey, dis.readInt());
			}
			else if (optionKey < 192)   /* 128-191 = boolean */
			{
			    this.setBooleanOption(optionKey, dis.readBoolean());
			}
			else if (optionKey < 224)   /* 192-223 = long */
			{
			    this.setLongOption(optionKey, dis.readLong());
			}
			else   /* 226-255 = Scrambled String */
			{
			    byte[] optionValue = new byte[dis.readUnsignedShort()];
			    dis.readFully(optionValue);
			    optionValue = Util.decipherPassword(optionValue);
			    this.setStringOption(optionKey, new String(optionValue));
			}
		}
		
		// Close record store
		account.closeRecordStore();

		// Hide offline?
		if (this.getBooleanOption(Options.OPTION_CL_HIDE_OFFLINE))
		{
			Options.this.setIntOption(Options.OPTION_CL_SORT_BY, 0);
		}

	}


	// Save option values to record store
	public void save() throws IOException, RecordStoreException
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
		Enumeration optionKeys = this.options.keys();
		while (optionKeys.hasMoreElements())
		{
		    int optionKey = ((Integer) optionKeys.nextElement()).intValue();
		    dos.writeByte(optionKey);
		    if (optionKey < 64)   /* 0-63 = String */
		    {
		        dos.writeUTF(this.getStringOption(optionKey));
			}
			else if (optionKey < 128)   /* 64-127 = int */
			{
			    dos.writeInt(this.getIntOption(optionKey));
			}
			else if (optionKey < 192)   /* 128-191 = boolean */
			{
			    dos.writeBoolean(this.getBooleanOption(optionKey));
			}
			else if (optionKey < 224)   /* 192-223 = long */
			{
				dos.writeLong(this.getLongOption(optionKey));
			}
			else   /* 226-255 = Scrambled String */
			{
			    byte[] optionValue = this.getStringOption(optionKey).getBytes();
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
	public synchronized String getStringOption(int key)
	{
		return ((String) this.options.get(new Integer(key)));
	}
	public synchronized int getIntOption(int key)
	{
	    return (((Integer) this.options.get(new Integer(key))).intValue());
	}
	public synchronized boolean getBooleanOption(int key)
	{
	    return (((Boolean) this.options.get(new Integer(key))).booleanValue());
	}
	public synchronized long getLongOption(int key)
	{
	    return (((Long) this.options.get(new Integer(key))).longValue());
	}


	// Option setting methods (no type checking!)
	public synchronized void setStringOption(int key, String value)
	{
	    this.options.put(new Integer(key), value);
	}
	public synchronized void setIntOption(int key, int value)
	{
	    this.options.put(new Integer(key), new Integer(value));
	}
	public synchronized void setBooleanOption(int key, boolean value)
	{
	    this.options.put(new Integer(key), new Boolean(value));
	}
	public synchronized void setLongOption(int key, long value)
	{
	    this.options.put(new Integer(key), new Long(value));
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

	// Retrieves color value from color scheme
	final public int getSchemeColor(int type)
	{
		switch (getIntOption(OPTION_COLOR_SCHEME))
		{
		case CLRSCHHEME_BOW:
			if (type == CLRSCHHEME_BLUE) return 0xFF;
			return (type == CLRSCHHEME_BACK) ? 0xFFFFFF : 0x000000;
			
		case CLRSCHHEME_WOB:
			if (type == CLRSCHHEME_BLUE) return 0xFFFF;
			return (type == CLRSCHHEME_BACK) ? 0x000000 : 0xFFFFFF;
			
		case CLRSCHHEME_WOBL:
			if (type == CLRSCHHEME_BLUE) return 0xFFFF;
			return (type == CLRSCHHEME_BACK) ? 0x000080 : 0xFFFFFF;
		}
		return 0;
	}
	
	
	/**************************************************************************/
	/**************************************************************************/


	// Form for editing option values
	public class OptionsForm implements CommandListener
	{
		boolean lastGroupsUsed, lastHideOffline;
		int lastSortMethod, lastColorScheme;

		// Commands
		private Command backCommand;
		private Command saveCommand;
        //#sijapp cond.if target is "MOTOROLA"#
        private Command selectCommand;
        //#sijapp cond.end#

		// Options menu
		private List optionsMenu;


		// Options form
		private Form optionsForm;


		// Options
		private TextField uinTextField;
		private TextField passwordTextField;
		private TextField srvHostTextField;
		private TextField srvPortTextField;
		private ChoiceGroup keepConnAliveChoiceGroup;
		private ChoiceGroup connTypeChoiceGroup;
		private ChoiceGroup autoConnectChoiceGroup;
		private ChoiceGroup shadowConnChoiceGroup;
		private ChoiceGroup uiLanguageChoiceGroup;
		private ChoiceGroup displayDateChoiceGroup;
		private ChoiceGroup clSortByChoiceGroup;
		private ChoiceGroup clHideOfflineChoiceGroup;
		// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "MOTOROLA"#
		private ChoiceGroup messageNotificationModeChoiceGroup;
		private TextField messageNotificationSoundfileTextField;
		private Gauge messageNotificationSoundVolume;
		private ChoiceGroup onlineNotificationModeChoiceGroup;
		private TextField onlineNotificationSoundfileTextField;
		private Gauge onlineNotificationSoundVolume;
		// #sijapp cond.elseif target is "RIM"#
		private ChoiceGroup messageNotificationModeChoiceGroup;
		private ChoiceGroup onlineNotificationModeChoiceGroup;
		// #sijapp cond.end#
		// #sijapp cond.if target is "SIEMENS" | target is "RIM" | target is "MIDP2" | target is "MOTOROLA"#
		private ChoiceGroup vibratorChoiceGroup;
		// #sijapp cond.end#
		private ChoiceGroup cp1251HackChoiceGroup;
		// #sijapp cond.if modules_TRAFFIC is "true" #
		private TextField costPerPacketTextField;
		private TextField costPerDayTextField;
		private TextField costPacketLengthTextField;
		private TextField currencyTextField;
		// #sijapp cond.end#
		private ChoiceGroup useSmallFont;
		private ChoiceGroup showUserGroups;
		private ChoiceGroup colorScheme;
		
		// #sijapp cond.if modules_HISTORY is "true" #
		private ChoiceGroup useHistory;
		// #sijapp cond.end#

		// Constructor
		public OptionsForm() throws NullPointerException
		{

			// Initialize commands
			this.backCommand = new Command(ResourceBundle.getString("back"), Command.BACK, 2);
			this.saveCommand = new Command(ResourceBundle.getString("save"), Command.SCREEN, 1);
                                                                // #sijapp cond.if target is "MOTOROLA"#
                                                                 this.selectCommand=new Command(ResourceBundle.getString("select"), Command.OK, 1);
                                                                // #sijapp cond.end#
			// Initialize options menu
			this.optionsMenu = new List(ResourceBundle.getString("options"), Choice.IMPLICIT);
			this.optionsMenu.append(ResourceBundle.getString("options_account"), null);
			this.optionsMenu.append(ResourceBundle.getString("options_network"), null);
			this.optionsMenu.append(ResourceBundle.getString("options_interface"), null);
			// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "MOTOROLA"#
			this.optionsMenu.append(ResourceBundle.getString("options_signaling"), null);
                                                                // #sijapp cond.end#
			
			// #sijapp cond.if modules_TRAFFIC is "true" #
			this.optionsMenu.append(ResourceBundle.getString("options_cost"), null);
			// #sijapp cond.end#
			// #sijapp cond.if target is "MOTOROLA"#
            this.optionsMenu.addCommand(this.selectCommand);
            // #sijapp cond.end#
            this.optionsMenu.addCommand(this.backCommand);
			this.optionsMenu.setCommandListener(this);

			// Initialize options form
			this.optionsForm = new Form(ResourceBundle.getString("options"));
			this.optionsForm.addCommand(this.saveCommand);
			this.optionsForm.addCommand(this.backCommand);
			this.optionsForm.setCommandListener(this);
			
			this.initSubMenuUI(0);
			this.initSubMenuUI(1);
			this.initSubMenuUI(2);
			this.initSubMenuUI(3);
			this.initSubMenuUI(4);
			
		}


		
		// Initialize the UI elements depending on the submenu id
        public void initSubMenuUI(int i)
        {

            switch (i)
            {
            case 0:

                // Initialize elements (account section)
                this.uinTextField = new TextField(ResourceBundle.getString("uin"), Options.this.getStringOption(Options.OPTION_UIN), 12,TextField.NUMERIC);
                this.passwordTextField = new TextField(ResourceBundle.getString("password"), Options.this.getStringOption(Options.OPTION_PASSWORD), 32, TextField.PASSWORD);
                break;

            case 1:
                // Initialize elements (network section)
                this.srvHostTextField = new TextField(ResourceBundle.getString("server_host"), Options.this.getStringOption(Options.OPTION_SRV_HOST), 32, TextField.ANY);
                this.srvPortTextField = new TextField(ResourceBundle.getString("server_port"), Options.this.getStringOption(Options.OPTION_SRV_PORT), 5, TextField.NUMERIC);
                this.keepConnAliveChoiceGroup = new ChoiceGroup(ResourceBundle.getString("keep_conn_alive"), Choice.MULTIPLE);
                this.keepConnAliveChoiceGroup.append(ResourceBundle.getString("yes"), null);
                this.keepConnAliveChoiceGroup.setSelectedIndex(0, Options.this.getBooleanOption(Options.OPTION_KEEP_CONN_ALIVE));
                this.connTypeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("conn_type"), Choice.MULTIPLE);
                this.connTypeChoiceGroup.append(ResourceBundle.getString("async"), null);
                if (Options.this.getIntOption(Options.OPTION_CONN_TYPE) == 0)
                {
                    this.connTypeChoiceGroup.setSelectedIndex(0, false);
                } else
                {
                    this.connTypeChoiceGroup.setSelectedIndex(0, true);
                }
                this.shadowConnChoiceGroup = new ChoiceGroup(ResourceBundle.getString("shadow_con")+"?", Choice.MULTIPLE);
                this.shadowConnChoiceGroup.append(ResourceBundle.getString("yes"), null);
                this.shadowConnChoiceGroup.setSelectedIndex(0, Options.this.getBooleanOption(Options.OPTION_SHADOW_CON));
                this.autoConnectChoiceGroup = new ChoiceGroup(ResourceBundle.getString("auto_connect") + "?", Choice.MULTIPLE);
                this.autoConnectChoiceGroup.append(ResourceBundle.getString("yes"), null);
                this.autoConnectChoiceGroup.setSelectedIndex(0, Options.this.getBooleanOption(Options.OPTION_AUTO_CONNECT));
                break;

            case 2:
                // Initialize elements (interface section)
                this.uiLanguageChoiceGroup = new ChoiceGroup(ResourceBundle.getString("language"), Choice.EXCLUSIVE);
                for (int j = 0; j < ResourceBundle.LANG_AVAILABLE.length; j++)
                {
                    this.uiLanguageChoiceGroup.append(ResourceBundle.getString("lang_" + ResourceBundle.LANG_AVAILABLE[j]), null);
                    if (ResourceBundle.LANG_AVAILABLE[j].equals(Options.this.getStringOption(Options.OPTION_UI_LANGUAGE)))
                    {
                        this.uiLanguageChoiceGroup.setSelectedIndex(j, true);
                    }
                }
                this.displayDateChoiceGroup = new ChoiceGroup(ResourceBundle.getString("display_date"), Choice.MULTIPLE);
                this.displayDateChoiceGroup.append(ResourceBundle.getString("yes"), null);
                this.displayDateChoiceGroup.setSelectedIndex(0, Options.this.getBooleanOption(Options.OPTION_DISPLAY_DATE));
                this.clSortByChoiceGroup = new ChoiceGroup(ResourceBundle.getString("sort_by"), Choice.EXCLUSIVE);
                this.clSortByChoiceGroup.append(ResourceBundle.getString("sort_by_status"), null);
                this.clSortByChoiceGroup.append(ResourceBundle.getString("sort_by_name"), null);
                this.clSortByChoiceGroup.setSelectedIndex(Options.this.getIntOption(Options.OPTION_CL_SORT_BY), true);
                this.clHideOfflineChoiceGroup = new ChoiceGroup(ResourceBundle.getString("hide_offline"), Choice.MULTIPLE);
                this.clHideOfflineChoiceGroup.append(ResourceBundle.getString("yes"), null);
                this.clHideOfflineChoiceGroup.setSelectedIndex(0, Options.this.getBooleanOption(Options.OPTION_CL_HIDE_OFFLINE));
                this.cp1251HackChoiceGroup = new ChoiceGroup(ResourceBundle.getString("cp1251"), Choice.MULTIPLE);
                this.cp1251HackChoiceGroup.append(ResourceBundle.getString("yes"), null);
                this.cp1251HackChoiceGroup.setSelectedIndex(0, Options.this.getBooleanOption(Options.OPTION_CP1251_HACK));
                this.useSmallFont = new ChoiceGroup(ResourceBundle.getString("chat_small_font"), Choice.MULTIPLE);
                this.useSmallFont.append(ResourceBundle.getString("yes"), null);
                this.useSmallFont.setSelectedIndex(0, Options.this.getBooleanOption(Options.OPTION_CHAT_SMALL_FONT));

                this.showUserGroups = new ChoiceGroup(ResourceBundle.getString("show_user_groups"), Choice.MULTIPLE);
                this.showUserGroups.append(ResourceBundle.getString("yes"), null);
                this.showUserGroups.setSelectedIndex(0, Options.this.getBooleanOption(Options.OPTION_USER_GROUPS));

                // #sijapp cond.if modules_HISTORY is "true" #
                this.useHistory = new ChoiceGroup(ResourceBundle.getString("use_history") + "?", Choice.MULTIPLE);
                this.useHistory.append(ResourceBundle.getString("yes"), null);
                this.useHistory.setSelectedIndex(0, Options.this.getBooleanOption(Options.OPTION_HISTORY));
                // #sijapp cond.end#
                
                this.colorScheme = new ChoiceGroup(ResourceBundle.getString("color_scheme"), Choice.EXCLUSIVE);
                this.colorScheme.append(ResourceBundle.getString("black_on_white"), null);
                this.colorScheme.append(ResourceBundle.getString("white_on_black"), null);
                this.colorScheme.append(ResourceBundle.getString("white_on_blue"), null);
                this.colorScheme.setSelectedIndex(Options.this.getIntOption(Options.OPTION_COLOR_SCHEME), true);

                break;
            case 3:

                // Initialize elements (Signaling section)

                // #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "MOTOROLA"#
                this.onlineNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("onl_notification"), Choice.EXCLUSIVE);
                this.onlineNotificationModeChoiceGroup.append(ResourceBundle.getString("no"), null);
                this.onlineNotificationModeChoiceGroup.append(ResourceBundle.getString("beep"), null);
                this.onlineNotificationModeChoiceGroup.append(ResourceBundle.getString("sound"), null);
                this.onlineNotificationModeChoiceGroup.setSelectedIndex(Options.this.getIntOption(Options.OPTION_ONLINE_NOTIFICATION_MODE),true);
                this.onlineNotificationSoundfileTextField = new TextField(ResourceBundle.getString("onl_sound_file_name"),Options.this.getStringOption(Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE), 32, TextField.ANY);
                this.messageNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("message_notification"),Choice.EXCLUSIVE);
                this.messageNotificationModeChoiceGroup.append(ResourceBundle.getString("no"), null);
                this.messageNotificationModeChoiceGroup.append(ResourceBundle.getString("beep"), null);
                this.messageNotificationModeChoiceGroup.append(ResourceBundle.getString("sound"), null);
                this.messageNotificationModeChoiceGroup.setSelectedIndex(Options.this.getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_MODE), true);
                this.messageNotificationSoundfileTextField = new TextField(ResourceBundle.getString("msg_sound_file_name"), Options.this.getStringOption(Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE), 32, TextField.ANY);
                this.messageNotificationSoundVolume = new Gauge(ResourceBundle.getString("volume"), true, 10, Options.this.getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_VOLUME) / 10);
                this.onlineNotificationSoundVolume = new Gauge(ResourceBundle.getString("volume"), true, 10, Options.this.getIntOption(Options.OPTION_ONLINE_NOTIFICATION_VOLUME) / 10);
                // #sijapp cond.elseif target is "RIM"#
                this.messageNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("onl_notification"), Choice.EXCLUSIVE);
                this.messageNotificationModeChoiceGroup.append(ResourceBundle.getString("no"), null);
                this.messageNotificationModeChoiceGroup.append(ResourceBundle.getString("beep"), null);
                this.messageNotificationModeChoiceGroup.setSelectedIndex(Options.this.getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_MODE), true);
                this.onlineNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("msg_notification"), Choice.EXCLUSIVE);
                this.onlineNotificationModeChoiceGroup.append(ResourceBundle.getString("no"), null);
                this.onlineNotificationModeChoiceGroup.append(ResourceBundle.getString("beep"), null);
                this.onlineNotificationModeChoiceGroup.setSelectedIndex(Options.this.getIntOption(Options.OPTION_ONLINE_NOTIFICATION_MODE),true);
                // #sijapp cond.end#
                // #sijapp cond.if target is "SIEMENS" | target is "RIM" | target is "MIDP2" | target is "MOTOROLA"#
                this.vibratorChoiceGroup = new ChoiceGroup(ResourceBundle.getString("vibration") + "?", Choice.MULTIPLE);
                this.vibratorChoiceGroup.append(ResourceBundle.getString("yes"), null);
                this.vibratorChoiceGroup.setSelectedIndex(0, Options.this.getBooleanOption(Options.OPTION_VIBRATOR));
                // #sijapp cond.end#

                // #sijapp cond.if target is "SIEMENS" | target is "RIM" | target is "MIDP2" | target is "MOTOROLA"#
                break;
            // #sijapp cond.end#

            // #sijapp cond.if modules_TRAFFIC is "true" #
            // #sijapp cond.if target isnot "DEFAULT"#
            case 4:
                // #sijapp cond.end#
                // Initialize elements (cost section)
                this.costPerPacketTextField = new TextField(ResourceBundle.getString("cpp"), Util.intToDecimal(Options.this.getIntOption(Options.OPTION_COST_PER_PACKET)), 6, TextField.ANY);
                this.costPerDayTextField = new TextField(ResourceBundle.getString("cpd"), Util.intToDecimal(Options.this.getIntOption(Options.OPTION_COST_PER_DAY)), 6, TextField.ANY);
                this.costPacketLengthTextField = new TextField(ResourceBundle.getString("plength"), String.valueOf(Options.this.getIntOption(Options.OPTION_COST_PACKET_LENGTH) / 1024), 4, TextField.NUMERIC);
                this.currencyTextField = new TextField(ResourceBundle.getString("currency"), Options.this.getStringOption(Options.OPTION_CURRENCY), 4, TextField.ANY);
                break;
            // #sijapp cond.end#
            }
        }
		
		// Activate options menu
		public void activate()
		{
			this.optionsMenu.setSelectedIndex(0, true);   // Reset
			Jimm.display.setCurrent(this.optionsMenu);
		}


		// Command listener
		public void commandAction(Command c, Displayable d)
		{
			// Look for select command
			// #sijapp cond.if target is "MOTOROLA"#
            if ((c == List.SELECT_COMMAND) || (c == this.selectCommand))
            // #sijapp cond.else#
            if (c == List.SELECT_COMMAND)
            // #sijapp cond.end#
			{
				lastHideOffline = Options.this.getBooleanOption(Options.OPTION_CL_HIDE_OFFLINE);
				lastGroupsUsed = Options.this.getBooleanOption(Options.OPTION_USER_GROUPS);
				lastSortMethod = Options.this.getIntOption(Options.OPTION_CL_SORT_BY);
				lastColorScheme = Options.this.getIntOption(Options.OPTION_COLOR_SCHEME);

				// Delete all items
				//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA"#
				this.optionsForm.deleteAll();
				//#sijapp cond.else #
				while (this.optionsForm.size() > 0) { this.optionsForm.delete(0); }
				//#sijapp cond.end#
				

				// Add elements, depending on selected option menu item
				switch (this.optionsMenu.getSelectedIndex())
				{
					case 0:
					    this.optionsForm.append(this.uinTextField);
						this.optionsForm.append(this.passwordTextField);
						break;
					case 1:
						this.optionsForm.append(this.srvHostTextField);
						this.optionsForm.append(this.srvPortTextField);
						this.optionsForm.append(this.keepConnAliveChoiceGroup);
						this.optionsForm.append(this.autoConnectChoiceGroup);
						this.optionsForm.append(this.connTypeChoiceGroup);
						this.optionsForm.append(this.shadowConnChoiceGroup);
						break;
					case 2:
						this.optionsForm.append(this.uiLanguageChoiceGroup);
						this.optionsForm.append(this.displayDateChoiceGroup);
						this.optionsForm.append(this.showUserGroups);
						this.optionsForm.append(this.clSortByChoiceGroup);
						this.optionsForm.append(this.clHideOfflineChoiceGroup);
						this.optionsForm.append(this.useSmallFont);
						
						// #sijapp cond.if modules_HISTORY is "true" #
						this.optionsForm.append(this.useHistory);
						// #sijapp cond.end#
						
						this.optionsForm.append(this.cp1251HackChoiceGroup);
						this.optionsForm.append(this.colorScheme);
						
						break;
					case 3:
						// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "MOTOROLA"#
						this.optionsForm.append(this.messageNotificationModeChoiceGroup);
						this.optionsForm.append(this.messageNotificationSoundVolume);
						this.optionsForm.append(this.messageNotificationSoundfileTextField);
						this.optionsForm.append(this.vibratorChoiceGroup);
						this.optionsForm.append(this.onlineNotificationModeChoiceGroup);
						this.optionsForm.append(this.onlineNotificationSoundVolume);
						this.optionsForm.append(this.onlineNotificationSoundfileTextField);
						// #sijapp cond.elseif target is "RIM"#
						this.optionsForm.append(this.messageNotificationModeChoiceGroup);
						this.optionsForm.append(this.vibratorChoiceGroup);
						this.optionsForm.append(this.onlineNotificationModeChoiceGroup);
						// #sijapp cond.end#
						
						// #sijapp cond.if target is "SIEMENS" | target is "RIM" | target is "MIDP2" | target is "MOTOROLA"#
						break;
						// #sijapp cond.end#

					// #sijapp cond.if modules_TRAFFIC is "true" #
					// #sijapp cond.if target isnot "DEFAULT"#
					case 4:
                    // #sijapp cond.end#
						this.optionsForm.append(this.costPerPacketTextField);
						this.optionsForm.append(this.costPerDayTextField);
						this.optionsForm.append(this.costPacketLengthTextField);
						this.optionsForm.append(this.currencyTextField);
						break;
					// #sijapp cond.end#
				}

				// Activate options form
				Jimm.display.setCurrent(this.optionsForm);

			}

			// Look for back command
			else if (c == this.backCommand)
           {
               if (d == this.optionsForm)
               {
                   this.initSubMenuUI(this.optionsMenu.getSelectedIndex());
                   Jimm.display.setCurrent(this.optionsMenu);
               }
               else
               {
                   // Active MM/CL
                   if (Jimm.jimm.getIcqRef().isConnected())
                   {
                       Jimm.jimm.getContactListRef().activate();
                   } else
                   {
                       Jimm.jimm.getMainMenuRef().activate();
                   }
               }

           }

			// Look for save command
			else if (c == this.saveCommand)
			{

				// Save values, depending on selected option menu item
				switch (this.optionsMenu.getSelectedIndex())
				{
					case 0:
						Options.this.setStringOption(Options.OPTION_UIN,this.uinTextField.getString());
						Options.this.setStringOption(Options.OPTION_PASSWORD,this.passwordTextField.getString());
						break;
					case 1:
					    Options.this.setStringOption(Options.OPTION_SRV_HOST,this.srvHostTextField.getString());
					    Options.this.setStringOption(Options.OPTION_SRV_PORT,this.srvPortTextField.getString());
						Options.this.setBooleanOption(Options.OPTION_KEEP_CONN_ALIVE,this.keepConnAliveChoiceGroup.isSelected(0));
						Options.this.setBooleanOption(Options.OPTION_AUTO_CONNECT,this.autoConnectChoiceGroup.isSelected(0));
						Options.this.setBooleanOption(Options.OPTION_SHADOW_CON,this.shadowConnChoiceGroup.isSelected(0));
						if (this.connTypeChoiceGroup.isSelected(0))
						{
							Options.this.setIntOption(Options.OPTION_CONN_TYPE,1);
						}
						else
						{
						    Options.this.setIntOption(Options.OPTION_CONN_TYPE,0);
						}
						break;
					case 2:
						Options.this.setStringOption(Options.OPTION_UI_LANGUAGE,ResourceBundle.LANG_AVAILABLE[this.uiLanguageChoiceGroup.getSelectedIndex()]);
						Options.this.setBooleanOption(Options.OPTION_DISPLAY_DATE,this.displayDateChoiceGroup.isSelected(0));
						
						int newSortMethod = 0;
						
						if (this.clHideOfflineChoiceGroup.isSelected(0))
						{
							newSortMethod = 0;
						}
						else
	  					{
							newSortMethod = this.clSortByChoiceGroup.getSelectedIndex();
						}
						
						Options.this.setIntOption(Options.OPTION_CL_SORT_BY, newSortMethod);
						
						Options.this.setBooleanOption(Options.OPTION_CL_HIDE_OFFLINE,this.clHideOfflineChoiceGroup.isSelected(0));
						Options.this.setBooleanOption(Options.OPTION_CP1251_HACK,this.cp1251HackChoiceGroup.isSelected(0));
						Options.this.setBooleanOption(Options.OPTION_CHAT_SMALL_FONT, this.useSmallFont.isSelected(0));
						
						// #sijapp cond.if modules_HISTORY is "true" #
						Options.this.setBooleanOption(Options.OPTION_HISTORY, this.useHistory.isSelected(0));
						// #sijapp cond.end#
						
						boolean newUseGroups = this.showUserGroups.isSelected(0);
						Options.this.setBooleanOption(Options.OPTION_USER_GROUPS, newUseGroups);
						
						int newColorScheme = colorScheme.getSelectedIndex(); 
						Options.this.setIntOption(Options.OPTION_COLOR_SCHEME, newColorScheme);
						
						boolean newHideOffline = Options.this.getBooleanOption(Options.OPTION_CL_HIDE_OFFLINE);
						Jimm.jimm.getContactListRef().optionsChanged
						(
							(newUseGroups != lastGroupsUsed) || (newHideOffline != lastHideOffline),
							(newSortMethod != lastSortMethod)
						);
						
						if (lastColorScheme != newColorScheme) Jimm.jimm.setColorScheme();
						
						break;
						
					case 3:
						// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "MOTOROLA"#
						Options.this.setIntOption(Options.OPTION_MESSAGE_NOTIFICATION_MODE,this.messageNotificationModeChoiceGroup.getSelectedIndex());
						Options.this.setStringOption(Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE,this.messageNotificationSoundfileTextField.getString());
						Options.this.setIntOption(Options.OPTION_MESSAGE_NOTIFICATION_VOLUME,this.messageNotificationSoundVolume.getValue()*10);
						Options.this.setIntOption(Options.OPTION_ONLINE_NOTIFICATION_MODE,this.onlineNotificationModeChoiceGroup.getSelectedIndex());
						Options.this.setStringOption(Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE,this.onlineNotificationSoundfileTextField.getString());
						Options.this.setIntOption(Options.OPTION_ONLINE_NOTIFICATION_VOLUME,this.onlineNotificationSoundVolume.getValue()*10);
						// System.out.println("Message Volume: "+Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_VOLUME));
						// System.out.println("Online Volume: "+Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_ONLINE_NOTIFICATION_VOLUME));
						// #sijapp cond.elseif target is "RIM"#
						Options.this.setIntOption(Options.OPTION_MESSAGE_NOTIFICATION_MODE,this.messageNotificationModeChoiceGroup.getSelectedIndex());
						Options.this.setIntOption(Options.OPTION_ONLINE_NOTIFICATION_MODE,this.onlineNotificationModeChoiceGroup.getSelectedIndex());
						// #sijapp cond.end#
						// #sijapp cond.if target is "SIEMENS" | target is "RIM" | target is "MIDP2" | target is "MOTOROLA"#
						Options.this.setBooleanOption(Options.OPTION_VIBRATOR,this.vibratorChoiceGroup.isSelected(0));
						break;
						// #sijapp cond.end#
					// #sijapp cond.if modules_TRAFFIC is "true" #
				    // #sijapp cond.if target isnot "DEFAULT"#
					case 4:
                    // #sijapp cond.end#
						Options.this.setIntOption(Options.OPTION_COST_PER_PACKET,Util.decimalToInt(this.costPerPacketTextField.getString()));
						this.costPerPacketTextField.setString(Util.intToDecimal(Options.this.getIntOption(Options.OPTION_COST_PER_PACKET)));
						Options.this.setIntOption(Options.OPTION_COST_PER_DAY,Util.decimalToInt(this.costPerDayTextField.getString()));
						this.costPerDayTextField.setString(Util.intToDecimal(Options.this.getIntOption(Options.OPTION_COST_PER_DAY)));
						Options.this.setIntOption(Options.OPTION_COST_PACKET_LENGTH,Integer.parseInt(this.costPacketLengthTextField.getString()) * 1024);
						Options.this.setStringOption(Options.OPTION_CURRENCY,this.currencyTextField.getString());
						break;
					// #sijapp cond.end#
				}

				// Save options
				try
				{
					Options.this.save();
				}
				catch (Exception e)
				{
					// Do nothing
				}

				// Activate MM/CL
				if (Jimm.jimm.getIcqRef().isConnected())
				{
					Jimm.jimm.getContactListRef().activate();
				}
				else
				{
					this.activate();
				}
			}
		}
	}
}

