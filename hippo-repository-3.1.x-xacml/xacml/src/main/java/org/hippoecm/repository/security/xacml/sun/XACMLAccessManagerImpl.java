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

package org.hippoecm.repository.security.xacml.sun;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import com.sun.xacml.ctx.Result;

import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.spi.Path;
import org.hippoecm.repository.security.xacml.JcrAttributeProfile;
import org.hippoecm.repository.security.xacml.XACMLAccessManager;
import org.hippoecm.repository.security.xacml.sun.utils.XACML2Util;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;

public class XACMLAccessManagerImpl extends XACMLAccessManager {
    private static final Logger log = LoggerFactory.getLogger(XACMLAccessManagerImpl.class);

    JcrAttributeProfile attrProfile;

    @Override
    public void init(final AMContext context) throws Exception {
        super.init(context);
        attrProfile = new JcrAttributeProfile(subject, npRes, ntMgr, itemMgr, hierMgr, zombieHierMgr);

        SunPDP.init();
    }

    @Override
    protected boolean isPermit(final Path absPath, final int permissions) {
        final RequestType request;
        try {
            request = createRequest(absPath, permissions);
        } catch (RepositoryException e) {
            log.error("Cannot create authorization request", e);
            return false;
        }
        final Result result = (Result) SunPDP.get().evaluate(request).getResults().iterator().next();
        if (result == null) {
            log.error("Cannot obtain result from SunPDP");
        }
        return result.getDecision() == Result.DECISION_PERMIT;
    }

    private RequestType createRequest(final Path absPath, final int permissions) throws RepositoryException {
        final Map<String, Set<String>> subjectAttrs = attrProfile.getSubjectAttributes();
        final Map<String, Set<String>> resourceAttrs = attrProfile.parse(absPath);
        final Map<String, Set<String>> actionAttrs = attrProfile.parse(permissions);

        log.info("subject: {}\nresource: {}\naction: {}", jsonPrint(subjectAttrs), jsonPrint(resourceAttrs), jsonPrint(actionAttrs));

        return XACML2Util.createRequest(subjectAttrs, resourceAttrs, actionAttrs);
    }

    private static String jsonPrint(final Map<String, Set<String>> attrs) {
        try {
            final StringWriter writer = new StringWriter();
            JSONValue.writeJSONString(attrs, writer);
            return writer.toString();
        } catch (IOException e) {
            log.error("Failed to parse attributes to json", e.getMessage());
        }
        return null;
    }
}

