/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2005-08  Jimm Project

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
 File: src/jimm/RunnableImpl.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package jimm;

import java.util.Vector;
import javax.microedition.lcdui.*;

import DrawControls.VirtualList;
import jimm.comm.Message;
import jimm.comm.Util;
import jimm.ContactItem;

public class MainThread implements Runnable
{
	private static Vector mainThreadTasks = new Vector();
	private static MainThread _this;

	final static private int TYPE_ADD_MSG             = 1;
	final static private int TYPE_USER_OFFLINE        = 4;
	final static private int TYPE_UPDATE_CONTACT_LIST = 5;
	final static private int TYPE_SHOW_USER_INFO      = 6;
	final static private int TYPE_UPDATE_CL_CAPTION   = 7;
	final static private int TYPE_USER_IS_TYPING      = 9;
	final static private int TYPE_RESET_CONTACTS      = 10;
	final static private int TYPE_SHOW_TIME           = 11;
	final static private int TYPE_ADD_CONTACT         = 12;
	final static private int TYPE_MINUTE_TASK         = 14;
	final static private int TYPE_MESS_DELIVERED      = 15;
	final static private int TYPE_SHOW_LAST_VESR      = 17;
	final static private int TYPE_SHOW_STATUS_STR     = 18;
	final static private int TYPE_BACK_TO_LAST_SCR    = 19;
	final static private int TYPE_ACTIVATE_CL         = 20;
	final static private int TYPE_ACTIVATE_MM         = 21;
	final static private int TYPE_RESET_LOGIN_TIMER   = 22;

//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
	final static public int TYPE_UPDATE_BUDDYICON    = 24;
//  #sijapp cond.end#


	MainThread()
	{
		_this = this;
	}
	
	private static void addMainThreadTask(int taskId, Object[] data)
	{
		int dataLen = (data != null) ? data.length : 0;
		Object[] packed = new Object[dataLen+1];
		packed[0] = new Integer(taskId);
		if (dataLen != 0) System.arraycopy(data, 0, packed, 1, data.length);
		synchronized (mainThreadTasks) { mainThreadTasks.addElement(packed); }
		Jimm.display.callSerially(_this);
	}

	
	/* Method run contains operations which have to be synchronized
	 with main events queue (in main thread)
	 If you want your code run in main thread, make new constant 
	 beginning of TYPE_ and write your source to switch block of 
	 RunnableImpl.run method.
	 To run you source call RunnableImpl.callSerially()
	 Note RunnableImpl.callSerially NEVER blocks calling thread */
	
	public void run()
	{
		Object[][] tasksArray;
		
		synchronized (mainThreadTasks)
		{
			tasksArray = new Object[mainThreadTasks.size()][];
			mainThreadTasks.copyInto(tasksArray);
			mainThreadTasks.removeAllElements();
		}
		
		for (int i = 0; i < tasksArray.length; i++)
		{
			Object[] task = tasksArray[i];
			int mode = ((Integer)task[0]).intValue();
			Object[] taskData = new Object[task.length-1];
			System.arraycopy(task, 1, taskData, 0, taskData.length);
			try
			{
				execureTask(mode, taskData);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static void execureTask(int type, Object[] data)
	{
		switch (type)
		{
//#sijapp cond.if target isnot "DEFAULT"#
		case TYPE_USER_IS_TYPING:
			ContactList.BeginTyping((String) data[0], getBoolean(data, 1));
			break;
//#sijapp cond.end#

		case TYPE_ADD_MSG:
			boolean isChecked = ContactList.addMessage((Message) data[0]);
//#sijapp cond.if target isnot "DEFAULT"#
			if (isChecked) {
				int vibraKind = Options.getInt(Options.OPTION_VIBRATOR);
				if (vibraKind == 2) {
					vibraKind = SplashCanvas.locked() ? 1 : 0;
				}
				if (vibraKind > 0) {
					Jimm.display.vibrate(500);
				}
			}
//#sijapp cond.end#			
			break;

		case TYPE_USER_OFFLINE:
			ContactList.update((String) data[0], ContactList.STATUS_OFFLINE);
			break;

		case TYPE_SHOW_USER_INFO:
			JimmUI.showUserInfo((String[]) data[0]);
			break;

		case TYPE_UPDATE_CL_CAPTION:
			//#sijapp cond.if modules_TRAFFIC="true"#
			ContactList.updateTitle(Traffic.getSessionTraffic());
			//#sijapp cond.else#
			//#			ContactList.updateTitle(0);
			//#sijapp cond.end#
			break;

		case TYPE_UPDATE_CONTACT_LIST:
			ContactList.update((String) data[0], getInt(data, 1), getInt(data, 2), (String) data[3],
					(byte[]) data[4], (byte[]) data[5], getInt(data, 6),
					getInt(data, 7), getInt(data, 8), getInt(data, 9), getInt(
							data, 10), getInt(data, 11), getInt(data, 12), getInt(data, 13)
							//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
							,(byte[]) data[14]
							//  #sijapp cond.end#
			);

			break;

		//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
		case TYPE_UPDATE_BUDDYICON:
			ContactList.update((String) data[0], (Image) data[1], (byte[]) data[2]);
			break;
		//  #sijapp cond.end#

		case TYPE_RESET_CONTACTS:
			ContactList.setStatusesOffline();
			break;
			
		case TYPE_SHOW_TIME:
			VirtualList.setBottomText((String)data[0]);
			break;
			
		case TYPE_ADD_CONTACT:
			ContactItem citem = (ContactItem)data[0];
			ContactList.addContactItem(citem);
			ChatHistory.updateChatIfExists(citem);
			break;
			
		case TYPE_MINUTE_TASK:
			Jimm.aaNextMinute();
			break;
			
		case TYPE_MESS_DELIVERED:
			ChatHistory.messageIsDelivered((String)data[0], getInt(data, 1));
			break;
			
		case TYPE_SHOW_LAST_VESR:
			JimmUI.internalShowLastVers();
			break;
			
		case TYPE_SHOW_STATUS_STR:
			JimmUI.showStatusMessage((String)data[0], (String)data[1]);
			break;
			
		case TYPE_BACK_TO_LAST_SCR:
			JimmUI.backToLastScreen();
			break;
			
		case TYPE_ACTIVATE_CL:
			Alert alert = (Alert)data[0];
			if (alert == null) ContactList.activateList();
			else ContactList.activateList(alert);
			break;
			
		case TYPE_ACTIVATE_MM:
			MainMenu.activate((Alert)data[0]);
			break;
			
		case TYPE_RESET_LOGIN_TIMER:
			ContactList.resetLoginTimer();
			break;
		}
	}

	static public void addMainThreadTask(int type, Object obj1)
	{
		addMainThreadTask(type, new Object[] { obj1 });
	}

	static public void addMainThreadTask(int type)
	{
		addMainThreadTask(type, null);
	}

	static public void addMainThreadTask(int type, Object obj1, Object obj2)
	{
		addMainThreadTask(type, new Object[] { obj1, obj2 });
	}

	///////////////////////////////////////////////////////////////////////////

	static public void updateContactListCaption()
	{
		addMainThreadTask(TYPE_UPDATE_CL_CAPTION);
	}

	static public void addMessageSerially(Object message)
	{
		addMainThreadTask(TYPE_ADD_MSG, message);
//#sijapp cond.if target is "MIDP2"#
		if (Options.getBoolean(Options.OPTION_BRING_UP)) Jimm.setMinimized(false);
//#sijapp cond.end #
	}

	static public void updateContactList(String uin, int status, int xStatus, String xStatusMessage,
			byte[] internalIP, byte[] externalIP, int dcPort, int dcType,
			int icqProt, int authCookie, int signon, int online, int idle, int regdate
//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
			, byte[] biHash
//#sijapp cond.end#
	)
	{
		Object[] arguments;
//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
		arguments = new Object[16];
//#sijapp cond.else#
		arguments = new Object[15];
//#sijapp cond.end#

		arguments[0] = uin;
		setInt(arguments, 1, status);
		setInt(arguments, 2, xStatus);
		arguments[3] = xStatusMessage;
		arguments[4] = internalIP;
		arguments[5] = externalIP;
		setInt(arguments, 6, dcPort);
		setInt(arguments, 7, dcType);
		setInt(arguments, 8, icqProt);
		setInt(arguments, 9, authCookie);
		setInt(arguments, 10, signon);
		setInt(arguments, 11, online);
		setInt(arguments, 12, idle);
		setInt(arguments, 13, regdate);
		//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
		arguments[14] = biHash;
		//#sijapp cond.end#

		addMainThreadTask(TYPE_UPDATE_CONTACT_LIST, arguments);
	}

	//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
	static public void updateBuddyIcon(String uin, Image image, byte[] biHashOfDone)
	{
		Object[] arguments = new Object[3];

		arguments[0] = uin;
		arguments[1] = image;
		arguments[2] = biHashOfDone;

		addMainThreadTask(TYPE_UPDATE_BUDDYICON, arguments);
	}
	//#sijapp cond.end#

	//#sijapp cond.if target isnot "DEFAULT"#
	static public void BeginTyping(String uin, boolean type)
	{
		Object[] args = new Object[2];
		args[0] = uin;
		setBoolean(args, 1, type);
		addMainThreadTask(TYPE_USER_IS_TYPING, args);
	}

	//#sijapp cond.end#

	static public void resetContactsOffline()
	{
		addMainThreadTask(TYPE_RESET_CONTACTS);
	}
	
	static public void showTime()
	{
		addMainThreadTask(TYPE_SHOW_TIME, Util.getDateString(true));
	}
	
	static public void addContact(ContactItem cItem)
	{
		addMainThreadTask(TYPE_ADD_CONTACT, cItem);
	}
	
	static public void minuteTask()
	{
		addMainThreadTask(TYPE_MINUTE_TASK);
	}
	
	static public void messageIsDelevered(String uin, int messId)
	{
		addMainThreadTask(TYPE_MESS_DELIVERED, uin, new Integer(messId));
	}
	
	static public void showLastJimmVers()
	{
		addMainThreadTask(TYPE_SHOW_LAST_VESR);
	}
	
	static public void showStatusString(String text, String uin)
	{
		addMainThreadTask(TYPE_SHOW_STATUS_STR, text, uin);
	}
	
	static public void backToLastScreenMT()
	{
		addMainThreadTask(TYPE_BACK_TO_LAST_SCR);
	}
	
	static public void activateContactListMT(Alert alert)
	{
		addMainThreadTask(TYPE_ACTIVATE_CL, alert);
	}
	
	static public void activateMainMenu(Alert alert)
	{
		addMainThreadTask(TYPE_ACTIVATE_MM, alert);
	}
	
	static public void resetLoginTimer()
	{
		addMainThreadTask(TYPE_RESET_LOGIN_TIMER);
	}
	
	static public void showUserInfo(String[] strData)
	{
		addMainThreadTask(TYPE_SHOW_USER_INFO, (Object)strData);
	}
	
	static public void userOffline(String uin)
	{
		addMainThreadTask(TYPE_USER_OFFLINE, uin);
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	static public void setBoolean(Object[] data, int index, boolean value)
	{
		data[index] = new Boolean(value);
	}

	static public boolean getBoolean(Object[] data, int index)
	{
		return (data[index] == null) ? false : ((Boolean) data[index])
				.booleanValue();
	}

	static public void setLong(Object[] data, int index, long value)
	{
		data[index] = new Long(value);
	}

	static public long getLong(Object[] data, int index)
	{
		return (data[index] == null) ? 0 : ((Long) data[index]).longValue();
	}

	static public void setInt(Object[] data, int index, int value)
	{
		data[index] = new Integer(value);
	}

	static public int getInt(Object[] data, int index)
	{
		return (data[index] == null) ? 0 : ((Integer) data[index]).intValue();
	}

}
