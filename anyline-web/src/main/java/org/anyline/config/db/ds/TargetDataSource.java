package org.anyline.config.db.ds; 
 
 
import java.lang.annotation.*; 
 
@Target({ElementType.TYPE, ElementType.METHOD}) 
@Retention(RetentionPolicy.RUNTIME) 
@Documented 
public @interface TargetDataSource { 
    String name(); 
}
