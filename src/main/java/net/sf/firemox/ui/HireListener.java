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
package net.sf.firemox.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import net.sf.firemox.tools.MCardCompare;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.2
 */
public class HireListener extends PlainDocument implements ActionListener,
		MouseListener {

	/**
	 * Creates a new instance of HireListener <br>
	 * 
	 * @param editor
	 *          the textField
	 * @param hireComponent
	 *          is component to enable/disable when an element is [de]selected
	 * @param toRefresh
	 * @param mappedList
	 *          the list to synchronize with the content of the text field.
	 */
	public HireListener(final JTextField editor, JButton hireComponent,
			RefreshableAdd toRefresh, JList mappedList) {
		this.hireComponent = hireComponent;
		this.toRefresh = toRefresh;
		this.mappedList = mappedList;
		this.editor = editor;
		editor.addActionListener(this);
		editor.setDocument(this);
		editor.addMouseListener(this);
		hireComponent.addActionListener(this);

		editor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				hitBackspace = false;
				// determine if the pressed key is backspace (needed by the remove
				// method)
				switch (e.getKeyCode()) {
				case KeyEvent.VK_BACK_SPACE:
					hitBackspace = true;
					hitBackspaceOnSelection = editor.getSelectionStart() != editor
							.getSelectionEnd();
					break;
				// ignore delete key
				case KeyEvent.VK_DELETE:
					e.consume();
					break;
				default:
					// continue
				}
			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		toRefresh.refreshAddComponent(false);
	}

	@Override
	public void remove(int offs, int len) throws BadLocationException {
		// return immediately when selecting an item
		if (selecting)
			return;
		if (hitBackspace) {
			// user hit backspace => move the selection backwards
			int workinOffs = offs;
			// old item keeps being selected
			if (workinOffs > 0) {
				if (hitBackspaceOnSelection)
					workinOffs--;
			} else {
				// User hit backspace with the cursor positioned on the start => beep
				// comboBox.getToolkit().beep(); // when available use:
				// UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
			}
			highlightCompletedText(workinOffs);
		} else {
			super.remove(offs, len);
		}
	}

	@Override
	public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException {
		// return immediately when selecting an item
		if (selecting)
			return;
		// insert the string into the document
		int workingOffs = offs;
		super.insertString(workingOffs, str, a);
		// lookup and select a matching item
		Object item = updateSelectedValue(getText(0, getLength()));
		if (item == null) {
			// keep old item selected if there is no match
			item = this.mappedList.getSelectedValue();
			workingOffs = workingOffs - str.length();
		}
		setText(item.toString());
		// select the completed part
		highlightCompletedText(workingOffs + str.length());
	}

	private void setText(String text) {
		try {
			// remove all text and insert the completed string
			super.remove(0, getLength());
			super.insertString(0, text, null);
		} catch (BadLocationException e) {
			throw new RuntimeException(e.toString());
		}
	}

	private void highlightCompletedText(int start) {
		editor.setCaretPosition(getLength());
		editor.moveCaretPosition(start);
	}

	/**
	 * @param select
	 *          the text to find.
	 * @return the selected value.
	 */
	@SuppressWarnings("unchecked")
	private Object updateSelectedValue(String pattern) {

		// iterate over all items
		if (pattern.length() == 0) {
			hireComponent.setEnabled(false);
		} else {
			try {
				final String selectedItem;
				if (mappedList.getSelectedValue() == null)
					selectedItem = null;
				else
					selectedItem = mappedList.getSelectedValue().toString().toLowerCase();
				String workingText = pattern.toLowerCase();
				String currentText = getText(0, getLength()).toLowerCase();
				// search for a different item only if the currently selected does not
				// match
				if (selectedItem != null && selectedItem.startsWith(pattern)) {
					return mappedList.getSelectedValue();
				}

				MListModel<MCardCompare> model = (MListModel<MCardCompare>) mappedList
						.getModel();
				for (int index = 0, n = model.getSize(); index < n; index++) {
					String value = model.getElementAt(index).toString().toLowerCase();
					if (value.startsWith(workingText)) {
						mappedList.setSelectedIndex(index);
						mappedList.ensureIndexIsVisible(index);
						remove(0, currentText.length());
						super.insertString(0, model.getElementAt(index).getName(), null);
						editor.setCaretPosition(value.length());
						editor.moveCaretPosition(workingText.length());
						return model.getElementAt(index);
					}
				}
			} catch (BadLocationException e1) {
				// Ignore this error
			}
		}
		return mappedList.getSelectedValue();
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == editor) {
			editor.selectAll();
		}
	}

	public void mouseEntered(MouseEvent e) {
		// unhandled
		return;
	}

	public void mouseExited(MouseEvent e) {
		// unhandled
		return;
	}

	public void mousePressed(MouseEvent e) {
		// unhandled
		return;
	}

	public void mouseReleased(MouseEvent e) {
		// unhandled
		return;
	}

	/**
	 * Hire component used with this HireListener
	 */
	private JComponent hireComponent;

	/**
	 * The component to refresh on active selection of an element of this list or
	 * on clic on the given JButton.
	 */
	private final RefreshableAdd toRefresh;

	private final JList mappedList;

	private final JTextField editor;

	boolean hitBackspace = false;

	boolean hitBackspaceOnSelection;

	boolean selecting = false;

}
