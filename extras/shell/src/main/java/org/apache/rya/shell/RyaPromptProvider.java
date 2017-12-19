/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rya.shell;

import static java.util.Objects.requireNonNull;

import org.apache.rya.shell.SharedShellState.ShellState;
import org.apache.rya.shell.SharedShellState.StorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

/**
 * Customizes the Rya Shell's prompt.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RyaPromptProvider extends DefaultPromptProvider {

    private final SharedShellState sharedState;

    @Autowired
    public RyaPromptProvider(final SharedShellState sharedState) {
        this.sharedState = requireNonNull(sharedState);
    }

    @Override
    public String getPrompt() {
        final ShellState state = sharedState.getShellState();
        // figure out the storage name: disconnected, mongo host, or Accumulo instance.
        String storageName = "unknown";
        if (state.getStorageType().isPresent()) {
            if (state.getStorageType().get() == StorageType.ACCUMULO) {
                storageName = state.getAccumuloDetails().get().getInstanceName();
            } else if (state.getStorageType().get() == StorageType.MONGO) {
                storageName = state.getMongoDetails().get().getHostname();
            } else {
                throw new java.lang.IllegalStateException("Missing or unknown storage type.");
            }
        }
        switch(state.getConnectionState()) {
            case DISCONNECTED:
                return "rya> ";
            case CONNECTED_TO_STORAGE:
            return String.format("rya/%s> ", storageName);
            case CONNECTED_TO_INSTANCE:
                return String.format("rya/%s:%s> ",
                        storageName,
                            state.getRyaInstanceName().or("unknown"));
            default:
                return "rya> ";
        }
    }
}