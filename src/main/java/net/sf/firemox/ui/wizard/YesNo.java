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
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class YesNo extends Ok {

	/**
	 * The YES key label for button.
	 */
	public static final String LABEL_YES = "yes";

	/**
	 * The NO key label for button.
	 */
	public static final String LABEL_NO = "no";

	/**
	 * Create a new instance of this class.
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
	 * @param yesButton
	 *          the 'yes button' label.
	 * @param noButton
	 *          the 'no button' label.
	 * @param width
	 *          the preferred width.
	 * @param height
	 *          the preferred height.
	 */
	public YesNo(Ability ability, String title, String description,
			String iconName, String yesButton, String noButton, int width, int height) {
		super(ability, title, description, iconName, noButton, width, height);

		// buttons
		if (yesButton == null) {
			okBtn = new JButton(LanguageManager.getString(LABEL_YES));
		} else {
			okBtn = new JButton(yesButton);
		}
		okBtn.setMnemonic(okBtn.getText().charAt(0));
		okBtn.setMaximumSize(new Dimension(100, 26));
		okBtn.addActionListener(this);
		buttonPanel.add(okBtn, 0);
		getRootPane().setDefaultButton(okBtn);
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param title
	 *          the title of this wizard.
	 * @param description
	 *          the description appended to the title of this wizard. This content
	 *          will be displayed as Html.
	 * @param iconName
	 *          the icon's name to display on the top right place.
	 * @param yesButton
	 *          the 'yes button' label.
	 * @param noButton
	 *          the 'no button' label.
	 * @param width
	 *          the preferred width.
	 * @param height
	 *          the preferred height.
	 */
	public YesNo(String title, String description, String iconName,
			String yesButton, String noButton, int width, int height) {
		this(null, title, description, iconName, yesButton, noButton, width, height);
	}

	/**
	 * Create a new instance of this class.
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
	 */
	public YesNo(Ability ability, String title, String description,
			String iconName, int width, int height) {
		this(ability, title, description, iconName, LanguageManager
				.getString(LABEL_YES), LanguageManager.getString(LABEL_NO), width,
				height);
	}

	/**
	 * Create a new instance of this class.
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
	 *          the text to display.
	 */
	public YesNo(Ability ability, String title, String description,
			String iconName, int width, int height, String text) {
		this(ability, title, description, iconName, width, height);
		gameParamPanel.add(new JLabel("<html>" + text));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == cancelBtn) {
			validAnswer(JOptionPane.NO_OPTION);
		} else if (event.getSource() == okBtn) {
			validAnswer(JOptionPane.YES_OPTION);
		} else {
			super.actionPerformed(event);
		}
	}

	/**
	 */
	protected final JButton okBtn;

}
