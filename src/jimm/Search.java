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
 File: src/jimm/Options.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimm;

import java.util.Vector;

import jimm.comm.SearchAction;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;

public class Search
{

    private SearchForm searchForm;

    // Results
    private Vector uin;
    private Vector nick;
    private Vector name;
    private Vector email;
    private Vector auth;
    private Vector status;
    private Vector gender;
    private Vector age;

    // Request
    private String reqUin;
    private String reqNick;
    private String reqFirstname;
    private String reqLastname;
    private String reqEmail;
    private String reqCity;
    private String reqKeyword;
    private boolean onlyOnline;

    // Constructor
    public Search()
    {
        this.uin = new Vector();
        this.nick = new Vector();
        this.name = new Vector();
        this.email = new Vector();
        this.auth = new Vector();
        this.status = new Vector();
        this.gender = new Vector();
        this.age = new Vector();

        this.searchForm = new SearchForm();
    }

    // Return the nick for SearchResult at index i
    public String getNick(int i)
    {
        return (String) this.nick.elementAt(i);
    }

    // Return the uin for SearchResult at index i
    public String getUIN(int i)
    {
        return (String) this.uin.elementAt(i);
    }

    // Return the auth String for SearchResult at index i
    public String getAuthString(int i)
    {
        return (String) this.auth.elementAt(i);
    }

    // Return the name for SearchResult at index i
    public String getName(int i)
    {
        return (String) this.name.elementAt(i);
    }

    // Return the email for SearchResult at index i
    public String getEmail(int i)
    {
        return (String) this.email.elementAt(i);
    }

    // Return the gender string for SearchResult at index i
    public String getGender(int i)
    {
        return (String) this.gender.elementAt(i);
    }

    // Return the age for SearchResult at index i
    public String getAge(int i)
    {
        return new String(this.age.elementAt(i).toString());
    }
    
    // Return the status for SearchResult at index i
    public Integer getStatus(int i)
    {
        return (Integer) this.status.elementAt(i);
    }

    // Add a result to the results vector
    public void addResult(String uin, String nick, String name, String email, String auth, int status, String gender,
            int age)
    {
        this.uin.addElement(uin);
        this.nick.addElement(nick);
        this.name.addElement(name);
        this.email.addElement(email);
        this.auth.addElement(auth);
        this.status.addElement(new Integer(status));
        this.gender.addElement(gender);
        this.age.addElement(new Integer(age));
    }

    // Set a search request
    public void setSearchRequest(String uin, String nick, String firstname, String lastname, String email, String city,
            String keyword,boolean onlyOnline)
    {
        this.reqUin = uin;
        this.reqNick = nick;
        this.reqFirstname = firstname;
        this.reqLastname = lastname;
        this.reqEmail = email;
        this.reqCity = city;
        this.reqKeyword = keyword;
        this.onlyOnline = onlyOnline;
    }

    // Returns data from the TextFields as an array
    public String[] getSearchRequest()
    {
        String request[] = new String[8];
        request[0] = reqUin;
        request[1] = reqNick;
        request[2] = reqFirstname;
        request[3] = reqLastname;
        request[4] = reqEmail;
        request[5] = reqCity;
        request[6] = reqKeyword;
        if (onlyOnline)
            request[7] = "1";
        else
            request[7] = "0";
        return request;
    }

    // Return size of search results
    public int size()
    {
        return uin.size();
    }

    // Return the SearchForm object
    public SearchForm getSearchForm()
    {
        return this.searchForm;
    }

    /** ************************************************************************* */
    /** ************************************************************************* */
    /** ************************************************************************* */

    // Class for the search forms
    public class SearchForm implements CommandListener
    {

        // Commands
        private Command backCommand;
        private Command searchCommand;
        private Command addCommand;
        private Command previousComamnd;
        private Command nextCommand;

        // Forms for results and query
        private Form searchForm;
        private Form resultScreen;

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
        
        // Choice box for online choice
        private ChoiceGroup onlyOnline;

        // Selectet index in result screen
        int selectedIndex;

        // constructor for search form
        public SearchForm()
        {
            // Commands
            this.searchCommand = new Command(ResourceBundle.getString("search_user"), Command.OK, 1);
            this.backCommand = new Command(ResourceBundle.getString("back"), Command.BACK, 2);
            this.addCommand = new Command(ResourceBundle.getString("add_to_list"), Command.ITEM, 2);
            this.previousComamnd = new Command(ResourceBundle.getString("prev"), Command.ITEM, 1);
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
            
            // Choice Group
            this.onlyOnline = new ChoiceGroup("",Choice.MULTIPLE);
            this.onlyOnline.append(ResourceBundle.getString("only_online"),null);

            this.searchForm.append(this.onlyOnline);
            this.searchForm.append(this.uinSearchTextBox);
            this.searchForm.append(this.nickSearchTextBox);
            this.searchForm.append(this.firstnameSearchTextBox);
            this.searchForm.append(this.lastnameSearchTextBox);
            this.searchForm.append(this.emailSearchTextBox);
            this.searchForm.append(this.citySearchTextBox);
            this.searchForm.append(this.keywordSearchTextBox);

            this.searchForm.addCommand(this.searchCommand);
            this.searchForm.addCommand(this.backCommand);
            this.searchForm.setCommandListener(this);

            // Result Screen
            resultScreen = new Form(ResourceBundle.getString("results"));

            resultScreen.addCommand(this.previousComamnd);
            resultScreen.addCommand(this.nextCommand);
            resultScreen.addCommand(this.addCommand);

        }

        // Activate search form
        public void activate(boolean result)
        {
            if (result)
            {
                drawResultScreen(selectedIndex);
                Jimm.display.setCurrent(this.resultScreen);
            } else
                Jimm.display.setCurrent(this.searchForm);
        }

        public void drawResultScreen(int n)
        {

            // Fonts used for result screen
            Font typeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
            Font contentFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

            // Image used to print a result item in
            Image resultImage;
            Image copy;
            Graphics g;

            // Remove the older entrys here
            if (resultScreen.size() > 0) resultScreen.delete(0);

            if (Search.this.size() > 0)
            {

                if (Search.this.size() == 1)
                {
                    resultScreen.removeCommand(this.nextCommand);
                    resultScreen.removeCommand(this.previousComamnd);
                }
                
                // Which line we are writing to?
                int lineMarker = 0;

                // Image used to print a result item in
                resultImage = Image.createImage(120, typeFont.getHeight() * 5 + 4 + 18);
                g = resultImage.getGraphics();

                // Draw a result entry
                
                // First draw the field markers in a different font
                g.setColor(0, 0, 0);
                g.setFont(typeFont);
                
                // Draw "Nr.: "
                g.drawString(ResourceBundle.getString("nr") + ".: ", 0, 2, Graphics.TOP | Graphics.LEFT);
                // Draw "UIN: "
                g.drawString(" " + ResourceBundle.getString("uin") + ": ", typeFont.stringWidth(ResourceBundle.getString("nr")+ ".: ") + contentFont.stringWidth(new Integer(n + 1).toString() + "/" + new Integer(Search.this.size()).toString()), 2, Graphics.TOP | Graphics.LEFT);
                lineMarker+=1;
                
                // Draw "Nick: "
                g.drawString(ResourceBundle.getString("nick") + ": ", 0, (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                // Draw "Auth: "
                g.drawString(ResourceBundle.getString("auth") + ": ", typeFont.stringWidth(ResourceBundle.getString("nick") + ": ") + contentFont.stringWidth(Search.this.getNick(n)) + 3, (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                lineMarker += 1;
                
                // Draw "Name: "
                g.drawString(ResourceBundle.getString("name") + ": ", 0, (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                lineMarker+=1;
                
                // Draw "Email: " only of there is an email adress
                if (Search.this.getEmail(n).length() > 0)
                {
                    g.drawString(ResourceBundle.getString("email") + ": ", 0, (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                    lineMarker+=1;
                }
                
                // Draw "Gender: "
                g.drawString(ResourceBundle.getString("gender") + ": ", 0, (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                // Draw "Age: "
                g.drawString(ResourceBundle.getString("age") + ": ", typeFont.stringWidth(ResourceBundle.getString("gender") + ": ") + contentFont.stringWidth(Search.this.getGender(n)) + 3, (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);

                g.setFont(contentFont);
                g.setColor(0, 0, 255);
                
                lineMarker = 0;

                // Draw counter
                g.drawString(new Integer(n + 1).toString() + "/" + new Integer(Search.this.size()).toString(), typeFont.stringWidth(ResourceBundle.getString("nr") + ".: "), 2, Graphics.TOP | Graphics.LEFT);
                // Draw UIN
                g.drawString(Search.this.getUIN(n), typeFont.stringWidth(ResourceBundle.getString("nr") + ".: ") + contentFont.stringWidth(new Integer(n + 1).toString() + "/" + new Integer(Search.this.size())) + typeFont.stringWidth(" " + ResourceBundle.getString("uin") + ": "), 2, Graphics.TOP | Graphics.LEFT);
                lineMarker+=1;
                
                // Draw nick
                g.drawString(Search.this.getNick(n), typeFont.stringWidth(ResourceBundle.getString("nick") + ": "),(typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                // Draw auth string
                g.drawString(Search.this.getAuthString(n), typeFont.stringWidth(ResourceBundle.getString("nick") + ": ") + contentFont.stringWidth(Search.this.getNick(n)) + typeFont.stringWidth(ResourceBundle.getString("nick") + ": "), (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                lineMarker+=1;
                
                // Draw name
                g.drawString(Search.this.getName(n), typeFont.stringWidth(ResourceBundle.getString("name") + ": "), (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                lineMarker+=1;
                
                // Draw email adress if there is one
                if (Search.this.getEmail(n).length() > 0)
                {
                    g.drawString(Search.this.getEmail(n), typeFont.stringWidth(ResourceBundle.getString("email") + ": "), (typeFont.getHeight() * lineMarker)+ 2, Graphics.TOP | Graphics.LEFT);
                    lineMarker+=1;
                }
                
                // Draw gender
                g.drawString(Search.this.getGender(n), typeFont.stringWidth(ResourceBundle.getString("gender") + ": "), (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                // Draw age
                g.drawString(Search.this.getAge(n), typeFont.stringWidth(ResourceBundle.getString("gender") + ": ") + contentFont.stringWidth(Search.this.getGender(n)) + typeFont.stringWidth(ResourceBundle.getString("age") + ": "), (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                lineMarker+=1;
                
                // Draw status image
                if (Search.this.getStatus(n).intValue() == 0) 
                    g.drawImage(ContactList.statusOfflineImg, 0, (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                else if (Search.this.getStatus(n).intValue() == 1)  
                    g.drawImage(ContactList.statusOnlineImg, 0, (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                else if (Search.this.getStatus(n).intValue() == 2)  
                    g.drawImage(ContactList.statusInvisibleImg, 0, (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);
                else
                    g.drawImage(Image.createImage(16, 16), 0, (typeFont.getHeight() * lineMarker) + 2, Graphics.TOP | Graphics.LEFT);

                copy = Image.createImage(resultImage);

                ImageItem item = new ImageItem(null, copy, ImageItem.LAYOUT_LEFT + ImageItem.LAYOUT_NEWLINE_BEFORE + ImageItem.LAYOUT_NEWLINE_AFTER, null);
                resultScreen.append(item);
                
            } else
            {
                // Image used to print a result item in
                resultImage = Image.createImage(120, typeFont.getHeight() + 4);
                g = resultImage.getGraphics();

                g.setColor(0, 0, 0);
                g.setFont(typeFont);
                g.drawString(ResourceBundle.getString("no_results"), 0, 2, Graphics.TOP | Graphics.LEFT);

                copy = Image.createImage(resultImage);

                ImageItem item = new ImageItem(null, copy, ImageItem.LAYOUT_LEFT + ImageItem.LAYOUT_NEWLINE_BEFORE
                        + ImageItem.LAYOUT_NEWLINE_AFTER, null);
                resultScreen.append(item);
            }

            resultScreen.addCommand(this.backCommand);

            resultScreen.setCommandListener(this);
            System.gc();
        }

        public void commandAction(Command c, Displayable d)
        {
            if (c == this.backCommand) Jimm.jimm.getMainMenuRef().activate();
            if (c == this.searchCommand)
            {
                // Display splash canvas
                SplashCanvas wait = Jimm.jimm.getSplashCanvasRef();
                wait.setMessage(ResourceBundle.getString("wait"));
                wait.setProgress(0);
                Jimm.display.setCurrent(wait);
                
                selectedIndex = 0;

                Search.this.setSearchRequest(this.uinSearchTextBox.getString(), this.nickSearchTextBox.getString(),
                        this.firstnameSearchTextBox.getString(), this.lastnameSearchTextBox.getString(),
                        this.emailSearchTextBox.getString(), this.emailSearchTextBox.getString(),
                        this.keywordSearchTextBox.getString(),this.onlyOnline.isSelected(0));

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
            if (c == this.nextCommand)
            {
                selectedIndex = (selectedIndex + 1) % Search.this.size();
                this.activate(true);
            }
            if (c == this.previousComamnd)
            {
                if (selectedIndex == 0)
                    selectedIndex = Search.this.size() - 1;
                else
                {
                    selectedIndex = (selectedIndex - 1) % Search.this.size();
                }
                this.activate(true);
            }
            if (c == this.addCommand && d == this.resultScreen)
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
            } else if (c == this.addCommand && d == this.groupList)
            {
                ContactListContactItem cItem = new ContactListContactItem(0, Jimm.jimm.getContactListRef()
                        .getGroupItems()[this.groupList.getSelectedIndex()].getId(), Search.this.getUIN(selectedIndex),
                        Search.this.getNick(selectedIndex), false, false);
                cItem.setBoolValue(ContactListContactItem.VALUE_IS_TEMP,true);

                Jimm.jimm.getIcqRef().addToContactList(cItem);

            }
        }
    }

}

