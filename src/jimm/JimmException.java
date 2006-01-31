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

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Displayable;


public class JimmException extends Exception
{


	// Returns the error description for the given error code
	public static String getErrDesc(int errCode, int extErrCode)
	{
		String errDesc = ResourceBundle.getString("error_" + errCode);
		//if (errDesc.endsWith("EXT"))
		//{
			if (errDesc == null) errDesc = ResourceBundle.getString("error_100");
			int ext = errDesc.indexOf("EXT");
			return (errDesc.substring(0, ext) + extErrCode + errDesc.substring(ext + 3));
		//}
		//else
		//	return errDesc;
	}


	/****************************************************************************/


	// True, if this is a critial exception
	protected boolean critical;


	// True, if an error message should be presented to the user
	protected boolean displayMsg;
	
	//  #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	// True, if this is an exceptuion for an peer connection
	protected boolean peer;
	//  #sijapp cond.end#


	// Constructs a critical JimmException
	public JimmException(int errCode, int extErrCode)
	{
		super(JimmException.getErrDesc(errCode, extErrCode));
		this.critical = true;
		this.displayMsg = true;
		//  #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		//  #sijapp cond.if modules_FILES is "true"#
		this.peer = false;
		//  #sijapp cond.end#
		//  #sijapp cond.end#
	}


	// Constructs a non-critical JimmException
	public JimmException(int errCode, int extErrCode, boolean displayMsg)
	{
		super(JimmException.getErrDesc(errCode, extErrCode));
		this.critical = false;
		this.displayMsg = displayMsg;
		//  #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		//  #sijapp cond.if modules_FILES is "true"#
		this.peer = false;
		//  #sijapp cond.end#
		//  #sijapp cond.end#
	}
	
	//  #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	//  #sijapp cond.if modules_FILES is "true"#
	// Constructs a non-critical JimmException with peer info
	public JimmException(int errCode, int extErrCode, boolean displayMsg, boolean _peer)
	{
		super(JimmException.getErrDesc(errCode, extErrCode));
		this.critical = false;
		this.displayMsg = displayMsg;
		this.peer = _peer;
	}
	//  #sijapp cond.end#
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
	
	//  #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	//  #sijapp cond.if modules_FILES is "true"#
	// Returns true if this is a peer exception
	public boolean isPeer()
	{
		return (this.peer);
	}
	//  #sijapp cond.end#
	//  #sijapp cond.end#


	// Exception handler
	public synchronized static Alert handleException(JimmException e)
	{

		// Critical exception
	    if (e.isCritical())
		{

			// Reset comm. subsystem
			//  #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	    	//  #sijapp cond.if modules_FILES is "true"#
			if (e.isPeer())
			    Icq.resetPeerCon();
			else
			    Icq.resetServerCon();
			//  #sijapp cond.else#
			Icq.resetServerCon();
			//  #sijapp cond.end#
			//  #sijapp cond.else#
			Icq.resetServerCon();
			//  #sijapp cond.end#
			
			if (SplashCanvas.locked()) SplashCanvas.unlock();

			// Display error message
			Alert errorMsg = new Alert(ResourceBundle.getString("error"), e.getMessage(), null, AlertType.ERROR);
			errorMsg.setTimeout(Alert.FOREVER);
			MainMenu.activate(errorMsg);
			return(errorMsg);

		}
		// Non-critical exception
		else
		{

			// Display error message, if required
			if (e.isDisplayMsg())
			{
				Alert errorMsg = new Alert(ResourceBundle.getString("warning"), e.getMessage(), null, AlertType.WARNING);
				errorMsg.setTimeout(Alert.FOREVER);
				if (Jimm.display.getCurrent() instanceof Alert)		
					Jimm.display.setCurrent(errorMsg);
				else
					Jimm.display.setCurrent(errorMsg, Jimm.display.getCurrent());
				return(errorMsg);
			}
		return(null);
		}

	}


}
