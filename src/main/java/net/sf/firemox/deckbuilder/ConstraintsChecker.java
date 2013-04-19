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
package net.sf.firemox.deckbuilder;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * A container of a set of constraints. Constraints are validated each time the
 * deck is updated.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public class ConstraintsChecker extends JPanel {

	private static final String IMAGE_CHECK = "constraint_check.gif";

	private static final String IMAGE_FAIL = "constraint_fail.gif";

	private final List<DeckConstraint> constraints;

	private final ImageIcon iconFail;

	private final ImageIcon iconCheck;

	/**
	 * The deck attached to this checker.
	 */
	private Deck deck;

	/**
	 * Create a new instance of this class.
	 */
	public ConstraintsChecker() {
		super();
		constraints = new ArrayList<DeckConstraint>();
		constraints.addAll(DeckConstraints.getDeckConstraints());
		iconFail = new ImageIcon(MToolKit.getIconPath(IMAGE_FAIL));
		iconCheck = new ImageIcon(MToolKit.getIconPath(IMAGE_CHECK));
		Collections.sort(constraints);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		for (DeckConstraint constraint : constraints) {
			final JButton checker = new JButton(constraint.getConstraintLocalName());
			checker.setAlignmentY(Component.LEFT_ALIGNMENT);
			checker.setHorizontalAlignment(SwingConstants.LEFT);
			checker.setMinimumSize(new Dimension(190, 20));
			checker.setMaximumSize(new Dimension(2000, 20));
			checker.setBorder(null);
			checker.setFocusPainted(false);
			add(checker);
		}
	}

	/**
	 * Return the checker at the specified index.
	 * 
	 * @return the checker at the specified index.
	 */
	private JButton getChecker(int index) {
		return (JButton) getComponent(index);
	}

	/**
	 * Set the deck attached to this checker.
	 * 
	 * @param deck
	 *          the deck attached to this checker.
	 */
	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	/**
	 * Update the constraint checkers.
	 */
	public void updateCheckers() {
		for (int index = 0; index < getComponentCount(); index++) {
			final JButton checker = getChecker(index);
			final DeckConstraint constraint = constraints.get(index);
			final List<String> errors = constraint.validate(deck);
			if (errors.isEmpty()) {
				checker.setIcon(iconCheck);
				checker.setToolTipText("<html>"
						+ LanguageManager.getString("db_constraint_check"));
			} else {
				checker.setIcon(iconFail);
				StringBuilder errorsString = new StringBuilder();
				for (String error : errors) {
					if (errorsString.length() != 0) {
						errorsString.append("<br>");
					}
					errorsString.append(error);
				}
				checker.setToolTipText("<html>"
						+ LanguageManager.getString("db_constraint_fail", errorsString
								.toString()));
			}
		}
	}
}
