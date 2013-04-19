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

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.UIHelper;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
public class WizardInfo extends JPanel {

	/**
	 */
	public WizardInfo() {
		super();
		descrLabel = new JLabel();
		wizLabel = new JLabel();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(Color.white);
		setBorder(new EtchedBorder());
		wizLabel.setHorizontalAlignment(SwingConstants.LEFT);
		add(wizLabel);
		descrLabel.setFont(MToolKit.defaultFont);
		descrLabel.setHorizontalAlignment(SwingConstants.LEFT);
		descrLabel.setVerticalTextPosition(SwingConstants.TOP);
		add(descrLabel);
	}

	private void reset(ImageIcon icon, String description) {
		descrLabel.setText(description == null ? "" : " " + description);
		if (icon != null)
			noNewMessage = false;
		wizLabel.setIcon(icon);
	}

	/**
	 * Reset the error state with the given description.
	 * 
	 * @param description
	 *          the description to display.
	 */
	protected void resetError(String description) {
		reset(ERROR_ICO, description);
	}

	/**
	 * Reset the warning state with the given description.
	 * 
	 * @param description
	 *          the description to display.
	 */
	public void resetWarning(String description) {
		reset(WARNING_ICO, description);
	}

	/**
	 * Reset the state.
	 */
	public void reset() {
		reset(null, null);
	}

	private JLabel descrLabel;

	private JLabel wizLabel;

	/**
	 * Flag indicating a new posted message.
	 */
	protected boolean noNewMessage;

	private static final ImageIcon WARNING_ICO = UIHelper
			.getIcon("wiz_warning.gif");

	private static final ImageIcon ERROR_ICO = UIHelper.getIcon("wiz_error.gif");
}
