/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.anyline.util;

import ognl.MemberAccess;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;

public class DefaultOgnlMemberAccess implements MemberAccess {
	public boolean allowPrivateAccess = false;
	public boolean allowProtectedAccess = false;
	public boolean allowPackageProtectedAccess = false;

	public DefaultOgnlMemberAccess(boolean allowAllAccess) {
		this(allowAllAccess, allowAllAccess, allowAllAccess);
	}

	public DefaultOgnlMemberAccess(boolean allowPrivateAccess,
								   boolean allowProtectedAccess, boolean allowPackageProtectedAccess) {
		super();
		this.allowPrivateAccess = allowPrivateAccess;
		this.allowProtectedAccess = allowProtectedAccess;
		this.allowPackageProtectedAccess = allowPackageProtectedAccess;
	}

	public boolean getAllowPrivateAccess() {
		return allowPrivateAccess;
	}

	public void setAllowPrivateAccess(boolean value) {
		allowPrivateAccess = value;
	}

	public boolean getAllowProtectedAccess() {
		return allowProtectedAccess;
	}

	public void setAllowProtectedAccess(boolean value) {
		allowProtectedAccess = value;
	}

	public boolean getAllowPackageProtectedAccess() {
		return allowPackageProtectedAccess;
	}

	public void setAllowPackageProtectedAccess(boolean value) {
		allowPackageProtectedAccess = value;
	}
	@SuppressWarnings("rawtypes")
	public Object setup(Map context, Object target, Member member,
			String propertyName) {
		Object result = null;

		if (isAccessible(context, target, member, propertyName)) {
			AccessibleObject accessible = (AccessibleObject) member;

			if (!accessible.isAccessible()) {
				result = Boolean.FALSE;
				accessible.setAccessible(true);
			}
		}
		return result;
	}
	@SuppressWarnings("rawtypes")
	public void restore(Map context, Object target, Member member,
			String propertyName, Object state) {
		if (state != null) {
			((AccessibleObject) member).setAccessible(((Boolean) state)
					.booleanValue());
		}
	}

	@SuppressWarnings("rawtypes")
	public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
		int modifiers = member.getModifiers();
		boolean result = Modifier.isPublic(modifiers);

		if (!result) {
			if (Modifier.isPrivate(modifiers)) {
				result = getAllowPrivateAccess();
			} else {
				if (Modifier.isProtected(modifiers)) {
					result = getAllowProtectedAccess();
				} else {
					result = getAllowPackageProtectedAccess();
				}
			}
		}
		return result;
	}

}