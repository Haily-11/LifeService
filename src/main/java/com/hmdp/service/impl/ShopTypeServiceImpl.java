package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public List<ShopType> getTypeListWithCache() {
        String key = CACHE_SHOP_TYPE_KEY;
        // 1. 从 Redis 查询缓存
        String cacheData = stringRedisTemplate.opsForValue().get(key);
        if (cacheData != null) {
            // 缓存命中，直接返回
            return JSONUtil.toList(cacheData, ShopType.class);
        }

        // 2. 缓存未命中，查询数据库
        List<ShopType> typeList = this.query().orderByAsc("sort").list();

        // 3. 将结果写入 Redis 缓存
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(typeList), 30, TimeUnit.MINUTES);

        // 4. 返回结果
        return typeList;

    }
}
