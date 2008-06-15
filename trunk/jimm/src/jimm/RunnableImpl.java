/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2005-06  Jimm Project

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

import java.util.TimerTask;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;

import DrawControls.VirtualList;
import jimm.comm.Icq;
import jimm.comm.Message;
import jimm.comm.Util;
import jimm.ContactItem;

public class RunnableImpl implements Runnable
{
	private int type;

	private Object[] data;

	private static MIDlet midlet;

	final static private int TYPE_ADD_MSG            = 1;
	final static public int TYPE_SET_CAPTION         = 3;
	final static public int TYPE_USER_OFFLINE        = 4;
	final static public int TYPE_UPDATE_CONTACT_LIST = 5;
	final static public int TYPE_SHOW_USER_INFO      = 6;
	final static public int TYPE_UPDATE_CL_CAPTION   = 7;
	final static public int TYPE_USER_IS_TYPING      = 9;
	final static public int TYPE_RESET_CONTACTS      = 10;
	final static public int TYPE_SHOW_TIME           = 11;
	final static public int TYPE_ADD_CONTACT         = 12;
	final static public int TYPE_MINUTE_TASK         = 14;
	final static public int TYPE_MESS_DELIVERED      = 15;
	final static public int TYPE_REQ_LAST_VESR       = 16;
	final static public int TYPE_SHOW_LAST_VESR      = 17;
	final static public int TYPE_SHOW_STATUS_STR     = 18;
	final static public int TYPE_BACK_TO_LAST_SCR    = 19;
	final static public int TYPE_ACTIVATE_CL         = 20;
	final static public int TYPE_ACTIVATE_MM         = 21;
	final static public int TYPE_RESET_LOGIN_TIMER   = 22;
	final static public int TYPE_RECONNECT           = 23;

	RunnableImpl(int type, Object[] data)
	{
		this.type = type;
		this.data = data;
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
		switch (type)
		{
//#sijapp cond.if target isnot "DEFAULT"#
		case TYPE_USER_IS_TYPING:
			ContactList.BeginTyping((String) data[0], getBoolean(data, 1));
			break;
//#sijapp cond.end#

		case TYPE_ADD_MSG:
//#sijapp cond.if target isnot "DEFAULT"#			
			int vibraKind = Options.getInt(Options.OPTION_VIBRATOR);
			if (vibraKind == 2) vibraKind = SplashCanvas.locked() ? 1 : 0;
			if (vibraKind > 0) Jimm.display.vibrate(500);
//#sijapp cond.end#			
			ContactList.addMessage((Message) data[0]);
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
			ContactList.update((String) data[0], getInt(data, 1), getInt(data, 2),
					(byte[]) data[3], (byte[]) data[4], getInt(data, 5),
					getInt(data, 6), getInt(data, 7), getInt(data, 8), getInt(
							data, 9), getInt(data, 10), getInt(data, 11));

			break;

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
			
		case TYPE_REQ_LAST_VESR:
			JimmUI.internalReqLastVersThread();
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
			
		case TYPE_RECONNECT:
			try {Thread.sleep(5000);} catch (Exception e) {}
			if (!Icq.isDisconnected())
			{
				ContactList.beforeConnect();
				Icq.connect();
			}
			break;
		}
	}

	static public void setMidlet(MIDlet midlet_)
	{
		midlet = midlet_;
	}

	synchronized static public void callSerially(int type, Object[] data)
	{
		Display.getDisplay(midlet).callSerially(new RunnableImpl(type, data));
	}

	static public void callSerially(int type, Object obj1)
	{
		callSerially(type, new Object[] { obj1 });
	}

	static public void callSerially(int type)
	{
		callSerially(type, null);
	}

	static public void callSerially(int type, Object obj1, Object obj2)
	{
		callSerially(type, new Object[] { obj1, obj2 });
	}

	///////////////////////////////////////////////////////////////////////////

	static public void updateContactListCaption()
	{
		callSerially(TYPE_UPDATE_CL_CAPTION);
	}

	static public void addMessageSerially(Object message)
	{
		callSerially(TYPE_ADD_MSG, message);
//#sijapp cond.if target is "MIDP2"#
		if (Options.getBoolean(Options.OPTION_BRING_UP)) Jimm.setMinimized(false);
//#sijapp cond.end #
	}

	static public void updateContactList(String uin, int status, int xStatus,
			byte[] internalIP, byte[] externalIP, int dcPort, int dcType,
			int icqProt, int authCookie, int signon, int online, int idle)
	{
		Object[] arguments = new Object[13];

		arguments[0] = uin;
		setInt(arguments, 1, status);
		setInt(arguments, 2, xStatus);
		arguments[3] = internalIP;
		arguments[4] = externalIP;
		setInt(arguments, 5, dcPort);
		setInt(arguments, 6, dcType);
		setInt(arguments, 7, icqProt);
		setInt(arguments, 8, authCookie);
		setInt(arguments, 9, signon);
		setInt(arguments, 10, online);
		setInt(arguments, 11, idle);

		callSerially(TYPE_UPDATE_CONTACT_LIST, arguments);
	}

	//#sijapp cond.if target isnot "DEFAULT"#
	static public void BeginTyping(String uin, boolean type)
	{
		Object[] args = new Object[2];
		args[0] = uin;
		setBoolean(args, 1, type);
		callSerially(TYPE_USER_IS_TYPING, args);
	}

	//#sijapp cond.end#

	static public void resetContactsOffline()
	{
		callSerially(TYPE_RESET_CONTACTS);
	}
	
	static public void showTime()
	{
		callSerially(TYPE_SHOW_TIME, Util.getDateString(true));
	}
	
	static public void addContact(ContactItem cItem)
	{
		callSerially(TYPE_ADD_CONTACT, cItem);
	}
	
	static public void minuteTask()
	{
		callSerially(TYPE_MINUTE_TASK);
	}
	
	static public void messageIsDelevered(String uin, int messId)
	{
		callSerially(TYPE_MESS_DELIVERED, uin, new Integer(messId));
	}
	
	static public void requestLastJimmVers()
	{
		RunnableImpl ri = new RunnableImpl(TYPE_REQ_LAST_VESR, null);
		new Thread(ri).start();
	}
	
	static public void showLastJimmVers()
	{
		callSerially(TYPE_SHOW_LAST_VESR);
	}
	
	static public void showStatusString(String text, String uin)
	{
		callSerially(TYPE_SHOW_STATUS_STR, text, uin);
	}
	
	static public void backToLastScreenMT()
	{
		callSerially(TYPE_BACK_TO_LAST_SCR);
	}
	
	static public void activateContactListMT(Alert alert)
	{
		callSerially(TYPE_ACTIVATE_CL, alert);
	}
	
	static public void activateMainMenu(Alert alert)
	{
		callSerially(TYPE_ACTIVATE_MM, alert);
	}
	
	static public void resetLoginTimer()
	{
		callSerially(TYPE_RESET_LOGIN_TIMER);
	}
	
	static public void reconnect()
	{
		RunnableImpl ri = new RunnableImpl(TYPE_RECONNECT, null);
		new Thread(ri).start();
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
