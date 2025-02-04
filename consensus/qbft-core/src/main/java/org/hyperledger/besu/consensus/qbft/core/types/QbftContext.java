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
package org.hyperledger.besu.consensus.qbft.core.types;

import org.hyperledger.besu.consensus.common.validator.ValidatorProvider;
import org.hyperledger.besu.ethereum.ConsensusContext;

/** Holds the QBFT specific mutable state. */
public class QbftContext implements ConsensusContext {

  private final ValidatorProvider validatorProvider;
  private final QbftBlockInterface blockInterface;

  /**
   * Instantiates a new Bft context.
   *
   * @param validatorProvider the validator provider
   * @param blockInterface the block interface
   */
  public QbftContext(
      final ValidatorProvider validatorProvider, final QbftBlockInterface blockInterface) {
    this.validatorProvider = validatorProvider;
    this.blockInterface = blockInterface;
  }

  /**
   * Gets validator provider.
   *
   * @return the validator provider
   */
  public ValidatorProvider getValidatorProvider() {
    return validatorProvider;
  }

  /**
   * Gets block interface.
   *
   * @return the block interface
   */
  public QbftBlockInterface getBlockInterface() {
    return blockInterface;
  }

  @Override
  public <C extends ConsensusContext> C as(final Class<C> klass) {
    return klass.cast(this);
  }
}
