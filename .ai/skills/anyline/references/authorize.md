# 角色、用户、权限相关操作

## 概述
本文档涵盖通过 Anyline MDM 实现对数据库角色、用户、权限的创建、修改、删除操作
通过service.authorize()调用以下方法

## 1 相关数据库对象 
角色:org.anyline.entity.authorize.Role
用户:org.anyline.entity.authorize.User
权限:org.anyline.entity.authorize.Privilege
角色组:org.anyline.entity.authorize.RoleGroup
用户组:org.anyline.entity.authorize.UserGroup
权限组:org.anyline.entity.authorize.PrivilegeGroup

```java
/* *****************************************************************************************************************
 * 													role
 * -----------------------------------------------------------------------------------------------------------------
 * boolean create(Role role) throws Exception
 * <T extends Role> List<T> roles(Role query) throws Exception
 * boolean rename(Role origin, Role update) throws Exception
 * boolean drop(Role role) throws Exception
 ******************************************************************************************************************/

/**
 * 创建角色
 * @param role 角色
 * @return boolean
 */
boolean create(Role role) throws Exception;
boolean create(List<Role> roles) throws Exception;

/**
 * 查询角色
 * @param query 查询条件 根据metadata属性
 * @return List
 */
<T extends Role> List<T> roles(Role query) throws Exception;
/**
 * 查询角色
 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
 * @param pattern 角色名
 * @return List
 */
default <T extends Role> List<T> roles(Catalog catalog, Schema schema, String pattern) throws Exception{
    Role query = new Role();
    query.setCatalog(catalog);
    query.setSchema(schema);
    query.setName(pattern);
    return roles(query);
}

/**
 * 查询角色
 * @return List
 */
default <T extends Role> List<T> roles() throws Exception {
    return roles(new Role());
}

/**
 * 查询角色
 * @return List
 */
default <T extends Role> List<T> roles(User user) throws Exception {
    Role query = new Role();
    query.setUser(user);
    return roles(query);
}

/**
 * 角色重命名
 * @param origin 原名
 * @param update 新名
 * @return boolean
 */
boolean rename(Role origin, Role update) throws Exception;

/**
 * 删除角色
 * @param role 角色
 * @return boolean
 */
boolean drop(Role role) throws Exception;

/* *****************************************************************************************************************
 * 													user
 * -----------------------------------------------------------------------------------------------------------------
 * boolean create(User user) throws Exception
 * <T extends Role> List<T> roles(User query) throws Exception
 * boolean rename(User origin, Role update) throws Exception
 * boolean drop(User user) throws Exception
 ******************************************************************************************************************/
/**
 * 创建用户
 * @param user 用户
 * @return boolean
 */
boolean create(User user) throws Exception;

/**
 * 创建用户
 * @param name 用户名
 * @param password 密码
 * @return boolean
 */
default boolean create(String name, String password) throws Exception {
    return create(new User(name, password));
}

/**
 * 查询用户
 * @param query 查询条件 根据metadata属性
 * @return List
 */
List<User> users(User query) throws Exception;
/**
 * 查询用户
 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
 * @param pattern 用户名
 * @return List
 */
default List<User> users(Catalog catalog, Schema schema, String pattern) throws Exception{
    User query = new User();
    query.setCatalog(catalog);
    query.setSchema(schema);
    query.setName(pattern);
    return users(query);
}
/**
 * 查询用户
 * @return List
 */
default List<User> users() throws Exception {
    return users(new User());
}
/**
 * 查询用户
 * @param pattern 用户名
 * @return List
 */
default List<User> users(String pattern) throws Exception {
    return users(new User(pattern));
}
/**
 * 查询用户
 * @param role 角色
 * @return List
 */
default List<User> users(Role role) throws Exception {
    User query = new User();
    query.setRole(role);
    return users(query);
}
/**
 * 用户重命名
 * @param origin 原名
 * @param update 新名
 * @return boolean
 */
boolean rename(User origin, User update) throws Exception;

/**
 * 用户重命名
 * @param origin 原名
 * @param update 新名
 * @return boolean
 */
default boolean rename(String origin, String update) throws Exception {
    return rename(new User(origin), new User(update));
}

/**
 * 删除用户
 * @param user 用户
 * @return boolean
 */
boolean drop(User user) throws Exception;
/**
 * 删除用户
 * @param user 用户名
 * @return boolean
 */
default boolean drop(String user) throws Exception {
    return drop(new User(user));
}

/* *****************************************************************************************************************
 * 													grant
 * -----------------------------------------------------------------------------------------------------------------
 * boolean grant(User user, Privilege ... privileges) throws Exception
 * boolean grant(String user, Privilege ... privileges) throws Exception
 * boolean grant(User user, Role ... roles) throws Exception
 * boolean grant(Role role, Privilege ... privileges) throws Exception
 ******************************************************************************************************************/
/**
 * 授权
 * @param user 用户
 * @param privileges 权限
 * @return boolean
 */
boolean grant(User user, Privilege ... privileges) throws Exception;
/**
 * 授权
 * @param user 用户
 * @param roles 角色
 * @return boolean
 */
boolean grant(User user, Role ... roles) throws Exception;
/**
 * 授权
 * @param role 角色
 * @param privileges 权限
 * @return boolean
 */
boolean grant(Role role, Privilege ... privileges) throws Exception;
/**
 * 授权
 * @param user 用户
 * @param privileges 权限
 * @return boolean
 */
default boolean grant(String user, Privilege ... privileges) throws Exception {
    return grant(new User(user), privileges);
}

/* *****************************************************************************************************************
 * 													revoke
 * -----------------------------------------------------------------------------------------------------------------
 * boolean revoke(User user, Privilege ... privileges) throws Exception
 * boolean revoke(String user, Privilege ... privileges) throws Exception
 * boolean revoke(User user, Role ... roles) throws Exception
 * boolean revoke(Role role, Privilege ... privileges) throws Exception
 ******************************************************************************************************************/
/**
 * 撤销授权
 * @param user 用户
 * @param privileges 权限
 * @return boolean
 */
boolean revoke(User user, Privilege ... privileges) throws Exception;

/**
 * 撤销授权
 * @param role 角色
 * @param privileges 权限
 * @return boolean
 */
boolean revoke(Role role, Privilege ... privileges) throws Exception;

/**
 * 撤销授权
 * @param user 用户
 * @param roles 角色
 * @return boolean
 */
boolean revoke(User user, Role ... roles) throws Exception;

/**
 * 撤销授权
 * @param user 用户
 * @param privileges 权限
 * @return boolean
 */
default boolean revoke(String user, Privilege ... privileges) throws Exception {
    return revoke(new User(user), privileges);
}
/* *****************************************************************************************************************
 * 													privilege
 * -----------------------------------------------------------------------------------------------------------------
 * List<Privilege> privileges(Privilege query) throws Exception;
 * List<Privilege> privileges(User user) throws Exception
 * List<Privilege> privileges(String user) throws Exception
 * boolean revoke(User user, Privilege ... privileges) throws Exception
 * boolean revoke(String user, Privilege ... privileges) throws Exception
 ******************************************************************************************************************/
/**
 * 查询用户权限
 * @param query 查询条件 根据metadata属性
 * @return List
 */
List<Privilege> privileges(Privilege query) throws Exception;

/**
 * 查询用户权限
 * @param user 用户
 * @return List
 */
default List<Privilege> privileges(User user) throws Exception {
    Privilege query = new Privilege();
    query.setUser(user);
    return privileges(query);
}

/**
 * 查询用户权限
 * @param role 角色
 * @return List
 */
default List<Privilege> privileges(Role role) throws Exception {
    Privilege query = new Privilege();
    query.setRole(role);
    return privileges(query);
}
/**
 * 查询用户权限
 * @param user 用户
 * @return List
 */
default List<Privilege> privileges(String user) throws Exception {
    return privileges(new User(user));
}
```