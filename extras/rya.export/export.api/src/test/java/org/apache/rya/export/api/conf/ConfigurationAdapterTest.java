/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rya.export.api.conf;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.rya.export.DBType;
import org.apache.rya.export.JAXBMergeConfiguration;
import org.apache.rya.export.api.conf.MergeConfiguration.Builder;
import org.junit.Assert;
import org.junit.Ignore;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * Tests the methods of {@link ConfigurationAdapter}.
 */
public class ConfigurationAdapterTest {
    private static final String CRLF = "\r\n";
    private static final String CONFIG_XML_STRING = String.join(CRLF,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
        "<mc:configuration xmlns:mc=\"http://rya.apache.org/export/api/mergeconfig\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://rya.apache.org/export/api/mergeconfig MergeConfiguration.xsd \">",
        "    <mc:parentHostname>parent_hostname</mc:parentHostname>",
        "    <mc:parentUsername>parent_username</mc:parentUsername>",
        "    <mc:parentPassword>parent_password</mc:parentPassword>",
        "    <mc:parentRyaInstanceName>parent_instance</mc:parentRyaInstanceName>",
        "    <mc:parentTablePrefix>parent_</mc:parentTablePrefix>",
        "    <mc:parentTomcalUrl>http://localhost:8080</mc:parentTomcalUrl>",
        "    <mc:parentDBType>mongo</mc:parentDBType>",
        "    <mc:parentPort>1111</mc:parentPort>",
        "    <mc:childHostname>child_hostname</mc:childHostname>",
        "    <mc:childUsername>child_username</mc:childUsername>",
        "    <mc:childPassword>child_password</mc:childPassword>",
        "    <mc:childRyaInstanceName>child_instance</mc:childRyaInstanceName>",
        "    <mc:childTablePrefix>child_</mc:childTablePrefix>",
        "    <mc:childTomcalUrl>http://localhost:8888</mc:childTomcalUrl>",
        "    <mc:childDBType>mongo</mc:childDBType>",
        "    <mc:childPort>2222</mc:childPort>",
        "    <mc:mergePolicy>timestamp</mc:mergePolicy>",
        "    <mc:useNtpServer>true</mc:useNtpServer>",
        "    <mc:ntpServerHost>time.nist.gov</mc:ntpServerHost>",
        "    <mc:toolStartTime>dialog</mc:toolStartTime>",
        "</mc:configuration>"
        );

    private static final ImmutableList<String> TAGS = ImmutableList.of(
        "<mc:parentHostname>",
        "<mc:parentUsername>",
        "<mc:parentPassword>",
        "<mc:parentRyaInstanceName>",
        "<mc:parentTablePrefix>",
        "<mc:parentTomcalUrl>",
        "<mc:parentDBType>",
        "<mc:parentPort>",
        "<mc:childHostname>",
        "<mc:childUsername>",
        "<mc:childPassword>",
        "<mc:childRyaInstanceName>",
        "<mc:childTablePrefix>",
        "<mc:childTomcalUrl>",
        "<mc:childDBType>",
        "<mc:childPort>",
        "<mc:mergePolicy>",
        "<mc:useNtpServer>",
        "<mc:ntpServerHost>",
        "<mc:toolStartTime>"
    );

    /**
     * Empties the contents of the specified tag.
     * @param tag the tag to empty.
     * @return the modified config.
     */
    private static String createConfigWithNullTagFor(final String tag) {
        final List<String> list = new ArrayList<>();
        final String[] split = CONFIG_XML_STRING.split(CRLF);
        for (final String line : split) {
            if (!line.contains(tag)) {
                list.add(line);
            }
        }
        final String result = Joiner.on(CRLF).join(list);
        return result;
    }

    //@Test
    @Ignore
    public void testCreateConfig() throws MergeConfigurationException {
        final InputStream inputStream = IOUtils.toInputStream(CONFIG_XML_STRING, Charsets.UTF_8);

        final ConfigurationAdapter<Builder, JAXBMergeConfiguration> configurationAdapter = new ConfigurationAdapter<>();
        // TODO: uncomment when MergeConfigurationCLI is available
        //final MergeConfiguration mergeConfiguration = configurationAdapter.createConfig(MergeConfigurationCLI.createConfigurationFromStream(inputStream, JAXBMergeConfiguration.class));
        final MergeConfiguration mergeConfiguration = configurationAdapter.createConfig(null);

        Assert.assertNotNull(mergeConfiguration);
        Assert.assertEquals(MergeConfiguration.class, mergeConfiguration.getClass());

        // Parent Properties
        Assert.assertEquals("parent_hostname", mergeConfiguration.getParentHostname());
        Assert.assertEquals("parent_username", mergeConfiguration.getParentUsername());
        Assert.assertEquals("parent_password", mergeConfiguration.getParentPassword());
        Assert.assertEquals("parent_instance", mergeConfiguration.getParentRyaInstanceName());
        Assert.assertEquals("parent_", mergeConfiguration.getParentTablePrefix());
        Assert.assertEquals("http://localhost:8080", mergeConfiguration.getParentTomcatUrl());
        Assert.assertEquals(DBType.MONGO, mergeConfiguration.getParentDBType());
        Assert.assertEquals(1111, mergeConfiguration.getParentPort());

        // Child Properties
        Assert.assertEquals("child_hostname", mergeConfiguration.getChildHostname());
        Assert.assertEquals("child_username", mergeConfiguration.getChildUsername());
        Assert.assertEquals("child_password", mergeConfiguration.getChildPassword());
        Assert.assertEquals("child_instance", mergeConfiguration.getChildRyaInstanceName());
        Assert.assertEquals("child_", mergeConfiguration.getChildTablePrefix());
        Assert.assertEquals("http://localhost:8888", mergeConfiguration.getChildTomcatUrl());
        Assert.assertEquals(DBType.MONGO, mergeConfiguration.getChildDBType());
        Assert.assertEquals(2222, mergeConfiguration.getChildPort());

        // Other Properties
        Assert.assertEquals(Boolean.TRUE, mergeConfiguration.getUseNtpServer());
        Assert.assertEquals("time.nist.gov", mergeConfiguration.getNtpServerHost());
        Assert.assertEquals("dialog", mergeConfiguration.getToolStartTime());
    }

    //@Test
    @Ignore
    public void testNullTags() {
        for (final String tag : TAGS) {
            final String configXml = createConfigWithNullTagFor(tag);

            final InputStream inputStream = IOUtils.toInputStream(configXml, Charsets.UTF_8);

            final ConfigurationAdapter<Builder, JAXBMergeConfiguration> configurationAdapter = new ConfigurationAdapter<>();
            try {
                // TODO: uncomment when MergeConfigurationCLI is available
                //configurationAdapter.createConfig(MergeConfigurationCLI.createConfigurationFromStream(inputStream, JAXBMergeConfiguration.class));
                configurationAdapter.createConfig(null);
                // Shouldn't reach here
                fail();
            } catch (final MergeConfigurationException e) {
                if (e.getCause() instanceof NullPointerException) {
                    // Expected
                } else {
                    fail();
                }
            }
        }
    }
}