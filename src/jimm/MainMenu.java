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
 File: src/jimm/MainMenu.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDletStateChangeException;

import jimm.comm.Icq;
import jimm.comm.SearchAction;
import jimm.comm.Action;
import jimm.comm.UpdateContactListAction;
import jimm.util.ResourceBundle;

public class MainMenu implements CommandListener
{
	private static final int TAG_EXIT = 1;
	private static final int TAG_RENAME_GROUPS = 2;
	private static final int TAG_DELETE_GROUPS = 3;
	private static final int TAG_CL = 4;

	private static MainMenu _this;

	/* Static constants for menu actios */
	private static final int MENU_CONNECT = 1;
	private static final int MENU_DISCONNECT = 2;
	private static final int MENU_LIST = 3;
	private static final int MENU_OPTIONS = 4;
	private static final int MENU_TRAFFIC = 5;
	private static final int MENU_KEYLOCK = 6;
	private static final int MENU_STATUS = 7;
	private static final int MENU_GROUPS = 8;
	private static final int MENU_ABOUT = 9;
	private static final int MENU_MINIMIZE = 10;
	private static final int MENU_SOUND = 11;
	private static final int MENU_EXIT = 12; /* Exit has to be biggest element cause it also marks the size */

	/* Abort command */
	private static Command backCommand = new Command(ResourceBundle.getString("back"), Command.BACK, 1);

	/* Send command */
	private static Command sendCommand = new Command(ResourceBundle.getString("send"), Command.OK, 1);

	/* Select command */
	private static Command selectCommand = new Command(ResourceBundle.getString("select"), Command.OK, 1);

	// #sijapp cond.if target is "MOTOROLA" #
	private static Command exitCommand = new Command(ResourceBundle.getString("exit_button"), Command.EXIT, 1);
	//#sijapp cond.end#

	/* List for selecting a online status */
	private static List statusList;
	
	private static List groupActList;
	
	/////////////////////////////////////////////////////////////////
	private static final int STATUS_NONE         = 0;
	private static final int STATUS_ADD_CONTACT  = 1;
	private static final int STATUS_ADD_GROUP    = 2;
	private static final int STATUS_RENAME_GROUP = 3;
	private static int status = STATUS_NONE;

	/* Initializer */
	static
	{
		MainMenu.statusList = new List(ResourceBundle.getString("set_status"), List.IMPLICIT);
		MainMenu.statusList.append(ResourceBundle.getString("status_online"), ContactList.statusOnlineImg);
		MainMenu.statusList.append(ResourceBundle.getString("status_chat"), ContactList.statusChatImg);
		MainMenu.statusList.append(ResourceBundle.getString("status_away"), ContactList.statusAwayImg);
		MainMenu.statusList.append(ResourceBundle.getString("status_na"), ContactList.statusNaImg);
		MainMenu.statusList.append(ResourceBundle.getString("status_occupied"), ContactList.statusOccupiedImg);
		MainMenu.statusList.append(ResourceBundle.getString("status_dnd"), ContactList.statusDndImg);
		MainMenu.statusList.append(ResourceBundle.getString("status_invisible"), ContactList.statusInvisibleImg);
		MainMenu.statusList.append(ResourceBundle.getString("status_invis_all"), ContactList.statusInvisibleImg);
	}

	/** ************************************************************************* */

	/* Visual list */
	static private List list;

	/* Menu event list */
	static private int[] eventList;

	/* Groups list */
	static private int[] groupIds;

	/* Form for the adding users dialog */
	static public Form textBoxForm;

	/* Text box for adding users to the contact list */
	static private TextField uinTextField;

	/* Textbox for  Status messages */
	static private TextBox statusMessage;

	/* Connected */
	static private boolean isConnected, haveToRebuild;

	public MainMenu()
	{
		_this = this;
	}

	static private Image getStatusImage()
	{
		long cursStatus = Options.getLong(Options.OPTION_ONLINE_STATUS);
		int imageIndex = JimmUI.getStatusImageIndex(cursStatus);
		return ContactList.getImageList().elementAt(imageIndex);
	}

	public static Displayable getDisplayable()
	{
		return list;
	}

	/* Builds the main menu (visual list) */
	static private void build()
	{
		if ((Icq.isConnected() != isConnected) || (MainMenu.list == null) || haveToRebuild)
		{
			MainMenu.eventList = new int[MENU_EXIT];
			MainMenu.list = new List(ResourceBundle.getString("menu"), List.IMPLICIT);

			if (Icq.isNotConnected())
			{
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("connect"), null)] = MENU_CONNECT;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("set_status"), getStatusImage())] = MENU_STATUS;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("contact_list"), null)] = MENU_LIST;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("options_lng"), null)] = MENU_OPTIONS;

				// #sijapp cond.if target is "MOTOROLA" #
				MainMenu.list.addCommand(MainMenu.selectCommand);
				MainMenu.list.addCommand(MainMenu.exitCommand);
				// #sijapp cond.end#
			}
			else
			{
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("keylock_enable"), null)] = MENU_KEYLOCK;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("disconnect"), null)] = MENU_DISCONNECT;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("set_status"), getStatusImage())] = MENU_STATUS;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("manage_contact_list"), null)] = MENU_GROUPS;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("options_lng"), null)] = MENU_OPTIONS;
				MainMenu.list.addCommand(MainMenu.backCommand);
				// #sijapp cond.if target is "MOTOROLA" #
				MainMenu.list.addCommand(MainMenu.selectCommand);
				// #sijapp cond.end#
			}

//#sijapp cond.if target isnot "DEFAULT"#
			boolean isSilent = Options.getBoolean(Options.OPTION_SILENT_MODE);
			MainMenu.eventList[MainMenu.list.append(getSoundValue(isSilent), null)] = MENU_SOUND;
//#sijapp cond.end#    	
			
			MainMenu.list.setCommandListener(_this);

			// #sijapp cond.if modules_TRAFFIC is "true" #
			MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("traffic_lng"), null)] = MENU_TRAFFIC;
			// #sijapp cond.end#
			MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("about"), null)] = MENU_ABOUT;
			// #sijapp cond.if target is "MIDP2" #
			if (Jimm.is_phone_SE()) MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("minimize"), null)] = MENU_MINIMIZE;
			// #sijapp cond.end#
			MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("exit"), null)] = MENU_EXIT;

			MainMenu.isConnected = Icq.isConnected();
		}
		else
		{
			if ((MainMenu.list != null) && (!Icq.isNotConnected())) MainMenu.list.set(2, ResourceBundle.getString("set_status"), getStatusImage());
		}
		
		haveToRebuild = false;
		MainMenu.list.setSelectedIndex(0, true);
	}
	
	static public void rebuild()
	{
		haveToRebuild = true;
	}

	/* Displays the given alert and activates the main menu afterwards */
	static public void activate(Alert alert)
	{
		status = STATUS_NONE;
		MainMenu.build();
		Jimm.display.setCurrent(alert, MainMenu.list);
	}

	/* Activates the main menu */
	static public void activate()
	{
		MainMenu.build();
		Jimm.display.setCurrent(MainMenu.list);
	}

	/* Show form for adding user */
	static public void showTextBoxForm(String caption, String label, String text, int fieldType)
	{
		textBoxForm = new Form(ResourceBundle.getString(caption));
		uinTextField = new TextField(ResourceBundle.getString(label), text, 16, fieldType);
		textBoxForm.append(uinTextField);
		
		textBoxForm.addCommand(sendCommand);
		textBoxForm.addCommand(backCommand);
		textBoxForm.setCommandListener(_this);
		Jimm.display.setCurrent(textBoxForm);
	}

	private void doExit(boolean anyway)
	{
		if (!anyway && ContactList.getUnreadMessCount() > 0)
		{
			JimmUI.messageBox(ResourceBundle.getString("attention"), ResourceBundle.getString("have_unread_mess"), JimmUI.MESBOX_YESNO, _this, TAG_EXIT);
		}
		else
		{
			Icq.disconnect();
   			try
			{
				Thread.sleep(500);
			} catch (InterruptedException e1)
			{
				/* Do nothing */
			}
			/* Exit app */
			try
			{
				Jimm.jimm.destroyApp(true);
			}
			catch (MIDletStateChangeException e)
			{ /* Do nothing */ 
			}
		}
	}
	
// #sijapp cond.if target isnot "DEFAULT" #	
	static private String getSoundValue(boolean value)
	{
		 return ResourceBundle.getString(value ? "#sound_on" : "#sound_off");
	}
// #sijapp cond.end#	

	/* Command listener */
	public void commandAction(Command c, Displayable d)
	{
// #sijapp cond.if target is "MOTOROLA" #
		/* Exit by soft button */
		if (c == MainMenu.exitCommand)
		{
			doExit(false);
			return;
		}
// #sijapp cond.end#
		
		if (d == groupActList)
		{
			if ((c == selectCommand) || (c == List.SELECT_COMMAND)) CLManagementItemSelected(groupActList.getSelectedIndex());
			else if (c == backCommand) activate();
			return;
		}

		if (JimmUI.getCommandType(c, TAG_RENAME_GROUPS) == JimmUI.CMD_OK)
		{
			String groupName = ContactList.getGroupById(groupIds[JimmUI.getLastSelIndex()]).getName(); 
			showTextBoxForm("rename_group", "group_name", groupName, TextField.ANY);
		} 
		
		else if (JimmUI.getCommandType(c, TAG_DELETE_GROUPS) == JimmUI.CMD_OK)
		{			
			UpdateContactListAction deleteGroupAct = new UpdateContactListAction(ContactList.getGroupById(groupIds[JimmUI.getLastSelIndex()]), UpdateContactListAction.ACTION_DEL);
			try
			{
				Icq.requestAction(deleteGroupAct);
				SplashCanvas.addTimerTask("wait", deleteGroupAct, false);
			}
			catch (JimmException e)
			{
				JimmException.handleException(e);
			}
		}
		
		/* Return to contact list */
		else if (c == MainMenu.backCommand)
		{
			if ((d == MainMenu.textBoxForm) || (d == statusList)) MainMenu.activate();
			else ContactList.activate();
		}
		else if ((c == sendCommand) && (d == MainMenu.textBoxForm))
		{
			Action act = null;

			switch (status)
			{
			case STATUS_ADD_GROUP:
				ContactListGroupItem newGroup = new ContactListGroupItem(uinTextField.getString());
				act = new UpdateContactListAction(newGroup, UpdateContactListAction.ACTION_ADD);
				break;
				
			case STATUS_RENAME_GROUP:
				ContactListGroupItem group = ContactList.getGroupById(groupIds[JimmUI.getLastSelIndex()]);
				group.setName(uinTextField.getString());
				ContactList.safeSave();
				act = new UpdateContactListAction(group, UpdateContactListAction.ACTION_RENAME);
				break;
				
			case STATUS_ADD_CONTACT:
				Search search = new Search(true);
				String data[] = new String[Search.LAST_INDEX];
				data[Search.UIN] = uinTextField.getString();
				act = new SearchAction(search, data, SearchAction.CALLED_BY_ADDUSER);
				break;
			}
			
			status = STATUS_NONE;
			
			try
			{
				Icq.requestAction(act);
			}
			catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical()) return;
			}
			// Start timer
			SplashCanvas.addTimerTask("wait", act, false);
		}

		/* User select OK in exit questiom message box */
		else if (JimmUI.getCommandType(c, TAG_EXIT) == JimmUI.CMD_YES)
		{
			doExit(true);
		}

		/* User select CANCEL in exit questiom message box */
		else if (JimmUI.getCommandType(c, TAG_EXIT) == JimmUI.CMD_NO)
		{
			ContactList.activate();
		}

		/* Menu item has been selected */

		else if (((c == List.SELECT_COMMAND) || (c == MainMenu.selectCommand)) && (d == MainMenu.list))
		{
			int selectedIndex = MainMenu.list.getSelectedIndex();
			switch (MainMenu.eventList[selectedIndex])
			{
			case MENU_CONNECT:
				/* Connect */
				Icq.reconnect_attempts = Options.getInt(Options.OPTION_RECONNECT_NUMBER);
				ContactList.beforeConnect();
				Icq.connect();
				break;

			case MENU_DISCONNECT:
				/* Disconnect */
				Icq.disconnect();
				Thread.yield();
				/* Show the main menu */
				MainMenu.activate();
				break;

			case MENU_LIST:
				/* ContactList */
				ContactList.activate();
				break;

			case MENU_KEYLOCK:
				/* Enable keylock */
				SplashCanvas.lock();
				break;

			case MENU_STATUS:
				/* Set status */
				int onlineStatus = (int) Options.getLong(Options.OPTION_ONLINE_STATUS);
				int index = 0;

				switch (onlineStatus)
				{
				case ContactList.STATUS_AWAY:
					index = 2;
					break;
				case ContactList.STATUS_CHAT:
					index = 1;
					break;
				case ContactList.STATUS_DND:
					index = 5;
					break;
				case ContactList.STATUS_INVISIBLE:
					index = 6;
					break;
				case ContactList.STATUS_NA:
					index = 3;
					break;
				case ContactList.STATUS_OCCUPIED:
					index = 4;
					break;
				case ContactList.STATUS_ONLINE:
					index = 0;
					break;
				case ContactList.STATUS_INVIS_ALL:
					index = 7;
					break;
				}

				MainMenu.statusList.setSelectedIndex(index, true);
				MainMenu.statusList.setCommandListener(_this);
				MainMenu.statusList.addCommand(backCommand);
				MainMenu.statusList.addCommand(selectCommand);
				Jimm.display.setCurrent(MainMenu.statusList);
				break;

			case MENU_GROUPS:
				
				groupActList = new List(ResourceBundle.getString("manage_contact_list"), List.IMPLICIT);
				
				groupActList.append(ResourceBundle.getString("add_user",     ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("search_user",  ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("add_group",    ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("rename_group", ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("del_group",    ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.setCommandListener(_this);
				groupActList.addCommand(backCommand);
				groupActList.addCommand(selectCommand);
				Jimm.display.setCurrent(groupActList);
			
				break;

			case MENU_OPTIONS:
				/* Options */
				Options.editOptions();
				break;

			// #sijapp cond.if modules_TRAFFIC is "true" #
			case MENU_TRAFFIC:
				/* Traffic */
				Traffic.trafficScreen.activate();
				break;
			// #sijapp cond.end #

			case MENU_ABOUT:
				// Display an info
				JimmUI.about(list);
				break;

			//#sijapp cond.if target is "MIDP2"#
			case MENU_MINIMIZE:
				/* Minimize Jimm (if supported) */                 
				Jimm.setMinimized(true);
				break;
			//#sijapp cond.end#
				
// #sijapp cond.if target isnot "DEFAULT" #
			case MENU_SOUND:
				boolean soundValue = ContactList.changeSoundMode( Icq.isConnected() );
				MainMenu.list.set(selectedIndex, getSoundValue(soundValue), null);
				break;
//#sijapp cond.end#				

			case MENU_EXIT:
				/* Exit */
				doExit(false);
				break;
			}
		}
		
		/* Online status has been selected */
		else if (((c == List.SELECT_COMMAND) || (c == MainMenu.selectCommand)) && (d == MainMenu.statusList))
		{

			/* Request online status change */
			int onlineStatus = ContactList.STATUS_ONLINE;
			switch (MainMenu.statusList.getSelectedIndex())
			{
			case 1:
				onlineStatus = ContactList.STATUS_CHAT;
				break;
			case 2:
				onlineStatus = ContactList.STATUS_AWAY;
				break;
			case 3:
				onlineStatus = ContactList.STATUS_NA;
				break;
			case 4:
				onlineStatus = ContactList.STATUS_OCCUPIED;
				break;
			case 5:
				onlineStatus = ContactList.STATUS_DND;
				break;
			case 6:
				onlineStatus = ContactList.STATUS_INVISIBLE;
				break;
			case 7:
				onlineStatus = ContactList.STATUS_INVIS_ALL;
				break;
			}

			/* Save new online status */
			Options.setLong(Options.OPTION_ONLINE_STATUS, onlineStatus);
			Options.safe_save();

			if (Icq.isConnected())
			{
				try
				{
					Icq.setOnlineStatus(onlineStatus);
				}
				catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical()) return;
				}
			}
			else
			{
				isConnected = !Icq.isConnected();
			}
			if ((onlineStatus != ContactList.STATUS_INVISIBLE) && (onlineStatus != ContactList.STATUS_INVIS_ALL) && (onlineStatus != ContactList.STATUS_ONLINE)
					&& (onlineStatus != ContactList.STATUS_CHAT))
			{
				//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
				statusMessage = new TextBox(ResourceBundle.getString("status_message"), Options.getString(Options.OPTION_STATUS_MESSAGE), 255, TextField.ANY
						| TextField.INITIAL_CAPS_SENTENCE);
				//#sijapp cond.else#
				statusMessage = new TextBox(ResourceBundle.getString("status_message"), Options.getString(Options.OPTION_STATUS_MESSAGE), 255, TextField.ANY);
				//#sijapp cond.end#

				statusMessage.addCommand(selectCommand);
				statusMessage.setCommandListener(_this);
				Jimm.display.setCurrent(statusMessage);
			}
			else
			{
				/* Active MM/CL */
				if (Icq.isConnected())
				{
					ContactList.activate();
				}
				else
				{
					MainMenu.activate();
				}
			}
		}
		else if ((c == selectCommand) && (d == statusMessage))
		{
			Options.setString(Options.OPTION_STATUS_MESSAGE, statusMessage.getString());
			Options.safe_save();
			/* Active MM/CL */
			if (Icq.isConnected())
			{
				ContactList.activate();
			}
			else
			{
				MainMenu.activate();
			}
		}
		
		/* Contact list management group */
		else if (JimmUI.getCommandType(c, TAG_CL) == JimmUI.CMD_OK) CLManagementItemSelected( JimmUI.getLastSelIndex() );
	}
	
	private void CLManagementItemSelected(int index)
	{
		switch (index)
		{
		case 0: /* Add user */
			status = STATUS_ADD_CONTACT;
			showTextBoxForm("add_user", "uin", null, TextField.NUMERIC);
			break;
			
		case 1: /* Search for User */
			Search searchf = new Search(false);
			searchf.getSearchForm().activate(Search.SearchForm.ACTIV_JUST_SHOW);
			break;
			
		case 2: /* Add group */
			status = STATUS_ADD_GROUP;
			showTextBoxForm("add_group", "group_name", null, TextField.ANY);
			break;
			
		case 3: /* Rename group */
			status = STATUS_RENAME_GROUP;
			groupIds = JimmUI.showGroupSelector("rename_group", TAG_RENAME_GROUPS, this, JimmUI.SHS_TYPE_ALL, -1); 
			break;
			
		case 4: /* Delete group */ 
			groupIds = JimmUI.showGroupSelector("del_group", TAG_DELETE_GROUPS, this, JimmUI.SHS_TYPE_EMPTY, -1);
			break;
		}
	}
	
	
}
