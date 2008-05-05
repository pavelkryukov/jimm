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
 Author(s): Andreas Rossbacher, Dmitry Tunin, Denis Artyomov
 *******************************************************************************/

//#sijapp cond.if (target="MIDP2"|target="MOTOROLA"|target="SIEMENS2")&modules_FILES="true"#
package jimm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.*;
import javax.microedition.media.*;
//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA"#
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.io.file.FileConnection;
//#sijapp cond.elseif target is "SIEMENS2"#
//# import com.siemens.mp.io.file.FileConnection;
//# import com.siemens.mp.io.file.FileSystemRegistry;
//#sijapp cond.end#
import javax.microedition.lcdui.*;
//#sijapp cond.if target isnot "MOTOROLA"#
import javax.microedition.media.control.VideoControl;
//#sijapp cond.end#

import DrawControls.*;
import jimm.comm.Icq;
import jimm.comm.FileTransferMessage;
import jimm.comm.Message;
import jimm.comm.PlainMessage;
import jimm.comm.SendMessageAction;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

public class FileTransfer implements CommandListener, Runnable
{
	private static final int MODE_CHECK_FILE_LEN   = 10001;
	private static final int MODE_SHOW_DESC_FORM   = 10002;
	private static final int MODE_SEND_THROUGH_WEB = 10003;
	private static final int MODE_BACK_TO_MENU     = 10004;
	
	private static final int WEB_ASK_RESULT_YES = 20000;
	private static final int WEB_ASK_RESULT_NO  = 20001;
	
	private int curMode;
	
	// Type of filetrasfer
	public static final int FT_TYPE_FILE_BY_NAME = 1;

	//#sijapp cond.if target isnot "MOTOROLA" #
	public static final int FT_TYPE_CAMERA_SNAPSHOT = 2;
	//#sijapp cond.end #

	// Viewfinder
	//#sijapp cond.if target isnot "MOTOROLA" #
	private ViewFinder vf;
	//#sijapp cond.end #

	// Form for entering the name and description
	private Form name_Desc;

	// File data
	private InputStream fis;

	private int fsize;
	
	private String exceptionText;
	
	TextList tlWebAsk;

	// File path and description TextField
	private TextField fileNameField;

	private TextField descriptionField;

	// Type and ContactItem
	private int type;
	
	private Alert alert;

	private ContactItem cItem;
	
	private String fileName, shortFileName;
	private FileSystem2 fileSystem;

	// Commands
	private Command backCommand = new Command(ResourceBundle.getString("back"),
			Command.BACK, 2);

	private Command okCommand = new Command(ResourceBundle.getString("ok"),
			Command.OK, 1);

	// Constructor
	public FileTransfer(int ftType, ContactItem _cItem)
	{
		type = ftType;
		cItem = _cItem;
	}

	// Return the cItem belonging to this FileTransfer
	public ContactItem getCItem()
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
		// Ask user about web file transfer
		if (Options.getBoolean(Options.OPTION_ASK_FOR_WEB_FT) 
		   && (Options.getInt(Options.OPTION_FT_MODE) == Options.FS_MODE_WEB))
		{
			tlWebAsk = new TextList(ResourceBundle.getString("ft_caption"));
			JimmUI.setColorScheme(tlWebAsk, true, -1);
			
			tlWebAsk.addBigText(ResourceBundle.getString("ft_web_ask"), tlWebAsk.getTextColor(), Font.STYLE_PLAIN, -1);
			tlWebAsk.doCRLF(-1);
			tlWebAsk.doCRLF(-1);
			tlWebAsk.addBigText(ResourceBundle.getString("ft_web_yes"), tlWebAsk.getTextColor(), Font.STYLE_BOLD, WEB_ASK_RESULT_YES);
			tlWebAsk.doCRLF(1);
			tlWebAsk.addBigText(ResourceBundle.getString("ft_web_no"), tlWebAsk.getTextColor(), Font.STYLE_BOLD, WEB_ASK_RESULT_NO);
			tlWebAsk.doCRLF(2);
			tlWebAsk.selectTextByIndex(WEB_ASK_RESULT_YES);
			tlWebAsk.addCommandEx(JimmUI.cmdSelect, VirtualList.MENU_TYPE_RIGHT_BAR);
			tlWebAsk.setCommandListener(this);
			tlWebAsk.activate(Jimm.display);
			return;
		}
		else startFtInternal();
	}
	
	private void startFtInternal()
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
				Jimm.setBkltOn(true);
				vf.start();
			}
		}

		else if (type == FileTransfer.FT_TYPE_FILE_BY_NAME)
		//#sijapp cond.end #
		{
			try
			{
				fileSystem = new FileSystem2();
				fileSystem.browse(null, this, false);
			} catch (Exception e)
			{
				//JimmException.handleException(e); TODO: do correct error handling
			}

		}
	}
	
	public void run() 
	{
		switch (curMode)
		{
		case MODE_CHECK_FILE_LEN:
			try
			{
				fileSystem.openFile(fileName, Connector.READ);
				fsize = (int)fileSystem.fileSize();
				fis = fileSystem.openInputStream();
				curMode = MODE_SHOW_DESC_FORM;
				Jimm.display.callSerially(this);
			} catch (Exception e) 
			{
				e.printStackTrace();
			}
			break;
			
		case MODE_SHOW_DESC_FORM:
			askForNameDesc(fileName, "");
			break;
			
		case MODE_SEND_THROUGH_WEB:
			try
			{
				sendFileThroughWebThread();
			}
			catch (JimmException e)
			{
				exceptionText = e.getMessage();
			}
			
			curMode = MODE_BACK_TO_MENU;
			Jimm.display.callSerially(this);
			
			break;
			
		case MODE_BACK_TO_MENU:
			free();
			if (exceptionText != null)
			{
				alert = new Alert(ResourceBundle.getString("ft_error"), exceptionText, null, AlertType.ERROR);
				alert.setCommandListener(this);
				alert.setTimeout(Alert.FOREVER);
				System.out.println(exceptionText);
				Jimm.display.setCurrent(alert);
				Jimm.setBkltOn(false);
				System.out.println("END");
			}
			else cItem.activate();
			break;
		}
	}
	
	private void sendFileThroughWebThread() throws JimmException
	{
		InputStream is;
		OutputStream os;
		HttpConnection sc;
		
		exceptionText = null;
		
		String host = "filetransfer.jimm.org";
		String url = "http://"+host+"/__receive_file.php";
		
		try
		{
			sc = (HttpConnection) Connector.open(url, Connector.READ_WRITE);
			sc.setRequestMethod(HttpConnection.POST);
			
			String boundary = "a9f843c9b8a736e53c40f598d434d283e4d9ff72";
			
			sc.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
			os = sc.openOutputStream();
			
			// Send post header
			StringBuffer buffer2 = new StringBuffer();
			buffer2.append("--").append(boundary).append("\r\n");
			buffer2.append("Content-Disposition: form-data; name=\"jimmfile\"; filename=\"").append(shortFileName).append("\"\r\n");
			buffer2.append("Content-Type: application/octet-stream\r\n");
			buffer2.append("Content-Transfer-Encoding: binary\r\n");
			buffer2.append("\r\n");
			os.write(Util.stringToByteArray(buffer2.toString(), true));

			// Send file data and show progress
			byte[] buffer = new byte[1024];
			int counter = fsize;
			do 
			{
				int read = fis.read(buffer);
				os.write(buffer, 0, read);
				counter -= read;
				if (fsize != 0)
				{
					int percent = 100*(fsize-counter)/fsize;
					SplashCanvas.setProgress(percent);
					SplashCanvas.setMessage(ResourceBundle.getString("ft_transfer")+" "+percent+"% / "+fsize/1024+"KB");
				}
			} while (counter > 0);
			
			// Send end of header
			StringBuffer buffer3 = new StringBuffer();
			buffer3.append("\r\n--").append(boundary).append("--\r\n");
			os.write(Util.stringToByteArray(buffer3.toString(), true));
			os.flush();

			int respCode = sc.getResponseCode();
			if (respCode != HttpConnection.HTTP_OK) throw new JimmException(194, respCode);
			
			// Read response
			is = sc.openInputStream();
			
			StringBuffer response = new StringBuffer();
			for (;;)
			{
				int read = is.read();
				if (read == -1) break; 
				response.append((char)(read & 0xFF));
			}
			
			String respString = response.toString();
			
			System.out.println(respString);
			
			int dataPos = respString.indexOf("http://");
			if (dataPos == -1) throw new JimmException(195, 0);
			
			respString = Util.replaceStr(respString, "\r\n", "");
			respString = Util.replaceStr(respString, "\r", "");
			respString = Util.replaceStr(respString, "\n", "");
			
			System.out.println(respString);

			// Close all http connection headers 
			os.close();
			is.close();
			sc.close();
		
			// Send info about file
			StringBuffer messText = new StringBuffer();
			messText.append("Filename: ").append(shortFileName).append("\n");
			messText.append("Filesize: ").append(fsize/1024).append("KB\n");
			messText.append("Link: ").append(respString);
		
			PlainMessage plainMsg = new PlainMessage(Options.getString(Options.OPTION_UIN), cItem, Message.MESSAGE_TYPE_NORM, Util.createCurrentDate(false), messText.toString());
			Icq.requestAction(new SendMessageAction(plainMsg));
		}
		catch (IOException e)
		{
			throw new JimmException(196, 0);
		}
	}
	
	public static FileTransferMessage getFTM()
	{
		return ftm;
	}
	
	public static void clearFTM()
	{
		ftm = null; 
	}
	
	private static FileTransferMessage ftm;

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
		ftm = new FileTransferMessage(Options
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
			if (e.isCritical()) return;
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
		Jimm.setBkltOn(true);
	}

	// Command listener
	public void commandAction(Command c, Displayable d)
	{
		if (JimmUI.isControlActive(tlWebAsk) && (c == JimmUI.cmdSelect))
		{
			int index = tlWebAsk.getCurrTextIndex(); 
			switch (index)
			{
			case WEB_ASK_RESULT_NO:
			case WEB_ASK_RESULT_YES:
				Options.setInt(Options.OPTION_FT_MODE, (index == WEB_ASK_RESULT_NO) ? Options.FS_MODE_NET : Options.FS_MODE_WEB);
				Options.setBoolean(Options.OPTION_ASK_FOR_WEB_FT, false);
				Options.safe_save();
				startFtInternal();
				return;
				
			default:
				return;
			}
		}
		else if ((alert != null) && (d == alert))
		{
			System.out.println("cItem.activate();");
			cItem.activate();
		}
		else if ((fileSystem != null) && fileSystem.isActive())
		{
			if (c == JimmUI.cmdOk)
			{
				curMode = MODE_CHECK_FILE_LEN;
				fileName = fileSystem.getValue();
				new Thread(this).start();
			}
			else
			{
				free();
				this.getCItem().activate();
			}
		}
		else
		{
			if (c == this.okCommand)
			{
				if (d == this.name_Desc)
				{
					switch (Options.getInt(Options.OPTION_FT_MODE))
					{
					case Options.FS_MODE_NET:
						this.initFT(this.fileNameField.getString(), this.descriptionField.getString());
						break;
						
					case Options.FS_MODE_WEB:
				        SplashCanvas.setProgress(0);
				        SplashCanvas.setMessage(ResourceBundle.getString("init_ft"));
				        SplashCanvas.removeCmd(SplashCanvas.cancelCommnad);
				        SplashCanvas.setCmdListener(this);
						SplashCanvas.show();
						
						fileName = this.fileNameField.getString();
						String[] fnItems = Util.explode(fileName, '/');
						shortFileName = (fnItems.length == 0) ? fileName : fnItems[fnItems.length-1];  
						curMode = MODE_SEND_THROUGH_WEB;
						new Thread(this).start();
						break;
					}
				}
			} else if (c == this.backCommand)
			{
				free();
				this.getCItem().activate();
			} else if (c == SplashCanvas.cancelCommnad)
			{
				free();
				ContactList.activate();
			}
		}
	}

	private void free()
	{
		//#sijapp cond.if target isnot "MOTOROLA" #
		vf = null;
		//#sijapp cond.end #
		try
		{
			if (fis != null) fis.close();
		} catch (Exception e) {} // Do nothing
		
		try
		{
			if (fileSystem != null) fileSystem.close();
		} catch (Exception e) {} // Do nothing
		
		fileSystem = null;
		fis = null;
		name_Desc = null;
		fileNameField = null;
		descriptionField = null;
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

		private int res_marker = 0;

		private int sourceWidth = 0;

		private int sourceHeight = 0;

		// Commands
		private Command backCommand;

		private Command okCommand;

		private Command resCommand;

		public ViewFinder()
		{

			backCommand = new Command(ResourceBundle.getString("back"),
					Command.BACK, 2);
			okCommand = new Command(ResourceBundle.getString("ok"),
					Command.SCREEN, 1);

			this.addCommand(backCommand);
			this.addCommand(okCommand);
			// this.addCommand(resCommand);
			this.setCommandListener(this);
		}

		private void reset()
		{
			img = null;
			if (vc != null)
			{
				vc.setVisible(false);
				vc = null;
			}
			if (p != null)
			{
				try
				{
					if (p.getState() == Player.STARTED)
						p.stop();
			        	p.deallocate();
					p.close();
				} catch (Exception e)
				{
				}
				p = null;
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
						+ (getWidth()-2) + "x"
						+ (getHeight()-2), 1, 1, Graphics.TOP
						| Graphics.LEFT);
			else
				g.drawString(ResourceBundle.getString("send_img") + "? "
						+ sourceWidth + "x"
						+ sourceHeight, 1, 1, Graphics.TOP
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
					String cam_dev = Options.getInt(Options.OPTION_CAMERAURI) == 0 ? "capture://video" : "capture://image";
					p = Manager.createPlayer(cam_dev);
					p.realize();

					// Get the video control
					vc = (VideoControl) p.getControl("VideoControl");

					if (vc != null)
					{
						vc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);

						int canvasWidth = this.getWidth();
						int canvasHeight = this.getHeight();

						try {
						  vc.setDisplayLocation(2, 2);
						  vc.setDisplaySize(canvasWidth - 4, canvasHeight - 4);
						}
						catch (MediaException me) {
						  try { vc.setDisplayFullScreen(true); }
						  catch (MediaException me2) {}
						}

//						int displayWidth = vc.getDisplayWidth();
//						int displayHeight = vc.getDisplayHeight();
//						int x = (canvasWidth - displayWidth) / 2;
//						int y = (canvasHeight - displayHeight) / 2;

//						vc.setDisplayLocation(x, y);

						vc.setVisible(true);
						p.start();
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
				if (data == null)
					data = getSnapshot(null);
				if (data == null)
					JimmException.handleException(new JimmException(183, 0,
							true));
				// this.stop();
				img = Image.createImage(data, 0, data.length);
				sourceWidth = img.getWidth();
				sourceHeight = img.getHeight();
				img = Util.createThumbnail(img, this.getWidth(), this.getHeight());
				viewfinder = false;
				vc.setVisible(false);
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
					FileTransfer.this.askForNameDesc("jimm_cam_"
							+ Util.getDateString(false,Util.createCurrentDate(true))
							+ "_" + Util.getCounter() + ".jpeg", "");
				} else
				this.takeSnapshot();
			} else if (c == this.backCommand)
			{
				if (!viewfinder)
				{
					viewfinder = true;
					active = false;
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
					FileTransfer.this.askForNameDesc("jimm_cam_"
							+ Util.getDateString(false,Util.createCurrentDate(true))
							+ "_" + Util.getCounter() + ".jpeg", "");
				} else
				{
					this.takeSnapshot();
				}

		}

	}
	//#sijapp cond.end #

}
//#sijapp cond.end#
