package com.manu.ymlproject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestValue implements CommandLineRunner {
    final String name;

    public TestValue(@Value("${lalala}") String myvar) {
        this.name = myvar;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(this.name);
    }
}
