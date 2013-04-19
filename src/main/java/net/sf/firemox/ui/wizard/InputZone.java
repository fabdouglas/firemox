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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.PropertyModel;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.zone.ZoneManager;

/**
 * A simple combobox displaying the available zones.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.92
 */
public class InputZone extends Ok {

	/**
	 * Creates a new instance of InputZone <br>
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
	 * @param allowedZones
	 *          the set of allowed zone id.
	 */
	public InputZone(Ability ability, String description, int[] allowedZones) {
		super(ability, LanguageManager.getString("wiz_input-zone.title"),
				LanguageManager.getString("wiz_input-zone.description") + description,
				"wiz_input-zone.gif", null, 250, 200);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		ZoneManager zoneManager = StackManager.PLAYERS[0].zoneManager;
		zoneBox = new JComboBox();
		for (int idZone : allowedZones) {
			PropertyModel pair = new PropertyModel(zoneManager.getContainer(idZone)
					.toString(), idZone);
			zoneBox.addItem(pair);
		}
		gameParamPanel.add(new JPanel());

		JPanel res = new JPanel();
		res.setLayout(new BoxLayout(res, BoxLayout.X_AXIS));
		res
				.setMaximumSize(new Dimension(UNBOUNDED_TXT_SIZE, MAXIMAL_TXT_SIZE + 10));
		JLabel label = new JLabel(LanguageManager.getString("wiz_input-zone.zone"));
		label.setMaximumSize(new Dimension(MAXIMAL_TXT_SIZE, MAXIMAL_TXT_SIZE));
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setVerticalAlignment(SwingConstants.BOTTOM);
		res.add(label);
		zoneBox.setMaximumSize(new Dimension(UNBOUNDED_TXT_SIZE, MAXIMAL_TXT_SIZE));
		gameParamPanel.add(res);
		gameParamPanel.add(zoneBox);
		gameParamPanel.add(new JPanel());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == cancelBtn) {
			indexAnswer = ((PropertyModel) zoneBox.getSelectedItem()).id;
			validAnswer(JOptionPane.OK_OPTION);
		}
	}

	private final JComboBox zoneBox;

}
