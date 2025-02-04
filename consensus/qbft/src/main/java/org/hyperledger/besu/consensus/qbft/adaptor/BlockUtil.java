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
package org.hyperledger.besu.consensus.qbft.adaptor;

import org.hyperledger.besu.consensus.qbft.core.types.QbftBlock;
import org.hyperledger.besu.ethereum.core.Block;

/** Utility class to convert between Besu and QBFT blocks. */
public class BlockUtil {

  /** Private constructor to prevent instantiation. */
  private BlockUtil() {}

  /**
   * Convert a QBFT block to a Besu block.
   *
   * @param block the QBFT block
   * @return the Besu block
   */
  public static Block toBesuBlock(final QbftBlock block) {
    if (block instanceof QbftBlockAdaptor) {
      return ((QbftBlockAdaptor) block).getBesuBlock();
    } else {
      throw new IllegalArgumentException("Unsupported block type");
    }
  }
}
