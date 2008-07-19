/*******************************************************************************
 Library of additional graphical screens for J2ME applications
 Copyright (C) 2003-08  Jimm Project

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
********************************************************************************
 File: src/DrawControls/TreeNode.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
*******************************************************************************/

package DrawControls;

import java.util.Vector;

import DrawControls.VirtualTreeCommands;

//! Tree node
/*! This class is used to handle tree nodes (adding, deleting, moveing...) */
public class TreeNode
{
	private Object data = null;

	protected Vector items = null;

	protected boolean expanded = false;

	protected int level = 0;

	//! Store object associated with the node
	protected TreeNode(Object data)
	{
		this.data = data;
	}

	//! Remove all elements from node
	protected void clear()
	{
		if (items != null)
			items.removeAllElements();
	}

	//! Returns object associated with the node
	public Object getData()
	{
		return data;
	}

	//! Returns node level 
	public int getLevel()
	{
		return level;
	}

	//! Returns true if node if expanded
	public boolean getExpanded()
	{
		return expanded;
	}

	// Returns number of subnodes
	public int size()
	{
		if (items == null)
			return 0;
		return items.size();
	}

	//! Returns subnode by index
	public TreeNode elementAt(int index)
	{
		return (TreeNode) items.elementAt(index);
	}

	/////

	protected TreeNode addItem(TreeNode newItem)
	{
		if (items == null)
			items = new Vector();
		items.addElement(newItem);
		return newItem;
	}

	protected void insertChild(TreeNode element, int index)
	{
		if (items == null)
			items = new Vector();
		items.insertElementAt(element, index);
	}

	protected void removeItem(int index)
	{
		items.removeElementAt(index);
	}

	protected int findItem(TreeNode item)
	{
		if (items == null)
			return -1;
		int size = this.size();
		for (int i = 0; i < size; i++)
			if (elementAt(i) == item)
				return i;
		return -1;
	}

	private static int getInsertionPos(Vector vect, TreeNode node,
			VirtualTreeCommands comparer)
	{
		int size = vect.size();
		int begin, end, middle, begRes, midRes, endRes;

		if (size == 0)
			return 0;

		begin = 0;
		end = size - 1;
		begRes = comparer.vtCompareNodes(node, (TreeNode) vect.elementAt(begin));
		endRes = comparer.vtCompareNodes(node, (TreeNode) vect.elementAt(end));

		if (begRes < 0)
			return 0;
		if (endRes > 0)
			return size;

		for (;;)
		{
			middle = (begin + end) / 2;
			midRes = comparer.vtCompareNodes(node, (TreeNode) vect
					.elementAt(middle));

			if (((midRes <= 0) && (endRes >= 0))
					|| ((midRes >= 0) && (endRes <= 0)))
			{
				begin = middle;
				begRes = midRes;
			} else
			{
				end = middle;
				endRes = midRes;
			}

			if ((end - begin) <= 1)
			{
				if (begRes < 0)
					return begin;
				else
					return end;
			}
		}
	}

	protected void sort(VirtualTreeCommands comparer)
	{
		int i, count;
		TreeNode currNode;
		Vector newItems = new Vector();

		if (items == null)
			return;
		count = items.size();
		for (i = 0; i < count; i++)
		{
			currNode = (TreeNode) items.elementAt(i);
			newItems.insertElementAt(currNode, getInsertionPos(newItems,
					currNode, comparer));
		}
		items = newItems;
	}

}
