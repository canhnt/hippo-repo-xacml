/*
 *  Copyright 2008-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.repository.decorating;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 */
public class ItemVisitorDecorator extends AbstractDecorator implements ItemVisitor {

    protected final ItemVisitor visitor;

    protected ItemVisitorDecorator(DecoratorFactory factory, Session session, ItemVisitor visitor) {
        super(factory, session);
        this.visitor = visitor;
    }

    /**
     * @inheritDoc
     */
    public void visit(Property property) throws RepositoryException {
        visitor.visit(factory.getPropertyDecorator(session, property));
    }

    /**
     * @inheritDoc
     */
    public void visit(Node node) throws RepositoryException {
        visitor.visit(factory.getNodeDecorator(session, node));
    }
}
