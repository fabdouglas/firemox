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
package net.sf.firemox.clickable.target.player;

import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.81
 */
public class AvatarButton extends JButton {

	/**
	 * Creates a new instance of JButton2 <br>
	 */
	public AvatarButton() {
		super();
	}

	/**
	 * Set the of this button.
	 * 
	 * @param defaultIcon
	 */
	public void setIcon(ImageIcon defaultIcon) {
		this.defaultIcon = defaultIcon;
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (defaultIcon != null) {
			super.paintBorder(g);
			g.drawImage(defaultIcon.getImage(), 0, 0, getWidth() - 2,
					getHeight() - 2, this);
		}
	}

	private ImageIcon defaultIcon;
}
