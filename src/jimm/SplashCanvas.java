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
	static public SplashCanvas _this;
	
	//Timer for repaint
	static private Timer t1,t2;

	// Location of the splash image (inside the JAR file)
	private static final String SPLASH_IMG = "/splash.png";

	// Image object, holds the splash image
	private static Image splash;

	// Image object, holds the background image
	// #sijapp cond.if target isnot  "DEFAULT" & target isnot "SIEMENS1"#
	private static Image background;
	// #sijapp cond.end#

	// Location of the notice image (inside the JAR file)
	private static final String NOTICE_IMG = "/notice.png";

	// Image object, holds the notice image
	private static Image notice;

	// Font used to display the logo (if image is not available)
	private static Font logoFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
	
	// Font used to display the version nr
	private static Font versionFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

	// Font (and font height in pixels) used to display informational messages
	private static Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
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
	static private String message;


	// Progress in percent
	static private int progress; // = 0


	// True if keylock has been enabled
	static private boolean isLocked;


	// Number of available messages
	static private int availableMessages;
	

	// Should the keylock message be drawn to the screen?
	static private boolean showKeylock;


	// Timestamp
	static private Date pressed; // = null
	
	// Version string
	static private boolean version;
	
	// Constructor
	public SplashCanvas(String message)
	{
		_this = this;
	    version = true;
	    //  #sijapp cond.if target is "MIDP2"#
		setFullScreenMode(!Jimm.is_phone_SE());
		//  #sijapp cond.end#
		
	    //  #sijapp cond.if target is "MOTOROLA" | target is "SIEMENS2"#
		setFullScreenMode(true);
		//  #sijapp cond.end#
		
		
		message = new String(message);
		showKeylock = false;
		// #sijapp cond.if target isnot  "DEFAULT" & target isnot "SIEMENS1"#
		// #sijapp cond.if target is "MOTOROLA"#
		SplashCanvas.background = Image.createImage(this.getWidth(), this.getHeight()+22);
		// #sijapp cond.else#
		SplashCanvas.background = Image.createImage(this.getWidth(), this.getHeight());
		// #sijapp cond.end#
		Graphics bg_graph = background.getGraphics();

		int r, g;
		for (int x = 0; x < this.getWidth(); x+=2)
		{
			int y;
			// #sijapp cond.if target is "MOTOROLA"#
			for (y = 0; y < this.getHeight()+22; y+=2)
			// #sijapp cond.else#
			for (y = 0; y < this.getHeight(); y+=2)
			// #sijapp cond.end#
			{
				r = x * y / (y + x + 1) % 256;
				g = ((r ^ x ^ y)) % 256;
				bg_graph.setColor(r, g, (r + g) % 256);
				bg_graph.fillRect(x, y, 2, 2);
			}
		}
		//  #sijapp cond.end#
	}


	// Constructor, blank message
	public SplashCanvas()
	{
		this("");
		_this = this;
	}


	// Returns the informational message
	static public synchronized String getMessage()
	{
		return (message);
	}


	// Sets the informational message
	static public synchronized void setMessage(String message)
	{
		SplashCanvas.message = new String(message);
		progress = 0;
	}


	// Returns the current progress in percent
	static public synchronized int getProgress()
	{
		return (progress);
	}


	// Sets the current progress in percent (and request screen refresh)
	static public synchronized void setProgress(int progress)
	{
		SplashCanvas.progress = progress;
		_this.repaint(0, _this.getHeight() - SplashCanvas.height - 2, _this.getWidth(), SplashCanvas.height + 2);
	}
	
	
	
	static public void delVersionString()
	{
	    version = false;
	}
	

	// Enable keylock
	static public synchronized void lock()
	{
		setMessage(ResourceBundle.getString("keylock_enabled"));
		setProgress(0);
		isLocked = true;
		//  #sijapp cond.if target is "MOTOROLA"#
		LightControl.Off();
		//  #sijapp cond.end#
		Jimm.display.setCurrent(_this);
		if (Options.getBooleanOption(Options.OPTION_DISPLAY_DATE))
        {
            t1 = new Timer();
            t1.schedule(new TimerTask()
            {

                public void run()
                {
                    _this.repaint();
                }
            }, 20000, 20000);
        }
		
	}


	// Disable keylock
	static private synchronized void unlock()
	{
		isLocked = false;
		availableMessages = 0;
        // #sijapp cond.if target is "RIM"#
        LED.setState(LED.STATE_OFF);
        //  #sijapp cond.end#
        	//  #sijapp cond.if target is "MOTOROLA"#
		if (Options.getBooleanOption(Options.OPTION_LIGHT_MANUAL))
		{
			LightControl.On();
		}
		//  #sijapp cond.end#
		if (Options.getBooleanOption(Options.OPTION_DISPLAY_DATE))
		{
			
			t1.cancel();
		}
		ContactList.activate();
	}
    
    // Is the screen locked?
	static public boolean locked()
    {
        return(isLocked);
    }


	// Called when message has been received
	static public synchronized void messageAvailable()
	{
		if (isLocked)
		{
			++availableMessages;
			// #sijapp cond.if target is "RIM"#
	        LED.setConfiguration(500, 250, LED.BRIGHTNESS_50);
	        LED.setState(LED.STATE_BLINKING);
            // #sijapp cond.end#
			// #sijapp cond.if target is "MOTOROLA"#
			if (Options.getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_MODE) == 0)
			Jimm.display.flashBacklight(1000);
			// #sijapp cond.end#
			_this.repaint();
		}
	}


	// Called when a key is pressed
	protected void keyPressed(int keyCode)
	{
		if (isLocked)
		{
		    if (keyCode == Canvas.KEY_POUND)
		        pressed = new Date();
		    else
		    {
		        showKeylock = true;
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
		if (isLocked && (keyCode == Canvas.KEY_POUND))
		{
			if ((pressed.getTime() + 1000) < System.currentTimeMillis())
			{
				unlock();
			}
		}
	}


	// Called when a key is released
	protected void keyReleased(int keyCode)
	{
		if (isLocked && (keyCode == Canvas.KEY_POUND))
		{
			if ((pressed.getTime() + 1000) < System.currentTimeMillis())
			{
				unlock();
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
			// #sijapp cond.if target isnot  "DEFAULT" & target isnot "SIEMENS1"#
			g.drawImage(background,0,0, Graphics.LEFT | Graphics.TOP);
			// #sijapp cond.else#
			g.setColor(0,0,0);
			g.fillRect(0,0,this.getWidth(),this.getHeight());
			// #sijapp cond.end#

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
			if (isLocked && availableMessages > 0)
			{
				g.drawImage(ContactList.eventPlainMessageImg, 1, this.getHeight()-(2*SplashCanvas.height)-9, Graphics.LEFT | Graphics.TOP);
				g.setColor(255, 255, 255);
				g.setFont(SplashCanvas.font);
				g.drawString("# " + availableMessages, ContactList.eventPlainMessageImg.getWidth() + 4, this.getHeight()-(2*SplashCanvas.height)-5, Graphics.LEFT | Graphics.TOP);
			}
            
            // Display the keylock message if someone hit the wrong key
            if (showKeylock)
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
                        showKeylock = false;
                        repaint();
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
			if (Options.getBooleanOption(Options.OPTION_DISPLAY_DATE))
			{
				g.drawString(Util.getDateString(false), this.getWidth() / 2, 12, Graphics.TOP | Graphics.HCENTER);
			}
			// Draw the progressbar message
			if ((message != null) && (message.length() > 0))
			{
				g.drawString(message, this.getWidth() / 2, this.getHeight(), Graphics.BOTTOM | Graphics.HCENTER);
			}
		}
		
		// Draw version
		if (version)
		{
		    g.setColor(0,0,0);
		    g.setFont(versionFont);
		    g.drawString(Jimm.VERSION,this.getWidth()-3,this.getHeight()-SplashCanvas.height-5, Graphics.BOTTOM | Graphics.RIGHT);
		}

		// Draw current progress
		int progressPx = this.getWidth() * progress / 100;
		g.setClip(0, this.getHeight() - SplashCanvas.height - 2, progressPx, SplashCanvas.height + 2);
		g.setColor(255, 255, 255);
		g.fillRect(0, this.getHeight() - SplashCanvas.height - 2, progressPx, SplashCanvas.height + 2);
		if (Jimm.jimm.getOptionsRef() != null)
		{
			g.setColor(0, 0, 0);
			// Draw the date bellow notice if set up to do so
			if (Options.getBooleanOption(Options.OPTION_DISPLAY_DATE))
			{
				g.drawString(Util.getDateString(false), this.getWidth() / 2, 12, Graphics.TOP | Graphics.HCENTER);
			}
				
			// Draw the progressbar message
			g.drawString(message, this.getWidth() / 2, this.getHeight(), Graphics.BOTTOM | Graphics.HCENTER);
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
		static private DirectConnectionAction dcAct;

		// Cancel Command
		static private Command cancelCommand;

		// Constructor
		public FileTransferTimerTask(DirectConnectionAction _dcAct)
		{
		    
			FileTransferTimerTask.dcAct = _dcAct;

			// Set the cancel command
			cancelCommand = new Command(ResourceBundle.getString("cancel"),Command.CANCEL,1);
			Jimm.jimm.getSplashCanvasRef().addCommand(cancelCommand);
			Jimm.jimm.getSplashCanvasRef().setCommandListener(this);

			// Activate the splash screen
			SplashCanvas.setMessage(ResourceBundle.getString("filetransfer"));
			Jimm.display.setCurrent(Jimm.jimm.getSplashCanvasRef());

		}


		// Command listener
		public void commandAction(Command c, Displayable d)
		{
			if (c == cancelCommand)
			{
			    ContactList.activate();
				Jimm.jimm.getSplashCanvasRef().removeCommand(cancelCommand);
				dcAct.setCancel(true);
				ContactList.activate();
				this.cancel();
			}
		}


		// Timer routine
		public void run()
		{
		    SplashCanvas.setProgress(dcAct.getProgress());
			if (dcAct.isCompleted())
			{
			    ContactList.activate();
			    Jimm.jimm.getSplashCanvasRef().removeCommand(cancelCommand);
				this.cancel();
				Alert ok = new Alert(ResourceBundle.getString("filetransfer"),ResourceBundle.getString("filetransfer")+" "+ResourceBundle.getString("was")+" "+ResourceBundle.getString("successful")+".\n"+ResourceBundle.getString("speed")+": "+dcAct.getSpeed()+" "+ResourceBundle.getString("kbs"),null, AlertType.INFO);
				ok.setTimeout(2000);
				Jimm.display.setCurrent(ok);
			}
			else if (dcAct.isError())
			{
			    ContactList.activate();
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
		static private ConnectAction connectAct;


		// Constructor
		public ConnectTimerTask(ConnectAction connectAct)
		{
			ConnectTimerTask.connectAct = connectAct;
		}


		// Timer routine
		public void run()
		{
		    SplashCanvas.setProgress(connectAct.getProgress());
			if (connectAct.isCompleted())
			{
			    ContactList.activate();
				this.cancel();
			}
			else if (connectAct.isError())
			{
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
		static private UpdateContactListAction updateContactListAct;

		// Constructor
		public UpdateContactListTimerTask(UpdateContactListAction updateContactListAct)
		{
			UpdateContactListTimerTask.updateContactListAct = updateContactListAct;
		}


		// Timer routine
		public void run()
		{
			if (updateContactListAct.isCompleted())
			{
				ContactList.activate();
				this.cancel();
			}
			else if (updateContactListAct.isError())
			{
				if (updateContactListAct.getErrorType() == 0 )
				{
					ContactList.activate(JimmException.handleException(new JimmException(154, 2, true)));
				}
				else
				{
				ContactList.activate();
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
		static private SearchAction searchAct;

		// Constructor
		public SearchTimerTask(SearchAction _searchAct)
		{
			SearchTimerTask.searchAct = _searchAct;
		}


		// Timer routine
		public void run()
		{
			if (searchAct.isCompleted())
			{
				searchAct.activateResult();
				this.cancel();
			} else if (searchAct.isError()) {
				if (!searchAct.excepHandled()) ContactList.activate(JimmException.handleException(new JimmException(154, 2, true)));
				{
					this.cancel();
				}
			}
		}


	}

}
