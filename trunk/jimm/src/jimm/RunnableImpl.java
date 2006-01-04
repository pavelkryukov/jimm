package jimm;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;

//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
import javax.microedition.media.PlayerListener;
import javax.microedition.media.Player;
//#sijapp cond.end#


import jimm.comm.Message;
import jimm.comm.Util;
import jimm.ContactListContactItem;


public class RunnableImpl implements Runnable
{
	private int type;
	private Object[] data;
	private static MIDlet midlet;
	
	final static public int TYPE_ADD_MSG             = 1;
	final static public int TYPE_CLOSE_PLAYER        = 2;
	final static public int TYPE_SET_CAPTION         = 3;
	final static public int TYPE_USER_OFFLINE        = 4;
	final static public int TYPE_UPDATE_CONTACT_LIST = 5;
	final static public int TYPE_SHOW_USER_INFO      = 6;
	final static public int TYPE_UPDATE_CL_CAPTION   = 7;
	
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
			
		case TYPE_SHOW_USER_INFO:
			JimmUI.showUserInfo((String[])data[0]);
			break;
			
		case TYPE_UPDATE_CL_CAPTION:
			//#sijapp cond.if modules_TRAFFIC="true"#
			ContactList.updateTitle(Traffic.getSessionTraffic(true));
			//#sijapp cond.else#
			ContactList.updateTitle(0);
			//#sijapp cond.end#
			break;
			
		case TYPE_UPDATE_CONTACT_LIST:
			ContactList.update
			(
				(String)data[0],
				Util.getLong(data,  1),
				Util.getInt (data,  2),
				(byte[])data[3],
				Util.getLong(data,  4),
				Util.getInt (data,  5),
				Util.getInt (data,  6),
				Util.getLong(data,  7),
				Util.getLong(data,  8),
				Util.getLong(data,  9),
				Util.getInt (data, 10)
			);
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
	
	static public void callSerially(int type)
	{
		callSerially(type, null);
	}
	
	
	static public void callSerially(int type, Object obj1, Object obj2)
	{
		callSerially(type, new Object[] {obj1, obj2});
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	static public void updateContactListCaption()
	{
		callSerially(TYPE_UPDATE_CL_CAPTION);
	}
	
	static public void updateContactList
	(
	   	String uin,
    	long status,
    	int capabilities,
    	byte[] internalIP,
    	long dcPort,
    	int dcType,
    	int icqProt,
        long authCookie,
        long signon,
        long online,
        int idle
	)
	{
		Object[] arguments = new Object[11];
		
		arguments[0] = uin;
		Util.setLong(arguments,  1, status      );
		Util.setInt (arguments,  2, capabilities);
		arguments[3] = internalIP;
		Util.setLong(arguments,  4, dcPort      );
		Util.setInt (arguments,  5, dcType      );
		Util.setInt (arguments,  6, icqProt     );
		Util.setLong(arguments,  7, authCookie  );
		Util.setLong(arguments,  8, signon      ); 
		Util.setLong(arguments,  9, online      );
		Util.setInt (arguments, 10, idle        );  
		
		callSerially(TYPE_UPDATE_CONTACT_LIST, arguments);
	}
	
}
