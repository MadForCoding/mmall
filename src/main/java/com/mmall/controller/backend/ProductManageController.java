package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("manage/product")
public class ProductManageController {

    @Autowired
    IUserService iUserService;

    @Autowired
    IProductService iProductService;

    @Autowired
    IFileService iFileService;

    @RequestMapping("product_save.do")
    @ResponseBody
    public ServerResponse<String> productSave(HttpSession session, Product product){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登陆");

        if(iUserService.checkAdminRole(user).isSuccess()){
            // 填充我们增加产品的业务逻辑
            return iProductService.saveOrUpdateProduct(product);
        }
        else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }

    }


    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse<String> setSaleStatus(HttpSession session, Integer productId, Integer status){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登陆");

        if(iUserService.checkAdminRole(user).isSuccess()){
            // 填充我们de业务逻辑
            //return iProductService.saveOrUpdateProduct(product);
            return iProductService.setSaleStatus(productId, status);
        }
        else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }

    }


    @RequestMapping("get_detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> getDetail(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登陆");

        if(iUserService.checkAdminRole(user).isSuccess()){
            // 填充我们de业务逻辑
            return iProductService.manageProductDetail(productId);
        }
        else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }

    }


    @RequestMapping("get_list.do")
    @ResponseBody
    public ServerResponse<PageInfo<ProductListVo>> getList(HttpSession session,
                                                           @RequestParam(value = "pageNum", defaultValue = "1")int pageNum,
                                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登陆");

        if(iUserService.checkAdminRole(user).isSuccess()){
            // 填充我们de业务逻辑
            return iProductService.getProductList(pageNum,pageSize);
        }
        else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }

    }


    @RequestMapping("product_search.do")
    @ResponseBody
    public ServerResponse<PageInfo<ProductListVo>> productSearch(HttpSession session,
                                                           String productName,
                                                           Integer productId,
                                                           @RequestParam(value = "pageNum", defaultValue = "1")int pageNum,
                                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登陆");

        if(iUserService.checkAdminRole(user).isSuccess()){
            // 填充我们de业务逻辑
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }
        else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }

    }

    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse<Map<String, String>> upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请重新登陆");

        if(iUserService.checkAdminRole(user).isSuccess()){
            // 填充我们de业务逻辑
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

            Map<String, String> fileMap = new HashMap<>();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);
            return ServerResponse.createBySuccess(fileMap);
        }
        else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @RequestMapping("rich_text_img_upload.do")
    @ResponseBody
    public Map<String, String> richTextImgUpload(HttpSession session,
                                                 @RequestParam(value = "upload_file", required = false) MultipartFile file,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response){
        // 富文本对于返回值有自己的要求，我们使用的是simditor，所以需要按照simditor的要求返回
        /*
        {
            "success" : true/false
            "msg"   :   "error message", # optional
            "file_path" : [real file path]
        }
         */
        Map<String, String> resultMap = new HashMap<>();

        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            resultMap.put("success", "false");
            resultMap.put("msg", "请登陆为管理员");
            return resultMap;
        }

        if(iUserService.checkAdminRole(user).isSuccess()){
            // 填充我们de业务逻辑
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success", "false");
                resultMap.put("msg", "上传失败");
                return resultMap;
            }

            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success", "true");
            resultMap.put("msg", "上传成功");
            resultMap.put("file_path", url);
            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");

        }
        else{
            resultMap.put("success", "false");
            resultMap.put("msg", "无权限操作");
        }
        return resultMap;
    }


}
