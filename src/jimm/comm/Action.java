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
 File: src/jimm/comm/Action.java
 Version: 0.3.1  Date: 2004/12/25
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm.comm;


import jimm.JimmException;


public abstract class Action
{


	// ICQ object
	protected Icq icq;


	// Set ICQ object
	protected void setIcq(Icq icq)
	{
		this.icq = icq;
	}


	// Returns true if the action can be performed
	public abstract boolean isExecutable();


	// Returns true if this is an exclusive command
	public abstract boolean isExclusive();


	// Init action
	protected abstract void init() throws JimmException;


	// Forwards received packet, returns true if packet was consumed
	protected abstract boolean forward(Packet packet) throws JimmException;


	// Returns true if the action is completed
	public abstract boolean isCompleted();


	// Returns ture if an error has occured
	public abstract boolean isError();


	// Returns a number between 0 and 100 (inclusive) which indicates the progress
	public int getProgress()
	{
		if (this.isCompleted())
			return (100);
		else
			return (0);
	}


}
