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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import nl.uva.sne.midd.MIDDException;
import nl.uva.sne.xacml.PDP;
import nl.uva.sne.xacml.policy.finder.PolicyFinder;
import nl.uva.sne.xacml.policy.parsers.MIDDParsingException;
import nl.uva.sne.xacml.policy.parsers.XACMLParsingException;
import nl.uva.sne.xacml.util.XACMLUtil;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;

public class JcrPDP extends PDP {
    private static final Logger log = LoggerFactory.getLogger(JcrPDP.class);

    private static final String XACML3_POLICY_FILE = "src/main/resources/jcr-xacml.xml";
    private static JcrPDP theInstance;

    private JcrPDP(final PolicySetType policyset, final PolicyFinder policyFinder) throws MIDDParsingException, XACMLParsingException, MIDDException {
        super(policyset, policyFinder);
    }

    private JcrPDP(final PolicyType policy) throws MIDDParsingException, XACMLParsingException, MIDDException {
        super(policy);
    }

    public static JcrPDP get() {
        if (theInstance == null){
            throw new IllegalArgumentException("Uninitialized PDP. Please call JcrPDP#init()");
        }
        return theInstance;
    }

    public static void init() throws IOException, SAXException, ParserConfigurationException, MIDDException, XACMLParsingException {
        if (theInstance != null) {
            log.warn("PDP already initialized");
            return;
        }

        final PolicySetType policyset = XACMLUtil.unmarshalPolicySetType(XACML3_POLICY_FILE);

        theInstance = new JcrPDP(policyset, null);
        theInstance.initialize();
    }
}
