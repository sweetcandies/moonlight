package com.funiverise.enums;

import lombok.Getter;

/**
 * @author InfosecPC01
 * @version 1.0
 * @description: TODO
 * @date 2021/12/13 11:07
 */
@Getter
public enum UserStatus {

    NORMAL("正常",0),
    FREEZE("冻结",1),
    CANCELED("已注销",2),
    AUDITING("审核中",3);

    private String status;

    private Integer code;

    UserStatus(String status,Integer code) {
        this.code = code;
        this.status = status;
    }

    /**
     * @description: 根据状态符号获取枚举对象
     * @param: [code]
     * @return: com.funiverise.enums.UserStatus
     * @author: Funny
     * @date: 2021/12/13 11:12
     */
    public static UserStatus getUserStatusByCode(Integer code) {
        for (UserStatus status: UserStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new NullPointerException("不存在的用户状态！");
    }

}
