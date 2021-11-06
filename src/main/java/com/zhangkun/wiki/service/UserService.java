package com.zhangkun.wiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhangkun.wiki.domain.User;
import com.zhangkun.wiki.domain.UserExample;
import com.zhangkun.wiki.exception.BusinessException;
import com.zhangkun.wiki.exception.BusinessExceptionCode;
import com.zhangkun.wiki.mapper.UserMapper;
import com.zhangkun.wiki.req.UserLoginReq;
import com.zhangkun.wiki.req.UserQueryReq;
import com.zhangkun.wiki.req.UserResetPasswordReq;
import com.zhangkun.wiki.req.UserSaveReq;
import com.zhangkun.wiki.resp.UserLoginResp;
import com.zhangkun.wiki.resp.UserQueryResp;
import com.zhangkun.wiki.resp.PageResp;
import com.zhangkun.wiki.util.CopyUtil;
import com.zhangkun.wiki.util.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;


@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SnowFlake snowFlake;

    /**
     * 用户列表请求
     * @param req
     * @return
     */
    public PageResp<UserQueryResp> list(UserQueryReq req) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        if(!ObjectUtils.isEmpty(req.getLoginName())) {
            criteria.andLoginNameEqualTo(req.getLoginName());
        }
        PageHelper.startPage(req.getPage(), req.getSize());
        List<User> userList = userMapper.selectByExample(userExample);

        PageInfo<User> pageInfo = new PageInfo<>(userList);
        System.out.println("总行数：" + pageInfo.getTotal());
        System.out.println("总页数：" + pageInfo.getPages());

        // 列表复制
        List<UserQueryResp> userRespList = CopyUtil.copyList(userList, UserQueryResp.class);

        PageResp<UserQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(userRespList);

        return pageResp;
    }

    /**
     * 用户编辑或增加
     * @param req
     */
    public void save(UserSaveReq req) {
        User user = CopyUtil.copy(req, User.class);
        if(ObjectUtils.isEmpty(req.getId())){
            User userDB = selectByLoginName(req.getLoginName());
            if(ObjectUtils.isEmpty(userDB)) {
                // 新增用户
                user.setId(snowFlake.nextId());
                userMapper.insert(user);
            } else {
                // 用户名已存在
                throw new BusinessException(BusinessExceptionCode.USER_LOGIN_NAME_EXIST);
            }
        } else {
            // 编辑用户（更新）
            // 当值为null时，不会修改对应字段内容
            user.setLoginName(null);
            user.setPassword(null);
            userMapper.updateByPrimaryKeySelective(user);
        }
    }

    /**
     * 用户删除
     * @param id
     */
    public void delete(Long id) {
        userMapper.deleteByPrimaryKey(id);
    }

    /**
     * 检查登录名是否存在
     * @param loginName
     * @return
     */
    public User selectByLoginName(String loginName) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andLoginNameEqualTo(loginName);
        List<User> userList = userMapper.selectByExample(userExample);
        if(CollectionUtils.isEmpty(userList)) {
            return null;
        } else {
            return userList.get(0);
        }
    }

    /**
     * 重置密码
     * @param req
     */
    public void resetPassword(UserResetPasswordReq req) {
        User user = CopyUtil.copy(req, User.class);
        userMapper.updateByPrimaryKeySelective(user);
    }

    /**
     * 登录
     * @param req
     */
    public UserLoginResp login(UserLoginReq req) {
        User userDB = selectByLoginName(req.getLoginName());
        if(ObjectUtils.isEmpty(userDB)) {
            // 用户名不存在
            throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
        } else {
            if(userDB.getPassword().equals(req.getPassword())) {
                // 登录成功
                UserLoginResp userLoginResp = CopyUtil.copy(userDB, UserLoginResp.class);
                return userLoginResp;
            } else {
                // 密码错误
                throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
            }
        }
    }
}
