/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-08  Jimm Project

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
 File: src/jimm/PhoneBook.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Mikitevich Ivan
 *******************************************************************************/
//#sijapp cond.if modules_PIM is "true" #
package jimm;

import java.util.*;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.*;
import javax.microedition.pim.*;

import javax.wireless.messaging.*;

import DrawControls.TextList;
import DrawControls.VirtualList;

import jimm.comm.Util;
import jimm.util.ResourceBundle;

public class PhoneBook implements CommandListener {

    private static PhoneBook _this;
    private static TextList list;
    private static TextBox smsText;
    private static TextBox numText;
    private static MessageConnection connection;
    private static TelNum[] allTelNum;
    private static Vector data = new Vector();
    private static final Command backCommand = new Command(ResourceBundle.getString("back"), Jimm.cmdBack, 1);
    private static final Command cmdCall = new Command(ResourceBundle.getString("call"), Command.ITEM, 4);
    private static final Command cmdWriteSMS = new Command(ResourceBundle.getString("write_sms"), Command.ITEM, 5);
    private static final Command cmdSendSMS = new Command(ResourceBundle.getString("send_sms"), Command.ITEM, 1);
    private static final Command cmdEditNum = new Command(ResourceBundle.getString("edit_num"), Command.ITEM, 6);
    static int counter = 0;

    public PhoneBook() {
	_this = this;
    }

    public static void addContactToList(String name, String number, int value) {
	synchronized (list) {
	    list.addBigText(name + ":", list.getTextColor(), Font.STYLE_BOLD, value);
	    list.doCRLF(value);
	    list.addBigText(number, Options.getSchemeColor(Options.CLRSCHHEME_OUTGOING, -1), Font.STYLE_PLAIN, value);
	    list.doCRLF(value);
	}
    }

    private static void fillPhonesInList() {
	list.lock();
	list.clear();
	for (int i = 0; i < allTelNum.length; i++) {
	    addContactToList(allTelNum[i].getName(), allTelNum[i].getTel(), allTelNum[i].getValue());
	}
	list.unlock();
    }

    private static void addTelNumInfo(Vector vct, String name, String tel, int value) {
	vct.addElement(new TelNum(name, tel, value));
    }

    public static void activate(boolean restorePos) {
	String cap = null;
	int lastPos = -1;

	if (list == null) {
	    list = new TextList(ResourceBundle.getString(cap));
	}

	if (restorePos) {
	    lastPos = list.getCurrTextIndex();
	}

	list.setMode(VirtualList.CURSOR_MODE_DISABLED);
	list.setCaption(ResourceBundle.getString("phone_book"));
	JimmUI.setColorScheme(list, false, -1, true);
	list.setCyclingCursor(true);
	list.addCommandEx(backCommand, VirtualList.MENU_TYPE_LEFT_BAR);
	list.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
	list.addCommandEx(cmdCall, VirtualList.MENU_TYPE_RIGHT);
	list.addCommandEx(cmdWriteSMS, VirtualList.MENU_TYPE_RIGHT);
	list.addCommandEx(cmdEditNum, VirtualList.MENU_TYPE_RIGHT);
	list.setCommandListener(_this);

	if (counter == 0) {
	    addTelNumInfo(data, ResourceBundle.getString("enter_phone_num"), "", counter);
	    counter++;
	    // load the list of names in a different thread
	    new Thread(new LoadContacts()).start();
	} else {
	    fillPhonesInList();
	}
	if (restorePos) {
	    list.selectTextByIndex(lastPos);
	}
	list.activate(Jimm.display);
    }

    private String getCurrPhoneNumber() {
	return allTelNum[list.getCurrTextIndex()].getTel();
    }

    private void setCurrPhoneNumber(String number) {
	allTelNum[list.getCurrTextIndex()].setTel(number);
    }

    /**
     * Sets the destination address and payload text for the text SMS.
     */
    private TextMessage prepareSMS(String number) {
	TextMessage message = (TextMessage) connection.newMessage(
		MessageConnection.TEXT_MESSAGE);
	message.setAddress("sms://" + number);
	String text = smsText.getString();
	message.setPayloadText(text);

	return message;
    }

    /**
     * Sends a text SMS.
     */
    private void sendSMS(final TextMessage message) {
	Thread smsThread = new Thread() {

	    public void run() {
		try {
		    connection.send(message);
		} catch (InterruptedIOException ex) {
		    // TODO: Exception (e.g. timeout) handling
		    } catch (IOException ex) {
		    // TODO: Exception (e.g. network failure) handling
		    } catch (IllegalArgumentException ex) {
		    // TODO: Exception (e.g. too big or otherwise invalid
		    // message) handling
		    } catch (SecurityException ex) {
		    // TODO: Exception (e.g. insufficient permissions) handling
		    }
	    }
	};
	smsThread.start();
    }

    public void commandAction(Command c, Displayable d) {
	Jimm.aaUserActivity();

	if (c == backCommand) {
	    RunnableImpl.backToLastScreenMT();

	} else if (c == cmdCall) {
	    String number = getCurrPhoneNumber();
	    if (number.length() > 0) {
		try {
		    Jimm.jimm.platformRequest("tel:" + number);
		} catch (Exception ignore) {/*Do nothing*/

		}
	    }

	} else if (c == cmdWriteSMS) {
	    String number = getCurrPhoneNumber();
	    smsText = new TextBox(null, "", 500, TextField.ANY);
	    smsText.addCommand(cmdSendSMS);
	    smsText.addCommand(backCommand);
	    smsText.setCommandListener(this);
	    Jimm.display.setCurrent(smsText);

	} else if (c == cmdSendSMS) {
	    String number = getCurrPhoneNumber();
	    if (number.length() > 0) {
		RunnableImpl.backToLastScreenMT();

		try {
		    connection = (MessageConnection) Connector.open("sms://:5000", Connector.WRITE);
		} catch (IOException ex) {
		    // TODO: Exception handling
		}

		TextMessage message = prepareSMS(number);
		sendSMS(message);
	    }

	} else if (c == cmdEditNum && list.getCurrTextIndex() == 0) {
	    String number = getCurrPhoneNumber();
	    numText = new TextBox(null, number, 20, TextField.PHONENUMBER);
	    numText.addCommand(JimmUI.cmdOk);
	    numText.addCommand(JimmUI.cmdCancel);
	    numText.setCommandListener(this);
	    Jimm.display.setCurrent(numText);

	} else if (numText != null && numText.isShown()) {
	    if (c == JimmUI.cmdOk) {
		setCurrPhoneNumber(numText.getString());
	    }
	    PhoneBook.activate(true);
	    numText = null;
	}
    }

    private static void loadNames(String name)
	    throws PIMException, SecurityException {
	javax.microedition.pim.ContactList cl = null;
	try {
	    cl = (javax.microedition.pim.ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY, name);

	    if (cl.isSupportedField(Contact.FORMATTED_NAME) && cl.isSupportedField(Contact.TEL)) {
		// TODO: Progress bar here
		Enumeration items = cl.items();
		Vector telNumbers = new Vector();
		while (items.hasMoreElements()) {
		    Contact contact = (Contact) items.nextElement();
		    int telCount = contact.countValues(Contact.TEL);
		    int nameCount = contact.countValues(Contact.FORMATTED_NAME);

		    if (telCount > 0 && nameCount > 0) {
			String contactName =
				contact.getString(Contact.FORMATTED_NAME, 0);
			for (int i = 0; i < telCount; i++) {
			    int telAttributes =
				    contact.getAttributes(Contact.TEL, i);
			    String telNumber =
				    contact.getString(Contact.TEL, i);
			    if (cl.isSupportedAttribute(Contact.TEL,
				    Contact.ATTR_MOBILE)) {
				if ((telAttributes & Contact.ATTR_MOBILE) != 0) {
				    telNumbers.insertElementAt(telNumber, 0);
				} else {
				    telNumbers.addElement(telNumber);
				}
			    } else {
				telNumbers.addElement(telNumber);
			    }
			}
			if (contactName.length() > 20) {
			    contactName = contactName.substring(0, 17) + "...";
			}
			for (int i = 0; i < telNumbers.size(); i++) {
			    addTelNumInfo(data, contactName, (String) telNumbers.elementAt(i), counter);
			    counter++;
			}
			telNumbers.removeAllElements();
		    }
		}
	    }
	} finally {
	    if (cl != null) {
		cl.close();
	    }
	}
    }

    private static class LoadContacts implements Runnable {

	public void run() {
	    try {
		String[] allContactLists = PIM.getInstance().listPIMLists(PIM.CONTACT_LIST);
		if (allContactLists.length != 0) {
		    for (int i = 0; i < allContactLists.length; i++) {
			loadNames(allContactLists[i]);
		    }
		}
	    } catch (PIMException e) {
		DebugLog.addText("PhoneBook: " + e.getMessage());
	    } catch (SecurityException e) {
		DebugLog.addText("PhoneBook: " + e.getMessage());
	    }
	    allTelNum = new TelNum[data.size()];
	    data.copyInto(allTelNum);
	    data.removeAllElements();
	    fillPhonesInList();
	}
    }
}
//#sijapp cond.end#
