package ioc;

import java.net.URL;

public class SimpleIOCTest {

    public static void main(String[] args) {

        String location = SimpleIOC.class.getResource("spring-test.xml").getFile();
        try {
            SimpleIOC ioc = new SimpleIOC(location);
            Wheel wheel = (Wheel) ioc.getBean("wheel");
            System.out.println(wheel);
            Car cat = (Car) ioc.getBean("car");
            System.out.println(cat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
