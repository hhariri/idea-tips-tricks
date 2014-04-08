/**
 * Copyright 2014 Netflix, Inc.
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
 */
package rx.schedulers;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Scheduler;
import rx.Subscription;
import rx.subscriptions.BooleanSubscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Func2;

public class TestScheduler extends Scheduler {
    private final Queue<TimedAction> queue = new PriorityQueue<TimedAction>(11, new CompareActionsByTime());

    private static class TimedAction {

        private final long time;
        private final Action1<Inner> action;
        private final Inner scheduler;

        private TimedAction(Inner scheduler, long time, Action1<Inner> action) {
            this.time = time;
            this.action = action;
            this.scheduler = scheduler;
        }

        @Override
        public String toString() {
            return String.format("TimedAction(time = %d, action = %s)", time, action.toString());
        }
    }

    private static class CompareActionsByTime implements Comparator<TimedAction> {
        @Override
        public int compare(TimedAction action1, TimedAction action2) {
            return Long.valueOf(action1.time).compareTo(Long.valueOf(action2.time));
        }
    }

    // Storing time in nanoseconds internally.
    private long time;

    @Override
    public long now() {
        return TimeUnit.NANOSECONDS.toMillis(time);
    }

    public void advanceTimeBy(long delayTime, TimeUnit unit) {
        advanceTimeTo(time + unit.toNanos(delayTime), TimeUnit.NANOSECONDS);
    }

    public void advanceTimeTo(long delayTime, TimeUnit unit) {
        long targetTime = unit.toNanos(delayTime);
        triggerActions(targetTime);
    }

    public void triggerActions() {
        triggerActions(time);
    }

    private void triggerActions(long targetTimeInNanos) {
        while (!queue.isEmpty()) {
            TimedAction current = queue.peek();
            if (current.time > targetTimeInNanos) {
                break;
            }
            time = current.time;
            queue.remove();

            // Only execute if not unsubscribed
            if (!current.scheduler.isUnsubscribed()) {
                current.action.call(current.scheduler);
            }
        }
        time = targetTimeInNanos;
    }

    @Override
    public Subscription schedule(Action1<Inner> action, long delayTime, TimeUnit unit) {
        InnerTestScheduler inner = new InnerTestScheduler();
        final TimedAction timedAction = new TimedAction(inner, time + unit.toNanos(delayTime), action);
        queue.add(timedAction);
        return inner;
    }

    @Override
    public Subscription schedule(Action1<Inner> action) {
        InnerTestScheduler inner = new InnerTestScheduler();
        final TimedAction timedAction = new TimedAction(inner, 0, action);
        queue.add(timedAction);
        return inner;
    }

    private final class InnerTestScheduler extends Inner {

        private BooleanSubscription s = new BooleanSubscription();

        @Override
        public void unsubscribe() {
            s.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return s.isUnsubscribed();
        }

        @Override
        public void schedule(Action1<Inner> action, long delayTime, TimeUnit unit) {
            final TimedAction timedAction = new TimedAction(this, time + unit.toNanos(delayTime), action);
            queue.add(timedAction);
        }

        @Override
        public void schedule(Action1<Inner> action) {
            final TimedAction timedAction = new TimedAction(this, 0, action);
            queue.add(timedAction);
        }

    }

}