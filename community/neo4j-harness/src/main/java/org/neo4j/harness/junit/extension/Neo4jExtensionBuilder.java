/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.harness.junit.extension;

import java.nio.file.Path;
import java.util.function.Function;

import org.neo4j.annotations.api.PublicApi;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.harness.Neo4jBuilder;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.procedure.Procedure;
import org.neo4j.procedure.UserAggregationFunction;
import org.neo4j.procedure.UserFunction;

/**
 * {@link Neo4jExtension} extension builder.
 */
@PublicApi
public class Neo4jExtensionBuilder
{
    private Neo4jBuilder builder;

    Neo4jExtensionBuilder()
    {
        this( Neo4jBuilders.newInProcessBuilder() );
    }

    protected Neo4jExtensionBuilder( Neo4jBuilder builder )
    {
        this.builder = builder;
    }

    /**
     * Configure the Neo4j to use provided directory
     *
     * @param workingDirectory new working directory
     * @return this configurator instance
     */
    public Neo4jExtensionBuilder withFolder( Path workingDirectory )
    {
        builder = builder.withWorkingDir( workingDirectory );
        return this;
    }

    /**
     * Configure the Neo4j instance. Configuration here can be both configuration aimed at the server as well as the
     * database tuning options. Please refer to the Neo4j Manual for details on available configuration options.
     *
     * @param key the config key
     * @param value the config value
     * @param <T> the setting type
     * @return this configurator instance
     */
    public <T> Neo4jExtensionBuilder withConfig( Setting<T> key, T value )
    {
        builder = builder.withConfig( key, value );
        return this;
    }

    /**
     * Shortcut for configuring the server to use an unmanaged extension. Please refer to the Neo4j Manual on how to
     * write unmanaged extensions.
     *
     * @param mountPath the http path, relative to the server base URI, that this extension should be mounted at.
     * @param extension the unmanaged extension class.
     * @return this configurator instance
     */
    public Neo4jExtensionBuilder withUnmanagedExtension( String mountPath, Class<?> extension )
    {
        builder = builder.withUnmanagedExtension( mountPath, extension );
        return this;
    }

    /**
     * Shortcut for configuring the server to find and mount all unmanaged extensions in the given package.
     * @see #withUnmanagedExtension(String, Class)
     * @param mountPath the http path, relative to the server base URI, that this extension should be mounted at.
     * @param packageName a java package with extension classes.
     * @return this configurator instance
     */
    public Neo4jExtensionBuilder withUnmanagedExtension( String mountPath, String packageName )
    {
        builder = builder.withUnmanagedExtension( mountPath, packageName );
        return this;
    }

    /**
     * Disable web server on configured Neo4j instance.
     * For cases where web server is not required to test specific functionality it can be fully disabled using this tuning option.
     * @return this configurator instance.
     */
    public Neo4jExtensionBuilder withDisabledServer()
    {
        builder = builder.withDisabledServer();
        return this;
    }

    /**
     * Data fixtures to inject upon server build. This can be either a file with a plain-text cypher query
     * (for example, myFixture.cyp), or a directory containing such files with the suffix ".cyp".
     * @param cypherFileOrDirectory file with cypher statement, or directory containing ".cyp"-suffixed files.
     * @return this configurator instance
     */
    public Neo4jExtensionBuilder withFixture( Path cypherFileOrDirectory )
    {
        builder = builder.withFixture( cypherFileOrDirectory );
        return this;
    }

    /**
     * Data fixture to inject upon server build. This should be a valid Cypher statement.
     * @param fixtureStatement a cypher statement
     * @return this configurator instance
     */
    public Neo4jExtensionBuilder withFixture( String fixtureStatement )
    {
        builder = builder.withFixture( fixtureStatement );
        return this;
    }

    /**
     * Data fixture to inject upon server build. This should be a user implemented fixture function
     * operating on a {@link GraphDatabaseService} instance
     * @param fixtureFunction a fixture function
     * @return this configurator instance
     */
    public Neo4jExtensionBuilder withFixture( Function<GraphDatabaseService,Void> fixtureFunction )
    {
        builder = builder.withFixture( fixtureFunction );
        return this;
    }

    /**
     * Pre-populate the server with databases copied from the specified source directory.
     * The source directory needs to have sub-folders `databases/neo4j` in which the source store files are located.
     * @param sourceDirectory the directory to copy from
     * @return this configurator instance
     */
    public Neo4jExtensionBuilder copyFrom( Path sourceDirectory )
    {
        builder = builder.copyFrom( sourceDirectory );
        return this;
    }

    /**
     * Configure the server to load the specified procedure definition class. The class should contain one or more
     * methods annotated with {@link Procedure}, these will become available to call through
     * cypher.
     *
     * @param procedureClass a class containing one or more procedure definitions
     * @return this configurator instance
     */
    public Neo4jExtensionBuilder withProcedure( Class<?> procedureClass )
    {
        builder = builder.withProcedure( procedureClass );
        return this;
    }

    /**
     * Configure the server to load the specified function definition class. The class should contain one or more
     * methods annotated with {@link UserFunction}, these will become available to call through
     * cypher.
     *
     * @param functionClass a class containing one or more function definitions
     * @return this configurator instance
     */
    public Neo4jExtensionBuilder withFunction( Class<?> functionClass )
    {
        builder = builder.withFunction( functionClass );
        return this;
    }

    /**
     * Configure the server to load the specified aggregation function definition class. The class should contain one or more
     * methods annotated with {@link UserAggregationFunction}, these will become available to call through
     * cypher.
     *
     * @param functionClass a class containing one or more function definitions
     * @return this configurator instance
     */
    public Neo4jExtensionBuilder withAggregationFunction( Class<?> functionClass )
    {
        builder = builder.withAggregationFunction( functionClass );
        return this;
    }

    public Neo4jExtension build()
    {
        return new Neo4jExtension( builder );
    }
}
