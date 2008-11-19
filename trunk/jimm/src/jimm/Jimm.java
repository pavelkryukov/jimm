/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-07  Jimm Project

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

import java.util.Timer;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import DrawControls.ImageList;
import DrawControls.VirtualList;

//#sijapp cond.if target is "MOTOROLA"#
//# import jimm.TimerTasks;
//# import jimm.Jimm;
//# import jimm.Options;
//# import com.motorola.funlight.*;
//#sijapp cond.end#

public class Jimm extends MIDlet
{

	// Version
	public static String VERSION;

	// Application main object
	public static Jimm jimm;

	// Display object
	public static Display display;

	/****************************************************************************/
	
	private static StringBuffer loadError = new StringBuffer();
	
	public static void setLoadError(String value)
	{
		loadError.append(value).append("\n\n");
	}
	
	static public final int PHONE_SONYERICSSON = 1000;
	static public final int PHONE_NOKIA        = 1001;
	static private int phoneType;
	
	static public int getPhoneVendor()
	{
		return phoneType;
	}

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

	//#sijapp cond.if target is "MOTOROLA" & (modules_FILES="true"|modules_HISTORY="true")#
	//#	static public final boolean supports_JSR75;
	//#sijapp cond.end#

	// Timer object
	private static Timer timer = new Timer();

	//#sijapp cond.if modules_TRAFFIC is "true" #
	// Traffic counter
	private Traffic traffic;

	//#sijapp cond.end#

	// Splash canvas object
	private SplashCanvas sc;

	// Storage for messages
	//#sijapp cond.if modules_HISTORY is "true" #
	private HistoryStorage history;

	//#sijapp cond.end#

	//#sijapp cond.if modules_PIM is "true" #
	public static PhoneBook phoneBook;

	//#sijapp cond.end#

	private JimmUI ui;

	public static final String microeditionPlatform = System.getProperty("microedition.platform");

	public static final String microeditionProfiles = System.getProperty("microedition.profiles");

	public static int cmdBack = Command.BACK;

	//#sijapp cond.if target="MOTOROLA"|target="MIDP2"#
	static
	{
		if (microeditionPlatform != null)
		{
			String melc = microeditionPlatform.toLowerCase();
			if (melc.indexOf("ericsson") != -1) {
				phoneType = PHONE_SONYERICSSON;
				try
				{
				String jp = System.getProperty("com.sonyericsson.java.platform").toLowerCase();
				if (jp.indexOf("sjp") != -1)
					cmdBack = Command.CANCEL;
				} catch (Exception e) {} //Do nothing
			}
			else if (melc.indexOf("nokia") != -1)
				phoneType = PHONE_NOKIA;
		}

		//#sijapp cond.if target is "MOTOROLA" & (modules_FILES="true"|modules_HISTORY="true")#
		//#		boolean jsr75 = false;
		//#		try
		//#		{
		//#			jsr75 = Class.forName("javax.microedition.io.file.FileConnection") != null;
		//#		}
		//#		catch (ClassNotFoundException cnfe)
		//#		{
		//#		}
		//#		finally
		//#		{
		//#			supports_JSR75 = jsr75;
		//#		}
		//#sijapp cond.end#
	}

	//#sijapp cond.end#

	// Start Jimm
	public void startApp() throws MIDletStateChangeException
	{
		RunnableImpl.setMidlet(this);

		// Return if MIDlet has already been initialized
		if (Jimm.jimm != null)
		{
			showWorkScreen();
			return;
		}
		
		ImageList.setUseAlpha(Display.getDisplay(this).numAlphaLevels() > 2);

		// Save MIDlet reference
		Jimm.jimm = this;

		// Get Jimm version
		Jimm.VERSION = this.getAppProperty("Jimm-Version");
		if (Jimm.VERSION == null)
			Jimm.VERSION = "###VERSION###";

		// Create options container 	 
		this.o = new Options();

		// Create splash canvas object
		this.sc = new SplashCanvas(ResourceBundle.getString("loading"));

		// Check available heap memory, display warning if less then 250 KB
		System.gc();
		if (Runtime.getRuntime().totalMemory() < 256000)
		{
			Alert errorMsg = new Alert(ResourceBundle.getString("warning"),
					JimmException.getErrDesc(170, 0), null, AlertType.WARNING);
			errorMsg.setTimeout(Alert.FOREVER);
			Display.getDisplay(this).setCurrent(errorMsg, this.sc);
			try
			{
				Thread.sleep(3000);
			} catch (InterruptedException e)
			{
				// Do nothing
			}
		}
		// Display splash canvas
		else
		{
			Display.getDisplay(this).setCurrent(this.sc);
		}

		// Get display object (and update progress indicator)
		Jimm.display = Display.getDisplay(this);
		SplashCanvas.setProgress(10);

		// Create ICQ object (and update progress indicator)
		this.icq = new Icq();
		SplashCanvas.setProgress(20);

		// Create object for text storage (and update progress indicator)
		//#sijapp cond.if modules_HISTORY is "true" #
		history = new HistoryStorage();
		SplashCanvas.setProgress(30);
		//#sijapp cond.end#

		// Initialize main menu object (and update progress indicator)
		this.mm = new MainMenu();
		SplashCanvas.setProgress(40);

		//#sijapp cond.if modules_TRAFFIC is "true" #
		// Create traffic Object (and update progress indicator)
		this.traffic = new Traffic();
		SplashCanvas.setProgress(50);
		//#sijapp cond.end#

		// Create contact list object (and update progress indicator)
		this.cl = new ContactList();
		ContactList.beforeConnect();
		SplashCanvas.setProgress(60);

		//#sijapp cond.if modules_PIM is "true" #
		Jimm.phoneBook = new PhoneBook();
		SplashCanvas.setProgress(65);

		//#sijapp cond.end#

		// Create chat hisotry object (and update progress indicator)
		this.ch = new ChatHistory();
		SplashCanvas.setProgress(70);

		// Create and load emotion icons
		//#sijapp cond.if modules_SMILES_STD="true" | modules_SMILES_ANI="true" #
		new Emotions();
		SplashCanvas.setProgress(90);
		//#sijapp cond.end#

		new Templates();

		ui = new JimmUI();
		
		//#sijapp cond.if target="MOTOROLA" | target="MIDP2"#
		DrawControls.VirtualList.setBackLightData
		(
			Options.getBoolean(Options.OPTION_LIGHT_MANUAL),
			Options.getInt(Options.OPTION_LIGHT_TIMEOUT)
		);
		//#sijapp cond.end#
		
		DrawControls.VirtualList.setDisplay(Jimm.display);
		VirtualList.setMirrorMenu(Options.getBoolean(Options.OPTION_MIRROR_MENU));
		VirtualList.setCapOffset(Options.getInt(Options.OPTION_CAPTION_OFFSET));
		//#sijapp cond.if target!="DEFAULT"#
		Options.setBackgroundImage( Options.getInt(Options.OPTION_BG_IMAGE_MODE),
						Options.getString(Options.OPTION_BG_IMAGE_URL));
		//#sijapp cond.end#

		if (loadError.length() == 0)
		{
			if (Options.getBoolean(Options.OPTION_AUTO_CONNECT))
			{
				// Connect
				Icq.reconnect_attempts = (Options.getBoolean(Options.OPTION_RECONNECT)) ?
							Options.getInt(Options.OPTION_RECONNECT_NUMBER) : 0;
				ContactList.beforeConnect();
				Icq.connect();
			} 
			else
			{
				// Activate main menu
				MainMenu.activateMenu();
			}
		}
		else
		{
			JimmUI.showLoadError(loadError.toString());
		}
		loadError = null;
		
		JimmUI.startTaskForTimeString();
		
		// Start one minute task 
		new Timer().schedule(new TimerTasks(TimerTasks.TYPE_MINUTE), 60*1000, 60*1000);
	}

	// Pause
	public void pauseApp()
	{
		// Do nothing
	}

	// Destroy Jimm
	public void destroyApp(boolean unconditional)
			throws MIDletStateChangeException
	{
		// Disconnect
		Icq.disconnect(false);

		// Save traffic
		//#sijapp cond.if modules_TRAFFIC is "true" #
		try
		{
			Traffic.save();
		} catch (Exception e)
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

	//#sijapp cond.if modules_HISTORY is "true" #
	// Returns a reference to the stored history object
	public HistoryStorage getHistory()
	{
		return (this.history);
	}

	//#sijapp cond.end#

	// Returns a reference to the timer object
	static public Timer getTimerRef()
	{
		return timer;
	}

	// Cancels the timer and makes a new one
	public void cancelTimer()
	{
		try
		{
			timer.cancel();
		} catch (IllegalStateException e)
		{
		}
		timer = new Timer();
	}

	// Returns a reference to splash canvas object
	public SplashCanvas getSplashCanvasRef()
	{
		return (this.sc);
	}

	//#sijapp cond.if modules_TRAFFIC is "true" #
	// Return a reference to traffic object
	public Traffic getTrafficRef()
	{
		return (this.traffic);
	}

	//#sijapp cond.end#

	public JimmUI getUIRef()
	{
		return ui;
	}

	static public void showWorkScreen()
	{
		if (SplashCanvas.locked())
			SplashCanvas.show();
		else if (Icq.isConnected())
			ContactList.activateList();
		else
			MainMenu.activateMenu();
	}

	//#sijapp cond.if target is "MIDP2" #
	// Set the minimize state of midlet
	static public void setMinimized(boolean mini)
	{
		if (mini)
		{
			Jimm.display.setCurrent(null);
		} else
		{
			Displayable disp = Jimm.display.getCurrent();
			if (!disp.isShown()) {
				if (disp != null)
					Jimm.display.setCurrent(null);
				showWorkScreen();
			}

		}
	}

	//#sijapp cond.end #
	
/* ************************************************************************* */
	
	/* Auto away stuff */
	
	static public final int AA_MODE_NONE = 0;
	static public final int AA_MODE_AWAY = 1;
	static public final int AA_MODE_NA = 2;
	
	static private int aaInactivityCounter;
	static private int aaMode;
	
	public static void aaUserActivity()
	{
		synchronized (jimm)
		{
			aaInactivityCounter = 0;
			
			if (Icq.isConnected() && (aaMode != AA_MODE_NONE))
			{
				aaMode = AA_MODE_NONE;
				try { 
					Icq.setOnlineStatus((int)Options.getLong(Options.OPTION_ONLINE_STATUS), 255);
					Icq.setInactiveTime(0x00000000);
				} catch (Exception e) {}
				ContactList.showStatusInCaption(-1, -1);
			}
		}
	}
	
	public static int aaGetMode()
	{
		synchronized (jimm)
		{
			return aaMode;
		}
	}
	
	public static void aaNextMinute()
	{
		if ( !Icq.isConnected() || !Options.getBoolean(Options.OPTION_USE_AUTOAWAY)) return;
		
		int time1 = Options.getInt(Options.OPTION_AUTOAWAY_TIME1);
		int time2 = Options.getInt(Options.OPTION_AUTOAWAY_TIME2);
		
		synchronized (jimm)
		{
			aaInactivityCounter++;
			switch (aaMode)
			{
			case AA_MODE_NONE:
				int status = (int)Options.getLong(Options.OPTION_ONLINE_STATUS);
				if ((aaInactivityCounter >= time1) && 
					(status != ContactList.STATUS_AWAY) && 
					(status != ContactList.STATUS_NA) &&
					(status != ContactList.STATUS_DND) &&
					(status != ContactList.STATUS_OCCUPIED) &&
					(status != ContactList.STATUS_INVIS_ALL) &&
					(status != ContactList.STATUS_INVISIBLE))
				{
					try {
						Icq.setOnlineStatus(ContactList.STATUS_AWAY, 255);
						Icq.setInactiveTime(time1 * 60);
					} catch (Exception e) {}
					ContactList.showStatusInCaption(ContactList.STATUS_AWAY, -1);
					aaMode = AA_MODE_AWAY;
				}
				break;
				
			case AA_MODE_AWAY:
				if (aaInactivityCounter >= time2)
				{
					try { Icq.setOnlineStatus(ContactList.STATUS_NA, 255); } catch (Exception e) {}
					ContactList.showStatusInCaption(ContactList.STATUS_NA, -1);
					aaMode = AA_MODE_NA;
				}
				break;
			}
		}
	}
	
/* ************************************************************************** */
	
	public static void setBkltOn(boolean ferever)
	{
//#sijapp cond.if target="MOTOROLA" | target="MIDP2"#
		if ( !Options.getBoolean(Options.OPTION_LIGHT_MANUAL) ) return;
		Jimm.display.flashBacklight(ferever ? Integer.MAX_VALUE : Options.getInt(Options.OPTION_LIGHT_TIMEOUT));
		//System.out.println("setBkltOn, ferever="+ferever);
//#sijapp cond.end#
	}
	
	public static void setBkltOff()
	{
//#sijapp cond.if target="MOTOROLA" | target="MIDP2"#
		if ( !Options.getBoolean(Options.OPTION_LIGHT_MANUAL) ) return;
		Jimm.display.flashBacklight(1);
		//System.out.println("setBkltOff");
//#sijapp cond.end#
	}
	
}
