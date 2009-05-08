//#sijapp cond.if target="MIDP2"#
package DrawControls.device;

import com.samsung.util.LCDLight;

public class SamsungDevice implements Device
{
	private boolean useBackLightControl;
	private boolean isLight;
	private int lightTimeout;

	public SamsungDevice()
	{
		lightTimeout = 0xffffffff;
	}

	public void changeBackLightIntensity(boolean increase)
	{
	}

	public void setBackLightOn(boolean forever)
	{
		if (useBackLightControl)
		{
			LCDLight.on(lightTimeout * 1000);
			isLight = true;
		}
	}

	public void setBackLightOff()
	{
		if (useBackLightControl)
		{
			LCDLight.off();
			isLight = false;
		}
	}

	public void setBackLightOnTime(boolean use, int value)
	{
		useBackLightControl = use;
		lightTimeout = value;
	}

	public void inverseBackLight()
	{
		if (useBackLightControl)
		{
			if (isLight)
				LCDLight.off();
			else
				LCDLight.on(lightTimeout * 1000);
			isLight = !isLight;
		}
	}

	public boolean featureSupported(int feature)
	{
		switch (feature)
		{
			case FEATURE_LIGHT_OFF:
				try {
					Class.forName("com.samsung.util.LCDLight");
					return true;
				} catch (Throwable t0) {}
		}
		return false;
	}
}

//#sijapp cond.end#