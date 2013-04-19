/*
 * Created on Aug 19, 2004 
 * Original filename was AttachmentLayout.java
 * 
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
package net.sf.firemox.ui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.tools.Configuration;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class AttachmentLayout implements LayoutManager {

	/**
	 * Attachment dx position
	 */
	private int attachmentDx;

	/**
	 * Attachment dy position
	 */
	private static int attachmentDy;

	/**
	 * Amount of pixels increased/decreased for each mouse wheel unit.
	 */
	public static final int PIXEL_UNTIT = 5;

	/**
	 * Creates a new instance of AttachmentLayout <br>
	 */
	public AttachmentLayout() {
		super();
		attachmentDy = (int) (CardFactory.cardHeight * 0.2f);
		attachmentDx = attachmentDy;
	}

	public Dimension preferredLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			final MCard card = (MCard) target;
			if (card.tapped) {
				return new Dimension(card.getMUI().tappedSize.width + attachmentDy
						* (card.getComponentCount() - 1), card.getMUI().tappedSize.height
						+ attachmentDx * (card.getComponentCount() - 1));

			}
			return new Dimension(card.getMUI().untappedSize.width + attachmentDx
					* (card.getComponentCount() - 1), card.getMUI().untappedSize.height
					+ attachmentDy * (card.getComponentCount() - 1));
		}
	}

	public Dimension minimumLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			return preferredLayoutSize(target);
		}
	}

	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			final MCard card = (MCard) target;
			final int nb = card.getComponentCount();
			if (card.tapped) {
				if (card.reversed && Configuration.getBoolean("reverseArt", true)) {
					for (int i = nb; i-- > 0;) {
						card.getComponent(i).setLocation((nb - i - 1) * attachmentDy,
								(nb - i - 1) * attachmentDx);
					}
				} else {
					for (int i = nb; i-- > 0;) {
						card.getComponent(i)
								.setLocation(i * attachmentDy, i * attachmentDx);
					}
				}
			} else if (card.reversed && Configuration.getBoolean("reverseArt", true)) {
				for (int i = nb; i-- > 0;) {
					card.getComponent(i).setLocation((nb - i - 1) * attachmentDx,
							i * attachmentDy);
				}
			} else {
				for (int i = nb; i-- > 0;) {
					card.getComponent(i).setLocation(i * attachmentDx,
							(nb - i - 1) * attachmentDy);
				}
			}
			card.setPreferredSize(preferredLayoutSize(card));
			card.setSize(card.getPreferredSize());
		}
	}

	public void addLayoutComponent(String name, Component comp) {
		// Ignore this event
	}

	public void removeLayoutComponent(Component comp) {
		// Ignore this event
	}

	/**
	 * Decrease the amount of units used to separate nested cards.
	 * 
	 * @param amount
	 *          the amount of units.
	 */
	public void decreaseCardLayout(int amount) {
		attachmentDx -= amount * PIXEL_UNTIT;
		attachmentDy -= amount * PIXEL_UNTIT;
		if (attachmentDx < 0) {
			attachmentDx = 0;
		}
		if (attachmentDy < 0) {
			attachmentDy = 0;
		}
	}

	/**
	 * Increase the amount of units used to separate nested cards.
	 * 
	 * @param amount
	 *          the amount of units.
	 */
	public void increaseCardLayout(int amount) {
		attachmentDx += amount * PIXEL_UNTIT;
		attachmentDy += amount * PIXEL_UNTIT;
		if (attachmentDx > CardFactory.cardWidth) {
			attachmentDx = CardFactory.cardWidth;
		}
		if (attachmentDy > CardFactory.cardHeight) {
			attachmentDy = CardFactory.cardHeight;
		}
	}
}