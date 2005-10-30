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

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.lcdui.*;

import java.io.DataInputStream;
import java.util.*;

import DrawControls.*;
import jimm.comm.Util;
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
	static private JimmUI jimmUIobj;
	
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
	
	JimmUI()
	{
		jimmUIobj = this;
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
	
	/////////////////////////
	//                     // 
	//     Message Box     //
	//                     //
	/////////////////////////
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
    
    // String for recent version
    static private String version;
    static private boolean versionLoaded = false;
    
	static public void about(Displayable lastDisplayable_)
	{
		System.gc();
		long freeMem = Runtime.getRuntime().freeMemory()/1024;
		
		final int textColor = Jimm.jimm.getOptionsRef().getSchemeColor(Options.CLRSCHHEME_TEXT);
	
		if (lastDisplayable_ != null) lastDisplayable = lastDisplayable_;
		if (aboutTextList == null) aboutTextList = new TextList(null);
		
		aboutTextList.lock();
		aboutTextList.clear();
		aboutTextList.setCursorMode(TextList.SEL_NONE);
		setColorScheme(aboutTextList);
		
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		aboutTextList.setTitle(ResourceBundle.getString("about"));
		aboutTextList.setFullScreenMode(false);
		aboutTextList.setFontSize(Font.SIZE_MEDIUM);
		//#sijapp cond.else#
		aboutTextList.setCaption(ResourceBundle.getString("about"));
		aboutTextList.setFontSize(Font.SIZE_SMALL);
		//#sijapp cond.end#
	    
		StringBuffer str = new StringBuffer();
		str.append(" ").append(ResourceBundle.getString("about_info")).append("\n")
		   .append(ResourceBundle.getString("free_heap")).append(": ")
		   .append(freeMem).append("kb\n\n")
           .append(ResourceBundle.getString("latest_ver"));
		
		if (versionLoaded) str.append(" ").append(version);
		else str.append("... ");
		
		try
		{
			Image image = Image.createImage("/icon.png");
			aboutTextList
				.addImage(image, null, image.getWidth(), image.getHeight(), -1)
				.addBigText(str.toString(), textColor, Font.STYLE_PLAIN, -1);
		
			aboutTextList.addCommand(cmdBack);
			aboutTextList.setCommandListener(jimmUIobj);
           
            // Set the color sceme (background would not fit otherwise)
			Jimm.display.setCurrent(aboutTextList);
		}
		catch (Exception e) {}
		
		aboutTextList.unlock();
        
		if (!versionLoaded) Jimm.jimm.getTimerRef().schedule(new GetVersionInfoTimerTask(), 2000);
	}
    	
	public void commandAction(Command c, Displayable d)
	{
		// "About" -> "Back"
		if ((d == aboutTextList) && (c == cmdBack))
		{
			synchronized(jimmUIobj)
			{
				Jimm.display.setCurrent(lastDisplayable);
				aboutTextList = null;
			}
		}
	}
	
	
	//////////////////////
	//                  //
	//    Clipboard     //
	//                  //
	//////////////////////
	
	static private String clipBoardText;
	
	static public String getClipBoardText()
	{
		return clipBoardText;
	}
	
	static public void setClipBoardText(boolean incoming, String date, String from, String text)
	{
		StringBuffer sb = new StringBuffer();
		sb.append('[').append(from).append(' ').append(date).append(']')
		  .append(incoming ? " >> " : " << ").append(text);
		clipBoardText = sb.toString();
	}
	
	static public void clearClipBoardText()
	{
		clipBoardText = null;	
	}
	
	
	////////////////////////
	//                    //
	//    Color scheme    //
	//                    //
	////////////////////////
	
	static public void setColorScheme(VirtualList vl)
	{
		if (vl == null) return;
		
		Options opt = Jimm.jimm.getOptionsRef();
		
		vl.setColors
		(
			opt.getSchemeColor(Options.CLRSCHHEME_BLUE),
			opt.getSchemeColor(Options.CLRSCHHEME_BACK),
			opt.getSchemeColor(Options.CLRSCHHEME_CURS),
			opt.getSchemeColor(Options.CLRSCHHEME_TEXT)
		);
	}
	
	static public void setColorScheme()
	{
		// #sijapp cond.if modules_HISTORY is "true" #
		Jimm.jimm.getHistory().setColorScheme();
		// #sijapp cond.end#
		
		Jimm.jimm.getChatHistoryRef().setColorScheme();
		setColorScheme((VirtualList)Jimm.jimm.getContactListRef().getVisibleContactListRef());
	}
    
    /*****************************************************************************/
    /*****************************************************************************/
    /*****************************************************************************/


    // Waits until contact listupdate is completed
    public static class GetVersionInfoTimerTask extends TimerTask
    {

        // Try to get current Jimm version from Jimm server
        ContentConnection ctemp;
        DataInputStream istemp;
        byte[] version_;

        // Timer routine
        public void run()
        {
            try
            {
                String url = "http://www.jimm.org/en/current_ver";
                ctemp = (ContentConnection) Connector.open(url);

                istemp = ctemp.openDataInputStream();
                version_ = new byte[istemp.available()];
                istemp.readFully(version_);
                version = new String(version_);
                versionLoaded = true;
            } catch (Exception e)
            {
                version = ResourceBundle.getString("no_recent_ver");
            }
            
            synchronized(jimmUIobj)
            {
            	if ((aboutTextList != null) && aboutTextList.isShown())
            	{
            		aboutTextList.addBigText
            		(
            			version,
            			aboutTextList.getTextColor(),
            			Font.STYLE_PLAIN,
            			-1
            		);
            	}
            }
        }

    }

	
}
