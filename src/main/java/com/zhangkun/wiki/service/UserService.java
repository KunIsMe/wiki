package com.zhangkun.wiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhangkun.wiki.domain.User;
import com.zhangkun.wiki.domain.UserExample;
import com.zhangkun.wiki.mapper.UserMapper;
import com.zhangkun.wiki.req.UserQueryReq;
import com.zhangkun.wiki.req.UserSaveReq;
import com.zhangkun.wiki.resp.UserQueryResp;
import com.zhangkun.wiki.resp.PageResp;
import com.zhangkun.wiki.util.CopyUtil;
import com.zhangkun.wiki.util.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

//        List<UserResp> userRespList = new ArrayList<>();
//        for (User user : userList) {
////            UserResp userResp = new UserResp();
////            BeanUtils.copyProperties(user, userResp);
//
//            // 对象复制
//            UserResp userResp = CopyUtil.copy(user, UserResp.class);
//            userRespList.add(userResp);
//        }

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
            // 新增用户
            user.setId(snowFlake.nextId());
            userMapper.insert(user);
        } else {
            // 编辑用户
            userMapper.updateByPrimaryKey(user);
        }
    }

    /**
     * 用户删除
     * @param id
     */
    public void delete(Long id) {
        userMapper.deleteByPrimaryKey(id);
    }
}
