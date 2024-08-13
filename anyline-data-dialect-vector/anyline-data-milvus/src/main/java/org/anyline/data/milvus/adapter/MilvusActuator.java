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

package org.anyline.data.milvus.adapter;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.rbac.request.*;
import org.anyline.annotation.Component;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.authorize.Privilege;
import org.anyline.entity.authorize.Role;
import org.anyline.entity.authorize.User;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.Column;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Table;
import org.anyline.metadata.refer.MetadataFieldRefer;
import org.anyline.util.BasicUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("anyline.environment.data.driver.actuator.milvus")
public class MilvusActuator implements DriverActuator {
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return MilvusAdapter.class;
    }

    @Override
    public DataSource getDataSource(DriverAdapter adapter, DataRuntime runtime) {
        return null;
    }

    @Override
    public Connection getConnection(DriverAdapter adapter, DataRuntime runtime, DataSource datasource) {
        return null;
    }

    @Override
    public void releaseConnection(DriverAdapter adapter, DataRuntime runtime, Connection connection, DataSource datasource) {

    }

    private MilvusClientV2 client(DataRuntime runtime) {
        return (MilvusClientV2) runtime.getProcessor();
    }
    @Override
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, DataSource datasource, T meta) {

    }

    @Override
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, T meta) {

    }

    @Override
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, Connection con, T meta) {

    }

    @Override
    public String product(DriverAdapter adapter, DataRuntime runtime, boolean create, String product) {
        return null;
    }

    @Override
    public String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version) {
        return null;
    }

    @Override
    public DataSet select(DriverAdapter adapter, DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String cmd, List<Object> values, LinkedHashMap<String, Column> columns) throws Exception {
        return null;
    }

    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        return null;
    }

    @Override
    public Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        return null;
    }

    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception {
        return 0;
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        return 0;
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        return 0;
    }

    /* *****************************************************************************************************************
     * 													role
     * -----------------------------------------------------------------------------------------------------------------
     * boolean create(DataRuntime runtime, Role role) throws Exception
     * boolean rename(DataRuntime runtime, Role origin, Role update) throws Exception;
     * boolean delete(DataRuntime runtime, Role role) throws Exception
     * <T extends Role> List<T> roles(Catalog catalog, Schema schema, String pattern) throws Exception
     ******************************************************************************************************************/
    /**
     * role[调用入口]<br/>
     * 创建角色
     * @param role 角色
     * @return boolean
     */
    public boolean create(DataRuntime runtime, Role role) throws Exception {
        CreateRoleReq req = CreateRoleReq.builder()
            .roleName(role.getName())
            .build();
        client(runtime).createRole(req);
        return true;
    }
    /**
     * role[调用入口]<br/>
     * 删除角色
     * @param role 角色
     * @return boolean
     */
    public boolean drop(DataRuntime runtime, Role role) throws Exception {
        DropRoleReq req = DropRoleReq.builder()
            .roleName(role.getName())
            .build();
        client(runtime).dropRole(req);
        return true;
    }
    public <T extends Role> List<T>  roles(DataRuntime runtime, String random, boolean greedy, Role query){
        List<T> list = new ArrayList<>();
        List<String> roles = client(runtime).listRoles();
        for(String role:roles){
            list.add((T)new Role(role));
        }
        return list;
    }

    /* *****************************************************************************************************************
     * 													user
     * -----------------------------------------------------------------------------------------------------------------
     * boolean create(DataRuntime runtime, User user) throws Exception
     * boolean rename(DataRuntime runtime, User origin, User update) throws Exception;
     * boolean drop(DataRuntime runtime, User user) throws Exception
     * List<User> users(Catalog catalog, Schema schema, String pattern) throws Exception
     ******************************************************************************************************************/

    /**
     * user[调用入口]<br/>
     * 创建 用户
     * @param user 用户
     * @return boolean
     */
    public boolean create(DataRuntime runtime, User user) throws Exception {
        CreateUserReq req = CreateUserReq.builder()
            .userName(user.getName())
            .password(user.getPassword())
            .build();
        client(runtime).createUser(req);
        return true;
    }
    /**
     * user[调用入口]<br/>
     * 删除 用户
     * @param user 用户
     * @return boolean
     */
    public boolean drop(DataRuntime runtime, User user) throws Exception {
        DropUserReq req = DropUserReq.builder()
            .userName(user.getName())
            .build();
        client(runtime).dropUser(req);
        return true;
    }
    public <T extends User> List<T>  users(DataRuntime runtime, String random, boolean greedy, User query){
        List<T> list = new ArrayList<>();
        List<String> users = client(runtime).listUsers();
        for(String user:users){
            list.add((T)new User(user));
        }
        return list;
    }
    /* *****************************************************************************************************************
     * 													privilege
     * -----------------------------------------------------------------------------------------------------------------
     * <T extends Privilege> List<T> privileges(DataRuntime runtime, User user)
     ******************************************************************************************************************/

    /**
     * privilege[调用入口]<br/>
     * 查询用户权限
     * @param query 查询条件 根据metadata属性
     * @return List
     */

    public <T extends Privilege> List<T> privileges(DataRuntime runtime, String random, boolean greedy, Privilege query) throws Exception {
        return new ArrayList<>();
    }

    /* *****************************************************************************************************************
     * 													grant
     * -----------------------------------------------------------------------------------------------------------------
     * boolean grant(DataRuntime runtime, User user, Privilege ... privileges)  throws Exception
     * boolean grant(DataRuntime runtime, User user, Role ... roles) throws Exception
     * boolean grant(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception
     ******************************************************************************************************************/

    /**
     * privilege[调用入口]<br/>
     * 授权
     * @param user 用户
     * @param privileges 权限
     * @return boolean
     */

    public boolean grant(DataRuntime runtime, User user, Privilege ... privileges)  throws Exception {
        return true;
    }

    /**
     * privilege[调用入口]<br/>
     * 授权
     * @param user 用户
     * @param roles 角色
     * @return boolean
     */

    public boolean grant(DataRuntime runtime, User user, Role ... roles)  throws Exception {
        return true;
    }

    /**
     * privilege[调用入口]<br/>
     * 授权
     * @param role 角色
     * @param privileges 权限
     * @return boolean
     */

    public boolean grant(DataRuntime runtime, Role role, Privilege ... privileges)  throws Exception {
        for(Privilege privilege:privileges){
            GrantPrivilegeReq.GrantPrivilegeReqBuilder build = GrantPrivilegeReq.builder();
            build.roleName(role.getName());
            if(BasicUtil.isNotEmpty(privilege.getObjectName())){
                build.objectName(privilege.getObjectName());
            }
            if(BasicUtil.isNotEmpty(privilege.getObjectType())){
                build.objectType(privilege.getObjectType());
            }
            build.privilege(privilege.getName());
            GrantPrivilegeReq req = build.build();
            client(runtime).grantPrivilege(req);
        }
        return true;
    }

    /* *****************************************************************************************************************
     * 													revoke
     * -----------------------------------------------------------------------------------------------------------------
     * boolean revoke(DataRuntime runtime, User user, Privilege ... privileges)  throws Exception
     * boolean revoke(DataRuntime runtime, User user, Role ... roles) throws Exception
     * boolean revoke(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception
     ******************************************************************************************************************/

    /**
     * revoke[调用入口]<br/>
     * 撤销授权
     * @param user 用户
     * @param privileges 权限
     * @return boolean
     */

    public boolean revoke(DataRuntime runtime, User user, Privilege ... privileges) throws Exception {
        return true;
    }

    /**
     * revoke[调用入口]<br/>
     * 撤销授权
     * @param user 用户
     * @param roles 角色
     * @return boolean
     */

    public boolean revoke(DataRuntime runtime, User user, Role ... roles) throws Exception {
        return true;
    }
    /**
     * revoke[调用入口]<br/>
     * 撤销授权
     * @param role 角色
     * @param privileges 权限
     * @return boolean
     */

    public boolean revoke(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception {
        return true;
    }


}
