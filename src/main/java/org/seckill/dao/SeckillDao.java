package org.seckill.dao;



import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;


import java.util.Date;
import java.util.List;


public interface SeckillDao
{

    /**
     * 减库存
     * @param seckillId
     * @param killTime
     * @return 如果影响行数>=1，表示更新库存的记录行数
     */
    int reduceNumber(@Param("seckillId") long seckillId,@Param("killTime") Date killTime);

    /**
     * 根据id查询秒杀的商品信息
     * @param seckillId
     * @return
     */
    Seckill queryById(long seckillId);

    /**
     * 根据偏移量查询秒杀商品列表
     * @param offset
     * @param limit
     * @return
     */
    //通过mybatis提供的注解@Param给每个参数一个名字，下一步(xml中)mybatis才能找到具体值
    List<Seckill> queryAll(@Param("offset") int offset,@Param("limit") int limit);



}
