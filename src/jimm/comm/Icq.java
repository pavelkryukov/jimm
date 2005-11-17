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
import java.util.Random;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
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
        if (!act.isExecutable()) { throw (new JimmException(140, 0)); }

        // Queue requested action
        synchronized (this)
        {
            this.reqAction.addElement(act);
        }

        // Connect?
        if (act instanceof ConnectAction)
        {

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

        // Request contact item adding
        UpdateContactListAction act = new UpdateContactListAction(cItem, UpdateContactListAction.ACTION_ADD);
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
        // #sijapp cond.if target isnot "MOTOROLA"#
        if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_SHADOW_CON))
        {
            // Make the shadow connection for Nokia 6230 of other devices if
            // needed
            ContentConnection ctemp = null;
            DataInputStream istemp = null;
            try
            {
                String url = "http://www.jimm.org/en/6230.html";
                ctemp = (ContentConnection) Connector.open(url);

                istemp = ctemp.openDataInputStream();
            } catch (Exception e)
            {
                // Do nothing
            }
        }
        // #sijapp cond.end#
        Options options = Jimm.jimm.getOptionsRef();
        // Connect
        ConnectAction act = new ConnectAction(options.getStringOption(Options.OPTION_UIN), options.getStringOption(Options.OPTION_PASSWORD), options.getStringOption(Options.OPTION_SRV_HOST), options.getStringOption(Options.OPTION_SRV_PORT));
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

        }
        else
        {

            // Display splash canvas
            SplashCanvas wait2 = Jimm.jimm.getSplashCanvasRef();
            wait2.setMessage(ResourceBundle.getString("wait"));
            wait2.setProgress(0);
            Jimm.display.setCurrent(wait2);

            // Request contact item removal
            UpdateContactListAction act2 = new UpdateContactListAction(cItem, UpdateContactListAction.ACTION_DEL);
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

    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    // #sijapp cond.if modules_FILES is "true"#
    // Resets the comm. subsystem
    public synchronized void resetPeerCon()
    {
        this.peerC = null;
    }

    // #sijapp cond.end#
    // #sijapp cond.end#

    /** *********************************************************************** */
    /** *********************************************************************** */
    /** *********************************************************************** */

    // Milliseconds to wait for new packets
    private static final int STANDBY = 250;

    // Wait object
    private Object wait = new Object();

    // Connection to the ICQ server
    Connection c;

    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    // #sijapp cond.if modules_FILES is "true"#
    // Connection to peer
    PeerConnection peerC;
    // #sijapp cond.end#
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
        this.c = new Connection();

        // Instantiate active actions vector
        this.actAction = new Vector();

        // Instantiate action listener
        this.actListener = new ActionListener();
        this.actListener.setIcq(this);

        // Instantiate KeepAliveTimerTask
        this.keepAliveTimerTask = new KeepAliveTimerTask();
        this.keepAliveTimerTask.setIcq(this);
        Jimm.jimm.getTimerRef().schedule(this.keepAliveTimerTask, Integer.parseInt(Jimm.jimm.getOptionsRef().getStringOption(Options.OPTION_CONN_ALIVE_INVTERV))*1000, Integer.parseInt(Jimm.jimm.getOptionsRef().getStringOption(Options.OPTION_CONN_ALIVE_INVTERV))*1000);

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
                            this.actAction.addElement(newAction);
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
                if (this.peerC != null)
                {
                    if (this.peerC.available() > 0)
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
                while ((this.c.available() > 0) || dcPacketAvailable)
                {
                    // Try to get packet
                    Packet packet = null;
                    try
                    {
                        if (this.c.available() > 0)
                            packet = this.c.getPacket();
                        else
                            if (dcPacketAvailable) packet = this.peerC.getPacket();
                    } catch (JimmException e)
                    {
                        JimmException.handleException(e);
                        if (e.isCritical()) throw (e);
                    }

                    // Forward received packet to all active actions and to the
                    // action listener
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
                            this.actListener.forward(packet);
                        } catch (JimmException e)
                        {
                            JimmException.handleException(e);
                            if (e.isCritical()) throw (e);
                        }
                    }

                    // Set dcPacketAvailable to true if the peerC is not null
                    // and there is an packet waiting
                    if (this.peerC != null)
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
                        if (this.c.available() > 0) packet = this.c.getPacket();
                    } catch (JimmException e)
                    {
                        JimmException.handleException(e);
                        if (e.isCritical()) throw (e);
                    }

                    // Forward received packet to all active actions and to the
                    // action listener
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
                            this.actListener.forward(packet);
                        } catch (JimmException e)
                        {
                            JimmException.handleException(e);
                            if (e.isCritical()) throw (e);
                        }
                    }
                }
                // #sijapp cond.end#
                // #sijapp cond.else#
                while ((this.c.available() > 0))
                {
                    // Try to get packet
                    Packet packet = null;
                    try
                    {
                        if (this.c.available() > 0) packet = this.c.getPacket();
                    } catch (JimmException e)
                    {
                        JimmException.handleException(e);
                        if (e.isCritical()) throw (e);
                    }

                    // Forward received packet to all active actions and to the
                    // action listener
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
                            this.actListener.forward(packet);
                        } catch (JimmException e)
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
        catch (JimmException e)
        {
            // Do nothing, already handled
        }

        // Close connection
        this.c.close();

        // Cancel KeepAliveTimerTask
        this.keepAliveTimerTask.cancel();

    }

    /** *********************************************************************** */
    /** *********************************************************************** */
    /** *********************************************************************** */

    // Connection
    public class Connection implements Runnable
    {

        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        // #sijapp cond.if modules_PROXY is "true"#
        private final byte[] SOCKS4_CMD_CONNECT =
        { (byte) 0x04, (byte) 0x01, (byte) 0x14, (byte) 0x46, // Port 5190
          (byte) 0x40, (byte) 0x0C, (byte) 0xA1, (byte) 0xB9, 
          (byte) 0x00 // IP 64.12.161.185 (default login.icq.com)
        };

        private final byte[] SOCKS5_HELLO =
        { (byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x02};

        private final byte[] SOCKS5_CMD_CONNECT =
        { (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x03};
        // #sijapp cond.end #
        // #sijapp cond.end #

        // Connection variables
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        private SocketConnection sc;
        // #sijapp cond.else#
        private StreamConnection sc;
        // #sijapp cond.end#
        private InputStream is;
        private OutputStream os;

        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        // #sijapp cond.if modules_PROXY is "true"#
        private boolean is_socks4 = false;
        private boolean is_socks5 = false;
        private boolean is_connected = false;
        // #sijapp cond.end#
        // #sijapp cond.end#

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

        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        // #sijapp cond.if modules_PROXY is "true"#
        // Tries to resolve given host IP
        private synchronized String ResolveIP(String host, String port)
        {
            if (Util.isIP(host)) return host;
            // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
            SocketConnection c;
            // #sijapp cond.else#
            StreamConnection c;
            // #sijapp cond.end#
            try
            {
                // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                c = (SocketConnection) Connector.open("socket://" + host + ":" + port, Connector.READ_WRITE);
                // #sijapp cond.else#
                c = (StreamConnection) Connector.open("socket://" + host + ":" + port, Connector.READ_WRITE);
                // #sijapp cond.end#

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

        }

        // Build socks4 CONNECT request
        private byte[] socks4_connect_request(String ip, String port)
        {
            byte[] buf = new byte[9];

            System.arraycopy(this.SOCKS4_CMD_CONNECT, 0, buf, 0, 9);
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

            System.arraycopy(this.SOCKS5_CMD_CONNECT, 0, buf, 0, 4);
            Util.putByte(buf, 4, host.length());
            byte[] bhost = Util.stringToByteArray(host);
            System.arraycopy(bhost, 0, buf, 5, bhost.length);
            Util.putWord(buf, 5 + bhost.length, Integer.parseInt(port));
            return buf;
        }

        // #sijapp cond.end #
        // #sijapp cond.end#

        // Opens a connection to the specified host and starts the receiver
        // thread
        public synchronized void connect(String hostAndPort) throws JimmException
        {
            // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
            // #sijapp cond.if modules_PROXY is "true"#
            int mode = Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_PRX_TYPE);
            this.is_connected = false;
            this.is_socks4 = false;
            this.is_socks5 = false;
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
                    this.connect_simple(hostAndPort);
                    break;
                case 1:
                    this.connect_socks4(host, port);
                    break;
                case 2:
                    this.connect_socks5(host, port);
                    break;
                case 3:
                    // Try better first
                    try
                    {
                        this.connect_socks5(host, port);
                    } catch (Exception e)
                    {
                        // Do nothing
                    }
                    // If not succeeded, then try socks4
                    if (!this.is_connected)
                    {
                        this.stream_close();
                        try
                        {
                            // Wait the given time
                            Thread.sleep(2000);
                        } catch (InterruptedException e)
                        {
                            // Do nothing
                        }
                        this.connect_socks4(host, port);
                    }
                    break;
                default:
                    this.connect_simple(hostAndPort);
                    break;
                }

                this.inputCloseFlag = false;
                this.rcvThread = new Thread(this);
                this.rcvThread.start();
                this.nextSequence = (new Random()).nextInt() % 0x0FFF;
                this.nextIcqSequence = 2;

            } catch (JimmException e)
            {
                throw (e);
            }

        }

        private synchronized void connect_simple(String hostAndPort) throws JimmException
        {
            // #sijapp cond.end#
            // #sijapp cond.end#
            try
            {
                // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                this.sc = (SocketConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);
                // #sijapp cond.else#
                this.sc = (StreamConnection) Connector.open("socket://" + hostAndPort, Connector.READ_WRITE);
                // #sijapp cond.end#
                this.is = this.sc.openInputStream();
                this.os = this.sc.openOutputStream();
                // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                // #sijapp cond.if modules_PROXY is "true"#
                this.is_connected = true;
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

        // Attempts to connect through socks4
        private synchronized void connect_socks4(String host, String port) throws JimmException
        {
            this.is_socks4 = false;
            String proxy_host = Jimm.jimm.getOptionsRef().getStringOption(Options.OPTION_PRX_SERV);
            String proxy_port = Jimm.jimm.getOptionsRef().getStringOption(Options.OPTION_PRX_PORT);
            int i = 0;
            byte[] buf;

            try
            {
                // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                this.sc = (SocketConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
                // #sijapp cond.else#
                this.sc = (StreamConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
                // #sijapp cond.end#
                this.is = this.sc.openInputStream();
                this.os = this.sc.openOutputStream();

                String ip = this.ResolveIP(host, port);

                this.os.write(this.socks4_connect_request(ip, port));
                this.os.flush();

                // Wait for responce
                while (this.is.available() == 0 && i < 50)
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
                if (this.is.available() == 8)
                {
                    // Read reply
                    buf = new byte[is.available()];
                    this.is.read(buf);

                    int ver = Util.getByte(buf, 0);
                    int meth = Util.getByte(buf, 1);
                    // All we need
                    if (ver == 0x00 && meth == 0x5A)
                    {
                        this.is_connected = true;
                        this.is_socks4 = true;
                    }
                    else
                    {
                        this.is_connected = false;
                        throw (new JimmException(118, 2));
                    }
                }
                // If we got responce packet bigger than mere proxy responce,
                // we might got destination server responce in tail of proxy
                // responce
                else
                    if (this.is.available() > 8)
                    {
                        this.is_connected = true;
                        this.is_socks4 = true;
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
            this.is_socks5 = false;
            String proxy_host = Jimm.jimm.getOptionsRef().getStringOption(Options.OPTION_PRX_SERV);
            String proxy_port = Jimm.jimm.getOptionsRef().getStringOption(Options.OPTION_PRX_PORT);
            String proxy_login = Jimm.jimm.getOptionsRef().getStringOption(Options.OPTION_PRX_NAME);
            String proxy_pass = Jimm.jimm.getOptionsRef().getStringOption(Options.OPTION_PRX_PASS);
            int i = 0;
            byte[] buf;

            try
            {
                // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                this.sc = (SocketConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
                // #sijapp cond.else#
                this.sc = (StreamConnection) Connector.open("socket://" + proxy_host + ":" + proxy_port, Connector.READ_WRITE);
                // #sijapp cond.end#
                this.is = this.sc.openInputStream();
                this.os = this.sc.openOutputStream();

                this.os.write(this.SOCKS5_HELLO);
                this.os.flush();

                // Wait for responce
                while (this.is.available() == 0 && i < 50)
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

                if (this.is.available() == 0) { throw (new JimmException(118, 2)); }

                // Read reply
                buf = new byte[this.is.available()];
                this.is.read(buf);

                int ver = Util.getByte(buf, 0);
                int meth = Util.getByte(buf, 1);

                // Plain text authorisation
                if (ver == 0x05 && meth == 0x02)
                {
                    this.os.write(this.socks5_authorize_request(proxy_login, proxy_pass));
                    this.os.flush();

                    // Wait for responce
                    while (this.is.available() == 0 && i < 50)
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

                    if (this.is.available() == 0) { throw (new JimmException(118, 2)); }

                    // Read reply
                    buf = new byte[this.is.available()];
                    this.is.read(buf);

                    meth = Util.getByte(buf, 1);

                    if (meth == 0x00)
                    {
                        this.is_connected = true;
                        this.is_socks5 = true;
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
                        this.is_connected = true;
                        this.is_socks5 = true;
                    }
                    // Something bad happened :'(
                    else
                    {
                        throw (new JimmException(118, 2));
                    }
                // If we got correct responce, send CONNECT
                if (this.is_connected == true)
                {
                    this.os.write(this.socks5_connect_request(host, port));
                    this.os.flush();
                }
                // #sijapp cond.else# 
                this.inputCloseFlag = false;
                this.rcvThread = new Thread(this);
                this.rcvThread.start();
                this.nextSequence = (new Random()).nextInt() % 0x0FFF;
                this.nextIcqSequence = 2;
                // #sijapp cond.end#                
                // #sijapp cond.else#
                this.inputCloseFlag = false;
                this.rcvThread = new Thread(this);
                this.rcvThread.start();
                this.nextSequence = (new Random()).nextInt() % 0x0FFF;
                this.nextIcqSequence = 2;
                // #sijapp cond.end#
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
            this.inputCloseFlag = true;

            this.stream_close();

            Thread.yield();
        }

        // Close input and output streams
        private synchronized void stream_close()
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
            if (this.os == null) { throw (new JimmException(123, 0)); }

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
                    Jimm.jimm.getTrafficRef().addTraffic(outpack.length + 40); // 40
                                                                                // is
                                                                                // the
                                                                                // overhead
                                                                                // for
                                                                                // each
                                                                                // packet
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
                        while (is.available() == 0)
                            Thread.sleep(250);
                    }
                    do
                    {
                        bRead = this.is.read(flapHeader, bReadSum, flapHeader.length - bReadSum);
                        if (bRead == -1) break;
                        bReadSum += bRead;
                    } while (bReadSum < flapHeader.length);
                    if (bRead == -1) break;
                    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                    // #sijapp cond.if modules_PROXY is "true"#
                    // Verify and strip out proxy responce
                    // Socks4 first
                    if (Util.getByte(flapHeader, 0) == 0x00 && this.is_socks4)
                    {
                        // Strip only on first packet
                        this.is_socks4 = false;
                        int rep = Util.getByte(flapHeader, 1);
                        if (rep != 0x5A)
                        {
                            // Something went wrong :(
                            throw (new JimmException(118, 1));
                        }

                        this.is.skip(2);

                        bReadSum = 0;
                        do
                        {
                            bRead = this.is.read(flapHeader, bReadSum, flapHeader.length - bReadSum);
                            if (bRead == -1) break;
                            bReadSum += bRead;
                        } while (bReadSum < flapHeader.length);
                    }
                    // Check for socks5
                    else
                        if (Util.getByte(flapHeader, 0) == 0x05 && this.is_socks5)
                        {
                            // Strip only on first packet
                            this.is_socks5 = false;

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
                                this.is.skip(4);
                            }
                            else
                                if (atyp == 0x03)
                                {
                                    int size = Util.getByte(flapHeader, 4);
                                    this.is.skip(size + 1);
                                }
                                else
                                {
                                    // Don't know what was that, but skip like
                                    // if it was an ip
                                    this.is.skip(4);
                                }

                            bReadSum = 0;
                            do
                            {
                                bRead = this.is.read(flapHeader, bReadSum, flapHeader.length - bReadSum);
                                if (bRead == -1) break;
                                bReadSum += bRead;
                            } while (bReadSum < flapHeader.length);
                        }
                    // #sijapp cond.end #
                    // #sijapp cond.end #                    

                    // Verify flap header
                    if (Util.getByte(flapHeader, 0) != 0x2A) { throw (new JimmException(124, 0)); }

                    // Allocate memory for flap data
                    flapData = new byte[Util.getWord(flapHeader, 4)];

                    // Read flap data
                    bReadSum = 0;
                    do
                    {
                        bRead = this.is.read(flapData, bReadSum, flapData.length - bReadSum);
                        if (bRead == -1) break;
                        bReadSum += bRead;
                    } while (bReadSum < flapData.length);
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
                if (!this.inputCloseFlag)
                {
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
                while (!this.inputCloseFlag)
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

}
