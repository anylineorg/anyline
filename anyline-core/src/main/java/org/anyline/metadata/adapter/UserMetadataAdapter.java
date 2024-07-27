/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.anyline.metadata.adapter;

import org.anyline.util.BasicUtil;

public class UserMetadataAdapter extends AbstractMetadataAdapter<UserMetadataAdapter> {

    protected String[] passwordRefer;
    protected String[] hostRefer;


    public String[] getPasswordRefers() {
        return passwordRefer;
    }

    public String getPasswordRefer() {
        if(null != passwordRefer && passwordRefer.length > 0) {
            return passwordRefer[0];
        }
        return null;
    }
    public UserMetadataAdapter setPasswordRefer(String[] passwordRefer) {
        this.passwordRefer = passwordRefer;
        return this;
    }
    public UserMetadataAdapter setPasswordRefer(String password) {
        if(BasicUtil.isNotEmpty(password)) {
            this.passwordRefer = password.split(",");
        }else{
            this.passwordRefer = null;
        }
        return this;
    }
    public String[] getHostRefers() {
        return hostRefer;
    }

    public String getHostRefer() {
        if(null != hostRefer && hostRefer.length > 0) {
            return hostRefer[0];
        }
        return null;
    }
    public UserMetadataAdapter setHostRefer(String[] hostRefer) {
        this.hostRefer = hostRefer;
        return this;
    }
    public UserMetadataAdapter setHostRefer(String host) {
        if(BasicUtil.isNotEmpty(host)) {
            this.hostRefer = host.split(",");
        }else{
            this.hostRefer = null;
        }
        return this;
    }

}
