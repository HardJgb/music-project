package org.example.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mapper.CustomerMapper;
import org.example.pojo.Customer;
import org.example.service.CustomerService;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerMapper customerMapper;
    /**
     * * 新增客户信息
     * * @param Customer
     * * @return
     * */
    @Override
    public Result add(Customer customer) {
        customerMapper.insertCustomer(customer);
        return Result.success(customer);

    }
    /** * 更新员工信息 * @param mCustomer * @return */
    @Override
    public Result update(Customer customer) {
        customerMapper.updateCustomer(customer);
        return Result.success(customer);
    }

    /**
     * * 删除员工信息
     * * @param mCustomer
     * * @return */
    @Override
    public Result del(int id) {
        customerMapper.delCustomer(id);
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
    public Result selectCustomer(String keyWord,int pageNum,int pageSize){
        //分页插件初始化
        PageHelper.startPage(pageNum,pageSize);
        //1.查询数据库
        List<Customer> customers = customerMapper.selectByName(keyWord);
        //2.分页插件返回数据
        PageInfo<Customer> customerPageData = new PageInfo<>(customers);
        //返回列表数据
        List<Customer> list = customerPageData.getList();
        //返回总页数
        int pages = customerPageData.getPages();
        //返回总条数
        long total = customerPageData.getTotal();

        Map data = new HashMap<>();
        data.put("list",list);
        data.put("pages",pages);
        data.put("total",total);
        data.put("pageNum",pageNum);
        data.put("pageSize",pageSize);
        return Result.success(data);
    }
}
