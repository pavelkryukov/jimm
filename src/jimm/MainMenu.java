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
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDletStateChangeException;

import jimm.comm.Action;
import jimm.comm.SearchAction;
import jimm.comm.SetOnlineStatusAction;
import jimm.comm.UpdateContactListAction;
import jimm.util.ResourceBundle;

public class MainMenu implements CommandListener
{
	private static final int MSGBS_EXIT = 1;
	
    // Abort command
    private static Command backCommand = new Command(ResourceBundle.getString("back"), Command.BACK, 1);
    
    // Send command
    private static Command sendCommand = new Command(ResourceBundle.getString("send"), Command.OK, 1);

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
    static public Form addUserOrGroup;
    
    // Flag if we we are adding user or group
    static private boolean addUserFlag;

    // Text box for adding users to the contact list
    static private TextField uinTextField;
    static private TextField nameTextField;

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
                this.list.append(ResourceBundle.getString("add_group"), null);
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

    // Show form for adding user
    public static void addUserOrGroupCmd(String uin,boolean userFlag)
	{
        addUserFlag = userFlag;
        // Reset and display textbox for entering uin or group name to add
        if (addUserFlag)
        {
        addUserOrGroup = new Form(ResourceBundle.getString("add_user"));
        uinTextField = new TextField(ResourceBundle.getString("uin"), uin, 16, TextField.NUMERIC);
        nameTextField = new TextField(ResourceBundle.getString("name"), "", 32, TextField.ANY);
        if (uin == null) 
            addUserOrGroup.append(uinTextField);
        else 
        {
        	StringItem si = new StringItem(ResourceBundle.getString("uin"), uin);
        	addUserOrGroup.append(si);
        }
        addUserOrGroup.append(nameTextField);
        }
        else
        {
            addUserOrGroup = new Form(ResourceBundle.getString("add_group"));
            uinTextField = new TextField(ResourceBundle.getString("group_name"), uin, 16, TextField.ANY);
            addUserOrGroup.append(uinTextField);
        }
        addUserOrGroup.addCommand(sendCommand);
        addUserOrGroup.addCommand(backCommand);
        addUserOrGroup.setCommandListener( Jimm.jimm.getMainMenuRef() );
        Jimm.display.setCurrent(addUserOrGroup);
	}
    
    private void doExit()
    {
        // Disconnect
        Jimm.jimm.getIcqRef().disconnect();
        
        // Save traffic
        //#sijapp cond.if modules_TRAFFIC is "true" #
		try 
		{
			Jimm.jimm.getTrafficRef().save();
		} 
		catch (Exception e) 
		{ // Do nothing
		} 
		//#sijapp cond.end#
	
		
		// Exit app
		try 
		{
			Jimm.jimm.destroyApp(true);
		} 
		catch (MIDletStateChangeException e) 
		{ // Do nothing 
		} 
    }
    
    Alert exitAlert;
    Command exitOkCommand, exitBackCommand;
    
    private void menuExit()
    {
   		if (/*Jimm.jimm.getContactListRef().getUnreadMessCount() > 0*/true)
   		{
   	    	Jimm.jimm.messageBox
			(
				ResourceBundle.getString("attention"),
				ResourceBundle.getString("have_unread_mess"),
				Jimm.MESBOX_YESNO,
				this,
				MSGBS_EXIT
			);
   	    	//if (result == 1) doExit();
   	    	//else Jimm.jimm.getContactListRef().activate();
   		}
   		else doExit();
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
            if ((d == this.groupList) || (d == MainMenu.addUserOrGroup) || (d == statusList))
                this.activate();
            else
                Jimm.jimm.getContactListRef().activate();
        } else if ((c == sendCommand) && (d == MainMenu.addUserOrGroup))
        {
            // Display splash canvas
            SplashCanvas wait2 = Jimm.jimm.getSplashCanvasRef();
            wait2.setMessage(ResourceBundle.getString("wait"));
            wait2.setProgress(0);
            Jimm.display.setCurrent(wait2);

            SearchAction act1;
            UpdateContactListAction act2;
            
            if (addUserFlag) // Make a search for the given UIN
            {
                Search search = new Search();
                search.setSearchRequest(uinTextField.getString(), "", "", "", "", "", "", false);

                act1 = new SearchAction(search, SearchAction.CALLED_BY_ADDUSER);
                act2 = null;
            }
            else // Add the group
            {
                ContactListGroupItem newGroup = new ContactListGroupItem(uinTextField.getString());

                act1 = null;
                act2 = new UpdateContactListAction(newGroup, true);
            }
            try
            {
                if (addUserFlag)
                    Jimm.jimm.getIcqRef().requestAction(act1);
                else
                    Jimm.jimm.getIcqRef().requestAction(act2);

            } catch (JimmException e)
            {
                JimmException.handleException(e);
                if (e.isCritical()) return;
            }

            DebugLog.addText("Send action");
            
            // Start timer
            if (addUserFlag)
                Jimm.jimm.getTimerRef().schedule(new SplashCanvas.SearchTimerTask(act1), 1000, 1000);
            else
                Jimm.jimm.getTimerRef().schedule(new SplashCanvas.UpdateContactListTimerTask(act2), 1000, 1000);
        }
        
        // User select OK in exit questiom message box
        else if (Jimm.jimm.isMsgBoxCommand(c, MSGBS_EXIT) == 1)
        {
        	doExit();
        }
        
        // User select CANCEL in exit questiom message box
        else if (Jimm.jimm.isMsgBoxCommand(c, MSGBS_EXIT) == 2)
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
                	addUserOrGroupCmd(null,true);
                    break;
                    
                case 4: // Search for User
                    Search searchf = new Search();
                    searchf.getSearchForm().activate(false);
                    break;
                case 5: // Add group
                    addUserOrGroupCmd(null,false);
                    break;
                    
                case 6:
                    // Options
                    Jimm.jimm.getOptionsRef().optionsForm.activate();

                    break;
                // #sijapp cond.if modules_TRAFFIC is "true" #
                case 7:
                    // Traffic

                    // Display an traffic alert
                    traffic.setIsActive(true);
                    traffic.trafficScreen.activate();

                    break;
                case 8:
                    // About

                    // Display an info alert
                    Alert about = new Alert(ResourceBundle.getString("about"), ResourceBundle
                            .getString("about_info"), null, AlertType.INFO);
                    about.setTimeout(Alert.FOREVER);
                    Jimm.jimm.getContactListRef().activate(about);

                    break;
                case 9:
                    // Exit
                	menuExit();

                    break;
                // #sijapp cond.else#
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
             	
                	menuExit();
                    break;

                // #sijapp cond.end#
                }
            } else
            {
                switch (this.list.getSelectedIndex())
                {
                case 0:
                    // Connect
                	Jimm.jimm.getContactListRef().beforeConnect();
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
					
					menuExit();
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
