//#sijapp cond.if (target="MOTOROLA"|target="MIDP2"|target="SIEMENS2")&(modules_FILES="true"|modules_HISTORY="true")#
package jimm;

//#sijapp cond.if target="MIDP2"|target="MOTOROLA"#
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.io.file.FileConnection;
//#sijapp cond.elseif target="SIEMENS2"#
import com.siemens.mp.io.file.FileConnection;
import com.siemens.mp.io.file.FileSystemRegistry;
//#sijapp cond.end#
import javax.microedition.io.Connector;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Command;
import jimm.util.ResourceBundle;
import java.util.Enumeration;
import java.util.Vector;
import java.io.*;
import DrawControls.*;

abstract class FileSystem
{
	public InputStream fileInputStream;

	public OutputStream fileOutputStream;

	public static FileSystem getInstance()
	{
		//#sijapp cond.if target="MOTOROLA"#
		if (!Jimm.supports_JSR75) return new MotorolaFileSystem();
		else
		//#sijapp cond.end#
		return new JSR75FileSystem();
	}

	public static String[] getDirectoryContents(String dir, boolean only_dirs)
			throws JimmException
	{
		//#sijapp cond.if target="MOTOROLA"#
		if (!Jimm.supports_JSR75) return MotorolaFileSystem
				.getDirectoryContents(dir, only_dirs);
		else
		//#sijapp cond.end#
		return JSR75FileSystem.getDirectoryContents(dir, only_dirs);
	}

	public static long totalSize(String root) throws Exception
	{
		//#sijapp cond.if target="MOTOROLA"#
		if (!Jimm.supports_JSR75) return MotorolaFileSystem.totalSize(root);
		else
		//#sijapp cond.end#
		return JSR75FileSystem.totalSize(root);
	}

	public abstract void openFile(String file) throws Exception;

	public abstract OutputStream openOutputStream() throws Exception;

	public abstract InputStream openInputStream() throws Exception;

	public abstract void close();

	public abstract long fileSize() throws Exception;
}

//#sijapp cond.if target="MOTOROLA"#
class MotorolaFileSystem extends FileSystem
{
	private com.motorola.io.FileConnection fileConnection;

	public static String[] getDirectoryContents(
		String currDir,
		boolean only_dirs) throws JimmException
	{
		String[] items = null;
		try
		{
			if (currDir.equals(FileBrowser.ROOT_DIRECTORY))
			{
				String[] roots = com.motorola.io.FileSystemRegistry.listRoots();
				items = new String[roots.length];
				for (int i = 0; i < roots.length; i++)
					items[i] = roots[i].substring(1);
			}
			else
			{
				com.motorola.io.FileConnection fileconn = (com.motorola.io.FileConnection) Connector
						.open("file://" + currDir);
				String[] list = fileconn.list();
				fileconn.close();
				Vector list_vect = new Vector(list.length + 1);
				list_vect.addElement(FileBrowser.PARENT_DIRECTORY);
				for (int i = 0; i < list.length; i++)
				{
					if (only_dirs & !list[i].endsWith("/")) continue;
					list_vect.addElement(list[i].substring(currDir.length()));
				}
				items = new String[list_vect.size()];
				list_vect.copyInto(items);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new JimmException(191, 0, true);
		}
		return items;
	}

	public static long totalSize(String name) throws Exception
	{
		long total_size = 0;
		com.motorola.io.FileConnection fileconn = (com.motorola.io.FileConnection) Connector
				.open("file:///" + name);
		total_size = fileconn.totalSize();
		fileconn.close();
		return total_size;
	}

	public void openFile(String file) throws Exception
	{
		fileConnection = (com.motorola.io.FileConnection) Connector
				.open("file://" + file);
	}

	public OutputStream openOutputStream() throws Exception
	{
		if (!fileConnection.exists())
		{
			fileConnection.create();
		}
		else if (fileConnection.exists() & (fileOutputStream == null))
		{
			fileConnection.delete();
			fileConnection.create();
		}
		return (fileOutputStream != null) ? fileOutputStream : fileConnection
				.openOutputStream();
	}

	public InputStream openInputStream() throws Exception
	{
		return (fileInputStream != null) ? fileInputStream : fileConnection
				.openInputStream();
	}

	public void close()
	{
		try
		{
			if (fileInputStream != null) fileInputStream.close();
			if (fileOutputStream != null) fileOutputStream.close();
			if (fileConnection != null) fileConnection.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public long fileSize() throws Exception
	{
		if (fileConnection != null) return fileConnection.fileSize();
		else return -1;
	}
}

//#sijapp cond.end#
class JSR75FileSystem extends FileSystem
{
	private FileConnection fileConnection;

	public static String[] getDirectoryContents(
		String currDir,
		boolean only_dirs) throws JimmException
	{
		System.out.println("getDirectoryContents.currDir=" + currDir);

		String[] items = null;
		try
		{
			if (currDir.equals(FileBrowser.ROOT_DIRECTORY))
			{
				System.out.println("currDir.equals(FileBrowser.ROOT_DIRECTORY)");
				Vector roots_vect = new Vector();
				Enumeration roots = FileSystemRegistry.listRoots();
				while (roots.hasMoreElements())
					roots_vect.addElement(((String) roots.nextElement()));
				items = new String[roots_vect.size()];
				roots_vect.copyInto(items);
			}
			else
			{
				FileConnection fileconn;
				//#sijapp cond.if target="SIEMENS2"#
				fileconn = (FileConnection) Connector.open("file://" + currDir);
				//#sijapp cond.else#
				fileconn = (FileConnection) Connector.open("file://localhost"
						+ currDir);
				//#sijapp cond.end#

				Enumeration list = fileconn.list();
				fileconn.close();
				Vector list_vect = new Vector();
				list_vect.addElement(FileBrowser.PARENT_DIRECTORY);
				while (list.hasMoreElements())
				{
					String filename = (String) list.nextElement();
					if (only_dirs & !filename.endsWith("/")) continue;
					list_vect.addElement(filename);
				}
				items = new String[list_vect.size()];
				list_vect.copyInto(items);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new JimmException(191, 0, true);
		}
		return items;
	}

	public static long totalSize(String name) throws Exception
	{
		long total_size = 0;
		FileConnection fileconn;
		//#sijapp cond.if target="SIEMENS2"#
		fileconn = (FileConnection) Connector.open("file:///" + name);
		//#sijapp cond.else#
		fileconn = (FileConnection) Connector.open("file://localhost/" + name);
		//#sijapp cond.end#

		total_size = fileconn.totalSize();
		fileconn.close();
		return total_size;
	}

	public void openFile(String file) throws Exception
	{
		fileConnection = (FileConnection) Connector.open("file://" + file);
	}

	public OutputStream openOutputStream() throws Exception
	{
		if (!fileConnection.exists())
		{
			fileConnection.create();
		}
		else if (fileConnection.exists() & (fileOutputStream == null))
		{
			fileConnection.delete();
			fileConnection.create();
		}
		return (fileOutputStream != null) ? fileOutputStream : fileConnection
				.openOutputStream();
	}

	public InputStream openInputStream() throws Exception
	{
		return (fileInputStream != null) ? fileInputStream : fileConnection
				.openInputStream();
	}

	public void close()
	{
		try
		{
			if (fileInputStream != null) fileInputStream.close();
			if (fileOutputStream != null) fileOutputStream.close();
			if (fileConnection != null) fileConnection.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public long fileSize() throws Exception
	{
		if (fileConnection != null) return fileConnection.fileSize();
		else return -1;
	}
}

interface FileBrowserListener
{
	public void onFileSelect(String file);

	public void onDirectorySelect(String directory);
}

public class FileBrowser implements CommandListener, VirtualTreeCommands,
		VirtualListCommands
{
	public static final String ROOT_DIRECTORY = "/";

	public static final String PARENT_DIRECTORY = "../";

	public static final Command backCommand = new Command(ResourceBundle
			.getString("back"), Command.SCREEN, 0);

	public static final Command selectCommand = new Command(ResourceBundle
			.getString("select"), Command.OK, 1);

	public static final Command openCommand = new Command(ResourceBundle
			.getString("open"), Command.OK, 1);

	private static boolean needToSelectDirectory, openCommandSelected;

	private static FileBrowser _this;

	private static VirtualTree tree;

	private static FileBrowserListener listener;

	private static ImageList imageList;

	private static String[] items;

	private static String currDir;

	public FileBrowser()
	{
		_this = this;
		imageList = new ImageList();
		try
		{
			imageList.load("/fs.png", -1, -1, -1);
		}
		catch (java.io.IOException e)
		{
		}
		tree = new VirtualTree(null, false);
		tree.setVTCommands(this);
		tree.setVLCommands(this);
		tree.setFullScreenMode(false);
		tree.setImageList(imageList);
		tree
				.setFontSize((imageList.getHeight() < 16) ? VirtualList.SMALL_FONT : VirtualList.MEDIUM_FONT);
		tree.setStepSize(-tree.getFontHeight() / 2);
		tree.setShowButtons(false);
		tree.addCommand(backCommand);
		tree.setCommandListener(this);
	}

	private static void reset()
	{
		tree.lock();
		items = new String[0];
		tree.clear();
		tree.unlock();
	}

	private static int getNodeWeight(String filename)
	{
		if (filename.equals(PARENT_DIRECTORY)) return 0;
		if (filename.endsWith("/")) return 10;
		return 20;
	}

	public int compareNodes(TreeNode node1, TreeNode node2)
	{
		int result = 0;
		String name1 = (String) node1.getData();
		String name2 = (String) node2.getData();
		int weight1 = getNodeWeight(name1);
		int weight2 = getNodeWeight(name2);
		if (weight1 == weight2) result = name1.toLowerCase().compareTo(name2
				.toLowerCase());
		else result = (weight1 < weight2) ? -1 : 1;
		return result;
	}

	private static void rebuildTree()
	{
		tree.lock();
		tree.clear();
		for (int i = 0; i < items.length; i++)
			tree.addNode(null, items[i]);
		tree.sortNode(null);
		tree.unlock();
		updateTreeCaptionAndCommands((String) tree.getCurrentItem().getData());
	}

	public static void setParameters(boolean select_dir)
	{
		needToSelectDirectory = select_dir;
	}

	public static void setListener(FileBrowserListener _listener)
	{
		listener = _listener;
	}

	public void VTnodeClicked(TreeNode node)
	{
		String file = (String) node.getData();
		
		if (file.equals(PARENT_DIRECTORY))
		{
			int d = currDir.lastIndexOf('/', currDir.length() - 2);
			currDir = (d != -1) ? currDir.substring(0, d + 1) : ROOT_DIRECTORY;
			reset();
			try
			{
				items = FileSystem.getDirectoryContents(currDir, needToSelectDirectory);
			}
			catch (JimmException e)
			{
				JimmException.handleException(e);
			}
			rebuildTree();
		}
		else if (file.endsWith("/"))
		{
			currDir += file;
			reset();
			try
			{
				items = FileSystem
						.getDirectoryContents(currDir, needToSelectDirectory);
			}
			catch (JimmException e)
			{
				JimmException.handleException(e);
			}
			if (needToSelectDirectory & !openCommandSelected
					& (items.length <= 1)) listener.onDirectorySelect(currDir);
			else rebuildTree();
			openCommandSelected = false;
		}
		else
		{
			listener.onFileSelect(currDir + file);
		}
	}

	private static void updateTreeCaptionAndCommands(String name)
	{
		tree.removeCommand(openCommand);
		tree.removeCommand(selectCommand);
		if (name.equals(PARENT_DIRECTORY))
		{
			int d = currDir.lastIndexOf('/', currDir.length() - 2);
			tree.addCommand(openCommand);
			tree
					.setTitle((d != -1) ? currDir.substring(0, d + 1) : ROOT_DIRECTORY);
		}
		else if (name.endsWith("/") & currDir.equals(ROOT_DIRECTORY))
		{
			try
			{
				tree.setTitle(ResourceBundle.getString("total_mem") + ": "
						+ (FileSystem.totalSize(name) >> 10)
						+ ResourceBundle.getString("kb"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				tree.setTitle("???");
			}
			if (needToSelectDirectory) tree.addCommand(selectCommand);
			tree.addCommand(openCommand);
		}
		else if (name.endsWith("/") & !currDir.equals(ROOT_DIRECTORY))
		{
			if (needToSelectDirectory) tree.addCommand(selectCommand);
			tree.addCommand(openCommand);
			tree.setTitle(currDir + name);
		}
		else
		{
			tree.addCommand(selectCommand);
			try
			{
				int file_size = 0;
				FileSystem file = FileSystem.getInstance();
				file.openFile(currDir + name);
				file_size = (int) (file.fileSize() >> 10);
				file.close();
				int ext = name.lastIndexOf('.');
				StringBuffer str_buf = new StringBuffer();
				if (ext != -1) str_buf = str_buf.append(name.substring(ext + 1)
						.toUpperCase()).append(", ");
				str_buf = str_buf.append(file_size).append(ResourceBundle
						.getString("kb"));
				tree.setTitle(str_buf.toString());
			}
			catch (Exception e)
			{
				e.printStackTrace();
				tree.setTitle("???");
			}
		}
	}

	public void onCursorMove(VirtualList sender)
	{
		if (sender == tree) updateTreeCaptionAndCommands((String) tree
				.getCurrentItem().getData());
	}

	public void VTGetItemDrawData(TreeNode src, ListItem dst)
	{
		String file = (String) src.getData();
		dst.text = file;
		dst.imageIndex = file.endsWith("/") ? 0 : 1;
		dst.color = 0x000000;
		dst.fontStyle = javax.microedition.lcdui.Font.STYLE_PLAIN;
	}

	public void onItemSelected(VirtualList sender)
	{
	}

	public void onKeyPress(VirtualList sender, int keyCode, int type)
	{
	}

	public static void activate() throws JimmException
	{
		if (_this == null) new FileBrowser();
		reset();
		currDir = ROOT_DIRECTORY;
		items = FileSystem.getDirectoryContents(currDir, needToSelectDirectory);
		rebuildTree();
		Jimm.display.setCurrent(tree);
	}

	public void commandAction(Command c, javax.microedition.lcdui.Displayable d)
	{
		if (d == tree)
		{
			if (c == openCommand)
			{
				openCommandSelected = true & needToSelectDirectory;
				VTnodeClicked(tree.getCurrentItem());
			}
			else if (c == selectCommand)
			{
				String filename = (String) tree.getCurrentItem().getData();
				if (filename.endsWith("/")) listener.onDirectorySelect(currDir
						+ filename);
				else listener.onFileSelect(currDir + filename);
			}
			else if (c == backCommand)
			{
				ContactList.activate();
			}
		}
	}
}
//#sijapp cond.end#