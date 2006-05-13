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
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
import javax.microedition.io.SocketConnection;
// #sijapp cond.else#
import javax.microedition.io.StreamConnection;
// #sijapp cond.end#

import jimm.ContactListContactItem;
import jimm.Jimm;
import jimm.JimmException;
import jimm.MainMenu;
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
	{
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x01
	};
    // State constants
    private static final int STATE_NOT_CONNECTED = 0;
    private static final int STATE_CONNECTED = 1;

    // Current state
    static private boolean connected = false;

    // Requested actions
    static private Vector reqAction = new Vector();

    // Thread
    static volatile Thread thread;
    
    // FLAP sequence number
    static int flapSEQ;
    
    public Icq()
    {
    	_this = this;
    	// Set starting point for seq numbers (not bigger then 0x8000)
        Random rand = new Random(System.currentTimeMillis());
        flapSEQ = rand.nextInt()%0x8000;
    }

    // Request an action
    static public void requestAction(Action act) throws JimmException
    {
        // Set reference to this ICQ object for callbacks
        act.setIcq(_this);

        // Look whether action is executable at the moment
        if (!act.isExecutable()) { throw (new JimmException(140, 0)); }

        // Queue requested action
        synchronized (_this)
        {
            reqAction.addElement(act);
        }

        // Connect?
        if (act instanceof ConnectAction)
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

	// Sends to server client-side contacts
	public static void addLocalContacts(String[] uins)
	{
		if (uins.length == 0) return;
		
		int len = 0, i;
		
		for (i = 0; i < uins.length; i++) len += (1+uins[i].length());
		
		byte[] buf = new byte[len];
		
		int position = 0;
		for (i = 0; i < uins.length; i++)
		{
			byte[] rowUin = Util.stringToByteArray(uins[i]);
			buf[position++] = (byte)rowUin.length;
			System.arraycopy(rowUin, 0, buf, position, rowUin.length);
			position += rowUin.length;
		}
		
		try 
		{
			c.sendPacket(new SnacPacket(0x0003, 0x0004, 0, new byte[0], buf));
		} catch (JimmException e) 
		{
			JimmException.handleException(e);
		}
	}

	public static void removeLocalContact(String uin) {
		byte[] buf = new byte[1 + uin.length()];
		Util.putByte(buf, 0, uin.length());
		System.arraycopy(uin.getBytes(), 0, buf, 1, uin.length());
		try {
			c.sendPacket(new SnacPacket(0x0003, 0x0005, 0, new byte[0], buf));
		} catch (JimmException e) {
			JimmException.handleException(e);
		}
	}

    // Adds a ContactListContactItem to the server saved contact list
    static public synchronized void addToContactList(ContactListContactItem cItem)
    {
        // Request contact item adding
        UpdateContactListAction act = new UpdateContactListAction(cItem, UpdateContactListAction.ACTION_ADD);
        
        try
        {
            requestAction(act);
        } catch (JimmException e)
        {
            JimmException.handleException(e);
            if (e.isCritical()) return;
        }

        // Start timer
        SplashCanvas.addTimerTask("wait", act, false);
        // System.out.println("start addContact");
    }

    // Connects to the ICQ network
    static public synchronized void connect()
    {
        // #sijapp cond.if target isnot "MOTOROLA"#
        if (Options.getBoolean(Options.OPTION_SHADOW_CON))
        {
            // Make the shadow connection for Nokia 6230 of other devices if
            // needed
            ContentConnection ctemp = null;
            DataInputStream istemp = null;
            try
            {
                String url = "http://shadow.jimm.org/";
                ctemp = (ContentConnection) Connector.open(url);

                istemp = ctemp.openDataInputStream();
            } catch (Exception e)
            {
                // Do nothing
            }
        }
        // #sijapp cond.end#
        // Connect
        ConnectAction act = new ConnectAction(Options.getString(Options.OPTION_UIN), Options.getString(Options.OPTION_PASSWORD), Options.getString(Options.OPTION_SRV_HOST), Options.getString(Options.OPTION_SRV_PORT));
        try
        {
            requestAction(act);

        } catch (JimmException e)
        {
            JimmException.handleException(e);
            if (e.isCritical()) return;
        }

        // Start timer
        SplashCanvas.addTimerTask("connecting", act, false);
    }

    // Disconnects from the ICQ network
    static public synchronized void disconnect()
    {
        if (!connected) return;


        // Disconnect
		c.close();
		resetServerCon();
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		// #sijapp cond.if modules_FILES is "true"#
		resetPeerCon();
		// #sijapp cond.end#
		// #sijapp cond.end#
        
        // #sijapp cond.if modules_TRAFFIC is "true" #
        try
        {
            Traffic.save();
        } catch (Exception e)
        { // Do nothing
        }
        // #sijapp cond.end#        
        
    }

    // Dels a ContactListContactItem to the server saved contact list
    static public synchronized void delFromContactList(ContactListContactItem cItem)
    {
        // Check whether contact item is temporary
        if (cItem.getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP))
        {
            // Remove this temporary contact item
			removeLocalContact(cItem.getStringValue(ContactListContactItem.CONTACTITEM_UIN));
            ContactList.removeContactItem(cItem);

            // Activate contact list
            ContactList.activate();

        }
        else
        {
            // Request contact item removal
            UpdateContactListAction act2 = new UpdateContactListAction(cItem, UpdateContactListAction.ACTION_DEL);
            try
            {
                Icq.requestAction(act2);
            } catch (JimmException e)
            {
                JimmException.handleException(e);
                if (e.isCritical()) return;
            }

            // Start timer
            SplashCanvas.addTimerTask("wait", act2, false);
        }
    }

    public synchronized void BeginTyping(String uin, boolean isTyping) throws JimmException
    {
		byte[] uinRaw = Util.stringToByteArray(uin);
		int tempBuffLen = Icq.MTN_PACKET_BEGIN.length + 1 + uinRaw.length + 2;
		int marker = 0;
		byte[] tempBuff = new byte[tempBuffLen];
		System.arraycopy(Icq.MTN_PACKET_BEGIN, 0, tempBuff, marker, Icq.MTN_PACKET_BEGIN.length);
		marker += Icq.MTN_PACKET_BEGIN.length;
		Util.putByte(tempBuff, marker, uinRaw.length);
		marker += 1;
		System.arraycopy(uinRaw, 0, tempBuff, marker, uinRaw.length);
		marker += uinRaw.length;
		Util.putWord(tempBuff, marker, ((isTyping) ? (0x0002) : (0x0000)));
		marker += 2;
		// Send packet
		SnacPacket snacPkt = new SnacPacket(0x0004, 0x0014, 0x00000000, new byte[0], tempBuff);
		this.c.sendPacket(snacPkt);
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
        connected = true;
    }
    
    // Returns and updates sequence nr
    static public int getFlapSequence()
    {
    	flapSEQ = ++flapSEQ%0x8000;
    	return flapSEQ;
    }

    // Resets the comm. subsystem
    static public synchronized void resetServerCon()
    {    	
    	// Stop thread
        thread = null;
        
        // Wake up thread in order to complete
		synchronized (Icq.wait) { Icq.wait.notify(); }

        // Reset all variables
        connected = false;
        
        // Reset all timer tasks
        Jimm.jimm.cancelTimer();
        
        // Delete all actions
        if (actAction != null) actAction.removeAllElements();
        if (reqAction != null) reqAction.removeAllElements();
       
    }
    
    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    // #sijapp cond.if modules_FILES is "true"#
    // Resets the comm. subsystem
    static public synchronized void resetPeerCon()
    {
    	// Close connection
    	peerC = null;
    }
    // #sijapp cond.end#
    // #sijapp cond.end#

    /** *********************************************************************** */
    /** *********************************************************************** */
    /** *********************************************************************** */
    
    // Wait object
    static private Object wait = new Object();

    // Connection to the ICQ server
    public static Connection c;

    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    // #sijapp cond.if modules_FILES is "true"#
    // Connection to peer
    static PeerConnection peerC;
    // #sijapp cond.end#
    // #sijapp cond.end#

    // All currently active actions
    static private Vector actAction;

    // Action listener
    static private ActionListener actListener;
    
    // Keep alive timer task
    static private TimerTasks keepAliveTimerTask;

    // Main loop
    public void run()
    {
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        // #sijapp cond.if modules_FILES is "true"#
        // Is a DC packet Available
        boolean dcPacketAvailable;
        // #sijapp cond.end#
        // #sijapp cond.end#

        // Get thread object
        Thread thread = Thread.currentThread();

        // Required variables
        Action newAction;

        // Instantiate connections
        if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_SOCKET)
        	c = new SOCKETConnection();
        else if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_HTTP)
        	c = new HTTPConnection();
        // #sijapp cond.if modules_PROXY is "true"#
        else if (Options.getInt(Options.OPTION_CONN_TYPE) == Options.CONN_TYPE_PROXY)
        	c = new SOCKSConnection();
        // #sijapp cond.end#

        // Instantiate active actions vector
        actAction = new Vector();

        // Instantiate action listener
        actListener = new ActionListener();
        
        keepAliveTimerTask = new TimerTasks(TimerTasks.ICQ_KEEPALIVE);
        long keepAliveInterv = Integer.parseInt(Options.getString(Options.OPTION_CONN_ALIVE_INVTERV))*1000;
        Jimm.jimm.getTimerRef().schedule(keepAliveTimerTask, keepAliveInterv, keepAliveInterv);

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
                        if ((actAction.size() == 1) && ((Action) actAction.elementAt(0)).isExclusive())
                        {
                            newAction = null;
                        }
                        else
                        {
                            newAction = (Action) reqAction.elementAt(0);
                            if (((actAction.size() > 0) && newAction.isExclusive()) || (!newAction.isExecutable()))
                            {
                                newAction = null;
                            }
                            else
                            {
                                reqAction.removeElementAt(0);
                            }
                        }
                    }
                    else
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
                else
                    if (newAction != null)
                    {
                        try
                        {
                            newAction.init();
                            actAction.addElement(newAction);
                        } catch (JimmException e)
                        {
                            JimmException.handleException(e);
                            if (e.isCritical()) throw (e);
                        }
                    }

                // Set dcPacketAvailable to true if the peerC is not null and
                // there is an packet waiting
                // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                // #sijapp cond.if modules_FILES is "true"#
                if (peerC != null)
                {
                    if (peerC.available() > 0)
                        dcPacketAvailable = true;
                    else
                        dcPacketAvailable = false;
                }
                else
                    dcPacketAvailable = false;
                // #sijapp cond.end#
                // #sijapp cond.end#

                // Read next packet, if available
                // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                // #sijapp cond.if modules_FILES is "true"#
                while ((c.available() > 0) || dcPacketAvailable)
                {
                    // Try to get packet
                    Packet packet = null;
                    try
                    {
                        if (c.available() > 0)
                            packet = c.getPacket();
                        else
                            if (dcPacketAvailable) packet = peerC.getPacket();
                    } catch (JimmException e)
                    {
                        JimmException.handleException(e);
                        if (e.isCritical()) throw (e);
                    }

                    // Forward received packet to all active actions and to the
                    // action listener
                    boolean consumed = false;
                    for (int i = 0; i < actAction.size(); i++)
                    {
                        try
                        {
                            if (((Action) actAction.elementAt(i)).forward(packet))
                            {
                                consumed = true;
                                break;
                            }
                        } catch (JimmException e)
                        {
                            JimmException.handleException(e);
                            if (e.isCritical()) throw (e);
                        }
                    }
                    if (!consumed)
                    {
                        try
                        {
                            actListener.forward(packet);
                        } catch (JimmException e)
                        {
                            JimmException.handleException(e);
                            if (e.isCritical()) throw (e);
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
                    }
                    else
                        dcPacketAvailable = false;
                }

                // #sijapp cond.else#

                while ((c.available() > 0))
                {
                    // Try to get packet
                    Packet packet = null;
                    try
                    {
                        if (c.available() > 0) packet = c.getPacket();
                    } catch (JimmException e)
                    {
                        JimmException.handleException(e);
                        if (e.isCritical()) throw (e);
                    }

                    // Forward received packet to all active actions and to the
                    // action listener
                    boolean consumed = false;
                    for (int i = 0; i < actAction.size(); i++)
                    {
                        try
                        {
                            if (((Action) actAction.elementAt(i)).forward(packet))
                            {
                                consumed = true;
                                break;
                            }
                        } catch (JimmException e)
                        {
                            JimmException.handleException(e);
                            if (e.isCritical()) throw (e);
                        }
                    }
                    if (!consumed)
                    {
                        try
                        {
                            actListener.forward(packet);
                        } catch (JimmException e)
                        {
                            JimmException.handleException(e);
                            if (e.isCritical()) throw (e);
                        }
                    }
                }
                // #sijapp cond.end#
                // #sijapp cond.else#
                while ((c.available() > 0))
                {
                    // Try to get packet
                    Packet packet = null;
                    try
                    {
                        if (c.available() > 0) packet = c.getPacket();
                    } catch (JimmException e)
                    {
                        JimmException.handleException(e);
                        if (e.isCritical()) throw (e);
                    }

                    // Forward received packet to all active actions and to the
                    // action listener
                    boolean consumed = false;
                    for (int i = 0; i < actAction.size(); i++)
                    {
                        try
                        {
                            if (((Action) actAction.elementAt(i)).forward(packet))
                            {
                                consumed = true;
                                break;
                            }
                        } catch (JimmException e)
                        {
                            JimmException.handleException(e);
                            if (e.isCritical()) throw (e);
                        }
                    }
                    if (!consumed)
                    {
                        try
                        {
                            actListener.forward(packet);
                        } catch (JimmException e)
                        {
                            JimmException.handleException(e);
                            if (e.isCritical()) throw (e);
                        }
                    }
                }
                // #sijapp cond.end#
                // Remove completed actions
                for (int i = 0; i < actAction.size(); i++)
                {
                    if (((Action) actAction.elementAt(i)).isCompleted() || ((Action) actAction.elementAt(i)).isError())
                    {
                        actAction.removeElementAt(i--);
                    }
                }

            }
        }
        // Critical JimmException
        catch (JimmException e)
        {
            // Do nothing, already handled
        }

        // Close connection
        c.close();
        resetServerCon();
    }
    
    /**************************************************************************/
    /**************************************************************************/
    /**************************************************************************/
    
    public abstract class Connection implements Runnable
    {
        // Disconnect flags
        protected volatile boolean inputCloseFlag;

        // Receiver thread
        protected volatile Thread rcvThread;

        // Received packets
        protected Vector rcvdPackets;
        
        // Opens a connection to the specified host and starts the receiver
        // thread
        public synchronized void connect(String hostAndPort) throws JimmException
        {

        }

        // Sets the reconnect flag and closes the connection
        public synchronized void close()
        {
        }

        // Returns the number of packets available
        public synchronized int available()
        {
            if (this.rcvdPackets == null)
            {
                return (0);
            }
            else
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
                if (this.rcvdPackets.size() == 0) { return (null); }
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
        
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		// #sijapp cond.if modules_FILES is "true"#

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

		// #sijapp cond.end#
		// #sijapp cond.end#

        // Main loop
        public void run()
        {

       

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
		public synchronized void connect(String hostAndPort) throws JimmException
		{
			try
			{
				connSeq++;
				// If this is the first connection initialize the connection with the proxy
				if (connSeq == 1)
				{
					this.inputCloseFlag = false;
					this.rcvThread = new Thread(this);
					this.rcvThread.start();
					// Wait the the finished init will notify us
					this.wait();
				}
				
				// Extract host and port from combined String (we need port as int value)
				String icqserver_host = hostAndPort.substring(0, hostAndPort.indexOf(":"));
				int icqserver_port = Integer.parseInt(hostAndPort.substring(hostAndPort.indexOf(":") + 1));
				// System.out.println("Connect via "+proxy_host+":"+proxy_port+" to: "+icqserver_host+" "+icqserver_port);
				// Send anser packet with connect to real server (via proxy)
				byte[] packet = new byte[icqserver_host.length() + 4];
				Util.putWord(packet, 0, icqserver_host.length());
				System.arraycopy(Util.stringToByteArray(icqserver_host), 0, packet, 2, icqserver_host.length());
				Util.putWord(packet, 2 + icqserver_host.length(), icqserver_port);

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

		// Sets the reconnect flag and closes the connection
		public synchronized void close()
		{
			this.inputCloseFlag = true;

			try
			{
				this.ism.close();
			} catch (Exception e)
			{ /* Do nothing */
			} finally
			{
				this.ism = null;
			}

			try
			{
				this.osd.close();
			} catch (Exception e)
			{ /* Do nothing */
			} finally
			{
				this.osd = null;
			}

			try
			{
				this.hcm.close();
				this.hcd.close();
			} catch (Exception e)
			{ /* Do nothing */
			} finally
			{
				this.hcm = null;
				this.hcd = null;
			}

			Thread.yield();
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
		public void sendPacket(Packet packet, byte[] rawData, int type, int connCount) throws JimmException
		{
			// Set the connection parameters
			try
			{
				this.hcd = (HttpConnection) Connector.open("http://" + proxy_host + ":" + proxy_port + "/data?sid=" + sid + "&seq=" + seq, Connector.READ_WRITE);
				this.hcd.setRequestProperty("User-Agent",Options.getString(Options.OPTION_HTTP_USER_AGENT));
				this.hcd.setRequestProperty("Cache-Control", "no-store no-cache");
				this.hcd.setRequestProperty("Pragma", "no-cache");
				this.hcd.setRequestMethod(HttpConnection.POST);
				this.osd = this.hcd.openOutputStream();
			} catch (IOException e)
			{
				this.close();
			}

			// Throw exception if output stream is not ready
			if (this.osd == null) { throw (new JimmException(128, 0, true)); }

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
						this.close();
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

					// #sijapp cond.if modules_TRAFFIC is "true" #

					// 40 is the overhead for each packet (TCP/IP)
					// 190 is the ca. overhead for the HTTP header
					// 14 bytes is the overhead for ICQ HTTP data header
					// 170 bytes is the ca. overhead of the HTTP/1.1 200 OK
					Traffic.addTraffic(outpack.length + 40 + 190 + 14 + 170);
					if (ContactList.getVisibleContactListRef().isShown())
					{
						RunnableImpl.updateContactListCaption();
					}
					// #sijapp cond.end#
					// System.out.println(" ");
				} catch (IOException e)
				{
					this.close();
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
			byte[]packet = new byte[0];
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
				while (!this.inputCloseFlag)
				{
					// Set connection parameters
					this.hcm = (HttpConnection) Connector.open(monitorURL, Connector.READ_WRITE);
					this.hcm.setRequestProperty("User-Agent",Options.getString(Options.OPTION_HTTP_USER_AGENT));
					this.hcm.setRequestProperty("Cache-Control", "no-store no-cache");
					this.hcm.setRequestProperty("Pragma", "no-cache");
					this.hcm.setRequestMethod(HttpConnection.GET);
					this.ism = this.hcm.openInputStream();
					if (hcm.getResponseCode() != HttpConnection.HTTP_OK) throw new IOException();
					// Read flap header
					bReadSumRequest = 0;

					do
					{
						bReadSum = 0;
						// Read HTTP packet length information
						do
						{
							bRead = ism.read(length, bReadSum, length.length - bReadSum);
							if (bRead == -1) break;
							bReadSum += bRead;
							bReadSumRequest += bRead;
						} while (bReadSum < length.length);
						if (bRead == -1) break;
						// Allocate memory for packet data
						httpPacket = new byte[Util.getWord(length, 0)];
						bReadSum = 0;

						// Read HTTP packet data
						do
						{
							bRead = ism.read(httpPacket, bReadSum, httpPacket.length - bReadSum);
							if (bRead == -1) break;
							bReadSum += bRead;
							bReadSumRequest += bRead;
						} while (bReadSum < httpPacket.length);
						if (bRead == -1) break;
						
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
									if (Util.getByte(httpPacket, contBytes) != 0x2A) { throw (new JimmException(124, 0)); }
									// Copy flap packet data from http packet
									packet = new byte[Util.getWord(httpPacket, contBytes + 4) + 6];
								}
								// Read packet data form httpPacket to packet
								// Packet contains the end of the flap packet
								if (httpPacket.length-contBytes >= (packet.length - flapMarker))
								{
									System.arraycopy(httpPacket, contBytes, packet, flapMarker, (packet.length - flapMarker));
									contBytes += (packet.length - flapMarker);
									flapMarker = packet.length;
								}
								// Packet does not contain the end of the flap packet
								else 
								{
									System.arraycopy(httpPacket, contBytes, packet, flapMarker, httpPacket.length - contBytes);
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
						}
						else
							if (Util.getWord(httpPacket, 2) == 0x0007)
							{
								// Construct and handle exception if we get a close rep for the connection we are
								// currently using
								if (Util.getWord(httpPacket, 10) == connSeq) throw new JimmException(221, 0);
							}
							else
								if (Util.getWord(httpPacket, 2) == 0x0002)
								{
									synchronized (this)
									{
										// Init answer from proxy set sid and proxy_host and proxy_port
										byte[] temp = new byte[16];
										System.arraycopy(httpPacket, 10, temp, 0, 16);
										sid = Util.byteArrayToHexString(temp);
										// Get IP of proxy
										byte[] ip = new byte[Util.getWord(httpPacket, 26)];
										System.arraycopy(httpPacket, 28, ip, 0, ip.length);
										this.proxy_host = Util.byteArrayToString(ip);

										// Get port for proxy
										this.proxy_port = Util.getWord(httpPacket, 28 + ip.length);

										// Set monitor URL to non init value
										monitorURL = "http://" + proxy_host + ":" + proxy_port + "/monitor?sid=" + sid;

										this.notify();
									}

								}
					} while (bReadSumRequest < hcm.getLength());
					
					// #sijapp cond.if modules_TRAFFIC is "true" #
					// This is not accurate for http connection
					// 42 is the overhead for each packet (2 byte packet length) (TCP IP)
					// 185 is the overhead for each monitor packet HTTP HEADER
					// 175 is the overhead for each HTTP/1.1 200 OK answer header
					// ICQ HTTP data header is counted in bReadSum
					Traffic.addTraffic(bReadSumRequest + 42 + 185 + 175);

					if ( ContactList.getVisibleContactListRef().isShown())
					{
						RunnableImpl.updateContactListCaption();
						Traffic.trafficScreen.update(false);
					}
					// #sijapp cond.end#
					
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
				if (!this.inputCloseFlag)
				{
					// Construct and handle exception
					JimmException f = new JimmException(125, 3);
					JimmException.handleException(f);
				}
				else
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
				if (!this.inputCloseFlag)
				{
					// Construct and handle exception
					JimmException f = new JimmException(125, 1);
					JimmException.handleException(f);
				}
				else
				{ /* Do nothing */
				}
			}

		}

	}

    /**************************************************************************/
    /**************************************************************************/
    /**************************************************************************/

    
    // SOCKETConnection
    public class SOCKETConnection extends Connection implements Runnable
    {
    	
        // Connection variables
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    	private SocketConnection sc;
        // #sijapp cond.else#
    	private StreamConnection sc;
        // #sijapp cond.end#
    	private InputStream is;
    	private OutputStream os;

        // FLAP sequence number counter
    	private int nextSequence;

        // ICQ sequence number counter
    	private int nextIcqSequence;
    	

        // Opens a connection to the specified host and starts the receiver thread
		public synchronized void connect(String hostAndPort) throws JimmException
		{
			try
			{
				// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
				sc = (SocketConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);
				// #sijapp cond.else#
				sc = (StreamConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);
				// #sijapp cond.end#
				is = sc.openInputStream();
				os = sc.openOutputStream();

				inputCloseFlag = false;
				rcvThread = new Thread(this);
				rcvThread.start();
				nextSequence = (new Random()).nextInt() % 0x0FFF;
				nextIcqSequence = 2;

			} catch (ConnectionNotFoundException e)
			{
				throw (new JimmException(121, 0));
			} catch (IllegalArgumentException e)
			{
				throw (new JimmException(122, 0));
			} catch (IOException e)
			{
				throw (new JimmException(120, 0));
			}
		}        

        // Sets the reconnect flag and closes the connection
        public synchronized void close()
        {
			inputCloseFlag = true;
			try
			{
				is.close();
			} catch (Exception e)
			{ /* Do nothing */
			} finally
			{
				is = null;
			}

			try
			{
				os.close();
			} catch (Exception e)
			{ /* Do nothing */
			} finally
			{
				os = null;
			}

			try
			{
				sc.close();
			} catch (Exception e)
			{ /* Do nothing */
			} finally
			{
				sc = null;
			}
			
			
			Thread.yield();
		}

        // Sends the specified packet
        public void sendPacket(Packet packet) throws JimmException
        {

            // Throw exception if output stream is not ready
            if (os == null) { throw (new JimmException(123, 0)); }

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
                    // #sijapp cond.if modules_TRAFFIC is "true" #
                    Traffic.addTraffic(outpack.length + 51); // 51 is the overhead for each packet
                    if (Traffic.trafficScreen.isActive() || ContactList.getVisibleContactListRef().isShown())
                    {
                    	RunnableImpl.updateContactListCaption();
                    }
                    // #sijapp cond.end#
                } catch (IOException e)
                {
                    close();
                }

            }

        }
        
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        // #sijapp cond.if modules_FILES is "true"#
        
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
        
        // #sijapp cond.end#
        // #sijapp cond.end#

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
                while (!inputCloseFlag)
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
                        bRead = is.read(flapHeader, bReadSum, flapHeader.length - bReadSum);
                        if (bRead == -1) break;
                        bReadSum += bRead;
                    } while (bReadSum < flapHeader.length);
                    if (bRead == -1) break;

                    // Verify flap header
                    if (Util.getByte(flapHeader, 0) != 0x2A) { throw (new JimmException(124, 0)); }

                    // Allocate memory for flap data
                    flapData = new byte[Util.getWord(flapHeader, 4)];

                    // Read flap data
                    bReadSum = 0;
                    do
                    {
                        bRead = is.read(flapData, bReadSum, flapData.length - bReadSum);
                        if (bRead == -1) break;
                        bReadSum += bRead;
                    } while (bReadSum < flapData.length);
                    if (bRead == -1) break;

                    // Merge flap header and data and count the data
                    rcvdPacket = new byte[flapHeader.length + flapData.length];
                    System.arraycopy(flapHeader, 0, rcvdPacket, 0, flapHeader.length);
                    System.arraycopy(flapData, 0, rcvdPacket, flapHeader.length, flapData.length);
                    // #sijapp cond.if modules_TRAFFIC is "true" #
                    Traffic.addTraffic(bReadSum + 57);
                    // 46 is the overhead for each packet (6 byte flap header)
                    if (ContactList.getVisibleContactListRef().isShown())
                    {
                    	RunnableImpl.updateContactListCaption();
						Traffic.trafficScreen.update(false);
                    }
                    // #sijapp cond.end#

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
                if (!inputCloseFlag)
                {
                    JimmException f = new JimmException(120, 3);
                    JimmException.handleException(f);
                }

                // Reset input close flag
                inputCloseFlag = false;

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
                if (!inputCloseFlag)
                {
                	JimmException f = new JimmException(120, 1);
                    JimmException.handleException(f);
                }
                // Reset input close flag
            }
        }

    }

    /**************************************************************************/
    /**************************************************************************/
    /**************************************************************************/

    // #sijapp cond.if modules_PROXY is "true"#
    
    // SOCKSConnection
    public class SOCKSConnection extends Connection implements Runnable
    {
    	
    	private final byte[] SOCKS4_CMD_CONNECT =
        { (byte) 0x04, (byte) 0x01, (byte) 0x14, (byte) 0x46, // Port 5190
          (byte) 0x40, (byte) 0x0C, (byte) 0xA1, (byte) 0xB9, 
          (byte) 0x00 // IP 64.12.161.185 (default login.icq.com)
        };

    	private final byte[] SOCKS5_HELLO =
        { (byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x02};

    	private final byte[] SOCKS5_CMD_CONNECT =
        { (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x03};
 

        // Connection variables
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    	private SocketConnection sc;
        // #sijapp cond.else#
    	private StreamConnection sc;
        // #sijapp cond.end#
    	private InputStream is;
    	private OutputStream os;

    	private boolean is_socks4 = false;
    	private boolean is_socks5 = false;
    	private boolean is_connected = false;

        // FLAP sequence number counter
    	private int nextSequence;

        // ICQ sequence number counter
    	private int nextIcqSequence;
    	
        // Tries to resolve given host IP
    	private synchronized String ResolveIP(String host, String port)
        {
            // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
            if (Util.isIP(host)) return host;
            SocketConnection c;
            
            try
            {
                c = (SocketConnection) Connector.open("socket://" + host + ":" + port, Connector.READ_WRITE);
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
            }
            catch (Exception e)
            {
                return "0.0.0.0";
            }
            
            // #sijapp cond.else#
            if (Util.isIP(host))
            { 
            	return host;
            }
            else
            {
            	return "0.0.0.0";
            }
            // #sijapp cond.end#
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
    	public synchronized void connect(String hostAndPort) throws JimmException
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
                        stream_close();
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

                inputCloseFlag = false;
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
        private synchronized void connect_socks4(String host, String port) throws JimmException
        {
            is_socks4 = false;
            String proxy_host = Options.getString(Options.OPTION_PRX_SERV);
            String proxy_port = Options.getString(Options.OPTION_PRX_PORT);
            int i = 0;
            byte[] buf;

            try
            {
                // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                sc = (SocketConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
                // #sijapp cond.else#
                sc = (StreamConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
                // #sijapp cond.end#
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
                    }
                    else
                    {
                        is_connected = false;
                        throw (new JimmException(118, 2));
                    }
                }
                // If we got responce packet bigger than mere proxy responce,
                // we might got destination server responce in tail of proxy
                // responce
                else
                    if (is.available() > 8)
                    {
                        is_connected = true;
                        is_socks4 = true;
                    }
                    else
                    {
                        throw (new JimmException(118, 2));
                    }
            } catch (ConnectionNotFoundException e)
            {
                throw (new JimmException(121, 0));
            } catch (IllegalArgumentException e)
            {
                throw (new JimmException(122, 0));
            } catch (IOException e)
            {
                throw (new JimmException(120, 0));
            }
        }

        // Attempts to connect through socks5
        private synchronized void connect_socks5(String host, String port) throws JimmException
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
                // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                sc = (SocketConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
                // #sijapp cond.else#
                sc = (StreamConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
                // #sijapp cond.end#
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

                if (is.available() == 0) { throw (new JimmException(118, 2)); }

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

                    if (is.available() == 0) { throw (new JimmException(118, 2)); }

                    // Read reply
                    buf = new byte[is.available()];
                    is.read(buf);

                    meth = Util.getByte(buf, 1);

                    if (meth == 0x00)
                    {
                        is_connected = true;
                        is_socks5 = true;
                    }
                    else
                    {
                        // Unknown error (bad login or pass)
                        throw (new JimmException(118, 3));
                    }
                }
                // Proxy without authorisation
                else
                    if (ver == 0x05 && meth == 0x00)
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
                throw (new JimmException(121, 0));
            } catch (IllegalArgumentException e)
            {
                throw (new JimmException(122, 0));
            } catch (IOException e)
            {
                throw (new JimmException(120, 0));
            }
        }

        // Sets the reconnect flag and closes the connection
        public synchronized void close()
        {
            inputCloseFlag = true;

            stream_close();

            Thread.yield();
        }

        // Close input and output streams
        private synchronized void stream_close()
        {
            try
            {
                is.close();
            } catch (Exception e)
            { /* Do nothing */
            } finally
            {
                is = null;
            }

            try
            {
                os.close();
            } catch (Exception e)
            { /* Do nothing */
            } finally
            {
                os = null;
            }

            try
            {
                sc.close();
            } catch (Exception e)
            { /* Do nothing */
            } finally
            {
                sc = null;
            }
        }

        // Sends the specified packet
        public void sendPacket(Packet packet) throws JimmException
        {

            // Throw exception if output stream is not ready
            if (os == null) { throw (new JimmException(123, 0)); }

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
                    // #sijapp cond.if modules_TRAFFIC is "true" #
                    Traffic.addTraffic(outpack.length + 51); // 51 is the overhead for each packet
                    if (Traffic.trafficScreen.isActive() || ContactList.getVisibleContactListRef().isShown())
                    {
                    	RunnableImpl.updateContactListCaption();
						Traffic.trafficScreen.update(false);
                    }
                    // #sijapp cond.end#
                } catch (IOException e)
                {
                    close();
                }

            }

        }
        
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        // #sijapp cond.if modules_FILES is "true"#
        
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
        
        // #sijapp cond.end#
        // #sijapp cond.end#

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
                while (!inputCloseFlag)
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
                        bRead = is.read(flapHeader, bReadSum, flapHeader.length - bReadSum);
                        if (bRead == -1) break;
                        bReadSum += bRead;
                    } while (bReadSum < flapHeader.length);
                    if (bRead == -1) break;
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
                            bRead = is.read(flapHeader, bReadSum, flapHeader.length - bReadSum);
                            if (bRead == -1) break;
                            bReadSum += bRead;
                        } while (bReadSum < flapHeader.length);
                    }
                    // Check for socks5
                    else
                        if (Util.getByte(flapHeader, 0) == 0x05 && is_socks5)
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
                            }
                            else
                                if (atyp == 0x03)
                                {
                                    int size = Util.getByte(flapHeader, 4);
                                    is.skip(size + 1);
                                }
                                else
                                {
                                    // Don't know what was that, but skip like
                                    // if it was an ip
                                    is.skip(4);
                                }

                            bReadSum = 0;
                            do
                            {
                                bRead = is.read(flapHeader, bReadSum, flapHeader.length - bReadSum);
                                if (bRead == -1) break;
                                bReadSum += bRead;
                            } while (bReadSum < flapHeader.length);
                        }

                    // Verify flap header
                    if (Util.getByte(flapHeader, 0) != 0x2A) { throw (new JimmException(124, 0)); }

                    // Allocate memory for flap data
                    flapData = new byte[Util.getWord(flapHeader, 4)];

                    // Read flap data
                    bReadSum = 0;
                    do
                    {
                        bRead = is.read(flapData, bReadSum, flapData.length - bReadSum);
                        if (bRead == -1) break;
                        bReadSum += bRead;
                    } while (bReadSum < flapData.length);
                    if (bRead == -1) break;

                    // Merge flap header and data and count the data
                    rcvdPacket = new byte[flapHeader.length + flapData.length];
                    System.arraycopy(flapHeader, 0, rcvdPacket, 0, flapHeader.length);
                    System.arraycopy(flapData, 0, rcvdPacket, flapHeader.length, flapData.length);
                    // #sijapp cond.if modules_TRAFFIC is "true" #
                    Traffic.addTraffic(bReadSum + 57);
                    // 46 is the overhead for each packet (6 byte flap header)
                    if (Traffic.trafficScreen.isActive() || ContactList.getVisibleContactListRef().isShown())
                    {
                    	RunnableImpl.updateContactListCaption();
						Traffic.trafficScreen.update(false);
                    }
                    // #sijapp cond.end#

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
                if (!inputCloseFlag)
                {
                    JimmException f = new JimmException(120, 3);
                    JimmException.handleException(f);
                }

                // Reset input close flag
                inputCloseFlag = false;

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
                if (!inputCloseFlag)
                {
                    JimmException f = new JimmException(120, 1);
                    JimmException.handleException(f);
                }

                // Reset input close flag
                inputCloseFlag = false;

            }
        }

    }

    // #sijapp cond.end #
    
    /**************************************************************************/
    /**************************************************************************/
    /**************************************************************************/

    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    // #sijapp cond.if modules_FILES is "true"#
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
        public synchronized void connect(String hostAndPort) throws JimmException
        {
            try
            {
            	//#sijapp cond.if modules_DEBUGLOG is "true" #
            	System.out.println("Peer conn: connecting to socket://" + hostAndPort);
            	//#sijapp cond.end #
            	
                this.sc = (SocketConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);
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
            }
            else
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
                if (this.rcvdPackets.size() == 0) { return (null); }
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
            if (this.os == null) { throw (new JimmException(128, 0, true, true)); }

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
                    // #sijapp cond.if modules_TRAFFIC is "true" #

                    // 51 is the overhead for each packet
                    Traffic.addTraffic(outpack.length + 51);
                    if (ContactList.getVisibleContactListRef().isShown())
                    {
                    	RunnableImpl.updateContactListCaption();
						Traffic.trafficScreen.update(false);
                    }
                    // #sijapp cond.end#
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
                        bRead = this.is.read(dcLength, bReadSum, dcLength.length - bReadSum);
                        if (bRead == -1) break;
                        bReadSum += bRead;
                    } while (bReadSum < dcLength.length);
                    if (bRead == -1) break;

                    // Allocate memory for flap data
                    rcvdPacket = new byte[Util.getWord(dcLength, 0, false)];

                    // Read flap data
                    bReadSum = 0;
                    do
                    {
                        bRead = this.is.read(rcvdPacket, bReadSum, rcvdPacket.length - bReadSum);
                        if (bRead == -1) break;
                        bReadSum += bRead;
                    } while (bReadSum < rcvdPacket.length);
                    if (bRead == -1) break;

                    // #sijapp cond.if modules_TRAFFIC is "true" #
                    Traffic.addTraffic(bReadSum + 53);

                    // 42 is the overhead for each packet (2 byte packet length)
                    if (ContactList.getVisibleContactListRef().isShown())
                    {
                    	RunnableImpl.updateContactListCaption();
						Traffic.trafficScreen.update(false);
                    }
                    // #sijapp cond.end#

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
                }
                else
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
                }
                else
                { /* Do nothing */
                }
            }

        }

    }
    // #sijapp cond.end#
    // #sijapp cond.end#
    
    public int getCurrentStatus()
    {
    	return isConnected() ? (int)Options.getLong(Options.OPTION_ONLINE_STATUS) : ContactList.STATUS_OFFLINE;
    }
}
