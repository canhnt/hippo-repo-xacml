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

package org.hippoecm.repository.security.xacml.sun.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hippoecm.repository.security.xacml.XMLDataType;

import oasis.names.tc.xacml._2_0.context.schema.os.ActionType;
import oasis.names.tc.xacml._2_0.context.schema.os.AttributeType;
import oasis.names.tc.xacml._2_0.context.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.context.schema.os.ObjectFactory;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceType;
import oasis.names.tc.xacml._2_0.context.schema.os.SubjectType;

/**
 * @author cngo
 * @version $Id$
 * @since 2015-08-23
 */
public class XACML2Util {
    private final static ObjectFactory factory = new ObjectFactory();


    public static ActionType createAction(final Map<String, Set<String>> attrs) {
        final ActionType action = factory.createActionType();
        action.getAttribute().addAll(XACML2Util.createAttributes(attrs));
        return action;
    }

    public static ResourceType createResource(final Map<String, Set<String>> attrs) {
        final ResourceType resource = factory.createResourceType();
        resource.getAttribute().addAll(XACML2Util.createAttributes(attrs));
        return resource;
    }

    public static SubjectType createSubject(final Map<String, Set<String>> attrs) {
        final SubjectType subject = factory.createSubjectType();
        subject.getAttribute().addAll(XACML2Util.createAttributes(attrs));
        return subject;
    }

    private static AttributeType createAttribute(Map.Entry<String, Set<String>> entry) {
        AttributeType attr = factory.createAttributeType();
        attr.setAttributeId(entry.getKey());
        attr.setDataType(XMLDataType.STRING.toString());

        final List<AttributeValueType> attrValues = entry.getValue().stream()
                .map(XACML2Util::createAttributeValue)
                .collect(Collectors.toList());
        attr.getAttributeValue().addAll(attrValues);
        return attr;
    }

    private static AttributeValueType createAttributeValue(String value) {
        AttributeValueType attrVal = factory.createAttributeValueType();
        attrVal.getContent().add(value);
        return attrVal;
    }

    public static Collection<AttributeType> createAttributes(final Map<String, Set<String>> attrs) {
        return attrs.entrySet().stream().map(XACML2Util::createAttribute).collect(Collectors.toList());
    }

    public static RequestType createRequest(final Map<String, Set<String>> subjectAttrs,
                                            final Map<String, Set<String>> resourceAttrs,
                                            final Map<String, Set<String>> actionAttrs) {
        final RequestType request = factory.createRequestType();
        request.getSubject().add(XACML2Util.createSubject(subjectAttrs));
        request.getResource().add(XACML2Util.createResource(resourceAttrs));
        request.setAction(XACML2Util.createAction(actionAttrs));

        return request;
    }
}
