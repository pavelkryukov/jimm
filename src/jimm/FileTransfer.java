/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-05  Jimm Project

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
 File: src/jimm/FileTransfer.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher, Dmitry Tunin
 *******************************************************************************/

//#sijapp cond.if (target="MIDP2"|target="MOTOROLA"|target="SIEMENS2")&modules_FILES="true"#
package jimm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA"#
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.io.file.FileConnection;
//#sijapp cond.elseif target is "SIEMENS2"#
//# import com.siemens.mp.io.file.FileConnection;
//# import com.siemens.mp.io.file.FileSystemRegistry;
//#sijapp cond.end#
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
//#sijapp cond.if target isnot "MOTOROLA"#
import javax.microedition.media.control.VideoControl;
//#sijapp cond.end#

import jimm.comm.Icq;
import jimm.comm.FileTransferMessage;
import jimm.comm.Message;
import jimm.comm.SendMessageAction;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

public class FileTransfer implements CommandListener, FileBrowserListener
{
	// Type of filetrasfer
	public static final int FT_TYPE_FILE_BY_NAME = 1;

	//#sijapp cond.if target isnot "MOTOROLA" #
	public static final int FT_TYPE_CAMERA_SNAPSHOT = 2;

	//#sijapp cond.end #

	// Request
	private String reqUin;

	//#sijapp cond.if target isnot "MOTOROLA" #
	// Viewfinder
	private ViewFinder vf;

	//#sijapp cond.end #

	// Form for entering the name and description
	private Form name_Desc;

	// File data
	private InputStream fis;

	private int fsize;

	// File path and description TextField
	private TextField fileNameField;

	private TextField descriptionField;

	// Type and ContactListContactItem
	private int type;

	private ContactListContactItem cItem;

	// Commands
	private Command backCommand = new Command(ResourceBundle.getString("back"),
			Command.BACK, 2);

	private Command okCommand = new Command(ResourceBundle.getString("ok"),
			Command.OK, 1);

	// Constructor
	public FileTransfer(int ftType, ContactListContactItem _cItem)
	{
		type = ftType;
		cItem = _cItem;

	}

	// Return the cItem belonging to this FileTransfer
	public ContactListContactItem getCItem()
	{
		return (this.cItem);
	}

	// Set the file data
	public void setData(InputStream is, int size)
	{
		fis = is;
		fsize = size;
	}

	// Start the file transfer procedure depening on the ft type
	public void startFT()
	{
		//#sijapp cond.if target isnot "MOTOROLA" #
		if (type == FileTransfer.FT_TYPE_CAMERA_SNAPSHOT)
		{
			if (!System.getProperty("supports.video.capture").equals("true"))
				JimmException.handleException(new JimmException(185, 0, true));
			else
			{
				vf = new ViewFinder();
				Display.getDisplay(Jimm.jimm).setCurrent(vf);
				vf.start();
			}
		}

		else if (type == FileTransfer.FT_TYPE_FILE_BY_NAME)
		//#sijapp cond.end #
		{
			try
			{
				FileBrowser.setListener(this);
				FileBrowser.setParameters(false);
				FileBrowser.activate();
			} catch (JimmException e)
			{
				JimmException.handleException(e);
			}

		}

	}

	//
	public void onFileSelect(String fileName)
	{
		try
		{
			InputStream fis = null;
			int size = 0;
			FileSystem file = FileSystem.getInstance();
			file.openFile(fileName);
			fis = file.openInputStream();
			size = (int) file.fileSize();
			// Set the file data in file transfer
			setData(fis, size);
			// Create filename and ask for name and description
			//#sijapp cond.if target is "SIEMENS2"| target is "MIDP2"#
			askForNameDesc(file.getName(), "");
			//#sijapp cond.else#
			//#			askForNameDesc(fileName, "");
			//#sijapp cond.end#
		} catch (Exception e)
		{
			e.printStackTrace();
			JimmException.handleException(new JimmException(191, 0, true));
		}
	}

	//
	public void onDirectorySelect(String s0)
	{
	}

	// Init the ft
	public void initFT(String filename, String description)
	{
		//#sijapp cond.if target isnot "MOTOROLA" #
		this.vf = null;
		//#sijapp cond.end #

		// Set the splash screen
		SplashCanvas.setProgress(0);
		SplashCanvas.setMessage(ResourceBundle.getString("init_ft"));
		SplashCanvas.addCmd(SplashCanvas.cancelCommnad);
		SplashCanvas.setCmdListener(this);
		SplashCanvas.show();

		// Send the ft message
		FileTransferMessage ftm = new FileTransferMessage(Options
				.getString(Options.OPTION_UIN), this.cItem,
				Message.MESSAGE_TYPE_EXTENDED, filename, description, fis,
				fsize);
		SendMessageAction act = new SendMessageAction(ftm);
		try
		{
			Icq.requestAction(act);
		} catch (JimmException e)
		{
			JimmException.handleException(e);
			if (e.isCritical())
				return;
		}

	}

	public void askForNameDesc(String filename, String description)
	{
		name_Desc = new Form(ResourceBundle.getString("name_desc"));
		this.fileNameField = new TextField(
				ResourceBundle.getString("filename"), filename, 255,
				TextField.ANY);
		this.descriptionField = new TextField(ResourceBundle
				.getString("description"), description, 255, TextField.ANY);

		name_Desc.append(this.fileNameField);
		name_Desc.append(this.descriptionField);
		name_Desc.append(new StringItem(
				ResourceBundle.getString("size") + ": ", String
						.valueOf(fsize / 1024)
						+ " kb"));
		//#sijapp cond.if modules_TRAFFIC is "true" #
		name_Desc.append(new StringItem(
				ResourceBundle.getString("cost") + ": ",
				Traffic.getString(((fsize / Options
						.getInt(Options.OPTION_COST_PACKET_LENGTH)) + 1)
						* Options.getInt(Options.OPTION_COST_PER_PACKET))
						+ " " + Options.getString(Options.OPTION_CURRENCY)));
		//#sijapp cond.end #

		name_Desc.addCommand(this.backCommand);
		name_Desc.addCommand(this.okCommand);
		name_Desc.setCommandListener(this);

		Jimm.display.setCurrent(name_Desc);
	}

	// Command listener
	public void commandAction(Command c, Displayable d)
	{
		if (c == this.okCommand)
		{
			if (d == this.name_Desc)
			{
				this.initFT(this.fileNameField.getString(),
						this.descriptionField.getString());
			}
		} else if (c == this.backCommand)
		{
			free();
			this.getCItem().activate(true);
		} else if (c == SplashCanvas.cancelCommnad)
		{
			free();
			ContactList.activate();
		}
	}

	private void free()
	{
		//#sijapp cond.if target isnot "MOTOROLA" #
		vf = null;
		//#sijapp cond.end #
		fis = null;
		name_Desc = null;
		fileNameField = null;
		System.gc();
	}

	/** ************************************************************************* */
	/** ************************************************************************* */
	/** ************************************************************************* */
	//#sijapp cond.if target isnot "MOTOROLA" #
	// Class for viewfinder
	public class ViewFinder extends Canvas implements CommandListener
	{

		// Resolution matrix
		private final String res[][] =
		{
		{ "80", "160", "320", "640" },
		{ "60", "120", "240", "480" } };

		// Variables
		private Player p = null;

		private VideoControl vc = null;

		private boolean active = false;

		private boolean viewfinder = true;

		private Image img;

		private byte[] data;

		//private byte[] data_pre;

		private int res_marker = 0;

		// Commands
		private Command backCommand;

		private Command okCommand;

		private Command resCommand;

		private Command selectCommand;

		public ViewFinder()
		{

			backCommand = new Command(ResourceBundle.getString("back"),
					Command.BACK, 2);
			okCommand = new Command(ResourceBundle.getString("ok"),
					Command.SCREEN, 1);
			// resCommand = new Command(ResourceBundle.getString("res"), Command.SCREEN, 2);
			selectCommand = new Command(ResourceBundle.getString("back"),
					Command.OK, 1);

			this.addCommand(backCommand);
			this.addCommand(okCommand);
			// this.addCommand(resCommand);
			this.setCommandListener(this);
		}

		private void reset()
		{
			if (p != null)
			{
				try
				{
					if (p.getState() == Player.STARTED)
						p.stop();
					p.close();
				} catch (Exception e)
				{
				}
				p = null;
			}

			if (vc != null)
			{
				vc.setVisible(false);
				vc = null;
			}
			System.gc();
		}

		// paint method, inherid form Canvas
		protected void paint(Graphics g)
		{
			g.setColor(0xffffffff);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			if (!viewfinder && (img != null))
			{
				g.drawImage(img, getWidth() / 2, getHeight() / 2,
						Graphics.VCENTER | Graphics.HCENTER);
				// System.out.println("drawedImage");
			}

			g.setColor(0x00000000);
			if (viewfinder)
				g.drawString(ResourceBundle.getString("viewfinder") + " "
						+ this.res[0][this.res_marker] + "x"
						+ this.res[1][this.res_marker], 1, 1, Graphics.TOP
						| Graphics.LEFT);
			else
				g.drawString(ResourceBundle.getString("send_img") + "? "
						+ this.res[0][this.res_marker] + "x"
						+ this.res[1][this.res_marker], 1, 1, Graphics.TOP
						| Graphics.LEFT);
		}

		// start the viewfinder
		public synchronized void start()
		{
			reset();
			if (!active)
			{
				try
				{
					// Create the player
					p = Manager.createPlayer("capture://video");
					p.realize();

					// Get the video control
					vc = (VideoControl) p.getControl("VideoControl");

					if (vc != null)
					{
						vc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);

						int canvasWidth = this.getWidth();
						int canvasHeight = this.getHeight();
						int displayWidth = vc.getDisplayWidth();
						int displayHeight = vc.getDisplayHeight();
						int x = (canvasWidth - displayWidth) / 2;
						int y = (canvasHeight - displayHeight) / 2;

						vc.setDisplayLocation(x, y);

						p.start();
						vc.setVisible(true);
						active = true;

					} else
					{
						JimmException.handleException(new JimmException(180, 0,
								true));
					}
				} catch (IOException ioe)
				{
					reset();
					JimmException.handleException(new JimmException(181, 0,
							true));

				} catch (MediaException me)
				{
					reset();
					JimmException.handleException(new JimmException(181, 1,
							true));
				} catch (SecurityException se)
				{
					reset();
					JimmException.handleException(new JimmException(181, 2,
							true));
				}
			}
		}

		private byte[] getSnapshot(String type)
		{
			byte[] data;
			try
			{
				data = vc.getSnapshot(type);
			} catch (Exception e)
			{
				return null;
			}
			return data;
		}

		// take a snapshot form the viewfinder
		public void takeSnapshot()
		{
			if (p != null)
			{
				data = getSnapshot("encoding=jpeg");
				if (data == null)
					data = getSnapshot("JPEG");
				else if (data == null)
					data = getSnapshot(null);
				if (data == null)
					JimmException.handleException(new JimmException(183, 0,
							true));
				this.stop();
				img = Image.createImage(data, 0, data.length);
				viewfinder = false;
				repaint();
			}
		}

		// stop the viewfinder
		public synchronized void stop()
		{
			if (active)
			{
				try
				{
					vc.setVisible(false);
					p.stop();

					// Remove video control at SE phones placing it beyond screen border
					//#sijapp cond.if target is "MIDP2" #
					if (Jimm.is_phone_SE())
						vc.setDisplayLocation(1000, 1000);
					//#sijapp cond.end #
				} catch (Exception e)
				{
					reset();
				}
				active = false;
			}
		}

		// action listener
		public void commandAction(Command c, Displayable d)
		{
			if (c == this.okCommand)
			{
				if (!viewfinder)
				{
					this.stop();
					this.reset();
					FileTransfer.this.setData(new ByteArrayInputStream(data),
							data.length);
					FileTransfer.this.askForNameDesc("jimm_cam"
							+ Util.getCounter() + ".jpeg", "");
				}
				this.takeSnapshot();
			} else if (c == this.backCommand)
			{
				if (!viewfinder)
				{
					viewfinder = true;
					start();
				} else
				{
					this.stop();
					this.reset();
					ContactList.activate();
					FileTransfer.this.vf = null;
				}
			} else if (c == this.resCommand)
			{
				this.res_marker++;
				this.res_marker = this.res_marker % this.res[0].length;
				//this.repaint();
			}
		}

		// Key pressed
		public void keyPressed(int keyCode)
		{
			if (getGameAction(keyCode) == FIRE)
				if (!viewfinder)
				{
					this.stop();
					this.reset();
					FileTransfer.this.setData(new ByteArrayInputStream(data),
							data.length);
					FileTransfer.this.askForNameDesc("jimm_cam"
							+ Util.getCounter() + ".jpeg", "");
				} else
				{
					this.takeSnapshot();
				}

		}

	}
	//#sijapp cond.end #

}
//#sijapp cond.end#