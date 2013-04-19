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
package net.sf.firemox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import net.sf.firemox.chart.CardColor;
import net.sf.firemox.chart.CardManaCost;
import net.sf.firemox.chart.CardTypes;
import net.sf.firemox.chart.ChartFilter;
import net.sf.firemox.chart.ChartSets;
import net.sf.firemox.chart.IChartKey;
import net.sf.firemox.chart.IDataProvider;
import net.sf.firemox.chart.datasets.Dataset;
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.card.CardModel;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.deckbuilder.CardLoader;
import net.sf.firemox.deckbuilder.CardView;
import net.sf.firemox.deckbuilder.ConstraintsChecker;
import net.sf.firemox.deckbuilder.Deck;
import net.sf.firemox.deckbuilder.DeckConstraints;
import net.sf.firemox.deckbuilder.DeckReader;
import net.sf.firemox.deckbuilder.DeckRules;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.token.IdCardColors;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Converter;
import net.sf.firemox.tools.FileFilterPlus;
import net.sf.firemox.tools.MCardCompare;
import net.sf.firemox.tools.MSaveDeck;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.ui.HireListener;
import net.sf.firemox.ui.MCardTableModel;
import net.sf.firemox.ui.MListModel;
import net.sf.firemox.ui.RefreshableAdd;
import net.sf.firemox.ui.TimerGlassPane;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.component.ThreadSafeJList;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.wizard.About;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * This class is a deck builder compatible with Firemox deck format. Can
 * also export/import other formats.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @author <a href="mailto:goldeneyemdk@users.sourceforge.net">Sebastien Genete
 *         </a>
 * @author Jan Blaha for deck loader and statistics
 * @since 0.2 2003/12/06, settings are loaded.
 * @since 0.2 you can load a deck file.
 * @since 0.2 you can load a specified MDB file (is MagicProject card list
 *        file).
 * @since 0.3 2003/12/12, you can save a deck file.
 * @since 0.3 new deck feature.
 * @since 0.4 sort MDB file feature for optimized load.
 * @since 0.4 deck are saved with alphabetically sort.
 * @since 0.5 statistics feature added.
 * @since 0.81 deck converter added.
 * @since 0.83 cleaned code, deck constraints form added.
 * @since 0.93 pie chart added, color filtering
 * @since 0.94 use of JTable instead of JList
 */
public final class DeckBuilder extends AbstractMainForm implements
		ListSelectionListener, RefreshableAdd, IDataProvider, TableModelListener {

	private static final int WINDOW_WIDTH = 700;

	private static final int WINDOW_HEIGHT = 700;

	/**
	 * Card loader bar
	 */
	protected CardLoader cardLoader;

	/**
	 * Load timer
	 */
	protected Timer timer;

	private final TimerGlassPane timerPanel;

	private JComponent listScrollerLeft;

	/**
	 * Creates new form DeckBuilder
	 */
	private DeckBuilder() {
		super("DeckBuilder");
		form = this;
		timerPanel = new TimerGlassPane();
		cardLoader = new CardLoader(timerPanel);
		timer = new Timer(200, cardLoader);
		setGlassPane(timerPanel);
		try {
			setIconImage(Picture.loadImage(IdConst.IMAGES_DIR + "deckbuilder.gif"));
		} catch (Exception e) {
			// IGNORING
		}

		// Load settings
		loadSettings();

		// Initialize components
		final JMenuItem newItem = UIHelper.buildMenu("menu_db_new", 'n', this);
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				InputEvent.CTRL_MASK));

		final JMenuItem loadItem = UIHelper.buildMenu("menu_db_load", 'o', this);
		loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_MASK));

		final JMenuItem saveAsItem = UIHelper
				.buildMenu("menu_db_saveas", 'a', this);
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

		final JMenuItem saveItem = UIHelper.buildMenu("menu_db_save", 's', this);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));

		final JMenuItem quitItem = UIHelper.buildMenu("menu_db_exit", this);
		quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				InputEvent.ALT_MASK));

		final JMenuItem deckConstraintsItem = UIHelper.buildMenu(
				"menu_db_constraints", 'c', this);
		deckConstraintsItem.setAccelerator(KeyStroke
				.getKeyStroke(KeyEvent.VK_F3, 0));

		final JMenuItem aboutItem = UIHelper
				.buildMenu("menu_help_about", 'a', this);
		aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,
				InputEvent.SHIFT_MASK));

		final JMenuItem convertDCK = UIHelper
				.buildMenu("menu_convert_DCK_MP", this);

		final JMenu mainMenu = UIHelper.buildMenu("menu_file");
		mainMenu.add(newItem);
		mainMenu.add(loadItem);
		mainMenu.add(saveAsItem);
		mainMenu.add(saveItem);
		mainMenu.add(new JSeparator());
		mainMenu.add(quitItem);

		super.optionMenu = new JMenu("Options");

		final JMenu convertMenu = UIHelper.buildMenu("menu_convert");
		convertMenu.add(convertDCK);

		final JMenuItem helpItem = UIHelper.buildMenu("menu_help_help", 'h', this);
		helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		final JMenu helpMenu = new JMenu("?");
		helpMenu.add(helpItem);
		helpMenu.add(deckConstraintsItem);
		helpMenu.add(aboutItem);

		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(mainMenu);
		initAbstractMenu();
		menuBar.add(optionMenu);
		menuBar.add(convertMenu);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);
		addWindowListener(this);

		// Build the panel containing amount of available cards
		final JLabel amountLeft = new JLabel("<html>0/?", SwingConstants.RIGHT);

		// Build the left list
		allListModel = new MListModel<MCardCompare>(amountLeft, false);
		leftList = new ThreadSafeJList(allListModel);
		leftList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		leftList.setLayoutOrientation(JList.VERTICAL);
		leftList.getSelectionModel().addListSelectionListener(this);
		leftList.addMouseListener(this);
		leftList.setVisibleRowCount(10);

		// Initialize the text field containing the amount to add
		addQtyTxt = new JTextField("1");

		// Build the "Add" button
		addButton = new JButton(LanguageManager.getString("db_add"));
		addButton.setMnemonic('a');
		addButton.setEnabled(false);

		// Build the panel containing : "Add" amount and "Add" button
		final Box addPanel = Box.createHorizontalBox();
		addPanel.add(addButton);
		addPanel.add(addQtyTxt);
		addPanel.setMaximumSize(new Dimension(32010, 26));

		// Build the panel containing the selected card name
		cardNameTxt = new JTextField();
		new HireListener(cardNameTxt, addButton, this, leftList);

		final JLabel searchLabel = new JLabel(LanguageManager
				.getString("db_search")
				+ " : ");
		searchLabel.setLabelFor(cardNameTxt);

		// Build the panel containing search label and card name text field
		final Box searchPanel = Box.createHorizontalBox();
		searchPanel.add(searchLabel);
		searchPanel.add(cardNameTxt);
		searchPanel.setMaximumSize(new Dimension(32010, 26));

		listScrollerLeft = new JScrollPane(leftList);
		MToolKit.addOverlay(listScrollerLeft);

		// Build the left panel containing : list, available amount, "Add" panel
		final JPanel srcPanel = new JPanel(null);
		srcPanel.add(searchPanel);
		srcPanel.add(listScrollerLeft);
		srcPanel.add(amountLeft);
		srcPanel.add(addPanel);
		srcPanel.setMinimumSize(new Dimension(220, 200));
		srcPanel.setLayout(new BoxLayout(srcPanel, BoxLayout.Y_AXIS));

		// Initialize constraints
		constraintsChecker = new ConstraintsChecker();
		constraintsChecker.setBorder(new EtchedBorder());
		final JScrollPane constraintsCheckerScroll = new JScrollPane(
				constraintsChecker);
		MToolKit.addOverlay(constraintsCheckerScroll);

		// create a pane with the oracle text for the present card
		oracleText = new JLabel();
		oracleText.setPreferredSize(new Dimension(180, 200));
		oracleText.setVerticalAlignment(SwingConstants.TOP);

		final JScrollPane oracle = new JScrollPane(oracleText,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		MToolKit.addOverlay(oracle);

		// build some Pie Charts and a panel to display it
		initSets();
		datasets = new ChartSets();
		final JTabbedPane tabbedPane = new JTabbedPane();
		for (ChartFilter filter : ChartFilter.values()) {
			final Dataset dataSet = filter.createDataSet(this);
			final JFreeChart chart = new JFreeChart(null, null, filter.createPlot(
					dataSet, painterMapper.get(filter)), false);
			datasets.addDataSet(filter, dataSet);
			ChartPanel pieChartPanel = new ChartPanel(chart, true);
			tabbedPane.add(pieChartPanel, filter.getTitle());
		}
		// add the Constraints scroll panel and Oracle text Pane to the tabbedPane
		tabbedPane.add(constraintsCheckerScroll, LanguageManager
				.getString("db_constraints"));
		tabbedPane.add(oracle, LanguageManager.getString("db_text"));
		tabbedPane.setSelectedComponent(oracle);

		// The toollBar for color filtering
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		final JButton clearButton = UIHelper.buildButton("clear");
		clearButton.addActionListener(this);
		toolBar.add(clearButton);
		final JToggleButton toggleColorlessButton = new JToggleButton(UIHelper
				.getTbsIcon("mana/colorless/small/" + MdbLoader.unknownSmlMana), true);
		toggleColorlessButton.setActionCommand("0");
		toggleColorlessButton.addActionListener(this);
		toolBar.add(toggleColorlessButton);
		for (int index = 1; index < IdCardColors.CARD_COLOR_NAMES.length; index++) {
			final JToggleButton toggleButton = new JToggleButton(
					UIHelper.getTbsIcon("mana/colored/small/"
							+ MdbLoader.coloredSmlManas[index]), true);
			toggleButton.setActionCommand(String.valueOf(index));
			toggleButton.addActionListener(this);
			toolBar.add(toggleButton);
		}

		// sorted card type combobox creation
		final List<String> idCards = new ArrayList<String>(Arrays
				.asList(CardFactory.exportedIdCardNames));
		Collections.sort(idCards);
		final Object[] cardTypes = ArrayUtils.addAll(new String[] { LanguageManager
				.getString("db_types.any") }, idCards.toArray());
		idCardComboBox = new JComboBox(cardTypes);
		idCardComboBox.setSelectedIndex(0);
		idCardComboBox.addActionListener(this);
		idCardComboBox.setActionCommand("cardTypeFilter");

		// sorted card properties combobox creation
		final List<String> properties = new ArrayList<String>(CardFactory
				.getPropertiesName(DeckConstraints.getMinProperty(), DeckConstraints
						.getMaxProperty()));
		Collections.sort(properties);
		final Object[] cardProperties = ArrayUtils.addAll(
				new String[] { LanguageManager.getString("db_properties.any") },
				properties.toArray());
		propertiesComboBox = new JComboBox(cardProperties);
		propertiesComboBox.setSelectedIndex(0);
		propertiesComboBox.addActionListener(this);
		propertiesComboBox.setActionCommand("propertyFilter");

		final JLabel colors = new JLabel(" " + LanguageManager.getString("colors")
				+ " : ");
		final JLabel types = new JLabel(" " + LanguageManager.getString("types")
				+ " : ");
		final JLabel property = new JLabel(" "
				+ LanguageManager.getString("properties") + " : ");

		// filter Panel with colors toolBar and card type combobox
		final Box filterPanel = Box.createHorizontalBox();
		filterPanel.add(colors);
		filterPanel.add(toolBar);
		filterPanel.add(types);
		filterPanel.add(idCardComboBox);
		filterPanel.add(property);
		filterPanel.add(propertiesComboBox);

		getContentPane().add(filterPanel, BorderLayout.NORTH);

		// Destination section :

		// Build the panel containing amount of available cards
		final JLabel rightAmount = new JLabel("0/?", SwingConstants.RIGHT);
		rightAmount.setMaximumSize(new Dimension(220, 26));

		// Build the right list
		rightListModel = new MCardTableModel(new MListModel<MCardCompare>(
				rightAmount, true));
		rightListModel.addTableModelListener(this);
		rightList = new JTable(rightListModel);
		rightList.setShowGrid(false);
		rightList.setTableHeader(null);
		rightList.getSelectionModel().addListSelectionListener(this);
		rightList.getColumnModel().getColumn(0).setMaxWidth(25);
		rightList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

		// Build the panel containing the selected deck
		deckNameTxt = new JTextField("loading...");
		deckNameTxt.setEditable(false);
		deckNameTxt.setBorder(null);
		final JLabel deckLabel = new JLabel(LanguageManager.getString("db_deck")
				+ " : ");
		deckLabel.setLabelFor(deckNameTxt);
		final Box deckNamePanel = Box.createHorizontalBox();
		deckNamePanel.add(deckLabel);
		deckNamePanel.add(deckNameTxt);
		deckNamePanel.setMaximumSize(new Dimension(220, 26));

		// Initialize the text field containing the amount to remove
		removeQtyTxt = new JTextField("1");

		// Build the "Remove" button
		removeButton = new JButton(LanguageManager.getString("db_remove"));
		removeButton.setMnemonic('r');
		removeButton.addMouseListener(this);
		removeButton.setEnabled(false);

		// Build the panel containing : "Remove" amount and "Remove" button
		final Box removePanel = Box.createHorizontalBox();
		removePanel.add(removeButton);
		removePanel.add(removeQtyTxt);
		removePanel.setMaximumSize(new Dimension(220, 26));

		// Build the right panel containing : list, available amount, constraints
		final JScrollPane deskListScroller = new JScrollPane(rightList);
		MToolKit.addOverlay(deskListScroller);
		deskListScroller.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		deskListScroller.setMinimumSize(new Dimension(220, 200));
		deskListScroller.setMaximumSize(new Dimension(220, 32000));

		final Box destPanel = Box.createVerticalBox();
		destPanel.add(deckNamePanel);
		destPanel.add(deskListScroller);
		destPanel.add(rightAmount);
		destPanel.add(removePanel);
		destPanel.setMinimumSize(new Dimension(220, 200));
		destPanel.setMaximumSize(new Dimension(220, 32000));

		// Build the panel containing the name of card in picture
		cardPictureNameTxt = new JLabel("<html><i>no selected card</i>");
		final Box cardPictureNamePanel = Box.createHorizontalBox();
		cardPictureNamePanel.add(cardPictureNameTxt);
		cardPictureNamePanel.setMaximumSize(new Dimension(32010, 26));

		// Group the detail panels
		final JPanel viewCard = new JPanel(null);
		viewCard.add(cardPictureNamePanel);
		viewCard.add(CardView.getInstance());
		viewCard.add(tabbedPane);
		viewCard.setLayout(new BoxLayout(viewCard, BoxLayout.Y_AXIS));

		final Box mainPanel = Box.createHorizontalBox();
		mainPanel.add(destPanel);
		mainPanel.add(viewCard);

		// Add the main panel
		getContentPane().add(srcPanel, BorderLayout.WEST);
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		// Size this frame
		getRootPane().setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		getRootPane().setMinimumSize(getRootPane().getPreferredSize());
		pack();
	}

	/**
	 * Load a given deck file into the given list.
	 * 
	 * @param fileName
	 *          deck file name.
	 * @param names
	 *          loaded cards to complete.
	 */
	protected void loadDeck(String fileName, MListModel<MCardCompare> names) {
		// empty file name or deck?
		if (fileName == null || fileName.length() == 0) {
			return;
		}

		// Append the deck name
		final String deckName = FilenameUtils.removeExtension(FilenameUtils
				.getName(fileName));
		setTitle("DeckBuilder : " + deckName);
		deckNameTxt.setText(deckName);

		names.clear();
		datasets.removeAll();
		try {
			Deck currentDeck = DeckReader.getDeck(this, fileName);
			if (currentDeck == null) {
				// Deck loading failure
				return;
			}

			// Validate this deck
			DeckReader.validateDeck(this, currentDeck, (String) null);

			// Update the card models of resolved cards
			final FileInputStream dbStream = MdbLoader.resetMdb();
			for (MCardCompare cardCompare : currentDeck.getCards()) {
				names.add(cardCompare);
				datasets.addCard(cardCompare.getModel(dbStream), cardCompare
						.getAmount());
			}
			constraintsChecker.setDeck(currentDeck);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			updateChecker();
			setAsSaved();
		}
	}

	private void setCard(MCardCompare card) {
		CardView.getInstance().setCard(card);
		cardPictureNameTxt.setText(card.getName());
		FileInputStream dbStream = MdbLoader.resetMdb();
		try {
			oracleText.setText("<html>Oracle Rules:<br><br>"
					+ ((MCard) card.getCard(dbStream)).getDatabase().getProperty(
							"card.text"));
		} catch (IOException e) {
			// ignore
			e.printStackTrace();
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			if (e.getSource() == leftList.getSelectionModel()) {
				if (leftList.getSelectedIndex() == -1 || allListModel.isEmpty()) {
					// No selection, disable fire button.
					addButton.setEnabled(false);
				} else {
					// Selection, enable the fire button.
					addButton.setEnabled(true);

					// Update the views
					final MCardCompare selectedCard = (MCardCompare) leftList
							.getSelectedValue();
					cardNameTxt.setText(selectedCard.getName());
					setCard(selectedCard);
				}
			} else if (e.getSource() == rightList.getSelectionModel()) {
				if (rightList.getSelectedRow() == -1) {
					// No selection, disable fire button.
					removeButton.setEnabled(false);
				} else {
					// Selection, enable the fire button.
					removeButton.setEnabled(true);

					// Update the views
					setCard(rightListModel.getCards().getElementAt(
							rightList.getSelectedRow()));
				}
			}
		}
	}

	/**
	 * Exit the Application
	 */
	private void exitForm() {
		verifyModification();
		if (consoleMode) {
			System.exit(0);
		} else {
			setVisible(false);
		}
	}

	/**
	 * Load the deck builder from the command line.
	 * 
	 * @param args
	 *          the command line arguments
	 */
	public static void main(String[] args) {
		consoleMode = true;
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		autoLoad();
	}

	/**
	 * Load the deck builder from Magic class.
	 */
	public static void loadFromMagic() {
		consoleMode = false;
		autoLoad();
	}

	/**
	 * Load the deck builder
	 */
	private static void autoLoad() {
		if (form == null) {
			form = new DeckBuilder();
			form.setLocation(
					(form.getToolkit().getScreenSize().width - WINDOW_WIDTH) / 2, (form
							.getToolkit().getScreenSize().height - WINDOW_HEIGHT) / 2);
		}
		form.setVisible(true);
		if (form.allListModel.isEmpty()) {
			form.timerPanel.setVisible(true);
			form.timer.start();
			new Thread(
			/**
			 * This class refresh the loading bar
			 */
			new Runnable() {
				public void run() {
					// Populate available cards and fill them into the left list
					form.readAvailableCards(form.allListModel);

					// Load the last deck file
					if (Configuration.getString("decks.deck(0)", "").length() > 0) {
						form.loadDeck(Configuration.getString("decks.deck(0)"),
								form.rightListModel.getCards());
						form.rightListModel.refresh();
					} else {
						form.setAsNew();
					}

					form.timer.stop();
					form.cardLoader.resetCounter();
				}
			}).start();
		}
	}

	/**
	 * Adding a card to the deck
	 * 
	 * @param unitary
	 */
	public void refreshAddComponent(boolean unitary) {

		if (leftList.isSelectionEmpty())
			return;

		String name = leftList.getSelectedValue().toString();
		int qty = 1;
		if (!unitary) {
			try {
				qty = Integer.parseInt(addQtyTxt.getText());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(form, "Malformed integer for quantity",
						"Internal Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		// User didn't type in a unique name...
		if (name.length() == 0) {
			cardNameTxt.requestFocusInWindow();
			return;
		}

		int index = rightListModel.getCards().indexOf(name);
		final MCardCompare card;
		if (index == -1) {
			// no selection, so insert at beginning
			int allIndex = allListModel.indexOf(name);
			assert allIndex != -1;
			card = allListModel.getElementAt(allIndex).clone();
			card.add(qty);
			rightListModel.getCards().insertElementAt(card, 0);
			rightListModel.fireTableRowsInserted(0, 0);

			// Reset the text field.
			cardNameTxt.requestFocusInWindow();
			cardNameTxt.setText("");
		} else { // add after the selected item
			rightListModel.getCards().getElementAt(index).add(qty);
			rightListModel.fireTableCellUpdated(index, 0);
		}

		// Update the card models of resolved cards
		final FileInputStream dbStream = MdbLoader.resetMdb();
		datasets.addCard(getCardModel(name, dbStream), qty);

		// deck list has been modified
		modifiedSinceSave = true;
	}

	/**
	 * removes cards from current deck according to the remove panel
	 */
	private void removeCardFromDeck() {

		final MCardCompare card = rightListModel.getCards().getElementAt(
				rightList.getSelectedRow());

		int qty = 1;

		try {
			qty = Integer.parseInt(removeQtyTxt.getText());
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(form, "Malformed integer for quantity",
					"Internal Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		card.remove(qty);

		if (card.getAmount() <= 0) {
			rightListModel.getCards().remove(rightList.getSelectedRow());
			rightListModel.fireTableRowsDeleted(0, rightList.getRowCount());
		} else
			rightListModel.fireTableCellUpdated(rightListModel.getCards().indexOf(
					card.getName()), 0);

		final FileInputStream dbStream = MdbLoader.resetMdb();
		datasets.removeCard(getCardModel(card.getName(), dbStream), qty);
		modifiedSinceSave = true;

	}

	/**
	 * Update the checker and repaint the right list.
	 */
	private void updateChecker() {
		constraintsChecker.updateCheckers();
		rightList.repaint();
	}

	/**
	 * Return the associated cardModel to the given real card name. The card is
	 * searched in the input starting from a given offset.
	 * 
	 * @param realCardName
	 *          the card name as written in the MDB stream.
	 * @param input
	 *          the input stream of current MDB.
	 * @return the corresponding CardModel instance, or null if the card has not
	 *         been matched.
	 */
	private CardModel getCardModel(String realCardName, FileInputStream input) {
		try {
			final int index = allListModel.indexOf(realCardName);
			final MCardCompare card = allListModel.getElementAt(index);
			return card.getModel(input);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * check if the current deck has been modified since it has been saved. If the
	 * current deck has been modified, we ask to the user if he want to save it
	 * before loading another.
	 * 
	 * @return true if the user has chosen to save the deck before continuing and
	 *         if the save has success. False otherwise
	 */
	private boolean verifyModification() {
		if (!modifiedSinceSave) {
			// no modification since the last save
			return true;
		}

		// ask to the user if we save the current deck before loading another
		Object[] options = { "Ok", "No", "Cancel" };
		switch (JOptionPane.showOptionDialog(form,
				"Current deck has been modified, save it before loading another?",
				"Save changes", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, null)) {
		case 0:
			// YES part
			return saveCurrentDeck();
		case 1:
			// NO part
			return true;
		default:
			// CANCEL part
			return false;
		}
	}

	/**
	 * Save the current deck. If the current deck has not been saved once, user
	 * have to specify the file's name of this new deck
	 * 
	 * @return true if the current deck has been correctly saved. false otherwise.
	 */
	private boolean saveCurrentDeck() {
		if (isNew || Configuration.getString("decks.deck(0)", "").length() == 0
				&& MToolKit.getDeckFile(this, JFileChooser.SAVE_DIALOG) == null) {
			return false;
		}
		return MSaveDeck.saveDeck(Configuration.getString("decks.deck(0)"),
				rightListModel.getCards(), form);
	}

	/**
	 * load the settings of jDecBuilder First byte is the Version Then :
	 * 
	 * @since 0.2 the MDB file
	 * @since 0.2 the last deck file used
	 * @since 0.7 the settings are read from .properties file
	 */
	private void loadSettings() {
		// read the MDB file
		if (consoleMode || MToolKit.tbsName == null) {
			/*
			 * The jDeckbuilder has not been launched from Magic form, or no TBS is
			 * currently defined, we load the last used TBS.
			 */
			LanguageManager.initLanguageManager(Configuration.getString("language",
					"auto"));
			optionMenu = new JMenu(LanguageManager.getString("options"));
			optionMenu.setMnemonic('o');
			initAbstractMenu();
			final String tbsName = Configuration.getString("lastTBS");
			if (tbsName != null && tbsName.length() != 0) {
				setMdb(tbsName);
			}
		}
		// read the last used deck file
	}

	/**
	 * Invoked when an action occurs.
	 * 
	 * @param e
	 *          attached event
	 */
	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if ("menu_help_about".equals(command)) {
			new About(form).setVisible(true);
		} else if ("menu_db_exit".equals(command)) {
			exitForm();
		} else if ("menu_db_new".equals(command)) {
			if (!verifyModification()) {
				return;
			}
			setTitle("DeckBuilder");
			deckNameTxt.setText("");
			rightListModel.getCards().clear();
			datasets.removeAll();
			modifiedSinceSave = false;
			updateChecker();
			setAsNew();
		} else if ("menu_convert_DCK_MP".equals(command)) {
			/*
			 * Convert an entire directory from a format to MP one
			 */
			try {
				Converter.convertDCK(MToolKit.showDialogFile(
						"Choose a directory of DCK to convert", 'o', null, FILTER_DECK,
						this, JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY)
						.getCanonicalPath());
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this,
						"Error occurred reading the specified directory", "File problem",
						JOptionPane.ERROR_MESSAGE);
			} catch (Exception e1) {
				// cancel of user;
			}
		} else if ("menu_db_constraints".equals(command)) {
			// Show the deck constraints applied on the current TBS
			new DeckRules(this).setVisible(true);
		} else if ("menu_help_help".equals(command)) {
			/*
			 * This method is invoked when user has chosen to see the help file. <br>
			 * TODO documentation is not yet done for this form
			 */
			JOptionPane.showMessageDialog(form,
					"Sorry, no documentation available for deck builder",
					"Negative yet implemented", JOptionPane.INFORMATION_MESSAGE);
		} else if ("menu_db_load".equals(command)) {
			if (verifyModification()) {
				String deckFile = MToolKit.getDeckFile(this, JFileChooser.OPEN_DIALOG);
				if (deckFile != null) {
					loadDeck(deckFile, rightListModel.getCards());
				}
			}
		} else if ("menu_db_saveas".equals(command)) {
			String deckFile = MToolKit.getDeckFile(this, JFileChooser.SAVE_DIALOG);
			if (deckFile != null) {
				MSaveDeck.saveDeck(deckFile, rightListModel.getCards(), form);
				setAsSaved();
			}
		} else if ("menu_db_save".equals(command)) {
			saveCurrentDeck();
			setAsSaved();
		} else {
			// several implemented filters
			final MListModel<MCardCompare> model = (MListModel<MCardCompare>) leftList
					.getModel();

			final FileInputStream dbStream = MdbLoader.resetMdb();

			final List<MCardCompare> toRemove = new ArrayList<MCardCompare>();

			if ("clear".equals(command)) {
				// Reset the color filters
				for (Component component : toolBar.getComponents()) {
					if (component instanceof JToggleButton) {
						((JToggleButton) component).setSelected(true);
					}
				}
			}
			model.addAll(model.removedDelegate);

			final int cardType = CardFactory.getIdCard((String) idCardComboBox
					.getSelectedItem());
			if (cardType != -1) {
				// "All" is not selected in card type filter
				// we remove the cards that don't have the selected card id
				for (MCardCompare cardCompare : model.delegate) {
					try {
						final CardModel cardModel = cardCompare.getModel(dbStream);
						if (!MCard.hasIdCard(cardModel.getIdCard(), cardType)) {
							toRemove.add(cardCompare);
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				model.removeAll(toRemove);
				toRemove.clear();
			}

			// property filter
			// we search for the property value, if it isn't found it's because "All"
			// is selected in the comboBox
			int property = CardFactory.getProperty((String) propertiesComboBox
					.getSelectedItem());

			if (property != -1) {
				// "All" is not selected in property filter
				// we remove the cards that don't have the selected property
				for (MCardCompare cardCompare : model.delegate) {
					try {
						final CardModel cardModel = cardCompare.getModel(dbStream);
						if (!MCard.hasProperty(cardModel.getProperties(), property)) {
							toRemove.add(cardCompare);
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				model.removeAll(toRemove);
				toRemove.clear();
			}

			// color filters
			for (int i = 1; i < IdCardColors.CARD_COLOR_VALUES.length + 1; i++) {
				final JToggleButton colorButton = (JToggleButton) toolBar
						.getComponent(i);
				if (!colorButton.isSelected()) {
					for (MCardCompare cardCompare : model.delegate) {
						try {
							final CardModel cardModel = cardCompare.getModel(dbStream);
							if (i == 1) {
								if (cardModel.getIdColor() == 0) {
									toRemove.add(cardCompare);
								}
							} else if ((cardModel.getIdColor() & IdCardColors.CARD_COLOR_VALUES[i - 1]) == IdCardColors.CARD_COLOR_VALUES[i - 1]) {
								toRemove.add(cardCompare);
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					model.removeAll(toRemove);
					toRemove.clear();
				}
			}
			leftList.repaint();
			if (!model.isEmpty())
				leftList.setSelectedIndex(0);
		}
	}

	/**
	 * Set the current TBS name.
	 * 
	 * @param tbsName
	 *          the TBS to define as current.
	 */
	protected void setToolKitMdb(String tbsName) {
		MdbLoader.setToolKitMdb(tbsName);
		readAvailableCards(allListModel);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == leftList && e.getClickCount() == 2) {
			refreshAddComponent(true);
		} else if (e.getSource() == removeButton
				&& rightList.getSelectedRow() != -1) {
			removeCardFromDeck();
		} else if (e.getSource() == cardNameTxt) {
			cardNameTxt.selectAll();
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		exitForm();
	}

	/**
	 * Set the working deck as new.
	 */
	protected void setAsNew() {
		isNew = true;
	}

	/**
	 * Set the working deck as saved.
	 */
	protected void setAsSaved() {
		isNew = false;
		modifiedSinceSave = false;
		String filename = FilenameUtils.removeExtension(FilenameUtils
				.getName(Configuration.getString("decks.deck(0)")));
		setTitle("DeckBuilder : ".concat(filename));
		deckNameTxt.setText(filename);
	}

	/**
	 * Are we working with a new deck?
	 */
	private boolean isNew = false;

	private final ChartSets datasets;

	private Map<ChartFilter, Map<Integer, IChartKey>> keyMapper;

	private Map<ChartFilter, Map<Integer, Paint>> painterMapper;

	private void initSets() {
		final Map<Integer, Paint> painterColorMapper = new TreeMap<Integer, Paint>();
		for (int index = IdCardColors.CARD_COLOR_VALUES.length; index-- > 0;) {
			painterColorMapper.put(IdCardColors.CARD_COLOR_VALUES[index],
					IdCardColors.CARD_COLOR[index]);
		}
		painterColorMapper.put(ChartFilter.DEFAULT_KEY, Color.YELLOW.darker()
				.darker());
		painterMapper = new HashMap<ChartFilter, Map<Integer, Paint>>();
		painterMapper.put(ChartFilter.color, painterColorMapper);

		final Map<Integer, IChartKey> keyColorMapper = new TreeMap<Integer, IChartKey>();
		for (int index = 0; index < IdCardColors.CARD_COLOR_VALUES.length; index++) {
			keyColorMapper.put(IdCardColors.CARD_COLOR_VALUES[index], new CardColor(
					LanguageManager.getString(IdCardColors.CARD_COLOR_NAMES[index]),
					IdCardColors.CARD_COLOR_VALUES[index]));
		}
		keyColorMapper.put(ChartFilter.DEFAULT_KEY, CardColor.UNKNOW_COLOR);

		final Map<Integer, IChartKey> keyTypeMapper = new TreeMap<Integer, IChartKey>();
		for (int index = 0; index < CardFactory.exportedIdCardNames.length; index++) {
			keyTypeMapper.put(CardFactory.exportedIdCardValues[index], new CardTypes(
					CardFactory.exportedIdCardNames[index],
					CardFactory.exportedIdCardValues[index]));
		}
		keyTypeMapper.put(ChartFilter.DEFAULT_KEY, CardTypes.UNKNOW_TYPE);

		keyMapper = new HashMap<ChartFilter, Map<Integer, IChartKey>>();
		keyMapper.put(ChartFilter.color, keyColorMapper);
		keyMapper.put(ChartFilter.type, keyTypeMapper);
		keyMapper.put(ChartFilter.property, new TreeMap<Integer, IChartKey>());
		keyMapper.put(ChartFilter.manacost, new TreeMap<Integer, IChartKey>());
	}

	private Map<Integer, IChartKey> getMapper(ChartFilter filter) {
		return keyMapper.get(filter);
	}

	public List<IChartKey> getKeys(ChartFilter filter) {
		List<IChartKey> list = new ArrayList<IChartKey>(getMapper(filter).values());
		Collections.sort(list);
		return list;
	}

	public Collection<IChartKey> getKeys(CardModel cardModel, ChartFilter filter) {
		Collection<IChartKey> result = new ArrayList<IChartKey>();
		switch (filter) {
		case color:
			int idColor = cardModel.getIdColor();
			if (idColor == 0)
				result.add(getMapper(filter).get(IdCardColors.CARD_COLOR_VALUES[0]));
			else {
				for (IChartKey value : getMapper(filter).values()) {
					if (value.getIntegerKey() != 0
							&& MCard.hasIdColor(idColor, value.getIntegerKey())) {
						result.add(value);
					}
				}
			}
			break;
		case type:
			int idCard = cardModel.getIdCard();
			for (IChartKey value : getMapper(filter).values()) {
				if (MCard.hasIdCard(idCard, value.getIntegerKey()))
					result.add(value);
			}
			break;
		case manacost:
			int total = 0;
			for (int index = IdCommonToken.WHITE_MANA + 1; index-- > 0;) {
				total += cardModel.getStaticRegisters()[index];
			}
			result.add(new CardManaCost(total));
			break;
		default:
			// TODO add the the other filters
		}
		if (result.isEmpty()) {
			IChartKey defaultKey = getMapper(filter).get(ChartFilter.DEFAULT_KEY);
			if (defaultKey != null)
				result.add(getMapper(filter).get(ChartFilter.DEFAULT_KEY));
		}
		return result;
	}

	public void tableChanged(TableModelEvent e) {
		if (e.getType() == TableModelEvent.UPDATE)
			rightListModel.getCards().refresh();
	}

	/**
	 * Read available cards from file and add them to the specified
	 * <code>cardNames</code>
	 * 
	 * @param cardNames
	 *          to this list method add card names
	 */
	protected void readAvailableCards(MListModel<MCardCompare> cardNames) {
		final FileInputStream dbStream = MdbLoader.resetMdb();
		listScrollerLeft.setVisible(false);
		try {
			cardNames.clear();
			while (dbStream.available() > 2) {
				// reads card name
				final String cardName = MToolKit.readString(dbStream);

				// reads card offset
				final long offset = MToolKit.readInt24(dbStream);
				final MCardCompare cardCompare = new MCardCompare(cardName, offset);
				cardNames.add(cardCompare);
			}
		} catch (IOException ex2) {
			// Ignore this error
			ex2.printStackTrace();
		} finally {
			listScrollerLeft.setVisible(true);
			cardNames.refresh();
		}
	}

	private static final FileFilterPlus FILTER_DECK = new FileFilterPlus("dck",
			"DCK : Shandalar deck file");

	/**
	 * The left list model containing all cards.
	 */
	protected final MListModel<MCardCompare> allListModel;

	/**
	 * The right table model containing the current deck cards.
	 */
	protected final MCardTableModel rightListModel;

	private static boolean modifiedSinceSave = false;

	private final JButton addButton;

	private final JButton removeButton;

	/**
	 * The right list : cards in the deck.
	 */
	private final JTable rightList;

	/**
	 * The left list : available cards.
	 */
	protected final JList leftList;

	private final JTextField cardNameTxt;

	/**
	 * Add text
	 */
	private final JTextField addQtyTxt;

	/**
	 * Remove text
	 */
	private final JTextField removeQtyTxt;

	/**
	 * The label displayed above the card picture
	 */
	private final JLabel cardPictureNameTxt;

	/**
	 * indicates if deck builder was launched from console
	 */
	public static boolean consoleMode = false;

	/**
	 * The toolBar containing some filters and card type combobox.
	 */
	private final JToolBar toolBar;

	private final JComboBox idCardComboBox;

	private final JComboBox propertiesComboBox;

	private final JLabel oracleText;

	private final ConstraintsChecker constraintsChecker;

	/**
	 * The deck title
	 */
	private final JTextField deckNameTxt;

	/**
	 * The unique instance of this form.
	 */
	public static DeckBuilder form;
}