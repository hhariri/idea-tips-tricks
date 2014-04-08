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
package rx.operators;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Scheduler;
import rx.Scheduler.Inner;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Func0;
import rx.util.functions.Func1;

/**
 * Applies a timeout policy for each element in the observable sequence, using
 * the specified scheduler to run timeout timers. If the next element isn't
 * received within the specified timeout duration starting from its predecessor,
 * the other observable sequence is used to produce future messages from that
 * point on.
 */
public final class OperationTimeout {

    public static <T> OnSubscribeFunc<T> timeout(Observable<? extends T> source, long timeout, TimeUnit timeUnit) {
        return new Timeout<T>(source, timeout, timeUnit, null, Schedulers.computation());
    }

    public static <T> OnSubscribeFunc<T> timeout(Observable<? extends T> sequence, long timeout, TimeUnit timeUnit, Observable<? extends T> other) {
        return new Timeout<T>(sequence, timeout, timeUnit, other, Schedulers.computation());
    }

    public static <T> OnSubscribeFunc<T> timeout(Observable<? extends T> source, long timeout, TimeUnit timeUnit, Scheduler scheduler) {
        return new Timeout<T>(source, timeout, timeUnit, null, scheduler);
    }

    public static <T> OnSubscribeFunc<T> timeout(Observable<? extends T> sequence, long timeout, TimeUnit timeUnit, Observable<? extends T> other, Scheduler scheduler) {
        return new Timeout<T>(sequence, timeout, timeUnit, other, scheduler);
    }

    private static class Timeout<T> implements Observable.OnSubscribeFunc<T> {
        private final Observable<? extends T> source;
        private final long timeout;
        private final TimeUnit timeUnit;
        private final Scheduler scheduler;
        private final Observable<? extends T> other;

        private Timeout(Observable<? extends T> source, long timeout, TimeUnit timeUnit, Observable<? extends T> other, Scheduler scheduler) {
            this.source = source;
            this.timeout = timeout;
            this.timeUnit = timeUnit;
            this.other = other;
            this.scheduler = scheduler;
        }

        @Override
        public Subscription onSubscribe(final Observer<? super T> observer) {
            final AtomicBoolean terminated = new AtomicBoolean(false);
            final AtomicLong actual = new AtomicLong(0L);  // Required to handle race between onNext and timeout
            final SerialSubscription serial = new SerialSubscription();
            final Object gate = new Object();
            CompositeSubscription composite = new CompositeSubscription();
            final Func0<Subscription> schedule = new Func0<Subscription>() {
                @Override
                public Subscription call() {
                    final long expected = actual.get();
                    return scheduler.schedule(new Action1<Inner>() {
                        @Override
                        public void call(Inner inner) {
                            boolean timeoutWins = false;
                            synchronized (gate) {
                                if (expected == actual.get() && !terminated.getAndSet(true)) {
                                    timeoutWins = true;
                                }
                            }
                            if (timeoutWins) {
                                if (other == null) {
                                    observer.onError(new TimeoutException());
                                }
                                else {
                                    serial.set(other.subscribe(observer));
                                }
                            }

                        }
                    }, timeout, timeUnit);
                }
            };
            SafeObservableSubscription subscription = new SafeObservableSubscription();
            composite.add(subscription.wrap(source.subscribe(new Observer<T>() {
                @Override
                public void onNext(T value) {
                    boolean onNextWins = false;
                    synchronized (gate) {
                        if (!terminated.get()) {
                            actual.incrementAndGet();
                            onNextWins = true;
                        }
                    }
                    if (onNextWins) {
                        serial.setSubscription(schedule.call());
                        observer.onNext(value);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    boolean onErrorWins = false;
                    synchronized (gate) {
                        if (!terminated.getAndSet(true)) {
                            onErrorWins = true;
                        }
                    }
                    if (onErrorWins) {
                        serial.unsubscribe();
                        observer.onError(error);
                    }
                }

                @Override
                public void onCompleted() {
                    boolean onCompletedWins = false;
                    synchronized (gate) {
                        if (!terminated.getAndSet(true)) {
                            onCompletedWins = true;
                        }
                    }
                    if (onCompletedWins) {
                        serial.unsubscribe();
                        observer.onCompleted();
                    }
                }
            })));
            composite.add(serial);
            serial.setSubscription(schedule.call());
            return composite;
        }
    }

    /** Timeout using a per-item observable sequence. */
    public static <T, U, V> OnSubscribeFunc<T> timeoutSelector(Observable<? extends T> source, Func0<? extends Observable<U>> firstValueTimeout, Func1<? super T, ? extends Observable<V>> valueTimeout, Observable<? extends T> other) {
        return new TimeoutSelector<T, U, V>(source, firstValueTimeout, valueTimeout, other);
    }

    /** Timeout using a per-item observable sequence. */
    private static final class TimeoutSelector<T, U, V> implements OnSubscribeFunc<T> {
        final Observable<? extends T> source;
        final Func0<? extends Observable<U>> firstValueTimeout;
        final Func1<? super T, ? extends Observable<V>> valueTimeout;
        final Observable<? extends T> other;

        public TimeoutSelector(Observable<? extends T> source, Func0<? extends Observable<U>> firstValueTimeout, Func1<? super T, ? extends Observable<V>> valueTimeout, Observable<? extends T> other) {
            this.source = source;
            this.firstValueTimeout = firstValueTimeout;
            this.valueTimeout = valueTimeout;
            this.other = other;
        }

        @Override
        public Subscription onSubscribe(Observer<? super T> t1) {
            CompositeSubscription csub = new CompositeSubscription();

            SourceObserver<T, V> so = new SourceObserver<T, V>(t1, valueTimeout, other, csub);
            if (firstValueTimeout != null) {
                Observable<U> o;
                try {
                    o = firstValueTimeout.call();
                } catch (Throwable t) {
                    t1.onError(t);
                    return Subscriptions.empty();
                }

                csub.add(o.subscribe(new TimeoutObserver<U>(so)));
            }
            csub.add(source.subscribe(so));
            return csub;
        }

        /** Observe the source. */
        private static final class SourceObserver<T, V> implements Observer<T>, TimeoutCallback {
            final Observer<? super T> observer;
            final Func1<? super T, ? extends Observable<V>> valueTimeout;
            final Observable<? extends T> other;
            final CompositeSubscription cancel;
            final Object guard;
            boolean done;
            final SerialSubscription tsub;
            final TimeoutObserver<V> to;

            public SourceObserver(Observer<? super T> observer, Func1<? super T, ? extends Observable<V>> valueTimeout, Observable<? extends T> other, CompositeSubscription cancel) {
                this.observer = observer;
                this.valueTimeout = valueTimeout;
                this.other = other;
                this.cancel = cancel;
                this.guard = new Object();
                this.tsub = new SerialSubscription();
                this.cancel.add(tsub);
                this.to = new TimeoutObserver<V>(this);
            }

            @Override
            public void onNext(T args) {
                tsub.set(Subscriptions.empty());

                synchronized (guard) {
                    if (done) {
                        return;
                    }
                    observer.onNext(args);
                }

                Observable<V> o;
                try {
                    o = valueTimeout.call(args);
                } catch (Throwable t) {
                    onError(t);
                    return;
                }

                SerialSubscription osub = new SerialSubscription();
                tsub.set(osub);

                osub.set(o.subscribe(to));
            }

            @Override
            public void onError(Throwable e) {
                synchronized (guard) {
                    if (done) {
                        return;
                    }
                    done = true;
                    observer.onError(e);
                }
                cancel.unsubscribe();
            }

            @Override
            public void onCompleted() {
                synchronized (guard) {
                    if (done) {
                        return;
                    }
                    done = true;
                    observer.onCompleted();
                }
                cancel.unsubscribe();
            }

            @Override
            public void timeout() {
                if (other != null) {
                    synchronized (guard) {
                        if (done) {
                            return;
                        }
                        done = true;
                    }
                    cancel.clear();
                    cancel.add(other.subscribe(observer));
                } else {
                    onCompleted();
                }
            }
        }

        /** The timeout callback. */
        private interface TimeoutCallback {
            void timeout();

            void onError(Throwable t);
        }

        /** Observe the timeout. */
        private static final class TimeoutObserver<V> implements Observer<V> {
            final TimeoutCallback parent;

            public TimeoutObserver(TimeoutCallback parent) {
                this.parent = parent;
            }

            @Override
            public void onNext(V args) {
                parent.timeout();
            }

            @Override
            public void onError(Throwable e) {
                parent.onError(e);
            }

            @Override
            public void onCompleted() {
                parent.timeout();
            }
        }
    }
}
