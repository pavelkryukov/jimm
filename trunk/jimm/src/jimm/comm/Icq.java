/*******************************************************************************
Jimm - J2ME ICQ client
Copyright (C) 2003  Manuel Linsmayer

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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
// #sijapp cond.if target is "MIDP2"#
import javax.microedition.io.SocketConnection;
// #sijapp cond.else#
import javax.microedition.io.StreamConnection;
// #sijapp cond.end#

import jimm.ContactListContactItem;
import jimm.Jimm;
import jimm.JimmException;
import jimm.Options;
import jimm.SplashCanvas;
import jimm.util.ResourceBundle;


public class Icq implements Runnable
{


	// State constants
	private static final int STATE_NOT_CONNECTED = 0;
	private static final int STATE_CONNECTED = 1;


	// Current state
	private int state = Icq.STATE_NOT_CONNECTED;


	// UIN
	private String uin;


	// Requested actions
	private Vector reqAction = new Vector();


	// Thread
	volatile Thread thread;


	// Request an action
	public void requestAction(Action act) throws JimmException
	{

		// Set reference to this ICQ object for callbacks
		act.setIcq(this);

		// Look whether action is executable at the moment
		if (!act.isExecutable())
		{
			throw (new JimmException(140, 0));
		}

		// Queue requested action
		synchronized (this)
		{
			this.reqAction.addElement(act);
		}

		// Connect?
		if (act instanceof ConnectAction)
		{

			// Save UIN
			this.uin = new String(((ConnectAction) act).getUin());

			// Create new thread and start
			this.thread = new Thread(this);
			this.thread.start();

		}

		// Notify main loop
		synchronized (this.wait)
		{
			this.wait.notify();
		}

	}

	// Adds a ContactListContactItem to the server saved contact list
	public synchronized void addToContactList(ContactListContactItem cItem)
	{        
        // Display splash canvas
        SplashCanvas wait2 = Jimm.jimm.getSplashCanvasRef();
        wait2.setMessage(ResourceBundle.getString("wait"));
        wait2.setProgress(0);
        Jimm.display.setCurrent(wait2);

        // Request contact item removal
        UpdateContactListAction act = new UpdateContactListAction(cItem, true);
        try
        {
            this.requestAction(act);
        } catch (JimmException e)
        {
            JimmException.handleException(e);
            if (e.isCritical()) return;
        }

        // Start timer
        Jimm.jimm.getTimerRef().schedule(new SplashCanvas.UpdateContactListTimerTask(act), 1000, 1000);
		// System.out.println("start addContact");
	}
	
	// Connects to the ICQ network
	public synchronized void connect()
	{
        // Display splash canvas
        SplashCanvas wait = Jimm.jimm.getSplashCanvasRef();
        wait.setMessage(ResourceBundle.getString("connecting"));
        wait.setProgress(0);
        Jimm.display.setCurrent(wait);

        Options options = Jimm.jimm.getOptionsRef();
        // Connect
        ConnectAction act = new ConnectAction(options.getStringOption(Options.OPTION_UIN), options.getStringOption(Options.OPTION_PASSWORD),
                options.getStringOption(Options.OPTION_SRV_HOST), options.getStringOption(Options.OPTION_SRV_PORT));
        try
        {
            this.requestAction(act);

        } catch (JimmException e)
        {
            JimmException.handleException(e);
            if (e.isCritical()) return;
        }

        // Start timer
        Jimm.jimm.getTimerRef().schedule(new SplashCanvas.ConnectTimerTask(act), 1000, 1000);
	}
	
	// Disconnects from the ICQ network
	public synchronized void disconnect()
	{
		if (state != STATE_CONNECTED) return;
		
        // Display splash canvas
        SplashCanvas wait = Jimm.jimm.getSplashCanvasRef();
        wait.setMessage(ResourceBundle.getString("disconnecting"));
        wait.setProgress(0);
        Jimm.display.setCurrent(wait);

        // Disconnect
        DisconnectAction act = new DisconnectAction();
        try
        {
            this.requestAction(act);
        } catch (JimmException e)
        {
            JimmException.handleException(e);
            if (e.isCritical()) return;
        }

        // Start timer
        Jimm.jimm.getTimerRef().schedule(new SplashCanvas.DisconnectTimerTask(act, false), 1000, 1000);
        // #sijapp cond.if modules_TRAFFIC is "true" #
        try
        {
            Jimm.jimm.getTrafficRef().save();
        } catch (Exception e)
        { // Do nothing
        }
        // #sijapp cond.end#
	}
	
	// Dels a ContactListContactItem to the server saved contact list
	public synchronized void delFromContactList(ContactListContactItem cItem)
	{
        // Check whether contact item is temporary
        if (cItem.returnBoolValue(ContactListContactItem.VALUE_IS_TEMP))
        {
            // Remove this temporary contact item
            Jimm.jimm.getContactListRef().removeContactItem(cItem);

            // Activate contact list
            Jimm.jimm.getContactListRef().activate();

        } else
        {

            // Display splash canvas
            SplashCanvas wait2 = Jimm.jimm.getSplashCanvasRef();
            wait2.setMessage(ResourceBundle.getString("wait"));
            wait2.setProgress(0);
            Jimm.display.setCurrent(wait2);

            // Request contact item removal
            UpdateContactListAction act2 = new UpdateContactListAction(cItem, false);
            try
            {
                Jimm.jimm.getIcqRef().requestAction(act2);
            } catch (JimmException e)
            {
                JimmException.handleException(e);
                if (e.isCritical()) return;
            }

            // Start timer
            Jimm.jimm.getTimerRef().schedule(new SplashCanvas.UpdateContactListTimerTask(act2), 1000, 1000);

        }
	}
	
	// Checks whether the comm. subsystem is in STATE_NOT_CONNECTED
	public synchronized boolean isNotConnected()
	{
		return (this.state == Icq.STATE_NOT_CONNECTED);
	}


	// Puts the comm. subsystem into STATE_NOT_CONNECTED
	protected synchronized void setNotConnected()
	{
		this.state = Icq.STATE_NOT_CONNECTED;
	}


	// Checks whether the comm. subsystem is in STATE_CONNECTED
	public synchronized boolean isConnected()
	{
		return (this.state == Icq.STATE_CONNECTED);
	}


	// Puts the comm. subsystem into STATE_CONNECTED
	protected synchronized void setConnected()
	{
		this.state = Icq.STATE_CONNECTED;
	}


	// Resets the comm. subsystem
	public synchronized void resetServerCon()
	{

		// Stop thread
		this.thread = null;

		// Reset all variables
		this.state = Icq.STATE_NOT_CONNECTED;
		this.reqAction = new Vector();

	}
	
	// #sijapp cond.if target is "MIDP2"#
	
	// Resets the comm. subsystem
	public synchronized void resetPeerCon()
	{
	    this.peerC = null;
	}
	
	// #sijapp cond.end#
	
	


	// Returns the current UIN
	public synchronized String getUin()
	{
		return (new String(this.uin));
	}
	
	
	/**************************************************************************/
	/**************************************************************************/
	/**************************************************************************/


	// Milliseconds to wait for new packets
	private static final int STANDBY = 250;


	// Wait object
	private Object wait = new Object();


	// Connection to the ICQ server
	Connection c;
	
	// #sijapp cond.if target is "MIDP2"#
	// Connection to peer
	PeerConnection peerC;
	// #sijapp cond.end#

	// All currently active actions
	private Vector actAction;


	// Action listener
	private ActionListener actListener;


	// Timer task responsible for keep the connection alive
	private KeepAliveTimerTask keepAliveTimerTask;


	// Main loop
	public void run()
	{
	    // #sijapp cond.if target is "MIDP2"#
	    // Is a DC packet Available
	    boolean dcPacketAvailable;
	    // #sijapp cond.end#
	    
		// Get thread object
		Thread thread = Thread.currentThread();

		// Required variables
		Action newAction;

		// Instantiate connections
		this.c = new Connection();
		

		// Instantiate active actions vector
		this.actAction = new Vector();

		// Instantiate action listener
		this.actListener = new ActionListener();
		this.actListener.setIcq(this);

		// Instantiate KeepAliveTimerTask
		this.keepAliveTimerTask = new KeepAliveTimerTask();
		this.keepAliveTimerTask.setIcq(this);
		Jimm.jimm.getTimerRef().schedule(this.keepAliveTimerTask, KeepAliveTimerTask.ALIVE_INTERVAL, KeepAliveTimerTask.ALIVE_INTERVAL);

		// Catch JimmExceptions
		try
		{

			// Abort only in error state
			while (this.thread == thread)
			{

				// Get next action
				synchronized (this)
				{
					if (this.reqAction.size() > 0)
					{
						if ((this.actAction.size() == 1) && ((Action) this.actAction.elementAt(0)).isExclusive())
						{
							newAction = null;
						}
						else
						{
							newAction = (Action) this.reqAction.elementAt(0);
							if (((this.actAction.size() > 0) && newAction.isExclusive()) || (!newAction.isExecutable()))
							{
								newAction = null;
							}
							else
							{
								this.reqAction.removeElementAt(0);
							}
						}
					}
					else
					{
						newAction = null;
					}
				}

				// Wait if a new action does not exist
				if ((newAction == null) && (this.c.available() == 0))
				{
					try
					{
						synchronized (this.wait)
						{
							this.wait.wait(Icq.STANDBY);
						}
					}
					catch (InterruptedException e)
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
						this.actAction.addElement(newAction);
					}
					catch (JimmException e)
					{
						JimmException.handleException(e);
						if (e.isCritical()) throw (e);
					}
				}
				

				// Set dcPacketAvailable to true if the peerC is not null and there is an packet waiting
				// #sijapp cond.if target is "MIDP2"#
				if(this.peerC != null)
				{
				    if (this.peerC.available() > 0)
				        dcPacketAvailable = true;
				    else
				        dcPacketAvailable = false;
				}
				else
				    dcPacketAvailable = false;
				// #sijapp cond.end#

				// Read next packet, if available
				
				// #sijapp cond.if target is "MIDP2"#
				while ((this.c.available() > 0) || dcPacketAvailable)
				{
					// Try to get packet
					Packet packet = null;
					try
					{
					    if (this.c.available() > 0)
					        packet = this.c.getPacket();
					    else if (dcPacketAvailable)
					        packet = this.peerC.getPacket();
					}
					catch (JimmException e)
					{
						JimmException.handleException(e);
						if (e.isCritical()) throw (e);
					}

					// Forward received packet to all active actions and to the action listener
					boolean consumed = false;
					for (int i = 0; i < this.actAction.size(); i++)
					{
						try
						{
							if (((Action) this.actAction.elementAt(i)).forward(packet))
							{
								consumed = true;
								break;
							}
						}
						catch (JimmException e)
						{
							JimmException.handleException(e);
							if (e.isCritical()) throw (e);
						}
					}
					if (!consumed)
					{
						try
						{
							this.actListener.forward(packet);
						}
						catch (JimmException e)
						{
							JimmException.handleException(e);
							if (e.isCritical()) throw (e);
						}
					}

					// Set dcPacketAvailable to true if the peerC is not null and there is an packet waiting
					if(this.peerC != null)
					{
					    if (this.peerC.available() > 0)
					        dcPacketAvailable = true;
					    else
					        dcPacketAvailable = false;
					}
					else
					    dcPacketAvailable = false;
				}


		// #sijapp cond.else#

				while ((this.c.available() > 0))
				{
					// Try to get packet
					Packet packet = null;
					try
					{
					    if (this.c.available() > 0)
					        packet = this.c.getPacket();
					}
					catch (JimmException e)
					{
						JimmException.handleException(e);
						if (e.isCritical()) throw (e);
					}

					// Forward received packet to all active actions and to the action listener
					boolean consumed = false;
					for (int i = 0; i < this.actAction.size(); i++)
					{
						try
						{
							if (((Action) this.actAction.elementAt(i)).forward(packet))
							{
								consumed = true;
								break;
							}
						}
						catch (JimmException e)
						{
							JimmException.handleException(e);
							if (e.isCritical()) throw (e);
						}
					}
					if (!consumed)
					{
						try
						{
							this.actListener.forward(packet);
						}
						catch (JimmException e)
						{
							JimmException.handleException(e);
							if (e.isCritical()) throw (e);
						}
					}
				}

        // #sijapp cond.end#
				// Remove completed actions
				for (int i = 0; i < this.actAction.size(); i++)
				{
					if (((Action) this.actAction.elementAt(i)).isCompleted() || ((Action) this.actAction.elementAt(i)).isError())
					{
						this.actAction.removeElementAt(i--);
					}
				}

			}
		}
		// Critical JimmException
		catch (JimmException e) {
			// Do nothing, already handled
		}

		// Close connection
		this.c.close();

		// Cancel KeepAliveTimerTask
		this.keepAliveTimerTask.cancel();

	}


	/**************************************************************************/
	/**************************************************************************/
	/**************************************************************************/


	// Connection
	public class Connection implements Runnable
	{


		// Connection variables
		// #sijapp cond.if target is "MIDP2"#
		private SocketConnection sc;
		// #sijapp cond.else#
		private StreamConnection sc;
		// #sijapp cond.end#
		private InputStream is;
		private OutputStream os;


		// Disconnect flags
		private volatile boolean inputCloseFlag;


		// Receiver thread
		private volatile Thread rcvThread;


		// Received packets
		private Vector rcvdPackets;


		// FLAP sequence number counter
		private int nextSequence;


		// ICQ sequence number counter
		private int nextIcqSequence;


		// Opens a connection to the specified host and starts the receiver thread
		public synchronized void connect(String hostAndPort) throws JimmException
		{
			try
			{
			    // #sijapp cond.if target is "MIDP2"#
			    this.sc = (SocketConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);
			    // #sijapp cond.else#
			    this.sc = (StreamConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);
			    // #sijapp cond.end#
				this.is = this.sc.openInputStream();
				this.os = this.sc.openOutputStream();
				this.inputCloseFlag = false;
				this.rcvThread = new Thread(this);
				this.rcvThread.start();
				this.nextSequence = (new Random()).nextInt() % 0x0FFF;
				this.nextIcqSequence = 2;			
			}
			catch (ConnectionNotFoundException e)
			{
				throw (new JimmException(121, 0));
			}
			catch (IllegalArgumentException e)
			{
				throw (new JimmException(122, 0));
			}
			catch (IOException e)
			{
				throw (new JimmException(120, 0));
			}
		}


		// Sets the reconnect flag and closes the connection
		public synchronized void close() {
			this.inputCloseFlag = true;
			try { this.is.close(); }
			catch (Exception e) { /* Do nothing */ }
			finally { this.is = null; }
			try { this.os.close(); }
			catch (Exception e) { /* Do nothing */ }
			finally { this.os = null; }
			try { this.sc.close(); }
			catch (Exception e) { /* Do nothing */ }
			finally { this.sc = null; }
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
				throw (new JimmException(123, 0));
			}

			// Request lock on output stream
			synchronized (this.os)
			{

				// Set sequence numbers
				packet.setSequence(this.nextSequence++);
				if (packet instanceof ToIcqSrvPacket)
				{
					((ToIcqSrvPacket) packet).setIcqSequence(this.nextIcqSequence++);
				}

				// Send packet and count the bytes
				try
				{
					byte[] outpack = packet.toByteArray();
					this.os.write(outpack);
					this.os.flush();
					// #sijapp cond.if modules_TRAFFIC is "true" #
					Jimm.jimm.getTrafficRef().addTraffic(outpack.length + 40); // 40 is the overhead for each packet
					if (Jimm.jimm.getTrafficRef().isActive() || Jimm.jimm.getContactListRef().getVisibleContactListRef().isShown())
					{
						Jimm.jimm.getTrafficRef().trafficScreen.update(false);
					}
					// #sijapp cond.end#
				}
				catch (IOException e)
				{
					this.close();
				}

			}

		}
		
		// #sijapp cond.if target is "MIDP2"#
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
					if (Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_CONN_TYPE) == 1)
	 				{
						while (is.available() == 0) Thread.sleep(250);
					}
					do
					{
						bRead = this.is.read(flapHeader, bReadSum, flapHeader.length - bReadSum);
						if (bRead == -1) break;
						bReadSum += bRead;
					}
					while (bReadSum < flapHeader.length);
					if (bRead == -1) break;

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
						bRead = this.is.read(flapData, bReadSum, flapData.length - bReadSum);
						if (bRead == -1) break;
						bReadSum += bRead;
					}
					while (bReadSum < flapData.length);
					if (bRead == -1) break;

					// Merge flap header and data and count the data
					rcvdPacket = new byte[flapHeader.length + flapData.length];
					System.arraycopy(flapHeader, 0, rcvdPacket, 0, flapHeader.length);
					System.arraycopy(flapData, 0, rcvdPacket, flapHeader.length, flapData.length);
					// #sijapp cond.if modules_TRAFFIC is "true" #
					Jimm.jimm.getTrafficRef().addTraffic(bReadSum + 46);
					// 46 is the overhead for each packet (6 byte flap header)
					if (Jimm.jimm.getTrafficRef().isActive() || Jimm.jimm.getContactListRef().getVisibleContactListRef().isShown())
					{
						Jimm.jimm.getTrafficRef().trafficScreen.update(false);
					}
					// #sijapp cond.end#

					// Lock object and add rcvd packet to vector
					synchronized (this.rcvdPackets)
					{
						this.rcvdPackets.addElement(rcvdPacket);
					}

					// Notify main loop
					synchronized (Icq.this.wait)
					{
						Icq.this.wait.notify();
					}
				}

			}
			// Catch communication exception
			catch (NullPointerException e)
			{

				// Construct and handle exception (only if input close flag has not been set)
				if (!this.inputCloseFlag)
				{
					JimmException f = new JimmException(120, 3);
					JimmException.handleException(f);
				}

				// Reset input close flag
				this.inputCloseFlag = false;

			}
			// Catch InterruptedException
			catch (InterruptedException e) { /* Do nothing */ }
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
				if (!this.inputCloseFlag) {
					JimmException f = new JimmException(120, 1);
					JimmException.handleException(f);
				}

				// Reset input close flag
				this.inputCloseFlag = false;

			}
		}

	}



	/**************************************************************************/
	/**************************************************************************/
	/**************************************************************************/

// #sijapp cond.if target is "MIDP2"#

	// PeerConnection
    public class PeerConnection implements Runnable
    {

        // Connection variables
        private SocketConnection sc;
        private InputStream is;
        private OutputStream os;

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
                // System.out.println("connect " + hostAndPort);
                this.sc = (SocketConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);        
                // System.out.println("connected");
                
                this.is = this.sc.openInputStream();
                this.os = this.sc.openOutputStream();
                this.rcvThread = new Thread(this);
                // System.out.println("thread done");
                this.rcvThread.start();
                // System.out.println("thread started");

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
                    
                    // 40 is the overhead for each packet
                    Jimm.jimm.getTrafficRef().addTraffic(outpack.length + 40); 
                    if (Jimm.jimm.getTrafficRef().isActive() || Jimm.jimm.getContactListRef().getVisibleContactListRef().isShown())
                    {
                        Jimm.jimm.getTrafficRef().trafficScreen.update(false);
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
                while (true)
                {

                    // Read flap header
                    bReadSum = 0;
                    if (Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_CONN_TYPE) == 1)
                    {
                        while (is.available() == 0)
                            Thread.sleep(250);
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
                    Jimm.jimm.getTrafficRef().addTraffic(bReadSum + 42);
                    
                    // 42 is the overhead for each packet (2 byte packet length)
                    if (Jimm.jimm.getTrafficRef().isActive() || Jimm.jimm.getContactListRef().getVisibleContactListRef().isShown())
                    {
                        Jimm.jimm.getTrafficRef().trafficScreen.update(false);
                    }
                    // #sijapp cond.end#

                    // Lock object and add rcvd packet to vector
                    synchronized (this.rcvdPackets)
                    {
                        this.rcvdPackets.addElement(rcvdPacket);
                    }

                    // Notify main loop
                    synchronized (Icq.this.wait)
                    {
                        Icq.this.wait.notify();
                    }
                }

            }
            // Catch communication exception
            catch (NullPointerException e)
            {

                // Construct and handle exception

                JimmException f = new JimmException(125, 3, true, true);
                JimmException.handleException(f);
                
            }
            // Catch InterruptedException
            catch (InterruptedException e)
            { /* Do nothing */
            }
            // Catch IO exception
            catch (IOException e)
            {
                // Construct and handle exception 
                JimmException f = new JimmException(125, 1, true, true);
                JimmException.handleException(f);

            }

        }

    }
	// #sijapp cond.end#

}
