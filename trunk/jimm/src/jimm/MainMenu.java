/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-04  Jimm Project

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
 File: src/jimm/MainMenu.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


package jimm;


import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDletStateChangeException;

import jimm.comm.ConnectAction;
import jimm.comm.DisconnectAction;
import jimm.comm.Icq;
import jimm.comm.SetOnlineStatusAction;
import jimm.util.ResourceBundle;


public class MainMenu implements CommandListener
{


	// Abort command
	private static Command backCommand = new Command(ResourceBundle.getString("jimm.res.Text", "back"), Command.BACK, 1);


	// List for selecting a online status
	private static List statusList;


	// Initializer
	static {

		MainMenu.statusList =
			new List(ResourceBundle.getString("jimm.res.Text", "set_status"),List.IMPLICIT);
		MainMenu.statusList.append(ResourceBundle.getString("jimm.res.Text", "status_online"),ContactList.statusOnlineImg);
		MainMenu.statusList.append(ResourceBundle.getString("jimm.res.Text", "status_chat"),ContactList.statusChatImg);
		MainMenu.statusList.append(ResourceBundle.getString("jimm.res.Text", "status_away"),ContactList.statusAwayImg);
		MainMenu.statusList.append(ResourceBundle.getString("jimm.res.Text", "status_na"),ContactList.statusNaImg);
		MainMenu.statusList.append(ResourceBundle.getString("jimm.res.Text", "status_occupied"),ContactList.statusOccupiedImg);
		MainMenu.statusList.append(ResourceBundle.getString("jimm.res.Text", "status_dnd"),ContactList.statusDndImg);
		MainMenu.statusList.append(ResourceBundle.getString("jimm.res.Text", "status_invisible"),ContactList.statusInvisibleImg);

	}


	/****************************************************************************/


	// Visual list
	private List list;


	// Connected
	private boolean isConnected;


	// Builds the main menu (visual list)
	private void build()
	{
		if ((Jimm.jimm.getIcqRef().isConnected() != isConnected) || (this.list == null))
		{
			if (Jimm.jimm.getIcqRef().isNotConnected())
			{
				this.list = new List(ResourceBundle.getString("jimm.res.Text", "menu"), List.IMPLICIT);
				this.list.append(ResourceBundle.getString("jimm.res.Text", "connect"), null);
				this.list.append(ResourceBundle.getString("jimm.res.Text", "options"), null);
//				#sijapp cond.if mod_TRAF is "true" #
				this.list.append(ResourceBundle.getString("jimm.res.Text", "traffic"), null);
//				#sijapp cond.end#
				this.list.append(ResourceBundle.getString("jimm.res.Text", "about"), null);
				this.list.append(ResourceBundle.getString("jimm.res.Text", "exit"), null);

				this.list.setCommandListener(this);
			}
			else
			{
				this.list = new List(ResourceBundle.getString("jimm.res.Text", "menu"), List.IMPLICIT);
				this.list.append(ResourceBundle.getString("jimm.res.Text", "keylock_enable"), null);
				this.list.append(ResourceBundle.getString("jimm.res.Text", "disconnect"), null);
				this.list.append(ResourceBundle.getString("jimm.res.Text", "set_status"), null);
				this.list.append(ResourceBundle.getString("jimm.res.Text", "add_user"), null);
				this.list.append(ResourceBundle.getString("jimm.res.Text", "search_user"), null);
				this.list.append(ResourceBundle.getString("jimm.res.Text", "options"), null);
//				#sijapp cond.if mod_TRAF is "true" #
				this.list.append(ResourceBundle.getString("jimm.res.Text", "traffic"), null);
//				#sijapp cond.end#
				this.list.append(ResourceBundle.getString("jimm.res.Text", "about"), null);
				this.list.append(ResourceBundle.getString("jimm.res.Text", "exit"), null);
				this.list.addCommand(MainMenu.backCommand);
				this.list.setCommandListener(this);
			}
			this.isConnected = Jimm.jimm.getIcqRef().isConnected();
		}
		this.list.setSelectedIndex(0, true);
	}


	// Displays the given alert and activates the main menu afterwards
	public void activate(Alert alert)
	{
		this.build();
		Jimm.display.setCurrent(alert, this.list);
	}

	// Activates the main menu
	public void activate()
	{
		this.build();
		Jimm.display.setCurrent(this.list);
	}


	// Command listener
	public void commandAction(Command c, Displayable d)
	{

		// Get ICQ object
		Icq icq = Jimm.jimm.getIcqRef();

		// Get options container
		Options options = Jimm.jimm.getOptionsRef();

//		#sijapp cond.if mod_TRAF is "true" #
		//Get traffic container

		Traffic traffic = Jimm.jimm.getTrafficRef();
//		#sijapp cond.end#

		// Return to contact list
		if (c == MainMenu.backCommand)
		{
			Jimm.jimm.getContactListRef().activate();
		}
		// Menu item has been selected
		else if ((c == List.SELECT_COMMAND) && (d == this.list))
		{
			if (this.isConnected)
			{
				switch (this.list.getSelectedIndex())
				{
					case 0:   // Enable keylock
						Jimm.jimm.getSplashCanvasRef().lock();
						break;
					case 1:   // Disconnect

						// Display splash canvas
						SplashCanvas wait = Jimm.jimm.getSplashCanvasRef();
						wait.setMessage(ResourceBundle.getString("jimm.res.Text", "disconnecting"));
						wait.setProgress(0);
						Jimm.display.setCurrent(wait);

						// Disconnect
						DisconnectAction act = new DisconnectAction();
						try
						{
							icq.requestAction(act);
						}
						catch (JimmException e)
						{
							JimmException.handleException(e);
							if (e.isCritical()) return;
						}

						// Start timer
						Jimm.jimm.getTimerRef().schedule(new SplashCanvas.DisconnectTimerTask(act, false), 1000, 1000);
//					#sijapp cond.if mod_TRAF is "true" #
						try
						{
							traffic.save();
						}
						catch (Exception e)
						{ // Do nothing
						}
//					#sijapp cond.end#
						break;

					case 2:   // Set status

						// Display status list
						long onlineStatus = options.getOnlineStatus();
						if (onlineStatus == ContactList.STATUS_AWAY)
						{
							MainMenu.statusList.setSelectedIndex(2, true);
						}
						else if (onlineStatus == ContactList.STATUS_CHAT)
						{
							MainMenu.statusList.setSelectedIndex(1, true);
						}
						else if (onlineStatus == ContactList.STATUS_DND)
						{
							MainMenu.statusList.setSelectedIndex(5, true);
						}
						else if (onlineStatus == ContactList.STATUS_INVISIBLE)
						{
							MainMenu.statusList.setSelectedIndex(6, true);
						}
						else if (onlineStatus == ContactList.STATUS_NA)
						{
							MainMenu.statusList.setSelectedIndex(3, true);
						}
						else if (onlineStatus == ContactList.STATUS_OCCUPIED)
						{
							MainMenu.statusList.setSelectedIndex(4, true);
						}
						else if (onlineStatus == ContactList.STATUS_ONLINE)
						{
							MainMenu.statusList.setSelectedIndex(0, true);
						}
						MainMenu.statusList.setCommandListener(this);
						Jimm.display.setCurrent(MainMenu.statusList);

						break;
					case 3:   // Add user

						// Not implemented yet
						Alert notImplemented2 = new Alert(ResourceBundle.getString("jimm.res.Text", "error"), ResourceBundle.getString("jimm.res.Text", "not_implemented"), null, AlertType.ERROR);
						notImplemented2.setTimeout(Alert.FOREVER);
						Jimm.jimm.getContactListRef().activate(notImplemented2);

						break;
					case 4:   // Search for User

						// Not implemented yet
						Alert notImplemented3 = new Alert(ResourceBundle.getString("jimm.res.Text", "error"), ResourceBundle.getString("jimm.res.Text", "not_implemented"), null, AlertType.ERROR);
						notImplemented3.setTimeout(Alert.FOREVER);
						Jimm.jimm.getContactListRef().activate(notImplemented3);

						break;
					case 5:   // Options
						options.optionsForm.activate();

						break;
//					#sijapp cond.if mod_TRAF is "true" #
					case 6:   // Traffic

						// Display an traffic alert
						traffic.setIsActive(true);
						traffic.trafficScreen.activate();

						break;
					case 7:   // About

						// Display an info alert
						Alert about = new Alert(ResourceBundle.getString("jimm.res.Text", "about"), ResourceBundle.getString("jimm.res.Text", "about_info"), null, AlertType.INFO);
						about.setTimeout(Alert.FOREVER);
						Jimm.jimm.getContactListRef().activate(about);

						break;
					case 8:   // Exit

						// Disconnect
						DisconnectAction act2 = new DisconnectAction();
						try
						{
							icq.requestAction(act2);
						}
						catch (JimmException e)
						{
							JimmException.handleException(e);
							if (e.isCritical()) return;
						}

						// Start timer
						Jimm.jimm.getTimerRef().schedule(new SplashCanvas.DisconnectTimerTask(act2, true), 1000, 1000);
						try
						{
							traffic.save();
						}
						catch (Exception e)
						{ // Do nothing
						}
						break;
//					#sijapp cond.else#
					case 6:   // About

						// Display an info alert
						Alert about = new Alert(ResourceBundle.getString("jimm.res.Text", "about"), ResourceBundle.getString("jimm.res.Text", "about_info"), null, AlertType.INFO);
						about.setTimeout(Alert.FOREVER);
						Jimm.jimm.getContactListRef().activate(about);

						break;
					case 7:   // Exit

						// Disconnect
						DisconnectAction act2 = new DisconnectAction();
						try
						{
							icq.requestAction(act2);
						}
						catch (JimmException e)
						{
							JimmException.handleException(e);
							if (e.isCritical()) return;
						}

						break;
//					#sijapp cond.end#
				}
			}
			else
			{
				switch (this.list.getSelectedIndex())
				{
					case 0:   // Connect

						// Display splash canvas
						SplashCanvas wait = Jimm.jimm.getSplashCanvasRef();
						wait.setMessage(ResourceBundle.getString("jimm.res.Text", "connecting"));
						wait.setProgress(0);
						Jimm.display.setCurrent(wait);

						// Connect
						ConnectAction act = new ConnectAction(options.getUin(), options.getPassword(), options.getSrvHost(), options.getSrvPort());
						try
						{
							icq.requestAction(act);

						}
						catch (JimmException e)
						{
							JimmException.handleException(e);
							if (e.isCritical()) return;
						}

						// Start timer
						Jimm.jimm.getTimerRef().schedule(new SplashCanvas.ConnectTimerTask(act), 1000, 1000);

						break;
					case 1:   // Options
						options.optionsForm.activate();

						break;
//					#sijapp cond.if mod_TRAF is "true" #
					case 2:   // Traffic

						// Display an traffic alert
						traffic.setIsActive(true);
						traffic.trafficScreen.activate();

						break;
					case 3:   // About

						// Display an info alert
						Alert about = new Alert(ResourceBundle.getString("jimm.res.Text", "about"), ResourceBundle.getString("jimm.res.Text", "about_info"), null, AlertType.INFO);
						about.setTimeout(Alert.FOREVER);
						this.activate(about);

						break;
					case 4:   // Exit

						try
						{
							traffic.save();
						}
						catch (Exception e)
						{ // Do nothing
						}
						try
						{
							Jimm.jimm.destroyApp(true);
						}
						catch (MIDletStateChangeException e)
						{
							// Do nothing
						}

						break;
//					#sijapp cond.else#
					case 2:   // About

						// Display an info alert
						Alert about = new Alert(ResourceBundle.getString("jimm.res.Text", "about"), ResourceBundle.getString("jimm.res.Text", "about_info"), null, AlertType.INFO);
						about.setTimeout(Alert.FOREVER);
						this.activate(about);

						break;
					case 3:   // Exit

						try
						{
							Jimm.jimm.destroyApp(true);
						}
						catch (MIDletStateChangeException e)
						{
							// Do nothing
						}

						break;
//					#sijapp cond.end#

				}
			}
		}
		// Online status has been selected
		else if ((c == List.SELECT_COMMAND) && (d == MainMenu.statusList))
		{

			// Request online status change
			long onlineStatus = ContactList.STATUS_ONLINE;
			switch (MainMenu.statusList.getSelectedIndex())
			{
				case 1:
					onlineStatus = ContactList.STATUS_CHAT;
					break;
				case 2:
					onlineStatus = ContactList.STATUS_AWAY;
					break;
				case 3:
					onlineStatus = ContactList.STATUS_NA;
					break;
				case 4:
					onlineStatus = ContactList.STATUS_OCCUPIED;
					break;
				case 5:
					onlineStatus = ContactList.STATUS_DND;
					break;
				case 6:
					onlineStatus = ContactList.STATUS_INVISIBLE;
					break;
			}
			try
			{
				SetOnlineStatusAction act = new SetOnlineStatusAction(onlineStatus);
				icq.requestAction(act);
			}
			catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical()) return;
			}

			// Activate main menu
			Jimm.jimm.getContactListRef().activate();

		}

	}


}
