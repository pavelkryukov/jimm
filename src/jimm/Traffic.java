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

import DrawControls.TextList;
import DrawControls.VirtualList;

import jimm.util.ResourceBundle;
import jimm.comm.Util;

public class Traffic implements Runnable
{

	// Static final variables	
	static final public int BYTES			= 1 << 0;
	static final public int KB				= 1 << 1;
	static final public int COST			= 1 << 2;
	static final public int SAVED_SINCE		= 1 << 3;

	static final public int SESSION			= 1 << 4;
	static final public int OUT_SESSION		= 1 << 5;
	static final public int IN_SESSION		= 1 << 6;
	static final public int OVERALL			= 1 << 7;
	static final public int OUT_OVERALL		= 1 << 8;
	static final public int IN_OVERALL		= 1 << 9;
	static final public int SAVED			= 1 << 10;

	// Traffic read from file
	static private int out_all_traffic;
	static private int in_all_traffic;

	// Traffic for this session
	static private int out_session_traffic;
	static private int in_session_traffic;

	// Date of last reset of all_traffic
	static private Date savedSince;

	// Date of the last use of the connection
	static private Date lastTimeUsed;

	// Amount of money for all
	static private int savedCost;

	// Amount of money for the costs per day for this session
	static private int costPerDaySum;
	
	static private Traffic _this;

	// Traffic Screen
	static private TrafficScreen trafficScreen;

	// Constructor
	public Traffic()
	{
		_this = this;
		out_session_traffic = 0;
		in_session_traffic = 0;
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
			out_all_traffic = 0;
			in_all_traffic = 0;
		}
	}
	
	public static void showScreen()
	{
		if (trafficScreen == null)
			trafficScreen = _this.new TrafficScreen();
		trafficScreen.activate();
	}
	
	private static final String RMS_NAME = "traffic";
	private static final String RMS_VERS = "V1";

	//Loads traffic from file
	static public void load() throws IOException
	{
		DataInputStream dis = Util.getRmsInputStream(RMS_NAME, RMS_VERS);
		if (dis == null) return;
		dis.readInt(); // skip old stored value "all_traffic"
		savedSince.setTime(dis.readLong());
		lastTimeUsed.setTime(dis.readLong());
		savedCost = dis.readInt();
		out_all_traffic = dis.readInt();
		in_all_traffic = dis.readInt();
	}

	// Saves traffic to file
	static public void save() throws IOException
	{
		// Add traffic amount and savedSince to record store
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(0);
		dos.writeLong(savedSince.getTime());
		dos.writeLong(lastTimeUsed.getTime());
		dos.writeInt((int) generateCostSum(false));
		dos.writeInt(out_all_traffic + out_session_traffic);
		dos.writeInt(in_all_traffic + in_session_traffic);
		Util.saveStreamToRms(baos.toByteArray(), RMS_NAME, RMS_VERS);
	}

	// Generates String for Traffic Info Screen
	static protected String getTrafficString(int type) 
	{
		synchronized (_this)
		{
			Calendar time = Calendar.getInstance();
			time.setTime(savedSince);

			int value = 0;
			switch (type) {
				case SESSION:
					value = out_session_traffic + in_session_traffic;
					break;
				case OUT_SESSION:
					value = out_session_traffic;
					break;
				case IN_SESSION:
					value = in_session_traffic;
					break;
				case SESSION + COST:
					return (getString(generateCostSum(true)) + " " + Options.getString(Options.OPTION_CURRENCY));
				case SAVED_SINCE:
					return (Util.makeTwo(time.get(Calendar.DAY_OF_MONTH)) + "." + Util.makeTwo(time.get(Calendar.MONTH) + 1) + "." + time.get(Calendar.YEAR) + " " + Util.makeTwo(time.get(Calendar.HOUR_OF_DAY)) + ":" + Util.makeTwo(time.get(Calendar.MINUTE)));
				case OVERALL:
					value = out_all_traffic + in_all_traffic + out_session_traffic + in_session_traffic;
					break;
				case OUT_OVERALL:
					value = out_all_traffic + out_session_traffic;
					break;
				case IN_OVERALL:
					value = in_all_traffic + in_session_traffic;
					break;
				case OVERALL + COST:
					return (getString(generateCostSum(false)) + " " + Options.getString(Options.OPTION_CURRENCY));
			}

			if (value != 0) {
				int k = (value > 1023) ? 1024 : 1;
				String tr = (value > 1023) ? ResourceBundle.getString("kb") : ResourceBundle.getString("byte");
				return ((value / k) + " " + tr);
			}
			return ("");
		}
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
		synchronized (_this) 
		{ 
			int cost;
			int costPerPacket = Options.getInt(Options.OPTION_COST_PER_PACKET) / 100;
			int costPacketLength = Options.getInt(Options.OPTION_COST_PACKET_LENGTH);

			if ((costPacketLength != 0) && ((out_session_traffic + in_session_traffic) != 0)) {
				cost = (((out_session_traffic + in_session_traffic) / costPacketLength) + 1) * costPerPacket;
			} else {
				cost = 0;
			}
			if (!thisSession) {
				cost += savedCost;
			}
			if ((!usedToday()) && (out_session_traffic != 0) && (in_session_traffic != 0) && (costPerDaySum == 0)) {
				costPerDaySum = costPerDaySum + Options.getInt(Options.OPTION_COST_PER_DAY);
				lastTimeUsed.setTime(new Date().getTime());
			}
			return (cost + costPerDaySum);
		}
	}

	//Returns value of  traffic
	static public int getSessionTraffic()
	{
		synchronized (_this) { 
			return ((out_session_traffic + in_session_traffic) / 1024);
		}
	}

	// Adds to "in" session traffic
	static public void addInTraffic(int bytes)
	{
		synchronized (_this) { in_session_traffic += bytes; }
		Jimm.display.callSerially(_this);
	}

	// Adds to "out" session traffic
	static public void addOutTraffic(int bytes)
	{
		synchronized (_this) { out_session_traffic += bytes; }
		Jimm.display.callSerially(_this);
	}

	// Reset the saved value
	static public void reset()
	{
		synchronized (_this)
		{
			out_all_traffic = 0;
			in_all_traffic = 0;
		}
		savedCost = 0;
		savedSince.setTime(new Date().getTime());
		try
		{
			save();
		} catch (Exception e)
		{ // Do nothing
		}
	}
	
	public void run()
	{
		if (trafficScreen != null) 
			trafficScreen.update(false);
	}

	/****************************************************************************/
	/****************************************************************************/
	/****************************************************************************/

	// Screen for Traffic information
	class TrafficScreen implements CommandListener
	{
		// Form elements
		private Command resetCommand;
		private TextList trafficTextList;

		//private Form trafficScreen;

		//Number of kB defines the threshold when the screen should be update
		private int updateThreshold;

		//Traffic value to compare to in kB
		private int compareTraffic;

		// Constructor
		public TrafficScreen()
		{
			updateThreshold = 1;
			compareTraffic = (byte) Traffic.getSessionTraffic();

			// Initialize command
			this.resetCommand = new Command(ResourceBundle.getString("reset"), Command.SCREEN, 2);

			// Initialize traffic screen
			this.trafficTextList = new TextList(ResourceBundle
					.getString("traffic_lng"));
			this.trafficTextList.setMode(VirtualList.CURSOR_MODE_DISABLED);

			//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA"#
			trafficTextList.setFontSize(Font.SIZE_MEDIUM);
			//#sijapp cond.else#
			//#			trafficTextList.setFontSize(Font.SIZE_SMALL);
			//#sijapp cond.end#

			this.trafficTextList.addCommandEx(this.resetCommand, VirtualList.MENU_TYPE_RIGHT_BAR);
			this.trafficTextList.addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
			this.trafficTextList.setCommandListener(this);
		}

		// Activate traffic form
		public void activate()
		{
			this.update(true);
			JimmUI.setColorScheme(this.trafficTextList, false, -1, true);
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
				this.trafficTextList.lock();
				this.trafficTextList.clear();
				this.trafficTextList.addBigText(
						ResourceBundle.getString("session") + ":\n", color,
						Font.STYLE_BOLD, -1).addBigText(

						ResourceBundle.getString("received") + ": " +
						Traffic.getTrafficString(IN_SESSION) + "\n",
						color, Font.STYLE_PLAIN, -1).addBigText(

						ResourceBundle.getString("transmitted") + ": " +
						Traffic.getTrafficString(OUT_SESSION) + "\n", color,
						Font.STYLE_PLAIN, -1).addBigText(

						ResourceBundle.getString("total") + ": " +
						Traffic.getTrafficString(SESSION) + "\n", color,
						Font.STYLE_PLAIN, -1).addBigText(

						ResourceBundle.getString("cost") + ": " +
						Traffic.getTrafficString(SESSION + COST) + "\n", color,
						Font.STYLE_PLAIN, -1).addBigText(
						ResourceBundle.getString("since") + " ", color,
						Font.STYLE_BOLD, -1).addBigText(
						Traffic.getTrafficString(Traffic.SAVED_SINCE) + "\n",
						color, Font.STYLE_BOLD, -1).addBigText(

						ResourceBundle.getString("received") + ": " +
						Traffic.getTrafficString(IN_OVERALL) + "\n",
						color, Font.STYLE_PLAIN, -1).addBigText(

						ResourceBundle.getString("transmitted") + ": " +
						Traffic.getTrafficString(OUT_OVERALL) + "\n", color,
						Font.STYLE_PLAIN, -1).addBigText(

						ResourceBundle.getString("total") + ": " +
						Traffic.getTrafficString(OVERALL) + "\n", color,
						Font.STYLE_PLAIN, -1).addBigText(

						ResourceBundle.getString("cost") + ": " +
						Traffic.getTrafficString(OVERALL + COST) + "\n", color,
						Font.STYLE_PLAIN, -1);
				compareTraffic = Traffic.getSessionTraffic();
				this.trafficTextList.unlock();
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
			if (c == JimmUI.cmdBack)
			{
				JimmUI.backToLastScreen();
			}
		}
	}
}
//#sijapp cond.end#
