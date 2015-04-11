/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.service.sql;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.plugin.CoreMixinPlugin;

import java.io.Closeable;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;


/**
 * Implementation of a SQL-using service.
 *
 * <p>This implementation does a few interesting things<br>
 *     - It's thread-safe
 *     - It allows applying additional driver-specific connection
 *     properties -- this allows us to do some light performance tuning in
 *     cases where we don't want to be as conservative as the driver developers
 *     - Caches DataSources. This cache is currently never cleared of stale entries
 *     -- if some plugin makes database connections to a ton of different databases
 *     we may want to implement this, but it is kinda unimportant.
 */
@NonnullByDefault
public class SqlServiceImpl implements SqlService, Closeable {

    private static final Map<String, Properties> PROTOCOL_SPECIFIC_PROPS;

    static {
        ImmutableMap.Builder<String, Properties> build = ImmutableMap.builder();
        final Properties mySqlProps = new Properties();
        mySqlProps.setProperty("useConfigs",
                "maxPerformance"); // Config options based on http://assets.en.oreilly
                // .com/1/event/21/Connector_J%20Performance%20Gems%20Presentation.pdf
        build.put("com.mysql.jdbc.Driver", mySqlProps);
        build.put("org.mariadb.jdbc.Driver", mySqlProps);

        PROTOCOL_SPECIFIC_PROPS = build.build();
    }

    private final LoadingCache<ConnectionInfo, HikariDataSource> connectionCache =
            CacheBuilder.newBuilder().removalListener(new RemovalListener<ConnectionInfo, HikariDataSource>() {
                @Override
                public void onRemoval(RemovalNotification<ConnectionInfo, HikariDataSource> notification) {
                    HikariDataSource source = notification.getValue();
                    if (source != null) {
                        source.close();
                    }
                }
            }).build(new CacheLoader<ConnectionInfo, HikariDataSource>() {
                @Override
                public HikariDataSource load(@Nonnull ConnectionInfo key) throws Exception {
                    HikariConfig config = new HikariConfig();
                    config.setUsername(key.getUser());
                    config.setPassword(key.getPassword());
                    config.setDriverClassName(key.getDriverClassName());
                    // https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing for info on pool sizing
                    config.setMaximumPoolSize((Runtime.getRuntime().availableProcessors() * 2) + 1);
                    Properties driverSpecificProperties = PROTOCOL_SPECIFIC_PROPS.get(key.getDriverClassName());
                    if (driverSpecificProperties != null) {
                        config.setDataSourceProperties(driverSpecificProperties);
                    }
                    config.setJdbcUrl(key.getAuthlessUrl());
                    return new HikariDataSource(config);
                }
            });

    @Override
    public DataSource getDataSource(String jdbcConnection) throws SQLException {
        jdbcConnection = getConnectionUrlFromAlias(jdbcConnection).or(jdbcConnection);
        ConnectionInfo info = ConnectionInfo.fromUrl(jdbcConnection);
        try {
            return this.connectionCache.get(info);
        } catch (ExecutionException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public void close() throws IOException {
        this.connectionCache.invalidateAll();
    }

    public static class ConnectionInfo {

        private static final Pattern URL_REGEX = Pattern.compile("(?:jdbc:)?([^:]+):(//)?(?:([^:]+)(?::([^@]+))?@)?(.*)");
        @Nullable private final String user;
        @Nullable private final String password;
        private final String driverClassName;
        private final String authlessUrl;
        private final String fullUrl;

        /**
         * Create a new ConnectionInfo with the give parameters
         * @param user The username to use when connecting to th database
         * @param password The password to connect with. If user is not null, password must not be null
         * @param driverClassName The class name of the driver to use for this connection
         * @param authlessUrl A JDBC url for this driver not containing authentication information
         * @param fullUrl The full jdbc url containing user, password, and database info
         */
        public ConnectionInfo(@Nullable String user, @Nullable String password, String driverClassName, String authlessUrl, String fullUrl) {
            this.user = user;
            this.password = password;
            this.driverClassName = driverClassName;
            this.authlessUrl = authlessUrl;
            this.fullUrl = fullUrl;
        }

        @Nullable
        public String getUser() {
            return this.user;
        }

        @Nullable
        public String getPassword() {
            return this.password;
        }

        public String getDriverClassName() {
            return this.driverClassName;
        }

        public String getAuthlessUrl() {
            return this.authlessUrl;
        }

        public String getFullUrl() {
            return this.fullUrl;
        }

        /**
         * Extracts the connection info from a JDBC url with additional authentication information as specified in {@link SqlService}.
         *
         * @param fullUrl The full JDBC URL as specified in SqlService
         * @return A constructed ConnectionInfo object using the info from the provided URL
         * @throws SQLException If the driver for the given URL is not present
         */
        public static ConnectionInfo fromUrl(String fullUrl) throws SQLException {
            Matcher match = URL_REGEX.matcher(fullUrl);
            if (!match.matches()) {
                throw new IllegalArgumentException("URL " + fullUrl + " is not a valid JDBC URL");
            }

            final String protocol = match.group(1);
            final boolean hasSlashes = match.group(2) != null;
            final String user = match.group(3);
            final String pass = match.group(4);
            final String serverDatabaseSpecifier = match.group(5);
            final String unauthedUrl = "jdbc:" + protocol + (hasSlashes ? "://" : ":") + serverDatabaseSpecifier;
            final String driverClass = DriverManager.getDriver(unauthedUrl).getClass().getCanonicalName();
            return new ConnectionInfo(user, pass, driverClass, unauthedUrl, fullUrl);
        }
    }

    @Override
    public Optional<String> getConnectionUrlFromAlias(String alias) {
        return Optional.fromNullable(CoreMixinPlugin.getGlobalConfig().getConfig().getSql().getAliases().get(alias));
    }

}
