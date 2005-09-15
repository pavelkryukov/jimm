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
 File: src/jimm/JimmUI.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package jimm;

import javax.microedition.lcdui.*;
import java.util.Hashtable;
import DrawControls.*;
import jimm.util.ResourceBundle;

public class JimmUI implements CommandListener
{
	// Commands codes
	final public static int CMD_OK     = 1;
	final public static int CMD_CANCEL = 2;
	final public static int CMD_YES    = 3;
	final public static int CMD_NO     = 4;
	final public static int CMD_FIND   = 5;
	final public static int CMD_BACK   = 6;
	
	// Commands
	final private static Command cmdOk     = new Command("OK",     Command.OK,     1);
	final private static Command cmdCancel = new Command("Cancel", Command.BACK,   2);
	final private static Command cmdYes    = new Command("Yes",    Command.OK,     1);
	final private static Command cmdNo     = new Command("No",     Command.CANCEL, 2);
	final private static Command cmdFind   = new Command("Find",   Command.OK,     1);
	final private static Command cmdBack   = new Command("Back",   Command.BACK,   2);

	static private Hashtable commands = new Hashtable();
	static private Displayable lastDisplayable;
	
	// Associate commands and commands codes 
	static 
	{
		commands.put(cmdOk,     new Integer(CMD_OK)    );
		commands.put(cmdCancel, new Integer(CMD_CANCEL));
		commands.put(cmdYes,    new Integer(CMD_YES)   );
		commands.put(cmdNo,     new Integer(CMD_NO)    );
		commands.put(cmdFind,   new Integer(CMD_FIND)  );
		commands.put(cmdBack,   new Integer(CMD_BACK)  );
	}
	
	// Returns commands index of command
	public static int getCommandIdx(Command command)
	{
		Object result = commands.get(command);
		return (result == null) ? -1 : ((Integer)result).intValue();  
	}
	
	// Place "object = null;" code here:
	private static void clearAll()
	{
		msgForm = null;
		aboutTextList = null;
		System.gc();
	}
	
	///////////////////////////////////////////////////
	//                                               // 
	//           Message Box implementation          //
	//                                               //
	///////////////////////////////////////////////////
	static private Form msgForm;
	static private int msgBoxTag;

	public static int isMsgBoxCommand(Command testCommand, int testTag)
	{
		return (msgBoxTag == testTag) ? getCommandIdx(testCommand) : -1;
	}

	final public static int MESBOX_YESNO    = 1;
	final public static int MESBOX_OKCANCEL = 2;
	static public void messageBox(String cap, String text, int type, CommandListener listener, int tag)
	{
		clearAll();
		
		msgBoxTag = tag;
		msgForm = new Form(cap);
		msgForm.append(text);
		
		switch (type)
		{
		case MESBOX_YESNO:
			msgForm.addCommand(cmdYes);
			msgForm.addCommand(cmdNo);
			break;
			
		case MESBOX_OKCANCEL:
			msgForm.addCommand(cmdOk);
			msgForm.addCommand(cmdCancel);
			break;
		}

		msgForm.setCommandListener(listener);
		Jimm.display.setCurrent(msgForm);
	}
	
	//////////////////////////////////////////////////////////////////////////////
	private static TextList aboutTextList;
	
	public void about(Displayable lastDisplayable_)
	{
		final int 
			textColor = Jimm.jimm.getOptionsRef().getSchemeColor(Options.CLRSCHHEME_TEXT),
			blueColor = Jimm.jimm.getOptionsRef().getSchemeColor(Options.CLRSCHHEME_BLUE);
	
		lastDisplayable = lastDisplayable_;
		clearAll();
		
		aboutTextList = new TextList(null);
		aboutTextList.setCursorMode(TextList.SEL_NONE);
		aboutTextList.setFontSize(Font.SIZE_SMALL);
		
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		aboutTextList.setTitle(ResourceBundle.getString("about"));
		aboutTextList.setFullScreenMode(false);
		//#sijapp cond.else#
		aboutTextList.setCaption(ResourceBundle.getString("about"));
		//#sijapp cond.end#
		
		StringBuffer str = new StringBuffer();
		str.append(" ").append(ResourceBundle.getString("about_info")).append("\n")
		   .append(ResourceBundle.getString("free_heap")).append(": ")
		   .append(Runtime.getRuntime().freeMemory()/1024).append("kb\n");
		
		try
		{
			aboutTextList
				.addImage(Image.createImage("/icon.png"), null)
				.addBigText(str.toString(), textColor, Font.STYLE_PLAIN, -1);
		
			aboutTextList.addCommand(cmdBack);
			aboutTextList.setCommandListener(this);
           
            // Set the color sceme (background would not fit otherwise)
            Jimm.setColorScheme(aboutTextList);
			Jimm.display.setCurrent(aboutTextList);
		}
		catch (Exception e) {}
	}
	
	
	public void commandAction(Command c, Displayable d)
	{
		// "About" -> "Back"
		if ((d == aboutTextList) && (c == cmdBack))
		{
			Jimm.display.setCurrent(lastDisplayable);
		}
	}
	
}
