/*
 * Copyright contributors to Besu.
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
package org.hyperledger.besu.plugin.services.txselection;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages the state of transaction selectors (including the plugin transaction selector {@link
 * PluginTransactionSelector}) during the block creation process. Some selectors have a state, for
 * example the amount of gas used by selected pending transactions so far, and changes made to the
 * state must be only commited after the evaluated pending transaction has been definitely selected
 * for inclusion, until that point it will be always possible to rollback the changes to the state
 * and return the previous commited state.
 */
@SuppressWarnings("rawtypes")
public class SelectorsStateManager {
  private final List<Map<TransactionSelector, DuplicableState>> uncommitedStates =
      new ArrayList<>();
  private Map<TransactionSelector, DuplicableState> committedState = new HashMap<>();
  private volatile boolean blockSelectionStarted = false;

  /** Create an empty selectors state manager, here to make javadoc linter happy. */
  public SelectorsStateManager() {}

  /**
   * Create, initialize and track the state for a selector.
   *
   * <p>Call to this method must be performed before the block selection is stated with {@link
   * #blockSelectionStarted()}, otherwise it fails.
   *
   * @param selector the selector
   * @param initialState the initial value of the state
   * @param <S> the type of the selector state
   */
  public <S extends DuplicableState> void createSelectorState(
      final TransactionSelector selector, final S initialState) {
    checkState(
        !blockSelectionStarted, "Cannot create selector state after block selection is started");
    committedState.put(selector, initialState);
  }

  /**
   * Called at the start of block selection, when the initialization is done, to prepare a new
   * working state based on the initial state.
   *
   * <p>After this method is called, it is not possible to call anymore {@link
   * #createSelectorState(TransactionSelector, DuplicableState)}
   */
  public void blockSelectionStarted() {
    blockSelectionStarted = true;
    uncommitedStates.add(duplicateLastState());
  }

  private Map<TransactionSelector, DuplicableState> duplicateLastState() {
    return getLast().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().deepCopy()));
  }

  /**
   * Get the working state for the specified selector
   *
   * @param selector the selector
   * @return the working state of the selector
   * @param <S> the type of the selector state
   */
  @SuppressWarnings("unchecked")
  public <S extends DuplicableState> S getSelectorWorkingState(final TransactionSelector selector) {
    return (S) uncommitedStates.getLast().get(selector);
  }

  /**
   * Get the commited state for the specified selector
   *
   * @param selector the selector
   * @return the commited state of the selector
   * @param <S> the type of the selector state
   */
  @SuppressWarnings("unchecked")
  public <S extends DuplicableState> S getSelectorCommittedState(
      final TransactionSelector selector) {
    return (S) committedState.get(selector);
  }

  /**
   * Commit the current working state and prepare a new working state based on the just commited
   * state
   */
  public void commit() {
    committedState = getLast();
    uncommitedStates.clear();
    uncommitedStates.add(duplicateLastState());
  }

  /**
   * Discards the current working state and prepare a new working state based on the just commited
   * state
   */
  public void rollback() {
    uncommitedStates.clear();
    uncommitedStates.add(duplicateLastState());
  }

  private Map<TransactionSelector, DuplicableState> getLast() {
    if (uncommitedStates.isEmpty()) {
      return committedState;
    }
    return uncommitedStates.getLast();
  }

  /**
   * A duplicate state object is one that is able to return, update and duplicate the state it
   * contains
   *
   * @param <S> the type of the state
   */
  public abstract static class DuplicableState<S> {
    private S state;

    /**
     * Create a duplicate state with the initial value
     *
     * @param state the initial state
     */
    public DuplicableState(final S state) {
      this.state = state;
    }

    /**
     * The method that concrete classes must implement to create a deep copy of the state
     *
     * @return a new duplicable state with a deep copy of the state
     */
    protected abstract DuplicableState<S> deepCopy();

    /**
     * Get the current state
     *
     * @return the current state
     */
    public S getState() {
      return state;
    }

    /**
     * Replace the current state with the passed one
     *
     * @param state the new state
     */
    public void setState(final S state) {
      this.state = state;
    }
  }

  /** A duplicable state containing a long */
  public static class DuplicableLongState extends DuplicableState<Long> {

    /**
     * Create a duplicate long state with the initial state
     *
     * @param state the initial state
     */
    public DuplicableLongState(final Long state) {
      super(state);
    }

    /**
     * Create a deep copy of this duplicable long state
     *
     * @return a copy of this duplicable long state
     */
    @Override
    public DuplicableLongState deepCopy() {
      return new DuplicableLongState(getState());
    }
  }
}
