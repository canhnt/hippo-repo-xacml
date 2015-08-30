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

import java.util.Arrays;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.jackrabbit.spi.Path;
import org.hippoecm.repository.security.xacml.XMLDataType;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributesType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;

public class JcrXACMLUtil {

    private static final String ACTION_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
    private static final String SUBJECT_CATEGORY = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
    private static final String RESOURCE_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";

    public enum AttributeIdType {
        SUBJECT_ID ("subject-id"),
        NODE_ID ("jcr:uuid"),
        NODE_PATH ("jcr:path"),
        ACTION_ID ("jcr:action");

        private final String value;

        AttributeIdType(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static final ObjectFactory factory = new ObjectFactory();

    public static RequestType createRequest(final Subject subject, final Path absPath, final int permissions) {
        final RequestType request = factory.createRequestType();

        final AttributesType subjectAttrs = createSubjectAttributes("alice");
        final AttributesType resourceAttrs = createResourceAttributes(absPath.getString(), AttributeIdType.NODE_PATH);
        final AttributesType actionAttrs = createActionAttributes(permissions);

        request.getAttributes().addAll(Arrays.asList(subjectAttrs, resourceAttrs, actionAttrs));
        return request;
    }

    private static AttributesType createActionAttributes(final int permissions) {
        final Set<String> jcrPermissions = JcrPermission.parseJcrPermissions(permissions);
        final AttributesType attrs = factory.createAttributesType();
        attrs.setCategory(ACTION_CATEGORY);

        final AttributeType actionAttr = createAttribute(AttributeIdType.ACTION_ID, XMLDataType.STRING,
                String.join("_", jcrPermissions));
        attrs.getAttribute().add(actionAttr);

        return attrs;
    }

    private static AttributesType createResourceAttributes(final String resource, final AttributeIdType resourceType) {
        final AttributesType attrs = factory.createAttributesType();
        attrs.setCategory(RESOURCE_CATEGORY);
        final AttributeType attr = createAttribute(resourceType, XMLDataType.STRING, resource);

        attrs.getAttribute().add(attr);
        return attrs;
    }

    private static AttributesType createSubjectAttributes(final String userId) {
        final AttributesType attrs = factory.createAttributesType();
        attrs.setCategory(SUBJECT_CATEGORY);
        final AttributeType attr = createAttribute(AttributeIdType.SUBJECT_ID, XMLDataType.STRING, userId);

        attrs.getAttribute().add(attr);
        return attrs;
    }

    private static AttributeType createAttribute(final AttributeIdType attributeId, final XMLDataType dataType, final Object value) {
        final AttributeType attr = factory.createAttributeType();
        attr.setAttributeId(attributeId.toString());
        final AttributeValueType attrValue = factory.createAttributeValueType();

        attrValue.setDataType(dataType.toString());
        attrValue.getContent().add(value);
        attr.getAttributeValue().add(attrValue);
        return attr;
    }

}
