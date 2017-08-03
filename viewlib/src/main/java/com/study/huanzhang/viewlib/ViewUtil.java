package com.study.huanzhang.viewlib;

import android.app.Activity;

/**
 * TODO What does this class to do?
 *
 * @author Muyangmin
 * @since 2.0.0
 */
public class ViewUtil {
    public static void bind(Activity activity) {
        //类名
        String clsName = activity.getClass().getName();
        try {
            //加载内部类
            Class<?> viewBidClass = Class.forName(clsName + "$$ViewBinder");
            //创建内部类对象
            ViewBinder viewBinder = (ViewBinder) viewBidClass.newInstance();
            //执行内部类方法
            viewBinder.bind(activity);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
