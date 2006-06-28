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

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;

//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
import javax.microedition.media.PlayerListener;
import javax.microedition.media.Player;
//#sijapp cond.end#

import jimm.comm.Message;
import jimm.ContactListContactItem;


public class RunnableImpl implements Runnable
{
	private int type;
	private Object[] data;
	private static MIDlet midlet;
	
	final static private int TYPE_ADD_MSG             = 1;
	final static public  int TYPE_SET_CAPTION         = 3;
	final static public  int TYPE_USER_OFFLINE        = 4;
	final static public  int TYPE_UPDATE_CONTACT_LIST = 5;
	final static public  int TYPE_SHOW_USER_INFO      = 6;
	final static public  int TYPE_UPDATE_CL_CAPTION   = 7;
	final static public  int TYPE_ADDCONTACT          = 8;
	final static public  int TYPE_USER_IS_TYPING      = 9;
	
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
			ContactList.BeginTyping((String)data[0], getBoolean(data,1));
			break;
			//#sijapp cond.end#
			
		case TYPE_ADD_MSG:
			ContactList.addMessage((Message)data[0], getBoolean(data, 1));
			break;
		
		case TYPE_USER_OFFLINE:
			ContactList.update((String)data[0], ContactList.STATUS_OFFLINE);
			break;
			
		case TYPE_SHOW_USER_INFO:
			JimmUI.showUserInfo((String[])data[0]);
			break;
			
		case TYPE_UPDATE_CL_CAPTION:
			//#sijapp cond.if modules_TRAFFIC="true"#
			ContactList.updateTitle(Traffic.getSessionTraffic());
			//#sijapp cond.else#
			ContactList.updateTitle(0);
			//#sijapp cond.end#
			break;
			
		case TYPE_UPDATE_CONTACT_LIST:
			ContactList.update
			(
				(String)data[0],
				getInt(data,  1),
				(byte[])data[2],
				(byte[])data[3],
				getInt(data,  4),
				getInt(data,  5),
				getInt(data,  6),
				getInt(data,  7),
				getInt(data,  8),
				getInt(data,  9),
				getInt(data, 10)
			);
			
			break;
			
		case TYPE_ADDCONTACT:
			ContactList.addContactItem((ContactListContactItem)data[0]);
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
		callSerially(type, new Object[] {obj1});
	}
	
	static public void callSerially(int type)
	{
		callSerially(type, null);
	}
	
	static public void callSerially(int type, Object obj1, Object obj2)
	{
		callSerially(type, new Object[] {obj1, obj2});
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	static public void updateContactListCaption()
	{
		callSerially(TYPE_UPDATE_CL_CAPTION);
	}
	
	static public void addMessageSerially(Object message)
	{
		boolean haveToBeepNow;
		
//#sijapp cond.if target is "MIDP2" #
		haveToBeepNow = Jimm.is_phone_SE();
//#sijapp cond.else#
		haveToBeepNow = false;
//#sijapp cond.end #
		
		callSerially(TYPE_ADD_MSG, message, new Boolean(!haveToBeepNow));
		if (haveToBeepNow) ContactList.playSoundNotification(ContactList.SOUND_TYPE_MESSAGE);
// #sijapp cond.if target is "MIDP2" #
		Jimm.setMinimized(false);
// #sijapp cond.end #
	}
	
	static public void updateContactList
	(
	   	String uin,
		int status,
		byte[] internalIP,
		byte[] externalIP,
		int dcPort,
		int dcType,
		int icqProt,
		int authCookie,
		int signon,
		int online,
		int idle
	)
	{
		Object[] arguments = new Object[12];
		
		arguments[0] = uin;
		setInt(arguments,  1, status      );
		arguments[2] = internalIP;
		arguments[3] = externalIP;
		setInt(arguments,  4, dcPort      );
		setInt(arguments,  5, dcType      );
		setInt(arguments,  6, icqProt     );
		setInt(arguments,  7, authCookie  );
		setInt(arguments,  8, signon      ); 
		setInt(arguments, 9, online      );
		setInt(arguments, 10, idle        );  
		
		callSerially(TYPE_UPDATE_CONTACT_LIST, arguments);
		ContactList.checkAndPlayOnlineSound(uin, status);
	}
	
	//#sijapp cond.if target isnot "DEFAULT"#
	static public void BeginTyping(String uin, boolean type)
	{
		Object[] args = new Object[2];
		args[0] = uin;
		setBoolean(args,1,type);
		callSerially(TYPE_USER_IS_TYPING, args);
	}
	//#sijapp cond.end#
	
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	static public void setBoolean(Object[] data, int index, boolean value)
	{
		data[index] = new Boolean(value);
	}
	
	static public boolean getBoolean(Object[] data, int index)
	{
		return (data[index] == null) ? false : ((Boolean)data[index]).booleanValue();
	}

	static public void setLong(Object[] data, int index, long value)
	{
		data[index] = new Long(value);
	}
	
	static public long getLong(Object[] data, int index)
	{
		return (data[index] == null) ? 0 : ((Long)data[index]).longValue();
	}

	static public void setInt(Object[] data, int index, int value)
	{
		data[index] = new Integer(value);
	}
	
	static public int getInt(Object[] data, int index)
	{
		return (data[index] == null) ? 0 : ((Integer)data[index]).intValue();
	}
	
	
}
