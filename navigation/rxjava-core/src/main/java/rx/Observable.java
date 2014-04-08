/**
 * Copyright 2014 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package rx;

import static rx.util.functions.Functions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import rx.joins.Pattern2;
import rx.joins.Plan0;
import rx.observables.BlockingObservable;
import rx.observables.ConnectableObservable;
import rx.observables.GroupedObservable;
import rx.observers.SafeSubscriber;
import rx.operators.OperationAll;
import rx.operators.OperationAmb;
import rx.operators.OperationAny;
import rx.operators.OperationAsObservable;
import rx.operators.OperationAverage;
import rx.operators.OperationBuffer;
import rx.operators.OperationCache;
import rx.operators.OperationCombineLatest;
import rx.operators.OperationConcat;
import rx.operators.OperationDebounce;
import rx.operators.OperationDefaultIfEmpty;
import rx.operators.OperationDefer;
import rx.operators.OperationDelay;
import rx.operators.OperationDematerialize;
import rx.operators.OperationDistinct;
import rx.operators.OperationDistinctUntilChanged;
import rx.operators.OperatorDoOnEach;
import rx.operators.OperationElementAt;
import rx.operators.OperationFilter;
import rx.operators.OperationFinally;
import rx.operators.OperationFlatMap;
import rx.operators.OperationGroupByUntil;
import rx.operators.OperationGroupJoin;
import rx.operators.OperationInterval;
import rx.operators.OperationJoin;
import rx.operators.OperationJoinPatterns;
import rx.operators.OperationMaterialize;
import rx.operators.OperationMergeDelayError;
import rx.operators.OperationMinMax;
import rx.operators.OperationMulticast;
import rx.operators.OperationObserveOn;
import rx.operators.OperationOnErrorResumeNextViaFunction;
import rx.operators.OperationOnErrorResumeNextViaObservable;
import rx.operators.OperationOnErrorReturn;
import rx.operators.OperationOnExceptionResumeNextViaObservable;
import rx.operators.OperationParallelMerge;
import rx.operators.OperationRepeat;
import rx.operators.OperationReplay;
import rx.operators.OperationRetry;
import rx.operators.OperationSample;
import rx.operators.OperationScan;
import rx.operators.OperationSequenceEqual;
import rx.operators.OperationSingle;
import rx.operators.OperationSkip;
import rx.operators.OperationSkipLast;
import rx.operators.OperationSkipUntil;
import rx.operators.OperationSkipWhile;
import rx.operators.OperationSubscribeOn;
import rx.operators.OperationSum;
import rx.operators.OperationSwitch;
import rx.operators.OperationSynchronize;
import rx.operators.OperationTakeLast;
import rx.operators.OperationTakeTimed;
import rx.operators.OperationTakeUntil;
import rx.operators.OperationTakeWhile;
import rx.operators.OperationThrottleFirst;
import rx.operators.OperationTimeInterval;
import rx.operators.OperationTimeout;
import rx.operators.OperationTimer;
import rx.operators.OperationToMap;
import rx.operators.OperationToMultimap;
import rx.operators.OperationToObservableFuture;
import rx.operators.OperationUsing;
import rx.operators.OperationWindow;
import rx.operators.OperatorZip;
import rx.operators.OperatorCast;
import rx.operators.OperatorFromIterable;
import rx.operators.OperatorGroupBy;
import rx.operators.OperatorMap;
import rx.operators.OperatorMerge;
import rx.operators.OperatorParallel;
import rx.operators.OperatorTake;
import rx.operators.OperatorTimestamp;
import rx.operators.OperatorToObservableList;
import rx.operators.OperatorToObservableSortedList;
import rx.operators.OperatorZipIterable;
import rx.plugins.RxJavaObservableExecutionHook;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;
import rx.subscriptions.Subscriptions;
import rx.util.OnErrorNotImplementedException;
import rx.util.Range;
import rx.util.TimeInterval;
import rx.util.Timestamped;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Action2;
import rx.util.functions.Func0;
import rx.util.functions.Func1;
import rx.util.functions.Func2;
import rx.util.functions.Func3;
import rx.util.functions.Func4;
import rx.util.functions.Func5;
import rx.util.functions.Func6;
import rx.util.functions.Func7;
import rx.util.functions.Func8;
import rx.util.functions.Func9;
import rx.util.functions.FuncN;
import rx.util.functions.Function;
import rx.util.functions.Functions;

/**
 * The Observable class that implements the Reactive Pattern.
 * <p>
 * This class provides methods for subscribing to the Observable as well as delegate methods to the
 * various Observers.
 * <p>
 * The documentation for this class makes use of marble diagrams. The following legend explains
 * these diagrams:
 * <p>
 * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/legend.png">
 * <p>
 * For more information see the
 * <a href="https://github.com/Netflix/RxJava/wiki/Observable">RxJava Wiki</a>
 * 
 * @param <T>
 *            the type of the items emitted by the Observable
 */
public class Observable<T> {

    final OnSubscribe<T> f;

    /**
     * Observable with Function to execute when subscribed to.
     * <p>
     * <em>Note:</em> Use {@link #create(OnSubscribe)} to create an Observable, instead of this
     * constructor, unless you specifically have a need for inheritance.
     * 
     * @param f
     *            {@link OnSubscribe} to be executed when {@link #subscribe(Subscriber)} is called
     */
    protected Observable(OnSubscribe<T> f) {
        this.f = f;
    }

    private final static RxJavaObservableExecutionHook hook = RxJavaPlugins.getInstance().getObservableExecutionHook();

    /**
     * Returns an Observable that will execute the specified function when a {@link Subscriber}
     * subscribes to it.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/create.png">
     * <p>
     * Write the function you pass to {@code create} so that it behaves as an Observable: It should
     * invoke the Subscriber's {@link Subscriber#onNext onNext}, {@link Subscriber#onError onError},
     * and {@link Subscriber#onCompleted onCompleted} methods appropriately.
     * <p>
     * A well-formed Observable must invoke either the Subscriber's {@code onCompleted} method
     * exactly once or its {@code onError} method exactly once.
     * <p>
     * See <a href="http://go.microsoft.com/fwlink/?LinkID=205219">Rx Design Guidelines (PDF)</a>
     * for detailed information.
     * 
     * @param <T>
     *            the type of the items that this Observable emits
     * @param f
     *            a function that accepts an {@code Subscriber<T>}, and invokes its {@code onNext},
     *            {@code onError}, and {@code onCompleted} methods as appropriate
     * @return an Observable that, when a {@link Subscriber} subscribes to it, will execute the
     *         specified function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-create">RxJava Wiki: create()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.create.aspx">MSDN: Observable.Create</a>
     */
    public final static <T> Observable<T> create(OnSubscribe<T> f) {
        return new Observable<T>(f);
    }

    /**
     * 
     */
    public static interface OnSubscribe<T> extends Action1<Subscriber<? super T>> {

    }

    /**
     * 
     */
    public final static <T> Observable<T> create(final OnSubscribeFunc<T> f) {
        return new Observable<T>(new OnSubscribe<T>() {

            @Override
            public void call(Subscriber<? super T> observer) {
                Subscription s = f.onSubscribe(observer);
                if (s != null && s != observer) {
                    observer.add(s);
                }
            }

        });
    }

    /**
     * 
     */
    public static interface OnSubscribeFunc<T> extends Function {

        public Subscription onSubscribe(Observer<? super T> op);

    }

    /**
     * Lift a function to the current Observable and return a new Observable that when subscribed to will pass the values of the current Observable through the function.
     * <p>
     * In other words, this allows chaining Observers together on an Observable for acting on the values within the Observable.
     * <p> {@code
     * observable.map(...).filter(...).take(5).lift(new ObserverA()).lift(new ObserverB(...)).subscribe()
     * }
     * 
     * @param bind
     * @return an Observable that emits values that are the result of applying the bind function to the values of the current Observable
     */
    public <R> Observable<R> lift(final Func1<Subscriber<? super R>, Subscriber<? super T>> bind) {
        return new Observable<R>(new OnSubscribe<R>() {

            @Override
            public void call(Subscriber<? super R> o) {
                subscribe(bind.call(o));
            }
        });
    }

    /* ******************************************************************************
     * Observers Below Here
     * ******************************************************************************
     */

    /**
     * Mirror the one Observable in an Iterable of several Observables that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     * 
     * @param sources
     *            an Iterable of Observable sources competing to react first
     * @return an Observable that emits the same sequence of items as whichever of the source
     *         Observables first emitted an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229115.aspx">MSDN: Observable.Amb</a>
     */
    public final static <T> Observable<T> amb(Iterable<? extends Observable<? extends T>> sources) {
        return create(OperationAmb.amb(sources));
    }

    /**
     * Given two Observables, mirror the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     * 
     * @param o1
     *            an Observable competing to react first
     * @param o2
     *            an Observable competing to react first
     * @return an Observable that emits the same sequence of items as whichever of the source
     *         Observables first emitted an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public final static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2) {
        return create(OperationAmb.amb(o1, o2));
    }

    /**
     * Given three Observables, mirror the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     * 
     * @param o1
     *            an Observable competing to react first
     * @param o2
     *            an Observable competing to react first
     * @param o3
     *            an Observable competing to react first
     * @return an Observable that emits the same sequence of items as whichever of the source
     *         Observables first emitted an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public final static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3) {
        return create(OperationAmb.amb(o1, o2, o3));
    }

    /**
     * Given four Observables, mirror the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     * 
     * @param o1
     *            an Observable competing to react first
     * @param o2
     *            an Observable competing to react first
     * @param o3
     *            an Observable competing to react first
     * @param o4
     *            an Observable competing to react first
     * @return an Observable that emits the same sequence of items as whichever of the source
     *         Observables first emitted an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public final static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4) {
        return create(OperationAmb.amb(o1, o2, o3, o4));
    }

    /**
     * Given five Observables, mirror the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     * 
     * @param o1
     *            an Observable competing to react first
     * @param o2
     *            an Observable competing to react first
     * @param o3
     *            an Observable competing to react first
     * @param o4
     *            an Observable competing to react first
     * @param o5
     *            an Observable competing to react first
     * @return an Observable that emits the same sequence of items as whichever of the source
     *         Observables first emitted an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public final static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4, Observable<? extends T> o5) {
        return create(OperationAmb.amb(o1, o2, o3, o4, o5));
    }

    /**
     * Given six Observables, mirror the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     * 
     * @param o1
     *            an Observable competing to react first
     * @param o2
     *            an Observable competing to react first
     * @param o3
     *            an Observable competing to react first
     * @param o4
     *            an Observable competing to react first
     * @param o5
     *            an Observable competing to react first
     * @param o6
     *            an Observable competing to react first
     * @return an Observable that emits the same sequence of items as whichever of the source
     *         Observables first emitted an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public final static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4, Observable<? extends T> o5, Observable<? extends T> o6) {
        return create(OperationAmb.amb(o1, o2, o3, o4, o5, o6));
    }

    /**
     * Given seven Observables, mirror the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     * 
     * @param o1
     *            an Observable competing to react first
     * @param o2
     *            an Observable competing to react first
     * @param o3
     *            an Observable competing to react first
     * @param o4
     *            an Observable competing to react first
     * @param o5
     *            an Observable competing to react first
     * @param o6
     *            an Observable competing to react first
     * @param o7
     *            an Observable competing to react first
     * @return an Observable that emits the same sequence of items as whichever of the source
     *         Observables first emitted an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public final static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4, Observable<? extends T> o5, Observable<? extends T> o6, Observable<? extends T> o7) {
        return create(OperationAmb.amb(o1, o2, o3, o4, o5, o6, o7));
    }

    /**
     * Given eight Observables, mirror the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     * 
     * @param o1
     *            an Observable competing to react first
     * @param o2
     *            an Observable competing to react first
     * @param o3
     *            an Observable competing to react first
     * @param o4
     *            an Observable competing to react first
     * @param o5
     *            an Observable competing to react first
     * @param o6
     *            an Observable competing to react first
     * @param o7
     *            an Observable competing to react first
     * @param o8
     *            an observable competing to react first
     * @return an Observable that emits the same sequence of items as whichever of the source
     *         Observables first emitted an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public final static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4, Observable<? extends T> o5, Observable<? extends T> o6, Observable<? extends T> o7, Observable<? extends T> o8) {
        return create(OperationAmb.amb(o1, o2, o3, o4, o5, o6, o7, o8));
    }

    /**
     * Given nine Observables, mirror the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     * 
     * @param o1
     *            an Observable competing to react first
     * @param o2
     *            an Observable competing to react first
     * @param o3
     *            an Observable competing to react first
     * @param o4
     *            an Observable competing to react first
     * @param o5
     *            an Observable competing to react first
     * @param o6
     *            an Observable competing to react first
     * @param o7
     *            an Observable competing to react first
     * @param o8
     *            an Observable competing to react first
     * @param o9
     *            an Observable competing to react first
     * @return an Observable that emits the same sequence of items as whichever of the source
     *         Observables first emitted an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public final static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4, Observable<? extends T> o5, Observable<? extends T> o6, Observable<? extends T> o7, Observable<? extends T> o8, Observable<? extends T> o9) {
        return create(OperationAmb.amb(o1, o2, o3, o4, o5, o6, o7, o8, o9));
    }

    /**
     * @deprecated use {@link #averageInteger}
     */
    @Deprecated
    public final static Observable<Integer> average(Observable<Integer> source) {
        return OperationAverage.average(source);
    }

    /**
     * Returns an Observable that emits the average of the Doubles emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.png">
     * 
     * @param source
     *            source Observable to compute the average of
     * @return an Observable that emits a single item: the average of all the Doubles emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageDouble()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public final static Observable<Double> averageDouble(Observable<Double> source) {
        return OperationAverage.averageDoubles(source);
    }

    /**
     * Returns an Observable that emits the average of the Floats emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.png">
     * 
     * @param source
     *            source Observable to compute the average of
     * @return an Observable that emits a single item: the average of all the Floats emitted by
     *         the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageFloat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public final static Observable<Float> averageFloat(Observable<Float> source) {
        return OperationAverage.averageFloats(source);
    }

    /**
     * Returns an Observable that emits the average of the Integers emitted by the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.png">
     * 
     * @param source
     *            source Observable to compute the average of
     * @return an Observable that emits a single item: the average of all the Integers emitted
     *         by the source Observable
     * @throws IllegalArgumentException
     *             if the source Observable emits no items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageInteger()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public final static Observable<Integer> averageInteger(Observable<Integer> source) {
        return OperationAverage.average(source);
    }

    /**
     * Returns an Observable that emits the average of the Longs emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.png">
     * 
     * @param source
     *            source Observable to compute the average of
     * @return an Observable that emits a single item: the average of all the Longs emitted by
     *         the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageLong()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public final static Observable<Long> averageLong(Observable<Long> source) {
        return OperationAverage.averageLongs(source);
    }

    /**
     * Combines two source Observables by emitting an item that aggregates the latest values of each
     * of the source Observables each time an item is received from either of the source
     * Observables, where this aggregation is defined by a specified function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            the second source Observable
     * @param combineFunction
     *            the aggregation function used to combine the items emitted by the source
     *            Observables
     * @return an Observable that emits items that are the result of combining the items emitted by
     *         the source Observables by means of the given aggregation function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public final static <T1, T2, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Func2<? super T1, ? super T2, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, combineFunction));
    }

    /**
     * Combines three source Observables by emitting an item that aggregates the latest values of
     * each of the source Observables each time an item is received from any of the source
     * Observables, where this aggregation is defined by a specified function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            the second source Observable
     * @param o3
     *            the third source Observable
     * @param combineFunction
     *            the aggregation function used to combine the items emitted by the source
     *            Observables
     * @return an Observable that emits items that are the result of combining the items emitted by
     *         the source Observables by means of the given aggregation function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public final static <T1, T2, T3, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Func3<? super T1, ? super T2, ? super T3, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, combineFunction));
    }

    /**
     * Combines four source Observables by emitting an item that aggregates the latest values of
     * each of the source Observables each time an item is received from any of the source
     * Observables, where this aggregation is defined by a specified function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            the second source Observable
     * @param o3
     *            the third source Observable
     * @param o4
     *            the fourth source Observable
     * @param combineFunction
     *            the aggregation function used to combine the items emitted by the source
     *            Observables
     * @return an Observable that emits items that are the result of combining the items emitted by
     *         the source Observables by means of the given aggregation function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public final static <T1, T2, T3, T4, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4,
            Func4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, combineFunction));
    }

    /**
     * Combines five source Observables by emitting an item that aggregates the latest values of
     * each of the source Observables each time an item is received from any of the source
     * Observables, where this aggregation is defined by a specified function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            the second source Observable
     * @param o3
     *            the third source Observable
     * @param o4
     *            the fourth source Observable
     * @param o5
     *            the fifth source Observable
     * @param combineFunction
     *            the aggregation function used to combine the items emitted by the source
     *            Observables
     * @return an Observable that emits items that are the result of combining the items emitted by
     *         the source Observables by means of the given aggregation function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public final static <T1, T2, T3, T4, T5, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5,
            Func5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, o5, combineFunction));
    }

    /**
     * Combines six source Observables by emitting an item that aggregates the latest values of each
     * of the source Observables each time an item is received from any of the source Observables,
     * where this aggregation is defined by a specified function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            the second source Observable
     * @param o3
     *            the third source Observable
     * @param o4
     *            the fourth source Observable
     * @param o5
     *            the fifth source Observable
     * @param o6
     *            the sixth source Observable
     * @param combineFunction
     *            the aggregation function used to combine the items emitted by the source
     *            Observables
     * @return an Observable that emits items that are the result of combining the items emitted by
     *         the source Observables by means of the given aggregation function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public final static <T1, T2, T3, T4, T5, T6, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6,
            Func6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, o5, o6, combineFunction));
    }

    /**
     * Combines seven source Observables by emitting an item that aggregates the latest values of
     * each of the source Observables each time an item is received from any of the source
     * Observables, where this aggregation is defined by a specified function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            the second source Observable
     * @param o3
     *            the third source Observable
     * @param o4
     *            the fourth source Observable
     * @param o5
     *            the fifth source Observable
     * @param o6
     *            the sixth source Observable
     * @param o7
     *            the seventh source Observable
     * @param combineFunction
     *            the aggregation function used to combine the items emitted by the source
     *            Observables
     * @return an Observable that emits items that are the result of combining the items emitted by
     *         the source Observables by means of the given aggregation function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public final static <T1, T2, T3, T4, T5, T6, T7, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7,
            Func7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, o5, o6, o7, combineFunction));
    }

    /**
     * Combines eight source Observables by emitting an item that aggregates the latest values of
     * each of the source Observables each time an item is received from any of the source
     * Observables, where this aggregation is defined by a specified function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            the second source Observable
     * @param o3
     *            the third source Observable
     * @param o4
     *            the fourth source Observable
     * @param o5
     *            the fifth source Observable
     * @param o6
     *            the sixth source Observable
     * @param o7
     *            the seventh source Observable
     * @param o8
     *            the eighth source Observable
     * @param combineFunction
     *            the aggregation function used to combine the items emitted by the source
     *            Observables
     * @return an Observable that emits items that are the result of combining the items emitted by
     *         the source Observables by means of the given aggregation function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public final static <T1, T2, T3, T4, T5, T6, T7, T8, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7, Observable<? extends T8> o8,
            Func8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, o5, o6, o7, o8, combineFunction));
    }

    /**
     * Combines nine source Observables by emitting an item that aggregates the latest values of
     * each of the source Observables each time an item is received from any of the source
     * Observables, where this aggregation is defined by a specified function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            the second source Observable
     * @param o3
     *            the third source Observable
     * @param o4
     *            the fourth source Observable
     * @param o5
     *            the fifth source Observable
     * @param o6
     *            the sixth source Observable
     * @param o7
     *            the seventh source Observable
     * @param o8
     *            the eighth source Observable
     * @param o9
     *            the ninth source Observable
     * @param combineFunction
     *            the aggregation function used to combine the items emitted by the source
     *            Observables
     * @return an Observable that emits items that are the result of combining the items emitted by
     *         the source Observables by means of the given aggregation function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public final static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7, Observable<? extends T8> o8,
            Observable<? extends T9> o9,
            Func9<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, o5, o6, o7, o8, o9, combineFunction));
    }

    /**
     * Returns an Observable that emits the items emitted by each of the Observables emitted by an
     * Observable, one after the other, without interleaving them.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param observables
     *            an Observable that emits Observables
     * @return an Observable that emits items all of the items emitted by the Observables emitted by {@code observables}, one after the other, without interleaving them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    public final static <T> Observable<T> concat(Observable<? extends Observable<? extends T>> observables) {
        return create(OperationConcat.concat(observables));
    }

    /**
     * Returns an Observable that emits the items emitted by two Observables, one after the other,
     * without interleaving them.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1
     *            an Observable to be concatenated
     * @param t2
     *            an Observable to be concatenated
     * @return an Observable that emits items emitted by the two source Observables, one after the
     *         other, without interleaving them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2) {
        return create(OperationConcat.concat(t1, t2));
    }

    /**
     * Returns an Observable that emits the items emitted by three Observables, one after the other,
     * without interleaving them.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1
     *            an Observable to be concatenated
     * @param t2
     *            an Observable to be concatenated
     * @param t3
     *            an Observable to be concatenated
     * @return an Observable that emits items emitted by the three source Observables, one after the
     *         other, without interleaving them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3) {
        return create(OperationConcat.concat(t1, t2, t3));
    }

    /**
     * Returns an Observable that emits the items emitted by four Observables, one after the other,
     * without interleaving them.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1
     *            an Observable to be concatenated
     * @param t2
     *            an Observable to be concatenated
     * @param t3
     *            an Observable to be concatenated
     * @param t4
     *            an Observable to be concatenated
     * @return an Observable that emits items emitted by the four source Observables, one after the
     *         other, without interleaving them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4) {
        return create(OperationConcat.concat(t1, t2, t3, t4));
    }

    /**
     * Returns an Observable that emits the items emitted by five Observables, one after the other,
     * without interleaving them.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1
     *            an Observable to be concatenated
     * @param t2
     *            an Observable to be concatenated
     * @param t3
     *            an Observable to be concatenated
     * @param t4
     *            an Observable to be concatenated
     * @param t5
     *            an Observable to be concatenated
     * @return an Observable that emits items emitted by the five source Observables, one after the
     *         other, without interleaving them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5) {
        return create(OperationConcat.concat(t1, t2, t3, t4, t5));
    }

    /**
     * Returns an Observable that emits the items emitted by six Observables, one after the other,
     * without interleaving them.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1
     *            an Observable to be concatenated
     * @param t2
     *            an Observable to be concatenated
     * @param t3
     *            an Observable to be concatenated
     * @param t4
     *            an Observable to be concatenated
     * @param t5
     *            an Observable to be concatenated
     * @param t6
     *            an Observable to be concatenated
     * @return an Observable that emits items emitted by the six source Observables, one after the
     *         other, without interleaving them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6) {
        return create(OperationConcat.concat(t1, t2, t3, t4, t5, t6));
    }

    /**
     * Returns an Observable that emits the items emitted by seven Observables, one after the other,
     * without interleaving them.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1
     *            an Observable to be concatenated
     * @param t2
     *            an Observable to be concatenated
     * @param t3
     *            an Observable to be concatenated
     * @param t4
     *            an Observable to be concatenated
     * @param t5
     *            an Observable to be concatenated
     * @param t6
     *            an Observable to be concatenated
     * @param t7
     *            an Observable to be concatenated
     * @return an Observable that emits items emitted by the seven source Observables, one after the
     *         other, without interleaving them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7) {
        return create(OperationConcat.concat(t1, t2, t3, t4, t5, t6, t7));
    }

    /**
     * Returns an Observable that emits the items emitted by eight Observables, one after the other,
     * without interleaving them.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1
     *            an Observable to be concatenated
     * @param t2
     *            an Observable to be concatenated
     * @param t3
     *            an Observable to be concatenated
     * @param t4
     *            an Observable to be concatenated
     * @param t5
     *            an Observable to be concatenated
     * @param t6
     *            an Observable to be concatenated
     * @param t7
     *            an Observable to be concatenated
     * @param t8
     *            an Observable to be concatenated
     * @return an Observable that emits items emitted by the eight source Observables, one after the
     *         other, without interleaving them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8) {
        return create(OperationConcat.concat(t1, t2, t3, t4, t5, t6, t7, t8));
    }

    /**
     * Returns an Observable that emits the items emitted by nine Observables, one after the other,
     * without interleaving them.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1
     *            an Observable to be concatenated
     * @param t2
     *            an Observable to be concatenated
     * @param t3
     *            an Observable to be concatenated
     * @param t4
     *            an Observable to be concatenated
     * @param t5
     *            an Observable to be concatenated
     * @param t6
     *            an Observable to be concatenated
     * @param t7
     *            an Observable to be concatenated
     * @param t8
     *            an Observable to be concatenated
     * @param t9
     *            an Observable to be concatenated
     * @return an Observable that emits items emitted by the nine source Observables, one after the
     *         other, without interleaving them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8, Observable<? extends T> t9) {
        return create(OperationConcat.concat(t1, t2, t3, t4, t5, t6, t7, t8, t9));
    }

    /**
     * Returns an Observable that calls an Observable factory to create its Observable for each new
     * Observer that subscribes. That is, for each subscriber, the actual Observable that subscriber
     * observes is determined by the factory function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/defer.png">
     * <p>
     * The defer Observer allows you to defer or delay emitting items from an Observable until such
     * time as an Observer subscribes to the Observable. This allows an {@link Observer} to easily
     * obtain updates or a refreshed version of the sequence.
     * 
     * @param observableFactory
     *            the Observable factory function to invoke for each {@link Observer} that
     *            subscribes to the resulting Observable
     * @param <T>
     *            the type of the items emitted by the Observable
     * @return an Observable whose {@link Observer}s' subscriptions trigger an invocation of the
     *         given Observable factory function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-defer">RxJava Wiki: defer()</a>
     */
    public final static <T> Observable<T> defer(Func0<? extends Observable<? extends T>> observableFactory) {
        return create(OperationDefer.defer(observableFactory));
    }

    /**
     * Returns an Observable that emits no items to the {@link Observer} and immediately invokes its {@link Observer#onCompleted onCompleted} method.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/empty.png">
     * 
     * @param <T>
     *            the type of the items (ostensibly) emitted by the Observable
     * @return an Observable that emits no items to the {@link Observer} but immediately invokes the {@link Observer}'s {@link Observer#onCompleted() onCompleted} method
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-empty-error-and-never">RxJava Wiki: empty()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229670.aspx">MSDN: Observable.Empty</a>
     */
    public final static <T> Observable<T> empty() {
        return from(new ArrayList<T>());
    }

    /**
     * Returns an Observable that emits no items to the {@link Observer} and immediately invokes its {@link Observer#onCompleted onCompleted} method on the specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/empty.s.png">
     * 
     * @param scheduler
     *            the scheduler to use to call the {@link Observer#onCompleted onCompleted} method
     * @param <T>
     *            the type of the items (ostensibly) emitted by the Observable
     * @return an Observable that emits no items to the {@link Observer} but immediately invokes the {@link Observer}'s {@link Observer#onCompleted() onCompleted} method with the
     *         specified {@code scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-empty-error-and-never">RxJava Wiki: empty()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229066.aspx">MSDN: Observable.Empty Method (IScheduler)</a>
     */
    public final static <T> Observable<T> empty(Scheduler scheduler) {
        return Observable.<T> empty().subscribeOn(scheduler);
    }

    /**
     * Returns an Observable that invokes an {@link Observer}'s {@link Observer#onError onError} method when the Observer subscribes to it.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/error.png">
     * 
     * @param exception
     *            the particular Throwable to pass to {@link Observer#onError onError}
     * @param <T>
     *            the type of the items (ostensibly) emitted by the Observable
     * @return an Observable that invokes the {@link Observer}'s {@link Observer#onError onError} method when the Observer subscribes to it
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-empty-error-and-never">RxJava Wiki: error()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244299.aspx">MSDN: Observable.Throw</a>
     */
    public final static <T> Observable<T> error(Throwable exception) {
        return new ThrowObservable<T>(exception);
    }

    /**
     * Returns an Observable that invokes an {@link Observer}'s {@link Observer#onError onError} method on the specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/error.s.png">
     * 
     * @param exception
     *            the particular Throwable to pass to {@link Observer#onError onError}
     * @param scheduler
     *            the scheduler on which to call {@link Observer#onError onError}
     * @param <T>
     *            the type of the items (ostensibly) emitted by the Observable
     * @return an Observable that invokes the {@link Observer}'s {@link Observer#onError onError} method, on the specified scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-empty-error-and-never">RxJava Wiki: error()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211711.aspx">MSDN: Observable.Throw</a>
     */
    public final static <T> Observable<T> error(Throwable exception, Scheduler scheduler) {
        return Observable.<T> error(exception).subscribeOn(scheduler);
    }

    /**
     * Converts a {@link Future} into an Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.Future.png">
     * <p>
     * You can convert any object that supports the {@link Future} interface into an Observable that
     * emits the return value of the {@link Future#get} method of that object, by passing the object
     * into the {@code from} method.
     * <p>
     * <em>Important note:</em> This Observable is blocking; you cannot unsubscribe from it.
     * 
     * @param future
     *            the source {@link Future}
     * @param <T>
     *            the type of object that the {@link Future} returns, and also the type of item to
     *            be emitted by the resulting Observable
     * @return an Observable that emits the item from the source {@link Future}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     */
    public final static <T> Observable<T> from(Future<? extends T> future) {
        return create(OperationToObservableFuture.toObservableFuture(future));
    }

    /**
     * Converts a {@link Future} into an Observable, with a timeout on the Future.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.Future.png">
     * <p>
     * You can convert any object that supports the {@link Future} interface into an Observable that
     * emits the return value of the {@link Future#get} method of that object, by passing the object
     * into the {@code from} method.
     * <p>
     * <em>Important note:</em> This Observable is blocking; you cannot unsubscribe from it.
     * 
     * @param future
     *            the source {@link Future}
     * @param timeout
     *            the maximum time to wait before calling {@code get()}
     * @param unit
     *            the {@link TimeUnit} of the {@code timeout} argument
     * @param <T>
     *            the type of object that the {@link Future} returns, and also the type of item to
     *            be emitted by the resulting Observable
     * @return an Observable that emits the item from the source {@link Future}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     */
    public final static <T> Observable<T> from(Future<? extends T> future, long timeout, TimeUnit unit) {
        return create(OperationToObservableFuture.toObservableFuture(future, timeout, unit));
    }

    /**
     * Converts a {@link Future}, operating on a specified {@link Scheduler}, into an Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.Future.s.png">
     * <p>
     * You can convert any object that supports the {@link Future} interface into an Observable that
     * emits the return value of the {@link Future#get} method of that object, by passing the object
     * into the {@code from} method.
     * <p>
     * 
     * @param future
     *            the source {@link Future}
     * @param scheduler
     *            the {@link Scheduler} to wait for the Future on. Use a Scheduler such as {@link Schedulers#threadPoolForIO()} that can block and wait on the future.
     * @param <T>
     *            the type of object that the {@link Future} returns, and also the type of item to
     *            be emitted by the resulting Observable
     * @return an Observable that emits the item from the source {@link Future}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     */
    public final static <T> Observable<T> from(Future<? extends T> future, Scheduler scheduler) {
        return create(OperationToObservableFuture.toObservableFuture(future)).subscribeOn(scheduler);
    }

    /**
     * Converts an {@link Iterable} sequence into an Observable that emits the items in the
     * sequence.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param iterable
     *            the source {@link Iterable} sequence
     * @param <T>
     *            the type of items in the {@link Iterable} sequence and the type of items to be
     *            emitted by the resulting Observable
     * @return an Observable that emits each item in the source {@link Iterable} sequence
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     */
    public final static <T> Observable<T> from(Iterable<? extends T> iterable) {
        return create(new OperatorFromIterable<T>(iterable));
    }

    /**
     * Converts an {@link Iterable} sequence into an Observable that operates on the specified
     * scheduler, emitting each item from the sequence.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.s.png">
     * 
     * @param iterable
     *            the source {@link Iterable} sequence
     * @param scheduler
     *            the scheduler on which the Observable is to emit the items of the iterable
     * @param <T>
     *            the type of items in the {@link Iterable} sequence and the type of items to be
     *            emitted by the resulting Observable
     * @return an Observable that emits each item in the source {@link Iterable} sequence, on the
     *         specified scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212140.aspx">MSDN: Observable.ToObservable</a>
     */
    public final static <T> Observable<T> from(Iterable<? extends T> iterable, Scheduler scheduler) {
        return create(new OperatorFromIterable<T>(iterable)).subscribeOn(scheduler);
    }

    /**
     * Converts an item into an Observable that emits that item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param t1
     *            the item
     * @param <T>
     *            the type of the item
     * @return an Observable that emits the item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     */
    // suppress unchecked because we are using varargs inside the method
    public final static <T> Observable<T> from(T t1) {
        return from(Arrays.asList(t1));
    }

    /**
     * Converts two items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param t1
     *            first item
     * @param t2
     *            second item
     * @param <T>
     *            the type of these items
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}
     */
    @Deprecated
    // suppress unchecked because we are using varargs inside the method
    public final static <T> Observable<T> from(T t1, T t2) {
        return from(Arrays.asList(t1, t2));
    }

    /**
     * Converts three items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param t1
     *            first item
     * @param t2
     *            second item
     * @param t3
     *            third item
     * @param <T>
     *            the type of these items
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    // suppress unchecked because we are using varargs inside the method
    public final static <T> Observable<T> from(T t1, T t2, T t3) {
        return from(Arrays.asList(t1, t2, t3));
    }

    /**
     * Converts four items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param t1
     *            first item
     * @param t2
     *            second item
     * @param t3
     *            third item
     * @param t4
     *            fourth item
     * @param <T>
     *            the type of these items
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    // suppress unchecked because we are using varargs inside the method
    public final static <T> Observable<T> from(T t1, T t2, T t3, T t4) {
        return from(Arrays.asList(t1, t2, t3, t4));
    }

    /**
     * Converts five items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param t1
     *            first item
     * @param t2
     *            second item
     * @param t3
     *            third item
     * @param t4
     *            fourth item
     * @param t5
     *            fifth item
     * @param <T>
     *            the type of these items
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    // suppress unchecked because we are using varargs inside the method
    public final static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5) {
        return from(Arrays.asList(t1, t2, t3, t4, t5));
    }

    /**
     * Converts six items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param t1
     *            first item
     * @param t2
     *            second item
     * @param t3
     *            third item
     * @param t4
     *            fourth item
     * @param t5
     *            fifth item
     * @param t6
     *            sixth item
     * @param <T>
     *            the type of these items
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    // suppress unchecked because we are using varargs inside the method
    public final static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5, T t6) {
        return from(Arrays.asList(t1, t2, t3, t4, t5, t6));
    }

    /**
     * Converts seven items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param t1
     *            first item
     * @param t2
     *            second item
     * @param t3
     *            third item
     * @param t4
     *            fourth item
     * @param t5
     *            fifth item
     * @param t6
     *            sixth item
     * @param t7
     *            seventh item
     * @param <T>
     *            the type of these items
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    // suppress unchecked because we are using varargs inside the method
    public final static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5, T t6, T t7) {
        return from(Arrays.asList(t1, t2, t3, t4, t5, t6, t7));
    }

    /**
     * Converts eight items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param t1
     *            first item
     * @param t2
     *            second item
     * @param t3
     *            third item
     * @param t4
     *            fourth item
     * @param t5
     *            fifth item
     * @param t6
     *            sixth item
     * @param t7
     *            seventh item
     * @param t8
     *            eighth item
     * @param <T>
     *            the type of these items
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    // suppress unchecked because we are using varargs inside the method
    public final static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8) {
        return from(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8));
    }

    /**
     * Converts nine items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param t1
     *            first item
     * @param t2
     *            second item
     * @param t3
     *            third item
     * @param t4
     *            fourth item
     * @param t5
     *            fifth item
     * @param t6
     *            sixth item
     * @param t7
     *            seventh item
     * @param t8
     *            eighth item
     * @param t9
     *            ninth item
     * @param <T>
     *            the type of these items
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    // suppress unchecked because we are using varargs inside the method
    public final static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9) {
        return from(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9));
    }

    /**
     * Converts ten items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * 
     * @param t1
     *            first item
     * @param t2
     *            second item
     * @param t3
     *            third item
     * @param t4
     *            fourth item
     * @param t5
     *            fifth item
     * @param t6
     *            sixth item
     * @param t7
     *            seventh item
     * @param t8
     *            eighth item
     * @param t9
     *            ninth item
     * @param t10
     *            tenth item
     * @param <T>
     *            the type of these items
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    // suppress unchecked because we are using varargs inside the method
    public final static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9, T t10) {
        return from(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10));
    }

    /**
     * Converts an Array into an Observable that emits the items in the Array.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param items
     *            the source array
     * @param <T>
     *            the type of items in the Array and the type of items to be emitted by the
     *            resulting Observable
     * @return an Observable that emits each item in the source Array
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     */
    //    @SafeVarargs // commenting out until we figure out if we can do Java7 compilation without breaking Android for just this feature
    public final static <T> Observable<T> from(T... t1) {
        return from(Arrays.asList(t1));
    }

    /**
     * Converts an Array into an Observable that emits the items in the Array on a specified
     * scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * 
     * @param items
     *            the source array
     * @param scheduler
     *            the scheduler on which the Observable emits the items of the array
     * @param <T>
     *            the type of items in the Array and the type of items to be emitted by the
     *            resulting Observable
     * @return an Observable that emits each item in the source Array
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-from">RxJava Wiki: from()</a>
     */
    public final static <T> Observable<T> from(T[] items, Scheduler scheduler) {
        return from(Arrays.asList(items), scheduler);
    }

    /**
     * Returns an Observable that emits a sequential number every specified interval of time.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/interval.png">
     * 
     * @param interval
     *            interval size in time units (see below)
     * @param unit
     *            time units to use for the interval size
     * @return an Observable that emits a sequential number each time interval
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-interval">RxJava Wiki: interval()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229027.aspx">MSDN: Observable.Interval</a>
     */
    public final static Observable<Long> interval(long interval, TimeUnit unit) {
        return create(OperationInterval.interval(interval, unit));
    }

    /**
     * Returns an Observable that emits a sequential number every specified interval of time, on a
     * specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/interval.s.png">
     * 
     * @param interval
     *            interval size in time units (see below)
     * @param unit
     *            time units to use for the interval size
     * @param scheduler
     *            the scheduler to use for scheduling the items
     * @return an Observable that emits a sequential number each time interval
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-interval">RxJava Wiki: interval()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228911.aspx">MSDN: Observable.Interval</a>
     */
    public final static Observable<Long> interval(long interval, TimeUnit unit, Scheduler scheduler) {
        return create(OperationInterval.interval(interval, unit, scheduler));
    }

    /**
     * Returns an Observable that emits a single item and then completes.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/just.png">
     * <p>
     * To convert any object into an Observable that emits that object, pass that object into the {@code just} method.
     * <p>
     * This is similar to the {@link #from(java.lang.Object[])} method, except that {@code from()} will convert an {@link Iterable} object into an Observable that emits each of the items in
     * the Iterable, one at a time, while the {@code just()} method converts an Iterable into an
     * Observable that emits the entire Iterable as a single item.
     * 
     * @param value
     *            the item to emit
     * @param <T>
     *            the type of that item
     * @return an Observable that emits {@code value} as a single item and then completes
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-just">RxJava Wiki: just()</a>
     */
    public final static <T> Observable<T> just(T value) {
        return from(Arrays.asList(value));
    }

    /**
     * Returns an Observable that emits a single item and then completes, on a specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/just.s.png">
     * <p>
     * This is a scheduler version of {@link #just(Object)}.
     * 
     * @param value
     *            the item to emit
     * @param <T>
     *            the type of that item
     * @param scheduler
     *            the scheduler to emit the single item on
     * @return an Observable that emits {@code value} as a single item and then completes, on a
     *         specified scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-just">RxJava Wiki: just()</a>
     * @deprecated Use {@link #from(T)}
     */
    @Deprecated
    public final static <T> Observable<T> just(T value, Scheduler scheduler) {
        return from(Arrays.asList((value)), scheduler);
    }

    /**
     * Returns an Observable that emits the single item emitted by the source Observable with the
     * maximum numeric value. If there is more than one item with the same maximum value, it emits
     * the last-emitted of these.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/max.png">
     * 
     * @param source
     *            an Observable to scan for the maximum emitted item
     * @return an Observable that emits this maximum item
     * @throws IllegalArgumentException
     *             if the source is empty
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-max">RxJava Wiki: max()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211837.aspx">MSDN: Observable.Max</a>
     */
    public final static <T extends Comparable<? super T>> Observable<T> max(Observable<T> source) {
        return OperationMinMax.max(source);
    }

    /**
     * Flattens an Iterable of Observables into one Observable, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine the items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param sequences
     *            the Iterable of Observables
     * @return an Observable that emits items that are the result of flattening the items emitted by
     *         the Observables in the Iterable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229590.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Iterable<? extends Observable<? extends T>> sequences) {
        return merge(from(sequences));
    }

    /**
     * Flattens an Iterable of Observables into one Observable, without any transformation, while
     * limiting the number of concurrent subscriptions to these Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine the items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param sequences
     *            the Iterable of Observables
     * @param maxConcurrent
     *            the maximum number of Observables that may be subscribed to concurrently
     * @return an Observable that emits items that are the result of flattening the items emitted by
     *         the Observables in the Iterable
     * @throw IllegalArgumentException if {@code maxConcurrent} is less than or equal to 0
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229923.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Iterable<? extends Observable<? extends T>> sequences, int maxConcurrent) {
        return merge(from(sequences), maxConcurrent);
    }

    /**
     * Flattens an Iterable of Observables into one Observable, without any transformation, while
     * limiting the number of concurrent subscriptions to these Observables, and subscribing to
     * these Observables on a specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine the items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param sequences
     *            the Iterable of Observables
     * @param maxConcurrent
     *            the maximum number of Observables that may be subscribed to concurrently
     * @param scheduler
     *            the scheduler on which to traverse the Iterable of Observables
     * @return an Observable that emits items that are the result of flattening the items emitted by
     *         the Observables in the Iterable
     * @throw IllegalArgumentException if {@code maxConcurrent} is less than or equal to 0
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244329.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Iterable<? extends Observable<? extends T>> sequences, int maxConcurrent, Scheduler scheduler) {
        return merge(from(sequences, scheduler), maxConcurrent);
    }

    /**
     * Flattens an Iterable of Observables into one Observable, without any transformation,
     * subscribing to these Observables on a specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine the items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param sequences
     *            the Iterable of Observables
     * @param scheduler
     *            the scheduler on which to traverse the Iterable of Observables
     * @return an Observable that emits items that are the result of flattening the items emitted by
     *         the Observables in the Iterable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244336.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Iterable<? extends Observable<? extends T>> sequences, Scheduler scheduler) {
        return merge(from(sequences, scheduler));
    }

    /**
     * Flattens an Observable that emits Observables into a single Observable that emits the items
     * emitted by those Observables, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.oo.png">
     * <p>
     * You can combine the items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param source
     *            an Observable that emits Observables
     * @return an Observable that emits items that are the result of flattening the Observables
     *         emitted by the {@code source} Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Observable<? extends Observable<? extends T>> source) {
        return source.lift(new OperatorMerge()); // any idea how to get these generics working?!
    }

    /**
     * Flattens an Observable that emits Observables into a single Observable that emits the items
     * emitted by those Observables, without any transformation, while limiting the maximum number
     * of concurrent subscriptions to these Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.oo.png">
     * <p>
     * You can combine the items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param source
     *            an Observable that emits Observables
     * @param maxConcurrent
     *            the maximum number of Observables that may be subscribed to concurrently
     * @return an Observable that emits items that are the result of flattening the Observables
     *         emitted by the {@code source} Observable
     * @throw IllegalArgumentException if {@code maxConcurrent} is less than or equal to 0
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211914.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Observable<? extends Observable<? extends T>> source, int maxConcurrent) {
        return source.lift(new OperatorMerge(maxConcurrent)); // any idea how to get these generics working?!
    }

    /**
     * Flattens two Observables into a single Observable, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @return an Observable that emits all of the items emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2) {
        return merge(from(t1, t2));
    }

    /**
     * Flattens three Observables into a single Observable, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @return an Observable that emits all of the items emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3) {
        return merge(from(t1, t2, t3));
    }

    /**
     * Flattens four Observables into a single Observable, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @return an Observable that emits all of the items emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4) {
        return merge(from(t1, t2, t3, t4));
    }

    /**
     * Flattens five Observables into a single Observable, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @param t5
     *            an Observable to be merged
     * @return an Observable that emits all of the items emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5) {
        return merge(from(t1, t2, t3, t4, t5));
    }

    /**
     * Flattens six Observables into a single Observable, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @param t5
     *            an Observable to be merged
     * @param t6
     *            an Observable to be merged
     * @return an Observable that emits all of the items emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6) {
        return merge(from(t1, t2, t3, t4, t5, t6));
    }

    /**
     * Flattens seven Observables into a single Observable, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @param t5
     *            an Observable to be merged
     * @param t6
     *            an Observable to be merged
     * @param t7
     *            an Observable to be merged
     * @return an Observable that emits all of the items emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7) {
        return merge(from(t1, t2, t3, t4, t5, t6, t7));
    }

    /**
     * Flattens eight Observables into a single Observable, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @param t5
     *            an Observable to be merged
     * @param t6
     *            an Observable to be merged
     * @param t7
     *            an Observable to be merged
     * @param t8
     *            an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @return an Observable that emits all of the items emitted by the source Observables
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8) {
        return merge(from(t1, t2, t3, t4, t5, t6, t7, t8));
    }

    /**
     * Flattens nine Observables into a single Observable, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @param t5
     *            an Observable to be merged
     * @param t6
     *            an Observable to be merged
     * @param t7
     *            an Observable to be merged
     * @param t8
     *            an Observable to be merged
     * @param t9
     *            an Observable to be merged
     * @return an Observable that emits all of the items emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8, Observable<? extends T> t9) {
        return merge(from(t1, t2, t3, t4, t5, t6, t7, t8, t9));
    }

    /**
     * Flattens an array of Observables into one Observable, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.io.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param sequences
     *            the array of Observables
     * @return an Observable that emits all of the items emitted by the Observables in the array
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Observable<? extends T>[] sequences) {
        return merge(from(sequences));
    }

    /**
     * Flattens an array of Observables into one Observable, without any transformation, traversing
     * the array on a specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.ios.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they appear as a single
     * Observable, by using the {@code merge} method.
     * 
     * @param sequences
     *            the array of Observables
     * @param scheduler
     *            the scheduler on which to traverse the array
     * @return an Observable that emits all of the items emitted by the Observables in the array
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229061.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> merge(Observable<? extends T>[] sequences, Scheduler scheduler) {
        return merge(from(sequences, scheduler));
    }

    /**
     * This behaves like {@link #merge(Observable)} except that if any of the merged Observables
     * notify of an error via {@link Observer#onError onError}, {@code mergeDelayError} will refrain
     * from propagating that error notification until all of the merged Observables have finished
     * emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications, {@code mergeDelayError} will only invoke the {@code onError} method of its Observers once.
     * <p>
     * This method allows an Observer to observe all successfully emitted items from all of the
     * source Observables without being interrupted by an error notification from one of them.
     * 
     * @param source
     *            an Observable that emits Observables
     * @return an Observable that emits all of the items emitted by the Observables emitted by the {@code source} Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public final static <T> Observable<T> mergeDelayError(Observable<? extends Observable<? extends T>> source) {
        return create(OperationMergeDelayError.mergeDelayError(source));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable)} except that if any of the merged
     * Observables notify of an error via {@link Observer#onError onError}, {@code mergeDelayError} will refrain from propagating that error notification until all of the merged Observables
     * have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if both merged Observables send {@code onError} notifications, {@code mergeDelayError} will only invoke the {@code onError} method of its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items from each of the
     * source Observables without being interrupted by an error notification from one of them.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @return an Observable that emits all of the items that are emitted by the two source
     *         Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable)} except that if any of
     * the merged Observables notify of an error via {@link Observer#onError onError}, {@code mergeDelayError} will refrain from propagating that error notification until all of
     * the merged Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications, {@code mergeDelayError} will only invoke the {@code onError} method of its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items from all of the
     * source Observables without being interrupted by an error notification from one of them.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @return an Observable that emits all of the items that are emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable, Observable)} except that
     * if any of the merged Observables notify of an error via {@link Observer#onError onError}, {@code mergeDelayError} will refrain from propagating that error notification until all of
     * the merged Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications, {@code mergeDelayError} will only invoke the {@code onError} method of its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items from all of the
     * source Observables without being interrupted by an error notification from one of them.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @return an Observable that emits all of the items that are emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable, Observable, Observable)} except that if any of the merged Observables notify of an error via {@link Observer#onError onError}
     * , {@code mergeDelayError} will refrain from propagating that
     * error notification until all of the merged Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications, {@code mergeDelayError} will only invoke the {@code onError} method of its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items from all of the
     * source Observables without being interrupted by an error notification from one of them.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @param t5
     *            an Observable to be merged
     * @return an Observable that emits all of the items that are emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4, t5));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable, Observable, Observable, Observable)} except that if any of the merged Observables notify of an error via
     * {@link Observer#onError onError}, {@code mergeDelayError} will refrain from propagating that
     * error notification until all of the merged Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications, {@code mergeDelayError} will only invoke the {@code onError} method of its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items from all of the
     * source Observables without being interrupted by an error notification from one of them.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @param t5
     *            an Observable to be merged
     * @param t6
     *            an Observable to be merged
     * @return an Observable that emits all of the items that are emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4, t5, t6));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable, Observable, Observable, Observable, Observable)} except that if any of the merged Observables notify of an error via
     * {@link Observer#onError onError}, {@code mergeDelayError} will refrain from propagating that
     * error notification until all of the merged Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications, {@code mergeDelayError} will only invoke the {@code onError} method of its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items from all of the
     * source Observables without being interrupted by an error notification from one of them.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @param t5
     *            an Observable to be merged
     * @param t6
     *            an Observable to be merged
     * @param t7
     *            an Observable to be merged
     * @return an Observable that emits all of the items that are emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4, t5, t6, t7));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable, Observable, Observable, Observable, Observable, Observable)} except that if any of the merged Observables notify of an error
     * via {@link Observer#onError onError}, {@code mergeDelayError} will refrain from propagating that
     * error notification until all of the merged Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications, {@code mergeDelayError} will only invoke the {@code onError} method of its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items from all of the
     * source Observables without being interrupted by an error notification from one of them.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @param t5
     *            an Observable to be merged
     * @param t6
     *            an Observable to be merged
     * @param t7
     *            an Observable to be merged
     * @param t8
     *            an Observable to be merged
     * @return an Observable that emits all of the items that are emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4, t5, t6, t7, t8));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable, Observable, Observable, Observable, Observable, Observable, Observable)} except that if any of the merged Observables notify
     * of an error via {@link Observer#onError onError}, {@code mergeDelayError} will refrain from propagating that
     * error notification until all of the merged Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications, {@code mergeDelayError} will only invoke the {@code onError} method of its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items from all of the
     * source Observables without being interrupted by an error notification from one of them.
     * 
     * @param t1
     *            an Observable to be merged
     * @param t2
     *            an Observable to be merged
     * @param t3
     *            an Observable to be merged
     * @param t4
     *            an Observable to be merged
     * @param t5
     *            an Observable to be merged
     * @param t6
     *            an Observable to be merged
     * @param t7
     *            an Observable to be merged
     * @param t8
     *            an Observable to be merged
     * @param t9
     *            an Observable to be merged
     * @return an Observable that emits all of the items that are emitted by the source Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public final static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8, Observable<? extends T> t9) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4, t5, t6, t7, t8, t9));
    }

    /**
     * Returns an Observable that emits the single numerically minimum item emitted by the source
     * Observable. If there is more than one such item, it returns the last-emitted one.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/min.png">
     * 
     * @param source
     *            an Observable to determine the minimum item of
     * @return an Observable that emits the minimum item emitted by the source Observable
     * @throws IllegalArgumentException
     *             if the source is empty
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229715.aspx">MSDN: Observable.Min</a>
     */
    public final static <T extends Comparable<? super T>> Observable<T> min(Observable<T> source) {
        return OperationMinMax.min(source);
    }

    /**
     * Returns an Observable that never sends any items or notifications to an {@link Observer}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/never.png">
     * <p>
     * This Observable is useful primarily for testing purposes.
     * 
     * @param <T>
     *            the type of items (not) emitted by the Observable
     * @return an Observable that never emits any items or sends any notifications to an {@link Observer}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-empty-error-and-never">RxJava Wiki: never()</a>
     */
    public final static <T> Observable<T> never() {
        return new NeverObservable<T>();
    }

    /**
     * Converts an {@code Observable<Observable<T>>} into another {@code Observable<Observable<T>>} whose emitted Observables emit the same items, but the number of such Observables is
     * restricted by {@code parallelObservables}.
     * <p>
     * For example, if the original {@code Observable<Observable<T>>} emits 100 Observables and {@code parallelObservables} is 8, the items emitted by the 100 original Observables will be
     * distributed among 8 Observables emitted by the resulting Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/parallelMerge.png">
     * <p>
     * This is a mechanism for efficiently processing <i>n</i> number of Observables on a smaller
     * <i>m</i> number of resources (typically CPU cores).
     * 
     * @param parallelObservables
     *            the number of Observables to merge into
     * @return an Observable of Observables constrained in number by {@code parallelObservables}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-parallelmerge">RxJava Wiki: parallelMerge()</a>
     */
    public final static <T> Observable<Observable<T>> parallelMerge(Observable<Observable<T>> source, int parallelObservables) {
        return OperationParallelMerge.parallelMerge(source, parallelObservables);
    }

    /**
     * Converts an {@code Observable<Observable<T>>} into another {@code Observable<Observable<T>>} whose emitted Observables emit the same items, but the number of such Observables is
     * restricted by {@code parallelObservables}, and each runs on a defined Scheduler.
     * <p>
     * For example, if the original {@code Observable<Observable<T>>} emits 100 Observables and {@code parallelObservables} is 8, the items emitted by the 100 original Observables will be
     * distributed among 8 Observables emitted by the resulting Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/parallelMerge.png">
     * <p>
     * This is a mechanism for efficiently processing <i>n</i> number of Observables on a smaller
     * <i>m</i> number of resources (typically CPU cores).
     * 
     * @param parallelObservables
     *            the number of Observables to merge into
     * @param scheduler
     *            the {@link Scheduler} to run each Observable on
     * @return an Observable of Observables constrained in number by {@code parallelObservables}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-parallelmerge">RxJava Wiki: parallelMerge()</a>
     */
    public final static <T> Observable<Observable<T>> parallelMerge(Observable<Observable<T>> source, int parallelObservables, Scheduler scheduler) {
        return OperationParallelMerge.parallelMerge(source, parallelObservables, scheduler);
    }

    /**
     * Returns an Observable that emits a sequence of Integers within a specified range.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/range.png">
     * 
     * @param start
     *            the value of the first Integer in the sequence
     * @param count
     *            the number of sequential Integers to generate
     * @return an Observable that emits a range of sequential Integers
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-range">RxJava Wiki: range()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229460.aspx">MSDN: Observable.Range</a>
     */
    public final static Observable<Integer> range(int start, int count) {
        return from(Range.createWithCount(start, count));
    }

    /**
     * Returns an Observable that emits a sequence of Integers within a specified range, on a
     * specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/range.s.png">
     * 
     * @param start
     *            the value of the first Integer in the sequence
     * @param count
     *            the number of sequential Integers to generate
     * @param scheduler
     *            the scheduler to run the generator loop on
     * @return an Observable that emits a range of sequential Integers
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-range">RxJava Wiki: range()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211896.aspx">MSDN: Observable.Range</a>
     */
    public final static Observable<Integer> range(int start, int count, Scheduler scheduler) {
        return from(Range.createWithCount(start, count), scheduler);
    }

    /**
     * Returns an Observable that emits a Boolean value that indicates whether two Observable
     * sequences are the same by comparing the items emitted by each Observable pairwise.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sequenceEqual.png">
     * 
     * @param first
     *            the first Observable to compare
     * @param second
     *            the second Observable to compare
     * @param <T>
     *            the type of items emitted by each Observable
     * @return an Observable that emits a Boolean value that indicates whether the two sequences are
     *         the same
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-sequenceequal">RxJava Wiki: sequenceEqual()</a>
     */
    public final static <T> Observable<Boolean> sequenceEqual(Observable<? extends T> first, Observable<? extends T> second) {
        return sequenceEqual(first, second, new Func2<T, T, Boolean>() {
            @Override
            public final Boolean call(T first, T second) {
                if (first == null) {
                    return second == null;
                }
                return first.equals(second);
            }
        });
    }

    /**
     * Returns an Observable that emits a Boolean value that indicates whether two Observable
     * sequences are the same by comparing the items emitted by each Observable pairwise based on
     * the results of a specified equality function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sequenceEqual.png">
     * 
     * @param first
     *            the first Observable to compare
     * @param second
     *            the second Observable to compare
     * @param equality
     *            a function used to compare items emitted by each Observable
     * @param <T>
     *            the type of items emitted by each Observable
     * @return an Observable that emits a Boolean value that indicates whether the two Observable
     *         two sequences are the same according to the specified function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-sequenceequal">RxJava Wiki: sequenceEqual()</a>
     */
    public final static <T> Observable<Boolean> sequenceEqual(Observable<? extends T> first, Observable<? extends T> second, Func2<? super T, ? super T, Boolean> equality) {
        return OperationSequenceEqual.sequenceEqual(first, second, equality);
    }

    /**
     * @deprecated use {@link #sumInteger}
     */
    @Deprecated
    public final static Observable<Integer> sum(Observable<Integer> source) {
        return OperationSum.sum(source);
    }

    /**
     * Returns an Observable that emits the sum of all the Doubles emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.png">
     * 
     * @param source
     *            the source Observable to compute the sum of
     * @return an Observable that emits a single item: the sum of all the Doubles emitted by the
     *         source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumDouble()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public final static Observable<Double> sumDouble(Observable<Double> source) {
        return OperationSum.sumDoubles(source);
    }

    /**
     * Returns an Observable that emits the sum of all the Floats emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.png">
     * 
     * @param source
     *            the source Observable to compute the sum of
     * @return an Observable that emits a single item: the sum of all the Floats emitted by the
     *         source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumFloat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public final static Observable<Float> sumFloat(Observable<Float> source) {
        return OperationSum.sumFloats(source);
    }

    /**
     * Returns an Observable that emits the sum of all the Integers emitted by the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.png">
     * 
     * @param source
     *            source Observable to compute the sum of
     * @return an Observable that emits a single item: the sum of all the Integers emitted by the
     *         source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumInteger()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public final static Observable<Integer> sumInteger(Observable<Integer> source) {
        return OperationSum.sum(source);
    }

    /**
     * Returns an Observable that emits the sum of all the Longs emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.png">
     * 
     * @param source
     *            source Observable to compute the sum of
     * @return an Observable that emits a single item: the sum of all the Longs emitted by the
     *         source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumLong()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public final static Observable<Long> sumLong(Observable<Long> source) {
        return OperationSum.sumLongs(source);
    }

    /**
     * Given an Observable that emits Observables, returns an Observable that emits the items
     * emitted by the most recently emitted of those Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/switchDo.png">
     * <p>
     * {@code switchDo()} subscribes to an Observable that emits Observables. Each time it
     * observes one of these emitted Observables, the Observable returned by {@code switchDo()}
     * begins emitting the items emitted by that Observable. When a new Observable is emitted,
     * {@code switchDo()} stops emitting items from the earlier-emitted Observable and begins
     * emitting items from the new one.
     * 
     * @param sequenceOfSequences
     *            the source Observable that emits Observables
     * @return an Observable that emits the items emitted by the Observable most recently emitted by
     *         the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-switchonnext">RxJava Wiki: switchOnNext()</a>
     * @deprecated use {@link #switchOnNext}
     */
    @Deprecated
    public final static <T> Observable<T> switchDo(Observable<? extends Observable<? extends T>> sequenceOfSequences) {
        return create(OperationSwitch.switchDo(sequenceOfSequences));
    }

    /**
     * Given an Observable that emits Observables, returns an Observable that emits the items
     * emitted by the most recently emitted of those Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/switchLatest.png">
     * <p>
     * {@code switchLatest()} subscribes to an Observable that emits Observables. Each time it
     * observes one of these emitted Observables, the Observable returned by {@code switchLatest()}
     * begins emitting the items emitted by that Observable. When a new Observable is emitted,
     * {@code switchLatest()} stops emitting items from the earlier-emitted Observable and begins
     * emitting items from the new one.
     * 
     * @param sequenceOfSequences
     *            the source Observable that emits Observables
     * @return an Observable that emits the items emitted by the Observable most recently emitted by
     *         the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-switchonnext">RxJava Wiki: switchOnNext()</a>
     * @see {@link #switchOnNext(Observable)}
     */
    public final static <T> Observable<T> switchLatest(Observable<? extends Observable<? extends T>> sequenceOfSequences) {
        return create(OperationSwitch.switchDo(sequenceOfSequences));
    }

    /**
     * Given an Observable that emits Observables, returns an Observable that emits the items
     * emitted by the most recently emitted of those Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/switchDo.png">
     * <p>
     * {@code switchOnNext()} subscribes to an Observable that emits Observables. Each time it
     * observes one of these emitted Observables, the Observable returned by {@code switchOnNext()}
     * begins emitting the items emitted by that Observable. When a new Observable is emitted,
     * {@code switchOnNext()} stops emitting items from the earlier-emitted Observable and begins
     * emitting items from the new one.
     * 
     * @param sequenceOfSequences
     *            the source Observable that emits Observables
     * @return an Observable that emits the items emitted by the Observable most recently emitted by
     *         the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-switchonnext">RxJava Wiki: switchOnNext()</a>
     */
    public final static <T> Observable<T> switchOnNext(Observable<? extends Observable<? extends T>> sequenceOfSequences) {
        return create(OperationSwitch.switchDo(sequenceOfSequences));
    }

    /**
     * @deprecated use {@link #synchronize()} or {@link #synchronize(Object)}
     */
    @Deprecated
    public final static <T> Observable<T> synchronize(Observable<T> source) {
        return create(OperationSynchronize.synchronize(source));
    }

    /**
     * Return an Observable that emits a 0L after the {@code initialDelay} and ever increasing
     * numbers after each {@code period} of time thereafter.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timer.p.png">
     * 
     * @param initialDelay
     *            the initial delay time to wait before emitting the first value of 0L
     * @param period
     *            the period of time between emissions of the subsequent numbers
     * @param unit
     *            the time unit for both {@code initialDelay} and {@code period}
     * @return an Observable that emits a 0L after the {@code initialDelay} and ever increasing
     *         numbers after each {@code period} of time thereafter
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-timer">RxJava Wiki: timer()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229435.aspx">MSDN: Observable.Timer</a>
     */
    public final static Observable<Long> timer(long initialDelay, long period, TimeUnit unit) {
        return timer(initialDelay, period, unit, Schedulers.computation());
    }

    /**
     * Return an Observable that emits a 0L after the {@code initialDelay} and ever increasing
     * numbers after each {@code period} of time thereafter, on a specified Scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timer.ps.png">
     * 
     * @param initialDelay
     *            the initial delay time to wait before emitting the first value of 0L
     * @param period
     *            the period of time between emissions of the subsequent numbers
     * @param unit
     *            the time unit for both {@code initialDelay} and {@code period}
     * @param scheduler
     *            the scheduler on which the waiting happens and items are emitted
     * @return an Observable that emits a 0L after the {@code initialDelay} and ever increasing
     *         numbers after each {@code period} of time thereafter, while running on the given {@code scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-timer">RxJava Wiki: timer()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229652.aspx">MSDN: Observable.Timer</a>
     */
    public final static Observable<Long> timer(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
        return create(new OperationTimer.TimerPeriodically(initialDelay, period, unit, scheduler));
    }

    /**
     * Returns an Observable that emits one item after a specified delay, and then completes.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timer.png">
     * 
     * @param delay
     *            the initial delay before emitting a single 0L
     * @param unit
     *            time units to use for the delay
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-timer">RxJava wiki: timer()</a>
     */
    public final static Observable<Long> timer(long delay, TimeUnit unit) {
        return timer(delay, unit, Schedulers.computation());
    }

    /**
     * Returns an Observable that emits one item after a specified delay, on a specified scheduler,
     * and then completes.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timer.s.png">
     * 
     * @param delay
     *            the initial delay before emitting a single 0L
     * @param unit
     *            time units to use for the delay
     * @param scheduler
     *            the scheduler to use for scheduling the item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-timer">RxJava wiki: timer()</a>
     */
    public final static Observable<Long> timer(long delay, TimeUnit unit, Scheduler scheduler) {
        return create(new OperationTimer.TimerOnce(delay, unit, scheduler));
    }

    /**
     * Constructs an Observable that creates a dependent resource object.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/using.png">
     * 
     * @param resourceFactory
     *            the factory function to create a resource object that depends on the Observable
     * @param observableFactory
     *            the factory function to obtain an Observable
     * @return the Observable whose lifetime controls the lifetime of the dependent resource object
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-using">RxJava Wiki: using()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229585.aspx">MSDN: Observable.Using</a>
     */
    public final static <T, RESOURCE extends Subscription> Observable<T> using(Func0<RESOURCE> resourceFactory, Func1<RESOURCE, Observable<T>> observableFactory) {
        return create(OperationUsing.using(resourceFactory, observableFactory));
    }

    /**
     * Joins together the results from several patterns via their plans.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param plans
     *            a series of plans created by use of the {@link #then} Observer on patterns
     * @return an Observable that emits the results from matching several patterns
     * @throws NullPointerException
     *             if {@code plans} is null
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229558.aspx">MSDN: Observable.When</a>
     */
    public final static <R> Observable<R> when(Iterable<? extends Plan0<R>> plans) {
        if (plans == null) {
            throw new NullPointerException("plans");
        }
        return create(OperationJoinPatterns.when(plans));
    }

    /**
     * Joins together the results from several patterns via their plans.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param plans
     *            a series of plans created by use of the {@link #then} Observer on patterns
     * @return an Observable that emits the results from matching several patterns
     * @throws NullPointerException
     *             if {@code plans} is null
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    public final static <R> Observable<R> when(Plan0<R>... plans) {
        return create(OperationJoinPatterns.when(plans));
    }

    /**
     * Joins the results from a pattern via its plan.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param p1
     *            the plan to join, created by use of the {@link #then} Observer on a pattern
     * @return an Observable that emits the results from matching a pattern
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public final static <R> Observable<R> when(Plan0<R> p1) {
        return create(OperationJoinPatterns.when(p1));
    }

    /**
     * Joins together the results from two patterns via their plans.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param p1
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p2
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @return an Observable that emits the results from matching two patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public final static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2) {
        return create(OperationJoinPatterns.when(p1, p2));
    }

    /**
     * Joins together the results from three patterns via their plans.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param p1
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p2
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p3
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @return an Observable that emits the results from matching three patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public final static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3) {
        return create(OperationJoinPatterns.when(p1, p2, p3));
    }

    /**
     * Joins together the results from four patterns via their plans.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param p1
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p2
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p3
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p4
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @return an Observable that emits the results from matching four patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public final static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4));
    }

    /**
     * Joins together the results from five patterns via their plans.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param p1
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p2
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p3
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p4
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p5
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @return an Observable that emits the results from matching five patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public final static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4, Plan0<R> p5) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4, p5));
    }

    /**
     * Joins together the results from six patterns via their plans.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param p1
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p2
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p3
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p4
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p5
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p6
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @return an Observable that emits the results from matching six patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public final static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4, Plan0<R> p5, Plan0<R> p6) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4, p5, p6));
    }

    /**
     * Joins together the results from seven patterns via their plans.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param p1
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p2
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p3
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p4
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p5
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p6
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p7
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @return an Observable that emits the results from matching seven patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public final static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4, Plan0<R> p5, Plan0<R> p6, Plan0<R> p7) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4, p5, p6, p7));
    }

    /**
     * Joins together the results from eight patterns via their plans.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param p1
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p2
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p3
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p4
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p5
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p6
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p7
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p8
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @return an Observable that emits the results from matching eight patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public final static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4, Plan0<R> p5, Plan0<R> p6, Plan0<R> p7, Plan0<R> p8) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4, p5, p6, p7, p8));
    }

    /**
     * Joins together the results from nine patterns via their plans.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param p1
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p2
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p3
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p4
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p5
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p6
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p7
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p8
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @param p9
     *            a plan, created by use of the {@link #then} Observer on a pattern
     * @return an Observable that emits the results from matching nine patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public final static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4, Plan0<R> p5, Plan0<R> p6, Plan0<R> p7, Plan0<R> p8, Plan0<R> p9) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4, p5, p6, p7, p8, p9));
    }

    /**
     * Returns an Observable that emits the results of a function of your choosing applied to
     * combinations items emitted, in sequence, by an Iterable of other Observables.
     * <p> {@code zip} applies this function in strict sequence, so the first item emitted by the new
     * Observable will be the result of the function applied to the first item emitted by each of
     * the source Observables; the second item emitted by the new Observable will be the result of
     * the function applied to the second item emitted by each of those Observables; and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will invoke {@code onNext} as
     * many times as the number of {@code onNext} invokations of the source Observable that emits
     * the fewest items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * 
     * @param ws
     *            an Iterable of source Observables
     * @param zipFunction
     *            a function that, when applied to an item emitted by each of the source
     *            Observables, results in an item that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-zip">RxJava Wiki: zip()</a>
     */
    public final static <R> Observable<R> zip(Iterable<? extends Observable<?>> ws, FuncN<? extends R> zipFunction) {
        List<Observable<?>> os = new ArrayList<Observable<?>>();
        for (Observable<?> o : ws) {
            os.add(o);
        }
        return Observable.just(os.toArray(new Observable<?>[os.size()])).lift(new OperatorZip<R>(zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your choosing applied to
     * combinations of <i>n</i> items emitted, in sequence, by the <i>n</i> Observables emitted by
     * a specified Observable.
     * <p> {@code zip} applies this function in strict sequence, so the first item emitted by the new
     * Observable will be the result of the function applied to the first item emitted by each of
     * the Observables emitted by the source Observable; the second item emitted by the new
     * Observable will be the result of the function applied to the second item emitted by each of
     * those Observables; and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will invoke {@code onNext} as
     * many times as the number of {@code onNext} invokations of the source Observable that emits
     * the fewest items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.o.png">
     * 
     * @param ws
     *            an Observable of source Observables
     * @param zipFunction
     *            a function that, when applied to an item emitted by each of the Observables
     *            emitted by {@code ws}, results in an item that will be emitted by the resulting
     *            Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-zip">RxJava Wiki: zip()</a>
     */
    public final static <R> Observable<R> zip(Observable<? extends Observable<?>> ws, final FuncN<? extends R> zipFunction) {
        return ws.toList().map(new Func1<List<? extends Observable<?>>, Observable<?>[]>() {

            @Override
            public Observable<?>[] call(List<? extends Observable<?>> o) {
                return o.toArray(new Observable<?>[o.size()]);
            }

        }).lift(new OperatorZip<R>(zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your choosing applied to
     * combinations of two items emitted, in sequence, by two other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p> {@code zip} applies this function in strict sequence, so the first item emitted by the new
     * Observable will be the result of the function applied to the first item emitted by {@code o1} and the first item emitted by {@code o2}; the second item emitted by the new Observable will
     * be the result of the function applied to the second item emitted by {@code o1} and the second
     * item emitted by {@code o2}; and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will invoke {@link Observer#onNext onNext} as many times as the number of {@code onNext} invocations of
     * the source Observable that emits the fewest items.
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            a second source Observable
     * @param zipFunction
     *            a function that, when applied to an item emitted by each of the source
     *            Observables, results in an item that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-zip">RxJava Wiki: zip()</a>
     */
    public final static <T1, T2, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, final Func2<? super T1, ? super T2, ? extends R> zipFunction) {
        return just(new Observable<?>[] { o1, o2 }).lift(new OperatorZip<R>(zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your choosing applied to
     * combinations of three items emitted, in sequence, by three other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p> {@code zip} applies this function in strict sequence, so the first item emitted by the new
     * Observable will be the result of the function applied to the first item emitted by {@code o1}, the first item emitted by {@code o2}, and the first item emitted by {@code o3};
     * the second item emitted by the new Observable will be the result of the function applied to
     * the second item emitted by {@code o1}, the second item emitted by {@code o2}, and the second
     * item emitted by {@code o3}; and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will invoke {@link Observer#onNext onNext} as many times as the number of {@code onNext} invocations of
     * the source Observable that emits the fewest items.
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            a second source Observable
     * @param o3
     *            a third source Observable
     * @param zipFunction
     *            a function that, when applied to an item emitted by each of the source
     *            Observables, results in an item that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-zip">RxJava Wiki: zip()</a>
     */
    public final static <T1, T2, T3, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Func3<? super T1, ? super T2, ? super T3, ? extends R> zipFunction) {
        return just(new Observable<?>[] { o1, o2, o3 }).lift(new OperatorZip<R>(zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your choosing applied to
     * combinations of four items emitted, in sequence, by four other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p> {@code zip} applies this function in strict sequence, so the first item emitted by the new
     * Observable will be the result of the function applied to the first item emitted by {@code o1}, the first item emitted by {@code o2}, the first item emitted by {@code o3}, and
     * the first item emitted by {@code 04}; the second item emitted by the new Observable will be
     * the result of the function applied to the second item emitted by each of those Observables;
     * and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will invoke {@link Observer#onNext onNext} as many times as the number of {@code onNext} invocations of
     * the source Observable that emits the fewest items.
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            a second source Observable
     * @param o3
     *            a third source Observable
     * @param o4
     *            a fourth source Observable
     * @param zipFunction
     *            a function that, when applied to an item emitted by each of the source
     *            Observables, results in an item that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-zip">RxJava Wiki: zip()</a>
     */
    public final static <T1, T2, T3, T4, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Func4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> zipFunction) {
        return just(new Observable<?>[] { o1, o2, o3, o4 }).lift(new OperatorZip<R>(zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your choosing applied to
     * combinations of five items emitted, in sequence, by five other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p> {@code zip} applies this function in strict sequence, so the first item emitted by the new
     * Observable will be the result of the function applied to the first item emitted by {@code o1}, the first item emitted by {@code o2}, the first item emitted by {@code o3}, the
     * first item emitted by {@code o4}, and the first item emitted by {@code o5}; the second item
     * emitted by the new Observable will be the result of the function applied to the second item
     * emitted by each of those Observables; and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will invoke {@link Observer#onNext onNext} as many times as the number of {@code onNext} invocations of
     * the source Observable that emits the fewest items.
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            a second source Observable
     * @param o3
     *            a third source Observable
     * @param o4
     *            a fourth source Observable
     * @param o5
     *            a fifth source Observable
     * @param zipFunction
     *            a function that, when applied to an item emitted by each of the source
     *            Observables, results in an item that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-zip">RxJava Wiki: zip()</a>
     */
    public final static <T1, T2, T3, T4, T5, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Func5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> zipFunction) {
        return just(new Observable<?>[] { o1, o2, o3, o4, o5 }).lift(new OperatorZip<R>(zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your choosing applied to
     * combinations of six items emitted, in sequence, by six other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p> {@code zip} applies this function in strict sequence, so the first item emitted by the new
     * Observable will be the result of the function applied to the first item emitted each source
     * Observable, the second item emitted by the new Observable will be the result of the function
     * applied to the second item emitted by each of those Observables, and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will invoke {@link Observer#onNext onNext} as many times as the number of {@code onNext} invocations of
     * the source Observable that emits the fewest items.
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            a second source Observable
     * @param o3
     *            a third source Observable
     * @param o4
     *            a fourth source Observable
     * @param o5
     *            a fifth source Observable
     * @param o6
     *            a sixth source Observable
     * @param zipFunction
     *            a function that, when applied to an item emitted by each of the source
     *            Observables, results in an item that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-zip">RxJava Wiki: zip()</a>
     */
    public final static <T1, T2, T3, T4, T5, T6, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6,
            Func6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> zipFunction) {
        return just(new Observable<?>[] { o1, o2, o3, o4, o5, o6 }).lift(new OperatorZip<R>(zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your choosing applied to
     * combinations of seven items emitted, in sequence, by seven other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p> {@code zip} applies this function in strict sequence, so the first item emitted by the new
     * Observable will be the result of the function applied to the first item emitted each source
     * Observable, the second item emitted by the new Observable will be the result of the function
     * applied to the second item emitted by each of those Observables, and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will invoke {@link Observer#onNext onNext} as many times as the number of {@code onNext} invocations of
     * the source Observable that emits the fewest items.
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            a second source Observable
     * @param o3
     *            a third source Observable
     * @param o4
     *            a fourth source Observable
     * @param o5
     *            a fifth source Observable
     * @param o6
     *            a sixth source Observable
     * @param o7
     *            a seventh source Observable
     * @param zipFunction
     *            a function that, when applied to an item emitted by each of the source
     *            Observables, results in an item that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-zip">RxJava Wiki: zip()</a>
     */
    public final static <T1, T2, T3, T4, T5, T6, T7, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7,
            Func7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> zipFunction) {
        return just(new Observable<?>[] { o1, o2, o3, o4, o5, o6, o7 }).lift(new OperatorZip<R>(zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your choosing applied to
     * combinations of eight items emitted, in sequence, by eight other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p> {@code zip} applies this function in strict sequence, so the first item emitted by the new
     * Observable will be the result of the function applied to the first item emitted each source
     * Observable, the second item emitted by the new Observable will be the result of the function
     * applied to the second item emitted by each of those Observables, and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will invoke {@link Observer#onNext onNext} as many times as the number of {@code onNext} invocations of
     * the source Observable that emits the fewest items.
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            a second source Observable
     * @param o3
     *            a third source Observable
     * @param o4
     *            a fourth source Observable
     * @param o5
     *            a fifth source Observable
     * @param o6
     *            a sixth source Observable
     * @param o7
     *            a seventh source Observable
     * @param o8
     *            an eighth source Observable
     * @param zipFunction
     *            a function that, when applied to an item emitted by each of the source
     *            Observables, results in an item that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-zip">RxJava Wiki: zip()</a>
     */
    public final static <T1, T2, T3, T4, T5, T6, T7, T8, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7, Observable<? extends T8> o8,
            Func8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> zipFunction) {
        return just(new Observable<?>[] { o1, o2, o3, o4, o5, o6, o7, o8 }).lift(new OperatorZip<R>(zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your choosing applied to
     * combinations of nine items emitted, in sequence, by nine other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p> {@code zip} applies this function in strict sequence, so the first item emitted by the new
     * Observable will be the result of the function applied to the first item emitted each source
     * Observable, the second item emitted by the new Observable will be the result of the function
     * applied to the second item emitted by each of those Observables, and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will invoke {@link Observer#onNext onNext} as many times as the number of {@code onNext} invocations of
     * the source Observable that emits the fewest items.
     * 
     * @param o1
     *            the first source Observable
     * @param o2
     *            a second source Observable
     * @param o3
     *            a third source Observable
     * @param o4
     *            a fourth source Observable
     * @param o5
     *            a fifth source Observable
     * @param o6
     *            a sixth source Observable
     * @param o7
     *            a seventh source Observable
     * @param o8
     *            an eighth source Observable
     * @param o9
     *            a ninth source Observable
     * @param zipFunction
     *            a function that, when applied to an item emitted by each of the source
     *            Observables, results in an item that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-zip">RxJava Wiki: zip()</a>
     */
    public final static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7, Observable<? extends T8> o8,
            Observable<? extends T9> o9, Func9<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? extends R> zipFunction) {
        return just(new Observable<?>[] { o1, o2, o3, o4, o5, o6, o7, o8, o9 }).lift(new OperatorZip<R>(zipFunction));
    }

    /**
     * Synonymous with {@code reduce()}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/aggregate.png">
     * 
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-reduce">RxJava Wiki: reduce()</a>
     * @see #reduce(Func2)
     * @deprecated use #reduce(Func2)
     */
    @Deprecated
    public final Observable<T> aggregate(Func2<T, T, T> accumulator) {
        return reduce(accumulator);
    }

    /**
     * Synonymous with {@code reduce()}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/aggregateSeed.png">
     * 
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-reduce">RxJava Wiki: reduce()</a>
     * @see #reduce(Object, Func2)
     * @deprecated use #reduce(Object, Func2)
     */
    @Deprecated
    public final <R> Observable<R> aggregate(R initialValue, Func2<R, ? super T, R> accumulator) {
        return reduce(initialValue, accumulator);
    }

    /**
     * Returns an Observable that emits a Boolean that indicates whether all of the items emitted by
     * the source Observable satisfy a condition.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/all.png">
     * 
     * @param predicate
     *            a function that evaluates an item and returns a Boolean
     * @return an Observable that emits {@code true} if all items emitted by the source Observable
     *         satisfy the predicate; otherwise, {@code false}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-all">RxJava Wiki: all()</a>
     */
    public final Observable<Boolean> all(Func1<? super T, Boolean> predicate) {
        return create(OperationAll.all(this, predicate));
    }

    /**
     * Returns a Pattern that matches when both Observables emit an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param right
     *            an Observable to match with the source Observable
     * @return a Pattern object that matches when both Observables emit an item
     * @throws NullPointerException
     *             if {@code right} is null
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: and()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229153.aspx">MSDN: Observable.And</a>
     */
    public final <T2> Pattern2<T, T2> and(Observable<T2> right) {
        return OperationJoinPatterns.and(this, right);
    }

    /**
     * Hides the identity of this Observable. Useful for instance when you have an implementation
     * of a subclass of Observable but you want to hide the properties and methods of this subclass
     * from whomever you are passing the Observable to.
     * 
     * @return an Observable that hides the identity of this Observable
     */
    public final Observable<T> asObservable() {
        return create(new OperationAsObservable<T>(this));
    }

    /**
     * Returns an Observable that transforms items emitted by the source Observable into Doubles by
     * using a function you provide and then emits the Double average of the complete sequence of
     * transformed values.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.f.png">
     * 
     * @param valueExtractor
     *            the function to transform an item emitted by the source Observable into a Double
     * @return an Observable that emits a single item: the Double average of the complete sequence
     *         of items emitted by the source Observable when transformed into Doubles by the
     *         specified function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageDouble()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public final Observable<Double> averageDouble(Func1<? super T, Double> valueExtractor) {
        return create(new OperationAverage.AverageDoubleExtractor<T>(this, valueExtractor));
    }

    /**
     * Returns an Observable that transforms items emitted by the source Observable into Floats by
     * using a function you provide and then emits the Float average of the complete sequence of
     * transformed values.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.f.png">
     * 
     * @param valueExtractor
     *            the function to transform an item emitted by the source Observable into a Float
     * @return an Observable that emits a single item: the Float average of the complete sequence of
     *         items emitted by the source Observable when transformed into Floats by the specified
     *         function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageFloat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public final Observable<Float> averageFloat(Func1<? super T, Float> valueExtractor) {
        return create(new OperationAverage.AverageFloatExtractor<T>(this, valueExtractor));
    }

    /**
     * Returns an Observable that transforms items emitted by the source Observable into Integers by
     * using a function you provide and then emits the Integer average of the complete sequence of
     * transformed values.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.f.png">
     * 
     * @param valueExtractor
     *            the function to transform an item emitted by the source Observable into an Integer
     * @return an Observable that emits a single item: the Integer average of the complete sequence
     *         of items emitted by the source Observable when transformed into Integers by the
     *         specified function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageInteger()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public final Observable<Integer> averageInteger(Func1<? super T, Integer> valueExtractor) {
        return create(new OperationAverage.AverageIntegerExtractor<T>(this, valueExtractor));
    }

    /**
     * Returns an Observable that transforms items emitted by the source Observable into Longs by
     * using a function you provide and then emits the Long average of the complete sequence of
     * transformed values.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.f.png">
     * 
     * @param valueExtractor
     *            the function to transform an item emitted by the source Observable into a Long
     * @return an Observable that emits a single item: the Long average of the complete sequence of
     *         items emitted by the source Observable when transformed into Longs by the specified
     *         function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageLong()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public final Observable<Long> averageLong(Func1<? super T, Long> valueExtractor) {
        return create(new OperationAverage.AverageLongExtractor<T>(this, valueExtractor));
    }

    /**
     * Returns an Observable that emits buffers of items it collects from the source Observable.
     * The resulting Observable emits connected, non-overlapping buffers. It emits the current
     * buffer and replaces it with a new buffer when the Observable produced by the specified {@code bufferClosingSelector} emits an item. It then uses the {@code bufferClosingSelector} to create a
     * new Observable to observe to indicate the end of the next buffer.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer1.png">
     * 
     * @param bufferClosingSelector
     *            a {@link Func0} that produces an Observable for each buffer created. When
     *            this {@code Observable} emits an item, {@code buffer()} emits the associated
     *            buffer and replaces it with a new one.
     * @return an Observable that emits a connected, non-overlapping buffer of items from
     *         the source Observable each time the current Observable created with the {@code bufferClosingSelector} argument emits an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     */
    public final <TClosing> Observable<List<T>> buffer(Func0<? extends Observable<? extends TClosing>> bufferClosingSelector) {
        return create(OperationBuffer.buffer(this, bufferClosingSelector));
    }

    /**
     * Returns an Observable that emits buffers of items it collects from the source Observable.
     * The resulting Observable emits connected, non-overlapping buffers, each containing {@code count} items. When the source Observable completes or encounters an error, the
     * resulting Observable emits the current buffer and propagates the notification from the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer3.png">
     * 
     * @param count
     *            the maximum number of items in each buffer before it should be emitted
     * @return an Observable that emits connected, non-overlapping buffers, each containing
     *         at most {@code count} items from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     */
    public final Observable<List<T>> buffer(int count) {
        return create(OperationBuffer.buffer(this, count));
    }

    /**
     * Returns an Observable that emits buffers of items it collects from the source Observable.
     * The resulting Observable emits buffers every {@code skip} items, each containing {@code count} items. When the source Observable completes or encounters an error, the
     * resulting Observable emits the current buffer and propagates the notification from the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer4.png">
     * 
     * @param count
     *            the maximum size of each buffer before it should be emitted
     * @param skip
     *            how many items emitted by the source Observable should be skipped before starting
     *            a new buffer. Note that when {@code skip} and {@code count} are equal, this is the
     *            same operation as {@link #buffer(int)}.
     * @return an Observable that emits buffers for every {@code skip} item from the source
     *         Observable and containing at most {@code count} items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     */
    public final Observable<List<T>> buffer(int count, int skip) {
        return create(OperationBuffer.buffer(this, count, skip));
    }

    /**
     * Returns an Observable that emits buffers of items it collects from the source Observable.
     * The resulting Observable starts a new buffer periodically, as determined by the {@code timeshift} argument. It emits each buffer after a fixed timespan, specified by the {@code timespan}
     * argument. When the source Observable completes or encounters an error, the
     * resulting Observable emits the current buffer and propagates the notification from the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer7.png">
     * 
     * @param timespan
     *            the period of time each buffer collects items before it is emitted
     * @param timeshift
     *            the period of time after which a new buffer will be created
     * @param unit
     *            the unit of time that applies to the {@code timespan} and {@code timeshift} arguments
     * @return an Observable that emits new buffers of items emitted by the source
     *         Observable periodically after a fixed timespan has elapsed
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     */
    public final Observable<List<T>> buffer(long timespan, long timeshift, TimeUnit unit) {
        return create(OperationBuffer.buffer(this, timespan, timeshift, unit));
    }

    /**
     * Returns an Observable that emits buffers of items it collects from the source Observable.
     * The resulting Observable starts a new buffer periodically, as determined by the {@code timeshift} argument, and on the specified {@code scheduler}. It emits each buffer
     * after a fixed timespan, specified by the {@code timespan} argument. When the source
     * Observable completes or encounters an error, the resulting Observable emits the current
     * buffer propagates the notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer7.s.png">
     * 
     * @param timespan
     *            the period of time each buffer collects items before it is emitted
     * @param timeshift
     *            the period of time after which a new buffer will be created
     * @param unit
     *            the unit of time that applies to the {@code timespan} and {@code timeshift} arguments
     * @param scheduler
     *            the {@link Scheduler} to use when determining the end and start of a buffer
     * @return an Observable that emits new buffers of items emitted by the source
     *         Observable periodically after a fixed timespan has elapsed
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     */
    public final Observable<List<T>> buffer(long timespan, long timeshift, TimeUnit unit, Scheduler scheduler) {
        return create(OperationBuffer.buffer(this, timespan, timeshift, unit, scheduler));
    }

    /**
     * Returns an Observable that emits buffers of items it collects from the source Observable.
     * The resulting Observable emits connected, non-overlapping buffers, each of a fixed duration
     * specified by the {@code timespan} argument. When the source Observable completes or
     * encounters an error, the resulting Observable emits the current buffer and propagates the
     * notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer5.png">
     * 
     * @param timespan
     *            the period of time each buffer collects items before it is emitted and replaced
     *            with a new buffer
     * @param unit
     *            the unit of time that applies to the {@code timespan} argument
     * @return an Observable that emits connected, non-overlapping buffers of items emitted
     *         by the source Observable within a fixed duration
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     */
    public final Observable<List<T>> buffer(long timespan, TimeUnit unit) {
        return create(OperationBuffer.buffer(this, timespan, unit));
    }

    /**
     * Returns an Observable that emits buffers of items it collects from the source Observable.
     * The resulting Observable emits connected, non-overlapping buffers, each of a fixed duration
     * specified by the {@code timespan} argument or a maximum size specified by the {@code count} argument (whichever is reached first). When the source Observable completes or encounters an
     * error, the resulting Observable emits the current buffer and propagates the notification from
     * the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer6.png">
     * 
     * @param timespan
     *            the period of time each buffer collects items before it is emitted and replaced
     *            with a new buffer
     * @param unit
     *            the unit of time which applies to the {@code timespan} argument
     * @param count
     *            the maximum size of each buffer before it is emitted
     * @return an Observable that emits connected, non-overlapping buffers of items emitted
     *         by the source Observable, after a fixed duration or when the buffer reaches maximum
     *         capacity (whichever occurs first)
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     */
    public final Observable<List<T>> buffer(long timespan, TimeUnit unit, int count) {
        return create(OperationBuffer.buffer(this, timespan, unit, count));
    }

    /**
     * Returns an Observable that emits buffers of items it collects from the source Observable.
     * The resulting Observable emits connected, non-overlapping buffers, each of a fixed duration
     * specified by the {@code timespan} argument as measured on the specified {@code scheduler}, or
     * a maximum size specified by the {@code count} argument (whichever is reached first). When the
     * source Observable completes or encounters an error, the resulting Observable emits the
     * current buffer and propagates the notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer6.s.png">
     * 
     * @param timespan
     *            the period of time each buffer collects items before it is emitted and replaced
     *            with a new buffer
     * @param unit
     *            the unit of time which applies to the {@code timespan} argument
     * @param count
     *            the maximum size of each buffer before it is emitted
     * @param scheduler
     *            the {@link Scheduler} to use when determining the end and start of a buffer
     * @return an Observable that emits connected, non-overlapping buffers of items emitted
     *         by the source Observable after a fixed duration or when the buffer reaches maximum
     *         capacity (whichever occurs first)
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     */
    public final Observable<List<T>> buffer(long timespan, TimeUnit unit, int count, Scheduler scheduler) {
        return create(OperationBuffer.buffer(this, timespan, unit, count, scheduler));
    }

    /**
     * Returns an Observable that emits buffers of items it collects from the source Observable.
     * The resulting Observable emits connected, non-overlapping buffers, each of a fixed duration
     * specified by the {@code timespan} argument and on the specified {@code scheduler}. When the
     * source Observable completes or encounters an error, the resulting Observable emits the
     * current buffer and propagates the notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer5.s.png">
     * 
     * @param timespan
     *            the period of time each buffer collects items before it is emitted and replaced
     *            with a new buffer
     * @param unit
     *            the unit of time which applies to the {@code timespan} argument
     * @param scheduler
     *            the {@link Scheduler} to use when determining the end and start of a buffer
     * @return an Observable that emits connected, non-overlapping buffers of items emitted
     *         by the source Observable within a fixed duration
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     */
    public final Observable<List<T>> buffer(long timespan, TimeUnit unit, Scheduler scheduler) {
        return create(OperationBuffer.buffer(this, timespan, unit, scheduler));
    }

    /**
     * Returns an Observable that emits buffers of items it collects from the source Observable.
     * The resulting Observable emits buffers that it creates when the specified {@code bufferOpenings} Observable emits an item, and closes when the Observable returned from
     * {@code bufferClosingSelector} emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer2.png">
     * 
     * @param bufferOpenings
     *            the Observable that, when it emits an item, causes a new buffer to be
     *            created
     * @param bufferClosingSelector
     *            the {@link Func1} that is used to produce an Observable for every buffer
     *            created. When this Observable emits an item, the associated buffer is
     *            emitted.
     * @return an Observable that emits buffers, containing items from the source
     *         Observable, that are created and closed when the specified Observables emit
     *         items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     */
    public final <TOpening, TClosing> Observable<List<T>> buffer(Observable<? extends TOpening> bufferOpenings, Func1<? super TOpening, ? extends Observable<? extends TClosing>> bufferClosingSelector) {
        return create(OperationBuffer.buffer(this, bufferOpenings, bufferClosingSelector));
    }

    /**
     * Returns an Observable that emits non-overlapping buffered items from the source Observable
     * each time the specified boundary Observable emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer8.png">
     * <p>
     * Completion of either the source or the boundary Observable causes the returned Observable
     * to emit the latest buffer and complete.
     * 
     * @param <B>
     *            the boundary value type (ignored)
     * @param boundary
     *            the boundary Observable
     * @return an Observable that emits buffered items from the source Observable when the boundary
     *         Observable emits an item
     * @see #buffer(rx.Observable, int)
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     */
    public final <B> Observable<List<T>> buffer(Observable<B> boundary) {
        return create(OperationBuffer.bufferWithBoundaryObservable(this, boundary));
    }

    /**
     * Returns an Observable that emits non-overlapping buffered items from the source Observable
     * each time the specified boundary Observable emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer8.png">
     * <p>
     * Completion of either the source or the boundary Observable causes the returned Observable
     * to emit the latest buffer and complete.
     * 
     * @param <B>
     *            the boundary value type (ignored)
     * @param boundary
     *            the boundary Observable
     * @param initialCapacity
     *            the initial capacity of each buffer chunk
     * @return an Observable that emits buffered items from the source Observable when the boundary
     *         Observable emits an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-buffer">RxJava Wiki: buffer()</a>
     * @see #buffer(rx.Observable, int)
     */
    public final <B> Observable<List<T>> buffer(Observable<B> boundary, int initialCapacity) {
        return create(OperationBuffer.bufferWithBoundaryObservable(this, boundary, initialCapacity));
    }

    /**
     * This method has similar behavior to {@link #replay} except that this auto-subscribes to the
     * source Observable rather than returning a {@link ConnectableObservable}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/cache.png">
     * <p>
     * This is useful when you want an Observable to cache responses and you can't control the
     * subscribe/unsubscribe behavior of all the {@link Subscriber}s.
     * <p>
     * When you call {@code cache()}, it does not yet subscribe to the source Observable. This only
     * happens when {@code subscribe} is called the first time on the Observable returned by
     * {@code cache()}.
     * <p>
     *                             <!-- IS THIS NOTE STILL VALID??? -->
     * <em>Note:</em> You sacrifice the ability to unsubscribe from the origin when you use the
     * {@code cache()} Observer so be careful not to use this Observer on Observables that emit an
     * infinite or very large number of items that will use up memory.
     * 
     * @return an Observable that, when first subscribed to, caches all of its items and
     *         notifications for the benefit of subsequent observers
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-cache">RxJava Wiki: cache()</a>
     */
    public final Observable<T> cache() {
        return create(OperationCache.cache(this));
    }

    /**
     * Returns an Observable that emits the items emitted by the source Observable, converted to the
     * specified type.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/cast.png">
     * 
     * @param klass
     *            the target class type that the items emitted by the source Observable will be
     *            converted to before being emitted by the resulting Observable
     * @return an Observable that emits each item from the source Observable after converting it to
     *         the specified type
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-cast">RxJava Wiki: cast()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211842.aspx">MSDN: Observable.Cast</a>
     */
    public final <R> Observable<R> cast(final Class<R> klass) {
        return lift(new OperatorCast<T, R>(klass));
    }

    /**
     * Collect values into a single mutable data structure.
     * <p>
     * This is a simplified version of {@code reduce} that does not need to return the state on each
     * pass.
     * <p>
     * 
     * @param state
     *            FIXME FIXME FIXME
     * @param collector
     *            FIXME FIXME FIXME
     * @return FIXME FIXME FIXME
     */
    public final <R> Observable<R> collect(R state, final Action2<R, ? super T> collector) {
        Func2<R, T, R> accumulator = new Func2<R, T, R>() {

            @Override
            public final R call(R state, T value) {
                collector.call(state, value);
                return state;
            }

        };
        return reduce(state, accumulator);
    }

    /**
     * Returns a new Observable that emits items resulting from applying a function that you supply
     * to each item emitted by the source Observable, where that function returns an Observable, and
     * then emitting the items that result from concatinating those resulting Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concatMap.png">
     * 
     * @param func
     *            a function that, when applied to an item emitted by the source Observable, returns
     *            an Observable
     * @return an Observable that emits the result of applying the transformation function to each
     *         item emitted by the source Observable and concatinating the Observables obtained from
     *         this transformation
     */
    public final <R> Observable<R> concatMap(Func1<? super T, ? extends Observable<? extends R>> func) {
        return concat(map(func));
    }

    /**
     * Returns an Observable that emits a Boolean that indicates whether the source Observable
     * emitted a specified item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/contains.png">
     * 
     * @param element
     *            the item to search for in the emissions from the source Observable
     * @return an Observable that emits {@code true} if the specified item is emitted by the source
     *         Observable, or {@code false} if the source Observable completes without emitting that
     *         item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-contains">RxJava Wiki: contains()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228965.aspx">MSDN: Observable.Contains</a>
     */
    public final Observable<Boolean> contains(final T element) {
        return exists(new Func1<T, Boolean>() {
            public final Boolean call(T t1) {
                return element == null ? t1 == null : element.equals(t1);
            }
        });
    }

    /**
     * Returns an Observable emits the count of the total number of items emitted by the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/count.png">
     * 
     * @return an Observable that emits a single item: the number of elements emitted by the
     *         source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-count-and-longcount">RxJava Wiki: count()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229470.aspx">MSDN: Observable.Count</a>
     * @see #longCount()
     */
    public final Observable<Integer> count() {
        return reduce(0, new Func2<Integer, T, Integer>() {
            @Override
            public final Integer call(Integer t1, T t2) {
                return t1 + 1;
            }
        });
    }

    /**
     * Return an Observable that mirrors the source Observable, except that it drops items emitted
     * by the source Observable that are followed by another item within a computed debounce
     * duration.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/debounce.f.png">
     * 
     * @param <U>
     *            the debounce value type (ignored)
     * @param debounceSelector
     *            function to retrieve a sequence that indicates the throttle duration for each item
     * @return an Observable that omits items emitted by the source Observable that are followed by
     *         another item within a computed debounce duration
     */
    public final <U> Observable<T> debounce(Func1<? super T, ? extends Observable<U>> debounceSelector) {
        return create(OperationDebounce.debounceSelector(this, debounceSelector));
    }

    /**
     * Return an Observable that mirrors the source Observable, except that it drops items emitted
     * by the source Observable that are followed by newer items before a timeout value expires.
     * The timer resets on each emission.
     * <p>
     * <em>Note:</em> If items keep being emitted by the source Observable faster than the timeout
     * then no items will be emitted by the resulting Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/debounce.png">
     * <p>
     * Information on debounce vs throttle:
     * <p>
     * <ul>
     * <li><a href="http://drupalmotion.com/article/debounce-and-throttle-visual-explanation">Debounce and Throttle: visual explanation</a></li>
     * <li><a href="http://unscriptable.com/2009/03/20/debouncing-javascript-methods/">Debouncing: javascript methods</a></li>
     * <li><a href="http://www.illyriad.co.uk/blog/index.php/2011/09/javascript-dont-spam-your-server-debounce-and-throttle/">Javascript - don't spam your server: debounce and throttle</a></li>
     * </ul>
     * 
     * @param timeout
     *            the time each item has to be "the most recent" of those emitted by the source
     *            Observable to ensure that it's not dropped
     * @param unit
     *            the {@link TimeUnit} for the timeout
     * @return an Observable that filters out items from the source Observable that are too
     *         quickly followed by newer items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-throttlewithtimeout-or-debounce">RxJava Wiki: debounce()</a>
     * @see #throttleWithTimeout(long, TimeUnit)
     */
    public final Observable<T> debounce(long timeout, TimeUnit unit) {
        return create(OperationDebounce.debounce(this, timeout, unit));
    }

    /**
     * Return an Observable that mirrors the source Observable, except that it drops items emitted
     * by the source Observable that are followed by newer items before a timeout value expires on
     * a specified Scheduler. The timer resets on each emission.
     * <p>
     * <em>Note:</em> If items keep being emitted by the source Observable faster than the timeout
     * then no items will be emitted by the resulting Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/debounce.s.png">
     * <p>
     * Information on debounce vs throttle:
     * <p>
     * <ul>
     * <li><a href="http://drupalmotion.com/article/debounce-and-throttle-visual-explanation">Debounce and Throttle: visual explanation</a></li>
     * <li><a href="http://unscriptable.com/2009/03/20/debouncing-javascript-methods/">Debouncing: javascript methods</a></li>
     * <li><a href="http://www.illyriad.co.uk/blog/index.php/2011/09/javascript-dont-spam-your-server-debounce-and-throttle/">Javascript - don't spam your server: debounce and throttle</a></li>
     * </ul>
     * 
     * @param timeout
     *            the time each item has to be "the most recent" of those emitted by the source
     *            Observable to ensure that it's not dropped
     * @param unit
     *            the unit of time for the specified timeout
     * @param scheduler
     *            the {@link Scheduler} to use internally to manage the timers that handle the
     *            timeout for each item
     * @return an Observable that filters out items from the source Observable that are too
     *         quickly followed by newer items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-throttlewithtimeout-or-debounce">RxJava Wiki: debounce()</a>
     * @see #throttleWithTimeout(long, TimeUnit, Scheduler)
     */
    public final Observable<T> debounce(long timeout, TimeUnit unit, Scheduler scheduler) {
        return create(OperationDebounce.debounce(this, timeout, unit, scheduler));
    }

    /**
     * Returns an Observable that emits the items emitted by the source Observable or a specified
     * default item if the source Observable is empty.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/defaultIfEmpty.png">
     * 
     * @param defaultValue
     *            the item to emit if the source Observable emits no items
     * @return an Observable that emits either the specified default item if the source Observable
     *         emits no items, or the items emitted by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-defaultifempty">RxJava Wiki: defaultIfEmpty()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229624.aspx">MSDN: Observable.DefaultIfEmpty</a>
     */
    public final Observable<T> defaultIfEmpty(T defaultValue) {
        return create(OperationDefaultIfEmpty.defaultIfEmpty(this, defaultValue));
    }

    /**
     * Returns an Observable that delays the subscription to and emissions from the souce Observable
     * via another Observable on a per-item basis.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/delay.oo.png">
     * <p>
     * <em>Note:</code> the resulting Observable will immediately propagate any {@code onError} notification from the source Observable.
     * 
     * @param <U>
     *            the subscription delay value type (ignored)
     * @param <V>
     *            the item delay value type (ignored)
     * @param subscriptionDelay
     *            a function that returns an Observable that triggers the subscription to the source
     *            Observable once it emits any item
     * @param itemDelay
     *            a function that returns an Observable for each item emitted by the source
     *            Observable, which is then used to delay the emission of that item by the resulting
     *            Observable until the Observable returned from {@code itemDelay} emits an item
     * @return an Observable that delays the subscription and emissions of the source Observable via
     *         another Observable on a per-item basis
     */
    public final <U, V> Observable<T> delay(
            Func0<? extends Observable<U>> subscriptionDelay,
            Func1<? super T, ? extends Observable<V>> itemDelay) {
        return create(OperationDelay.delay(this, subscriptionDelay, itemDelay));
    }

    /**
     * Returns an Observable that delays the emissions of the source Observable via another
     * Observable on a per-item basis.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/delay.o.png">
     * <p>
     * <em>Note:</code> the resulting Observable will immediately propagate any {@code onError} notification from the source Observable.
     * 
     * @param <U>
     *            the item delay value type (ignored)
     * @param itemDelay
     *            a function that returns an Observable for each item emitted by the source
     *            Observable, which is then used to delay the emission of that item by the resulting
     *            Observable until the Observable returned from {@code itemDelay} emits an item
     * @return an Observable that delays the emissions of the source Observable via another
     *         Observable on a per-item basis
     */
    public final <U> Observable<T> delay(Func1<? super T, ? extends Observable<U>> itemDelay) {
        return create(OperationDelay.delay(this, itemDelay));
    }

    /**
     * Returns an Observable that emits the items emitted by the source Observable shifted forward
     * in time by a specified delay. Error notifications from the source Observable are not delayed.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/delay.png">
     * 
     * @param delay
     *            the delay to shift the source by
     * @param unit
     *            the {@link TimeUnit} in which {@code period} is defined
     * @return the source Observable shifted in time by the specified delay
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-delay">RxJava Wiki: delay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229810.aspx">MSDN: Observable.Delay</a>
     */
    public final Observable<T> delay(long delay, TimeUnit unit) {
        return OperationDelay.delay(this, delay, unit, Schedulers.computation());
    }

    /**
     * Returns an Observable that emits the items emitted by the source Observable shifted forward
     * in time by a specified delay. Error notifications from the source Observable are not delayed.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/delay.s.png">
     * 
     * @param delay
     *            the delay to shift the source by
     * @param unit
     *            the {@link TimeUnit} in which {@code period} is defined
     * @param scheduler
     *            the {@link Scheduler} to use for delaying
     * @return the source Observable shifted in time by the specified delay
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-delay">RxJava Wiki: delay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229280.aspx">MSDN: Observable.Delay</a>
     */
    public final Observable<T> delay(long delay, TimeUnit unit, Scheduler scheduler) {
        return OperationDelay.delay(this, delay, unit, scheduler);
    }

    /**
     * Return an Observable that delays the subscription to the source Observable by a given amount
     * of time.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/delaySubscription.png">
     * 
     * @param delay
     *            the time to delay the subscription
     * @param unit
     *            the time unit
     * @return an Observable that delays the subscription to the source Observable by the given
     *         amount
     */
    public final Observable<T> delaySubscription(long delay, TimeUnit unit) {
        return delaySubscription(delay, unit, Schedulers.computation());
    }

    /**
     * Return an Observable that delays the subscription to the source Observable by a given amount
     * of time, both waiting and subscribing on a given Scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/delaySubscription.s.png">
     * 
     * @param delay
     *            the time to delay the subscription
     * @param unit
     *            the time unit of {@code delay}
     * @param scheduler
     *            the scheduler on which the waiting and subscription will happen
     * @return an Observable that delays the subscription to the source Observable by a given
     *         amount, waiting and subscribing on the given Scheduler
     */
    public final Observable<T> delaySubscription(long delay, TimeUnit unit, Scheduler scheduler) {
        return create(OperationDelay.delaySubscription(this, delay, unit, scheduler));
    }

    /**
     * Returns an Observable that reverses the effect of {@link #materialize materialize} by
     * transforming the {@link Notification} objects emitted by the source Observable into the items
     * or notifications they represent.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/dematerialize.png">
     * 
     * @return an Observable that emits the items and notifications embedded in the {@link Notification} objects emitted by the source Observable
     * @throws Throwable
     *             if the source Observable is not of type {@code Observable<Notification<T>>}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-dematerialize">RxJava Wiki: dematerialize()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229047.aspx">MSDN: Observable.dematerialize</a>
     */
    @SuppressWarnings("unchecked")
    public final <T2> Observable<T2> dematerialize() {
        return create(OperationDematerialize.dematerialize((Observable<? extends Notification<? extends T2>>) this));
    }

    /**
     * Returns an Observable that emits all items emitted by the source Observable that are
     * distinct.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/distinct.png">
     * 
     * @return an Observable that emits only those items emitted by the source Observable that are
     *         distinct from each other
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-distinct">RxJava Wiki: distinct()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229764.aspx">MSDN: Observable.distinct</a>
     */
    public final Observable<T> distinct() {
        return create(OperationDistinct.distinct(this));
    }

    /**
     * Returns an Observable that emits all items emitted by the source Observable that are distinct
     * according to a key selector function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/distinct.key.png">
     * 
     * @param keySelector
     *            a function that projects an emitted item to a key value that is used to decide
     *            whether an item is distinct from another one or not
     * @return an Observable that emits those items emitted by the source Observable that have
     *         distinct keys
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-distinct">RxJava Wiki: distinct()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244310.aspx">MSDN: Observable.distinct</a>
     */
    public final <U> Observable<T> distinct(Func1<? super T, ? extends U> keySelector) {
        return create(OperationDistinct.distinct(this, keySelector));
    }

    /**
     * Returns an Observable that emits all items emitted by the source Observable that are distinct
     * from their immediate predecessors.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/distinctUntilChanged.png">
     * 
     * @return an Observable that emits those items from the source Observable that are distinct
     *         from their immediate predecessors
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-distinctuntilchanged">RxJava Wiki: distinctUntilChanged()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229494.aspx">MSDN: Observable.distinctUntilChanged</a>
     */
    public final Observable<T> distinctUntilChanged() {
        return create(OperationDistinctUntilChanged.distinctUntilChanged(this));
    }

    /**
     * Returns an Observable that emits all items emitted by the source Observable that are distinct
     * from their immediate predecessors, according to a key selector function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/distinctUntilChanged.key.png">
     * 
     * @param keySelector
     *            a function that projects an emitted item to a key value that is used to decide
     *            whether an item is distinct from another one or not
     * @return an Observable that emits those items from the source Observable whose keys are
     *         distinct from those of their immediate predecessors
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-distinctuntilchanged">RxJava Wiki: distinctUntilChanged()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229508.aspx">MSDN: Observable.distinctUntilChanged</a>
     */
    public final <U> Observable<T> distinctUntilChanged(Func1<? super T, ? extends U> keySelector) {
        return create(OperationDistinctUntilChanged.distinctUntilChanged(this, keySelector));
    }

    /**
     * Modifies an Observable so that it invokes an action when it calls {@code onCompleted}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/doOnCompleted.png">
     * 
     * @param onCompleted
     *            the action to invoke when the source Observable calls {@code onCompleted}
     * @return the source Observable with the side-effecting behavior applied
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-dooncompleted">RxJava Wiki: doOnCompleted()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229804.aspx">MSDN: Observable.Do</a>
     */
    public final Observable<T> doOnCompleted(final Action0 onCompleted) {
        Observer<T> observer = new Observer<T>() {
            @Override
            public final void onCompleted() {
                onCompleted.call();
            }

            @Override
            public final void onError(Throwable e) {
            }

            @Override
            public final void onNext(T args) {
            }

        };

        return lift(new OperatorDoOnEach<T>(observer));
    }

    /**
     * Modifies an Observable so that it invokes an action for each item it emits.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/doOnEach.png">
     * 
     * @param observer
     *            the action to invoke for each item emitted by the source Observable
     * @return the source Observable with the side-effecting behavior applied
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-dooneach">RxJava Wiki: doOnEach()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229307.aspx">MSDN: Observable.Do</a>
     */
    public final Observable<T> doOnEach(final Action1<Notification<? super T>> onNotification) {
        Observer<T> observer = new Observer<T>() {
            @Override
            public final void onCompleted() {
                onNotification.call(new Notification<T>());
            }

            @Override
            public final void onError(Throwable e) {
                onNotification.call(new Notification<T>(e));
            }

            @Override
            public final void onNext(T v) {
                onNotification.call(new Notification<T>(v));
            }

        };

        return lift(new OperatorDoOnEach<T>(observer));
    }

    /**
     * Modifies an Observable so that it notifies an Observer for each item it emits.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/doOnEach.png">
     * 
     * @param observer
     *            the action to invoke for each item emitted by the source Observable
     * @return the source Observable with the side-effecting behavior applied
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-dooneach">RxJava Wiki: doOnEach()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229307.aspx">MSDN: Observable.Do</a>
     */
    public final Observable<T> doOnEach(Observer<? super T> observer) {
        return lift(new OperatorDoOnEach<T>(observer));
    }

    /**
     * Modifies an Observable so that it invokes an action if it calls {@code onError}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/doOnError.png">
     * 
     * @param onError
     *            the action to invoke if the source Observable calls {@code onError}
     * @return the source Observable with the side-effecting behavior applied
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-doonerror">RxJava Wiki: doOnError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229804.aspx">MSDN: Observable.Do</a>
     */
    public final Observable<T> doOnError(final Action1<Throwable> onError) {
        Observer<T> observer = new Observer<T>() {
            @Override
            public final void onCompleted() {
            }

            @Override
            public final void onError(Throwable e) {
                onError.call(e);
            }

            @Override
            public final void onNext(T args) {
            }

        };

        return lift(new OperatorDoOnEach<T>(observer));
    }

    /**
     * Modifies an Observable so that it invokes an action when it calls {@code onNext}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/doOnNext.png">
     * 
     * @param onNext
     *            the action to invoke when the source Observable calls {@code onNext}
     * @return the source Observable with the side-effecting behavior applied
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-dooneach">RxJava Wiki: doOnNext()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229804.aspx">MSDN: Observable.Do</a>
     */
    public final Observable<T> doOnNext(final Action1<? super T> onNext) {
        Observer<T> observer = new Observer<T>() {
            @Override
            public final void onCompleted() {
            }

            @Override
            public final void onError(Throwable e) {
            }

            @Override
            public final void onNext(T args) {
                onNext.call(args);
            }

        };

        return lift(new OperatorDoOnEach<T>(observer));
    }

    /**
     * Returns an Observable that emits the single item at a specified index in a sequence of
     * emissions from a source Observbable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/elementAt.png">
     * 
     * @param index
     *            the zero-based index of the item to retrieve
     * @return an Observable that emits a single item: the item at the specified position in the
     *         sequence of those emitted by the source Observable
     * @throws IndexOutOfBoundsException
     *             if {@code index} is greater than or equal to the number of items emitted by the
     *             source Observable
     * @throws IndexOutOfBoundsException
     *             if {@code index} is less than 0
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-elementat">RxJava Wiki: elementAt()</a>
     */
    public final Observable<T> elementAt(int index) {
        return create(OperationElementAt.elementAt(this, index));
    }

    /**
     * Returns an Observable that emits the item found at a specified index in a sequence of
     * emissions from a source Observable, or a default item if that index is out of range.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/elementAtOrDefault.png">
     * 
     * @param index
     *            the zero-based index of the item to retrieve
     * @param defaultValue
     *            the default item
     * @return an Observable that emits the item at the specified position in the sequence emitted
     *         by the source Observable, or the default item if that index is outside the bounds of
     *         the source sequence
     * @throws IndexOutOfBoundsException
     *             if {@code index} is less than 0
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-elementatordefault">RxJava Wiki: elementAtOrDefault()</a>
     */
    public final Observable<T> elementAtOrDefault(int index, T defaultValue) {
        return create(OperationElementAt.elementAtOrDefault(this, index, defaultValue));
    }

    /**
     * Returns an Observable that emits {@code true} if any item emitted by the source Observable
     * satisfies a specified condition, otherwise {@code false}. <em>Note:</em> this always emits {@code false} if the source Observable is empty.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/exists.png">
     * <p>
     * In Rx.Net this is the {@code any} Observer but we renamed it in RxJava to better match Java
     * naming idioms.
     * 
     * @param predicate
     *            the condition to test items emitted by the source Observable
     * @return an Observable that emits a Boolean that indicates whether any item emitted by the
     *         source Observable satisfies the {@code predicate}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-exists-and-isempty">RxJava Wiki: exists()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211993.aspx" >MSDN: Observable.Any</a> Note: the description in this page was wrong at the time of this writing.
     */
    public final Observable<Boolean> exists(Func1<? super T, Boolean> predicate) {
        return create(OperationAny.exists(this, predicate));
    }

    /**
     * Filter items emitted by an Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/filter.png">
     * 
     * @param predicate
     *            a function that evaluates the items emitted by the source Observable, returning {@code true} if they pass the filter
     * @return an Observable that emits only those items emitted by the source Observable that the
     *         filter evaluates as {@code true}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-filter-or-where">RxJava Wiki: filter()</a>
     */
    public final Observable<T> filter(Func1<? super T, Boolean> predicate) {
        return create(OperationFilter.filter(this, predicate));
    }

    /**
     * Registers an {@link Action0} to be called when this Observable invokes either {@link Observer#onCompleted onCompleted} or {@link Observer#onError onError}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/finallyDo.png">
     * 
     * @param action
     *            an {@link Action0} to be invoked when the source Observable finishes
     * @return an Observable that emits the same items as the source Observable, then invokes the {@link Action0}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-finallydo">RxJava Wiki: finallyDo()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212133.aspx">MSDN: Observable.Finally</a>
     */
    public final Observable<T> finallyDo(Action0 action) {
        return create(OperationFinally.finallyDo(this, action));
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the source Observable,
     * or raises an {@code IllegalArgumentException} if the source Observable is empty.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/first.png">
     * 
     * @return an Observable that emits only the very first item emitted by the source Observable,
     *         or raises an {@code IllegalArgumentException} if the source Observable is empty
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-first">RxJava Wiki: first()</a>
     * @see MSDN: {@code Observable.firstAsync()}
     */
    public final Observable<T> first() {
        return take(1).single();
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the source Observable
     * that satisfies a specified condition, or raises an {@code IllegalArgumentException} if no
     * such items are emitted.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/firstN.png">
     * 
     * @param predicate
     *            the condition that an item emitted by the source Observable has to satisfy
     * @return an Observable that emits only the very first item emitted by the source Observable
     *         that satisfies the {@code predicate}, or raises an {@code IllegalArgumentException} if no such items are emitted
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-first">RxJava Wiki: first()</a>
     * @see MSDN: {@code Observable.firstAsync()}
     */
    public final Observable<T> first(Func1<? super T, Boolean> predicate) {
        return takeFirst(predicate).single();
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the source Observable,
     * or a default item if the source Observable completes without emitting anything.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/firstOrDefault.png">
     * 
     * @param defaultValue
     *            the default item to emit if the source Observable doesn't emit anything
     * @return an Observable that emits only the very first item from the source, or a default item
     *         if the source Observable completes without emitting any items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-firstordefault">RxJava Wiki: firstOrDefault()</a>
     * @see MSDN: {@code Observable.firstOrDefaultAsync()}
     */
    public final Observable<T> firstOrDefault(T defaultValue) {
        return take(1).singleOrDefault(defaultValue);
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the source Observable
     * that satisfies a specified condition, or a default item if the source Observable emits no
     * such items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/firstOrDefaultN.png">
     * 
     * @param predicate
     *            the condition any item emitted by the source Observable has to satisfy
     * @param defaultValue
     *            the default item to emit if the source Observable doesn't emit anything that
     *            satisfies the {@code predicate}
     * @return an Observable that emits only the very first item emitted by the source Observable
     *         that satisfies the {@code predicate}, or a default item if the source Observable
     *         emits no such items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-firstordefault">RxJava Wiki: firstOrDefault()</a>
     * @see MSDN: {@code Observable.firstOrDefaultAsync()}
     */
    public final Observable<T> firstOrDefault(T defaultValue, Func1<? super T, Boolean> predicate) {
        return takeFirst(predicate).singleOrDefault(defaultValue);
    }

    /**
     * Returns an Observable that emits items based on applying a function that you supply to each
     * item emitted by the source Observable, where that function returns an Observable, and then
     * merging those resulting Observables and emitting the results of this merger.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/flatMap.png">
     * <p>
     * <em>Note:</em> {@code mapMany} and {@code flatMap} are equivalent.
     * 
     * @param func
     *            a function that, when applied to an item emitted by the source Observable,
     *            returns an Observable
     * @return an Observable that emits the result of applying the transformation function to each
     *         item emitted by the source Observable and merging the results of the Observables
     *         obtained from this transformation
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-mapmany-or-flatmap-and-mapmanydelayerror">RxJava Wiki: flatMap()</a>
     * @see #mapMany(Func1)
     */
    public final <R> Observable<R> flatMap(Func1<? super T, ? extends Observable<? extends R>> func) {
        return mergeMap(func);
    }

    /**
     * Groups the items emitted by an Observable according to a specified criterion, and emits these
     * grouped items as {@link GroupedObservable}s, one {@code GroupedObservable} per group.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/groupBy.png">
     * 
     * @param keySelector
     *            a function that extracts the key for each item
     * @param <K>
     *            the key type
     * @return an Observable that emits {@link GroupedObservable}s, each of which corresponds to a
     *         unique key value and each of which emits those items from the source Observable that
     *         share that key value
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-groupby-and-groupbyuntil">RxJava Wiki: groupBy</a>
     */
    public final <K> Observable<GroupedObservable<K, T>> groupBy(final Func1<? super T, ? extends K> keySelector) {
        return lift(new OperatorGroupBy<K, T>(keySelector));
    }

    /**
     * Groups the items emitted by an Observable according to a specified criterion, and emits these
     * grouped items, transformed by a selector, within {@link GroupedObservable}s, one {@code GroupedObservable} per group.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/groupBy.png">
     * 
     * @param keySelector
     *            a function that extracts the key from an item
     * @param elementSelector
     *            a function to map a source item to an item emitted by a {@link GroupedObservable}
     * @param <K>
     *            the key type
     * @param <R>
     *            the type of items emitted by the resulting {@link GroupedObservable}s
     * @return an Observable that emits {@link GroupedObservable}s, each of which corresponds to a
     *         unique key value and emits transformed items corresponding to items from the source
     *         Observable that share that key value
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-groupby-and-groupbyuntil">RxJava Wiki: groupBy</a>
     */
    public final <K, R> Observable<GroupedObservable<K, R>> groupBy(final Func1<? super T, ? extends K> keySelector, final Func1<? super T, ? extends R> elementSelector) {
        return null;
    }

    /**
     * Groups the items emitted by an Observable according to a specified key selector function
     * until the duration Observable expires for the key.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/groupByUntil.png">
     * 
     * @param keySelector
     *            a function to extract the key for each item
     * @param durationSelector
     *            a function to signal the expiration of a group
     * @return an Observable that emits {@link GroupedObservable}s, each of which corresponds to a
     *         key value and each of which emits all items emitted by the source Observable during
     *         that key's duration that share that same key value
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-groupby-and-groupbyuntil">RxJava Wiki: groupByUntil()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211932.aspx">MSDN: Observable.GroupByUntil</a>
     */
    public final <TKey, TDuration> Observable<GroupedObservable<TKey, T>> groupByUntil(Func1<? super T, ? extends TKey> keySelector, Func1<? super GroupedObservable<TKey, T>, ? extends Observable<? extends TDuration>> durationSelector) {
        return groupByUntil(keySelector, Functions.<T> identity(), durationSelector);
    }

    /**
     * Groups the items emitted by an Observable (transformed by a selector) according to a
     * specified key selector function until the duration Observable expires for the key.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/groupByUntil.png">
     * 
     * @param keySelector
     *            a function to extract the key for each item
     * @param valueSelector
     *            a function to map each item emitted by the source Observable to an item emitted by
     *            one of the resulting {@link GroupedObservable}s
     * @param durationSelector
     *            a function to signal the expiration of a group
     * @return an Observable that emits {@link GroupedObservable}s, each of which corresponds to a
     *         key value and each of which emits all items emitted by the source Observable during
     *         that key's duration that share that same key value, transformed by the value selector
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-groupby-and-groupbyuntil">RxJava Wiki: groupByUntil()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229433.aspx">MSDN: Observable.GroupByUntil</a>
     */
    public final <TKey, TValue, TDuration> Observable<GroupedObservable<TKey, TValue>> groupByUntil(Func1<? super T, ? extends TKey> keySelector, Func1<? super T, ? extends TValue> valueSelector, Func1<? super GroupedObservable<TKey, TValue>, ? extends Observable<? extends TDuration>> durationSelector) {
        return create(new OperationGroupByUntil<T, TKey, TValue, TDuration>(this, keySelector, valueSelector, durationSelector));
    }

    /**
     * Return an Observable that correlates two Observables when they overlap in time and groups the
     * results.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/groupJoin.png">
     * 
     * @param right
     *            the other Observable to correlate items from the source Observable with
     * @param leftDuration
     *            a function that returns an Observable whose emissions indicate the duration of the
     *            values of the source Observable
     * @param rightDuration
     *            a function that returns an Observable whose emissions indicate the duration of the
     *            values of the {@code right} Observable
     * @param resultSelector
     *            a function that takes an item emitted by each Observable and returns the value to
     *            be emitted by the resulting Observable
     * @return an Observable that emits items based on combining those items emitted by the source
     *         Observables whose durations overlap
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-join-and-groupjoin">RxJava Wiiki: groupJoin</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244235.aspx">MSDN: Observable.GroupJoin</a>
     */
    public final <T2, D1, D2, R> Observable<R> groupJoin(Observable<T2> right, Func1<? super T, ? extends Observable<D1>> leftDuration,
            Func1<? super T2, ? extends Observable<D2>> rightDuration,
            Func2<? super T, ? super Observable<T2>, ? extends R> resultSelector) {
        return create(new OperationGroupJoin<T, T2, D1, D2, R>(this, right, leftDuration, rightDuration, resultSelector));
    }

    /**
     * Ignores all items emitted by the source Observable and only calls {@code onCompleted} or {@code onError}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/ignoreElements.png">
     * 
     * @return an empty Observable that only calls {@code onCompleted} or {@code onError}, based on
     *         which one is called by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-ignoreelements">RxJava Wiki: ignoreElements()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229242.aspx">MSDN: Observable.IgnoreElements</a>
     */
    public final Observable<T> ignoreElements() {
        return filter(alwaysFalse());
    }

    /**
     * Returns an Observable that emits {@code true} if the source Observable is empty, otherwise {@code false}.
     * <p>
     * In Rx.Net this is negated as the {@code any} Observer but we renamed this in RxJava to better
     * match Java naming idioms.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/isEmpty.png">
     * 
     * @return an Observable that emits a Boolean
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-exists-and-isempty">RxJava Wiki: isEmpty()</a>
     * @see <a href= "http://msdn.microsoft.com/en-us/library/hh229905.aspx">MSDN: Observable.Any</a>
     */
    public final Observable<Boolean> isEmpty() {
        return create(OperationAny.isEmpty(this));
    }

    /**
     * Correlates the items emitted by two Observables based on overlapping durations.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/join_.png">
     * 
     * @param right
     *            the second Observable to join items from
     * @param leftDurationSelector
     *            a function to select a duration for each item emitted by the source Observable,
     *            used to determine overlap
     * @param rightDurationSelector
     *            a function to select a duration for each item emitted by the {@code right} Observable, used to determine overlap
     * @param resultSelector
     *            a function that computes an item to be emitted by the resulting Observable for any
     *            two overlapping items emitted by the two Observables
     * @return an Observable that emits items correlating to items emitted by the source Observables
     *         that have overlapping durations
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-join">RxJava Wiki: join()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229750.aspx">MSDN: Observable.Join</a>
     */
    public final <TRight, TLeftDuration, TRightDuration, R> Observable<R> join(Observable<TRight> right, Func1<T, Observable<TLeftDuration>> leftDurationSelector,
            Func1<TRight, Observable<TRightDuration>> rightDurationSelector,
            Func2<T, TRight, R> resultSelector) {
        return create(new OperationJoin<T, TRight, TLeftDuration, TRightDuration, R>(this, right, leftDurationSelector, rightDurationSelector, resultSelector));
    }

    /**
     * Returns an Observable that emits the last item emitted by the source Observable or notifies
     * observers of an {@code IllegalArgumentException} if the source Observable is empty.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/last.png">
     * 
     * @return an Observable that emits the last item from the source Observable or notifies
     *         observers of an error
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observable-Operators#wiki-last">RxJava Wiki: last()</a>
     * @see MSDN: {@code Observable.lastAsync()}
     */
    public final Observable<T> last() {
        return takeLast(1).single();
    }

    /**
     * Returns an Observable that emits only the last item emitted by the source Observable that
     * satisfies a given condition, or an {@code IllegalArgumentException} if no such items are
     * emitted.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/last.p.png">
     * 
     * @param predicate
     *            the condition any source emitted item has to satisfy
     * @return an Observable that emits only the last item satisfying the given condition from the
     *         source, or an {@code IllegalArgumentException} if no such items are emitted
     * @throws IllegalArgumentException
     *             if no items that match the predicate are emitted by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observable-Operators#wiki-last">RxJava Wiki: last()</a>
     * @see MSDN: {@code Observable.lastAsync()}
     */
    public final Observable<T> last(Func1<? super T, Boolean> predicate) {
        return filter(predicate).takeLast(1).single();
    }

    /**
     * Returns an Observable that emits only the last item emitted by the source Observable, or a
     * default item if the source Observable completes without emitting any items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/lastOrDefault.png">
     * 
     * @param defaultValue
     *            the default item to emit if the source Observable is empty
     * @return an Observable that emits only the last item emitted by the source Observable, or a
     *         default item if the source Observable is empty
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-lastOrDefault">RxJava Wiki: lastOrDefault()</a>
     * @see MSDN: {@code Observable.lastOrDefaultAsync()}
     */
    public final Observable<T> lastOrDefault(T defaultValue) {
        return takeLast(1).singleOrDefault(defaultValue);
    }

    /**
     * Returns an Observable that emits only the last item emitted by the source Observable that
     * satisfies a specified condition, or a default item if no such item is emitted by the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/lastOrDefault.p.png">
     * 
     * @param defaultValue
     *            the default item to emit if the source Observable doesn't emit anything that
     *            satisfies the specified {@code predicate}
     * @param predicate
     *            the condition any item emitted by the source Observable has to satisfy
     * @return an Observable that emits only the last item emitted by the source Observable that
     *         satisfies the given condition, or a default item if no such item is emitted by the
     *         source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-lastOrDefault">RxJava Wiki: lastOrDefault()</a>
     * @see MSDN: {@code Observable.lastOrDefaultAsync()}
     */
    public final Observable<T> lastOrDefault(T defaultValue, Func1<? super T, Boolean> predicate) {
        return filter(predicate).takeLast(1).singleOrDefault(defaultValue);
    }

    /**
     * Returns an Observable that counts the total number of items emitted by the source Observable
     * and emits this count as a 64-bit Long.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/longCount.png">
     * 
     * @return an Observable that emits a single item: the number of items emitted by the source
     *         Observable as a 64-bit Long item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-count-and-longcount">RxJava Wiki: count()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229120.aspx">MSDN: Observable.LongCount</a>
     * @see #count()
     */
    public final Observable<Long> longCount() {
        return reduce(0L, new Func2<Long, T, Long>() {
            @Override
            public final Long call(Long t1, T t2) {
                return t1 + 1;
            }
        });
    }

    /**
     * Returns an Observable that applies a specified function to each item emitted by the source
     * Observable and emits the results of these function applications.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/map.png">
     * 
     * @param func
     *            a function to apply to each item emitted by the Observable
     * @return an Observable that emits the items from the source Observable, transformed by the
     *         specified function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-map">RxJava Wiki: map()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244306.aspx">MSDN: Observable.Select</a>
     */
    public final <R> Observable<R> map(Func1<? super T, ? extends R> func) {
        return lift(new OperatorMap<T, R>(func));
    }

    /**
     * Returns a new Observable by applying a function that you supply to each item emitted by the
     * source Observable, where that function returns an Observable, and then merging those
     * resulting Observables and emitting the results of this merger.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mapMany.png">
     * <p>
     * <em>Note:</em> {@code mapMany} and {@code flatMap} are equivalent.
     * 
     * @param func
     *            a function that, when applied to an item emitted by the source Observable, returns
     *            an Observable
     * @return an Observable that emits the result of applying the transformation function to each
     *         item emitted by the source Observable and merging the results of the Observables
     *         obtained from these transformations
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-mapmany-or-flatmap-and-mapmanydelayerror">RxJava Wiki: mapMany()</a>
     * @see #flatMap(Func1)
     * @deprecated use {@link #flatMap(Func1)}
     */
    @Deprecated
    public final <R> Observable<R> mapMany(Func1<? super T, ? extends Observable<? extends R>> func) {
        return mergeMap(func);
    }

    /**
     * Turns all of the emissions and notifications from a source Observable into emissions marked
     * with their original types within {@link Notification} objects.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/materialize.png">
     * 
     * @return an Observable that emits items that are the result of materializing the items and
     *         notifications of the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-materialize">RxJava Wiki: materialize()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229453.aspx">MSDN: Observable.materialize</a>
     */
    public final Observable<Notification<T>> materialize() {
        return create(OperationMaterialize.materialize(this));
    }

    /**
     * Returns an Observable that emits the maximum item emitted by the source Observable, according
     * to the specified comparator. If there is more than one item with the same maximum value, it
     * emits the last-emitted of these.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/max.png">
     * 
     * @param comparator
     *            the comparer used to compare items
     * @return an Observable that emits the maximum item emitted by the source Observable, according
     *         to the specified comparator
     * @throws IllegalArgumentException
     *             if the source is empty
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-max">RxJava Wiki: max()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211635.aspx">MSDN: Observable.Max</a>
     */
    public final Observable<T> max(Comparator<? super T> comparator) {
        return OperationMinMax.max(this, comparator);
    }

    /**
     * Returns an Observable that emits a List of items emitted by the source Observable that have
     * the maximum key value. For a source Observable that emits no items, the resulting Observable
     * emits an empty List.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/maxBy.png">
     * 
     * @param selector
     *            this function accepts an item emitted by the source Observable and returns a key
     * @return an Observable that emits a List of those items emitted by the source Observable that
     *         had the largest key value of all of the emitted items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-maxby">RxJava Wiki: maxBy()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229058.aspx">MSDN: Observable.MaxBy</a>
     */
    public final <R extends Comparable<? super R>> Observable<List<T>> maxBy(Func1<T, R> selector) {
        return OperationMinMax.maxBy(this, selector);
    }

    /**
     * Returns an Observable that emits a List of items emitted by the source Observable that have
     * the maximum key value according to a specified comparator. For a source Observable that emits
     * no items, the resulting Observable emits an empty List.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/maxBy.png">
     * 
     * @param selector
     *            this function accepts an item emitted by the source Observable and returns a key
     * @param comparator
     *            the comparator used to compare key values
     * @return an Observable that emits a List of those items emitted by the source Observable that
     *         had the largest key value of all of the emitted items according to the specified
     *         comparator
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-maxby">RxJava Wiki: maxBy()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244330.aspx">MSDN: Observable.MaxBy</a>
     */
    public final <R> Observable<List<T>> maxBy(Func1<T, R> selector, Comparator<? super R> comparator) {
        return OperationMinMax.maxBy(this, selector, comparator);
    }

    /**
     * Returns an Observable that emits the results of applying a specified function to each item
     * emitted by the source Observable, where that function returns an Observable, and then merging
     * those resulting Observables and emitting the results of this merger.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeMap.png">
     * 
     * @param func
     *            a function that, when applied to an item emitted by the source Observable, returns
     *            an Observable
     * @return an Observable that emits the result of applying the transformation function to each
     *         item emitted by the source Observable and merging the results of the Observables
     *         obtained from these transformations
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-mapmany-or-flatmap-and-mapmanydelayerror">RxJava Wiki: flatMap()</a>
     * @see #flatMap(Func1)
     */
    public final <R> Observable<R> mergeMap(Func1<? super T, ? extends Observable<? extends R>> func) {
        return merge(map(func));
    }

    /**
     * Returns an Observable that applies a function to each item emitted or notification raised by
     * the source Observable and then flattens the Observables returned from these functions and
     * emits the resulting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeMap.nce.png">
     * 
     * @param <R>
     *            the result type
     * @param onNext
     *            a function that returns an Observable to merge for each item emitted by the source
     *            Observable
     * @param onError
     *            a function that returns an Observable to merge for an onError notification from
     *            the source Observable
     * @param onCompleted
     *            a function that returns an Observable to merge for an onCompleted notification
     *            from the source Observable
     * @return an Observable that emits the results of merging the Observables returned from
     *         applying the specified functions to the emissions and notifications of the source
     *         Observable
     */
    public final <R> Observable<R> mergeMap(
            Func1<? super T, ? extends Observable<? extends R>> onNext,
            Func1<? super Throwable, ? extends Observable<? extends R>> onError,
            Func0<? extends Observable<? extends R>> onCompleted) {
        return create(OperationFlatMap.flatMap(this, onNext, onError, onCompleted));
    }

    /**
     * Returns an Observable that emits the results of a specified function to the pair of values
     * emitted by the source Observable and a specified collection Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeMap.r.png">
     * 
     * @param <U>
     *            the type of items emitted by the collection Observable
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param collectionSelector
     *            a function that returns an Observable for each item emitted by the source
     *            Observable
     * @param resultSelector
     *            a function that combines one item emitted by each of the source and collection
     *            Observables and returns an item to be emitted by the resulting Observable
     * @return an Observable that emits the results of applying a function to a pair of values
     *         emitted by the source Observable and the collection Observable
     */
    public final <U, R> Observable<R> mergeMap(Func1<? super T, ? extends Observable<? extends U>> collectionSelector,
            Func2<? super T, ? super U, ? extends R> resultSelector) {
        return create(OperationFlatMap.flatMap(this, collectionSelector, resultSelector));
    }

    /**
     * Returns an Observable that merges each item emitted by the source Observable with the values
     * in an Iterable corresponding to that item that is generated by a selector.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeMapIterable.png">
     * 
     * @param <R>
     *            the type of item emitted by the resulting Observable
     * @param collectionSelector
     *            a function that returns an Iterable sequence of values for when given an item
     *            emitted by the source Observable
     * @return an Observable that emits the results of merging the items emitted by the source
     *         Observable with the values in the Iterables corresponding to those items, as
     *         generated by {@code collectionSelector}
     */
    public final <R> Observable<R> mergeMapIterable(Func1<? super T, ? extends Iterable<? extends R>> collectionSelector) {
        return merge(map(OperationFlatMap.flatMapIterableFunc(collectionSelector)));
    }

    /**
     * Returns an Observable that emits the results of applying a function to the pair of values
     * from the source Observable and an Iterable corresponding to that item that is generated by
     * a selector.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeMapIterable.r.png">
     * 
     * @param <U>
     *            the collection element type
     * @param <R>
     *            the type of item emited by the resulting Observable
     * @param collectionSelector
     *            a function that returns an Iterable sequence of values for each item emitted by
     *            the source Observable
     * @param resultSelector
     *            a function that returns an item based on the item emitted by the source Observable
     *            and the Iterable returned for that item by the {@code collectionSelector}
     * @return an Observable that emits the items returned by {@code resultSelector} for each item
     *         in the source Observable
     */
    public final <U, R> Observable<R> mergeMapIterable(Func1<? super T, ? extends Iterable<? extends U>> collectionSelector,
            Func2<? super T, ? super U, ? extends R> resultSelector) {
        return mergeMap(OperationFlatMap.flatMapIterableFunc(collectionSelector), resultSelector);
    }

    /**
     * Returns an Observable that emits the minimum item emitted by the source Observable, according
     * to a specified comparator. If there is more than one such item, it returns the last-emitted
     * one.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/min.png">
     * 
     * @param comparator
     *            the comparer used to compare elements
     * @return an Observable that emits the minimum item emitted by the source Observable according
     *         to the specified comparator
     * @throws IllegalArgumentException
     *             if the source is empty
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-min">RxJava Wiki: min()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229095.aspx">MSDN: Observable.Min</a>
     */
    public final Observable<T> min(Comparator<? super T> comparator) {
        return OperationMinMax.min(this, comparator);
    }

    /**
     * Returns an Observable that emits a List of items emitted by the source Observable that have
     * the minimum key value. For a source Observable that emits no items, the resulting Observable
     * emits an empty List.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/minBy.png">
     * 
     * @param selector
     *            the key selector function
     * @return an Observable that emits a List of all of the items from the source Observable that
     *         had the lowest key value of any items emitted by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-minby">RxJava Wiki: minBy()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228970.aspx">MSDN: Observable.MinBy</a>
     */
    public final <R extends Comparable<? super R>> Observable<List<T>> minBy(Func1<T, R> selector) {
        return OperationMinMax.minBy(this, selector);
    }

    /**
     * Returns an Observable that emits a List of items emitted by the source Observable that have
     * the minimum key value according to a given comparator function. For a source Observable that
     * emits no items, the resulting Observable emits an empty List.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/minBy.png">
     * 
     * @param selector
     *            the key selector function
     * @param comparator
     *            the comparator used to compare key values
     * @return an Observable that emits a List of all of the items from the source Observable that
     *         had the lowest key value of any items emitted by the source Observable according to
     *         the specified comparator
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-minby">RxJava Wiki: minBy()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228970.aspx">MSDN: Observable.MinBy</a>
     */
    public final <R> Observable<List<T>> minBy(Func1<T, R> selector, Comparator<? super R> comparator) {
        return OperationMinMax.minBy(this, selector, comparator);
    }

    /**
     * Returns an Observable that emits items produced by multicasting the source Observable within
     * a selector function.
     * 
     * @param subjectFactory
     *            the {@link Subject} factory
     * @param selector
     *            the selector function, which can use the multicasted source Observable subject to
     *            the policies enforced by the created {@code Subject}
     * @return an Observable that emits the items produced by multicasting the source Observable
     *         within a selector function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablepublish-and-observablemulticast">RxJava: Observable.publish() and Observable.multicast()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229708.aspx">MSDN: Observable.Multicast</a>
     */
    public final <TIntermediate, TResult> Observable<TResult> multicast(
            final Func0<? extends Subject<? super T, ? extends TIntermediate>> subjectFactory,
            final Func1<? super Observable<TIntermediate>, ? extends Observable<TResult>> selector) {
        return OperationMulticast.multicast(this, subjectFactory, selector);
    }

    /**
     * Returns a {@link ConnectableObservable} that upon connection causes the source Observable to
     * push results into the specified subject.
     * 
     * @param subject
     *            the {@link Subject} for the {@link ConnectableObservable} to push source items
     *            into
     * @param <R>
     *            the type of items emitted by the resulting {@code ConnectableObservable}
     * @return a {@link ConnectableObservable} that upon connection causes the source Observable to
     *         push results into the specified {@link Subject}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablepublish-and-observablemulticast">RxJava Wiki: Observable.publish() and Observable.multicast()</a>
     */
    public final <R> ConnectableObservable<R> multicast(Subject<? super T, ? extends R> subject) {
        return OperationMulticast.multicast(this, subject);
    }

    /**
     * Modify the source Observable so that it asynchronously notifies {@link Observer}s on the
     * specified {@link Scheduler}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/observeOn.png">
     * 
     * @param scheduler
     *            the {@link Scheduler} to notify {@link Observer}s on
     * @return the source Observable modified so that its {@link Observer}s are notified on the
     *         specified {@link Scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-observeon">RxJava Wiki: observeOn()</a>
     */
    public final Observable<T> observeOn(Scheduler scheduler) {
        return create(OperationObserveOn.observeOn(this, scheduler));
    }

    /**
     * Filters the items emitted by an Observable, only emitting those of the specified type.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/ofClass.png">
     * 
     * @param klass
     *            the class type to filter the items emitted by the source Observable
     * @return an Observable that emits items from the source Observable of type {@code klass}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-oftype">RxJava Wiki: ofType()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229380.aspx">MSDN: Observable.OfType</a>
     */
    public final <R> Observable<R> ofType(final Class<R> klass) {
        return filter(new Func1<T, Boolean>() {
            public final Boolean call(T t) {
                return klass.isInstance(t);
            }
        }).cast(klass);
    }

    /**
     * Instruct an Observable to pass control to another Observable rather than invoking {@link Observer#onError onError} if it encounters an error.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/onErrorResumeNext.png">
     * <p>
     * By default, when an Observable encounters an error that prevents it from emitting the
     * expected item to its {@link Observer}, the Observable invokes its Observer's {@code onError} method, and then quits without invoking any more of its Observer's methods. The
     * {@code onErrorResumeNext} method changes this behavior. If you pass a function that returns
     * an Observable ({@code resumeFunction}) to {@code onErrorResumeNext}, if the original
     * Observable encounters an error, instead of invoking its Observer's {@code onError} method, it
     * will instead relinquish control to the Observable returned from {@code resumeFunction}, which
     * will invoke the Observer's {@link Observer#onNext onNext} method if it is able to do so. In
     * such a case, because no Observable necessarily invokes {@code onError}, the Observer may
     * never know that an error happened.
     * <p>
     * You can use this to prevent errors from propagating or to supply fallback data should errors
     * be encountered.
     * 
     * @param resumeFunction
     *            a function that returns an Observable that will take over if the source Observable
     *            encounters an error
     * @return the original Observable, with appropriately modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#wiki-onerrorresumenext">RxJava Wiki: onErrorResumeNext()</a>
     */
    public final Observable<T> onErrorResumeNext(final Func1<Throwable, ? extends Observable<? extends T>> resumeFunction) {
        return create(OperationOnErrorResumeNextViaFunction.onErrorResumeNextViaFunction(this, resumeFunction));
    }

    /**
     * Instruct an Observable to pass control to another Observable rather than invoking {@link Observer#onError onError} if it encounters an error.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/onErrorResumeNext.png">
     * <p>
     * By default, when an Observable encounters an error that prevents it from emitting the
     * expected item to its {@link Observer}, the Observable invokes its Observer's {@code onError} method, and then quits without invoking any more of its Observer's methods. The
     * {@code onErrorResumeNext} method changes this behavior. If you pass another Observable
     * ({@code resumeSequence}) to an Observable's {@code onErrorResumeNext} method, if the original
     * Observable encounters an error, instead of invoking its Observer's {@code onError} method, it
     * will instead relinquish control to {@code resumeSequence} which will invoke the Observer's {@link Observer#onNext onNext} method if it is able to do so. In such a case, because no
     * Observable necessarily invokes {@code onError}, the Observer may never know that an error
     * happened.
     * <p>
     * You can use this to prevent errors from propagating or to supply fallback data should errors
     * be encountered.
     * 
     * @param resumeSequence
     *            a function that returns an Observable that will take over if the source Observable
     *            encounters an error
     * @return the original Observable, with appropriately modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#wiki-onerrorresumenext">RxJava Wiki: onErrorResumeNext()</a>
     */
    public final Observable<T> onErrorResumeNext(final Observable<? extends T> resumeSequence) {
        return create(OperationOnErrorResumeNextViaObservable.onErrorResumeNextViaObservable(this, resumeSequence));
    }

    /**
     * Instruct an Observable to emit an item (returned by a specified function) rather than
     * invoking {@link Observer#onError onError} if it encounters an error.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/onErrorReturn.png">
     * <p>
     * By default, when an Observable encounters an error that prevents it from emitting the
     * expected item to its {@link Observer}, the Observable invokes its Observer's {@code onError} method, and then quits without invoking any more of its Observer's methods. The
     * {@code onErrorReturn} method changes this behavior. If you pass a function
     * ({@code resumeFunction}) to an Observable's {@code onErrorReturn} method, if the original
     * Observable encounters an error, instead of invoking its Observer's {@code onError} method, it
     * will instead emit the return value of {@code resumeFunction}.
     * <p>
     * You can use this to prevent errors from propagating or to supply fallback data should errors
     * be encountered.
     * 
     * @param resumeFunction
     *            a function that returns an item that the new Observable will emit if the source
     *            Observable encounters an error
     * @return the original Observable with appropriately modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#wiki-onerrorreturn">RxJava Wiki: onErrorReturn()</a>
     */
    public final Observable<T> onErrorReturn(Func1<Throwable, ? extends T> resumeFunction) {
        return create(OperationOnErrorReturn.onErrorReturn(this, resumeFunction));
    }

    /**
     * Instruct an Observable to pass control to another Observable rather than invoking {@link Observer#onError onError} if it encounters an {@link java.lang.Exception}.
     * <p>
     * This differs from {@link #onErrorResumeNext} in that this one does not handle {@link java.lang.Throwable} or {@link java.lang.Error} but lets those continue through.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/onExceptionResumeNextViaObservable.png">
     * <p>
     * By default, when an Observable encounters an exception that prevents it from emitting the
     * expected item to its {@link Observer}, the Observable invokes its Observer's {@code onError} method, and then quits without invoking any more of its Observer's methods. The
     * {@code onExceptionResumeNext} method changes this behavior. If you pass another Observable
     * ({@code resumeSequence}) to an Observable's {@code onExceptionResumeNext} method, if the
     * original Observable encounters an exception, instead of invoking its Observer's {@code onError} method, it will instead relinquish control to {@code resumeSequence} which
     * will invoke the Observer's {@link Observer#onNext onNext} method if it is able to do so. In
     * such a case, because no Observable necessarily invokes {@code onError}, the Observer may
     * never know that an exception happened.
     * <p>
     * You can use this to prevent exceptions from propagating or to supply fallback data should
     * exceptions be encountered.
     * 
     * @param resumeSequence
     *            a function that returns an Observable that will take over if the source Observable
     *            encounters an exception
     * @return the original Observable, with appropriately modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#wiki-onexceptionresumenext">RxJava Wiki: onExceptionResumeNext()</a>
     */
    public final Observable<T> onExceptionResumeNext(final Observable<? extends T> resumeSequence) {
        return create(OperationOnExceptionResumeNextViaObservable.onExceptionResumeNextViaObservable(this, resumeSequence));
    }

    /**
     * Perform work on the source {@code Observable<T>} in parallel by sharding it on a {@link Schedulers#computation()} {@link Scheduler}, and return the resulting {@code Observable<R>}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/parallel.png">
     * 
     * @param f
     *            a {@link Func1} that applies Observable Observers to {@code Observable<T>} in
     *            parallel and returns an {@code Observable<R>}
     * @return an Observable that emits the results of applying {@link Func1} to the items emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-parallel">RxJava Wiki: parallel()</a>
     */
    public final <R> Observable<R> parallel(Func1<Observable<T>, Observable<R>> f) {
        return lift(new OperatorParallel<T, R>(f, Schedulers.computation()));
    }

    /**
     * Perform work on the source {@code Observable<T>} in parallel by sharding it on a {@link Scheduler}, and return the resulting {@code Observable<R>}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/parallel.png">
     * 
     * @param f
     *            a {@link Func1} that applies Observable Observers to {@code Observable<T>} in
     *            parallel and returns an {@code Observable<R>}
     * @param s
     *            a {@link Scheduler} to perform the work on
     * @return an Observable that emits the results of applying {@link Func1} to the items emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-parallel">RxJava Wiki: parallel()</a>
     */
    public final <R> Observable<R> parallel(final Func1<Observable<T>, Observable<R>> f, final Scheduler s) {
        return lift(new OperatorParallel<T, R>(f, s));
    }

    /**
     * Protects against errors being thrown from Observer implementations and ensures
     * onNext/onError/onCompleted contract compliance.
     * <p>
     * See https://github.com/Netflix/RxJava/issues/216 for a discussion on "Guideline 6.4: Protect
     * calls to user code from within an Observer"
     */
    private Subscription protectivelyWrapAndSubscribe(Subscriber<? super T> o) {
        return subscribe(new SafeSubscriber<T>(o));
    }

    /**
     * Returns a {@link ConnectableObservable}, which waits until its {@link ConnectableObservable#connect connect} method is called before it begins emitting
     * items to those {@link Observer}s that have subscribed to it.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/publishConnect.png">
     * 
     * @return a {@link ConnectableObservable} that upon connection causes the source Observable to
     *         emit items to its {@link Observer}s
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablepublish-and-observablemulticast">RxJava Wiki: publish()</a>
     */
    public final ConnectableObservable<T> publish() {
        return OperationMulticast.multicast(this, PublishSubject.<T> create());
    }

    /**
     * Returns an Observable that emits the results of invoking a specified selector on items
     * emitted by a {@link ConnectableObservable} that shares a single subscription to the
     * underlying sequence.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/publishConnect.f.png">
     * 
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param selector
     *            a function that can use the multicasted source sequence as many times as needed,
     *            without causing multiple subscriptions to the source sequence. Subscribers to the
     *            given source will receive all notifications of the source from the time of the
     *            subscription forward.
     * @return an Observable that emits the results of invoking the selector on the items emitted by
     *         a {@link ConnectableObservable} that shares a single subscription to the underlying
     *         sequence
     */
    public final <R> Observable<R> publish(Func1<? super Observable<T>, ? extends Observable<R>> selector) {
        return multicast(new Func0<Subject<T, T>>() {
            @Override
            public final Subject<T, T> call() {
                return PublishSubject.create();
            }
        }, selector);
    }

    /**
     * Returns an Observable that emits the results of invoking a specified selector on items
     * emitted by a {@link ConnectableObservable} that shares a single subscription to the
     * underlying Observable that shares a single subscription to the underlying sequence and starts
     * with {@code initialValue}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/publishConnect.if.png">
     * 
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param selector
     *            a function that can use the multicasted source sequence as many times as needed,
     *            without causing multiple subscriptions to the source Observable. Subscribers to
     *            the source will receive all notifications of the source from the time of the
     *            subscription forward
     * @param initialValue
     *            the initial value of the underlying {@link BehaviorSubject}
     * @return an Observable that emits {@code initialValue} followed by the results of invoking the
     *         selector on a {@ConnectableObservable} that shares a single subscription to the
     *         underlying sequence
     */
    public final <R> Observable<R> publish(Func1<? super Observable<T>, ? extends Observable<R>> selector, final T initialValue) {
        return multicast(new Func0<Subject<T, T>>() {
            @Override
            public final Subject<T, T> call() {
                return BehaviorSubject.create(initialValue);
            }
        }, selector);
    }

    /**
     * Returns a {@link ConnectableObservable} that shares a single subscription to the underlying
     * Observable and starts with {@code initialValue}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/publishConnect.i.png">
     * 
     * @param initialValue
     *            the initial value of the underlying {@link BehaviorSubject}
     * @return a {@link ConnectableObservable} that shares a single subscription to the underlying
     *         Observable and starts with {@code initialValue}
     */
    public final ConnectableObservable<T> publish(T initialValue) {
        return OperationMulticast.multicast(this, BehaviorSubject.<T> create(initialValue));
    }

    /**
     * Returns a {@link ConnectableObservable} that emits only the last item emitted by the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/publishLast.png">
     * 
     * @return a {@link ConnectableObservable} that emits only the last item emitted by the source
     *         Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablepublishlast">RxJava Wiki: publishLast()</a>
     */
    public final ConnectableObservable<T> publishLast() {
        return OperationMulticast.multicast(this, AsyncSubject.<T> create());
    }

    /**
     * Retusna an Observable that emits items that are results of invoking a specified selector on
     * items emitted by a {@link ConnectableObservable} that shares a single subscription to the
     * underlying sequence but contains only its last emission.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/publishLast.f.png">
     * 
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param selector
     *            a function that can use the multicasted source sequence as many times as needed,
     *            without causing multiple subscriptions to the source Observable. Subscribers to
     *            the source will only receive the last item emitted by the source.
     * @return an Observable that emits items that are the result of invoking the selector on a {@link ConnectableObservable} that shares a single subscription to the underlying
     *         Observable but contains only its last emission.
     */
    public final <R> Observable<R> publishLast(Func1<? super Observable<T>, ? extends Observable<R>> selector) {
        return multicast(new Func0<Subject<T, T>>() {
            @Override
            public final Subject<T, T> call() {
                return AsyncSubject.create();
            }
        }, selector);
    }

    /**
     * Returns an Observable that applies a function of your choosing to the first item emitted by
     * a source Observable, then feeds the result of that function along with the second item
     * emitted by the source Observable into the same function, and so on until all items have been
     * emitted by the source Observable, and emits the final result from the final call to your
     * function as its sole item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/reduce.png">
     * <p>
     * This technique, which is called "reduce" here, is sometimes called "aggregate," "fold,"
     * "accumulate," "compress," or "inject" in other programming contexts. Groovy, for instance,
     * has an {@code inject()} method that does a similar operation on lists.
     * 
     * @param accumulator
     *            an accumulator function to be invoked on each item emitted by the source
     *            Observable, whose result will be used in the next accumulator call
     * @return an Observable that emits a single item that is the result of accumulating the items
     *         emitted by the source Observable
     * @throws IllegalArgumentException
     *             if the source Observable emits no items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-reduce">RxJava Wiki: reduce()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229154.aspx">MSDN: Observable.Aggregate</a>
     * @see <a href="http://en.wikipedia.org/wiki/Fold_(higher-order_function)">Wikipedia: Fold (higher-order function)</a>
     */
    public final Observable<T> reduce(Func2<T, T, T> accumulator) {
        /*
         * Discussion and confirmation of implementation at
         * https://github.com/Netflix/RxJava/issues/423#issuecomment-27642532
         * 
         * It should use last() not takeLast(1) since it needs to emit an error if the sequence is
         * empty.
         */
        return create(OperationScan.scan(this, accumulator)).last();
    }

    /**
     * Returns an Observable that applies a function of your choosing to the first item emitted by a
     * source Observable and a specified seed value, then feeds the result of that function along
     * with the second item emitted by an Observable into the same function, and so on until all
     * items have been emitted by the source Observable, emitting the final result from the final
     * call to your function as its sole item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/reduceSeed.png">
     * <p>
     * This technique, which is called "reduce" here, is sometimec called "aggregate," "fold,"
     * "accumulate," "compress," or "inject" in other programming contexts. Groovy, for instance,
     * has an {@code inject()} method that does a similar operation on lists.
     * 
     * @param initialValue
     *            the initial (seed) accumulator value
     * @param accumulator
     *            an accumulator function to be invoked on each item emitted by the source
     *            Observable, the result of which will be used in the next accumulator call
     * @return an Observable that emits a single item that is the result of accumulating the output
     *         from the items emitted by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-reduce">RxJava Wiki: reduce()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229154.aspx">MSDN: Observable.Aggregate</a>
     * @see <a href="http://en.wikipedia.org/wiki/Fold_(higher-order_function)">Wikipedia: Fold (higher-order function)</a>
     */
    public final <R> Observable<R> reduce(R initialValue, Func2<R, ? super T, R> accumulator) {
        return create(OperationScan.scan(this, initialValue, accumulator)).takeLast(1);
    }

    /**
     * Returns an Observable that repeats the sequence of items emitted by the source Observable
     * indefinitely.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/repeat.png">
     * 
     * @return an Observable that emits the items emitted by the source Observable repeatedly and in
     *         sequence
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-repeat">RxJava Wiki: repeat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229428.aspx">MSDN: Observable.Repeat</a>
     */
    public final Observable<T> repeat() {
        return this.repeat(Schedulers.currentThread());
    }

    /**
     * Returns an Observable that repeats the sequence of items emitted by the source Observable
     * indefinitely, on a particular scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/repeat.s.png">
     * 
     * @param scheduler
     *            the scheduler to emit the items on
     * @return an Observable that emits the items emitted by the source Observable repeatedly and in
     *         sequence
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#wiki-repeat">RxJava Wiki: repeat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229428.aspx">MSDN: Observable.Repeat</a>
     */
    public final Observable<T> repeat(Scheduler scheduler) {
        return create(OperationRepeat.repeat(this, scheduler));
    }

    /**
     * Returns a {@link ConnectableObservable} that shares a single subscription to the underlying
     * Observable that will replay all of its items and notifications to any future {@link Observer}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.png">
     * 
     * @return a {@link ConnectableObservable} that upon connection causes the source Observable to
     *         emit its items to its {@link Observer}s
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     */
    public final ConnectableObservable<T> replay() {
        return OperationMulticast.multicast(this, ReplaySubject.<T> create());
    }

    /**
     * Returns an Observable that emits items that are the results of invoking a specified selector
     * on the items emitted by a {@link ConnectableObservable} that shares a single subscription to
     * the underlying Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.f.png">
     * 
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param selector
     *            the selector function, which can use the multicasted sequence as many times as
     *            needed, without causing multiple subscriptions to the Observable
     * @return an Observable that emits items that are the results of invoking the selector on a {@link ConnectableObservable} that shares a single subscription to the underlying
     *         Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229653.aspx">MSDN: Observable.Replay</a>
     */
    public final <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector) {
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public final Subject<T, T> call() {
                return ReplaySubject.create();
            }
        }, selector);
    }

    /**
     * Returns an Observable that emits items that are the results of invoking a specified selector
     * on items emitted by a {@link ConnectableObservable} that shares a single subscription to the
     * underlying Observable, replaying {@code bufferSize} notifications.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.fn.png">
     * 
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param selector
     *            the selector function, which can use the multicasted sequence as many times as
     *            needed, without causing multiple subscriptions to the Observable
     * @param bufferSize
     *            the buffer size that limits the number of items the connectable observable can
     *            replay
     * @return an Observable that emits items that are the results of invoking the selector on
     *         items emitted by a {@link ConnectableObservable} that shares a single subscription to
     *         the underlying Observable replaying no more than {@code bufferSize} items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211675.aspx">MSDN: Observable.Replay</a>
     */
    public final <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, final int bufferSize) {
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public final Subject<T, T> call() {
                return OperationReplay.replayBuffered(bufferSize);
            }
        }, selector);
    }

    /**
     * Returns an Observable that emits items that are the results of invoking a specified selector
     * on items emitted by a {@link ConnectableObservable} that shares a single subscription to the
     * underlying Observable, replaying no more than {@code bufferSize} items that were emitted
     * within a specified time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.fnt.png">
     * 
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param selector
     *            a selector function, which can use the multicasted sequence as many times as
     *            needed, without causing multiple subscriptions to the Observable
     * @param bufferSize
     *            the buffer size that limits the number of items the connectable observable can
     *            replay
     * @param time
     *            the duration of the window in which the replayed items must have been emitted
     * @param unit
     *            the time unit of {@code time}
     * @return an Observable that emits items that are the results of invoking the selector on items
     *         emitted by a {@link ConnectableObservable} that shares a single subscription to the
     *         underlying Observable, and replay no more than {@code bufferSize} items that were
     *         emitted within the window defined by {@code time}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228952.aspx">MSDN: Observable.Replay</a>
     */
    public final <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, int bufferSize, long time, TimeUnit unit) {
        return replay(selector, bufferSize, time, unit, Schedulers.computation());
    }

    /**
     * Returns an Observable that emits items that are the results of invoking a specified selector
     * on items emitted by a {@link ConnectableObservable} that shares a single subscription to the
     * underlying Observable, replaying no more than {@code bufferSize} items that were emitted
     * within a specified time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.fnts.png">
     * 
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param selector
     *            a selector function, which can use the multicasted sequence as many times as
     *            needed, without causing multiple subscriptions to the Observable
     * @param bufferSize
     *            the buffer size that limits the number of items the connectable observable can
     *            replay
     * @param time
     *            the duration of the window in which the replayed items must have been emitted
     * @param unit
     *            the time unit of {@code time}
     * @param scheduler
     *            the scheduler that is the time source for the window
     * @return an Observable that emits items that are the results of invoking the selector on items
     *         emitted by a {@link ConnectableObservable} that shares a single subscription to the
     *         underlying Observable, and replay no more than {@code bufferSize} items that were
     *         emitted within the window defined by {@code time}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229404.aspx">MSDN: Observable.Replay</a>
     */
    public final <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, final int bufferSize, final long time, final TimeUnit unit, final Scheduler scheduler) {
        if (bufferSize < 0) {
            throw new IllegalArgumentException("bufferSize < 0");
        }
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public final Subject<T, T> call() {
                return OperationReplay.replayWindowed(time, unit, bufferSize, scheduler);
            }
        }, selector);
    }

    /**
     * Returns an Observable that emits items that are the results of invoking a specified selector
     * on items emitted by a {@link ConnectableObservable} that shares a single subscription to the
     * underlying Observable, replaying a maximum of {@code bufferSize} items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.fns.png">
     * 
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param selector
     *            a selector function, which can use the multicasted sequence as many times as
     *            needed, without causing multiple subscriptions to the Observable
     * @param bufferSize
     *            the buffer size that limits the number of items the connectable observable can
     *            replay
     * @param scheduler
     *            the scheduler on which the replay is observed
     * @return an Observable that emits items that are the results of invoking the selector on items
     *         emitted by a {@link ConnectableObservable} that shares a single subscription to the
     *         underlying Observable, replaying no more than {@code bufferSize} notifications
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229928.aspx">MSDN: Observable.Replay</a>
     */
    public final <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, final int bufferSize, final Scheduler scheduler) {
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public final Subject<T, T> call() {
                return OperationReplay.<T> createScheduledSubject(OperationReplay.<T> replayBuffered(bufferSize), scheduler);
            }
        }, selector);
    }

    /**
     * Returns an Observable that emits items that are the results of invoking a specified selector
     * on items emitted by a {@link ConnectableObservable} that shares a single subscription to the
     * underlying Observable, replaying all items that were emitted within a specified time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.ft.png">
     * 
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param selector
     *            a selector function, which can use the multicasted sequence as many times as
     *            needed, without causing multiple subscriptions to the Observable
     * @param time
     *            the duration of the window in which the replayed items must have been emitted
     * @param unit
     *            the time unit of {@code time}
     * @return an Observable that emits items that are the results of invoking the selector on items
     *         emitted by a {@link ConnectableObservable} that shares a single subscription to the
     *         underlying Observable, replaying all items that were emitted within the window
     *         defined by {@code time}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229526.aspx">MSDN: Observable.Replay</a>
     */
    public final <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, long time, TimeUnit unit) {
        return replay(selector, time, unit, Schedulers.computation());
    }

    /**
     * Returns an Observable that emits items that are the results of invoking a specified selector
     * on items emitted by a {@link ConnectableObservable} that shares a single subscription to the
     * underlying Observable, replaying all items that were emitted within a specified time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.fts.png">
     * 
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param selector
     *            a selector function, which can use the multicasted sequence as many times as
     *            needed, without causing multiple subscriptions to the Observable
     * @param time
     *            the duration of the window in which the replayed items must have been emitted
     * @param unit
     *            the time unit of {@code time}
     * @param scheduler
     *            the scheduler that is the time source for the window
     * @return an Observable that emits items that are the results of invoking the selector on items
     *         emitted by a {@link ConnectableObservable} that shares a single subscription to the
     *         underlying Observable, replaying all items that were emitted within the window
     *         defined by {@code time}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244327.aspx">MSDN: Observable.Replay</a>
     */
    public final <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, final long time, final TimeUnit unit, final Scheduler scheduler) {
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public final Subject<T, T> call() {
                return OperationReplay.replayWindowed(time, unit, -1, scheduler);
            }
        }, selector);
    }

    /**
     * Returns an Observable that emits items that are the results of invoking a specified selector
     * on items emitted by a {@link ConnectableObservable} that shares a single subscription to the
     * underlying Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.fs.png">
     * 
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param selector
     *            a selector function, which can use the multicasted sequence as many times as
     *            needed, without causing multiple subscriptions to the Observable
     * @param scheduler
     *            the scheduler where the replay is observed
     * @return an Observable that emits items that are the results of invoking the selector on items
     *         emitted by a {@link ConnectableObservable} that shares a single subscription to the
     *         underlying Observable, replaying all items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211644.aspx">MSDN: Observable.Replay</a>
     */
    public final <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, final Scheduler scheduler) {
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public final Subject<T, T> call() {
                return OperationReplay.createScheduledSubject(ReplaySubject.<T> create(), scheduler);
            }
        }, selector);
    }

    /**
     * Returns a {@link ConnectableObservable} that shares a single subscription to the source
     * Observable that replays at most {@code bufferSize} items emitted by that Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.n.png">
     * 
     * @param bufferSize
     *            the buffer size that limits the number of items that can be replayed
     * @return a {@link ConnectableObservable} that shares a single subscription to the source
     *         Observable and replays at most {@code bufferSize} items emitted by that Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211976.aspx">MSDN: Observable.Replay</a>
     */
    public final ConnectableObservable<T> replay(int bufferSize) {
        return OperationMulticast.multicast(this, OperationReplay.<T> replayBuffered(bufferSize));
    }

    /**
     * Returns a {@link ConnectableObservable} that shares a single subscription to the underlying
     * Observable and replays at most {@code bufferSize} items that were emitted during a specified
     * time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.nt.png">
     * 
     * @param bufferSize
     *            the buffer size that limits the number of items that can be replayed
     * @param time
     *            the duration of the window in which the replayed items must have been emitted
     * @param unit
     *            the time unit of {@code time}
     * @return a {@link ConnectableObservable} that shares a single subscription to the underlying
     *         Observable and replays at most {@code bufferSize} items that were emitted during the
     *         window defined by {@code time}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229874.aspx">MSDN: Observable.Replay</a>
     */
    public final ConnectableObservable<T> replay(int bufferSize, long time, TimeUnit unit) {
        return replay(bufferSize, time, unit, Schedulers.computation());
    }

    /**
     * Returns a {@link ConnectableObservable} that shares a single subscription to the underlying
     * Observable and that replays a maximum of {@code bufferSize} items that are emitted within
     * a specified time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.nts.png">
     * 
     * @param bufferSize
     *            the buffer size that limits the number of items that can be replayed
     * @param time
     *            the duration of the window in which the replayed items must have been emitted
     * @param unit
     *            the time unit of {@code time}
     * @param scheduler
     *            the scheduler that is used as a time source for the window
     * @return a {@link ConnectableObservable} that shares a single subscription to the underlying
     *         Observable and replays at most {@code bufferSize} items that were emitted during the
     *         window defined by {@code time}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211759.aspx">MSDN: Observable.Replay</a>
     */
    public final ConnectableObservable<T> replay(int bufferSize, long time, TimeUnit unit, Scheduler scheduler) {
        if (bufferSize < 0) {
            throw new IllegalArgumentException("bufferSize < 0");
        }
        return OperationMulticast.multicast(this, OperationReplay.<T> replayWindowed(time, unit, bufferSize, scheduler));
    }

    /**
     * Returns a {@link ConnectableObservable} that shares a single subscription to the source
     * Observable and replays at most {@code bufferSize} items emitted by that Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.ns.png">
     * 
     * @param bufferSize
     *            the buffer size that limits the number of items that can be replayed
     * @param scheduler
     *            the scheduler on which the Observers will observe the emitted items
     * @return a {@link ConnectableObservable} that shares a single subscription to the underlying
     *         Observable and replays at most {@code bufferSize} items that were emitted by the
     *         Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229814.aspx">MSDN: Observable.Replay</a>
     */
    public final ConnectableObservable<T> replay(int bufferSize, Scheduler scheduler) {
        return OperationMulticast.multicast(this,
                OperationReplay.createScheduledSubject(
                        OperationReplay.<T> replayBuffered(bufferSize), scheduler));
    }

    /**
     * Returns a {@link ConnectableObservable} that shares a single subscription to the source
     * Observable and replays all items emitted by that Observable within a specified time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.t.png">
     * 
     * @param time
     *            the duration of the window in which the replayed items must have been emitted
     * @param unit
     *            the time unit of {@code time}
     * @return a {@link ConnectableObservable} that shares a single subscription to the underlying
     *         Observable and replays the items that were emitted during the window defined by {@code time}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229232.aspx">MSDN: Observable.Replay</a>
     */
    public final ConnectableObservable<T> replay(long time, TimeUnit unit) {
        return replay(time, unit, Schedulers.computation());
    }

    /**
     * Returns a {@link ConnectableObservable} that shares a single subscription to the source
     * Observable and replays all items emitted by that Observable within a specified time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.ts.png">
     * 
     * @param time
     *            the duration of the window in which the replayed items must have been emitted
     * @param unit
     *            the time unit of {@code time}
     * @param scheduler
     *            the scheduler that is the time source for the window
     * @return a {@link ConnectableObservable} that shares a single subscription to the underlying
     *         Observable and replays the items that were emitted during the window defined by {@code time}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211811.aspx">MSDN: Observable.Replay</a>
     */
    public final ConnectableObservable<T> replay(long time, TimeUnit unit, Scheduler scheduler) {
        return OperationMulticast.multicast(this, OperationReplay.<T> replayWindowed(time, unit, -1, scheduler));
    }

    /**
     * Returns a {@link ConnectableObservable} that shares a single subscription to the underlying
     * Observable that will replay all of its items and notifications to any future {@link Observer} on the given {@link Scheduler}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.s.png">
     * 
     * @param scheduler
     *            the scheduler on which the Observers will observe the emitted items
     * @return a {@link ConnectableObservable} that shares a single subscription to the source
     *         Observable that will replay all of its items and notifications to any future {@link Observer} on the given {@link Scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#wiki-observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211699.aspx">MSDN: Observable.Replay</a>
     */
    public final ConnectableObservable<T> replay(Scheduler scheduler) {
        return OperationMulticast.multicast(this, OperationReplay.createScheduledSubject(ReplaySubject.<T> create(), scheduler));
    }

    /**
     * Return an Observable that mirrors the source Observable, resubscribing to it if it calls {@code onError} (infinite retry count).
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/retry.png">
     * <p>
     * If the source Observable calls {@link Observer#onError}, this method will resubscribe to the
     * source Observable.
     * <p>
     * Any and all items emitted by the source Observable will be emitted by the resulting
     * Observable, even those emitted during failed subscriptions. For example, if an Observable
     * fails at first but emits {@code [1, 2]} then succeeds the second time and emits {@code [1, 2, 3, 4, 5]} then the complete sequence of emissions and notifications would be
     * {@code [1, 2, 1, 2, 3, 4, 5, onCompleted]}.
     * 
     * @return the source Observable modified with retry logic
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#wiki-retry">RxJava Wiki: retry()</a>
     */
    public final Observable<T> retry() {
        return create(OperationRetry.retry(this));
    }

    /**
     * Return an Observable that mirrors the source Observable, resubscribing to it if it calls {@code onError} up to a certain number of retries.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/retry.png">
     * <p>
     * If the source Observable calls {@link Observer#onError}, this method will resubscribe to the
     * source Observable for a maximum of {@code retryCount} resubscriptions.
     * <p>
     * Any and all items emitted by the source Observable will be emitted by the resulting
     * Observable, even those emitted during failed subscriptions. For example, if an Observable
     * fails at first but emits {@code [1, 2]} then succeeds the second time and emits {@code [1, 2, 3, 4, 5]} then the complete sequence of emissions and notifications would be
     * {@code [1, 2, 1, 2, 3, 4, 5, onCompleted]}.
     * 
     * @param retryCount
     *            number of retry attempts before failing
     * @return the source Observable modified with retry logic
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#wiki-retry">RxJava Wiki: retry()</a>
     */
    public final Observable<T> retry(int retryCount) {
        return create(OperationRetry.retry(this, retryCount));
    }

    /**
     * Returns an Observable that emits the results of sampling the items emitted by the source
     * Observable at a specified time interval.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sample.png">
     * 
     * @param period
     *            the sampling rate
     * @param unit
     *            the {@link TimeUnit} in which {@code period} is defined
     * @return an Observable that emits the results of sampling the items emitted by the source
     *         Observable at the specified time interval
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-sample-or-throttlelast">RxJava Wiki: sample()</a>
     */
    public final Observable<T> sample(long period, TimeUnit unit) {
        return create(OperationSample.sample(this, period, unit));
    }

    /**
     * Returns an Observable that emits the results of sampling the items emitted by the source
     * Observable at a specified time interval.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sample.s.png">
     * 
     * @param period
     *            the sampling rate
     * @param unit
     *            the {@link TimeUnit} in which {@code period} is defined
     * @param scheduler
     *            the {@link Scheduler} to use when sampling
     * @return an Observable that emits the results of sampling the items emitted by the source
     *         Observable at the specified time interval
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-sample-or-throttlelast">RxJava Wiki: sample()</a>
     */
    public final Observable<T> sample(long period, TimeUnit unit, Scheduler scheduler) {
        return create(OperationSample.sample(this, period, unit, scheduler));
    }

    /**
     * Return an Observable that emits the results of sampling the items emitted by the source
     * Observable whenever the specified {@code sampler} Observable emits an item or completes.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sample.o.png">
     * 
     * @param sampler
     *            the Observable to use for sampling the source Observable
     * @return an Observable that emits the results of sampling the items emitted by this Observable
     *         whenever the {@code sampler} Observable emits an item or completes
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-sample-or-throttlelast">RxJava Wiki: sample()</a>
     */
    public final <U> Observable<T> sample(Observable<U> sampler) {
        return create(new OperationSample.SampleWithObservable<T, U>(this, sampler));
    }

    /**
     * Returns an Observable that applies a function of your choosing to the first item emitted by a
     * source Observable, then feeds the result of that function along with the second item emitted
     * by the source Observable into the same function, and so on until all items have been emitted
     * by the source Observable, emitting the result of each of these iterations.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/scan.png">
     * <p>
     * This sort of function is sometimes called an accumulator.
     * 
     * @param accumulator
     *            an accumulator function to be invoked on each item emitted by the source
     *            Observable, whose result will be emitted to {@link Observer}s via {@link Observer#onNext onNext} and used in the next accumulator call
     * @return an Observable that emits the results of each call to the accumulator function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-scan">RxJava Wiki: scan()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211665.aspx">MSDN: Observable.Scan</a>
     */
    public final Observable<T> scan(Func2<T, T, T> accumulator) {
        return create(OperationScan.scan(this, accumulator));
    }

    /**
     * Returns an Observable that applies a function of your choosing to the first item emitted by a
     * source Observable and a seed value, then feeds the result of that function along with the
     * second item emitted by the source Observable into the same function, and so on until all
     * items have been emitted by the source Observable, emitting the result of each of these
     * iterations.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/scanSeed.png">
     * <p>
     * This sort of function is sometimes called an accumulator.
     * <p>
     * Note that the Observable that results from this method will emit {@code initialValue} as its
     * first emitted item.
     * 
     * @param initialValue
     *            the initial (seed) accumulator item
     * @param accumulator
     *            an accumulator function to be invoked on each item emitted by the source
     *            Observable, whose result will be emitted to {@link Observer}s via {@link Observer#onNext onNext} and used in the next accumulator call
     * @return an Observable that emits the results of each call to the accumulator function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-scan">RxJava Wiki: scan()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211665.aspx">MSDN: Observable.Scan</a>
     */
    public final <R> Observable<R> scan(R initialValue, Func2<R, ? super T, R> accumulator) {
        return create(OperationScan.scan(this, initialValue, accumulator));
    }

    /**
     * If the source Observable completes after emitting a single item, return an Observable
     * that emits that item. If the source Observable emits more than one item or no items, throw
     * an {@code IllegalArgumentException}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/single.png">
     * 
     * @return an Observable that emits the single item emitted by the source Observable
     * @throws IllegalArgumentException
     *             if the source emits more than one item or no items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-single-and-singleordefault">RxJava Wiki: single()</a>
     * @see MSDN: {@code Observable.singleAsync()}
     */
    public final Observable<T> single() {
        return create(OperationSingle.<T> single(this));
    }

    /**
     * If the Observable completes after emitting a single item that matches a specified predicate,
     * return an Observable that emits that item. If the source Observable emits more than one such
     * item or no such items, throw an {@code IllegalArgumentException}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/single.p.png">
     * 
     * @param predicate
     *            a predicate function to evaluate items emitted by the source Observable
     * @return an Observable that emits the single item emitted by the source Observable that
     *         matches the predicate
     * @throws IllegalArgumentException
     *             if the source Observable emits either more than one item that matches the
     *             predicate or no items that match the predicate
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-single-and-singleordefault">RxJava Wiki: single()</a>
     * @see MSDN: {@code Observable.singleAsync()}
     */
    public final Observable<T> single(Func1<? super T, Boolean> predicate) {
        return filter(predicate).single();
    }

    /**
     * If the source Observable completes after emitting a single item, return an Observable that
     * emits that item; if the source Observable is empty, return an Observable that emits a default
     * item. If the source Observable emits more than one item, throw an {@code IllegalArgumentException.} <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/singleOrDefault.png">
     * 
     * @param defaultValue
     *            a default value to emit if the source Observable emits no item
     * @return an Observable that emits the single item emitted by the source Observable, or a
     *         default item if the source Observable is empty
     * @throws IllegalArgumentException
     *             if the source Observable emits more than one item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-single-and-singleordefault">RxJava Wiki: single()</a>
     * @see MSDN: {@code Observable.singleOrDefaultAsync()}
     */
    public final Observable<T> singleOrDefault(T defaultValue) {
        return create(OperationSingle.<T> singleOrDefault(this, defaultValue));
    }

    /**
     * If the Observable completes after emitting a single item that matches a predicate, return an
     * Observable that emits that item; if the source Observable emits no such item, return an
     * Observable that emits a default item. If the source Observable emits more than one such item,
     * throw an {@code IllegalArgumentException}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/singleOrDefault.p.png">
     * 
     * @param defaultValue
     *            a default item to emit if the source Observable emits no matching items
     * @param predicate
     *            a predicate function to evaluate items emitted by the source Observable
     * @return an Observable that emits the single item emitted by the source Observable that
     *         matches the predicate, or the default item if no emitted item matches the predicate
     * @throws IllegalArgumentException
     *             if the source Observable emits more than one item that matches the predicate
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-single-and-singleordefault">RxJava Wiki: single()</a>
     * @see MSDN: {@code Observable.singleOrDefaultAsync()}
     */
    public final Observable<T> singleOrDefault(T defaultValue, Func1<? super T, Boolean> predicate) {
        return filter(predicate).singleOrDefault(defaultValue);
    }

    /**
     * Returns an Observable that skips the first {@code num} items emitted by the source Observable
     * and emits the remainder.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skip.png">
     * 
     * @param num
     *            the number of items to skip
     * @return an Observable that is identical to the source Observable except that it does not
     *         emit the first {@code num} items that the source Observable emits
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-skip">RxJava Wiki: skip()</a>
     */
    public final Observable<T> skip(int num) {
        return create(OperationSkip.skip(this, num));
    }

    /**
     * Returns an Observable that skips values emitted by the source Observable before a specified
     * time window elapses.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skip.t.png">
     * 
     * @param time
     *            the length of the time window to skip
     * @param unit
     *            the time unit of {@code time}
     * @return an Observable that skips values emitted by the source Observable before the time
     *         window defined by {@code time} elapses
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-skip">RxJava Wiki: skip()</a>
     */
    public final Observable<T> skip(long time, TimeUnit unit) {
        return skip(time, unit, Schedulers.computation());
    }

    /**
     * Returns an Observable that skips values emitted by the source Observable before a specified
     * time window on a specified {@link Scheduler} elapses.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skip.ts.png">
     * 
     * @param time
     *            the length of the time window to skip
     * @param unit
     *            the time unit of {@code time}
     * @param scheduler
     *            the {@link Scheduler} on which the timed wait happens
     * @return an Observable that skips values emitted by the source Observable before the time
     *         window defined by {@code time} and {@code scheduler} elapses
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-skip">RxJava Wiki: skip()</a>
     */
    public final Observable<T> skip(long time, TimeUnit unit, Scheduler scheduler) {
        return create(new OperationSkip.SkipTimed<T>(this, time, unit, scheduler));
    }

    /**
     * Returns an Observable that drops a specified number of items from the end of the sequence
     * emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipLast.png">
     * <p>
     * This Observer accumulates a queue long enough to store the first {@code count} items. As more
     * items are received, items are taken from the front of the queue and emitted by the returned
     * Observable. This causes such items to be delayed.
     * 
     * @param count
     *            number of items to drop from the end of the source sequence
     * @return an Observable that emits the items emitted by the source Observable except for the
     *         dropped ones at the end
     * @throws IndexOutOfBoundsException
     *             if {@code count} is less than zero
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-skiplast">RxJava Wiki: skipLast()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211750.aspx">MSDN: Observable.SkipLast</a>
     */
    public final Observable<T> skipLast(int count) {
        return create(OperationSkipLast.skipLast(this, count));
    }

    /**
     * Returns an Observable that drops items emitted by the source Observable during a specified
     * time window before the source completes.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipLast.t.png">
     * 
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @return an Observable that drops those items emitted by the source Observable in a time
     *         window before the source completes defined by {@code time}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-skiplast">RxJava Wiki: skipLast()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211750.aspx">MSDN: Observable.SkipLast</a>
     */
    public final Observable<T> skipLast(long time, TimeUnit unit) {
        return skipLast(time, unit, Schedulers.computation());
    }

    /**
     * Returns an Observable that drops items emitted by the source Observable during a specified
     * time window (defined on a specified scheduler) before the source completes.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipLast.ts.png">
     * 
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @param scheduler
     *            the scheduler used as the time source
     * @return an Observable that drops those items emitted by the source Observable in a time
     *         window before the source completes defined by {@code time} and {@code scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-skiplast">RxJava Wiki: skipLast()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211750.aspx">MSDN: Observable.SkipLast</a>
     */
    public final Observable<T> skipLast(long time, TimeUnit unit, Scheduler scheduler) {
        return create(new OperationSkipLast.SkipLastTimed<T>(this, time, unit, scheduler));
    }

    /**
     * Returns an Observable that skips items emitted by the source Observable until a second
     * Observable emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipUntil.png">
     * 
     * @param other
     *            the second Observable that has to emit an item before the source Observable's
     *            elements begin to be mirrored by the resulting Observable
     * @return an Observable that skips items from the source Observable until the second
     *         Observable emits an item, then emits the remaining items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-skipuntil">RxJava Wiki: skipUntil()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229358.aspx">MSDN: Observable.SkipUntil</a>
     */
    public final <U> Observable<T> skipUntil(Observable<U> other) {
        return create(new OperationSkipUntil<T, U>(this, other));
    }

    /**
     * Returns an Observable that skips all items emitted by the source Observable as long as a
     * specified condition holds true, but emits all further source items as soon as the condition
     * becomes false.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipWhile.png">
     * 
     * @param predicate
     *            a function to test each item emitted from the source Observable
     * @return an Observable that begins emitting items emitted by the source Observable when the
     *         specified predicate becomes false
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-skipwhile-and-skipwhilewithindex">RxJava Wiki: skipWhile()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229685.aspx">MSDN: Observable.SkipWhile</a>
     */
    public final Observable<T> skipWhile(Func1<? super T, Boolean> predicate) {
        return create(OperationSkipWhile.skipWhile(this, predicate));
    }

    /**
     * Returns an Observable that skips all items emitted by the source Observable as long as a
     * specified condition holds true, but emits all further source items as soon as the condition
     * becomes false.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipWhileWithIndex.png">
     * 
     * @param predicate
     *            a function to test each item emitted from the source Observable. It takes the
     *            emitted item as the first parameter and the sequential index of the emitted item
     *            as a second parameter.
     * @return an Observable that begins emitting items emitted by the source Observable when the
     *         specified predicate becomes false
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-skipwhile-and-skipwhilewithindex">RxJava Wiki: skipWhileWithIndex()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211631.aspx">MSDN: Observable.SkipWhile</a>
     */
    public final Observable<T> skipWhileWithIndex(Func2<? super T, Integer, Boolean> predicate) {
        return create(OperationSkipWhile.skipWhileWithIndex(this, predicate));
    }

    /**
     * Returns an Observable that emits the items in a specified {@link Iterable} before it begins
     * to emit items emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param values
     *            an Iterable that contains the items you want the modified Observable to emit first
     * @return an Observable that emits the items in the specified {@link Iterable} and then emits
     *         the items emitted by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     */
    public final Observable<T> startWith(Iterable<T> values) {
        return concat(Observable.<T> from(values), this);
    }

    /**
     * Returns an Observable that emits the items in a specified {@link Iterable}, on a specified {@link Scheduler} before it begins to emit items emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.s.png">
     * 
     * @param values
     *            an Iterable that contains the items you want the modified Observable to emit first
     * @param scheduler
     *            the scheduler to emit the prepended values on
     * @return an Observable that emits the items in the specified {@link Iterable} and then emits
     *         the items emitted by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229372.aspx">MSDN: Observable.StartWith</a>
     */
    public final Observable<T> startWith(Iterable<T> values, Scheduler scheduler) {
        return concat(from(values, scheduler), this);
    }

    /**
     * Returns an Observable that emits a specified item before it begins to emit items emitted by
     * the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1
     *            the item to emit
     * @return an Observable that emits the specified item before it begins to emit items emitted by
     *         the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     */
    public final Observable<T> startWith(T t1) {
        return concat(Observable.<T> from(t1), this);
    }

    /**
     * Returns an Observable that emits the specified items before it begins to emit items emitted
     * by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1
     *            the first item to emit
     * @param t2
     *            the second item to emit
     * @return an Observable that emits the specified items before it begins to emit items emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     */
    public final Observable<T> startWith(T t1, T t2) {
        return concat(Observable.<T> from(t1, t2), this);
    }

    /**
     * Returns an Observable that emits the specified items before it begins to emit items emitted
     * by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1
     *            the first item to emit
     * @param t2
     *            the second item to emit
     * @param t3
     *            the third item to emit
     * @return an Observable that emits the specified items before it begins to emit items emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     */
    public final Observable<T> startWith(T t1, T t2, T t3) {
        return concat(Observable.<T> from(t1, t2, t3), this);
    }

    /**
     * Returns an Observable that emits the specified items before it begins to emit items emitted
     * by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1
     *            the first item to emit
     * @param t2
     *            the second item to emit
     * @param t3
     *            the third item to emit
     * @param t4
     *            the fourth item to emit
     * @return an Observable that emits the specified items before it begins to emit items emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     */
    public final Observable<T> startWith(T t1, T t2, T t3, T t4) {
        return concat(Observable.<T> from(t1, t2, t3, t4), this);
    }

    /**
     * Returns an Observable that emits the specified items before it begins to emit items emitted
     * by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1
     *            the first item to emit
     * @param t2
     *            the second item to emit
     * @param t3
     *            the third item to emit
     * @param t4
     *            the fourth item to emit
     * @param t5
     *            the fifth item to emit
     * @return an Observable that emits the specified items before it begins to emit items emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     */
    public final Observable<T> startWith(T t1, T t2, T t3, T t4, T t5) {
        return concat(Observable.<T> from(t1, t2, t3, t4, t5), this);
    }

    /**
     * Returns an Observable that emits the specified items before it begins to emit items emitted
     * by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1
     *            the first item to emit
     * @param t2
     *            the second item to emit
     * @param t3
     *            the third item to emit
     * @param t4
     *            the fourth item to emit
     * @param t5
     *            the fifth item to emit
     * @param t6
     *            the sixth item to emit
     * @return an Observable that emits the specified items before it begins to emit items emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     */
    public final Observable<T> startWith(T t1, T t2, T t3, T t4, T t5, T t6) {
        return concat(Observable.<T> from(t1, t2, t3, t4, t5, t6), this);
    }

    /**
     * Returns an Observable that emits the specified items before it begins to emit items emitted
     * by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1
     *            the first item to emit
     * @param t2
     *            the second item to emit
     * @param t3
     *            the third item to emit
     * @param t4
     *            the fourth item to emit
     * @param t5
     *            the fifth item to emit
     * @param t6
     *            the sixth item to emit
     * @param t7
     *            the seventh item to emit
     * @return an Observable that emits the specified items before it begins to emit items emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     */
    public final Observable<T> startWith(T t1, T t2, T t3, T t4, T t5, T t6, T t7) {
        return concat(Observable.<T> from(t1, t2, t3, t4, t5, t6, t7), this);
    }

    /**
     * Returns an Observable that emits the specified items before it begins to emit items emitted
     * by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1
     *            the first item to emit
     * @param t2
     *            the second item to emit
     * @param t3
     *            the third item to emit
     * @param t4
     *            the fourth item to emit
     * @param t5
     *            the fifth item to emit
     * @param t6
     *            the sixth item to emit
     * @param t7
     *            the seventh item to emit
     * @param t8
     *            the eighth item to emit
     * @return an Observable that emits the specified items before it begins to emit items emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     */
    public final Observable<T> startWith(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8) {
        return concat(Observable.<T> from(t1, t2, t3, t4, t5, t6, t7, t8), this);
    }

    /**
     * Returns an Observable that emits the specified items before it begins to emit items emitted
     * by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1
     *            the first item to emit
     * @param t2
     *            the second item to emit
     * @param t3
     *            the third item to emit
     * @param t4
     *            the fourth item to emit
     * @param t5
     *            the fifth item to emit
     * @param t6
     *            the sixth item to emit
     * @param t7
     *            the seventh item to emit
     * @param t8
     *            the eighth item to emit
     * @param t9
     *            the ninth item to emit
     * @return an Observable that emits the specified items before it begins to emit items emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     */
    public final Observable<T> startWith(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9) {
        return concat(Observable.<T> from(t1, t2, t3, t4, t5, t6, t7, t8, t9), this);
    }

    /**
     * Returns an Observable that emits the items from a specified array, on a specified scheduler,
     * before it begins to emit items emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.s.png">
     * 
     * @param values
     *            the items you want the modified Observable to emit first
     * @param scheduler
     *            the scheduler to emit the prepended values on
     * @return an Observable that emits the items from {@code values}, on {@code scheduler}, before
     *         it begins to emit items emitted by the source Observable.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-startwith">RxJava Wiki: startWith()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229372.aspx">MSDN: Observable.StartWith</a>
     */
    public final Observable<T> startWith(T[] values, Scheduler scheduler) {
        return startWith(Arrays.asList(values), scheduler);
    }

    /**
     * Subscribe and ignore all events.
     * 
     * @return a {@link Subscription} reference with which the {@link Observer} can stop receiving
     *         items before the Observable has finished sending them
     */
    public final Subscription subscribe() {
        return protectivelyWrapAndSubscribe(new Subscriber<T>() {

            @Override
            public final void onCompleted() {
                // do nothing
            }

            @Override
            public final void onError(Throwable e) {
                throw new OnErrorNotImplementedException(e);
            }

            @Override
            public final void onNext(T args) {
                // do nothing
            }

        });
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     * 
     * @param onNext
     *            FIXME FIXME FIXME
     * @return a {@link Subscription} reference with which the {@link Observer} can stop receiving
     *         items before the Observable has finished sending them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#wiki-onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public final Subscription subscribe(final Action1<? super T> onNext) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }

        /**
         * Wrapping since raw functions provided by the user are being invoked.
         * 
         * See https://github.com/Netflix/RxJava/issues/216 for discussion on "Guideline 6.4: Protect calls to user code from within an Observer"
         */
        return protectivelyWrapAndSubscribe(new Subscriber<T>() {

            @Override
            public final void onCompleted() {
                // do nothing
            }

            @Override
            public final void onError(Throwable e) {
                throw new OnErrorNotImplementedException(e);
            }

            @Override
            public final void onNext(T args) {
                onNext.call(args);
            }

        });
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     * 
     * @param onNext
     *            FIXME FIXME FIXME
     * @param onError
     *            FIXME FIXME FIXME
     * @return a {@link Subscription} reference with which the {@link Observer} can stop receiving
     *         items before the Observable has finished sending them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#wiki-onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public final Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }
        if (onError == null) {
            throw new IllegalArgumentException("onError can not be null");
        }

        /**
         * Wrapping since raw functions provided by the user are being invoked.
         * 
         * See https://github.com/Netflix/RxJava/issues/216 for discussion on
         * "Guideline 6.4: Protect calls to user code from within an Observer"
         */
        return protectivelyWrapAndSubscribe(new Subscriber<T>() {

            @Override
            public final void onCompleted() {
                // do nothing
            }

            @Override
            public final void onError(Throwable e) {
                onError.call(e);
            }

            @Override
            public final void onNext(T args) {
                onNext.call(args);
            }

        });
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     * 
     * @param onNext
     *            FIXME FIXME FIXME
     * @param onError
     *            FIXME FIXME FIXME
     * @param onComplete
     *            FIXME FIXME FIXME
     * @return a {@link Subscription} reference with which the {@link Observer} can stop receiving
     *         items before the Observable has finished sending them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#wiki-onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public final Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onComplete) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }
        if (onError == null) {
            throw new IllegalArgumentException("onError can not be null");
        }
        if (onComplete == null) {
            throw new IllegalArgumentException("onComplete can not be null");
        }

        /**
         * Wrapping since raw functions provided by the user are being invoked.
         * 
         * See https://github.com/Netflix/RxJava/issues/216 for discussion on "Guideline 6.4: Protect calls to user code from within an Observer"
         */
        return protectivelyWrapAndSubscribe(new Subscriber<T>() {

            @Override
            public final void onCompleted() {
                onComplete.call();
            }

            @Override
            public final void onError(Throwable e) {
                onError.call(e);
            }

            @Override
            public final void onNext(T args) {
                onNext.call(args);
            }

        });
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     * 
     * @param onNext
     *            FIXME FIXME FIXME
     * @param onError
     *            FIXME FIXME FIXME
     * @param onComplete
     *            FIXME FIXME FIXME
     * @param scheduler
     *            FIXME FIXME FIXME
     * @return a {@link Subscription} reference with which the {@link Observer} can stop receiving
     *         items before the Observable has finished sending them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#wiki-onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public final Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onComplete, Scheduler scheduler) {
        return subscribeOn(scheduler).subscribe(onNext, onError, onComplete);
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     * 
     * @param onNext
     *            FIXME FIXME FIXME
     * @param onError
     *            FIXME FIXME FIXME
     * @param scheduler
     *            FIXME FIXME FIXME
     * @return a {@link Subscription} reference with which the {@link Observer} can stop receiving
     *         items before the Observable has finished sending them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#wiki-onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public final Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError, Scheduler scheduler) {
        return subscribeOn(scheduler).subscribe(onNext, onError);
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     * 
     * @param onNext
     *            FIXME FIXME FIXME
     * @param scheduler
     *            FIXME FIXME FIXME
     * @return a {@link Subscription} reference with which the {@link Observer} can stop receiving
     *         items before the Observable has finished sending them
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#wiki-onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public final Subscription subscribe(final Action1<? super T> onNext, Scheduler scheduler) {
        return subscribeOn(scheduler).subscribe(onNext);
    }

    public final Subscription subscribe(final Observer<? super T> observer, Scheduler scheduler) {
        return subscribeOn(scheduler).subscribe(observer);
    }
    
    public final Subscription subscribe(final Observer<? super T> observer) {
        return subscribe(new Subscriber<T>() {

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

        });
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     * <p>
     * A typical implementation of {@code subscribe} does the following:
     * <ol>
     * <li>It stores a reference to the Observer in a collection object, such as a {@code List<T>} object.</li>
     * <li>It returns a reference to the {@link Subscription} interface. This enables Observers to
     * unsubscribe, that is, to stop receiving items and notifications before the Observable stops
     * sending them, which also invokes the Observer's {@link Observer#onCompleted onCompleted} method.</li>
     * </ol><p>
     * An {@code Observable<T>} instance is responsible for accepting all subscriptions and
     * notifying all Observers. Unless the documentation for a particular {@code Observable<T>} implementation indicates otherwise, Observers should make no assumptions about the order in
     * which multiple Observers will receive their notifications.
     * <p>
     * For more information see the
     * <a href="https://github.com/Netflix/RxJava/wiki/Observable">RxJava Wiki</a>
     * 
     * @param observer
     *            the {@link Observer}
     * @return a {@link Subscription} reference with which the {@link Observer} can stop receiving
     *         items before the Observable has finished sending them
     * @throws IllegalArgumentException
     *             if the {@link Observer} provided as the argument to {@code subscribe()} is {@code null}
     */
    public final Subscription subscribe(Subscriber<? super T> observer) {
        // allow the hook to intercept and/or decorate
        OnSubscribe<T> onSubscribeFunction = hook.onSubscribeStart(this, f);
        // validate and proceed
        if (observer == null) {
            throw new IllegalArgumentException("observer can not be null");
        }
        if (onSubscribeFunction == null) {
            throw new IllegalStateException("onSubscribe function can not be null.");
            // the subscribe function can also be overridden but generally that's not the appropriate approach so I won't mention that in the exception
        }
        try {
            /**
             * See https://github.com/Netflix/RxJava/issues/216 for discussion on "Guideline 6.4: Protect calls to user code from within an Observer"
             */
            if (isInternalImplementation(observer)) {
                onSubscribeFunction.call(observer);
            } else {
                onSubscribeFunction.call(new SafeSubscriber<T>(observer));
            }
            return hook.onSubscribeReturn(this, observer);
        } catch (OnErrorNotImplementedException e) {
            // special handling when onError is not implemented ... we just rethrow
            throw e;
        } catch (Throwable e) {
            // if an unhandled error occurs executing the onSubscribe we will propagate it
            try {
                observer.onError(hook.onSubscribeError(this, e));
            } catch (OnErrorNotImplementedException e2) {
                // special handling when onError is not implemented ... we just rethrow
                throw e2;
            } catch (Throwable e2) {
                // if this happens it means the onError itself failed (perhaps an invalid function implementation)
                // so we are unable to propagate the error correctly and will just throw
                RuntimeException r = new RuntimeException("Error occurred attempting to subscribe [" + e.getMessage() + "] and then again while trying to pass to onError.", e2);
                hook.onSubscribeError(this, r);
                throw r;
            }
            return Subscriptions.empty();
        }
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     * <p>
     * A typical implementation of {@code subscribe} does the following:
     * <ol>
     * <li>It stores a reference to the Observer in a collection object, such as a {@code List<T>} object.</li>
     * <li>It returns a reference to the {@link Subscription} interface. This enables Observers to
     * unsubscribe, that is, to stop receiving items and notifications before the Observable stops
     * sending them, which also invokes the Observer's {@link Observer#onCompleted onCompleted} method.</li>
     * </ol><p>
     * An {@code Observable<T>} instance is responsible for accepting all subscriptions and
     * notifying all Observers. Unless the documentation for a particular {@code Observable<T>} implementation indicates otherwise, Observers should make no assumptions about the order in
     * which multiple Observers will receive their notifications.
     * <p>
     * For more information see the
     * <a href="https://github.com/Netflix/RxJava/wiki/Observable">RxJava Wiki</a>
     * 
     * @param observer
     *            the {@link Observer}
     * @param scheduler
     *            the {@link Scheduler} on which Observers subscribe to the Observable
     * @return a {@link Subscription} reference with which Observers can stop receiving items and
     *         notifications before the Observable has finished sending them
     * @throws IllegalArgumentException
     *             if an argument to {@code subscribe()} is {@code null}
     */
    public final Subscription subscribe(Subscriber<? super T> observer, Scheduler scheduler) {
        return subscribeOn(scheduler).subscribe(observer);
    }

    /**
     * Asynchronously subscribes and unsubscribes Observers to this Observable on the specified {@link Scheduler}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/subscribeOn.png">
     * 
     * @param scheduler
     *            the {@link Scheduler} to perform subscription and unsubscription actions on
     * @return the source Observable modified so that its subscriptions and unsubscriptions happen
     *         on the specified {@link Scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-subscribeon">RxJava Wiki: subscribeOn()</a>
     */
    public final Observable<T> subscribeOn(Scheduler scheduler) {
        return create(OperationSubscribeOn.subscribeOn(this, scheduler));
    }

    /**
     * Returns an Observable that extracts a Double from each of the items emitted by the source
     * Observable via a function you specify, and then emits the sum of these Doubles.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.f.png">
     * 
     * @param valueExtractor
     *            the function to extract a Double from each item emitted by the source Observable
     * @return an Observable that emits the Double sum of the Double values corresponding to the
     *         items emitted by the source Observable as transformed by the provided function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumDouble()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public final Observable<Double> sumDouble(Func1<? super T, Double> valueExtractor) {
        return create(new OperationSum.SumDoubleExtractor<T>(this, valueExtractor));
    }

    /**
     * Returns an Observable that extracts a Float from each of the items emitted by the source
     * Observable via a function you specify, and then emits the sum of these Floats.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.f.png">
     * 
     * @param valueExtractor
     *            the function to extract a Float from each item emitted by the source Observable
     * @return an Observable that emits the Float sum of the Float values corresponding to the items
     *         emitted by the source Observable as transformed by the provided function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumFloat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public final Observable<Float> sumFloat(Func1<? super T, Float> valueExtractor) {
        return create(new OperationSum.SumFloatExtractor<T>(this, valueExtractor));
    }

    /**
     * Returns an Observable that extracts an Integer from each of the items emitted by the source
     * Observable via a function you specify, and then emits the sum of these Integers.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.f.png">
     * 
     * @param valueExtractor
     *            the function to extract an Integer from each item emitted by the source Observable
     * @return an Observable that emits the Integer sum of the Integer values corresponding to the
     *         items emitted by the source Observable as transformed by the provided function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumInteger()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public final Observable<Integer> sumInteger(Func1<? super T, Integer> valueExtractor) {
        return create(new OperationSum.SumIntegerExtractor<T>(this, valueExtractor));
    }

    /**
     * Returns an Observable that extracts a Long from each of the items emitted by the source
     * Observable via a function you specify, and then emits the sum of these Longs.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.f.png">
     * 
     * @param valueExtractor
     *            the function to extract a Long from each item emitted by the source Observable
     * @return an Observable that emits the Long sum of the Long values corresponding to the items
     *         emitted by the source Observable as transformed by the provided function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumLong()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public final Observable<Long> sumLong(Func1<? super T, Long> valueExtractor) {
        return create(new OperationSum.SumLongExtractor<T>(this, valueExtractor));
    }

    /**
     * Returns a new Observable by applying a function that you supply to each item emitted by the
     * source Observable that returns an Observable, and then emitting the items emitted by the
     * most recently emitted of these Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/switchMap.png">
     * 
     * @param func
     *            a function that, when applied to an item emitted by the source Observable, returns
     *            an Observable
     * @return an Observable that emits the items emitted by the Observable returned from applying {@code func} to the most recently emitted item emitted by the source Observable
     */
    public final <R> Observable<R> switchMap(Func1<? super T, ? extends Observable<? extends R>> func) {
        return switchOnNext(map(func));
    }

    /**
     * Wraps the source Observable in another Observable that ensures that the resulting Observable
     * is chronologically well-behaved.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/synchronize.png">
     * <p>
     * A well-behaved Observable does not interleave its invocations of the {@link Observer#onNext onNext}, {@link Observer#onCompleted onCompleted}, and {@link Observer#onError onError} methods of
     * its {@link Observer}s; it invokes either {@code onCompleted} or {@code onError} only once; and it never invokes {@code onNext} after
     * invoking either {@code onCompleted} or {@code onError}. {@code synchronize} enforces this,
     * and the Observable it returns invokes {@code onNext} and {@code onCompleted} or {@code onError} synchronously.
     * 
     * @return an Observable that is a chronologically well-behaved version of the source
     *         Observable, and that synchronously notifies its {@link Observer}s
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-synchronize">RxJava Wiki: synchronize()</a>
     */
    public final Observable<T> synchronize() {
        return create(OperationSynchronize.synchronize(this));
    }

    /**
     * Wraps the source Observable in another Observable that ensures that the resulting Observable
     * is chronologically well-behaved by acquiring a mutual-exclusion lock for the object provided
     * as the {@code lock} parameter.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/synchronize.png">
     * <p>
     * A well-behaved Observable does not interleave its invocations of the {@link Observer#onNext onNext}, {@link Observer#onCompleted onCompleted}, and {@link Observer#onError onError} methods of
     * its {@link Observer}s; it invokes either {@code onCompleted} or {@code onError} only once; and it never invokes {@code onNext} after
     * invoking either {@code onCompleted} or {@code onError}. {@code synchronize} enforces this,
     * and the Observable it returns invokes {@code onNext} and {@code onCompleted} or {@code onError} synchronously.
     * 
     * @param lock
     *            the lock object to synchronize each observer call on
     * @return an Observable that is a chronologically well-behaved version of the source
     *         Observable, and that synchronously notifies its {@link Observer}s
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-synchronize">RxJava Wiki: synchronize()</a>
     */
    public final Observable<T> synchronize(Object lock) {
        return create(OperationSynchronize.synchronize(this, lock));
    }

    /**
     * Returns an Observable that emits only the first {@code num} items emitted by the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/take.png">
     * <p>
     * This method returns an Observable that will invoke a subscribing {@link Observer}'s {@link Observer#onNext onNext} function a maximum of {@code num} times before invoking
     * {@link Observer#onCompleted onCompleted}.
     * 
     * @param num
     *            the maximum number of items to emit
     * @return an Observable that emits only the first {@code num} items emitted by the source
     *         Observable, or all of the items from the source Observable if that Observable emits
     *         fewer than {@code num} items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-take">RxJava Wiki: take()</a>
     */
    public final Observable<T> take(final int num) {
        return lift(new OperatorTake<T>(num));
    }

    /**
     * Returns an Observable that emits those items emitted by source Observable before a specified
     * time runs out.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/take.t.png">
     * 
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @return an Observable that emits those items emitted by the source Observable before the time
     *         runs out
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-take">RxJava Wiki: take()</a>
     */
    public final Observable<T> take(long time, TimeUnit unit) {
        return take(time, unit, Schedulers.computation());
    }

    /**
     * Returns an Observable that emits those items emitted by source Observable before a specified
     * time (on a specified scheduler) runs out.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/take.ts.png">
     * 
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @param scheduler
     *            the scheduler used for time source
     * @return an Observable that emits those items emitted by the source Observable before the time
     *         runs out, according to the specified scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-take">RxJava Wiki: take()</a>
     */
    public final Observable<T> take(long time, TimeUnit unit, Scheduler scheduler) {
        return create(new OperationTakeTimed.TakeTimed<T>(this, time, unit, scheduler));
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeFirst.png">
     * 
     * @return an Observable that emits only the very first item emitted by the source Observable,
     *         or an empty Observable if the source Observable completes without emitting a single
     *         item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-first">RxJava Wiki: first()</a>
     * @see MSDN: {@code Observable.firstAsync()}
     * @deprecated use {@code take(1)} directly
     */
    @Deprecated
    public final Observable<T> takeFirst() {
        return take(1);
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the source Observable
     * that satisfies a specified condition.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeFirstN.png">
     * 
     * @param predicate
     *            the condition any item emitted by the source Observable has to satisfy
     * @return an Observable that emits only the very first item emitted by the source Observable
     *         that satisfies the given condition, or that completes without emitting anything if
     *         the source Observable completes without emitting a single condition-satisfying item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-first">RxJava Wiki: first()</a>
     * @see MSDN: {@code Observable.firstAsync()}
     */
    public final Observable<T> takeFirst(Func1<? super T, Boolean> predicate) {
        return filter(predicate).take(1);
    }

    /**
     * Returns an Observable that emits only the last {@code count} items emitted by the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLast.n.png">
     * 
     * @param count
     *            the number of items to emit from the end of the sequence of items emitted by the
     *            source Observable
     * @return an Observable that emits only the last {@code count} items emitted by the source
     *         Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-takelast">RxJava Wiki: takeLast()</a>
     */
    public final Observable<T> takeLast(final int count) {
        return create(OperationTakeLast.takeLast(this, count));
    }

    /**
     * Return an Observable that emits at most a specified number of items from the source
     * Observable that were emitted in a specified window of time before the Observable completed.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLast.tn.png">
     * 
     * @param count
     *            the maximum number of items to emit
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @return an Observable that emits at most {@code count} items from the source Observable that
     *         were emitted in a specified window of time before the Observable completed.
     */
    public final Observable<T> takeLast(int count, long time, TimeUnit unit) {
        return takeLast(count, time, unit, Schedulers.computation());
    }

    /**
     * Return an Observable that emits at most a specified number of items from the source
     * Observable that were emitted in a specified window of time before the Observable
     * completed, where the timing information is provided by a given scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLast.tns.png">
     * 
     * @param count
     *            the maximum number of items to emit
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @param scheduler
     *            the Scheduler that provides the timestamps for the observed items
     * @return an Observable that emits at most {@code count} items from the source Observable that
     *         were emitted in a specified window of time before the Observable completed, where
     *         the timing information is provided by the given {@code scheduler}
     */
    public final Observable<T> takeLast(int count, long time, TimeUnit unit, Scheduler scheduler) {
        if (count < 0) {
            throw new IllegalArgumentException("count >= 0 required");
        }
        return create(OperationTakeLast.takeLast(this, count, time, unit, scheduler));
    }

    /**
     * Return an Observable that emits the items from the source Observable that were emitted in a
     * specified window of time before the Observable completed.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLast.t.png">
     * 
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @return an Observable that emits the items from the source Observable that were emitted in
     *         the window of time before the Observable completed specified by {@code time}
     */
    public final Observable<T> takeLast(long time, TimeUnit unit) {
        return takeLast(time, unit, Schedulers.computation());
    }

    /**
     * Return an Observable that emits the items from the source Observable that were emitted in a
     * specified window of time before the Observable completed, where the timing information is
     * provided by a specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLast.ts.png">
     * 
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @param scheduler
     *            the Scheduler that provides the timestamps for the Observed items
     * @return an Observable that emits the items from the source Observable that were emitted in
     *         the window of time before the Observable completed specified by {@code time}, where
     *         the timing information is provided by {@code scheduler}
     */
    public final Observable<T> takeLast(long time, TimeUnit unit, Scheduler scheduler) {
        return create(OperationTakeLast.takeLast(this, time, unit, scheduler));
    }

    /**
     * Return an Observable that emits a single List containing the last {@code count} elements
     * emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLastBuffer.png">
     * 
     * @param count
     *            the number of items to emit in the list
     * @return an Observable that emits a single list containing the last {@code count} elements
     *         emitted by the source Observable
     */
    public final Observable<List<T>> takeLastBuffer(int count) {
        return takeLast(count).toList();
    }

    /**
     * Return an Observable that emits a single List containing at most {@code count} items from
     * the source Observable that were emitted during a specified window of time before the
     * source Observable completed.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLastBuffer.tn.png">
     * 
     * @param count
     *            the maximum number of items to emit
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @return an Observable that emits a single List containing at most {@code count} items emitted
     *         by the source Observable during the time window defined by {@code time} before the
     *         source Observable completed
     */
    public final Observable<List<T>> takeLastBuffer(int count, long time, TimeUnit unit) {
        return takeLast(count, time, unit).toList();
    }

    /**
     * Return an Observable that emits a single List containing at most {@code count} items from
     * the source Observable that were emitted during a specified window of time (on a specified
     * scheduler) before the source Observable completed.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLastBuffer.tns.png">
     * 
     * @param count
     *            the maximum number of items to emit
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @param scheduler
     *            the scheduler that provides the timestamps for the observed items
     * @return an Observable that emits a single List containing at most {@code count} items emitted
     *         by the source Observable during the time window defined by {@code time} before the
     *         source Observable completed
     */
    public final Observable<List<T>> takeLastBuffer(int count, long time, TimeUnit unit, Scheduler scheduler) {
        return takeLast(count, time, unit, scheduler).toList();
    }

    /**
     * Return an Observable that emits a single List containing those items from the source
     * Observable that were emitted during a specified window of time before the source Observable
     * completed.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLastBuffer.t.png">
     * 
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @return an Observable that emits a single List containing the items emitted by the source
     *         Observable during the time window defined by {@code time} before the source
     *         Observable completed
     */
    public final Observable<List<T>> takeLastBuffer(long time, TimeUnit unit) {
        return takeLast(time, unit).toList();
    }

    /**
     * Return an Observable that emits a single List containing those items from the source
     * Observable that were emitted during a specified window of time before the source Observable
     * completed, where the timing information is provided by the given Scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLastBuffer.ts.png">
     * 
     * @param time
     *            the length of the time window
     * @param unit
     *            the time unit of {@code time}
     * @param scheduler
     *            the Scheduler that provides the timestamps for the observed items
     * @return an Observable that emits a single List containing the items emitted by the source
     *         Observable during the time window defined by {@code time} before the source
     *         Observable completed, where the timing information is provided by {@code scheduler}
     */
    public final Observable<List<T>> takeLastBuffer(long time, TimeUnit unit, Scheduler scheduler) {
        return takeLast(time, unit, scheduler).toList();
    }

    /**
     * Returns an Observable that emits the items emitted by the source Observable until a second
     * Observable emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeUntil.png">
     * 
     * @param other
     *            the Observable whose first emitted item will cause {@code takeUntil} to stop
     *            emitting items from the source Observable
     * @param <E>
     *            the type of items emitted by {@code other}
     * @return an Observable that emits the items emitted by the source Observable until such time
     *         as {@code other} emits its first item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-takeuntil">RxJava Wiki: takeUntil()</a>
     */
    public final <E> Observable<T> takeUntil(Observable<? extends E> other) {
        return OperationTakeUntil.takeUntil(this, other);
    }

    /**
     * Returns an Observable that emits items emitted by the source Observable so long as each item
     * satisfied a specified condition, and then completes as soon as this condition is not
     * satisfied.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeWhile.png">
     * 
     * @param predicate
     *            a function that evaluates an item emitted by the source Observable and returns a
     *            Boolean
     * @return an Observable that emits the items from the source Observable so long as each item
     *         satisfies the condition defined by {@code predicate}, then completes
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-takewhile-and-takewhilewithindex">RxJava Wiki: takeWhile()</a>
     */
    public final Observable<T> takeWhile(final Func1<? super T, Boolean> predicate) {
        return create(OperationTakeWhile.takeWhile(this, predicate));
    }

    /**
     * Returns an Observable that emits the items emitted by a source Observable so long as a given
     * predicate remains true, where the predicate operates on both the item and its index relative
     * to the complete sequence of emitted items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeWhileWithIndex.png">
     * 
     * @param predicate
     *            a function to test each item emitted by the source Observable for a condition; the
     *            second parameter of the function represents the sequential index of the source
     *            item; it returns a Boolean
     * @return an Observable that emits items from the source Observable so long as the predicate
     *         continues to return {@code true} for each item, then completes
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#wiki-takewhile-and-takewhilewithindex">RxJava Wiki: takeWhileWithIndex()</a>
     */
    public final Observable<T> takeWhileWithIndex(final Func2<? super T, ? super Integer, Boolean> predicate) {
        return create(OperationTakeWhile.takeWhileWithIndex(this, predicate));
    }

    /**
     * Matches when the Observable has an available item and projects the item by invoking the
     * selector function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     * 
     * @param selector
     *            selector that will be invoked for items emitted by the source Observable
     * @return a {@link Plan} that produces the projected results, to be fed (with other Plans) to
     *         the {@link #when} Observer
     * @throws NullPointerException
     *             if {@code selector} is null
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#wiki-and-then-and-when">RxJava Wiki: then()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211662.aspx">MSDN: Observable.Then</a>
     */
    public final <R> Plan0<R> then(Func1<T, R> selector) {
        return OperationJoinPatterns.then(this, selector);
    }

    /**
     * Returns an Observable that emits only the first item emitted by the source Observable during
     * sequential time windows of a specified duration.
     * <p>
     * This differs from {@link #throttleLast} in that this only tracks passage of time whereas {@link #throttleLast} ticks at scheduled intervals.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleFirst.png">
     * 
     * @param windowDuration
     *            time to wait before emitting another item after emitting the last item
     * @param unit
     *            the unit of time of {@code windowDuration}
     * @return an Observable that performs the throttle operation
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-throttlefirst">RxJava Wiki: throttleFirst()</a>
     */
    public final Observable<T> throttleFirst(long windowDuration, TimeUnit unit) {
        return create(OperationThrottleFirst.throttleFirst(this, windowDuration, unit));
    }

    /**
     * Returns an Observable that emits only the first item emitted by the source Observable during
     * sequential time windows of a specified duration, where the windows are managed by a specified
     * Scheduler.
     * <p>
     * This differs from {@link #throttleLast} in that this only tracks passage of time whereas {@link #throttleLast} ticks at scheduled intervals.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleFirst.s.png">
     * 
     * @param skipDuration
     *            time to wait before emitting another item after emitting the last item
     * @param unit
     *            the unit of time of {@code skipDuration}
     * @param scheduler
     *            the {@link Scheduler} to use internally to manage the timers that handle timeout
     *            for each event
     * @return an Observable that performs the throttle operation
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-throttlefirst">RxJava Wiki: throttleFirst()</a>
     */
    public final Observable<T> throttleFirst(long skipDuration, TimeUnit unit, Scheduler scheduler) {
        return create(OperationThrottleFirst.throttleFirst(this, skipDuration, unit, scheduler));
    }

    /**
     * Returns an Observable that emits only the last item emitted by the source Observable during
     * sequential time windows of a specified duration.
     * <p>
     * This differs from {@link #throttleFirst} in that this ticks along at a scheduled interval
     * whereas {@link #throttleFirst} does not tick, it just tracks passage of time.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleLast.png">
     * 
     * @param intervalDuration
     *            duration of windows within which the last item emitted by the source Observable
     *            will be emitted
     * @param unit
     *            the unit of time of {@code intervalDuration}
     * @return an Observable that performs the throttle operation
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-takelast">RxJava Wiki: throttleLast()</a>
     * @see #sample(long, TimeUnit)
     */
    public final Observable<T> throttleLast(long intervalDuration, TimeUnit unit) {
        return sample(intervalDuration, unit);
    }

    /**
     * Returns an Observable that emits only the last item emitted by the source Observable during
     * sequential time windows of a specified duration, where the duration is governed by a
     * specified Scheduler.
     * <p>
     * This differs from {@link #throttleFirst} in that this ticks along at a scheduled interval
     * whereas {@link #throttleFirst} does not tick, it just tracks passage of time.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleLast.s.png">
     * 
     * @param intervalDuration
     *            duration of windows within which the last item emitted by the source Observable
     *            will be emitted
     * @param unit
     *            the unit of time of {@code intervalDuration}
     * @param scheduler
     *            the {@link Scheduler} to use internally to manage the timers that handle timeout
     *            for each event
     * @return an Observable that performs the throttle operation
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-takelast">RxJava Wiki: throttleLast()</a>
     * @see #sample(long, TimeUnit, Scheduler)
     */
    public final Observable<T> throttleLast(long intervalDuration, TimeUnit unit, Scheduler scheduler) {
        return sample(intervalDuration, unit, scheduler);
    }

    /**
     * Returns an Observable that only emits those items emitted by the source Observable that are
     * not followed by another emitted item within a specified time window.
     * <p>
     * <em>Note:</em> If the source Observable keeps emitting items more frequently than the length
     * of the time window then no items will be emitted by the resulting Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleWithTimeout.png">
     * <p>
     * Information on debounce vs throttle:
     * <p>
     * <ul>
     * <li><a href="http://drupalmotion.com/article/debounce-and-throttle-visual-explanation">Debounce and Throttle: visual explanation</a></li>
     * <li><a href="http://unscriptable.com/2009/03/20/debouncing-javascript-methods/">Debouncing: javascript methods</a></li>
     * <li><a href="http://www.illyriad.co.uk/blog/index.php/2011/09/javascript-dont-spam-your-server-debounce-and-throttle/">Javascript - don't spam your server: debounce and throttle</a></li>
     * </ul>
     * 
     * @param timeout
     *            the length of the window of time that must pass after the emission of an item from
     *            the source Observable in which that Observable emits no items in order for the
     *            item to be emitted by the resulting Observable
     * @param unit
     *            the {@link TimeUnit} of {@code timeout}
     * @return an Observable that filters out items that are too quickly followed by newer items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-throttlewithtimeout-or-debounce">RxJava Wiki: throttleWithTimeout()</a>
     * @see #debounce(long, TimeUnit)
     */
    public final Observable<T> throttleWithTimeout(long timeout, TimeUnit unit) {
        return create(OperationDebounce.debounce(this, timeout, unit));
    }

    /**
     * Returns an Observable that only emits those items emitted by the source Observable that are
     * not followed by another emitted item within a specified time window, where the time window
     * is governed by a specified Scheduler.
     * <p>
     * <em>Note:</em> If the source Observable keeps emitting items more frequently than the length
     * of the time window then no items will be emitted by the resulting Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleWithTimeout.s.png">
     * <p>
     * Information on debounce vs throttle:
     * <p>
     * <ul>
     * <li><a href="http://drupalmotion.com/article/debounce-and-throttle-visual-explanation">Debounce and Throttle: visual explanation</a></li>
     * <li><a href="http://unscriptable.com/2009/03/20/debouncing-javascript-methods/">Debouncing: javascript methods</a></li>
     * <li><a href="http://www.illyriad.co.uk/blog/index.php/2011/09/javascript-dont-spam-your-server-debounce-and-throttle/">Javascript - don't spam your server: debounce and throttle</a></li>
     * </ul>
     * 
     * @param timeout
     *            the length of the window of time that must pass after the emission of an item from
     *            the source Observable in which that Observable emits no items in order for the
     *            item to be emitted by the resulting Observable
     * @param unit
     *            the {@link TimeUnit} of {@code timeout}
     * @param scheduler
     *            the {@link Scheduler} to use internally to manage the timers that handle the
     *            timeout for each item
     * @return an Observable that filters out items that are too quickly followed by newer items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-throttlewithtimeout-or-debounce">RxJava Wiki: throttleWithTimeout()</a>
     * @see #debounce(long, TimeUnit, Scheduler)
     */
    public final Observable<T> throttleWithTimeout(long timeout, TimeUnit unit, Scheduler scheduler) {
        return create(OperationDebounce.debounce(this, timeout, unit, scheduler));
    }

    /**
     * Returns an Observable that emits records of the time interval between consecutive items
     * emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeInterval.png">
     * 
     * @return an Observable that emits time interval information items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-timeinterval">RxJava Wiki: timeInterval()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212107.aspx">MSDN: Observable.TimeInterval</a>
     */
    public final Observable<TimeInterval<T>> timeInterval() {
        return create(OperationTimeInterval.timeInterval(this));
    }

    /**
     * Returns an Observable that emits records of the time interval between consecutive items
     * emitted by the source Observable, where this interval is computed on a specified Scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeInterval.s.png">
     * 
     * @param scheduler
     *            the {@link Scheduler} used to compute time intervals
     * @return an Observable that emits time interval information items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-timeinterval">RxJava Wiki: timeInterval()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212107.aspx">MSDN: Observable.TimeInterval</a>
     */
    public final Observable<TimeInterval<T>> timeInterval(Scheduler scheduler) {
        return create(OperationTimeInterval.timeInterval(this, scheduler));
    }

    /**
     * Returns an Observable that completes if either the first item emitted by the source
     * Observable or any subsequent item don't arrive within time windows defined by other
     * Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout5.png">
     * 
     * @param <U>
     *            the first timeout value type (ignored)
     * @param <V>
     *            the subsequent timeout value type (ignored)
     * @param firstTimeoutSelector
     *            a function that returns an Observable that determines the timeout window for the
     *            first source item
     * @param timeoutSelector
     *            a function that returns an Observable for each item emitted by the source
     *            Observable and that determines the timeout window in which the subsequent source
     *            item must arrive in order to continue the sequence
     * @return an Observable that completes if either the first item or any subsequent item doesn't
     *         arrive within the time windows specified by the timeout selectors
     */
    public final <U, V> Observable<T> timeout(Func0<? extends Observable<U>> firstTimeoutSelector, Func1<? super T, ? extends Observable<U>> timeoutSelector) {
        if (firstTimeoutSelector == null) {
            throw new NullPointerException("firstTimeoutSelector");
        }
        return timeout(firstTimeoutSelector, timeoutSelector, Observable.<T> empty());
    }

    /**
     * Returns an Observable that mirrors the source Observable, but switches to a fallback
     * Observable if either the first item emitted by the source Observable or any subsequent item
     * don't arrive within time windows defined by other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout6.png">
     * 
     * @param <U>
     *            the first timeout value type (ignored)
     * @param <V>
     *            the subsequent timeout value type (ignored)
     * @param firstTimeoutSelector
     *            a function that returns an Observable which determines the timeout window for the
     *            first source item
     * @param timeoutSelector
     *            a function that returns an Observable for each item emitted by the source
     *            Observable and that determines the timeout window in which the subsequent source
     *            item must arrive in order to continue the sequence
     * @param other
     *            the fallback Observable to switch to if the source Observable times out
     * @return an Observable that mirrors the source Observable, but switches to the {@code other} Observable if either the first item emitted by the source Observable or any
     *         subsequent item don't arrive within time windows defined by the timeout selectors
     */
    public final <U, V> Observable<T> timeout(Func0<? extends Observable<U>> firstTimeoutSelector, Func1<? super T, ? extends Observable<U>> timeoutSelector, Observable<? extends T> other) {
        if (firstTimeoutSelector == null) {
            throw new NullPointerException("firstTimeoutSelector");
        }
        if (other == null) {
            throw new NullPointerException("other");
        }
        return create(OperationTimeout.timeoutSelector(this, firstTimeoutSelector, timeoutSelector, other));
    }

    /**
     * Returns an Observable that mirrors the source Observable, but completes if an item emitted by
     * the source Observable doesn't arrive within a window of time after the emission of the
     * previous item, where that period of time is measured by an Observable that is a function
     * of the previous item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout3.png">
     * <p>
     * The arrival of the first source item is never timed out.
     * 
     * @param <U>
     *            the timeout value type (ignored)
     * @param timeoutSelector
     *            a function that returns an observable for each item emitted by the source
     *            Observable and that determines the timeout window for the subsequent item
     * @return an Observable that mirrors the source Observable, but completes if a item emitted by
     *         the source Observable takes longer to arrive than the time window defined by the
     *         selector for the previously emitted item
     */
    public final <U> Observable<T> timeout(Func1<? super T, ? extends Observable<U>> timeoutSelector) {
        return timeout(timeoutSelector, Observable.<T> empty());
    }

    /**
     * Returns an Observable that mirrors the source Observable, but that switches to a fallback
     * Observable if an item emitted by the source Observable doesn't arrive within a window of time
     * after the emission of the previous item, where that period of time is measured by an
     * Observable that is a function of the previous item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout4.png">
     * <p>
     * The arrival of the first source item is never timed out.
     * 
     * @param <U>
     *            the timeout value type (ignored)
     * @param timeoutSelector
     *            a function that returns an observable for each item emitted by the source
     *            Observable and that determines the timeout window for the subsequent item
     * @param other
     *            the fallback Observable to switch to if the source Observable times out
     * @return an Observable that mirrors the source Observable, but switches to mirroring a
     *         fallback Observable if a item emitted by the source Observable takes longer to arrive
     *         than the time window defined by the selector for the previously emitted item
     */
    public final <U> Observable<T> timeout(Func1<? super T, ? extends Observable<U>> timeoutSelector, Observable<? extends T> other) {
        if (other == null) {
            throw new NullPointerException("other");
        }
        return create(OperationTimeout.timeoutSelector(this, null, timeoutSelector, other));
    }

    /**
     * Returns an Observable that mirrors the source Observable but applies a timeout policy for
     * each emitted item. If the next item isn't emitted within the specified timeout duration
     * starting from its predecessor, the resulting Observable terminates and notifies observers of
     * a {@code TimeoutException}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout.1.png">
     * 
     * @param timeout
     *            maximum duration between emitted items before a timeout occurs
     * @param timeUnit
     *            the unit of time that applies to the {@code timeout} argument.
     * @return the source Observable modified to notify observers of a {@code TimeoutException} in
     *         case of a timeout
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-timeout">RxJava Wiki: timeout()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244283.aspx">MSDN: Observable.Timeout</a>
     */
    public final Observable<T> timeout(long timeout, TimeUnit timeUnit) {
        return create(OperationTimeout.timeout(this, timeout, timeUnit));
    }

    /**
     * Returns an Observable that mirrors the source Observable but applies a timeout policy for
     * each emitted item. If the next item isn't emitted within the specified timeout duration
     * starting from its predecessor, the resulting Observable begins instead to mirror a fallback
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout.2.png">
     * 
     * @param timeout
     *            maximum duration between items before a timeout occurs
     * @param timeUnit
     *            the unit of time that applies to the {@code timeout} argument
     * @param other
     *            the fallback Observable to use in case of a timeout
     * @return the source Observable modified to switch to the fallback Observable in case of a
     *         timeout
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-timeout">RxJava Wiki: timeout()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229512.aspx">MSDN: Observable.Timeout</a>
     */
    public final Observable<T> timeout(long timeout, TimeUnit timeUnit, Observable<? extends T> other) {
        return create(OperationTimeout.timeout(this, timeout, timeUnit, other));
    }

    /**
     * Returns an Observable that mirrors the source Observable but applies a timeout policy for
     * each emitted item using a specified Scheduler. If the next item isn't emitted within the
     * specified timeout duration starting from its predecessor, the resulting Observable begins
     * instead to mirror a fallback Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout.2s.png">
     * 
     * @param timeout
     *            maximum duration between items before a timeout occurs
     * @param timeUnit
     *            the unit of time that applies to the {@code timeout} argument
     * @param other
     *            the Observable to use as the fallback in case of a timeout
     * @param scheduler
     *            the {@link Scheduler} to run the timeout timers on
     * @return the source Observable modified so that it will switch to the fallback Observable in
     *         case of a timeout
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-timeout">RxJava Wiki: timeout()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211676.aspx">MSDN: Observable.Timeout</a>
     */
    public final Observable<T> timeout(long timeout, TimeUnit timeUnit, Observable<? extends T> other, Scheduler scheduler) {
        return create(OperationTimeout.timeout(this, timeout, timeUnit, other, scheduler));
    }

    /**
     * Returns an Observable that mirrors the source Observable but applies a timeout policy for
     * each emitted item, where this policy is governed on a specified Scheduler. If the next item
     * isn't emitted within the specified timeout duration starting from its predecessor, the
     * resulting Observable terminates and notifies observers of a {@code TimeoutException}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout.1s.png">
     * 
     * @param timeout
     *            maximum duration between items before a timeout occurs
     * @param timeUnit
     *            the unit of time that applies to the {@code timeout} argument
     * @param scheduler
     *            the Scheduler to run the timeout timers on
     * @return the source Observable modified to notify observers of a {@code TimeoutException} in
     *         case of a timeout
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-timeout">RxJava Wiki: timeout()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228946.aspx">MSDN: Observable.Timeout</a>
     */
    public final Observable<T> timeout(long timeout, TimeUnit timeUnit, Scheduler scheduler) {
        return create(OperationTimeout.timeout(this, timeout, timeUnit, scheduler));
    }

    /**
     * Returns an Observable that emits each item emitted by the source Observable, wrapped in a {@link Timestamped} object.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timestamp.png">
     * 
     * @return an Observable that emits timestamped items from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-timestamp">RxJava Wiki: timestamp()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229003.aspx">MSDN: Observable.Timestamp</a>
     */
    public final Observable<Timestamped<T>> timestamp() {
        return timestamp(Schedulers.immediate());
    }

    /**
     * Returns an Observable that emits each item emitted by the source Observable, wrapped in a {@link Timestamped} object whose timestamps are provided by a specified Scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timestamp.s.png">
     * 
     * @param scheduler
     *            the {@link Scheduler} to use as a time source
     * @return an Observable that emits timestamped items from the source Observable with timestamps
     *         provided by the {@code scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#wiki-timestamp">RxJava Wiki: timestamp()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229003.aspx">MSDN: Observable.Timestamp</a>
     */
    public final Observable<Timestamped<T>> timestamp(Scheduler scheduler) {
        return lift(new OperatorTimestamp<T>(scheduler));
    }

    /**
     * Converts an Observable into a {@link BlockingObservable} (an Observable with blocking
     * Observers).
     * 
     * @return a {@code BlockingObservable} version of this Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Blocking-Observable-Operators">RxJava Wiki: Blocking Observable Observers</a>
     */
    public final BlockingObservable<T> toBlockingObservable() {
        return BlockingObservable.from(this);
    }

    /**
     * Returns an Observable that emits a single item, a list composed of all the items emitted by
     * the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toList.png">
     * <p>
     * Normally, an Observable that returns multiple items will do so by invoking its {@link Observer}'s {@link Observer#onNext onNext} method for each such item. You can change
     * this behavior, instructing the Observable to compose a list of all of these items and then to
     * invoke the Observer's {@code onNext} function once, passing it the entire list, by calling
     * the Observable's {@code toList} method prior to calling its {@link #subscribe} method.
     * <p>
     * Be careful not to use this Observer on Observables that emit infinite or very large numbers
     * of items, as you do not have the option to unsubscribe.
     * 
     * @return an Observable that emits a single item: a List containing all of the items emitted by
     *         the source Observable.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-tolist">RxJava Wiki: toList()</a>
     */
    public final Observable<List<T>> toList() {
        return lift(new OperatorToObservableList<T>());
    }

    /**
     * Return an Observable that emits a single HashMap containing all items emitted by the source
     * Observable, mapped by the keys returned by a specified {@code keySelector} function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMap.png">
     * <p>
     * If more than one source item maps to the same key, the HashMap will contain the latest of
     * those items.
     * 
     * @param keySelector
     *            the function that extracts the key from a source item to be used in the HashMap
     * @return an Observable that emits a single item: a HashMap containing the mapped items from
     *         the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229137.aspx">MSDN: Observable.ToDictionary</a>
     */
    public final <K> Observable<Map<K, T>> toMap(Func1<? super T, ? extends K> keySelector) {
        return create(OperationToMap.toMap(this, keySelector));
    }

    /**
     * Return an Observable that emits a single HashMap containing values corresponding to items
     * emitted by the source Observable, mapped by the keys returned by a specified {@code keySelector} function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMap.png">
     * <p>
     * If more than one source item maps to the same key, the HashMap will contain a single entry
     * that corresponds to the latest of those items.
     * 
     * @param keySelector
     *            the function that extracts the key from a source item to be used in the HashMap
     * @param valueSelector
     *            the function that extracts the value from a source item to be used in the HashMap
     * @return an Observable that emits a single item: a HashMap containing the mapped items from
     *         the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212075.aspx">MSDN: Observable.ToDictionary</a>
     */
    public final <K, V> Observable<Map<K, V>> toMap(Func1<? super T, ? extends K> keySelector, Func1<? super T, ? extends V> valueSelector) {
        return create(OperationToMap.toMap(this, keySelector, valueSelector));
    }

    /**
     * Return an Observable that emits a single Map, returned by a specified {@code mapFactory} function, that contains keys and values extracted from the items emitted by the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMap.png">
     * 
     * @param keySelector
     *            the function that extracts the key from a source item to be used in the Map
     * @param valueSelector
     *            the function that extracts the value from the source items to be used as value in
     *            the Map
     * @param mapFactory
     *            the function that returns a Map instance to be used
     * @return an Observable that emits a single item: a Map that contains the mapped items emitted
     *         by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     */
    public final <K, V> Observable<Map<K, V>> toMap(Func1<? super T, ? extends K> keySelector, Func1<? super T, ? extends V> valueSelector, Func0<? extends Map<K, V>> mapFactory) {
        return create(OperationToMap.toMap(this, keySelector, valueSelector, mapFactory));
    }

    /**
     * Return an Observable that emits a single HashMap that contains an ArrayList of items emitted
     * by the source Observable keyed by a specified {@code keySelector} function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMultiMap.png">
     * 
     * @param keySelector
     *            the function that extracts the key from the source items to be used as key in the
     *            HashMap
     * @return an Observable that emits a single item: a HashMap that contains an ArrayList of items
     *         mapped from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212098.aspx">MSDN: Observable.ToLookup</a>
     */
    public final <K> Observable<Map<K, Collection<T>>> toMultimap(Func1<? super T, ? extends K> keySelector) {
        return create(OperationToMultimap.toMultimap(this, keySelector));
    }

    /**
     * Return an Observable that emits a single HashMap that contains an ArrayList of values
     * extracted by a specified {@code valueSelector} function from items emitted by the source
     * Observable, keyed by a specified {@code keySelector} function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMultiMap.png">
     * 
     * @param keySelector
     *            the function that extracts a key from the source items to be used as key in the
     *            HashMap
     * @param valueSelector
     *            the function that extracts a value from the source items to be used as value in
     *            the HashMap
     * @return an Observable that emits a single item: a HashMap that contains an ArrayList of items
     *         mapped from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229101.aspx">MSDN: Observable.ToLookup</a>
     */
    public final <K, V> Observable<Map<K, Collection<V>>> toMultimap(Func1<? super T, ? extends K> keySelector, Func1<? super T, ? extends V> valueSelector) {
        return create(OperationToMultimap.toMultimap(this, keySelector, valueSelector));
    }

    /**
     * Return an Observable that emits a single Map, returned by a specified {@code mapFactory} function, that contains an ArrayList of values, extracted by a specified {@code valueSelector} function
     * from items emitted by the source Observable and keyed by the {@code keySelector} function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMultiMap.png">
     * 
     * @param keySelector
     *            the function that extracts a key from the source items to be used as the key in
     *            the Map
     * @param valueSelector
     *            the function that extracts a value from the source items to be used as the value
     *            in the Map
     * @param mapFactory
     *            the function that returns a Map instance to be used
     * @return an Observable that emits a single item: a Map that contains a list items mapped
     *         from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     */
    public final <K, V> Observable<Map<K, Collection<V>>> toMultimap(Func1<? super T, ? extends K> keySelector, Func1<? super T, ? extends V> valueSelector, Func0<? extends Map<K, Collection<V>>> mapFactory) {
        return create(OperationToMultimap.toMultimap(this, keySelector, valueSelector, mapFactory));
    }

    /**
     * Return an Observable that emits a single Map, returned by a specified {@code mapFactory} function, that contains a custom collection of values, extracted by a specified {@code valueSelector}
     * function from items emitted by the source Observable, and keyed by the {@code keySelector} function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMultiMap.png">
     * 
     * @param keySelector
     *            the function that extracts a key from the source items to be used as the key in
     *            the Map
     * @param valueSelector
     *            the function that extracts a value from the source items to be used as the value
     *            in the Map
     * @param mapFactory
     *            the function that returns a Map instance to be used
     * @param collectionFactory
     *            the function that returns a Collection instance for a particular key to be used in
     *            the Map
     * @return an Observable that emits a single item: a Map that contains the collection of mapped
     *         items from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     */
    public final <K, V> Observable<Map<K, Collection<V>>> toMultimap(Func1<? super T, ? extends K> keySelector, Func1<? super T, ? extends V> valueSelector, Func0<? extends Map<K, Collection<V>>> mapFactory, Func1<? super K, ? extends Collection<V>> collectionFactory) {
        return create(OperationToMultimap.toMultimap(this, keySelector, valueSelector, mapFactory, collectionFactory));
    }

    /**
     * Returns an Observable that emits a list that contains the items emitted by the source
     * Observable, in a sorted order. Each item emitted by the Observable must implement {@link Comparable} with respect to all other items in the sequence.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toSortedList.png">
     * 
     * @throws ClassCastException
     *             if any item emitted by the Observable does not implement {@link Comparable} with
     *             respect to all other items emitted by the Observable
     * @return an Observable that emits a list that contains the items emitted by the source
     *         Observable in sorted order
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-tosortedlist">RxJava Wiki: toSortedList()</a>
     */
    public final Observable<List<T>> toSortedList() {
        return lift(new OperatorToObservableSortedList<T>());
    }

    /**
     * Returns an Observable that emits a list that contains the items emitted by the source
     * Observable, in a sorted order based on a specified comparison function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toSortedList.f.png">
     * 
     * @param sortFunction
     *            a function that compares two items emitted by the source Observable and returns an
     *            Integer that indicates their sort order
     * @return an Observable that emits a list that contains the items emitted by the source
     *         Observable in sorted order
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-tosortedlist">RxJava Wiki: toSortedList()</a>
     */
    public final Observable<List<T>> toSortedList(Func2<? super T, ? super T, Integer> sortFunction) {
        return lift(new OperatorToObservableSortedList<T>(sortFunction));
    }

    /**
     * Returns an Observable that represents a filtered version of the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/where.png">
     * 
     * @param predicate
     *            a function that evaluates an item emitted by the source Observable, returning {@code true} if it passes the filter
     * @return an Observable that emits only those items emitted by the source Observable that the
     *         filter evaluates as {@code true}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#wiki-filter-or-where">RxJava Wiki: where()</a>
     * @see #filter(Func1)
     */
    @Deprecated
    public final Observable<T> where(Func1<? super T, Boolean> predicate) {
        return filter(predicate);
    }

    /**
     * Returns an Observable that emits windows of items it collects from the source Observable.
     * The resulting Observable emits connected, non-overlapping windows. It emits the current
     * window and opens a new one when the Observable produced by the specified {@code closingSelector} emits an item. The {@code closingSelector} then creates a new
     * Observable to generate the closer of the next window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window1.png">
     * 
     * @param closingSelector
     *            a {@link Func0} that produces an Observable for every window created. When this
     *            Observable emits an item, {@code window()} emits the associated window and begins
     *            a new one.
     * @return an Observable that emits connected, non-overlapping windows of items from the source
     *         Observable when {@code closingSelector} emits an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-window">RxJava Wiki: window()</a>
     */
    public final <TClosing> Observable<Observable<T>> window(Func0<? extends Observable<? extends TClosing>> closingSelector) {
        return create(OperationWindow.window(this, closingSelector));
    }

    /**
     * Returns an Observable that emits windows of items it collects from the source Observable.
     * The resulting Observable emits connected, non-overlapping windows, each containing {@code count} items. When the source Observable completes or encounters an error, the
     * resulting Observable emits the current window and propagates the notification from the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window3.png">
     * 
     * @param count
     *            the maximum size of each window before it should be emitted
     * @return an Observable that emits connected, non-overlapping windows, each containing at most {@code count} items from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-window">RxJava Wiki: window()</a>
     */
    public final Observable<Observable<T>> window(int count) {
        return create(OperationWindow.window(this, count));
    }

    /**
     * Returns an Observable that emits windows of items it collects from the source Observable.
     * The resulting Observable emits windows every {@code skip} items, each containing
     * no more than {@code count} items. When the source Observable completes or encounters an
     * error, the resulting Observable emits the current window and propagates the notification from
     * the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window4.png">
     * 
     * @param count
     *            the maximum size of each window before it should be emitted
     * @param skip
     *            how many items need to be skipped before starting a new window. Note that if {@code skip} and {@code count} are equal this is the same operation as {@link #window(int)}.
     * @return an Observable that emits windows every {@code skip} items containing at most {@code count} items from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-window">RxJava Wiki: window()</a>
     */
    public final Observable<Observable<T>> window(int count, int skip) {
        return create(OperationWindow.window(this, count, skip));
    }

    /**
     * Returns an Observable that emits windows of items it collects from the source Observable.
     * The resulting Observable starts a new window periodically, as determined by the {@code timeshift} argument. It emits each window after a fixed timespan, specified by the {@code timespan}
     * argument. When the source Observable completes or Observable completes or
     * encounters an error, the resulting Observable emits the current window and propagates the
     * notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window7.png">
     * 
     * @param timespan
     *            the period of time each window collects items before it should be emitted
     * @param timeshift
     *            the period of time after which a new window will be created
     * @param unit
     *            the unit of time that applies to the {@code timespan} and {@code timeshift} arguments
     * @return an Observable that emits new windows periodically as a fixed timespan elapses
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-window">RxJava Wiki: window()</a>
     */
    public final Observable<Observable<T>> window(long timespan, long timeshift, TimeUnit unit) {
        return create(OperationWindow.window(this, timespan, timeshift, unit));
    }

    /**
     * Returns an Observable that emits windows of items it collects from the source Observable.
     * The resulting Observable starts a new window periodically, as determined by the {@code timeshift} argument. It emits each window after a fixed timespan, specified by the {@code timespan}
     * argument. When the source Observable completes or Observable completes or
     * encounters an error, the resulting Observable emits the current window and propagates the
     * notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window7.s.png">
     * 
     * @param timespan
     *            the period of time each window collects items before it should be emitted
     * @param timeshift
     *            the period of time after which a new window will be created
     * @param unit
     *            the unit of time that applies to the {@code timespan} and {@code timeshift} arguments
     * @param scheduler
     *            the {@link Scheduler} to use when determining the end and start of a window
     * @return an Observable that emits new windows periodically as a fixed timespan elapses
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-window">RxJava Wiki: window()</a>
     */
    public final Observable<Observable<T>> window(long timespan, long timeshift, TimeUnit unit, Scheduler scheduler) {
        return create(OperationWindow.window(this, timespan, timeshift, unit, scheduler));
    }

    /**
     * Returns an Observable that emits windows of items it collects from the source Observable.
     * The resulting Observable emits connected, non-overlapping windows, each of a fixed duration
     * specified by the {@code timespan} argument. When the source Observable completes or
     * encounters an error, the resulting Observable emits the current window and propagates the
     * notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window5.png">
     * 
     * @param timespan
     *            the period of time each window collects items before it should be emitted and
     *            replaced with a new window
     * @param unit
     *            the unit of time that applies to the {@code timespan} argument
     * @return an Observable that emits connected, non-overlapping windows represending items
     *         emitted by the source Observable during fixed, consecutive durations
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-window">RxJava Wiki: window()</a>
     */
    public final Observable<Observable<T>> window(long timespan, TimeUnit unit) {
        return create(OperationWindow.window(this, timespan, unit));
    }

    /**
     * Returns an Observable that emits windows of items it collects from the source Observable.
     * The resulting Observable emits connected, non-overlapping windows, each of a fixed duration
     * as specified by the {@code timespan} argument or a maximum size as specified by the {@code count} argument (whichever is reached first). When the source Observable completes or
     * encounters an error, the resulting Observable emits the current window and propagates the
     * notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window6.png">
     * 
     * @param timespan
     *            the period of time each window collects items before it should be emitted and
     *            replaced with a new window
     * @param unit
     *            the unit of time that applies to the {@code timespan} argument
     * @param count
     *            the maximum size of each window before it should be emitted
     * @return an Observable that emits connected, non-overlapping windows of items from the source
     *         Observable that were emitted during a fixed duration of time or when the window has
     *         reached maximum capacity (whichever occurs first)
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-window">RxJava Wiki: window()</a>
     */
    public final Observable<Observable<T>> window(long timespan, TimeUnit unit, int count) {
        return create(OperationWindow.window(this, timespan, unit, count));
    }

    /**
     * Returns an Observable that emits windows of items it collects from the source Observable. The
     * resulting Observable emits connected, non-overlapping windows, each of a fixed duration
     * specified by the {@code timespan} argument or a maximum size specified by the {@code count} argument (whichever is reached first). When the source Observable completes or encounters an
     * error, the resulting Observable emits the current window and propagates the notification from
     * the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window6.s.png">
     * 
     * @param timespan
     *            the period of time each window collects items before it should be emitted and
     *            replaced with a new window
     * @param unit
     *            the unit of time which applies to the {@code timespan} argument
     * @param count
     *            the maximum size of each window before it should be emitted
     * @param scheduler
     *            the {@link Scheduler} to use when determining the end and start of a window
     * @return an Observable that emits connected, non-overlapping windows of items from the source
     *         Observable that were emitted during a fixed duration of time or when the window has
     *         reached maximum capacity (whichever occurs first)
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-window">RxJava Wiki: window()</a>
     */
    public final Observable<Observable<T>> window(long timespan, TimeUnit unit, int count, Scheduler scheduler) {
        return create(OperationWindow.window(this, timespan, unit, count, scheduler));
    }

    /**
     * Returns an Observable that emits windows of items it collects from the source Observable.
     * The resulting Observable emits connected, non-overlapping windows, each of a fixed duration
     * as specified by the {@code timespan} argument. When the source Observable completes or
     * encounters an error, the resulting Observable emits the current window and propagates the
     * notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window5.s.png">
     * 
     * @param timespan
     *            the period of time each window collects items before it should be emitted and
     *            replaced with a new window
     * @param unit
     *            the unit of time which applies to the {@code timespan} argument
     * @param scheduler
     *            the {@link Scheduler} to use when determining the end and start of a window
     * @return an Observable that emits connected, non-overlapping windows containing items emitted
     *         by the source Observable within a fixed duration
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-window">RxJava Wiki: window()</a>
     */
    public final Observable<Observable<T>> window(long timespan, TimeUnit unit, Scheduler scheduler) {
        return create(OperationWindow.window(this, timespan, unit, scheduler));
    }

    /**
     * Returns an Observable that emits windows of items it collects from the source Observable.
     * The resulting Observable emits windows that contain those items emitted by the source
     * Observable between the time when the {@code windowOpenings} Observable emits an item and when
     * the Observable returned by {@code closingSelector} emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window2.png">
     * 
     * @param windowOpenings
     *            an Observable that, when it emits an item, causes another window to be created
     * @param closingSelector
     *            a {@link Func1} that produces an Observable for every window created. When this
     *            Observable emits an item, the associated window is closed and emitted
     * @return an Observable that emits windows of items emitted by the source Observable that are
     *         governed by the specified window-governing Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#wiki-window">RxJava Wiki: window()</a>
     */
    public final <TOpening, TClosing> Observable<Observable<T>> window(Observable<? extends TOpening> windowOpenings, Func1<? super TOpening, ? extends Observable<? extends TClosing>> closingSelector) {
        return create(OperationWindow.window(this, windowOpenings, closingSelector));
    }

    /**
     * Returns an Observable that emits non-overlapping windows of items it collects from the
     * source observable where the boundary of each window is determined by the items emitted from
     * a specified boundary-governing Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window8.png">
     * 
     * @param <U>
     *            the window element type (ignored)
     * @param boundary
     *            an Observable whose emitted items close and open windows
     * @return an Observable that emits non-overlapping windows of items it collects from the source
     *         Observable where the boundary of each window is determined by the items emitted from
     *         the {@code boundary} Observable
     */
    public final <U> Observable<Observable<T>> window(Observable<U> boundary) {
        return create(OperationWindow.window(this, boundary));
    }

    /**
     * Returns an Observable that emits items that are the result of applying a specified function
     * to pairs of values, one each from the source Observable and a specified Iterable sequence.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.i.png">
     * <p>
     * Note that the {@code other} Iterable is evaluated as items are observed from the source
     * Observable; it is not pre-consumed. This allows you to zip infinite streams on either side.
     * 
     * @param <T2>
     *            the type of items in the {@code other} Iterable
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param other
     *            the Iterable sequence
     * @param zipFunction
     *            a function that combines the pairs of items from the Observable and the Iterable
     *            to generate the items to be emitted by the resulting Observable
     * @return an Observable that pairs up values from the source Observable and the {@code other} Iterable sequence and emits the results of {@code zipFunction} applied to these pairs
     */
    public final <T2, R> Observable<R> zip(Iterable<? extends T2> other, Func2<? super T, ? super T2, ? extends R> zipFunction) {
        return lift(new OperatorZipIterable<T, T2, R>(other, zipFunction));
    }

    /**
     * Returns an Observable that emits items that are the result of applying a specified function
     * to pairs of values, one each from the source Observable and another specified Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * 
     * @param <T2>
     *            the type of items emitted by the {@code other} Observable
     * @param <R>
     *            the type of items emitted by the resulting Observable
     * @param other
     *            the other Observable
     * @param zipFunction
     *            a function that combines the pairs of items from the two Observables to generate
     *            the items to be emitted by the resulting Observable
     * @return an Observable that pairs up values from the source Observable and the {@code other} Observable and emits the results of {@code zipFunction} applied to these pairs
     */
    public final <T2, R> Observable<R> zip(Observable<? extends T2> other, Func2<? super T, ? super T2, ? extends R> zipFunction) {
        return zip(this, other, zipFunction);
    }

    /**
     * An Observable that never sends any information to an {@link Observer}.
     * 
     * This Observable is useful primarily for testing purposes.
     * 
     * @param <T>
     *            the type of item (not) emitted by the Observable
     */
    private static class NeverObservable<T> extends Observable<T> {
        public NeverObservable() {
            super(new OnSubscribe<T>() {

                @Override
                public void call(Subscriber<? super T> observer) {
                    // do nothing
                }

            });
        }
    }

    /**
     * An Observable that invokes {@link Observer#onError onError} when the {@link Observer} subscribes to it.
     * 
     * @param <T>
     *            the type of item (ostensibly) emitted by the Observable
     */
    private static class ThrowObservable<T> extends Observable<T> {

        public ThrowObservable(final Throwable exception) {
            super(new OnSubscribe<T>() {

                /**
                 * Accepts an {@link Observer} and calls its {@link Observer#onError onError} method.
                 * 
                 * @param observer
                 *            an {@link Observer} of this Observable
                 * @return a reference to the subscription
                 */
                @Override
                public void call(Subscriber<? super T> observer) {
                    observer.onError(exception);
                }

            });
        }
    }

    @SuppressWarnings("rawtypes")
    private final static ConcurrentHashMap<Class, Boolean> internalClassMap = new ConcurrentHashMap<Class, Boolean>();

    /**
     * Whether a given {@link Function} is an internal implementation inside rx.* packages or not.
     * <p>
     * For why this is being used see https://github.com/Netflix/RxJava/issues/216 for discussion on
     * "Guideline 6.4: Protect calls to user code from within an Observer"
     * <p>
     * Note: If strong reasons for not depending on package names comes up then the implementation
     * of this method can change to looking for a marker interface.
     * 
     * @param o
     *            FIXME FIXME FIXME
     * @return {@code true} if the given function is an internal implementation, and {@code false} otherwise
     */
    private boolean isInternalImplementation(Object o) {
        if (o == null) {
            return true;
        }
        // prevent double-wrapping (yeah it happens)
        if (o instanceof SafeSubscriber) {
            return true;
        }

        Class<?> clazz = o.getClass();
        if (internalClassMap.containsKey(clazz)) {
            //don't need to do reflection
            return internalClassMap.get(clazz);
        } else {
            // we treat the following package as "internal" and don't wrap it
            Package p = o.getClass().getPackage(); // it can be null
            Boolean isInternal = (p != null && p.getName().startsWith("rx.operators"));
            internalClassMap.put(clazz, isInternal);
            return isInternal;
        }
    }
}
