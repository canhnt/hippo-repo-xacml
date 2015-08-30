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

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;
import javax.jcr.version.VersionException;
import javax.security.auth.Subject;

import org.apache.jackrabbit.commons.iterator.AccessControlPolicyIteratorAdapter;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.core.state.ItemState;
import org.apache.jackrabbit.core.state.ItemStateListener;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.name.PathFactoryImpl;
import org.hippoecm.repository.jackrabbit.HippoSessionItemStateManager;
import org.hippoecm.repository.security.HippoAMContext;
import org.hippoecm.repository.security.xacml.sne.JcrPDP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class XACMLAccessManager implements AccessManager, AccessControlManager, ItemStateListener {
    private static final Logger log = LoggerFactory.getLogger(XACMLAccessManager.class);
    protected Subject subject;
    protected NamePathResolver npRes;
    protected NodeTypeManager ntMgr;
    protected HippoSessionItemStateManager itemMgr;
    protected HierarchyManager hierMgr;
    protected HierarchyManager zombieHierMgr;

    @Override
    public Privilege[] getSupportedPrivileges(final String absPath) throws PathNotFoundException, RepositoryException {
        return new Privilege[0];
    }

    @Override
    public Privilege privilegeFromName(final String privilegeName) throws AccessControlException, RepositoryException {
        return null;
    }

    @Override
    public boolean hasPrivileges(final String absPath, final Privilege[] privileges) throws PathNotFoundException, RepositoryException {
        return false;
    }

    @Override
    public Privilege[] getPrivileges(final String absPath) throws PathNotFoundException, RepositoryException {
        return new Privilege[0];
    }

    @Override
    public AccessControlPolicy[] getPolicies(final String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        log.warn("Implementation does not provide applicable policies -> getPolicy() always returns an empty array.");

        return new AccessControlPolicy[0];
    }

    @Override
    public AccessControlPolicy[] getEffectivePolicies(final String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        return new AccessControlPolicy[0];
    }

    @Override
    public AccessControlPolicyIterator getApplicablePolicies(final String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        log.warn("Implementation does not provide applicable policies -> returning empty iterator.");
        return AccessControlPolicyIteratorAdapter.EMPTY;
    }

    @Override
    public void setPolicy(final String absPath, final AccessControlPolicy policy) throws PathNotFoundException, AccessControlException, AccessDeniedException, LockException, VersionException, RepositoryException {
        throw new AccessControlException("AccessControlPolicy " + policy + " cannot be applied.");

    }

    @Override
    public void removePolicy(final String absPath, final AccessControlPolicy policy) throws PathNotFoundException, AccessControlException, AccessDeniedException, LockException, VersionException, RepositoryException {
        throw new AccessControlException("No AccessControlPolicy has been set through this API -> Cannot be removed.");
    }

    @Override
    public void init(final AMContext context) throws AccessDeniedException, Exception {
        JcrPDP.init();

        subject = context.getSubject();
        npRes = context.getNamePathResolver();

        if (context instanceof HippoAMContext) {
            ntMgr = ((HippoAMContext) context).getNodeTypeManager();
            itemMgr = (HippoSessionItemStateManager) ((HippoAMContext) context).getSessionItemStateManager();
        }

        hierMgr = itemMgr.getHierarchyMgr();
        zombieHierMgr = itemMgr.getAtticAwareHierarchyMgr();
    }

    @Override
    public void init(final AMContext context, final AccessControlProvider acProvider, final WorkspaceAccessManager wspAccessMgr) throws AccessDeniedException, Exception {
        init(context);
    }

    @Override
    public void close() throws Exception {

    }

    @Deprecated
    @Override
    public void checkPermission(final ItemId id, final int permissions) throws AccessDeniedException, ItemNotFoundException, RepositoryException {
        if (!isGranted(id, permissions)) {
            throw new AccessDeniedException("Cannot access to " + id.toString());
        }
    }

    @Override
    public void checkPermission(final Path absPath, final int permissions) throws AccessDeniedException, RepositoryException {
        if (!isPermit(absPath, permissions)) {
            throw new AccessDeniedException("Cannot access to " + absPath.getString());
        }
    }

    @Override
    public void checkRepositoryPermission(final int permissions) throws AccessDeniedException, RepositoryException {

    }

    @Deprecated
    @Override
    public boolean isGranted(final ItemId id, final int permissions) throws ItemNotFoundException, RepositoryException {
        log.debug("Grant permissions {} on '{}'", permissions, id);

        if (!id.denotesNode()) {
            final PropertyId propertyId = (PropertyId) id;
            return isGranted(propertyId.getParentId(), permissions);
        } else {
            return isGranted(hierMgr.getPath(id), permissions);
        }
    }

    @Override
    public boolean isGranted(final Path absPath, final int permissions) throws RepositoryException {
        log.info("Grant permission {} on '{}'", permissions,
                absPath != null ? absPath.getString() : "");

        if (permissions == Permission.READ) {
            return canRead(absPath, null);
        }
        return isPermit(absPath, permissions);
    }

    @Override
    public boolean isGranted(final Path parentPath, final Name childName, final int permissions) throws RepositoryException {
        Path p = PathFactoryImpl.getInstance().create(parentPath, childName, true);
        return isGranted(p, permissions);
    }

    @Override
    public boolean canRead(final Path itemPath, final ItemId itemId) throws RepositoryException {
        log.debug("Can read on {} ", itemId);

        // try itemId first if both parameters are set as it is faster
        if (itemId != null) {
            if (itemId.denotesNode()) {
                return isGranted(itemId, Permission.READ);
            } else {
                return true;
            }
        } else if (itemPath != null) {
            return isGranted(itemPath, Permission.READ);
        } else {
            // itemId and absPath are null
            return false;
        }
    }

    @Override
    public boolean canAccess(final String workspaceName) throws RepositoryException {
        return true;
    }


    @Override
    public void stateCreated(final ItemState created) {

    }

    @Override
    public void stateModified(final ItemState modified) {

    }

    @Override
    public void stateDestroyed(final ItemState destroyed) {

    }

    @Override
    public void stateDiscarded(final ItemState discarded) {

    }

    protected abstract boolean isPermit(final Path absPath, final int permissions);


}
