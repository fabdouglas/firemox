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
package net.sf.firemox.ui.component.task;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

import org.apache.commons.lang.WordUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public abstract class TaskAction extends AbstractAction {

	/**
	 * The action title. The title will be translated with
	 * {@link net.sf.firemox.ui.i18n.LanguageManagerMDB }.
	 */
	protected String title;

	/**
	 * The associated propety value. This property will interpreted by a JavaBean.
	 */
	private String propertyKey;

	/**
	 * The card currently used by this action.
	 */
	private MCard card;

	/**
	 * @param dbStream
	 *          the stream containg the definition of this action.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected TaskAction(final InputStream dbStream) throws IOException {
		super();
		this.title = LanguageManagerMDB.getString(MToolKit.readString(dbStream));
		super.putValue(Action.NAME, "<html><b>" + title + "<b> : -");
		this.propertyKey = MToolKit.readString(dbStream);
	}

	public abstract void actionPerformed(ActionEvent e);

	/**
	 * Revalidate this action with the given card.
	 * 
	 * @param card
	 *          the new card used to revalidate content of this action.
	 */
	public final void revalidate(MCard card) {
		this.card = card;
		try {
			setValue(processJavaBean(this, propertyKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String processJavaBean(Object object, String propertyKey)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if (object == null) {
			return "-";
		}
		int indexSep = propertyKey.indexOf('.');
		if (propertyKey.length() > 0) {
			if (indexSep == -1) {
				indexSep = propertyKey.length();
			}
			final int indexParam = propertyKey.indexOf('[');

			if (indexParam != -1 && indexSep > indexParam) {
				// at least on parameter
				final int indexParam2 = propertyKey.indexOf(']');
				final String parameter = propertyKey.substring(indexParam + 1,
						indexParam2);
				return processJavaBean(object.getClass().getMethod(
						getGetter(propertyKey.substring(0, indexParam)), String.class)
						.invoke(object, parameter), propertyKey.substring(indexParam2 + 1));
			}

			// no parameter
			return processJavaBean(object.getClass().getMethod(
					getGetter(propertyKey.substring(0, indexSep))).invoke(object),
					propertyKey.substring(indexSep >= propertyKey.length() ? indexSep
							: indexSep + 1));
		}
		return object.toString();
	}

	/**
	 * Return a getter method : <br>
	 * getGetter(&quot;&quot;) = &quot;get&quot;<br>
	 * getGetter(*) = &quot;get*&quot;<br>
	 * getGetter(&quot;i-aM-fine&quot;) = &quot;getIAmFine&quot;<br>
	 * 
	 * @param property
	 * @return return a getter method
	 */
	private static String getGetter(String property) {
		return "get"
				+ WordUtils.capitalizeFully(property, new char[] { '-' }).replace("-",
						"");
	}

	/**
	 * Return the card currently used by this action.<br>
	 * Unreferenced method, but called with reflection.
	 * 
	 * @return the card currently used by this action.
	 */
	public MCard getCard() {
		return card;
	}

	/**
	 * Set a new value to this action. This action will be isplayed as
	 * <code>title : <param>htmlValue</param></code>
	 * 
	 * @param htmlValue
	 *          the new value to set to this action.
	 */
	protected void setValue(String htmlValue) {
		putValue(Action.NAME, "<html><b>" + title + " : </b>" + htmlValue);
	}
}
