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
Author: Manuel Linsmayer / Andreas Rossbacher
*******************************************************************************/

package jimm.comm;

import jimm.Jimm;
import jimm.JimmException;
//import jimm.Options;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.StreamConnection;

public class Icq implements Runnable {

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
  public void requestAction(Action act) throws JimmException {

    // Set reference to this ICQ object for callbacks
    act.setIcq(this);

    // Look whether action is executable at the moment
    if (!act.isExecutable()) {
      throw (new JimmException(140, 0));
    }

    // Queue requested action
    synchronized (this) {
      this.reqAction.addElement(act);
    }

    // Connect?
    if (act instanceof ConnectAction) {

      // Save UIN
      this.uin = new String(((ConnectAction) act).getUin());

      // Create new thread and start
      this.thread = new Thread(this);
      this.thread.start();

    }

    // Notify main loop
    synchronized (this.wait) {
      this.wait.notify();
    }

  }

  // Checks whether the comm. subsystem is in STATE_NOT_CONNECTED
  public synchronized boolean isNotConnected() {
    if (this.state == Icq.STATE_NOT_CONNECTED) {
      return (true);
    } else {
      return (false);
    }
  }

  // Puts the comm. subsystem into STATE_NOT_CONNECTED
  protected synchronized void setNotConnected() {
    this.state = Icq.STATE_NOT_CONNECTED;
  }

  // Checks whether the comm. subsystem is in STATE_CONNECTED
  public synchronized boolean isConnected() {
    if (this.state == Icq.STATE_CONNECTED) {
      return (true);
    } else {
      return (false);
    }
  }

  // Puts the comm. subsystem into STATE_CONNECTED
  protected synchronized void setConnected() {
    this.state = Icq.STATE_CONNECTED;
  }

  // Resets the comm. subsystem
  public synchronized void reset() {

    // Stop thread
    this.thread = null;

    // Reset all variables
    this.state = Icq.STATE_NOT_CONNECTED;
    this.reqAction = new Vector();

  }

  // Returns the current UIN
  public synchronized String getUin() {
    return (new String(this.uin));
  }

  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/

  // Milliseconds to wait for new packets
  private static final int STANDBY = 250;

  // Wait object
  private Object wait = new Object();

  // Connection to the ICQ server
  Connection c;

  // All currently active actions
  private Vector actAction;

  // Action listener
  private ActionListener actListener;

  // Timer task responsible for keep the connection alive
  private KeepAliveTimerTask keepAliveTimerTask;

  // Main loop
  public void run() {

    // Get thread object
    Thread thread = Thread.currentThread();

    // Required variables
    Action newAction;

    // Instantiate connection
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
    try {

      // Abort only in error state
      while (this.thread == thread) {

        // Get next action
        synchronized (this) {
          if (this.reqAction.size() > 0) {
            if ((this.actAction.size() == 1) && ((Action) this.actAction.elementAt(0)).isExclusive()) {
              newAction = null;
            } else {
              newAction = (Action) this.reqAction.elementAt(0);
              if (((this.actAction.size() > 0) && newAction.isExclusive()) || (!newAction.isExecutable())) {
                newAction = null;
              } else {
                this.reqAction.removeElementAt(0);
              }
            }
          } else {
            newAction = null;
          }
        }

        // Wait if a new action does not exist
        if ((newAction == null) && (this.c.available() == 0)) {
          try {
            synchronized (this.wait) {
              this.wait.wait(Icq.STANDBY);
            }
          } catch (InterruptedException e) {
            // Do nothing
          }
        }
        // Initialize action
        else if (newAction != null) {
          try {
            newAction.init();
            this.actAction.addElement(newAction);
          } catch (JimmException e) {
            JimmException.handleException(e);
            if (e.isCritical())
              throw (e);
          }
        }

        // Read next packet, if available
        while (this.c.available() > 0) {

          // Try to get packet
          Packet packet = null;
          try {
            packet = this.c.getPacket();
          } catch (JimmException e) {
            JimmException.handleException(e);
            if (e.isCritical())
              throw (e);
          }

          // Forward received packet to all active actions and to the action listener
          boolean consumed = false;
          for (int i = 0; i < this.actAction.size(); i++) {
            try {
              if (((Action) this.actAction.elementAt(i)).forward(packet)) {
                consumed = true;
                break;
              }
            } catch (JimmException e) {
              JimmException.handleException(e);
              if (e.isCritical())
                throw (e);
            }
          }
          if (!consumed) {
            try {
              this.actListener.forward(packet);
            } catch (JimmException e) {
              JimmException.handleException(e);
              if (e.isCritical())
                throw (e);
            }
          }

        }

        // Remove completed actions
        for (int i = 0; i < this.actAction.size(); i++) {
          if (((Action) this.actAction.elementAt(i)).isCompleted() || ((Action) this.actAction.elementAt(i)).isError()) {
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
    // System.out.println("1");
    this.c.close();

    // Cancel KeepAliveTimerTask
    this.keepAliveTimerTask.cancel();

  }

  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/

  // Connection
  public class Connection implements Runnable {

    // Connection variables
    //private StreamConnection sci;
    private StreamConnection sc;
    private InputStream is;
    private OutputStream os;

    // Disconnect flags
    private volatile boolean inputCloseFlag;
    //private volatile boolean outputCloseFlag;

    // Receiver thread
    private volatile Thread rcvThread;

    // Received packets
    private Vector rcvdPackets;

    // FLAP sequence number counter
    private int nextSequence;

    // ICQ sequence number counter
    private int nextIcqSequence;

    // Opens a connection to the specified host and starts the receiver thread
    public synchronized void connect(String hostAndPort) throws JimmException {
      try {
        this.sc = (StreamConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);
        this.is = this.sc.openInputStream();
        this.os = this.sc.openOutputStream();
		this.inputCloseFlag = false;
        this.rcvThread = new Thread(this);
        this.rcvThread.start();
        this.nextSequence = (new Random()).nextInt() % 0x0FFF;
        this.nextIcqSequence = 2;
      } catch (ConnectionNotFoundException e) {
        //e.printStackTrace();
        throw (new JimmException(121, 0));
      } catch (IllegalArgumentException e) {
        //e.printStackTrace();
        throw (new JimmException(122, 0));
      } catch (IOException e) {
        e.printStackTrace();
        throw (new JimmException(120, 0));
      }
    }

    // Opens a connection to the specified host and starts the receiver thread
    public synchronized void connect(String host, String port) throws JimmException {
      this.connect(host + ":" + port);
    }

    // Sets the reconnect flag and closes the connection
    public synchronized void close() {
      this.inputCloseFlag = true;
      //this.outputCloseFlag = true;

      try {
        this.is.close();
      } catch (Exception e) { /* Do nothing */
      } finally {
        this.is = null;
      }

      try {
        this.os.close();
      } catch (Exception e) { /* Do nothing */
      } finally {
        this.os = null;
      }

      try {
        this.sc.close();
      } catch (Exception e) { /* Do nothing */
      } finally {
        this.sc = null;
      }
	  Thread.yield();
    }

    // Returns the number of packets available
    public synchronized int available() {
      if (this.rcvdPackets == null) {
        return (0);
      } else {
        return (this.rcvdPackets.size());
      }
    }

    // Returns the next packet, or null if no packet is available
    public Packet getPacket() throws JimmException {

      // Request lock on packet buffer and get next packet, if available
      byte[] packet;
      synchronized (this.rcvdPackets) {
        if (this.rcvdPackets.size() == 0) {
          return (null);
        }
        packet = (byte[]) this.rcvdPackets.elementAt(0);
        this.rcvdPackets.removeElementAt(0);
      }

      // Parse and return packet
      return (Packet.parse(packet));

    }

    // Sends the specified packet
    public void sendPacket(Packet packet) throws JimmException {

      // Throw exception if output stream is not ready
      if (this.os == null) {
        throw (new JimmException(123, 0));
      }

      // Request lock on output stream
      synchronized (this.os) {

        // Set sequence numbers
        packet.setSequence(this.nextSequence++);
        if (packet instanceof ToIcqSrvPacket) {
          ((ToIcqSrvPacket) packet).setIcqSequence(this.nextIcqSequence++);
        }
        // Send packet and count the bytes
        try {
          byte[] outpack = packet.toByteArray();
		  // #sijapp cond.if mod_TRAF is "true" #
          Jimm.jimm.getTrafficRef().addTraffic(outpack.length + 40); // 40 is the overhead for each packet
          if (Jimm.jimm.getTrafficRef().isActive() || Jimm.jimm.getContactListRef().getVisibleContactListRef().isShown()) {
            Jimm.jimm.getTrafficRef().trafficScreen.update(false);
          }
         // #sijapp cond.end#
          this.os.write(outpack);
          this.os.flush();
        } catch (IOException e) {
          this.close();
          //this.outputCloseFlag = false;
        }

      }

    }

    // Main loop
    public void run() {

      // Required variables
      byte[] flapHeader = new byte[6];
      byte[] flapData;
      byte[] rcvdPacket;
      int bRead, bReadSum;

      // Reset packet buffer
      synchronized (this) {
        this.rcvdPackets = new Vector();
      }

      // Try
      try {

        // Check abort condition
        while (!this.inputCloseFlag) {
          // Read flap header
          bReadSum = 0;
          bRead = 0;

        if (Jimm.jimm.getOptionsRef().getConnType() == 1)
          while (is.available() == 0) {
            Thread.sleep(250);
          }

          do {
            bRead = this.is.read(flapHeader, bReadSum, flapHeader.length - bReadSum);
            if (bRead == -1)
              break;
            bReadSum += bRead;

          } while (bReadSum < flapHeader.length);

          if (bRead == -1)
            break;

          // Verify flap header
          if (Util.getByte(flapHeader, 0) != 0x2A) {
            throw (new JimmException(124, 0));
          }

          // Allocate memory for flap data
          flapData = new byte[Util.getWord(flapHeader, 4)];

          // Read flap data
          bReadSum = 0;

          do {
            bRead = this.is.read(flapData, bReadSum, flapData.length - bReadSum);
            if (bRead == -1)
              break;
            bReadSum += bRead;
          } while (bReadSum < flapData.length);
          if (bRead == -1)
            break;

          // Merge flap header and data and count the data
          rcvdPacket = new byte[flapHeader.length + flapData.length];

          System.arraycopy(flapHeader, 0, rcvdPacket, 0, flapHeader.length);
          System.arraycopy(flapData, 0, rcvdPacket, flapHeader.length, flapData.length);
//		  #sijapp cond.if mod_TRAF is "true" #
          Jimm.jimm.getTrafficRef().addTraffic(bReadSum + 46);
          // 46 is the overhead for each packet (6 byte flap header)
          if (Jimm.jimm.getTrafficRef().isActive() || Jimm.jimm.getContactListRef().getVisibleContactListRef().isShown()) {
            Jimm.jimm.getTrafficRef().trafficScreen.update(false);
          }
//		  #sijapp cond.end#
          // Lock object and add rcvd packet to vector
          synchronized (this.rcvdPackets) {
            this.rcvdPackets.addElement(rcvdPacket);
          }

          // Notify main loop
          synchronized (Icq.this.wait) {
            Icq.this.wait.notify();
          }
        }

      }
      // Catch communication exception

      catch (NullPointerException e) {
        // Construct and handle exception (only if input close flag has not been set)
        if (!this.inputCloseFlag) {
          JimmException f = new JimmException(120, 3);
          JimmException.handleException(f);
        }

        // Reset input close flag
        this.inputCloseFlag = false;
      }
      //Catch Interr exception
      catch (InterruptedException e) {
			// Do nothing
        }



      catch (JimmException e) {

        // Handle exception
        JimmException.handleException(e);

      }

      // Catch IO exception
      catch (IOException e) {

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

}
