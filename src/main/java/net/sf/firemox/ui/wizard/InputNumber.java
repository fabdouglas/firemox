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

import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.NumberFormatter;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * A message box prompting a number to a player. The value may have constraint.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class InputNumber extends YesNo {

	/**
	 * Creates a new instance of InputNumber <br>
	 * 
	 * @param title
	 *          the title of this wizard.
	 * @param description
	 *          the description appended to the title of this wizard. This content
	 *          will be displayed as Html.
	 * @param min
	 *          the minimal value.
	 * @param max
	 *          the maximal value.
	 * @param defaultValue
	 *          the default value.
	 */
	public InputNumber(String title, String description, int min, int max,
			int defaultValue) {
		this(null, title, description, min, max, true, defaultValue, true);
	}

	/**
	 * Creates a new instance of MInputIntegerFrame <br>
	 * 
	 * @param ability
	 *          ability to associate to this ability. If this ability has an
	 *          associated picture, it will be used instead of given picture.
	 *          Ability's name is also used to fill the title. This ability will
	 *          be used to restart this wizard in case of Background button is
	 *          used.
	 * @param description
	 *          the description appended to the title of this wizard. This content
	 *          will be displayed as Html.
	 * @param min
	 *          the minimal value.
	 * @param max
	 *          the maximal value.
	 * @param allowCancel
	 *          Is the cancel button is allowed.
	 * @param defaultValue
	 *          the default value.
	 * @param permissiveMax
	 *          if true, the max value is only an advise. Otherwise, a greater
	 *          value can be specified.
	 */
	public InputNumber(Ability ability, String description, int min, int max,
			boolean allowCancel, int defaultValue, boolean permissiveMax) {
		this(
				ability,
				LanguageManager.getString("wiz_input-number.title"),
				LanguageManager.getString("wiz_input-number.description") + description,
				min, max, allowCancel, defaultValue, permissiveMax);
	}

	/**
	 * Creates a new instance of MInputIntegerFrame <br>
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
	 * @param min
	 *          the minimal value.
	 * @param max
	 *          the maximal value.
	 * @param allowCancel
	 *          Is the cancel button is allowed.
	 * @param defaultValue
	 *          the default value.
	 * @param permissiveMax
	 *          if true, the max value is only an advise. Otherwise, a greater
	 *          value can be specified.
	 */
	private InputNumber(Ability ability, String title, String description,
			int min, int max, boolean allowCancel, int defaultValue,
			boolean permissiveMax) {
		super(ability, title, description, "wiz_input-number.gif", null,
				LanguageManager.getString("cancel"), 300, 200);
		if (allowCancel) {
			setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		} else {
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			this.cancelBtn.setEnabled(false);
		}
		this.min = min;
		this.max = max;
		this.permissiveMax = permissiveMax;
		this.defaultValue = defaultValue;
		gameParamPanel.setLayout(new BoxLayout(gameParamPanel, BoxLayout.X_AXIS));
		final NumberFormatter format = new NumberFormatter();
		format.setMinimum(min);
		if (permissiveMax) {
			gameParamPanel.add(new JLabel(LanguageManager.getString("value") + " ["
					+ min + " - " + max + "] : "));
			format.setMaximum(max);
		} else {
			gameParamPanel.add(new JLabel(LanguageManager.getString("value") + " ["
					+ min + " - " + max + "]+ : "));
		}
		format.setCommitsOnValidEdit(true);
		intText = new JFormattedTextField(format);
		intText.setMaximumSize(new Dimension(2000, 20));
		intText.setText(String.valueOf(defaultValue));
		addCheckValidity(intText);
		gameParamPanel.add(intText);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == cancelBtn) {
			// set the static integer token to the
			indexAnswer = defaultValue;
			super.actionPerformed(event);
		} else if (event.getSource() == okBtn) {
			// set the static integer token
			if (checkValidity()) {
				super.actionPerformed(event);
			} else {
				intText.selectAll();
			}
		} else {
			super.actionPerformed(event);
		}
	}

	@Override
	protected boolean checkValidity() {
		try {
			indexAnswer = Integer.parseInt(MToolKit.replaceWhiteSpaces(intText
					.getText().replaceAll(",", "").replaceAll("\\.", "")));
			if (indexAnswer >= min) {
				if (indexAnswer <= max) {
					okBtn.setEnabled(true);
					return true;
				}
				if (!permissiveMax) {
					wizardInfo.resetWarning(LanguageManager
							.getString("wiz_input-number.no-strict-bounds"));
					okBtn.setEnabled(true);
					return true;
				}
			}
		} catch (NumberFormatException e) {
			//
		}
		wizardInfo.resetError(LanguageManager
				.getString("wiz_input-number.strict-bounds"));
		okBtn.setEnabled(false);
		return false;
	}

	/**
	 * The text field containing the integer answer
	 */
	private JTextField intText;

	/**
	 * The default value
	 */
	private final int defaultValue;

	/**
	 * The minimal accepted value.
	 */
	private final int min;

	/**
	 * The maximal accepted value.
	 */
	private final int max;

	/**
	 * If true, the max value is only an advise. Otherwise, a greater value can be
	 * specified.
	 */
	private boolean permissiveMax;
}
