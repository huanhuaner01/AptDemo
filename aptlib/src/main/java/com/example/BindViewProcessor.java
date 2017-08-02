package com.example;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * 作者：余天然 on 2017/2/6 上午11:01
 */
@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(BindView.class.getCanonicalName());
    }

    /**
     * package com.example.helloworld;
     * public final class HelloWorld {
     *  public static void main(String[] args) {
     *      System.out.println("BindView, APT");
     * }
     * }
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
     /*   Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            BindView view = element.getAnnotation(BindView.class);
            int id = view.value();

            MethodSpec main = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String[].class, "args")
                    .addStatement("$T.out.println($S)", System.class, "BindView, " + var)
                    .build();
            TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(main)
                    .build();
            JavaFile javaFile = JavaFile.builder("com.soubu.helloworld", helloWorld)
                    .build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;*/
        //存放着整个App的所有注解类型 以类为Key 类下的注解成员变量为value
        Map<TypeElement, List<FieldViewBinding>> targetMap = new HashMap<>();
        //遍历整个app的BindView注解成员变量 并将该类 和成员变量保存到Map中
        setElemtBindView(roundEnvironment, targetMap, BindView.class);
        //向源码中添加代码
        addClassAndMethod(targetMap);
        return false;
    }


    private void setElemtBindView(RoundEnvironment roundEnvironment, Map<TypeElement, List<FieldViewBinding>> targetMap, Class<BindView> annotation) {
        //得到整个app含有@BindView注解的类 Element代表类结构
        for (Element element : roundEnvironment.getElementsAnnotatedWith(annotation)) {
            //获取类名
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            //根据类名获取所有的注解
            List<FieldViewBinding> list = targetMap.get(enclosingElement);
            if (null == list) {
                list = new ArrayList<>();
                targetMap.put(enclosingElement, list);
            }
            //获取控件id 自定义注解@BindView的value返回值
            int id = element.getAnnotation(annotation).value();
            //获取成员变量的名--->titleText
            String fieldName = element.getSimpleName().toString();
            //获取成员变量类型信息---->TextView
            TypeMirror typeMirror = element.asType();
            FieldViewBinding fieldViewBinding = new FieldViewBinding(fieldName, typeMirror, id);
            list.add(fieldViewBinding);
        }
    }

    private void addClassAndMethod(Map<TypeElement, List<FieldViewBinding>> targetMap) {
        //遍历Map
        for (Map.Entry<TypeElement, List<FieldViewBinding>> item : targetMap.entrySet()) {
            List<FieldViewBinding> list = item.getValue();
            if (null == list || list.size() == 0) {
                continue;
            }
            //类类型 com.example....MainActivity
            TypeElement typeElement = item.getKey();
            //获取包名 com.example...
            String packageName = getPackageName(typeElement);
            //根据包名获取类名 MainActivity
            String className = getClassName(typeElement, packageName);
            //类型 <T extends MainActivity>
            ClassName name = ClassName.bestGuess(className);
            //获取我们定义的接口包名 和类名
            ClassName viewBinder = ClassName.get("com.example.injectlibrary", "ViewBinder");
            //生成java类 MainActivity$$ViewBinder
            TypeSpec.Builder result = TypeSpec.classBuilder(className + "$$ViewBinder")
                    .addModifiers(Modifier.PUBLIC)//将该类声明为public
                    .addTypeVariable(TypeVariableName.get("T", name))//声明该类的类型 <T extends MainActivity>
                    .addSuperinterface(ParameterizedTypeName.get(viewBinder, name));//该类的实现接口 以及接口类型
            //生成方法
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")//方法名 "bind"与ViewBinder接口中的回调保持一致
                    .addModifiers(Modifier.PUBLIC)//方法声明为public
                    .returns(TypeName.VOID)//方法返回值为void
                    .addAnnotation(Override.class)//方法注解 实现接口方法
                    .addParameter(name, "target", Modifier.FINAL);//参数类型（MainActivity） 参数名 参数修饰符
            //遍历该类下声明了@BindView注解的成员变量List集合
            for (int i = 0; i < list.size(); i++) {
                FieldViewBinding fieldViewBinding = list.get(i);
                //成员变量类型信息 --android.text.TextView
                String packageNameString = fieldViewBinding.getTypeMirror().toString();
                //得到成员变量的类名---TextView
                ClassName viewclassName = ClassName.bestGuess(packageNameString);
                //方法里面添加执行逻辑 $L $T 占位符 参数顺序一定要对 以及“target”一定要与上面的行参保持一致 代表的就是mainActivity
                //相当于：mainActivity.textview= (TextView) mainActivity.findViewById(R.id.textView);
                methodBuilder.addStatement("target.$L=($T)target.findViewById($L)", fieldViewBinding.getName(), viewclassName, fieldViewBinding.getResId());
            }
            result.addMethod(methodBuilder.build());//往类里面添加方法
      /*      try {
                //生成Java类信息 包名 类
             *//*   JavaFile.builder(packageName, result.build())
                        .addFileComment("auto create make")//类注释
                        .build()
                        .writeTo(filer);//写出*//*
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }
    /**
     * 获取类名(通过截取包名获取 若是内部类会将"."替换为"$")
     * @param typeElement
     * @param packageName
     */
    private String getClassName(TypeElement typeElement, String packageName) {
        int packageNameLength = packageName.length() + 1;
        return typeElement.getQualifiedName().toString().substring(packageNameLength).replace(".", "$");
    }

    /**
     * 获取包名
     * @param enclosingElement
     * @return
     */
    private String getPackageName(TypeElement enclosingElement) {
//        return elementUtil.getPackageOf(enclosingElement).getQualifiedName().toString();
        return null;
    }
}
