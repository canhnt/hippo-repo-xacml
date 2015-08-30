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

package org.hippoecm.repository.security;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.commons.iterator.AccessControlPolicyIteratorAdapter;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.core.state.ItemState;
import org.apache.jackrabbit.core.state.ItemStateListener;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyAccessManager implements AccessManager, AccessControlManager, ItemStateListener {
    private static final Logger log = LoggerFactory.getLogger(DummyAccessManager.class);

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
        return true;
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

    /**
     * Initialize this access manager. An <code>AccessDeniedException</code> will be thrown if the subject of the given
     * <code>context</code> is not granted access to the specified workspace.
     *
     * @param context access manager context
     * @throws AccessDeniedException if the subject is not granted access to the specified workspace.
     * @throws Exception             if another error occurs
     */
    @Override
    public void init(final AMContext context) throws AccessDeniedException, Exception {

    }

    @Override
    public void init(final AMContext context, final AccessControlProvider acProvider, final WorkspaceAccessManager wspAccessMgr) throws AccessDeniedException, Exception {

    }

    /**
     * Close this access manager. After having closed an access manager, further operations on this object are treated
     * as illegal and throw
     *
     * @throws Exception if an error occurs
     */
    @Override
    public void close() throws Exception {

    }

    @Deprecated
    @Override
    public void checkPermission(final ItemId id, final int permissions) throws AccessDeniedException, ItemNotFoundException, RepositoryException {
        log.info("Permit permission {} on '{}'", permissions, id);
    }

    @Override
    public void checkPermission(final Path absPath, final int permissions) throws AccessDeniedException, RepositoryException {
        log.info("Permit permission {} on '{}'", permissions,
                absPath != null ? absPath.getString() : "");
    }

    @Override
    public void checkRepositoryPermission(final int permissions) throws AccessDeniedException, RepositoryException {

    }

    @Override
    public boolean isGranted(final ItemId id, final int permissions) throws ItemNotFoundException, RepositoryException {
        log.info("Grant permissions {} on '{}'", permissions, id);
        return true;
    }

    @Override
    public boolean isGranted(final Path absPath, final int permissions) throws RepositoryException {
        log.info("Grant permission {} on '{}'", permissions,
                absPath != null ? absPath.getString() : "");
        return true;
    }


    @Override
    public boolean isGranted(final Path parentPath, final Name childName, final int permissions) throws RepositoryException {
        log.info("Grant permission {} on '{}' with child {}", permissions,
                parentPath != null ? parentPath.getString() : "",
                childName != null ? childName.getLocalName(): "");
        return true;
    }

    @Override
    public boolean canRead(final Path itemPath, final ItemId itemId) throws RepositoryException {
        log.info("Can read on {} ", itemId);
        return true;
    }

    @Override
    public boolean canAccess(final String workspaceName) throws RepositoryException {
        return true;
    }

    @Override
    public void stateCreated(final ItemState created) {

    }

    public void stateModified(final ItemState modified) {

    }

    @Override
    public void stateDestroyed(final ItemState destroyed) {

    }

    @Override
    public void stateDiscarded(final ItemState discarded) {

    }
}
