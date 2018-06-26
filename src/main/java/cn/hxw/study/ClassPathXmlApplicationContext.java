package cn.hxw.study;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @author huangxiaowei
 * @date 2018/6/26 09:42
 * @description
 */
public class ClassPathXmlApplicationContext implements Application {

    private String fileName;

    public ClassPathXmlApplicationContext(String fileName) {
        this.fileName = fileName;
    }

    public Object getBean(String beanID) {
        // 获取本类的当前目录
        String currentPath = this.getClass().getResource("").getPath().toString();

        // dom4j解释器
        SAXReader saxReader = new SAXReader();

        // xml文档
        Document document = null;

        //
        Object obj = null;

        try {
            document = saxReader.read(new File(currentPath + fileName));
            String xpath = "/beans/bean[@id='"+beanID+"']";
            Element element = (Element) document.selectSingleNode(xpath);
            String className = element.attributeValue("class");
            obj = Class.forName(className).newInstance();

            Element elementProperty = (Element) element.selectSingleNode("property");
            if(null != elementProperty){
                System.out.println("当前bean有属性需要注入");
                String propertyName = elementProperty.attributeValue("name");
                System.out.println("当前bean注入的属性为"+propertyName);

                // 拼接出注入方法
                String methodName = "set"
                        +(propertyName.substring(0, 1)).toUpperCase()
                        +propertyName.substring(1,propertyName.length());
                System.out.println("自动调用注入方法为"+methodName);

                String setObjectName = elementProperty.attributeValue("ref");
                System.out.println("需要注入的对象名为"+setObjectName);

                Object diObject = getBean(setObjectName);
                System.out.println("注入的对象实例"+diObject);

                Method[] methods = obj.getClass().getMethods();

                for(Method method : methods){
                    if(methodName.equals(method.getName())){
                        method.invoke(obj,diObject);
                    }
                }
            }else {
                System.out.println("当前bean没有属性，无需注入");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }
}
