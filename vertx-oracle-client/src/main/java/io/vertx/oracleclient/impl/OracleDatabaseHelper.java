/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl;

import io.vertx.oracleclient.OracleConnectOptions;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.datasource.OracleDataSource;

import static io.vertx.oracleclient.impl.Helper.getOrHandleSQLException;
import static io.vertx.oracleclient.impl.Helper.runOrHandleSQLException;

public class OracleDatabaseHelper {

  public static OracleDataSource createDataSource(OracleConnectOptions options) {
    OracleDataSource oracleDataSource =
      getOrHandleSQLException(oracle.jdbc.pool.OracleDataSource::new);

    runOrHandleSQLException(() ->
      oracleDataSource.setURL(composeJdbcUrl(options)));

    configureStandardOptions(oracleDataSource, options);
    configureExtendedOptions(oracleDataSource, options);
    configureJdbcDefaults(oracleDataSource);

    return oracleDataSource;
  }

  /**
   * Composes an Oracle JDBC URL from {@code OracleConnectOptions}, as
   * specified in the javadoc of
   * {@link #createDataSource(OracleConnectOptions)}
   *
   * @param options Oracle Connection options. Must not be {@code null}.
   * @return An Oracle Connection JDBC URL
   */
  private static String composeJdbcUrl(OracleConnectOptions options) {
    String serviceName = options.getDatabase();
    String host = options.getHost();
    int port = options.getPort();
    boolean isTcps = options.isSsl();

    return String.format("jdbc:oracle:thin:@%s%s%s%s",
      Boolean.TRUE.equals(isTcps) ? "tcps:" : "",
      host,
      port > 0 ? (":" + port) : "",
      serviceName != null ? ("/" + serviceName) : "");

  }

  /**
   * Configures an {@code OracleDataSource}.
   *
   * @param oracleDataSource An data source to configure
   * @param options          OracleConnectOptions options. Not null.
   */
  private static void configureStandardOptions(
    OracleDataSource oracleDataSource, OracleConnectOptions options) {

    String user = options.getUser();
    if (user != null) {
      runOrHandleSQLException(() -> oracleDataSource.setUser(user));
    }

    CharSequence password = options.getPassword();
    if (password != null) {
      runOrHandleSQLException(() ->
        oracleDataSource.setPassword(password.toString()));
    }

    int connectTimeout = options.getConnectTimeout();
    if (connectTimeout > 0) {
      runOrHandleSQLException(() ->
        oracleDataSource.setLoginTimeout(connectTimeout));
    }

  }

  private static void configureExtendedOptions(
    OracleDataSource oracleDataSource, OracleConnectOptions options) {

    // Handle the short form of the TNS_ADMIN option
    String tnsAdmin = options.getTnsAdmin();
    if (tnsAdmin != null) {
      // Configure using the long form: oracle.net.tns_admin
      runOrHandleSQLException(() ->
        oracleDataSource.setConnectionProperty(
          OracleConnection.CONNECTION_PROPERTY_TNS_ADMIN, tnsAdmin));
    }

    // TODO Iterate over the other properties.
  }

  /**
   * Configures an {@code oracleDataSource} with any connection properties that
   * this adapter requires by default.
   *
   * @param oracleDataSource An data source to configure
   */
  private static void configureJdbcDefaults(OracleDataSource oracleDataSource) {

    // Have the Oracle JDBC Driver implement behavior that the JDBC
    // Specification defines as correct. The javadoc for this property lists
    // all of it's effects. One effect is to have ResultSetMetaData describe
    // FLOAT columns as the FLOAT type, rather than the NUMBER type. This
    // effect allows the Oracle R2DBC Driver obtain correct metadata for
    // FLOAT type columns. The property is deprecated, but the deprecation note
    // explains that setting this to "false" is deprecated, and that it
    // should be set to true; If not set, the 21c driver uses a default value
    // of false.
    @SuppressWarnings("deprecation")
    String enableJdbcSpecCompliance =
      OracleConnection.CONNECTION_PROPERTY_J2EE13_COMPLIANT;
    runOrHandleSQLException(() ->
      oracleDataSource.setConnectionProperty(enableJdbcSpecCompliance, "true"));

    // Have the Oracle JDBC Driver cache PreparedStatements by default.
    runOrHandleSQLException(() -> {
      // Don't override a value set by user code
      String userValue = oracleDataSource.getConnectionProperty(
        OracleConnection.CONNECTION_PROPERTY_IMPLICIT_STATEMENT_CACHE_SIZE);

      if (userValue == null) {
        // The default value of the OPEN_CURSORS parameter in the 21c
        // and 19c databases is 50:
        // https://docs.oracle.com/en/database/oracle/oracle-database/21/refrn/OPEN_CURSORS.html#GUID-FAFD1247-06E5-4E64-917F-AEBD4703CF40
        // Assuming this default, then a default cache size of 25 will keep
        // each session at or below 50% of it's cursor capacity, which seems
        // reasonable.
        oracleDataSource.setConnectionProperty(
          OracleConnection.CONNECTION_PROPERTY_IMPLICIT_STATEMENT_CACHE_SIZE,
          "25");
      }
    });

    // TODO: Disable the result set cache? This is needed to support the
    //  SERIALIZABLE isolation level, which requires result set caching to be
    //  disabled.
  }
}
