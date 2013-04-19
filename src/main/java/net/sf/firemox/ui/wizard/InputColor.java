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
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class InputColor extends Ok {

	/**
	 * Creates a new instance of this class <br>
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
	 * @param allowedColors
	 *          the bit set of allowed colors.
	 * @param allowColorless
	 *          Is colorless mana available.
	 * @param multiselect
	 *          Is multi selection allowed.
	 */
	public InputColor(Ability ability, String description, int allowedColors,
			boolean allowColorless, boolean multiselect) {
		super(ability, LanguageManager.getString("wiz_input-color.title"),
				LanguageManager.getString("wiz_input-color.description") + description,
				"wiz_input-color.gif", null, 250, 400);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.multiselect = multiselect;
		gameParamPanel.setLayout(null);
		/**
		 * Layout : <br>
		 * ______________ <br>
		 * |.............| <br>
		 * |......@......| <br>
		 * |.............| <br>
		 * |.@....C....@.| <br>
		 * |.............| <br>
		 * |...@.....@...| <br>
		 * |.............| <br>
		 * |_____________| <br>
		 * PS : keep javadoc aspect of this comment.
		 */
		final ButtonGroup group = new ButtonGroup();
		double rot = -2.48 * Math.PI / (IdCommonToken.COLOR_NAMES.length - 1);
		gameParamPanel.setBackground(Color.BLACK);
		indexAnswer = -1;
		for (int i = IdCommonToken.COLOR_NAMES.length; i-- > 1;) {
			final AbstractButton button;
			if (multiselect) {
				button = new JToggleButton(MToolKit.getTbsBigManaPicture(i));
			} else {
				button = new JRadioButton(MToolKit.getTbsBigManaPicture(i));
			}
			rot += 2 * Math.PI / (IdCommonToken.COLOR_NAMES.length - 1);
			if (((allowedColors >> (i - 1)) & 1) != 0) {
				if (!multiselect) {
					group.add(button);
				}
				button.setBorder(new EtchedBorder());
				button.setSize(new Dimension(50, 50));
				button.setPreferredSize(new Dimension(50, 50));
				button.setHorizontalAlignment(SwingConstants.CENTER);
				button.addActionListener(this);
				button.setBackground(Color.BLACK);
				button.setSelectedIcon(button.getIcon());
				button.setActionCommand(String.valueOf(i));
				button.setIconTextGap(0);
				gameParamPanel.add(button);
				button.setLocation((int) (CENTER_X + RADIUS * Math.cos(rot)),
						(int) (CENTER_Y + RADIUS * Math.sin(rot)));
			}
		}
		if (allowColorless) {
			final JRadioButton button = new JRadioButton(MToolKit
					.getTbsBigManaPicture(0));
			if (!multiselect) {
				group.add(button);
			}
			button.setBorder(new EtchedBorder());
			button.setSize(new Dimension(50, 50));
			button.setPreferredSize(new Dimension(50, 50));
			button.setHorizontalAlignment(SwingConstants.CENTER);
			button.addActionListener(this);
			button.setBackground(Color.BLACK);
			button.setSelectedIcon(button.getIcon());
			button.setActionCommand("0");
			button.setIconTextGap(0);
			gameParamPanel.add(button);
			button.setLocation(CENTER_X - 25, CENTER_Y - 25);
		}
		wizardInfo.resetError(LanguageManager
				.getString("wiz_input-color.noselection"));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == cancelBtn) {
			// we can exit only if a color has been selected
			if (indexAnswer != -1) {
				validAnswer(JOptionPane.OK_OPTION);
			}
		} else {
			final String command = event.getActionCommand();
			if (command != null && command.length() == 1) {
				int indexAnswer = getCodeFromIndex(Integer.parseInt(command));
				if (multiselect) {
					currentButton = (AbstractButton) event.getSource();
					if (((AbstractButton) event.getSource()).isSelected()) {
						if (Wizard.indexAnswer == -1)
							Wizard.indexAnswer = indexAnswer;
						else
							Wizard.indexAnswer |= indexAnswer;
						currentButton.setBackground(Color.RED);
						currentButton.setBorderPainted(true);
						wizardInfo.reset();
					} else {
						if (Wizard.indexAnswer != -1)
							Wizard.indexAnswer &= ~indexAnswer;
						if (Wizard.indexAnswer == 0) {
							Wizard.indexAnswer = -1;
							wizardInfo.resetError(LanguageManager
									.getString("wiz_input-color.noselection"));
						}
						currentButton.setBackground(Color.BLACK);
						currentButton.setBorderPainted(false);
					}
				} else {
					if (currentButton != null) {
						currentButton.setBackground(Color.BLACK);
						currentButton.setBorderPainted(false);
					}
					currentButton = (AbstractButton) event.getSource();
					currentButton.setBackground(Color.RED);
					currentButton.setBorderPainted(true);
					Wizard.indexAnswer = indexAnswer;
					wizardInfo.reset();
				}
			} else {
				super.actionPerformed(event);
			}
		}
	}

	private static int getCodeFromIndex(int index) {
		if (index == 0)
			return 0;
		return 1 << index - 1;
	}

	private AbstractButton currentButton;

	private static final int CENTER_X = 100;

	private static final int CENTER_Y = 100;

	private static final int RADIUS = 75;

	private boolean multiselect;

}
