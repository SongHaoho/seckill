package org.seckill.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit，spring容器启动时加载哪些配置文件
@ContextConfiguration({"classpath:spring/spring-service.xml",
        "classpath:spring/spring-dao.xml"})

public class SeckillServiceTest {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void testGetSeckillList() throws Exception {
        List<Seckill> list=seckillService.getSeckillList();
        logger.info("list={}",list);
    }

    @Test
    public void testGetById() throws Exception {

        long seckillId=1000;
        Seckill seckill=seckillService.getById(seckillId);
        logger.info("seckill={}",seckill);
    }

    @Test
    public void testExportSeckillUrl() throws Exception {
        long id=1000;
        Exposer exposer=seckillService.exportSeckillUrl(id);
        logger.info("exposer={}",exposer);
    }

    @Test
    public void executeSeckill() throws Exception {

        long id=1000;
        long phone = 13276517652L;
        String md5="989782d9699322a22fb2686b2a90db8a";
        try {
            SeckillExecution excution = seckillService.executeSeckill(id, phone, md5);
            logger.info("result={}",excution);
        } catch (RepeatKillException e)
        {
            e.printStackTrace();
        }catch (SeckillCloseException e1)
        {
            e1.printStackTrace();
        }
    }

    //以上两个测试方法组成完整逻辑代码测试，可重复执行
    @Test
    public void testSeckillLogic() throws Exception {
        long id=1001;
        Exposer exposer=seckillService.exportSeckillUrl(id);
        if (exposer.isExposed())
        {
            logger.info("exposer={}",exposer);
            long userPhone=13476191876L;
            String md5=exposer.getMd5();
            try {
                SeckillExecution excution = seckillService.executeSeckill(id, userPhone, md5);
                logger.info("result={}",excution);
            }catch (RepeatKillException e)
            {
                e.printStackTrace();
            }catch (SeckillCloseException e1)
            {
                e1.printStackTrace();
            }
        }else {
            //秒杀未开启
            logger.warn("exposer={}",exposer);
        }
    }
}