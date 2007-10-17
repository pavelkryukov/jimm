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

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDletStateChangeException;

import jimm.comm.Icq;
import jimm.comm.SearchAction;
import jimm.comm.Action;
import jimm.comm.UpdateContactListAction;
import jimm.util.ResourceBundle;
import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.VirtualListCommands;

public class MainMenu implements CommandListener, VirtualListCommands
{
	private static final int TAG_EXIT = 1;
	private static final int TAG_RENAME_GROUPS = 2;
	private static final int TAG_DELETE_GROUPS = 3;
	private static final int TAG_CL = 4;
	
	private static final int SELECT_STATUS = 1;
	private static final int SELECT_XSTATUS = 2;
	
	private static int statusSelection = 0;

	private static MainMenu _this;

	/* Static constants for menu actios */
	private static final int MENU_CONNECT = 1;

	private static final int MENU_DISCONNECT = 2;

	private static final int MENU_LIST = 3;

	private static final int MENU_OPTIONS = 4;

	private static final int MENU_TRAFFIC = 5;

	private static final int MENU_KEYLOCK = 6;

	private static final int MENU_STATUS = 7;
	private static final int MENU_XSTATUS = 8;

	private static final int MENU_GROUPS = 9;

	private static final int MENU_ABOUT = 10;

	private static final int MENU_MINIMIZE = 11;

	private static final int MENU_SOUND = 12;

	private static final int MENU_MYSELF = 13;

	private static final int MENU_EXIT = 14; /* Exit has to be biggest element cause it also marks the size */

	/* Abort command */
	private static Command backCommand = new Command(ResourceBundle
			.getString("back"), Command.BACK, 1);

	/* Send command */
	private static Command sendCommand = new Command(ResourceBundle
			.getString("send"), Command.OK, 1);

	/* Select command */
	private static Command selectCommand = new Command(ResourceBundle
			.getString("select"), Command.OK, 1);

	//#sijapp cond.if target is "MOTOROLA" #
	//#	private static Command exitCommand = new Command(ResourceBundle.getString("exit_button"), Command.EXIT, 1);
	//#sijapp cond.end#

	/* List for selecting a online status */
	//private static List statusList;
	private static TextList statusList;
	
	private static List groupActList;

	/////////////////////////////////////////////////////////////////
	private static final int STATUS_NONE = 0;

	private static final int STATUS_ADD_CONTACT = 1;

	private static final int STATUS_ADD_GROUP = 2;

	private static final int STATUS_RENAME_GROUP = 3;

	private static int status = STATUS_NONE;

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
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("set_xstatus"), getStatusImage())] = MENU_XSTATUS;

				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("contact_list"), null)] = MENU_LIST;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("options_lng"), null)] = MENU_OPTIONS;

				//#sijapp cond.if target is "MOTOROLA" #
				//#				MainMenu.list.addCommand(MainMenu.selectCommand);
				//#				MainMenu.list.addCommand(MainMenu.exitCommand);
				//#sijapp cond.end#
			}
			else
			{
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("keylock_enable"), null)] = MENU_KEYLOCK;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("disconnect"), null)] = MENU_DISCONNECT;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("set_status"), getStatusImage())] = MENU_STATUS;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("set_xstatus"), getStatusImage())] = MENU_XSTATUS;

				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("manage_contact_list"), null)] = MENU_GROUPS;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("myself"), null)] = MENU_MYSELF;
				MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("options_lng"), null)] = MENU_OPTIONS;
				MainMenu.list.addCommand(MainMenu.backCommand);
				//#sijapp cond.if target is "MOTOROLA" #
				//# 			MainMenu.list.addCommand(MainMenu.selectCommand);
				//#sijapp cond.end#
			}

			//#sijapp cond.if target isnot "DEFAULT"#
			boolean isSilent = Options.getBoolean(Options.OPTION_SILENT_MODE);
			MainMenu.eventList[MainMenu.list.append(getSoundValue(isSilent), null)] = MENU_SOUND;
			//#sijapp cond.end#    	

			MainMenu.list.setCommandListener(_this);

			//#sijapp cond.if modules_TRAFFIC is "true" #
			MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("traffic_lng"), null)] = MENU_TRAFFIC;
			//#sijapp cond.end#
			MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("about"), null)] = MENU_ABOUT;
			//#sijapp cond.if target is "MIDP2" #
			if (Jimm.is_phone_SE()) MainMenu.eventList[MainMenu.list.append(ResourceBundle.getString("minimize"), null)] = MENU_MINIMIZE;
			//#sijapp cond.end#
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
	static public void showTextBoxForm(String caption, String label,
			String text, int fieldType)
	{
		textBoxForm = new Form(ResourceBundle.getString(caption));
		uinTextField = new TextField(ResourceBundle.getString(label), text, 16,
				fieldType);
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
			JimmUI.messageBox(ResourceBundle.getString("attention"),
					ResourceBundle.getString("have_unread_mess"),
					JimmUI.MESBOX_YESNO, _this, TAG_EXIT);
		} else
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
			} catch (MIDletStateChangeException e)
			{ /* Do nothing */
			}
		}
	}

	//#sijapp cond.if target isnot "DEFAULT" #	
	static private String getSoundValue(boolean value)
	{
		return ResourceBundle.getString(value ? "#sound_on" : "#sound_off");
	}

	//#sijapp cond.end#	
	
	private static void initStatusList()
	{
		statusList = new TextList(ResourceBundle.getString("set_status"));
		statusList.setCursorMode(TextList.SEL_NONE);
		statusList.lock();
		addStatusItem("status_online", ContactList.statusOnlineImg,    ContactList.STATUS_ONLINE);
		addStatusItem("status_chat", ContactList.statusChatImg,      ContactList.STATUS_CHAT);
		addStatusItem("status_away", ContactList.statusAwayImg,      ContactList.STATUS_AWAY);
		addStatusItem("status_na", ContactList.statusNaImg,        ContactList.STATUS_NA);
		addStatusItem("status_occupied", ContactList.statusOccupiedImg,  ContactList.STATUS_OCCUPIED);
		addStatusItem("status_dnd", ContactList.statusDndImg,       ContactList.STATUS_DND);
		addStatusItem("status_invisible", ContactList.statusInvisibleImg, ContactList.STATUS_INVISIBLE);
		addStatusItem("status_invis_all", ContactList.statusInvisibleImg, ContactList.STATUS_INVIS_ALL);
		statusList.unlock();
		statusSelection = SELECT_STATUS;
	}
	
	private static void addStatusItem(String text, Image image, int value)
	{
		if (image != null) statusList.addImage(image, null, value);
		statusList.addBigText(" "+ResourceBundle.getString(text), statusList.getTextColor(), Font.STYLE_PLAIN, value);
		statusList.doCRLF(value);
	}
	
    private static final String[] xstatus = {
    	"xstatus_none",
        "xstatus_angry",
        "xstatus_duck",
        "xstatus_tired",
        "xstatus_party",
        "xstatus_beer",
        "xstatus_thinking",
        "xstatus_eating",
        "xstatus_tv",
        "xstatus_friends",
        "xstatus_coffee",
        "xstatus_music",
        "xstatus_business",
        "xstatus_camera",
        "xstatus_funny",
        "xstatus_phone",
        "xstatus_games",
        "xstatus_college",
        "xstatus_shopping",
        "xstatus_sick",
        "xstatus_sleeping",
        "xstatus_surfing",
        "xstatus_internet",
        "xstatus_engineering",
        "xstatus_typing",
        "xstatus_unk",
        "xstatus_ppc",
        "xstatus_mobile",
        "xstatus_man",
        "xstatus_wc",
        "xstatus_question",
        "xstatus_way",
        "xstatus_heart",
        "xstatus_cigarette",
        "xstatus_sex",
        "xstatus_rambler_search",
        "xstatus_rambler_journal"
    };

	private static void initXStatusList()
	{
		statusList = new TextList(ResourceBundle.getString("set_xstatus"));
		statusList.setCursorMode(TextList.SEL_NONE);
		statusList.lock();
		for (int i = 0; i < xstatus.length; i++)
			addStatusItem(xstatus[i], ContactList.xStatusImages.elementAt(i-1), i);
		
		statusList.unlock();
		statusSelection = SELECT_XSTATUS;
	}
	
	public void onKeyPress(VirtualList sender, int keyCode, int type) {}
	public void onCursorMove(VirtualList sender) {}

	public void onItemSelected(VirtualList sender)
	{
		if (sender == statusList) userSelectStatus();
	}
	
	/* Command listener */
	public void commandAction(Command c, Displayable d)
	{
		//#sijapp cond.if target is "MOTOROLA" #
		/* Exit by soft button */
		//#		if (c == MainMenu.exitCommand)
		//#		{
		//#			doExit(false);
		//#			return;
		//#		}
		//#sijapp cond.end#
		if (d == groupActList)
		{
			if ((c == selectCommand) || (c == List.SELECT_COMMAND))
				CLManagementItemSelected(groupActList.getSelectedIndex());
			else if (c == backCommand)
				activate();
			return;
		}

		if (JimmUI.getCommandType(c, TAG_RENAME_GROUPS) == JimmUI.CMD_OK)
		{
			String groupName = ContactList.getGroupById(
					groupIds[JimmUI.getLastSelIndex()]).getName();
			showTextBoxForm("rename_group", "group_name", groupName,
					TextField.ANY);
		}

		else if (JimmUI.getCommandType(c, TAG_DELETE_GROUPS) == JimmUI.CMD_OK)
		{
			UpdateContactListAction deleteGroupAct = new UpdateContactListAction(
					ContactList
							.getGroupById(groupIds[JimmUI.getLastSelIndex()]),
					UpdateContactListAction.ACTION_DEL);
			try
			{
				Icq.requestAction(deleteGroupAct);
				SplashCanvas.addTimerTask("wait", deleteGroupAct, false);
			} catch (JimmException e)
			{
				JimmException.handleException(e);
			}
		}

		/* Return to contact list */
		else if (c == MainMenu.backCommand)
		{
			if ((d == MainMenu.textBoxForm) || (d == statusList))
				MainMenu.activate();
			else
				ContactList.activate();
			statusList = null;
		} else if ((c == sendCommand) && (d == MainMenu.textBoxForm))
		{
			Action act = null;

			switch (status)
			{
			case STATUS_ADD_GROUP:
				ContactListGroupItem newGroup = new ContactListGroupItem(
						uinTextField.getString());
				act = new UpdateContactListAction(newGroup,
						UpdateContactListAction.ACTION_ADD);
				break;

			case STATUS_RENAME_GROUP:
				ContactListGroupItem group = ContactList
						.getGroupById(groupIds[JimmUI.getLastSelIndex()]);
				group.setName(uinTextField.getString());
				ContactList.safeSave();
				act = new UpdateContactListAction(group,
						UpdateContactListAction.ACTION_RENAME);
				break;

			case STATUS_ADD_CONTACT:
				Search search = new Search(true);
				String data[] = new String[Search.LAST_INDEX];
				data[Search.UIN] = uinTextField.getString();
				act = new SearchAction(search, data,
						SearchAction.CALLED_BY_ADDUSER);
				break;
			}

			status = STATUS_NONE;

			try
			{
				Icq.requestAction(act);
			} catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical())
					return;
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

		else if (((c == List.SELECT_COMMAND) || (c == MainMenu.selectCommand))
				&& (d == MainMenu.list))
		{
			int selectedIndex = MainMenu.list.getSelectedIndex();
			switch (MainMenu.eventList[selectedIndex])
			{
			case MENU_CONNECT:
				/* Connect */
				Icq.reconnect_attempts = Options
						.getInt(Options.OPTION_RECONNECT_NUMBER);
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

			case MENU_STATUS: /* Set status */
			case MENU_XSTATUS: /* Set xstatus */
				int stValue;
				
				if (MainMenu.eventList[selectedIndex] == MENU_STATUS)
				{
					initStatusList();
					stValue = (int)Options.getLong(Options.OPTION_ONLINE_STATUS);
				}
				else
				{
					initXStatusList();
					stValue = Options.getInt(Options.OPTION_XSTATUS);
				}
				
				System.out.println("stValue="+stValue);
				
				MainMenu.statusList.selectTextByIndex(stValue);
			
				JimmUI.setColorScheme(statusList);
				
				MainMenu.statusList.setCommandListener(_this);
				MainMenu.statusList.setVLCommands(_this);
				MainMenu.statusList.addCommand(backCommand);
				MainMenu.statusList.addCommand(selectCommand);
				Jimm.display.setCurrent(MainMenu.statusList);
			
				break;

			case MENU_GROUPS:

				groupActList = new List(ResourceBundle
						.getString("manage_contact_list"), List.IMPLICIT);

				groupActList.append(ResourceBundle.getString("add_user",
						ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("search_user",
						ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("add_group",
						ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("rename_group",
						ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("del_group",
						ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.setCommandListener(_this);
				groupActList.addCommand(backCommand);
				groupActList.addCommand(selectCommand);
				Jimm.display.setCurrent(groupActList);

				break;

			case MENU_OPTIONS:
				/* Options */
				Options.editOptions();
				break;

			//#sijapp cond.if modules_TRAFFIC is "true" #
			case MENU_TRAFFIC:
				/* Traffic */
				Traffic.trafficScreen.activate();
				break;
			//#sijapp cond.end #

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

			//#sijapp cond.if target isnot "DEFAULT" #
			case MENU_SOUND:
				boolean soundValue = ContactList.changeSoundMode(Icq
						.isConnected());
				MainMenu.list.set(selectedIndex, getSoundValue(soundValue),
						null);
				break;
			//#sijapp cond.end#				

			case MENU_MYSELF:
				JimmUI.requiestUserInfo(Options.getString(Options.OPTION_UIN),
						"");
				break;

			case MENU_EXIT:
				/* Exit */
				doExit(false);
				break;
			}
		}

		/* Online status has been selected */
		else if (((c == List.SELECT_COMMAND) || (c == MainMenu.selectCommand)) && (d == MainMenu.statusList))
		{
			userSelectStatus();
		} 
		else if ((c == selectCommand) && (d == statusMessage))
		{
			Options.setString(Options.OPTION_STATUS_MESSAGE, statusMessage.getString());
			Options.safe_save();
			/* Active MM/CL */
			if (Icq.isConnected())
			{
				ContactList.activate();
			} else
			{
				MainMenu.activate();
			}
		}

		/* Contact list management group */
		else if (JimmUI.getCommandType(c, TAG_CL) == JimmUI.CMD_OK)
			CLManagementItemSelected(JimmUI.getLastSelIndex());
	}
	
	private void userSelectStatus()
	{
		boolean activateMenu = false;
		switch (statusSelection)
		{
		case SELECT_STATUS:
			int onlineStatus = statusList.getCurrTextIndex();
			Options.setLong(Options.OPTION_ONLINE_STATUS, onlineStatus);

			if (Icq.isConnected())
			{
				try
				{
					Icq.setOnlineStatus(onlineStatus);
				} catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical()) return;
				}
			}
			
			if ((onlineStatus != ContactList.STATUS_INVISIBLE)
					&& (onlineStatus != ContactList.STATUS_INVIS_ALL)
					&& (onlineStatus != ContactList.STATUS_ONLINE)
					&& (onlineStatus != ContactList.STATUS_CHAT))
			{
				//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
				statusMessage = new TextBox(ResourceBundle
						.getString("status_message"), Options
						.getString(Options.OPTION_STATUS_MESSAGE), 255,
						TextField.ANY | TextField.INITIAL_CAPS_SENTENCE);
				//#sijapp cond.else#
				//#				statusMessage = new TextBox(ResourceBundle.getString("status_message"), Options.getString(Options.OPTION_STATUS_MESSAGE), 255, TextField.ANY);
				//#sijapp cond.end#

				statusMessage.addCommand(selectCommand);
				statusMessage.setCommandListener(_this);
				Jimm.display.setCurrent(statusMessage);
			} 
			else activateMenu = true;
			break;
	
		case SELECT_XSTATUS:
			Options.setInt(Options.OPTION_XSTATUS, statusList.getCurrTextIndex()-1);
			if (Icq.isConnected())
			{
				try
				{
					Icq.sendUserUnfoPacket();
				} catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical()) return;
				}
			}
			activateMenu = true;
			break;
		}
		Options.safe_save();
		statusList = null;
		
		if (activateMenu) /* Active MM/CL */
		{
			if (Icq.isConnected()) ContactList.activate();
			else MainMenu.activate();
		}
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
			groupIds = JimmUI.showGroupSelector("rename_group",
					TAG_RENAME_GROUPS, this, JimmUI.SHS_TYPE_ALL, -1);
			break;

		case 4: /* Delete group */
			groupIds = JimmUI.showGroupSelector("del_group", TAG_DELETE_GROUPS,
					this, JimmUI.SHS_TYPE_EMPTY, -1);
			break;
		}
	}

}
