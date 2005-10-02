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
        this.searchForm = new SearchForm();
        
        this.results = new Vector();
    }
    
    // Add a result to the results vector
    public void addResult(String uin, String nick, String name, String email, String auth, int status, String gender,
            int age)
    {
        SearchResult result = new SearchResult(uin,nick,name,email,auth,status,gender,age);
        this.results.addElement(result);
    }
    
    // Return a result object by given Nr
    public SearchResult getResult(int nr)
    {
        return (SearchResult) results.elementAt(nr);
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
        return this.searchForm;
    }

    /** ************************************************************************* */
    /** ************************************************************************* */
    /** ************************************************************************* */
    
    // Class for search result entries
    private class SearchResult
    {
        
        // Return types
        public static final int FIELD_UIN	  = 1;
        public static final int FIELD_NICK    = 2;
        public static final int FIELD_NAME    = 3;
        public static final int FIELD_EMAIL   = 4;
        public static final int FIELD_STATUS  = 5;
        public static final int FIELD_AUTH    = 6;
        public static final int FIELD_GENDER  = 7;
        public static final int FIELD_AGE     = 8;
        
        // Results
        private String uin;
        private String nick;
        private String name;
        private String email;
        private String auth;
        private int status;
        private String gender;
        private int age;
        
        public SearchResult(String _uin,String _nick,String _name,String _email,String _auth,int _status,String _gender,int _age)
        {
            uin = _uin;
            nick = _nick;
            name = _name;
            email = _email;
            auth = _auth;
            status = _status;
            gender = _gender;
            age = _age;
            
        }
        
        // Return given String value
        public String getStringValue(int value)
        {
            switch (value)
            {
            case FIELD_UIN: return uin; 
            case FIELD_NICK: return nick;
            case FIELD_NAME: return name;
            case FIELD_EMAIL: return email;
            case FIELD_AUTH: return auth;
            case FIELD_GENDER: return gender;
            case FIELD_AGE: return (age == 0) ? null : Integer.toString(age);
            default: return "";
            }
        }
        
        // Return given int value
        public int getStatus()
        {
            return status; 
        }
    
    }
    
    /** ************************************************************************* */
    /** ************************************************************************* */
    /** ************************************************************************* */
    
    // Class for show user info and react to left or right keys 
    private class SearchTextList extends TextList implements VirtualListCommands  
	{
    	public SearchTextList()
    	{
    		super(null);
    		setCursorMode(SEL_NONE);
    		setVLCommands(this);
    	}
    	
    	public void onKeyPress(VirtualList sender, int keyCode) 
    	{
    		switch (getGameAction(keyCode))
			{
    		case Canvas.LEFT:
    			searchForm.nextOrPrev(false);
    			break;
    		
    		case Canvas.RIGHT:
    			searchForm.nextOrPrev(true);
    			break;
			}
    	}
    	
    	public void onCursorMove(VirtualList sender) {}
    	public void onItemSelected(VirtualList sender) {}
	}

    // Class for the search forms
    public class SearchForm implements CommandListener
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

            this.searchForm.addCommand(this.searchCommand);
            this.searchForm.addCommand(this.backCommand);
            this.searchForm.setCommandListener(this);

            // Result Screen
            screen = new SearchTextList();
            screen.addCommand(this.previousCommand);
            screen.addCommand(this.nextCommand);
            screen.addCommand(this.addCommand);
        }

        // Activate search form
        public void activate(boolean result)
        {
            if (result)
            {
                drawResultScreen(selectedIndex);
                Jimm.display.setCurrent(this.screen);
            } else
                Jimm.display.setCurrent(this.searchForm);
        }
        
        int bigTextIndex;
        
        private void showString(int n, String resName, int id, boolean nextLine)
        {
        	String text = Search.this.getResult(n).getStringValue(id);
        	if (text == null)  return;
        	if (text.trim().length() == 0) return;
        	String name = ResourceBundle.getString(resName)+": ";
        	screen.addBigText(name, 0x0, Font.STYLE_BOLD, bigTextIndex);
        	if (nextLine) screen.doCRLF(); 
        	screen.addBigText(text, 0x0000ff, Font.STYLE_PLAIN, bigTextIndex);
        	screen.doCRLF();
        	bigTextIndex++;
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
                
                // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                screen.setFullScreenMode(false);
                screen.setTitle(ResourceBundle.getString("results")+" "+Integer.toString(n+1) + "/" + Integer.toString(Search.this.size()) );
                // #sijapp cond.else #
                screen.setCaption(ResourceBundle.getString("results")+" "+Integer.toString(n+1) + "/" + Integer.toString(Search.this.size()) );
                // #sijapp cond.end#
                
                screen.setFontSize(Font.SIZE_MEDIUM);
                bigTextIndex = 0;
                
                // UIN
                showString(n, "uin", SearchResult.FIELD_UIN, false);
                
                // Nick
                showString(n, "nick", SearchResult.FIELD_NICK, false);
                
                // Name
                showString(n, "name", SearchResult.FIELD_NAME, true);
                
                // EMail
                showString(n, "email", SearchResult.FIELD_EMAIL, true);
                
                // Auth
                showString(n, "auth", SearchResult.FIELD_AUTH, false);
                
                // Gender
                showString(n, "gender", SearchResult.FIELD_GENDER, false);
                
                // Age
                showString(n, "age", SearchResult.FIELD_AGE, false);
                
				// Draw status image
                int stat = Search.this.getResult(n).getStatus();
                int imgIndex = 0;
                if (stat == 0) imgIndex = 6;
                else if (stat == 1) imgIndex = 7;
                else if (stat == 2) imgIndex = 3;
                
                screen
					.addBigText(ResourceBundle.getString("status") + ": ",0x0,Font.STYLE_BOLD, bigTextIndex++)
					.addImage(ContactList.getImageList().elementAt(imgIndex), null)
					.doCRLF();
                screen.unlock();
            } else
            {
                // Show a result entry
            	
            	screen.lock();
            	// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                screen.setFullScreenMode(false);
                screen.setTitle(ResourceBundle.getString("results")+" 0/0");
                // #sijapp cond.else #
                screen.setCaption(ResourceBundle.getString("results")+" 0/0");
                // #sijapp cond.end#
            	
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

        public void commandAction(Command c, Displayable d)
        {
            if (c == this.backCommand) Jimm.jimm.getMainMenuRef().activate();
            else if (c == this.searchCommand)
            {
                // Display splash canvas
                SplashCanvas wait = Jimm.jimm.getSplashCanvasRef();
                wait.setMessage(ResourceBundle.getString("wait"));
                wait.setProgress(0);
                Jimm.display.setCurrent(wait);
                
                selectedIndex = 0;

                Search.this.setSearchRequest(this.uinSearchTextBox.getString(), this.nickSearchTextBox.getString(),
                        this.firstnameSearchTextBox.getString(), this.lastnameSearchTextBox.getString(),
						this.emailSearchTextBox.getString(), this.citySearchTextBox.getString(),
						this.keywordSearchTextBox.getString(), this.gender.getSelectedIndex(),
						this.onlyOnline.isSelected(0));

                SearchAction act = new SearchAction(Search.this,SearchAction.CALLED_BY_SEARCHUSER);
                try
                {
                    Jimm.jimm.getIcqRef().requestAction(act);

                } catch (JimmException e)
                {
                    JimmException.handleException(e);
                    if (e.isCritical()) return;
                }
                
//              // Start timer
                Jimm.jimm.getTimerRef().schedule(new SplashCanvas.SearchTimerTask(act), 1000, 1000);

            }
            else if (c == this.nextCommand) nextOrPrev(true);
            else if (c == this.previousCommand) nextOrPrev(false);
            else if (c == this.addCommand && d == screen)
            {
                if (Jimm.jimm.getContactListRef().getGroupItems().length == 0)
                {
                    Jimm.jimm.getMainMenuRef().addUserOrGroupCmd(null, false);
                    Alert errorMsg = new Alert(ResourceBundle.getString("warning"), JimmException.getErrDesc(161, 0), null, AlertType.WARNING);
                    errorMsg.setTimeout(Alert.FOREVER);
                    Jimm.display.setCurrent(errorMsg,Jimm.jimm.getMainMenuRef().addUserOrGroup);
                } else
                {
                    // Show list of groups to select which group to add to
                    groupList = new List(ResourceBundle.getString("whichgroup"), List.EXCLUSIVE);
                    for (int i = 0; i < Jimm.jimm.getContactListRef().getGroupItems().length; i++)
                    {
                        groupList.append(Jimm.jimm.getContactListRef().getGroupItems()[i].getName(), null);
                    }
                    groupList.addCommand(backCommand);
                    groupList.addCommand(addCommand);
                    groupList.setCommandListener(this);
                    Jimm.display.setCurrent(groupList);
                }
            } else if (c == this.addCommand && d == this.groupList)
            {
                ContactListContactItem cItem = new ContactListContactItem(Jimm.jimm.getContactListRef()
                        .getGroupItems()[this.groupList.getSelectedIndex()].getId(), Search.this.getResult(selectedIndex).getStringValue(SearchResult.FIELD_UIN),
                        Search.this.getResult(selectedIndex).getStringValue(SearchResult.FIELD_NICK), false, false);
                cItem.setBoolValue(ContactListContactItem.VALUE_IS_TEMP,true);
                cItem.setStatus(Search.this.getResult(selectedIndex).getStatus());
                Jimm.jimm.getIcqRef().addToContactList(cItem);

            }
        }
    }

}

