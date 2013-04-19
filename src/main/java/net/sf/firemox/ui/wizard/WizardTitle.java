/*
 * Created on Dec 17, 2004
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
 */
package net.sf.firemox.ui.wizard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.81
 */
class WizardTitle extends JPanel {

	/**
	 * @param icon
	 *          is the rightmost displayed icon
	 * @param description
	 *          displayed text
	 */
	protected WizardTitle(final ImageIcon icon, String description) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(Color.white);
		setBorder(new EtchedBorder(EtchedBorder.RAISED));
		JLabel descrLabel = new JLabel("<html>" + description);
		descrLabel.setVerticalAlignment(SwingConstants.TOP);
		descrLabel.setFont(MToolKit.defaultFont);
		add(descrLabel);
		JPanel wizLabel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				icon.paintIcon(this, g, 0, 0);
			}
		};
		wizLabel.setPreferredSize(new Dimension(64, 64));
		wizLabel.setMinimumSize(new Dimension(64, 64));
		add(wizLabel);
	}

}
