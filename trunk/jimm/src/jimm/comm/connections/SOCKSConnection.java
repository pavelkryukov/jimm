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
 File: src/jimm/comm/connections/SOCKSConnection.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher, Perminov Andrey
*******************************************************************************/

package jimm.comm.connections;

//#sijapp cond.if modules_PROXY is "true"#

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Vector;


//#sijapp cond.if target!="DEFAULT"#
	import javax.microedition.io.SocketConnection;
//#sijapp cond.else#
	import javax.microedition.io.StreamConnection;
//#sijapp cond.end#

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;

import jimm.ContactList;
import jimm.JimmException;
import jimm.Options;
import jimm.RunnableImpl;
import jimm.comm.Icq;
import jimm.comm.Packet;
import jimm.comm.ToIcqSrvPacket;
import jimm.comm.Util;

//#sijapp cond.if modules_TRAFFIC is "true" #
	import jimm.Traffic;
//#sijapp cond.end#


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

	// ICQ sequence number counter
	private int nextIcqSequence;

	public boolean haveToSetNullAfterDisconnect()
	{
		return false;
	}
	

	// Tries to resolve given host IP
	private synchronized String ResolveIP(String host, String port)
	{
		//#sijapp cond.if target!="DEFAULT"#
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
			// Set starting point for seq numbers (not bigger then 0x8000)
			Random rand = new Random(System.currentTimeMillis());
			flapSEQ = rand.nextInt() % 0x8000;
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
			//#sijapp cond.if target!="DEFAULT"#
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
		} catch (SecurityException e)
		{
			throw (new JimmException(119, 0));
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
			//#sijapp cond.if target!="DEFAULT"#
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
		} catch (SecurityException e)
		{
			throw (new JimmException(119, 0));
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
			packet.setSequence(getFlapSequence());
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

	//#sijapp cond.if target!="DEFAULT" & modules_FILES="true"#

	// Return the port this connection is running on
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

	// Return the ip this connection is running on
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
				synchronized (Icq.getWaitObj())
				{
					Icq.getWaitObj().notify();
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
