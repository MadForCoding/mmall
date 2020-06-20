package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    // 调用日志打印该类信息到控制台
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse<String> addCategory(String categoryName, Integer parentId) {
        if(parentId == null || StringUtils.isBlank(categoryName))
            return ServerResponse.createByErrorMessage("添加商品类别失败");

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true); //表示这个分类可用

        int resultCount = categoryMapper.insert(category);
        if(resultCount > 0)
            return ServerResponse.createBySuccessMessage("添加商品类别成功");

        return ServerResponse.createByErrorMessage("添加商品类别失败");
    }

    @Override
    public ServerResponse<String> updateCategory(Integer categoryId, String categoryName) {
        if(categoryId == null || StringUtils.isBlank(categoryName))
            return ServerResponse.createByErrorMessage("修改商品名字失败");

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        category.setUpdateTime(new Date(System.currentTimeMillis()));

        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(resultCount > 0)
            return ServerResponse.createBySuccessMessage("修改名字成功");

        return ServerResponse.createByErrorMessage("修改商品类别名字失败");



    }


    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> list = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(list))
            logger.info("未找到当前分类的子分类");

        return ServerResponse.createBySuccess(list);

    }

    /**
     * <p>递归查询该节点与其孩子节点的id</p>
     * @param currentId
     * @return
     */
    @Override
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer parentId) {
        Set<Category> set = Sets.newHashSet();

        List<Category> list = categoryMapper.selectCategoryChildrenByParentId(parentId);
        for(Category cat : list)
            findChildrenCategory(set, cat.getId());

        List<Integer> res = Lists.newArrayList();
        for(Category category : set){
            res.add(category.getId());
        }

        return ServerResponse.createBySuccess(res);



    }

    private Set<Category> findChildrenCategory(Set<Category> set, Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);

        if(category == null || set.contains(category))
            return set;

        set.add(category);

        // check children
        List<Category> list = categoryMapper.selectCategoryChildrenByParentId(category.getId());
        for(Category item : list){
            findChildrenCategory(set, item.getId());
        }
        return set;
    }


}
