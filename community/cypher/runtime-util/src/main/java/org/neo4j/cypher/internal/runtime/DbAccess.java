/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.cypher.internal.runtime;

import java.util.Optional;

import org.neo4j.internal.kernel.api.NodeCursor;
import org.neo4j.internal.kernel.api.PropertyCursor;
import org.neo4j.internal.kernel.api.RelationshipScanCursor;
import org.neo4j.values.AnyValue;
import org.neo4j.values.storable.TextValue;
import org.neo4j.values.storable.Value;
import org.neo4j.values.virtual.ListValue;
import org.neo4j.values.virtual.MapValue;

/**
 * Used to expose db access to expressions
 */
public interface DbAccess extends EntityById
{
    Value nodeProperty( long node,
            int property,
            NodeCursor nodeCursor,
            PropertyCursor propertyCursor,
            boolean throwOnDeleted );

    int[] nodePropertyIds( long node,
            NodeCursor nodeCursor,
            PropertyCursor propertyCursor );

    int propertyKey( String name );

    int nodeLabel( String name );

    int relationshipType( String name );

    boolean nodeHasProperty( long node,
            int property,
            NodeCursor nodeCursor,
            PropertyCursor propertyCursor );

    Value relationshipProperty( long node,
            int property,
            RelationshipScanCursor relationshipScanCursor,
            PropertyCursor propertyCursor,
            boolean throwOnDeleted );

    int[] relationshipPropertyIds( long node,
            RelationshipScanCursor relationshipScanCursor,
            PropertyCursor propertyCursor );

    boolean relationshipHasProperty( long node,
            int property,
            RelationshipScanCursor relationshipScanCursor,
            PropertyCursor propertyCursor );

    int nodeGetOutgoingDegree( long node, NodeCursor nodeCursor );

    int nodeGetOutgoingDegree( long node, int relationship, NodeCursor nodeCursor );

    int nodeGetIncomingDegree( long node, NodeCursor nodeCursor );

    int nodeGetIncomingDegree( long node, int relationship, NodeCursor nodeCursor );

    int nodeGetTotalDegree( long node, NodeCursor nodeCursor );

    int nodeGetTotalDegree( long node, int relationship, NodeCursor nodeCursor );

    void singleRelationship( long id, RelationshipScanCursor cursor );

    ListValue getLabelsForNode( long id, NodeCursor nodeCursor );

    TextValue getTypeForRelationship( long id, RelationshipScanCursor relationshipCursor );

    boolean isLabelSetOnNode( int label, long id, NodeCursor nodeCursor );

    String getPropertyKeyName( int token );

    MapValue nodeAsMap( long id, NodeCursor nodeCursor, PropertyCursor propertyCursor );

    MapValue relationshipAsMap( long id, RelationshipScanCursor relationshipCursor, PropertyCursor propertyCursor );

    Value getTxStateNodePropertyOrNull( long nodeId, int propertyKey );

    Value getTxStateRelationshipPropertyOrNull( long relId, int propertyKey );

    AnyValue callFunction( int id, AnyValue[] args, String[] allowed );

    /**
     * @return `Optional.empty` if TxState has no changes.
     *         `Optional.of(true)` if the property was changed.
     *         `Optional.of(false)` if the property or the entity were deleted in TxState.
     */
    Optional<Boolean> hasTxStatePropertyForCachedNodeProperty( long nodeId, int propertyKeyId );

    /**
     * @return `Optional.empty` if TxState has no changes.
     *         `Optional.of(true)` if the property was changed.
     *         `Optional.of(false)` if the property or the entity were deleted in TxState.
     */
    Optional<Boolean> hasTxStatePropertyForCachedRelationshipProperty( long relId, int propertyKeyId );

    long nodeCountByCountStore(int labelId);

    long relationshipCountByCountStore(int startLabelId, int typeId, int endLabelId);
}
