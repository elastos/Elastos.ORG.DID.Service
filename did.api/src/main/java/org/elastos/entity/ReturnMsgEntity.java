/**
 * Copyright (c) 2017-2018 The Elastos Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.elastos.entity;

/**
 * clark
 * <p>
 * 9/3/18
 */
public class ReturnMsgEntity<V> {

    private Long status;
    private V result;

    public Long getStatus() {
        return status;
    }

    public ReturnMsgEntity setStatus(Long status) {
        this.status = status;
        return this;
    }

    public V getResult() {
        return result;
    }

    public ReturnMsgEntity setResult(V result) {
        this.result = result;
        return this;
    }

    public static class ELAReturnMsg{
        private Object id;
        private String version;
        private Object result;
        private Error error;

        public class Error {
            private String code;
            private String message;

            public String getCode() {
                return code;
            }

            public void setCode(String code) {
                this.code = code;
            }

            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }
        }

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public Error getError() {
            return error;
        }

        public void setError(Error error) {
            this.error = error;
        }
    }
}
