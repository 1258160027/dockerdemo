package org.code.dockerdemo.jmeter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author: zl
 * @date: 2021-01-19
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping
    public void test(){
        String clientId = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", clientId,30, TimeUnit.MILLISECONDS);
        if (!lock){
            log.info("网络拥挤，请稍后再试~~~");
        }
        try {
            if (lock){
                int stock = (int)redisTemplate.opsForValue().get("num");
                if (stock > 0){
                    Long num = redisTemplate.opsForValue().decrement("num");
                    log.info("扣减成功，剩余库存{}",num);
                    //Thread.sleep(30);
                }else {
                    log.info("对不起物品已经售罄~~~~");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (clientId.equals(redisTemplate.opsForValue().get("lock"))){
                redisTemplate.delete("lock");
                log.info("锁释放成功~~~");
            }
        }
    }
}
