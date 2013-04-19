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
package net.sf.firemox.clickable.target.player;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EtchedBorder;

import net.sf.firemox.action.PayMana;
import net.sf.firemox.action.WaitActivatedChoice;
import net.sf.firemox.action.WaitTriggeredBufferChoice;
import net.sf.firemox.action.target.ChosenTarget;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.mana.ManaPool;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.TargetFactory;
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.deckbuilder.Deck;
import net.sf.firemox.modifier.RegisterIndirection;
import net.sf.firemox.modifier.RegisterModifier;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.Synchronizer;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.stack.ActionManager;
import net.sf.firemox.stack.EventManager;
import net.sf.firemox.stack.MPhase;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.stack.TargetHelper;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.MCommonVars;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.zone.ZoneManager;

/**
 * Represents a player : name (avatar, preferences,...), mana and zones. Has
 * also a register like cards to store data as life, poison, maximal number of
 * cards in hand and number of cards to draw counter.<br>
 * TODO support modifiers for player components, like cards.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public abstract class Player extends Target {

	/**
	 * Player view width
	 */
	public static final int PLAYER_SIZE_WIDTH = 80;

	/**
	 * Player view height
	 */
	public static final int PLAYER_SIZE_HEIGHT = 40;

	/**
	 * Translated "the opponent has the priority" text.
	 */
	protected String handedText;

	/**
	 * creates a new instance of MPlayer
	 * 
	 * @param idPlayer
	 *          id of this player
	 * @param mana
	 *          manas of this player
	 * @param zoneManager
	 *          the zoneManager of this player
	 * @param morePanel
	 *          the panel containing player info.
	 */
	protected Player(int idPlayer, ManaPool mana, ZoneManager zoneManager,
			JPanel morePanel) {
		this.idPlayer = idPlayer;
		this.zoneManager = zoneManager;
		this.mana = mana;
		this.morePanel = morePanel;
	}

	/**
	 * Update the opponent side depending on the "enable reverse" options.
	 */
	public abstract void updateReversed();

	/**
	 * Remove, and then fill the phases of this player
	 * 
	 * @param phases
	 *          the phases to add.
	 */
	public void resetPhases(MPhase[] phases) {
		this.phases.removeAll();
		for (MPhase phase : phases) {
			this.phases.add(phase);
		}
		this.phases.doLayout();
	}

	@Override
	public boolean isCard() {
		return false;
	}

	@Override
	public final boolean isAbility(int abilityType) {
		return false;
	}

	@Override
	public final boolean isSpell() {
		return false;
	}

	/**
	 * tell if this player is you
	 * 
	 * @return true if this player is you
	 */
	public abstract boolean isYou();

	/**
	 * tell if this player is the current one
	 * 
	 * @return true if this player is the current one
	 */
	public boolean isCurrentPlayer() {
		return StackManager.idCurrentPlayer == idPlayer;
	}

	/**
	 * Return the opponent player
	 * 
	 * @return the opponent player
	 */
	public Player getOpponent() {
		return StackManager.PLAYERS[1 - idPlayer];
	}

	@Override
	protected final void highLight(Color highLightColor) {
		setBackground(highLightColor);
		if (CardFactory.ACTIVATED_COLOR.equals(highLightColor)) {
			// this player has activated abilities, we add them as JButton
			final List<Ability> abilities = WaitActivatedChoice.getInstance()
					.abilitiesOf(playerCard);
			if (abilities.size() > 0) {
				abilitiesPanel.add(new JLabel(LanguageManager.getString("abilities")
						+ " : "));
			}
			for (int i = abilities.size(); i-- > 0;) {
				final JButton button = new JButton(abilities.get(i).toString());
				button.setActionCommand("" + i);
				button.addActionListener(this);
				abilitiesPanel.add(button);
			}
		}
		avatarButton.setBackground(highLightColor);
		MagicUIComponents.playerTabbedPanel.setSelectedComponent(morePanel);
		MagicUIComponents.playerTabbedPanel.setBackgroundAt(
				MagicUIComponents.playerTabbedPanel.indexOfComponent(morePanel),
				highLightColor);
		super.highLight(highLightColor);
	}

	@Override
	public void disHighLight() {
		isHighLighted = false;
		setBackground(new Color(204, 204, 204));
		avatarButton.setBackground(null);
		abilitiesPanel.removeAll();
		try {
			MagicUIComponents.playerTabbedPanel
					.setBackgroundAt(MagicUIComponents.playerTabbedPanel
							.indexOfComponent(morePanel), null);
		} catch (Exception e) {
			Log.error("Error in tabbedpanel : " + e);
		}
		super.disHighLight();
	}

	/**
	 * Set to the register of this card a value to a specified index. The given
	 * operation is used to apply operation on old and the given value. To set the
	 * given value as the new one, use the "set" operation.
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

		if (index < 6) {
			// update mana pool buttons
			mana.manaButtons[index].repaint();
		} else if (index == IdTokens.LIFE) {
			// update life button
			updateLife();
		} else if (index == IdTokens.POISON) {
			// update poison button
			updatePoison();
		}
	}

	@Override
	public void update(Graphics g) {
		super.paintComponent(g);
		// draw the highlighted rectangle
		if (isHighLighted) {
			g.setColor(highLightColor);
			g.draw3DRect(0, 0, getWidth() - 1, getHeight() - 1, true);
			g.draw3DRect(1, 1, getWidth() - 3, getHeight() - 3, true);
		}
		g.dispose();
	}

	/**
	 * update the life counter label
	 */
	private void updateLife() {
		lifeLabel.setText(String.valueOf(registers[IdTokens.LIFE]));
	}

	/**
	 * update the life counter label
	 */
	private void updatePoison() {
		poisonLabel.setText(String.valueOf(registers[IdTokens.POISON]));
	}

	/**
	 * Initialize the labels referencing to the registers of the MPlayer
	 * components.
	 */
	public static void init() {
		unsetHandedPlayer();
		for (Player player : StackManager.PLAYERS) {
			player.updateLife();
			player.updatePoison();
			player.mana.setVisible(PayMana.useMana);
		}
		MagicUIComponents.chatHistoryText.setContact(StackManager.PLAYERS[0]
				.getNickName(), StackManager.PLAYERS[1].getNickName());
		MagicUIComponents.logListing.setContact(StackManager.PLAYERS[0]
				.getNickName(), StackManager.PLAYERS[1].getNickName());
	}

	/**
	 * return the player's name
	 * 
	 * @return the player's name
	 */
	@Override
	public String toString() {
		return getNickName();
	}

	/**
	 * Return the player's name.
	 * 
	 * @return the player's name
	 */
	public abstract String getNickName();

	/**
	 * Indicates if this player decline to response to the current effect.
	 * 
	 * @return true if this player decline to response to the current effect.
	 * @see MPhase#declineResponseMe()
	 * @since 0.30
	 * @since 0.31 an option "skip all even opponent's spell" is supported
	 * @since 0.80 "medium decline" is supported
	 */
	public boolean declineResponseMe() {
		if (MPhase.phases[StackManager.idCurrentPlayer][EventManager.phaseIndex]
				.declineResponseMe()) {
			return false;
		}
		for (int i = EventManager.phaseIndex + 1; i < EventManager.turnStructure.length; i++) {
			if (MPhase.phases[StackManager.idCurrentPlayer][i].declineResponseMe()) {
				return true;
			}
		}
		for (int i = 0; i < EventManager.turnStructure.length; i++) {
			if (MPhase.phases[1 - StackManager.idCurrentPlayer][i]
					.declineResponseMe()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Indicates if this player decline to play any ability with an empty stack.
	 * 
	 * @return if this player decline to play any ability with an empty stack.
	 * @see MPhase#breakpoint()
	 * @since 0.30
	 * @since 0.31 an option "skip all even opponent's spell" is supported
	 * @since 0.80 "medium decline" is supported
	 */
	public boolean declinePlay() {
		if (MPhase.phases[StackManager.idCurrentPlayer][EventManager.phaseIndex]
				.breakpoint()) {
			return false;
		}
		for (int i = EventManager.phaseIndex + 1; i < EventManager.turnStructure.length; i++) {
			if (MPhase.phases[StackManager.idCurrentPlayer][i].declineResponseMe()) {
				return true;
			}
		}
		for (int i = 0; i < EventManager.turnStructure.length; i++) {
			if (MPhase.phases[1 - StackManager.idCurrentPlayer][i]
					.declineResponseMe()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Indicates if this player decline to response to the current effect owned by
	 * opponent.
	 * 
	 * @return true if this player decline to response to the current effect owned
	 *         by opponent.
	 * @see MPhase#declineResponseOpponent()
	 * @since 0.30
	 * @since 0.31 an option "skip all even opponent's spell" is supported
	 * @since 0.80 "medium decline" is supported
	 */
	public boolean declineResponseOpponent() {
		if (MPhase.phases[StackManager.idCurrentPlayer][EventManager.phaseIndex]
				.declineResponseOpponent()) {
			return false;
		}
		for (int i = EventManager.phaseIndex + 1; i < EventManager.turnStructure.length; i++) {
			if (MPhase.phases[StackManager.idCurrentPlayer][i]
					.declineResponseOpponent()) {
				return true;
			}
		}
		for (int i = 0; i < EventManager.turnStructure.length; i++) {
			if (MPhase.phases[1 - StackManager.idCurrentPlayer][i]
					.declineResponseOpponent()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set this player as active one
	 */
	public void setActivePlayer() {
		StackManager.idActivePlayer = idPlayer;
		setHandedPlayer();
		EventManager.updatePhasesGUI();
	}

	/**
	 * Set this player as handed one
	 */
	public void setHandedPlayer() {
		MagicUIComponents.targetTimer.resetCounter();
		MagicUIComponents.waitingLabel.setText(handedText);
		MagicUIComponents.skipButton.setEnabled(isYou());
		MagicUIComponents.skipMenu.setEnabled(isYou());
		StackManager.idHandedPlayer = idPlayer;
		infoPanel.setBackground(Color.ORANGE);
	}

	/**
	 * Remove to all players the possibility to do something
	 */
	public static void unsetHandedPlayer() {
		StackManager.oldIdHandedPlayer = StackManager.idHandedPlayer;
		StackManager.idHandedPlayer = -1;
		for (Player player : StackManager.PLAYERS) {
			player.infoPanel.setBackground(null);
		}
		MagicUIComponents.skipButton.setEnabled(false);
		MagicUIComponents.skipMenu.setEnabled(false);
		MagicUIComponents.waitingLabel.setText(HANDED_NOBODY);
		MagicUIComponents.waitingLabel.setForeground(Color.ORANGE);
	}

	/**
	 * Wait for the active player, then the non-active player to make choice of
	 * the order of triggered abilities to be put from the buffer to the stack
	 * 
	 * @param resolveOnEmpty
	 *          if true, the stack resolution would be broken if no triggered
	 *          abilities have been played instead of playing the
	 *          WaitActivatedChoice action.
	 * @return true if NO high priority triggered ability has been played from the
	 *         TBZ. So the stack can be resolved.
	 */
	public boolean waitTriggeredBufferChoice(boolean resolveOnEmpty) {
		StackManager.actionManager.currentAction = WaitTriggeredBufferChoice
				.getInstance();

		// process now the posted refresh requests to fake a real-time update
		StackManager.processRefreshRequests();
		if (waitTriggeredBufferChoiceRec()) {
			// no player has stacked triggered any ability
			if (resolveOnEmpty) {
				StackManager.actionManager.waitingOnMiddle = false;
				WaitTriggeredBufferChoice.getInstance().finished();
			}
			return true;
		}
		return false;
	}

	/**
	 * Return true if there was one or several processed hidden triggered
	 * abilities
	 * 
	 * @return true if there was one or several processed hidden triggered
	 *         abilities
	 */
	public boolean processHiddenTriggered() {
		return zoneManager.triggeredBuffer.resolveHiddenHighLevel(idPlayer)
				|| getOpponent().zoneManager.triggeredBuffer
						.resolveHiddenHighLevel(1 - idPlayer)
				|| zoneManager.triggeredBuffer.resolveHiddenNormalLevel(idPlayer)
				|| getOpponent().zoneManager.triggeredBuffer
						.resolveHiddenNormalLevel(1 - idPlayer)
				|| zoneManager.triggeredBuffer.resolveHiddenLowestLevel(idPlayer)
				|| getOpponent().zoneManager.triggeredBuffer
						.resolveHiddenLowestLevel(1 - idPlayer);
	}

	/**
	 * List, purge, recheck, add found triggered abilities into the stack. If only
	 * one ability has been found, it is played automatically. If several
	 * abilities are found for YOU, and if the 'auto-stack' option is enabled, the
	 * abilities are all added sequentially added to the stack.
	 * 
	 * @return true if NO triggered ability has been found in the TBZ.
	 */
	private boolean waitTriggeredBufferChoiceRec() {
		if (processHiddenTriggered()) {
			// The triggered buffer zone has just handled this wait
			return false;
		}

		/*
		 * The triggered buffer zone contains no more abstract abilities. The
		 * remaining abilities are normal ones and must be chosen by players to
		 * determine the order they go to the stack.
		 */
		if (zoneManager.triggeredBuffer.getCardCount() > 0) {
			// Try to get automatically the ability to play
			TriggeredCard triggered = zoneManager.triggeredBuffer.chooseAbility();
			if (triggered != null) {
				/*
				 * only one ability has been found
				 */
				StackManager.idActivePlayer = idPlayer;
				StackManager.actionManager.succeedClickOn(triggered);
			} else if (zoneManager.triggeredBuffer.getCardCount() > 1
					&& MCommonVars.autoStack && isYou()) {
				// Several abilities have been found, but 'auto-stack' option is on.
				StackManager.idActivePlayer = idPlayer;
				Log.debug("\t>>>> outAuto :" + counter++);
				TriggeredCard triggered0 = (TriggeredCard) StackManager.PLAYERS[0].zoneManager.triggeredBuffer
						.getComponent(0);
				Synchronizer.setAsHanded();
				Log.debug("clickedOn triggeredcard " + triggered0
						+ "- AUTO STACK OPTION");
				triggered0.sendClickToOpponent();
				StackManager.actionManager.succeedClickOn(triggered0);
			} else if (zoneManager.triggeredBuffer.getCardCount() == 0) {
				/*
				 * There is no more playable ability, they have been all been purged
				 * during the "re check" process in the chooseAbility() method.
				 */
				return true;
			} else {
				/*
				 * player gets the hand to choose the order the triggered abilities go
				 * to the stack
				 */
				Log.debug("\t>>>> outPrompt :" + counter++ + "(idPlayer=" + idPlayer
						+ ", triggered size=" + zoneManager.triggeredBuffer.getCardCount()
						+ ")");
				setActivePlayer();
				zoneManager.triggeredBuffer.highlightStackable();
			}
			/*
			 * At least one triggered ability has been found. If we are in the
			 * End-of-Phase event, we break the stack resolution in order to
			 * re-dispatch this event later.
			 */
			return false;
		}

		// no triggered ability has been found
		if (idPlayer == StackManager.idActivePlayer) {
			// now, non-active player stacks the waiting triggered abilities
			return getOpponent().waitTriggeredBufferChoiceRec();
		}
		return true;
	}

	static int counter = 0;

	@Override
	public void mouseClicked(MouseEvent e) {
		// only if left button is pressed
		StackManager.noReplayToken.take();
		try {
			if (ConnectionManager.isConnected()
					&& e.getButton() == MouseEvent.BUTTON1
					&& StackManager.idHandedPlayer == 0
					&& StackManager.actionManager.clickOn(this)) {
				Log.debug("clickOn(Mouse):player");
				sendClickToOpponent();
				StackManager.actionManager.succeedClickOn(this);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			StackManager.noReplayToken.release();
		}
	}

	/**
	 * Initialize the UI of this player.
	 */
	public void initUI() {
		handSplitter = new JSplitPane();
		handSplitter.setDividerSize(12);
		handSplitter.setOneTouchExpandable(true);
		handSplitter.setAutoscrolls(true);
		handSplitter.setOpaque(false);
		handSplitter.setBorder(null);

		setToolTipText(LanguageManager.getString(getClass().getSimpleName()));
		setForeground(Color.RED);
		setBorder(new EtchedBorder());
		addMouseListener(this);
		setPreferredSize(new Dimension(130, PLAYER_SIZE_HEIGHT));
		playerCard = new PlayerCard(this);
		avatarButton = new AvatarButton();
		avatarButton.setMinimumSize(new Dimension(1, 180));
		avatarButton.addMouseListener(this);
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(null);
		mainPanel.add(handSplitter, BorderLayout.CENTER);
		infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

		lifeLabel = new JLabel("", UIHelper.getIcon("life.gif"), 0);
		lifeLabel.setBackground(Color.pink);
		lifeLabel.setForeground(new Color(0, 153, 153));
		lifeLabel.setToolTipText(LanguageManager.getString("life"));
		lifeLabel.setBorder(new EtchedBorder());
		lifeLabel.setIconTextGap(6);
		lifeLabel.setPreferredSize(new Dimension(45, PLAYER_SIZE_HEIGHT));
		lifeLabel.setOpaque(true);

		poisonLabel = new JLabel("", UIHelper.getIcon("poison.gif"), 0);
		poisonLabel.setBackground(new Color(0, 102, 0));
		poisonLabel.setForeground(new Color(153, 255, 153));
		poisonLabel.setToolTipText(LanguageManager.getString("poison"));
		poisonLabel.setBorder(new EtchedBorder());
		poisonLabel.setIconTextGap(6);
		poisonLabel.setPreferredSize(new Dimension(45, PLAYER_SIZE_HEIGHT));
		poisonLabel.setOpaque(true);

		phases = new JPanel();
		phases.setLayout(new BoxLayout(phases, BoxLayout.Y_AXIS));
		phases.setOpaque(false);
		phases.setMinimumSize(new Dimension(43, PLAYER_SIZE_WIDTH));

		final JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));

		// nick name
		nickNamePanel = new JPanel();
		nickNamePanel.setLayout(new BoxLayout(nickNamePanel, BoxLayout.X_AXIS));
		final JLabel nickLabel = new JLabel(UIHelper.getIcon("nickname.gif"));
		nickLabel.setPreferredSize(new Dimension(18, 18));
		nickNamePanel.add(nickLabel);
		namePanel.add(nickNamePanel);

		// real name
		realNamePanel = new JPanel();
		realNamePanel.setLayout(new BoxLayout(realNamePanel, BoxLayout.X_AXIS));
		// by default the real name panel is not visible
		realNamePanel.setVisible(false);
		final JLabel realLabel = new JLabel(UIHelper.getIcon("id.gif"));
		realLabel.setPreferredSize(new Dimension(18, 18));
		realNamePanel.add(realLabel);
		namePanel.add(realNamePanel);

		// avatar picture
		morePanel.add(avatarButton, BorderLayout.CENTER);
		morePanel.add(namePanel, BorderLayout.NORTH);

		// toggle buttons
		togglePanel = new JPanel();
		togglePanel.setLayout(new BoxLayout(togglePanel, BoxLayout.X_AXIS));
		morePanel.add(togglePanel, BorderLayout.SOUTH);

		abilitiesPanel = new JPanel();
		abilitiesPanel.setLayout(new BoxLayout(abilitiesPanel, BoxLayout.Y_AXIS));
		morePanel.add(abilitiesPanel, BorderLayout.SOUTH);
		handedText = LanguageManager.getString("handed-"
				+ getClass().getSimpleName().toLowerCase());
		zoneManager.initUI();
	}

	/**
	 * This method is invoked when opponent has clicked on this object. this call
	 * should be done from the listener.
	 * 
	 * @param data
	 *          data sent by opponent.
	 */
	public static void clickOn(byte[] data) {
		// waiting for player information
		Log.debug("clickOn(VirtualInput):player");
		StackManager.actionManager.succeedClickOn(StackManager.PLAYERS[data[0]]);
	}

	@Override
	public void sendClickToOpponent() {
		// send this information to our opponent
		ConnectionManager.send(CoreMessageType.CLICK_PLAYER, (byte) (1 - idPlayer));
	}

	@Override
	public int getValue(int index) {
		if (index == IdTokens.MANA_POOL) {
			if (StackManager.actionManager.idHandler == ActionManager.HANDLER_INITIALIZATION
					&& StackManager.getSpellController() == this) {
				return mana.allManas()
						- StackManager.getTotalManaCost(StackManager.tokenCard);
			}
			return mana.allManas();
		}
		if (index == IdTokens.ID) {
			return getId();
		}
		if (index < IdTokens.FIRST_FREE_CARD_INDEX) {
			return registers[index];
		}
		// TODO support modifiers for player here
		return registers[index];
	}

	@Override
	public int getTimestamp() {
		return 0;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (StackManager.currentAbility != null
				&& StackManager.actionManager.currentAction != null
				&& StackManager.actionManager.currentAction instanceof ChosenTarget) {
			// append the "this is [not] a valid target" text
			if (((ChosenTarget) StackManager.actionManager.currentAction)
					.isValidTarget(this)) {
				setToolTipText("<html>" + getNickName()
						+ TargetFactory.tooltipValidTarget);
			} else {
				setToolTipText("<html>" + getNickName()
						+ TargetFactory.tooltipInvalidTarget);
			}
		} else {
			setToolTipText(getNickName());
		}
	}

	@Override
	public void removeModifier(RegisterModifier modifier, int index) {
		throw new InternalError("not yet implemented");
	}

	@Override
	public void removeModifier(RegisterIndirection indirection, int index) {
		throw new InternalError("not yet implemented");
	}

	@Override
	public Target getLastKnownTargetable(int timeStamp) {
		return this;
	}

	/**
	 * Increment the reference counter for the current timestamp of this card.
	 */
	@Override
	public void addTimestampReference() {
		// Nothing to do
	}

	@Override
	public void decrementTimestampReference(int timestamp) {
		// Nothing to do
	}

	/**
	 * Set the icon (32x32) of this player
	 * 
	 * @param imageIcon
	 *          the small avatar.
	 */
	protected void init(Image imageIcon) {
		this.imageIcon = imageIcon;
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		// draw the highlighted rectangle
		super.paint(g);
		final Graphics2D g2D = (Graphics2D) g;
		if (!isYou() && Configuration.getBoolean("reverseSide", false)) {
			g2D.translate(getWidth() - 1, getHeight() - 1);
			g2D.rotate(Math.PI);
		}
		g2D.drawImage(imageIcon, 3, 4, null);
		if (isHighLighted) {
			g.setColor(highLightColor);
			g.draw3DRect(0, 0, getWidth() - 1, getHeight() - 1, true);
			g.draw3DRect(1, 1, getWidth() - 3, getHeight() - 3, true);
		}

		// Draw the target Id if helper said it
		final String id = TargetHelper.getInstance().getMyId(this);
		if (id != null) {
			if (id == TargetHelper.STR_CONTEXT1) {
				// TODO I am in the context 1, draw a picture
				g2D.setColor(Color.BLUE);
				g2D
						.setFont(g2D.getFont()
								.deriveFont(Font.BOLD, PLAYER_SIZE_HEIGHT - 4));
				g2D.drawString(String.valueOf(id), 25, PLAYER_SIZE_HEIGHT - 2);
			} else if (id == TargetHelper.STR_CONTEXT2) {
				// TODO I am in the context 2, draw a picture
				g2D.setColor(Color.BLUE);
				g2D
						.setFont(g2D.getFont()
								.deriveFont(Font.BOLD, PLAYER_SIZE_HEIGHT - 4));
				g2D.drawString(String.valueOf(id), 25, PLAYER_SIZE_HEIGHT - 2);
			} else if (id != TargetHelper.STR_SOURCE) {
				// } else if (id == TargetHelper.STR_SOURCE) {
				// TODO I am the source, draw a picture
				// } else {
				// I am a target
				g2D.drawImage(TargetHelper.getInstance().getTargetPictureSml(), 30, 5,
						null);
			}
		}

		g2D.dispose();
	}

	@Override
	public int getId() {
		return idPlayer;
	}

	/**
	 * where all triggered abilities would go before to go to the stack
	 */
	public ZoneManager zoneManager;

	/**
	 * id of the player
	 */
	public int idPlayer;

	/**
	 * is the manas of this player
	 */
	public ManaPool mana;

	/**
	 * This card is used to represent this player as a card/
	 */
	public PlayerCard playerCard;

	/**
	 * The button containig the avatar picture
	 */
	protected AvatarButton avatarButton;

	/**
	 * Available string settings of any player.
	 */
	protected static final String[] SETTINGS = { "email", "yahoo", "msn", "icq" };

	/**
	 * Panel containing some information about player
	 */
	protected JPanel morePanel;

	/**
	 * The label representing player's lives.
	 */
	protected JLabel lifeLabel;

	/**
	 * The label representing player's poison.
	 */
	protected JLabel poisonLabel;

	/**
	 * The panel containing the hand and the play
	 */
	public JPanel mainPanel;

	/**
	 * The main panel containing player's lives+poison+mana+buttons.
	 */
	protected JPanel infoPanel;

	/**
	 * The panel representing player's phases.
	 */
	protected JPanel phases;

	/**
	 * Abilities panel (not yet implemented)
	 */
	protected JPanel abilitiesPanel;

	/**
	 * The splitter of game/hand zones.
	 */
	public JSplitPane handSplitter;

	/**
	 * The panel containing all icons representing contact info.
	 */
	protected JPanel togglePanel;

	/**
	 * Nickname panel : button and label.
	 */
	protected JPanel nickNamePanel;

	/**
	 * real name panel : button and label.
	 */
	protected JPanel realNamePanel;

	private Image imageIcon;

	/**
	 * The deck of this player.
	 */
	private Deck deck;

	private static final String HANDED_NOBODY = LanguageManager
			.getString("handed-nobody");

	/**
	 * Set the deck of this player.
	 * 
	 * @param deck
	 *          the deck of this player.
	 */
	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	/**
	 * Return the current deck of player.
	 * 
	 * @return the current deck of player.
	 */
	public Deck getDeck() {
		return deck;
	}
}