package jimm;

import java.util.TimerTask;
import jimm.comm.Action;
import jimm.comm.Icq;

public class TimerTasks extends TimerTask implements javax.microedition.lcdui.CommandListener
{
	public static final int SC_AUTO_REPAINT = 1;

	public static final int SC_HIDE_KEYLOCK = 2;

	//#sijapp cond.if target="MOTOROLA"#
	public static final int VL_SWITCHOFF_BKLT = 10;

	//#sijapp cond.end#
	public static final int ICQ_KEEPALIVE = 100;

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
			
			//#sijapp cond.if target="MOTOROLA"#
			case VL_SWITCHOFF_BKLT:
				DrawControls.VirtualList.setBkltOn(false);
				break;
			//#sijapp cond.end#
				
			case ICQ_KEEPALIVE:
				if (Icq.isConnected() && Options.getBoolean(Options.OPTION_KEEP_CONN_ALIVE))
				{
					// Instantiate and send an alive packet
					try
					{
						Icq.c.sendPacket(new jimm.comm.Packet(5, new byte[0]));
						System.out.println("Ping sent");
					}
					catch (JimmException e)
					{
						System.out.println("Ping exception - 1!");
						JimmException.handleException(e);
						if (e.isCritical()) cancel();
					}
					catch (Exception e)
					{
						System.out.println("Ping exception - 2!");
					}
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

	public void commandAction(javax.microedition.lcdui.Command c, javax.microedition.lcdui.Displayable d)
	{
		if (c == SplashCanvas.cancelCommnad)
		{
			action.onEvent(Action.ON_CANCEL);
			cancel();
		}
	}
}
