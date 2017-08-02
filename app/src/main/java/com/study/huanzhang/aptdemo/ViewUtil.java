package com.study.huanzhang.aptdemo;

import android.app.Activity;

import com.example.BindView;

import java.lang.reflect.Field;

/**
 * TODO What does this class to do?
 *
 * @author Muyangmin
 * @since 2.0.0
 */
public class ViewUtil {
    public static void bind(Activity activity) {
        Class<? extends Activity> clazz = activity.getClass();
        //遍历所有的字段
        for (Field field : clazz.getDeclaredFields()) {
            //处理字段
            if (field.isAnnotationPresent(BindView.class)) {
                BindView anno = field.getAnnotation(BindView.class);
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
