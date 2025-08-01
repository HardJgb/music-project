package org.example.service;

import org.example.pojo.Customer;
import org.example.pojo.User;
import org.example.utils.Result;

public interface CustomerService {
    Result add(Customer Customer);

    Result update(Customer Customer);

    Result del(int id);

    Result selectCustomer(String keyWord,int pageNum,int pageSize);
}
