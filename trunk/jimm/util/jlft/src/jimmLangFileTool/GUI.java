/*******************************************************************************
 JimmLangFileTool - Simple Java GUI for editing/comparing Jimm language files
 Copyright (C) 2005  Jimm Project

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
 File: src/jimmLangFileTool/GUI.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimmLangFileTool;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.JLabel;
import java.awt.GridLayout;

public class GUI extends JFrame implements ActionListener
{
	private static final int BASE_SECTION = 0;
	private static final int BASE_KEY = 0;	
	private static final int BASE_VALUE = 1;	
	private static final int COMPARE_SECTION = 2;
	private static final int COMPARE_KEY = 2;
	private static final int COMPARE_VALUE = 3;
	
	// Variables
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JScrollPane compareScrollPane = null;
	private JTable compareTable = null;
	private JlftTableModel jlftTableModel = null;  //  @jve:decl-index=0:visual-constraint=""
	private TableCellRenderer renderer;
	
	private JimmLangFileTool jlft;
	private JToolBar compareToolBar = null;
	private JButton openBase = null;
	private JButton openCompare = null;
	private JButton saveCompare = null;
	private JButton remove = null;
	private JButton about = null;
	private JPanel legendPanel = null;
	private JLabel white = null;
	private JLabel yellow = null;
	private JLabel orange = null;
	private JLabel red = null;
	private JButton removeGroup = null;
	
	
	/**
	 * This is the default constructor
	 */
	public GUI(JimmLangFileTool _jlft)
	{
		jlft = _jlft;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	public void initialize()
	{
		renderer = new JlftTableRenderer();
		this.setContentPane(getJContentPane(false));
		this.setTitle("Jimm Lang File Tool");
		this.setSize(new java.awt.Dimension(850,500));
		this.setLocation(new java.awt.Point(20,20));
		fillInValues();
		this.pack();
		this.setVisible(true);
	}
	
	private int getSizeForTable()
	{
		int rowCount;
		
		int lRowCountBase = 0;
		int lRowCountCompare = 0;
		
		if(jlft.getBase() != null){
			lRowCountBase = jlft.getBase().getEntrysize();
		}
		if(jlft.getCompare() != null){
			lRowCountCompare = jlft.getCompare().getEntrysize();
		}
		
		rowCount = lRowCountBase > lRowCountCompare ? lRowCountBase : lRowCountCompare;
		
		return rowCount;
	}
	
	private void clearColum(int pColum){
		for(int i=0;i<compareTable.getRowCount();i++){
			compareTable.setValueAt(null, i, pColum);
			compareTable.setValueAt(null, i, pColum+1);
		}
	}
	
	private void fillInValues()
	{
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;

		// Delete all old stuff
		compareTable.removeAll();
		compareTable.setModel(this.getDefaultTableModel(true));
		this.setTableProps();

		// Set base values
		if (jlft.getBase() != null) {
			for (i = 0; i < jlft.getBase().size(); i++) {
				compareTable.setValueAt((jlft.getBase().get(i)), i + k + l, BASE_KEY);
				for (j = 0; j < (jlft.getBase().get(i)).size(); j++) {
					if (((jlft.getBase().get(i)).get(j)).getTranslated() != LGString.REMOVED) {
						compareTable.setValueAt((jlft.getBase().get(i)).get(j), i + k + j + 1 + l, BASE_KEY);
						compareTable.setValueAt((jlft.getBase().get(i)).get(j), i + k + j + 1 + l, BASE_VALUE);
					} else {
						l--;
					}
				}
				k += j;
			}
		}
		
		int usedExtraLinesInCompage = 0;
		// Set compare values
		if (jlft.getCompare() != null) {
			for (int lCompareCountLGFS =  0; lCompareCountLGFS < jlft.getCompare().size(); lCompareCountLGFS++){
				if ((jlft.getCompare().get(lCompareCountLGFS)).isNotInBase() && !(jlft.getCompare().get(lCompareCountLGFS)).isRemoved() ) {
					if(JimmLangFileTool.DEBUG){
						System.out.println("Found Group in Compare which was not in Base: "+jlft.getCompare().get(lCompareCountLGFS).getId());
					}
					compareTable.setValueAt(jlft.getCompare().get(lCompareCountLGFS), jlft.getBase().getEntrysize()+usedExtraLinesInCompage, COMPARE_KEY);
					usedExtraLinesInCompage++;
				}
				// First handle LGFileSubset entries 
				// Find the key in the base object and place compare at the same row
				for(int lBaseCount=0;lBaseCount<compareTable.getRowCount();lBaseCount++){
					if(compareTable.getValueAt(lBaseCount, BASE_KEY) instanceof LGFileSubset){
						if (((LGFileSubset)compareTable.getValueAt(lBaseCount, BASE_KEY)).getId().equals(jlft.getCompare().get(lCompareCountLGFS).getId())){
							compareTable.setValueAt((jlft.getCompare().get(lCompareCountLGFS)), lBaseCount, COMPARE_KEY);
							break;
						}
					}
				}
				
				// Now handle the LGString entries in the LGFileSubset entries
				// Find the key in the base object and place compare at the same row
				for(int lCompareCountLGS=0;lCompareCountLGS<jlft.getCompare().get(lCompareCountLGFS).size();lCompareCountLGS++){
					if ((jlft.getCompare().get(lCompareCountLGFS)).get(lCompareCountLGS).getTranslated() == LGString.NOT_IN_BASE_FILE) {
						if(JimmLangFileTool.DEBUG){
							System.out.println("Found String in Compare which was not in base. Group: "+jlft.getCompare().get(lCompareCountLGFS).getId()+" String: "+jlft.getCompare().get(lCompareCountLGFS).get(lCompareCountLGS).getKey());
						}
						compareTable.setValueAt(jlft.getCompare().get(lCompareCountLGFS).get(lCompareCountLGS), jlft.getBase().getEntrysize()+usedExtraLinesInCompage, COMPARE_KEY);
						compareTable.setValueAt(jlft.getCompare().get(lCompareCountLGFS).get(lCompareCountLGS), jlft.getBase().getEntrysize()+usedExtraLinesInCompage, COMPARE_VALUE);
						usedExtraLinesInCompage++;
						continue;
					}
					for(int lBaseCount=0;lBaseCount<compareTable.getRowCount();lBaseCount++){
						if(compareTable.getValueAt(lBaseCount, BASE_KEY) instanceof LGString){
							if(((LGString)compareTable.getValueAt(lBaseCount, BASE_KEY)).getKey().equals(jlft.getCompare().get(lCompareCountLGFS).get(lCompareCountLGS).getKey())){
								compareTable.setValueAt(jlft.getCompare().get(lCompareCountLGFS).get(lCompareCountLGS), lBaseCount, COMPARE_KEY);
								compareTable.setValueAt(jlft.getCompare().get(lCompareCountLGFS).get(lCompareCountLGS), lBaseCount, COMPARE_VALUE);
							}
						}
					}
					
				}
				
			}
		}
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane(boolean refresh)
	{
		if (jContentPane == null || refresh)
		{
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.gridx = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.setPreferredSize(new java.awt.Dimension(850,500));
			jContentPane.setMinimumSize(new java.awt.Dimension(810,66));
			jContentPane.add(getCompareToolBar(), gridBagConstraints);
			jContentPane.add(getCompareScrollPane(), gridBagConstraints1);
		}
		return jContentPane;
	}

	/**
	 * This method initializes compareScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCompareScrollPane()
	{
		if (compareScrollPane == null)
		{
			compareScrollPane = new JScrollPane();
			compareScrollPane.setName("compareScrollPane");
			compareScrollPane.setViewportView(getCompareTable());
		}
		return compareScrollPane;
	}

	/**
	 * This method initializes compareTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getCompareTable()
	{
		if (compareTable == null)
		{
			compareTable = new JTable();
			compareTable.setBackground(java.awt.Color.white);
			compareTable.setFont(new Font("DialogInput",Font.PLAIN, 12));
			compareTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
			compareTable.setModel(getDefaultTableModel(false));
			this.setTableProps();
		}
		return compareTable;
	}
	
	public void setTableProps()
	{
		TableColumn colum = compareTable.getColumnModel().getColumn(0);
		colum.setMinWidth(170);
		colum.setMaxWidth(170);
		colum.setPreferredWidth(170);
		colum = compareTable.getColumnModel().getColumn(1);
		colum.setMinWidth(220);
		colum.setPreferredWidth(220);
		colum = compareTable.getColumnModel().getColumn(2);
		colum.setMinWidth(170);
		colum.setMaxWidth(170);
		colum.setPreferredWidth(170);
		colum = compareTable.getColumnModel().getColumn(3);
		colum.setMinWidth(220);
		colum.setPreferredWidth(220);
		JTextField field = new JTextField();
		field.setFont(new Font("DialogInput",Font.PLAIN, 12));
		TableCellEditor editor = new DefaultCellEditor(field);
		try
		{
			compareTable.setDefaultEditor(Class.forName("java.lang.Object"),editor);
			compareTable.setDefaultRenderer(Class.forName("java.lang.Object"),renderer);
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method initializes defaultTableModel	
	 * 	
	 * @return javax.swing.table.DefaultTableModel	
	 */
	private DefaultTableModel getDefaultTableModel(boolean refresh)
	{
		if (jlftTableModel == null || refresh) {
			jlftTableModel = new JlftTableModel();
			Vector<String> columIdent = new Vector<String>();
			LGFile lCompare = jlft.getCompare();
			LGFile lBase = jlft.getBase();

			columIdent.add("Base key " + (lBase != null ? lBase.getName() : ""));
			columIdent.add("Base value " + (lBase != null ? lBase.getName() : ""));

			columIdent.add("Compare key " + (lCompare != null ? lCompare.getName() : ""));
			columIdent.add("Compare value " + (lCompare != null ? lCompare.getName() : ""));

			jlftTableModel.setColumnIdentifiers(columIdent);
			jlftTableModel.setColumnCount(4);
			jlftTableModel.setRowCount(this.getSizeForTable());
		}
		return jlftTableModel;
	}
	

	
	/**
	 * This method initializes compareToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getCompareToolBar()
	{
		if (compareToolBar == null)
		{
			compareToolBar = new JToolBar();
			compareToolBar.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
			compareToolBar.setFloatable(false);
			compareToolBar.setName("compareToolBar");
			compareToolBar.add(getOpenBase());
			compareToolBar.add(getOpenCompare());
			compareToolBar.add(getSaveCompare());
			compareToolBar.add(getRemove());
			compareToolBar.add(getRemoveGroup());
			compareToolBar.add(getAbout());
			compareToolBar.add(getLegendPanel());
		}
		return compareToolBar;
	}

	/**
	 * This method initializes openBase	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOpenBase()
	{
		if (openBase == null)
		{
			openBase = new JButton();
			openBase.setText("Open base");
			openBase.setToolTipText("Open a base langauge file (the left one)");
			openBase.addActionListener(this);
		}
		return openBase;
	}

	/**
	 * This method initializes openCompare	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOpenCompare()
	{
		if (openCompare == null)
		{
			openCompare = new JButton();
			openCompare.setText("Open compare");
			openCompare.setToolTipText("Open a compare langauge file (the left one)");
			openCompare.addActionListener(this);
		}
		return openCompare;
	}

	/**
	 * This method initializes saveCompare	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveCompare()
	{
		if (saveCompare == null)
		{
			saveCompare = new JButton();
			saveCompare.setText("Save compare");
			saveCompare.setToolTipText("Save the compare file (the right one)");
			saveCompare.addActionListener(this);
		}
		return saveCompare;
	}

	/**
	 * This method initializes remove	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRemove()
	{
		if (remove == null)
		{
			remove = new JButton();
			remove.setText("Remove");
			remove.setToolTipText("Remove the currently selected langauge String");
			remove.addActionListener(this);
		}
		return remove;
	}

	/**
	 * This method initializes about	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAbout()
	{
		if (about == null)
		{
			about = new JButton();
			about.setText("About");
			about.setToolTipText("Display info about this application");
			about.addActionListener(this);
		}
		return about;
	}

	public void actionPerformed(ActionEvent act){
		int answer = -1;
		if (act.getSource() == this.openBase) {
			JFileChooser chooser = new JFileChooser(LGFile.sBasePath);
			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				;
			{
				jlft.setBase(null);
				clearColum(BASE_SECTION);
				File file = chooser.getSelectedFile();

				try {
					LGFile lLGFile = LGFile.load(file.getCanonicalPath());
					Hashtable<String,LGString> lDuplicates = lLGFile.checkForDuplicates();
					if(lDuplicates.size() > 0){
						String lDupString = "";
						Enumeration<String> lDupKeys = lDuplicates.keys();
						while(lDupKeys.hasMoreElements()){
							lDupString+=lDuplicates.get(lDupKeys.nextElement()).getKey()+"\n";
						}
						JOptionPane.showMessageDialog(this, "Duplicate found in base file!\n"+lDupString+"\n Please remve these from the base file before continuing! ", "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						jlft.setBase(lLGFile);
					}
				} catch (Exception e) {
					if(JimmLangFileTool.DEBUG){
						System.out.println("Error loading file");
					}
				}
				jlft.compare();
				this.fillInValues();
			}
		}
		if (act.getSource() == this.openCompare) {
			
			if (jlft.getBase() != null) {
				JFileChooser chooser = new JFileChooser(LGFile.sBasePath);
				int returnVal = chooser.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION);
				{
					jlft.setCompare(null);
					clearColum(COMPARE_SECTION);
					File file = chooser.getSelectedFile();

					try {
						jlft.setCompare(LGFile.load(file.getCanonicalPath()));
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this, "Error loading the file", "Error", JOptionPane.ERROR_MESSAGE);
					}
					jlft.compare();
					this.fillInValues();
				}
			}
			else {
				JOptionPane.showMessageDialog(this, "You have to open a base file before you can open a compare file", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (act.getSource() == this.saveCompare) {
			JFileChooser chooser = new JFileChooser(LGFile.sBasePath);
			int returnVal = chooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				;
			{
				File file = chooser.getSelectedFile();

				try {
					jlft.getCompare().save(file.getCanonicalPath());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "Error saving the file", "Error", JOptionPane.ERROR_MESSAGE);
				}
				jlft.compare();
				this.fillInValues();
			}
		}
		if (act.getSource() == this.remove)
			if ((compareTable.getSelectedColumn() == -1) || (compareTable.getSelectedRow() == -1))
				JOptionPane.showMessageDialog(this, "You have to select an item first", "Select first", JOptionPane.ERROR_MESSAGE);
			else if (((compareTable.getValueAt(compareTable.getSelectedRow(), compareTable.getSelectedColumn()) instanceof LGString))) {
				if ((((LGString) compareTable.getValueAt(compareTable.getSelectedRow(), compareTable.getSelectedColumn())).isTranslated() != LGString.NOT_IN_BASE_FILE))
					JOptionPane.showMessageDialog(this, "You can only delete items which are not in the base file(red).", "Cannot delete item", JOptionPane.ERROR_MESSAGE);
				else {
					answer = JOptionPane.showConfirmDialog(this, "Delete \"" + ((LGString) compareTable.getValueAt(compareTable.getSelectedRow(), compareTable.getSelectedColumn())).getKey() + "\"?", "Delete?", JOptionPane.YES_NO_OPTION);
					switch (answer) {
					case JOptionPane.YES_OPTION:
						if(JimmLangFileTool.DEBUG){
							System.out.println("YES");
						}
						((LGString) compareTable.getValueAt(compareTable.getSelectedRow(), compareTable.getSelectedColumn())).setTranslated(LGString.REMOVED);
						fillInValues();
						break;
					default: // do nothing
					}
				}
			}

		if (act.getSource() == this.removeGroup)
			if (compareTable.getSelectedColumn() > 1)
				if ((compareTable.getSelectedColumn() == -1) || (compareTable.getSelectedRow() == -1))
					JOptionPane.showMessageDialog(this, "You have to select a group item first", "Select first", JOptionPane.ERROR_MESSAGE);
				else if ((compareTable.getValueAt(compareTable.getSelectedRow(), compareTable.getSelectedColumn()) instanceof LGFileSubset)) {
					answer = JOptionPane.showConfirmDialog(this, "Delete \"" + ((LGFileSubset) compareTable.getValueAt(compareTable.getSelectedRow(), compareTable.getSelectedColumn())).getId() + "\"?", "Delete?", JOptionPane.YES_NO_OPTION);
					switch (answer) {
					case JOptionPane.YES_OPTION:
						if(JimmLangFileTool.DEBUG){
							System.out.println("YES");
							System.out.println("Delete it");
						}
						LGFileSubset lRemoveSubset = ((LGFileSubset) compareTable.getValueAt(compareTable.getSelectedRow(), compareTable.getSelectedColumn()));
						lRemoveSubset.setRemoved(true);
						for(int i=0;i<lRemoveSubset.size();i++){
							lRemoveSubset.get(i).setTranslated(LGString.REMOVED);
						}
						
						fillInValues();
						break;
					default: // do nothing
					}
				}

		if (act.getSource() == this.about)
			JOptionPane.showMessageDialog(this, "Jimm Lang File Tool - Tool for editing Jimm language Files\n\n Version " + JimmLangFileTool.VERSION + "\n\n(c) Jimm project 2005-2008\n\nwww.jimm.org\n\n", "About Jimm Lang File Tool", JOptionPane.PLAIN_MESSAGE);

	}



	public class JlftTableRenderer extends DefaultTableCellRenderer
	{
	
		private static final long serialVersionUID = 1L;
		LGString lgs;
	
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof LGString)
			{
				lgs = (LGString) value;
	
				switch (lgs.isTranslated())
				{
				case LGString.TRANSLATED:
					cell.setBackground(Color.white);
					break;
				case LGString.NEWLY_TRANSLATED:
					cell.setBackground(Color.orange);
					break;
				case LGString.NOT_IN_BASE_FILE:
					cell.setBackground(Color.red);
					break;
				case LGString.NOT_TRANSLATED:
					cell.setBackground(Color.yellow);
					break;
				default:
					cell.setBackground(Color.white);
				}
			}
			else
			{
				cell.setFocusable(false);
				cell.setBackground(Color.lightGray);
			}
	
			return cell;
	
		}
	}


	public class JlftTableModel extends DefaultTableModel
	{
		
		private static final long serialVersionUID = 1L;
		LGString lgs;
		
		public boolean isCellEditable(int row, int colum)
		{
			if(!(super.getValueAt(row,colum) instanceof LGString) || (colum  < 2) || ((LGString)super.getValueAt(row,colum)).isTranslated() == LGString.NOT_IN_BASE_FILE)
				return false;
			else
				return true;
			
		}
		
		public Object getValueAt(int row, int colum)
		{
			if(super.getValueAt(row,colum) instanceof LGString)
			{
				lgs = (LGString)super.getValueAt(row,colum);
				if(colum %2 == 0)
					lgs.setReturnKey(true);
				else
					lgs.setReturnKey(false);
				return lgs;
			}
			else
				return super.getValueAt(row,colum);
		}
		
		public void setValueAt(Object value,int row, int colum)
		{
			if (value instanceof String && !(((String)value).startsWith("MOD_") || ((String)value).startsWith("TAR_") || ((String)value).startsWith("GENERAL")) && getValueAt(row, colum) instanceof LGString)
			{
				lgs = (LGString) getValueAt(row, colum);
				if (!((String) value).equals(lgs.getValue()))
				{
					lgs.setTranslated(LGString.NEWLY_TRANSLATED);
					lgs.setValue((String)value);
					super.setValueAt(lgs.getClone(), row, colum);
				}
			}
			else
				super.setValueAt(value, row, colum);
	
		}
	}


	/**
	 * This method initializes legendPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getLegendPanel()
	{
		if (legendPanel == null)
		{
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(2);
			gridLayout.setHgap(3);
			gridLayout.setVgap(3);
			gridLayout.setColumns(2);
			legendPanel = new JPanel();
			legendPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3));
			legendPanel.setLayout(gridLayout);
			legendPanel.add(getWhite(), null);
			legendPanel.add(getRed(), null);
			legendPanel.add(getOrange(), null);
			legendPanel.add(getYellow(), null);
		}
		return legendPanel;
	}

	/**
	 * This method initializes white	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getWhite()
	{
		if (white == null)
		{
			white = new JLabel();
			white.setText("Key was found in base and compare file");
			white.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
			white.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 10));
			white.setOpaque(true);
			white.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,3,0,3));
			white.setBackground(new Color(255,255,255));
		}
		return white;
	}

	/**
	 * This method initializes yellow	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getYellow()
	{
		if (yellow == null)
		{
			yellow = new JLabel();
			yellow.setText("Key was not found in compare file");
			yellow.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
			yellow.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 10));
			yellow.setOpaque(true);
			yellow.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,3,0,3));
			yellow.setBackground(java.awt.Color.yellow);
		}
		return yellow;
	}

	/**
	 * This method initializes orange	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getOrange()
	{
		if (orange == null)
		{
			orange = new JLabel();
			orange.setText("Text for this key was changed but not saved yet");
			orange.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
			orange.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 10));
			orange.setOpaque(true);
			orange.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,3,0,3));
			orange.setBackground(java.awt.Color.orange);
		}
		return orange;
	}

	/**
	 * This method initializes red	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getRed()
	{
		if (red == null)
		{
			red = new JLabel();
			red.setText("Key was found in compare but not in base");
			red.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
			red.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 10));
			red.setOpaque(true);
			red.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,3,0,3));
			red.setBackground(java.awt.Color.red);
		}
		return red;
	}

	/**
	 * This method initializes removeGroup	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRemoveGroup()
	{
		if (removeGroup == null)
		{
			removeGroup = new JButton();
			removeGroup.setText("Remove group");
			removeGroup.setToolTipText("Remove a whole group of Strings at once");
			removeGroup.addActionListener(this);
		}
		return removeGroup;
	}

}
