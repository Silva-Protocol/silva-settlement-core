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
package org.silva.settlement.core.chain.ledger.model.crypto;

import org.silva.settlement.ethereum.model.settlement.Signature;
import org.silva.settlement.infrastructure.crypto.key.asymmetric.SecureKey;

import java.util.Optional;

/**
 * description:
 * @author carrot
 */
public class ValidatorSigner {

    private SecureKey secureKey;

    // public key
    public byte[] getAuthor() {
        return secureKey.getPubKey();
    }

    public ValidatorSigner(SecureKey secureKey) {
        this.secureKey = secureKey;
    }

    public Optional<Signature> signMessage(byte[] id) {
        var signature = secureKey.sign(id);
        return Optional.of(new Signature(signature));
    }
}
