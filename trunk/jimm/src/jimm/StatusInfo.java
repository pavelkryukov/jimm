package jimm;

import javax.microedition.lcdui.Image;

public class StatusInfo
{
	public static final int TYPE_STATUS   = 1;
	public static final int TYPE_X_STATUS = 2;
	
	public static final int FLAG_IN_MENU    = 1 << 0;
	public static final int FLAG_HAVE_DESCR = 1 << 1;
	public static final int FLAG_STD        = 1 << 2;
	
	private int     type;
	private int     value;
	private String  text;
	private Image   image;
	private int     flags;
	
	StatusInfo(int type, int value, String text, Image image, int flags)
	{
		this.type  = type;
		this.value = value;
		this.text  = text;
		this.image = image;
		this.flags = flags;
	}
	
	public int getType() 
	{
		return type; 
	}

	public int getValue()
	{
		return value;
	}

	public String getText()
	{
		return text;
	}

	public Image getImage()
	{
		return image;
	}

	public int getFlags()
	{
		return flags;
	}
	
	public boolean testFlag(int mask)
	{
		return (flags&mask) != 0;
	}

}
