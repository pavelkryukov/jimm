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

//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA"#
//#sijapp cond.if modules_FILES is "true"#
package jimm;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
//#sijapp cond.if target is "MOTOROLA"#
import com.motorola.io.FileConnection;
// #sijapp cond.else#
import javax.microedition.io.StreamConnection;
// #sijapp cond.end#
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import jimm.comm.FileTransferMessage;
import jimm.comm.SendMessageAction;
import jimm.comm.Util;
import jimm.util.ResourceBundle;


public class FileTransfer implements CommandListener
{
    // #sijapp cond.if target is "MOTOROLA"#
     FileConnection fc;
    // #sijapp cond.end#
    
    // Type of filetrasfer
    public static final int FT_TYPE_FILE_BY_NAME = 1;
    public static final int FT_TYPE_CAMERA_SNAPSHOT = 2;

    // Request
    private String reqUin;

    // Viewfinder
    private ViewFinder vf;

    // File data
    private byte[] data;

    // Form for entering the name and description
    private Form name_Desc;

    // File path and description TextField
    private TextField fileNameField;
    private TextField descriptionField;

    // Type and ContactListContactItem
    private int type;
    private ContactListContactItem cItem;

    // Commands
    private Command backCommand = new Command(ResourceBundle.getString("back"), Command.BACK, 2);
    private Command okCommand = new Command(ResourceBundle.getString("ok"), Command.OK, 1);
    private Command cancelCommand = new Command(ResourceBundle.getString("cancel"), Command.OK, 1);

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
    public void setData(byte[] _data)
    {
        this.data = _data;
    }

    // Start the file transfer procedure depening on the ft type
    public void startFT()
    {
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
        } else if (type == FileTransfer.FT_TYPE_FILE_BY_NAME)
        {
            FileSelector fc = new FileSelector();
            try 
            {
                fc.startFc();
            }
            catch (JimmException e)
            {
                JimmException.handleException(e);
            }
                
        }

    }

    // Init the ft
    public void initFT(String filename, String description)
    {
        this.vf = null;

        // Set the splash screen
        Jimm.jimm.getSplashCanvasRef().setProgress(0);
        Jimm.jimm.getSplashCanvasRef().setMessage(ResourceBundle.getString("init_ft"));
        Jimm.jimm.getSplashCanvasRef().addCommand(this.cancelCommand);
        Jimm.jimm.getSplashCanvasRef().setCommandListener(this);
        Display.getDisplay(Jimm.jimm).setCurrent(Jimm.jimm.getSplashCanvasRef());

        // Send the ft message
        FileTransferMessage ftm = new FileTransferMessage(Jimm.jimm.getIcqRef().getUin(), this.cItem, filename, description, this.data);
        SendMessageAction act = new SendMessageAction(ftm);
        try
        {
            Jimm.jimm.getIcqRef().requestAction(act);

        } catch (JimmException e)
        {
            JimmException.handleException(e);
            if (e.isCritical()) return;
        }

    }

    public void askForNameDesc(String filename, String description)
    {
        name_Desc = new Form(ResourceBundle.getString("name_desc"));
        this.fileNameField = new TextField(ResourceBundle.getString("filename"), filename, 255, TextField.ANY);
        this.descriptionField = new TextField(ResourceBundle.getString("description"), description, 255, TextField.ANY);

        name_Desc.append(this.fileNameField);
        name_Desc.append(this.descriptionField);

        name_Desc.addCommand(this.backCommand);
        name_Desc.addCommand(this.okCommand);
        name_Desc.setCommandListener(this);

        Jimm.display.setCurrent(name_Desc);
    }
    
    // Return the cancel command
    public Command getCancelCommand()
    {
        return (this.cancelCommand);
    }

    // Command listener
    public void commandAction(Command c, Displayable d)
    {
        if (c == this.okCommand)
        {
            if (d == this.name_Desc)
            {
                this.initFT(this.fileNameField.getString(), this.descriptionField.getString());
            } else
            {
                // System.out.println("do filename read");
                // byte array for data and File object for the file itself
                byte[] fileData;
                String path = this.fileNameField.getString();

               // #sijapp cond.if target is "MOTOROLA"#
               try
                {
                    fc = (FileConnection) Connector.open("file://" + path);
                    System.out.println("fc");
                    int size=(int) fc.fileSize();
                    DataInputStream dis = fc.openDataInputStream();
                     fileData = new byte[size];
                    dis.readFully(fileData, 0, size);
                    dis.close();
                     fc.close();
                    // Set the file data in file transfer
                    FileTransfer.this.setData(fileData);

                    // Create filename and ask for name and description
                    int i = path.length() - 1;
                    char backslash = '/';
                    while (path.charAt(i) != backslash)
                        i--;
                    this.askForNameDesc(path.substring(i + 1), "");
                } catch (IOException ioe)
                {
                   // JimmException.handleException(new JimmException(192, 0, true));
                } catch (Exception e)
                {
                    //JimmException.handleException(new JimmException(191, 0, true));
                } 
               // #sijapp cond.else#
               StreamConnection con = null;
                
               try
               {
                   // Try to read the file
                   con = (StreamConnection) Connector.open("file:///" + path);
                   InputStream is = con.openInputStream();
                   DataInputStream dis = new DataInputStream(is);
                   fileData = new byte[dis.available()];
                   is.read(fileData, 0, dis.available());

                   // Set the file data in file transfer
                   FileTransfer.this.setData(fileData);
                   // Create filename and ask for name and description
                   int i = path.length() - 1;
                   char backslash = '/';
                   while (path.charAt(i) != backslash)
                       i--;
                   this.askForNameDesc(path.substring(i + 1), "");
               } catch (IOException ioe)
               {
                   JimmException.handleException(new JimmException(192, 0, true));
               } catch (Exception e)
               {
                   JimmException.handleException(new JimmException(191, 0, true));
               } finally
               {
                   if (con != null) try
                   {
                       con.close();
                   } catch (IOException e)
                   {
                   } // Do nothing
               }
               // #sijapp cond.end#
            }

        } else if (c == this.backCommand)
        {

            this.getCItem().activateMenu();
        } else if (c == this.cancelCommand)
        {
            Jimm.jimm.getContactListRef().activate();
            Jimm.jimm.getSplashCanvasRef().removeCommand(this.cancelCommand);
        }

    }

    /** ************************************************************************* */
    /** ************************************************************************* */
    /** ************************************************************************* */

    // Class for viewfinder
    public class ViewFinder extends Canvas implements CommandListener
    {

        // Resolution matrix
        private final String res[][] =
        {
        { "80", "160", "320", "640"},
        { "60", "120", "240", "480"}};

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

            backCommand = new Command(ResourceBundle.getString("back"), Command.BACK, 2);
            okCommand = new Command(ResourceBundle.getString("ok"), Command.SCREEN, 1);
            // resCommand = new Command(ResourceBundle.getString("res"), Command.SCREEN, 2);
            selectCommand = new Command(ResourceBundle.getString("back"), Command.OK, 1);

            this.addCommand(backCommand);
            this.addCommand(okCommand);
            // this.addCommand(resCommand);
            this.setCommandListener(this);

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
                } else
                {
                    JimmException.handleException(new JimmException(180, 0, true));
                }
            } catch (IOException ioe)
            {
                JimmException.handleException(new JimmException(181, 0, true));
                reset();
            } catch (MediaException me)
            {
                JimmException.handleException(new JimmException(181, 1, true));
                reset();
            } catch (SecurityException se)
            {
                JimmException.handleException(new JimmException(181, 2, true));
                reset();
            }

        }

        private void reset()
        {
            if (p != null)
            {
                p.close();
                p = null;
            }
            vc = null;
        }

        // paint method, inherid form Canvas
        protected void paint(Graphics g)
        {
            g.setColor(0xffffffff);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            if (!viewfinder && (img != null))
            {
                g.drawImage(img, getWidth() / 2, getHeight() / 2, Graphics.VCENTER | Graphics.HCENTER);
                // System.out.println("drawedImage");
            }

            g.setColor(0x00000000);
            if (viewfinder)
                g.drawString(ResourceBundle.getString("viewfinder") + " " + this.res[0][this.res_marker] + "x"
                        + this.res[1][this.res_marker], 1, 1, Graphics.TOP | Graphics.LEFT);
            else
                g.drawString(ResourceBundle.getString("send_img") + "? " + this.res[0][this.res_marker] + "x"
                        + this.res[1][this.res_marker], 1, 1, Graphics.TOP | Graphics.LEFT);
        }

        // start the viewfinder
        public synchronized void start()
        {
            if ((p != null) && !active)
            {
                try
                {
                    p.start();
                    vc.setVisible(true);
                    active = true;
                } catch (MediaException me)
                {
                    JimmException.handleException(new JimmException(182, 0, true));
                    reset();
                } catch (SecurityException se)
                {
                    JimmException.handleException(new JimmException(183, 0, true));
                    reset();
                }
            }
        }

        // take a snapshot form the viewfinder
        public void takeSnapshot()
        {
            if (p != null)
            {
                try
                {
                    //data = vc.getSnapshot("encoding=jpeg&width=" + this.res[0][this.res_marker] + "&height=" + this.res[1][this.res_marker]);
                    //data_pre = vc.getSnapshot(null);
                    data = vc.getSnapshot(null);

                    this.stop();
                    img = Image.createImage(data, 0, data.length);
                    viewfinder = false;
                    repaint();

                } catch (MediaException me)
                {
                    JimmException.handleException(new JimmException(183, 0, true));
                }
            }
        }

        // stop the viewfinder
        public synchronized void stop()
        {
            if ((p != null) && active)
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
                    FileTransfer.this.setData(data);
                    FileTransfer.this.askForNameDesc("jimm_cam" + Util.getCounter() + ".jpeg", "");
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
                    Jimm.jimm.getContactListRef().activate();
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
            if (getGameAction(keyCode) == FIRE) if (!viewfinder)
            {
                this.stop();
                this.reset();
                FileTransfer.this.setData(data);
                FileTransfer.this.askForNameDesc("jimm_cam" + Util.getCounter() + ".jpeg", "");
            } else
            {
                this.takeSnapshot();
            }

        }

    }

    /** ************************************************************************* */
    /** ************************************************************************* */
    /** ************************************************************************* */
    
    // Class for file selection
    public class FileSelector implements CommandListener
    {

        private String currDirName;

        private Command select = new Command(ResourceBundle.getString("select"), Command.ITEM, 1);
        private Command back = new Command(ResourceBundle.getString("back"), Command.BACK, 2);
        private Command exit = new Command(ResourceBundle.getString("cancel"), Command.EXIT, 3);

        private Image dirIcon, fileIcon;
        private Image[] iconList;

        /* special string denotes upper directory */
        private final String UP_DIRECTORY = "..";

        /*
         * special string that denotes apper directory accessible by this
         * browser. this virtual directory contains all roots.
         */
        private final String MEGA_ROOT = "/";

        /* separator string as defined by FC specification */
        private final String SEP_STR = "/";

        /* separator character as defined by FC specification */
        private final char SEP = '/';

        public FileSelector()
        {
            currDirName = MEGA_ROOT;
            try
            {
                dirIcon = Image.createImage("dir.png");
            } catch (IOException e)
            {
                dirIcon = null;
            }
            try
            {
                fileIcon = Image.createImage("file.png");
            } catch (IOException e)
            {
                fileIcon = null;
            }
            iconList = new Image[]
            { fileIcon, dirIcon};

        }

        public void startFc() throws JimmException
        {
            try
            {
                showCurrDir();
            } catch (SecurityException e)
            {
                throw new JimmException(193,0,true);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        public void commandAction(Command c, Displayable d)
        {
            if (c == select)
            {
                List curr = (List) d;
                final String currFile = curr.getString(curr.getSelectedIndex());
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        if (currFile.endsWith(SEP_STR) || currFile.equals(UP_DIRECTORY))
                            traverseDirectory(currFile);
                        else
                            try {
                                startFT(currFile);
                            }
                        	catch (JimmException e)
                        	{
                        	    JimmException.handleException(e);
                        	}
                    }
                }).start();
            }
            else
                if (c == back)
                    showCurrDir();
                else
                    if (c == exit) 
                        FileTransfer.this.getCItem().activateMenu();
        }

        /**
         * Show file list in the current directory .
         */
        void showCurrDir()
        {
            Enumeration e;
            FileConnection currDir = null;
            List browser;
            try
            {
                if (MEGA_ROOT.equals(currDirName))
                {
                    e = FileSystemRegistry.listRoots();
                    browser = new List(currDirName, List.IMPLICIT);
                }
                else
                {
                    currDir = (FileConnection) Connector.open("file://localhost/" + currDirName);
                    e = currDir.list();
                    browser = new List(currDirName, List.IMPLICIT);
                    // not root - draw UP_DIRECTORY
                    browser.append(UP_DIRECTORY, dirIcon);
                }

                while (e.hasMoreElements())
                {
                    String fileName = (String) e.nextElement();
                    if (fileName.charAt(fileName.length() - 1) == SEP)
                        browser.append(fileName, dirIcon);
                    else
                        browser.append(fileName, fileIcon);
                }

                browser.setSelectCommand(select);
                browser.addCommand(exit);

                browser.setCommandListener(this);

                if (currDir != null)
                    currDir.close();

                Jimm.display.setCurrent(browser);
            } catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }

        void traverseDirectory(String fileName)
        {
            /*
             * In case of directory just change the current directory and show
             * it
             */
            if (currDirName.equals(MEGA_ROOT))
            {
                if (fileName.equals(UP_DIRECTORY))
                {
                    // can not go up from MEGA_ROOT
                    return;
                }
                currDirName = fileName;
            }
            else
                if (fileName.equals(UP_DIRECTORY))
                {
                    // Go up one directory
                    // TODO use setFileConnection when implemented
                    int i = currDirName.lastIndexOf(SEP, currDirName.length() - 2);
                    if (i != -1)
                    {
                        currDirName = currDirName.substring(0, i + 1);
                    }
                    else
                    {
                        currDirName = MEGA_ROOT;
                    }
                }
                else
                {
                    currDirName = currDirName + fileName;
                }
            showCurrDir();
        }

        void startFT(String fileName) throws JimmException
        {
            try
            {
                FileConnection fc = (FileConnection) Connector.open("file://localhost/" + currDirName + fileName);
                if (!fc.exists()) { throw new IOException(); }

                InputStream fis = fc.openInputStream();
                byte[] b = new byte[(int)fc.fileSize()];

                int length = fis.read(b, 0, (int)fc.fileSize());

                fis.close();
                fc.close();

                // Set the file data in file transfer
                FileTransfer.this.setData(b);
                // Create filename and ask for name and description
                FileTransfer.this.askForNameDesc(fileName, "");

            } catch (Exception e)
            {
                throw new JimmException(192,0,true);
            }
        }
    }
}
//#sijapp cond.end#
//#sijapp cond.end#