package cn.chen.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//注解继承
//@Inherited
public @interface EnjoyRequestMapping {
   String value() default "";
}
