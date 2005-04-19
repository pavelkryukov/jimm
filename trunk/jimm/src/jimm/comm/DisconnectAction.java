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
 File: src/jimm/comm/DisconnectAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


package jimm.comm;


import jimm.JimmException;


public class DisconnectAction extends Action
{


	// Action states
	public static final int STATE_INIT_DONE = 1;


	/****************************************************************************/


	// Current action state
	private int state;


	// Returns true if STATE_CONNECTED is active
	public boolean isExecutable()
	{
		return (this.icq.isConnected());
	}


	// This is an exclusive command, so this returns true
	public boolean isExclusive()
	{
		return (true);
	}


	// Init action
	protected void init() throws JimmException
	{
		this.icq.resetServerCon();
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		// #sijapp cond.if modules_FILES is "true"#
		this.icq.resetPeerCon();
		// #sijapp cond.end#
		// #sijapp cond.end#
		this.state = DisconnectAction.STATE_INIT_DONE;
	}


	// Forwards received packet, returns true if packet was consumed
	protected boolean forward(Packet packet) throws JimmException
	{
		return (false);
	}


	// Returns true if the action is completed
	public boolean isCompleted()
	{
		return (this.state == DisconnectAction.STATE_INIT_DONE);
	}


	// Returns true if an error has occured
	public boolean isError()
	{
		return (false);
	}


}
