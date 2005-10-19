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
 File: src/jimm/SplashCanvas.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


package jimm;

import DrawControls.TextList;
import jimm.comm.ConnectAction;
//  #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
//  #sijapp cond.if modules_FILES is "true"#
import jimm.comm.DirectConnectionAction;
//  #sijapp cond.end#
//  #sijapp cond.end#
import jimm.comm.DisconnectAction;
import jimm.comm.RequestInfoAction;
import jimm.comm.SearchAction;
import jimm.comm.UpdateContactListAction;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

import java.io.IOException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDletStateChangeException;
import java.util.Date;
import java.util.TimerTask;
import java.util.Timer;
//  #sijapp cond.if target is "MOTOROLA"#
import DrawControls.LightControl;
import javax.microedition.lcdui.Screen;
//  #sijapp cond.end#

//#sijapp cond.if target is "RIM"#
import net.rim.device.api.system.LED;
//#sijapp cond.end#


public class SplashCanvas extends Canvas
{
	//Timer for repaint
	private Timer t1,t2;

	// Location of the splash image (inside the JAR file)
	private static final String SPLASH_IMG = "/splash.png";


	// Image object, holds the splash image
	private static Image splash;


	// Image object, holds the background image
	private static Image background;


	// Location of the notice image (inside the JAR file)
	private static final String NOTICE_IMG = "/notice.png";


	// Image object, holds the notice image
	private static Image notice;


	// Font used to display the logo (if image is not available)
	private static Font logoFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
	
	// Font used to display the version nr
	private static Font versionFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

	// Font (and font height in pixels) used to display informational messages
	private static Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC, Font.SIZE_SMALL);
	private static int height = font.getHeight();
	

	// Initializer block
	static
	{

		// Construct splash image
		try
		{
			SplashCanvas.splash = Image.createImage(SplashCanvas.SPLASH_IMG);
			SplashCanvas.notice = Image.createImage(SplashCanvas.NOTICE_IMG);
		}
		catch (IOException e)
		{
			// Do nothing
		}

	}


	/*****************************************************************************/


	// Message to display beneath the splash image
	private String message;


	// Progress in percent
	private int progress; // = 0


	// True if keylock has been enabled
	private boolean isLocked;


	// Number of available messages
	private int availableMessages;
	

	// Should the keylock message be drawn to the screen?
	private boolean showKeylock;


	// Timestamp
	private Date pressed; // = null
	
	// Version string
	private boolean version;
	
	// Constructor
	public SplashCanvas(String message)
	{
	    this.version = true;
	    //  #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		this.setFullScreenMode(true);
		//  #sijapp cond.end#
		this.message = new String(message);
		this.showKeylock = false;
		//  #sijapp cond.if target is "MOTOROLA"#
		SplashCanvas.background = Image.createImage(this.getWidth(), this.getHeight()+22);
		//  #sijapp cond.else#
		SplashCanvas.background = Image.createImage(this.getWidth(), this.getHeight());
		//  #sijapp cond.end#
		int r, g;
		Graphics bg_graph = background.getGraphics();
		for (int x = 0; x < this.getWidth(); x+=2)
		{
			//  #sijapp cond.if target is "MOTOROLA"#
			for (int y = 0; y < this.getHeight()+22; y+=2)
			//  #sijapp cond.else#
			for (int y = 0; y < this.getHeight(); y+=2)
			//  #sijapp cond.end#
			{
				r = x * y / (y + x + 1) % 256;
				g = ((r ^ x ^ y)) % 256;
				bg_graph.setColor(r, g, (r + g) % 256);
				bg_graph.fillRect(x, y, 2, 2);
			}
		}
	}


	// Constructor, blank message
	public SplashCanvas()
	{
		this("");
	}


	// Returns the informational message
	public synchronized String getMessage()
	{
		return (this.message);
	}


	// Sets the informational message
	public synchronized void setMessage(String message)
	{
		this.message = new String(message);
		this.progress = 0;
	}


	// Returns the current progress in percent
	public synchronized int getProgress()
	{
		return (this.progress);
	}


	// Sets the current progress in percent (and request screen refresh)
	public synchronized void setProgress(int progress)
	{
		this.progress = progress;
		this.repaint(0, this.getHeight() - SplashCanvas.height - 2, this.getWidth(), SplashCanvas.height + 2);
	}
	
	
	
	public void delVersionString()
	{
	    this.version = false;
	}
	

	// Enable keylock
	public synchronized void lock()
	{
		this.setMessage(ResourceBundle.getString("keylock_enabled"));
		this.setProgress(0);
		this.isLocked = true;
		//  #sijapp cond.if target is "MOTOROLA"#
		LightControl.Off();
		//  #sijapp cond.end#
		Jimm.display.setCurrent(this);
		if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_DISPLAY_DATE))
        {
            t1 = new Timer();
            t1.schedule(new TimerTask()
            {

                public void run()
                {
                    SplashCanvas.this.repaint();
                }
            }, 20000, 20000);
        }
		
	}


	// Disable keylock
	private synchronized void unlock()
	{
		this.isLocked = false;
		this.availableMessages = 0;
        // #sijapp cond.if target is "RIM"#
        LED.setState(LED.STATE_OFF);
        //  #sijapp cond.end#
        	//  #sijapp cond.if target is "MOTOROLA"#
		if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_LIGHT_MANUAL))
		{
			LightControl.On();
		}
		//  #sijapp cond.end#
		if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_DISPLAY_DATE))
		{
			
			t1.cancel();
		}
		Jimm.jimm.getContactListRef().activate();
	}
    
    // Is the screen locked?
    public boolean locked()
    {
        return(this.isLocked);
    }


	// Called when message has been received
	public synchronized void messageAvailable()
	{
		if (this.isLocked)
		{
			++this.availableMessages;
			// #sijapp cond.if target is "RIM"#
	        LED.setConfiguration(500, 250, LED.BRIGHTNESS_50);
	        LED.setState(LED.STATE_BLINKING);
            // #sijapp cond.end#
			// #sijapp cond.if target is "MOTOROLA"#
			if (Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_MODE) == 0)
			Jimm.display.flashBacklight(1000);
			// #sijapp cond.end#
			this.repaint();
		}
	}


	// Called when a key is pressed
	protected void keyPressed(int keyCode)
	{
		if (this.isLocked)
		{
		    if (keyCode == Canvas.KEY_POUND)
		        this.pressed = new Date();
		    else
		    {
		        this.showKeylock = true;
                this.repaint();
			//  #sijapp cond.if target is "MOTOROLA"#
		       Jimm.display.flashBacklight(2000);
			// #sijapp cond.end#
		    }
		}
	}


	// Called when a key is repeated (held down)
	protected void keyRepeated(int keyCode)
	{
		if (this.isLocked && (keyCode == Canvas.KEY_POUND))
		{
			if ((this.pressed.getTime() + 1000) < System.currentTimeMillis())
			{
				this.unlock();
			}
		}
	}


	// Called when a key is released
	protected void keyReleased(int keyCode)
	{
		if (this.isLocked && (keyCode == Canvas.KEY_POUND))
		{
			if ((this.pressed.getTime() + 1000) < System.currentTimeMillis())
			{
				this.unlock();
			}
		}
	}

	// Render the splash image
	protected void paint(Graphics g)
	{	
        // Do we need to draw the splash image?
		if (g.getClipY() < this.getHeight() - SplashCanvas.height - 2)
		{
			// Draw background
			g.drawImage(background,0,0, Graphics.LEFT | Graphics.TOP);

			// Display splash image (or text)
			if (SplashCanvas.splash != null)
			{
				g.drawImage(SplashCanvas.splash, this.getWidth() / 2, this.getHeight() / 2, Graphics.HCENTER | Graphics.VCENTER);
			}
			else
			{
				g.setColor(255, 255, 255);
				g.setFont(SplashCanvas.logoFont);
				g.drawString("jimm", this.getWidth() / 2, this.getHeight() / 2 + 5, Graphics.HCENTER | Graphics.BASELINE);
				g.setFont(SplashCanvas.font);
			}

			// Display notice image (or nothing)
			if (SplashCanvas.notice != null)
			{
				g.drawImage(SplashCanvas.notice, this.getWidth() / 2, 2, Graphics.HCENTER | Graphics.TOP);
			}

			// Display message icon, if keylock is enabled
			if (this.isLocked && this.availableMessages > 0)
			{
				g.drawImage(ContactList.eventPlainMessageImg, 1, this.getHeight()-(2*SplashCanvas.height)-9, Graphics.LEFT | Graphics.TOP);
				g.setColor(255, 255, 255);
				g.setFont(SplashCanvas.font);
				g.drawString("# " + this.availableMessages, ContactList.eventPlainMessageImg.getWidth() + 4, this.getHeight()-(2*SplashCanvas.height)-5, Graphics.LEFT | Graphics.TOP);
			}
            
            // Display the keylock message if someone hit the wrong key
            if (this.showKeylock)
            {
                
                // Init the dimensions
                int x,y,size_x,size_y;
                size_x = this.getWidth()/10*8;
                size_y = Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM).getHeight()*TextList.getLineNumbers(ResourceBundle.getString("keylock_message"),size_x-8,0,0,0)+8;
                x = this.getWidth()/2-(this.getWidth()/10*4);
                y = this.getHeight()/2-(size_y/2);
                
                
                g.setColor(255, 255, 255);
                g.fillRect(x,y,size_x,size_y);
                g.setColor(0,0,0);
                g.drawRect(x+2,y+2,size_x-5,size_y-5);
                TextList.showText(g,ResourceBundle.getString("keylock_message"),x+4,y+4,size_x-8,size_y-8,TextList.MEDIUM_FONT,0,0);
                
                t2 = new Timer();
                t2.schedule(new TimerTask()
                {

                    public void run()
                    {
                        SplashCanvas.this.showKeylock = false;
                        SplashCanvas.this.repaint();
                    }
                }, 3000);
            }

		}

		// Draw white bottom bar
		g.setColor(255, 255, 255);
		g.setStrokeStyle(Graphics.DOTTED);
		g.drawLine(0, this.getHeight() - SplashCanvas.height - 3, this.getWidth(), this.getHeight() - SplashCanvas.height - 3);

		// Draw message
		if (Jimm.jimm.getOptionsRef() != null)
		{
			g.setColor(255, 255, 255);
			g.setFont(SplashCanvas.font);
			// Draw the date bellow notice if set up to do so
			if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_DISPLAY_DATE))
			{
				g.drawString(Util.getDateString(false), this.getWidth() / 2, 12, Graphics.TOP | Graphics.HCENTER);
			}
			// Draw the progressbar message
			if ((this.message != null) && (this.message.length() > 0))
			{
				g.drawString(this.message, this.getWidth() / 2, this.getHeight(), Graphics.BOTTOM | Graphics.HCENTER);
			}
		}
		
		// Draw version
		if (this.version)
		{
		    g.setColor(0,0,0);
		    g.setFont(versionFont);
		    g.drawString(Jimm.VERSION,this.getWidth()-3,this.getHeight()-SplashCanvas.height-5, Graphics.BOTTOM | Graphics.RIGHT);
		}

		// Draw current progress
		int progressPx = this.getWidth() * this.progress / 100;
		g.setClip(0, this.getHeight() - SplashCanvas.height - 2, progressPx, SplashCanvas.height + 2);
		g.setColor(255, 255, 255);
		g.fillRect(0, this.getHeight() - SplashCanvas.height - 2, progressPx, SplashCanvas.height + 2);
		if (Jimm.jimm.getOptionsRef() != null)
		{
			g.setColor(0, 0, 0);
			// Draw the date bellow notice if set up to do so
			if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_DISPLAY_DATE))
			{
				g.drawString(Util.getDateString(false), this.getWidth() / 2, 12, Graphics.TOP | Graphics.HCENTER);
			}
				
			// Draw the progressbar message
			g.drawString(this.message, this.getWidth() / 2, this.getHeight(), Graphics.BOTTOM | Graphics.HCENTER);
		}

	}


	/*****************************************************************************/
	/*****************************************************************************/
	/*****************************************************************************/


	// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    // #sijapp cond.if modules_FILES is "true"#

	// Activates the contact list after connection has been established
	public static class FileTransferTimerTask extends TimerTask implements CommandListener
	{

		// Reference to ConnectAction
		private DirectConnectionAction dcAct;

		// Cancel Command
		private Command cancelCommand;

		// Constructor
		public FileTransferTimerTask(DirectConnectionAction _dcAct)
		{
		    
			this.dcAct = _dcAct;

			// Set the cancel command
			cancelCommand = new Command(ResourceBundle.getString("cancel"),Command.CANCEL,1);
			Jimm.jimm.getSplashCanvasRef().addCommand(cancelCommand);
			Jimm.jimm.getSplashCanvasRef().setCommandListener(this);

			// Activate the splash screen
			Jimm.jimm.getSplashCanvasRef().setMessage(ResourceBundle.getString("filetransfer"));
			Jimm.display.setCurrent(Jimm.jimm.getSplashCanvasRef());

		}


		// Command listener
		public void commandAction(Command c, Displayable d)
		{
			if (c == this.cancelCommand)
			{
			    Jimm.jimm.getContactListRef().activate();
				Jimm.jimm.getSplashCanvasRef().removeCommand(cancelCommand);
				this.dcAct.setCancel(true);
				Jimm.jimm.getContactListRef().activate();
				this.cancel();
			}
		}


		// Timer routine
		public void run()
		{
		    Jimm.jimm.getSplashCanvasRef().setProgress(this.dcAct.getProgress());
			if (this.dcAct.isCompleted())
			{
			    Jimm.jimm.getContactListRef().activate();
			    Jimm.jimm.getSplashCanvasRef().removeCommand(cancelCommand);
				this.cancel();
				Alert ok = new Alert(ResourceBundle.getString("filetransfer"),ResourceBundle.getString("filetransfer")+" "+ResourceBundle.getString("was")+" "+ResourceBundle.getString("successful")+".\n"+ResourceBundle.getString("speed")+": "+this.dcAct.getSpeed()+" "+ResourceBundle.getString("kbs"),null, AlertType.INFO);
				ok.setTimeout(2000);
				Jimm.display.setCurrent(ok);
			}
			else if (this.dcAct.isError())
			{
			    Jimm.jimm.getContactListRef().activate();
			    Jimm.jimm.getSplashCanvasRef().removeCommand(cancelCommand);
				this.cancel();
				Alert err = new Alert(ResourceBundle.getString("filetransfer"),ResourceBundle.getString("filetransfer")+" "+ResourceBundle.getString("was")+" "+ResourceBundle.getString("not")+" "+ResourceBundle.getString("successful")+"!",null, AlertType.WARNING);
				Jimm.display.setCurrent(err);
			}
		}
		
	}
	//#sijapp cond.end#
    //#sijapp cond.end#


	/*****************************************************************************/
	/*****************************************************************************/
	/*****************************************************************************/


	// Activates the contact list after connection has been established
	public static class ConnectTimerTask extends TimerTask
	{


		// Reference to ConnectAction
		private ConnectAction connectAct;


		// Constructor
		public ConnectTimerTask(ConnectAction connectAct)
		{
			this.connectAct = connectAct;
		}


		// Timer routine
		public void run()
		{
		    Jimm.jimm.getSplashCanvasRef().setProgress(this.connectAct.getProgress());
			if (this.connectAct.isCompleted())
			{
			    Jimm.jimm.getContactListRef().activate();
				this.cancel();
			}
			else if (this.connectAct.isError())
			{
			    this.cancel();
			}
		}


	}


	/*****************************************************************************/
	/*****************************************************************************/
	/*****************************************************************************/


	// Activates the main menu after connection has been terminated
	public static class DisconnectTimerTask extends TimerTask
	{


		// Reference to DisconnectAction
		private DisconnectAction disconnectAct;


		// Exit after disconnecting?
		private boolean exit;


		// Constructor
		public DisconnectTimerTask(DisconnectAction disconnectAct, boolean exit)
		{
			this.disconnectAct = disconnectAct;
			this.exit = exit;
		}


		// Timer routine
		public void run()
		{
			if (this.disconnectAct.isCompleted())
			{
				this.cancel();
				if (this.exit)
				{
					try
					{
						Jimm.jimm.destroyApp(true);
					}
					catch (MIDletStateChangeException e)
					{
						/* Do nothing */
					}
				}
				else
				{
					Jimm.jimm.getMainMenuRef().activate();
				}
			}
			else if (this.disconnectAct.isError())
			{
				this.cancel();
			}
		}


	}


	/*****************************************************************************/
	/*****************************************************************************/
	/*****************************************************************************/
	
	// Waits until meta information is available
	public static class RequestInfoTimerTask extends TimerTask implements CommandListener
	{
		TextList tl = new TextList(null);
		Command cmdBack = new Command(ResourceBundle.getString("back"), Command.BACK, 0); 
		
		public void commandAction(Command c, Displayable d)
		{
			if (c == cmdBack) Jimm.jimm.getContactListRef().activate();
		}
		
		int bigTextIndex = 0;
		
		private void startInfoSect(String name)
		{
			bigTextIndex++;
			tl.addBigText
			(
				ResourceBundle.getString(name),
				tl.getTextColor(),
				Font.STYLE_BOLD,
				bigTextIndex
			).doCRLF();
		}

		private void addToTextList(int index, String langStr)
		{
			String data = requestInfoAct.getStringData(index);
			if (data.length() == 0) return;
			tl.addBigText(ResourceBundle.getString(langStr)+": ", tl.getTextColor(), Font.STYLE_PLAIN, bigTextIndex)
			  .addBigText(data, Jimm.jimm.getOptionsRef().getSchemeColor(Options.CLRSCHHEME_BLUE), Font.STYLE_PLAIN, bigTextIndex)
			  .doCRLF();
		}
		
		// Reference to RequestInfoAction
		private RequestInfoAction requestInfoAct;


		// Constructor
		public RequestInfoTimerTask(RequestInfoAction requestInfoAct)
		{
			this.requestInfoAct = requestInfoAct;
			JimmUI.setColorScheme(tl);
			tl.addCommand(cmdBack);
			tl.setCursorMode(TextList.SEL_NONE);
			tl.setCommandListener(this);
		}


		// Timer routine
		public void run()
		{
			if (this.requestInfoAct.isCompleted())
			{
				tl.clear();
				
				// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
				tl.setTitle(requestInfoAct.getStringData(RequestInfoAction.UIN));
				tl.setFullScreenMode(false);
				tl.setFontSize(Font.SIZE_MEDIUM);
				// #sijapp cond.else#
				tl.setCaption(requestInfoAct.getStringData(RequestInfoAction.UIN));
				tl.setFontSize(Font.SIZE_SMALL);
				// #sijapp cond.end#
				
				startInfoSect("main_info");
				addToTextList(RequestInfoAction.NICK,      "nick");
				addToTextList(RequestInfoAction.NAME,      "name");
				addToTextList(RequestInfoAction.GENDER,    "gender");
				addToTextList(RequestInfoAction.AGE,       "age");
				addToTextList(RequestInfoAction.EMAIL,     "email");
				addToTextList(RequestInfoAction.BDAY,      "birth_day");
				addToTextList(RequestInfoAction.CPHONE,    "cell_phone");
				addToTextList(RequestInfoAction.HOME_PAGE, "home_page");
				addToTextList(RequestInfoAction.ABOUT,     "notes");
				addToTextList(RequestInfoAction.INETRESTS, "interests");
				
				startInfoSect("home_info");
				addToTextList(RequestInfoAction.CITY,      "city");
				addToTextList(RequestInfoAction.STATE,     "state");
				addToTextList(RequestInfoAction.ADDR,      "addr");
				addToTextList(RequestInfoAction.PHONE,     "phone");
				addToTextList(RequestInfoAction.FAX,       "fax");
				
				startInfoSect("work_info");
				addToTextList(RequestInfoAction.W_NAME,    "title");
				addToTextList(RequestInfoAction.W_DEP,     "depart");
				addToTextList(RequestInfoAction.W_POS,     "position");
				addToTextList(RequestInfoAction.W_CITY,    "city");
				addToTextList(RequestInfoAction.W_STATE,   "state");
				addToTextList(RequestInfoAction.W_ADDR,    "addr");
				addToTextList(RequestInfoAction.W_PHONE,   "phone");
				addToTextList(RequestInfoAction.W_FAX,     "fax");
				
				Jimm.display.setCurrent(tl);
				
				this.cancel();
			}
			else if (this.requestInfoAct.isError())
			{
			    
			    Jimm.jimm.getContactListRef().activate(JimmException.handleException(new JimmException(160, 0, true)));
			    this.cancel();
			}
		}


	}


	/*****************************************************************************/
	/*****************************************************************************/
	/*****************************************************************************/


	// Waits until contact listupdate is completed
	public static class UpdateContactListTimerTask extends TimerTask
	{


		// Reference to UpdateContactListAction
		private UpdateContactListAction updateContactListAct;


		// Constructor
		public UpdateContactListTimerTask(UpdateContactListAction updateContactListAct)
		{
			this.updateContactListAct = updateContactListAct;
		}


		// Timer routine
		public void run()
		{
			if (this.updateContactListAct.isCompleted())
			{
				Jimm.jimm.getContactListRef().activate();
				this.cancel();
			}
			else if (this.updateContactListAct.isError())
			{
				if (this.updateContactListAct.getErrorType() == 0 )
				{
					Jimm.jimm.getContactListRef().activate(JimmException.handleException(new JimmException(154, 2, true)));
				}
				else
				{
				Jimm.jimm.getContactListRef().activate();
				}
				this.cancel();
			}
		}


	}


	/*****************************************************************************/
	/*****************************************************************************/
	/*****************************************************************************/


	// Waits until contact listupdate is completed
	public static class SearchTimerTask extends TimerTask
	{


		// Reference to UpdateContactListAction
		private SearchAction searchAct;


		// Constructor
		public SearchTimerTask(SearchAction _searchAct)
		{
			this.searchAct = _searchAct;
		}


		// Timer routine
		public void run()
		{
			if (this.searchAct.isCompleted())
			{
				searchAct.activateResult();
				this.cancel();
			} else if (this.searchAct.isError()) {
				if (!searchAct.excepHandled()) Jimm.jimm.getContactListRef().activate(JimmException.handleException(new JimmException(154, 2, true)));
				{
					this.cancel();
				}
			}
		}


	}

}
