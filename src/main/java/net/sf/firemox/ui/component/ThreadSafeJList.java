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
package net.sf.firemox.ui.component;

import java.awt.Dimension;

import javax.swing.JList;

import net.sf.firemox.tools.MCardCompare;
import net.sf.firemox.ui.MListModel;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public class ThreadSafeJList extends JList {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param allListModel
	 */

	public ThreadSafeJList(MListModel<MCardCompare> allListModel) {
		super(allListModel);
	}

	@Override
	public Dimension getPreferredSize() {
		try {
			previousPreferredSize = super.getPreferredSize();
		} catch (Exception e) {
			// Ignore this error
		}
		return previousPreferredSize;
	}

	private Dimension previousPreferredSize = new Dimension(100, 500);

}
