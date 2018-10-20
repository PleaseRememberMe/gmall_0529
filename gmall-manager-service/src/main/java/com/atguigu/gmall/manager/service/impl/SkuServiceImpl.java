package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.SkuService;
import com.atguigu.gmall.manager.constant.RedisCacheKeyConst;
import com.atguigu.gmall.manager.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuAttrValueMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuImageMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuInfoMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuSaleAttrValueMapper;
import com.atguigu.gmall.manager.mapper.spu.SpuSaleAttrMapper;
import com.atguigu.gmall.manager.sku.*;
import com.atguigu.gmall.manager.spu.SpuSaleAttr;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;


    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    JedisPool jedisPool;

    @Override
    public List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(Integer catalog3Id) {
        return baseAttrInfoMapper.getBaseAttrInfoByCatalog3Id(catalog3Id);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrBySpuId(Integer spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrBySpuId(spuId);
    }

    @Transactional
    @Override
    public void saveBigSkuInfo(SkuInfo skuInfo) {
            //先保存基本的skuInfo 信息
        skuInfoMapper.insert(skuInfo);
            //在保存提交的图片、平台属性，销售属性
        List<SkuImage> skuImages = skuInfo.getSkuImages();
        for (SkuImage skuImage : skuImages) {
              skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insert(skuImage);
        }


        List<SkuAttrValue> skuAttrValues = skuInfo.getSkuAttrValues();
        for (SkuAttrValue skuAttrValue : skuAttrValues) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insert(skuAttrValue);
        }

        List<SkuSaleAttrValue> skuSaleAttrValues = skuInfo.getSkuSaleAttrValues();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValues) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }

    }

    @Override
    public List<SkuInfo> getSkuInfoBySpuId(Integer spuId) {
        return skuInfoMapper.selectList(new QueryWrapper<SkuInfo>().eq("spu_id",spuId));
    }

    @Override
    public SkuInfo getSkuInfoBySkuId(Integer skuId) {
        Jedis jedis = jedisPool.getResource();
        //信息不能一直在缓存，所以要在key上加上过期时间
        String  key= RedisCacheKeyConst.SKU_INFO_PREFIX+skuId+RedisCacheKeyConst.SKU_INFO_SUFFIX;
        SkuInfo result=null;
        //先去缓存看看
        String s=jedis.get(key);
        if(s!=null){
            //如果缓存中有就把他转换为自己想要的对象
            log.debug("缓存中找到了：{}",skuId);
            result=JSON.parseObject(s,SkuInfo.class);
            jedis.close();
            return result;
        }
        if("null".equals(s)){
            //防止缓存穿透
            //在数据库查找但是数据库没有的该信息，给缓存中放一个null串
            return null;
        }
        if(s==null){
            //当这个数据等于null的时候
            //说明缓存中没有，必须先从数据库中查出来，在放到缓存中
            //我们需要加锁，拿到锁在去查数据
            String token = UUID.randomUUID().toString();
            String lock=jedis.set(RedisCacheKeyConst.LOCK_SKU_INFO,token,"NX","EX",RedisCacheKeyConst.LOCK_TIMEOUT);
            if(lock ==null){
                //没有拿到锁
                log.debug("没有获取到锁，请重试");
                try {
                    Thread.sleep(1000);//等待1秒
                } catch (InterruptedException e) {
                     log.error("睡眠出错了");
                }
                //自旋锁
                getSkuInfoBySkuId(skuId);

            }else if("OK".equals(lock)){
                log.debug("获取到锁了，查数据库了");
                result = getFromDb(skuId);
                //将对象转换为json存到redis中
                String skuInfoJSON = JSON.toJSONString(result);
                log.debug("从数据库查到数据 了：{}",skuInfoJSON);
                //存到缓存中第二天就有人能查到新的数据了，保质期一天
                if("null".equals(skuInfoJSON)){
                    //空数据缓存时间短
                    jedis.setex(key,RedisCacheKeyConst.SKU_INFO_NULL_TIMEOUT,skuInfoJSON);
                }else{
                    //正常数据缓存时间长
                    jedis.setex(key,RedisCacheKeyConst.SKU_INFO_TIMEOUT,skuInfoJSON);
                }

                //手动释放，即使释放失败，也会自动过期删除
                //判断是否还是我的锁，如果是才删
                //NB之处....释放锁；解锁有问题吗？删锁的错误姿势
//                String redisToken = jedis.get(RedisCacheKeyConst.LOCK_SKU_INFO);
//                if(token.equals(redisToken)){
//                    jedis.del(RedisCacheKeyConst.LOCK_SKU_INFO);
//                }else{
//                    //业务逻辑已经超出锁的时间了，别人已经持有锁了，我们不要把别人锁删了
//                }

                //脚本；正确的解锁；一定要是原子操作
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                jedis.eval(script,
                        Collections.singletonList(RedisCacheKeyConst.LOCK_SKU_INFO),
                        Collections.singletonList(token));

            }
            jedis.close();
            return  result;
        }
        return null;
    }

    @Override
    public List<SkuAttrValueMappingTo> getSkuAttrValueMapping(Integer spuId) {
        return skuSaleAttrValueMapper.getSkuAttrValueMapping(spuId);
    }

    private SkuInfo getFromDb(Integer skuId){
        log.debug("缓存中没找到。从数据准备查询skuId是{}的商品信息",skuId);
        //1、先查出skuInfo基本信息
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(skuInfo == null){
            //即使没有数据也返回出去放在缓存
            return null;
        }
        //2.查出这个skuInfo的图片信息
        List<SkuImage> skuImages = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        skuInfo.setSkuImages(skuImages);

        //3。查出整个skuAttrValue销售属性信息
        List<SkuAllSaveAttrAndValueTo> skuAllSaveAttrAndValueTos=skuSaleAttrValueMapper.getSkuAllSaveAttrAndValue(skuId,skuInfo.getSpuId());
        skuInfo.setSkuAllSaveAttrAndValueTos(skuAllSaveAttrAndValueTos);
        //加缓存：redis作为缓存中间件；内存数据库
        return skuInfo;
    }


}
