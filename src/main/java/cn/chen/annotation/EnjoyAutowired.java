package cn.chen.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//注解继承
//@Inherited
public @interface EnjoyAutowired {
   String value() default "";
}
