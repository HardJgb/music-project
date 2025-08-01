package org.example.mapper;

import org.example.pojo.Customer;
import org.example.pojo.Order;

import java.util.List;

public interface OrderMapper {
    //根据订单ID查询订单，以及订单关联的用户的信息！、
    Order selectById(int id);

    List<Order> selectAll();
    List<Order> selectAllByStatus(String keyWord);
    int insertOrder(Order order);
    // 根据订单id修改
    int updateOrder(Order order);
    // 根据订单id删除
    int delOrder(Integer id);

}
