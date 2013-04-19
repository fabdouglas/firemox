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
package net.sf.firemox.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.target.card.CardModel;
import net.sf.firemox.database.data.TranslatableData;
import net.sf.firemox.management.MonitoredCheckContent;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
class UrlTokenizer {

	private List<String> constraints;

	private List<String> streamStrings;

	UrlTokenizer(Node node) {
		String stream = node.getAttribute("url");
		constraints = new ArrayList<String>();
		streamStrings = new ArrayList<String>();
		int constraintIndex = -1;
		int streamIndex = 0;
		while ((constraintIndex = stream.indexOf("${", constraintIndex)) != -1) {
			int closingBrace = stream.indexOf("}", constraintIndex);
			if (closingBrace == -1) {
				throw new InternalError("Unclosed '{' in stream : " + stream);
			}
			String constraint = stream.substring(constraintIndex + 2, closingBrace)
					.trim().toLowerCase();
			if (constraint.contains("+")) {
				// There is an inner contraint
				constraints
						.add(constraint.substring(0, constraint.indexOf('+')).trim());
				streamStrings.add(stream.substring(streamIndex, constraintIndex));
				streamStrings.add("%ADD%");
				constraints.add(constraint.substring(constraint.indexOf('+') + 1)
						.trim());
			} else {
				constraints.add(constraint);
				streamStrings.add(stream.substring(streamIndex, constraintIndex));
			}
			constraintIndex = closingBrace + 1;
			streamIndex = constraintIndex;
		}
		streamStrings.add(stream.substring(streamIndex));
	}

	/**
	 * Given a score depending of accessible constraint within the given
	 * properties. Currently, score is
	 * <ul>
	 * <li><code>-1</code> for a not valid stream : constraints have not been
	 * all been found in the given properties</li>
	 * <li><code>0</code> for a valid stream : constraints have not all been
	 * found in the given properties</li>
	 * <li><code>positive number</code> for a valid stream with so much
	 * constraints (better) </li>
	 * </ul>
	 * 
	 * @param properties
	 *          set of available properties.
	 * @return the score.
	 */
	public int getUrlScore(Map<String, String> properties) {
		boolean hasCardNameAttr = false;
		for (String constraint : constraints) {
			if ("card.name".equals(constraint)) {
				hasCardNameAttr = true;
			} else {
				if (properties == null || !properties.containsKey(constraint)) {
					return -1;
				}
			}
		}
		if (properties == null) {
			return hasCardNameAttr ? 0 : 1;
		}
		return properties.size() + constraints.size() + (hasCardNameAttr ? 1 : 0);
	}

	/**
	 * Return an URL built from the expression, card, constraints, and proxy.
	 * 
	 * @param cardModel
	 *          the card represented by the built url.
	 * @param properties
	 *          the properties used to replace requested properties within the
	 *          expression.
	 * @param proxy
	 *          the proxy containing the requested url. Would be used to get local
	 *          values.
	 * @return an URL built from the expression.
	 */
	public String getUrl(CardModel cardModel, Map<String, ?> properties,
			Proxy proxy) {
		final StringBuilder res = new StringBuilder(100);
		res.append(streamStrings.get(0));
		for (int i = 0; i < constraints.size(); i++) {
			final String constrainValue = getConstraint(constraints.get(i),
					cardModel, properties, proxy);
			if (constrainValue == null) {
				throw new RuntimeException("URL constraint #" + i + " '"
						+ this.constraints.get(i) + "' is not defined in given card ["
						+ cardModel + "] properties " + properties.values().toString());
			}
			if (i < constraints.size() - 1
					&& "%ADD%".equals(streamStrings.get(i + 1))) {
				res.append(String.valueOf(Integer.parseInt(constrainValue)
						+ Integer.parseInt(getConstraint(constraints.get(i + 1), cardModel,
								properties, proxy)) - 1));
				i++;
			} else {
				if (res.toString().contains(MonitoredCheckContent.STR_ZIP_PATH)) {
					res.append(constrainValue);
				} else {
					res.append(getJspUrl(constrainValue));
				}
			}
			res.append(streamStrings.get(i + 1));
		}
		return res.toString();
	}

	private String getConstraint(String constraint, CardModel cardModel,
			Map<String, ?> constraints, Proxy proxy) {
		if ("card.name".equals(constraint)) {
			return cardModel.getCardName();
		}
		if ("card.keyname".equals(constraint)) {
			return cardModel.getKeyName();
		}
		if ("card.language".equals(constraint)) {
			return cardModel.getLanguage();
		}
		if (constraints == null || constraints.get(constraint) == null) {
			// the constraint does not exist
			return null;
		}
		final Object value = constraints.get(constraint);
		if (value instanceof String) {
			return (String) value;
		}
		if (proxy != null) {
			return proxy.getLocalValueFromGlobal(constraint,
					((TranslatableData) value).getValue());
		}
		return ((TranslatableData) value).getValue();
	}

	/**
	 * Return the given string with all specials chars replaced by "%.." string
	 * 
	 * @param bruteString
	 *          the String to be parsed.
	 * @return the given string transformed to suit to an URL address.
	 */
	private String getJspUrl(String bruteString) {
		return bruteString.trim().replace(" ", "%20").replace("!", "%21").replace(
				"\"", "%22").replace("#", "%23").replace("&", "%26")
				.replace("'", "%27").replace("(", "%28").replace(")", "%29").replace(
						"*", "%2A").replace("+", "%2B").replace(",", "%2C").replace("-",
						"%2D").replace(".", "%2E").replace("/", "%2F").replace(":", "%3A")
				.replace(";", "%3B").replace("<", "%3C").replace("=", "%3D").replace(
						">", "%3E").replace("@", "%41").replace("^", "%5E").replace("_",
						"%60").replace("`", "%61").replace("{", "%7B").replace("}", "%7C")
				.replace("|", "%7D").replace("~", "%7E");
	}

	@Override
	public String toString() {
		return "{streamStrings=" + streamStrings + ", constraints=" + constraints
				+ "}";
	}

}
