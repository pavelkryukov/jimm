/*******************************************************************************
 Library of additional graphical screens for J2ME applications
 Copyright (C) 2003-08  Jimm Project

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
********************************************************************************
 File: src/jimm/comm/connections/Connection.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
*******************************************************************************/

package jimm.comm.connections;

import java.util.Vector;
import jimm.JimmException;
import jimm.comm.Packet;


public abstract class Connection implements Runnable
{
	private Object inputCloseFlagSynch = new Object();
	
	// Disconnect flags
	private volatile boolean inputCloseFlag;
	
	// Connection state
	protected volatile boolean state = false;		// true - connected, false - disconnected

	// Receiver thread
	protected volatile Thread rcvThread;

	// Received packets
	protected Vector rcvdPackets;

	// FLAP sequence number
	protected int flapSEQ;

	// Type of connection for exceptions handling
	protected int typeNetwork;

	// Opens a connection to the specified host and starts the receiver
	// thread
	public synchronized void connect(String hostAndPort)
			throws JimmException
	{

	}
	
	// Returns and updates sequence nr
	public synchronized int getFlapSequence()
	{
		flapSEQ = ++flapSEQ % 0x8000;
		return flapSEQ;
	}

	void setInputCloseFlag(boolean value)
	{
		synchronized (inputCloseFlagSynch) { inputCloseFlag = value; }
	}
	
	boolean getInputCloseFlag()
	{
		synchronized (inputCloseFlagSynch) { return inputCloseFlag; }
	}

	// Sets the reconnect flag and closes the connection
	final public void notifyToDisconnect()
	{
		setInputCloseFlag(true);
	}
	
	public abstract void forceDisconnect();

	// Return the connection state
	public synchronized boolean getState()
	{
		return (this.state);
	}

	// Returns the number of packets available
	public synchronized int available()
	{
		if (this.rcvdPackets == null)
		{
			return (0);
		} else
		{
			return (this.rcvdPackets.size());
		}
	}

	// Returns the next packet, or null if no packet is available
	public Packet getPacket() throws JimmException
	{

		// Request lock on packet buffer and get next packet, if available
		byte[] packet;
		synchronized (this.rcvdPackets)
		{
			if (this.rcvdPackets.size() == 0)
			{
				return (null);
			}
			packet = (byte[]) this.rcvdPackets.elementAt(0);
			this.rcvdPackets.removeElementAt(0);
		}

		// Parse and return packet
		return (Packet.parse(packet));

	}

	// Sends the specified packet always type 5 (FLAP packet)
	public void sendPacket(Packet packet) throws JimmException
	{
	}

	//  #sijapp cond.if target!="DEFAULT" & modules_FILES="true"#

	// Return the port this connection is running on
	public int getLocalPort()
	{
		return (0);
	}

	// Return the ip this connection is running on
	public byte[] getLocalIP()
	{
		return (new byte[4]);
	}

	//  #sijapp cond.end#

	// Main loop
	public void run()
	{

	}

	public boolean haveToSetNullAfterDisconnect()
	{
		return true;
	}

}

