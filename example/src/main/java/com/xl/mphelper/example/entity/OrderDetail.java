package com.xl.mphelper.example.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author lele
 * @since 2021-10-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("order_detail")
public class OrderDetail implements Serializable, Shardable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId("detail_id")
    private Long detailId;

    /**
     * 订单id
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 商品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 单价
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 商品数量
     */
    @TableField("product_count")
    private Integer productCount;
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 最后修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;


    @Override
    public String suffix() {
         return createTime.getYear() + "_" + createTime.getMonth().getValue();
    }


    public static OrderDetail randomData(LocalDateTime time) {
        OrderDetail detail = new OrderDetail();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        detail.setProductCount(random.nextInt(1, 500));
        detail.setUnitPrice(BigDecimal.valueOf(random.nextDouble(10, 8000)));
        detail.setCreateTime(time);
        String productName = UUID.randomUUID().toString();
        detail.setProductName(productName.substring(0, random.nextInt(productName.length())));
        return detail;
    }

}
