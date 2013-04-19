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
 * 
 */
package net.sf.firemox.zone;

import java.awt.FlowLayout;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;

import javax.swing.JScrollPane;

import net.sf.firemox.clickable.target.card.DelayedCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.ui.component.TableTop;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public class DelayedBuffer extends MZone {

	/**
	 * The zone name.
	 */
	public static final String ZONE_NAME = "dbz";

	/**
	 * create a new instance of DelayedBuffer
	 * 
	 * @param superPanel
	 *          scroll panel containing this panel
	 * @param you
	 *          is this zone is controlled by you.
	 * @since 0.3 feature "reverseImage" implemented
	 * @since 0.80 feature "reverseImage" removed, since this panel has been moved
	 *        in a JTabbedPanel with the stack
	 * @see IdZones
	 */
	DelayedBuffer(JScrollPane superPanel, boolean you) {
		super(IdZones.DELAYED, new FlowLayout(FlowLayout.LEFT, 2, 2), superPanel,
				!you, ZONE_NAME);
		this.you = you;
		this.reverseImage = false;
	}

	@Override
	public void initUI() {
		super.initUI();
		addHierarchyBoundsListener(new HierarchyBoundsListener() {

			public void ancestorMoved(HierarchyEvent evt) {
				// Ignore this event
			}

			public void ancestorResized(HierarchyEvent evt) {
				updatePanel();
			}
		});
	}

	/**
	 * Add a delayed ability to this zone
	 * 
	 * @param delayed
	 *          the delayed to add
	 */
	public void add(DelayedCard delayed) {
		super.add(delayed);
		updatePanel();
	}

	@Override
	public int getControllerIdPlayer() {
		return you ? 0 : 1;
	}

	@Override
	public void disHighLight() {
		TableTop.getInstance().tabbedPane.setBackgroundAt(
				TableTop.getInstance().tabbedPane.indexOfComponent(superPanel), null);
	}

	@Override
	public boolean isMustBePaintedReversed(MCard card) {
		return false;
	}

	/**
	 * is this zone is controlled by you.
	 */
	private boolean you;
}