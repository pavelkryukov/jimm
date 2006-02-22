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
import jimm.comm.Util;
import jimm.comm.Icq;
import jimm.comm.Action;
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
	
	public final static Command cancelCommnad = new Command(ResourceBundle.getString("Cancel"), Command.BACK, 1);
	
	//Timer for repaint
	static private Timer t1,t2;

	// Location of the splash image (inside the JAR file)
	private static final String SPLASH_IMG = "/logo.png";

	// Image object, holds the splash image
	private static Image splash;

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
	static protected boolean showKeylock;

	//
	static protected boolean unlockPressed;
	
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
	
	static public Image getSplashImage()
	{
		if (SplashCanvas.splash == null)
		{
			try
			{
				SplashCanvas.splash = Image.createImage(SplashCanvas.SPLASH_IMG);
			}
			catch (Exception e)
			{
				SplashCanvas.splash = null;
			}
		}
		return SplashCanvas.splash;
	}


	// Sets the current progress in percent (and request screen refresh)
	static public synchronized void setProgress(int progress)
	{
		if (SplashCanvas.progress == progress) return;
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
		if (Options.getBoolean(Options.OPTION_DISPLAY_DATE)) (t2 = new Timer()).schedule(new TimerTasks(TimerTasks.SC_AUTO_REPAINT), 20000, 20000);
		
	}


	// Disable keylock
	static public synchronized void unlock()
	{
		isLocked = false;
		availableMessages = 0;
        // #sijapp cond.if target is "RIM"#
        LED.setState(LED.STATE_OFF);
        //  #sijapp cond.end#
        	//  #sijapp cond.if target is "MOTOROLA"#
		if (Options.getBoolean(Options.OPTION_LIGHT_MANUAL))
		{
			LightControl.On();
		}
		//  #sijapp cond.end#
		if (Options.getBoolean(Options.OPTION_DISPLAY_DATE))
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
			if (Options.getInt(Options.OPTION_MESS_NOTIF_MODE) == 0)
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
		    if (keyCode == Canvas.KEY_POUND) {
		        unlockPressed = true;
				(t1 = new Timer()).schedule(new TimerTasks(TimerTasks.SC_UNLOCK), 900);
		    } else
		    {
				if (t1 != null) t1.cancel();
		        showKeylock = true;
                this.repaint();
			//  #sijapp cond.if target is "MOTOROLA"#
		       Jimm.display.flashBacklight(2000);
			// #sijapp cond.end#
		    }
		}
	}

	// Called when a key is released
	protected void keyReleased(int keyCode)
	{
		unlockPressed = false;
	}

	// Render the splash image
	protected void paint(Graphics g)
	{	
        // Do we need to draw the splash image?
		if (g.getClipY() < this.getHeight() - SplashCanvas.height - 2)
		{
			// Draw background
			g.setColor(0,111,177);
			g.fillRect(0,0,this.getWidth(),this.getHeight());

			// Display splash image (or text)
			Image image = getSplashImage();
			if (image != null)
			{
				g.drawImage(image, this.getWidth() / 2, this.getHeight() / 2, Graphics.HCENTER | Graphics.VCENTER);
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
                
				(t1 = new Timer()).schedule(new TimerTasks(TimerTasks.SC_HIDE_KEYLOCK), 2000);

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
			if (Options.getBoolean(Options.OPTION_DISPLAY_DATE))
			{
				g.drawString(Util.getDateString(false), this.getWidth() / 2, 12, Graphics.TOP | Graphics.HCENTER);
				g.drawString(Util.getCurrentDay(), this.getWidth() / 2, 13+SplashCanvas.font.getHeight(), Graphics.TOP | Graphics.HCENTER);
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
		    g.setColor(255,255,255);
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
			if (Options.getBoolean(Options.OPTION_DISPLAY_DATE))
			{
				g.drawString(Util.getDateString(false), this.getWidth() / 2, 12, Graphics.TOP | Graphics.HCENTER);
			}
				
			// Draw the progressbar message
			g.drawString(message, this.getWidth() / 2, this.getHeight(), Graphics.BOTTOM | Graphics.HCENTER);
		}

	}

	public static void addTimerTask(String captionLngStr, Action action, boolean canCancel)
	{
		TimerTasks timerTask = new TimerTasks(action); 
		
		SplashCanvas._this.removeCommand(SplashCanvas.cancelCommnad);
		if (canCancel)
		{
			SplashCanvas._this.addCommand(SplashCanvas.cancelCommnad);
			SplashCanvas._this.setCommandListener(timerTask);
		}
		
		SplashCanvas.setMessage(ResourceBundle.getString(captionLngStr));
		SplashCanvas.setProgress(0);
		Jimm.display.setCurrent(SplashCanvas._this);
		
		Jimm.jimm.getTimerRef().schedule(timerTask, 1000, 1000);
	}
}

class TimerTasks extends TimerTask implements CommandListener
{
	public static final int SC_AUTO_REPAINT = 1;
	public static final int SC_HIDE_KEYLOCK = 2;
	public static final int SC_UNLOCK = 3;
	private int type = -1;

	private Action action;
	boolean wasError = false;
	
	public TimerTasks(Action action)
	{
		this.action = action;
	}
	
	public TimerTasks(int type)
	{
		this.type = type;
	}
	
	public void run()
	{
		if (wasError) return;
		if (type != -1) {
			switch (type) {
				case SC_AUTO_REPAINT:
					SplashCanvas._this.repaint();
					break;
				case SC_HIDE_KEYLOCK:
					SplashCanvas.showKeylock = false;
					SplashCanvas._this.repaint();
					break;
				case SC_UNLOCK:
					if (SplashCanvas.unlockPressed) {
						SplashCanvas.unlockPressed = false;
						SplashCanvas.unlock();
					}
					break;
			}
			return;
		}

		SplashCanvas.setProgress(action.getProgress());
		if (action.isCompleted())
		{
			cancel();
			action.onEvent(Action.ON_COMPLETE);
		}
		else if (action.isError())
		{
			wasError = true;
			cancel();
			action.onEvent(Action.ON_ERROR);
		}
	}
	
	public void commandAction(Command c, Displayable d)
	{
		if (c == SplashCanvas.cancelCommnad)
		{
			action.onEvent(Action.ON_CANCEL);
			cancel();
		}
	}
}
