//#sijapp cond.if target!="DEFAULT"#

package DrawControls.device;

import javax.microedition.lcdui.Display;

public class StdDevice implements Device
{
	private boolean useBackLightControl;
	private int backLightOnTime;
	private int type;
	private Display display;
	private boolean isLight;
	
	public StdDevice(int type, Display display)
	{
		this.display = display;
		this.type = type;
	}
	
	public void changeBackLightIntensity(boolean increase)
	{
	}

	public void setBackLightOn()
	{
		if (type == Device.PHONE_MOTOROLA && useBackLightControl)
		{
			display.flashBacklight(1000*backLightOnTime);
		}
	}

	public void setBackLightOnTime(boolean use, int value)
	{
		backLightOnTime = value;
		useBackLightControl = use; 
	}
	
	public void inverseBackLight()
	{
		if (type == Device.PHONE_MOTOROLA)
		{
			display.flashBacklight(isLight ? 1 : Integer.MAX_VALUE);
			isLight = !isLight;
		}
	}

	public boolean featureSupported(int feature)
	{
		switch (feature)
		{
		case FEATURE_MINIMIZE:  return (type == PHONE_SONYERICSSON);
		case FEATURE_LIGHT_OFF: return (type == PHONE_MOTOROLA);
		}
		return false;
	}
}

//#sijapp cond.end#