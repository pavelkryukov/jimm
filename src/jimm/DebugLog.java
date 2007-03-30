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
import jimm.Jimm;

public class DebugLog
//#sijapp cond.if modules_DEBUGLOG is "true" #
		implements CommandListener
//#sijapp cond.end#
{
	//#sijapp cond.if modules_DEBUGLOG is "true" #
	private static TextList list;

	private static Command backCommand = new Command("Back", Command.BACK, 1);

	static
	{
		list = new TextList(null);
		list.addCommand(backCommand);
		list.setCommandListener(new DebugLog());
		list.setFontSize(TextList.SMALL_FONT);
		list.setCaption("Debug log");
		list.setCursorMode(TextList.SEL_NONE);
	}

	private static boolean wasShown = false;

	public static void activate()
	{
		if (!wasShown)
		{
			JimmUI.setColorScheme(list);
			wasShown = true;
		}

		Jimm.display.setCurrent(list);
	}

	static int counter = 0;

	public void commandAction(Command c, Displayable d)
	{
		ContactList.activate();
	}

	//#sijapp cond.end#

	public static void addText(String text)
	{
		//#sijapp cond.if modules_DEBUGLOG is "true" #
		synchronized (list)
		{
			list.addBigText("[" + Integer.toString(counter + 1) + "]: ", 0xFF,
					Font.STYLE_PLAIN, counter);
			list.addBigText(text, 0, Font.STYLE_PLAIN, counter);
			list.doCRLF(counter);
			counter++;
		}
		//#sijapp cond.else#
		//#		System.out.println(text);
		//#sijapp cond.end#
	}
}
