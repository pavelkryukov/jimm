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
 File: src/jimm/Search.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimm;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;

import jimm.comm.SearchAction;
import jimm.comm.Icq;
import jimm.util.ResourceBundle;
import DrawControls.*;

public class Search
{
    private SearchForm searchForm;

    // Request
    private String reqUin;
    private String reqNick;
    private String reqFirstname;
    private String reqLastname;
    private String reqEmail;
    private String reqCity;
    private String reqKeyword;
    private int gender;
    private boolean onlyOnline;
    
    // Results
    private Vector results;

    // Constructor
    public Search()
    {
    	this.results = new Vector();
    }
    
    // Add a result to the results vector
    public void addResult(String uin, String nick, String name, String email, String auth, int status, String gender,
            int age)
    {
    	String[] resultData = new String[JimmUI.UI_LAST_ID];
   	
    	resultData[JimmUI.UI_UIN]    = uin;
    	resultData[JimmUI.UI_NICK]   = nick;
    	resultData[JimmUI.UI_NAME]   = name;
    	resultData[JimmUI.UI_EMAIL]  = email;
    	resultData[JimmUI.UI_AUTH]   = auth;
    	resultData[JimmUI.UI_STATUS] = Integer.toString(status);
    	resultData[JimmUI.UI_GENDER] = gender;
    	resultData[JimmUI.UI_AGE]    = Integer.toString(age);
    	
        this.results.addElement(resultData);
    }
    
    // Return a result object by given Nr
    public String[] getResult(int nr)
    {
        return (String[]) results.elementAt(nr);
    }

    // Set a search request
    public void setSearchRequest(String uin, String nick, String firstname, String lastname, String email,
    		String city, String keyword, int gender, boolean onlyOnline)
    {
        this.reqUin = uin;
        this.reqNick = nick;
        this.reqFirstname = firstname;
        this.reqLastname = lastname;
        this.gender = gender;
        this.reqEmail = email;
        this.reqCity = city;
        this.reqKeyword = keyword;
        this.onlyOnline = onlyOnline;
    }

    // Returns data from the TextFields as an array
    public String[] getSearchRequest()
    {
        String request[] = new String[9];
        request[0] = reqUin;
        request[1] = reqNick;
        request[2] = reqFirstname;
        request[3] = reqLastname;
        request[4] = reqEmail;
        request[5] = reqCity;
        request[6] = reqKeyword;
        request[7] = "" + gender;
        if (onlyOnline)
            request[8] = "1";
        else
            request[8] = "0";
        return request;
    }

    // Return size of search results
    public int size()
    {
        return results.size();
    }

    // Return the SearchForm object
    public SearchForm getSearchForm()
    {
    	if (searchForm == null) searchForm = new SearchForm();
        return searchForm;
    }

    
    /** ************************************************************************* */
    /** ************************************************************************* */
    /** ************************************************************************* */
    
    // Class for the search forms
    public class SearchForm implements CommandListener, VirtualListCommands
    {
        // Commands
        private Command backCommand;
        private Command searchCommand;
        private Command addCommand;
        private Command previousCommand;
        private Command nextCommand;

        // Forms for results and query
        private Form searchForm;
        private TextList screen;

        // List for group selection
        private List groupList;

        // Textboxes for search
        private TextField uinSearchTextBox;
        private TextField nickSearchTextBox;
        private TextField firstnameSearchTextBox;
        private TextField lastnameSearchTextBox;
        private TextField emailSearchTextBox;
        private TextField citySearchTextBox;
        private TextField keywordSearchTextBox;
        
        // Choice boxes for gender and online choice
        private ChoiceGroup gender;
        private ChoiceGroup onlyOnline;

        // Selectet index in result screen
        int selectedIndex;

        // constructor for search form
        public SearchForm()
        {
            // Commands
            this.searchCommand = new Command(ResourceBundle.getString("search_user"), Command.OK, 1);
            this.backCommand = new Command(ResourceBundle.getString("back"), Command.BACK, 2);
            this.addCommand = new Command(ResourceBundle.getString("add_to_list"), Command.ITEM, 3);
            this.previousCommand = new Command(ResourceBundle.getString("prev"), Command.ITEM, 2);
            this.nextCommand = new Command(ResourceBundle.getString("next"), Command.ITEM, 1);

            // Form
            this.searchForm = new Form(ResourceBundle.getString("search_user"));

            // TextFields
            this.uinSearchTextBox = new TextField(ResourceBundle.getString("uin"), "", 32,
                    TextField.NUMERIC);
            this.nickSearchTextBox = new TextField(ResourceBundle.getString("nick"), "", 32,
                    TextField.ANY);
            this.firstnameSearchTextBox = new TextField(ResourceBundle.getString("firstname"), "", 32,
                    TextField.ANY);
            this.lastnameSearchTextBox = new TextField(ResourceBundle.getString("lastname"), "", 32,
                    TextField.ANY);
            this.emailSearchTextBox = new TextField(ResourceBundle.getString("email"), "", 32,
                    TextField.EMAILADDR);
            this.citySearchTextBox = new TextField(ResourceBundle.getString("city"), "", 32,
                    TextField.ANY);
            this.keywordSearchTextBox = new TextField(ResourceBundle.getString("keyword"), "", 32,
                    TextField.ANY);
            
            // Choice Groups
            this.gender = new ChoiceGroup(ResourceBundle.getString("gender"),Choice.EXCLUSIVE);
            this.gender.append(ResourceBundle.getString("female_male"),null);
            this.gender.append(ResourceBundle.getString("female"),null);
            this.gender.append(ResourceBundle.getString("male"),null);
            this.onlyOnline = new ChoiceGroup("",Choice.MULTIPLE);
            this.onlyOnline.append(ResourceBundle.getString("only_online"),null);

            this.searchForm.append(this.onlyOnline);
            this.searchForm.append(this.uinSearchTextBox);
            this.searchForm.append(this.nickSearchTextBox);
            this.searchForm.append(this.firstnameSearchTextBox);
            this.searchForm.append(this.lastnameSearchTextBox);
            this.searchForm.append(this.gender);
            this.searchForm.append(this.emailSearchTextBox);
            this.searchForm.append(this.citySearchTextBox);
            this.searchForm.append(this.keywordSearchTextBox);
            this.searchForm.setCommandListener(this);

            // Result Screen
            screen = new TextList(null);
            screen.setVLCommands(this);
            screen.addCommand(this.previousCommand);
            screen.addCommand(this.nextCommand);
            screen.addCommand(this.addCommand);
            screen.setCursorMode(TextList.SEL_NONE);
            JimmUI.setColorScheme(screen);
        }
        
        // Activate search form
        public void activate(boolean result)
        {
            if (result)
            {
                drawResultScreen(selectedIndex);
                Jimm.display.setCurrent(this.screen);
            }
            else
            {
                this.searchForm.addCommand(this.searchCommand);
                this.searchForm.addCommand(this.backCommand);
                Jimm.display.setCurrent(this.searchForm);
            }
        }
        
        public void drawResultScreen(int n)
        {
            // Remove the older entrys here
            screen.clear();

            if (Search.this.size() > 0)
            {

                if (Search.this.size() == 1)
                {
                    screen.removeCommand(this.nextCommand);
                    screen.removeCommand(this.previousCommand);
                }
                
                screen.lock();
                
                JimmUI.fillUserInfo(getResult(n), screen);
                screen.setCaption( ResourceBundle.getString("results")+" "+Integer.toString(n+1) + "/" + Integer.toString(Search.this.size()) );
                screen.unlock();
            }
            else
            {
                // Show a result entry
            	
            	screen.lock();
            	screen.setCaption(ResourceBundle.getString("results")+" 0/0");
                screen.addBigText(ResourceBundle.getString("no_results")+": ",0x0,Font.STYLE_BOLD, -1);
                screen.unlock();
            }

            screen.addCommand(this.backCommand);

            screen.setCommandListener(this);
        }
        
        public void nextOrPrev(boolean next)
        {
            if (next)
            {
                selectedIndex = (selectedIndex + 1) % Search.this.size();
                this.activate(true);
            }
            else
            {
                if (selectedIndex == 0)
                    selectedIndex = Search.this.size() - 1;
                else
                {
                    selectedIndex = (selectedIndex - 1) % Search.this.size();
                }
                this.activate(true);
            }
      	
        }
        
    	public void onKeyPress(VirtualList sender, int keyCode,int type) 
    	{
		if (type == VirtualList.KEY_PRESSED)
    	{
    		switch (sender.getGameAction(keyCode))
			{
    		case Canvas.LEFT:
    			nextOrPrev(false);
    			break;
    		
    		case Canvas.RIGHT:
    			nextOrPrev(true);
    			break;
			}
    	}
    	}
    	
    	public void onCursorMove(VirtualList sender) {}
    	public void onItemSelected(VirtualList sender) {}

        public void commandAction(Command c, Displayable d)
        {
            if (c == this.backCommand)
            {
            	if (d == screen) activate(false);
            	else
            	{
            		searchForm = null;
            		MainMenu.activate();
            	}
            }
            else if (c == this.searchCommand)
            {
                selectedIndex = 0;

                Search.this.setSearchRequest(this.uinSearchTextBox.getString(), this.nickSearchTextBox.getString(),
                        this.firstnameSearchTextBox.getString(), this.lastnameSearchTextBox.getString(),
						this.emailSearchTextBox.getString(), this.citySearchTextBox.getString(),
						this.keywordSearchTextBox.getString(), this.gender.getSelectedIndex(),
						this.onlyOnline.isSelected(0));

                SearchAction act = new SearchAction(Search.this,SearchAction.CALLED_BY_SEARCHUSER);
                try
                {
                    Icq.requestAction(act);

                } catch (JimmException e)
                {
                    JimmException.handleException(e);
                    if (e.isCritical()) return;
                }
                
                // Start timer 
                SplashCanvas.addTimerTask("wait", act, 0, 0, false);
            }
            else if (c == this.nextCommand) nextOrPrev(true);
            else if (c == this.previousCommand) nextOrPrev(false);
            else if (c == this.addCommand && d == screen)
            {
            	searchForm = null;
                if (ContactList.getGroupItems().length == 0)
                {
                    MainMenu.addUserOrGroupCmd(null, false);
                    Alert errorMsg = new Alert(ResourceBundle.getString("warning"), JimmException.getErrDesc(161, 0), null, AlertType.WARNING);
                    errorMsg.setTimeout(Alert.FOREVER);
                    Jimm.display.setCurrent(errorMsg,MainMenu.addUserOrGroup);
                } else
                {
                    // Show list of groups to select which group to add to
                    groupList = new List(ResourceBundle.getString("whichgroup"), List.EXCLUSIVE);
                    for (int i = 0; i < ContactList.getGroupItems().length; i++)
                    {
                        groupList.append(ContactList.getGroupItems()[i].getName(), null);
                    }
                    groupList.addCommand(backCommand);
                    groupList.addCommand(addCommand);
                    groupList.setCommandListener(this);
                    Jimm.display.setCurrent(groupList);
                }
            } else if (c == this.addCommand && d == this.groupList)
            {
            	String[] resultData = getResult(selectedIndex);
                ContactListContactItem 
                	cItem = new ContactListContactItem(-1,
                			ContactList.getGroupItems()[groupList.getSelectedIndex()].getId(),
                			resultData[JimmUI.UI_UIN],
                			resultData[JimmUI.UI_NICK],
                        false, false);
                cItem.setBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP,true);
                cItem.setIntValue(ContactListContactItem.CONTACTITEM_STATUS, Integer.parseInt(resultData[JimmUI.UI_STATUS]));
                Icq.addToContactList(cItem);
            }
        }
    }

}

