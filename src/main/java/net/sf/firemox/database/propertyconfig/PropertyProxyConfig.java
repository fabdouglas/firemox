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
package net.sf.firemox.database.propertyconfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.database.Proxy;
import net.sf.firemox.database.data.CollectionData;
import net.sf.firemox.database.data.StringData;
import net.sf.firemox.database.data.TranslatableData;
import net.sf.firemox.database.data.TranslatedCollectionData;
import net.sf.firemox.database.data.TranslatedStringData;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.expression.IntValue;
import net.sf.firemox.expression.StringMethod;
import net.sf.firemox.xml.XmlTools;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class PropertyProxyConfig extends PropertyConfig {

	private String delimiterLeft;

	private String delimiterRight;

	private Expression startingOffset;

	private Expression endingOffset;

	/**
	 * Available values of this property.
	 */
	public static Map<String, Expression> values = new HashMap<String, Expression>();

	/**
	 * Create a new instance of this class.
	 * 
	 * @param node
	 *          the node containing definition of value.
	 */
	public PropertyProxyConfig(Node node) {
		super("card." + node.getAttribute("name"));
		delimiterLeft = node.getAttribute("delimiter-left");
		delimiterRight = node.getAttribute("delimiter-right");
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream(20);
			XmlTools.writeAttrOptions(node, "starting-offset", out);
			startingOffset = ExpressionFactory
					.readNextExpression(new ByteArrayInputStream(out.toByteArray()));
			out.reset();
			XmlTools.writeAttrOptions(node, "ending-offset", out);
			endingOffset = ExpressionFactory
					.readNextExpression(new ByteArrayInputStream(out.toByteArray()));
			out.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	boolean isTranslated() {
		return DatabaseFactory.propertiesCacheConfig.get(getName()).isTranslated();
	}

	@Override
	public final TranslatableData parseProperty(String cardName, String stream,
			Proxy proxy) {
		StringMethod.testedString = stream;
		int startValue = startingOffset.getValue(null, SystemCard.instance, null);
		if (startValue != -1) {
			values.put("%last-offset", new IntValue(startValue));
			int endValue = endingOffset.getValue(null, SystemCard.instance, null);
			if (endValue != -1) {
				values.put("%last-offset", new IntValue(endValue));
				String property = stream.substring(startValue, endValue).trim();
				if (delimiterLeft != null && delimiterRight != null
						&& delimiterLeft.length() > 0 && delimiterRight.length() > 0) {
					// is a collection
					final ArrayList<String> list = new ArrayList<String>();
					int lastOffset = 0;
					while ((lastOffset = property.indexOf(delimiterLeft, lastOffset)) != -1) {
						lastOffset += delimiterLeft.length();
						int boundOffset = property.indexOf(delimiterRight, lastOffset);
						if (boundOffset < 0) {
							break;
						}

						list.add(proxy.getGlobalValueFromLocal(getName(), property
								.substring(lastOffset, boundOffset)));
						lastOffset = boundOffset + delimiterRight.length();
					}
					final String[] array = new String[list.size()];
					list.toArray(array);
					if (DatabaseFactory.propertiesCacheConfig.get(getName()) != null
							&& isTranslated()) {
						return new TranslatedCollectionData(this, array);
					}
					return new CollectionData(this, array);
				}
				// is a simple value
				if (DatabaseFactory.propertiesCacheConfig.get(getName()) != null
						&& isTranslated()) {
					return new TranslatedStringData(this, proxy.getGlobalValueFromLocal(
							getName(), property));
				}
				return new StringData(this, proxy.getGlobalValueFromLocal(getName(),
						property));
			}
		}
		return null;
	}
}
