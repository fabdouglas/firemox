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
 * 
 */

package net.sf.firemox.clickable.target.card;

import static net.sf.firemox.clickable.target.card.CardFactory.ACTIVATED_COLOR;
import static net.sf.firemox.clickable.target.card.CardFactory.WARNING_PICTURE;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import net.sf.firemox.action.MoveCard;
import net.sf.firemox.action.listener.WaitingAbility;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.ActivatedAbility;
import net.sf.firemox.clickable.ability.ReplacementAbility;
import net.sf.firemox.clickable.ability.UserAbility;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.TargetFactory;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.database.DatabaseCard;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.deckbuilder.DeckConstraints;
import net.sf.firemox.event.ModifiedIdCard;
import net.sf.firemox.event.ModifiedIdColor;
import net.sf.firemox.event.ModifiedProperty;
import net.sf.firemox.event.ModifiedRegister;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.modifier.AbilityModifier;
import net.sf.firemox.modifier.ColorModifier;
import net.sf.firemox.modifier.ControllerModifier;
import net.sf.firemox.modifier.IdCardModifier;
import net.sf.firemox.modifier.PlayableZoneModifier;
import net.sf.firemox.modifier.PropertyModifier;
import net.sf.firemox.modifier.RegisterIndirection;
import net.sf.firemox.modifier.RegisterModifier;
import net.sf.firemox.modifier.model.ModifierModel;
import net.sf.firemox.modifier.model.ObjectFactory;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.operation.Add;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.operation.Set;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdPositions;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.Pair;
import net.sf.firemox.ui.Tappable;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.layout.AttachmentLayout;
import net.sf.firemox.zone.ExpandableZone;
import net.sf.firemox.zone.MZone;
import net.sf.firemox.zone.ZoneManager;

import org.apache.commons.lang.ArrayUtils;

/**
 * This class corresponds to the graphical element of a specified card, and
 * corresponds to an actor. When loading a new card, first we take care that a
 * card with same name has not been already loaded in order to save memory, so
 * all cards having the same name share the same Image object. <br>
 * 
 * @since 0.52 "enableReverse" option
 * @since 0.70 support modifiers
 * @since 0.71 art and rule author
 * @since 0.72 support static effects, removed art-author
 * @since 0.85 property pictures are displayed
 * @since 0.90 database + card model
 * @since 0.91 keywords added, externalized to CardModel, explicit attachment
 *        object, and is movable.
 * @see net.sf.firemox.token.IdCardColors
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @author <a href="mailto:kismet-sl@users.sourceforge.net">Stefano "Kismet"
 *         Lenzi</a>
 */
public class MCard extends AbstractCard implements Tappable, MouseWheelListener {

	/**
	 * Create a new instance of Card reading from a file.
	 * 
	 * @param cardName
	 *          the card name
	 * @param inputFile
	 *          is the file read, containing information
	 * @param controller
	 *          is the controller of this card
	 * @param owner
	 *          is the owner of this card
	 * @param constraints
	 *          the constraints of the card for database management
	 */
	public MCard(String cardName, InputStream inputFile, Player controller,
			Player owner, Map<String, String> constraints) {
		super();
		// Read card name
		initUI(null, CardFactory.getCardModel(cardName, inputFile), constraints);
		initModel();
		this.copiedCard = null;
		this.owner = owner;
		this.controller = controller;
		this.originalController = controller;
		lastKnownInstances = new HashMap<Integer, LastKnownCardInfo>();
	}

	/**
	 * 
	 */
	private void initModel() {
		final CardModel cardModel = getCardModel();
		// Cache registers
		registers = cardModel.getStaticRegisters().clone();
		cachedRegisters = registers.clone();

		// Cache card type, coded with 2 byte
		cachedIdCard = cardModel.getIdCard();

		// Cache card color, coded with one byte
		cachedIdColor = cardModel.getIdColor();

		// Cache list of abilities
		cachedAbilities = new ArrayList<Ability>(cardModel.getAbilities().length);
		for (Ability ability : cardModel.getAbilities()) {
			cachedAbilities.add(ability.clone(this));
		}

		// Cache list of properties, i.e. first strike,trample,Legend,Knight,...
		cachedProperties = new HashSet<Integer>();
		for (int property : cardModel.getProperties()) {
			cachedProperties.add(property);
		}

		// build modifier from models
		indirections = new RegisterIndirection[IdTokens.CARD_REGISTER_SIZE];
		registerModifiers = new RegisterModifier[IdTokens.CARD_REGISTER_SIZE];
	}

	/**
	 * Create a new instance of Card exactly like cardRef instance. Is called only
	 * from clone method.
	 * 
	 * @param cardRef
	 *          is the model for this new instance
	 * @param database
	 *          the database of this new card.
	 */
	public MCard(MCard cardRef, DatabaseCard database) {
		// Database is shared.
		this.database = database;
		this.copiedCard = null;

		// Initialize UI of this new Card
		initUI(null, database.getCardModel(), null);
		initModel();
		lastKnownInstances = new HashMap<Integer, LastKnownCardInfo>();

		if (cardRef != null) {
			controller = cardRef.controller;
			owner = cardRef.owner;
		}
		originalController = controller;
	}

	/**
	 * Create a new instance of Card tokenized. No field is initialized.
	 */

	protected MCard() {
		super();
		cachedAbilities = null;
		database = null;
		copiedCard = null;
		originalController = null;
	}

	/**
	 * Create a new instance of tokenized Card. This constructor should be called
	 * only to obtain the a copy of the specified card sharing registers and
	 * abilities. Also, any modification done on registers or ability of one of
	 * these cards will be visible on the other. The zone information of cards is
	 * not shared, and the new card will have it's zone initialized with stack
	 * identifier instead of side. The picture used for this card will be the
	 * specified one if it is not null. Otherwise the cardRef picture will be
	 * used.
	 * 
	 * @param pictureName
	 *          is the picture name used to represent the copy. May be null.
	 * @param cardRef
	 *          is the model for this instance
	 * @see IdZones#STACK
	 */
	public MCard(String pictureName, MCard cardRef) {
		// Database is shared.
		database = cardRef.database;

		// Initialize UI of this new Card
		initUI(pictureName, cardRef.database.getCardModel(), null);

		this.idZone = IdZones.STACK;
		this.registers = cardRef.registers;
		this.cachedRegisters = cardRef.cachedRegisters;
		this.cachedProperties = cardRef.cachedProperties;
		this.cachedAbilities = null;
		this.copiedCard = cardRef;
		this.cachedIdCard = cardRef.cachedIdCard;
		this.cachedIdColor = cardRef.cachedIdColor;
		this.indirections = new RegisterIndirection[IdTokens.CARD_REGISTER_SIZE];
		this.registerModifiers = new RegisterModifier[IdTokens.CARD_REGISTER_SIZE];
		this.controller = cardRef.controller;
		this.originalController = controller;
		this.owner = cardRef.owner;
		tokenize();
	}

	@Override
	public boolean isAbility(int abilityType) {
		return false;
	}

	@Override
	public boolean isSpell() {
		return true;
	}

	/**
	 * Indicates whether this card suits to the specified position code.
	 * 
	 * @param position
	 *          the matching position code
	 * @return true if this card suits to the specified position code.
	 * @see net.sf.firemox.token.IdPositions#ON_THE_BOTTOM
	 * @see net.sf.firemox.token.IdPositions#ON_THE_TOP
	 */
	public boolean isInPosition(int position) {
		return getContainer().isInPosition(this, position);
	}

	/**
	 * Indicates whether this card suits to the specified min and max position codes.
	 * 
	 * @param min
	 *          the matching minimum position code
	 * @param max
	 *          the matching maximum position code
	 * @return true if this card suits to the specified position codes.
	 * @see net.sf.firemox.token.IdPositions#ON_THE_BOTTOM
	 * @see net.sf.firemox.token.IdPositions#ON_THE_TOP
	 */
	public boolean isInPosition(int min, int max) {
		return getContainer().isInPosition(this, min, max);
	}

	/**
	 * Return the container of this card.
	 * 
	 * @return the container of this card.
	 */
	public MZone getContainer() {
		return controller.zoneManager.getContainer(getIdZone());
	}

	@Override
	public boolean isACopy() {
		return copiedCard != null;
	}

	/**
	 * Indicates if the specified sort of card idCard contains the card's sort.
	 * 
	 * @param idCard
	 *          are the sorts we match.
	 * @return true if the specified sort idCard contains the card's sort.
	 * @see #hasIdCard(int,int)
	 */
	public boolean hasIdCard(int idCard) {
		return hasIdCard(getIdCard(), idCard);
	}

	/**
	 * Indicates if idCardBIG contains completely idCardMatched. <br>
	 * 
	 * @param idCardBIG
	 *          are the set of types.
	 * @param idCardMatched
	 *          are the type we match.
	 * @return true if idCardBIG contains completely idCardMatched. <br>
	 */
	public static boolean hasIdCard(int idCardBIG, int idCardMatched) {
		return (idCardBIG & idCardMatched) == idCardMatched;
	}

	/**
	 * Indicates if idCardBIG contains partially idCardMatched. <br>
	 * 
	 * @param idCardBIG
	 *          are the set of types.
	 * @param idCardMatched
	 *          are the type we match.
	 * @return true if idCardBIG contains partially idCardMatched. <br>
	 */
	public static boolean intersectionIdCard(int idCardBIG, int idCardMatched) {
		return (idCardBIG & idCardMatched) != 0;
	}

	/**
	 * Indicates if propertyBIG contains completely propertyMatched. <br>
	 * 
	 * @param propertyBIG
	 *          are the set of properties.
	 * @param propertyMatched
	 *          is the property we match.
	 * @return true if propertyBIG contains completely propertyMatched. <br>
	 */
	public static boolean hasProperty(int[] propertyBIG, int propertyMatched) {
		return ArrayUtils.contains(propertyBIG, propertyMatched);
	}

	/**
	 * Indicates if the specified color idColor contains the card's colors. <br>
	 * 
	 * @param idColor
	 *          are the colors we match.
	 * @return true if the specified color idColor contains the card's colors.
	 * @see #hasIdColor(int,int)
	 */
	public boolean hasIdColor(int idColor) {
		return hasIdColor(getIdColor(), idColor);
	}

	/**
	 * Indicates if the specified color idColorBIG contains the other specified
	 * color idColorMatched. <br>
	 * 
	 * @param idColorBIG
	 *          are the colors that idColorMatched must have
	 * @param idColorMatched
	 *          are the colors we match.
	 * @return true if the specified color idColorBIG contains the other specified
	 *         color idColorMatched.
	 */
	public static boolean hasIdColor(int idColorBIG, int idColorMatched) {
		return (idColorBIG & idColorMatched) == idColorMatched;
	}

	/**
	 * <ul>
	 * Indicates if this card match with the specified place and constraint. As
	 * the specified place is an integer, it may contain another information about
	 * constraint in the high bits : xxyy
	 * <li>xx represents the constraint, may be : "must be untapped" or "must be
	 * tapped".
	 * <li>yy represents the zone identifier.
	 * </ul>
	 * The zones are compared, then the constraint tapped/untapped is checked.
	 * <br>
	 * If <code>zoneConstaint</code> is ID__ANYWHERE, return true. <br>
	 * If <code>zoneConstaint</code> is same as current zone of this card and
	 * the constraint is validated, return true <br>
	 * 
	 * @param zoneConstaint
	 *          the zone id and optional tapped information.
	 * @return true if <code>zoneConstaint</code> is ID__ANYWHERE or
	 *         <code>zoneConstaint</code> is same as current place of this card
	 *         and the constraint is verified.
	 * @see IdZones#PLAY_TAPPED
	 * @see IdZones#PLAY_UNTAPPED
	 * @see IdZones
	 */
	public boolean isSameState(int zoneConstaint) {
		switch (zoneConstaint) {
		case IdZones.PLAY_TAPPED:
			return idZone == IdZones.PLAY && tapped;
		case IdZones.PLAY_UNTAPPED:
			return idZone == IdZones.PLAY && !tapped;
		default:
			return idZone == zoneConstaint;
		}
	}

	/**
	 * Compare a zone with the current card'szone
	 * 
	 * @param idZone
	 *          the other zone
	 * @return true the specified zone, and the current card's zone are the same.
	 */
	public boolean isSameIdZone(int idZone) {
		return isSameIdZone(this.idZone, idZone);
	}

	/**
	 * @param idZone
	 *          the other zone
	 * @param other
	 *          another zone
	 * @return true the specified zones are the same.
	 */
	public static boolean isSameIdZone(int idZone, int other) {
		return getIdZone(idZone, null) == getIdZone(other, null);
	}

	/**
	 * Return the zone identifier of this card.
	 * 
	 * @see IdZones
	 * @return the zone identifier of this card. IdZones#PLAY, IdZones#HAND,...
	 * @see IdZones
	 */
	public int getIdZone() {
		return idZone;
	}

	/**
	 * Return the place where is this card.
	 * 
	 * @param idZone
	 *          the zone identifier This identifier may contain staus information
	 *          like "tapped", "untapped"
	 * @param context
	 *          The optional context attached to the request.
	 * @return the place identifier corresponding to the specified zone, and with
	 *         any status information
	 * @see IdZones
	 */
	public static int getIdZone(int idZone, ContextEventListener context) {
		if (idZone == IdZones.CONTEXT && context != null) {
			// TODO complete to support the AccessibleContext pattern
			return context.getZoneContext();
		}
		return idZone & 0x0F;
	}

	/**
	 * Return a zone code representing the zone and the state of this card.
	 * 
	 * @param idZone
	 *          the zone identifier without status information like "tapped",
	 *          "untapped"
	 * @param tapped
	 *          indicate the state of this car.
	 * @return the place identifier corresponding to the specified zone, and with
	 *         any status information
	 * @see IdZones
	 */
	public static int getIdZone(int idZone, boolean tapped) {
		return idZone == IdZones.PLAY ? tapped ? IdZones.PLAY_TAPPED
				: IdZones.PLAY_UNTAPPED : idZone;
	}

	/**
	 * returns all IdColors of this card
	 * 
	 * @return all IdColors of this card
	 * @see net.sf.firemox.token.IdCardColors
	 */
	public int getIdColor() {
		return cachedIdColor;
	}

	/**
	 * returns all IdCards of this card
	 * 
	 * @return all IdCards of this card
	 */
	public int getIdCard() {
		return cachedIdCard;
	}

	/**
	 * Indicates if this card has this property.
	 * 
	 * @param property
	 *          is the type required
	 * @return true if this card has the required property.
	 */
	public boolean hasProperty(int property) {
		if (cachedProperties == null) {
			return false;
		}
		if (property < DeckConstraints.getMinProperty()
				|| property > DeckConstraints.getMaxProperty()) {
			return cachedProperties.contains(property);
		}
		return cachedProperties.contains(property)
				|| cachedProperties.contains(DeckConstraints.MASTER);
	}

	/**
	 * indicates if this card has this idType.
	 * 
	 * @param idType
	 *          is the type required
	 * @param creator
	 *          the card that has created the modifier to ignore.
	 * @return true if this card has the required type
	 */
	public boolean hasPropertyNotFromCreator(int idType, MCard creator) {
		boolean found;
		int[] properties = getCardModel().getProperties();
		found = properties != null && Arrays.binarySearch(properties, idType) >= 0;
		if (creator == this) {
			found = false;
		}
		if (propertyModifier != null) {
			return propertyModifier.hasPropertyNotFromCreator(idType, found, creator);
		}
		return found;
	}

	@Override
	public boolean needReverse() {
		if (getParent() != null) {
			if (isAttached()) {
				return !StackManager.isYou(((MCard) getParent()).controller);
			}
			switch (getIdZone()) {
			case IdZones.STACK:
			case IdZones.DELAYED:
			case IdZones.TRIGGERED:
			case IdZones.SIDE:
				return false;
			default:
				return !StackManager.isYou(controller);
			}
		}
		return !StackManager.isYou(controller);
	}

	@Override
	public void moveCard(int destinationZone, Player newController,
			boolean newIsTapped, int idPosition) {

		// Timestamp process
		if (timestampReferences > 0) {
			// one or several triggered abilities make reference to this card
			lastKnownInstances.put(timeStamp, new LastKnownCardInfoImpl(this,
					timestampReferences, destinationZone));
			timestampReferences = 0;
		}
		boolean fromPlayToPlay = idZone == IdZones.PLAY
				&& destinationZone == IdZones.PLAY;

		// remove card from it's previous zone
		if (idZone == IdZones.PLAY) {
			if (!fromPlayToPlay) {
				// clear damages
				clearDamages();
			}

			final MCard attachedTo = isAttached() ? (MCard) getParent() : null;
			controller.zoneManager.play.remove(this);

			if (!fromPlayToPlay) {
				tap(false);
			}

			if (attachedTo != null) {
				// this card was attached to another one in play
				attachedTo.ui.updateLayout();
			}
			controller.zoneManager.play.repaint();
		} else if (getParent() != null) {
			getContainer().remove(this);
		}

		// update positions and controller
		if (controller == null) {
			owner = newController;
		}
		controller = newController;
		idZone = destinationZone;
		timeStamp++;
		isHighLighted = false;
		reversed = needReverse();
		clearPrivateNamedObject();
		ui.updateLayout();

		// move to the destination this card
		switch (destinationZone) {
		case IdZones.PLAY:
			// Initialize the registers
			if (!fromPlayToPlay) {
				int[] staticregisters = getCardModel().getStaticRegisters();
				System.arraycopy(staticregisters, 0, registers, 0,
						staticregisters.length);
			}
			// update card UI
			tap(newIsTapped);
			newController.zoneManager.play.addBottom(this);
			break;
		case IdZones.NOWHERE:
			// the specified card will never be seen again
			break;
		default:
			switch (idPosition) {
			case IdPositions.ON_THE_TOP:
				newController.zoneManager.getContainer(destinationZone).addTop(this);
				break;
			case IdPositions.ON_THE_BOTTOM:
			default:
				newController.zoneManager.getContainer(destinationZone).addBottom(this);
			}
		}
	}

	/**
	 * Indicates whether the card with the specified zone identifier is tapped or
	 * not.
	 * 
	 * @param idZone
	 *          the zone id + tapped position information.
	 * @return true if the card with the specified zone identifier is tapped or
	 *         not.
	 */
	public static boolean isTapped(int idZone) {
		return (idZone & IdZones.PLAY_TAPPED) == IdZones.PLAY_TAPPED;
	}

	/**
	 * Register abilities of this card, supposing card is in the specified zone.
	 * 
	 * @param zone
	 *          The supposed zone this card will go to
	 */
	public void registerAbilities(int zone) {
		for (Ability ability : cachedAbilities) {
			if (ability.eventComing().isWellPlaced(zone)) {
				ability.registerToManager();
			}
		}
	}

	/**
	 * Return a default ability associated to this card. There is no guarantee
	 * this method returns always the same ability.
	 * 
	 * @return a default ability associated to this card.
	 */
	public Ability getDummyAbility() {
		if (cachedAbilities == null || cachedAbilities.isEmpty())
			// TODO Manage the no-ability case for dummyAbility
			return null;
		return cachedAbilities.get(0);
	}

	/**
	 * Register abilities of this card, supposing card is in the specified zone.
	 * 
	 * @param zone
	 *          The supposed zone this card will go to
	 */
	public void registerReplacementAbilities(int zone) {
		for (Ability ability : cachedAbilities) {
			if (ability instanceof ReplacementAbility
					&& ability.eventComing().isWellPlaced(zone)) {
				ability.registerToManager();
			}
		}
	}

	/**
	 * Unregister useless abilities from the eventManager.
	 */
	public void unregisterAbilities() {
		for (Ability ability : cachedAbilities) {
			if (!ability.eventComing().isWellPlaced()) {
				ability.removeFromManager();
			}
		}
	}

	/**
	 * Initialize card picture, database, layout and zone location (SIDE). If the
	 * specified card picture is null, the picture associated to the model will be
	 * used.
	 * 
	 * @param pictureName
	 *          the picture name used to represent the copy. May be null.
	 * @param cardModel
	 *          rules author of this card
	 * @param constraints
	 *          the constraints of the card
	 */
	private void initUI(String pictureName, CardModel cardModel,
			Map<String, String> constraints) {
		isHighLighted = false;
		idZone = IdZones.SIDE;

		// Get the proxy/database information if it is not already set
		if (database == null) {
			database = DatabaseFactory.getDatabase(pictureName, cardModel,
					constraints);
		}
		this.originalDatabase = database;
		setName(cardModel.getCardName());

		ui = new VirtualCard(this);
		add(ui);
		setLayout(new AttachmentLayout());
		ui.updateSizes();
		addMouseWheelListener(this);
	}

	public void tap(boolean tapped) {
		if (this.tapped != tapped) {
			this.tapped = tapped;
			// [un]tap the attached elements too
			for (int i = getComponentCount(); i-- > 0;) {
				((Tappable) getComponent(i)).tap(tapped);
			}
			ui.updateLayout();
		}
	}

	@Override
	public void reverse(boolean reversed) {
		super.reverse(reversed);
		ui.updateLayout();
	}

	@Override
	public void highLight(boolean... highlightedZones) {
		super.highLight(ACTIVATED_COLOR);
		switch (getIdZone()) {
		case IdZones.SIDE:
		case IdZones.STACK:
			highlightedZones[idZone] = true;
			break;
		default:
			highlightedZones[controller.idPlayer * IdZones.NB_ZONE + idZone] = true;
		}
	}

	@Override
	public void targetize(boolean... highlightedZones) {
		super.highLight(TargetFactory.TARGET_COLOR);
		switch (getIdZone()) {
		case IdZones.SIDE:
		case IdZones.STACK:
			highlightedZones[idZone] = true;
			break;
		default:
			highlightedZones[controller.idPlayer * IdZones.NB_ZONE + idZone] = true;
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		// Update mouse wheel layout only if this card is in play with nested cards
		if (getIdZone() == IdZones.PLAY && getComponentCount() > 1) {
			final LayoutManager layoutManager = getLayout();
			if (layoutManager != null && layoutManager instanceof AttachmentLayout
					&& e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				if (e.getWheelRotation() < 0) {
					((AttachmentLayout) layoutManager).decreaseCardLayout(-e
							.getWheelRotation());
				} else {
					((AttachmentLayout) layoutManager).increaseCardLayout(e
							.getWheelRotation());
				}
			}
			doLayout();
			getParent().doLayout();
			getContainer().doLayout();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!(getParent() instanceof MZone) && !(getParent() instanceof MCard)) {
			return;
		}
		StackManager.noReplayToken.take();
		MZone container = getContainer();
		try {
			TargetFactory.triggerTargetable = this;
			if (ConnectionManager.isConnected()
					&& e.getButton() == MouseEvent.BUTTON1) {
				// only if left button is pressed
				if (!StackManager.actionManager.clickOn(this)) {
					// this card is activated and you control it
					if (!(StackManager.actionManager.currentAction instanceof WaitingAbility)) {
						// we are not waiting ability
						if (container instanceof ExpandableZone) {
							((ExpandableZone) container).toggle();
						}
						return;
					}
					final List<Ability> abilities = ((WaitingAbility) StackManager.actionManager.currentAction)
							.abilitiesOf(this);
					final List<Ability> advAbilities = ((WaitingAbility) StackManager.actionManager.currentAction)
							.advancedAbilitiesOf(this);

					// is there any playable ability with this card ?
					if ((abilities == null || abilities.isEmpty())
							&& (advAbilities == null || advAbilities.isEmpty())) {
						// no playable ability, and this card is not wait for -> toggle
						if (container instanceof ExpandableZone) {
							((ExpandableZone) container).toggle();
						}
						return;
					}

					// a choice has to be made
					if (advAbilities != null && !advAbilities.isEmpty()
							|| abilities != null && abilities.size() > 1) {
						/*
						 * several abilities for this card, we fill the popup ability menu
						 */
						TargetFactory.abilitiesMenu.removeAll();
						if (abilities != null) {
							for (Ability ability : abilities) {
								final JMenuItem item = new JMenuItem("<html>"
										+ ability.toHtmlString(null) + "</html>");
								item.addActionListener(this);
								TargetFactory.abilitiesMenu.add(item);
							}
						}
						if (advAbilities != null && !advAbilities.isEmpty()) {
							TargetFactory.abilitiesMenu.add(new JSeparator());
							for (Ability ability : advAbilities) {
								final JMenuItem item = new JMenuItem("<html>"
										+ ability.toHtmlString(null) + "</html>", WARNING_PICTURE);
								item.addActionListener(this);
								TargetFactory.abilitiesMenu.add(item);
							}
						}
						// show the option popup menu
						TargetFactory.abilitiesMenu.show(e.getComponent(), e.getX(), e
								.getY());
						return;
					} else if (abilities != null && abilities.size() == 1
							&& (advAbilities == null || advAbilities.isEmpty())) {
						// only one playable ability, we select it automatically
						((UserAbility) abilities.get(0)).mouseClicked(0);
					}
				} else if (StackManager.idHandedPlayer != 0) {
					if (container instanceof ExpandableZone) {
						((ExpandableZone) container).toggle();
					}
				} else {
					// Since this click has been handled, the opponent will be informed on
					sendClickToOpponent();
					StackManager.actionManager.succeedClickOn(this);
				}
			} else if (ConnectionManager.isConnected()) {
				// only if not left button is pressed
				CardFactory.contextMenu.removeAll();
				TargetFactory.triggerTargetable = this;

				// add counter item
				CardFactory.countItem.setText(LanguageManager.getString("countcard",
						container.toString(), String.valueOf(container.getCardCount())));
				CardFactory.contextMenu.add(CardFactory.countItem);

				// add refresh picture item
				CardFactory.contextMenu.add(CardFactory.reloadPictureItem);

				// add expand/gather item
				if (container instanceof ExpandableZone) {
					if (container == ZoneManager.expandedZone) {
						CardFactory.contextMenu.add(CardFactory.gatherItem);
					} else {
						CardFactory.contextMenu.add(CardFactory.expandItem);
					}
				}
				CardFactory.contextMenu.add(new JSeparator());

				// add the "database" item --> activate the tabbed panel
				CardFactory.contextMenu.add(CardFactory.databaseCardInfoItem);

				// TODO add "java properties" item --> show an inspect popup.
				// CardFactory.contextMenu.add(CardFactory.javaDebugItem);

				// TODO add "show id" option

				// show the option popup menu
				CardFactory.contextMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			StackManager.noReplayToken.release();
		}
	}

	@Override
	public void sendClickToOpponent() {
		ConnectionManager.send(CoreMessageType.CLICK_CARD, getBytes());
	}

	/**
	 * Return the data array representing this card. This array can be send throw
	 * network.
	 * 
	 * @return bytes representing this card.
	 */
	public final byte[] getBytes() {
		Pair<Integer, Integer> index = controller.zoneManager.getContainer(idZone)
				.getRealIndexOf(this);
		// get index of this card within it's container
		return new byte[] { (byte) (1 - controller.idPlayer), (byte) getIdZone(),
				(byte) index.key.intValue(), (byte) index.value.intValue() };
	}

	/**
	 * This method is invoked when opponent has clicked on this object.
	 * 
	 * @param data
	 *          data sent by opponent.
	 */
	public static void clickOn(byte[] data) {
		// reading card information
		MCard card = getCard(data);
		StackManager.actionManager.clickOn(card);
		StackManager.actionManager.succeedClickOn(card);
	}

	/**
	 * Return the component from information read from opponent.
	 * 
	 * @param data
	 *          data sent by opponent.
	 * @return the card read from the specified input stream
	 */
	public static MCard getCard(byte[] data) {
		// waiting for card information
		int idPlayer = data[0];
		int idZone = data[1];
		int index = data[2];
		int childIndex = data[3];
		if (childIndex == 0) {
			// has no parent
			return StackManager.PLAYERS[idPlayer].zoneManager.getContainer(idZone)
					.getCard(index);
		}
		// is within another card (has parent)
		return (MCard) StackManager.PLAYERS[idPlayer].zoneManager.getContainer(
				idZone).getCard(index).getComponent(childIndex);
	}

	@Override
	public int getValue(int index) {
		if (index == IdTokens.MANA_POOL) {
			if (idZone == IdZones.STACK) {
				// return the total cost of associated ability
				return StackManager.getTotalManaPaid(this);
			}
			return MToolKit.manaPool(cachedRegisters);
		}
		if (index == IdTokens.ID) {
			return getId();
		}

		if (index >= IdTokens.FIRST_FREE_CARD_INDEX) {
			return registers[index];
		}
		if (cachedRegisters == null) {
			throw new InternalError("modifiedRegisters is null in getValue in card");
		}
		return cachedRegisters[index];
	}

	@Override
	public int getValueIndirection(int index) {
		// first, read registers indirections
		if (indirections[index] != null) {
			return indirections[index].getValue(registers[index]);
		}
		return registers[index];
	}

	/**
	 * Add a modifier to this object
	 * 
	 * @param modifier
	 *          the color-modifier to add to this object
	 */
	public void addModifier(ColorModifier modifier) {
		if (colorModifier != null) {
			colorModifier = (ColorModifier) colorModifier.addModifier(modifier);
		} else {
			colorModifier = modifier;
		}
	}

	/**
	 * Add a modifier to this object
	 * 
	 * @param modifier
	 *          the id card modifier to add to this object
	 */
	public void addModifier(IdCardModifier modifier) {
		if (idCardModifier != null) {
			idCardModifier = (IdCardModifier) idCardModifier.addModifier(modifier);
		} else {
			idCardModifier = modifier;
		}
	}

	/**
	 * Add a modifier to this object
	 * 
	 * @param modifier
	 *          the ability-modifier to add to this object
	 */
	public void addModifier(AbilityModifier modifier) {
		if (abilityModifier != null) {
			abilityModifier = (AbilityModifier) abilityModifier.addModifier(modifier);
		} else {
			abilityModifier = modifier;
		}
	}

	/**
	 * Add a modifier to this object
	 * 
	 * @param modifier
	 *          the property-modifier to add to this object
	 */
	public void addModifier(PropertyModifier modifier) {
		if (propertyModifier != null) {
			propertyModifier = (PropertyModifier) propertyModifier
					.addModifier(modifier);
		} else {
			propertyModifier = modifier;
		}
	}

	/**
	 * Add a modifier to this object
	 * 
	 * @param modifier
	 *          the controller-modifier to add to this object
	 */
	public void addModifier(ControllerModifier modifier) {
		if (controllerModifier != null) {
			controllerModifier = (ControllerModifier) controllerModifier
					.addModifier(modifier);
		} else {
			controllerModifier = modifier;
		}
	}

	/**
	 * Add a modifier to this object
	 * 
	 * @param modifier
	 *          the playable zone-modifier to add to this object
	 */
	public void addModifier(PlayableZoneModifier modifier) {
		if (playableZoneModifier != null) {
			playableZoneModifier = (PlayableZoneModifier) playableZoneModifier
					.addModifier(modifier);
		} else {
			playableZoneModifier = modifier;
		}
	}

	@Override
	public void removeModifier(RegisterModifier modifier, int index) {
		if (registerModifiers[index] != null) {
			registerModifiers[index] = (RegisterModifier) registerModifiers[index]
					.removeModifier(modifier);
		} else {
			Log.warn("Card moved, unable to remove a null modifier");
		}

	}

	@Override
	public void removeModifier(RegisterIndirection indirection, int index) {
		if (indirections[index] != null) {
			indirections[index] = (RegisterIndirection) indirections[index]
					.removeModifier(indirection);
		} else {
			Log.warn("Card moved, unable to remove a null modifier");
		}
	}

	/**
	 * Remove the specified id card modifier
	 * 
	 * @param modifier
	 *          the id card modifier to be removed from this object
	 */
	public void removeModifier(IdCardModifier modifier) {
		if (modifier != null) {
			idCardModifier = (IdCardModifier) idCardModifier.removeModifier(modifier);
		} else {
			Log.warn("Card moved, unable to remove a null modifier");
		}
	}

	/**
	 * Remove the specified ability-modifier
	 * 
	 * @param modifier
	 *          the ability-modifier to be removed from this object
	 */
	public void removeModifier(AbilityModifier modifier) {
		if (modifier != null) {
			abilityModifier = (AbilityModifier) abilityModifier
					.removeModifier(modifier);
		} else {
			Log.warn("Card moved, unable to remove a null modifier");
		}
	}

	/**
	 * Remove the specified controller-modifier
	 * 
	 * @param modifier
	 *          the controller-modifier to be removed from this object
	 */
	public void removeModifier(ControllerModifier modifier) {
		if (modifier != null) {
			controllerModifier = (ControllerModifier) controllerModifier
					.removeModifier(modifier);
		} else {
			Log.warn("Card moved, unable to remove a null modifier");
		}
	}

	/**
	 * Remove the specified property-modifier
	 * 
	 * @param modifier
	 *          the property-modifier to be removed from this object
	 */
	public void removeModifier(PropertyModifier modifier) {
		if (modifier != null) {
			propertyModifier = (PropertyModifier) propertyModifier
					.removeModifier(modifier);
		} else {
			Log.warn("Card moved, unable to remove a null modifier");
		}
	}

	/**
	 * Remove the specified playable zone-modifier
	 * 
	 * @param modifier
	 *          the playable zone-modifier to be removed from this object
	 */
	public void removeModifier(PlayableZoneModifier modifier) {
		if (modifier != null) {
			playableZoneModifier = (PlayableZoneModifier) playableZoneModifier
					.removeModifier(modifier);
		} else {
			Log.warn("Card moved, unable to remove a null modifier");
		}
	}

	/**
	 * Remove the specified color-modifier.
	 * 
	 * @param modifier
	 *          the color-modifier to be removed from this object
	 */
	public void removeModifier(ColorModifier modifier) {
		if (modifier != null) {
			colorModifier = (ColorModifier) colorModifier.removeModifier(modifier);
		} else {
			Log.warn("Card moved, unable to remove a null modifier");
		}
	}

	/**
	 * Indicated if this card is attached or not.
	 * 
	 * @return true value if this card is attached.
	 */
	public boolean isAttached() {
		return getParent() != null && getParent() instanceof MCard;
	}

	@Override
	public String getTooltipString() {
		return ui.getTooltipString();
	}

	/**
	 * Refresh the id card of this card, and raise an event if the value has been
	 * changed.
	 */
	public void refreshIdCard() {
		int modifiedIdCard = idCardModifier != null ? idCardModifier
				.getIdCard(getCardModel().getIdCard()) : getCardModel().getIdCard();
		if (this.cachedIdCard != modifiedIdCard) {
			// the id card of this card has changed
			final int modifiedIdCardTmp = this.cachedIdCard;
			this.cachedIdCard = modifiedIdCard;
			ModifiedIdCard.dispatchEvent(this, modifiedIdCard | modifiedIdCardTmp);
			ui.resetCachedData();
		}
	}

	/**
	 * Refresh the granted abilities of this card. No event raised.
	 */
	public void refreshAbilities() {
		if (abilityModifier == null) {
			registerAbilities(idZone);
		} else {
			final List<Ability> toRegister = new ArrayList<Ability>(cachedAbilities
					.size() + 2);

			for (Ability ability : cachedAbilities) {
				if (ability.eventComing().isWellPlaced(idZone)) {
					toRegister.add(ability);
				}
			}

			abilityModifier.calculateDeltaAbilities(toRegister);

			// First, unregister the disabled abilities
			for (Ability ability : cachedAbilities) {
				if (!toRegister.contains(ability)) {
					// this ability has to been unregistered
					ability.removeFromManager();
				}
			}

			// Then, register the granted abilities
			for (Ability ability : toRegister) {
				ability.registerToManager();
			}
		}
	}

	/**
	 * Refresh the colors of this card, and raise an event if the value has been
	 * changed.
	 */
	public void refreshIdColor() {
		int modifiedIdColor = colorModifier != null ? colorModifier
				.getIdColor(getCardModel().getIdColor()) : getCardModel().getIdColor();
		if (this.cachedIdColor != modifiedIdColor) {
			// the color of this card has changed
			int modifiedIdColorTmp = this.cachedIdColor;
			this.cachedIdColor = modifiedIdColor;
			ModifiedIdColor.dispatchEvent(this, modifiedIdColor | modifiedIdColorTmp);
			this.cachedIdColor = modifiedIdColor;
			ui.resetCachedData();
		}
	}

	/**
	 * Refresh the properties of this card, and raise an event if the value has
	 * been changed. NOTE : there is no cache for properties.
	 * 
	 * @param property
	 *          the property to refresh.
	 */
	public void refreshProperties(int property) {
		final CardModel cardModel = getCardModel();
		boolean raised = false;
		if (property == IdConst.ALL) {
			// All properties have to be refreshed
			java.util.Set<Integer> oldProperties = new HashSet<Integer>(
					cachedProperties);
			cachedProperties.clear();
			for (int initProperty : cardModel.getProperties()) {
				cachedProperties.add(initProperty);
			}
			if (propertyModifier != null) {
				propertyModifier.fillProperties(cachedProperties);
			}
			for (Integer cacheProperty : cachedProperties) {
				if (!oldProperties.contains(cacheProperty)) {
					ModifiedProperty.dispatchEvent(this, cacheProperty);
					raised = true;
				}
			}
			for (Integer oldProperty : oldProperties) {
				if (!cachedProperties.contains(oldProperty)) {
					ModifiedProperty.dispatchEvent(this, oldProperty);
					raised = true;
				}
			}
		} else {
			final boolean oldFound = cachedProperties.contains(property);
			int[] properties = cardModel.getProperties();
			boolean found = properties != null
					&& Arrays.binarySearch(properties, property) >= 0;
			if (propertyModifier != null) {
				found = propertyModifier.hasProperty(property, found);
			}
			if (oldFound != found) {
				if (found) {
					cachedProperties.add(property);
				} else {
					cachedProperties.remove(property);
				}
				ModifiedProperty.dispatchEvent(this, property);
				raised = true;
			}
		}
		if (raised) {
			ui.resetCachedData();
			repaint();
		}
	}

	/**
	 * Refresh the registers of this card, and raise an event if the value has
	 * been changed.
	 * 
	 * @param index
	 *          is the register index to refresh
	 */
	public void refreshRegisters(int index) {
		if (index == IdConst.ALL) {
			for (int i = 0; i < registerModifiers.length; i++) {
				refreshRegisters(i);
			}
		} else {
			final int modifiedRegister;
			if (registerModifiers[index] != null)
				modifiedRegister = registerModifiers[index]
						.getValue(getValueIndirection(index));
			else {
				modifiedRegister = getValueIndirection(index);
			}
			if (this.cachedRegisters[index] != modifiedRegister) {
				// the register of this card has changed. Negative values are not
				// permitted
				cachedRegisters[index] = modifiedRegister < 0 ? 0 : modifiedRegister;
				ModifiedRegister.dispatchEvent(this, this, IdTokens.CARD, index, Set
						.getInstance(), modifiedRegister);
				ui.resetCachedData();
				repaint();
			}
		}
	}

	/**
	 * Refresh the controller of this card, and raise an event if the value has
	 * been changed.
	 */
	public void refreshController() {
		final Player controller = controllerModifier != null ? controllerModifier
				.getPlayer(originalController) : originalController;
		if (this.controller != controller) {
			// the controller of this card has changed
			MoveCard.moveCard(this, controller, getIdZone(idZone, tapped), null, 0,
					null, false);
		}
		ui.resetCachedData();
	}

	@Override
	public int countAllCardsOf(Test test, Ability ability, boolean canBePreempted) {
		int result = 0;
		if (canBePreempted) {
			result = test.test(ability, this) ? 1 : 0;
		} else {
			result = test.testPreemption(ability, this) ? 1 : 0;
		}
		for (int j = getComponentCount(); j-- > 1;) {
			if (getComponent(j) instanceof MCard) {
				if (canBePreempted) {
					if (test.test(ability, (MCard) getComponent(j))) {
						result++;
					}
				} else if (!canBePreempted
						&& test.testPreemption(ability, (MCard) getComponent(j))) {
					result++;
				}
			}
		}
		return result;
	}

	@Override
	public void checkAllCardsOf(Test test, List<Target> list, Ability ability) {
		if (test.test(ability, this)) {
			list.add(this);
		}
		for (int j = getComponentCount(); j-- > 1;) {
			if (getComponent(j) instanceof MCard
					&& test.test(ability, (MCard) getComponent(j))) {
				list.add((MCard) getComponent(j));
			}
		}
	}

	/**
	 * Indicates this card can be played from a specified zone.
	 * 
	 * @param supposedZone
	 *          the zone where the this card would be played from.
	 * @param idZone
	 *          the zone where this card can be played from.
	 * @return true if this card is playable from the supposed zone
	 */
	public boolean playableZone(int supposedZone, int idZone) {
		if (playableZoneModifier != null) {
			return playableZoneModifier.playableIn(idZone, idZone == supposedZone);
		}
		return supposedZone == idZone;
	}

	/**
	 * [un]Register ActivatedAbilities depending on the current zone of this card
	 */
	public void updateAbilities() {
		for (Ability ability : cachedAbilities) {
			if (ability instanceof ActivatedAbility) {
				ability.removeFromManager();
				for (int j = IdZones.STACK; j-- > 0;) {
					if (ability.eventComing().isWellPlaced(j)) {
						ability.registerToManager();
						break;
					}
				}
			}
		}
	}

	/**
	 * Set to the register of this card a value to a specified index. The given
	 * operation is uesed to apply operation on old and the given value. To set
	 * the given value as the new one, use the "set" operation.
	 * 
	 * @param index
	 *          is the index of register to modify
	 * @param operation
	 *          the operation to use
	 * @param rightValue
	 *          is the value to use as right operand for the operation
	 */
	public void setValue(int index, Operation operation, int rightValue) {
		registers[index] = operation.process(registers[index], rightValue);
		if (index == IdCommonToken.DAMAGE) {
			MContextCardCardIntInt context = null;
			if (StackManager.getInstance().getAbilityContext() != null
					&& StackManager.triggered.getAbilityContext() instanceof MContextCardCardIntInt
					&& operation instanceof Add) {
				context = (MContextCardCardIntInt) StackManager.triggered
						.getAbilityContext();
				if (context.getValue() != rightValue) {
					throw new InternalError("wrong damage value regValue=" + rightValue
							+ ", context.int=" + context.getValue());
				}
			}
			if (operation instanceof Add) {
				Log.debug(new StringBuilder("Add ").append(rightValue).append(
						" damage to card ").append(getCardName()).append("@").append(
						hashCode()).append(":").append(rightValue));
				Damage damage = new Damage(context == null ? SystemCard.instance
						: context.getCard2(), rightValue, context == null ? 0 : context
						.getValue2());
				damage.tap(tapped);
				add(damage);
			} else {
				// in any other cases, we remove all damages
				clearDamages();
			}
			ui.updateLayout();
		}
		if (index < IdTokens.FIRST_FREE_CARD_INDEX) {
			// we need to refresh the cache of this index
			int modifiedRegister = registerModifiers[index] != null ? registerModifiers[index]
					.getValue(getValueIndirection(index))
					: getValueIndirection(index);
			if (this.cachedRegisters[index] != modifiedRegister) {
				// the register of this card has changed.
				// Negative values are not
				cachedRegisters[index] = modifiedRegister < 0 ? 0 : modifiedRegister;
				ui.resetCachedData();
				// no generated event, this managed in the ModifyRegister action
			}
		} else {
			repaint();
		}
	}

	/**
	 * Set a new zone for this card.
	 * 
	 * @param idZone
	 *          the new zone.
	 */
	public void setIdZone(int idZone) {
		this.idZone = idZone;
	}

	@Override
	public void addTimestampReference() {
		timestampReferences++;
	}

	@Override
	public void decrementTimestampReference(int timestamp) {
		if (this.timeStamp == timestamp) {
			timestampReferences--;
		} else if (lastKnownInstances.get(timestamp) != null
				&& lastKnownInstances.get(timestamp).removeTimestamp(timestamp)) {
			/*
			 * There is no more reference to this timestamp, so we need no longer the
			 * saved information about this card.
			 */
			lastKnownInstances.remove(timestamp);
		}
	}

	/**
	 * Return all properties of this card
	 * 
	 * @return all properties of this card
	 */
	public java.util.Set<Integer> getProperties() {
		return cachedProperties;
	}

	@Override
	public Target getLastKnownTargetable(int timeStamp) {
		if (timeStamp == this.timeStamp) {
			assert timestampReferences > 0;
			return this;
		}
		if (lastKnownInstances.get(timeStamp) == null) {
			return this;
		}
		return lastKnownInstances.get(timeStamp).createLastKnownCard();
	}

	@Override
	public int getTimestamp() {
		return timeStamp;
	}

	/**
	 * Return occurrences number of the given object with the given name attached
	 * to this card.
	 * 
	 * @param objectName
	 *          the object's name to find within the register modifiers chain.
	 * @param objectTest
	 *          The test applied on specific modifier to be removed.
	 * @return occurrences number of the given object with the given name attached
	 *         to this card.
	 */
	public int getNbObjects(String objectName, Test objectTest) {
		return ObjectFactory.getObjectModifierModel(objectName).getNbObject(this,
				objectTest);
	}

	@Override
	public void clearDamages() {
		for (int i = getComponentCount(); i-- > 0;) {
			if (getComponent(i) instanceof Damage) {
				remove(getComponent(i));
			}
			registers[IdCommonToken.DAMAGE] = 0;
			cachedRegisters[IdCommonToken.DAMAGE] = 0;
		}
	}

	/**
	 * The database configuration of this card
	 */
	private DatabaseCard originalDatabase;

	/**
	 * The modified id card.
	 */
	public int cachedIdCard;

	/**
	 * The modified color.
	 */
	public int cachedIdColor;

	/**
	 * The modified registers.
	 */
	public int[] cachedRegisters;

	/**
	 * The original player.
	 */
	public final Player originalController;

	/**
	 * Player owner
	 */
	protected Player owner;

	/**
	 * the zone identifier
	 * 
	 * @see IdZones
	 */
	protected int idZone;

	/**
	 * Indicates if this card should be tapped or not.
	 */
	public boolean tapped;

	/**
	 * original card which staid at it's place not really moved to stack.
	 */
	private final MCard copiedCard;

	/**
	 * The colors modifiers on this object.
	 */
	public ColorModifier colorModifier;

	/**
	 * The id card modifiers on this object.
	 */
	public IdCardModifier idCardModifier;

	/**
	 * The ability modifiers on this object.
	 */
	public AbilityModifier abilityModifier;

	/**
	 * The properties modifiers on this object.
	 */
	public PropertyModifier propertyModifier;

	/**
	 * The playable zone modifiers on this object.
	 */
	public PlayableZoneModifier playableZoneModifier;

	/**
	 * Represents all referenced instances of this card. <br>
	 * Key is timestamp of this card. <br>
	 * Value is LastKnownCardInfo instance.
	 */
	private Map<Integer, LastKnownCardInfo> lastKnownInstances;

	/**
	 * This timestamp corresponds to the amount of card movements.
	 */
	protected int timeStamp;

	/**
	 * the reference counter for the current timestamp of this card.
	 */
	protected int timestampReferences;

	/**
	 * List of properties of this card.
	 */
	public java.util.Set<Integer> cachedProperties;

	/**
	 * @since 0.91
	 * @see net.sf.firemox.clickable.target.card.CardModel#getModifierModels()
	 * @return the shared modifier models.
	 */
	public ModifierModel getModifierModels() {
		return this.getCardModel().getModifierModels();
	}

	@Override
	public int hashCode() {
		return getCardName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	/**
	 * Returns the cardModel reference of this card.
	 * 
	 * @return the cardModel reference of this card.
	 */
	public CardModel getCardModel() {
		return database.getCardModel();
	}

	/**
	 * Set the new owner of this card.
	 * 
	 * @param owner
	 *          the new owner.
	 */
	public void setOwner(Player owner) {
		this.owner = owner;
	}

	/**
	 * Return card's owner.
	 * 
	 * @return card's owner.
	 */
	public Player getOwner() {
		return owner;
	}

	/**
	 * Update the card model. Also, post some refresh request on some attributes
	 * of this card.
	 * 
	 * @param database
	 *          the new database of this card.
	 */
	public void setDataBase(DatabaseCard database) {
		unregisterAbilities();
		this.database = database;
		final CardModel cardModel = getCardModel();
		setName(getCardModel().getCardName());
		cachedAbilities = new ArrayList<Ability>(cardModel.getAbilities().length);
		for (Ability ability : cardModel.getAbilities()) {
			cachedAbilities.add(ability.clone(this));
		}
		registers = getCardModel().getStaticRegisters().clone();
		StackManager.postRefreshAbilities(this);
		StackManager.postRefreshColor(this);
		StackManager.postRefreshIdCard(this);
		StackManager.postRefreshProperties(this, IdConst.ALL);
		StackManager.postRefreshRegisters(this, IdConst.ALL);
		repaint();
	}

	/**
	 * Return <code>true</code> if this card has a database configuration
	 * different from the original one.
	 * 
	 * @return <code>true</code> if this card has a database configuration
	 *         different from the original one.
	 */
	public boolean hasDirtyDataBase() {
		return originalDatabase != getDatabase();
	}

	/**
	 * Return the original database as imprinted.
	 * 
	 * @return the original database.
	 */
	public DatabaseCard getOriginalDatabase() {
		return originalDatabase;
	}

	/**
	 * Return the attached cards.
	 * 
	 * @return the attached cards.
	 */
	public Collection<MCard> getAttachedCards() {
		final Collection<MCard> attachedCards = new ArrayList<MCard>(2);
		for (Component component : getComponents()) {
			if (component instanceof MCard)
				attachedCards.add((MCard) component);
		}
		return attachedCards;
	}

}