package jimm;

import jimm.comm.Icq;

public class Threads implements Runnable
{
	final static public int TYPE_REQ_LAST_VESR = 1;
	final static public int TYPE_RECONNECT     = 2;
	
	private int type; 
	
	public Threads(int type)
	{
		this.type = type;
	}
	
	public void run()
	{
		switch (type)
		{
		case TYPE_REQ_LAST_VESR:
			JimmUI.internalReqLastVersThread();
			break;
			
		case TYPE_RECONNECT:
			if (!Icq.isDisconnected())
			{
				try {Thread.sleep(5000);} catch (Exception e) {}
				ContactList.beforeConnect();
				Icq.connect();
			}
			break;
		}
	}
	
	static public void requestLastJimmVers()
	{
		Threads ri = new Threads(TYPE_REQ_LAST_VESR);
		new Thread(ri).start();
	}
	
	static public void reconnect()
	{
		Threads ri = new Threads(TYPE_RECONNECT);
		new Thread(ri).start();
	}
	
		
	

}
