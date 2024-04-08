package org.anyline.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    String value() default "";

    /**
     * 加载顺序, 小的先加载
     * @return int
     */
    int index() default 0;
}
