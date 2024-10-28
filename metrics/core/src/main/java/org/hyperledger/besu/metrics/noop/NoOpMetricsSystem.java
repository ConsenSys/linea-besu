/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.metrics.noop;

import org.hyperledger.besu.metrics.ObservableMetricsSystem;
import org.hyperledger.besu.metrics.Observation;
import org.hyperledger.besu.plugin.services.metrics.Counter;
import org.hyperledger.besu.plugin.services.metrics.Histogram;
import org.hyperledger.besu.plugin.services.metrics.LabelledGauge;
import org.hyperledger.besu.plugin.services.metrics.LabelledMetric;
import org.hyperledger.besu.plugin.services.metrics.MetricCategory;
import org.hyperledger.besu.plugin.services.metrics.OperationTimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

/** The NoOp metrics system. */
public class NoOpMetricsSystem implements ObservableMetricsSystem {

  /** The constant NO_OP_COUNTER. */
  public static final Counter NO_OP_COUNTER = new NoOpCounter();

  /** The constant NO_OP_GAUGE. */
  public static final LabelledGauge NO_OP_GAUGE = new NoOpValueCollector();

  private static final OperationTimer.TimingContext NO_OP_TIMING_CONTEXT = () -> 0;

  /** The constant NO_OP_OPERATION_TIMER. */
  public static final OperationTimer NO_OP_OPERATION_TIMER = () -> NO_OP_TIMING_CONTEXT;

  /** The constant NO_OP_HISTOGRAM. */
  public static final Histogram NO_OP_HISTOGRAM = d -> {};

  /** The constant NO_OP_LABELLED_1_COUNTER. */
  public static final LabelledMetric<Counter> NO_OP_LABELLED_1_COUNTER =
      new LabelCountingNoOpMetric<>(1, NO_OP_COUNTER);

  /** The constant NO_OP_LABELLED_2_COUNTER. */
  public static final LabelledMetric<Counter> NO_OP_LABELLED_2_COUNTER =
      new LabelCountingNoOpMetric<>(2, NO_OP_COUNTER);

  /** The constant NO_OP_LABELLED_3_COUNTER. */
  public static final LabelledMetric<Counter> NO_OP_LABELLED_3_COUNTER =
      new LabelCountingNoOpMetric<>(3, NO_OP_COUNTER);

  /** The constant NO_OP_LABELLED_1_OPERATION_TIMER. */
  public static final LabelledMetric<OperationTimer> NO_OP_LABELLED_1_OPERATION_TIMER =
      new LabelCountingNoOpMetric<>(1, NO_OP_OPERATION_TIMER);

  /** The constant NO_OP_LABELLED_1_HISTOGRAM. */
  public static final LabelledMetric<Histogram> NO_OP_LABELLED_1_HISTOGRAM =
      new LabelCountingNoOpMetric<>(1, NO_OP_HISTOGRAM);

  /** The constant NO_OP_LABELLED_1_GAUGE. */
  public static final LabelledGauge NO_OP_LABELLED_1_GAUGE =
      new LabelledGaugeNoOpMetric(1, NO_OP_GAUGE);

  /** The constant NO_OP_LABELLED_2_GAUGE. */
  public static final LabelledGauge NO_OP_LABELLED_2_GAUGE =
      new LabelledGaugeNoOpMetric(2, NO_OP_GAUGE);

  /** The constant NO_OP_LABELLED_3_GAUGE. */
  public static final LabelledGauge NO_OP_LABELLED_3_GAUGE =
      new LabelledGaugeNoOpMetric(3, NO_OP_GAUGE);

  /** Default constructor */
  public NoOpMetricsSystem() {}

  @Override
  public LabelledMetric<Counter> createLabelledCounter(
      final MetricCategory category,
      final String name,
      final String help,
      final String... labelNames) {
    return getCounterLabelledMetric(labelNames.length);
  }

  /**
   * Gets counter labelled metric.
   *
   * @param labelCount the label count
   * @return the counter labelled metric
   */
  public static LabelledMetric<Counter> getCounterLabelledMetric(final int labelCount) {
    switch (labelCount) {
      case 1:
        return NO_OP_LABELLED_1_COUNTER;
      case 2:
        return NO_OP_LABELLED_2_COUNTER;
      case 3:
        return NO_OP_LABELLED_3_COUNTER;
      default:
        return new LabelCountingNoOpMetric<>(labelCount, NO_OP_COUNTER);
    }
  }

  @Override
  public LabelledMetric<OperationTimer> createSimpleLabelledTimer(
      final MetricCategory category,
      final String name,
      final String help,
      final String... labelNames) {
    return getOperationTimerLabelledMetric(labelNames.length);
  }

  @Override
  public LabelledMetric<OperationTimer> createLabelledTimer(
      final MetricCategory category,
      final String name,
      final String help,
      final String... labelNames) {
    return getOperationTimerLabelledMetric(labelNames.length);
  }

  /**
   * Gets operation timer labelled metric.
   *
   * @param labelCount the label count
   * @return the operation timer labelled metric
   */
  public static LabelledMetric<OperationTimer> getOperationTimerLabelledMetric(
      final int labelCount) {
    if (labelCount == 1) {
      return NO_OP_LABELLED_1_OPERATION_TIMER;
    } else {
      return new LabelCountingNoOpMetric<>(labelCount, NO_OP_OPERATION_TIMER);
    }
  }

  /**
   * Gets histogram labelled metric.
   *
   * @param labelCount the label count
   * @return the histogram labelled metric
   */
  public static LabelledMetric<Histogram> getHistogramLabelledMetric(final int labelCount) {
    if (labelCount == 1) {
      return NO_OP_LABELLED_1_HISTOGRAM;
    } else {
      return new LabelCountingNoOpMetric<>(labelCount, NO_OP_HISTOGRAM);
    }
  }

  @Override
  public void createGauge(
      final MetricCategory category,
      final String name,
      final String help,
      final DoubleSupplier valueSupplier) {}

  @Override
  public LabelledMetric<Histogram> createLabelledHistogram(
      final MetricCategory category,
      final String name,
      final String help,
      final double[] buckets,
      final String... labelNames) {
    return null;
  }

  @Override
  public LabelledGauge createLabelledGauge(
      final MetricCategory category,
      final String name,
      final String help,
      final String... labelNames) {
    return getLabelledGauge(labelNames.length);
  }

  /**
   * Gets labelled gauge.
   *
   * @param labelCount the label count
   * @return the labelled gauge
   */
  public static LabelledGauge getLabelledGauge(final int labelCount) {
    switch (labelCount) {
      case 1:
        return NO_OP_LABELLED_1_GAUGE;
      case 2:
        return NO_OP_LABELLED_2_GAUGE;
      case 3:
        return NO_OP_LABELLED_3_GAUGE;
      default:
        return new LabelledGaugeNoOpMetric(labelCount, NO_OP_GAUGE);
    }
  }

  @Override
  public Stream<Observation> streamObservations(final MetricCategory category) {
    return Stream.empty();
  }

  @Override
  public Stream<Observation> streamObservations() {
    return Stream.empty();
  }

  @Override
  public Set<MetricCategory> getEnabledCategories() {
    return Collections.emptySet();
  }

  /**
   * The Label counting NoOp metric.
   *
   * @param <T> the type parameter
   */
  public static class LabelCountingNoOpMetric<T> implements LabelledMetric<T> {

    /** The Label count. */
    final int labelCount;

    /** The Fake metric. */
    final T fakeMetric;

    /**
     * Instantiates a new Label counting NoOp metric.
     *
     * @param labelCount the label count
     * @param fakeMetric the fake metric
     */
    LabelCountingNoOpMetric(final int labelCount, final T fakeMetric) {
      this.labelCount = labelCount;
      this.fakeMetric = fakeMetric;
    }

    @Override
    public T labels(final String... labels) {
      Preconditions.checkArgument(
          labels.length == labelCount,
          "The count of labels used must match the count of labels expected.");
      return fakeMetric;
    }
  }

  /** The Labelled gauge NoOp metric. */
  public static class LabelledGaugeNoOpMetric implements LabelledGauge {
    /** The Label count. */
    final int labelCount;

    /** The Label values cache. */
    final List<String> labelValuesCache = new ArrayList<>();

    /**
     * Instantiates a new Labelled gauge NoOp metric.
     *
     * @param labelCount the label count
     * @param fakeMetric the fake metric
     */
    public LabelledGaugeNoOpMetric(final int labelCount, final LabelledGauge fakeMetric) {
      this.labelCount = labelCount;
      this.fakeMetric = fakeMetric;
    }

    /** The Fake metric. */
    final LabelledGauge fakeMetric;

    @Override
    public void labels(final DoubleSupplier valueSupplier, final String... labelValues) {
      final String labelValuesString = String.join(",", labelValues);
      Preconditions.checkArgument(
          !labelValuesCache.contains(labelValuesString),
          "Received label values that were already in use " + labelValuesString);
      Preconditions.checkArgument(
          labelValues.length == labelCount,
          "The count of labels used must match the count of labels expected.");
      Preconditions.checkNotNull(valueSupplier, "No valueSupplier specified");
    }

    @Override
    public boolean isLabelsObserved(final String... labelValues) {
      Preconditions.checkArgument(
          labelValues.length == labelCount,
          "The count of labels used must match the count of labels expected.");
      final String labelValuesString = String.join(",", labelValues);
      return labelValuesCache.contains(labelValuesString);
    }
  }
}
