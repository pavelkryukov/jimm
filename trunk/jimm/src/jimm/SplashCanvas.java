/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-08  Jimm Project

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
//#sijapp cond.if target is "SIEMENS2"#
//# import java.io.IOException;
//#sijapp cond.end#

import jimm.comm.Util;
import jimm.comm.Icq;
import jimm.comm.Action;
import jimm.util.ResourceBundle;
import javax.microedition.lcdui.*;
import java.util.Timer;

import DrawControls.TextList;
import DrawControls.VirtualList;


//#sijapp cond.if target is "RIM"#
//# import net.rim.device.api.system.LED;
//#sijapp cond.end#

public class SplashCanvas extends Canvas implements CommandListener
{
	static private SplashCanvas _this;

	public final static Command 
		cancelCommand = new Command(ResourceBundle.getString("cancel"), Jimm.cmdBack, 1);

	//Timer for repaint
	static private Timer t1, t2;

	// Image object, holds the splash image
	private static Image imgSplash;
	
	private static Image imgClientIcon;

	//#sijapp cond.if target is "SIEMENS2"#
	//#	private static final String BATT_IMG = "/batt.png";
	//#	private static Image battImg = null;
	//#	
	//#	private static Image getBattImg() 
	//#	{
	//#		if( battImg == null )
	//#		{
	//#			try
	//#			{
	//#				battImg = Image.createImage(SplashCanvas.BATT_IMG);
	//#			}
	//#			catch(IOException e){}
	//#		}
	//#		return battImg;
	//#	}

	//#sijapp cond.end#


	// Font used to display the logo (if image is not available)
	private static Font logoFont = Font.getFont(Font.FACE_SYSTEM,
			Font.STYLE_BOLD, Font.SIZE_LARGE);

	// Font (and font height in pixels) used to display informational messages
	private static Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
			Font.SIZE_SMALL);

	private static int fontHeight = font.getHeight();

	// Initializer block
	static
	{
		try { imgClientIcon = Image.createImage("/icon.png"); }   catch (Exception e) {}
	}
	
	static private Image getSplashImage()
	{
		if (imgSplash == null)
			try { imgSplash = Image.createImage("/logo.png"); }   catch (Exception e) {}
		return imgSplash;
	}

	/*****************************************************************************/

	// Message to display beneath the splash image
	static private String message;

	// Last error code message
	static private String lastErrCode;

	// Progress in percent
	static private int progress; // = 0

	// True if keylock has been enabled
	static private boolean isLocked;

	// isError occured while connection
	static private boolean errFlag;

	// Number of available messages
	static private int availableMessages;

	// Time since last key # pressed 
	static private long poundPressTime;

	// Should the keylock message be drawn to the screen?
	static protected boolean showKeylock;

	static private Image statusImage = null;

	// Constructor
	public SplashCanvas(String message)
	{
		_this = this;
//#sijapp cond.if target is "MIDP2"#
		setFullScreenMode(Jimm.getPhoneVendor() != Jimm.PHONE_SONYERICSSON);
//#sijapp cond.elseif target is "MOTOROLA" | target is "SIEMENS2"#
		setFullScreenMode(true);
//#sijapp cond.end#
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
	static public synchronized void setLastErrCode(String errcode)
	{
		SplashCanvas.lastErrCode = (errcode != null) ? new String(errcode) : null;
		_this.repaint();
	}

	// Sets the error flag
	static public synchronized void setErrFlag(boolean errF)
	{
		SplashCanvas.errFlag = errF;
		SplashCanvas._this.repaint();
	}

	// Sets the informational message
	static public synchronized void setMessage(String message)
	{
		SplashCanvas.message = new String(message);
		SplashCanvas._this.repaint();
	}

	public static synchronized void setStatusToDraw(Image statusImage)
	{
		if (statusImage == null)
		{
			StatusInfo statInfo = JimmUI.findStatus(StatusInfo.TYPE_STATUS, Icq.getCurrentStatus());
			if (statInfo != null) statusImage = statInfo.getImage(); 
		}
		SplashCanvas.statusImage = statusImage;
	}

	// Returns the current progress in percent
	static public synchronized int getProgress()
	{
		return (progress);
	}

	static public void show()
	{
		if (t2 != null)
		{
			t2.cancel();
			t2 = null;
		}
		Jimm.display.setCurrent(_this);
		
		Jimm.aaUserActivity();
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
		VirtualList.setMpbPercent(progress);
		SplashCanvas.progress = progress;
		_this.repaint();
		_this.serviceRepaints();
	}

	// Enable keylock
	static public synchronized void lock()
	{
		SplashCanvas._this.removeCommand(SplashCanvas.cancelCommand);
		if (isLocked) return;

		isLocked = true;
		Jimm.setBkltOn(false);
		setProgress(0);
		setMessage(ResourceBundle.getString("keylock_enabled"));

		setStatusToDraw(null);
		
		Jimm.display.setCurrent(_this);
		Jimm.setBkltOff();

		if (Options.getBoolean(Options.OPTION_DISPLAY_DATE))
		{
			(t2 = new Timer()).schedule(new TimerTasks(
					TimerTasks.SC_AUTO_REPAINT), 20000, 20000);
		}
		
//#sijapp cond.if target="MOTOROLA"#
		Jimm.display.flashBacklight(1000*Options.getInt(Options.OPTION_LIGHT_TIMEOUT));
//#sijapp cond.end #		
		
	}

	// Disable keylock
	static public synchronized void unlock(boolean showContactList)
	{
		if (!isLocked)
			return;

		isLocked = false;
		availableMessages = 0;
		//#sijapp cond.if target is "RIM"#
		//#        LED.setState(LED.STATE_OFF);
		//  #sijapp cond.end#
		//  #sijapp cond.if target is "MOTOROLA"#
		//#		if (Options.getBoolean(Options.OPTION_LIGHT_MANUAL)) Jimm.setBkltOn(true);
		//  #sijapp cond.end#
		if (Options.getBoolean(Options.OPTION_DISPLAY_DATE) && (t2 != null))
			t2.cancel();

		if (_this.isShown()) JimmUI.backToLastScreen();
	}

	// Is the screen locked?
	static public boolean locked()
	{
		return (isLocked);
	}

	protected void hideNotify()
	{
		imgSplash = null;
//		resetLastTask();
	}

	// Called when message has been received
	static public synchronized void messageAvailable()
	{
		if (isLocked)
		{
			++availableMessages;
			//#sijapp cond.if target is "RIM"#
			//#	        LED.setConfiguration(500, 250, LED.BRIGHTNESS_50);
			//#	        LED.setState(LED.STATE_BLINKING);
			//#sijapp cond.end#
			_this.repaint();
		}
	}

	// Called when a key is pressed
	protected void keyPressed(int keyCode)
	{
//#sijapp cond.if target="MOTOROLA"#
		Jimm.display.flashBacklight(1000*Options.getInt(Options.OPTION_LIGHT_TIMEOUT));
//#sijapp cond.end #		
		if (isLocked)
		{
			if (keyCode == Canvas.KEY_POUND)
			{
				poundPressTime = System.currentTimeMillis();
			} else
			{
				if (t1 != null)
					t1.cancel();
				showKeylock = true;
				this.repaint();
			}
		}
	}

	private void tryToUnlock(int keyCode)
	{
		if (!isLocked)
			return;
		if (keyCode != Canvas.KEY_POUND)
		{
			poundPressTime = 0;
			return;
		}

		if ((poundPressTime != 0)
				&& ((System.currentTimeMillis() - poundPressTime) > 900))
		{
			unlock(true);
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
		if (g.getClipY() >= this.getHeight() - SplashCanvas.fontHeight - 2) return;
		
		int bgColor = Options.getSchemeColor(Options.CLRSCHHEME_BACK, -1);
		int txtColor = Options.getSchemeColor(Options.CLRSCHHEME_TEXT, -1);

		int textColor = VirtualList.checkTextColor(txtColor);
		int width = this.getWidth();
		int height = this.getHeight();
		
		/* Prepares for bottom bar */
		Image draw_img = statusImage != null ? statusImage : imgClientIcon;
		int barColor = Options.getSchemeColor(Options.CLRSCHHEME_CURS, -1);
		int barColor1 = VirtualList.transformColorLight(barColor, 16);
		int barColor2 = VirtualList.transformColorLight(barColor, -16);
		int barBackColor = VirtualList.mergeColors(barColor, 0x909090, 70);
		if (errFlag) barColor = 0xFF4040;
		int barHeight = fontHeight;
		if (draw_img != null)
		{
			int imgHeight = draw_img.getHeight();
			if (imgHeight > barHeight) barHeight = imgHeight; 
		}
		barHeight += 4;
		
		
//#sijapp cond.if target!="DEFAULT"#		
		// Draw background
		Image backImage = VirtualList.getBackImage();
		if (backImage != null)
			VirtualList.drawBgImage(backImage, width, height, g);
		else
		{
//#sijapp cond.end#			
			g.setColor(bgColor);
			g.fillRect(0, 0, width, height);
//#sijapp cond.if target!="DEFAULT"#			
		}
//#sijapp cond.end#		

		// Display splash image (or text)
		Image imgSplash = getSplashImage();
		if (imgSplash != null)
		{
			g.drawImage(imgSplash, width/2, height/2, Graphics.HCENTER | Graphics.VCENTER);
		} 
		else
		{
			g.setColor(textColor);
			g.setFont(SplashCanvas.logoFont);
			g.drawString("jimm", width/2, height/2+5, Graphics.HCENTER|Graphics.BASELINE);
			g.setFont(SplashCanvas.font);
		}

		// Display message icon, if keylock is enabled
		if (isLocked && availableMessages > 0 && JimmUI.eventPlainMessageImg != null)
		{
			g.drawImage(
				JimmUI.eventPlainMessageImg, 
				2, 
				height-barHeight-JimmUI.eventPlainMessageImg.getHeight()-2, 
				Graphics.LEFT|Graphics.TOP
			);
			g.setColor(textColor);
			g.setFont(SplashCanvas.font);
			g.drawString("# " + availableMessages,
				JimmUI.eventPlainMessageImg.getWidth() + 4, 
				height-barHeight-fontHeight-5, 
				Graphics.LEFT | Graphics.TOP
			);
		}

		if (lastErrCode != null) 
		{
		    g.setColor(textColor);
		    g.setFont(SplashCanvas.font);
		    g.drawString(lastErrCode, 4, height-barHeight-fontHeight-5, Graphics.LEFT | Graphics.TOP);
		}
		
		//#sijapp cond.if target is "SIEMENS2"#
		//#			String accuLevel = System.getProperty("MPJC_CAP");
		//#			if( accuLevel != null && isLocked )
		//#			{
		//#				accuLevel += "%";
		//#				int fontX = getWidth() -  SplashCanvas.font.stringWidth(accuLevel) - 1;
		//#				if( getBattImg() != null )
		//#					g.drawImage(getBattImg(), fontX - getBattImg().getWidth() - 1, this.getHeight()-(2*SplashCanvas.fontHeight)-9, Graphics.LEFT | Graphics.TOP);
		//#				g.setColor(255, 255, 255);
		//#				g.setFont(SplashCanvas.font);
		//#				g.drawString(accuLevel, fontX, height-(2*SplashCanvas.fontHeight)-5, Graphics.LEFT | Graphics.TOP);
		//#			}
		//#sijapp cond.end#

		int y = 2;
		g.setColor(textColor);
		g.setFont(SplashCanvas.font);
		
		// Jimm version
		g.drawString("Jimm "+Jimm.VERSION, width/2, y, Graphics.HCENTER|Graphics.TOP);
		y += fontHeight+2;
		
		// Display notice
		g.drawString("Not affiliated with ICQ inc.", width/2, y, Graphics.HCENTER|Graphics.TOP);
		y += fontHeight+2;
		
		// Draw the date bellow notice if set up to do so
		if (Options.getBoolean(Options.OPTION_DISPLAY_DATE))
		{
			g.setFont(SplashCanvas.font);
			g.drawString(Util.getDateString(false), width / 2, y, Graphics.TOP|Graphics.HCENTER);
			y += fontHeight;
			g.drawString(Util.getCurrentDay(), width / 2, y, Graphics.TOP|Graphics.HCENTER);
		}
		// Display the keylock message if someone hit the wrong key
		if (showKeylock)
		{

			// Init the dimensions
			int x, size_x, size_y;
			size_x = this.getWidth() / 10 * 8;
			size_y = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
					Font.SIZE_MEDIUM).getHeight()
					* TextList.getLineNumbers(ResourceBundle
							.getString("keylock_message"), size_x - 8, 0,
							0, 0) + 8;
			x = width / 2 - (this.getWidth() / 10 * 4);
			y = this.getHeight() / 2 - (size_y / 2);

			g.setColor(VirtualList.getInverseColor(textColor));
			g.fillRect(x, y, size_x, size_y);
			g.setColor(textColor);
			g.drawRect(x + 2, y + 2, size_x - 5, size_y - 5);
			TextList.showText(g, ResourceBundle
					.getString("keylock_message"), x + 4, y + 4,
					size_x - 8, size_y - 8, VirtualList.MEDIUM_FONT, 0, textColor);

			(t1 = new Timer()).schedule(new TimerTasks(
					TimerTasks.SC_HIDE_KEYLOCK), 2000);

		}

		
		// Draws bottom bar
		g.setFont(SplashCanvas.font);
		int xDelimPos = progress*width/100;
		g.setColor(barBackColor);
		g.fillRect(xDelimPos, height-barHeight, width-xDelimPos, barHeight);
		
		if (progress != 0)
		{
			VirtualList.drawRect(g, barColor1, barColor2, 0, height-barHeight, xDelimPos, height, 255);
			g.setColor(VirtualList.getInverseColor(barColor));
			g.drawRect(0, height-barHeight, xDelimPos, barHeight-1);
		}
		
		int msgWidth = 0;
		if (draw_img != null) msgWidth += fontHeight/2+draw_img.getWidth();
		if (message != null) msgWidth += font.stringWidth(message);
		
		int x = (width-msgWidth)/2;
		if (draw_img != null)
		{
			g.drawImage(draw_img, x, height-barHeight/2, Graphics.LEFT|Graphics.VCENTER);
			x += draw_img.getWidth()+fontHeight/2; 
		}
		
		if (message != null)
		{
			g.setColor(VirtualList.getInverseColor(barColor));
			g.drawString(message, x, height-(barHeight+fontHeight)/2, Graphics.LEFT|Graphics.TOP);
		}

	}
	
	public static int getAreaWidth()
	{
		return _this.getWidth();
	}

	private static TimerTasks lastTimerTask;
	private static Action lastAction;
	
	public static void resetLastTask()
	{
		if (lastTimerTask != null)
		{
			try { lastTimerTask.cancel(); } catch (Exception e) {}
			lastTimerTask = null;
			lastAction = null;
		}
	}

	public static void addTimerTask(String captionLngStr, Action action,
			boolean canCancel)
	{
		if (t2 != null)
		{
			t2.cancel();
			t2 = null;
		}

		resetLastTask();

		isLocked = false;

		TimerTasks timerTask = new TimerTasks(action);

		SplashCanvas._this.removeCommand(SplashCanvas.cancelCommand);
		if (canCancel)
		{
			SplashCanvas._this.addCommand(SplashCanvas.cancelCommand);
			SplashCanvas._this.setCommandListener(_this);
		}

		//  #sijapp cond.if target="MIDP2" | target="MOTOROLA"#
		SplashCanvas._this.setFullScreenMode(!canCancel);
		//#sijapp cond.end#

		SplashCanvas.setMessage(ResourceBundle.getString(captionLngStr));
		SplashCanvas.setErrFlag(false);
		SplashCanvas.setProgress(0);
		Jimm.display.setCurrent(SplashCanvas._this);

		Jimm.getTimerRef().schedule(timerTask, 1000, 1000);
		
		lastTimerTask = timerTask;
		lastAction = action;
	}
	
	public static void addTimerTask(Action action)
	{
		TimerTasks timerTask = new TimerTasks(action);

		VirtualList.setMiniProgressBar(true);
		VirtualList.setMpbPercent(0);

		Jimm.getTimerRef().schedule(timerTask, 1000, 1000);
		
		lastTimerTask = timerTask;
		lastAction = action;
	}

	public void commandAction(Command c, Displayable d)
	{
		if (c == SplashCanvas.cancelCommand)
		{
			if (lastAction != null)
			{
				lastAction.onEvent(Action.ON_CANCEL);
				resetLastTask();
			}
		}
	}
}
