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
 File: src/jimm/comm/Icq.java
 Version: ###VERSION###  Date: ###DATE###
 Author: Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
import javax.microedition.io.SocketConnection;
//#sijapp cond.else#
//# import javax.microedition.io.StreamConnection;
//#sijapp cond.end#

import jimm.ContactItem;
import jimm.Jimm;
import jimm.JimmUI;
import jimm.JimmException;
import jimm.Options;
import jimm.SplashCanvas;
import jimm.util.ResourceBundle;
import jimm.ContactList;
import jimm.RunnableImpl;
import jimm.TimerTasks;

//#sijapp cond.if modules_TRAFFIC is "true" #
import jimm.Traffic;

//#sijapp cond.end#

public class Icq implements Runnable
{
	private static Icq _this;

	public static final byte[] MTN_PACKET_BEGIN =
	{ (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 };
	
	// Current state
	static private boolean connected = false;

	// Requested actions
	static private Vector reqAction = new Vector();

	// Thread
	static volatile Thread thread;

	// FLAP sequence number
	static int flapSEQ;

	// Current visibility mode
	static private int currentVisibility;

	static String lastStatusChangeTime;

	public Icq()
	{
		_this = this;
		// Set starting point for seq numbers (not bigger then 0x8000)
		Random rand = new Random(System.currentTimeMillis());
		flapSEQ = rand.nextInt() % 0x8000;
	}
	
	static
	{
		initClientIndData();
	}

	// Request an action
	static public void requestAction(Action act) throws JimmException
	{
		// Set reference to this ICQ object for callbacks
		act.setIcq(_this);

		// Look whether action is executable at the moment
		if (!act.isExecutable())
		{
			throw (new JimmException(140, 0));
		}

		// Queue requested action
		synchronized (_this)
		{
			reqAction.addElement(act);
		}

		// Connect?
		if ((act instanceof ConnectAction) || (act instanceof RegisterNewUinAction))
		{
			// Create new thread and start
			thread = new Thread(_this);
			thread.start();

		}

		// Notify main loop
		synchronized (wait)
		{
			wait.notify();
		}

	}

	// Connects to the ICQ network
	static public synchronized void connect()
	{
		Icq.connecting = true;
		//#sijapp cond.if target isnot "MOTOROLA"#
		if (Options.getBoolean(Options.OPTION_SHADOW_CON))
		{
			// Make the shadow connection for Nokia 6230 of other devices if
			// needed
			ContentConnection ctemp = null;
			try
			{
				String url = "http://shadow.jimm.org/";
				ctemp = (ContentConnection) Connector.open(url);

				ctemp.openDataInputStream();
			} catch (Exception e)
			{
				// Do nothing
			}
		}
		//#sijapp cond.end#
		// Connect
		ConnectAction act = new ConnectAction(Options
				.getString(Options.OPTION_UIN), Options
				.getString(Options.OPTION_PASSWORD), Options
				.getString(Options.OPTION_SRV_HOST), Options
				.getString(Options.OPTION_SRV_PORT));
		try
		{
			requestAction(act);

		} catch (JimmException e)
		{
			if (!reconnect(e))
			{
				JimmException.handleException(e);
			}
		}

		SplashCanvas.setStatusToDraw(jimm.JimmUI.getStatusImageIndex(Options
				.getLong(Options.OPTION_ONLINE_STATUS)));
		// Start timer
		SplashCanvas.addTimerTask("connecting", act, true);

		lastStatusChangeTime = Util.getDateString(true);
	}


	// Connects to the ICQ network for register new uin
	static public synchronized void connectForNewUIN(String newPassword)
	{
		// Connect
		RegisterNewUinAction act = new RegisterNewUinAction(newPassword, Options
					.getString(Options.OPTION_SRV_HOST), Options
					.getString(Options.OPTION_SRV_PORT));
		try
		{
			requestAction(act);

		} catch (JimmException e)
		{
			JimmException.handleException(e);
		}

		RegisterNewUinAction.addTimerTask (act);
		lastStatusChangeTime = Util.getDateString(true);
	}
	
	static public synchronized void removeAllActions()
	{
		if (reqAction != null)
			reqAction.removeAllElements();
	}

	/* Disconnects from the ICQ network */
	static public synchronized void disconnect(boolean force)
	{
		if (c == null) return;
		
		thread = null;
		synchronized (wait) { wait.notifyAll(); }		
		
		if (force) c.forceDisconnect();
		else c.notifyToDisconnect();

		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
		//#sijapp cond.if modules_FILES is "true"#
		resetPeerCon();
		//#sijapp cond.end#
		//#sijapp cond.end#

		//#sijapp cond.if modules_TRAFFIC is "true" #
		try
		{
			Traffic.save();
		} catch (Exception e)
		{ /* Do nothing */
		}
		//#sijapp cond.end#

		/* Reset all contacts offine */
		RunnableImpl.resetContactsOffline();
		
		setNotConnected();
		
		if (c.haveToSetNullAfterDisconnect()) c = null;
	}

	static public void setVisibility(int value)
	{
		currentVisibility = value;
	}

	static public int getVisibility()
	{
		return currentVisibility;
	}

	// Checks whether the comm. subsystem is in STATE_NOT_CONNECTED
	static public synchronized boolean isNotConnected()
	{
		return !connected;
	}

	// Puts the comm. subsystem into STATE_NOT_CONNECTED
	static protected synchronized void setNotConnected()
	{
		connected = false;
	}

	// Checks whether the comm. subsystem is in STATE_CONNECTED
	static public synchronized boolean isConnected()
	{
		return connected;
	}

	// Puts the comm. subsystem into STATE_CONNECTED
	static protected synchronized void setConnected()
	{
		SplashCanvas.setLastErrCode(null);
		Icq.reconnect_attempts = Options
				.getInt(Options.OPTION_RECONNECT_NUMBER);
		connected = true;
	}

	// Returns and updates sequence nr
	static public int getFlapSequence()
	{
		flapSEQ = ++flapSEQ % 0x8000;
		return flapSEQ;
	}

	// Resets the comm. subsystem
	static public synchronized void resetServerCon()
	{
		// Wake up thread in order to complete
		synchronized (Icq.wait)
		{
			Icq.wait.notify();
		}

		// Reset all variables
		connected = false;

		// Reset all timer tasks
		Jimm.jimm.cancelTimer();

		// Delete all actions
		if (actAction != null)
		{
			actAction.removeAllElements();
		}
		if (reqAction != null)
		{
			reqAction.removeAllElements();
		}

	}

	//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
	//#sijapp cond.if modules_FILES is "true"#
	// Resets the comm. subsystem
	static public synchronized void resetPeerCon()
	{
		// Close connection
		peerC = null;
	}

	//#sijapp cond.end#
	//#sijapp cond.end#

	/** *********************************************************************** */
	/** *********************************************************************** */
	/** *********************************************************************** */

	// Wait object
	static private Object wait = new Object();

	// Connection to the ICQ server
	static Connection c;

	// Connection to peer
//#sijapp cond.if target!="DEFAULT" & modules_FILES="true"#
	static PeerConnection peerC;
//#sijapp cond.end#

	// All currently active actions
	static private Vector actAction;

	// Action listener
	static private ActionListener actListener;

	// Keep alive timer task
	static private TimerTasks keepAliveTimerTask;

	public static int reconnect_attempts;
	
	public static void sendPacket(Packet packet) throws JimmException
	{
		if (c == null) return; // TODO: may be better to thow exception?
		c.sendPacket(packet);
	}
	
	public static void connect(String data) throws JimmException
	{
		if (c == null) return; // TODO: may be better to thow exception?
		c.connect(data);
	}

	// Main loop
	public void run()
	{
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
		//#sijapp cond.if modules_FILES is "true"#
		// Is a DC packet Available
		boolean dcPacketAvailable;
		//#sijapp cond.end#
		//#sijapp cond.end#

		// Get thread object
		Thread thread = Thread.currentThread();
		// Required variables
		Action newAction = null;

		// Instantiate connections
		if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_SOCKET)
			c = new SOCKETConnection();
		else if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_HTTP)
			c = new HTTPConnection();
		//#sijapp cond.if modules_PROXY is "true"#
		else if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_PROXY)
			c = new SOCKSConnection();
		//#sijapp cond.end#

		// Instantiate active actions vector
		actAction = new Vector();

		// Instantiate action listener
		actListener = new ActionListener();

		keepAliveTimerTask = new TimerTasks(TimerTasks.ICQ_KEEPALIVE);
		long keepAliveInterv = Integer.parseInt(Options
				.getString(Options.OPTION_CONN_ALIVE_INVTERV)) * 1000;
		Jimm.getTimerRef().schedule(keepAliveTimerTask, keepAliveInterv,
				keepAliveInterv);

		// Catch JimmExceptions
		try
		{
			// Abort only in error state
			while (Icq.thread == thread)
			{
				// Get next action
				synchronized (this)
				{
					if (reqAction.size() > 0)
					{
						if ((actAction.size() == 1)
								&& ((Action) actAction.elementAt(0))
										.isExclusive())
						{
							newAction = null;
						} else
						{
							if (reqAction != null && reqAction.size() != 0)
								newAction = (Action) reqAction.elementAt(0);
							if (((actAction.size() > 0) && newAction
									.isExclusive())
									|| (!newAction.isExecutable()))
							{
								newAction = null;
							} else
							{
								if (reqAction != null && reqAction.size() != 0)
									reqAction.removeElementAt(0);
							}
						}
					} else
					{
						newAction = null;
					}
				}

				// Wait if a new action does not exist
				if ((newAction == null) && (c.available() == 0))
				{
					try
					{
						synchronized (wait)
						{
							wait.wait(/*Icq.STANDBY*/);
						}
					} catch (InterruptedException e)
					{
						// Do nothing
					}

				}
				// Initialize action
				else if (newAction != null)
				{
					try
					{
						newAction.init();
						actAction.addElement(newAction);
					} catch (JimmException e)
					{
						if (!reconnect(e))
							JimmException.handleException(e);
						if (e.isCritical())
							throw (e);
					}
				}

				// Set dcPacketAvailable to true if the peerC is not null and
				// there is an packet waiting
				//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
				//#sijapp cond.if modules_FILES is "true"#
				if (peerC != null)
				{
					if (peerC.available() > 0)
						dcPacketAvailable = true;
					else
						dcPacketAvailable = false;
				} else
					dcPacketAvailable = false;
				//#sijapp cond.end#
				//#sijapp cond.end#

				// Read next packet, if available
				//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
				//#sijapp cond.if modules_FILES is "true"#
				while ((c.available() > 0) || dcPacketAvailable)
				{
					// Try to get packet
					Packet packet = null;
					try
					{
						if (c.available() > 0)
							packet = c.getPacket();
						else if (dcPacketAvailable)
							packet = peerC.getPacket();
					} catch (JimmException e)
					{
						if (!reconnect(e))
							JimmException.handleException(e);
						if (e.isCritical())
							throw (e);
					}

					// Forward received packet to all active actions and to the
					// action listener
					boolean consumed = false;
					for (int i = 0; i < actAction.size(); i++)
					{
						try
						{
							if (((Action) actAction.elementAt(i))
									.forward(packet))
							{
								consumed = true;
								break;
							}
						} catch (JimmException e)
						{
							if (!reconnect(e))
								JimmException.handleException(e);
							if (e.isCritical())
								throw (e);
						}
					}
					if (!consumed)
					{
						try
						{
							actListener.forward(packet);
						} catch (JimmException e)
						{
							if (!reconnect(e))
								JimmException.handleException(e);
							if (e.isCritical())
								throw (e);
						}
					}

					// Set dcPacketAvailable to true if the peerC is not null
					// and there is an packet waiting
					if (peerC != null)
					{
						if (peerC.available() > 0)
							dcPacketAvailable = true;
						else
							dcPacketAvailable = false;
					} else
						dcPacketAvailable = false;
				}

				//#sijapp cond.else#
				//#
				//#                while ((c.available() > 0))
				//#                {
				//#                    // Try to get packet
				//#                    Packet packet = null;
				//#                    try
				//#                    {
				//#                        if (c.available() > 0) packet = c.getPacket();
				//#                    } catch (JimmException e)
				//#                    {
				//#                    	if(!reconnect(e))
				//#                            JimmException.handleException(e);
				//#                        if (e.isCritical()) throw (e);
				//#                    }
				//#
				//#                    // Forward received packet to all active actions and to the
				//#                    // action listener
				//#                    boolean consumed = false;
				//#                    for (int i = 0; i < actAction.size(); i++)
				//#                    {
				//#                        try
				//#                        {
				//#                            if (((Action) actAction.elementAt(i)).forward(packet))
				//#                            {
				//#                                consumed = true;
				//#                                break;
				//#                            }
				//#                        } catch (JimmException e)
				//#                       {
				//#                        	if(!reconnect(e))
				//#                                JimmException.handleException(e);
				//#                            if (e.isCritical()) throw (e);
				//#                        }
				//#                    }
				//#                    if (!consumed)
				//#                    {
				//#                        try
				//#                        {
				//#                            actListener.forward(packet);
				//#                       } catch (JimmException e)
				//#                        {
				//#                        	if(!reconnect(e))
				//#                                JimmException.handleException(e);
				//#                            if (e.isCritical()) throw (e);
				//#                        }
				//#                    }
				//#                }
				//#
				//#sijapp cond.end#
				//#sijapp cond.else#
				//#                while ((c.available() > 0))
				//#                {
				//#                    // Try to get packet
				//#                   Packet packet = null;
				//#                    try
				//#                    {
				//#                        if (c.available() > 0) packet = c.getPacket();
				//#                    } catch (JimmException e)
				//#                    {
				//#                    	if(!reconnect(e))
				//#                            JimmException.handleException(e);
				//#                        if (e.isCritical()) throw (e);
				//#                    }
				//#
				//#                    // Forward received packet to all active actions and to the
				//#                    // action listener
				//#                    boolean consumed = false;
				//#                    for (int i = 0; i < actAction.size(); i++)
				//#                    {
				//#                        try
				//#                        {
				//#                            if (((Action) actAction.elementAt(i)).forward(packet))
				//#                            {
				//#                                consumed = true;
				//#                                break;
				//#                            }
				//#                        } catch (JimmException e)
				//#                        {
				//#                        	if(!reconnect(e))
				//#                                JimmException.handleException(e);
				//#                            if (e.isCritical()) throw (e);
				//#                        }
				//#                    }
				//#
				//#                    if (!consumed)
				//#                    {
				//#                        try
				//#                        {
				//#                            actListener.forward(packet);
				//#                        } catch (JimmException e)
				//#                        {
				//#                        	if(!reconnect(e))
				//#                                JimmException.handleException(e);
				//#                            if (e.isCritical()) throw (e);
				//#                        }
				//#                    }
				//#                }
				//#sijapp cond.end#

				// Remove completed actions
				for (int i = 0; i < actAction.size(); i++)
				{
					if (((Action) actAction.elementAt(i)).isCompleted()
							|| ((Action) actAction.elementAt(i)).isError())
					{
						actAction.removeElementAt(i--);
					}
				}

			}
		}
		catch (Exception e)
		{
		}

		if (!Options.getBoolean(Options.OPTION_RECONNECT) && c != null)
		{
			// Close connection
			c.notifyToDisconnect();

			resetServerCon();

			/* Reset all contacts offine */
			RunnableImpl.resetContactsOffline();
		}

	}

	public static volatile boolean connecting = false;

	private static boolean isNotCriticalConnectionError (int errcode) {
		switch (errcode) {
		    case 110:	// Login from another device
		    case 111:	// Bad password
		    case 112:	// Non-existant UIN
		    case 117:	// Empty UIN and/or password
		    case 122:	// Specified server host and/or port is invalid
		    case 127:	// peer connection: specified server host and/or port is invalid
			return false;
		}
		return true;
	}

	public synchronized static boolean reconnect(JimmException e)
	{
		int errCode = e.getErrCode();
		if (reconnect_attempts-- > 0
				&& Options.getBoolean(Options.OPTION_RECONNECT)
				&& e.isCritical() && connecting
				&& isNotCriticalConnectionError (errCode))
		{
			SplashCanvas.setLastErrCode(e.getFullErrCode());
			SplashCanvas.setErrFlag(true);
			disconnect(true);
			removeAllActions();
			Thread.yield();
			ContactList.beforeConnect();
                        try // Wait the given time
                        {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {}
			SplashCanvas.setErrFlag(false);
			connect();
			return true;
		} else {
			connecting = false;
		}

		return false;
	}

	/**************************************************************************/
	/**************************************************************************/
	/**************************************************************************/

	public abstract class Connection implements Runnable
	{
		private Object inputCloseFlagSynch = new Object();
		
		// Disconnect flags
		private volatile boolean inputCloseFlag;

		// Receiver thread
		protected volatile Thread rcvThread;

		// Received packets
		protected Vector rcvdPackets;

		// Opens a connection to the specified host and starts the receiver
		// thread
		public synchronized void connect(String hostAndPort)
				throws JimmException
		{

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

		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
		//#sijapp cond.if modules_FILES is "true"#

		// Retun the port this connection is running on
		public int getLocalPort()
		{
			return (0);
		}

		// Retun the ip this connection is running on
		public byte[] getLocalIP()
		{
			return (new byte[4]);
		}

		//#sijapp cond.end#
		//#sijapp cond.end#

		// Main loop
		public void run()
		{

		}
		
		public boolean haveToSetNullAfterDisconnect()
		{
			return true;
		}

	}

	/**************************************************************************/
	/**************************************************************************/
	/**************************************************************************/

	public class HTTPConnection extends Connection implements Runnable
	{

		// Connection variables
		private HttpConnection hcm; // Connection for monitor URLs (receiving)

		private HttpConnection hcd; // Connection for data URLSs (sending)

		private InputStream ism;

		private OutputStream osd;

		// URL for the monitor thread
		private String monitorURL;

		// HTTP Connection sequence
		private int seq;

		// HTTP Connection session ID
		private String sid;

		// IP and port of HTTP Proxy Server to connect to
		private String proxy_host;

		private int proxy_port;

		// Counter for the connections to the http proxy server
		private int connSeq;

		public HTTPConnection()
		{
			seq = 0;
			connSeq = 0;
			monitorURL = "http://http.proxy.icq.com/hello";
		}

		// Opens a connection to the specified host and starts the receiver thread
		public synchronized void connect(String hostAndPort)
				throws JimmException
		{
			try
			{
				connSeq++;
				// If this is the first connection initialize the connection with the proxy
				if (connSeq == 1)
				{
					this.setInputCloseFlag(false);
					this.rcvThread = new Thread(this);
					this.rcvThread.start();
					// Wait the the finished init will notify us
					this.wait();
				}

				// Extract host and port from combined String (we need port as int value)
				String icqserver_host = hostAndPort.substring(0, hostAndPort
						.indexOf(":"));
				int icqserver_port = Integer.parseInt(hostAndPort
						.substring(hostAndPort.indexOf(":") + 1));
				// System.out.println("Connect via "+proxy_host+":"+proxy_port+" to: "+icqserver_host+" "+icqserver_port);
				// Send anser packet with connect to real server (via proxy)
				byte[] packet = new byte[icqserver_host.length() + 4];
				Util.putWord(packet, 0, icqserver_host.length());
				System.arraycopy(Util.stringToByteArray(icqserver_host), 0,
						packet, 2, icqserver_host.length());
				Util.putWord(packet, 2 + icqserver_host.length(),
						icqserver_port);

				this.sendPacket(null, packet, 0x003, connSeq);

				// If this was not the first connection to the ICQ server close the previous
				if (connSeq != 1)
				{
					DisconnectPacket reply = new DisconnectPacket();
					this.sendPacket(reply, null, 0x0005, connSeq - 1);
					this.sendPacket(null, new byte[0], 0x0006, connSeq - 1);
				}

			} catch (IllegalArgumentException e)
			{
				throw (new JimmException(127, 0));
			} catch (InterruptedException e)
			{
				// Do nothing
			}
		}

		/***************************************************************************** 
		 ***************************************************************************** 
		 * 
		 * Sends and gets packets wraped in http requeste from ICQ http proxy server.
		 *  Packets to send and receive look like this:
		 * 
		 *  WORD	Size	Size of the upcoming packet
		 *  WORD	Version	Version of the ICQ Proxy Protocol (always 0x0443)
		 *  WORD	Type	Type of the upcoming packet must be one of these:
		 *  				0x0002	Reply on server hello
		 *  				0x0003	Loginrequest to ICQ server
		 *  				0x0004	Reply to login
		 *  				0x0005  FLAP packet
		 *  				0x0006  Close connection
		 *  				0x0007	Close connection reply
		 *  DWORD	Unkn	0x00000000
		 *  WORD	Unkn	0x0000
		 *  WORD	ConnSq	Number of connection the packet is for
		 *  ...		Data	Data of the packet (Size - 14 bytes)
		 * 
		 ***************************************************************************** 
		 *****************************************************************************/

		// Sends the specific packet (with the possibility of setting the packet type
		public void sendPacket(Packet packet, byte[] rawData, int type,
				int connCount) throws JimmException
		{
			// Set the connection parameters
			try
			{
				this.hcd = (HttpConnection) Connector.open("http://"
						+ proxy_host + ":" + proxy_port + "/data?sid=" + sid
						+ "&seq=" + seq, Connector.READ_WRITE);
				this.hcd.setRequestProperty("User-Agent", Options
						.getString(Options.OPTION_HTTP_USER_AGENT));
				this.hcd.setRequestProperty("x-wap-profile", Options
						.getString(Options.OPTION_HTTP_WAP_PROFILE));
				this.hcd.setRequestProperty("Cache-Control",
						"no-store no-cache");
				this.hcd.setRequestProperty("Pragma", "no-cache");
				this.hcd.setRequestMethod(HttpConnection.POST);
				this.osd = this.hcd.openOutputStream();
			} catch (IOException e)
			{
				this.notifyToDisconnect();
			}

			// Throw exception if output stream is not ready
			if (this.osd == null)
			{
				throw (new JimmException(128, 0, true));
			}

			// Request lock on output stream
			synchronized (this.osd)
			{

				// Send packet and count the bytes
				try
				{
					byte[] outpack;

					// Add http header (it has 14 bytes)
					if (rawData == null)
					{
						rawData = packet.toByteArray();
						outpack = new byte[14 + rawData.length];
					}

					outpack = new byte[14 + rawData.length];
					Util.putWord(outpack, 0, rawData.length + 12); // Length
					Util.putWord(outpack, 2, 0x0443); // Version
					Util.putWord(outpack, 4, type);
					Util.putDWord(outpack, 6, 0x00000000); // Unknown
					Util.putDWord(outpack, 10, connCount);
					// The "real" data
					System.arraycopy(rawData, 0, outpack, 14, rawData.length);
					// System.out.println("Sent: "+outpack.length+" b");
					this.osd.write(outpack);
					// this.osd.flush();

					// Send the data
					if (hcd.getResponseCode() != HttpConnection.HTTP_OK)
						this.notifyToDisconnect();
					else
						seq++;

					try
					{
						this.osd.close();
						this.hcd.close();
					} catch (Exception e)
					{
						// Do nothing
					} finally
					{
						this.osd = null;
						this.hcd = null;
					}

					//#sijapp cond.if modules_TRAFFIC is "true" #

					// 40 is the overhead for each packet (TCP/IP)
					// 190 is the ca. overhead for the HTTP header
					// 14 bytes is the overhead for ICQ HTTP data header
					// 170 bytes is the ca. overhead of the HTTP/1.1 200 OK
					Traffic.addTraffic(outpack.length + 40 + 190 + 14 + 170);
					if (ContactList.getVisibleContactListRef().isActive())
					{
						RunnableImpl.updateContactListCaption();
					}
					//#sijapp cond.end#
					// System.out.println(" ");
				} catch (IOException e)
				{
					this.notifyToDisconnect();
				}

			}

		}

		// Sends the specified packet always type 5 (FLAP packet)
		public void sendPacket(Packet packet) throws JimmException
		{
			this.sendPacket(packet, null, 0x0005, connSeq);
		}

		// Main loop
		public void run()
		{

			// Required variables
			byte[] length = new byte[2];
			byte[] httpPacket;
			byte[] packet = new byte[0];
			int flapMarker = 0;

			int bRead, bReadSum;
			int bReadSumRequest = 0;

			// Reset packet buffer
			synchronized (this)
			{
				this.rcvdPackets = new Vector();
			}

			// Try
			try
			{
				// Check abort condition
				while (!this.getInputCloseFlag())
				{
					// Set connection parameters
					this.hcm = (HttpConnection) Connector.open(monitorURL,
							Connector.READ_WRITE);
					this.hcm.setRequestProperty("User-Agent", Options
							.getString(Options.OPTION_HTTP_USER_AGENT));
					this.hcm.setRequestProperty("x-wap-profile", Options
							.getString(Options.OPTION_HTTP_WAP_PROFILE));
					this.hcm.setRequestProperty("Cache-Control",
							"no-store no-cache");
					this.hcm.setRequestProperty("Pragma", "no-cache");
					this.hcm.setRequestMethod(HttpConnection.GET);
					this.ism = this.hcm.openInputStream();
					if (hcm.getResponseCode() != HttpConnection.HTTP_OK)
						throw new IOException();
					// Read flap header
					bReadSumRequest = 0;

					do
					{
						bReadSum = 0;
						// Read HTTP packet length information
						do
						{
							bRead = ism.read(length, bReadSum, length.length
									- bReadSum);
							if (bRead == -1)
								break;
							bReadSum += bRead;
							bReadSumRequest += bRead;
						} while (bReadSum < length.length);
						if (bRead == -1)
							break;
						// Allocate memory for packet data
						httpPacket = new byte[Util.getWord(length, 0)];
						bReadSum = 0;

						// Read HTTP packet data
						do
						{
							bRead = ism.read(httpPacket, bReadSum,
									httpPacket.length - bReadSum);
							if (bRead == -1)
								break;
							bReadSum += bRead;
							bReadSumRequest += bRead;
						} while (bReadSum < httpPacket.length);
						if (bRead == -1)
							break;

						// Only process type 5 (flap) packets
						if (Util.getWord(httpPacket, 2) == 0x0005)
						{
							// Packet has 12 bytes header and could contain more than one FLAP
							int contBytes = 12;
							while (contBytes < httpPacket.length)
							{

								// Verify flap header only if we are sure there is a start
								if (flapMarker == 0)
								{
									if (Util.getByte(httpPacket, contBytes) != 0x2A)
									{
										throw (new JimmException(124, 0));
									}
									// Copy flap packet data from http packet
									packet = new byte[Util.getWord(httpPacket,
											contBytes + 4) + 6];
								}
								// Read packet data form httpPacket to packet
								// Packet contains the end of the flap packet
								if (httpPacket.length - contBytes >= (packet.length - flapMarker))
								{
									System.arraycopy(httpPacket, contBytes,
											packet, flapMarker,
											(packet.length - flapMarker));
									contBytes += (packet.length - flapMarker);
									flapMarker = packet.length;
								}
								// Packet does not contain the end of the flap packet
								else
								{
									System.arraycopy(httpPacket, contBytes,
											packet, flapMarker,
											httpPacket.length - contBytes);
									flapMarker += (httpPacket.length - contBytes);
									contBytes += httpPacket.length - contBytes;
								}
								// If all the bytes from a flap packet have been read add that packet to the queue
								if (flapMarker == packet.length)
								{
									// Lock object and add rcvd packet to vector
									synchronized (this.rcvdPackets)
									{
										this.rcvdPackets.addElement(packet);
									}
									flapMarker = 0;
								}
							}

							// Notify main loop
							synchronized (Icq.wait)
							{
								Icq.wait.notify();
							}
						} else if (Util.getWord(httpPacket, 2) == 0x0007)
						{
							// Construct and handle exception if we get a close rep for the connection we are
							// currently using
							if (Util.getWord(httpPacket, 10) == connSeq)
								throw new JimmException(221, 0);
						} else if (Util.getWord(httpPacket, 2) == 0x0002)
						{
							synchronized (this)
							{
								// Init answer from proxy set sid and proxy_host and proxy_port
								byte[] temp = new byte[16];
								System.arraycopy(httpPacket, 10, temp, 0, 16);
								sid = Util.byteArrayToHexString(temp);
								// Get IP of proxy
								byte[] ip = new byte[Util.getWord(httpPacket,
										26)];
								System.arraycopy(httpPacket, 28, ip, 0,
										ip.length);
								this.proxy_host = Util.byteArrayToString(ip);

								// Get port for proxy
								this.proxy_port = Util.getWord(httpPacket,
										28 + ip.length);

								// Set monitor URL to non init value
								monitorURL = "http://" + proxy_host + ":"
										+ proxy_port + "/monitor?sid=" + sid;

								this.notify();
							}

						}
					} while (bReadSumRequest < hcm.getLength());

					//#sijapp cond.if modules_TRAFFIC is "true" #
					// This is not accurate for http connection
					// 42 is the overhead for each packet (2 byte packet length) (TCP IP)
					// 185 is the overhead for each monitor packet HTTP HEADER
					// 175 is the overhead for each HTTP/1.1 200 OK answer header
					// ICQ HTTP data header is counted in bReadSum
					Traffic.addTraffic(bReadSumRequest + 42 + 185 + 175);

					if (ContactList.getVisibleContactListRef().isActive())
					{
						RunnableImpl.updateContactListCaption();
						Traffic.trafficScreen.update(false);
					}
					//#sijapp cond.end#

					try
					{
						this.ism.close();
						this.hcm.close();
					} catch (Exception e)
					{
						// Do nothing
					} finally
					{
						this.ism = null;
						this.hcm = null;
					}
				}
			}
			// Catch communication exception
			catch (NullPointerException e)
			{
				if (!this.getInputCloseFlag())
				{
					// Construct and handle exception
					JimmException f = new JimmException(125, 3);
					JimmException.handleException(f);
				} else
				{ /* Do nothing */
				}
			}
			// Catch JimmException
			catch (JimmException e)
			{

				// Handle exception
				JimmException.handleException(e);

			}
			// Catch IO exception
			catch (IOException e)
			{
				if (!this.getInputCloseFlag())
				{
					// Construct and handle exception
					JimmException f = new JimmException(125, 1);
					JimmException.handleException(f);
				} else
				{ /* Do nothing */
				}
			}
			
			closeStreams();
			setNotConnected();
		}
		
		private void closeStreams()
		{
			try { this.ism.close(); } catch (Exception e) {}
			this.ism = null;

			try { this.osd.close(); } catch (Exception e) {}
			this.osd = null;

			try { this.hcm.close(); } catch (Exception e) {}
			this.hcm = null;
			
			try { this.hcd.close(); } catch (Exception e) {}
			this.hcd = null;
		}
		
		public void forceDisconnect()
		{
			setInputCloseFlag(true);
			closeStreams();
		}

	}

	/**************************************************************************/
	/**************************************************************************/
	/**************************************************************************/

	// SOCKETConnection
	public class SOCKETConnection extends Connection implements Runnable
	{

		// Connection variables
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
		private SocketConnection sc;

		//#sijapp cond.else#
		//#    	private StreamConnection sc;
		//#sijapp cond.end#
		private InputStream is;

		private OutputStream os;

		// FLAP sequence number counter
		private int nextSequence;

		// ICQ sequence number counter
		private int nextIcqSequence;

		// Opens a connection to the specified host and starts the receiver thread
		public synchronized void connect(String hostAndPort)
				throws JimmException
		{
			try
			{
				//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
				sc = (SocketConnection) Connector.open("socket://"
						+ hostAndPort, Connector.READ_WRITE);
				//#sijapp cond.else#
				//#				sc = (StreamConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);
				//#sijapp cond.end#
				is = sc.openInputStream();
				os = sc.openOutputStream();

				setInputCloseFlag(false);
				rcvThread = new Thread(this);
				rcvThread.start();
				nextSequence = (new Random()).nextInt() % 0x0FFF;
				nextIcqSequence = 2;

			} catch (ConnectionNotFoundException e)
			{
				if (!getInputCloseFlag()) throw (new JimmException(121, 0));
			} catch (IllegalArgumentException e)
			{
				throw (new JimmException(122, 0));
			} catch (IOException e)
			{
				throw (new JimmException(120, 0));
			}
		}

		// Sends the specified packet
		public void sendPacket(Packet packet) throws JimmException
		{

			// Throw exception if output stream is not ready
			if (os == null)
			{
				JimmException e = new JimmException(123, 0);
				if (!Icq.reconnect(e))
					throw e;
			}

			// Request lock on output stream
			synchronized (os)
			{

				// Set sequence numbers
				packet.setSequence(nextSequence++);
				if (packet instanceof ToIcqSrvPacket)
				{
					((ToIcqSrvPacket) packet).setIcqSequence(nextIcqSequence++);
				}

				// Send packet and count the bytes
				try
				{
					byte[] outpack = packet.toByteArray();
					os.write(outpack);
					os.flush();
					//#sijapp cond.if modules_TRAFFIC is "true" #
					Traffic.addTraffic(outpack.length + 51); // 51 is the overhead for each packet
					if (Traffic.trafficScreen.isActive()
							|| ContactList.getVisibleContactListRef().isActive())
					{
						RunnableImpl.updateContactListCaption();
					}
					//#sijapp cond.end#
				} catch (IOException e)
				{
					notifyToDisconnect();
					if (!getInputCloseFlag())
					{
						JimmException ex = new JimmException(120, 3);
						if (!Icq.reconnect(ex)) throw ex;
					}
				}

			}

		}

		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
		//#sijapp cond.if modules_FILES is "true"#

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

		//#sijapp cond.end#
		//#sijapp cond.end#

		// Main loop
		public void run()
		{

			// Required variables
			byte[] flapHeader = new byte[6];
			byte[] flapData;
			byte[] rcvdPacket;
			int bRead = 0, bReadSum;

			// Reset packet buffer
			synchronized (this)
			{
				rcvdPackets = new Vector();
			}

			// Try
			try
			{
				// Check abort condition
				while (!getInputCloseFlag())
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
						bRead = is.read(flapHeader, bReadSum, flapHeader.length
								- bReadSum);
						if (bRead == -1)
							break;
						bReadSum += bRead;
					} while (bReadSum < flapHeader.length);
					if (bRead == -1)
						break;

					// Verify flap header
					if (Util.getByte(flapHeader, 0) != 0x2A)
					{
						throw (new JimmException(124, 0));
					}

					// Allocate memory for flap data
					flapData = new byte[Util.getWord(flapHeader, 4)];

					// Read flap data
					bReadSum = 0;
					do
					{
						bRead = is.read(flapData, bReadSum, flapData.length
								- bReadSum);
						if (bRead == -1)
							break;
						bReadSum += bRead;
					} while (bReadSum < flapData.length);
					if (bRead == -1)
						break;

					// Merge flap header and data and count the data
					rcvdPacket = new byte[flapHeader.length + flapData.length];
					System.arraycopy(flapHeader, 0, rcvdPacket, 0,
							flapHeader.length);
					System.arraycopy(flapData, 0, rcvdPacket,
							flapHeader.length, flapData.length);
					//#sijapp cond.if modules_TRAFFIC is "true" #
					Traffic.addTraffic(bReadSum + 57);
					// 46 is the overhead for each packet (6 byte flap header)
					if (ContactList.getVisibleContactListRef().isActive())
					{
						RunnableImpl.updateContactListCaption();
						Traffic.trafficScreen.update(false);
					}
					//#sijapp cond.end#

					// Lock object and add rcvd packet to vector
					synchronized (rcvdPackets)
					{
						rcvdPackets.addElement(rcvdPacket);
					}

					// Notify main loop
					synchronized (Icq.wait)
					{
						Icq.wait.notify();
					}
				}

			}
			// Catch communication exception
			catch (NullPointerException e)
			{

				// Construct and handle exception (only if input close flag has not been set)
				if (!getInputCloseFlag())
				{
					JimmException f = new JimmException(120, 3);
					JimmException.handleException(f);
				}

				// Reset input close flag
				setInputCloseFlag(false);

			}
			// Catch InterruptedException
			catch (InterruptedException e)
			{ /* Do nothing */
			}
			// Catch JimmException
			catch (JimmException e)
			{
				if (!Icq.reconnect(e))
					JimmException.handleException(e);
			}
			// Catch IO exception
			catch (IOException e)
			{
				// Construct and handle exception (only if input close flag has not been set)
				if (!getInputCloseFlag())
				{
					System.out.println("getInputCloseFlag()="+getInputCloseFlag());
					JimmException f = new JimmException(120, 1);
					if (!Icq.reconnect(f))
						JimmException.handleException(f);
				}
				// Reset input close flag
			}
			
			// Sometimes Nokia emulator stops working and bRead returns -1 
			if (bRead == -1 && !getInputCloseFlag())
			{
				JimmException f = new JimmException(120, 4);
				if (!Icq.reconnect(f))
					JimmException.handleException(f);
			}
			
			closeStreams();
			setNotConnected();
		}
		
		private void closeStreams()
		{
			try { is.close(); } catch (Exception e) {} 
			is = null;

			try { os.close(); } catch (Exception e) {}
			os = null;

			try { sc.close(); } catch (Exception e) {}
			sc = null;
			
		}
		
		public void forceDisconnect()
		{
			setInputCloseFlag(true);
			closeStreams();
		}

	}

	/**************************************************************************/
	/**************************************************************************/
	/**************************************************************************/

	//#sijapp cond.if modules_PROXY is "true"#
	// SOCKSConnection
	public class SOCKSConnection extends Connection implements Runnable
	{

		private final byte[] SOCKS4_CMD_CONNECT =
		{ (byte) 0x04, (byte) 0x01, (byte) 0x14, (byte) 0x46, // Port 5190
				(byte) 0x40, (byte) 0x0C, (byte) 0xA1, (byte) 0xB9, (byte) 0x00 // IP 64.12.161.185 (default login.icq.com)
		};

		private final byte[] SOCKS5_HELLO =
		{ (byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x02 };

		private final byte[] SOCKS5_CMD_CONNECT =
		{ (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x03 };

		// Connection variables
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
		private SocketConnection sc;

		//#sijapp cond.else#
		//#    	private StreamConnection sc;
		//#sijapp cond.end#
		private InputStream is;

		private OutputStream os;

		private boolean is_socks4 = false;

		private boolean is_socks5 = false;

		private boolean is_connected = false;

		// FLAP sequence number counter
		private int nextSequence;

		// ICQ sequence number counter
		private int nextIcqSequence;
		
		public boolean haveToSetNullAfterDisconnect()
		{
			return false;
		}
		

		// Tries to resolve given host IP
		private synchronized String ResolveIP(String host, String port)
		{
			//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
			if (Util.isIP(host))
				return host;
			SocketConnection c;

			try
			{
				c = (SocketConnection) Connector.open("socket://" + host + ":"
						+ port, Connector.READ_WRITE);
				String ip = c.getAddress();

				try
				{
					c.close();
				} catch (Exception e)
				{ /* Do nothing */
				} finally
				{
					c = null;
				}

				return ip;
			} catch (Exception e)
			{
				return "0.0.0.0";
			}

			//#sijapp cond.else#
			//#            if (Util.isIP(host))
			//#            	return host;
			//#            else
			//#           	return "0.0.0.0";
			//#sijapp cond.end#
		}

		// Build socks4 CONNECT request
		private byte[] socks4_connect_request(String ip, String port)
		{
			byte[] buf = new byte[9];

			System.arraycopy(SOCKS4_CMD_CONNECT, 0, buf, 0, 9);
			Util.putWord(buf, 2, Integer.parseInt(port));
			byte[] bip = Util.ipToByteArray(ip);
			System.arraycopy(bip, 0, buf, 4, 4);

			return buf;
		}

		// Build socks5 AUTHORIZE request
		private byte[] socks5_authorize_request(String login, String pass)
		{
			byte[] buf = new byte[3 + login.length() + pass.length()];

			Util.putByte(buf, 0, 0x01);
			Util.putByte(buf, 1, login.length());
			Util.putByte(buf, login.length() + 2, pass.length());
			byte[] blogin = Util.stringToByteArray(login);
			byte[] bpass = Util.stringToByteArray(pass);
			System.arraycopy(blogin, 0, buf, 2, blogin.length);
			System.arraycopy(bpass, 0, buf, blogin.length + 3, bpass.length);

			return buf;
		}

		// Build socks5 CONNECT request
		private byte[] socks5_connect_request(String host, String port)
		{
			byte[] buf = new byte[7 + host.length()];

			System.arraycopy(SOCKS5_CMD_CONNECT, 0, buf, 0, 4);
			Util.putByte(buf, 4, host.length());
			byte[] bhost = Util.stringToByteArray(host);
			System.arraycopy(bhost, 0, buf, 5, bhost.length);
			Util.putWord(buf, 5 + bhost.length, Integer.parseInt(port));
			return buf;
		}

		// Opens a connection to the specified host and starts the receiver
		// thread
		public synchronized void connect(String hostAndPort)
				throws JimmException
		{
			int mode = Options.getInt(Options.OPTION_PRX_TYPE);
			is_connected = false;
			is_socks4 = false;
			is_socks5 = false;
			String host = "";
			String port = "";

			if (mode != 0)
			{
				int sep = 0;
				for (int i = 0; i < hostAndPort.length(); i++)
				{
					if (hostAndPort.charAt(i) == ':')
					{
						sep = i;
						break;
					}
				}
				// Get Host and Port
				host = hostAndPort.substring(0, sep);
				port = hostAndPort.substring(sep + 1);
			}
			try
			{
				switch (mode)
				{
				case 0:
					connect_socks4(host, port);
					break;
				case 1:
					connect_socks5(host, port);
					break;
				case 2:
					// Try better first
					try
					{
						connect_socks5(host, port);
					} catch (Exception e)
					{
						// Do nothing
					}
					// If not succeeded, then try socks4
					if (!is_connected)
					{
						closeStreams();
						try
						{
							// Wait the given time
							Thread.sleep(2000);
						} catch (InterruptedException e)
						{
							// Do nothing
						}
						connect_socks4(host, port);
					}
					break;
				}

				setInputCloseFlag(false);
				rcvThread = new Thread(this);
				rcvThread.start();
				nextSequence = (new Random()).nextInt() % 0x0FFF;
				nextIcqSequence = 2;
			} catch (JimmException e)
			{
				throw (e);
			}
		}

		// Attempts to connect through socks4
		private synchronized void connect_socks4(String host, String port)
				throws JimmException
		{
			is_socks4 = false;
			String proxy_host = Options.getString(Options.OPTION_PRX_SERV);
			String proxy_port = Options.getString(Options.OPTION_PRX_PORT);
			int i = 0;
			byte[] buf;

			try
			{
				//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
				sc = (SocketConnection) Connector.open("socket://" + proxy_host
						+ ":" + proxy_port, Connector.READ_WRITE);
				//#sijapp cond.else#
				//#                sc = (StreamConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
				//#sijapp cond.end#
				is = sc.openInputStream();
				os = sc.openOutputStream();

				String ip = ResolveIP(host, port);

				os.write(socks4_connect_request(ip, port));
				os.flush();

				// Wait for responce
				while (is.available() == 0 && i < 50)
				{
					try
					{
						// Wait the given time
						i++;
						Thread.sleep(100);
					} catch (InterruptedException e)
					{
						// Do nothing
					}
				}
				// Read packet
				// If only got proxy responce packet, parse it
				if (is.available() == 8)
				{
					// Read reply
					buf = new byte[is.available()];
					is.read(buf);

					int ver = Util.getByte(buf, 0);
					int meth = Util.getByte(buf, 1);
					// All we need
					if (ver == 0x00 && meth == 0x5A)
					{
						is_connected = true;
						is_socks4 = true;
					} else
					{
						is_connected = false;
						throw (new JimmException(118, 2));
					}
				}
				// If we got responce packet bigger than mere proxy responce,
				// we might got destination server responce in tail of proxy
				// responce
				else if (is.available() > 8)
				{
					is_connected = true;
					is_socks4 = true;
				} else
				{
					throw (new JimmException(118, 2));
				}
			} catch (ConnectionNotFoundException e)
			{
				if (!getInputCloseFlag()) throw (new JimmException(121, 0));
			} catch (IllegalArgumentException e)
			{
				throw (new JimmException(122, 0));
			} catch (IOException e)
			{
				throw (new JimmException(120, 0));
			}
		}

		// Attempts to connect through socks5
		private synchronized void connect_socks5(String host, String port)
				throws JimmException
		{
			is_socks5 = false;
			String proxy_host = Options.getString(Options.OPTION_PRX_SERV);
			String proxy_port = Options.getString(Options.OPTION_PRX_PORT);
			String proxy_login = Options.getString(Options.OPTION_PRX_NAME);
			String proxy_pass = Options.getString(Options.OPTION_PRX_PASS);
			int i = 0;
			byte[] buf;

			try
			{
				//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
				sc = (SocketConnection) Connector.open("socket://" + proxy_host
						+ ":" + proxy_port, Connector.READ_WRITE);
				//#sijapp cond.else#
				//#                sc = (StreamConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
				//#sijapp cond.end#
				is = sc.openInputStream();
				os = sc.openOutputStream();

				os.write(SOCKS5_HELLO);
				os.flush();

				// Wait for responce
				while (is.available() == 0 && i < 50)
				{
					try
					{
						// Wait the given time
						i++;
						Thread.sleep(100);
					} catch (InterruptedException e)
					{
						// Do nothing
					}
				}

				if (is.available() == 0)
				{
					throw (new JimmException(118, 2));
				}

				// Read reply
				buf = new byte[is.available()];
				is.read(buf);

				int ver = Util.getByte(buf, 0);
				int meth = Util.getByte(buf, 1);

				// Plain text authorisation
				if (ver == 0x05 && meth == 0x02)
				{
					os.write(socks5_authorize_request(proxy_login, proxy_pass));
					os.flush();

					// Wait for responce
					while (is.available() == 0 && i < 50)
					{
						try
						{
							// Wait the given time
							i++;
							Thread.sleep(100);
						} catch (InterruptedException e)
						{
							// Do nothing
						}
					}

					if (is.available() == 0)
					{
						throw (new JimmException(118, 2));
					}

					// Read reply
					buf = new byte[is.available()];
					is.read(buf);

					meth = Util.getByte(buf, 1);

					if (meth == 0x00)
					{
						is_connected = true;
						is_socks5 = true;
					} else
					{
						// Unknown error (bad login or pass)
						throw (new JimmException(118, 3));
					}
				}
				// Proxy without authorisation
				else if (ver == 0x05 && meth == 0x00)
				{
					is_connected = true;
					is_socks5 = true;
				}
				// Something bad happened :'(
				else
				{
					throw (new JimmException(118, 2));
				}
				// If we got correct responce, send CONNECT
				if (is_connected == true)
				{
					os.write(socks5_connect_request(host, port));
					os.flush();
				}
			} catch (ConnectionNotFoundException e)
			{
				if (!getInputCloseFlag()) throw (new JimmException(121, 0));
			} catch (IllegalArgumentException e)
			{
				throw (new JimmException(122, 0));
			} catch (IOException e)
			{
				throw (new JimmException(120, 0));
			}
		}

		public void forceDisconnect()
		{
			setInputCloseFlag(true);
			closeStreams();
		}

		// Close input and output streams
		private synchronized void closeStreams()
		{
			try { is.close(); } catch (Exception e) {}
			is = null;

			try { os.close(); } catch (Exception e) {}
			os = null;

			try { sc.close(); } catch (Exception e) {}
			sc = null;
		}

		// Sends the specified packet
		public void sendPacket(Packet packet) throws JimmException
		{

			// Throw exception if output stream is not ready
			if (os == null)
			{
				throw (new JimmException(123, 0));
			}

			// Request lock on output stream
			synchronized (os)
			{

				// Set sequence numbers
				packet.setSequence(nextSequence++);
				if (packet instanceof ToIcqSrvPacket)
				{
					((ToIcqSrvPacket) packet).setIcqSequence(nextIcqSequence++);
				}

				// Send packet and count the bytes
				try
				{
					byte[] outpack = packet.toByteArray();
					os.write(outpack);
					os.flush();
					//#sijapp cond.if modules_TRAFFIC is "true" #
					Traffic.addTraffic(outpack.length + 51); // 51 is the overhead for each packet
					if (Traffic.trafficScreen.isActive()
							|| ContactList.getVisibleContactListRef().isActive())
					{
						RunnableImpl.updateContactListCaption();
						Traffic.trafficScreen.update(false);
					}
					//#sijapp cond.end#
				} catch (IOException e)
				{
					notifyToDisconnect();
				}

			}

		}

		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
		//#sijapp cond.if modules_FILES is "true"#

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

		//#sijapp cond.end#
		//#sijapp cond.end#

		// Main loop
		public void run()
		{

			// Required variables
			byte[] flapHeader = new byte[6];
			byte[] flapData;
			byte[] rcvdPacket;
			int bRead, bReadSum;

			// Reset packet buffer
			synchronized (this)
			{
				rcvdPackets = new Vector();
			}

			// Try
			try
			{

				// Check abort condition
				while (!getInputCloseFlag())
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
						bRead = is.read(flapHeader, bReadSum, flapHeader.length
								- bReadSum);
						if (bRead == -1)
							break;
						bReadSum += bRead;
					} while (bReadSum < flapHeader.length);
					if (bRead == -1)
						break;
					// Verify and strip out proxy responce
					// Socks4 first
					if (Util.getByte(flapHeader, 0) == 0x00 && is_socks4)
					{
						// Strip only on first packet
						is_socks4 = false;
						int rep = Util.getByte(flapHeader, 1);
						if (rep != 0x5A)
						{
							// Something went wrong :(
							throw (new JimmException(118, 1));
						}

						is.skip(2);

						bReadSum = 0;
						do
						{
							bRead = is.read(flapHeader, bReadSum,
									flapHeader.length - bReadSum);
							if (bRead == -1)
								break;
							bReadSum += bRead;
						} while (bReadSum < flapHeader.length);
					}
					// Check for socks5
					else if (Util.getByte(flapHeader, 0) == 0x05 && is_socks5)
					{
						// Strip only on first packet
						is_socks5 = false;

						int rep = Util.getByte(flapHeader, 1);
						if (rep != 0x00)
						{
							// Something went wrong :(
							throw (new JimmException(118, 1));
						}
						// Check ATYP and skip BND.ADDR
						int atyp = Util.getByte(flapHeader, 3);

						if (atyp == 0x01)
						{
							is.skip(4);
						} else if (atyp == 0x03)
						{
							int size = Util.getByte(flapHeader, 4);
							is.skip(size + 1);
						} else
						{
							// Don't know what was that, but skip like
							// if it was an ip
							is.skip(4);
						}

						bReadSum = 0;
						do
						{
							bRead = is.read(flapHeader, bReadSum,
									flapHeader.length - bReadSum);
							if (bRead == -1)
								break;
							bReadSum += bRead;
						} while (bReadSum < flapHeader.length);
					}

					// Verify flap header
					if (Util.getByte(flapHeader, 0) != 0x2A)
					{
						throw (new JimmException(124, 0));
					}

					// Allocate memory for flap data
					flapData = new byte[Util.getWord(flapHeader, 4)];

					// Read flap data
					bReadSum = 0;
					do
					{
						bRead = is.read(flapData, bReadSum, flapData.length
								- bReadSum);
						if (bRead == -1)
							break;
						bReadSum += bRead;
					} while (bReadSum < flapData.length);
					if (bRead == -1)
						break;

					// Merge flap header and data and count the data
					rcvdPacket = new byte[flapHeader.length + flapData.length];
					System.arraycopy(flapHeader, 0, rcvdPacket, 0,
							flapHeader.length);
					System.arraycopy(flapData, 0, rcvdPacket,
							flapHeader.length, flapData.length);
					//#sijapp cond.if modules_TRAFFIC is "true" #
					Traffic.addTraffic(bReadSum + 57);
					// 46 is the overhead for each packet (6 byte flap header)
					if (Traffic.trafficScreen.isActive()
							|| ContactList.getVisibleContactListRef().isActive())
					{
						RunnableImpl.updateContactListCaption();
						Traffic.trafficScreen.update(false);
					}
					//#sijapp cond.end#

					// Lock object and add rcvd packet to vector
					synchronized (rcvdPackets)
					{
						rcvdPackets.addElement(rcvdPacket);
					}

					// Notify main loop
					synchronized (Icq.wait)
					{
						Icq.wait.notify();
					}
				}

			}
			// Catch communication exception
			catch (NullPointerException e)
			{

				// Construct and handle exception (only if input close flag has not been set)
				if (!getInputCloseFlag())
				{
					JimmException f = new JimmException(120, 3);
					JimmException.handleException(f);
				}

				// Reset input close flag
				setInputCloseFlag(false);

			}
			// Catch InterruptedException
			catch (InterruptedException e)
			{ /* Do nothing */
			}
			// Catch JimmException
			catch (JimmException e)
			{

				// Handle exception
				JimmException.handleException(e);

			}
			// Catch IO exception
			catch (IOException e)
			{
				// Construct and handle exception (only if input close flag has not been set)
				if (!getInputCloseFlag())
				{
					JimmException f = new JimmException(120, 1);
					JimmException.handleException(f);
				}

				// Reset input close flag
				setInputCloseFlag(false);

			}
			
			//closeStreams();
			//setNotConnected();
		}
		
		

	}

	//#sijapp cond.end #

	/**************************************************************************/
	/**************************************************************************/
	/**************************************************************************/

	//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
	//#sijapp cond.if modules_FILES is "true"#
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
					synchronized (Icq.wait)
					{
						Icq.wait.notify();
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
			}

		}

	}

	//#sijapp cond.end#
	//#sijapp cond.end#

	public static int getCurrentStatus()
	{
		return isConnected() ? (int) Options
				.getLong(Options.OPTION_ONLINE_STATUS)
				: ContactList.STATUS_OFFLINE;
	}

	static public void setInactiveTime(long inactiveTime) throws JimmException
	{
		byte[] buf = new byte[4];
		
		Util.putDWord(buf, 0, inactiveTime);
		sendPacket(new SnacPacket(0x0001, 0x0011, 0x0011, new byte[0], buf));
	}

	static public void setOnlineStatus(int status, int xStatus) throws JimmException
	{
		ByteArrayOutputStream statBuffer = new ByteArrayOutputStream();
		ByteArrayOutputStream visBuffer = new ByteArrayOutputStream();
		
		int onlineStatus = Util.translateStatusSend(status);
		
		int visibilityItemId = Options.getInt(Options.OPTION_VISIBILITY_ID);
		byte bCode = 0;

		/* Main status */
		if (status != -1)
		{
			/* Visiblity */
			if (visibilityItemId != 0)
			{
				if (onlineStatus == Util.SET_STATUS_INVISIBLE)
					bCode = (status == ContactList.STATUS_INVIS_ALL) ? (byte) 2 : (byte) 3;
				else
					bCode = (byte) 4;
				Util.writeWord(visBuffer, 0, true);
				Util.writeWord(visBuffer, 0, true);
				Util.writeWord(visBuffer, visibilityItemId, true);
				Util.writeWord(visBuffer, 4, true);
				Util.writeWord(visBuffer, 5, true);
				Util.writeWord(visBuffer, 0xCA, true);
				Util.writeWord(visBuffer, 1, true);
				Util.writeByte(visBuffer, bCode);
			
				// Change privacy setting according to new status
				if ((getVisibility() != bCode) && (onlineStatus == Util.SET_STATUS_INVISIBLE))
				{
					setVisibility(bCode);
					SnacPacket reply2pre = new SnacPacket(
							SnacPacket.CLI_ROSTERUPDATE_FAMILY,
							SnacPacket.CLI_ROSTERUPDATE_COMMAND,
							SnacPacket.CLI_ROSTERUPDATE_COMMAND, new byte[0], visBuffer.toByteArray());
					sendPacket(reply2pre);
				}
			}
		
			Util.writeWord(statBuffer, 0x06, true); // TLV (0x06)
			Util.writeWord(statBuffer, 4, true); // TLV len
			Util.writeDWord(statBuffer, onlineStatus|0x10000000, true);

			if (xStatus != 255)
			{
				Util.writeWord(statBuffer, 0x0C, true);		// TLV (0x0C)
				Util.writeWord(statBuffer, 0x25, true);		// TLV len
				Util.writeDWord(statBuffer, 0xC0A80001, true);	// 192.168.0.1, cannot get own IP address
				Util.writeDWord(statBuffer, 0x0000ABCD, true);	// Port 43981
				Util.writeByte(statBuffer, 0x00);		// Firewall
				Util.writeWord(statBuffer, 0x08, true);		// Support protocol version 8
				Util.writeDWord(statBuffer, 0x00000000, true);
				Util.writeDWord(statBuffer, 0x00000050, true);
				Util.writeDWord(statBuffer, 0x00000003, true);
				Util.writeDWord(statBuffer, 0xFFFFFFFE, true);
				Util.writeDWord(statBuffer, 0x00010000, true);
				Util.writeDWord(statBuffer, 0xFFFFFFFE, true);
				Util.writeWord(statBuffer, 0x0000, true);		
			}
		}

		/* xStatus */
		if (xStatus != 255)
		{
			int icqXStatus = convertJimmXStatToIcqXStat(xStatus);
			byte[] szMoodId = Util.stringToByteArray(ResourceBundle.getString(JimmUI.xStatusStrings[xStatus+1]), true);

			Util.writeWord(statBuffer, 0x1D, true);  // TLV (0x1D)

			String message = icqXStatus != -1 ? ("icqmood"+icqXStatus) : "";

			Util.writeWord(statBuffer, 12+szMoodId.length+message.length(), true);   // TLV Length
			Util.writeWord(statBuffer, 0x0002, true);   // Text Status
			Util.writeByte(statBuffer, 0x04);
			Util.writeByte(statBuffer, szMoodId.length + 0x04);

			Util.writeWord(statBuffer, szMoodId.length, true);
			Util.writeByteArray(statBuffer, szMoodId);
			Util.writeWord(statBuffer, 0x0000, true);

			Util.writeWord(statBuffer, 0x000E, true);
			Util.writeLenAndString(statBuffer, message, false);
		}

		if (statBuffer.size() != 0)
		{
			SnacPacket packet = new SnacPacket(SnacPacket.CLI_SETSTATUS_FAMILY,
					SnacPacket.CLI_SETSTATUS_COMMAND, SnacPacket.CLI_SETSTATUS_COMMAND, new byte[0],
					statBuffer.toByteArray());
			sendPacket(packet);
		}
		
		// Change privacy setting according to new status
		if ((status != -1) && (getVisibility() != bCode) && (visibilityItemId != 0 && onlineStatus != Util.SET_STATUS_INVISIBLE))
		{
			setVisibility(bCode);
			SnacPacket reply2post = new SnacPacket(
					SnacPacket.CLI_ROSTERUPDATE_FAMILY,
					SnacPacket.CLI_ROSTERUPDATE_COMMAND,
					SnacPacket.CLI_ROSTERUPDATE_COMMAND, new byte[0], visBuffer.toByteArray());
			sendPacket(reply2post);
		}

		lastStatusChangeTime = Util.getDateString(true);
	}

	public static String getLastStatusChangeTime()
	{
		return lastStatusChangeTime;
	}
	
	static public void sendUserUnfoPacket() throws JimmException
	{
		ByteArrayOutputStream capsStream = new ByteArrayOutputStream();
		byte[] packet = null;
		
		try
		{
			byte[] ver = Util.stringToByteArray("###VERSION###");
			int verLen = ver.length;
			if (verLen > 10) verLen = 10;
			for (int i = 0; i < verLen; i++) CAP_VERSION[i+5] = ver[i];    
			
			capsStream.write(new byte[] {(byte)0x00, (byte)0x05, (byte)0x00, (byte)0x00});
			capsStream.write(CAP_AIM_SERVERRELAY);
			capsStream.write(CAP_AIM_ISICQ);
			capsStream.write(CAP_ICHAT);
			capsStream.write(CAP_UTF8);
			capsStream.write(CAP_VERSION);
			
			//#sijapp cond.if target isnot  "DEFAULT"#
			if (Options.getInt(Options.OPTION_TYPING_MODE) > 0) capsStream.write(CAP_MTN);
			//#sijapp cond.end#
			
			int xStatus = Options.getInt(Options.OPTION_XSTATUS);
			for (int i = 0; i < XSTATUS_CONSTS.length; i += 18)
			{
				if (XSTATUS_CONSTS[i] == xStatus)
				capsStream.write(XSTATUS_CONSTS, i+2, 16);
			}
			
			packet = capsStream.toByteArray();
			Util.putWord(packet, 2, packet.length-4);
		}
		catch (Exception e) {}
		
		sendPacket(new SnacPacket(SnacPacket.CLI_SETUSERINFO_FAMILY, SnacPacket.CLI_SETUSERINFO_COMMAND, 4, new byte[0], packet));

		byte[] buf = new byte[4];
		Util.putDWord(buf, 0, 0x00040000);
		sendPacket(new SnacPacket(SnacPacket.CLI_SETUSERINFO_FAMILY, SnacPacket.CLI_SETUSERINFO_COMMAND, 4, new byte[0], buf));
	}
	
	////////////////
	
	// CAPS
	public static final byte[] CAP_AIM_ISICQ       = Util.explodeToBytes("09,46,13,44,4C,7F,11,D1,82,22,44,45,53,54,00,00", ',', 16);
	public static final byte[] CAP_ICHAT           = Util.explodeToBytes("09,46,00,00,4C,7F,11,D1,82,22,44,45,53,54,00,00", ',', 16);
	public static final byte[] CAP_VERSION         = Util.explodeToBytes("*Jimm,20,00,00,00,00,00,00,00,00,00,00,00", ',', 16);
	public static final byte[] CAP_MTN             = Util.explodeToBytes("56,3f,c8,09,0b,6f,41,bd,9f,79,42,26,09,df,a2,f3", ',', 16);
	public static final byte[] CAP_AIM_SERVERRELAY = Util.explodeToBytes("09,46,13,49,4C,7F,11,D1,82,22,44,45,53,54,00,00", ',', 16);
	public static final byte[] CAP_UTF8            = Util.explodeToBytes("09,46,13,4E,4C,7F,11,D1,82,22,44,45,53,54,00,00", ',', 16);
	public static final byte[] CAP_UTF8_GUID       = Util.explodeToBytes("7b,30,39,34,36,31,33,34,45,2D,34,43,37,46,2D,31,31,44,31,2D,38,32,32,32,2D,34,34,34,35,35,33,35,34,30,30,30,30,7D", ',', 16);
	
	
	private static final byte[] CAP_QIPINFIUM      = Util.explodeToBytes("7c,73,75,02,c3,be,4f,3e,a6,9f,01,53,13,43,1e,1a", ',', 16);
	private static final byte[] CAP_QIPPDASYM      = Util.explodeToBytes("51,AD,D1,90,72,04,47,3D,A1,A1,49,F4,A3,97,A4,1F", ',', 16);
	private static final byte[] CAP_QIPPDAWIN      = Util.explodeToBytes("56,3F,C8,09,0B,6F,41,51,49,50,20,20,20,20,20,21", ',', 16);
//	private static final byte[] CAP_QIPPLUGINS     = Util.explodeToBytes("7C,53,3F,FA,68,00,4F,21,BC,FB,C7,D2,43,9A,AD,31", ',', 16);
//	private static final byte[] CAP_AUDIO          = Util.explodeToBytes("09,46,01,04,4c,7f,11,d1,82,22,44,45,53,54,00,00", ',', 16);
//	private static final byte[] CAP_VIDEO          = Util.explodeToBytes("09,46,01,01,4c,7f,11,d1,82,22,44,45,53,54,00,00", ',', 16);
	private static final byte[] CAP_HTMLMESSAGES   = Util.explodeToBytes("01,38,ca,7b,76,9a,49,15,88,f2,13,fc,00,97,9e,a8", ',', 16);
//	private static final byte[] CAP_XMultiUserChat = Util.explodeToBytes("67,36,15,15,61,2d,4c,07,8f,3d,bd,e6,40,8e,a0,41", ',', 16);
//	private static final byte[] CAP_XtZers         = Util.explodeToBytes("b2,ec,8f,16,7c,6f,45,1b,bd,79,dc,58,49,78,88,b9", ',', 16);
	private static final byte[] CAP_IsICQLITE      = Util.explodeToBytes("17,8c,2d,9b,da,a5,45,bb,8d,db,f3,bd,bd,53,a1,0a", ',', 16);
	private static final byte[] CAP_MIRANDAIM      = Util.explodeToBytes("4D,69,72,61,6E,64,61,4D,00,00,00,00,00,00,00,00", ',', 16);
	private static final byte[] CAP_TRILLIAN       = Util.explodeToBytes("97,b1,27,51,24,3c,43,34,ad,22,d6,ab,f7,3f,14,09", ',', 16);
	private static final byte[] CAP_TRILCRYPT      = Util.explodeToBytes("f2,e7,c7,f4,fe,ad,4d,fb,b2,35,36,79,8b,df,00,00", ',', 16);
	private static final byte[] CAP_SIM            = Util.explodeToBytes("*SIM client  ,0,0,0,0", ',', 16);
	private static final byte[] CAP_SIMOLD         = Util.explodeToBytes("97,b1,27,51,24,3c,43,34,ad,22,d6,ab,f7,3f,14,00", ',', 16);
	private static final byte[] CAP_LICQ           = Util.explodeToBytes("*Licq client ',0,0,0,0", ',', 16);
	private static final byte[] CAP_KOPETE         = Util.explodeToBytes("*Kopete ICQ  ',0,0,0,0", ',', 16);
	private static final byte[] CAP_MICQ           = Util.explodeToBytes("*mICQ ,A9,* R.K. ',0,0,0,0", ',', 16);
	private static final byte[] CAP_ANDRQ          = Util.explodeToBytes("*&RQinside,0,0,0,0,0,0,0", ',', 16);
	private static final byte[] CAP_QIP            = Util.explodeToBytes("56,3F,C8,09,0B,6F,41,*QIP 2005a", ',', 16);
	private static final byte[] CAP_IM2            = Util.explodeToBytes("74,ED,C3,36,44,DF,48,5B,8B,1C,67,1A,1F,86,09,9F", ',', 16);
	private static final byte[] CAP_MACICQ         = Util.explodeToBytes("dd,16,f2,02,84,e6,11,d4,90,db,00,10,4b,9b,4b,7d", ',', 16);
	private static final byte[] CAP_RICHTEXT       = Util.explodeToBytes("97,b1,27,51,24,3c,43,34,ad,22,d6,ab,f7,3f,14,92", ',', 16);
	private static final byte[] CAP_IS2001         = Util.explodeToBytes("2e,7a,64,75,fa,df,4d,c8,88,6f,ea,35,95,fd,b6,df", ',', 16);
	private static final byte[] CAP_IS2002         = Util.explodeToBytes("10,cf,40,d1,4c,7f,11,d1,82,22,44,45,53,54,00,00", ',', 16);
	private static final byte[] CAP_STR20012       = Util.explodeToBytes("a0,e9,3f,37,4f,e9,d3,11,bc,d2,00,04,ac,96,dd,96", ',', 16);
	private static final byte[] CAP_AIMICON        = Util.explodeToBytes("09,46,13,46,4c,7f,11,d1,82,22,44,45,53,54,00,00", ',', 16);
	private static final byte[] CAP_AIMIMIMAGE     = Util.explodeToBytes("09,46,13,45,4c,7f,11,d1,82,22,44,45,53,54,00,00", ',', 16);
	private static final byte[] CAP_AIMCHAT        = Util.explodeToBytes("74,8F,24,20,62,87,11,D1,82,22,44,45,53,54,00,00", ',', 16);
	private static final byte[] CAP_UIM            = Util.explodeToBytes("A7,E4,0A,96,B3,A0,47,9A,B8,45,C9,E4,67,C5,6B,1F", ',', 16);
	private static final byte[] CAP_RAMBLER        = Util.explodeToBytes("7E,11,B7,78,A3,53,49,26,A8,02,44,73,52,08,C4,2A", ',', 16);
	private static final byte[] CAP_ABV            = Util.explodeToBytes("00,E7,E0,DF,A9,D0,4F,e1,91,62,C8,90,9A,13,2A,1B", ',', 16);
	private static final byte[] CAP_NETVIGATOR     = Util.explodeToBytes("4C,6B,90,A3,3D,2D,48,0E,89,D6,2E,4B,2C,10,D9,9F", ',', 16);
	private static final byte[] CAP_XTRAZ          = Util.explodeToBytes("1A,09,3C,6C,D7,FD,4E,C5,9D,51,A6,47,4E,34,F5,A0", ',', 16);
	private static final byte[] CAP_AIMFILE        = Util.explodeToBytes("09,46,13,43,4C,7F,11,D1,82,22,44,45,53,54,00,00", ',', 16);
	private static final byte[] CAP_DIRECT         = Util.explodeToBytes("09,46,13,44,4C,7F,11,D1,82,22,44,45,53,54,00,00", ',', 16);
	private static final byte[] CAP_JIMM           = Util.explodeToBytes("*Jimm ", ',', 16);
	private static final byte[] CAP_AVATAR         = Util.explodeToBytes("09,46,13,4C,4C,7F,11,D1,82,22,44,45,53,54,00,00", ',', 16);
	private static final byte[] CAP_TYPING         = Util.explodeToBytes("56,3f,c8,09,0b,6f,41,bd,9f,79,42,26,09,df,a2,f3", ',', 16);
	private static final byte[] CAP_MCHAT          = Util.explodeToBytes("*mChat icq", ',', 16);
	private static final byte[] CAP_IMPLUS         = Util.explodeToBytes("8e,cd,90,e7,4f,18,28,f8,02,ec,d6,18,a4,e9,de,68", ',', 16);
	private static final byte[] CAP_JIMM_SMAPER    = Util.explodeToBytes("*Smaper ", ',', 16);
	
	// Arrays for new capability blowup
	private static final byte[] CAP_OLD_HEAD = { (byte) 0x09, (byte) 0x46 };
	private static final byte[] CAP_OLD_TAIL = Util.explodeToBytes("4C,7F,11,D1,82,22,44,45,53,54,00,00", ',', 16);

	
	private static final byte[] XSTATUS_CONSTS = Util.explodeToBytes(
		"00,17,01,D8,D7,EE,AC,3B,49,2A,A5,8D,D3,D8,77,E6,6B,92,"+ // ANGRY
		"01,01,5A,58,1E,A1,E5,80,43,0C,A0,6F,61,22,98,B7,E4,C7,"+ // DUCK
		"02,02,83,C9,B7,8E,77,E7,43,78,B2,C5,FB,6C,FC,C3,5B,EC,"+ // TIRED
		"03,03,E6,01,E4,1C,33,73,4B,D1,BC,06,81,1D,6C,32,3D,81,"+ // PARTY
		"04,04,8C,50,DB,AE,81,ED,47,86,AC,CA,16,CC,32,13,C7,B7,"+ // BEER
		"05,05,3F,B0,BD,36,AF,3B,4A,60,9E,EF,CF,19,0F,6A,5A,7F,"+ // THINKING
		"06,06,F8,E8,D7,B2,82,C4,41,42,90,F8,10,C6,CE,0A,89,A6,"+ // EATING
		"07,07,80,53,7D,E2,A4,67,4A,76,B3,54,6D,FD,07,5F,5E,C6,"+ // TV
		"08,08,F1,8A,B5,2E,DC,57,49,1D,99,DC,64,44,50,24,57,AF,"+ // FRIENDS
		"09,09,1B,78,AE,31,FA,0B,4D,38,93,D1,99,7E,EE,AF,B2,18,"+ // COFFEE
		"0A,0A,61,BE,E0,DD,8B,DD,47,5D,8D,EE,5F,4B,AA,CF,19,A7,"+ // MUSIC
		"0B,0B,48,8E,14,89,8A,CA,4A,08,82,AA,77,CE,7A,16,52,08,"+ // BUSINESS
		"0C,0C,10,7A,9A,18,12,32,4D,A4,B6,CD,08,79,DB,78,0F,09,"+ // CAMERA
		"0D,0D,6F,49,30,98,4F,7C,4A,FF,A2,76,34,A0,3B,CE,AE,A7,"+ // FUNNY
		"0E,0E,12,92,E5,50,1B,64,4F,66,B2,06,B2,9A,F3,78,E4,8D,"+ // PHONE
		"0F,0F,D4,A6,11,D0,8F,01,4E,C0,92,23,C5,B6,BE,C6,CC,F0,"+ // GAMES
		"10,10,60,9D,52,F8,A2,9A,49,A6,B2,A0,25,24,C5,E9,D2,60,"+ // COLLEGE
		"11,00,63,62,73,37,A0,3F,49,FF,80,E5,F7,09,CD,E0,A4,EE,"+ // SHOPPING
		"12,11,1F,7A,40,71,BF,3B,4E,60,BC,32,4C,57,87,B0,4C,F1,"+ // SICK
		"13,12,78,5E,8C,48,40,D3,4C,65,88,6F,04,CF,3F,3F,43,DF,"+ // SLEEPING
		"14,13,A6,ED,55,7E,6B,F7,44,D4,A5,D4,D2,E7,D9,5C,E8,1F,"+ // SURFING
		"15,14,12,D0,7E,3E,F8,85,48,9E,8E,97,A7,2A,65,51,E5,8D,"+ // INTERNET
		"16,15,BA,74,DB,3E,9E,24,43,4B,87,B6,2F,6B,8D,FE,E5,0F,"+ // ENGINEERING
		"17,16,63,4F,6B,D8,AD,D2,4A,A1,AA,B9,11,5B,C2,6D,05,A1,"+ // TYPING
		"18,FF,2C,E0,E4,E5,7C,64,43,70,9C,3A,7A,1C,E8,78,A7,DC,"+ // UNK
		"19,FF,10,11,17,C9,A3,B0,40,F9,81,AC,49,E1,59,FB,D5,D4,"+ // PPC
		"1A,FF,16,0C,60,BB,DD,44,43,F3,91,40,05,0F,00,E6,C0,09,"+ // MOBILE
		"1B,FF,64,43,C6,AF,22,60,45,17,B5,8C,D7,DF,8E,29,03,52,"+ // MAN
		"1C,FF,16,F5,B7,6F,A9,D2,40,35,8C,C5,C0,84,70,3C,98,FA,"+ // WC
		"1D,FF,63,14,36,FF,3F,8A,40,D0,A5,CB,7B,66,E0,51,B3,64,"+ // QUESTION
		"1E,FF,B7,08,67,F5,38,25,43,27,A1,FF,CF,4C,C1,93,97,97,"+ // WAY
		"1F,FF,DD,CF,0E,A9,71,95,40,48,A9,C6,41,32,06,D6,F2,80,"+ // HEART
		"20,FF,3F,B0,BD,36,AF,3B,4A,60,9E,EF,CF,19,0F,6A,5A,7E,"+ // CIGARETTE
		"21,FF,E6,01,E4,1C,33,73,4B,D1,BC,06,81,1D,6C,32,3D,82,"+ // SEX
		"22,FF,D4,E2,B0,BA,33,4E,4F,A5,98,D0,11,7D,BF,4D,3C,C8,"+ // SEARCH
		"23,FF,00,72,D9,08,4A,D1,43,DD,91,99,6F,02,69,66,02,6F",  // DIARY
		
		/*"24,FF,64,43,c6,af,22,60,45,17,b5,8c,d7,df,8e,29,03,52,"+ // I'm hight (miranda)
		"25,FF,63,14,36,ff,3f,8a,40,d0,a5,cb,7b,66,e0,51,b3,64,"+ // To be or not to be? (miranda)
		"26,FF,10,11,17,c9,a3,b0,40,f9,81,ac,49,e1,59,fb,d5,d4",  // Cooking (miranda) */
		',', 16);

	public static boolean isXStatusStd(int index)
	{
		return (index < 0) ? true : (XSTATUS_CONSTS[index*18+1] != -1);
	}

	/* Capabilities */
	public static final int CAPF_NO_INTERNAL     = 0;      // No capability
	public static final int CAPF_AIM_SERVERRELAY = 1 << 0; // Client unterstands type-2 messages
	public static final int CAPF_UTF8_INTERNAL   = 1 << 1; // Client unterstands UTF-8 messages
	public static final int CAPF_RICHTEXT        = 1 << 2;
	public static final int CAPF_AIMICON         = 1 << 3;
	public static final int CAPF_AIMCHAT         = 1 << 4;
	public static final int CAPF_XTRAZ           = 1 << 5;
	public static final int CAPF_AIMFILE         = 1 << 6;
	public static final int CAPF_AIMIMIMAGE      = 1 << 7;
	public static final int CAPF_AVATAR          = 1 << 8;	
	public static final int CAPF_DIRECT          = 1 << 9;
	public static final int CAPF_TYPING          = 1 << 10;
	public static final int CAPF_HTMLMESSAGES    = 1 << 11;
	public static final int CAPF_AUDIO           = 1 << 12;
	public static final int CAPF_VIDEO           = 1 << 14;

	// Client capabilities for detection
	private static final long CAPF_MIRANDAIM     = 1l << 32;
	private static final long CAPF_TRILLIAN      = 1l << 33;
	private static final long CAPF_TRILCRYPT     = 1l << 34;
	private static final long CAPF_SIM           = 1l << 35;
	private static final long CAPF_SIMOLD        = 1l << 36;
	private static final long CAPF_LICQ          = 1l << 37;
	private static final long CAPF_KOPETE        = 1l << 38;
	private static final long CAPF_MICQ          = 1l << 39;
	private static final long CAPF_ANDRQ         = 1l << 40;
	private static final long CAPF_QIP           = 1l << 41;
	private static final long CAPF_IM2           = 1l << 42;
	private static final long CAPF_MACICQ        = 1l << 43;
	private static final long CAPF_IS2001        = 1l << 44;
	private static final long CAPF_IS2002        = 1l << 45;
	private static final long CAPF_STR20012      = 1l << 46;
	private static final long CAPF_UIM           = 1l << 47;
	private static final long CAPF_RAMBLER       = 1l << 48;
	private static final long CAPF_ABV           = 1l << 49;
	private static final long CAPF_NETVIGATOR    = 1l << 50;
	private static final long CAPF_JIMM          = 1l << 51;
	private static final long CAPF_MCHAT         = 1l << 52;
	private static final long CAPF_QIPINFIUM     = 1l << 53;
	private static final long CAPF_IsICQLITE     = 1l << 54;
	private static final long CAPF_QIPPDASYM     = 1l << 55;
	private static final long CAPF_QIPPDAWIN     = 1l << 56;
	private static final long CAPF_IMPLUS        = 1l << 57;
	private static final long CAPF_JIMM_SMAPER   = 1l << 58;

	
//	public static final int CAPF_QIPPLUGINS      = 1l << 59;
//	public static final int CAPF_XMultiUserChat  = 1l << 60;
//	public static final int CAPF_XtZers          = 1l << 61;

	// Client IDs
	public static final byte CLI_NONE = 0;
	public static final byte CLI_QIP = 1;
	public static final byte CLI_MIRANDA = 2;
	public static final byte CLI_LICQ = 3;
	public static final byte CLI_TRILLIAN = 4;
	public static final byte CLI_SIM = 5;
	public static final byte CLI_KOPETE = 6;
	public static final byte CLI_MICQ = 7;
	public static final byte CLI_ANDRQ = 8;
	public static final byte CLI_IM2 = 9;
	public static final byte CLI_MACICQ = 10;
	public static final byte CLI_AIM = 11;
	public static final byte CLI_UIM = 12;
	public static final byte CLI_WEBICQ = 13;
	public static final byte CLI_GAIM = 14;
	public static final byte CLI_ALICQ = 15;
	public static final byte CLI_STRICQ = 16;
	public static final byte CLI_YSM = 17;
	public static final byte CLI_VICQ = 18;
	public static final byte CLI_LIBICQ2000 = 19;
	public static final byte CLI_JIMM = 20;
	public static final byte CLI_SMARTICQ = 21;
	public static final byte CLI_ICQLITE4 = 22;
	public static final byte CLI_ICQLITE5 = 23;
	public static final byte CLI_ICQ98 = 24;
	public static final byte CLI_ICQ99 = 25;
	public static final byte CLI_ICQ2001B = 26;
	public static final byte CLI_ICQ2002A2003A = 27;
	public static final byte CLI_ICQ2000 = 28;
	public static final byte CLI_ICQ2003B = 29;
	public static final byte CLI_ICQLITE = 30;
	public static final byte CLI_GNOMEICQ = 31;
	public static final byte CLI_AGILE = 32;
	public static final byte CLI_SPAM = 33;
	public static final byte CLI_CENTERICQ = 34;
	public static final byte CLI_LIBICQJABBER = 35;
	public static final byte CLI_ICQ2GO = 36;
	public static final byte CLI_ICQPPC = 37;
	public static final byte CLI_STICQ = 38;
	public static final byte CLI_MCHAT = 39;
	public static final byte CLI_QIPINFIUM = 40;
	public static final byte CLI_ICQ6 = 41;
	public static final byte CLI_QIPPDASYM = 42;
	public static final byte CLI_QIPPDAWIN = 43;
	public static final byte CLI_IMPLUS = 44;
	public static final byte CLI_JIMM_SMAPER = 45;
	
	private static int[] clientIndexes;
	private static int[] clientImageIndexes;
	private static String[] clientNames;
	
	private static void initClientIndData()
	{
		Vector vInd = new Vector();
		Vector vImg = new Vector();
		Vector vNames = new Vector();
		//                    name                      index              image index
		initClientIndDataItem("Not detected",           CLI_NONE,          -1, vInd, vImg, vNames);
		initClientIndDataItem("QIP",                    CLI_QIP,           1,  vInd, vImg, vNames);
		initClientIndDataItem("Miranda",                CLI_MIRANDA,       2,  vInd, vImg, vNames);
		initClientIndDataItem("LIcq",                   CLI_LICQ,          26, vInd, vImg, vNames);
		initClientIndDataItem("Trillian",               CLI_TRILLIAN,      5,  vInd, vImg, vNames);
		initClientIndDataItem("SIM",                    CLI_SIM,           6,  vInd, vImg, vNames);
		initClientIndDataItem("Kopete",                 CLI_KOPETE,        7,  vInd, vImg, vNames);
		initClientIndDataItem("MICQ",                   CLI_MICQ,          -1, vInd, vImg, vNames);
		initClientIndDataItem("&RQ",                    CLI_ANDRQ,         3,  vInd, vImg, vNames);
		initClientIndDataItem("IM2",                    CLI_IM2,           29, vInd, vImg, vNames);
		initClientIndDataItem("ICQ for MAC",            CLI_MACICQ,        23, vInd, vImg, vNames);
		initClientIndDataItem("AIM",                    CLI_AIM,           -1, vInd, vImg, vNames);
		initClientIndDataItem("UIM",                    CLI_UIM,           -1, vInd, vImg, vNames);
		initClientIndDataItem("WebICQ",                 CLI_WEBICQ,        -1, vInd, vImg, vNames);
		initClientIndDataItem("Gaim",                   CLI_GAIM,          24, vInd, vImg, vNames);
		initClientIndDataItem("Alicq",                  CLI_ALICQ,         -1, vInd, vImg, vNames);
		initClientIndDataItem("StrICQ",                 CLI_STRICQ,        -1, vInd, vImg, vNames);
		initClientIndDataItem("YSM",                    CLI_YSM,           -1, vInd, vImg, vNames);
		initClientIndDataItem("vICQ",                   CLI_VICQ,          -1, vInd, vImg, vNames);
		initClientIndDataItem("Libicq2000",             CLI_LIBICQ2000,    11, vInd, vImg, vNames);
		initClientIndDataItem("Jimm",                   CLI_JIMM,           8, vInd, vImg, vNames);
		initClientIndDataItem("SmartICQ",               CLI_SMARTICQ,      -1, vInd, vImg, vNames);
		initClientIndDataItem("ICQ Lite v4",            CLI_ICQLITE4,      18, vInd, vImg, vNames);
		initClientIndDataItem("ICQ Lite v5",            CLI_ICQLITE5,      19, vInd, vImg, vNames);
		initClientIndDataItem("ICQ 98",                 CLI_ICQ98,         -1, vInd, vImg, vNames);
		initClientIndDataItem("ICQ 99",                 CLI_ICQ99,         -1, vInd, vImg, vNames);
		initClientIndDataItem("ICQ 2001b",              CLI_ICQ2001B,      -1, vInd, vImg, vNames);
		initClientIndDataItem("ICQ 2002a/2003a",        CLI_ICQ2002A2003A, -1, vInd, vImg, vNames);
		initClientIndDataItem("ICQ 2000",               CLI_ICQ2000,       -1, vInd, vImg, vNames);
		initClientIndDataItem("ICQ 2003b",              CLI_ICQ2003B,      20, vInd, vImg, vNames);
		initClientIndDataItem("ICQ Lite",               CLI_ICQLITE,       17, vInd, vImg, vNames);
		initClientIndDataItem("Gnome ICQ",              CLI_GNOMEICQ,      25, vInd, vImg, vNames);
		initClientIndDataItem("Agile Messenger",        CLI_AGILE,         10, vInd, vImg, vNames);
		initClientIndDataItem("SPAM:)",                 CLI_SPAM,          -1, vInd, vImg, vNames);
		initClientIndDataItem("CenterICQ",              CLI_CENTERICQ,     -1, vInd, vImg, vNames);
		initClientIndDataItem("Libicq2000 from Jabber", CLI_LIBICQJABBER,  -1, vInd, vImg, vNames);
		initClientIndDataItem("ICQ2GO!",                CLI_ICQ2GO,        21, vInd, vImg, vNames);
		initClientIndDataItem("ICQ for Pocket PC",      CLI_ICQPPC,        -1, vInd, vImg, vNames);
		initClientIndDataItem("StIcq",                  CLI_STICQ,         9,  vInd, vImg, vNames);
		initClientIndDataItem("MChat",                  CLI_MCHAT,         22, vInd, vImg, vNames);
		initClientIndDataItem("QIP Infium",             CLI_QIPINFIUM,     15, vInd, vImg, vNames);
		initClientIndDataItem("ICQ 6",                  CLI_ICQ6,          16, vInd, vImg, vNames);
		initClientIndDataItem("QIP Mobile (Symbian)",   CLI_QIPPDASYM,     13, vInd, vImg, vNames);
		initClientIndDataItem("QIP PDA (Windows)",      CLI_QIPPDAWIN,     14, vInd, vImg, vNames);
		initClientIndDataItem("IM+",                    CLI_IMPLUS,        30, vInd, vImg, vNames);
		initClientIndDataItem("Smaper",                 CLI_JIMM_SMAPER,   31, vInd, vImg, vNames);
		
		clientNames = new String[vNames.size()];
		vNames.copyInto(clientNames);
		
		clientIndexes = new int[vInd.size()];
		for (int i = vInd.size()-1; i >= 0; i--) clientIndexes[i] = ((Integer)vInd.elementAt(i)).intValue();
		
		clientImageIndexes = new int[vImg.size()];
		for (int i = vImg.size()-1; i >= 0; i--) clientImageIndexes[i] = ((Integer)vImg.elementAt(i)).intValue();
	}
	
	private static void initClientIndDataItem(String name, int index, int imageIndex, Vector vIndexes, Vector vImg, Vector vNames)
	{
		vNames.addElement(name);
		vIndexes.addElement(new Integer(index));
		vImg.addElement(new Integer(imageIndex-1));
	}
	
	private static int convertJimmXStatToIcqXStat(int jimmValue)
	{
		for (int i = 0; i < XSTATUS_CONSTS.length; i += 18) 
			if (XSTATUS_CONSTS[i] == (byte)jimmValue) 
				return XSTATUS_CONSTS[i+1] == 255 ? -1 : XSTATUS_CONSTS[i+1]; 
		return -1;
	}
	
	private static final byte[] xStWord = {'i', 'c', 'q', 'm', 'o', 'o', 'd' };
	
	/* TODO: find out format of 001D TLV and correct method! */
	public static int detectStandartXStatus(byte[] data)
	{
		int idx = Util.indexOf(data, xStWord);
		if (idx < 2) return -1;
		int len = Util.getWord(data, idx-2);
		String strData = Util.byteArrayToString(data, idx, len, false);
		String text = strData.substring(7);
		int stdStatusValue = Util.strToIntDef(text, -1);
		for (int i = 0; i < XSTATUS_CONSTS.length; i += 18)
			if (XSTATUS_CONSTS[i+1] == stdStatusValue) return XSTATUS_CONSTS[i]; 
		return -1;
	}
	
	public static int detectXStatus(byte[] capabilities)
	{
		for (int i = 0; i < capabilities.length; i += 16)
		{
			for (int j = 0; j < XSTATUS_CONSTS.length; j += 18)
			{
				int counter = 0;
				for (int k = 0; k < 16; k++, counter++) 
					if (capabilities[i+k] != XSTATUS_CONSTS[j+k+2]) break;
				if (counter == 16) return XSTATUS_CONSTS[j];
			}
		}
		return -1;
	}
	
	public static void detectUserClientAndParseCaps(ContactItem item, int dwFP1, int dwFP2, int dwFP3, byte[] capabilities, int wVersion, boolean statusChange)
	{
		int client = CLI_NONE;
		String szVersion = "";
		long caps = CAPF_NO_INTERNAL;

		if (capabilities != null)
		{
			//Caps parsing
			for (int j = 0; j < capabilities.length / 16; j++)
			{
				int j16 = j * 16;
				if (Util.byteArrayEquals(capabilities, j16, CAP_AIM_SERVERRELAY, 0, 16))
				{
					caps |= CAPF_AIM_SERVERRELAY;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_UTF8, 0, 16))
				{
					caps |= CAPF_UTF8_INTERNAL;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_MIRANDAIM, 0, 8))
				{
					caps |= CAPF_MIRANDAIM;
					szVersion = detectClientVersion(capabilities, CLI_MIRANDA, j);
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_TRILLIAN, 0, 16))
				{
					caps |= CAPF_TRILLIAN;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_TRILCRYPT, 0, 16))
				{
					caps |= CAPF_TRILCRYPT;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_SIM, 0, 0xC))
				{
					caps |= CAPF_SIM;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_SIMOLD, 0, 16))
				{
					caps |= CAPF_SIMOLD;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_LICQ, 0, 0xC))
				{
					caps |= CAPF_LICQ;
					szVersion = detectClientVersion(capabilities, CLI_LICQ, j);
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_KOPETE, 0, 0xC))
				{
					caps |= CAPF_KOPETE;
					szVersion = detectClientVersion(capabilities, CLI_KOPETE, j);
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_MICQ, 0, 16))
				{
					caps |= CAPF_MICQ;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_ANDRQ, 0, 9))
				{
					caps |= CAPF_ANDRQ;
					szVersion = detectClientVersion(capabilities, CLI_ANDRQ, j);
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_QIPPDASYM, 0, 16))
				{
					caps |= CAPF_QIPPDASYM;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_QIPPDAWIN, 0, 16))
				{
					caps |= CAPF_QIPPDAWIN;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_QIP, 0, 11))
				{
					caps |= CAPF_QIP;
					szVersion = detectClientVersion(capabilities, CLI_QIP, j);
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_IM2, 0, 16))
				{
					caps |= CAPF_IM2;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_MACICQ, 0, 16))
				{
					caps |= CAPF_MACICQ;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_RICHTEXT, 0, 16))
				{
					caps |= CAPF_RICHTEXT;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_IS2001, 0, 16))
				{
					caps |= CAPF_IS2001;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_IS2002, 0, 16))
				{
					caps |= CAPF_IS2002;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_STR20012, 0, 16))
				{
					caps |= CAPF_STR20012;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_AIMICON, 0, 16))
				{
					caps |= CAPF_AIMICON;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_AIMCHAT, 0, 16))
				{
					caps |= CAPF_AIMCHAT;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_UIM, 0, 16))
				{
					caps |= CAPF_UIM;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_RAMBLER, 0, 16))
				{
					caps |= CAPF_RAMBLER;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_ABV, 0, 16))
				{
					caps |= CAPF_ABV;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_NETVIGATOR, 0, 16))
				{
					caps |= CAPF_NETVIGATOR;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_XTRAZ, 0, 16))
				{
					caps |= CAPF_XTRAZ;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_AIMFILE, 0, 16))
				{
					caps |= CAPF_AIMFILE;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_JIMM, 0, 5))
				{
					caps |= CAPF_JIMM;
					szVersion = detectClientVersion(capabilities, CLI_JIMM, j);
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_JIMM_SMAPER, 0, CAP_JIMM_SMAPER.length))
				{
					caps |= CAPF_JIMM_SMAPER;
					szVersion = detectClientVersion(capabilities, CLI_JIMM_SMAPER, j);
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_AIMIMIMAGE, 0, 16)) caps |= CAPF_AIMIMIMAGE;
				else if (Util.byteArrayEquals(capabilities, j16, CAP_AVATAR, 0, 16)) caps |= CAPF_AVATAR;
				else if (Util.byteArrayEquals(capabilities, j16, CAP_DIRECT, 0, 16)) caps |= CAPF_DIRECT;
				else if (Util.byteArrayEquals(capabilities, j16, CAP_TYPING, 0, 16)) caps |= CAPF_TYPING;
				else if (Util.byteArrayEquals(capabilities, j16, CAP_MCHAT, 0, 9))
				{
					caps |= CAPF_MCHAT;
					szVersion = detectClientVersion(capabilities, CLI_MCHAT, j);
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_QIPINFIUM, 0, 16))
				{
					caps |= CAPF_QIPINFIUM;
				}
//				else if (Util.byteArrayEquals(capabilities, j16, CAP_AUDIO, 0, 16))
//				{
//					caps |= CAPF_AUDIO;
//				}
//				else if (Util.byteArrayEquals(capabilities, j16, CAP_VIDEO, 0, 16))
//				{
//					caps |= CAPF_VIDEO;
//				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_HTMLMESSAGES, 0, 16))
				{
					caps |= CAPF_HTMLMESSAGES;
				}
//				else if (Util.byteArrayEquals(capabilities, j16, CAP_XMultiUserChat, 0, 16))
//				{
//					caps |= CAPF_XMultiUserChat;
//				}
//				else if (Util.byteArrayEquals(capabilities, j16, CAP_XtZers, 0, 16))
//				{
//					caps |= CAPF_XtZers;
//				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_IsICQLITE, 0, 16))
				{
					caps |= CAPF_IsICQLITE;
				}
				else if (Util.byteArrayEquals(capabilities, j16, CAP_IMPLUS, 0, 16))
				{
					caps |= CAPF_IMPLUS;
				}
			}
		}

		//Client detection
		//If this is status change we don`t need to detect client... 
		if (!statusChange)
		{
			switch (1)
			{
			default:
				if ((caps & CAPF_IMPLUS) != 0)
				{
					client = CLI_IMPLUS;
  					if ((dwFP1 & 0xFFFFFFF0) == 0x494D2B00) {
						switch (dwFP1 & 0xFF) {
							case 0x03:		//SmartPhone, Pocket PC
								szVersion += "(SmartPhone, Pocket PC)";
								break;
							case 0x05:		//Win32
								szVersion += "(Win32)";
								break;
						}
					}
					break;
				}
				if ((caps & CAPF_JIMM_SMAPER) != 0)
				{
					client = CLI_JIMM_SMAPER;
				}
				if ((caps & CAPF_QIPINFIUM) != 0)
				{
					client = CLI_QIPINFIUM;
					szVersion += "(" + dwFP1 + ")" + ((dwFP2 == 0xB) ? " Beta" : "");
					break;
				}
				if ((caps & CAPF_QIPPDASYM) != 0)
				{
					client = CLI_QIPPDASYM;
					break;
				}
				if ((caps & CAPF_QIPPDAWIN) != 0)
				{
					client = CLI_QIPPDAWIN;
					break;
				}
				if ((caps & CAPF_MCHAT) != 0)
				{
					client = CLI_MCHAT;
					break;
				}
				if ((caps & CAPF_JIMM) != 0)
				{
					client = CLI_JIMM;
					break;
				}
				if ((caps & CAPF_QIP) != 0)
				{
					client = CLI_QIP;
					if (((dwFP1 >> 24) & 0xFF) != 0) szVersion += " (" + ((dwFP1 >> 24) & 0xFF) + ((dwFP1 >> 16) & 0xFF) + ((dwFP1 >> 8) & 0xFF)
							+ (dwFP1 & 0xFF) + ")";
					break;
				}

				if (((caps & (CAPF_TRILLIAN + CAPF_TRILCRYPT)) != 0) && (dwFP1 == 0x3b75ac09))
				{
					client = CLI_TRILLIAN;
					break;
				}

				if (((caps & CAPF_IM2) != 0) && (dwFP1 == 0x3FF19BEB))
				{
					client = CLI_IM2;
					break;
				}

				if ((caps & (CAPF_SIM + CAPF_SIMOLD)) != 0)
				{
					client = CLI_SIM;
					break;
				}

				if ((caps & CAPF_KOPETE) != 0)
				{
					client = CLI_KOPETE;
					break;
				}

				if ((caps & CAPF_LICQ) != 0)
				{
					client = CLI_LICQ;
					break;
				}

				if (((caps & CAPF_AIMICON) != 0) && ((caps & CAPF_AIMFILE) != 0) && ((caps & CAPF_AIMIMIMAGE) != 0))
				{
					client = CLI_GAIM;
					break;
				}

				if ((caps & CAPF_UTF8_INTERNAL) != 0)
				{
					switch (wVersion)
					{
					case 10:
						if (((caps & CAPF_TYPING) != 0) && ((caps & CAPF_RICHTEXT) != 0))
						{
							client = CLI_ICQ2003B;
						}
					case 7:
						if (((caps & CAPF_AIM_SERVERRELAY) == 0) && ((caps & CAPF_DIRECT) == 0) && (dwFP1 == 0) && (dwFP2 == 0) && (dwFP3 == 0))
						{
							client = CLI_ICQ2GO;
						}
						break;
					default:
						if ((dwFP1 == 0) && (dwFP2 == 0) && (dwFP3 == 0))
						{
							if ((caps & CAPF_RICHTEXT) != 0)
							{
								client = CLI_ICQLITE;
								if (((caps & CAPF_AVATAR) != 0) && ((caps & CAPF_XTRAZ) != 0))
								{
									if ((caps & CAPF_AIMFILE) != 0) // TODO: add more
									client = CLI_ICQLITE5;
									else client = CLI_ICQLITE4;
								}
							}
							else if (((caps & CAPF_IsICQLITE) != 0) && ((caps & CAPF_HTMLMESSAGES) != 0))
								{
									client = CLI_ICQ6;
								}
							else if ((caps & CAPF_UIM) != 0) client = CLI_UIM;
							else client = CLI_AGILE;
						}
						break;
					}
				}

				if ((caps & CAPF_MACICQ) != 0)
				{
					client = CLI_MACICQ;
					break;
				}

				if (((caps & CAPF_AIMCHAT) != 0) && ((caps & CAPF_IsICQLITE) == 0))
				{
					client = CLI_AIM;
					break;
				}

				if ((dwFP1 & 0xFF7F0000) == 0x7D000000)
				{
					client = CLI_LICQ;
					int ver = dwFP1 & 0xFFFF;
					if (ver % 10 != 0)
					{
						szVersion = ver / 1000 + "." + (ver / 10) % 100 + "." + ver % 10;
					}
					else
					{
						szVersion = ver / 1000 + "." + (ver / 10) % 100;
					}
					break;
				}

				switch (dwFP1)
				{
				case 0x7FFFFFFF:

					if ((caps & CAPF_MIRANDAIM) != 0)
					{
						client = CLI_MIRANDA;
						szVersion = "IM: " + szVersion + " ICQ: " +((dwFP2 >> 24) & 0x7F) + "." + ((dwFP2 >> 16) & 0xFF) + "." + ((dwFP2 >> 8) & 0xFF) + "." + (dwFP2 & 0xFF);
					}
					break;
				case 0xFFFFFFFF:
					if ((dwFP3 == 0xFFFFFFFF) && (dwFP2 == 0xFFFFFFFF))
					{
						client = CLI_GAIM;
						break;
					}
					if ((dwFP2 == 0) && (dwFP3 != 0xFFFFFFFF))
					{
						if (wVersion == 7)
						{
							client = CLI_WEBICQ;
							break;
						}
						if ((dwFP3 == 0x3B7248ED) && ((caps & CAPF_UTF8_INTERNAL) == 0) && ((caps & CAPF_RICHTEXT) == 0))
						{
							client = CLI_SPAM;
							break;
						}
					}
					client = CLI_MIRANDA;
					szVersion = ((dwFP2 >> 24) & 0x7F) + "." + ((dwFP2 >> 16) & 0xFF) + "." + ((dwFP2 >> 8) & 0xFF) + "." + (dwFP2 & 0xFF);
					break;
				case 0xFFFFFFFE:
					if (dwFP3 == dwFP1)
					{
						client = CLI_JIMM;
					}
					break;
				case 0xFFFFFF8F:
					client = CLI_STRICQ;
					break;
				case 0xFFFFFF42:
					client = CLI_MICQ;
					break;
				case 0xFFFFFFBE:
					client = CLI_ALICQ;
					break;
				case 0xFFFFFF7F:
					client = CLI_ANDRQ;
					szVersion = ((dwFP2 >> 24) & 0xFF) + "." + ((dwFP2 >> 16) & 0xFF) + "." + ((dwFP2 >> 8) & 0xFF) + "." + (dwFP2 & 0xFF);
					break;
				case 0xFFFFFFAB:
					client = CLI_YSM;
					break;
				case 0x04031980:
					client = CLI_VICQ;
					break;
				case 0x3AA773EE:
					if ((dwFP2 == 0x3AA66380) && (dwFP3 == 0x3A877A42))
					{
						if (wVersion == 7)
						{
							if (((caps & CAPF_AIM_SERVERRELAY) != 0) && ((caps & CAPF_DIRECT) != 0))
							{
								if ((caps & CAPF_RICHTEXT) != 0)
								{
									client = CLI_CENTERICQ;
									break;
								}
								client = CLI_LIBICQJABBER;
							}
						}
						client = CLI_LIBICQ2000;
					}
					break;
				case 0x3b75ac09:
					client = CLI_TRILLIAN;
					break;
				case 0x3BA8DBAF: // FP2: 0x3BEB5373; FP3: 0x3BEB5262;
					if (wVersion == 2) client = CLI_STICQ;
					break;
				case 0x3FF19BEB:
					if ((wVersion == 8) && (dwFP1 == dwFP3)) //FP2: 0x3FEC05EB; FP3: 0x3FF19BEB;
					client = CLI_IM2;
					break;
				case 0x4201F414:
					if (((dwFP2 & dwFP3) == dwFP1) && (wVersion == 8)) client = CLI_SPAM;
					break;
				default:
					break;
				}

				if (client != CLI_NONE) break;

				if ((dwFP1 != 0) && (dwFP1 == dwFP3) && (dwFP3 == dwFP2) && (caps == 0))
				{
					client = CLI_VICQ;
					break;
				}
				if (((caps & CAPF_AIM_SERVERRELAY) != 0) && ((caps & CAPF_DIRECT) != 0) && ((caps & CAPF_UTF8_INTERNAL) != 0)
						&& ((caps & CAPF_RICHTEXT) != 0))
				{

					if ((dwFP1 != 0) && (dwFP2 != 0) && (dwFP3 != 0)) client = CLI_ICQ2002A2003A;
					break;
				}
				if (((caps & (CAPF_STR20012 + CAPF_AIM_SERVERRELAY)) != 0) && ((caps & CAPF_IS2001) != 0))
				{
					if ((dwFP1 == 0) && (dwFP2 == 0) && (dwFP3 == 0) && (wVersion == 0)) client = CLI_ICQPPC;
					else client = CLI_ICQ2001B; //FP1: 1068985885; FP2:0; FP3:1068986138
					break;
				}
				if (wVersion == 7)
				{
					if (((caps & CAPF_AIM_SERVERRELAY) != 0) && ((caps & CAPF_DIRECT) != 0))
					{
						if ((dwFP1 == 0) && (dwFP2 == 0) && (dwFP3 == 0)) client = CLI_ANDRQ;
						else client = CLI_ICQ2000;
						break;
					}
					else if ((caps & CAPF_RICHTEXT) != 0)
					{
						client = CLI_GNOMEICQ;
						break;
					}
				}
				if (dwFP1 > 0x35000000 && dwFP1 < 0x40000000)
				{
					switch (wVersion)
					{
					case 6:
						client = CLI_ICQ99;
						break;
					case 7:
						client = CLI_ICQ2000;
						break;
					case 8:
						client = CLI_ICQ2001B;
						break;
					case 9:
						client = CLI_ICQLITE;
						break;
					case 10:
						client = CLI_ICQ2003B;
						break;
					}
					break;
				}
			}
			if (client != CLI_NONE)
			{
				item.setIntValue(ContactItem.CONTACTITEM_CLIENT, client);
				item.setStringValue(ContactItem.CONTACTITEM_CLIVERSION, szVersion);
			}
		}

		item.setIntValue(ContactItem.CONTACTITEM_CAPABILITIES, (int)(caps&0xFFFFFFFF));
		//#sijapp cond.if modules_DEBUGLOG is "true"#
		System.out.println("uin - " + item.getStringValue(ContactItem.CONTACTITEM_UIN) + " found capabilities count:" + capabilities.length/16);
		System.out.println("dwFP1 = " + "0x"+Integer.toHexString(dwFP1) + ", " + "dwFP2 = " + "0x"+Integer.toHexString(dwFP2) + ", " + "dwFP3 = " + "0x"+Integer.toHexString(dwFP3));
		System.out.println("wVersion = " + Integer.toHexString(wVersion));
		System.out.println("client = " + client + ", " + "szVersion = " + szVersion);
		Util.PrintCapabilities("", capabilities);
		//#sijapp cond.end#
	}

	public static String getClientString(int cli)
	{
		for (int i = clientIndexes.length-1; i >= 0; i--)
			if (clientIndexes[i] == cli) return (clientNames[i]);
		return null;
	}
	
	public static int getClientImageID(int cli)
	{
		for (int i = clientIndexes.length-1; i >= 0; i--) 
			if (clientIndexes[i] == cli) return clientImageIndexes[i]; 
		return -1; 
	}

	private static String detectClientVersion(byte[] buf1, int cli,
			int tlvNum)
	{
		byte[] buf = new byte[16];
		System.arraycopy(buf1, tlvNum * 16, buf, 0, 16);
		String ver = "";
		
		switch (cli)
		{
		case CLI_MIRANDA:
			if ((buf[0xC] == 0) && (buf[0xD] == 0) && (buf[0xE] == 0)
					&& (buf[0xF] == 1))
			{
				ver = "0.1.2.0";
			} else if ((buf[0xC] == 0) && (buf[0xD] <= 3) && (buf[0xE] <= 3)
					&& (buf[0xF] <= 1))
			{
				ver = "0." + buf[0xD] + "." + buf[0xE] + "." + buf[0xF];
			} else
			{
				ver = (buf[0x8] & 0x7F) + "." + buf[0x9] + "." + buf[0xA] + "."
						+ buf[0xB];
			}
			break;
			
		case CLI_LICQ:
			ver = buf[0xC] + "." + (buf[0xD] % 100) + "." + buf[0xE];
			break;
			
		case CLI_KOPETE:
			ver = buf[0xC] + "." + buf[0xD] + "." + buf[0xE] + "." + buf[0xF];
			break;
			
		case CLI_ANDRQ:
			ver = (char) buf[0xC] + "." + (char) buf[0xB];// + "." +buf[0xA] + "." +buf[9];
			break;
			
		case CLI_JIMM:
			ver = Util.byteArrayToString(buf, 5, 11);
			break;
			
		case CLI_QIP:
			ver = Util.byteArrayToString(buf, 11, 5);
			break;
			
		case CLI_MCHAT:
			ver = Util.byteArrayToString(buf, 0, 16);
			break;
			
		case CLI_JIMM_SMAPER:
			ver = Util.byteArrayToString(buf, CAP_JIMM_SMAPER.length, 16-CAP_JIMM_SMAPER.length);
			break;
		}

		return ver;
	}
	
	// Merge two received capabilities into one byte array
	public static byte[] mergeCapabilities(byte[] capabilities_old,
			byte[] capabilities_new)
	{
		if (capabilities_new == null)
			return capabilities_old;
		if (capabilities_old == null)
			return capabilities_new;

		// Extend new capabilities to match with old ones
		byte[] extended_new = new byte[capabilities_new.length * 8];
		for (int i = 0; i < capabilities_new.length; i += 2)
		{
			System.arraycopy(CAP_OLD_HEAD, 0, extended_new, (i * 8),
					CAP_OLD_HEAD.length);
			System.arraycopy(capabilities_new, i, extended_new,
					((i * 8) + CAP_OLD_HEAD.length), 2);
			System.arraycopy(CAP_OLD_TAIL, 0, extended_new, ((i * 8)
					+ CAP_OLD_HEAD.length + 2), CAP_OLD_TAIL.length);
		}
		// Check for coexisting capabilities and merge
		boolean found = false;
		for (int i = 0; i < capabilities_old.length; i += 16)
		{
			byte[] tmp_old = new byte[16];
			System.arraycopy(capabilities_old, i, tmp_old, 0, 16);
			for (int j = 0; j < extended_new.length; j += 16)
			{
				//System.out.println(j + " " + i + " " + extended_new.length);
				byte[] tmp_new = new byte[16];
				System.arraycopy(extended_new, j, tmp_new, 0, 16);
				if (tmp_old == tmp_new)
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				//System.out.println("Merge capability");
				byte[] merged = new byte[extended_new.length + 16];
				System.arraycopy(extended_new, 0, merged, 0,
						extended_new.length);
				System.arraycopy(tmp_old, 0, merged, extended_new.length,
						tmp_old.length);
				extended_new = merged;
				found = false;
			}
		}
		return extended_new;
	}
	
	static public void sendCLI_ADDSTART() throws JimmException
	{
		sendPacket(new SnacPacket(SnacPacket.CLI_ADDSTART_FAMILY,
				SnacPacket.CLI_ADDSTART_COMMAND, SnacPacket.CLI_ADDSTART_COMMAND,
				new byte[0], new byte[0]));
	}

	static public void sendCLI_ADDEND() throws JimmException
	{
		sendPacket(new SnacPacket(SnacPacket.CLI_ADDEND_FAMILY,
				SnacPacket.CLI_ADDEND_COMMAND, SnacPacket.CLI_ADDEND_COMMAND, new byte[0],
				new byte[0]));
	}
	
	static public final int PROCESS_BUDDY_ADD = 10000;
	static public final int PROCESS_BUDDY_DELETE = 10001;
	
	static public void sendProcessBuddy(int mode, String name, int id, int groupId, int buddyType) throws JimmException
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		/* Name */
		Util.writeLenAndString(buffer, name, true);

		/* Group ID */
		Util.writeWord(buffer, groupId, true);

		/* ID */
		Util.writeWord(buffer, id, true);

		/* Type */
		Util.writeWord(buffer, buddyType, true);
		
		/* No additional data */
		Util.writeWord(buffer, 0, true);
		
		int command = -1;
		
		switch (mode)
		{
		case PROCESS_BUDDY_ADD:
			command = SnacPacket.CLI_ROSTERADD_COMMAND;
			break;
			
		case PROCESS_BUDDY_DELETE:
			command = SnacPacket.CLI_ROSTERDELETE_COMMAND;
			break;
		
		default:
			throw new JimmException(0, 0);
		}
		
		SnacPacket packet = new SnacPacket(0x0013, command, Util.getCounter(), new byte[0], buffer.toByteArray());
		
		sendPacket(packet);
	}

	public static boolean runActionAndProcessError(Action act) 
	{
		try
		{
			Icq.requestAction(act);
		} catch (JimmException e)
		{
			JimmException.handleException(e);
			return false;
		}
		
		SplashCanvas.addTimerTask("wait", act, false);
		
		return true;
	}
	
	public static void removeLocalContact(String uin)
	{
		byte[] buf = new byte[1 + uin.length()];
		Util.putByte(buf, 0, uin.length());
		System.arraycopy(uin.getBytes(), 0, buf, 1, uin.length());
		try
		{
			sendPacket(new SnacPacket(0x0003, 0x0005, 0, new byte[0], buf));
		} catch (JimmException e)
		{
			JimmException.handleException(e);
		}
	}

	// Adds a ContactItem to the server saved contact list
	static public synchronized void addToContactList(
			ContactItem cItem)
	{
		// Request contact item adding
		UpdateContactListAction act = new UpdateContactListAction(cItem,
				UpdateContactListAction.ACTION_ADD);

		try
		{
			requestAction(act);
		} catch (JimmException e)
		{
			JimmException.handleException(e);
			if (e.isCritical())
				return;
		}

		// Start timer
		SplashCanvas.addTimerTask("wait", act, false);
		// System.out.println("start addContact");
	}

	// Dels a ContactItem to the server saved contact list
	static public synchronized boolean delFromContactList(
			ContactItem cItem)
	{
		// Check whether contact item is temporary
		if (cItem.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP) && 
				!cItem.getBooleanValue(ContactItem.CONTACTITEM_IS_PHANTOM))
		{
			// Remove this temporary contact item
			removeLocalContact(cItem
					.getStringValue(ContactItem.CONTACTITEM_UIN));
			ContactList.removeContactItem(cItem);

			// Activate contact list
			ContactList.activateList();
		} 
		else
		{
			// Request contact item removal
			UpdateContactListAction act2 = new UpdateContactListAction(cItem,
					UpdateContactListAction.ACTION_DEL);
			try
			{
				Icq.requestAction(act2);
			} catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical())
					return false;
			}

			// Start timer
			SplashCanvas.addTimerTask("wait", act2, false);
		}
		return true;
	}

	//#sijapp cond.if target isnot "DEFAULT"#
	public synchronized static void beginTyping(String uin, boolean isTyping)
			throws JimmException
	{
		byte[] uinRaw = Util.stringToByteArray(uin);
		int tempBuffLen = Icq.MTN_PACKET_BEGIN.length + 1 + uinRaw.length + 2;
		int marker = 0;
		byte[] tempBuff = new byte[tempBuffLen];
		System.arraycopy(Icq.MTN_PACKET_BEGIN, 0, tempBuff, marker,
				Icq.MTN_PACKET_BEGIN.length);
		marker += Icq.MTN_PACKET_BEGIN.length;
		Util.putByte(tempBuff, marker, uinRaw.length);
		marker += 1;
		System.arraycopy(uinRaw, 0, tempBuff, marker, uinRaw.length);
		marker += uinRaw.length;
		Util.putWord(tempBuff, marker, ((isTyping) ? (0x0002) : (0x0000)));
		marker += 2;
		// Send packet
		SnacPacket snacPkt = new SnacPacket(0x0004, 0x0014, 0x00000000,
				new byte[0], tempBuff);
		sendPacket(snacPkt);
	}

	//#sijapp cond.end#
	
	
}
