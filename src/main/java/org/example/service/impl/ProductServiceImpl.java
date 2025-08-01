package org.example.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mapper.ProductMapper;
import org.example.pojo.Product;
import org.example.service.ProductService;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;
    /**
     * * 新增产品信息
     * * @param Product
     * * @return
     * */
    @Override
    public Result add(Product product) {
        productMapper.insert(product);
        return Result.success(product);

    }
    /**
     * * 更新产品信息
     * * @param Product
     * * @return */
    @Override
    public Result update(Product product) {
        productMapper.updateByPrimaryKey(product);
        return Result.success(product);
    }

    /**
     * * 删除产品信息
     * * @param Product
     * * @return */
    @Override
    public Result del(long id) {
        productMapper.deleteByPrimaryKey(id);
        return Result.success(null);
    }

    @Override
    public Result selectProduct(String keyWord, int pageNum, int pageSize){
        //分页插件初始化
        PageHelper.startPage(pageNum,pageSize);
        //1.查询数据库
        List<Product> products = productMapper.selectByName(keyWord);
        //2.分页插件返回数据
        PageInfo<Product> productPageData = new PageInfo<>(products);
        //返回列表数据
        List<Product> list = productPageData.getList();
        //返回总页数
        int pages = productPageData.getPages();
        //返回总条数
        long total = productPageData.getTotal();

        Map data = new HashMap<>();
        data.put("list",list);
        data.put("pages",pages);
        data.put("total",total);
        data.put("pageNum",pageNum);
        data.put("pageSize",pageSize);
        return Result.success(data);
    }

}
