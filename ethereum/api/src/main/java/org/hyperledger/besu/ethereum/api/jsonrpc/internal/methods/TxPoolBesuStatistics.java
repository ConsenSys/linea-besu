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
package org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods;

import org.hyperledger.besu.ethereum.api.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.results.PendingTransactionsStatisticsResult;
import org.hyperledger.besu.ethereum.eth.transactions.PendingTransaction;
import org.hyperledger.besu.ethereum.eth.transactions.PendingTransactions;

import java.util.Collection;

public class TxPoolBesuStatistics implements JsonRpcMethod {

  private final PendingTransactions pendingTransactions;

  public TxPoolBesuStatistics(final PendingTransactions pendingTransactions) {
    this.pendingTransactions = pendingTransactions;
  }

  @Override
  public String getName() {
    return RpcMethod.TX_POOL_BESU_STATISTICS.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequestContext requestContext) {
    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), statistics());
  }

  private PendingTransactionsStatisticsResult statistics() {
    final Collection<PendingTransaction> pendingTransaction =
        pendingTransactions.getPendingTransactions();
    final long localCount =
        pendingTransaction.stream().filter(PendingTransaction::isReceivedFromLocalSource).count();
    final long remoteCount = pendingTransaction.size() - localCount;
    return new PendingTransactionsStatisticsResult(
        pendingTransactions.maxSize(), localCount, remoteCount);
  }
}
