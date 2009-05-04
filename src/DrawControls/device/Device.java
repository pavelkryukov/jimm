package DrawControls.device;

public interface Device
{
	static public final int PHONE_STANDART     = 1000;
	static public final int PHONE_SONYERICSSON = 1001;
	static public final int PHONE_NOKIA        = 1002;
	static public final int PHONE_MOTOROLA     = 1003;
	
	static public final int FEATURE_LIGHT_INTENSITY = 2000;
	static public final int FEATURE_MINIMIZE        = 2001;
	static public final int FEATURE_LIGHT_OFF       = 2002;

//#sijapp cond.if target!="DEFAULT"#	
	boolean featureSupported(int feature);
	void changeBackLightIntensity(boolean increase);
	void setBackLightOn();
	void setBackLightOnTime(boolean use, int value);
	void inverseBackLight();
//#sijapp cond.end#	
}



