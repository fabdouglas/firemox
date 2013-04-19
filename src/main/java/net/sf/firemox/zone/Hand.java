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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;

import javax.swing.JScrollPane;

import net.sf.firemox.clickable.Clickable;
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.ui.layout.FlowLayout2;

/**
 * The hand zone. This zone is the zone the spells are played from.
 * 
 * @author Fabrice Daugan
 * @since 0.2d
 * @since 0.3 feature "reverseImage" implemented
 * @since 0.4 you can now change wallpaper/color of this MZone and setting are
 *        saved.
 */
public class Hand extends MZone implements HierarchyBoundsListener {

	/**
	 * The zone name.
	 */
	public static final String ZONE_NAME = "hand";

	/**
	 * create a new instance of Hand
	 * 
	 * @param superPanel
	 *          scroll panel containing this panel
	 * @param reverseImage
	 *          if true the back picture will be reversed
	 * @since 0.3 feature "reverseImage" implemented
	 * @see IdZones
	 */
	Hand(JScrollPane superPanel, boolean reverseImage) {
		super(IdZones.HAND, new FlowLayout(), superPanel, reverseImage, ZONE_NAME);
	}

	@Override
	public void initUI() {
		super.initUI();
		setMinimumSize(new Dimension(90, 60));
		updateLayouts(this,
				(Configuration.getBoolean("reverseSide", false) || Configuration
						.getBoolean("reverseArt", true))
						&& reverseImage);
		addHierarchyBoundsListener(this);
	}

	public void ancestorMoved(HierarchyEvent evt) {
		// Ignore this event
	}

	public void ancestorResized(HierarchyEvent evt) {
		updatePanel();
	}

	/**
	 * update this hand
	 */
	@Override
	public void updatePanel() {
		int count = getComponentCount();
		if (count == 0) {
			count = 1;
		}
		if (getParent() != null) {
			// The UI is initialized
			int nbPerLine = getParent().getWidth() / (CardFactory.cardWidth + 5);
			if (nbPerLine > 0) {
				setPreferredSize(new Dimension(getParent().getWidth(),
						(CardFactory.cardHeight + 5) * (count + nbPerLine - 1) / nbPerLine));
			}
			superPanel.setViewportView(this);
		}
	}

	/**
	 * 
	 */
	@Override
	public void updateReversed() {
		// need to update?
		if ((Configuration.getBoolean("reverseSide", false) || Configuration
				.getBoolean("reverseArt", true))
				&& reverseImage
				&& getLayout() instanceof FlowLayout
				|| !((Configuration.getBoolean("reverseSide", false) || Configuration
						.getBoolean("reverseArt", true)) && reverseImage)
				&& getLayout() instanceof FlowLayout2) {
			updateLayouts(this,
					(Configuration.getBoolean("reverseSide", false) || Configuration
							.getBoolean("reverseArt", true))
							&& reverseImage);
		}
		updatePanel();
	}

	/**
	 * Dishighlight all cards of this zone manager
	 */
	@Override
	public void disHighLightAll() {
		for (int i = getComponentCount(); i-- > 0;) {
			((Clickable) getComponent(i)).disHighLight();
		}
	}

	/**
	 * Dishighlight only this component, not the components of this zone.
	 */
	@Override
	public void disHighLight() {
		// nothing to do
	}

	/**
	 * Highlight only this component, not the components of this zone. Use instead
	 * the specific highlight color of the desired zone.
	 * 
	 * @param targetColor
	 */
	@Override
	public void highLight(Color targetColor) {
		// nothing to do
	}

}