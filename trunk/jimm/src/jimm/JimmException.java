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
 File: src/jimm/JimmException.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import jimm.util.ResourceBundle;
import jimm.comm.Icq;

import javax.microedition.lcdui.*;

public class JimmException extends Exception
{

	// Returns the error description for the given error code
	public static String getErrDesc(int errCode, int extErrCode)
	{
		String errDesc = ResourceBundle.getString("error_" + errCode);
		int ext = errDesc.indexOf("EXT");
		if (ext != -1)
			return (errDesc.substring(0, ext) + extErrCode + errDesc
					.substring(ext + 3));
		return errDesc;
	}

	/****************************************************************************/

	// True, if this is a critial exception
	protected boolean critical;

	// True, if an error message should be presented to the user
	protected boolean displayMsg;

	// #sijapp cond.if target!="DEFAULT"#
	// True, if this is an exceptuion for an peer connection
	protected boolean peer;

	//  #sijapp cond.end#

	private int _ErrCode;
	private int _ExtErrCode;

	public int getErrCode()
	{
		return _ErrCode;
	}
	public String getFullErrCode()
	{
		return "#" + _ErrCode + "." + _ExtErrCode;
	}

	// Constructs a critical JimmException
	public JimmException(int errCode, int extErrCode)
	{
		super(JimmException.getErrDesc(errCode, extErrCode));
		this._ErrCode = errCode;
		this._ExtErrCode = extErrCode;
		this.critical = true;
		this.displayMsg = true;
		//  #sijapp cond.if target!="DEFAULT" & modules_FILES="true"#
		this.peer = false;
		//  #sijapp cond.end#

	}

	// Constructs a non-critical JimmException
	public JimmException(int errCode, int extErrCode, boolean displayMsg)
	{
		super(JimmException.getErrDesc(errCode, extErrCode));
		this._ErrCode = errCode;
		this._ExtErrCode = extErrCode;
		this.critical = false;
		this.displayMsg = displayMsg;
		//  #sijapp cond.if target!="DEFAULT" & modules_FILES="true"#
		this.peer = false;
		//  #sijapp cond.end#
	}

	//  #sijapp cond.if target!="DEFAULT" & modules_FILES="true"#
	// Constructs a non-critical JimmException with peer info
	public JimmException(int errCode, int extErrCode, boolean displayMsg,
			boolean _peer)
	{
		super(JimmException.getErrDesc(errCode, extErrCode));
		this._ErrCode = errCode;
		this._ExtErrCode = extErrCode;
		this.critical = false;
		this.displayMsg = displayMsg;
		this.peer = _peer;
	}
	//  #sijapp cond.end#

	// Returns true if an error message should be presented to the user
	public boolean isDisplayMsg()
	{
		return (this.displayMsg);
	}

	// Returns true if this is a critical exception
	public boolean isCritical()
	{
		return (this.critical);
	}

	//  #sijapp cond.if target!="DEFAULT" & modules_FILES="true"#
	// Returns true if this is a peer exception
	public boolean isPeer()
	{
		return (this.peer);
	}
	//  #sijapp cond.end#

	// Exception handler
	public synchronized static Alert handleException(JimmException e)
	{

		// Critical exception
		if (e.isCritical())
		{
			// Reset comm. subsystem
			//  #sijapp cond.if target!="DEFAULT" & modules_FILES="true"#
			if (e.isPeer())
				Icq.resetPeerCon();
			else
				Icq.resetServerCon();
			//  #sijapp cond.else#
			//#			Icq.resetServerCon();
			//  #sijapp cond.end#

			boolean diconnFlag = Icq.isDisconnected(); 
			Icq.disconnect(true);
			Icq.setDisconnected(diconnFlag);

			Icq.resetServerCon();

			// Set offline status for all contacts and reset online counters 
			ContactList.setStatusesOffline();
			SplashCanvas.setStatusToDraw(JimmUI
					.getStatusImageIndex(ContactList.STATUS_OFFLINE));
			SplashCanvas.setErrFlag(true);

			Alert errorMsg = null;
			
			if (Icq.isNotCriticalConnectionError(e.getErrCode()) && 
					!Icq.isDisconnected() && 
					Icq.reconnect_attempts > 0)
			{
				int reconTotal = Options.getInt(Options.OPTION_RECONNECT_NUMBER);
				SplashCanvas.setLastErrCode(e.getFullErrCode()+" "+(reconTotal-Icq.reconnect_attempts+1)+"/"+reconTotal);
				Icq.reconnect_attempts--;
				
				System.out.println("err_code="+e.getFullErrCode());
				RunnableImpl.reconnect();
			}
			else
			{
				// Unlock splash (if locked)
				if (SplashCanvas.locked())
					SplashCanvas.unlock(true);

				// Display error message
				errorMsg = new Alert(ResourceBundle.getString("error"), e.getMessage(), null, AlertType.ERROR);
				errorMsg.setTimeout(Alert.FOREVER);
				RunnableImpl.activateMainMenu(errorMsg);
			}
			return (errorMsg);
		}
		// Non-critical exception
		else
		{

			// Display error message, if required
			if (e.isDisplayMsg())
			{
				Alert errorMsg = new Alert(ResourceBundle.getString("warning"),
						e.getMessage(), null, AlertType.WARNING);
				errorMsg.setTimeout(Alert.FOREVER);

				SplashCanvas.unlock(false);

				if (Icq.isConnected())
					RunnableImpl.activateContactListMT(errorMsg);
				else
					RunnableImpl.activateMainMenu(errorMsg);
				return (errorMsg);
			}
			return (null);
		}

	}
}
