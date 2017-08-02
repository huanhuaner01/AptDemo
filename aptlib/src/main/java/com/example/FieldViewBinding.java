package com.example;

import javax.lang.model.type.TypeMirror;

/**
 * Created by huanzhang on 2017/8/2.
 */

public class FieldViewBinding {
    private String name;
    private TypeMirror typeMirror;
    private int resId;
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
