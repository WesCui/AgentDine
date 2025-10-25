package com.sky.annotation;
// 自定义注解：自动填充,用于在插入或更新数据时，自动填充某些字段（如创建时间、更新时间、创建人、更新人等）

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



@Target(ElementType.METHOD) //表示该注解可以应用于方法上
@Retention(RetentionPolicy.RUNTIME) //表示该注解在运行时仍然可用
public @interface AutoFill {

    OperationType value(); //定义一个名为value的属性，用于指定操作类型（插入或更新）


}
