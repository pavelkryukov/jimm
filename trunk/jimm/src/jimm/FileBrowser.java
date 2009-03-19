/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-07  Jimm Project

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 ********************************************************************************
 File: src/jimm/FileBrowser.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher, Andrey Kazakov, Artyomov Denis, 
 *******************************************************************************/

//#sijapp cond.if (target!="DEFAULT")&(modules_FILES="true"|modules_HISTORY="true")#
package jimm;

//#sijapp cond.if target="MIDP2"|target="MOTOROLA"|target="RIM"#
import javax.microedition.io.file.*;
//#sijapp cond.elseif target="SIEMENS2"#
//#import com.siemens.mp.io.file.FileConnection;
//#import com.siemens.mp.io.file.FileSystemRegistry;
//#sijapp cond.end#
import javax.microedition.io.Connector;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;

import java.util.Enumeration;
import java.util.Vector;
import java.io.*;
import DrawControls.*;

//#sijapp cond.if target="MOTOROLA"#

interface MotoFileSystem
{
	public void open(String fileName, int mode) throws IOException;
	public void enumRoots(Vector result) throws IOException;
	public void enumFiles(Vector result, String fileName) throws IOException;
	public void closeFileConn() throws IOException;
	public OutputStream openOutputStream() throws IOException;
	public InputStream openInputStream() throws IOException;
	public long fileSize() throws IOException;
}

class MotoFileSystemHelper implements MotoFileSystem
{
	private com.motorola.io.FileConnection fileConn;
	
	public void open(String fileName, int mode) throws IOException
	{
		fileConn = (com.motorola.io.FileConnection)Connector.open("file://" + fileName);
	}
	
	public void enumRoots(Vector result) throws IOException
	{
		String[] roots = com.motorola.io.FileSystemRegistry.listRoots();
		for (int i = 0; i < roots.length; i++) result.addElement(roots[i]);
	}
	
	public void enumFiles(Vector result, String fileName) throws IOException
	{
		try
		{
			com.motorola.io.FileConnection fileconn = (com.motorola.io.FileConnection)Connector.open("file://" + fileName);
			String[] list = fileconn.list();
			fileconn.close();
			result.addElement(FileSystem2.PARENT_DIRECTORY);
			for (int i = 0; i < list.length; i++) result.addElement(list[i]);
		}
		catch (Exception e) {}
	}
	
	public void closeFileConn() throws IOException
	{
		if (fileConn == null) return;
		fileConn.close();
		fileConn = null;
	}
	
	public OutputStream openOutputStream() throws IOException
	{
		if ( !fileConn.exists() ) fileConn.create();
		return fileConn.openOutputStream();
	}
	
	public InputStream openInputStream() throws IOException
	{
		return fileConn.openInputStream();
	}
	
	public long fileSize() throws IOException
	{
		return (fileConn == null) ? -1 : fileConn.fileSize();
	}
	
}

class StdFileSystemHelper implements MotoFileSystem
{
	private FileConnection fileConn;
	
	public void open(String fileName, int mode) throws IOException
	{
		fileConn = (FileConnection)Connector.open("file://" + fileName, mode);
	}
	
	public void enumRoots(Vector result)
	{
		Enumeration roots = FileSystemRegistry.listRoots();
		while (roots.hasMoreElements()) result.addElement(roots.nextElement());
	}
	
	public void enumFiles(Vector result, String fileName) throws IOException
	{
		FileConnection fileconn = (FileConnection) Connector.open("file://" + fileName, Connector.READ);
		Enumeration list = fileconn.list("*", true);
		fileconn.close();
		
		result.addElement(FileSystem2.PARENT_DIRECTORY);
		String filename;
		while (list.hasMoreElements())
		{
			filename = (String) list.nextElement();
			result.addElement(filename);
		}
		filename = null;
	}
	
	public void closeFileConn() throws IOException
	{
		if (fileConn == null) return;
		fileConn.close();
		fileConn = null;
	}
	
	public OutputStream openOutputStream() throws IOException
	{
		if ( !fileConn.exists() ) fileConn.create();
		return fileConn.openOutputStream();
	}
	
	public InputStream openInputStream() throws IOException
	{
		return fileConn.openInputStream();
	}
	
	public long fileSize() throws IOException
	{
		return (fileConn == null) ? -1 : fileConn.fileSize();
	}
	
}
//#sijapp cond.end#

class FileSystem2 implements CommandListener, Runnable
{
	private CommandListener externalListener;
	private TextList list;
	final static String ROOT_DIRECTORY = "/";
	final static String PARENT_DIRECTORY = "../";
	private ImageList imageList;
	private final static int MODE_SCAN_DIRECTORY = 1;
	private final static int MODE_SHOW_RESULTS   = 2;
	private int currentMode;
	private String currentDir;
	private String selectedItem;
	private Vector items;
	private boolean onlyDirs;
	
	//#sijapp cond.if target="MOTOROLA"#
	MotoFileSystem motoFileConnection;
	//#sijapp cond.else#
	private FileConnection fileConnection;
	//#sijapp cond.end#
	
	public FileSystem2()
	{
		//#sijapp cond.if target="MOTOROLA"#
		try
		{
			Class.forName("javax.microedition.io.file.FileConnection");
			motoFileConnection = new StdFileSystemHelper();
		}
		catch (ClassNotFoundException cnfe)
		{
			motoFileConnection = new MotoFileSystemHelper();
		}
		//#sijapp cond.end#
		
		imageList = new ImageList();
		try
		{
//#sijapp cond.if target="MIDP2" | target="SIEMENS2"#
			imageList.setScale(Options.getInt(Options.OPTION_IMG_SCALE));
//#sijapp cond.end#
			imageList.load("/fs.png", -1, -1, -1, Jimm.getPhoneVendor() == Jimm.PHONE_NOKIA);
		} catch (java.io.IOException e) {}		
		
		items = new Vector();
		list = new TextList(null);
	}
	
	public void browse(String root, CommandListener externalListener, boolean onlyDirs)
	{
		this.externalListener = externalListener;
		this.onlyDirs = onlyDirs;
		
		if (root == null) root = ROOT_DIRECTORY;
		
		list.removeAllCommands();
		
		list.addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
		
		if (onlyDirs)
		{
//#sijapp cond.if target!="RIM" & target!="DEFAULT"#
			list.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
//#sijapp cond.end#
			list.addCommandEx(JimmUI.cmdSelect2, VirtualList.MENU_TYPE_RIGHT);
			list.addCommandEx(JimmUI.cmdOk, VirtualList.MENU_TYPE_RIGHT);
		}
		else
			list.addCommandEx(JimmUI.cmdSelect, VirtualList.MENU_TYPE_RIGHT_BAR);
		
		JimmUI.setColorScheme(list, false, -1, true);
		list.setMode(VirtualList.CURSOR_MODE_DISABLED);
		list.activate(Jimm.display);
		list.setCommandListener(this);
		showFolder(root);
	}
	

	private void showFolder(String path)
	{
		if (currentMode == MODE_SCAN_DIRECTORY) return;
		currentMode = MODE_SCAN_DIRECTORY;
		currentDir = path;
		new Thread(this).start();
	}
	
	//#sijapp cond.if target="MOTOROLA"#
	private void enumRootsMoto(Vector result) throws IOException
	{
		motoFileConnection.enumRoots(result);
	}
	
	private void enumFilesMoto(Vector result, String fileName) throws IOException
	{
		motoFileConnection.enumFiles(result, fileName);
	}
	
	//#sijapp cond.else#
	
	private void enumRootsStd(Vector result)
	{
		Enumeration roots = FileSystemRegistry.listRoots();
		while (roots.hasMoreElements()) result.addElement(roots.nextElement());
	}
	
	private void enumFilesStd(Vector result, String fileName) throws IOException
	{
		FileConnection fileconn;
		//#sijapp cond.if target="SIEMENS2"#
		fileconn = (FileConnection) Connector.open("file://" + fileName);
		//#sijapp cond.else#
		fileconn = (FileConnection) Connector.open("file://localhost"+fileName, Connector.READ);
		//#sijapp cond.end#

		Enumeration list = fileconn.list("*", true);
		fileconn.close();
		
		result.addElement(PARENT_DIRECTORY);
		String filename;
		while (list.hasMoreElements())
		{
			filename = (String) list.nextElement();
			result.addElement(filename);
		}
		filename = null;
	}
	
	//#sijapp cond.end#
	
	public void run() 
	{
		switch (currentMode)
		{
		case MODE_SCAN_DIRECTORY:
			try
			{
				items.removeAllElements();
				if (currentDir.equals(ROOT_DIRECTORY))
				{
					//#sijapp cond.if target="MOTOROLA"#
					enumRootsMoto(items);
					//#sijapp cond.else#
					enumRootsStd(items);
					//#sijapp cond.end#
				}
				else
				{
					//#sijapp cond.if target="MOTOROLA"#
					enumFilesMoto(items, currentDir);
					//#sijapp cond.else#
					enumFilesStd(items, currentDir);
					//#sijapp cond.end#
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			currentMode = MODE_SHOW_RESULTS;
			Jimm.display.callSerially(this);
			break;
			
		case MODE_SHOW_RESULTS:
			
			// Show last path element at caption 
			int index1 = -1, index2 = -1;
			boolean isDelim = false;
			for (int i = currentDir.length()-1; i >= 0; i--)
			{
				isDelim = (currentDir.charAt(i) == '/');
				if (isDelim)
				{
					if (index1 == -1) index1 = i;
					else if (index2 == -1) index2 = i;
					else break;
				}
			}
			
			list.setCaption
			(
				(index1 != -1) && (index2 != -1) ? 
				currentDir.substring(index2+1, index1) : 
				"Root"
			);

			// Show directory content
			list.clear();
			
			// Show dirs
			String itemText;
			for (int i = 0; i < items.size(); i++)
			{
				itemText = (String)items.elementAt(i);
				if (!itemText.endsWith("/")) continue;
				JimmUI.addTextListItem(list, itemText, imageList.elementAt(0), i, true, -1, Font.STYLE_PLAIN);
			}
			
			// Show files
			if (!onlyDirs) for (int i = 0; i < items.size(); i++)
			{
				itemText = (String)items.elementAt(i);
				if (itemText.endsWith("/")) continue;
				JimmUI.addTextListItem(list, itemText, imageList.elementAt(1), i, true, -1, Font.STYLE_PLAIN);
			}
			break;
		}
	}
	
	public void commandAction(Command c, Displayable d)
	{
		Jimm.aaUserActivity();
		
		if (c == JimmUI.cmdSelect2 || c == JimmUI.cmdOk || c == JimmUI.cmdSelect) 
		{
			int index = list.getCurrTextIndex();
			if (index < 0) return;
			String itemText = (String)items.elementAt(index);
			selectedItem = currentDir+itemText;
			if (itemText == PARENT_DIRECTORY)
			{
				String parentDir = null;
				for (int i = currentDir.length()-2; i >= 0; i--) if (currentDir.charAt(i) == '/')
				{
					parentDir = currentDir.substring(0, i+1);
					break;
				}
				if (parentDir != null) showFolder(parentDir);
			}
			else if (itemText.endsWith("/"))
			{
				if (c == JimmUI.cmdOk || c == JimmUI.cmdSelect)
					showFolder(selectedItem);
				else if (c == JimmUI.cmdSelect2)
					externalListener.commandAction(JimmUI.cmdOk, d);
			}
			else
			{
				externalListener.commandAction(JimmUI.cmdOk, d);
			}
				
		}
		else if (c == JimmUI.cmdBack) externalListener.commandAction(JimmUI.cmdBack, d);
	}
	
	public boolean isActive()
	{
		return (list == null) ? false : list.isActive();
	}
	
	public String getValue()
	{
		return selectedItem;
	}
	
	public void openFile(String fileName, int mode) throws IOException
	{
		close();
		//#sijapp cond.if target="MOTOROLA"#
		motoFileConnection.open(fileName, mode);
		//#sijapp cond.else#
		fileConnection = (FileConnection) Connector.open("file://" + fileName, mode);
		//#sijapp cond.end#
	}
	
	public InputStream openInputStream() throws IOException
	{
		InputStream result;
		//#sijapp cond.if target="MOTOROLA"#
		result = motoFileConnection.openInputStream();
		//#sijapp cond.else#
		result = fileConnection.openInputStream();
		//#sijapp cond.end#
		return result;
	}
	
	public OutputStream openOutputStream() throws IOException
	{
		OutputStream result;
		//#sijapp cond.if target="MOTOROLA"#
		result = motoFileConnection.openOutputStream();
		//#sijapp cond.else#
		if ( !fileConnection.exists() ) fileConnection.create();
		result = fileConnection.openOutputStream();
		//#sijapp cond.end#
		return result;
	}
	
	public void close() throws IOException
	{
		//#sijapp cond.if target="MOTOROLA"#
		motoFileConnection.closeFileConn();
		//#sijapp cond.else#
		if (fileConnection != null)
		{
			fileConnection.close();
			fileConnection = null;
		}
		//#sijapp cond.end#
	}
	
	public long fileSize() throws IOException
	{
		long result = 0;
		//#sijapp cond.if target="MOTOROLA"#
		result = motoFileConnection.fileSize();
		//#sijapp cond.else#
		result = (fileConnection == null) ? -1 : fileConnection.fileSize();
		//#sijapp cond.end#
		return result;
	}
}

//#sijapp cond.end#