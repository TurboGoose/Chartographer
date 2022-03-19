package ru.turbo.goose;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.turbo.goose.utils.PathHolder;

@SpringBootApplication
public class ChartographerApp {
    public static void main(String[] args) {
        PathHolder.setPath(args[0]);
        SpringApplication.run(ChartographerApp.class, args);
    }
}
