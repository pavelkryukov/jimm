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
 File: src/jimm/comm/connections/PeerConnection.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
*******************************************************************************/

package jimm.comm.connections;

//#sijapp cond.if target!="DEFAULT" & modules_FILES="true"#


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import jimm.ContactList;
import jimm.JimmException;
import jimm.Options;
import jimm.RunnableImpl;
import jimm.comm.Icq;
import jimm.comm.Packet;
import jimm.comm.Util;

//#sijapp cond.if modules_TRAFFIC is "true" #
import jimm.Traffic;
//#sijapp cond.end#

// PeerConnection
public class PeerConnection implements Runnable
{

	// Connection variables
	private SocketConnection sc;

	private InputStream is;

	private OutputStream os;

	// Disconnect flags
	private volatile boolean inputCloseFlag;

	// Receiver thread
	private volatile Thread rcvThread;

	// Received packets
	private Vector rcvdPackets;

	// Type of connection for exceptions handling
	protected int typeNetwork;

	public PeerConnection ()
	{
		this.typeNetwork = JimmException.ICQ_PEER;
	}

	// Opens a connection to the specified host and starts the receiver
	// thread
	public synchronized void connect(String hostAndPort)
			throws JimmException
	{
		try
		{
			//#sijapp cond.if modules_DEBUGLOG is "true" #
			System.out.println("Peer conn: connecting to socket://"
					+ hostAndPort);
			//#sijapp cond.end #

			this.sc = (SocketConnection) Connector.open("socket://"
					+ hostAndPort, Connector.READ_WRITE);
			this.is = this.sc.openInputStream();
			this.os = this.sc.openOutputStream();

			this.inputCloseFlag = false;

			this.rcvThread = new Thread(this);
			this.rcvThread.start();

		} catch (ConnectionNotFoundException e)
		{
			throw (new JimmException(126, 0, true, true));
		} catch (IllegalArgumentException e)
		{
			throw (new JimmException(127, 0, true, true));
		} catch (IOException e)
		{
			throw (new JimmException(125, 0, true, true));
		} catch (SecurityException e)
		{
			throw (new JimmException(119, 0));
		}
	}

	// Sets the reconnect flag and closes the connection
	public synchronized void close()
	{
		this.inputCloseFlag = true;

		try
		{
			this.is.close();
		} catch (Exception e)
		{ /* Do nothing */
		} finally
		{
			this.is = null;
		}

		try
		{
			this.os.close();
		} catch (Exception e)
		{ /* Do nothing */
		} finally
		{
			this.os = null;
		}

		try
		{
			this.sc.close();
		} catch (Exception e)
		{ /* Do nothing */
		} finally
		{
			this.sc = null;
		}

		Thread.yield();
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

	// Sends the specified packet
	public void sendPacket(Packet packet) throws JimmException
	{

		// Throw exception if output stream is not ready
		if (this.os == null)
		{
			throw (new JimmException(128, 0, true, true));
		}

		// Request lock on output stream
		synchronized (this.os)
		{

			// Send packet and count the bytes
			try
			{
				byte[] outpack = packet.toByteArray();
				this.os.write(outpack);
				this.os.flush();
				// System.out.println("Peer packet sent length: "+outpack.length);
				//#sijapp cond.if modules_TRAFFIC is "true" #

				// 51 is the overhead for each packet
				Traffic.addTraffic(outpack.length + 51);
				if (ContactList.getVisibleContactListRef().isActive())
				{
					RunnableImpl.updateContactListCaption();
					Traffic.trafficScreen.update(false);
				}
				//#sijapp cond.end#
			} catch (IOException e)
			{
				this.close();
			}

		}

	}

	// Retun the port this connection is running on
	public int getLocalPort()
	{
		try
		{
			return (this.sc.getLocalPort());
		} catch (IOException e)
		{
			return (0);
		}
	}

	// Retun the ip this connection is running on
	public byte[] getLocalIP()
	{
		try
		{
			return (Util.ipToByteArray(this.sc.getLocalAddress()));
		} catch (IOException e)
		{
			return (new byte[4]);
		}
	}

	// Main loop
	public void run()
	{

		// Required variables
		byte[] dcLength = new byte[2];
		byte[] rcvdPacket;
		int bRead, bReadSum;

		// Reset packet buffer
		synchronized (this)
		{
			this.rcvdPackets = new Vector();
		}

		// Try
		try
		{

			// Check abort condition
			while (!this.inputCloseFlag)
			{

				// Read flap header
				bReadSum = 0;
				if (Options.getInt(Options.OPTION_CONN_PROP) == 1)
				{
					while (is.available() == 0)
						Thread.sleep(250);
					if (is == null)
						break;
				}
				do
				{
					bRead = this.is.read(dcLength, bReadSum,
							dcLength.length - bReadSum);
					if (bRead == -1)
						break;
					bReadSum += bRead;
				} while (bReadSum < dcLength.length);
				if (bRead == -1)
					break;

				// Allocate memory for flap data
				rcvdPacket = new byte[Util.getWord(dcLength, 0, false)];

				// Read flap data
				bReadSum = 0;
				do
				{
					bRead = this.is.read(rcvdPacket, bReadSum,
							rcvdPacket.length - bReadSum);
					if (bRead == -1)
						break;
					bReadSum += bRead;
				} while (bReadSum < rcvdPacket.length);
				if (bRead == -1)
					break;

				//#sijapp cond.if modules_TRAFFIC is "true" #
				Traffic.addTraffic(bReadSum + 53);

				// 42 is the overhead for each packet (2 byte packet length)
				if (ContactList.getVisibleContactListRef().isActive())
				{
					RunnableImpl.updateContactListCaption();
					Traffic.trafficScreen.update(false);
				}
				//#sijapp cond.end#

				// Lock object and add rcvd packet to vector
				synchronized (this.rcvdPackets)
				{
					this.rcvdPackets.addElement(rcvdPacket);
				}

				// Notify main loop
				synchronized (Icq.getWaitObj())
				{
					Icq.getWaitObj().notify();
				}
			}

		}
		// Catch communication exception
		catch (NullPointerException e)
		{
			if (!this.inputCloseFlag)
			{
				// Construct and handle exception
				JimmException f = new JimmException(125, 3, true, true);
				JimmException.handleException(f);
			} else
			{ /* Do nothing */
			}
		}
		// Catch InterruptedException
		catch (InterruptedException e)
		{ /* Do nothing */
		}
		// Catch IO exception
		catch (IOException e)
		{
			if (!this.inputCloseFlag)
			{
				// Construct and handle exception
				JimmException f = new JimmException(125, 1, true, true);
				JimmException.handleException(f);
			} else
			{ /* Do nothing */
			}
		} catch (SecurityException e)
		{
			// Construct and handle exception
			JimmException f = new JimmException(119, 1, true, true);
			JimmException.handleException(f);
		}
		finally
		{
			this.close();
		}
		
	}

}

//#sijapp cond.end#

