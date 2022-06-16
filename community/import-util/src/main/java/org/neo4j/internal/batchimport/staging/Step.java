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
package org.neo4j.internal.batchimport.staging;

import java.util.concurrent.TimeUnit;
import org.neo4j.internal.batchimport.Parallelizable;
import org.neo4j.internal.batchimport.stats.Key;
import org.neo4j.internal.batchimport.stats.StepStats;

/**
 * One step in {@link Stage}, where a {@link Stage} is a sequence of steps. Each step works on batches.
 * Batches are typically received from an upstream step, or produced in the step itself. If there are more steps
 * {@link #setDownstream(Step) downstream} then processed batches are passed down. Each step has maximum
 * "work-ahead" size where it awaits the downstream step to catch up if the queue size goes beyond that number.
 *
 * Batches are associated with a ticket, which is simply a long value incremented for each batch.
 * It's the first step that is responsible for generating these tickets, which will stay unchanged with
 * each batch all the way through the stage. Steps that have multiple threads processing batches can process
 * received batches in any order, but must make sure to send batches to its downstream
 * (i.e. calling {@link #receive(long, Object)} on its downstream step) ordered by ticket.
 *
 * @param <T> the type of batch objects received from upstream.
 */
public interface Step<T> extends Parallelizable, AutoCloseable, Panicable {
    /**
     * Whether or not tickets arrive in {@link #receive(long, Object)} ordered by ticket number.
     */
    int ORDER_SEND_DOWNSTREAM = 0x1;

    int RECYCLE_BATCHES = 0x2;

    /**
     * Starts the processing in this step, such that calls to {@link #receive(long, Object)} can be accepted.
     *
     * @param orderingGuarantees which ordering guarantees that will be upheld.
     */
    void start(int orderingGuarantees);

    /**
     * @return name of this step.
     */
    String name();

    /**
     * Receives a batch from upstream, queues it for processing.
     *
     * @param ticket ticket associates with the batch. Tickets are generated by producing steps and must follow
     * each batch all the way through a stage.
     * @param batch the batch object to queue for processing.
     * @return how long it time (millis) was spent waiting for a spot in the queue.
     */
    long receive(long ticket, T batch);

    /**
     * @return statistics about this step at this point in time.
     */
    StepStats stats();

    default boolean isIdle() {
        return false;
    }

    /**
     * Convenience method for getting a long value of a particular stat.
     *
     * @param key key to get stat for.
     * @return long stat for the given {@code key}.
     */
    default long longStat(Key key) {
        return stats().stat(key).asLong();
    }

    /**
     * @return max number of processors assignable to this step.
     */
    default int maxProcessors() {
        return 1;
    }

    /**
     * Called by upstream to let this step know that it will not send any more batches.
     */
    void endOfUpstream();

    /**
     * @return {@code true} if this step has received AND processed all batches from upstream, or in
     * the case of a producer, that this step has produced all batches.
     */
    boolean isCompleted();

    default void awaitCompleted() throws InterruptedException {
        awaitCompleted(Long.MAX_VALUE, TimeUnit.HOURS);
    }

    /**
     * Waits until this step is {@link #isCompleted() completed} and maximum the specified amount of time.
     * @param time max amount of time to wait for completion.
     * @param unit unit of time to wait.
     * @return {@code true} if this step was {@link #isCompleted() completed} before calling this method or
     * became completed during the specified time period, otherwise {@code false}.
     */
    boolean awaitCompleted(long time, TimeUnit unit) throws InterruptedException;

    /**
     * Called by the {@link Stage} when setting up the stage. This will form a pipeline of steps,
     * making up the stage.
     * @param downstreamStep {@link Step} to send batches to downstream.
     */
    void setDownstream(Step<?> downstreamStep);

    /**
     * Closes any resources kept open by this step. Called after a {@link Stage} is executed, whether successful or not.
     */
    @Override
    void close() throws Exception;
}
