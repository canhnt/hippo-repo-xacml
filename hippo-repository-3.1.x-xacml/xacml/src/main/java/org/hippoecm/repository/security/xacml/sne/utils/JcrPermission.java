/*
 * Copyright 2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hippoecm.repository.security.xacml.sne.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.core.security.authorization.Permission;

/**
 * @author cngo
 * @version $Id$
 * @since 2015-08-22
 */
public class JcrPermission {

    public static final Map<Integer, String> MAP_PERMISSIONS = initPermissions();

    private static Map<Integer, String> initPermissions() {
        Map<Integer, String> map = new HashMap<>();
        map.put(Permission.READ, "READ");
        map.put(Permission.SET_PROPERTY, "SET_PROPERTY");
        map.put(Permission.ADD_NODE, "ADD_NODE");
        map.put(Permission.REMOVE_NODE, "REMOVE_NODE");
        map.put(Permission.REMOVE_PROPERTY, "REMOVE_PROPERTY");
        return Collections.unmodifiableMap(map);
    }

    static Set<String> parseJcrPermissions(final int permissions) {
        Set<String> jcrPermissions = new HashSet<>();

        if ((permissions & Permission.ALL) != 0) {
            jcrPermissions.addAll(MAP_PERMISSIONS.values());
            return jcrPermissions;
        }

        MAP_PERMISSIONS.entrySet().stream().forEach(entry -> {
            if ((permissions & entry.getKey()) != 0) {
                jcrPermissions.add(entry.getValue());
            }
        });
        return jcrPermissions;
    }
}
