package jimm;

import javax.microedition.lcdui.*;

import jimm.comm.Icq;
import jimm.util.ResourceBundle;
import DrawControls.*;

public class PrivateListsForm extends VirtualList implements VirtualListCommands, CommandListener, JimmScreen
{
	private short[] values;
	private ContactItem[] items;
	private Command cmdChange = new Command(ResourceBundle.getString("change"), Command.OK, 1);
	private int curCol;
	private int itemHeight;
	static private final int COL_COUNT = 3; 
	private final int props[] = {
		ContactItem.CONTACTITEM_IGN_ID, 
		ContactItem.CONTACTITEM_INV_ID, 
		ContactItem.CONTACTITEM_VIS_ID
	};
	int rectColor;
	private final String[] names = {"D", "I", "V"};
	
	public PrivateListsForm()
	{
		super(ResourceBundle.getString("priv_lists"));
		
		addCommandEx(JimmUI.cmdCancel, VirtualList.MENU_TYPE_LEFT_BAR);
//#sijapp cond.if target!="RIM" & target!="DEFAULT"#
		addCommandEx(JimmUI.cmdMenu,   VirtualList.MENU_TYPE_RIGHT_BAR);
//#sijapp cond.end#
		addCommandEx(JimmUI.cmdSave,   VirtualList.MENU_TYPE_RIGHT);
		addCommandEx(cmdChange,        VirtualList.MENU_TYPE_RIGHT);
		addCommandEx(JimmUI.cmdInfo,   VirtualList.MENU_TYPE_RIGHT);
		
		setCommandListener(this);
		
		JimmUI.setColorScheme(this, false, -1, true);
		
		items = ContactList.getContactItems();
		// Sort list
		boolean sorted;
		int len;
		for (;;)
		{
			sorted = true;
			len = items.length-1;
			for (int i = 0; i < len; i++)
			{
				if (items[i].getSortText().compareTo(items[i+1].getSortText()) > 0)
				{
					ContactItem tmp = items[i+1];
					items[i+1] = items[i];
					items[i] = tmp;
					sorted = false;
				}
			}
			if (sorted) break;
		}
		
		values = new short[items.length];
		short value;
		for (int i = items.length-1; i >= 0; i--)
		{
			value = 0;
			for (int j = props.length-1; j >= 0; j--) 
				if (items[i].getIntValue(props[j]) != 0) value += (1 << j); 
			values[i] = value; 
		}
		
		curCol = 0;
		itemHeight = getFontHeight();
		setVLCommands(this);
		
		int backColor = getBkgrndColor();
		int textColor = getTextColor();
		rectColor = mergeColors(backColor, textColor, 20);
	}
	
	
	public void activate()
	{
		activate(Jimm.display);
		JimmUI.setLastScreen(this, false);
	}
	
	public boolean isScreenActive()
	{
		return isActive();
	}
	
	protected void getCurXVals(int[] values)
	{
		int x = getGridX(curCol);
		values[0] = x;
		values[1] = x+itemHeight;
	}
	
	private int getGridX(int col)
	{
		return getWidth()-scrollerWidth-borderWidth-COL_COUNT*itemHeight+col*itemHeight;
	}
	
	public void vlKeyPress(VirtualList sender, int keyCode, int type)
	{
		if (type == VirtualList.KEY_PRESSED)
		{
			int lastCol = curCol;
			switch (getGameAction(keyCode))
			{
			case Canvas.LEFT:
				curCol--;
				if (curCol < 0) curCol = 0;
				break;

			case Canvas.RIGHT:
				curCol++;
				if (curCol >= COL_COUNT) curCol = COL_COUNT-1;
				break;
			}

			if (lastCol != curCol) invalidate();
		}
	}
	
	public void vlCursorMoved(VirtualList sender) 
	{
		Jimm.aaUserActivity();
	}
	
	public void vlItemClicked(VirtualList sender) {}

	protected void afterDrawCaption(Graphics g, int height)
	{
		Font f = g.getFont();
		int x;
		String str;
		for (int col = 0; col < COL_COUNT; col++)
		{
			x = getGridX(col);
			str = names[col];
			g.drawString
			(
				str, 
				x+(itemHeight-f.stringWidth(str))/2, 
				(height-f.getHeight())/2, 
				Graphics.TOP|Graphics.LEFT
			);
		}
	}
	
	protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int paintMode)
	{
		super.drawItemData(g, index, x1, y1, x2, y2, paintMode);
		int x;
		int rectX;
		int rectY;
		int rectW;
		int rectH;
		
		int color;
		int color1;
		int color2;
		boolean value;
		for (int col = 0; col < COL_COUNT; col++)
		{
			x = getGridX(col);
			rectX = x+2;
			rectY = y1+2;
			rectW = itemHeight-5;
			rectH = itemHeight-5;
			
			color = getBkgrndColor();
			color1 = transformColorLight(color, 20);
			color2 = transformColorLight(color, -20);
			drawRect(g, color1, color2, rectX, rectY, rectX+rectW, rectY+rectH, 255);
			
			value = ((values[index] & (1 << col)) != 0);
			if (value)
			{
				g.setColor(getTextColor());
				g.drawLine(rectX, rectY, rectX+rectW, rectY+rectH);
				g.drawLine(rectX, rectY+rectH, rectX+rectW, rectY);
			}
			else g.setColor(rectColor);
			
			g.drawRect(rectX, rectY, rectW, rectH);
		}
	}

	protected int getSize()
	{
		return (items == null) ? 0 : items.length;
	}
	
	protected void get(int index, ListItem item)
	{
		item.text = items[index].getStringValue(ContactItem.CONTACTITEM_NAME);
		item.color = getTextColor();
	}
	
	public void commandAction(Command c, Displayable d)
	{
		Jimm.aaUserActivity();
		
		if (c == cmdChange)
		{
			int index = getCurrIndex();
			if ((index >= getSize()) || (index < 0)) return;
			int mask = (1 << curCol);
			values[index] ^= mask;
			
			if ((values[index] & mask) != 0)
			{
				if (curCol == 1) values[index] &= ~4;
				else if (curCol == 2) values[index] &= ~2;
			}
			invalidate();
		}
		
		else if (c == JimmUI.cmdSave)
		{
			processChanges();
		}
		
		else if (c == JimmUI.cmdCancel)
		{
			JimmUI.backToLastScreen();
		}
		
		else if (c == JimmUI.cmdInfo)
		{
			int index = getCurrIndex();
			if ((index >= getSize()) || (index < 0)) return;
			JimmUI.requiestUserInfo
			(
				items[index].getStringValue(ContactItem.CONTACTITEM_UIN),
				items[index].getStringValue(ContactItem.CONTACTITEM_NAME),
				false
			);
		}
	}
	
	private void processChanges()
	{
		boolean wasChanges = false;
		ContactItem ci;
		boolean newVal;
		boolean oldVal;
		for (int i = items.length-1; i >= 0; i--)
		{
			ci = items[i];
			for (int col = 0; col < COL_COUNT; col++)
			{
				newVal = ((values[i] & (1 << col)) != 0);
				oldVal = ci.getIntValue(props[col]) != 0;
				if (newVal != oldVal) wasChanges = true;
			}
			if (wasChanges) break;
		}
		ci = null;
		
		if (!wasChanges)
		{
			JimmUI.backToLastScreen();
			return;
		}
		
		try
		{
			int[] types = {0x000E, 0x0003, 0x0002};
			
			Icq.sendCLI_ADDSTART();
			
			String uin;
			for (int i = items.length-1; i >= 0; i--)
			{
				ci = items[i];
				uin = ci.getStringValue(ContactItem.CONTACTITEM_UIN);
				for (int col = 0; col < COL_COUNT; col++)
				{
					newVal = ((values[i] & (1 << col)) != 0);
					oldVal = ci.getIntValue(props[col]) != 0;
					if (newVal && !oldVal)
					{
						int newId = ContactList.generateNewIdForBuddy();
						Icq.sendProcessBuddy(Icq.PROCESS_BUDDY_ADD, uin, newId, 0, types[col]);
						ci.setIntValue(props[col], newId);
					}
					
					if (!newVal && oldVal)
					{
						Icq.sendProcessBuddy(Icq.PROCESS_BUDDY_DELETE, uin, ci.getIntValue(props[col]), 0, types[col]);
						ci.setIntValue(props[col], 0);
					}
				}
			}
			
			Icq.sendCLI_ADDEND();
			
			ContactList.safeSave();
			
		}
		catch (Exception e) { }
		
		JimmUI.backToLastScreen();
	}	
}


