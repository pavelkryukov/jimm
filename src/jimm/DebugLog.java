package jimm;


import javax.microedition.lcdui.*;

import DrawControls.TextList;
import jimm.Jimm;

//#sijapp cond.if modules_DEBUGLOG is "true" #

class Helper implements CommandListener
{
    public void commandAction(Command c, Displayable d)
    {
        Jimm.jimm.getContactListRef().activate();
    }
}


public class DebugLog
{
  private static TextList list;
  
  private static Command backCommand = new Command("back", Command.BACK, 1);
  
  
  static
  {
      list = new TextList("debug log");
      list.addCommand(backCommand);
      list.setCommandListener( new Helper() );
      list.setFontSize(TextList.SMALL_FONT);
  }
  
  
  public static void activate()
  {
      Jimm.display.setCurrent(list);
  }
  
  static int counter = 0;
  
  synchronized public static void addText(String text)
  {
  	list.addBigText("["+Integer.toString(++counter)+"]", 0xFF, Font.STYLE_PLAIN);
	list.addBigText(text, 0, Font.STYLE_PLAIN);
  }

}

//#sijapp cond.else#

public class DebugLog
{
	synchronized public static void addText(String text)
  {
      System.out.println(text);
  }
}


//#sijapp cond.end#