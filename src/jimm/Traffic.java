/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-07  Jimm Project

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
 File: src/jimm/Traffic.java
 Version: ###VERSION###  Date: ###DATE###
 Author: Andreas Rossbacher
 *******************************************************************************/

//#sijapp cond.if modules_TRAFFIC is "true" #
package jimm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import DrawControls.TextList;

import jimm.util.ResourceBundle;
import jimm.comm.Icq;
import jimm.comm.Util;

public class Traffic
{

	// Static final variables	
	static final public int SESSION = 10;

	static final public int OVERALL = 20;

	static final public int SAVED = 30;

	static final public int BYTES = 1;

	static final public int KB = 2;

	static final public int COST = 3;

	static final public int SAVED_SINCE = 4;

	// Persistent variables

	// Traffic read form file
	static private int all_traffic;

	// Traffic for this session
	static private int session_traffic;

	// Date of last reset of all_traffic
	static private Date savedSince;

	// Date of the last use of the connection
	static private Date lastTimeUsed;

	// Amount of money for all
	static private int savedCost;

	// Amount of money for the costs per day for this session
	static private int costPerDaySum;

	// Traffic Screen
	static public TrafficScreen trafficScreen;

	// Constructor
	public Traffic()
	{
		session_traffic = 0;
		savedCost = 0;
		lastTimeUsed = new Date(1);
		costPerDaySum = 0;
		savedSince = new Date();
		try
		{
			load();
		} catch (Exception e)
		{
			savedSince.setTime(new Date().getTime());
			all_traffic = 0;
		}
		// Construct traffic scrren
		trafficScreen = new TrafficScreen();
	}

	//Loads traffic from file

	static public void load() throws IOException, RecordStoreException
	{

		// Open record store
		RecordStore traffic = RecordStore.openRecordStore("traffic", false);

		// Temporary variables
		byte[] buf;
		ByteArrayInputStream bais;
		DataInputStream dis;

		// Get traffic amount and savedSince to record store
		buf = traffic.getRecord(2);
		bais = new ByteArrayInputStream(buf);
		dis = new DataInputStream(bais);
		all_traffic = dis.readInt();
		savedSince.setTime(dis.readLong());
		lastTimeUsed.setTime(dis.readLong());
		savedCost = dis.readInt();
		// Close record store
		traffic.closeRecordStore();

	}

	// Saves traffic from file

	static public void save() throws IOException, RecordStoreException
	{

		// Open record store
		RecordStore traffic = RecordStore.openRecordStore("traffic", true);

		// Add empty records if necessary
		while (traffic.getNumRecords() < 4)
		{
			traffic.addRecord(null, 0, 0);
		}

		// Temporary variables
		byte[] buf;
		ByteArrayOutputStream baos;
		DataOutputStream dos;

		// Add version info to record store
		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);
		dos.writeUTF(Jimm.VERSION);
		buf = baos.toByteArray();
		traffic.setRecord(1, buf, 0, buf.length);

		// Add traffic amount and savedSince to record store
		baos = new ByteArrayOutputStream();

		dos = new DataOutputStream(baos);
		dos.writeInt(all_traffic + session_traffic);
		dos.writeLong(savedSince.getTime());
		dos.writeLong(lastTimeUsed.getTime());
		dos.writeInt((int) generateCostSum(false));
		buf = baos.toByteArray();
		traffic.setRecord(2, buf, 0, buf.length);

		// Close record store
		traffic.closeRecordStore();

	}

	// Generates String for Traffic Info Screen
	static protected String getTrafficString(int type)
	{
		Calendar time = Calendar.getInstance();
		time.setTime(savedSince);
		switch (type)
		{
		case SESSION + BYTES:
			return (session_traffic + " " + ResourceBundle.getString("byte"));
		case SESSION + KB:
			return (session_traffic / 1024 + " " + ResourceBundle
					.getString("kb"));
		case SESSION + COST:
			return (getString(generateCostSum(true)) + " " + Options
					.getString(Options.OPTION_CURRENCY));
		case SAVED_SINCE:
			return (Util.makeTwo(time.get(Calendar.DAY_OF_MONTH)) + "."
					+ Util.makeTwo(time.get(Calendar.MONTH) + 1) + "."
					+ time.get(Calendar.YEAR) + " "
					+ Util.makeTwo(time.get(Calendar.HOUR_OF_DAY)) + ":" + Util
					.makeTwo(time.get(Calendar.MINUTE)));
		case OVERALL + BYTES:
			return ((all_traffic + session_traffic) + " " + ResourceBundle
					.getString("byte"));
		case OVERALL + KB:
			return (((all_traffic + session_traffic) / 1024) + " " + ResourceBundle
					.getString("kb"));
		case OVERALL + COST:
			return (getString(generateCostSum(false)) + " " + Options
					.getString(Options.OPTION_CURRENCY));
		}
		return ("");

	}

	// Returns String value of cost value
	static public String getString(int value)
	{
		String costString = "";
		String afterDot = "";
		try
		{
			if (value != 0)
			{
				costString = Integer.toString(value / 10000) + ".";
				afterDot = Integer.toString(value % 10000);
				while (afterDot.length() != 4)
					afterDot = "0" + afterDot;
				while ((afterDot.endsWith("0")) && (afterDot.length() > 2))
				{
					afterDot = afterDot.substring(0, afterDot.length() - 1);
				}
				costString = costString + afterDot;
				return costString;
			} else
				return new String("0.00");

		} catch (Exception e)
		{
			return new String("0.00");
		}
	}

	// Determins whenever we were already connected today or not
	static private boolean usedToday()
	{
		//		Date now = new Date();
		Calendar time_now = Calendar.getInstance();
		Calendar time_lastused = Calendar.getInstance();
		time_now.setTime(new Date());
		time_lastused.setTime(lastTimeUsed);
		if ((time_now.get(Calendar.DAY_OF_MONTH) == time_lastused
				.get(Calendar.DAY_OF_MONTH))
				&& (time_now.get(Calendar.MONTH) == time_lastused
						.get(Calendar.MONTH))
				&& (time_now.get(Calendar.YEAR) == time_lastused
						.get(Calendar.YEAR)))
		{
			return (true);

		} else
		{
			return (false);
		}
	}

	// Generates int of money amount spent on connection
	static protected int generateCostSum(boolean thisSession)
	{

		int cost;
		int costPerPacket = Options.getInt(Options.OPTION_COST_PER_PACKET);
		int costPacketLength = Options
				.getInt(Options.OPTION_COST_PACKET_LENGTH);

		if (thisSession)
			if (session_traffic != 0)
				cost = ((session_traffic / costPacketLength) + 1)
						* costPerPacket;
			else
				cost = 0;
		else if (session_traffic != 0)
			cost = ((session_traffic / costPacketLength) + 1) * costPerPacket
					+ savedCost;
		else
			cost = savedCost;
		if ((!usedToday()) && (session_traffic != 0) && (costPerDaySum == 0))
		{
			costPerDaySum = costPerDaySum
					+ Options.getInt(Options.OPTION_COST_PER_DAY);
			lastTimeUsed.setTime(new Date().getTime());
		}
		return (cost + costPerDaySum);
	}

	//Returns value of  traffic
	static public int getSessionTraffic()
	{
		return (session_traffic / 1024);
	}

	// Adds to session traffic
	static public void addTraffic(int bytes)
	{
		session_traffic += bytes;
	}

	// Reset the saved value
	static public void reset()
	{
		all_traffic = 0;
		savedCost = 0;
		savedSince.setTime(new Date().getTime());
		try
		{
			save();
		} catch (Exception e)
		{ // Do nothing
		}
	}

	/****************************************************************************/
	/****************************************************************************/
	/****************************************************************************/

	// Screen for Traffic information
	public class TrafficScreen implements CommandListener
	{

		// Form elements
		private Command resetCommand;

		private Command okCommand;

		private TextList trafficTextList;

		//private Form trafficScreen;

		//Number of kB defines the threshold when the screen should be update
		private byte updateThreshold;

		//Traffic value to compare to in kB
		private byte compareTraffic;

		// Constructor
		public TrafficScreen()
		{
			// super(ResourceBundle.getString("traffic_lng"),Options.)
			updateThreshold = 1;
			compareTraffic = (byte) Traffic.getSessionTraffic();

			// Initialize command
			//#sijapp cond.if target is "MOTOROLA" # 
			//#			this.resetCommand = new Command(ResourceBundle.getString("reset"), Command.BACK, 2);
			//#			this.okCommand = new Command(ResourceBundle.getString("ok"), Command.OK, 1);
			//#sijapp cond.else #
			this.resetCommand = new Command(ResourceBundle.getString("reset"),
					Command.SCREEN, 2);
			this.okCommand = new Command(ResourceBundle.getString("ok"),
					Command.BACK, 1);
			//#sijapp cond.end #

			// Initialize traffic screen
			this.trafficTextList = new TextList(ResourceBundle
					.getString("traffic_lng"));
			this.trafficTextList.setMode(TextList.MODE_TEXT);

			//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA"#
			trafficTextList.setFontSize(Font.SIZE_MEDIUM);
			//#sijapp cond.else#
			//#			trafficTextList.setFontSize(Font.SIZE_SMALL);
			//#sijapp cond.end#

			this.trafficTextList.addCommandEx(this.resetCommand, TextList.MENU_TYPE_RIGHT_BAR);
			this.trafficTextList.addCommandEx(this.okCommand, TextList.MENU_TYPE_LEFT_BAR);
			this.trafficTextList.setCommandListener(this);
		}

		// Activate traffic form
		public void activate()
		{
			this.update(true);
			JimmUI.setColorScheme(this.trafficTextList, false, -1);
			this.trafficTextList.activate(Jimm.display);
		}

		// Is the traffic screen active?
		public boolean isActive()
		{
			return (Jimm.display.getCurrent().equals(this.trafficTextList));
		}

		public void update(boolean doIt)
		{
			if (((Traffic.getSessionTraffic() - compareTraffic) >= updateThreshold)
					|| doIt)
			{
				int color = trafficTextList.getTextColor();
				this.trafficTextList.clear();
				this.trafficTextList.addBigText(
						ResourceBundle.getString("session") + ":\n", color,
						Font.STYLE_BOLD, -1).addBigText(
						Traffic.getTrafficString(SESSION + BYTES) + "\n",
						color, Font.STYLE_PLAIN, -1).addBigText(
						Traffic.getTrafficString(SESSION + KB) + "\n", color,
						Font.STYLE_PLAIN, -1).addBigText(
						Traffic.getTrafficString(SESSION + COST) + "\n", color,
						Font.STYLE_PLAIN, -1).addBigText(
						ResourceBundle.getString("since") + " ", color,
						Font.STYLE_BOLD, -1).addBigText(
						Traffic.getTrafficString(Traffic.SAVED_SINCE) + "\n",
						color, Font.STYLE_BOLD, -1).addBigText(
						Traffic.getTrafficString(OVERALL + BYTES) + "\n",
						color, Font.STYLE_PLAIN, -1).addBigText(
						Traffic.getTrafficString(OVERALL + KB) + "\n", color,
						Font.STYLE_PLAIN, -1).addBigText(
						Traffic.getTrafficString(OVERALL + COST) + "\n", color,
						Font.STYLE_PLAIN, -1);
				compareTraffic = (byte) Traffic.getSessionTraffic();
				this.trafficTextList.repaint();
			}
		}

		// Command listener
		public void commandAction(Command c, Displayable d)
		{

			// Look for save command
			if (c == this.resetCommand)
			{
				Traffic.reset();
				this.update(true);
				this.trafficTextList.repaint();
			}
			if (c == this.okCommand)
			{
				if (Icq.isConnected())
				{
					ContactList.activate();
				} else
				{
					MainMenu.activate();
				}
			}
		}
	}
}
//#sijapp cond.end#
