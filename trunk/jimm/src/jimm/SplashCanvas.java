/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-04  Jimm Project

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


import jimm.comm.ConnectAction;
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
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDletStateChangeException;
import java.util.Date;
import java.util.Calendar;
import java.util.TimerTask;


public class SplashCanvas extends Canvas
{


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


  /****************************************************************************/


  // Message to display beneath the splash image
  private String message;

  // Progress in percent
  private int progress;

  // True if keylock has been enabled
  private boolean isLocked; // = false

  // True if at least one message is available
  private boolean isMessageAvailable; // = false

  // Timestamp
  private Date pressed; // = null

  // Used for time to be displayed on the spash screen
  private Calendar time = Calendar.getInstance();


  // Constructor
  public SplashCanvas(String message)
  {
    this.message = new String(message);
    this.progress = 0;
    background = Image.createImage(this.getWidth(),this.getHeight());
    int r,g;
    Graphics bg_graph = background.getGraphics();
    for(int x=0;x<this.getWidth();x+=2)
    {
    	for(int y=0;y<this.getHeight();y+=2)
    	{
    		r=x*y / (y+x+1) % 256;
    		g=(r^x^y) % 256;
    		bg_graph.setColor(r,g,(r+g) % 256);
    		bg_graph.fillRect(x,y,2,2);
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


  // Enable keylock
  public synchronized void lock()
  {
    this.setMessage(ResourceBundle.getString("jimm.res.Text", "keylock_enabled"));
    this.setProgress(0);
    this.isLocked = true;
    Jimm.display.setCurrent(this);
  }


  // Disable keylock
  private synchronized void unlock()
  {
    this.isLocked = false;
    this.isMessageAvailable = false;
    Jimm.jimm.getContactListRef().activate();
  }


  // Called when message has been received
  public synchronized void messageAvailable()
  {
    if (this.isLocked)
    {
      this.isMessageAvailable = true;
      this.repaint();
    }
  }


  // Called when a key is pressed
  protected void keyPressed(int keyCode)
  {
    if (this.isLocked && (keyCode == Canvas.KEY_POUND))
    {
      this.pressed = new Date();
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


  // Called to get a date String
  public String getDateString(boolean onlyTime)
  {
	time.setTime(new Date());
	String datestr = new String("failed");
	datestr = Util.makeTwo(time.get(Calendar.HOUR_OF_DAY)) + ":" + Util.makeTwo(time.get(Calendar.MINUTE));
	if (! onlyTime){
		datestr = Util.makeTwo(time.get(Calendar.DAY_OF_MONTH)) + "." + 
				  Util.makeTwo(time.get(Calendar.MONTH) + 1) + "." +
				  String.valueOf(time.get(Calendar.YEAR)) + " " + datestr;
	}
	return datestr;
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
      if (this.isLocked && this.isMessageAvailable)
	  	g.drawImage(ContactList.eventPlainMessageImg, 1, 1, Graphics.LEFT | Graphics.TOP);

	}
    // Display time and and date (if requested)
    try{  
	    if (Jimm.jimm.getOptionsRef().isDisplayDate())
	    {
		  // Draw white bottom bar
	      //g.setColor(0, 0, 0);
		  //g.fillRect(0, this.getHeight() - SplashCanvas.height - 3, this.getWidth(), SplashCanvas.height + 2);

	      g.setColor(255, 255, 255);
	      g.setStrokeStyle(Graphics.DOTTED);
	      g.drawLine(0, this.getHeight() - SplashCanvas.height - 3, this.getWidth(), this.getHeight() - SplashCanvas.height - 2);
		  // Draw message
	      g.setColor(255, 255, 255);
	      g.setFont(SplashCanvas.font);
	      g.drawString(this.getDateString(false), this.getWidth() / 2, this.getHeight(), Graphics.BOTTOM | Graphics.HCENTER);
		  // Draw current progress
	      int progressPx = this.getWidth() / 100 * this.progress;
	      g.setClip(0, this.getHeight() - SplashCanvas.height - 2, progressPx, SplashCanvas.height + 2);
	      g.setColor(255, 255, 255);
	      g.fillRect(0, this.getHeight() - SplashCanvas.height - 2, progressPx, SplashCanvas.height + 2);
	      g.setColor(0, 0, 0);
	      g.drawString(this.getDateString(false), this.getWidth() / 2, this.getHeight(), Graphics.BOTTOM | Graphics.HCENTER);
		}
    }
    catch (NullPointerException e){
    	// Do nothing
    }
	
    // Display message, if available
    if ((this.message != null) && (this.message.length() > 0))
    {
	  // Draw white bottom bar
      //g.setColor(0, 0, 0);
      //g.fillRect(0, this.getHeight() - SplashCanvas.height - 3, this.getWidth(), SplashCanvas.height + 2);
      g.setColor(255, 255, 255);
      g.setStrokeStyle(Graphics.DOTTED);
      g.drawLine(0, this.getHeight() - SplashCanvas.height - 3, this.getWidth(), this.getHeight() - SplashCanvas.height - 2);

      // Draw message
      g.setColor(255, 255, 255);
      g.setFont(SplashCanvas.font);
      g.drawString(this.message, this.getWidth() / 2, this.getHeight(), Graphics.BOTTOM | Graphics.HCENTER);

      // Draw current progress
      int progressPx = this.getWidth() / 100 * this.progress;
      g.setClip(0, this.getHeight() - SplashCanvas.height - 2, progressPx, SplashCanvas.height + 2);
      g.setColor(255, 255, 255);
      g.fillRect(0, this.getHeight() - SplashCanvas.height - 2, progressPx, SplashCanvas.height + 2);
      g.setColor(0, 0, 0);
      g.drawString(this.message, this.getWidth() / 2, this.getHeight(), Graphics.BOTTOM | Graphics.HCENTER);

    }
  }


  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/


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
        Jimm.jimm.getContactListRef().resetListPosition();
        Jimm.jimm.getContactListRef().activate();
        this.cancel();
      }
      else if (this.connectAct.isError())
      {
        this.cancel();
      }
    }


  }


  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/


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
          { /* Do nothing */
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


  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/


  // Waits until meta information is available
  public static class RequestInfoTimerTask extends TimerTask
  {


    // Reference to RequestInfoAction
    private RequestInfoAction requestInfoAct;


    // Constructor
    public RequestInfoTimerTask(RequestInfoAction requestInfoAct)
    {
      this.requestInfoAct = requestInfoAct;
    }


    // Timer routine
    public void run()
    {
      if (this.requestInfoAct.isCompleted())
      {
        String uin = this.requestInfoAct.getUin();
        String nick = this.requestInfoAct.getNick();
        String name = (this.requestInfoAct.getFirstName() + " " + this.requestInfoAct.getLastName()).trim();
        String email = this.requestInfoAct.getEmail();
        String infoText = ResourceBundle.getString("jimm.res.Text", "uin") + ": " + (uin.length() > 0 ? uin : "-") + (char) (0x0D) +
            ResourceBundle.getString("jimm.res.Text", "nick") + ": " + (nick.length() > 0 ? nick : "-") + (char) (0x0D) +
            ResourceBundle.getString("jimm.res.Text", "name") + ": " + (name.length() > 0 ? name : "-") + (char) (0x0D) +
            ResourceBundle.getString("jimm.res.Text", "email") + ": " + (email.length() > 0 ? email : "-");
        Alert info = new Alert(ResourceBundle.getString("jimm.res.Text", "info"), infoText, null, AlertType.INFO);
        Jimm.jimm.getContactListRef().activate(info);
        this.cancel();
      }
      else if (this.requestInfoAct.isError())
      {
        Jimm.jimm.getContactListRef().activate();
        JimmException.handleException(new JimmException(160, 0, true));
        this.cancel();
      }
    }


  }


  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/


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
        Jimm.jimm.getContactListRef().update();
        Jimm.jimm.getContactListRef().activate();
        this.cancel();
      }
      else if (this.updateContactListAct.isError())
      {
        Jimm.jimm.getContactListRef().update();
        Jimm.jimm.getContactListRef().activate();
        if (this.updateContactListAct.getErrorType() == 0 )
            JimmException.handleException(new JimmException(154, 2, true));
        this.cancel();
      }
    }

  }
  
  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/


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
      }
      else if (this.searchAct.isError())
      {
        if (!searchAct.excepHandled())
            JimmException.handleException(new JimmException(154, 2, true));
        this.cancel();
      }
    }

  }

}
