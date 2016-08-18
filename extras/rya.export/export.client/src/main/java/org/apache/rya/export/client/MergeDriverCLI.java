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
package org.apache.rya.export.client;

import static org.apache.rya.export.DBType.ACCUMULO;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.rya.export.DBType;
import org.apache.rya.export.JAXBAccumuloMergeConfiguration;
import org.apache.rya.export.JAXBMergeConfiguration;
import org.apache.rya.export.accumulo.AccumuloMerger;
import org.apache.rya.export.accumulo.AccumuloParentMetadataRepository;
import org.apache.rya.export.accumulo.AccumuloRyaStatementStore;
import org.apache.rya.export.accumulo.conf.AccumuloExportConstants;
import org.apache.rya.export.accumulo.util.TimeUtils;
import org.apache.rya.export.api.MergerException;
import org.apache.rya.export.api.conf.AccumuloConfigurationAdapter;
import org.apache.rya.export.api.conf.AccumuloMergeConfiguration;
import org.apache.rya.export.api.conf.ConfigurationAdapter;
import org.apache.rya.export.api.conf.MergeConfiguration;
import org.apache.rya.export.api.conf.MergeConfiguration.Builder;
import org.apache.rya.export.api.conf.MergeConfigurationCLI;
import org.apache.rya.export.api.conf.MergeConfigurationException;
import org.apache.rya.export.client.gui.DateTimePickerDialog;

import mvm.rya.indexing.accumulo.ConfigUtils;

/**
 * Drives the MergeTool.
 */
public class MergeDriverCLI {
    private static final Logger LOG = Logger.getLogger(MergeDriverCLI.class);

    private static final String DIALOG_TITLE = "Select a Start Time/Date";
    private static final String DIALOG_MESSAGE =
        "<html>Choose the time of the data to merge.<br>Only data modified AFTER the selected time will be merged.</html>";

    private static MergeConfiguration configuration;

    public static void main(final String [] args) throws ParseException {
        final String log4jConfiguration = System.getProperties().getProperty("log4j.configuration");
        if (StringUtils.isNotBlank(log4jConfiguration)) {
            final String parsedConfiguration = StringUtils.removeStart(log4jConfiguration, "file:");
            final File configFile = new File(parsedConfiguration);
            if (configFile.exists()) {
                DOMConfigurator.configure(parsedConfiguration);
            } else {
                BasicConfigurator.configure();
            }
        }
        boolean hasAccumuloDatastore = false;
        try {
            final ConfigurationAdapter<Builder, JAXBMergeConfiguration> configurationAdapter = new ConfigurationAdapter<>();
            configuration = MergeConfigurationCLI.createConfiguration(args, configurationAdapter, JAXBMergeConfiguration.class);
            if (configuration.getParentDBType() == DBType.ACCUMULO || configuration.getChildDBType() == DBType.ACCUMULO) {
                hasAccumuloDatastore = true;
                final AccumuloConfigurationAdapter accumuloConfigurationAdapter = new AccumuloConfigurationAdapter();
                configuration = MergeConfigurationCLI.createConfiguration(args, accumuloConfigurationAdapter, JAXBAccumuloMergeConfiguration.class);
            }
        } catch (final MergeConfigurationException e) {
            LOG.error("Configuration failed.", e);
        }


        final Configuration parentConfig = new Configuration();
        parentConfig.set(ConfigUtils.CLOUDBASE_INSTANCE, configuration.getParentRyaInstanceName());
        parentConfig.set(ConfigUtils.CLOUDBASE_USER, configuration.getParentUsername());
        parentConfig.set(ConfigUtils.CLOUDBASE_PASSWORD, configuration.getParentPassword());
        parentConfig.set(ConfigUtils.CLOUDBASE_TBL_PREFIX, configuration.getParentTablePrefix());
        parentConfig.set(AccumuloExportConstants.PARENT_TOMCAT_URL_PROP, configuration.getParentTomcatUrl());

        if (hasAccumuloDatastore) {
            parentConfig.setBoolean(ConfigUtils.USE_MOCK_INSTANCE, ((AccumuloMergeConfiguration)configuration).getParentInstanceType().isMock());
            parentConfig.set(AccumuloExportConstants.ACCUMULO_INSTANCE_TYPE_PROP, ((AccumuloMergeConfiguration)configuration).getParentInstanceType().toString());
            parentConfig.set(ConfigUtils.CLOUDBASE_ZOOKEEPERS, ((AccumuloMergeConfiguration)configuration).getParentZookeepers());
            parentConfig.set(ConfigUtils.CLOUDBASE_AUTHS, ((AccumuloMergeConfiguration)configuration).getParentAuths());
        }

        final Configuration childConfig = new Configuration();
        childConfig.set(ConfigUtils.CLOUDBASE_INSTANCE, configuration.getChildRyaInstanceName());
        childConfig.set(ConfigUtils.CLOUDBASE_USER, configuration.getChildUsername());
        childConfig.set(ConfigUtils.CLOUDBASE_PASSWORD, configuration.getChildPassword());
        childConfig.set(ConfigUtils.CLOUDBASE_TBL_PREFIX, configuration.getChildTablePrefix());
        childConfig.set(AccumuloExportConstants.CHILD_TOMCAT_URL_PROP, configuration.getChildTomcatUrl());

        if (hasAccumuloDatastore) {
            childConfig.setBoolean(ConfigUtils.USE_MOCK_INSTANCE, ((AccumuloMergeConfiguration)configuration).getChildInstanceType().isMock());
            childConfig.set(AccumuloExportConstants.ACCUMULO_INSTANCE_TYPE_PROP, ((AccumuloMergeConfiguration)configuration).getChildInstanceType().toString());
            childConfig.set(ConfigUtils.CLOUDBASE_ZOOKEEPERS, ((AccumuloMergeConfiguration)configuration).getChildZookeepers());
            childConfig.set(ConfigUtils.CLOUDBASE_AUTHS, ((AccumuloMergeConfiguration)configuration).getChildAuths());
        }


        String startTime = configuration.getToolStartTime();

        // Display start time dialog if requested
        if (AccumuloExportConstants.USE_START_TIME_DIALOG.equals(startTime)) {
            LOG.info("Select start time from dialog...");

            final DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog(DIALOG_TITLE, DIALOG_MESSAGE);
            dateTimePickerDialog.setVisible(true);

            final Date date = dateTimePickerDialog.getSelectedDateTime();
            startTime = AccumuloExportConstants.START_TIME_FORMATTER.format(date);
            LOG.info("Will merge all data after " + date);
        } else if (startTime != null) {
            try {
                final Date date = AccumuloExportConstants.START_TIME_FORMATTER.parse(startTime);
                LOG.info("Will merge all data after " + date);
            } catch (final java.text.ParseException e) {
                LOG.error("Unable to parse the provided start time: " + startTime, e);
            }
        }

        final boolean useTimeSync = configuration.getUseNtpServer();
        if (useTimeSync) {
            final String tomcatUrl = configuration.getChildTomcatUrl();
            final String ntpServerHost = configuration.getNtpServerHost();
            Long timeOffset = null;
            try {
                LOG.info("Comparing child machine's time to NTP server time...");
                timeOffset = TimeUtils.getNtpServerAndMachineTimeDifference(ntpServerHost, tomcatUrl);
            } catch (IOException | java.text.ParseException e) {
                LOG.error("Unable to get time difference between machine and NTP server.", e);
            }
            if (timeOffset != null) {
                parentConfig.set(AccumuloExportConstants.CHILD_TIME_OFFSET_PROP, "" + timeOffset);
            }
        }

        if(configuration.getParentDBType() == ACCUMULO && configuration.getChildDBType() == ACCUMULO) {
            //do traditional Mergetool shenanigans
            AccumuloRyaStatementStore parentAccumuloRyaStatementStore = null;
            AccumuloRyaStatementStore childAccumuloRyaStatementStore = null;
            try {
                parentAccumuloRyaStatementStore = new AccumuloRyaStatementStore(parentConfig);
                childAccumuloRyaStatementStore = new AccumuloRyaStatementStore(childConfig);
            } catch (final MergerException e) {
                LOG.error("Failed to create statement stores", e);
            }

            final AccumuloParentMetadataRepository accumuloParentMetadataRepository = new AccumuloParentMetadataRepository(parentAccumuloRyaStatementStore.getRyaDAO());

            final AccumuloMerger accumuloMerger = new AccumuloMerger(parentAccumuloRyaStatementStore, childAccumuloRyaStatementStore, accumuloParentMetadataRepository);
            accumuloMerger.runJob();
        } else {

        }

        LOG.info("Starting Merge Tool");

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread thread, final Throwable throwable) {
                LOG.error("Uncaught exception in " + thread.getName(), throwable);
            }
        });

        //final int returnCode = setupAndRun(args);

        LOG.info("Finished running Merge Tool");
        System.exit(1);
    }
}
