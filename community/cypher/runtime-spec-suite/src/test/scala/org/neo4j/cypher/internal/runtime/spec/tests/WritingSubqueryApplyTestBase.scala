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
package org.neo4j.cypher.internal.runtime.spec.tests

import org.neo4j.cypher.internal.CypherRuntime
import org.neo4j.cypher.internal.RuntimeContext
import org.neo4j.cypher.internal.logical.builder.AbstractLogicalPlanBuilder.createNode
import org.neo4j.cypher.internal.runtime.spec.Edition
import org.neo4j.cypher.internal.runtime.spec.LogicalQueryBuilder
import org.neo4j.cypher.internal.runtime.spec.RuntimeTestSuite

abstract class WritingSubqueryApplyTestBase[CONTEXT <: RuntimeContext](edition: Edition[CONTEXT],
                                                                       runtime: CypherRuntime[CONTEXT]
                                                                      ) extends RuntimeTestSuite[CONTEXT](edition, runtime) {

  test("should handle RHS with R/W dependencies") {
    // given
    val sizeHint = 16
    val inputVals = (0 until sizeHint).toArray
    val input = inputValues(inputVals.map(Array[Any](_)): _*)

    given {
      nodeGraph(1)
    }

    // when
    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x")
      .apply(fromSubquery = true)
      .|.create(createNode("n"))
      .|.eager()
      .|.allNodeScan("y", "x")
      .input(variables = Seq("x"))
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    val expected = inputVals.flatMap(i => (0 until Math.pow(2, i).toInt).map(_ => i))

    // then
    runtimeResult should beColumns("x").withRows(singleColumn(expected))
  }

  test("should handle RHS with R/W dependencies on top of join") {
    // given
    val sizeHint = 16
    val inputVals = (0 until sizeHint).toArray
    val input = inputValues(inputVals.map(Array[Any](_)): _*)

    given {
      nodeGraph(1).head
    }

    // when
    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x")
      .apply(fromSubquery = true)
      .|.create(createNode("n"))
      .|.eager()
      .|.nodeHashJoin("y")
      .|.|.allNodeScan("y", "x")
      .|.allNodeScan("y", "x")
      .input(variables = Seq("x"))
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    val expected = inputVals.flatMap(i => (0 until Math.pow(2, i).toInt).map(_ => i))

    // then
    runtimeResult should beColumns("x").withRows(singleColumn(expected))
  }

  test("should handle RHS with R/W dependencies on top of union") {
    // given
    val sizeHint = 4
    val initialNodeCount = 1
    val inputVals = (0 until sizeHint).toArray
    val input = inputValues(inputVals.map(Array[Any](_)): _*)

    given {
      nodeGraph(initialNodeCount).head
    }

    // when
    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x")
      .apply(fromSubquery = true)
      .|.create(createNode("n"))
      .|.eager()
      .|.union()
      .|.|.allNodeScan("y", "x")
      .|.allNodeScan("y", "x")
      .input(variables = Seq("x"))
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    val expected = {
      var nodeCount = initialNodeCount
      for {
        inputVal <- inputVals
      } yield {
        val unionOutputRows = nodeCount * 2
        val inputValRepetitions = (0 until unionOutputRows).map(_ => inputVal)
        nodeCount += inputValRepetitions.size
        inputValRepetitions
      }
    }.flatten

    // then
    runtimeResult should beColumns("x").withRows(singleColumn(expected))
  }

  test("should handle RHS with R/W dependencies on top of cartesian product") {
    // given
    val sizeHint = 4
    val initialNodeCount = 1
    val inputVals = (0 until sizeHint).toArray
    val input = inputValues(inputVals.map(Array[Any](_)): _*)

    given {
      nodeGraph(initialNodeCount).head
    }

    // when
    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x")
      .apply(fromSubquery = true)
      .|.create(createNode("n"))
      .|.eager()
      .|.cartesianProduct()
      .|.|.allNodeScan("z", "x")
      .|.allNodeScan("y", "x")
      .input(variables = Seq("x"))
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    val expected = {
      var nodeCount = initialNodeCount
      for {
        inputVal <- inputVals
      } yield {
        val cartesianProductOutputRows = nodeCount * nodeCount
        val inputValRepetitions = (0 until cartesianProductOutputRows).map(_ => inputVal)
        nodeCount += inputValRepetitions.size
        inputValRepetitions
      }
    }.flatten

    // then
    runtimeResult should beColumns("x").withRows(singleColumn(expected))
  }

  test("should handle RHS with R/W dependencies on top of nested unions") {
    // given
    val sizeHint = 4
    val initialNodeCount = 1
    val inputVals = (0 until sizeHint).toArray
    val input = inputValues(inputVals.map(Array[Any](_)): _*)

    given {
      nodeGraph(initialNodeCount).head
    }

    // when
    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x")
      .apply(fromSubquery = true)
      .|.create(createNode("n"))
      .|.eager()
      .|.union()
      .|.|.union()
      .|.|.|.limit(1)
      .|.|.|.allNodeScan("y", "x")
      .|.|.allNodeScan("y", "x")
      .|.allNodeScan("y", "x")
      .input(variables = Seq("x"))
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    val expected = {
      var nodeCount = initialNodeCount
      for {
        inputVal <- inputVals
      } yield {
        val unionOutputRows = nodeCount * 2 + 1
        val inputValRepetitions = (0 until unionOutputRows).map(_ => inputVal)
        nodeCount += inputValRepetitions.size
        inputValRepetitions
      }
    }.flatten

    // then
    runtimeResult should beColumns("x").withRows(singleColumn(expected))
  }
}
