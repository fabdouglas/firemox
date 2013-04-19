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

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.ui.ToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * <ul>
 * A simple OK message box with
 * <li>header containing Html tile, description, icon
 * <li>Text
 * <li>OK button
 * <li>BACKGROUND button
 * <li>validator
 * </ul>
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Ok extends Wizard {

	/**
	 * Maximal text content's size.
	 */
	protected static final int MAXIMAL_TXT_SIZE = 25;

	/**
	 * 
	 */
	protected static final int UNBOUNDED_TXT_SIZE = 2100;

	/**
	 * Left margin.
	 */
	protected static final int MARGIN_SIZE = 95;

	/**
	 * The OK key label for button.
	 */
	public static final String LABEL_OK = "ok";

	/**
	 * The CANCEL key label for button.
	 */
	public static final String LABEL_CANCEL = "cancel";

	/**
	 * Creates a new instance of this class <br>
	 * 
	 * @param ability
	 *          ability to associate to this ability. If this ability has an
	 *          associated picture, it will be used instead of given picture.
	 *          Ability's name is also used to fill the title. This ability will
	 *          be used to restart this wizard in case of Background button is
	 *          used.
	 * @param title
	 *          the title of this wizard.
	 * @param description
	 *          the description appended to the title of this wizard. This content
	 *          will be displayed as Html.
	 * @param iconName
	 *          the icon's name to display on the top right place.
	 * @param button
	 *          the button text to use. If is <code>null</code>, OK will be
	 *          used.
	 * @param width
	 *          the preferred width.
	 * @param height
	 *          the preferred height.
	 */
	public Ok(Ability ability, String title, String description, String iconName,
			String button, int width, int height) {
		super(ability, title, description, iconName, width, height);

		// buttons
		if (button == null) {
			cancelBtn = new JButton(LanguageManager.getString(LABEL_OK));
		} else {
			cancelBtn = new JButton(button);
		}
		cancelBtn.setMnemonic(cancelBtn.getText().charAt(0));
		cancelBtn.addActionListener(this);
		cancelBtn.setMaximumSize(new Dimension(100, 76));
		buttonPanel.add(cancelBtn, 0);
		getRootPane().setDefaultButton(cancelBtn);
		ToolKit.addCancelByEscapeKey(this, cancelBtn);
	}

	/**
	 * Creates a new instance of this class <br>
	 * 
	 * @param title
	 *          the title of this wizard.
	 * @param description
	 *          the description appended to the title of this wizard. This content
	 *          will be displayed as Html.
	 * @param iconName
	 *          the icon's name to display on the top right place.
	 * @param button
	 *          the button text to use. If is <code>null</code>, OK will be
	 *          used.
	 * @param width
	 *          the preferred width.
	 * @param height
	 *          the preferred height.
	 */
	public Ok(String title, String description, String iconName, String button,
			int width, int height) {
		this(null, title, description, iconName, button, width, height);
	}

	/**
	 * Creates a new instance of OK <br>
	 * 
	 * @param ability
	 *          ability to associate to this ability. If this ability has an
	 *          associated picture, it will be used instead of given picture.
	 *          Ability's name is also used to fill the title. This ability will
	 *          be used to restart this wizard in case of Background button is
	 *          used.
	 * @param title
	 *          the title of this wizard.
	 * @param description
	 *          the description appended to the title of this wizard. This content
	 *          will be displayed as Html.
	 * @param iconName
	 *          the icon's name to display on the top right place.
	 * @param width
	 *          the preferred width.
	 * @param height
	 *          the preferred height.
	 * @param text
	 */
	public Ok(Ability ability, String title, String description, String iconName,
			int width, int height, String text) {
		this(ability, title, description, iconName, null, width, height);
		gameParamPanel.add(new JLabel("<html>" + text));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == cancelBtn) {
			validAnswer(JOptionPane.OK_OPTION);
		} else {
			super.actionPerformed(event);
		}
	}

	@Override
	protected boolean checkValidity() {
		return true;
	}

	/**
	 * The "cancel" button
	 */
	protected JButton cancelBtn;

}
