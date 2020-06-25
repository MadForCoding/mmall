package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class IProductServiceImpl implements IProductService {

    @Autowired
    ProductMapper productMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    ICategoryService iCategoryService;

    @Override
    public ServerResponse<String> saveOrUpdateProduct(Product product) {
        if(product == null)
             return ServerResponse.createByErrorMessage("新增或更新产品参数不正确");

        if(StringUtils.isNotBlank(product.getSubImages())){
            // 取subimages里面的第一张照片为mainImage
            String mainImage = product.getSubImages().substring(0,product.getSubImages().indexOf(",")).trim();

            if(product.getId() != null){
                // 更新产品
                int resultCount = productMapper.updateByPrimaryKey(product);
                if(resultCount > 0)
                    return ServerResponse.createBySuccessMessage("更新产品成功");
                return ServerResponse.createByErrorMessage("更新产品失败");
            }else{
                int resultCount = productMapper.insert(product);
                if(resultCount > 0)
                    return ServerResponse.createBySuccessMessage("插入产品成功");
                return ServerResponse.createByErrorMessage("插入产品失败");

            }
        }

        return ServerResponse.createByErrorMessage("新增或更新产品参数不正确");
    }

    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if(productId == null || status == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(), ResponseCode.ILLEGAL_ARGUEMENT.getDesc());

        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        product.setUpdateTime(new Date(System.currentTimeMillis()));
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount > 0)
            return ServerResponse.createBySuccessMessage("修改产品状态成功");

        return ServerResponse.createByErrorMessage("修改产品状态失败");
    }

    @Override
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if(productId == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(), ResponseCode.ILLEGAL_ARGUEMENT.getDesc());

        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null)
            return ServerResponse.createByErrorMessage("产品已下架或者删除");

        ProductDetailVo productDetailVo = assembleProductDetail(product);
        return ServerResponse.createBySuccess(productDetailVo);

    }

    private ProductDetailVo assembleProductDetail(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null)
            productDetailVo.setParentCategoryId(0);
        else
            productDetailVo.setParentCategoryId(category.getParentId());

        productDetailVo.setCreateTimeString(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTimeString(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }


    @Override
    public ServerResponse<PageInfo<ProductListVo>> getProductList(int pageNum, int pageSize) {
        // startPage--start
        // 填充自己的sql查询语句
        // pageHelper收尾
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.getList();

        List<ProductListVo> res = new ArrayList<>();

        for(Product product : productList){
            res.add(assembleProductListVo(product));
        }

        PageInfo<ProductListVo> pageResult = new PageInfo<>(res);
        return ServerResponse.createBySuccess(pageResult);

    }

    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        productListVo.setSubtitle(product.getSubtitle());
        return productListVo;
    }

    @Override
    public ServerResponse<PageInfo<ProductListVo>> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        if(StringUtils.isNotBlank(productName))
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectByNameAndId(productName, productId);
        List<ProductListVo> res = new ArrayList<>();
        for(Product product : productList){
            res.add(assembleProductListVo(product));
        }
        PageInfo<ProductListVo> pageResult = new PageInfo<>(res);
        return ServerResponse.createBySuccess(pageResult);

    }


    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if(productId == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(), ResponseCode.ILLEGAL_ARGUEMENT.getDesc());

        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null)
            return ServerResponse.createByErrorMessage("产品已下架或者删除");

        if(product.getStatus() != Const.ProductStatusEnum.OnSALE.getCode())
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        ProductDetailVo productDetailVo = assembleProductDetail(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId,
                                                                int pageNum, int pageSize, String orderBy) {
        if(StringUtils.isBlank(keyword) && categoryId == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(), ResponseCode.ILLEGAL_ARGUEMENT.getDesc());

        List<Integer> subCategoryId = new ArrayList<>();

        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyword)){
                //没有该分类， 并且还没有关键字，这个时候返回一个空的结果集，不会报错
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> list = new ArrayList<>();
                PageInfo pageInfo = new PageInfo(list);
                return ServerResponse.createBySuccess(pageInfo);
            }

            subCategoryId = iCategoryService.selectCategoryAndChildrenById(categoryId).getData();
        }

        if(StringUtils.isNotBlank(keyword))
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();

        PageHelper.startPage(pageNum,pageSize);
        //排序处理
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }

        List<Product> productList = productMapper.selectByNameAndCategoryIds(
                StringUtils.isBlank(keyword)?null:keyword, subCategoryId.size() == 0?null:subCategoryId);

        List<ProductListVo> productListVoList = new ArrayList<>();
        for(Product product : productList){
            ProductListVo vo = assembleProductListVo(product);
            productListVoList.add(vo);
        }
        PageInfo pageInfo = new PageInfo(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);

    }
}
