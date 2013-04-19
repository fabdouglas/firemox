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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JPanel;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class TimerGlassPane extends JPanel {

	private static final int SIZE_HEIGHT = 105;

	private static final int SIZE_WIDTH = 105;

	/**
	 * Composite for all paints of this component.
	 */
	private AlphaComposite composite;

	/**
	 * The slide show counter used to draw some animated pictures.
	 */
	private int slideShowConter;

	/**
	 * List of pictures to use for make an animated picture. May be null.
	 */
	private Image[] gifPicture;

	/**
	 * The picture currently displayed on front of this zone.
	 */
	private Image timerPicture = null;

	/**
	 * Create a new instance of this class.
	 */
	public TimerGlassPane() {
		setOpaque(false);
		composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	}

	/**
	 * Enable/disable timer picture on this zone
	 * 
	 * @param timerPicture
	 *          the picture to display on front of this zone.
	 * @param gifPicture
	 *          list of pictures to use for make an animated picture. May be null.
	 */
	public void updatePicture(Image timerPicture, Image[] gifPicture) {
		if (timerPicture == null) {
			setVisible(false);
		} else {
			if (this.timerPicture != timerPicture) {
				this.timerPicture = timerPicture;
				this.gifPicture = gifPicture;
				slideShowConter = 0;
				if (isVisible()) {
					repaint();
				}
			} else {
				if (this.gifPicture != gifPicture) {
					slideShowConter = -1;
					this.gifPicture = gifPicture;
				}
				if (this.gifPicture != null) {
					slideShowConter++;
					if (isVisible()) {
						repaint();
					}
				}
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		if (timerPicture != null && isVisible()) {
			final int x = getSize().width / 2 - SIZE_WIDTH / 2;
			final int y = getSize().height / 2 - SIZE_HEIGHT / 2;
			final Graphics2D g2d = (Graphics2D) g;
			g2d.setComposite(composite);
			g.setColor(Color.BLACK);
			g.drawRoundRect(x, y, SIZE_WIDTH + 1, SIZE_HEIGHT + 1, 2, 2);
			g.drawImage(timerPicture, x + 1, y + 1, null);
			if (gifPicture != null) {
				g.drawImage(gifPicture[slideShowConter % gifPicture.length], x
						+ timerPicture.getWidth(null) - 20, y
						+ timerPicture.getHeight(null) - 20, null);
			}
			g.dispose();
		}
	}
}
