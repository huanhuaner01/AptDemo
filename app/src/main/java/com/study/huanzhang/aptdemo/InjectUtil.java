package com.study.huanzhang.aptdemo;

import android.app.Activity;

import java.lang.reflect.Field;

public class InjectUtil {

    public static void bind(Activity activity) {
        Class<? extends Activity> clazz = activity.getClass();
        //遍历所有的字段
        for (Field field : clazz.getDeclaredFields()) {
            //处理字段
            if (field.isAnnotationPresent(BindView.class)) {
                IView anno = field.getAnnotation(IView.class);
                int value = anno.value();
                try {
                    field.setAccessible(true);
                    field.set(activity, activity.findViewById(value));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
