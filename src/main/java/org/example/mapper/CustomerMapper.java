package org.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.pojo.Customer;
import org.example.pojo.User;

import java.util.List;

/**
* @author 123
* @description 针对表【customer】的数据库操作Mapper
* @createDate 2024-07-13 13:31:41
* @Entity org.example.pojo.Customer
*/
public interface CustomerMapper {

    Customer selectById(Integer id);

    List<Customer> selectAll();

    int insertCustomer(Customer Customer);
    // 根据产品id修改产品
    int updateCustomer(Customer customer);
    // 根据商品id删除产品
    int delCustomer(Integer id);

    List<Customer> selectByName(String keyWord);



}
