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
package org.hyperledger.besu.consensus.qbft.jsonrpc.methods;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.consensus.common.validator.ValidatorProvider;
import org.hyperledger.besu.consensus.common.validator.VoteProvider;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.exception.InvalidJsonRpcParameters;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.datatypes.rpc.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.RpcErrorType;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QbftProposeValidatorVoteTest {
  private final ValidatorProvider validatorProvider = mock(ValidatorProvider.class);
  private final VoteProvider voteProvider = mock(VoteProvider.class);
  private final String QBFT_METHOD = "qbft_proposeValidatorVote";
  private final String JSON_RPC_VERSION = "2.0";
  private QbftProposeValidatorVote method;

  @BeforeEach
  public void setup() {
    method = new QbftProposeValidatorVote(validatorProvider);
    when(validatorProvider.getVoteProviderAtHead()).thenReturn(Optional.of(voteProvider));
  }

  @Test
  public void returnsCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(QBFT_METHOD);
  }

  @Test
  public void exceptionWhenNoParamsSupplied() {
    assertThatThrownBy(() -> method.response(requestWithParams()))
        .isInstanceOf(InvalidJsonRpcParameters.class)
        .hasMessage("Missing required json rpc parameter at index 0");
  }

  @Test
  public void exceptionWhenNoAuthSupplied() {
    assertThatThrownBy(() -> method.response(requestWithParams(Address.fromHexString("1"))))
        .isInstanceOf(InvalidJsonRpcParameters.class)
        .hasMessage("Missing required json rpc parameter at index 1");
  }

  @Test
  public void exceptionWhenNoAddressSupplied() {
    assertThatThrownBy(() -> method.response(requestWithParams("true")))
        .isInstanceOf(InvalidJsonRpcParameters.class)
        .hasMessageContaining("Invalid json rpc parameter at index 0");
  }

  @Test
  public void exceptionWhenInvalidBoolParameterSupplied() {
    assertThatThrownBy(() -> method.response(requestWithParams(Address.fromHexString("1"), "c")))
        .isInstanceOf(InvalidJsonRpcParameters.class)
        .hasMessageContaining("Invalid json rpc parameter at index 1");
  }

  @Test
  public void methodNotEnabledWhenNoVoteProvider() {
    final JsonRpcRequestContext request = requestWithParams(Address.fromHexString("1"));
    final JsonRpcResponse expectedResponse =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.METHOD_NOT_ENABLED);
    when(validatorProvider.getVoteProviderAtHead()).thenReturn(Optional.empty());

    final JsonRpcResponse response = method.response(request);

    assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
  }

  @Test
  public void addValidator() {
    final Address parameterAddress = Address.fromHexString("1");
    final JsonRpcRequestContext request = requestWithParams(parameterAddress, "true");
    final JsonRpcResponse expectedResponse =
        new JsonRpcSuccessResponse(request.getRequest().getId(), true);

    final JsonRpcResponse response = method.response(request);

    assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);

    verify(voteProvider).authVote(parameterAddress);
  }

  @Test
  public void removeValidator() {
    final Address parameterAddress = Address.fromHexString("1");
    final JsonRpcRequestContext request = requestWithParams(parameterAddress, "false");
    final JsonRpcResponse expectedResponse =
        new JsonRpcSuccessResponse(request.getRequest().getId(), true);

    final JsonRpcResponse response = method.response(request);

    assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);

    verify(voteProvider).dropVote(parameterAddress);
  }

  private JsonRpcRequestContext requestWithParams(final Object... params) {
    return new JsonRpcRequestContext(new JsonRpcRequest(JSON_RPC_VERSION, QBFT_METHOD, params));
  }
}
