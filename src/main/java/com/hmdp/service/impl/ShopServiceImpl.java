package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.SystemConstants;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
        //缓存穿透
        //Shop shop = queryWithPassThrough(id);
//        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        //互斥解决缓存击穿
//         shop = queryWithMutex(id);

        //逻辑过期解决缓存击穿
        Shop shop = cacheClient.queryWithLogicExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById,20L, TimeUnit.MINUTES);
        if (shop == null) {
            return Result.fail("店铺不存在！");
        }
        //返回
        return Result.ok(shop);
    }

//    public Shop queryWithMutex(Long id){
//        String key = CACHE_SHOP_KEY+id;
//        //1.从redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        //2.判断是否存在
//        if(StrUtil.isNotBlank(shopJson)){
//            //3.存在，返回
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//
//        //判断命中的是否为空值
//        if (shopJson != null) {
//            //为空值，返回错误
//            return null;
//        }
//
//        //4.实现缓存重建
//        //4.1获取互斥锁
//        String lockKey = LOCK_SHOP_KEY+id;
//        Shop shop = null;
//        try {
//            boolean isLock = tryLock(lockKey);
//            //4.2判断是否获取成功
//            if(!isLock){
//                //4.3失败，则休眠并重试
//                Thread.sleep(50);
//                return queryWithMutex(id);
//
//            }
//
//            //4.4成功，根据id查询数据库
//            shop = getById(id);
//
//            //模拟重建的延时
//            Thread.sleep(200);
//            //5.存在，写入redis
//            if(shop==null){
//                stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);
//                return null;
//            }
//
//            //6.存在，写入redis
//            stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            //7.释放互斥锁
//            unlock(lockKey);
//        }
//
//        //8.返回
//        return shop;
//    }


//    public Shop queryWithPassThrough(Long id){
//        String key = CACHE_SHOP_KEY+id;
//        //1.从redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        //2.判断是否存在
//        if(StrUtil.isNotBlank(shopJson)){
//            //3.存在，返回
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//
//        //判断命中的是否为空值
//        if (shopJson != null) {
//            //4.1 为空值，返回错误
//            return null;
//        }
//
//        //4.不存在，根据id查询数据库M
//        Shop shop = getById(id);
//
//这里可能出现缓存穿透，解决方法：缓存空对象（同时设置时间
//        //5.如果在数据库中没找到，返回错误
//        if(shop==null){
//           return Result.fail("店铺不存在！");
//       }
//
//        if(shop==null){
//            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);
//            return null;
//        }
//
//        //6.存在，写入redis
//        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        //7.返回
//        return shop;
//

 


    @Override
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空！");
        }
        //1.更新数据库
        updateById(shop);
        //2.删除redis缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY+id);
        //3.返回
        return Result.ok();
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        //1.是否根据坐标查询
        if(x==null||y==null){
            //不需要坐标查询，按数据库查询
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }
        //2.计算分页参数
        int from = (current-1)*SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current*SystemConstants.DEFAULT_PAGE_SIZE;
        //3.查询redis，根据距离排序，分页。结果：shopId、distance
        String key = SHOP_GEO_KEY+typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .search(
                        key,
                        GeoReference.fromCoordinate(x,y),
                        new Distance(5000),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(end)
                );

        //4.解析出Id
        if(results==null){
            return Result.ok(Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if(list.size()<=from){
            //没有下一页了，结束
            return Result.ok(Collections.emptyList());
        }
        //截取from 到 end部分
        List<Long> ids = new ArrayList<>(list.size());
        Map<String,Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result->{
            //获取店铺id
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            //获取距离
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr,distance);
        });

        //5.根据id查询店铺
        String idsStr = StrUtil.join(",", ids);
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id,"+idsStr+")").list();
        for(Shop shop:shops){
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        //6.返回
        return Result.ok(shops);
    }
}
