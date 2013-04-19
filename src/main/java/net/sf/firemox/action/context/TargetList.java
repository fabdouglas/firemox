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
package net.sf.firemox.action.context;

import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class TargetList implements ActionContext {

	/**
	 * Create a new instance of this class.
	 */
	public TargetList() {
		super();
	}

	/**
	 * Add yhe specified target to the list of this context.
	 * 
	 * @param target
	 *          the target to add
	 * @param test
	 *          the attached test validating the target
	 */
	public void add(Target target, Test test) {
		if (targetList == null) {
			targetList = new ArrayList<Target>();
			this.testList = new ArrayList<Test>();
		}
		targetList.add(target);
		testList.add(test);
	}

	/**
	 * Add the specified targets to the list of this context.
	 * 
	 * @param targetList
	 *          the targets to add
	 * @param testList
	 *          the attached tests validating the targets
	 */
	public void addAll(List<Target> targetList, List<Test> testList) {
		if (this.targetList == null) {
			this.targetList = new ArrayList<Target>();
			this.testList = new ArrayList<Test>();
		}
		this.targetList.addAll(targetList);
		this.testList.addAll(testList);
	}

	/**
	 * The targets added by this context.
	 */
	public List<Target> targetList;

	/**
	 * The restriction test used to add the targets.
	 */
	public List<Test> testList;

	/**
	 * Rollback the target list content.
	 */
	public void rollback() {
		for (int index = 0; index < targetList.size(); index++) {
			StackManager.getInstance().getTargetedList().removeTargeted(
					targetList.get(index));
		}
	}

	/**
	 * Replay the target list operation.
	 * 
	 * @param eventOption
	 *          is the event mode : Force, default, none. Depending this value,
	 *          the added target will be rechecked before resolution process
	 * @param raiseEvent
	 *          if false, is any case, no event can be triggered.
	 */
	public void replay(int eventOption, boolean raiseEvent) {
		for (int index = 0; index < targetList.size(); index++) {
			StackManager.getInstance().getTargetedList().addTarget(eventOption,
					targetList.get(index), testList.get(index), raiseEvent);
		}
	}
}
