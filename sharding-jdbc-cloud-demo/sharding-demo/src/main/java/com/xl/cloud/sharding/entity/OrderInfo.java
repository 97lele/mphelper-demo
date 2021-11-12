package com.xl.cloud.sharding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
@TableName("order_info")
public class OrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单主键
     */
    @TableId("order_id")
    private Long orderId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

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

    /**
     * 用户名称
     */
    @TableField("user_name")
    private String userName;

    /**
     * 订单金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField(exist = false)
    private List<OrderDetail> detailList;



    public static List<OrderInfo> batchRandomData() {
        ThreadLocalRandom current = ThreadLocalRandom.current();
        int i = current.nextInt(1, 10);
        List<OrderInfo> infos = new ArrayList<>(i);
        for (int j = 0; j < i; j++) {
            OrderInfo orderInfo = randomData();
            infos.add(orderInfo);
        }
        return infos;
    }

    public static OrderInfo randomData() {
        OrderInfo orderInfo = new OrderInfo();
        ThreadLocalRandom current = ThreadLocalRandom.current();
        LocalDateTime createTime = LocalDateTime.now().plusMonths(current.nextLong(-3,1));
        orderInfo.setCreateTime(createTime);
        long userId = current.nextLong(1000000);
        orderInfo.setUserId(userId);
        String userName = UUID.randomUUID().toString();
        orderInfo.setUserName(userName.substring(0, current.nextInt(userName.length())));
        int i = current.nextInt(current.nextInt(1, 10));
        List<OrderDetail> detailList = new ArrayList<>(i);
        for (int j = 0; j < i; j++) {
            OrderDetail orderDetail = OrderDetail.randomData(createTime);
            orderDetail.setUserId(orderInfo.getUserId());
            detailList.add(orderDetail);
        }
        orderInfo.detailList = detailList;
        orderInfo.calculateTotal();
        return orderInfo;
    }

    public void calculateTotal() {
        if (CollectionUtils.isNotEmpty(detailList)) {
            this.totalAmount = detailList.stream().map(e -> e.getUnitPrice().multiply(BigDecimal.valueOf(e.getProductCount())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
}
