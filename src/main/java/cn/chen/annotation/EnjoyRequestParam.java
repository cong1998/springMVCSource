package cn.chen.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//注解继承
//@Inherited
public @interface EnjoyRequestParam {
   String value() default "";
}
