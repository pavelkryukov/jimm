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
 File: src/jimm/comm/KeepAliveTimerTask.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;


import jimm.Jimm;
import jimm.JimmException;
import jimm.Options;

import java.util.TimerTask;


public class KeepAliveTimerTask extends TimerTask
{

	// ICQ object
	protected Icq icq;


	// Set ICQ object
	protected void setIcq(Icq icq)
	{
		this.icq = icq;
	}


	// Send an alive packet
	public void run()
	{

		// If STATE_CONNECTED is active, we've already got an reference to the ICQ object and the corresponding option has been set, send an alive packet
		if ((this.icq != null) && this.icq.isConnected() && Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_KEEP_CONN_ALIVE))
		{

			// Instantiate and send an alive packet
			try
			{
				this.icq.c.sendPacket(new PingPacket());
			}
			catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical()) this.cancel();
			}

		}

	}


}
