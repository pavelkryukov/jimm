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
 *******************************************************************************
 File: src/jimm/Options.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 ******************************************************************************/


/*******************************************************************************
 Current record store format:

 Record #1: VERSION               (UTF-8)
 Record #2: UIN                   (UTF-8)
            SRV_HOST              (UTF-8)
            SRV_PORT              (UTF-8)
            KEEP_CONN_ALIVE       (Boolean)
            CONN_TYPE             (Integer)
            UI_LANGUAGE           (UTF-8)
            DISPLAY_DATE          (Boolean)
            CL_SORT_BY            (Integer)
            CL_HIDE_OFFLINE       (Boolean)
            MSG_NOTIFICATION_MODE (Integer)
            ONL_NOTIFICATION_MODE (Integer)
            MSG_NOTIFY_SOUND_FILE (UTF-8)
            ONL_NOTIFY_SOUND_FILE (UTF-8)
            VIBRATOR              (Boolean)
            KEEP_CHAT             (Boolean)
            USE_CP1251_HACK       (Boolean)
            COST_PER_PACKET       (Integer)
            COST_PER_DAY          (Integer)
            COST_PACKET_LENGTH    (Integer)
            CURRENCY              (UTF-8)
            ONLINE_STATUS         (Long)
 Record #3: PASSWORD              (Somewhat encrypted UTF-8)
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

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;


public class Options
{

    // Keys for getting Information of Option values
    // String
    public static final int OPTION_UIN = 1;
    public static final int OPTION_PASSWORD = 2;
    public static final int OPTION_SRV_HOST = 3;
    public static final int OPTION_SRV_PORT = 4;
    public static final int OPTION_UI_LANGUAGE = 5;
    public static final int OPTION_ONLINE_SOUNDFILE_NAME = 6;
    public static final int OPTION_MESSAGE_SOUNDFILE_NAME = 7;
    public static final int OPTION_CURRENCY = 8;
    
    // Boolean
    public static final int OPTION_KEEP_CONN_ALIVE = 20;
    public static final int OPTION_DISPLAY_DATE = 21;
    public static final int OPTION_CL_HIDE_OFFLINE = 22;
    public static final int OPTION_VIBRATOR = 23;
    public static final int OPTION_KEEPCHAT = 24;
    public static final int OPTION_CP1251_HACK = 25;
    
    // Int
    public static final int OPTION_CONN_TYPE = 40;
    public static final int OPTION_CL_SORT_BY = 41;
    public static final int OPTION_MESSAGE_SOUND = 42;
    public static final int OPTION_ONLINE_SOUND = 43;
    public static final int OPTION_COST_PER_PACKET = 44;
    public static final int OPTION_COST_PER_DAY = 45;
    public static final int OPTION_COST_PACKET_LENGHT = 46;
    
    // Long
    public static final int OPTION_ONLINE_STATUS = 60;
    
    
	
    // Default values
	public static final String DEFAULT_SRV_HOST = "login.icq.com";
	public static final String DEFAULT_SRV_PORT = "5190";
	public static final boolean DEFAULT_KEEP_CONN_ALIVE = true;
	public static final int DEFAULT_CONN_TYPE = 0;
	public static final String DEFAULT_UI_LANGUAGE = ResourceBundle.LANG_AVAILABLE[0];
	public static final boolean DEFAULT_DISPLAY_DATE = false;
	public static final int DEFAULT_CL_SORT_BY = 0;
	public static final boolean  DEFAULT_CL_HIDE_OFFLINE = false;
	// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
	public static final int DEFAULT_MSG_NOTIFICATION = 0;
	public static final int DEFAULT_ONL_NOTIFICATION = 0;
	//#sijapp cond.end#
	//#sijapp cond.if target is "SIEMENS"#
	public static final String DEFAULT_MSG_NOTIFY_SOUND_FILE = "message.mmf";
	public static final String DEFAULT_ONL_NOTIFY_SOUND_FILE = "online.mmf";
	//#sijapp cond.end#
	//#sijapp cond.if target is "MIDP2"#
	public static final String DEFAULT_MSG_NOTIFY_SOUND_FILE = "message.wav";
	public static final String DEFAULT_ONL_NOTIFY_SOUND_FILE = "online.wav";
	//#sijapp cond.end#
	// #sijapp cond.if target is "SIEMENS" | target is "RIM"#
	public static final boolean DEFAULT_VIBRATOR = false;
	// #sijapp cond.end#
	public static final boolean DEFAULT_KEEP_CHAT = true;
	public static final boolean DEFAULT_CP1251_HACK = false;
	//#sijapp cond.if modules_TRAFFIC is "true" #
	public static final int DEFAULT_COST_PER_PACKET = 0;
	public static final int DEFAULT_COST_PER_DAY = 0;
	public static final int DEFAULT_COST_PACKET_LENGTH = 1024;
	public static final String DEFAULT_CURRENCY = "$";
	//#sijapp cond.end#
	public static final long DEFAULT_ONLINE_STATUS = ContactList.STATUS_ONLINE;


	/**************************************************************************/


	// Section account, UIN
	private String uin;

	// Section account, password
	private String password;


	// Section network, hostname of the login server
	private String srvHost;

	// Section network, port of the login server
	private String srvPort;

	// Section network, keep connection alive flag
	private boolean keepConnAlive;

	// Section network, type of connection used
	private int conn_type;


	// Section interface, user interface language/localization
	private String uiLanguage;

	// Section interface, date display flag
	private boolean displayDate;

	// Section interface, setting cl sort method
	private int clSortBy;

	// Section interface, show/hide offline contacts flag
	private boolean clHideOffline;

	// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
	// Section interface, message notification mode
	private int msgNotificationMode;

	// Section interface, online notification mode
	private int onlNotificationMode;
	// #sijapp cond.end#

	// #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
	// Section interface, online soundfile name
	private String onlSoundFileName;

	//Section interface, message soundfile name
	private String msgSoundFileName;
	// #sijapp cond.end#

	// #sijapp cond.if target is "SIEMENS" | target is "RIM"#
	// Section interface, vibration enabled/disabled
	private boolean vibrator;
	// #sijapp cond.end#

	// Section interface, keep the chat history enabled/disabled
	private boolean keep_chat;

	//Section interface, use the cp1251 hack?
	private boolean cp1251_hack;


	// #sijapp cond.if modules_TRAFFIC is "true"#
	// Section cost, cost per packet
	private int costPerPacket;

	// Section cost, cost per day
	private int costPerDay;

	// Section cost, length of packet to be charged in bytes
	private int costPacketLength;

	// Section cost, name or symbol of currency
	private String currency;
	// #sijapp cond.end#


	// Current online status (internal setting)
	private long onlineStatus;


	// Options form
	public OptionsForm optionsForm;


	// Constructor
	public Options()
	{

		// Try to load option values from record store
		try
		{
			this.load();
			ResourceBundle.setCurrUiLanguage(this.getOptionValueString(Options.OPTION_UI_LANGUAGE));
		}
		// Use default values if loading option values from record store failed
		catch (Exception e)
		{
			this.setOptionValueString(Options.OPTION_UIN,"");
			this.setOptionValueString(Options.OPTION_PASSWORD,"");
			this.setOptionValueString(Options.OPTION_SRV_HOST,Options.DEFAULT_SRV_HOST);
			this.setOptionValueString(Options.OPTION_SRV_PORT,Options.DEFAULT_SRV_PORT);
			this.setOptionValueBool(Options.OPTION_KEEP_CONN_ALIVE,Options.DEFAULT_KEEP_CONN_ALIVE);
			this.setOptionValueInt(Options.OPTION_CONN_TYPE,Options.DEFAULT_CONN_TYPE);
			this.setOptionValueString(Options.OPTION_UI_LANGUAGE,Options.DEFAULT_UI_LANGUAGE);
			this.setOptionValueBool(Options.OPTION_DISPLAY_DATE,Options.DEFAULT_DISPLAY_DATE);
			this.setOptionValueInt(Options.OPTION_CL_SORT_BY,Options.DEFAULT_CL_SORT_BY);
			this.setOptionValueBool(Options.OPTION_CL_HIDE_OFFLINE,Options.DEFAULT_CL_HIDE_OFFLINE);
			// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
			this.setOptionValueInt(Options.OPTION_MESSAGE_SOUND,ContactList.SOUND_TYPE_MESSAGE);
			this.setOptionValueInt(Options.OPTION_ONLINE_SOUND,ContactList.SOUND_TYPE_ONLINE);
			// #sijapp cond.end#
			// #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
			this.setOptionValueString(Options.OPTION_MESSAGE_SOUNDFILE_NAME,Options.DEFAULT_MSG_NOTIFY_SOUND_FILE);
			this.setOptionValueString(Options.OPTION_ONLINE_SOUNDFILE_NAME,Options.DEFAULT_ONL_NOTIFY_SOUND_FILE);
			// #sijapp cond.end#
			// #sijapp cond.if target is "SIEMENS" | target is "RIM"#
			this.setOptionValueBool(Options.OPTION_VIBRATOR,Options.DEFAULT_VIBRATOR);
			// #sijapp cond.end#
			this.setOptionValueBool(Options.OPTION_KEEPCHAT,Options.DEFAULT_KEEP_CHAT);
			this.setOptionValueBool(Options.OPTION_CP1251_HACK,Options.DEFAULT_CP1251_HACK);
			// #sijapp cond.if modules_TRAFFIC is "true" #
			this.setOptionValueInt(Options.OPTION_COST_PER_PACKET,Options.DEFAULT_COST_PER_PACKET);
			this.setOptionValueInt(Options.OPTION_COST_PER_DAY,DEFAULT_COST_PER_DAY);
			this.setOptionValueInt(Options.OPTION_COST_PACKET_LENGHT,DEFAULT_COST_PACKET_LENGTH);
			this.setOptionValueString(Options.OPTION_CURRENCY,DEFAULT_CURRENCY);
			// #sijapp cond.end#
			this.setOptionValueLong(Options.OPTION_ONLINE_STATUS,Options.DEFAULT_ONLINE_STATUS);
		}

		// Construct option form
		this.optionsForm = new OptionsForm();
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

		// Get all settings from 2nd record (except password)
		buf = account.getRecord(2);
		bais = new ByteArrayInputStream(buf);
		dis = new DataInputStream(bais);
		this.setOptionValueString(Options.OPTION_UIN,dis.readUTF());
		this.setOptionValueString(Options.OPTION_SRV_HOST,dis.readUTF());
		this.setOptionValueString(Options.OPTION_SRV_PORT,dis.readUTF());
		this.setOptionValueBool(Options.OPTION_KEEP_CONN_ALIVE,dis.readBoolean());
		this.setOptionValueInt(Options.OPTION_CONN_TYPE,dis.readInt());
		this.setOptionValueString(Options.OPTION_UI_LANGUAGE,dis.readUTF());
		this.setOptionValueBool(Options.OPTION_DISPLAY_DATE,dis.readBoolean());
		this.setOptionValueInt(Options.DEFAULT_CL_SORT_BY,dis.readInt());
		this.setOptionValueBool(Options.OPTION_CL_HIDE_OFFLINE,dis.readBoolean());
		// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
		this.setOptionValueInt(Options.OPTION_MESSAGE_SOUND,dis.readInt());
		this.setOptionValueInt(Options.OPTION_ONLINE_SOUND,dis.readInt());
		// #sijapp cond.else#
		dis.skipBytes(2*4);
		// #sijapp cond.end#
		// #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
		this.setOptionValueString(Options.OPTION_MESSAGE_SOUNDFILE_NAME,dis.readUTF());
		this.setOptionValueString(Options.OPTION_ONLINE_SOUNDFILE_NAME,dis.readUTF());
		// #sijapp cond.else#
		dis.readUTF();
		dis.readUTF();
		// #sijapp cond.end#
		// #sijapp cond.if target is "SIEMENS" | target is "RIM"#
		this.setOptionValueBool(Options.OPTION_VIBRATOR,dis.readBoolean());
		// #sijapp cond.else#
		dis.skipBytes(1);
		// #sijapp cond.end#
		this.setOptionValueBool(Options.OPTION_KEEPCHAT,dis.readBoolean());
		this.setOptionValueBool(Options.OPTION_CP1251_HACK,dis.readBoolean());
		// #sijapp cond.if modules_TRAFFIC is "true" #
		this.setOptionValueInt(Options.OPTION_COST_PER_PACKET,dis.readInt());
		this.setOptionValueInt(Options.OPTION_COST_PER_DAY,dis.readInt());
		this.setOptionValueInt(Options.OPTION_COST_PACKET_LENGHT,dis.readInt());
		this.setOptionValueString(Options.OPTION_CURRENCY,dis.readUTF());
		// #sijapp cond.else#
		dis.skipBytes(3*4);
		dis.readUTF();
		// #sijapp cond.end#
		this.setOptionValueLong(Options.OPTION_ONLINE_STATUS,dis.readLong());

		// Get password from 3rd record
		buf = account.getRecord(3);
		buf = Util.decipherPassword(buf);
		bais = new ByteArrayInputStream(buf);
		dis = new DataInputStream(bais);
		this.setOptionValueString(Options.OPTION_PASSWORD,dis.readUTF());

		// Close record store
		account.closeRecordStore();

		// Hide offline?
		if (this.getOptionValueBool(Options.OPTION_CL_HIDE_OFFLINE))
			Options.this.setOptionValueInt(Options.DEFAULT_CL_SORT_BY,0);
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

		// Save all settings into 2nd record (except password)
		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);
		dos.writeUTF(this.getOptionValueString(Options.OPTION_UIN));
		dos.writeUTF(this.getOptionValueString(Options.OPTION_SRV_HOST));
		dos.writeUTF(this.getOptionValueString(Options.OPTION_SRV_PORT));
		dos.writeBoolean(this.getOptionValueBool(Options.OPTION_KEEP_CONN_ALIVE));
		dos.writeInt(this.getOptionValueInt(Options.OPTION_CONN_TYPE));
		dos.writeUTF(this.getOptionValueString(Options.OPTION_UI_LANGUAGE));
		dos.writeBoolean(this.getOptionValueBool(Options.OPTION_DISPLAY_DATE));
		dos.writeInt(this.getOptionValueInt(Options.OPTION_CL_SORT_BY));
		dos.writeBoolean(this.getOptionValueBool(Options.OPTION_CL_HIDE_OFFLINE));
		// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
		dos.writeInt(this.getOptionValueInt(Options.OPTION_MESSAGE_SOUND));
		dos.writeInt(this.getOptionValueInt(Options.OPTION_ONLINE_SOUND));
		// #sijapp cond.else#
		dos.writeInt(0);
		dos.writeInt(0);
		//	#sijapp cond.end#
		// #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
		dos.writeUTF(this.getOptionValueString(Options.OPTION_MESSAGE_SOUNDFILE_NAME));
		dos.writeUTF(this.getOptionValueString(Options.OPTION_ONLINE_SOUNDFILE_NAME));
		// #sijapp cond.else#
		dos.writeUTF("");
		dos.writeUTF("");
		// #sijapp cond.end#
		// #sijapp cond.if target is "SIEMENS" | target is "RIM"#
		dos.writeBoolean(this.getOptionValueBool(Options.OPTION_VIBRATOR));
		// #sijapp cond.else#
		dos.writeBoolean(false);
		// #sijapp cond.end#
		dos.writeBoolean(this.getOptionValueBool(Options.OPTION_KEEPCHAT));
		dos.writeBoolean(this.getOptionValueBool(Options.OPTION_CP1251_HACK));
		// #sijapp cond.if modules_TRAFFIC is "true" #
		dos.writeInt(this.getOptionValueInt(Options.OPTION_COST_PER_PACKET));
		dos.writeInt(this.getOptionValueInt(Options.OPTION_COST_PER_DAY));
		dos.writeInt(this.getOptionValueInt(Options.OPTION_COST_PACKET_LENGHT));
		dos.writeUTF(this.getOptionValueString(Options.OPTION_CURRENCY));
		// #sijapp cond.else#
		dos.writeInt(0);
		dos.writeInt(0);
		dos.writeInt(0);
		dos.writeUTF("");
		// #sijapp cond.end#
		dos.writeLong(this.getOptionValueLong(Options.OPTION_ONLINE_STATUS));
		buf = baos.toByteArray();
		account.setRecord(2, buf, 0, buf.length);

		// Save password into 3rd record
		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);
		dos.writeUTF(this.getOptionValueString(Options.OPTION_PASSWORD));
		buf = baos.toByteArray();
		buf = Util.decipherPassword(buf);
		account.setRecord(3, buf, 0, buf.length);

		// Close record store
		account.closeRecordStore();

	}

	/**************************************************************************/
	// Get Option values for String, boolean, int and long
	/**************************************************************************/
	
	// Return Option value (String)
    public synchronized String getOptionValueString(int option)
    {
        switch (option)
        {
	        case Options.OPTION_UIN: return (this.uin);
	        case Options.OPTION_PASSWORD: return (this.password);
	        case Options.OPTION_SRV_HOST: return (this.srvHost);
	        case Options.OPTION_SRV_PORT: return (this.srvPort);
	        case Options.OPTION_UI_LANGUAGE: return (this.uiLanguage);
//	      #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
	        case Options.OPTION_ONLINE_SOUNDFILE_NAME: return (this.onlSoundFileName); 
	        case Options.OPTION_MESSAGE_SOUNDFILE_NAME: return (this.msgSoundFileName);
//		  #sijapp cond.end#
//	      #sijapp cond.if modules_TRAFFIC is "true" #
	        case Options.OPTION_CURRENCY: return (this.currency);
//		  #sijapp cond.end#
	        default: return ("");
        }
    }
	
	// Return Option value (boolean)
	public synchronized boolean getOptionValueBool(int option)
	{
		switch (option)
		{
			case Options.OPTION_KEEP_CONN_ALIVE: return(this.keepConnAlive);
			case Options.OPTION_DISPLAY_DATE: return(this.displayDate);
			case Options.OPTION_CL_HIDE_OFFLINE: return(this.clHideOffline);
//		      #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
			case Options.OPTION_VIBRATOR: return(this.vibrator);
//			  #sijapp cond.end#
			case Options.OPTION_KEEPCHAT: return(this.keep_chat);
			case Options.OPTION_CP1251_HACK: return(this.cp1251_hack);
			default: return (false);
		}
	}
	
	// Return Option value (int)
	public synchronized int getOptionValueInt(int option)
	{
	    switch (option)
	    {
	    	case Options.OPTION_CONN_TYPE: return(this.conn_type);
	    	case Options.OPTION_CL_SORT_BY: return(this.clSortBy);
//		      #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
	    	case Options.OPTION_MESSAGE_SOUND: return(this.msgNotificationMode);
	    	case Options.OPTION_ONLINE_SOUND: return(this.onlNotificationMode);
//			  #sijapp cond.end#
//		      #sijapp cond.if modules_TRAFFIC is "true" #
	    	case Options.OPTION_COST_PER_PACKET: return(this.costPerPacket);
	    	case Options.OPTION_COST_PER_DAY: return(this.costPerDay);
	    	case Options.OPTION_COST_PACKET_LENGHT: return(this.costPacketLength);
//			  #sijapp cond.end#
	    	default: return(0);
	    }
	}
	
	// Return Option value (long)
	public synchronized long getOptionValueLong(int option)
	{
	    return (this.onlineStatus);
	}
	
	/**************************************************************************/
	// Set Option values for String, boolean, int and long
	/**************************************************************************/
	
	
	// Return Option value (String)
	public synchronized void setOptionValueString(int option,String value)
	{
        switch (option)
        {
	        case Options.OPTION_UIN: this.uin = value;
	        case Options.OPTION_PASSWORD: this.password = value;
	        case Options.OPTION_SRV_HOST: this.srvHost = value;
	        case Options.OPTION_SRV_PORT: this.srvPort = value;
	        case Options.OPTION_UI_LANGUAGE: this.uiLanguage = value;
//		      #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
	        case Options.OPTION_ONLINE_SOUNDFILE_NAME: this.onlSoundFileName = value; 
	        case Options.OPTION_MESSAGE_SOUNDFILE_NAME: this.msgSoundFileName = value;
//			  #sijapp cond.end#
//		      #sijapp cond.if modules_TRAFFIC is "true" #
	        case Options.OPTION_CURRENCY: this.currency = value;
//			  #sijapp cond.end#
        }
	}
	
	// Return Option value (boolean)
	public synchronized void setOptionValueBool(int option,boolean value)
	{
		switch (option)
		{
			case Options.OPTION_KEEP_CONN_ALIVE: this.keepConnAlive = value;
			case Options.OPTION_DISPLAY_DATE: this.displayDate = value;
			case Options.OPTION_CL_HIDE_OFFLINE: this.clHideOffline = value; 
//		      #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
			case Options.OPTION_VIBRATOR: this.vibrator = value;
//			  #sijapp cond.end#
			case Options.OPTION_KEEPCHAT: this.keep_chat = value;
			case Options.OPTION_CP1251_HACK: this.cp1251_hack = value;
		}
	}
	
	// Return Option value (int)
	public synchronized void setOptionValueInt(int option,int value)
	{
	    switch (option)
	    {
	    	case Options.OPTION_CONN_TYPE: this.conn_type = value; 
	    	case Options.OPTION_CL_SORT_BY: this.clSortBy = value;
//		      #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
	    	case Options.OPTION_MESSAGE_SOUND: this.msgNotificationMode = value;
	    	case Options.OPTION_ONLINE_SOUND: this.onlNotificationMode = value;
//			  #sijapp cond.end#
//		      #sijapp cond.if modules_TRAFFIC is "true" #
	    	case Options.OPTION_COST_PER_PACKET: this.costPerPacket = value;
	    	case Options.OPTION_COST_PER_DAY: this.costPerDay = value;
	    	case Options.OPTION_COST_PACKET_LENGHT: this.costPacketLength = value;
//			  #sijapp cond.end#
	    }
	}
	
	// Return Option value (long)
	public synchronized void setOptionValueLong(int option,long value)
	{
	    this.onlineStatus = value;
	}

	/**************************************************************************/
	/**************************************************************************/
	/**************************************************************************/


	// Form for editing option values
	public class OptionsForm implements CommandListener
	{


		// Commands
		private Command backCommand;
		private Command saveCommand;


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
		private ChoiceGroup uiLanguageChoiceGroup;
		private ChoiceGroup displayDateChoiceGroup;
		private ChoiceGroup clSortByChoiceGroup;
		private ChoiceGroup clHideOfflineChoiceGroup;
		// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
		private ChoiceGroup msgNotificationModeChoiceGroup;
		private ChoiceGroup onlNotificationModeChoiceGroup;
		// #sijapp cond.end#
		// #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
		private TextField msgSoundFileTextField;
		private TextField onlSoundFileTextField;
		// #sijapp cond.end#
		// #sijapp cond.if target is "SIEMENS" | target is "RIM"#
		private ChoiceGroup vibratorChoiceGroup;
		// #sijapp cond.end#
		private ChoiceGroup keepChatChoiceGroup;
		private ChoiceGroup cp1251ChoiceGroup;
		// #sijapp cond.if modules_TRAFFIC is "true" #
		private TextField costPerPacketTextField;
		private TextField costPerDayTextField;
		private TextField costPacketLengthTextField;
		private TextField currencyTextField;
		// #sijapp cond.end#


		// Constructor
		public OptionsForm()
		{

			// Initialize commands
			this.backCommand = new Command(ResourceBundle.getString("jimm.res.Text", "back"), Command.BACK, 1);
			this.saveCommand = new Command(ResourceBundle.getString("jimm.res.Text", "save"), Command.SCREEN, 1);

			// Initialize options menu
			this.optionsMenu = new List(ResourceBundle.getString("jimm.res.Text", "options"), Choice.IMPLICIT);
			this.optionsMenu.append(ResourceBundle.getString("jimm.res.Text", "options_account"), null);
			this.optionsMenu.append(ResourceBundle.getString("jimm.res.Text", "options_network"), null);
			this.optionsMenu.append(ResourceBundle.getString("jimm.res.Text", "options_interface"), null);
			// #sijapp cond.if modules_TRAFFIC is "true" #
			this.optionsMenu.append(ResourceBundle.getString("jimm.res.Text", "options_cost"), null);
			// #sijapp cond.end#
			this.optionsMenu.addCommand(this.backCommand);
			this.optionsMenu.setCommandListener(this);

			// Initialize options form
			this.optionsForm = new Form(ResourceBundle.getString("jimm.res.Text", "options"));
			this.optionsForm.addCommand(this.saveCommand);
			this.optionsForm.setCommandListener(this);

			// Initialize elements (account section)
			this.uinTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "uin"), Options.this.getOptionValueString(Options.OPTION_UIN), 12, TextField.NUMERIC);
			this.passwordTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "password"),Options.this.getOptionValueString(Options.OPTION_PASSWORD), 32, TextField.PASSWORD);

			// Initialize elements (network section)
			this.srvHostTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "server_host"), Options.this.getOptionValueString(Options.OPTION_SRV_HOST), 32, TextField.ANY);
			this.srvPortTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "server_port"), Options.this.getOptionValueString(Options.OPTION_SRV_PORT), 5, TextField.NUMERIC);
			this.keepConnAliveChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "keep_conn_alive"), Choice.MULTIPLE);
			this.keepConnAliveChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
			this.keepConnAliveChoiceGroup.setSelectedIndex(0,Options.this.getOptionValueBool(Options.OPTION_KEEP_CONN_ALIVE));
			this.connTypeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "conn_type"), Choice.MULTIPLE);
			this.connTypeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "async"), null);
			if (Options.this.getOptionValueInt(Options.OPTION_CONN_TYPE) == 0)
			{
				this.connTypeChoiceGroup.setSelectedIndex(0,false);
			}
			else
			{
				this.connTypeChoiceGroup.setSelectedIndex(0,true);
			}

			// Initialize elements (interface section)
			this.uiLanguageChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "language"), Choice.EXCLUSIVE);
			for (int i = 0; i < ResourceBundle.LANG_AVAILABLE.length; i++)
			{
				this.uiLanguageChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "lang_" + ResourceBundle.LANG_AVAILABLE[i]), null);
				if (ResourceBundle.LANG_AVAILABLE[i].equals(Options.this.getOptionValueString(Options.OPTION_UI_LANGUAGE)))
				{
					this.uiLanguageChoiceGroup.setSelectedIndex(i, true);
				}
			}
			this.displayDateChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "display_date"), Choice.MULTIPLE);
			this.displayDateChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
			this.displayDateChoiceGroup.setSelectedIndex(0,Options.this.getOptionValueBool(Options.OPTION_DISPLAY_DATE));
			this.clSortByChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "sort_by"), Choice.EXCLUSIVE);
			this.clSortByChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "sort_by_status"), null);
			this.clSortByChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "sort_by_name"), null);
			this.clSortByChoiceGroup.setSelectedIndex(Options.this.getOptionValueInt(Options.OPTION_CL_SORT_BY), true);
			this.clHideOfflineChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "hide_offline"), Choice.MULTIPLE);
			this.clHideOfflineChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
			this.clHideOfflineChoiceGroup.setSelectedIndex(0, Options.this.getOptionValueBool(Options.OPTION_CL_HIDE_OFFLINE));
			this.keepChatChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "keep_chat"), Choice.MULTIPLE);
			this.keepChatChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
			this.keepChatChoiceGroup.setSelectedIndex(0, Options.this.getOptionValueBool(Options.OPTION_KEEPCHAT));
			this.cp1251ChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "cp1251"), Choice.MULTIPLE);
			this.cp1251ChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
			this.cp1251ChoiceGroup.setSelectedIndex(0, Options.this.getOptionValueBool(Options.OPTION_CP1251_HACK));
			// #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
			this.msgNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "onl_notification"), Choice.EXCLUSIVE);
			this.msgNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "no"), null);
			this.msgNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "beep"), null);
			this.msgNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "sound"), null);
			this.msgNotificationModeChoiceGroup.setSelectedIndex(Options.this.getOptionValueInt(Options.OPTION_MESSAGE_SOUND), true);
			this.msgSoundFileTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "msg_sound_file_name"), Options.this.getOptionValueString(Options.OPTION_MESSAGE_SOUNDFILE_NAME), 32, TextField.ANY);
			this.onlNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "msg_notification"), Choice.EXCLUSIVE);
			this.onlNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "no"), null);
			this.onlNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "beep"), null);
			this.onlNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "sound"), null);
			this.onlNotificationModeChoiceGroup.setSelectedIndex(Options.this.getOptionValueInt(Options.OPTION_ONLINE_SOUND), true);
			this.onlSoundFileTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "onl_sound_file_name"), Options.this.getOptionValueString(Options.OPTION_ONLINE_SOUNDFILE_NAME), 32, TextField.ANY);
			// #sijapp cond.end#
			// #sijapp cond.if target is "RIM"#
			this.msgNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "onl_notification"), Choice.EXCLUSIVE);
			this.msgNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "no"), null);
			this.msgNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "beep"), null);
			this.msgNotificationModeChoiceGroup.setSelectedIndex(Options.this.getOptionValueInt(Options.OPTION_MESSAGE_SOUND), true);
			this.onlNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "msg_notification"), Choice.EXCLUSIVE);
			this.onlNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "no"), null);
			this.onlNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "beep"), null);
			this.onlNotificationModeChoiceGroup.setSelectedIndex(Options.this.getOptionValueInt(Options.OPTION_ONLINE_SOUND), true);
			// #sijapp cond.end#
			// #sijapp cond.if target is "SIEMENS" | target is "RIM"#
			this.vibratorChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "vibration")+"?", Choice.MULTIPLE);
			this.vibratorChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
			this.vibratorChoiceGroup.setSelectedIndex(0,Options.this.getOptionValueBool(Options.OPTION_VIBRATOR));
			// #sijapp cond.end#

			// #sijapp cond.if modules_TRAFFIC is "true" #
			// Initialize elements (cost section)
			this.costPerPacketTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "cpp"), this.getString(Options.this.getOptionValueInt(Options.OPTION_COST_PER_PACKET)), 6, TextField.ANY);
			this.costPerDayTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "cpd"), this.getString(Options.this.getOptionValueInt(Options.OPTION_COST_PER_DAY)), 6, TextField.ANY);
			this.costPacketLengthTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "plength"), String.valueOf(Options.this.getOptionValueInt(Options.OPTION_COST_PACKET_LENGHT) / 1024), 4, TextField.NUMERIC);
			this.currencyTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "currency"), Options.this.getOptionValueString(Options.OPTION_CURRENCY), 4, TextField.ANY);
			// #sijapp cond.end#

		}


		// #sijapp cond.if modules_TRAFFIC is "true" #
		// Returns String value of cost value
		public String getString(int value)
		{
			String costString = "";
			String afterDot = "";
			try
			{
				if (value != 0) {
					costString = Integer.toString(value / 1000) + ".";
					afterDot = Integer.toString(value % 1000);
					while (afterDot.length() != 3)
					{
						afterDot = "0" + afterDot;
					}
					while ((afterDot.endsWith("0")) && (afterDot.length() > 2))
					{
						afterDot = afterDot.substring(0, afterDot.length() - 1);
					}
					costString = costString + afterDot;
					return costString;
				}
				else
				{
					return new String("0.0");
				}
			}
			catch (Exception e)
			{
				return new String("0.0");
			}
		}


		// Extracts the number value form String
		public int getValue(String string)
		{
			int value = 0;
			byte i = 0;
			char c = new String(".").charAt(0);
			try
			{
				for (i = 0; i < string.length(); i++)
				{
					if (c != string.charAt(i))
	 				{
						break;
					}
				}
				if (i == string.length()-1)
				{
					value = Integer.parseInt(string) * 1000;
					return (value);
				}
				else
				{
					while (c != string.charAt(i))
					{
						i++;
					}
					value = Integer.parseInt(string.substring(0, i)) * 1000;
					string = string.substring(i + 1, string.length());
					while (string.length() > 3)
					{
						string = string.substring(0, string.length() - 1);
					}
					while (string.length() < 3)
					{
						string = string + "0";
					}
					value = value + Integer.parseInt(string);
					return value;
				}
			}
			catch (Exception e)
			{
				System.out.println("3: "+value);
				return (0);
			}
		}
		// #sijapp cond.end#


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
			if (c == List.SELECT_COMMAND)
			{

				// Delete all items
				while (this.optionsForm.size() > 0) { this.optionsForm.delete(0); }

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
						this.optionsForm.append(this.connTypeChoiceGroup);
						break;
					case 2:
						this.optionsForm.append(this.uiLanguageChoiceGroup);
						this.optionsForm.append(this.displayDateChoiceGroup);
						this.optionsForm.append(this.clSortByChoiceGroup);
						this.optionsForm.append(this.clHideOfflineChoiceGroup);
						this.optionsForm.append(this.keepChatChoiceGroup);
						this.optionsForm.append(this.cp1251ChoiceGroup);
						// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
						this.optionsForm.append(this.msgNotificationModeChoiceGroup);
						this.optionsForm.append(this.onlNotificationModeChoiceGroup);
						// #sijapp cond.end#
						// #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
						this.optionsForm.append(this.msgSoundFileTextField);
						this.optionsForm.append(this.onlSoundFileTextField);
						// #sijapp cond.end#
						// #sijapp cond.if target is "SIEMENS" | target is "RIM"#
						this.optionsForm.append(this.vibratorChoiceGroup);
						// #sijapp cond.end#
						break;
					// #sijapp cond.if modules_TRAFFIC is "true" #
					case 3:
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

				// Active MM/CL
				if (Jimm.jimm.getIcqRef().isConnected())
				{
					Jimm.jimm.getContactListRef().activate();
				}
				else
				{
					Jimm.jimm.getMainMenuRef().activate();
				}

			}

			// Look for save command
			else if (c == this.saveCommand)
			{

				// Save values, depending on selected option menu item
				switch (this.optionsMenu.getSelectedIndex())
				{
					case 0:
						Options.this.setOptionValueString(Options.OPTION_UIN,this.uinTextField.getString());
						Options.this.setOptionValueString(Options.OPTION_PASSWORD,this.passwordTextField.getString());
						break;
					case 1:
					    Options.this.setOptionValueString(Options.OPTION_SRV_HOST,this.srvHostTextField.getString());
					    Options.this.setOptionValueString(Options.OPTION_SRV_PORT,this.srvPortTextField.getString());
						Options.this.setOptionValueBool(Options.OPTION_KEEP_CONN_ALIVE,this.keepConnAliveChoiceGroup.isSelected(0));
						if (this.connTypeChoiceGroup.isSelected(0))
						{
							Options.this.setOptionValueInt(Options.OPTION_CONN_TYPE,1);
						}
						else
						{
						    Options.this.setOptionValueInt(Options.OPTION_CONN_TYPE,0);
						}
						break;
					case 2:
						Options.this.setOptionValueString(Options.OPTION_UI_LANGUAGE,ResourceBundle.LANG_AVAILABLE[this.uiLanguageChoiceGroup.getSelectedIndex()]);
						Options.this.setOptionValueBool(Options.OPTION_DISPLAY_DATE,this.displayDateChoiceGroup.isSelected(0));
						if (this.clHideOfflineChoiceGroup.isSelected(0))
						{
							Options.this.setOptionValueInt(Options.OPTION_CL_SORT_BY,0);
						}
						else
	  					{
						    Options.this.setOptionValueInt(Options.OPTION_CL_SORT_BY,this.clSortByChoiceGroup.getSelectedIndex());
						}
						Options.this.setOptionValueBool(Options.OPTION_CL_HIDE_OFFLINE,this.clHideOfflineChoiceGroup.isSelected(0));
						Options.this.setOptionValueBool(Options.OPTION_KEEPCHAT,this.keepChatChoiceGroup.isSelected(0));
						Options.this.setOptionValueBool(Options.OPTION_CP1251_HACK,this.cp1251ChoiceGroup.isSelected(0));
						// #sijapp cond.if target is "SIEMENS" | target is "RIM"#
						Options.this.setOptionValueBool(Options.OPTION_VIBRATOR,this.vibratorChoiceGroup.isSelected(0));
						// #sijapp cond.end#
						// #sijapp cond.if target is "SIEMENS" | target is "MIDP2" | target is "RIM"#
						Options.this.setOptionValueInt(Options.OPTION_MESSAGE_SOUND,this.msgNotificationModeChoiceGroup.getSelectedIndex());
						Options.this.setOptionValueInt(Options.OPTION_ONLINE_SOUND,this.onlNotificationModeChoiceGroup.getSelectedIndex());
						// #sijapp cond.end#
						// #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
						Options.this.setOptionValueString(Options.OPTION_MESSAGE_SOUNDFILE_NAME,this.msgSoundFileTextField.getString());
						Options.this.setOptionValueString(Options.OPTION_ONLINE_SOUNDFILE_NAME,this.onlSoundFileTextField.getString());
						// #sijapp cond.end#
						Jimm.jimm.getContactListRef().sortAll();
						break;
					// #sijapp cond.if modules_TRAFFIC is "true" #
					case 3:
						Options.this.setOptionValueInt(Options.OPTION_COST_PER_PACKET,this.getValue(this.costPerPacketTextField.getString()));
						this.costPerPacketTextField.setString(this.getString(Options.this.getOptionValueInt(Options.OPTION_COST_PER_PACKET)));
						Options.this.setOptionValueInt(Options.OPTION_COST_PER_DAY,this.getValue(this.costPerDayTextField.getString()));
						this.costPerDayTextField.setString(this.getString(Options.this.getOptionValueInt(Options.OPTION_COST_PER_DAY)));
						Options.this.setOptionValueInt(Options.OPTION_COST_PACKET_LENGHT,Integer.parseInt(this.costPacketLengthTextField.getString()) * 1024);
						Options.this.setOptionValueString(Options.OPTION_CURRENCY,this.currencyTextField.getString());
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
					Jimm.jimm.getContactListRef().refreshList(true, false, Integer.MAX_VALUE);
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
