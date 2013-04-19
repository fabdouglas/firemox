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
package net.sf.firemox.zone;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;

/**
 * Represents Graveyard or Library zone.<br>
 * All indexes of this zone are inverted.
 * 
 * @author Fabrice Daugan
 * @since 0.2c
 * @since 0.3 feature "reverseImage" implemented
 * @since 0.4 you can now change wallpaper/color of this MZone and setting are
 *        saved.
 * @since 0.54.16 new shuffle algorithm
 */
public class ExpandableZone extends MZone {

	/**
	 * create a new instance of ExpandableZone
	 * 
	 * @param zoneConfiguration
	 *          the zone configuration
	 * @param superPanel
	 *          scroll panel containing this panel
	 * @param reverseImage
	 *          if true the back picture will be reversed
	 * @since 0.3 feature "reverseImage" implemented
	 */
	protected ExpandableZone(ZoneConfiguration zoneConfiguration,
			JScrollPane superPanel, boolean reverseImage) {
		super(zoneConfiguration.getZoneId(), null, superPanel, reverseImage,
				zoneConfiguration.getZoneName());
		this.zoneConfiguration = zoneConfiguration;
	}

	@Override
	public void initUI() {
		super.initUI();
		if (zoneConfiguration.getLayoutClass() != null) {
			try {
				setLayout((LayoutManager) Class.forName(
						zoneConfiguration.getLayoutClass()).newInstance());
			} catch (Exception e) {
				Log.error("Unable to load the specified layout manager '"
						+ zoneConfiguration.getLayoutClass() + "'", e);
			}
		}
		superPanel
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		superPanel
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	}

	@Override
	public final void highLight(Color targetColor) {
		// deploy this panel
		if (ZoneManager.expandedZone != this) {
			deployPanel();
		}
	}

	/**
	 * Toggle display of this zone
	 */
	public void toggle() {
		if (ZoneManager.expandedZone == this) {
			// Gather this zone
			ZoneManager.expandedZone = null;
			ZoneManager.expandedPanel.setVisible(false);
			gatheredParent
					.add(superPanel, zoneConfiguration.getLayoutConstraintYou());
			updatePanel();
		} else {
			// deploy this zone
			deployPanel();
		}
	}

	/**
	 * seprarate cards of this zone
	 */
	public void deployPanel() {
		if (ZoneManager.expandedZone == this) {
			return;
		}

		if (ZoneManager.expandedZone != null) {
			// gather the previous zone
			ZoneManager.expandedZone.toggle();
		}

		final int count = getCardCount();
		if (count == 0)
			return;
		ZoneManager.expandedZone = this;
		Point location = MToolKit.getAbsoluteLocation(getTop());
		location.translate(-6, -6);
		ZoneManager.expandedPanel.setLocation(location);
		ZoneManager.expandedPanel.removeAll();
		gatheredParent = (JPanel) superPanel.getParent();
		ZoneManager.expandedPanel.add(superPanel);

		for (int a = 0; a < count; a++) {
			getCard(a).setLocation(2 + (CardFactory.cardWidth + 2) * a, 2);
		}

		setPreferredSize(new Dimension((CardFactory.cardWidth + 2) * count + 4,
				CardFactory.cardHeight + 10));
		ZoneManager.expandedPanel.setSize(new Dimension(Math.min(600,
				getPreferredSize().width + 10), CardFactory.cardHeight + 28));
		ZoneManager.expandedPanel.setMaximumSize(ZoneManager.expandedPanel
				.getSize());
		ZoneManager.expandedPanel.setPreferredSize(ZoneManager.expandedPanel
				.getSize());
		superPanel
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		superPanel.setViewportView(this);
		ZoneManager.expandedPanel.setVisible(true);
	}

	/**
	 * update this library = hide library too
	 */
	@Override
	public void updatePanel() {
		final int count = getCardCount();
		if (ZoneManager.expandedZone == this) {
			toggle();
		} else {
			for (int a = 0; a < count; a++) {
				getCard(a).setLocation(18 + a / IdConst.CARD_3D_COUNT,
						2 + a / IdConst.CARD_3D_COUNT);
			}
			setPreferredSize(new Dimension((IdConst.STD_WIDTH + 5) / 2,
					CardFactory.cardHeight + 10));
			superPanel
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			superPanel.setViewportView(this);
		}
	}

	@Override
	public void disHighLight() {
		// gather this zone
		if (ZoneManager.expandedZone == this) {
			toggle();
		}
	}

	/**
	 * Dishighlight all cards of this zone manager
	 */
	@Override
	public void disHighLightAll() {
		for (Component card : getComponents()) {
			((MCard) card).disHighLight();
		}
		disHighLight();
	}

	@Override
	public boolean isMustBePaintedReversed(MCard card) {
		if (getCardCount() == 0
				|| (card != getTop() && ZoneManager.expandedZone != this))
			return false;
		return super.isMustBePaintedReversed(card);
	}

	@Override
	public boolean isMustBePainted(MCard card) {
		if (getCardCount() == 0
				|| (card != getTop() && ZoneManager.expandedZone != this))
			return false;
		return super.isMustBePainted(card);
	}

	@Override
	public boolean startDragAndDrop(MCard card, Point mousePoint) {
		dragAndDropComponent = null;
		return false;
	}

	/**
	 * The configuration attached to this zone.
	 */
	public final ZoneConfiguration zoneConfiguration;

	/**
	 * The panel containing this zone when it is not expanded.
	 */
	private JPanel gatheredParent;

	/**
	 * Is this zone can be gathered or not.
	 * 
	 * @return <code>true</code> if this zone can be gathered.
	 */
	public boolean canBeGathered() {
		return zoneConfiguration.getLayoutClass() == null;
	}

}