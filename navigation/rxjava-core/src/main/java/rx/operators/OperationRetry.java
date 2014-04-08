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

import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Scheduler.Inner;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.util.functions.Action1;

public class OperationRetry {

    private static final int INFINITE_RETRY = -1;

    public static <T> OnSubscribeFunc<T> retry(final Observable<T> observable, final int retryCount) {
        return new Retry<T>(observable, retryCount);
    }

    public static <T> OnSubscribeFunc<T> retry(final Observable<T> observable) {
        return new Retry<T>(observable, INFINITE_RETRY);
    }

    private static class Retry<T> implements OnSubscribeFunc<T> {

        private final Observable<T> source;
        private final int retryCount;
        private final AtomicInteger attempts = new AtomicInteger(0);

        public Retry(Observable<T> source, int retryCount) {
            this.source = source;
            this.retryCount = retryCount;
        }

        @Override
        public Subscription onSubscribe(final Observer<? super T> observer) {
            return Schedulers.trampoline().schedule(new Action1<Inner>() {

                @Override
                public void call(final Inner inner) {
                    final Action1<Inner> _self = this;
                    attempts.incrementAndGet();
                    source.subscribe(new Observer<T>() {

                        @Override
                        public void onCompleted() {
                            observer.onCompleted();
                        }

                        @Override
                        public void onError(Throwable e) {
                            if ((retryCount == INFINITE_RETRY || attempts.get() <= retryCount) && !inner.isUnsubscribed()) {
                                // retry again
                                inner.schedule(_self);
                            } else {
                                // give up and pass the failure
                                observer.onError(e);
                            }
                        }

                        @Override
                        public void onNext(T v) {
                            observer.onNext(v);
                        }

                    });
                }
            });
        }

    }
}
