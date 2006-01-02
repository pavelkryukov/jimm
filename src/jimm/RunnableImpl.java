package jimm;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;

//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
import javax.microedition.media.PlayerListener;
import javax.microedition.media.Player;
//#sijapp cond.end#


import jimm.comm.Message;
import jimm.ContactListContactItem;


public class RunnableImpl implements Runnable
{
	private int type;
	private Object[] data;
	private static MIDlet midlet;
	
	final static public int TYPE_ADD_MSG        = 1;
	final static public int TYPE_CLOSE_PLAYER   = 2;
	final static public int TYPE_SET_CAPTION    = 3;
	final static public int TYPE_USER_OFFLINE   = 4;
	final static public int TYPE_STATUS_CHANGED = 5;
	final static public int TYPE_SHOW_USER_INFO = 6;
	
	RunnableImpl(int type, Object[] data)
	{
		this.type = type;
		this.data = data;
	}
	
	// Method run contains operations which have to be synchronized
	// with main events queue (in main thread)
	// If you want your code run in main thread, make new constant 
	// beginning of TYPE_ and write your source to switch block of 
	// RunnableImpl.run method.
	// To run you source call RunnableImpl.callSerially()
	// Note RunnableImpl.callSerially NEVER blocks calling thread
	public void run()
	{
		switch (type)
		{
		case TYPE_ADD_MSG:
			ContactList.addMessage((Message)data[0]);
			break;
			
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		case TYPE_CLOSE_PLAYER:	
        	if ((String)data[1] == PlayerListener.END_OF_MEDIA)
        	{
        		((Player)data[0]).close();
        	}
        	break;
		//#sijapp cond.end#	
			
		case TYPE_USER_OFFLINE:
			ContactList.update((String)data[0], ContactList.STATUS_OFFLINE);
			break;
			
		case TYPE_STATUS_CHANGED:
			boolean[] boolValues = (boolean[])data[1];
			ContactList.contactChanged
			(
				(ContactListContactItem)data[0],
				boolValues[0],
				boolValues[1],
				boolValues[2]
			);
			break;
			
		case TYPE_SHOW_USER_INFO:
			JimmUI.showUserInfo((String[])data[0]);
			break;
		}
	}
	
	static public void setMidlet(MIDlet midlet_)
	{
		midlet = midlet_;
	}
	
	synchronized static public void callSerially(int type, Object[] data)
	{
		Display.getDisplay(midlet).callSerially(new RunnableImpl(type, data));
	}
	
	static public void callSerially(int type, Object obj1)
	{
		callSerially(type, new Object[] {obj1});
	}
	
	static public void callSerially(int type, Object obj1, Object obj2)
	{
		callSerially(type, new Object[] {obj1, obj2});
	}
	
	
}
