package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";

    public static final String USERNAME = "username";

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc");
    }

    public interface Role{
        int ROLE_CUSTOMER = 0; // Normal user
        int ROLE_ADMIN = 1; // Admin
    }

    public enum ProductStatusEnum{

        OnSALE(1, "在线");

        private int code;
        private String value;

        ProductStatusEnum(int code, String value){
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }
    }
}
