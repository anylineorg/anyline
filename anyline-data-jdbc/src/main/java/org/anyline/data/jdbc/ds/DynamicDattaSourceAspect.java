package org.anyline.data.jdbc.ds;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(-1)//保证在@Transactional之前执行
@Component
public class DynamicDattaSourceAspect {

    private Logger logger = LoggerFactory.getLogger(DynamicDattaSourceAspect.class);

    //改变数据源
    @Before("@annotation(targetDataSource)")
    public void changeDataSource(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        String ds = targetDataSource.name();

        if (!DataSourceHolder.contains(ds)) {
            //joinPoint.getSignature() ：获取连接点的方法签名对象
            logger.error("[切换数据源][数据源不存在][数据源:{}][signature:{}]",ds, joinPoint.getSignature());
        } else {
            logger.warn("[切换数据源][数据源:{}]",ds);
            DataSourceHolder.setDataSource(ds);
        }
    }

    @After("@annotation(targetDataSource)")
    public void clearDataSource(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        logger.warn("[清除数据源][数据源:" + targetDataSource.name() + "]");
        DataSourceHolder.setDefaultDataSource();
    }
}