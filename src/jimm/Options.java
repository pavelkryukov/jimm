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
 File: src/jimm/Options.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


/********************************************************************************
 Current record store format:

 Record #1: VERSION                    (UTF-8)
 Record #2: UIN                        (UTF-8)
            SRV_HOST                   (UTF-8)
            SRV_PORT                   (UTF-8)
            KEEP_CONN_ALIVE            (Boolean)
            CONN_TYPE				   (Integer)
            UI_LANGUAGE                (UTF-8)
            DISPLAY_DATE               (Boolean)
            CL_SORT_BY                 (Integer)
            CL_HIDE_OFFLINE            (Boolean)
            MSG_NOTIFICATION_MODE      (Integer)
            ONL_NOTIFICATION_MODE      (Integer)
            MSG_NOTIFY_SOUND_FILE	   (UTF-8)
            ONL_NOTIFY_SOUND_FILE	   (UTF-8)
            VIBRATOR                   (Boolean)
            KEEP_CHAT				   (Boolean)
            USE_CP1251_HACK			   (Boolean)
            COST_PER_PACKET            (Integer)
            COST_PER_DAY               (Integer)
            COST_PACKET_LENGTH         (Integer)
            CURRENCY                   (UTF-8)
            DISPLAY_ADVERTISEMENT_MODE (Integer)
            ONLINE_STATUS              (Long)
 Record #3: PASSWORD                   (Somewhat encrypted UTF-8)
 *******************************************************************************/


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
//import javax.microedition.rms.RecordStoreNotFoundException;


public class Options
{


  // Default values
  public static final String DEFAULT_SRV_HOST = "login.icq.com";
  public static final String DEFAULT_SRV_PORT = "5190";
  public static final boolean DEFAULT_KEEP_CONN_ALIVE = true;
  public static final int DEFAULT_CONN_TYPE = 0;
  public static final String DEFAULT_UI_LANGUAGE = ResourceBundle.LANG_AVAILABLE[0];
  public static final boolean DEFAULT_DISPLAY_DATE = false;
  public static final int DEFAULT_CL_SORT_BY = 0;
  public static final boolean  DEFAULT_CL_HIDE_OFFLINE = false;
  // #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
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
  // #sijapp cond.if target is "SIEMENS"#
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
  public static final int DEFAULT_DISPLAY_ADVERTISEMENT = 0;
  public static final long DEFAULT_ONLINE_STATUS = ContactList.STATUS_ONLINE;


  /****************************************************************************/


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

  // #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
  // Section interface, message notification mode
  private int msgNotificationMode;
  
  // Section interface, online notification mode
  private int onlNotificationMode;

  // Section interface, online soundfile name
  private String onlSoundFileName;
  
  //Section interface, message soundfile name
  private String msgSoundFileName;

  // #sijapp cond.end#
  //#sijapp cond.if target is "SIEMENS"#
  // Section interface, vibration enabled/disabled
  private boolean vibrator;
  // #sijapp cond.end#

  // Section interface, keep the chat history enabled/disabled
  private boolean keep_chat;
  
  //Section interface, use the cp1251 hack?
 private boolean cp1251_hack;

 //#sijapp cond.if modules_TRAFFIC is "true" #
  // Section cost, cost per packet
  private int costPerPacket;

  // Section cost, cost per day
  private int costPerDay;

  // Section cost, length of packet to be charged in bytes
  private int costPacketLength;

  // Section cost, name or symbol of currency
  private String currency;
//#sijapp cond.end#

  // Section other, display advertisement mode
  private int displayAdvertisementMode;


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
      ResourceBundle.setCurrUiLanguage(this.getUiLanguage());
    }
    // Use default values if loading option values from record store failed
    catch (Exception e)
    {
      this.setUin("");
      this.setPassword("");
      this.setSrvHost(Options.DEFAULT_SRV_HOST);
      this.setSrvPort(Options.DEFAULT_SRV_PORT);
      this.setKeepConnAlive(Options.DEFAULT_KEEP_CONN_ALIVE);
      this.setConnType(Options.DEFAULT_CONN_TYPE);
      this.setUiLanguage(Options.DEFAULT_UI_LANGUAGE);
      this.setDisplayDate(Options.DEFAULT_DISPLAY_DATE);
      this.setClSortBy(Options.DEFAULT_CL_SORT_BY);
      this.setClHideOffline(Options.DEFAULT_CL_HIDE_OFFLINE);
      // #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
      this.setNotificationMode(Options.DEFAULT_MSG_NOTIFICATION,ContactList.SOUND_TYPE_MESSAGE);
      this.setNotificationMode(Options.DEFAULT_ONL_NOTIFICATION,ContactList.SOUND_TYPE_ONLINE);
      this.setSoundFileName(Options.DEFAULT_MSG_NOTIFY_SOUND_FILE,ContactList.SOUND_TYPE_MESSAGE);
      this.setSoundFileName(Options.DEFAULT_ONL_NOTIFY_SOUND_FILE,ContactList.SOUND_TYPE_ONLINE);
//	  #sijapp cond.end#
//	  #sijapp cond.if target is "SIEMENS"#
      this.setVibrator(Options.DEFAULT_VIBRATOR);
      // #sijapp cond.end#
      this.setKeepChat(Options.DEFAULT_KEEP_CHAT);
      this.setCP1251Hack(Options.DEFAULT_CP1251_HACK);
//	  #sijapp cond.if modules_TRAFFIC is "true" #
      this.setCostPerPacket(Options.DEFAULT_COST_PER_PACKET);
      this.setCostPerDay(Options.DEFAULT_COST_PER_DAY);
      this.setCostPacketLength(Options.DEFAULT_COST_PACKET_LENGTH);
      this.setCurrency(Options.DEFAULT_CURRENCY);
//	  #sijapp cond.end#
      this.setDisplayAdvertisementMode(Options.DEFAULT_DISPLAY_ADVERTISEMENT);
      this.setOnlineStatus(Options.DEFAULT_ONLINE_STATUS);
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
    this.setUin(dis.readUTF());
    this.setSrvHost(dis.readUTF());
    this.setSrvPort(dis.readUTF());
    this.setKeepConnAlive(dis.readBoolean());
    this.setConnType(dis.readInt());
    this.setUiLanguage(dis.readUTF());
    this.setDisplayDate(dis.readBoolean());
    this.setClSortBy(dis.readInt());
    this.setClHideOffline(dis.readBoolean());
    // #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
    this.setNotificationMode(dis.readInt(),ContactList.SOUND_TYPE_MESSAGE);
    this.setNotificationMode(dis.readInt(),ContactList.SOUND_TYPE_ONLINE);
    this.setSoundFileName(dis.readUTF(),ContactList.SOUND_TYPE_MESSAGE);
    this.setSoundFileName(dis.readUTF(),ContactList.SOUND_TYPE_ONLINE);
	// #sijapp cond.else#
	 dis.readInt();
	 dis.readBoolean();
	// #sijapp cond.end#
	//	#sijapp cond.if target is "SIEMENS"#
    this.setVibrator(dis.readBoolean());
	//	#sijapp cond.else#
	dis.readBoolean();
    // #sijapp cond.end#
    this.setKeepChat(dis.readBoolean());
    this.setCP1251Hack(dis.readBoolean());
	//	#sijapp cond.if modules_TRAFFIC is "true" #
    this.setCostPerPacket(dis.readInt());
    this.setCostPerDay(dis.readInt());
    this.setCostPacketLength(dis.readInt());
    this.setCurrency(dis.readUTF());
	//	#sijapp cond.else#
	dis.readInt();
	dis.readInt();
	dis.readInt();
	dis.readUTF();
	//	#sijapp cond.end#
    this.setDisplayAdvertisementMode(dis.readInt());
    this.setOnlineStatus(dis.readLong());

    // Get password from 3rd record
    buf = account.getRecord(3);
    buf = Util.decipherPassword(buf);
    bais = new ByteArrayInputStream(buf);
    dis = new DataInputStream(bais);
    this.setPassword(dis.readUTF());

    // Close record store
    account.closeRecordStore();

	if (this.clHideOffline)
		Options.this.setClSortBy(0);
	else
	Options.this.setClSortBy(this.getClSortBy());

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
    dos.writeUTF(this.getUin());
    dos.writeUTF(this.getSrvHost());
    dos.writeUTF(this.getSrvPort());
    dos.writeBoolean(this.isKeepConnAlive());
    dos.writeInt(this.getConnType());
    dos.writeUTF(this.getUiLanguage());
    dos.writeBoolean(this.isDisplayDate());
    dos.writeInt(this.getClSortBy());
    dos.writeBoolean(this.isClHideOffline());
    // #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
    dos.writeInt(this.getNotificationMode(ContactList.SOUND_TYPE_MESSAGE));
    dos.writeInt(this.getNotificationMode(ContactList.SOUND_TYPE_ONLINE));
    dos.writeUTF(this.getSoundFileName(ContactList.SOUND_TYPE_MESSAGE));
    dos.writeUTF(this.getSoundFileName(ContactList.SOUND_TYPE_ONLINE));
	// #sijapp cond.else#
	dos.writeInt(0);
	dos.writeBoolean(false);
	//	#sijapp cond.end#
	//	#sijapp cond.if target is "SIEMENS"#
    dos.writeBoolean(this.isVibrator());
    // #sijapp cond.else#
    dos.writeBoolean(false);
    // #sijapp cond.end#
    dos.writeBoolean(this.keepChat());
    dos.writeBoolean(this.cp1251Hack());
	//	#sijapp cond.if modules_TRAFFIC is "true" #
    dos.writeInt(this.getCostPerPacket());
    dos.writeInt(this.getCostPerDay());
    dos.writeInt(this.getCostPacketLength());
    dos.writeUTF(this.getCurrency());
	//	#sijapp cond.else#
	dos.writeInt(0);
	   dos.writeInt(0);
	   dos.writeInt(0);
	   dos.writeUTF(" ");
//	#sijapp cond.end#
    dos.writeInt(this.getDisplayAdvertisementMode());
    dos.writeLong(this.getOnlineStatus());
    buf = baos.toByteArray();
    account.setRecord(2, buf, 0, buf.length);

    // Save password into 3rd record
    baos = new ByteArrayOutputStream();
    dos = new DataOutputStream(baos);
    dos.writeUTF(this.getPassword());
    buf = baos.toByteArray();
    buf = Util.decipherPassword(buf);
    account.setRecord(3, buf, 0, buf.length);

    // Close record store
    account.closeRecordStore();

  }


  // Return UIN
  public synchronized String getUin()
  {
    return (new String(this.uin));
  }

  // Set UIN
  public synchronized void setUin(String uin)
  {
    this.uin = new String(uin);
  }


  // Return password
  public synchronized String getPassword()
  {
    return (new String(this.password));
  }

  // Set password
  public synchronized void setPassword(String password)
  {
    this.password = new String(password);
  }


  // Return hostname of the login server
  public synchronized String getSrvHost()
  {
    return (new String(this.srvHost));
  }

  // Set hostname of the login server
  public synchronized void setSrvHost(String srvHost)
  {
    this.srvHost = new String(srvHost);
  }


  // Return port of the login server
  public synchronized String getSrvPort()
  {
    return (new String(this.srvPort));
  }

  // Set port of the login server
  public synchronized void setSrvPort(String srvPort)
  {
    this.srvPort = new String(srvPort);
  }


  // Return keep connection alive flag
  public synchronized boolean isKeepConnAlive()
  {
    return (this.keepConnAlive);
  }

  // Set connection tye
  public synchronized void setConnType(int connType)
  {
    this.conn_type = connType;
  }
  
  // Return connection type
  public synchronized int getConnType()
  {
	return (this.conn_type);
  }

  // Set keep connection alive flag
  public synchronized void setKeepConnAlive(boolean keepConnAlive)
  {
	this.keepConnAlive = keepConnAlive;
  }


  // Return UI language
  public synchronized String getUiLanguage()
  {
    return (new String(this.uiLanguage));
  }

  // Set UI language
  public synchronized void setUiLanguage(String uiLanguage)
  {
    this.uiLanguage = new String(uiLanguage);
  }


  // Return date display flag
  public synchronized boolean isDisplayDate()
  {
    return (this.displayDate);
  }

  // Set date display flag
  public synchronized void setDisplayDate(boolean displayDate)
  {
    this.displayDate = displayDate;
  }


  // Return cl sort method
  public synchronized int getClSortBy() {
    return (this.clSortBy);
  }

  // Set cl sort method
  public synchronized void setClSortBy(int clSortBy) {
    this.clSortBy = clSortBy;
  }


  // Return show/hide offline contacts flag
  public synchronized boolean isClHideOffline() {
    return (this.clHideOffline);
  }

  // Set show/hide offline contacts flag
  public synchronized void setClHideOffline(boolean clHideOffline) {
    this.clHideOffline = clHideOffline;
  }


  // #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
  // Return message notification mode
  public synchronized int getNotificationMode(int notType)
  {
    switch(notType)
    {
    case ContactList.SOUND_TYPE_MESSAGE:
        return (this.msgNotificationMode);
    case ContactList.SOUND_TYPE_ONLINE:
        return (this.onlNotificationMode);
    }
      return (this.msgNotificationMode);
  }

  // Set message notification mode
  public synchronized void setNotificationMode(int NotificationMode,int notType)
  {
    switch(notType)
    {
    case ContactList.SOUND_TYPE_MESSAGE:
        this.msgNotificationMode = NotificationMode;
    case ContactList.SOUND_TYPE_ONLINE:
        this.onlNotificationMode = NotificationMode;
    }
      
  }

  //Return sound file name
   public synchronized String getSoundFileName(int notType)
  {
    switch(notType)
    {
    case ContactList.SOUND_TYPE_MESSAGE:
        return (this.msgSoundFileName);
    case ContactList.SOUND_TYPE_ONLINE:
        return (this.onlSoundFileName);
    default:
        return (this.onlSoundFileName);
    }
       
  }

  //  Set soundfile name
  public synchronized void setSoundFileName(String soundFile,int notType)
  {
    switch (notType)
    {
    case ContactList.SOUND_TYPE_MESSAGE:
        this.msgSoundFileName = soundFile;
    case ContactList.SOUND_TYPE_ONLINE:
        this.onlSoundFileName = soundFile;
    }
      
  }

//#sijapp cond.end#
//#sijapp cond.if target is "SIEMENS"#
  //  Return vibrator
  public synchronized boolean isVibrator()
  {
    return (this.vibrator);
  }

  //  Set vibrator
  public synchronized void setVibrator(boolean vibrator)
  {
    this.vibrator = vibrator;
  }
  // #sijapp cond.end#

  //Return keepChat
  public synchronized boolean keepChat() {
	  return (this.keep_chat);
  }

  //  Set keepChat
  public synchronized void setKeepChat(boolean keep_chat) {
      this.keep_chat = keep_chat;
  }
  
  //Return cp1261Hack
  public synchronized boolean cp1251Hack() {
	  return (this.cp1251_hack);
  }

  //  Set cp1251_hack
  public synchronized void setCP1251Hack(boolean cp1251_hack) {
	  this.cp1251_hack = cp1251_hack;
  }

//#sijapp cond.if modules_TRAFFIC is "true" #
  // Get cost per packet
  public synchronized int getCostPerPacket()
  {
    return (this.costPerPacket);
  }

  // Set cost per packet
  public synchronized void setCostPerPacket(int costPerPacket)
  {

    this.costPerPacket = costPerPacket;
  }


  // Get cost per day
  public synchronized int getCostPerDay()
  {
    return (this.costPerDay);
  }

  // Set cost per day
  public synchronized void setCostPerDay(int costPerDay)
  {
    this.costPerDay = costPerDay;
  }


  // Get length of packet to be charged in bytes
  public synchronized int getCostPacketLength()
  {
    return (this.costPacketLength);
  }

  // Set length of packet to be charged in bytes
  public synchronized void setCostPacketLength(int costPacketLength)
  {
    this.costPacketLength = costPacketLength;
  }


  // Get name or symbol of currency
  public synchronized String getCurrency()
  {
    return (this.currency);
  }

  // Set name or symbol of currency
  public synchronized void setCurrency(String currency)
  {
    this.currency = currency;
  }
//#sijapp cond.end#

  // Return display advertisement mode
  public synchronized int getDisplayAdvertisementMode()
  {
    return (this.displayAdvertisementMode);
  }

  // Set display advertisement mode
  public synchronized void setDisplayAdvertisementMode(int displayAdvertisementMode)
  {
    this.displayAdvertisementMode = displayAdvertisementMode;
  }


  // Return current online status
  public synchronized long getOnlineStatus()
  {
    return (this.onlineStatus);
  }

  // Set current online status
  public synchronized void setOnlineStatus(long onlineStatus)
  {
    this.onlineStatus = onlineStatus;
  }


  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/


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
    // #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
    private ChoiceGroup msgNotificationModeChoiceGroup;
    private ChoiceGroup onlNotificationModeChoiceGroup;
    private TextField msgSoundFileTextField;
    private TextField onlSoundFileTextField;
	// #sijapp cond.end#
//	#sijapp cond.if target is "SIEMENS"#
    private ChoiceGroup vibratorChoiceGroup;
    // #sijapp cond.end#
	private ChoiceGroup keepChatChoiceGroup;
	private ChoiceGroup cp1251ChoiceGroup;
//	#sijapp cond.if modules_TRAFFIC is "true" #
    private TextField costPerPacketTextField;
    private TextField costPerDayTextField;
    private TextField costPacketLengthTextField;
    private TextField currencyTextField;
//	#sijapp cond.end#
    private ChoiceGroup displayAdvertisementModeChoiceGroup;


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
//	  #sijapp cond.if modules_TRAFFIC is "true" #
      this.optionsMenu.append(ResourceBundle.getString("jimm.res.Text", "options_cost"), null);
//	  #sijapp cond.end#
      this.optionsMenu.append(ResourceBundle.getString("jimm.res.Text", "options_other"), null);
      this.optionsMenu.addCommand(this.backCommand);
      this.optionsMenu.setCommandListener(this);
      // Initialize options form
      this.optionsForm = new Form(ResourceBundle.getString("jimm.res.Text", "options"));
      this.optionsForm.addCommand(this.saveCommand);
      this.optionsForm.setCommandListener(this);
      // Initialize elements (account section)
      this.uinTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "uin"), Options.this.getUin(), 9, TextField.NUMERIC);
      this.passwordTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "password"), Options.this.getPassword(), 8, TextField.PASSWORD);

      // Initialize elements (network section)
      this.srvHostTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "server_host"), Options.this.getSrvHost(), 32, TextField.ANY);
      this.srvPortTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "server_port"), Options.this.getSrvPort(), 5, TextField.NUMERIC);
      this.keepConnAliveChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "keep_conn_alive"), Choice.MULTIPLE);
      this.keepConnAliveChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
      this.keepConnAliveChoiceGroup.setSelectedIndex(0,Options.this.isKeepConnAlive());
	  this.connTypeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "conn_type"), Choice.MULTIPLE);
	  this.connTypeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "async"), null);
	  if (Options.this.getConnType() == 0)
	  	this.connTypeChoiceGroup.setSelectedIndex(0,false);
	  else
	  	this.connTypeChoiceGroup.setSelectedIndex(0,true);

      // Initialize elements (interface section)
      this.uiLanguageChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "language"), Choice.EXCLUSIVE);
      for (int i = 0; i < ResourceBundle.LANG_AVAILABLE.length; i++)
      {
        this.uiLanguageChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "lang_" + ResourceBundle.LANG_AVAILABLE[i]), null);
        if (ResourceBundle.LANG_AVAILABLE[i].equals(Options.this.getUiLanguage()))
        {
          this.uiLanguageChoiceGroup.setSelectedIndex(i, true);
        }
      }
      this.displayDateChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "display_date"), Choice.MULTIPLE);
      this.displayDateChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
      this.displayDateChoiceGroup.setSelectedIndex(0,Options.this.isDisplayDate());
      this.clSortByChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "sort_by"), Choice.EXCLUSIVE);
      this.clSortByChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "sort_by_status"), null);
      this.clSortByChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "sort_by_name"), null);
      this.clSortByChoiceGroup.setSelectedIndex(Options.this.getClSortBy(), true);
      this.clHideOfflineChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "hide_offline"), Choice.MULTIPLE);
      this.clHideOfflineChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
      this.clHideOfflineChoiceGroup.setSelectedIndex(0, Options.this.isClHideOffline());
	  this.keepChatChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "keep_chat"), Choice.MULTIPLE);
	  this.keepChatChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
	  this.keepChatChoiceGroup.setSelectedIndex(0, Options.this.keepChat());
	  this.cp1251ChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "cp1251"), Choice.MULTIPLE);	
	  this.cp1251ChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
	  this.cp1251ChoiceGroup.setSelectedIndex(0, Options.this.cp1251Hack());

      // #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
      this.msgNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "onl_notification"), Choice.EXCLUSIVE);
      this.msgNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "no"), null);
      this.msgNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "beep"), null);
      this.msgNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "sound"), null);
      this.msgNotificationModeChoiceGroup.setSelectedIndex(Options.this.getNotificationMode(ContactList.SOUND_TYPE_MESSAGE), true);
	  this.msgSoundFileTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "msg_sound_file_name"), Options.this.getSoundFileName(ContactList.SOUND_TYPE_MESSAGE), 32, TextField.ANY);
	  
      this.onlNotificationModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "msg_notification"), Choice.EXCLUSIVE);
      this.onlNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "no"), null);
      this.onlNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "beep"), null);
      this.onlNotificationModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "sound"), null);
      this.onlNotificationModeChoiceGroup.setSelectedIndex(Options.this.getNotificationMode(ContactList.SOUND_TYPE_ONLINE), true);
	  this.onlSoundFileTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "onl_sound_file_name"), Options.this.getSoundFileName(ContactList.SOUND_TYPE_ONLINE), 32, TextField.ANY);
//	  #sijapp cond.end#
//	  #sijapp cond.if target is "SIEMENS"#
      this.vibratorChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "vibration")+"?", Choice.MULTIPLE);
      this.vibratorChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
      this.vibratorChoiceGroup.setSelectedIndex(0,Options.this.isVibrator());
      // #sijapp cond.end#
//	  #sijapp cond.if modules_TRAFFIC is "true" #
      // Initialize elements (cost section)
      this.costPerPacketTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "cpp"), this.getString(Options.this.getCostPerPacket()), 6, TextField.ANY);
      this.costPerDayTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "cpd"), this.getString(Options.this.getCostPerDay()), 6, TextField.ANY);
      this.costPacketLengthTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "plength"), String.valueOf(Options.this.getCostPacketLength() / 1024), 4, TextField.NUMERIC);
      this.currencyTextField = new TextField(ResourceBundle.getString("jimm.res.Text", "currency"), Options.this.getCurrency(), 4, TextField.ANY);
//	  #sijapp cond.end#
      // Initialize elements (other section)
      this.displayAdvertisementModeChoiceGroup = new ChoiceGroup(ResourceBundle.getString("jimm.res.Text", "display_advertisement"), Choice.EXCLUSIVE);
      this.displayAdvertisementModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "yes"), null);
      this.displayAdvertisementModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "once_a_session"), null);
      this.displayAdvertisementModeChoiceGroup.append(ResourceBundle.getString("jimm.res.Text", "no"), null);
      this.displayAdvertisementModeChoiceGroup.setSelectedIndex(Options.this.getDisplayAdvertisementMode(), true);
    }

	//	  #sijapp cond.if modules_TRAFFIC is "true" #

	// Returns String value of cost value
	public String getString(int value) {
		String costString = "";
		String afterDot = "";
		try {
			if (value != 0) {
				costString = Integer.toString(value / 1000) + ".";
				afterDot = Integer.toString(value % 1000);
				while (afterDot.length() != 3)
					afterDot = "0" + afterDot;
				while ((afterDot.endsWith("0")) && (afterDot.length() > 2)) {
					afterDot = afterDot.substring(0, afterDot.length() - 1);
				}
				costString = costString + afterDot;
				return costString;
			}
			else
				return new String("0.0");

		}
		catch (Exception e) {
			return new String("0.0");
		}
	}

	// Extracts the number value form String
	public int getValue(String string) {
		int value = 0;
		byte i = 0;
		char c = new String(".").charAt(0);
		try {
			for (i = 0; i < string.length(); i++) {
				if (c != string.charAt(i))
					break;
			}
			if (i == string.length()-1) {
				value = Integer.parseInt(string) * 1000;
				return (value);
			}
			else {

				while (c != string.charAt(i)) {
					i++;
				}
				value = Integer.parseInt(string.substring(0, i)) * 1000;
				string = string.substring(i + 1, string.length());
				while (string.length() > 3) {
					string = string.substring(0, string.length() - 1);
				}
				while (string.length() < 3) {
					string = string + "0";
				}
				value = value + Integer.parseInt(string);
				return value;

			}
		}
		catch (Exception e) {
			System.out.println("3: "+value);
			return (0);
		}
	}

//	#sijapp cond.end#

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
      if (c == List.SELECT_COMMAND) {

        // Delete all items
        while (this.optionsForm.size() > 0) { this.optionsForm.delete(0); }

        // Add elements, depending on selected option menu item
        switch (this.optionsMenu.getSelectedIndex()) {
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
            // #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
            this.optionsForm.append(this.msgNotificationModeChoiceGroup);
            this.optionsForm.append(this.msgSoundFileTextField);
            this.optionsForm.append(this.onlNotificationModeChoiceGroup);
            this.optionsForm.append(this.onlSoundFileTextField);
//			#sijapp cond.end#
//			#sijapp cond.if target is "SIEMENS"#
            this.optionsForm.append(this.vibratorChoiceGroup);
            // #sijapp cond.end#
            break;
//			#sijapp cond.if modules_TRAFFIC is "true" #
          case 3:
            this.optionsForm.append(this.costPerPacketTextField);
            this.optionsForm.append(this.costPerDayTextField);
            this.optionsForm.append(this.costPacketLengthTextField);
            this.optionsForm.append(this.currencyTextField);
            break;
          case 4:
            this.optionsForm.append(this.displayAdvertisementModeChoiceGroup);
            break;
//			#sijapp cond.else#
			case 3:
			  this.optionsForm.append(this.displayAdvertisementModeChoiceGroup);
			  break;
//			#sijapp cond.end#

        }

        // Activate options form
        Jimm.display.setCurrent(this.optionsForm);

      }

      // Look for back command
      else if (c == this.backCommand) {

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
      else if (c == this.saveCommand) {

        // Save values, depending on selected option menu item
        switch (this.optionsMenu.getSelectedIndex())
        {
          case 0:
            Options.this.setUin(this.uinTextField.getString());
            Options.this.setPassword(this.passwordTextField.getString());
            break;
          case 1:
            Options.this.setSrvHost(this.srvHostTextField.getString());
            Options.this.setSrvPort(this.srvPortTextField.getString());
            Options.this.setKeepConnAlive(this.keepConnAliveChoiceGroup.isSelected(0));
			if (this.connTypeChoiceGroup.isSelected(0))
				Options.this.setConnType(1);
			else
				Options.this.setConnType(0);
            break;
          case 2:
            Options.this.setUiLanguage(ResourceBundle.LANG_AVAILABLE[this.uiLanguageChoiceGroup.getSelectedIndex()]);
            Options.this.setDisplayDate(this.displayDateChoiceGroup.isSelected(0));

            if (this.clHideOfflineChoiceGroup.isSelected(0))
            	Options.this.setClSortBy(0);
            else
			Options.this.setClSortBy(this.clSortByChoiceGroup.getSelectedIndex());

            Options.this.setClHideOffline(this.clHideOfflineChoiceGroup.isSelected(0));
            Options.this.setKeepChat(this.keepChatChoiceGroup.isSelected(0));
			Options.this.setCP1251Hack(this.keepChatChoiceGroup.isSelected(0));
            
//			#sijapp cond.if target is "SIEMENS"#
            Options.this.setVibrator(this.vibratorChoiceGroup.isSelected(0));
//			#sijapp cond.end#
//			#sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
            Options.this.setSoundFileName(this.msgSoundFileTextField.getString(),ContactList.SOUND_TYPE_MESSAGE);
            Options.this.setNotificationMode(this.msgNotificationModeChoiceGroup.getSelectedIndex(),ContactList.SOUND_TYPE_MESSAGE);
            
            Options.this.setSoundFileName(this.onlSoundFileTextField.getString(),ContactList.SOUND_TYPE_ONLINE);
            Options.this.setNotificationMode(this.onlNotificationModeChoiceGroup.getSelectedIndex(),ContactList.SOUND_TYPE_ONLINE);
            // #sijapp cond.end#
            Jimm.jimm.getContactListRef().sortAll();
            break;
//			#sijapp cond.if modules_TRAFFIC is "true" #
          case 3:
            Options.this.setCostPerPacket(this.getValue(this.costPerPacketTextField.getString()));
			this.costPerPacketTextField.setString(this.getString(Options.this.getCostPerPacket()));

            Options.this.setCostPerDay(this.getValue(this.costPerDayTextField.getString()));
			this.costPerDayTextField.setString(this.getString(Options.this.getCostPerDay()));

            Options.this.setCostPacketLength(Integer.parseInt(this.costPacketLengthTextField.getString()) * 1024);
            Options.this.setCurrency(this.currencyTextField.getString());
            break;
          case 4:
            Options.this.setDisplayAdvertisementMode(this.displayAdvertisementModeChoiceGroup.getSelectedIndex());
            break;
//			#sijapp cond.else#
			case 3:
			  Options.this.setDisplayAdvertisementMode(this.displayAdvertisementModeChoiceGroup.getSelectedIndex());
			  break;
//			#sijapp cond.end#

        }
        try
        {
          Options.this.save();
        }
        catch (Exception e) {
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
