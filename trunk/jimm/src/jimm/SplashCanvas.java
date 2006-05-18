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
//  #sijapp cond.if target is "MOTOROLA"#
import DrawControls.VirtualList;
//  #sijapp cond.end#
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

//#sijapp cond.if target is "RIM"#
import net.rim.device.api.system.LED;
//#sijapp cond.end#


public class SplashCanvas extends Canvas
{
	static private SplashCanvas _this;
	
	public final static Command cancelCommnad = new Command(ResourceBundle.getString("cancel"), Command.BACK, 1);
	
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
	
	// Time since last key # pressed 
	static private long poundPressTime;

	// Should the keylock message be drawn to the screen?
	static protected boolean showKeylock;

	static private int status_index = -1;

	// Constructor
	public SplashCanvas(String message)
	{
		_this = this;
	    //  #sijapp cond.if target is "MIDP2"#
		setFullScreenMode(!Jimm.is_phone_SE());
		//  #sijapp cond.end#
	    //  #sijapp cond.if target is "MOTOROLA" | target is "SIEMENS2"#
		setFullScreenMode(true);
		//  #sijapp cond.end#
		setMessage(message);
		showKeylock = false;
	}

	// Constructor, blank message
	public SplashCanvas()
	{
		this(null);
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
		setProgress(0);
	}

	public static synchronized void setStatusToDraw(int st_index)
	{
		status_index = st_index;
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
	
	static public void show()
	{
		if (t2 != null)
		{
			t2.cancel();
			t2 = null;
		}
		Jimm.display.setCurrent(_this);
		isLocked = false;
	}
	
	static public void addCmd(Command cmd)
	{
		_this.addCommand(cmd);
	}
	
	static public void removeCmd(Command cmd)
	{
		_this.removeCommand(cmd);
	}
	
	static public void setCmdListener(CommandListener l)
	{
		_this.setCommandListener(l);
	}
	
	static public void Repaint()
	{
		_this.repaint();
	}

	// Sets the current progress in percent (and request screen refresh)
	static public synchronized void setProgress(int progress)
	{
		if (SplashCanvas.progress == progress) return;
		SplashCanvas.progress = progress;
		//#sijapp cond.if target is "MIDP2"#
		_this.repaint();
		//#sijapp cond.else#
		_this.repaint(0, _this.getHeight() - SplashCanvas.height - 2, _this.getWidth(), SplashCanvas.height + 2);
		//#sijapp cond.end#
	}
	
	// Enable keylock
	static public synchronized void lock()
	{
		if (isLocked) return;
		
		isLocked = true;
		//  #sijapp cond.if target is "MOTOROLA"#
		VirtualList.setBkltOn(false);
		//  #sijapp cond.end#
		setMessage(ResourceBundle.getString("keylock_enabled"));
		setStatusToDraw(JimmUI.getStatusImageIndex(Icq.getCurrentStatus()));
		Jimm.display.setCurrent(_this);
		
		if (Options.getBoolean(Options.OPTION_DISPLAY_DATE))
		{
			(t2 = new Timer()).schedule(new TimerTasks(TimerTasks.SC_AUTO_REPAINT), 20000, 20000);
		}
		
	}


	// Disable keylock
	static public synchronized void unlock()
	{
		if (!isLocked) return;
		
		isLocked = false;
		availableMessages = 0;
        // #sijapp cond.if target is "RIM"#
        LED.setState(LED.STATE_OFF);
        //  #sijapp cond.end#
        	//  #sijapp cond.if target is "MOTOROLA"#
		if (Options.getBoolean(Options.OPTION_LIGHT_MANUAL)) VirtualList.setBkltOn(true);
		VirtualList.disableLED();
		//  #sijapp cond.end#
		if (Options.getBoolean(Options.OPTION_DISPLAY_DATE) && (t2 != null)) t2.cancel();
		
		if (_this.isShown()) ContactList.activate();
	}
    
    // Is the screen locked?
	static public boolean locked()
    {
        return (isLocked);
    }

	protected void hideNotify()
	{
		SplashCanvas.splash = null;
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
			if (Jimm.funlight_device_type != 0)
			{
				VirtualList.setLEDmode(VirtualList.BKLT_TYPE_LIGHTING, 1000, 0xFFAA00);
			}
			else
			{
				VirtualList.flashBklt(1000);
			}
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
			{
		        poundPressTime = System.currentTimeMillis();
		    }
			else
		    {
				if (t1 != null) t1.cancel();
		        showKeylock = true;
                this.repaint();
			//  #sijapp cond.if target is "MOTOROLA"#
				VirtualList.flashBklt(2500);
				VirtualList.setLEDmode(VirtualList.BKLT_TYPE_LIGHTING, 2500, JimmUI.st_colors[JimmUI.getStatusIndex(Icq.getCurrentStatus())]);
			// #sijapp cond.end#
		    }
		}
	}
	
	private void tryToUnlock(int keyCode)
	{
		if (!isLocked) return;
		if (keyCode != Canvas.KEY_POUND)
		{
			poundPressTime = 0;
			return;
		}
		
	
		if ((poundPressTime != 0) && ((System.currentTimeMillis()-poundPressTime) > 900))
		{
			unlock();
			poundPressTime = 0;
		}
	}

	// Called when a key is released
	protected void keyReleased(int keyCode)
	{
		tryToUnlock(keyCode);
	}
	
	protected void keyRepeated(int keyCode)
	{
		tryToUnlock(keyCode);
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
            


			// Draw the date bellow notice if set up to do so
			if (Options.getBoolean(Options.OPTION_DISPLAY_DATE))
			{
				g.setColor(255, 255, 255);
				g.setFont(SplashCanvas.font);
				g.drawString(Util.getDateString(false), this.getWidth() / 2, 12, Graphics.TOP | Graphics.HCENTER);
				g.drawString(Util.getCurrentDay(), this.getWidth() / 2, 13+SplashCanvas.font.getHeight(), Graphics.TOP | Graphics.HCENTER);
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

		g.setColor(255, 255, 255);
		g.setFont(SplashCanvas.font);

		Image draw_img = null;
		int im_width = 0;
		if (status_index != -1)
		{
			draw_img = ContactList.smallIcons.elementAt(status_index);
			im_width = draw_img.getWidth();
		}

		// Draw the progressbar message
		g.drawString(message, (this.getWidth() / 2) + (im_width / 2), this.getHeight(), Graphics.BOTTOM | Graphics.HCENTER);

		if (draw_img != null)
		{
			g.drawImage(draw_img, (this.getWidth() / 2) - (font.stringWidth(message) / 2) + (im_width / 2), this.getHeight() - (height / 2), Graphics.VCENTER | Graphics.RIGHT);
		}

		// Draw current progress
		int progressPx = this.getWidth() * progress / 100;
		if (progressPx < 1) return;

		g.setClip(0, this.getHeight() - SplashCanvas.height - 2, progressPx, SplashCanvas.height + 2);
		g.setColor(255, 255, 255);
		g.fillRect(0, this.getHeight() - SplashCanvas.height - 2, progressPx, SplashCanvas.height + 2);

		g.setColor(0, 0, 0);
				
		// Draw the progressbar message
		g.drawString(message, (this.getWidth() / 2) + (im_width / 2), this.getHeight(), Graphics.BOTTOM | Graphics.HCENTER);

		if (draw_img != null)
		{
			g.drawImage(draw_img, (this.getWidth() / 2) - (font.stringWidth(message) / 2) + (im_width / 2), this.getHeight() - (height / 2), Graphics.VCENTER | Graphics.RIGHT);
		}

	}

	public static void startTimer()
	{
		if (status_index != 8)
		{
			new Timer().schedule(new TimerTasks(TimerTasks.SC_RESET_TEXT_AND_IMG), 3000);
		//#sijapp cond.if target="MOTOROLA"#
			VirtualList.setLEDmode(VirtualList.BKLT_TYPE_BLINKING, 3000, JimmUI.st_colors[status_index]);
		}
		else
		{
			VirtualList.setLEDmode(VirtualList.BKLT_TYPE_LIGHTING, -1, JimmUI.st_colors[9]);
		//#sijapp cond.end#
		}
}

	public static void addTimerTask(String captionLngStr, Action action, boolean canCancel)
	{
		if (t2 != null)
		{
			t2.cancel();
			t2 = null;
		}
		
		isLocked = false;
		
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
