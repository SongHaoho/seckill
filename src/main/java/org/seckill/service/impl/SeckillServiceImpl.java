package org.seckill.service.impl;


import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

//@Component @Service @Dao @Controller
@Service
public class SeckillServiceImpl implements SeckillService
{
    //日志对象
    private Logger logger= LoggerFactory.getLogger(this.getClass());

    //md5盐值字符串，用于混淆md5
    private final String salt="sh#$%&weishanxian.?{.}";

    //注入Service依赖
    @Autowired //@Resource，@Inject
    private SeckillDao seckillDao;

    @Autowired //@Resource
    private SuccessKilledDao successKilledDao;

    /*@Autowired
    private RedisDao redisDao;*/

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
    	Seckill seckill = seckillDao.queryById(seckillId);
    	if (seckill == null) {
            return new Exposer(false, seckillId);
            }
        //若是秒杀未开启(系统时间在秒杀事件范围外)
        Date startTime=seckill.getStartTime();
        Date endTime=seckill.getEndTime();        
        Date nowTime=new Date();//系统当前时间
        if (startTime.getTime()>nowTime.getTime() || endTime.getTime()<nowTime.getTime())
        {
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }
        //若是秒杀开启，返回秒杀商品的id、加密的md5
        String md5=getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    /**
     * 生成md5，定义为私有方法，输出秒杀接口地址和执行秒杀操作两个接口都会用到此方法
     * @param seckillId
     * @return
     */
    private String getMD5(long seckillId)
    {
    	//当用户不知道拼接规则，不知道盐值，则不会知道md5是多少(即转化特定字符串的过程不可逆)
        String base=seckillId+"/"+salt;
        //Spring专门生成md5的工具类
        String md5= DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    //秒杀是否成功，成功:减库存，增加明细；失败:抛出异常，事务回滚
    @Transactional
    /**
     * 使用注解控制事务方法的优点:
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5==null||!md5.equals(getMD5(seckillId)))
        {
            throw new SeckillException("seckill data rewrite");//秒杀数据被重写了
        }
        //执行秒杀逻辑:减库存+增加购买明细
        Date nowTime=new Date();
        try{
            //否则更新了库存，秒杀成功,记录购买行为
            int insertCount=successKilledDao.insertSuccessKilled(seckillId,userPhone);
            //唯一：seckillId,userPhone。数据表插入使用INSERT ignore，插入冲突时会忽略，返回0
            if (insertCount<=0)
            {
            	//重复秒杀
                throw new RepeatKillException("seckill repeated");
            }else {
                //减库存,热点商品竞争
                int updateCount=seckillDao.reduceNumber(seckillId,nowTime);
                if (updateCount<=0)
                {
                    //没有更新库存记录，说明秒杀结束 rollback
                    throw new SeckillCloseException("seckill is closed");
                }else {
                    //秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息 commit
                    SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
                }
                
            }
    
        }catch (SeckillCloseException e1)
        {
            throw e1;
        }catch (RepeatKillException e2)
        {
            throw e2;
        }catch (Exception e)
        {
            logger.error(e.getMessage(),e);
            //所以编译期异常转化为运行期异常，Spring的声明式事务会rollback
            throw new SeckillException("seckill inner error :"+e.getMessage());
        }
    }
}







