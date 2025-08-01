package org.example.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mapper.OrderMapper;
import org.example.pojo.Order;
import org.example.service.OrderService;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    /**
     * * 新增订单信息
     * * @param Customer
     * * @return
     * */
    @Override
    public Result add(Order order) {
        orderMapper.insertOrder(order);
        return Result.success(order);

    }
    /** * 更新订单信息 * @param mCustomer * @return */
    @Override
    public Result update(Order order) {
        orderMapper.updateOrder(order);
        return Result.success(order);
    }

    /**
     * * 删除订单信息
     * * @param mCustomer
     * * @return */
    @Override
    public Result del(int id) {
        orderMapper.delOrder(id);
        return Result.success(null);
    }

    /**
     * 分页模糊查询业务
     * 1.查询数据库
     * 2.分页插件返回数据
     * @param keyWord
     * @param pageNum
     * @param pageSize
     */
    @Override
    public Result selectOrder(String keyWord, int pageNum, int pageSize){
        //分页插件初始化
        PageHelper.startPage(pageNum,pageSize);
        //1.查询数据库
        List<Order> orders = orderMapper.selectAllByStatus(keyWord);
        //2.分页插件返回数据
        PageInfo<Order> orderPageData = new PageInfo<>(orders);
        //返回列表数据
        List<Order> list = orderPageData.getList();
        //返回总页数
        int pages = orderPageData.getPages();
        //返回总条数
        long total = orderPageData.getTotal();

        Map data = new HashMap<>();
        data.put("list",list);
        data.put("pages",pages);
        data.put("total",total);
        data.put("pageNum",pageNum);
        data.put("pageSize",pageSize);
        return Result.success(data);
    }


}
