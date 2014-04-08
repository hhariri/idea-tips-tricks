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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

public class OperatorMapTest {

    @Mock
    Observer<String> stringObserver;
    @Mock
    Observer<String> stringObserver2;

    final static Func2<String, Integer, String> APPEND_INDEX = new Func2<String, Integer, String>() {
        @Override
        public String call(String value, Integer index) {
            return value + index;
        }
    };

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMap() {
        Map<String, String> m1 = getMap("One");
        Map<String, String> m2 = getMap("Two");
        Observable<Map<String, String>> observable = Observable.from(m1, m2);

        Observable<String> m = observable.lift(new OperatorMap<Map<String, String>, String>(new Func1<Map<String, String>, String>() {

            @Override
            public String call(Map<String, String> map) {
                return map.get("firstName");
            }

        }));
        m.subscribe(stringObserver);

        verify(stringObserver, never()).onError(any(Throwable.class));
        verify(stringObserver, times(1)).onNext("OneFirst");
        verify(stringObserver, times(1)).onNext("TwoFirst");
        verify(stringObserver, times(1)).onCompleted();
    }

    @Test
    public void testMapMany() {
        /* simulate a top-level async call which returns IDs */
        Observable<Integer> ids = Observable.from(1, 2);

        /* now simulate the behavior to take those IDs and perform nested async calls based on them */
        Observable<String> m = ids.flatMap(new Func1<Integer, Observable<String>>() {

            @Override
            public Observable<String> call(Integer id) {
                /* simulate making a nested async call which creates another Observable */
                Observable<Map<String, String>> subObservable = null;
                if (id == 1) {
                    Map<String, String> m1 = getMap("One");
                    Map<String, String> m2 = getMap("Two");
                    subObservable = Observable.from(m1, m2);
                } else {
                    Map<String, String> m3 = getMap("Three");
                    Map<String, String> m4 = getMap("Four");
                    subObservable = Observable.from(m3, m4);
                }

                /* simulate kicking off the async call and performing a select on it to transform the data */
                return subObservable.map(new Func1<Map<String, String>, String>() {
                    @Override
                    public String call(Map<String, String> map) {
                        return map.get("firstName");
                    }
                });
            }

        });
        m.subscribe(stringObserver);

        verify(stringObserver, never()).onError(any(Throwable.class));
        verify(stringObserver, times(1)).onNext("OneFirst");
        verify(stringObserver, times(1)).onNext("TwoFirst");
        verify(stringObserver, times(1)).onNext("ThreeFirst");
        verify(stringObserver, times(1)).onNext("FourFirst");
        verify(stringObserver, times(1)).onCompleted();
    }

    @Test
    public void testMapMany2() {
        Map<String, String> m1 = getMap("One");
        Map<String, String> m2 = getMap("Two");
        Observable<Map<String, String>> observable1 = Observable.from(m1, m2);

        Map<String, String> m3 = getMap("Three");
        Map<String, String> m4 = getMap("Four");
        Observable<Map<String, String>> observable2 = Observable.from(m3, m4);

        Observable<Observable<Map<String, String>>> observable = Observable.from(observable1, observable2);

        Observable<String> m = observable.flatMap(new Func1<Observable<Map<String, String>>, Observable<String>>() {

            @Override
            public Observable<String> call(Observable<Map<String, String>> o) {
                return o.map(new Func1<Map<String, String>, String>() {

                    @Override
                    public String call(Map<String, String> map) {
                        return map.get("firstName");
                    }
                });
            }

        });
        m.subscribe(stringObserver);

        verify(stringObserver, never()).onError(any(Throwable.class));
        verify(stringObserver, times(1)).onNext("OneFirst");
        verify(stringObserver, times(1)).onNext("TwoFirst");
        verify(stringObserver, times(1)).onNext("ThreeFirst");
        verify(stringObserver, times(1)).onNext("FourFirst");
        verify(stringObserver, times(1)).onCompleted();

    }

    @Test
    public void testMapWithError() {
        Observable<String> w = Observable.from("one", "fail", "two", "three", "fail");
        Observable<String> m = w.lift(new OperatorMap<String, String>(new Func1<String, String>() {
            @Override
            public String call(String s) {
                if ("fail".equals(s)) {
                    throw new RuntimeException("Forced Failure");
                }
                return s;
            }
        }));

        m.subscribe(stringObserver);
        verify(stringObserver, times(1)).onNext("one");
        verify(stringObserver, never()).onNext("two");
        verify(stringObserver, never()).onNext("three");
        verify(stringObserver, never()).onCompleted();
        verify(stringObserver, times(1)).onError(any(Throwable.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapWithIssue417() {
        Observable.from(1).observeOn(Schedulers.computation())
                .map(new Func1<Integer, Integer>() {
                    public Integer call(Integer arg0) {
                        throw new IllegalArgumentException("any error");
                    }
                }).toBlockingObservable().single();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapWithErrorInFuncAndThreadPoolScheduler() throws InterruptedException {
        // The error will throw in one of threads in the thread pool.
        // If map does not handle it, the error will disappear.
        // so map needs to handle the error by itself.
        Observable<String> m = Observable.from("one")
                .observeOn(Schedulers.computation())
                .map(new Func1<String, String>() {
                    public String call(String arg0) {
                        throw new IllegalArgumentException("any error");
                    }
                });

        // block for response, expecting exception thrown
        m.toBlockingObservable().last();
    }

    /**
     * While mapping over range(1,1).last() we expect IllegalArgumentException since the sequence is empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testErrorPassesThruMap() {
        Observable.range(1, 0).last().map(new Func1<Integer, Integer>() {

            @Override
            public Integer call(Integer i) {
                return i;
            }

        }).toBlockingObservable().single();
    }

    /**
     * We expect IllegalStateException to pass thru map.
     */
    @Test(expected = IllegalStateException.class)
    public void testErrorPassesThruMap2() {
        Observable.error(new IllegalStateException()).map(new Func1<Object, Object>() {

            @Override
            public Object call(Object i) {
                return i;
            }

        }).toBlockingObservable().single();
    }

    /**
     * We expect an ArithmeticException exception here because last() emits a single value
     * but then we divide by 0.
     */
    @Test(expected = ArithmeticException.class)
    public void testMapWithErrorInFunc() {
        Observable.range(1, 1).last().map(new Func1<Integer, Integer>() {

            @Override
            public Integer call(Integer i) {
                return i / 0;
            }

        }).toBlockingObservable().single();
    }

    private static Map<String, String> getMap(String prefix) {
        Map<String, String> m = new HashMap<String, String>();
        m.put("firstName", prefix + "First");
        m.put("lastName", prefix + "Last");
        return m;
    }
}
