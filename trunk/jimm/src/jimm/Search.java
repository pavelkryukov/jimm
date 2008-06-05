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
 File: src/jimm/Search.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimm;

import java.util.Vector;
import javax.microedition.lcdui.*;

import jimm.comm.SearchAction;
import jimm.comm.Icq;
import jimm.comm.Util;
import jimm.util.*;
import DrawControls.*;

public class Search
{
	SearchForm searchForm;

	private boolean liteVersion;

	final public static int UIN = 0;

	final public static int NICK = 1;

	final public static int FIRST_NAME = 2;

	final public static int LAST_NAME = 3;

	final public static int EMAIL = 4;

	final public static int CITY = 5;

	final public static int KEYWORD = 6;

	final public static int GENDER = 7;

	final public static int ONLY_ONLINE = 8;

	final public static int AGE = 9;

	final public static int LAST_INDEX = 10;

	/* Results */
	private Vector results;

	/* Constructor */
	public Search(boolean liteVersion)
	{
		this.results = new Vector();
		this.liteVersion = liteVersion;
	}

	/* Add a result to the results vector */
	public void addResult(String uin, String nick, String name, String email,
			String auth, int status, String gender, int age)
	{
		String[] resultData = new String[JimmUI.UI_LAST_ID];

		resultData[JimmUI.UI_UIN_LIST] = uin;
		resultData[JimmUI.UI_NICK] = nick;
		resultData[JimmUI.UI_NAME] = name;
		resultData[JimmUI.UI_EMAIL] = email;
		resultData[JimmUI.UI_AUTH] = auth;
		resultData[JimmUI.UI_STATUS] = Integer.toString(status);
		resultData[JimmUI.UI_GENDER] = gender;
		resultData[JimmUI.UI_AGE] = Integer.toString(age);

		this.results.addElement(resultData);
	}

	/* Return a result object by given Nr */
	public String[] getResult(int nr)
	{
		return (String[]) results.elementAt(nr);
	}

	/* Return size of search results */
	public int size()
	{
		return results.size();
	}

	/* Return the SearchForm object */
	public SearchForm getSearchForm()
	{
		if (searchForm == null)
			searchForm = new SearchForm();
		return searchForm;
	}

	/** ************************************************************************* */
	/** ************************************************************************* */
	/** ************************************************************************* */

	/* Class for the search forms */
	public class SearchForm implements CommandListener, VirtualListCommands, JimmScreen
	{
		int lastType;
		
		/* Commands */
		private Command backCommand;
		private Command searchCommand;
		private Command addCommand;
		private Command previousCommand;
		private Command nextCommand;
		private Command cmdSendMessage;
		private Command cmdShowInfo;

		/* Forms for results and query */
		private Form searchForm;
		private TextList screen;

		/* List for group selection */
		private TextList groupList;

		/* Textboxes for search */
		private TextField uinSearchTextBox;
		private TextField nickSearchTextBox;
		private TextField firstnameSearchTextBox;
		private TextField lastnameSearchTextBox;
		private TextField emailSearchTextBox;
		private TextField citySearchTextBox;
		private TextField keywordSearchTextBox;

		private ChoiceGroup chgrAge;

		/* Choice boxes for gender and online choice */
		private ChoiceGroup gender;
		private ChoiceGroup onlyOnline;

		/* Selectet index in result screen */
		int selectedIndex;

		/* constructor for search form */
		public SearchForm()
		{
			/* Commands */
			this.searchCommand = new Command(ResourceBundle
					.getString("search_user"), Command.OK, 1);
			this.backCommand = new Command(ResourceBundle.getString("back"),
					Command.BACK, 2);
			this.addCommand = new Command(ResourceBundle
					.getString("add_to_list"), Command.OK, 1);
			this.previousCommand = new Command(
					ResourceBundle.getString("prev"), Command.ITEM, 4);
			this.nextCommand = new Command(ResourceBundle.getString("next"),
					Command.ITEM, 5);
			this.cmdSendMessage = new Command(ResourceBundle
					.getString("send_message"), Command.ITEM, 6);
			this.cmdShowInfo = new Command(ResourceBundle.getString("info"),
					Command.ITEM, 7);

			/* Form */
			this.searchForm = new Form(ResourceBundle.getString("search_user"));

			/* TextFields */
			this.uinSearchTextBox = new TextField(ResourceBundle
					.getString("uin"), "", 32, TextField.NUMERIC);
			this.nickSearchTextBox = new TextField(ResourceBundle
					.getString("nick"), "", 32, TextField.ANY);
			this.firstnameSearchTextBox = new TextField(ResourceBundle
					.getString("firstname"), "", 32, TextField.ANY);
			this.lastnameSearchTextBox = new TextField(ResourceBundle
					.getString("lastname"), "", 32, TextField.ANY);
			this.emailSearchTextBox = new TextField(ResourceBundle
					.getString("email"), "", 32, TextField.EMAILADDR);
			this.citySearchTextBox = new TextField(ResourceBundle
					.getString("city"), "", 32, TextField.ANY);
			this.keywordSearchTextBox = new TextField(ResourceBundle
					.getString("keyword"), "", 32, TextField.ANY);

			chgrAge = new ChoiceGroup(ResourceBundle.getString("age"),
					ChoiceGroup.EXCLUSIVE, Util.explode(
							"-|18-22|23-29|30-39|40-49|50-59|>60", '|'), null);

			/* Choice Groups */
			this.gender = new ChoiceGroup(ResourceBundle.getString("gender"),
					Choice.EXCLUSIVE);
			this.gender.append(ResourceBundle.getString("female_male"), null);
			this.gender.append(ResourceBundle.getString("female"), null);
			this.gender.append(ResourceBundle.getString("male"), null);
			this.onlyOnline = new ChoiceGroup("", Choice.MULTIPLE);
			this.onlyOnline.append(ResourceBundle.getString("only_online"),
					null);

			this.searchForm.append(this.onlyOnline);
			this.searchForm.append(this.uinSearchTextBox);
			this.searchForm.append(this.nickSearchTextBox);
			this.searchForm.append(this.firstnameSearchTextBox);
			this.searchForm.append(this.lastnameSearchTextBox);
			this.searchForm.append(this.citySearchTextBox);
			this.searchForm.append(this.gender);
			this.searchForm.append(this.emailSearchTextBox);
			this.searchForm.append(this.keywordSearchTextBox);
			this.searchForm.append(this.chgrAge);
			this.searchForm.setCommandListener(this);

			/* Result Screen */
			screen = new TextList(null);
			screen.setVLCommands(this);
//#sijapp cond.if target!="RIM" & target!="DEFAULT"#
			screen.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
//#sijapp cond.end#
			screen.addCommandEx(this.previousCommand, VirtualList.MENU_TYPE_RIGHT);
			screen.addCommandEx(this.nextCommand, VirtualList.MENU_TYPE_RIGHT);
			screen.addCommandEx(this.addCommand, VirtualList.MENU_TYPE_RIGHT);
			screen.addCommandEx(this.cmdSendMessage, VirtualList.MENU_TYPE_RIGHT);
			screen.addCommandEx(this.cmdShowInfo, VirtualList.MENU_TYPE_RIGHT);
			screen.setMode(TextList.CURSOR_MODE_DISABLED);
			JimmUI.setColorScheme(screen, false, -1, true);
		}

		static final public int ACTIV_SHOW_RESULTS = 1;

		static final public int ACTIV_JUST_SHOW = 2;

		static final public int ACTIV_SHOW_NORESULTS = 3;
		
		public void activate()
		{
			activate(lastType);
		}
		
		public boolean isScreenActive()
		{
			if (searchForm != null && searchForm.isShown()) return true;
			if (screen != null && screen.isActive()) return true;
			if (groupList != null && groupList.isActive()) return true; 
			return false;
		}

		/* Activate search form */
		public void activate(int type)
		{
			lastType = type;
			switch (type)
			{
			case ACTIV_SHOW_RESULTS:
				drawResultScreen(selectedIndex);
				this.screen.activate(Jimm.display);
				break;

			case ACTIV_JUST_SHOW:
				this.searchForm.addCommand(this.searchCommand);
				this.searchForm.addCommand(this.backCommand);
				Jimm.display.setCurrent(this.searchForm);
				Jimm.setBkltOn(true);
				break;

			case ACTIV_SHOW_NORESULTS:
				this.searchForm.addCommand(this.searchCommand);
				this.searchForm.addCommand(this.backCommand);
				Alert alert = new Alert(null, ResourceBundle
						.getString("no_results"), null, null);
				alert.setTimeout(Alert.FOREVER);
				Jimm.display.setCurrent(alert, this.searchForm);
				Jimm.setBkltOn(false);
				break;
			}
			JimmUI.setLastScreen(this, false);
		}

		public void drawResultScreen(int n)
		{
			/* Remove the older entrys here */
			screen.clear();

			if (Search.this.size() > 0)
			{

				if (Search.this.size() == 1)
				{
					screen.removeCommandEx(this.nextCommand);
					screen.removeCommandEx(this.previousCommand);
				}

				screen.lock();

				JimmUI.fillUserInfo(getResult(n), screen);
				screen.setCaption(ResourceBundle.getString("results") + " "
						+ Integer.toString(n + 1) + "/"
						+ Integer.toString(Search.this.size()));
				screen.unlock();
			} else
			{
				/* Show a result entry */

				screen.lock();
				screen.setCaption(ResourceBundle.getString("results") + " 0/0");
				screen.addBigText(
						ResourceBundle.getString("no_results") + ": ", 0x0,
						Font.STYLE_BOLD, -1);
				screen.unlock();
			}

			screen.addCommandEx(this.backCommand, VirtualList.MENU_TYPE_LEFT_BAR);

			screen.setCommandListener(this);
		}

		public void nextOrPrev(boolean next)
		{
			if (next)
			{
				selectedIndex = (selectedIndex + 1) % Search.this.size();
				this.activate(Search.SearchForm.ACTIV_SHOW_RESULTS);
			} else
			{
				if (selectedIndex == 0)
					selectedIndex = Search.this.size() - 1;
				else
				{
					selectedIndex = (selectedIndex - 1) % Search.this.size();
				}
				this.activate(Search.SearchForm.ACTIV_SHOW_RESULTS);
			}

		}

		public void vlKeyPress(VirtualList sender, int keyCode, int type)
		{
			if (type == VirtualList.KEY_PRESSED)
			{
				switch (sender.getGameAction(keyCode))
				{
				case Canvas.LEFT:
					nextOrPrev(false);
					break;

				case Canvas.RIGHT:
					nextOrPrev(true);
					break;
				}
			}
		}

		public void vlCursorMoved(VirtualList sender)
		{
		}

		public void vlItemClicked(VirtualList sender)
		{
		}

		public void commandAction(Command c, Displayable d)
		{
			if (c == this.backCommand || c == JimmUI.cmdCancel)
			{
				JimmUI.backToLastScreen();
			} 
			else if (c == this.searchCommand)
			{
				selectedIndex = 0;

				String[] data = new String[Search.LAST_INDEX];

				data[Search.UIN] = this.uinSearchTextBox.getString();
				data[Search.NICK] = this.nickSearchTextBox.getString();
				data[Search.FIRST_NAME] = this.firstnameSearchTextBox
						.getString();
				data[Search.LAST_NAME] = this.lastnameSearchTextBox.getString();
				data[Search.EMAIL] = this.emailSearchTextBox.getString();
				data[Search.CITY] = this.citySearchTextBox.getString();
				data[Search.KEYWORD] = this.keywordSearchTextBox.getString();
				data[Search.GENDER] = Integer.toString(this.gender
						.getSelectedIndex());
				data[Search.ONLY_ONLINE] = this.onlyOnline.isSelected(0) ? "1"
						: "0";
				data[Search.AGE] = Integer.toString(this.chgrAge
						.getSelectedIndex());

				SearchAction act = new SearchAction(Search.this, data,
						SearchAction.CALLED_BY_SEARCHUSER);
				try
				{
					Icq.requestAction(act);

				} catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical())
						return;
				}

				/* Clear results */
				results.removeAllElements();

				/* Start timer */
				SplashCanvas.addTimerTask("wait", act, true);
			}

			/* "Next" command */
			else if (c == this.nextCommand)
				nextOrPrev(true);

			/* "Previous" command */
			else if (c == this.previousCommand)
				nextOrPrev(false);
			else if ((c == this.addCommand) && JimmUI.isControlActive(screen))
			{
				searchForm = null;
				if (ContactList.getGroupItems().length == 0)
				{
					Alert errorMsg = new Alert(ResourceBundle
							.getString("warning"), JimmException.getErrDesc(
							161, 0), null, AlertType.WARNING);
					errorMsg.setTimeout(Alert.FOREVER);
					ContactList.activate(errorMsg);
				} else
				{
					/* Show list of groups to select which group to add to */
					groupList = JimmUI.showGroupSelector("whichgroup", this, JimmUI.SHS_TYPE_ALL, -1); 
				}
			} else if (c == JimmUI.cmdOk && JimmUI.isControlActive(this.groupList))
			{
				int groupId = groupList.getCurrTextIndex();
				if (groupId == -1) return;
				String[] resultData = getResult(selectedIndex);
				ContactItem cItem = new ContactItem(-1, groupId, resultData[JimmUI.UI_UIN_LIST], resultData[JimmUI.UI_NICK], false, false);
				cItem.setBooleanValue(ContactItem.CONTACTITEM_NO_AUTH, resultData[JimmUI.UI_AUTH].equals("1"));
				cItem.setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP, true);
				cItem.setIntValue(ContactItem.CONTACTITEM_STATUS, ContactList.STATUS_OFFLINE);
				Icq.addToContactList(cItem);
			}

			/* Command "Send message" */
			else if (c == this.cmdSendMessage)
			{
				String[] resultData = getResult(selectedIndex);

				ContactItem cItem = ContactList
						.createTempContact(resultData[JimmUI.UI_UIN_LIST]);
				//ContactItem.CONTACTITEM_HAS_CHAT
				cItem.setStringValue(ContactItem.CONTACTITEM_NAME,
						resultData[JimmUI.UI_NICK]);
				JimmUI.writeMessage(cItem, new String());
			}

			/* Command "Show info" */
			else if (c == this.cmdShowInfo)
			{
				String[] resultData = getResult(selectedIndex);
				JimmUI.requiestUserInfo(resultData[JimmUI.UI_UIN_LIST],
						resultData[JimmUI.UI_NICK], false);
			}
		}

	} /* end "class SearchForm" */

}
