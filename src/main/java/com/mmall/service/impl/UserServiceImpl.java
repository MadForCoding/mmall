package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        // TODO MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);


        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user){

        ServerResponse<String> responseValid = checkValid(user.getUsername(), Const.USERNAME);
        if(!responseValid.isSuccess())
            return responseValid;

        responseValid = checkValid(user.getEmail(), Const.EMAIL);
        if(!responseValid.isSuccess())
            return responseValid;

        // MD5加密
        user.setRole(Const.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        user.setCreateTime(new Date(System.currentTimeMillis()));
        user.setUpdateTime(new Date(System.currentTimeMillis()));

        int resultCount = userMapper.insertSelective(user);

        if(resultCount == 0)
            return ServerResponse.createByErrorMessage("注册失败");

        return ServerResponse.createBySuccessMessage("注册成功");

    }

    /**
     * check username and email whether has been registered.
     * If resigered, return ServerResponse with error code.
     * else, return ServerResponse with success code.
     * @param str
     * @param type
     * @return ServerResponse
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type){
        if(StringUtils.isBlank(str))
            return ServerResponse.createByErrorMessage("用户名或者邮箱未填写");

        if(StringUtils.isNotBlank(type)){
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("用户已存在");
                }
            }
            if(Const.EMAIL.equals((type))){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("Email已注册");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        // 用户不存在
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse<String> responseValid = checkValid(username, Const.USERNAME);
        if(responseValid.isSuccess())
            return ServerResponse.createByErrorMessage("用户不存在");

        String question = userMapper.selectQuestionByUsername(username);

        if(StringUtils.isBlank(question))
            return ServerResponse.createByErrorMessage("找回密码的问题是空");

        return ServerResponse.createBySuccess(question);


    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount == 0)
            return ServerResponse.createByErrorMessage("提示问题回答不正确");

        String token = UUID.randomUUID().toString();
        TokenCache.setKey(TokenCache.TOKEN_PREFIX+username, token);
        return ServerResponse.createBySuccess(token);

    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        ServerResponse<String> checkUsername = checkValid(username, Const.USERNAME);
        if(checkUsername.isSuccess())
            return ServerResponse.createByErrorMessage("用户不存在");

        if(StringUtils.isBlank(forgetToken))
            return ServerResponse.createByErrorMessage("token无效");

        if(StringUtils.isBlank(passwordNew))
            return ServerResponse.createByErrorMessage("新密码无效，请重新输入");

        String s = TokenCache.getkey(TokenCache.TOKEN_PREFIX+username);
        // check user forgetToken and server token
        if(StringUtils.equals(forgetToken, TokenCache.getkey(TokenCache.TOKEN_PREFIX+username))){
            String MD5passwordNew = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.updatePasswordByUsername(username, MD5passwordNew);
            if(resultCount > 0)
                return ServerResponse.createBySuccessMessage("修改密码成功");

        }else{
            return ServerResponse.createByErrorMessage("重置密码失败，请重新输入密码提示答案");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");

    }


    @Override
    public ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew) {
        int resultCount = userMapper.checkPassword(user.getId(), MD5Util.MD5EncodeUtf8(passwordOld));
        if(resultCount == 0)
            return ServerResponse.createByErrorMessage("旧密码错误");

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount == 0)
            return ServerResponse.createByErrorMessage("密码更新失败");

        return ServerResponse.createBySuccessMessage("密码更新成功");
    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        // username是不可以被更新的
        // 新email也要进行校验，防止email被其他用户注册使用
        int resultCount = userMapper.checkEmailByUserId(user.getId(), user.getEmail());
        if(resultCount > 0)
            return ServerResponse.createByErrorMessage("邮箱被占用");

        User updateUser = new User();
        updateUser.setRole(Const.Role.ROLE_CUSTOMER);
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setAnswer(user.getAnswer());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setUpdateTime(user.getUpdateTime());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount == 0)
            return ServerResponse.createByErrorMessage("更新信息失败");

        return ServerResponse.createBySuccess("更新个人信息成功",updateUser);

    }

    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null)
            return ServerResponse.createByErrorMessage("找不到当前用户");

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse<String> checkAdminRole(User user) {
        if(user.getRole() == Const.Role.ROLE_ADMIN)
            return ServerResponse.createBySuccess();
        else
            return ServerResponse.createByError();
    }
}
