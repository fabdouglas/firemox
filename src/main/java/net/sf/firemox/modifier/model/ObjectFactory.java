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
package net.sf.firemox.modifier.model;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.modifier.ModifierType;
import net.sf.firemox.test.Test;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.Picture;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
public final class ObjectFactory {

	/**
	 * Create a new instance of this class.
	 */
	private ObjectFactory() {
		super();
	}

	/**
	 * Return object corresponding to the given name.
	 * 
	 * @param objectName
	 *          the object's name.
	 * @return object corresponding to the given name.
	 */
	public static ObjectModifierModel getObjectModifierModel(String objectName) {
		return objectModels.get(objectName);
	}

	/**
	 * Return occurrences number of the given object with the given name.
	 * 
	 * @param objectName
	 *          the object's name.
	 * @param objectTest
	 *          The test applied on specific modifier to be removed.
	 * @param onCard
	 *          the card on which objects will be counted.
	 * @return occurrences number of this object attached to the given card.
	 */
	public static int getNbObject(String objectName, MCard onCard, Test objectTest) {
		return getObjectModifierModel(objectName).getNbObject(onCard, objectTest);
	}

	/**
	 * Return picture of the given object.
	 * 
	 * @param objectName
	 *          the object's name
	 * @return picture of the given object.
	 */
	public static Image getObjectPicture(String objectName) {
		if (objectPictures.get(objectName) == null) {
			try {
				objectPictures.put(objectName, Picture
						.loadImage(MToolKit.getTbsPicture("objects/"
								+ objectName.replaceAll("/", "") + ".gif")));
			} catch (MalformedURLException e) {
				// IGNORING
			}
		}
		return objectPictures.get(objectName);
	}

	/**
	 * Remove if can, one instance of given object with the specified name.
	 * 
	 * @param objectName
	 *          the object to remove.
	 * @param fromCard
	 *          the card the object will be removed from.
	 * @param objectTest
	 *          The test applied on specific modifier to be removed.
	 */
	public static void removeObjectModifier(String objectName, MCard fromCard,
			Test objectTest) {
		getObjectModifierModel(objectName).removeObject(fromCard, objectTest);
	}

	/**
	 * Read the available objects.
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>nb of objects [1]</li>
	 * <li>objects i [...]</li>
	 * </ul>
	 * 
	 * @param dbStream
	 *          the mdb stream's header.
	 * @throws IOException
	 *           error during the mdb stream read.
	 */
	public static void init(InputStream dbStream) throws IOException {
		if (objectModels == null) {
			objectModels = new HashMap<String, ObjectModifierModel>();
			objectPictures = new HashMap<String, Image>();
		} else {
			objectModels.clear();
			objectPictures.clear();
		}
		for (int count = dbStream.read(); count-- > 0;) {
			ObjectModifierModel obj = readObjectModifier(dbStream);
			objectModels.put(obj.getObjectName(), obj);

			// is there another modifier bundled with this?
			while (dbStream.read() != 0) {
				((ModifierModel) obj).next = (ModifierModel) readObjectModifier(dbStream);
				obj = (ObjectModifierModel) ((ModifierModel) obj).next;
			}
		}
	}

	/**
	 * Return the next ObjectModifierModel read from the given stream.
	 * 
	 * @param dbStream
	 *          the mdb stream's header.
	 * @return the next ObjectModifierModel read from the given stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	private static ObjectModifierModel readObjectModifier(InputStream dbStream)
			throws IOException {
		final ModifierType objetType = ModifierType.deserialize(dbStream);
		ObjectModifierModel obj = null;
		switch (objetType) {
		case REGISTER_MODIFIER:
			obj = new ObjectRegisterModifierModel(dbStream);
			break;
		case IDCARD_MODIFIER:
			obj = new ObjectIdCardModifierModel(dbStream);
			break;
		case COLOR_MODIFIER:
			obj = new ObjectColorModifierModel(dbStream);
			break;
		case ABILITY_MODIFIER:
			obj = new ObjectAbilityModifierModel(dbStream);
			break;
		case PROPERTY_MODIFIER:
			obj = new ObjectPropertyModifierModel(dbStream);
			break;
		default:
			throw new InternalError("Unknown object modifier type : " + objetType);
		}
		return obj;
	}

	/**
	 * Links of object name --> object type <br>
	 * Key : Object name : String <br>
	 * Value : ObjectModifierModel
	 */
	private static HashMap<String, ObjectModifierModel> objectModels;

	/**
	 * Pictures of already used objects
	 */
	private static HashMap<String, Image> objectPictures;

	/**
	 * Return the named register modifier madel.
	 * 
	 * @param name
	 *          the modifier name.
	 * @return the named register modifier madel.
	 */
	public static ObjectRegisterModifierModel getObjectRegisterModifierModel(
			String name) {
		return (ObjectRegisterModifierModel) objectModels.get(name);
	}

	/**
	 * Return the named type modifier madel.
	 * 
	 * @param name
	 *          the modifier name.
	 * @return the named type modifier madel.
	 */
	public static ObjectIdCardModifierModel getObjectIdCardModifierModel(
			String name) {
		return (ObjectIdCardModifierModel) objectModels.get(name);
	}

	/**
	 * Return the named color modifier madel.
	 * 
	 * @param name
	 *          the modifier name.
	 * @return the named color modifier madel.
	 */
	public static ObjectColorModifierModel getObjectColorModifierModel(String name) {
		return (ObjectColorModifierModel) objectModels.get(name);
	}

	/**
	 * Return the named property modifier madel.
	 * 
	 * @param name
	 *          the modifier name.
	 * @return the named property modifier madel.
	 */
	public static ObjectPropertyModifierModel getObjectPropertyModifierModel(
			String name) {
		return (ObjectPropertyModifierModel) objectModels.get(name);
	}
}
