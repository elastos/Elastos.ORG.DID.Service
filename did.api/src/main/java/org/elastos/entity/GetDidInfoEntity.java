/**
 * Copyright (c) 2017-2018 The Elastos Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.elastos.entity;

import java.util.List;

/**
 * clark
 * <p>
 * 9/27/18
 */
public class GetDidInfoEntity<T> {

    private List<String> txIds;
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getTxIds() {
        return txIds;
    }

    public void setTxIds(List<String> txIds) {
        this.txIds = txIds;
    }
}
