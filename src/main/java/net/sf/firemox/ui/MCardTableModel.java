/*
 *   Firemox is a turn based strategy simulator
 *   Copyright (C) 2003-2007 Fabrice Daugan
 *
 *   This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 *   This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 *
 *   You should have received a copy of the GNU General Public License along  
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sf.firemox.ui;

import javax.swing.table.AbstractTableModel;

import net.sf.firemox.tools.MCardCompare;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94 create a TableModel with a list of MCardCompare
 */
public class MCardTableModel extends AbstractTableModel {

	private MListModel<MCardCompare> mCardList;

	/**
	 * Create a new TableModel with an MCardCompare list as source for cells.
	 * 
	 * @param mCardList
	 */
	public MCardTableModel(MListModel<MCardCompare> mCardList) {
		this.mCardList = mCardList;
	}

	/**
	 * number of columns of the table
	 * 
	 * @return 2 columns
	 */
	public int getColumnCount() {
		return 2;
	}

	/**
	 * number of rowns of the table
	 * 
	 * @return number of cards in deck
	 */
	public int getRowCount() {
		return mCardList.getSize();
	}

	/**
	 * only the quantities column may be edited
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 1)
			return false;
		return true;
	}

	/**
	 * get the value in cell [rowIndex; columnIndex]
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return cell value
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		MCardCompare card = mCardList.getElementAt(rowIndex);
		if (columnIndex == 1)
			return card.getName();
		return card.getAmount();
	}

	/**
	 * set value at cell [rowIndex; columnIndex]
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		int i = Integer.parseInt(aValue.toString());
		mCardList.getElementAt(rowIndex).setAmount(i);

		if (i == 0) {
			fireTableCellUpdated(rowIndex, columnIndex);
			mCardList.remove(rowIndex);
			return;
		}
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	/**
	 * @return mCardList -> list of cards
	 */
	public MListModel<MCardCompare> getCards() {
		return mCardList;
	}

	/**
	 * Refresh the list.
	 */
	public void refresh() {
		fireTableDataChanged();
	}

}
