/*
   Custom Controls for J2ME
   (c) 2004 Artyomov Denis (artyomov@inbox.ru)
   
   No license needs to use this software. You can use it as you want :)
   Any improvements are very welcome! :)
*/


package DrawControls;

import DrawControls.TreeNode;

public interface TreeNodeComparer
{
	int compareNodes(TreeNode node1, TreeNode node2);
}

