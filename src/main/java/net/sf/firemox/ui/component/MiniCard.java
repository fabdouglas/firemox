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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;

import net.sf.firemox.token.IdConst;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class MiniCard extends ImageIcon {

	/**
	 * The width of icon.
	 */
	public static final int CARD_ICON_WIDTH = 15;

	/**
	 * The height of icon.
	 */
	public static final int CARD_ICON_HEIGHT = CARD_ICON_WIDTH
			* IdConst.STD_HEIGHT / IdConst.STD_WIDTH;

	/**
	 * OPtional back picture.
	 */
	protected ImageIcon backImage;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param image
	 *          the image to display.
	 * @param backImage
	 *          the background image behing the card.
	 */
	public MiniCard(Image image, ImageIcon backImage) {
		super(image);
		this.backImage = backImage;
	}

	@Override
	protected void loadImage(Image image) {
		// Nothing to do
	}

	/**
	 * Paints the icon. The top-left corner of the icon is drawn at the point (<code>x</code>,
	 * <code>y</code>) in the coordinate space of the graphics context
	 * <code>g</code>. If this icon has no image observer, this method uses the
	 * <code>c</code> component as the observer.
	 * 
	 * @param c
	 *          the component to be used as the observer if this icon has no image
	 *          observer
	 * @param g
	 *          the graphics context
	 * @param x
	 *          the X coordinate of the icon's top-left corner
	 * @param y
	 *          the Y coordinate of the icon's top-left corner
	 */
	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		backImage.paintIcon(c, g, x, y);
		g.drawImage(getImage(), x, y, x + CARD_ICON_WIDTH, y + CARD_ICON_HEIGHT, c);
	}

	/**
	 * Gets the width of the icon.
	 * 
	 * @return the width in pixels of this icon
	 */
	@Override
	public int getIconWidth() {
		return backImage.getIconWidth();
	}

	/**
	 * Gets the height of the icon.
	 * 
	 * @return the height in pixels of this icon
	 */
	@Override
	public int getIconHeight() {
		return backImage.getIconHeight();
	}

}
