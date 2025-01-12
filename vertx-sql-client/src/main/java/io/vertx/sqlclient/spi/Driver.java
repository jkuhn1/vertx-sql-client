/*
 * Copyright (C) 2020 IBM Corporation
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
 *
 */
package io.vertx.sqlclient.spi;

import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;

/**<
 * An entry point to the Vertx Reactive SQL Client
 * Every driver must implement this interface.
 */
public interface Driver {

  /**
   * Create a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param vertx the Vertx instance to be used with the connection pool
   * @param databases the list of databases
   * @param options the options for creating the pool
   * @return the connection pool
   */
  Pool createPool(Vertx vertx, List<? extends SqlConnectOptions> databases, PoolOptions options);

  /**
   * Create a connection factory to the given {@code database}.
   *
   * @param vertx the Vertx instance t
   * @param database the database to connect to
   * @return the connection factory
   */
  ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database);

  /**
   * @return true if the driver accepts the {@code connectOptions}, false otherwise
   */
  boolean acceptsOptions(SqlConnectOptions connectOptions);
  
  /**
   * @param connectionUri a connection URI
   * @return true if the driver accepts the {@code uri}, false otherwise
   */
  boolean acceptsUri(String connectionUri);
  
  /**
   * Returns a specific {@link SqlConnectOptions} instance corresponding to the {@code uri}.
   * 
   * @param <T> the expected type of {@code SqlConnectOptions}
   * @param connectionUri a connection URI
   * @return connect options specific to the driver
   * @throws IllegalArgumentException if the driver does not accept the {@code uri}
   */
  <T extends SqlConnectOptions> T getConnectOptions(String connectionUri);
  
  /**
   * Returns a specific {@link SqlConnectOptions} instance corresponding to the {@code uri} and 
   * initialized with {@code json}.
   * 
   * @param <T> the expected type of {@code SqlConnectOptions}
   * @param connectionUri a connection URI
   * @param json a json
   * @return connect options specific to the driver
   * @throws IllegalArgumentException if the driver does not accept the {@code uri}
   */
  <T extends SqlConnectOptions> T getConnectOptions(String connectionUri, JsonObject json);
}
