package org.example.service;

import org.example.mapper.ProductMapper;
import org.example.pojo.Product;
import org.example.utils.Result;

public interface ProductService {

    Result add(Product product);

    Result update(Product product);

    Result del(long id);

    Result selectProduct(String keyWord, int pageNum, int pageSize);
}
