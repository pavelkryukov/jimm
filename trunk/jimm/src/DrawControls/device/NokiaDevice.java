//#sijapp cond.if target="MIDP2"#

package DrawControls.device;

import com.nokia.mid.ui.DeviceControl;

public class NokiaDevice implements Device
{
	private static final int CHANGE_INTENSITY_STEP = 10;
	
	private boolean useBackLightControl;
	private int lightIntensity;
	private boolean isLight;
	
	public NokiaDevice()
	{
		lightIntensity = 50;
	}
	
	public void changeBackLightIntensity(boolean increase)
	{
		if (increase) lightIntensity += CHANGE_INTENSITY_STEP;
		else lightIntensity -= CHANGE_INTENSITY_STEP;
		if (lightIntensity < 1) lightIntensity = 1;
		if (lightIntensity > 100) lightIntensity = 100;
		DeviceControl.setLights(0, lightIntensity);
	}

	public void setBackLightOn(boolean forever)
	{
		if (useBackLightControl)
        {
			DeviceControl.setLights(0, lightIntensity);
            isLight = true;
        }
	}
	
	public void setBackLightOff()
	{
		if (useBackLightControl)
		{
			DeviceControl.setLights(0, 0);
			isLight = false;
		}
	}

	public void setBackLightOnTime(boolean use, int value)
	{
		useBackLightControl = use;
	}

	public void inverseBackLight()
	{
		DeviceControl.setLights(0, !isLight ? lightIntensity : 0);
		isLight = !isLight;
	}

	public boolean featureSupported(int feature)
	{
		switch (feature)
		{
		case FEATURE_LIGHT_INTENSITY:
		case FEATURE_LIGHT_OFF:
			return true;
		}
		return false;
	}
}

//#sijapp cond.end#