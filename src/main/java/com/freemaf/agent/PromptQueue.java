
package com.freemaf.agent;

import java.io.File;

import java.util.List;

import java.util.concurrent.BlockingQueue;

import java.util.concurrent.LinkedBlockingQueue;

public final class PromptQueue {

    private final BlockingQueue<Prompt> queue = new LinkedBlockingQueue<>();

    public void add(String prompt) {

        queue.add(Prompt.of(prompt));

    }

    public void add(Prompt prompt) {

        queue.add(prompt);

    }

    public void addUser(String text, List<File> files) {

        queue.add(Prompt.user(text, files));

    }

    public Prompt take() throws InterruptedException {

        return queue.take();

    }

}
