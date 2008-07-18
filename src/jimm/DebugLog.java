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
 File: src/jimm/DebugLog.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package jimm;

import javax.microedition.lcdui.*;

import DrawControls.TextList;
import DrawControls.VirtualList;
import jimm.Jimm;
import jimm.comm.Util;

public class DebugLog
//#sijapp cond.if modules_DEBUGLOG is "true" #
		implements CommandListener
//#sijapp cond.end#
{
	//#sijapp cond.if modules_DEBUGLOG is "true" #
	private static TextList list;

	private static final Command backCommand = new Command("Back", Jimm.cmdBack, 1);
	private static final Command cmdCopyText = new Command("Copy text", Command.ITEM, 4);
	private static final Command cmdClear = new Command("Clear", Command.ITEM, 8);

	static
	{
		list = new TextList(null);
		list.addCommandEx(backCommand, VirtualList.MENU_TYPE_RIGHT_BAR);

		list.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_LEFT_BAR);
		list.addCommandEx(cmdClear, VirtualList.MENU_TYPE_LEFT);
		list.addCommandEx(cmdCopyText, VirtualList.MENU_TYPE_LEFT);
		list.setCommandListener(new DebugLog());
		list.setFontSize(VirtualList.SMALL_FONT);
		list.setCaption("Debug log");
		list.setMode(VirtualList.CURSOR_MODE_DISABLED);
	}

	private static boolean wasShown = false;

	public static void activate()
	{
		if (!wasShown)
		{
			JimmUI.setColorScheme(list, false, -1, true);
			wasShown = true;
		}

		list.activate(Jimm.display);
	}

	static int counter = 0;

	public void commandAction(Command c, Displayable d)
	{
		Jimm.aaUserActivity();
		if (c == backCommand)
		{
			ContactList.activateList();
		}
		else if (c == cmdClear)
		{
			list.clear();
		}
		else if (c == cmdCopyText)
		{
			JimmUI.setClipBoardText("[DebugLog]\n" + list.getCurrText(0, false));
		}
	}

	//#sijapp cond.end#

	public static void addText(String text)
	{
		//#sijapp cond.if modules_DEBUGLOG is "true" #
		synchronized (list)
		{
			list.addBigText("(" + Util.getDateString(true) +"): ", 0xFF,
					Font.STYLE_PLAIN, counter);
			list.addBigText(text, 0, Font.STYLE_PLAIN, counter);
			list.doCRLF(counter);
			counter++;
		}
		//#sijapp cond.else#
		//#		System.out.println("(" + Util.getDateString(true) + "): " + text);
		//#sijapp cond.end#
	}
}
