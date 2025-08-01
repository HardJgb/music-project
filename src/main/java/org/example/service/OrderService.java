package org.example.service;

import org.example.pojo.Order;
import org.example.utils.Result;

public interface OrderService {
    Result add(Order order);

    Result update(Order order);

    Result del(int id);


    Result selectOrder(String keyWord, int pageNum, int pageSize);
}
