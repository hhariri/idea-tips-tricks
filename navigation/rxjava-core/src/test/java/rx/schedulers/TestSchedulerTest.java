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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import rx.Scheduler.Inner;
import rx.Subscription;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

public class TestSchedulerTest {

    @SuppressWarnings("unchecked")
    // mocking is unchecked, unfortunately
    @Test
    public final void testPeriodicScheduling() {
        final Func1<Long, Void> calledOp = mock(Func1.class);

        final TestScheduler scheduler = new TestScheduler();
        Subscription subscription = scheduler.schedulePeriodically(new Action1<Inner>() {
            @Override
            public void call(Inner inner) {
                System.out.println(scheduler.now());
                calledOp.call(scheduler.now());
            }
        }, 1, 2, TimeUnit.SECONDS);

        verify(calledOp, never()).call(anyLong());

        InOrder inOrder = Mockito.inOrder(calledOp);

        scheduler.advanceTimeBy(999L, TimeUnit.MILLISECONDS);
        inOrder.verify(calledOp, never()).call(anyLong());

        scheduler.advanceTimeBy(1L, TimeUnit.MILLISECONDS);
        inOrder.verify(calledOp, times(1)).call(1000L);

        scheduler.advanceTimeBy(1999L, TimeUnit.MILLISECONDS);
        inOrder.verify(calledOp, never()).call(3000L);

        scheduler.advanceTimeBy(1L, TimeUnit.MILLISECONDS);
        inOrder.verify(calledOp, times(1)).call(3000L);

        scheduler.advanceTimeBy(5L, TimeUnit.SECONDS);
        inOrder.verify(calledOp, times(1)).call(5000L);
        inOrder.verify(calledOp, times(1)).call(7000L);

        subscription.unsubscribe();
        scheduler.advanceTimeBy(11L, TimeUnit.SECONDS);
        inOrder.verify(calledOp, never()).call(anyLong());
    }

    @Test
    public final void testImmediateUnsubscribes() {
        TestScheduler s = new TestScheduler();

        final AtomicInteger counter = new AtomicInteger(0);

        Subscription subscription = s.schedule(new Action1<Inner>() {

            @Override
            public void call(Inner inner) {
                counter.incrementAndGet();
                System.out.println("counter: " + counter.get());
                inner.schedule(this);
            }

        });
        subscription.unsubscribe();
        assertEquals(0, counter.get());
    }

}
