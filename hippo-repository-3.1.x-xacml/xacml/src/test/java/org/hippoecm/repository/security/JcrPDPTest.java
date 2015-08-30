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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.jackrabbit.core.security.authorization.Permission;
import org.hippoecm.repository.security.xacml.sne.JcrPDP;
import org.hippoecm.repository.security.xacml.sne.utils.JcrXACMLUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import nl.uva.sne.midd.MIDDException;
import nl.uva.sne.xacml.policy.parsers.XACMLParsingException;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;
import static org.junit.Assert.*;

public class JcrPDPTest {

    @BeforeClass
    public static void init() throws XACMLParsingException, SAXException, MIDDException, ParserConfigurationException, IOException {
        JcrPDP.init();
    }

    @Test
    public void aliceShouldbePermitted() {
//        final RequestType request = JcrXACMLUtil.createRequest("/content/documents", Permission.READ | Permission.ADD_NODE);
//        final DecisionType decision = JcrPDP.get().evaluate(request).getResult().get(0).getDecision();
//        assertEquals(DecisionType.PERMIT, decision);
    }
}