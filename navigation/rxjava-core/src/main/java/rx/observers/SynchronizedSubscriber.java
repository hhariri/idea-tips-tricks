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
package rx.observers;

import rx.Observer;
import rx.Subscriber;
import rx.operators.SafeObservableSubscription;

/**
 * A thread-safe Observer for transitioning states in operators.
 * <p>
 * Execution rules are:
 * <ul>
 * <li>Allow only single-threaded, synchronous, ordered execution of onNext, onCompleted, onError</li>
 * <li>Once an onComplete or onError are performed, no further calls can be executed</li>
 * <li>If unsubscribe is called, this means we call completed() and don't allow any further onNext calls.</li>
 * </ul>
 * 
 * @param <T>
 */
public final class SynchronizedSubscriber<T> extends Subscriber<T> {

    private final Observer<? super T> observer;

    public SynchronizedSubscriber(Subscriber<? super T> subscriber, Object lock) {
        super(subscriber);
        SafeObservableSubscription s = new SafeObservableSubscription();
        subscriber.add(s);
        this.observer = new SynchronizedObserver<T>(subscriber, s, lock);
    }

    /**
     * Used when synchronizing an Subscriber without access to the subscription.
     * 
     * @param Observer
     */
    public SynchronizedSubscriber(Subscriber<? super T> subscriber) {
        this(subscriber, new Object());
    }

    @Override
    public void onCompleted() {
        observer.onCompleted();
    }

    @Override
    public void onError(Throwable e) {
        observer.onError(e);
    }

    @Override
    public void onNext(T t) {
        observer.onNext(t);
    }

}