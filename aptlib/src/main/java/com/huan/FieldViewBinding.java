package com.huan;

import javax.lang.model.type.TypeMirror;

/**
 * 成员变量bean,包含变量的命名名称（as:mTextView），变量的类型(as:TextView),变量的id(as:R.id.tv)
 * Created by huanzhang on 2017/8/2.
 */

public class FieldViewBinding {
    private String name; //名称
    private TypeMirror typeMirror; //类型
    private int resId;  //id
    public FieldViewBinding(String name, TypeMirror typeMirror, int resId) {
        this.name = name;
        this.typeMirror = typeMirror;
        this.resId = resId;
    }

    public String getName() {
        return name;
    }

    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public int getResId() {
        return resId;
    }
}
