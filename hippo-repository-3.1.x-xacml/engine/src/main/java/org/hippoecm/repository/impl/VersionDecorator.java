/*
 *  Copyright 2008-2014 Hippo B.V. (http://www.onehippo.com)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.repository.impl;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.hippoecm.repository.api.HippoNode;
import org.hippoecm.repository.api.Localized;
import org.hippoecm.repository.decorating.DecoratorFactory;

public class VersionDecorator extends org.hippoecm.repository.decorating.VersionDecorator {

    protected final Version version;

    public VersionDecorator(DecoratorFactory factory, Session session, Version version) {
        super(factory, session, version);
        this.version = version;
    }

    public Node getCanonicalNode() throws RepositoryException {
        Node canonical = ((SessionDecorator)getSession()).getCanonicalNode(version);
        if(canonical != null) {
            return factory.getNodeDecorator(session, canonical);
        } else {
            return null;
        }
    }

    @Override
    public boolean isVirtual() throws RepositoryException {
        return getIdentifier().startsWith("cafeface");
    }

    @Override
    public boolean recomputeDerivedData() throws RepositoryException {
        return false;
    }

    public String getLocalizedName() throws RepositoryException {
        return ((HippoNode)version).getLocalizedName();
    }

    public String getLocalizedName(Localized localized) throws RepositoryException {
        return ((HippoNode)version).getLocalizedName(localized);
    }

    @Override
    public Map<Localized, String> getLocalizedNames() throws RepositoryException {
        return ((HippoNode)version).getLocalizedNames();
    }
}
