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
 File: src/jimm/Templates.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Igor Palkin
 *******************************************************************************/

package jimm;

import java.io.*;
import java.util.Vector;

import javax.microedition.lcdui.*;

import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.VirtualListCommands;

import jimm.comm.Util;
import jimm.util.ResourceBundle;

public class Templates implements VirtualListCommands, CommandListener
{
	private static Command cmdSelectTemplate = new Command(ResourceBundle
			.getString("select"), Command.OK, 1);

	private static Command backCommand = new Command(ResourceBundle
			.getString("back"), Jimm.cmdBack, 2);

	private static Command cmdNewTemplate = new Command(ResourceBundle
			.getString("add_new"), Command.ITEM, 3);

	private static Command cmdDelTemplate = new Command(ResourceBundle
			.getString("delete"), Command.ITEM, 4);

	private static Command clearCommand = new Command(ResourceBundle
			.getString("clear"), Command.ITEM, 5);

	private static Command addCommand = new Command(ResourceBundle
			.getString("add_new"), Command.OK, 1);

	private static Command cancelCommand = new Command(ResourceBundle
			.getString("back"), Jimm.cmdBack, 2);

	private static TextList templateList;

	private static Templates _this;

	private static TextBox templateTextbox;

	private static final int TMPL_DEL = 1;
	private static final int TMPL_CLALL = 2;
	
	private static TextBox textBox;
	private static int caretPos;
	private static Vector items = new Vector();
	
	private static String RMS_NAME = "templates2";

	public Templates()
	{
		_this = this;
		loadFromRMS();
	}

	public static void showTemplates(TextBox textBox)
	{
		Templates.textBox = textBox;
		
		templateList = new TextList(null);
		JimmUI.setColorScheme(templateList, false, -1, true);
		templateList.setCaption(ResourceBundle.getString("templates"));
		
//#sijapp cond.if target!="RIM" & target!="DEFAULT"#
		templateList.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
//#sijapp cond.end#

		if (textBox != null)
		{
			caretPos = textBox.getCaretPosition();
			templateList.addCommandEx(cmdSelectTemplate, VirtualList.MENU_TYPE_LEFT_BAR);
		}
		templateList.addCommandEx(
			backCommand, 
			textBox != null ? VirtualList.MENU_TYPE_RIGHT : VirtualList.MENU_TYPE_LEFT_BAR
		);
		templateList.addCommandEx(cmdNewTemplate, VirtualList.MENU_TYPE_RIGHT);
		templateList.addCommandEx(cmdDelTemplate, VirtualList.MENU_TYPE_RIGHT);
		templateList.addCommandEx(clearCommand, VirtualList.MENU_TYPE_RIGHT);
		refreshList();
		templateList.setCommandListener(_this);
		templateList.setVLCommands(_this);
		templateList.activate(Jimm.display);
	}

	public static boolean isMyOkCommand(Command c)
	{
		return (c == cmdSelectTemplate);
	}

	public void vlKeyPress(VirtualList sender, int keyCode, int type){}

	public void vlCursorMoved(VirtualList sender) {}

	public void vlItemClicked(VirtualList sender)
	{
		select();
	}

	public void commandAction(Command c, Displayable d)
	{
		Jimm.aaUserActivity();
		
		if (c == backCommand)
		{
			if (textBox != null)
			{
				Jimm.display.setCurrent(textBox);
				templateList = null;
			}
			else JimmUI.backToLastScreen();
		}

		else if (c == cmdSelectTemplate)
		{
			select();
		}

		else if (c == cmdNewTemplate)
		{
			templateTextbox = new TextBox(ResourceBundle.getString("new_template"), 
					null, 1000, TextField.ANY);
			templateTextbox.addCommand(addCommand);
			templateTextbox.addCommand(cancelCommand);
			templateTextbox.setCommandListener(_this);
			Jimm.display.setCurrent(templateTextbox);
			Jimm.setBkltOn(true);
		}
		
		else if (c == cmdDelTemplate)
		{
			JimmUI.messageBox(ResourceBundle.getString("attention"),
					ResourceBundle.getString("delete") + "?",
					JimmUI.MESBOX_YESNO, _this, TMPL_DEL);			
		}

		else if (c == addCommand)
		{
			String text = templateTextbox.getString().trim();
			if (text.length() != 0)
			{
				items.addElement(text);
				saveToRMS();
				refreshList();
			}
			templateList.activate(Jimm.display);
			templateTextbox = null;
		}

		else if (c == cancelCommand)
		{
			templateList.activate(Jimm.display);
			templateTextbox = null;
		}

		else if (c == clearCommand)
		{
			JimmUI.messageBox(ResourceBundle.getString("attention"),
					ResourceBundle.getString("clear") + "?",
					JimmUI.MESBOX_YESNO, _this, TMPL_CLALL);
		}

		else if (JimmUI.getCommandType(c, TMPL_CLALL) == JimmUI.CMD_YES)
		{
			items.removeAllElements();
			saveToRMS();
			templateList.activate(Jimm.display);
			templateList = null;
		}

		else if (JimmUI.getCommandType(c, TMPL_CLALL) == JimmUI.CMD_NO)
		{
			templateList.activate(Jimm.display);
		}
		
		else if (JimmUI.getCommandType(c, TMPL_DEL) == JimmUI.CMD_YES)
		{
			int index = templateList.getCurrTextIndex();
			if (index != -1)
			{
				items.removeElementAt(index);
				saveToRMS();
			}
			templateList.activate(Jimm.display);
		}
		
		else if (JimmUI.getCommandType(c, TMPL_DEL) == JimmUI.CMD_NO)
		{
			templateList.activate(Jimm.display);
		}
	}

	static private void select()
	{
		if (textBox == null) return;
		String selectedTemplate = null;
		if (templateList.getSize() != 0) selectedTemplate = (String)items.elementAt(templateList.getCurrTextIndex());
		templateList = null;
		Jimm.display.setCurrent(textBox);
		if (selectedTemplate != null) textBox.insert(selectedTemplate, caretPos);
	}

	private static void refreshList()
	{
		if (templateList == null) return;
		templateList.lock();
		try
		{
			templateList.clear();
			for (int i = 0; i < items.size(); i++)
				templateList.addBigText((String)items.elementAt(i), 
						templateList.getTextColor(), Font.STYLE_PLAIN, i).doCRLF(i);
		}
		finally
		{
			templateList.unlock();
		}
	}
	
	public static void saveToRMS()
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			int count = items.size();
			dos.writeInt(count);
			for (int i = 0; i < count; i++)
				dos.writeUTF((String)items.elementAt(i));
			Util.saveStreamToRms(baos.toByteArray(), RMS_NAME, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void loadFromRMS()
	{
		items.removeAllElements();
		DataInputStream stream = Util.getRmsInputStream(RMS_NAME, null);
		if (stream == null) return;
		try
		{
			int count = stream.readInt();
			for (int i = 0; i < count; i++)
			{
				String text = stream.readUTF();
				items.addElement(text);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			items.removeAllElements();
		}
	}
}
