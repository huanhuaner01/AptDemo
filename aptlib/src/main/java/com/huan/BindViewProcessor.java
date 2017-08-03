package com.huan;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * 注解解析器，javac的时候会将相关的注解传入注解解析器进行解析
 *
 * @author huanzhang
 * @since 2.0.0
 */
@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {
    private Elements elementUtil;
    private Filer filer;
    /**
     * 支持bindview类型的注解
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(BindView.class.getCanonicalName());
    }


    /**
     * 初始化
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtil = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
    }

    /**
     * 处理注解的方法
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //定义一个map,装类名和类相关的组件信息
        Map<TypeElement, List<FieldViewBinding>> targetMap = new HashMap<>();

        setElemtBindView(roundEnvironment, targetMap, BindView.class);
        addClassAndMethod(targetMap);
        return false;
    }

    /**
     * 解析注解，封装到map中，key为类，value是类里面的相关的注解的成员变量及信息
     * @param roundEnvironment
     * @param targetMap
     * @param annotation
     */
    private void setElemtBindView(RoundEnvironment roundEnvironment, Map<TypeElement,
            List<FieldViewBinding>> targetMap, Class<BindView> annotation) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(annotation)) {
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            List<FieldViewBinding> list = targetMap.get(enclosingElement);
            if (null == list) {
                list = new ArrayList<>();
                targetMap.put(enclosingElement, list);
            }
            int id = element.getAnnotation(annotation).value();
            String fieldName = element.getSimpleName().toString();
            TypeMirror typeMirror = element.asType();
            FieldViewBinding fieldViewBinding = new FieldViewBinding(fieldName, typeMirror, id);
            list.add(fieldViewBinding);
        }
    }

    /**
     * 添加类和方法
     * @param targetMap
     */
    private void addClassAndMethod(Map<TypeElement, List<FieldViewBinding>> targetMap) {
        //遍历Map
        for (Map.Entry<TypeElement, List<FieldViewBinding>> item : targetMap.entrySet()) {
            List<FieldViewBinding> list = item.getValue();
            if (null == list || list.size() == 0) {
                continue;
            }
            TypeElement typeElement = item.getKey();
            String packageName = getPackageName(typeElement);
            String className = getClassName(typeElement, packageName);
            ClassName name = ClassName.bestGuess(className);
            ClassName viewBinder = ClassName.get("com.study.huanzhang.viewlib", "ViewBinder");
            TypeSpec.Builder result = TypeSpec.classBuilder(className + "$$ViewBinder")
                    .addModifiers(Modifier.PUBLIC)
                    .addTypeVariable(TypeVariableName.get("T", name))
                    .addSuperinterface(ParameterizedTypeName.get(viewBinder, name));
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC, Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    .addAnnotation(Override.class)
                    .addParameter(name, "target", Modifier.FINAL);
            for (int i = 0; i < list.size(); i++) {
                FieldViewBinding fieldViewBinding = list.get(i);
                String packageNameString = fieldViewBinding.getTypeMirror().toString();
                ClassName viewclassName = ClassName.bestGuess(packageNameString);
                methodBuilder.addStatement("target.$L=($T)target.findViewById($L)",
                        fieldViewBinding.getName(), viewclassName, fieldViewBinding.getResId());
            }
            result.addMethod(methodBuilder.build());
            try {
                JavaFile.builder(packageName, result.build())
                        .addFileComment("auto create make")
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getClassName(TypeElement typeElement, String packageName) {
        int packageNameLength = packageName.length() + 1;
        return typeElement.getQualifiedName().toString().substring(packageNameLength).replace(".", "$");
    }

    private String getPackageName(TypeElement enclosingElement) {
        return elementUtil.getPackageOf(enclosingElement).getQualifiedName().toString();
    }
}
