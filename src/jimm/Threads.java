/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2008  Jimm Project

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
 File: src/jimm/Threads.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package jimm;

import jimm.comm.Icq;

public class Threads implements Runnable
{
	final static public int TYPE_REQ_LAST_VESR = 1;
	final static public int TYPE_RECONNECT     = 2;
	
	private int type; 
	
	public Threads(int type)
	{
		this.type = type;
	}
	
	public void run()
	{
		switch (type)
		{
		case TYPE_REQ_LAST_VESR:
			JimmUI.internalReqLastVersThread();
			break;
			
		case TYPE_RECONNECT:
			if (!Icq.isDisconnected())
			{
				try {Thread.sleep(5000);} catch (Exception e) {}
				ContactList.beforeConnect();
				Icq.connect();
			}
			break;
		}
	}
	
	static public void requestLastJimmVers()
	{
		Threads ri = new Threads(TYPE_REQ_LAST_VESR);
		new Thread(ri).start();
	}
	
	static public void reconnect()
	{
		Threads ri = new Threads(TYPE_RECONNECT);
		new Thread(ri).start();
	}
	
		
	

}
