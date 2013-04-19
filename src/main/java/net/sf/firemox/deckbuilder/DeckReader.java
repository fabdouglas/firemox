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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.database.DatabaseCard;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.MInputStream;
import net.sf.firemox.network.NetworkActor;
import net.sf.firemox.token.IdPositions;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MCardCompare;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.component.LoaderConsole;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.zone.MZone;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class DeckReader {

	/**
	 * Create a new instance of this class.
	 */
	private DeckReader() {
		// nothing to do
	}

	private static final int DELIM_COMMA = 1;

	private static final int DELIM_SPACE2 = 2;

	/**
	 * Return the deck component corresponding to the given stream.
	 * 
	 * @param deckStream
	 *          the deck stream.
	 * @return the deck component corresponding to the given stream.
	 */
	public static Deck getDeck(MInputStream deckStream) {
		final Deck deck = new Deck("unknown");
		String line = null;

		// list the referenced cards
		line = deckStream.readLine();
		while (line != null && !"%EOF%".equals(line)) {
			// is it a valid line?
			if (line.length() != 0 && !line.startsWith("#")) {

				// To manage the "card_name;dd" format
				int index = line.indexOf("  ");
				int format = 0;
				if (index == -1) {
					// To manage the "dd card_name" format
					index = line.indexOf(";");
					format = DELIM_COMMA;
				} else {
					format = DELIM_SPACE2;
				}
				if (index != -1) {
					String croppedLine = (line + " ").substring(index + 1).trim();
					String cardName;
					int nextIndex = croppedLine.indexOf(";");
					int nb = 0;
					switch (format) {
					case DELIM_COMMA:
						cardName = line.substring(0, index).trim();
						if (nextIndex == -1) {
							nb = Integer.parseInt(croppedLine);
						} else {
							nb = Integer.parseInt(croppedLine.substring(0, nextIndex).trim());
						}
						break;
					case DELIM_SPACE2:
						nb = Integer.parseInt(line.substring(0, index).trim());
						if (nextIndex == -1) {
							cardName = croppedLine;
						} else {
							cardName = croppedLine.substring(0, nextIndex);
						}
						break;
					default:
						throw new InternalError("Unknown delimiter : '" + format + "'");
					}
					try {
						Map<String, String> properties = null;
						if (nextIndex != -1) {
							// constraints parsing
							properties = new HashMap<String, String>(5);
							String[] propertiesArray = croppedLine.substring(nextIndex + 1)
									.trim().split(";");
							for (String property : propertiesArray) {
								property = property.trim();
								if (propertiesArray.length > 0) {
									int indexOfEqual = property.indexOf("=");
									if (indexOfEqual != -1) {
										properties.put(property.substring(0, indexOfEqual),
												property.substring(indexOfEqual + 1));
									} else {
										JOptionPane.showMessageDialog(MagicUIComponents.magicForm,
												LanguageManager.getString("malformeddeck", line
														.substring(0, index)), LanguageManager
														.getString("error"), JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						}
						deck.addCard(cardName, nb, properties);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(MagicUIComponents.magicForm,
								LanguageManager.getString("malformeddeck", line.substring(0,
										index)), LanguageManager.getString("error"),
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			line = deckStream.readLine();
		}
		return deck;
	}

	/**
	 * Return the deck component corresponding to the given file.
	 * 
	 * @param parentComponent
	 *          determines the <code>Frame</code> in which the dialog is
	 *          displayed; if <code>null</code>, or if the
	 *          <code>parentComponent</code> has no <code>Frame</code>, a
	 *          default <code>Frame</code> is used
	 * @param deckFile
	 *          the deck file.
	 * @return the deck component corresponding to the given file.
	 */
	public static Deck getDeck(Component parentComponent, String deckFile) {
		if (deckFile.length() == 0) {
			if (parentComponent != null) {
				JOptionPane.showMessageDialog(parentComponent, LanguageManager
						.getString("wiz_network.deck.missed"), LanguageManager
						.getString("error"), JOptionPane.WARNING_MESSAGE);
			}
			return null;
		}
		final String deckShortFile = MToolKit.getShortDeckFile(deckFile);
		final InputStream deckStream = MToolKit.getResourceAsStream(deckShortFile);
		if (deckStream == null) {
			JOptionPane.showMessageDialog(parentComponent, LanguageManager
					.getString("wiz_network.deck.notfound"), LanguageManager
					.getString("error"), JOptionPane.WARNING_MESSAGE);
			throw new RuntimeException("Could not open deck file '" + deckShortFile
					+ "'");
		}
		return getDeck(new MInputStream(deckStream, null));
	}

	/**
	 * Fill player's zone with the cards found in InputFile
	 * 
	 * @param deck
	 *          the deck to add.
	 * @param dbFile
	 *          opened file read containing the available cards
	 * @param zone
	 *          the destination zone.
	 * @param owner
	 *          the player owning the created cards.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static void fillZone(Deck deck, FileInputStream dbFile, MZone zone,
			Player owner) throws IOException {
		// while not EOF
		LoaderConsole
				.beginTask(
						LanguageManager.getString("wiz_network.validatingdeck"),
						((ConnectionManager.client == null && !owner.isYou()) || (ConnectionManager.client != null && owner
								.isYou())) ? 35 : 65);
		owner.setDeck(deck);

		// Load cards rules
		LoaderConsole
				.beginTask(
						LanguageManager.getString("wiz_network.loadingcards"),
						((ConnectionManager.client == null && !owner.isYou()) || (ConnectionManager.client != null && owner
								.isYou())) ? 45 : 75);
		MCard lastLoadedCard = null;
		for (MCardCompare cardCompare : deck.getCards()) {
			final String realCardName = cardCompare.getName();
			final int nbCards = cardCompare.getAmount();
			Log.debug(cardCompare);
			MCard cardRef = null;
			if (lastLoadedCard != null
					&& realCardName.equals(lastLoadedCard.getName())) {
				DatabaseCard database = DatabaseFactory.getDatabase(null,
						lastLoadedCard.getCardModel(), cardCompare.getConstraints());
				cardRef = new MCard(lastLoadedCard, database);
			} else {
				dbFile.getChannel().position(cardCompare.getMdbOffset());
				cardRef = new MCard(realCardName, dbFile, owner, owner, cardCompare
						.getConstraints());
				lastLoadedCard = cardRef;
			}
			cardRef.moveCard(zone.getZoneId(), owner, false, IdPositions.ON_THE_TOP);

			// clone the same card
			for (int a = nbCards; a-- > 1;) {
				new MCard(cardRef, cardRef.getDatabase()).moveCard(zone.getZoneId(),
						owner, false, IdPositions.ON_THE_TOP);
			}
		}

	}

	/**
	 * Validate a deck read from the given stream.
	 * 
	 * @param parentComponent
	 *          determines the <code>Frame</code> in which the dialog is
	 *          displayed; if <code>null</code>, or if the
	 *          <code>parentComponent</code> has no <code>Frame</code>, a
	 *          default <code>Frame</code> is used
	 * @param deck
	 * @param deckConstraint
	 *          the deck constraint name.
	 * @return <code>true</code> if the read deck is valid.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static boolean validateDeck(Component parentComponent, Deck deck,
			String deckConstraint) throws IOException {
		if (deckConstraint == null) {
			return validateDeck(parentComponent, deck, (DeckConstraint) null);
		}
		return validateDeck(parentComponent, deck, DeckConstraints
				.getDeckConstraint(deckConstraint));
	}

	/**
	 * Validate a deck read from the given stream.
	 * 
	 * @param parentComponent
	 *          determines the <code>Frame</code> in which the dialog is
	 *          displayed; if <code>null</code>, or if the
	 *          <code>parentComponent</code> has no <code>Frame</code>, a
	 *          default <code>Frame</code> is used
	 * @param deck
	 * @param deckConstraint
	 *          the deck constraint name.
	 * @return <code>true</code> if the read deck is valid.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static boolean validateDeck(Component parentComponent, Deck deck,
			DeckConstraint deckConstraint) throws IOException {
		final List<String> errors = deck.validate(deckConstraint);
		if (!errors.isEmpty()) {
			NetworkActor.cancelling = true;
			final String errorContent;
			if (deckConstraint == null) {
				errorContent = errors.get(0);
			} else {
				errorContent = LanguageManager.getString(
						"wiz_network.deck.constraint.error", deckConstraint
								.getConstraintLocalName())
						+ " :\n" + errors.toString();
			}
			JOptionPane.showMessageDialog(null, errorContent, LanguageManager
					.getString("wiz_network.deck.constraint.error.title"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}
