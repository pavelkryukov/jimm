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
 File: src/jimm/JimmException.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm;


import jimm.util.ResourceBundle;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;


public class JimmException extends Exception
{


	// Returns the error description for the given error code
	public static String getErrDesc(int errCode, int extErrCode)
	{
		String errDesc = ResourceBundle.getString("error_" + errCode);
		if (errDesc == null) errDesc = ResourceBundle.getString("error_100");
		int ext = errDesc.indexOf("EXT");
		return (errDesc.substring(0, ext) + extErrCode + errDesc.substring(ext + 3));
	}


	/****************************************************************************/


	// True, if this is a critial exception
	protected boolean critical;


	// True, if an error message should be presented to the user
	protected boolean displayMsg;


	// Constructs a critical JimmException
	public JimmException(int errCode, int extErrCode)
	{
		super(JimmException.getErrDesc(errCode, extErrCode));
		this.critical = true;
		this.displayMsg = true;
	}


	// Constructs a non-critical JimmException
	public JimmException(int errCode, int extErrCode, boolean displayMsg)
	{
		super(JimmException.getErrDesc(errCode, extErrCode));
		this.critical = false;
		this.displayMsg = displayMsg;
	}


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


	// Exception handler
	public synchronized static void handleException(JimmException e)
	{

		// Critical exception
		if (e.isCritical())
		{

			// Reset comm. subsystem
			Jimm.jimm.getIcqRef().reset();

			// Display error message
			Alert errorMsg = new Alert(ResourceBundle.getString("error"), e.getMessage(), null, AlertType.ERROR);
			errorMsg.setTimeout(Alert.FOREVER);
			Jimm.jimm.getMainMenuRef().activate(errorMsg);

		}
		// Non-critical exception
		else
		{

			// Display error message, if required
			if (e.isDisplayMsg())
			{
				Alert errorMsg = new Alert(ResourceBundle.getString("warning"), e.getMessage(), null, AlertType.WARNING);
				errorMsg.setTimeout(Alert.FOREVER);
				Jimm.display.setCurrent(errorMsg, Jimm.display.getCurrent());
			}

		}

	}


}
