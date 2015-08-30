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

package org.hippoecm.repository.security.xacml.sne;

import org.apache.jackrabbit.spi.Path;
import org.hippoecm.repository.security.xacml.XACMLAccessManager;
import org.hippoecm.repository.security.xacml.sne.utils.JcrXACMLUtil;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;

public class XACMLAccessManagerImpl extends XACMLAccessManager {
    @Override
    protected boolean isPermit(final Path absPath, final int permissions) {
        final RequestType request = JcrXACMLUtil.createRequest(subject, absPath, permissions);

        final ResponseType response = JcrPDP.get().evaluate(request);
        return response.getResult().get(0).getDecision() == DecisionType.PERMIT;
    }
}
