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
 File: src/DrawControls/VirtualTree.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package DrawControls;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Graphics;

import DrawControls.VirtualTreeCommands;
import DrawControls.VirtualList;
import DrawControls.TreeNode;

//! Tree implementation, which allows programmers to store node data themself
/*!
 VirtualTree is successor of VirtualList. It store tree structure in.
 It shows itself on display and handles user key commands.
 You must inherit new class from VirtualDrawTree and reload next functions:
 \par
 VirtualTree#getItemDrawData Tree control call this function for request
 of data for tree node to be drawn
 */
public class VirtualTree extends VirtualList
{
	final protected TreeNode root = new TreeNode(null);
	private Vector drawItems;
	private boolean isChanged = false;
	private int stepSize = 6;
	private boolean showButtons = true;
	private boolean autoExpand = true;
	private int currFontHeight;
	
	private VirtualTreeCommands commands;

	{
		root.expanded = true;
		currFontHeight = getFontHeight();
	}

	//! Constructor
	public VirtualTree(String capt, boolean autoExpand)
	{
		super(capt);
		this.autoExpand = autoExpand;
	}

	//! Constructor
	public VirtualTree(String capt, //!< Caption shown at the top of control
		int capTextColor, //!< Caption text color
		int backColor, //!< Control back color
		int fontSize, /*!< Control font size. This font size if used both 
					 for caption and text in tree nodes */
		boolean autoExpand)
	{
		super(capt, capTextColor, backColor, fontSize, VirtualList.CURSOR_MODE_ENABLED);
		this.autoExpand = autoExpand;
	}

	public void setVTCommands(VirtualTreeCommands commands)
	{
		this.commands = commands;
	}

	//! For internal use only
	/*! If someone change node structure wasChanged mast be called! */
	protected void wasChanged()
	{
		isChanged = true;
	}

	// private TreeNode getDrawItem(int index)
	private TreeNode getDrawItem(int index)
	{
		return (TreeNode) drawItems.elementAt(index);
	}

	// private void checkToRebuildTree()
	private void checkToRebuildTree()
	{
		if (isChanged || (drawItems == null))
		{
			rebuildTreeIntItems();
		}
	}

	//! Sets size of space for next level node
	public void setStepSize(int value)
	{
		stepSize = value;
		invalidate();
	}

	public void setShowButtons(boolean value)
	{
		if (value == showButtons) return;
		showButtons = value;
		invalidate();
	}

	public boolean getShowButtons()
	{
		return showButtons;
	}

	//! Returns current selected node
	public TreeNode getCurrentItem()
	{
		if ((getCurrIndex() < 0) || (getCurrIndex() >= drawItems.size())) return null;
		return getDrawItem(getCurrIndex());
	}

	//! Set node as current. Make autoscroll if needs.
	public void setCurrentItem(TreeNode node)
	{
		int count, i;

		if (getLocked())
		{
			lastNode = node;
			return;
		}

		checkToRebuildTree();

		if (getCurrentItem() == node) return;

		// finding at visible nodes
		count = drawItems.size();
		for (i = 0; i < count; i++)
		{
			if (getDrawItem(i) != node) continue;
			setCurrentItem(i);
			return;
		}

		// finding at all nodes
		Vector path = new Vector();
		buildNodePath(path, getRoot(), node);

		count = path.size();
		if (count != 0)
		{
			// make item visible
			//System.out.println("Path to tree node");
			for (i = 0; i < count; i++)
			{
				((TreeNode) path.elementAt(i)).expanded = true;
			}
			rebuildTreeIntItems();
			setCurrentItem(node);
			wasChanged();
			invalidate();
		}
	}

	// Build path to node int tree
	private boolean buildNodePath(Vector path, TreeNode root, TreeNode node)
	{
		int count = root.size();
		for (int i = 0; i < count; i++)
		{
			TreeNode childNode = root.elementAt(i);
			if (childNode == node) return true;
			if (buildNodePath(path, childNode, node))
			{
				path.addElement(childNode);
				return true;
			}
		}
		return false;
	}

	//! Returns root node (root node is parent for all nodes and never visible).
	public TreeNode getRoot()
	{
		return root;
	}

	//! Internal function
	/*! Changes node state*/
	protected boolean itemSelected()
	{
		TreeNode currItem = getCurrentItem();
		if (currItem == null) return false;

		if (autoExpand)
		{
			if (currItem.size() != 0)
			{
				currItem.expanded = !currItem.expanded;
				rebuildTreeIntItems();
				invalidate();
			}
			return false;
		}
		
		executeCommand(findMenuByType(Command.OK));
		return true; 
	}

	//#sijapp cond.if target is "MIDP2"#
	protected boolean pointerPressedOnUtem(int index, int x, int y, int mode)
	{
		TreeNode currItem = getCurrentItem();
		if (currItem == null) return false;
		if ((currItem.size() > 0) && (x < (3 * getFontHeight() / 2 + currItem.level * stepSize)))
		{
			itemSelected();
			return true;
		}
		return false;
	}

	//#sijapp cond.end#

	//! For internal use only
	protected int getSize()
	{
		checkToRebuildTree();
		return drawItems.size();
	}

	// private void rebuildTreeIntItems()
	private void rebuildTreeIntItems()
	{
		isChanged = false;
		if (drawItems == null) drawItems = new Vector();
		drawItems.removeAllElements();
		int count = root.size();
		for (int i = 0; i < count; i++)
			fillTreeIntItems(root.elementAt(i), 0);
		checkCurrItem();
	}

	// private void fillTreeIntItems(TextDrawTreeItem top, int level)
	private void fillTreeIntItems(TreeNode top, int level)
	{
		drawItems.addElement(top);
		top.level = level;
		if (top.getExpanded() == true)
		{
			int count = top.size();
			for (int i = 0; i < count; i++) fillTreeIntItems(top.elementAt(i), level + 1);
		}
	}

	// protected void get(int index, ListItem item)
	protected void get(int index, ListItem item)
	{
		checkToRebuildTree();
		TreeNode treeItem = getDrawItem(index);
		commands.vtGetItemDrawData(treeItem, item);
		item.horizOffset = treeItem.level * stepSize;
		if ((showButtons) && (treeItem.size() != 0)) item.horizOffset += 3*currFontHeight/4;
	}

	// private static int drawNodeRect(Graphics g, TreeNode item, int x, int y1, int y2) - 
	// draw + or - before node text
	private static int drawNodeRect(Graphics g, TreeNode item, int x, int y1, int y2, int fontHeight)
	{
		int height = 2 * fontHeight / 3;
		if (height < 7) height = 7;
		if (height % 2 == 0) height--;
		if (item.size() != 0)
		{
			int y = (y1 + y2 - height) / 2;
			int oldColor = g.getColor();

			g.setColor(0x808080);
			g.drawRect(x, y, height - 1, height - 1);
			int mx = x + height / 2;
			int my = y + height / 2;
			g.drawLine(x + 2, my, x + height - 3, my);
			if (!item.getExpanded()) g.drawLine(mx, y + 2, mx, y + height - 3);
			g.setColor(oldColor);
		}
		return height + 1;
	}

	//! For internal use only
	/*! Draw a tree node. Called by base class DrawControls#VirtualDrawList */
	protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int paintMode)
	{
		checkToRebuildTree();
		
		super.drawItemData(g, index, x1, y1, x2, y2, paintMode);
		
		if (showButtons)
		{
			TreeNode treeItem = getDrawItem(index);
			int x = x1 + treeItem.level * stepSize;
			drawNodeRect(g, treeItem, x, y1, y2, y2-y1);
		}
	}

	//! Add new node
	/*! Method "addNode" insert new item at node root. Function return 
	 reference to new node. */
	public TreeNode addNode(TreeNode node, //!< root for item to be inserted
		Object obj //!< object be stored at new tree node
	)
	{
		if (node == null) node = this.root;
		TreeNode result = new TreeNode(obj);
		node.addItem(result);
		wasChanged();
		invalidate();
		return result;
	}

	// private TreeNode findParent(TreeNodeInternal root, TreeNode node)
	private TreeNode findParent(TreeNode root, TreeNode node)
	{
		if (root.findItem(node) != -1) return root;
		int count = root.size();
		for (int i = 0; i < count; i++)
		{
			TreeNode result = findParent(root.elementAt(i), node);
			if (result != null) return result;
		}
		return null;
	}

	//! Removes node from tree. Returns true if removeing is successful.
	public boolean removeNode(TreeNode node //!< Node to be removed
	)
	{
		storeLastNode();
		TreeNode parent = findParent(root, node);
		if (parent == null) return false;
		int index = parent.findItem(node);
		if (index == -1) return false;
		parent.removeItem(index);
		checkCurrItem();
		wasChanged();
		invalidate();
		restoreLastNode();
		return true;
	}

	//! Move one tree node to another. Returns true if moving is successful.
	public boolean moveNode(TreeNode node, //!< Node to move
		TreeNode dst //!< destination for node
	)
	{
		if (node == dst) return false;
		if (!removeNode(node)) return false;
		if (dst == null) dst = root;
		dst.addItem(node);
		checkCurrItem();
		wasChanged();
		invalidate();
		return true;
	}

	protected int compareNodes(TreeNode node1, TreeNode node2)
	{
		return 0;
	}

	public void sortNode(TreeNode node)
	{
		storeLastNode();
		if (node == null) node = getRoot();
		node.sort(commands);
		if (node.getExpanded())
		{
			wasChanged();
			invalidate();
		}
		restoreLastNode();
	}

	public void insertChild(TreeNode root, TreeNode element, int index)
	{
		if (root == null) root = getRoot();
		storeLastNode();
		root.insertChild(element, index);
		if (root.getExpanded())
		{
			wasChanged();
			invalidate();
		}
		restoreLastNode();
	}

	public void deleteChild(TreeNode root, int index)
	{
		if (root == null) root = getRoot();
		storeLastNode();
		root.items.removeElementAt(index);
		if (root.getExpanded())
		{
			wasChanged();
			invalidate();
		}
		restoreLastNode();
	}

	public int getIndexOfChild(TreeNode root, TreeNode element)
	{
		if (root.items == null) return -1;
		return root.items.indexOf(element);
	}

	//! Expand or collapse tree node. NOTE: this is not recursive operation!
	public void setExpandFlag(TreeNode node, //!< Tree node
		boolean value /*!< expand/collapse flag. True - node is expanded, 
	 false - node is collapsed */
	)
	{
		if (node.expanded == value) return;
		node.expanded = value;
		wasChanged();
		//checkCurrItem();
		invalidate();
	}

	//! Remove all nodes from tree
	public void clear()
	{
		root.clear();
		rebuildTreeIntItems();
		checkCurrItem();
		checkTopItem();
		invalidate();
	}

	TreeNode lastNode = null;

	private void storeLastNode()
	{
		lastNode = getCurrentItem();
	}

	protected void afterUnlock()
	{
		restoreLastNode();
	}

	private void restoreLastNode()
	{
		if (getLocked()) return;
		setCurrentItem(lastNode);
		lastNode = null;
	}
}
