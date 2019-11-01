package ioc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangdong
 */
public class SimpleIOC {
    /**
     * 将根据配置文件生成的bean保存到容器中，此map充当重启
     */
    private Map<String, Object> beanMap = new HashMap<>();

    /**
     * 构造函数
     *
     * @param location
     */
    public SimpleIOC(String location) throws Exception {
        loadBeans(location);
    }

    /**
     * 获取bean
     *
     * @param name
     * @return
     */
    public Object getBean(String name) {
        Object bean = beanMap.get(name);
        if (null == bean) {
            throw new IllegalArgumentException("there is no bean with name" + name);
        }
        return bean;
    }

    /**
     * 根据配置文件加载bean
     *
     * @param location
     */
    private void loadBeans(String location) throws Exception {
        //-----加载xml配置文件
        InputStream inputStream = new FileInputStream(location);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        Element root = document.getDocumentElement();
        NodeList nodes = root.getChildNodes();

        //-----遍历 <bean> 标签
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                String id = ele.getAttribute("id");
                String className = ele.getAttribute("class");

                //-----加载beanClass
                Class beanClass = null;
                try {
                    beanClass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return;
                }

                //-----创建bean
                Object bean = beanClass.newInstance();

                //-----遍历 <property> 标签
                NodeList property = ele.getElementsByTagName("property");
                for (int j = 0; j < property.getLength(); j++) {
                    Node propertyNode = property.item(j);
                    if (propertyNode instanceof Element) {
                        Element propertyElement = (Element) propertyNode;
                        String name = propertyElement.getAttribute("name");
                        String value = propertyElement.getAttribute("value");

                        //-----利用反射将bean相关字段访问权限设置为可访问
                        Field declaredField = bean.getClass().getDeclaredField(name);
                        declaredField.setAccessible(true);
                        if (null != value && value.length() > 0) {
                            declaredField.set(bean, value);
                        } else {
                            String ref = propertyElement.getAttribute("ref");
                            if (null == ref || ref.length() == 0) {
                                throw new IllegalArgumentException("ref config error");
                            }
                            declaredField.set(bean, getBean(ref));
                        }
                        //-----将bean注册到bean容器中
                        registerBean(id, bean);
                    }
                }
            }
        }
    }

    private void registerBean(String id, Object bean) {
        beanMap.put(id, bean);
    }
}
