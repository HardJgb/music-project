package org.example.mapper;

import org.example.pojo.Customer;
import org.example.pojo.Product;

import java.util.List;

/**
* @author 123
* @description 针对表【product】的数据库操作Mapper
* @createDate 2024-07-14 13:37:01
* @Entity org.example.pojo.Product
*/
public interface ProductMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Long id);

    List<Product> selectAll();
    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectByName(String keyWord);

}
