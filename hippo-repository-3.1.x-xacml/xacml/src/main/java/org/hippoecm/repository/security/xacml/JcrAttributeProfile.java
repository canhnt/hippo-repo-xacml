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

package org.hippoecm.repository.security.xacml;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.security.Privilege;
import javax.security.auth.Subject;

import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.state.ItemState;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.NoSuchItemStateException;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.name.PathFactoryImpl;
import org.hippoecm.repository.jackrabbit.HippoSessionItemStateManager;
import org.hippoecm.repository.security.principals.GroupPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cngo
 * @version $Id$
 * @since 2015-08-23
 */
public class JcrAttributeProfile {
    private static final Logger log = LoggerFactory.getLogger(JcrAttributeProfile.class);

    private final Subject subject;
    private final NamePathResolver namePathResolver;
    private final NodeTypeManager nodetypeManager;
    private final HippoSessionItemStateManager itemManager;
    private final HierarchyManager hierarchyManager;
    private final HierarchyManager zombineHierManager;
    private final NodeId rootNodeId;

    private final Map<String, Set<String>> subjectAttributes;

    public JcrAttributeProfile(final Subject subject,
                               final NamePathResolver npRes,
                               final NodeTypeManager ntMgr,
                               final HippoSessionItemStateManager itemManager,
                               final HierarchyManager hierMgr,
                               final HierarchyManager zombieHierMgr) throws RepositoryException {
        this.subject = subject;
        subjectAttributes = parse(subject);

        this.namePathResolver = npRes;
        this.nodetypeManager = ntMgr;
        this.itemManager = itemManager;
        this.hierarchyManager = hierMgr;
        this.zombineHierManager = zombieHierMgr;

        this.rootNodeId = hierarchyManager.resolveNodePath(PathFactoryImpl.getInstance().getRootPath());
    }

    private Map<String, Set<String>> parse(final Subject subject) {
        Map<String, Set<String>> attrs = new HashMap<>();
        attrs.put("group", getGroups(subject));
        attrs.put("userId", getUserIds(subject));
        return attrs;
    }

    private static Set<String> getGroups(final Subject subject) {
        return subject.getPrincipals(GroupPrincipal.class).stream().map(GroupPrincipal::getName).collect(Collectors.toSet());
    }

    private static Set<String> getUserIds(final Subject subject) {
        return subject.getPrincipals(UserPrincipal.class).stream().map(UserPrincipal::getName).collect(Collectors.toSet());
    }

    public Map<String, Set<String>> parse(final Path absPath) throws RepositoryException {
        NodeId id = getNodeId(absPath);
        NodeState nodeState;
        try {
            nodeState = (NodeState) getItemState(id);
        } catch (NoSuchItemStateException e) {
            throw new PathNotFoundException("Path not found " + namePathResolver.getJCRPath(absPath), e);
        }

        Map<String, Set<String>> attrs = new HashMap<>();
        attrs.put("nodename", new HashSet<>(Arrays.asList(getNodeName(nodeState))));
        attrs.put("jcr:path", new HashSet<>(Arrays.asList(absPath.getString())));
//        attrs.put("nodetype", getNodeType(nodeState));
        attrs.put("jcr:uuid", new HashSet<>(Arrays.asList(nodeState.getNodeId().toString())));

        // node properties
        attrs.put("jcr:primaryType", new HashSet<>(Arrays.asList(nodeState.getNodeTypeName().toString())));
        return attrs;
    }

    private String getJcrPath(final NodeState nodeState) {
        return null;
    }

    public Map<String, Set<String>> parse(final int permissions) {
        final Set<String> privileges = new HashSet<>();
        if ((permissions & Permission.READ) != 0) {
            privileges.add(Privilege.JCR_READ);
        }

        if ((permissions & Permission.ADD_NODE) != 0) {
            privileges.add(Privilege.JCR_ADD_CHILD_NODES);
        }

        if ((permissions & (Permission.SET_PROPERTY | Permission.REMOVE_PROPERTY)) != 0) {
            privileges.add(Privilege.JCR_MODIFY_PROPERTIES);
        }

        if ((permissions & Permission.REMOVE_NODE) != 0) {
            privileges.add(Privilege.JCR_REMOVE_CHILD_NODES);
        }

        final HashMap<String, Set<String>> privs = new HashMap<>();
        privs.put("privileges", privileges);
        return privs;
    }

    private String getNodeType(final NodeState nodeState) {
        return null;
    }

    /**
     * Get the <code>NodeId</code> for the absolute path. If the absolute path points
     * to a property return the NodeId of the parent. This method return null instead of
     * throwing a <code>PathNotFoundException</code> for performance reasons.
     * @param absPath the absolute Path
     * @return the NodeId of the node (holding the property) or null when the node not found
     * @throws RepositoryException
     */
    private NodeId getNodeId(Path absPath) throws RepositoryException {
        if (!absPath.isAbsolute()) {
            throw new RepositoryException("Absolute path expected, got " + absPath);
        }

        if (absPath.denotesRoot()) {
            return rootNodeId;
        }

        try {
            ItemId itemId = hierarchyManager.resolvePath(absPath);
            if (itemId != null) {
                if (itemId instanceof PropertyId) {
                    return ((PropertyId) itemId).getParentId();
                }
                return (NodeId) itemId;
            }
        } catch (RepositoryException e) {
            // fall through and try zombie hierMgr
            if (log.isDebugEnabled()) {
                log.debug("Error while resolving node id of: " + absPath, e);
            }
        }

        try {
            // try zombie parent, probably a property
            ItemId itemId = zombineHierManager.resolvePath(absPath);
            if (itemId instanceof PropertyId) {
                return ((PropertyId) itemId).getParentId();
            }
            return (NodeId) itemId;
        } catch (RepositoryException e) {
            // fall thru and throw a path not found exception
            if (log.isDebugEnabled()) {
                log.debug("Error while resolving node id of: " + absPath, e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Unable to resolve the node id from the path " + absPath);
        }
        return null;
    }

    /**
     * Try to get a state from the item manager by first checking the normal states,
     * then checking the transient states and last checking the attic state.
     * @param id the item id
     * @return the item state
     * @throws NoSuchItemStateException when the state cannot be found
     */
    private ItemState getItemState(ItemId id) throws NoSuchItemStateException {
        if (id == null) {
            throw new IllegalArgumentException("ItemId cannot be null");
        }
        if (itemManager.hasItemState(id)) {
            try {
                return itemManager.getItemState(id);
            } catch (ItemStateException e) {
                log.debug("Error while trying to get item state from the normal ism of id: " + id, e);
            }
        } else if (itemManager.hasTransientItemState(id)) {
            try {
                return itemManager.getTransientItemState(id);
            } catch (ItemStateException e) {
                log.debug("Error while trying to get item state from the transient ism of id: " + id, e);
            }
        } else if (itemManager.hasTransientItemStateInAttic(id)) {
            try {
                return itemManager.getAtticItemState(id);
            } catch (ItemStateException e) {
                log.debug("Error while trying to get item state from the attic ism of id: " + id, e);
            } catch (RepositoryException e) {
                log.debug("Error while trying to get item state from the attic ism of id: " + id, e);
            }
        }

        // nothing found...
        String msg = "Item state not found in normal, transient or attic: " + id;
        NoSuchItemStateException e = new NoSuchItemStateException(msg);
        log.debug(msg, e);
        throw e;
    }


    private String getNodeName(final NodeState nodeState) throws ItemNotFoundException {
        Name nodeName = getNodeName(nodeState, hierarchyManager);
        if (nodeName == null) {
            // try zombieHierMgr if regular hierMgr has failed to retrieve node
            nodeName = getNodeName(nodeState, zombineHierManager);
        }
        if (nodeName != null) {
            return nodeName.toString();
        }
        String msg = "Node with id " + nodeState.getId() + " with status [" + nodeState.getStatus()
                + "] not found in hierarchy manager or zombie hierarchy manager.";
        log.warn(msg);
        throw new ItemNotFoundException(msg);
    }

    /**
     * Helper function to resolve the name of the node of the nodeState in the specified hierarchy manager.
     * @param nodeState
     * @param hierMgr hierarchy manager to use for resolving the node name
     * @return the Name or null when the name can not be found.
     */
    private static Name getNodeName(NodeState nodeState, HierarchyManager hierMgr) {
        try {
            return hierMgr.getName(nodeState.getId());
        } catch (ItemNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Node with id " + nodeState.getId() + " with status [" + nodeState.getStatus()
                        + "] not found in hierarchy manager " + hierMgr.getClass().getName());
            }
        } catch (RepositoryException e) {
            // hierMgr throws RepositoryException when it encounters an ItemStateException
            // this can indicate data corruption, but if the error is not caught here it will fail the
            // complete QueryImpl.execute() method instead of skipping just the failed node.

            log.warn(
                    "Error while resolving name for node with id " + nodeState.getId() + " with status ["
                            + nodeState.getStatus() + "] not found in hierarchy manager "
                            + hierMgr.getClass().getName(), e);
        }
        return null;
    }

    public Map<String,Set<String>> getSubjectAttributes() {
        return subjectAttributes;
    }
}
