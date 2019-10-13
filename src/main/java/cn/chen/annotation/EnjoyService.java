package cn.chen.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//注解继承
//@Inherited
public @interface EnjoyService {
   String value() default "";
}
