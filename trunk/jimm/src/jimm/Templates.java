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
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.VirtualListCommands;

import jimm.util.ResourceBundle;

public class Templates implements VirtualListCommands, CommandListener
{
	private static Command selectTemplateCommand = new Command(ResourceBundle
			.getString("select"), Command.OK, 1);

	private static Command backCommand = new Command(ResourceBundle
			.getString("back"), Command.BACK, 2);

	private static Command newTemplateCommand = new Command(ResourceBundle
			.getString("add_new"), Command.ITEM, 3);

	private static Command deleteTemplateCommand = new Command(ResourceBundle
			.getString("delete"), Command.ITEM, 4);

	private static Command clearCommand = new Command(ResourceBundle
			.getString("clear"), Command.ITEM, 5);

	private static Command addCommand = new Command(ResourceBundle
			.getString("add_new"), Command.OK, 1);

	private static Command cancelCommand = new Command(ResourceBundle
			.getString("back"), Command.BACK, 2);

	private static TextList templateList;

	private static Displayable lastDisplay;

	private static CommandListener selectionListener;

	private static String selectedTemplate;

	private static Templates _this;

	private static RecordStore rms = null;

	private static TextBox templateTextbox;

	private static final int TMPL_DEL = 1;

	private static final int TMPL_CLALL = 2;

	public Templates()
	{
		_this = this;
	}

	public static void selectTemplate(CommandListener selectionListener_,
			Displayable lastDisplay_)
	{
		lastDisplay = lastDisplay_;
		selectionListener = selectionListener_;
		templateList = new TextList(null);
		JimmUI.setColorScheme(templateList);
		templateList.setCaption(ResourceBundle.getString("templates"));
		templateList.addCommand(selectTemplateCommand);
		templateList.addCommand(backCommand);
		templateList.addCommand(newTemplateCommand);
		templateList.addCommand(clearCommand);
		refreshList();
		templateList.setCommandListener(_this);
		templateList.setVLCommands(_this);
		Jimm.display.setCurrent(templateList);
	}

	public static String getSelectedTemplate()
	{
		return selectedTemplate;
	}

	public static boolean isMyOkCommand(Command c)
	{
		return (c == selectTemplateCommand);
	}

	public void onKeyPress(VirtualList sender, int keyCode, int type)
	{
	}

	public void onCursorMove(VirtualList sender)
	{
	}

	public void onItemSelected(VirtualList sender)
	{
		select();
	}

	public void commandAction(Command c, Displayable d)
	{
		if (c == backCommand)
		{
			Jimm.display.setCurrent(lastDisplay);
			templateList = null;
		}

		if (c == selectTemplateCommand)
		{
			select();
		}

		if (c == newTemplateCommand)
		{
			templateTextbox = new TextBox(ResourceBundle
					.getString("new_template"), null, 1000, TextField.ANY);
			templateTextbox.addCommand(addCommand);
			templateTextbox.addCommand(cancelCommand);
			templateTextbox.setCommandListener(_this);
			Jimm.display.setCurrent(templateTextbox);
		}

		if (c == addCommand)
		{
			addRecord(templateTextbox.getString());
			refreshList();
			Jimm.display.setCurrent(templateList);
			templateTextbox = null;
		}

		if (c == cancelCommand)
		{
			Jimm.display.setCurrent(templateList);
			templateTextbox = null;
		}

		if (c == clearCommand)
		{
			JimmUI.messageBox(ResourceBundle.getString("attention"),
					ResourceBundle.getString("clear") + "?",
					JimmUI.MESBOX_YESNO, _this, TMPL_CLALL);
		}

		if (JimmUI.getCommandType(c, TMPL_CLALL) == JimmUI.CMD_YES)
		{
			try
			{
				rms.closeRecordStore();
				rms = null;
				System.gc();
				RecordStore.deleteRecordStore("templates");
			} catch (Exception e)
			{
			}
			Jimm.display.setCurrent(lastDisplay);
			templateList = null;
		}

		if (JimmUI.getCommandType(c, TMPL_CLALL) == JimmUI.CMD_NO)
		{
			Jimm.display.setCurrent(templateList);
		}
	}

	static private void select()
	{
		if (templateList.getSize() == 0)
			selectedTemplate = null;
		else
			selectedTemplate = getRecord(templateList.getCurrIndex());
		Jimm.display.setCurrent(lastDisplay);
		templateList = null;
		selectionListener.commandAction(selectTemplateCommand, templateList);
	}

	private static void refreshList()
	{
		templateList.lock();
		templateList.clear();
		for (int i = 0; i < getRecordCount(); i++)
			templateList.addBigText(getRecord(i), templateList.getTextColor(),
					Font.STYLE_PLAIN, i).doCRLF(i);
		templateList.unlock();
	}

	private static void openRMS()
	{
		if (rms == null)
			try
			{
				rms = RecordStore.openRecordStore("templates", true);
			} catch (Exception e)
			{
			}
		;
	}

	private static void addRecord(String s)
	{
		openRMS();
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream das = new DataOutputStream(baos);
			das.writeUTF(s);
			byte[] buffer = baos.toByteArray();
			rms.addRecord(buffer, 0, buffer.length);
			rms.closeRecordStore();
			rms = null;
			System.gc();
		} catch (Exception e)
		{
		}
		;
	}

	private static int getRecordCount()
	{
		openRMS();
		int n = 0;
		try
		{
			n = rms.getNumRecords();
		} catch (Exception e)
		{
		}
		return n;
	}

	private static String getRecord(int num)
	{
		byte[] data;
		String s;
		openRMS();
		try
		{
			data = rms.getRecord(++num);
			ByteArrayInputStream bais = new ByteArrayInputStream(data, 0,
					data.length);
			DataInputStream dis = new DataInputStream(bais);
			s = dis.readUTF();
		} catch (Exception e)
		{
			return null;
		}
		return s;
	}

}
