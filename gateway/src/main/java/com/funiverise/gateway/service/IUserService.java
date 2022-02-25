package com.funiverise.gateway.service;

import com.funiverise.gateway.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.funiverise.message.ReturnMsg;
import com.funiverise.object.vo.UserDetailVO;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
public interface IUserService extends IService<User> {

    ReturnMsg<UserDetailVO> getUserDetail(String username);

    User getUserByUsername(String username);

    ReturnMsg<String> loginByPassword(String username, String password, String app);
}
