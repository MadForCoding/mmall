package com.mmall.test;

import org.junit.Test;

import java.math.BigDecimal;

public class BigDecimalTest {

    @Test
    public void test1(){
        System.out.println(0.05+0.01);
        System.out.println(1.0-0.42);
        System.out.println(4.015*100);
        System.out.println(123.3/100);
    }

    @Test
    public void test2(){
        BigDecimal big1 = new BigDecimal(0.05);
        BigDecimal big2 = new BigDecimal(0.01);
        System.out.println(big1.add(big2));
    }

    @Test
    public void test3(){
        // 使用bigdecimal来解决精度丢失问题
        // 在使用bigdecimal解决问题的时候，要使用string类型的构造器
        BigDecimal big1 = new BigDecimal("0.05");
        BigDecimal big2 = new BigDecimal("0.01");
        System.out.println(big1.add(big2));
    }
}
