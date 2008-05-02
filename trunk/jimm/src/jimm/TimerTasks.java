package jimm;

import jimm.util.ResourceBundle;
import java.util.TimerTask;

import javax.microedition.lcdui.Displayable;

import DrawControls.VirtualList;

import jimm.comm.Action;
import jimm.comm.Icq;

//#sijapp cond.if target="MOTOROLA"#
//# import com.motorola.funlight.*;
//#sijapp cond.end#

public class TimerTasks extends TimerTask implements
		javax.microedition.lcdui.CommandListener
{
	public static final int SC_AUTO_REPAINT = 1;
	public static final int SC_HIDE_KEYLOCK = 2;
	public static final int SC_RESET_TEXT_AND_IMG = 3;
	final static public int TYPE_FLASH = 4;
	final static public int TYPE_CREEPING = 5;
	final static public int TYPE_MINUTE = 6;

	//#sijapp cond.if target="MOTOROLA"#
	//#	public static final int VL_SWITCHOFF_BKLT = 10;
	//#	public static final int VL_SWITCHOFF_LED    = 11;
	//#	public static final int VL_LED_CHANGE_STATE = 12;
	//#sijapp cond.end#
	public static final int ICQ_KEEPALIVE = 100;

	//#sijapp cond.if target="MOTOROLA"#
	//#	Region[] regions;
	//#	int tries;
	//#	int cmode;
	//#sijapp cond.end#

	private int type = -1;

	private Action action;

	boolean wasError = false;
	boolean canceled = false;
	


	private Object flashDispl;
	private String flashText, flashOldText;
	private int flashCounter;	

	public TimerTasks(Action action)
	{
		this.action = action;
	}

	public TimerTasks(int type)
	{
		this.type = type;
	}
	
	public TimerTasks(Object displ, String text, int counter, int type)
	{
		this.flashDispl = displ;
		this.flashText = text;
		this.flashOldText = JimmUI.getCaption(displ);
		this.flashCounter = (type == TYPE_FLASH) ? counter : 0;
		this.type = type;
	}
	
	public boolean cancel()
	{
		canceled = true;
		return super.cancel();
	}
	
	public boolean isCanceled()
	{
		return canceled;
	}
	
	public int getType()
	{
		return type;
	}

	//#sijapp cond.if target="MOTOROLA"#
	//#	public TimerTasks(int type, Region[] regions, int tries)
	//#	{
	//#		this.type = type;
	//#		this.regions = regions;
	//#		this.tries = tries;
	//#	}
	//#sijapp cond.end#

	public void run()
	{
		if (wasError)
			return;
		if (type != -1)
		{
			switch (type)
			{
			case SC_AUTO_REPAINT:
				SplashCanvas.Repaint();
				break;
				
			case SC_HIDE_KEYLOCK:
				SplashCanvas.showKeylock = false;
				SplashCanvas.Repaint();
				break;
				
			case SC_RESET_TEXT_AND_IMG:
				SplashCanvas.setMessage(ResourceBundle
						.getString("keylock_enabled"));
				SplashCanvas.setStatusToDraw(JimmUI.getStatusImageIndex(Icq
						.getCurrentStatus()));
				SplashCanvas.Repaint();
				break;
			//#sijapp cond.if target="MOTOROLA"#
			//#			case VL_SWITCHOFF_BKLT:
			//#				DrawControls.VirtualList.setBkltOn(false);
			//#				break;
			//#			case VL_SWITCHOFF_LED:
			//#				DrawControls.VirtualList.disableLED();
			//#				break;
			//#			case VL_LED_CHANGE_STATE:
			//#				if (((cmode % 2) == 0) & (tries != 1))
			//#				{
			//#					regions[0].getControl();
			//#					if (regions[1] != null) regions[1].getControl();
			//#				}
			//#				else
			//#				{
			//#					regions[0].releaseControl();
			//#					if (regions[1] != null) regions[1].releaseControl();
			//#				}
			//#				tries--;
			//#				cmode = (cmode++ % 2);
			//#				if (tries < 1)
			//#				{
			//#					DrawControls.VirtualList.disableLED();
			//#					cancel();
			//#				}
			//#				break;
			//#sijapp cond.end#

			case ICQ_KEEPALIVE:
				if (Icq.isConnected()
						&& Options.getBoolean(Options.OPTION_KEEP_CONN_ALIVE))
				{
					// Instantiate and send an alive packet
					try
					{
						Icq.c.sendPacket(new jimm.comm.Packet(5, new byte[0]));
					} catch (JimmException e)
					{
						JimmException.handleException(e);
						if (e.isCritical())
							cancel();
					}
				}
				break;
				
			case TYPE_FLASH:
				if (flashCounter == 0)
				{		
					JimmUI.setCaption(flashDispl, flashOldText);
					cancel();
					flashDispl = null;
					return;
				}
				if (checkFlashControlIsActive()) return;
				JimmUI.setCaption(flashDispl, ((flashCounter & 1) == 0) ? flashText : " ");
				flashCounter--;
				break;

			case TYPE_CREEPING:
				if (checkFlashControlIsActive()) return;
				JimmUI.setCaption(flashDispl, flashText.substring(flashCounter));
				flashCounter++;
				if (flashCounter > flashText.length() - 5) flashCounter = 0;
				break;
				
			case TYPE_MINUTE:
				RunnableImpl.minuteTask();
			}
			return;
		}

		SplashCanvas.setProgress(action.getProgress());
		if (action.isCompleted())
		{
			cancel();
			action.onEvent(Action.ON_COMPLETE);
		} else if (action.isError())
		{
			wasError = true;
			cancel();
			action.onEvent(Action.ON_ERROR);
		}
	}
	
	private boolean checkFlashControlIsActive()
	{
		boolean isVisible = false;
		if (flashDispl instanceof VirtualList)
		{
			isVisible = JimmUI.isControlActive((VirtualList)flashDispl);
		}
		else if (flashDispl instanceof Displayable)
		{
			isVisible = ((Displayable)flashDispl).isShown();
		}
		
		if (!isVisible)
		{
			JimmUI.setCaption(flashDispl, flashOldText);
			cancel();
			flashDispl = null;
		}
		
		return !isVisible;
	}
	
	public void flashRestoreOldCaption()
	{
		JimmUI.setCaption(flashDispl, flashOldText);
	}

	public void commandAction(javax.microedition.lcdui.Command c,
			javax.microedition.lcdui.Displayable d)
	{
		if (c == SplashCanvas.cancelCommnad)
		{
			action.onEvent(Action.ON_CANCEL);
			cancel();
		}
	}
}
