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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MCardCompare;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class Deck {

	/**
	 * The last loaded card name
	 */
	private static String lastCardName = null;

	/**
	 * Is this deck has been validated.
	 */
	private boolean isValidated;

	/**
	 * The offset corresponding to the last card read.
	 */
	private static long lastCardPosition = 0L;

	/**
	 * The current instance of deck. Used by some test such as
	 * {@link net.sf.firemox.expression.DeckCounter}. <br>
	 * This field should be removed to be added as parameter of this kind of test.
	 * 
	 * @see #validate(String)
	 */
	public static Deck currentDeck;

	/**
	 * The deck name
	 */
	private final String deckName;

	/**
	 * The cards of this deck.
	 */
	private final List<MCardCompare> cards;

	/**
	 * The additional properties of this deck.
	 */
	private Map<String, Object> properties;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param deckName
	 *          The deck name.
	 */
	public Deck(String deckName) {
		this.deckName = deckName;
		this.cards = new ArrayList<MCardCompare>();
		this.properties = new HashMap<String, Object>();
	}

	/**
	 * Return the cards of this deck.
	 * 
	 * @return the cards of this deck.
	 */
	public List<MCardCompare> getCards() {
		return cards;
	}

	/**
	 * Return a property value of this deck.
	 * 
	 * @param propertyName
	 *          the property name.
	 * @return a property. Null if the property does not exist.
	 */
	public Object getProperty(String propertyName) {
		if (properties == null) {
			return null;
		}
		return properties.get(propertyName);
	}

	/**
	 * Add any number of cards to this deck.
	 * 
	 * @param cardName
	 *          the card name.
	 * @param amount
	 *          the amount of cards to add to this deck.
	 * @param properties
	 *          the optional properties attached to these cards.
	 */
	public void addCard(String cardName, int amount,
			Map<String, String> properties) {
		if (this.properties == null && properties == null) {
			for (MCardCompare card : cards) {
				if (card.getName().equals(cardName)) {
					card.add(amount);
					return;
				}
			}
		}
		cards.add(new MCardCompare(cardName, amount, properties, 0L));
	}

	/**
	 * Add any number of cards to this deck.
	 * 
	 * @param card
	 *          the card specification.
	 */
	public void addCard(MCardCompare card) {
		cards.add(card);
	}

	/**
	 * Validate this deck.
	 * 
	 * @param deckConstraint
	 *          the deck constraint name.
	 * @return list of validation error. Validation is ok when this list is empty.
	 * @throws IOException
	 *           if input exception occurred.
	 */
	public List<String> validate(DeckConstraint deckConstraint)
			throws IOException {
		Log.info("Validating deck...");
		final List<String> errors = new ArrayList<String>();
		final FileInputStream dbStream = MdbLoader.resetMdb();

		// sort the cards A -> Z
		Collections.sort(cards);

		// Resolve real card names
		List<MCardCompare> cardErrors = new ArrayList<MCardCompare>(cards.size());
		for (MCardCompare card : cards) {
			if (null == updateRealCardName(card, dbStream)) {
				// This card doesn't exist
				cardErrors.add(card);
			}
		}
		if (!cardErrors.isEmpty()) {
			cards.removeAll(cardErrors);
			StringBuilder builderError = new StringBuilder();
			for (MCardCompare card : cardErrors) {
				if (builderError.length() != 0) {
					builderError.append(", \n ");
				}
				builderError.append(card.getName());
			}
			errors.add(LanguageManager.getString("notyetimplemented", builderError
					.toString()));
		}

		// Now, validate the deck against the given constraint
		this.deckConstraint = deckConstraint;
		if (deckConstraint != null) {
			errors.addAll(deckConstraint.validate(this));
		}
		isValidated = errors.isEmpty();
		return errors;
	}

	/**
	 * Validate this deck.
	 * 
	 * @param deckConstraint
	 *          the deck constraint name.
	 * @return list of validation error. Validation is ok when this list is empty.
	 * @throws IOException
	 *           if input exception occurred.
	 */
	public List<String> validate(String deckConstraint) throws IOException {
		if (deckConstraint != null) {
			return validate(DeckConstraints.getDeckConstraint(deckConstraint));
		}
		return validate((DeckConstraint) null);
	}

	/**
	 * Set the current offset of the specified stream, to the first byte of the
	 * specified card and return the real card name.
	 * 
	 * @param card
	 *          the card configuration.
	 * @param dbStream
	 *          opened file read containing the available cards
	 * @return the real card name if exist,null if card has not been found.
	 * @throws IOException
	 *           if input exception occurred.
	 */
	private static String updateRealCardName(MCardCompare card,
			FileInputStream dbStream) throws IOException {
		/*
		 * search this card now in the data base until we found it or we or greater
		 * name was found.
		 */
		String cardName = card.getName();
		String keyName = MToolKit.getKeyName(cardName);
		final long currentOffset = dbStream.getChannel().position();
		if (lastCardName != null
				&& MToolKit.getKeyName(lastCardName).compareTo(keyName) == 0) {
			dbStream.getChannel().position(lastCardPosition);
			card.setName(lastCardName);
			card.setMdbOffset(MToolKit.readInt24(dbStream));
			return lastCardName;
		}
		if (lastCardName == null
				|| MToolKit.getKeyName(lastCardName).compareTo(keyName) > 0) {
			MdbLoader.resetMdb();
		}
		while (true) {
			final String realCard = MToolKit.readString(dbStream);
			final int result = MToolKit.getKeyName(realCard).compareTo(keyName);
			if (result == 0) {
				// card is found
				lastCardName = realCard;
				lastCardPosition = dbStream.getChannel().position();
				card.setName(realCard);
				card.setMdbOffset(MToolKit.readInt24(dbStream));
				return realCard;
			} else if (result > 0) {
				// card doesn't exist, return the over read bytes
				dbStream.getChannel().position(currentOffset);
				return null;
			} else {
				dbStream.getChannel().position(dbStream.getChannel().position() + 3);
			}
		}
	}

	@Override
	public String toString() {
		return deckName;
	}

	/**
	 * Return the deck constraint applied to this deck.May be <code>null</code>.
	 * 
	 * @return the deck constraint applied to this deck.May be <code>null</code>.
	 */
	public DeckConstraint getConstraint() {
		return deckConstraint;
	}

	/**
	 * The deck constraint applied to this deck.May be <code>null</code>.
	 */
	private DeckConstraint deckConstraint;

	/**
	 * Write the cards of this deck into the given output stream.
	 * 
	 * @param out
	 *          the output stream where this deck will be written.
	 */
	public void send(OutputStream out) {
		for (MCardCompare card : cards) {
			MToolKit.writeString(out, card.toString());
		}
	}

	/**
	 * Return available stream of deck. Not <code>null</code> only when
	 * validating a deck. The returned stream corresponds to the first offset of
	 * first card.
	 * 
	 * @return Available stream of deck. Not <code>null</code> only when
	 *         validating a deck.
	 */
	public FileInputStream getMdbStream() {
		return MdbLoader.resetMdb();
	}

	/**
	 * Is this deck has been validated.
	 * 
	 * @return <code>true</code> if this deck has been validated.
	 */
	public boolean isValidated() {
		return isValidated;
	}

	/**
	 * @param card
	 */
	public void removeCard(MCardCompare card) {
		getCards().remove(card);
	}
}
