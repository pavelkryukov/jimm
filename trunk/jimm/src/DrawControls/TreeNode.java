/*
   Custom Controls for J2ME
   (c) 2004 Artyomov Denis (artyomov@inbox.ru)
   
   No license needs to use this software. You can use it as you want :)
   Any improvements are very welcome! :)
*/


package DrawControls;

import java.util.Vector;

import DrawControls.TreeNodeComparer;


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
  
  //! Romove all elements from node
  protected void clear()
  {
    if (items != null) items.removeAllElements();
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
    if (items == null) return 0;
    return items.size();
  }
  
  //! Returns subnode by index
  public TreeNode elementAt(int index)
  {
    return (TreeNode)items.elementAt(index);
  }
  
  /////
  
  protected TreeNode addItem(TreeNode newItem)
  {
    if (items == null) items = new Vector();
    items.addElement(newItem);
    return newItem;
  }
  
	protected void insertChild(TreeNode element, int index)
	{
	    if (items == null) items = new Vector();
	    items.insertElementAt(element, index);
	}
  
  
  protected void removeItem(int index)
  {
    items.removeElementAt(index);
  }
  
  protected int findItem(TreeNode item)
  {
    if (items == null) return -1;
    int size = this.size();
    for (int i = 0; i < size; i++) if (elementAt(i) == item) return i;
    return -1;
  }
  
	private static int getInsertionPos(Vector vect, TreeNode node, TreeNodeComparer comparer)
	{
	    int size = vect.size();
	    int i, begin, end, middle, begRes, midRes, endRes;
	    
	    if (size == 0) return 0;
	    
	    begin = 0;
	    end = size-1;
        begRes = comparer.compareNodes(node, (TreeNode)vect.elementAt(begin));
        endRes = comparer.compareNodes(node, (TreeNode)vect.elementAt(end)); 
        
        if (begRes < 0) return 0;
        if (endRes > 0) return size;
	    
	    for (;;)
	    {
	        middle = (begin+end)/2;
	        midRes = comparer.compareNodes(node, (TreeNode)vect.elementAt(middle));
	        
	        if (((midRes <= 0) && (endRes >= 0)) || ((midRes >= 0) && (endRes <= 0)))
	        {
	            begin = middle;
	            begRes = midRes; 
	        }
	        else
	        {
	            end = middle;
	            endRes = midRes;
	        }
	        
	        if ((end-begin) <= 1)
	        {
	            if (begRes < 0) return begin;
	            else return end; 
	        }
	    }
	}
 
	protected void sort(TreeNodeComparer comparer)
	{
		int i, count;
		TreeNode currNode;
		Vector newItems = new Vector(); 
		
		if (items == null) return;
		count = items.size();
		for (i = 0; i < count; i++)
		{
		    currNode = (TreeNode)items.elementAt(i);
		    newItems.insertElementAt(currNode, getInsertionPos(newItems, currNode, comparer));
		}
		items = newItems;
	}

}

