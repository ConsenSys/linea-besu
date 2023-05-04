/*
 * Copyright Hyperledger Besu Contributors.
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
package org.hyperledger.besu.ethereum.mainnet;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.ethereum.linea.CalldataLimits;
import org.hyperledger.besu.ethereum.linea.LineaBlockBodyValidator;
import org.hyperledger.besu.ethereum.linea.LineaParameters;
import org.hyperledger.besu.ethereum.linea.LineaTransactionValidator;
import org.hyperledger.besu.ethereum.mainnet.feemarket.FeeMarket;
import org.hyperledger.besu.evm.MainnetEVMs;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.plugin.data.TransactionType;

import java.math.BigInteger;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

public class LineaProtocolSpecs {
  private static final int LINEA_BLOCK_MAX_CALLDATA_SIZE = 70000;
  private static final int LINEA_TX_MAX_CALLDATA_SIZE = 60000;

  private LineaProtocolSpecs() {}

  static ProtocolSpecBuilder lineaDefinition(
      final Optional<BigInteger> chainId,
      final OptionalInt configContractSizeLimit,
      final OptionalInt configStackSizeLimit,
      final boolean enableRevertReason,
      final GenesisConfigOptions genesisConfigOptions,
      final EvmConfiguration evmConfiguration,
      final LineaParameters lineaParameters) {

    // calldata limits overridden?
    final int txCalldataMaxSize =
        lineaParameters.maybeTransactionCalldataMaxSize().orElse(LINEA_TX_MAX_CALLDATA_SIZE);
    final int blockCalldataMaxSize =
        lineaParameters.maybeBlockCalldataMaxSize().orElse(LINEA_BLOCK_MAX_CALLDATA_SIZE);
    final CalldataLimits calldataLimits =
        new CalldataLimits(txCalldataMaxSize, blockCalldataMaxSize);

    final FeeMarket zeroBaseFeeMarket = FeeMarket.zeroBaseFee(0L);

    return MainnetProtocolSpecs.parisDefinition(
            chainId,
            configContractSizeLimit,
            configStackSizeLimit,
            enableRevertReason,
            genesisConfigOptions,
            evmConfiguration)
        .feeMarket(zeroBaseFeeMarket)
        .calldataLimits(calldataLimits)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LineaTransactionValidator(
                    gasCalculator,
                    gasLimitCalculator,
                    zeroBaseFeeMarket,
                    true,
                    chainId,
                    Set.of(
                        TransactionType.FRONTIER,
                        TransactionType.ACCESS_LIST,
                        TransactionType.EIP1559),
                    txCalldataMaxSize))
        .blockBodyValidatorBuilder(LineaBlockBodyValidator::new)
        // some Linea evm opcodes behave differently.
        .evmBuilder(
            (gasCalculator, jdCacheConfig) ->
                MainnetEVMs.linea(gasCalculator, chainId.orElse(BigInteger.ZERO), evmConfiguration))
        .name("Linea");
  }
}
