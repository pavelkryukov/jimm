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
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDletStateChangeException;

import jimm.comm.SearchAction;
import jimm.comm.SetOnlineStatusAction;
import jimm.util.ResourceBundle;

public class MainMenu implements CommandListener
{

    // Abort command
    private static Command backCommand = new Command(ResourceBundle.getString("back"), Command.BACK, 1);

    // List for selecting a online status
    private static List statusList;

    // Initializer
    static
    {
  		MainMenu.statusList = new List(ResourceBundle.getString("set_status"), List.IMPLICIT);
   		MainMenu.statusList.append(ResourceBundle.getString("status_online"), ContactList.statusOnlineImg);
   		MainMenu.statusList.append(ResourceBundle.getString("status_chat"), ContactList.statusChatImg);
   		MainMenu.statusList.append(ResourceBundle.getString("status_away"), ContactList.statusAwayImg);
   		MainMenu.statusList.append(ResourceBundle.getString("status_na"), ContactList.statusNaImg);
   		MainMenu.statusList.append(ResourceBundle.getString("status_occupied"), ContactList.statusOccupiedImg);
   		MainMenu.statusList.append(ResourceBundle.getString("status_dnd"), ContactList.statusDndImg);
   		MainMenu.statusList.append(ResourceBundle.getString("status_invisible"), ContactList.statusInvisibleImg);
    }

    /** ************************************************************************* */

    // Visual list
    private List list;

    // List for all groups avaialable
    private List groupList;

    // Form for the adding users dialog
    public Form addUser;

    // Text box for adding users to the contact list
    private TextField uinTextField;
    private TextField nameTextField;

    // Connected
    private boolean isConnected;
    
    private Image getStatusImage()
    {
    	long cursStatus = Jimm.jimm.getOptionsRef().getLongOption(Options.OPTION_ONLINE_STATUS);
    	int imageIndex = ContactListContactItem.getStatusImageIndex(cursStatus);
    	return ContactList.getImageList().elementAt(imageIndex);
    }

    // Builds the main menu (visual list)
    private void build()
    {
        if ((Jimm.jimm.getIcqRef().isConnected() != isConnected) || (this.list == null))
        {
            if (Jimm.jimm.getIcqRef().isNotConnected())
            {
                this.list = new List(ResourceBundle.getString("menu"), List.IMPLICIT);
                this.list.append(ResourceBundle.getString("connect"), null);
                this.list.append(ResourceBundle.getString("contact_list"), null);
                this.list.append(ResourceBundle.getString("options"), null);
                // #sijapp cond.if modules_TRAFFIC is "true" #
                this.list.append(ResourceBundle.getString("traffic"), null);
                // #sijapp cond.end#
                this.list.append(ResourceBundle.getString("about"), null);
                this.list.append(ResourceBundle.getString("exit"), null);

                this.list.setCommandListener(this);
            } else
            {
                this.list = new List(ResourceBundle.getString("menu"), List.IMPLICIT);
                this.list.append(ResourceBundle.getString("keylock_enable"), null);
                this.list.append(ResourceBundle.getString("disconnect"), null);
                this.list.append(ResourceBundle.getString("set_status"), getStatusImage());
                this.list.append(ResourceBundle.getString("add_user"), null);
                this.list.append(ResourceBundle.getString("search_user"), null);
                this.list.append(ResourceBundle.getString("options"), null);
                // #sijapp cond.if modules_TRAFFIC is "true" #
                this.list.append(ResourceBundle.getString("traffic"), null);
                // #sijapp cond.end#
                this.list.append(ResourceBundle.getString("about"), null);
                this.list.append(ResourceBundle.getString("exit"), null);
                this.list.addCommand(MainMenu.backCommand);
                this.list.setCommandListener(this);
            }
            this.isConnected = Jimm.jimm.getIcqRef().isConnected();
        }
        else
        {
            if ((this.list != null) && (!Jimm.jimm.getIcqRef().isNotConnected()))
                this.list.set(2,ResourceBundle.getString("set_status"), getStatusImage());
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

        // #sijapp cond.if modules_TRAFFIC is "true" #
        
        //Get traffic container
        Traffic traffic = Jimm.jimm.getTrafficRef();
        // #sijapp cond.end#

        // Return to contact list
        if (c == MainMenu.backCommand)
        {
            if ((d == this.groupList) || (d == this.addUser) || (d == statusList))
                this.activate();
            else
                Jimm.jimm.getContactListRef().activate();
        }
        
        // Menu item has been selected
        else if ((c == List.SELECT_COMMAND) && (d == this.list))
        {
            if (this.isConnected)
            {
                switch (this.list.getSelectedIndex())
                {
                case 0:
                    // Enable keylock
                    Jimm.jimm.getSplashCanvasRef().lock();
                    break;
                case 1:
                    // Disconnect
                    Jimm.jimm.getIcqRef().disconnect();
                    break;

                case 2:
                    // Set status

                    // Display status list
                    long onlineStatus = Jimm.jimm.getOptionsRef().getLongOption(Options.OPTION_ONLINE_STATUS);
                    if (onlineStatus == ContactList.STATUS_AWAY)
                    {
                        MainMenu.statusList.setSelectedIndex(2, true);
                    } else if (onlineStatus == ContactList.STATUS_CHAT)
                    {
                        MainMenu.statusList.setSelectedIndex(1, true);
                    } else if (onlineStatus == ContactList.STATUS_DND)
                    {
                        MainMenu.statusList.setSelectedIndex(5, true);
                    } else if (onlineStatus == ContactList.STATUS_INVISIBLE)
                    {
                        MainMenu.statusList.setSelectedIndex(6, true);
                    } else if (onlineStatus == ContactList.STATUS_NA)
                    {
                        MainMenu.statusList.setSelectedIndex(3, true);
                    } else if (onlineStatus == ContactList.STATUS_OCCUPIED)
                    {
                        MainMenu.statusList.setSelectedIndex(4, true);
                    } else if (onlineStatus == ContactList.STATUS_ONLINE)
                    {
                        MainMenu.statusList.setSelectedIndex(0, true);
                    }
                    MainMenu.statusList.setCommandListener(this);
                    MainMenu.statusList.addCommand(backCommand);
                    Jimm.display.setCurrent(MainMenu.statusList);

                    break;
                case 3: // Add user
                    
                    break;
                case 4:
                    // Search for User
                    break;
                case 5:
                    // Options
                    Jimm.jimm.getOptionsRef().optionsForm.activate();

                    break;
                // #sijapp cond.if modules_TRAFFIC is "true" #
                case 6:
                    // Traffic

                    // Display an traffic alert
                    traffic.setIsActive(true);
                    traffic.trafficScreen.activate();

                    break;
                case 7:
                    // About

                    // Display an info alert
                    Alert about = new Alert(ResourceBundle.getString("about"), ResourceBundle
                            .getString("about_info"), null, AlertType.INFO);
                    about.setTimeout(Alert.FOREVER);
                    Jimm.jimm.getContactListRef().activate(about);

                    break;
                case 8:
                    // Exit

                    // Disconnect
                    Jimm.jimm.getIcqRef().disconnect();
					// Save traffic
					try {
						traffic.save();
					} catch (Exception e) { // Do nothing
					}
					// Exit app
					try {
						Jimm.jimm.destroyApp(true);
					} catch (MIDletStateChangeException e) {
						// Do nothing
					}
                    break;
                // #sijapp cond.else#
                case 6:
                    // About

                    // Display an info alert
                    Alert about = new Alert(ResourceBundle.getString("about"), ResourceBundle
                            .getString("about_info"), null, AlertType.INFO);
                    about.setTimeout(Alert.FOREVER);
                    Jimm.jimm.getContactListRef().activate(about);

                    break;
                case 7:
                    // Exit

						// Disconnect
						Jimm.jimm.getIcqRef().disconnect();

						// Exit app
						try {
							Jimm.jimm.destroyApp(true);
						} catch (MIDletStateChangeException e) {
							// Do nothing
						}

                    break;

                // #sijapp cond.end#
                }
            } else
            {
                switch (this.list.getSelectedIndex())
                {
                case 0:
                    // Connect
                    Jimm.jimm.getIcqRef().connect();
                                    
                    break;
                case 1:
                    // ContactList
                    Jimm.jimm.getContactListRef().activate();

                    break;
                case 2:
                    // Options
                    Jimm.jimm.getOptionsRef().optionsForm.activate();

                    break;
                // #sijapp cond.if modules_TRAFFIC is "true" #
                case 3:
                    // Traffic

                    // Display an traffic alert
                    traffic.setIsActive(true);
                    traffic.trafficScreen.activate();

                    break;
                case 4:
                    // About

                    // Display an info alert
                    Alert about = new Alert(ResourceBundle.getString("about"), ResourceBundle
                            .getString("about_info"), null, AlertType.INFO);
                    about.setTimeout(Alert.FOREVER);
                    this.activate(about);

                    break;
                case 5:
                    // Exit

                    // Disconnect
					Jimm.jimm.getIcqRef().disconnect();
					
					// Save the traffic
                    try
                    {
                        traffic.save();
                    } catch (Exception e)
                    { // Do nothing
                    }
                    try
                    {
                        Jimm.jimm.destroyApp(true);
                    } catch (MIDletStateChangeException e)
                    {
                        // Do nothing
                    }

                    break;
                // #sijapp cond.else#
                case 3:
                    // About

                    // Display an info alert
                    Alert about = new Alert(ResourceBundle.getString("about"), ResourceBundle
                            .getString("about_info"), null, AlertType.INFO);
                    about.setTimeout(Alert.FOREVER);
                    this.activate(about);

                    break;
                case 4:
                    // Exit

                    try
                    {
                        Jimm.jimm.destroyApp(true);
                    } catch (MIDletStateChangeException e)
                    {
                        // Do nothing
                    }

                    break;
                // #sijapp cond.end#

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
                Jimm.jimm.getIcqRef().requestAction(act);
            } catch (JimmException e)
            {
                JimmException.handleException(e);
                if (e.isCritical()) return;
            }

            // Activate main menu
            Jimm.jimm.getContactListRef().activate();

        }

    }

}
