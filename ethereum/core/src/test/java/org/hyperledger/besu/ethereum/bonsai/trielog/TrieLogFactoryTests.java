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
 *
 */
package org.hyperledger.besu.ethereum.bonsai.trielog;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.bonsai.worldview.StorageSlotKey;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderTestFixture;
import org.hyperledger.besu.ethereum.core.BlockchainSetupUtil;
import org.hyperledger.besu.ethereum.worldstate.DataStorageFormat;
import org.hyperledger.besu.ethereum.worldstate.StateTrieAccountValue;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TrieLogFactoryTests {

  final BlockchainSetupUtil setup = BlockchainSetupUtil.forTesting(DataStorageFormat.BONSAI);

  final Address accountFixture = Address.fromHexString("0xdeadbeef");

  final BlockHeader headerFixture =
      new BlockHeaderTestFixture()
          .parentHash(setup.getGenesisState().getBlock().getHash())
          .coinbase(Address.ZERO)
          .buildHeader();

  final TrieLogLayer trieLogFixture =
      new TrieLogLayer()
          .setBlockHash(headerFixture.getBlockHash())
          .addAccountChange(
              accountFixture,
              null,
              new StateTrieAccountValue(0, Wei.fromEth(1), Hash.EMPTY, Hash.EMPTY))
          .addCodeChange(
              Address.ZERO,
              null,
              Bytes.fromHexString("0xfeeddeadbeef"),
              headerFixture.getBlockHash())
          .addStorageChange(Address.ZERO, new StorageSlotKey(UInt256.ZERO), null, UInt256.ONE);

  @Test
  public void testSerializeDeserializeAreEqual() {

    TrieLogFactory<TrieLogLayer> factory = new TrieLogFactoryImpl();
    byte[] rlp = factory.serialize(trieLogFixture);

    TrieLogLayer layer = factory.deserialize(rlp);
    assertThat(layer).isEqualTo(trieLogFixture);
  }

  @Test
  public void testZkSlotKeyIsZeroIsPresent() {
    // similar test but with zk criteria of decoding slot key when it is present, even if all zero
    TrieLogFactory<TrieLogLayer> factory = new ZkTrieLogFactoryImpl();
    byte[] rlp = factory.serialize(trieLogFixture);

    TrieLogLayer layer = factory.deserialize(rlp);
    assertThat(layer).isEqualTo(trieLogFixture);

    // assert slot key is present for an all zero key:
    assertThat(
            layer.getStorage().get(Address.ZERO).keySet().stream()
                .map(StorageSlotKey::slotKey)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(key -> key.equals(UInt256.ZERO)))
        .isTrue();
  }
}
