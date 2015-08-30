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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.combine.PermitOverridesPolicyAlg;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.support.finder.StaticPolicyFinderModule;
import com.sun.xacml.support.finder.StaticRefPolicyFinderModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import nl.uva.sne.midd.MIDDException;
import nl.uva.sne.xacml.policy.parsers.XACMLParsingException;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;

/**
 * @author cngo
 * @version $Id$
 * @since 2015-08-23
 */
public class SunPDP extends PDP {
    private static final Logger log = LoggerFactory.getLogger(SunPDP.class);

    public static final String[] STATIC_XACML2_POLICIES = {
            "src/main/resources/jcr-xacml2.xml"
    };
    public static final String[] STATIC_XACML2_REF_POLICIES = {
    };

    private static SunPDP theInstance;

    private SunPDP(final PDPConfig config) {
        super(config);
    }

    public static SunPDP get() {
        if (theInstance == null) {
            throw new IllegalArgumentException("Uninitialized PDP. Please call SunPDP#init()");
        }
        return theInstance;
    }

    public static void init() throws IOException, SAXException, ParserConfigurationException, MIDDException, XACMLParsingException, URISyntaxException, UnknownIdentifierException {
        if (theInstance != null) {
            log.warn("PDP already initialized");
            return;
        }

        //Initialization
        final StaticPolicyFinderModule staticModule = new StaticPolicyFinderModule(
                PermitOverridesPolicyAlg.algId, Arrays.asList(STATIC_XACML2_POLICIES));

        final StaticRefPolicyFinderModule staticRefModule = new StaticRefPolicyFinderModule(
                Arrays.asList(STATIC_XACML2_REF_POLICIES));

        theInstance = new SunPDP(createPDPConfig(Arrays.asList(staticModule, staticRefModule)));
    }

    private static PDPConfig createPDPConfig(final List<PolicyFinderModule> policyModules) {
        final PolicyFinder policyFinder = new PolicyFinder();
        policyFinder.setModules(new HashSet<>(policyModules));
        return new PDPConfig(null, policyFinder, null);
    }
}
