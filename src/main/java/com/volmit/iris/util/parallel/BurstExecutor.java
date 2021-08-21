/*
 * Iris is a World Generator for Minecraft Bukkit Servers
 * Copyright (c) 2021 Arcane Arts (Volmit Software)
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.volmit.iris.util.parallel;

import com.volmit.iris.Iris;
import com.volmit.iris.util.collection.KList;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.*;

@SuppressWarnings("ALL")
public class BurstExecutor {
    private final ExecutorService executor;
    private final KList<Future<?>> futures;

    public BurstExecutor(ExecutorService executor, int burstSizeEstimate) {
        this.executor = executor;
        futures = new KList<Future<?>>(burstSizeEstimate);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Future<?> queue(Runnable r) {
        synchronized (futures) {

            Future<?> c = executor.submit(r);
            futures.add(c);
            return c;
        }
    }

    public BurstExecutor queue(List<Runnable> r) {
        synchronized (futures) {
            for (Runnable i : new KList<>(r)) {
                queue(i);
            }
        }

        return this;
    }

    public BurstExecutor queue(Runnable[] r) {
        synchronized (futures) {
            for (Runnable i : r) {
                queue(i);
            }
        }

        return this;
    }

    public void complete() {
        synchronized (futures) {
            if (futures.isEmpty()) {
                return;
            }

            try {
                for(Future<?> i : futures)
                {
                    i.get();
                }

                futures.clear();
            } catch (InterruptedException | ExecutionException e) {
                Iris.reportError(e);
            }
        }
    }

    public boolean complete(long maxDur) {
        synchronized (futures) {
            if (futures.isEmpty()) {
                return true;
            }

            try {
                try {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(maxDur, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    return false;
                }
                futures.clear();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Iris.reportError(e);
            }
        }

        return false;
    }
}
