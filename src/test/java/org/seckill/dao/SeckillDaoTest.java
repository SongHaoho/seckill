package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
/**
 * 配置spring和junit整合，为了junit启动时加载springIOC容器
 * SeckillDao实现类是mybatis做的，同时跟Spring整合把该实现类注入到Spring容器中
 */
//junit启动时加载springIOC容器
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    //注入Dao实现类依赖
    @Resource
    private SeckillDao seckillDao ;

    @Test
    public void reduceNumber() throws Exception {
        long seckillId=1000;
        Date date=new Date();
        int updateCount=seckillDao.reduceNumber(seckillId,date);
        System.out.println(updateCount);
    }

    @Test
    public void queryById() throws Exception {
        long seckillId=1001;
        Seckill seckill=seckillDao.queryById(seckillId);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    /**
     * List<Seckill> queryAll(int offset,int limit);
     * java不会保存形参记录，queryAll(int offset,int limit)=》queryAll(arg0,arg1)
     * 用符号取代对应参数，造成多个参数时，需要告诉mybatis哪个位置的参数叫什么名字
     */
    @Test
    public void queryAll() throws Exception {
        List<Seckill> seckills=seckillDao.queryAll(0,100);
        for (Seckill seckill : seckills)
        {
            System.out.println(seckill);
        }
    }

}