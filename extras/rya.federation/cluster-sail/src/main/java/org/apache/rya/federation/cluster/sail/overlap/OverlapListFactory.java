package org.apache.rya.federation.cluster.sail.overlap;

import static java.util.Objects.requireNonNull;

import org.apache.rya.federation.cluster.sail.OverlapList;
import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.apache.rya.federation.cluster.sail.exception.OverlapListException;

/**
 * Factory for creating {@link OverlapList} types.
 */
public class OverlapListFactory {
    /**
     * Private constructor to prevent instantiation.
     */
    private OverlapListFactory() {
    }

    /**
     * Holds the singleton instance.
     */
    private static class InstanceHolder {
        private static OverlapListFactory INSTANCE = new OverlapListFactory();
    }

    /**
     * @return the singleton instance of {@link OverlapListFactory}.
     */
    public static OverlapListFactory getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Creates the overlap list based on the database type name specified in the
     * config.
     * @param config the {@link ClusterFederationConfig}. (not null)
     * @return the {@link OverlapList}.
     * @throws OverlapListException
     */
    public OverlapList createOverlapList(final ClusterFederationConfig config) throws OverlapListException {
        final String type = requireNonNull(config).getOverlapListDbType();
        final OverlapListDbType overlapListDbType = OverlapListDbType.fromName(type);

        if (overlapListDbType != null) {
            return overlapListDbType.createOverlapList(config);
        } else {
            throw new OverlapListException("Unsupported overlap list database type: " + type);
        }
    }
}