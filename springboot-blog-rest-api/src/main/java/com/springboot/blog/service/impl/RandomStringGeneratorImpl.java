package com.springboot.blog.service.impl;

import com.springboot.blog.service.RandomStringGenerator;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RandomStringGeneratorImpl implements RandomStringGenerator {
    @Override
    public String randomText(int size) {
        StringBuilder randomText = new StringBuilder();
        for(int i = 0; i < size; i++){
            randomText.append(generateRandomChar());
        }
        return randomText.toString();
    }

    private char generateRandomChar() {
        int value = new Random().nextInt(26);
        return (char) (value + (int) 'A');
    }
}
