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
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
public class Choice extends YesNo implements ListSelectionListener {

	/**
	 * Creates a new instance of Choice <br>
	 * 
	 * @param ability
	 *          ability to associate to this ability. If this ability has an
	 *          associated picture, it will be used instead of given picture.
	 *          Ability's name is also used to fill the title. This ability will
	 *          be used to restart this wizard in case of Background button is
	 *          used.
	 * @param allowCancel
	 *          Is the cancel button is allowed.
	 * @param actions
	 *          set of available choices?
	 */
	public Choice(Ability ability, boolean allowCancel, List<String> actions) {
		super(ability, LanguageManager.getString("wiz_choice.title"),
				LanguageManager.getString("wiz_choice.description")
						+ ability.getAbilityTitle(), "wiz_choice.gif", LanguageManager
						.getString("ok"), LanguageManager.getString("cancel"), 500, 300);
		this.actions = actions;

		// Fill the combobox of actions
		final DefaultListModel model = new DefaultListModel();
		actionList = new JList(model);
		actionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		actionList.setLayoutOrientation(JList.VERTICAL);
		actionList.addListSelectionListener(this);
		for (String actionStr : actions) {
			if (actionStr != null) {
				model.addElement("<html>" + actionStr);
			}
		}
		if (model.isEmpty()) {
			// cancel button is disabled but no possible choice !?
			indexAnswer = 0;
			optionAnswer = JOptionPane.NO_OPTION;
			return;
		}

		/*
		 * TODO Enable the auto choice? if (count == 1 &&
		 * Magic.makeChoiceAsPossible) { // cancel button is disabled and there is
		 * only one possibility for (int i = actions.size(); i-- > 0;) { if
		 * (actions.get(i) != null) { indexAnswer = i; } } optionAnswer =
		 * JOptionPane.YES_OPTION; return; }
		 */
		if (allowCancel) {
			// "cancel" button is enabled and is the default of "ESC" action
			setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		} else {
			// "cancel" button is disabled, window cannot be closed without choice
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			this.cancelBtn.setEnabled(false);
			this.okBtn.setEnabled(false);
		}

		final JScrollPane listScrollerLeft = new JScrollPane(actionList);
		listScrollerLeft.setPreferredSize(new Dimension(100, 280));
		gameParamPanel.add(listScrollerLeft);

		// TODO makeChoiceAsPossible = new JCheckBox(LanguageManager
		// .getString("makeChoiceAsPossible"));
		// makeChoiceAsPossible.setSelected(Magic.makeChoiceAsPossible);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible && actionList == null) {
			return;
		}
		super.setVisible(visible);
	}

	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			if (actionList.getSelectedIndex() == -1) {
				// No selection, disable fire button.
				okBtn.setEnabled(false);
			} else {
				// Selection, enable the fire button.
				okBtn.setEnabled(true);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == cancelBtn) {
			// set the static integer token to the
			indexAnswer = 0;
		} else if (event.getSource() == okBtn) {
			if (actionList.getSelectedValue() == null) {
				// no selected value TODO disable OK button while no selection
				return;
			}
			// set the static integer token
			indexAnswer = -1;
			for (int i = actions.size(); i-- > 0;) {
				if (actions.get(i).equals(
						actionList.getSelectedValue().toString().substring(
								"<html>".length()))) {
					indexAnswer = i;
					break;
				}
			}
			if (indexAnswer == -1) {
				throw new IllegalStateException(
						"Unable to find selected index in choice");
			}
			// TODO Magic.makeChoiceAsPossible = makeChoiceAsPossible.isSelected();
		}
		super.actionPerformed(event);
	}

	/**
	 * The list of available actions
	 */
	private final JList actionList;

	/**
	 * The actions list displayed in List GUI component. This array may contain
	 * <code>null</code> objects. These ones are ignored.
	 */
	private final List<String> actions;
}