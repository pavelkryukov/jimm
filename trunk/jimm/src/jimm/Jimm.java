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
 File: src/jimm/Jimm.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


package jimm;


import jimm.comm.Icq;
import jimm.util.ResourceBundle;
import jimm.JimmUI;

import java.util.Timer;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class Jimm extends MIDlet
{


	// Version
	public static String VERSION;


	// Application main object
	public static Jimm jimm;


	// Display object
	public static Display display;


	/****************************************************************************/


	// ICQ object
	private Icq icq;


	// Options container
	private Options o;


	// Main menu object
	private MainMenu mm;


	// Contact list object
	private ContactList cl;

	// Chat history object
	private ChatHistory ch;
	
    // #sijapp cond.if target is "MIDP2" #
	// Minimized state variable
	private boolean minimized, is_phone_SE;
    // #sijapp cond.end #

	// Timer object
	private Timer t;


	// #sijapp cond.if modules_TRAFFIC is "true" #
	// Traffic counter
	private Traffic traffic;
	// #sijapp cond.end#


	// Splash canvas object
	private SplashCanvas sc;
	
	// Storage for messages
	// #sijapp cond.if modules_HISTORY is "true" #
	private HistoryStorage history;
	// #sijapp cond.end#

	//#sijapp cond.if modules_SMILES is "true" #
	private Emotions emotions;
	// #sijapp cond.end#
	
	private JimmUI ui;

	// Start Jimm
	public void startApp() throws MIDletStateChangeException
	{
		RunnableImpl.setMidlet(this);
		
		// Return if MIDlet has already been initialized
		if (Jimm.jimm != null)
        {
		    // #sijapp cond.if target is "MIDP2" #
		    this.setMinimized(false);
		    // #sijapp cond.end #
		    ContactList.activate();
            return;
        }
		
		// Save MIDlet reference
		Jimm.jimm = this;
		
		// #sijapp cond.if target is "MIDP2" #
		String microeditionPlatform = System.getProperty("microedition.platform");
		if (microeditionPlatform != null)
			is_phone_SE = (microeditionPlatform.toLowerCase().indexOf("ericsson") != -1);
		// #sijapp cond.end#
		
		// Get Jimm version
		Jimm.VERSION = this.getAppProperty("Jimm-Version");
		if (Jimm.VERSION == null) Jimm.VERSION = "###VERSION###";

		// Create splash canvas object
		this.sc = new SplashCanvas();
		SplashCanvas.setMessage(ResourceBundle.getString("loading"));

		// Check available heap memory, display warning if less then 250 KB
		if (Runtime.getRuntime().totalMemory() < 256000)
		{
			Alert errorMsg = new Alert(ResourceBundle.getString("warning"), JimmException.getErrDesc(170, 0), null, AlertType.WARNING);
			errorMsg.setTimeout(Alert.FOREVER);
			Display.getDisplay(this).setCurrent(errorMsg, this.sc);
			try
			{
				Thread.sleep(3000);
			}
			catch (InterruptedException e)
			{
				// Do nothing
			}
		}
		// Display splash canvas
		else
		{
			Display.getDisplay(this).setCurrent(this.sc);
		}
		
	    // #sijapp cond.if target is "MIDP2" #
		// Set minimized = false
		this.setMinimized(false);
	    // #sijapp cond.end #
		
		// Get display object (and update progress indicator)
		Jimm.display = Display.getDisplay(this);
		SplashCanvas.setProgress(10);

		// Create ICQ object (and update progress indicator)
		this.icq = new Icq();
		SplashCanvas.setProgress(20);
		
		// Create object for text storage (and update progress indicator)
		// #sijapp cond.if modules_HISTORY is "true" #
		history = new HistoryStorage();
		SplashCanvas.setProgress(30);
		// #sijapp cond.end#

		// Create options container (and update progress indicator)
		this.o = new Options();
		SplashCanvas.setProgress(40);

		// Initialize main menu object (and update progress indicator)
		this.mm = new MainMenu();
		SplashCanvas.setProgress(50);

		// #sijapp cond.if modules_TRAFFIC is "true" #
		// Create traffic Object (and update progress indicator)
		this.traffic = new Traffic();
		SplashCanvas.setProgress(60);
		// #sijapp cond.end#
		
		// Create contact list object (and update progress indicator)
		this.cl = new ContactList();
		ContactList.beforeConnect();
		SplashCanvas.setProgress(70);
		
		// Create chat hisotry object (and update progress indicator)
		this.ch = new ChatHistory();
		SplashCanvas.setProgress(80);

		// Create timer object (and update progress indicator)
		this.t = new Timer();
		SplashCanvas.setProgress(90);
		
		// Create and load emotion icons
		//#sijapp cond.if modules_SMILES is "true" #
		emotions = new Emotions();
		// #sijapp cond.end#
		
		ui = new JimmUI();
		
		// set color scheme for all forms
		JimmUI.setColorScheme();
		
		if (Options.getBooleanOption(Options.OPTION_AUTO_CONNECT))
        {
            // Remove version string from splash screen
			SplashCanvas.delVersionString();
		    // Connect
            ContactList.beforeConnect();
            Icq.connect();
        } else
        {
            // Remove version string from splash screen
        	SplashCanvas.delVersionString();
            // Activate main menu
            MainMenu.activate();
        }
	}


	// Pause
	public void pauseApp()
	{
		// Do nothing
	}


	// Destroy Jimm
	public void destroyApp(boolean unconditional) throws MIDletStateChangeException
	{
        // Disconnect
        Icq.disconnect();       
        
        // Save traffic
        //#sijapp cond.if modules_TRAFFIC is "true" #
		try 
		{
			Jimm.jimm.getTrafficRef().save();
		} 
		catch (Exception e) 
		{ // Do nothing
		} 
		//#sijapp cond.end#
		
		Jimm.display.setCurrent(null);
		this.notifyDestroyed();
	}


	// Returns a reference to ICQ object
	public Icq getIcqRef()
	{
		return (this.icq);
	}


	// Returns a reference to options container
	public Options getOptionsRef()
	{
		return (this.o);
	}


	// Returns a reference to the main menu object
	public MainMenu getMainMenuRef()
	{
		return (this.mm);
	}


	// Returns a reference to the contact list object
	public ContactList getContactListRef()
	{
		return (this.cl);
	}
	
	// Returns a reference to the chat history list object
	public ChatHistory getChatHistoryRef()
	{
		return (this.ch);
	}

	// #sijapp cond.if modules_HISTORY is "true" #
	// Returns a reference to the stored history object
	public HistoryStorage getHistory()
	{
		return (this.history);
	}
	// #sijapp cond.end#

	// Returns a reference to the timer object
	public Timer getTimerRef()
	{
		return (this.t);
	}


	// Returns a reference to splash canvas object
	public SplashCanvas getSplashCanvasRef()
	{
		return (this.sc);
	}


	// #sijapp cond.if modules_TRAFFIC is "true" #
	// Return a reference to traffic object
	public Traffic getTrafficRef()
	{
		return (this.traffic);
	}
	// #sijapp cond.end#
	
	//#sijapp cond.if modules_SMILES is "true" #
	/*
	public Emotions getEmotionsRef()
	{
		return emotions;
	}*/
	// #sijapp cond.end#
	
	public JimmUI getUIRef()
	{
		return ui;
	}
	
    // #sijapp cond.if target is "MIDP2" #
	// Set the minimize variable
	public void setMinimized(boolean mini)
	{
	    this.minimized = mini;
	}
	
	// Return if the app is in minimized state
	public boolean minimized()
	{
	    return(this.minimized);
	}
	
	public boolean is_phone_SE()
	{
		return is_phone_SE;
	}	
	
    // #sijapp cond.end #
}

