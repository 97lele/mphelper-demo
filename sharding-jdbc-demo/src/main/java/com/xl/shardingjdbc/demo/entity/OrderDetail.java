package com.xl.shardingjdbc.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
public class OrderDetail implements Serializable {

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
