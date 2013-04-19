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
package net.sf.firemox.ui.wizard;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class WizardImageIcon extends ImageIcon {

	/**
	 * The width of displayed icon.
	 */
	public static final int CARD_ICON_WIDTH = 15;

	/**
	 * The alpha composite of background
	 */
	public static final float VALUE_ALPHA_COMPOSITE = .7f;

	/**
	 * The height of displayed icon.
	 */
	public static final int CARD_ICON_HEIGHT = CARD_ICON_WIDTH
			* IdConst.STD_HEIGHT / IdConst.STD_WIDTH;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param card
	 *          the card to display in this panel.
	 * @param iconName
	 *          the back picture to display in this panel.
	 */
	public WizardImageIcon(MCard card, String iconName) {
		this(card.image(), iconName);
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param cardImage
	 *          the image to display in this panel.
	 * @param iconName
	 *          the back picture to display in this panel.
	 */
	public WizardImageIcon(Image cardImage, String iconName) {
		super(MToolKit.getIconPath(iconName));
		this.cardImage = cardImage;
	}

	/**
	 * Set the card to display in this panel.
	 * 
	 * @param card
	 *          the card to display in this panel.
	 */
	public void setCard(MCard card) {
		cardImage = card.image();
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2D = (Graphics2D) g;
		g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
				VALUE_ALPHA_COMPOSITE));
		g.drawImage(getImage(), x, y, getIconWidth(), getIconHeight(), c);
		g2D.setColor(c.getBackground());
		g2D.fillRect(0, getIconHeight(), getIconWidth(), c.getHeight()
				- getIconHeight());
		if (cardImage != null) {
			g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_DEFAULT);
			g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
			g.drawImage(cardImage, x, y, CARD_ICON_WIDTH, CARD_ICON_HEIGHT, c);
		}
	}

	/**
	 * The optional card to display in the icon.
	 */
	private Image cardImage;

}
